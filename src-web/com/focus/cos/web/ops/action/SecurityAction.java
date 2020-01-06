package com.focus.cos.web.ops.action;

import java.io.IOException;
import java.net.URLDecoder;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.crypto.Cipher;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.json.JSONArray;
import org.json.JSONObject;

import com.focus.cos.api.AlarmSeverity;
import com.focus.cos.api.AlarmType;
import com.focus.cos.api.LogSeverity;
import com.focus.cos.api.Status;
import com.focus.cos.api.Sysalarm;
import com.focus.cos.api.SysalarmClient;
import com.focus.cos.api.Sysemail;
import com.focus.cos.api.SysemailClient;
import com.focus.cos.api.Syslog;
import com.focus.cos.api.SyslogClient;
import com.focus.cos.api.Sysnotify;
import com.focus.cos.api.SysnotifyClient;
import com.focus.cos.api.SyspublishClient;
import com.focus.cos.api.Sysuser;
import com.focus.cos.api.SysuserClient;
import com.focus.cos.web.action.GridAction;
import com.focus.cos.web.common.AjaxResult;
import com.focus.cos.web.common.Kit;
import com.focus.cos.web.common.ZKMgr;
import com.focus.cos.web.config.service.SysCfgMgr;
import com.focus.cos.web.ops.service.Monitor.RunFetchMonitor;
import com.focus.cos.web.ops.service.MonitorMgr;
import com.focus.cos.web.ops.service.SecurityMgr;
import com.focus.cos.web.user.service.RoleMgr;
import com.focus.cos.web.user.vo.User;
import com.focus.cos.web.util.RsaKeyTools;
import com.focus.util.Base64;
import com.focus.util.Base64X;
import com.focus.util.HttpUtils;
import com.focus.util.Item;
import com.focus.util.QuickSort;
import com.focus.util.Tools;
import com.focus.util.Zookeeper;
import com.opensymphony.xwork.ModelDriven;

/**
 * 系统安全管理
 * 处理平台在安全控制机制下对外能力开放
 * @author focus
 *
 */
public class SecurityAction extends GridAction implements ModelDriven
{
	private static final long serialVersionUID = -3807157813813837237L;
	private static final Log log = LogFactory.getLog(SecurityAction.class);
	/*身份令牌*/
//	private String token;
	/*用户请求的数据*/
	private String data;
	/*签名，用私钥进行验证*/
	private String signature;
	/*随机数*/
	private String nonce;
	/*时间戳*/
	private String ts;
	/*单点登录账号*/
	private String ssoAccount;
	/*安全令牌管理者*/
	private String admin;
	/*安全管理器*/
	private SecurityMgr securityMgr;
	/*安全回调URL地址*/
	private String securityCallbackUrl;
	/*监听管理器*/
	private MonitorMgr monitorMgr;
	/*私钥*/
	private String privatekey;
	/*令牌*/
	private String token;
	/*单点登录token*/
	private String sso_token;
	/**
	 * 打开测试页面
	 * @return
	 */
	public String openttest(){
		String zkpath = "/cos/config/security/"+id;
		JSONObject token;
		try {
			token = ZKMgr.getZookeeper().getJSONObject(zkpath);
			log.info(String.format("Open the test of %s from %s :%s", datatype, zkpath, token.toString(4)));
			this.timestamp = System.currentTimeMillis();
			this.token = token.getString("token");
			this.privatekey  = token.getString("privatekey");
			this.ssoAccount = super.getUserAccount();
			this.account = token.getString("account");
			ArrayList<Item> actions = new ArrayList<Item>();
			if( "sysuser".equals(datatype) ){
				actions.add(new Item("列表查询所有系统用户", "listUser"));
				actions.add(new Item("新增系统用户", "addUser"));
				actions.add(new Item("得到指定用户的信息", "getUser"));
				actions.add(new Item("得到指定用户的角色情况", "getRole"));
			}
			else if( "syslog".equals(datatype) ){
				actions.add(new Item("发送系统日志", "sendLog"));
			}
			else if( "sysnotify".equals(datatype) ){
				actions.add(new Item("发送系统通知", "sendNotify"));
			}
			else if( "sysalarm".equals(datatype) ){
				actions.add(new Item("发送系统告警", "sendAlarm"));
				actions.add(new Item("关闭系统告警", "closeAlarm"));
				actions.add(new Item("查询系统告警", "queryAlarm"));
			}
			else if( "sysemail".equals(datatype) ){
				actions.add(new Item("发送文字邮件", "sendEmail"));
				actions.add(new Item("发送富文本邮件", "sendHtml"));
				actions.add(new Item("发送多图邮件", "sendImages"));
				actions.add(new Item("发送快照邮件", "sendSnapshot"));
			}
			else if( "programpublish".equals(datatype) ){
				actions.add(new Item("添加系统程序", "addProgram"));
				actions.add(new Item("修改程序配置", "setProgram"));
				actions.add(new Item("删除系统程序", "delProgram"));

				ArrayList<JSONObject> servers = MonitorMgr.getInstance().getServers();
				for(JSONObject server : servers ){
					String ip = server.getString("ip");
					int port = server.getInt("port");
					RunFetchMonitor runner = MonitorMgr.getTracker().getRunFetchMonitor(ip, port);
					if( runner != null && runner.isConnect() ){
						localDataObject = new JSONObject();
						localDataObject.put("ip", ip);
						localDataObject.put("port", port);
						this.jsonData = localDataObject.toString();
//						System.err.println(localDataObject.toString(4));
						break;
					}
				}
			}
			else if( "synchfiles".equals(datatype) ){
				actions.add(new Item("同步集群文件", "synchfiles"));
			}
			else if( "configmonitor".equals(datatype) ){
				actions.add(new Item("新增伺服器监控", "addServer"));
				actions.add(new Item("新增伺服器集群", "addCluster"));
				actions.add(new Item("移除伺服器监控", "delServer"));
				actions.add(new Item("删除伺服器集群", "delCluster"));
				actions.add(new Item("重命名伺服集群", "renameCluster"));
			}
			else if( "reportmonitor".equals(datatype) ){
				actions.add(new Item("报告伺服器监控", "reportmonitor"));
				ArrayList<JSONObject> servers = MonitorMgr.getInstance().getServers();
				for(JSONObject server : servers ){
					String ip = server.getString("ip");
					int port = server.getInt("port");
					RunFetchMonitor runner = MonitorMgr.getTracker().getRunFetchMonitor(ip, port);
					if( runner != null && runner.isConnect() ){
						this.localDataObject = runner.getJsonMonitorData();
						this.jsonData = localDataObject.toString();
//						System.err.println(localDataObject.toString(4));
						break;
					}
				}
			}
			listData = actions;
		} catch (Exception e) {
			log.error(String.format("Failed to open the test of %s form %s for %s", datatype, zkpath, e.getMessage()));
		}
		if( jsonData == null ){
			jsonData = "{}";
		}
		if( "sso".equals(datatype) ){
			return "sso";
		}
		return "test";
	}
	/**
	 * 利用grid架构管理配置数据
	 * @return String
	 */
	public String manager()
	{
		try 
		{
			SecurityMgr.buildDefaultSecurity();
			String xmlpath = "/grid/local/syssecurity.xml";
	        return this.grid(xmlpath);
		}
		catch (Exception e) 
		{
			return "close";
		}		
	}

	/**
	 * 查看自己的私钥
	 * @return
	 */
	public String mykey(){

    	ServletOutputStream sos = null;
		try 
		{
			JSONObject token = ZKMgr.getZookeeper().getJSONObject("/cos/config/security/"+this.account);
			getResponse().setContentType("text/html");
			getResponse().setCharacterEncoding("UTF-8");
			sos = getResponse().getOutputStream();
			sos.print("<html><body style='padding: 1px;font-size:9pt;background:#000;color:#fff;word-break:break-all;overflow-x:hidden;'>");
			if( token != null && token.has("privatekey") ){
				String privatekey = token.getString("privatekey");
				if( !privatekey.isEmpty() ){
					sos.write(("<pre style='padding-top:5px;padding-bottom:5px;padding-left:5px;font-size:12px;color:#33ffff;'>"+privatekey+"</pre>").getBytes("UTF-8"));
				}
				else{
					sos.write(("<p style='padding-top:5px;padding-bottom:5px;padding-left:5px;font-size:14px;color:red;background:'>未生成自己的签名密钥key</p>").getBytes("UTF-8"));
				}
			}
			else{
				sos.write(("<p style='padding-top:5px;padding-bottom:5px;padding-left:5px;font-size:14px;color:red;background:'>安全令牌不存在或者未生成自己的签名密钥key</p>").getBytes("UTF-8"));
			}
			sos.flush();
		}
		catch (Exception e) 
		{
			try {
				sos.write(("<p style='padding-top:5px;padding-bottom:5px;padding-left:5px;font-size:14px;color:red;background:'>预览自己的签名密钥key出现异常"+e+"</p>").getBytes("UTF-8"));
			} catch (Exception e1) {
			}
		}
        finally
        {
        	if( sos != null )
        	{
	        	try
	        	{
	    			sos.print("</body><html>");
					sos.close();
	        	} catch (IOException e1) {
				}
        	}
        }
		return null;
	}
	/**
	 * 创建RSA加密
	 * @return
	 */
	public String creatersa(){

		try
		{
			JSONObject token = ZKMgr.getZookeeper().getJSONObject("/cos/config/security/"+this.account);
			if( token != null ){
				KeyPairGenerator keyPair = KeyPairGenerator.getInstance("RSA");
	            SecureRandom random = new SecureRandom();
	            keyPair.initialize(512, random);
	            KeyPair keyP = keyPair.generateKeyPair();
	            Key publicKey = keyP.getPublic();
	            Key privateKey = keyP.getPrivate();

	    		token.put("publickey", Base64X.encode(publicKey.getEncoded()));
	    		String privatekey = RsaKeyTools.getPemPrivateKey(privateKey);
	            token.put("privatekey", privatekey);
	            ZKMgr.getZookeeper().setJSONObject("/cos/config/security/"+this.account, token);
			}
			else{
				this.setResponseException("安全令牌账户【"+this.account+"】并不存在");
			}
		}
		catch(Exception e)
		{
			this.setResponseException("创建新的加密密钥出现异常"+e);
		}
        return this.grid("/grid/local/syssecurity.xml");
	}
	
	/**
	 * 通用的设置公钥私钥的接口
	 * @return
	 */
	public String setrsa()
	{
		JSONObject rsp = new JSONObject();
		HttpServletRequest request = super.getRequest();
		String zkpath = request.getParameter("zkpath");
		try{
//			String version = request.getParameter("version");
//			String remark = request.getParameter("remark");
//			String token = request.getParameter("token");
			if( zkpath == null ){
				throw new Exception("请求参数不存在");
			}
			log.info(String.format("Craete the rsa to %s", zkpath));
			ZooKeeper zk = ZKMgr.getZooKeeper();
			log.info(String.format("Get the instance of zookeeper(%s)", zk.getState().toString()));
			Stat stat = zk.exists(zkpath, false);
			log.info(String.format("Found stat(%s)", stat));
			if( stat != null ) {
				String json = new String(zk.getData(zkpath, false, stat), "UTF-8");
				log.info(String.format("Get the data from %s\r\n\t  %s", zkpath, json));
				JSONObject data = new JSONObject(json);
				KeyPairGenerator keyPair = KeyPairGenerator.getInstance("RSA");
	            SecureRandom random = new SecureRandom();
	            keyPair.initialize(512, random);
	            KeyPair keyP = keyPair.generateKeyPair();
	            Key publicKey = keyP.getPublic();
	            Key privateKey = keyP.getPrivate();

	            data.put("publickey", Base64X.encode(publicKey.getEncoded()));
	    		String privatekey = RsaKeyTools.getPemPrivateKey(privateKey);
	    		data.put("privatekey", privatekey);
	            zk.setData(zkpath, data.toString().getBytes("UTF-8"), stat.getVersion());
	            log.info(String.format("Succeed to create the rsa of %s %s", zkpath, privateKey));
	    		rsp.put("errcode", 0);
	    		rsp.put("errmsg", "OK");
	    		rsp.put("privatekey", privatekey);
			}
			else{
				throw new Exception("要设置的RSA节点不存在");
			}			
		}
		catch(Exception e){
			log.error("Failed to set the rsa to "+zkpath, e);
			rsp.put("errcode", 1);
			rsp.put("errmsg", "设置公钥私钥出现异常"+e);
		}
		finally{
		}
		return super.response(super.getResponse(), rsp.toString());
	}
	/**
	 * 验证访问令牌
	 * @param zookeeper
	 * @return
	 */
	private boolean oauth(Zookeeper zookeeper, String type)
	{
	    String ip = super.getRequest().getRemoteAddr();
	    String referer = super.getRequest().getHeader("referer");
	    logInfo("oauth(%s\r\n\ttimestamp=%s\r\n\tnonce=%s\r\n\tsignature=%s)\r\n from %s / %s", 
	    	this.account, this.ts, this.nonce, this.signature, referer, ip);
	    if (this.account == null) {
	    	super.setResponseException("没有安全令牌账户禁止通过该令牌访问系统服务。");
	    	return false;
	    }
	    if ((this.nonce == null) || (this.ts == null) || (this.signature == null)) {
	    	super.setResponseException("请求没有签名禁止通过该令牌访问系统服务。");
	      	return false;
	    }
	    if (Tools.isNumeric(this.ts)) {
	    	this.timestamp = Long.parseLong(this.ts);
	    }
	    else {
	    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    	try {
	    		this.timestamp = sdf.parse(this.ts).getTime();
	    	} catch (ParseException e) {
	    		super.setResponseException("服务请求的签名时间戳【" + this.ts + "】格式不正确，禁止通过该令牌访问系统服务。");
	    		return false;
	    	}
	    }
	    if (System.currentTimeMillis() - 86400000L > this.timestamp) {
	    	super.setResponseException("服务请求的签名时间戳过期，禁止通过该令牌访问系统服务。");
	      	return false;
	    }
	    if (this.securityMgr.checkNonce(this.nonce)) {
	    	super.setResponseException("服务请求的签名随机数【" + this.nonce + "】已经使用过，禁止通过该令牌访问系统服务。");
	      	return false;
	    }
	    JSONObject token = zookeeper.getJSONObject("/cos/config/security/" + this.account);
	    if (token == null) {
	    	super.setResponseException("安全令牌账户【" + this.account + "】无效。");
	      	return false;
	    }
	    if (!token.has("token")) {
	    	super.setResponseException("安全令牌账户【" + this.account + "】没有配置令牌，禁止通过该令牌访问系统服务。");
	      	return false;
	    }
	    
	    if( !type.equals(token.getString("type")) ){
	    	super.setResponseException("安全令牌账户【" + this.account + "】配置的接口类型是【"+token.getString("type")+"】，禁止通过该令牌访问系统服务【"+type+"】。");
	      	return false;
	    }
	    try
	    {
	    	byte[] publicKey = Base64X.decode(token.getString("publickey"));
	    	if (publicKey != null)
	    	{
	    		if (!RsaKeyTools.verify(token.getString("token") + this.ts + this.nonce, publicKey, RsaKeyTools.hexStringToByteArray(this.signature))) {
    				super.setResponseException("安全令牌账户【" + this.account + "】数字签名【" + this.signature + "】无效(时间戳:" + 
						this.timestamp + ",随机数:" + this.nonce + ")，禁止通过该令牌访问系统服务。");
    				return false;
	    		}
	    		if( ssoAccount != null ){
	    			byte[] payload = RsaKeyTools.hexStringToByteArray(this.ssoAccount);
	    			RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKey));
	    			//RSA解密
	    			Cipher cipher = Cipher.getInstance("RSA");
	    			cipher.init(Cipher.DECRYPT_MODE, pubKey);
	    			ssoAccount = new String(cipher.doFinal(payload));
	    		}
	    		if ((this.data != null) && ((this.data.startsWith("{")) || (this.data.startsWith("[")))) {
	    			this.localDataObject = new JSONObject(this.data);
	    		}
	    	}
	    	else {
	    		logError("Failed to confirm the signature by token: %s", token.toString(4));
	    		super.setResponseException("安全令牌账户【" + this.account + "】签名公钥不存在，禁止通过该令牌访问系统服务。");
	    		return false;
	      	}
	    }
	    catch (Exception e)
	    {
	    	logError(e, "Failed to confirm the signature by token: %s", token.toString(4));
	    	super.setResponseException("安全令牌账户【" + this.account + "】对签名进行验签发生异常[" + e + "]，禁止通过该令牌访问系统服务。");
	    	return false;
	    }
	    
	    if (!token.has("admin")) {
	    	super.setResponseException("安全令牌账户【" + this.account + "】没有配置管理用户，禁止通过该令牌访问系统服务。");
	    	return false;
	    }
	    this.admin = token.getString("admin");
	    if (token.has("host")) {
		    String host = token.getString("host");
		    if( referer != null && !host.isEmpty() && referer.indexOf(host) == -1) {
		    	super.setResponseException("安全令牌账户【" + this.account + "】禁止您通过【" + referer + "】访问系统服务。");
		    	return false;
		    }
	    }
	    
	    if (token.has("ip") ) {
	    	String _ip = token.getString("ip");
	    	if( _ip != null && !_ip.isEmpty() ){
			    if ("0:0:0:0:0:0:0:1".equals(ip)) {
			    	ip = "127.0.0.1";
			    }
			    if (!token.getString("ip").equals(ip)) {
			    	super.setResponseException("安全令牌账户【" + this.account + "】禁止您通过该地址【" + ip + "】访问系统服务。");
			    	return false;
			    }
	    	}
	    }
	    return true;
	}
	/**
	 * 安全回调的URL链接
	 * @return
	 */
    public String getSecurityCallbackUrl()
    {
    	return this.securityCallbackUrl;
    }
    /**
     * 跨域访问的的回调方法
     * @return
     */
    public String callback()
    {
    	ServletOutputStream sos = null;
		try 
		{
			String script = Kit.unicode2Chr(super.getRequest().getParameter("script"));
			log.info("securitycallback:"+script);
			getResponse().setContentType("text/html");
			getResponse().setCharacterEncoding("UTF-8");
			sos = getResponse().getOutputStream();
			sos.print("<html><body><script type='text/javascript'>");
			sos.write(script.getBytes("UTF-8"));
			sos.print("</script></body><html>");
			sos.flush();
		}
		catch (Exception e) 
		{
			log.error("Failed to callback", e);
		}
        finally
        {
        	if( sos != null )
        	{
	        	try
	        	{
					sos.close();
	        	} catch (IOException e1) {
				}
        	}
        }
    	return null;
    }
	/**
	 * 添加系统账号的方法
	 * @return
	 */
	//输出用户对象
	private User theuser;
	public String addsysuser(){
	    String referer = super.getRequest().getHeader("referer");
	    try
	    {
	    	this.securityCallbackUrl = (referer.substring(0, referer.indexOf("/", referer.indexOf("//") + 2)) + "/securitycallback/");
	    	trans.append("Receive the request, the headers and parameters is below ");
	    	HttpServletRequest req = super.getRequest();
	    	Enumeration<String> names = req.getHeaderNames();
	    	while (names.hasMoreElements())
	    	{
		        String key = (String)names.nextElement();
		        String value = req.getHeader(key);
		        if ("connection".equalsIgnoreCase(key))
		        	value = "keep-alive";
		        trans.append("\r\n\t" + key + " : " + value);
	    	}	      
	      
		    if ((referer != null) && (referer.indexOf("/addsysuser/") != -1)) return null;
		    if (!oauth(ZKMgr.getZookeeper(), "addsysuser")) {
		    	return "403";
		    }
		    Sysuser sysuser = SysuserClient.getUser(this.admin);
		    if (sysuser == null) {
		    	super.setResponseException("安全令牌账户【" + this.account + "】配置的管理用户不存在，禁止通过该令牌访问系统服务。");
		    	return "alert";
		    }
		    this.theuser = new User();
		    if (this.localDataObject.has("username"))
		    {
		    	this.theuser.setUsername(this.localDataObject.getString("username"));
		    }
		    if (this.localDataObject.has("realname"))
		    {
		    	this.theuser.setRealname(this.localDataObject.getString("realname"));
		    }
		    if (this.localDataObject.has("email"))
		    {
		    	this.theuser.setEmail(this.localDataObject.getString("email"));
		    }
		    if (this.localDataObject.has("password"))
		    {
		    	this.theuser.setPassword(DigestUtils.md5Hex(this.localDataObject.getString("password")));
		    }
		    if( this.localDataObject.has("roleid") ){//如果指定了角色权限组ID，那么默认添加的角色就是它
		    	this.theuser.setRoleid(localDataObject.getInt("roleid"));
		    }
		    JSONArray roles = new JSONArray();
		    JSONObject role = null;
		    role = ZKMgr.getZookeeper().getJSONObject("/cos/config/role");
		    if( this.theuser.getRoleid() == -1 ){
		    	if (role == null)
		    	{
		    		super.setResponseException("安全令牌账户【" + this.account + "】读取角色权限组配置数据，请先进行角色权限组管理。");
		    		return "alert";
		    	}
		    	role = RoleMgr.setRolesTree(RoleMgr.getMyRole(role, sysuser.getRoleid().byteValue()), sysuser.getRoleid().byteValue());
		    	if (role != null) {
		    		role.put("checked", false);
		    		roles.put(role);
		    		if (!isSysadmin()) {
		    			role.put("chkDisabled", true);
		    		}
		    	}
		    }
		    else{
		    	role = RoleMgr.getMyRole(role, this.theuser.getRoleid());
		    }
	    	super.logsecurity("用户通过安全令牌账户【" + this.account + "】打开了新增系统用户的界面，安全令牌账户关联的系统账户是" + this.admin + 
	    			",角色ID是" + sysuser.getRoleid(), "系统接口访问", role != null ? role.toString(4) : "未知角色数据", null);
		    this.jsonData = roles.toString();
		    super.getSession().setAttribute("account_security", localDataObject.has("creator")?localDataObject.getString("creator"):sysuser.getUsername());
		    this.listData = ZKMgr.getZookeeper().getJSONObjects("/cos/config/userpropcfg");
		    QuickSort sorter = new QuickSort() {
		    	public boolean compareTo(Object sortSrc, Object pivot) {
		    		JSONObject l = (JSONObject)sortSrc;
		    		JSONObject r = (JSONObject)pivot;
		    		if ((!l.has("sort")) || (!r.has("sort"))) return false;
		    		return l.getInt("sort") < r.getInt("sort");
		    	}
		    };
		    sorter.sort(this.listData);
		    String _inherit = this.localDataObject.has("inherit") ? this.localDataObject.getString("inherit") : null;
		    for (int i = 0; i < this.listData.size(); i++)
		    {
		    	JSONObject e = (JSONObject)this.listData.get(i);
		    	String id = e.getString("id");
		    	String value = this.localDataObject.has(id) ? this.localDataObject.getString(id) : null;
		    	if (value != null)
		    	{
		    		e.put("value", value);
		    	}
		    	if ((_inherit != null) && 
		    			(_inherit.indexOf(id) != -1)) {
		    		e.put("inherit", "checked='true'");
		    	}
		    }
		    this.account = null;
	    }
	    catch (Exception e)
	    {
	      super.setResponseException("安全令牌账户【" + this.account + "】打开新增系统用户界面失败，因为异常: " + e.toString());
	      return "403";
	    }
	    return "security-addaccount";
	}
	
	/**
	 * 配置监控
	 * @return
	 */
	public String configmonitor(){
		Interfacer i = new Interfacer("configmonitor", super.getRequest(), super.getResponse()){
			@Override
			public void handle(JSONObject response) {
				AjaxResult<String> result = null;
		    	int id = 0;
		    	if( "addServer".equalsIgnoreCase(localDataObject.getString("action")) )
		    	{
		    		result = monitorMgr.addServer(
	    				localDataObject.getInt("parent"),
	    				localDataObject.getString("ip"),
	    				localDataObject.getInt("port"),
	    				"json",
	    				System.currentTimeMillis());
		    		if( result.getResult() != null && !result.getResult().isEmpty() ){
		    			JSONObject json = new JSONObject(result.getResult());
		    			id = json.getInt("id");
		    		}
		    	}
		    	else if( "addCluster".equalsIgnoreCase(localDataObject.getString("action")) )
		    	{
		    		result = monitorMgr.addCluster(
	    				localDataObject.getInt("parent"),
	    				localDataObject.getString("name"),
	    				System.currentTimeMillis());
		    		if( result.getResult() != null && !result.getResult().isEmpty() ){
		    			JSONObject json = new JSONObject(result.getResult());
		    			id = json.getInt("id");
		    		}
		    	}
		    	else if( "delServer".equalsIgnoreCase(localDataObject.getString("action")) ) {
		    		result = monitorMgr.delServer(localDataObject.getInt("id"), 0, System.currentTimeMillis());
		    	}
		    	else if( "delCluster".equalsIgnoreCase(localDataObject.getString("action")) ) {
		    		result = monitorMgr.delCluster(
		    			localDataObject.getInt("id"), 
		    			localDataObject.has("delServer")?localDataObject.getBoolean("delServer"):true,
		    			0,
		    			System.currentTimeMillis());
		    	}
		    	else if( "renameCluster".equalsIgnoreCase(localDataObject.getString("action")) ) {
		    		result = monitorMgr.renameCluster(
		    			localDataObject.getInt("id"), 
		    			localDataObject.getString("name"),
		    			System.currentTimeMillis());
		    	}
		    	else{
		    		result = new AjaxResult<String>();
		    		result.setSucceed(false);
		    		result.setMessage("未知用户请求");
		    	}
		    	response.put("result", result.isSucceed()?1:0);
		    	response.put("message", result.getMessage());
		    	response.put("id", id);
			}
		};
		return i.execute();
	}
	/**
	 * 同步监控模块
	 * @return
	 */
	public String reportmonitor(){
		ServletOutputStream sos = null;
		HttpServletResponse rsp = super.getResponse();
		try
		{
			StringBuilder sb = new StringBuilder();
			sb.append("Receive the request of report monitor, the headers and parameters is below ");
	    	HttpServletRequest req = super.getRequest();
	    	Enumeration<String> names = req.getHeaderNames();
	    	while (names.hasMoreElements())
	    	{
		        String key = (String)names.nextElement();
		        String value = req.getHeader(key);
		        if ("connection".equalsIgnoreCase(key))
		        	value = "keep-alive";
		        sb.append("\r\n\t" + key + " : " + value);
	    	}	      
	    	names = req.getParameterNames();
	    	while (names.hasMoreElements())
	    	{
		        String key = (String)names.nextElement();
		        String value = req.getParameter(key);
		        sb.append("\r\n\t" + key + " = " + value);
	    	}
	    	this.logInfo(sb.toString());
    		sos = rsp.getOutputStream();
		    if (!oauth(ZKMgr.getZookeeper(), "reportmonitor")) {
		    	rsp.setStatus(401);
		    }
		    else if( this.localDataObject.has("servers") ){
		    	//正式处理请求数据
		    	logInfo("Get the monitor-data of servers %s", localDataObject.toString(4));
		    	JSONArray servers = this.localDataObject.getJSONArray("servers");
		    	for(int i = 0; i < servers.length(); i++ )
		    	{
		    		JSONObject server = servers.getJSONObject(i);
		    		String ip = server.getString("ip");
		    		int port = server.getInt("port");
					RunFetchMonitor runner = monitorMgr.getMonitor().getRunFetchMonitor(ip, port);
					if( runner != null )
					{
//						System.err.println(server.toString(4));
						runner.setJsonMonitorData(server);
					}
					else
					{//如果运行的监听器不就添加服务器
//						monitorMgr.addServer(0, ip, port, "json", System.currentTimeMillis());
						logError("Discard the server(%s:%s) for not found runner.",ip, port);
					}
		    	}
//		    	log.info(sb.toString());
//		    	rsp.setContentType("application/json;charset=utf8");
//		    	rsp.setHeader("Content-disposition", "inline; filename="+System.currentTimeMillis()+".json");
		    	rsp.setStatus(200);
		    	responseException = "监控数据上报成功";
		    }
		    else{
		    	rsp.setStatus(404);
		    	responseException = "未知请求";
		    }
		}
		catch (Exception e)
		{
			super.setResponseException("接收上报的监控数据请求发生异常:"+e.getMessage());
			rsp.setStatus(500);
			if( this.localDataObject != null ){
				logError(e, "Failed to handle synch of monitor: %s", this.localDataObject.toString(4));
			}
			else{
				logError(e, "Failed to handle synch of monitor: %s", data);
			}
		}
        finally
        {
        	if( sos != null )
	    		try
				{
	    			if( responseException != null ){
	    				sos.write(super.responseException.getBytes("UTF-8"));
				    	logInfo(responseException);
				    	if( rsp.getStatus() != 200 ){
				    		securityMgr.logoper(getRequest(), LogSeverity.INFO, 
			    				String.format("系统监控报告接口被调用，%s", responseException), "系统接口", trans.toString(), null);
				    	}
	    			}
	    			sos.flush();
	    			sos.close();
				}
				catch (IOException e)
				{
					log.error("", e);
				}
        }
    	securityMgr.writeLog(trans.append("\r\n").toString());
		return null;
	}

	/**
	 * 单点登录
	 * @return
	 */
	public String sso(){
	    String referer = super.getRequest().getHeader("referer");
	    try
	    {
	    	trans.append("\r\nReceive the request of sso form referer "+referer);
	    	HttpServletRequest req = super.getRequest();
	    	Enumeration<String> names = req.getHeaderNames();
	    	while (names.hasMoreElements())
	    	{
		        String key = (String)names.nextElement();
		        String value = req.getHeader(key);
		        if ("connection".equalsIgnoreCase(key))
		        	value = "keep-alive";
		        trans.append("\r\n\t" + key + " : " + value);
	    	}
	    	names = req.getParameterNames();
	    	while (names.hasMoreElements())
	    	{
		        String key = (String)names.nextElement();
		        String value = req.getParameter(key);
		        trans.append("\r\n\t" + key + " = " + value);
	    	}
	    	System.err.println(trans.toString());
	    	int i = account.indexOf('_');
	    	if( i == -1  ){
		    	super.setResponseException("安全令牌账户配置的不存在，禁止通过该令牌执行单点登录。");
		    	return "403";
	    	}
	    	this.ssoAccount = account.substring(i+1);
	    	account = account.substring(0, i);
		    if (!oauth(ZKMgr.getZookeeper(), "sso")) {
		    	return "403";
		    }
		    User user = this.securityMgr.findByAccount(this.ssoAccount);
		    if (user == null) {
		    	super.setResponseException("单点登录账户配置的账号不存在，禁止通过该令牌执行单点登录。");
		    	return "403";
		    }
			if( RoleMgr.isRoleAbort(user.getRoleid()) )
			{
				setResponseException("所属的角色权限组所有用户已经被禁止登录。");
		    	return "403";
			}
			JSONObject role = ZKMgr.getZookeeper().getJSONObject("/cos/config/role");
			if (role == null)
			{
				super.setResponseException("安全令牌账户读取角色权限组配置数据，，禁止通过该令牌执行单点登录。");
				return "40";
			}
			if( user.getStatus() == Status.Disable.getValue() )
			{
				setResponseException("该用户账号已经被停用。");
		    	return "403";
			}
			HttpSession session = super.getSession();
			long lastChangePasswordTime = user.getLastChangePasswordTime();
			if( lastChangePasswordTime == 0 ){
				session.setAttribute("forceResetPassword", "初始登录需要重置密码");
			}
			lastChangePasswordTime = user.getLastChangePasswordTime();
			long day = System.currentTimeMillis() - lastChangePasswordTime;
			day /= Tools.MILLI_OF_DAY;

			if(user.getErrorCount() > 0){
				user.setErrorCount(0);
			}
			String ip = Kit.getIp(super.getRequest());
			String region = HttpUtils.getIpRegion(ip);
			user.setToken();
			String lingpai = user.getToken();
			//将上次登录的时间放在session中，用于展示系统通知
			long lastLastLoginTime = user.getLastLoginTime();
			String loginRemark = "您的账号上次的登录时间是"+Tools.getFormatTime("yyyy-MM-dd HH:mm", lastLastLoginTime)+
					"，登录IP是"+user.getLastLoginIp()+"，登录区域是"+user.getLastLoginRegion()+"。"+
					"您本次登录时间是"+Tools.getFormatTime("yyyy-MM-dd HH:mm", System.currentTimeMillis())+
					"，登录IP是"+ip+"，登录区域是"+region+"。请随时确保您的账号安全。";
			if( lastLastLoginTime == 0 ){
				loginRemark = "这是您第一次登录，您本次登录时间是"+Tools.getFormatTime("yyyy-MM-dd HH:mm", System.currentTimeMillis())+
					"，登录IP是"+ip+"，登录区域是"+region+"。请随时确保您的账号安全。";
			}
			session.setAttribute("Timestamp-LastLogin", lastLastLoginTime);
			user.setLastLogin(new Date());
			user.setLastLoginIp(ip);
			user.setLastLoginRegion(region);
			this.securityMgr.updateSsoLogin(user);
			JSONObject cookie = new JSONObject();
			cookie = ZKMgr.getZookeeper().getJSONObject("/cos/user/properties/"+user.getUsername(), true);
			cookie = cookie!=null?cookie:new JSONObject();
			if( !cookie.has("sso_token") ){
				setResponseException("该账号没有配置单点登录。");
				return "403"; 
			}
			if( !cookie.getString("sso_token").equals(sso_token) ){
				setResponseException("该账号单点登录鉴权失败。");
				return "403"; 
			}
			cookie.put("id", user.getId());
			cookie.put("roleid", user.getRoleid());
			cookie.put("username", user.getUsername());
			if("admin".equals(user.getUsername()) )
				cookie.put("realname", "系统管理员");
			else if( user.getRealname() == null )
				cookie.put("realname", user.getUsername());
			else
				cookie.put("realname", user.getRealname());
			if( user.getEmail() != null )
				cookie.put("email", user.getEmail());
			cookie.put("token", user.getToken());
			cookie.put("lastLogin", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", user.getLastLogin().getTime()));
			cookie.put("lastLoginTimestamp", user.getLastLoginTime());
			if( securityMgr.setSsoLoginCookie(cookie, req.getCookies()) )
			{
				session.setAttribute("account_name", user.getUsername());
				session.setAttribute("account", cookie);
				logsecurity("用户账号【"+user.getRealname()+"】单点登录成功，登录IP是"+ip+"，登录区域是"+region+"。", "用户登录", null, null);
				this.viewTitle = this.getText("label.ema.login.home.page");
				try{
					Sysnotify notify = new Sysnotify();
					notify.setUseraccount(user.getUsername());
					notify.setFilter("用户账号安全");
					notify.setNotifytime(new Date());
					notify.setPriority(1);
					notify.setTitle("欢迎您"+(cookie.has("count")?("第"+cookie.getInt("count")+"次"):"首次")+"使用"+SysCfgMgr.get("SysName"));
					notify.setContext(loginRemark);
					SysnotifyClient.send( notify);
				}
				catch(Exception e){
					log.error("Failed to send the notify of system for ", e);
				}
			}
			else{
				session.invalidate();
				setResponseException("登录会话失效，请重新尝试登录");
			}
			super.getResponse().sendRedirect(Kit.URL_PATH(super.getRequest())+"main");
		    return null;
	    }
	    catch (Exception e)
	    {
	      super.setResponseException("安全令牌账户进行单点登录失败，因为异常: " + e.toString());
	      return "403";
	    }
	}
	/**
	 * 查询系统账号信息
	 * @return
	 */
	public String sysuser(){
		Interfacer i = new Interfacer("sysuser", super.getRequest(), super.getResponse()){
			@Override
			public void handle(JSONObject response) {
				// TODO Auto-generated method stub
				if( "addUser".equalsIgnoreCase(localDataObject.getString("action")) )
		    	{
		    		if( !localDataObject.has("email") ){
		    			response.put("message", "添加系统用户没有设置用户邮箱");
		    		}
		    		else if( !localDataObject.has("account") ){
		    			response.put("message", "添加系统用户没有设置用户账号");
		    		}
		    		else if( !localDataObject.has("realname") ){
		    			response.put("message", "添加系统用户没有设置用户实名");
		    		}
		    		else{
			    		Sysuser user = new Sysuser();
			    		user.setEmail(localDataObject.getString("email"));
			    		user.setUsername(localDataObject.getString("account"));
			    		user.setRealname(localDataObject.getString("realname"));
			    		user.setRoleid((byte)localDataObject.getInt("role"));
			    		user.setPassword(localDataObject.getString("password"));
			    		if( localDataObject.has("sex") ){
			    			user.setSex((byte)localDataObject.getInt("sex"));
			    		}
			    		try{
			    			SysuserClient.add(user);
			    			response.put("id", user.getId());
			    			response.put("result", true);
			    		}
			    		catch(Exception e){
			    			response.put("message", e.getMessage());
			    		}
		    		}
		    	}
		    	else if( "listUser".equalsIgnoreCase(localDataObject.getString("action")) )
		    	{
		    		int role = localDataObject.has("role")?localDataObject.getInt("role"):-1;
		    		int sex = localDataObject.has("sex")?localDataObject.getInt("sex"):-1;
		    		int status = localDataObject.has("status")?localDataObject.getInt("status"):-1;
		    		ArrayList<Sysuser> users = SysuserClient.listUser(role, sex, status);
		    		JSONArray data = new JSONArray();
		    		for(Sysuser user : users){
		    			JSONObject e = new JSONObject();
		    			e.put("account", user.getUsername());
		    			e.put("realname", user.getRealname());
		    			e.put("email", user.getEmail());
		    			e.put("creator", user.getCreator());
		    			e.put("id", user.getId());
		    			e.put("sex", user.getSex());
		    			e.put("status", user.getStatus());
		    			e.put("role", user.getRoleid());
		    			
		    			data.put(e);
		    		}
		    		try {
		    			String path;
						Zookeeper zk = ZKMgr.getZookeeper();
						for(int i = 0; i < data.length(); i++){
							JSONObject e = data.getJSONObject(i);
							path = "/cos/user/properties/"+e.getString("account");
							JSONObject p = zk.getJSONObject(path, true);
							if( p != null ){
								Iterator<?> iterator = p.keys();
								while(iterator.hasNext()){
									String key = iterator.next().toString();
									Object val = p.get(key);
									e.put(key, val);
								}
							}
						}
					} catch (Exception e1) {
					}
	    			response.put("result", true);
		    		response.put("data", data);
		    	}
		    	else if( "getUser".equalsIgnoreCase(localDataObject.getString("action")) ) {
		    		if( !localDataObject.has("account") ){
		    			response.put("message", "没有提供查找用户的账号");
		    		}
		    		else{
		    			Sysuser user = SysuserClient.getUser(localDataObject.getString("account"));
			    		if( user == null ){
			    			response.put("message", "查找用户信息失败，应该是接口系统配置错误，请联系系统管理员");
			    		}
			    		else{
			    			response.put("result", true);
			    			JSONObject e = new JSONObject();
			    			e.put("account", user.getUsername());
			    			e.put("realname", user.getRealname());
			    			e.put("email", user.getEmail());
			    			e.put("creator", user.getCreator());
			    			e.put("id", user.getId());
			    			e.put("sex", user.getSex());
			    			e.put("status", user.getStatus());
			    			e.put("role", user.getRoleid());
				    		response.put("data", e);
				    		try {
				    			String path;
								Zookeeper zk = ZKMgr.getZookeeper();
								path = "/cos/user/properties/"+e.getString("account");
								JSONObject p = zk.getJSONObject(path, true);
								if( p != null ){
									Iterator<?> iterator = p.keys();
									while(iterator.hasNext()){
										String key = iterator.next().toString();
										Object val = p.get(key);
										e.put(key, val);
									}
								}
							} catch (Exception e1) {
							}
			    		}
		    		}
		    	}
		    	else if( "getRole".equalsIgnoreCase(localDataObject.getString("action")) ) {
		    		if( !localDataObject.has("account") ){
		    			response.put("message", "没有提供查找用户的账号");
		    		}
		    		else{
		    			try{
				    		JSONObject data = SysuserClient.getRole(localDataObject.getString("account"));
			    			response.put("result", true);
			    			response.put("data", data);
		    			}
		    			catch(Exception e){
//			    			response.put("message", "查找用户角色权限信息失败，应该是接口系统配置错误，请联系系统管理员");
			    			response.put("message", e.getMessage());
		    			}
		    		}
		    	}
		    	else{
	    			response.put("message", "未知用户请求["+localDataObject.getString("action")+"]");
		    	}
			}
		};
		return i.execute();
	}
	
	/**
	 * 记录系统日志
	 * @return
	 */
	public String syslog(){

		Interfacer i = new Interfacer("syslog", super.getRequest(), super.getResponse()){
			@Override
			public void handle(JSONObject response) {
				// TODO Auto-generated method stub

	    		if( !localDataObject.has("type") ){
	    			response.put("message", "您没有设置发送日志的类型");
	    		}
	    		else if( !localDataObject.has("severity") ){
	    			response.put("message", "您没有设置发送日志的级别");
	    		}
	    		else if( !localDataObject.has("category") ){
	    			response.put("message", "您没有填写发送日志的类别描述");
	    		}
	    		else if( !localDataObject.has("text") ){
	    			response.put("message", "您没有填写发送日志的内容");
	    		}
	    		else{
	    			Syslog syslog = new Syslog();
	    			syslog.setAccount(localDataObject.has("account")?localDataObject.getString("account"):"");
		    		syslog.setLogtext(localDataObject.has("text")?localDataObject.getString("text"):"");
		    		syslog.setLogseverity(localDataObject.has("severity")?localDataObject.getInt("severity"):2);
		    		syslog.setLogtype(localDataObject.has("type")?localDataObject.getInt("type"):0);
		    		syslog.setCategory(localDataObject.has("category")?localDataObject.getString("category"):"");
		    		syslog.setContext(localDataObject.has("context")?localDataObject.getString("context"):"");
		    		syslog.setContextlink(localDataObject.has("contextlink")?localDataObject.getString("contextlink"):"");
		    		try{
		    			SyslogClient.write(syslog);
		    			response.put("result", true);
		    			response.put("id", syslog.getLogid());
		    			response.put("message", "成功提交系统日志");
		    		}
		    		catch(Exception e){
		    			response.put("message", e.getMessage());
		    		}
	    		}
			}
			
		};
		return i.execute();
	}
	
	/**
	 * 系统通知
	 * @return
	 */
	public String sysnotify(){
		Interfacer i = new Interfacer("sysnotify", super.getRequest(), super.getResponse()){
			@Override
			public void handle(JSONObject response) {
				// TODO Auto-generated method stub

	    		if( !localDataObject.has("account") ){
	    			response.put("message", "您没有设置发送通知的接收用户账号");
	    		}
	    		else if( !localDataObject.has("title") ){
	    			response.put("message", "您没有设置发送通知的标题");
	    		}
	    		else if( !localDataObject.has("filter") ){
	    			response.put("message", "您没有填写发送通知的类别描述");
	    		}
	    		else if( !localDataObject.has("context") ){
	    			response.put("message", "您没有填写发送通知的内容");
	    		}
	    		else{
	    			Sysnotify notify = new Sysnotify();
	    			notify.setUseraccount(localDataObject.has("account")?localDataObject.getString("account"):"");
	    			notify.setTitle(localDataObject.has("title")?localDataObject.getString("title"):"");
	    			notify.setFilter(localDataObject.has("filter")?localDataObject.getString("filter"):"");
	    			notify.setContext(localDataObject.has("context")?localDataObject.getString("context"):"");
	    			notify.setContextlink(localDataObject.has("contextlink")?localDataObject.getString("contextlink"):"");
	    			notify.setAction(localDataObject.has("action")?localDataObject.getString("action"):"");
	    			notify.setActionlink(localDataObject.has("actionlink")?localDataObject.getString("actionlink"):"");
		    		try{
		    			SysnotifyClient.send(notify);
		    			response.put("result", true);
		    			response.put("id", notify.getNid());
		    			response.put("message", "成功发送系统通知");
		    		}
		    		catch(Exception e){
		    			response.put("message", e.getMessage());
		    		}
	    		}
			}
			
		};
		return i.execute();
	}
	
	/**
	 * 发送系统告警
	 * @return
	 */
	public String sysalarm(){
		Interfacer i = new Interfacer("sysalarm", super.getRequest(), super.getResponse()) {
			@Override
			public void handle(JSONObject response) {
		    	if( "sendAlarm".equalsIgnoreCase(localDataObject.getString("action")) )
		    	{
		    		if( !localDataObject.has("category") ){
		    			response.put("message", "没有填写告警类别: B(业务告警),Q(服务质量告警),S(系统告警),E(环境告警),D(设备告警);");
		    		}
		    		else if( !localDataObject.has("severity") ){
		    			response.put("message", "没有填写告警级别: BLACK(致命),RED(严重),ORANGE(重要),YELLOW(次要),BLUE(警告);");
		    		}
		    		else if( !localDataObject.has("type") ){
		    			response.put("message", "需要设置告警类型，用户自定义各种告警的唯一标识");
		    		}
		    		else if( !localDataObject.has("subsys") ){
		    			response.put("message", "需要设置产生告警的所属子系统的标识，例如Sys");
		    		}
		    		else if( !localDataObject.has("dn") ){
		    			response.put("message", "需要设置产生告警的物理网元IP地址");
		    		}
		    		else if( !localDataObject.has("title") ){
		    			response.put("message", "需要设置告警标题");
		    		}
		    		else if( !localDataObject.has("text") ){
		    			response.put("message", "需要设置告警描述");
		    		}
		    		else if( !localDataObject.has("cause") ){
		    			response.put("message", "需要设置告警原因");
		    		}
		    		else{
			    		Sysalarm alarm = new Sysalarm();
			    		alarm.setSysid(localDataObject.has("subsys")?localDataObject.getString("subsys"):"");
			    		alarm.setSeverity(localDataObject.has("severity")?localDataObject.getString("severity"):"");
			    		alarm.setType(localDataObject.has("category")?localDataObject.getString("category"):"");
			    		alarm.setId(localDataObject.has("type")?localDataObject.getString("type"):"");
			    		alarm.setDn(localDataObject.has("dn")?localDataObject.getString("dn"):"");
			    		alarm.setTitle(localDataObject.has("title")?localDataObject.getString("title"):"");
			    		alarm.setText(localDataObject.has("text")?localDataObject.getString("text"):"");
			    		alarm.setCause(localDataObject.has("cause")?localDataObject.getString("cause"):"");
			    		try{
			    			SysalarmClient.send(alarm);
			    			response.put("result", true);
			    			response.put("id", alarm.getAlarmid());
			    			response.put("message", "成功发送告警");
			    		}
			    		catch(Exception e){
			    			response.put("message", e.getMessage());
			    		}
		    		}
		    	}
		    	else if( "closeAlarm".equalsIgnoreCase(localDataObject.getString("action")) )
		    	{
		    		if( !localDataObject.has("type") ){
		    			response.put("message", "需要设置关闭告警的类型，用户自定义的");
		    		}
		    		else if( !localDataObject.has("subsys") ){
		    			response.put("message", "需要设置关闭告警的所属子系统的标识，例如Sys");
		    		}
		    		else if( !localDataObject.has("remark") ){
		    			response.put("message", "需要设置关闭告警的描述");
		    		}
		    		else{
			    		try{
			    			List<Sysalarm> list = SysalarmClient.close(localDataObject.getString("subsys"), localDataObject.getString("type"), localDataObject.getString("remark"));
			    			response.put("result", true);
			    			JSONArray data = new JSONArray();
			    			for(Sysalarm alarm : list){
			    				JSONObject e = new JSONObject();
			    				e.put("id", alarm.getAlarmid());
			    				e.put("category", alarm.getType());
			    				e.put("severity", alarm.getSeverity());
			    				e.put("type", alarm.getId());
			    				e.put("subsys", alarm.getSysid());
			    				e.put("dn", alarm.getDn());
			    				e.put("cause", alarm.getCause());
			    				e.put("title", alarm.getTitle());
			    				e.put("text", alarm.getText());
			    				e.put("event_time", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", alarm.getEventTime().getTime()));
			    				e.put("close_remark", alarm.getAckRemark());
			    				e.put("contact", alarm.getContact());
			    				e.put("responser", alarm.getResponser());
			    				data.put(e);
			    			}
			    			response.put("data", data);
			    			response.put("message", String.format("成功关闭%s个告警", data.length()));
			    		}
			    		catch(Exception e){
			    			response.put("message", e.getMessage());
			    		}
		    		}
		    	}
		    	else if( "queryAlarm".equalsIgnoreCase(localDataObject.getString("action")) ) {
		    		if( !localDataObject.has("category") ){
		    			response.put("message", "查询告警没有填写告警类型: B(业务告警),Q(服务质量告警),S(系统告警),E(环境告警),D(设备告警);");
		    		}
		    		else if( !localDataObject.has("severity") ){
		    			response.put("message", "查询告警没有填写告警级别: BLACK(致命),RED(严重),ORANGE(重要),YELLOW(次要),BLUE(警告);");
		    		}
		    		else if( !localDataObject.has("subsys") ){
		    			response.put("message", "查询告警需要设置告警的所属子系统的标识，例如Sys");
		    		}
		    		else if( !localDataObject.has("range") ){
		    			response.put("message", "需要设置告警状态查询范围: 0是未关闭告警，1是已关闭告警，-1所有告警");
		    		}
		    		else{
			    		try{
			    			AlarmSeverity severity = AlarmSeverity.get(localDataObject.getString("severity"));
			    			AlarmType type = AlarmType.get(localDataObject.getString("category"));
			    			String sysid = localDataObject.getString("subsys");
			    			String dn = localDataObject.getString("dn");
			    			Boolean activing = localDataObject.getInt("range")==0;
			    			List<Sysalarm> list = SysalarmClient.query(severity, type, sysid, dn, activing);
			    			response.put("result", true);
			    			JSONArray data = new JSONArray();
			    			for(Sysalarm alarm : list){
			    				JSONObject e = new JSONObject();
			    				e.put("id", alarm.getAlarmid());
			    				e.put("category", alarm.getType());
			    				e.put("severity", alarm.getSeverity());
			    				e.put("type", alarm.getId());
			    				e.put("subsys", alarm.getSysid());
			    				e.put("dn", alarm.getDn());
			    				e.put("cause", alarm.getCause());
			    				e.put("title", alarm.getTitle());
			    				e.put("text", alarm.getText());
			    				e.put("event_time", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", alarm.getEventTime().getTime()));
			    				e.put("contact", alarm.getContact());
			    				e.put("responser", alarm.getResponser());
			    				e.put("closed", alarm.getActiveStatus()==0);
			    				if( alarm.getAckTime() != null ){
			    					e.put("close_time", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", alarm.getAckTime().getTime()));
			    					e.put("close_remark", alarm.getAckRemark());
			    				}
			    				data.put(e);
			    			}
			    			response.put("data", data);
			    			response.put("message", "成功查询告警");
			    		}
			    		catch(Exception e){
			    			response.put("message", e.getMessage());
			    		}
		    		}
		    	}
		    	else{
	    			response.put("message", "未知用户请求["+localDataObject.getString("action")+"]");
		    	}
			}
		};
		return i.execute();
	}
	
	/**
	 * 发送系统邮件
	 * @return
	 */
	public String sysemail(){
		Interfacer i = new Interfacer("sysemail", super.getRequest(), super.getResponse()){
			@Override
			public void handle(JSONObject response) {
				// TODO Auto-generated method stub

	    		if( !localDataObject.has("to") ){
	    			response.put("message", "您没有填写发送的邮件地址");
	    		}
	    		else if( !localDataObject.has("subject") ){
	    			response.put("message", "您没有填写邮件标题");
	    		}
	    		else if( !localDataObject.has("content") ){
	    			response.put("message", "您没有填写邮件内容");
	    		}
	    		else{
				    Sysemail outbox = new Sysemail();
	    			outbox.setMailTo(localDataObject.getString("to"));
	    			outbox.setSubject(localDataObject.getString("subject"));
	    			outbox.setSysid(localDataObject.has("subsys")?localDataObject.getString("subsys"):"");
			    	String content = localDataObject.has("content")?localDataObject.getString("content"):"";
	    			if( "sendHtml".equalsIgnoreCase(localDataObject.getString("action")) ){
			    		try{
//			    			content = Tools.replaceStr(content, " ", "+");
			    			byte[] payload = Base64.decode(content);
			    			content = new String(payload, "UTF-8");
			    		}
			    		catch(Exception e){
			    		}
			    		outbox.setContent("html=="+content);
			    	}
			    	else if( "sendSnapshot".equalsIgnoreCase(localDataObject.getString("action")) )
			    	{
			    		outbox.setContent("snapshot=="+content);
			    	}
			    	else if( "sendImages".equalsIgnoreCase(localDataObject.getString("action")) ) {
			    		outbox.setContent("images=="+content);
			    	}
			    	else{
			    		outbox.setContent((localDataObject.has("content")?localDataObject.getString("content"):""));
			    	}
		    		try{
		    			String attachment = localDataObject.has("attachment")?localDataObject.getString("attachment"):"";
		    			if( !attachment.isEmpty() ){
		    				attachment = URLDecoder.decode(attachment, "UTF-8");
		    			}
			    		outbox.setAttachments(attachment);
		    			SysemailClient.send(outbox);
		    			response.put("result", true);
		    			response.put("id", outbox.getEid());
		    			response.put("message", "成功提交邮件到发件箱");
		    		}
		    		catch(Exception e){
		    			response.put("message", e.getMessage());
		    		}
	    		}
			}
			
		};
		return i.execute();
	}
	/**
	 * 系统程序发布接口
	 * @return
	 */
	public String programpublish(){
		Interfacer i = new Interfacer("programpublish", super.getRequest(), super.getResponse()){
			@Override
			public void handle(JSONObject response) {
				// TODO Auto-generated method stub
	    		if( !localDataObject.has("id") ){
	    			response.put("message", "没有设置程序的唯一标识");
	    		}
	    		else if( !localDataObject.has("oper") ){
	    			response.put("message", "没有填写操作吗: 0新增程序；1修改程序；删除程序");
	    		}
	    		else if( !localDataObject.has("operuser") ){
	    			response.put("message", "没有填写操作用户, 由接口调用者填写谁调用了该接口");
	    		}
	    		else if( !localDataObject.has("operlog") ){
	    			response.put("message", "没有填写操作描述");
	    		}
	    		else if( !localDataObject.has("ip") ){
	    			response.put("message", "没有指定发布程序到伺服器的IP地址");
	    		}
	    		else if( !localDataObject.has("port") ){
	    			response.put("message", "没有指定主控端口");
	    		}
	    		else{
	    			String ip = localDataObject.getString("ip");
					int port = localDataObject.getInt("port");
					RunFetchMonitor runner = MonitorMgr.getTracker().getRunFetchMonitor(ip, port);
					if( runner != null && runner.isConnect() ){
						localDataObject.put("serverkey", runner.getSysDesc().getServerid());
			    		try{
			    			SyspublishClient.submit(localDataObject);
			    			response.put("result", true);
			    			response.put("message", "成功提交程序发布到系统请联系系统管理员审核");
			    		}
			    		catch(Exception e){
			    			response.put("message", e.getMessage());
			    		}
					}
					else{
		    			response.put("message", "发布程序的伺服器没有启动或者主控链接不可用");
					}
	    		}
			}
			
		};
		return i.execute();
	}
	
	/**
	 * 集群文件同步接口
	 * @return
	 */
	public String synchfiles(){
		return null;
	}

	public void setData(String data) {
		this.data = data;
	}

	public User getTheuser() {
		return theuser;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public void setNonce(String nonce) {
		this.nonce = nonce;
	}

	public void setTs(String ts) {
		this.ts = ts;
	}

	public void setSecurityMgr(SecurityMgr securityMgr) {
		this.securityMgr = securityMgr;
	}

	public void setMonitorMgr(MonitorMgr monitorMgr) {
		this.monitorMgr = monitorMgr;
	}

	public String getToken() {
		return token;
	}
	public String getPrivatekey() {
		return privatekey;
	}

	public String getAdmin() {
		return admin;
	}
	

	/**
	 * 接口消息处理器
	 * @author think
	 *
	 */
	abstract class Interfacer{
		protected HttpServletRequest req;
		protected HttpServletResponse rsp;
		protected HttpSession session;
		protected String type;
		public Interfacer(String type, HttpServletRequest req, HttpServletResponse rsp){
			session = req.getSession();
			this.req = req;
			this.rsp = rsp;
			this.type = type;
		}
		
		public abstract void handle(JSONObject response);
		
		public String execute(){

			ServletOutputStream sos = null;
			try
			{
		        String remote = req.getRemoteAddr();
		        String uri = req.getRequestURI();
//		    	System.out.println(remote+"@"+uri);
				StringBuilder sb = new StringBuilder("Receive the request("+req.getMethod()+") of "+type);
		    	sb.append("\r\n\t-------------------");
		    	Enumeration<String> names = req.getHeaderNames();
		    	while (names.hasMoreElements())
		    	{
			        String key = (String)names.nextElement();
			        String value = req.getHeader(key);
			        if ("connection".equalsIgnoreCase(key))
			        	value = "keep-alive";
			        sb.append("\r\n\t" + key + " : " + value);
		    	}
		    	sb.append("\r\n\t-------------------");
		    	names = req.getParameterNames();
		    	while (names.hasMoreElements())
		    	{
			        String key = (String)names.nextElement();
			        String value = req.getParameter(key);
			        sb.append("\r\n\t" + key + " = " + value);
		    	}
		    	logInfo(sb.toString());
			    if (!oauth(ZKMgr.getZookeeper(), type)) {
			    	rsp.setStatus(401);
			    }
			    else{
				    Sysuser sysuser = SysuserClient.getUser(getAdmin());
				    if (sysuser == null) {
				    	setResponseException("安全令牌账户【" + account + "】配置的管理用户不存在，禁止通过该令牌访问系统服务。");
				    	rsp.setStatus(403);
				    }
				    else{
					    session.setAttribute("account_security", sysuser.getUsername());
				    	//正式处理请求数据
				    	JSONObject response = new JSONObject();
		    			response.put("result", false);
		    			handle(response);
				    	rsp.setStatus(200);
					    String referer = req.getHeader("referer");
				    	if( referer != null && referer.indexOf("security!openttest.action") != -1 ){
//				    		messageCode = "json";
				    		setEditorType("json");
				    		setEditorContent(response.toString(4));
//				    		System.err.println(response.toString(4));
				    		return "xml_json_css_sql";
				    	}
				    	return response(rsp, response.toString());
				    }
			    }
			}
			catch (Exception e)
			{
				e.printStackTrace();
				setResponseException("处理系统接口请求发生异常:"+e.getMessage());
				rsp.setStatus(500);
				logError(e, "Failed to handle %s for exception.", type);
			}
	        finally
	        {
	        	if( rsp.getStatus() != 200 ){
		    		try
					{
		    			sos = rsp.getOutputStream();
		    			if ( sos != null ){
		    				if( responseException != null ){
		    					sos.write(responseException.getBytes("UTF-8"));
						    	logError(getResponseException());
				        		securityMgr.logoper(getRequest(), LogSeverity.ERROR, 
					        			String.format("系统接口[%s]调用出错，%s", type, responseException), "系统接口", trans.toString(), null);
		    				}
		    				sos.flush();
		    				sos.close();
		    			}
					}
					catch (IOException e)
					{
						log.error("", e);
					}
	        	}
	        	else{
//	        		securityMgr.logoper(getRequest(), LogSeverity.INFO, 
//	        			String.format("系统接口[%s]被成功调用", type), "系统接口", trans.toString(), null);
			    	logInfo("Succeed to handle request.");
	        	}
	        	securityMgr.writeLog(trans.append("\r\n").toString());
	        }
			return null;
		}
	}
	public String getSsoAccount() {
		return ssoAccount;
	}
	public void setSso_token(String sso_token) {
		this.sso_token = sso_token;
	}
	public String getSso_token() {
		return sso_token;
	}
}
