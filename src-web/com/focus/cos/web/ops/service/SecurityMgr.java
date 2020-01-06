package com.focus.cos.web.ops.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.HashMap;

import javax.crypto.Cipher;
import javax.servlet.http.Cookie;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.json.JSONObject;

import com.focus.cos.web.common.AjaxResult;
import com.focus.cos.web.common.PathFactory;
import com.focus.cos.web.common.ZKMgr;
import com.focus.cos.web.service.SvrMgr;
import com.focus.cos.web.user.dao.UserDAO;
import com.focus.cos.web.user.vo.User;
import com.focus.cos.web.util.RsaKeyTools;
import com.focus.util.Base64X;
import com.focus.util.Tools;

/**
 * 安全管理处理安全请求的随机数
 * @author think
 *
 */
public class SecurityMgr extends SvrMgr implements Runnable 
{
	private static final Log log = LogFactory.getLog(SecurityMgr.class);
	HashMap<String, Boolean> previewable = new HashMap<String, Boolean>();

	private HashMap<String, Nonce> nonces = new HashMap<String, Nonce>();

	private ArrayList<Nonce> cach = new ArrayList<Nonce>();

	private long timestamp;

	private boolean running;
	/*用户对象*/
	private UserDAO userDao;
	class Nonce {
		String value;

		long timestamp;

		Nonce(String v) {
			this.value = v;
			this.timestamp = System.currentTimeMillis();
		}
	}

	public User findByAccount(String account)
		throws Exception
	{
		return userDao!=null?(User)userDao.findByAccount(account):null; 
	}
	//更新单点登录用户数据
	public void updateSsoLogin(User user)
	{
		if( userDao == null )
		{
			return;
		}
		userDao.attachDirty(user);
	}
	/**
	 * 设置登录cookie
	 * @param user
	 * @param cookie
	 * @param cookies
	 * @return 是否登录
	 */
	public boolean setSsoLoginCookie(JSONObject cookie, Cookie[] cookies)
	{
		if( cookie.has("password") ){
			cookie.remove("password");
		}
		ZooKeeper zookeeper = null;
		String path;
		boolean logon = false;
		try
		{
			zookeeper = ZKMgr.getZooKeeper();
			path = "/cos/login/user/"+cookie.getString("username");
			Stat stat = zookeeper.exists(path, false);
			if( stat == null)
			{
				cookie.put("count", 1);
				zookeeper.create(path, cookie.toString().getBytes("UTF-8"), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			else
			{
				cookie.put("count", stat.getVersion());
				zookeeper.setData(path, cookie.toString().getBytes("UTF-8"), stat.getVersion());
			}
			logon = true;
			if( cookies != null )
				for( Cookie c : cookies )
				{
					String name = c.getName();// get the cookie name
					String value = c.getValue(); // get the cookie value
					if( "COSSESSIONID".equalsIgnoreCase(name) )
					{
						path = "/cos/login/cookie";
						stat = zookeeper.exists(path, false); 
						if( stat == null)
						{
							zookeeper.create(path, "记录登录用户的节点".getBytes("UTF-8"), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
						}
						path = "/cos/login/cookie/"+value;
						cookie.put("sessionid", value);
						stat = zookeeper.exists(path, false); 
						if( stat == null)
						{
							zookeeper.create(path, cookie.toString().getBytes("UTF-8"), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
						}
						else
						{
							zookeeper.setData(path, cookie.toString().getBytes("UTF-8"), stat.getVersion());
						}
						logon = true;
						break;
					}
				}
		}
		catch(Exception e)
		{
			logon = false;
			log.warn("Failed to set cookie to zookeeper for "+e);
		}
		return logon;
	}
	/**
	 * 每小时清理一次随机数，同时如果发现日志队列有数据就把日志写入文件
	 */
	public void run() {
		this.running = true;
		long lastClearNonce = 0;
		while (this.running) {
			synchronized (this) {
				if( System.currentTimeMillis() - lastClearNonce > Tools.MILLI_OF_HOUR ){
					long yesterday = System.currentTimeMillis() - 86400000L;
					StringBuilder sb = new StringBuilder("Release the cach of nonces("
							+ this.nonces.size() + ")");
					int c = 0;
					for (int i = 0; i < this.cach.size(); i++) {
						Nonce nonce = (Nonce) this.cach.get(i);
						if (yesterday <= nonce.timestamp)
							break;
						this.cach.remove(i);
						c++;
						i--;
						this.nonces.remove(nonce.value);
						sb.append(String.format(
								"\r\n\t[%s] %s",
								new Object[] {
										nonce.value,
										Tools.getFormatTime("yyyy-MM-dd HH:mm:ss",
												nonce.timestamp) }));
					}

					sb.append(String.format("%s cach of noce has been release.",
							new Object[] { Integer.valueOf(c) }));
					log.info(sb);
					lastClearNonce = System.currentTimeMillis();
				}
				
				try {
					wait(3600000L);
				} catch (InterruptedException localInterruptedException) {
				}
			}
		}
		this.running = false;
	}

	public synchronized void close() {
		this.running = false;
		this.cach.clear();
		this.nonces.clear();
		notifyAll();
	}

	public synchronized boolean checkNonce(String nonce) {
		if (this.nonces.containsKey(nonce))
			return true;
		if (this.timestamp == 0L) {
			this.timestamp = System.currentTimeMillis();
			Thread thread = new Thread(this);
			thread.start();
		}
		log.info("Cach the nonce(" + nonce + ") into the nonces("
				+ this.nonces.size() + ").");
		Nonce n = new Nonce(nonce);
		this.nonces.put(nonce, n);
		this.cach.add(n);
		return false;
	}
	
	/**
	 * 构建缺失的安全配置，用于各种接口调用
	 * @throws Exception 
	 */
	public static void buildDefaultSecurity()
	{
//		{
//		    "host": "localhost",
//		    "token": "12345",
//		    "remark": "独守空房sdfsdfsdf",
//		    "admin": "liuxue",
//		    "_timestamp": 1521028690747,
//		    "account": "3lvb5qlnmod",
//		    "type": "griddigg",
//		    "publickey": "hcttKVlfa1nduP/89VzjjV9K6t9t69fj9Zfgzh5x+U00XM0duwmN1Wi8yE6N0w54MPaXBuFChAlaC46LvCcJbTy1uAzcdKnipW0qbW20DanGY4JIqbMbEIhS9tz99V==",
//		    "privatekey": "-----BEGIN RSA PRIVATE KEY-----\r\nMIIBOwIBAAJBALJfEM6rGVttR1tIh5e4oWaNqlS4t56H1vKRphgiMnYKiHS8xiF7\r\nbUqohnEFIDZazWtybWutkKZmPH7/yb1bl/MCAwEAAQJAfitagyVp0U7yG3KpXrud\r\nhyIL6tOiJoPlmj1GJGoEEZPdD5jJG3x5vfvXASC84+AhCZBdp9Q24v+vGjnU5fSR\r\noQIhAP1gpyf/0CzfiSZmlDD4dswYRAHuAoD6/D6KFGd2cwVDAiEAtDetSRBIBk3b\r\nOrRMnpFYLItrNt4EfIDIpnSXXEq7n5ECIQDprew77af22qpIqi5eA6i8jlyaUwIo\r\nJlHg88Phr9JgtQIgNcEVsuFkWl0Gsy/sWq/HuSGszSTT+b8AGoZUT3cLF8ECIQDv\r\nr1Pe76p1v0DY6d7lZ4Kw18Zv4oTGmcwyoNxsvUP7+g==\r\n-----END RSA PRIVATE KEY-----\r\n",
//		    "ip": "127.0.0.1"
//		}
		JSONObject security = null;
		String zkpath = null;
		try{

			zkpath = "/cos/config/security/_sysuser";
			if( ZKMgr.getZookeeper().exists(zkpath) == null ){
				security = new JSONObject();
				security.put("token", Tools.getUniqueValue());
				security.put("remark", "为上层应用开发提供系统用户操作接口，包括新增用户、查询用户、查看用户、获取用户角色权限信息");
				security.put("admin", "admin");
				security.put("account", "_sysuser");
				security.put("type", "sysuser");

				KeyPairGenerator keyPair = KeyPairGenerator.getInstance("RSA");
	            SecureRandom random = new SecureRandom();
	            keyPair.initialize(512, random);
	            KeyPair keyP = keyPair.generateKeyPair();
	            Key publicKey = keyP.getPublic();
	            Key privateKey = keyP.getPrivate();
	    		String privatekey = RsaKeyTools.getPemPrivateKey(privateKey);
				security.put("publickey", Base64X.encode(publicKey.getEncoded()));
				security.put("privatekey", privatekey);
				ZKMgr.getZookeeper().createObject(zkpath, security);
				log.info("Succed to build the default config of security "+security.toString(4));
			}
			zkpath = "/cos/config/security/_sysemail";
			if( ZKMgr.getZookeeper().exists(zkpath) == null ){
				security = new JSONObject();
				security.put("token", Tools.getUniqueValue());
				security.put("remark", "为上层应用开发提供系统邮件发送接口");
				security.put("admin", "admin");
				security.put("account", "_sysemail");
				security.put("type", "sysemail");

				KeyPairGenerator keyPair = KeyPairGenerator.getInstance("RSA");
	            SecureRandom random = new SecureRandom();
	            keyPair.initialize(512, random);
	            KeyPair keyP = keyPair.generateKeyPair();
	            Key publicKey = keyP.getPublic();
	            Key privateKey = keyP.getPrivate();
	    		String privatekey = RsaKeyTools.getPemPrivateKey(privateKey);
				security.put("publickey", Base64X.encode(publicKey.getEncoded()));
				security.put("privatekey", privatekey);
				ZKMgr.getZookeeper().createObject(zkpath, security);
				log.info("Succed to build the default config of security "+security.toString(4));
			}
			zkpath = "/cos/config/security/_syslog";
			if( ZKMgr.getZookeeper().exists(zkpath) == null ){
				security = new JSONObject();
				security.put("token", Tools.getUniqueValue());
				security.put("remark", "为上层应用开发提供系统日志发送接口");
				security.put("admin", "admin");
				security.put("account", "_syslog");
				security.put("type", "syslog");

				KeyPairGenerator keyPair = KeyPairGenerator.getInstance("RSA");
	            SecureRandom random = new SecureRandom();
	            keyPair.initialize(512, random);
	            KeyPair keyP = keyPair.generateKeyPair();
	            Key publicKey = keyP.getPublic();
	            Key privateKey = keyP.getPrivate();
	    		String privatekey = RsaKeyTools.getPemPrivateKey(privateKey);
				security.put("publickey", Base64X.encode(publicKey.getEncoded()));
				security.put("privatekey", privatekey);
				ZKMgr.getZookeeper().createObject(zkpath, security);
				log.info("Succed to build the default config of security "+security.toString(4));
			}
			zkpath = "/cos/config/security/_sysnotify";
			if( ZKMgr.getZookeeper().exists(zkpath) == null ){
				security = new JSONObject();
				security.put("token", Tools.getUniqueValue());
				security.put("remark", "为上层应用开发提供系统通知发送接口");
				security.put("admin", "admin");
				security.put("account", "_sysnotify");
				security.put("type", "sysnotify");

				KeyPairGenerator keyPair = KeyPairGenerator.getInstance("RSA");
	            SecureRandom random = new SecureRandom();
	            keyPair.initialize(512, random);
	            KeyPair keyP = keyPair.generateKeyPair();
	            Key publicKey = keyP.getPublic();
	            Key privateKey = keyP.getPrivate();
	    		String privatekey = RsaKeyTools.getPemPrivateKey(privateKey);
				security.put("publickey", Base64X.encode(publicKey.getEncoded()));
				security.put("privatekey", privatekey);
				ZKMgr.getZookeeper().createObject(zkpath, security);
				log.info("Succed to build the default config of security "+security.toString(4));
			}
			zkpath = "/cos/config/security/_sysalarm";
			if( ZKMgr.getZookeeper().exists(zkpath) == null ){
				security = new JSONObject();
				security.put("token", Tools.getUniqueValue());
				security.put("remark", "为上层应用开发提供系统告警发送接口");
				security.put("admin", "admin");
				security.put("account", "_sysalarm");
				security.put("type", "sysalarm");

				KeyPairGenerator keyPair = KeyPairGenerator.getInstance("RSA");
	            SecureRandom random = new SecureRandom();
	            keyPair.initialize(512, random);
	            KeyPair keyP = keyPair.generateKeyPair();
	            Key publicKey = keyP.getPublic();
	            Key privateKey = keyP.getPrivate();
	    		String privatekey = RsaKeyTools.getPemPrivateKey(privateKey);
				security.put("publickey", Base64X.encode(publicKey.getEncoded()));
				security.put("privatekey", privatekey);
				ZKMgr.getZookeeper().createObject(zkpath, security);
				log.info("Succed to build the default config of security "+security.toString(4));
			}
			zkpath = "/cos/config/security/_programpublish";//发布程序
			if( ZKMgr.getZookeeper().exists(zkpath) == null ){
				security = new JSONObject();
				security.put("token", Tools.getUniqueValue());
				security.put("remark", "为上层应用开发提供系统程序发布接口");
				security.put("admin", "admin");
				security.put("account", "_programpublish");
				security.put("type", "programpublish");

				KeyPairGenerator keyPair = KeyPairGenerator.getInstance("RSA");
	            SecureRandom random = new SecureRandom();
	            keyPair.initialize(512, random);
	            KeyPair keyP = keyPair.generateKeyPair();
	            Key publicKey = keyP.getPublic();
	            Key privateKey = keyP.getPrivate();
	    		String privatekey = RsaKeyTools.getPemPrivateKey(privateKey);
				security.put("publickey", Base64X.encode(publicKey.getEncoded()));
				security.put("privatekey", privatekey);
				ZKMgr.getZookeeper().createObject(zkpath, security);
				log.info("Succed to build the default config of security "+security.toString(4));
			}
			zkpath = "/cos/config/security/_synchfiles";//拷贝程序
			if( ZKMgr.getZookeeper().exists(zkpath) == null ){
				security = new JSONObject();
				security.put("token", Tools.getUniqueValue());
				security.put("remark", "为上层应用开发提供集群文件同步接口");
				security.put("admin", "admin");
				security.put("account", "_synchfiles");
				security.put("type", "synchfiles");

				KeyPairGenerator keyPair = KeyPairGenerator.getInstance("RSA");
	            SecureRandom random = new SecureRandom();
	            keyPair.initialize(512, random);
	            KeyPair keyP = keyPair.generateKeyPair();
	            Key publicKey = keyP.getPublic();
	            Key privateKey = keyP.getPrivate();
	    		String privatekey = RsaKeyTools.getPemPrivateKey(privateKey);
				security.put("publickey", Base64X.encode(publicKey.getEncoded()));
				security.put("privatekey", privatekey);
				ZKMgr.getZookeeper().createObject(zkpath, security);
				log.info("Succed to build the default config of security "+security.toString(4));
			}
			
			zkpath = "/cos/config/security/_monitorreport";
			if( ZKMgr.getZookeeper().exists(zkpath) == null ){
				security = new JSONObject();
				security.put("token", Tools.getUniqueValue());
				security.put("remark", "为上层应用开发提供集群监控上报接口");
				security.put("admin", "admin");
				security.put("account", "_monitorreport");
				security.put("type", "reportmonitor");

				KeyPairGenerator keyPair = KeyPairGenerator.getInstance("RSA");
	            SecureRandom random = new SecureRandom();
	            keyPair.initialize(512, random);
	            KeyPair keyP = keyPair.generateKeyPair();
	            Key publicKey = keyP.getPublic();
	            Key privateKey = keyP.getPrivate();
	    		String privatekey = RsaKeyTools.getPemPrivateKey(privateKey);
				security.put("publickey", Base64X.encode(publicKey.getEncoded()));
				security.put("privatekey", privatekey);
				ZKMgr.getZookeeper().createObject(zkpath, security);
				log.info("Succed to build the default config of security "+security.toString(4));
			}
			zkpath = "/cos/config/security/_monitorconfig";
			if( ZKMgr.getZookeeper().exists(zkpath) == null ){
				security = new JSONObject();
				security.put("token", Tools.getUniqueValue());
				security.put("remark", "为上层应用开发提供集群监控配置接口");
				security.put("admin", "admin");
				security.put("account", "_monitorconfig");
				security.put("type", "configmonitor");

				KeyPairGenerator keyPair = KeyPairGenerator.getInstance("RSA");
	            SecureRandom random = new SecureRandom();
	            keyPair.initialize(512, random);
	            KeyPair keyP = keyPair.generateKeyPair();
	            Key publicKey = keyP.getPublic();
	            Key privateKey = keyP.getPrivate();
	    		String privatekey = RsaKeyTools.getPemPrivateKey(privateKey);
				security.put("publickey", Base64X.encode(publicKey.getEncoded()));
				security.put("privatekey", privatekey);
				ZKMgr.getZookeeper().createObject(zkpath, security);
				log.info("Succed to build the default config of security "+security.toString(4));
			}
			zkpath = "/cos/config/security/_diggapi";
			if( ZKMgr.getZookeeper().exists(zkpath) == null ){
				security = new JSONObject();
				security.put("token", Tools.getUniqueValue());
				security.put("remark", "为上层应用开发提供元数据模板查询接口");
				security.put("admin", "admin");
				security.put("account", "_diggapi");
				security.put("type", "griddigg");

				KeyPairGenerator keyPair = KeyPairGenerator.getInstance("RSA");
	            SecureRandom random = new SecureRandom();
	            keyPair.initialize(512, random);
	            KeyPair keyP = keyPair.generateKeyPair();
	            Key publicKey = keyP.getPublic();
	            Key privateKey = keyP.getPrivate();
	    		String privatekey = RsaKeyTools.getPemPrivateKey(privateKey);
				security.put("publickey", Base64X.encode(publicKey.getEncoded()));
				security.put("privatekey", privatekey);
				ZKMgr.getZookeeper().createObject(zkpath, security);
				log.info("Succed to build the default config of security "+security.toString(4));
			}
		}
		catch(Exception e )
		{
			log.error("Failed to build the default config of security to zookeeper for exception", e);
		}
	}
	/**
	 * 写日志
	 * @param info
	 * @param args
	 */
	public synchronized void writeLog(String trans){
		OutputStreamWriter out = null;
		try
		{
			File dir = new File(PathFactory.getAppPath(), "log/COSPortal");
			if (!dir.exists())
			{
				dir.mkdirs();
			}
			String filename = "cosapi_"+Tools.getFormatTime("yyyy-MM-dd", System.currentTimeMillis()) + ".txt";
			File file = new File(dir, filename);
			out = new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8");
			out.write(trans);
		}
		catch (Exception e)
		{
			log.error("Failed to write the log of cos-api.", e);
		}
		finally
		{
			if (out != null)
				try {
					out.close();
				} catch (IOException e) {
				}
		}
	}
	
	/**
	 * 账号加密
	 * @param privateKey
	 * @param cosAccount
	 * @param apiAccount
	 * @return 加密账号
	 */
	public AjaxResult<String> encryptSsoAccount(String privateKey, String cosAccount, String apiAccount){
		AjaxResult<String> result = new AjaxResult<String>();
		try{
			PEMParser parser = new PEMParser(new StringReader(privateKey));
			//从解析器读取PEM密钥对
			PEMKeyPair pari = (PEMKeyPair)parser.readObject();
			parser.close();//关闭解析器
			//从密钥对中得到私钥码流
			byte[] pk = pari.getPrivateKeyInfo().getEncoded();
			RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(pk));
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, priKey);
			String out = RsaKeyTools.bytes2String(cipher.doFinal(cosAccount.getBytes("UTF-8")));
			result.setResult(apiAccount+"_"+out);
			result.setSucceed(true);
		}
		catch(Exception e){
			result.setMessage("加密单点登录账号异常"+e);
		}
		return result;
	}
	
	public void setUserDao(UserDAO userDao)
	{
		this.userDao = userDao;
	}
}