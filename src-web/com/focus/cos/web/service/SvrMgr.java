package com.focus.cos.web.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.data.Stat;
import org.bson.Document;
import org.directwebremoting.WebContextFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import com.focus.cos.api.LogSeverity;
import com.focus.cos.api.LogType;
import com.focus.cos.api.Status;
import com.focus.cos.api.Syslog;
import com.focus.cos.api.SyslogClient;
import com.focus.cos.api.Sysnotify;
import com.focus.cos.api.SysnotifyClient;
import com.focus.cos.api.Sysuser;
import com.focus.cos.api.SysuserClient;
import com.focus.cos.control.Command;
import com.focus.cos.web.common.Kit;
import com.focus.cos.web.common.PathFactory;
import com.focus.util.Tools;
import com.focus.util.Zookeeper;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

/**
 * 
 * @author focus
 *
 */
public class SvrMgr
{
	private static final Log log = LogFactory.getLog(SvrMgr.class);
	//时间戳
	protected long timestamp;
	protected static HashMap<String, MongoClient> Mongos = new HashMap<String, MongoClient>();
	private static HashMap<String, Long> Timestamps = new HashMap<String, Long>();

	/**
	 * 导出模块
	 * @param zookeeper
	 * @param zkpath
	 * @param oldpath
	 * @param newpath
	 * @param zos
	 * @throws Exception
	 */
	public void exportModule(Zookeeper zookeeper, String oldId, String newId, ZipOutputStream zos) throws Exception{
		String zkpath = "/cos/config/modules/"+oldId;
		Stat stat = zookeeper.exists(zkpath);
		if( stat == null ) {
			return;
		}
		byte[] payload = zookeeper.getData(zkpath);
		JSONObject cfg = new JSONObject(new String(payload, "UTF-8"));
		String filename = zkpath;
		if( newId != null ){
			cfg.put("id", newId);
			filename = "/cos/config/modules/"+newId;
			payload = cfg.toString().getBytes("UTF-8");
		}
		filename = Kit.chr2Unicode(filename);
		ZipEntry entry = new ZipEntry(filename);
		entry.setTime(stat.getMtime());
		zos.putNextEntry(entry);
		zos.write(payload); 
		zos.flush();
		zos.closeEntry();
		List<String> list = zookeeper.getChildren(zkpath);
		if( newId != null ){
			for(String name : list){
				exportZookeeper(zookeeper, zkpath+"/"+name, "/"+oldId+"/", "/"+newId+"/", zos);
			}
		}
		else{
			for(String name : list){
				this.exportZookeeper(zookeeper, zkpath+"/"+name, zos);
			}
		}
	}
	/**
	 * 导出配置数据
	 * @param zkpath
	 * @param zos
	 * @param zookeeper
	 * @throws Exception 
	 */
	public void exportZookeeper(Zookeeper zookeeper, String zkpath, String oldpath, String newpath, ZipOutputStream zos) throws Exception{
		Stat stat = zookeeper.exists(zkpath);
		if( stat == null ) {
			return;
		}
		if(!"/".equals(zkpath)){
			byte[] payload = zookeeper.getData(zkpath);
			String filename = Kit.chr2Unicode(zkpath.replaceAll(oldpath, newpath));
			if( payload == null ){
				payload = new byte[0];
				filename = "_"+filename;
			}
			ZipEntry entry = new ZipEntry(filename);
			entry.setTime(stat.getMtime());
			zos.putNextEntry(entry);
			zos.write(payload); 
			zos.flush();
			zos.closeEntry();
		}
		List<String> list = zookeeper.getChildren(zkpath);
		if( zkpath.equals("/") ){
			zkpath = "";
		}
		for(String name : list){
			exportZookeeper(zookeeper, zkpath+"/"+name, oldpath, newpath, zos);
		}
	}
	/**
	 * 导出配置数据
	 * @param zkpath
	 * @param zos
	 * @param zookeeper
	 * @throws Exception 
	 */
	public void exportZookeeper(Zookeeper zookeeper, String zkpath, ZipOutputStream zos) throws Exception{
		Stat stat = zookeeper.exists(zkpath);
		if( stat == null ) {
			return;
		}
		if(!"/".equals(zkpath)){

			byte[] payload = zookeeper.getData(zkpath);
			String filename = Kit.chr2Unicode(zkpath);
			if( payload == null ){
				payload = new byte[0];
				filename = "_"+filename;
			}
			ZipEntry entry = new ZipEntry(filename);
			entry.setTime(stat.getMtime());
			zos.putNextEntry(entry);
			zos.write(payload); 
			zos.flush();
			zos.closeEntry();
			/*
			byte[] payload = zookeeper.getData(zkpath);
//		ZipEntry entry = new ZipEntry(Tools.encodeMD5(zkpath));
			ZipEntry entry = new ZipEntry(Kit.chr2Unicode(zkpath));
			entry.setTime(stat.getMtime());
			zos.putNextEntry(entry);
//		byte[] header = zkpath.getBytes("UTF-8");
//		zos.write(Tools.intToBytes(header.length));
//		zos.write(header);
			zos.write(payload); 
			zos.flush();
			zos.closeEntry();
			*/
		}
		List<String> list = zookeeper.getChildren(zkpath);
		if( zkpath.equals("/") ){
			zkpath = "";
		}
		for(String name : list){
			exportZookeeper(zookeeper, zkpath+"/"+name, zos);
		}
	}
	/**
	 * 释放
	 * @param host
	 * @param port
	 * @param database
	 */
    public static void release(String host, int port, String database)
    {
		String url = database+"@"+host+":"+port;
		MongoClient client = Mongos.remove(url);
		if( client != null )
		{
			client.close();
		}
    }
    
    public String getTs(){
    	return String.valueOf(System.currentTimeMillis());
    }

    /**
     * 产生随机数
     * @return
     */
    public String getNonce()
    {
      return Tools.getUniqueValue();
    }
	/**
	 * 获得数据库表操作句柄
	 * @param address
	 * @param port
	 * @param username
	 * @param password
	 * @param database
	 * @param tablename
	 * @return
	 * @throws Exception
	 */
	public static MongoCollection<Document> getMongoCollection(String host, int port, String username, String password, String database, String databse1, String tablename)
		throws Exception
	{
		if( databse1.isEmpty() ) databse1 = database;
		MongoClient mongo = null;
		String url = database+"@"+host+":"+port;
		synchronized(Mongos)
		{
			try
			{
				mongo = Mongos.get(url);
				MongoDatabase db = null;
				if( mongo != null )
				{
					if( Tools.MILLI_OF_MINUTE > System.currentTimeMillis() - Timestamps.get(url) )
					{
						try
						{
							db = mongo.getDatabase(databse1);
							if( db == null )
							{
								mongo.close();
								mongo = null;
								Mongos.remove(url);
							}
							Timestamps.put(url, System.currentTimeMillis());
						}
						catch(Exception e)
						{
							mongo.close();
							mongo = null;
							Mongos.remove(url);
						}
					}
					else
					{
						mongo.close();
						mongo = null;
						Mongos.remove(url);
					}
				}
				if( mongo == null )
				{
					List<MongoCredential> credentialsList = new ArrayList<MongoCredential>();
//					password = new String(Base64X.decode(password));
					MongoCredential credential = MongoCredential.createScramSha1Credential(username, database, password.toCharArray());
					credentialsList.add(credential);
					ServerAddress serverAddress = new ServerAddress(host, port); 
					mongo = new MongoClient(serverAddress, credentialsList);
					Mongos.put(url, mongo);
					Timestamps.put(url, System.currentTimeMillis());
					log.info("Connect mongo("+url+":"+mongo.getReadPreference().getName()+").");
				}
				if( db == null )
				{
					db = mongo.getDatabase(databse1);
				}
				return db.getCollection(tablename);
			}
			catch(Exception e)
			{
				if( mongo != null ){
					mongo.close();
					Mongos.remove(url);
				}
				log.error("Failed to get client of mongo for", e);
				throw new Exception("芒果数据库("+url+", "+username+")连接异常"+e.getMessage());
			}
		}
	}
	/**
	 * 
	 * @param text
	 * @param category
	 * @param severity
	 * @return
	 */
	public Syslog logoper(
			LogSeverity severity,
			String text,
			String category)
	{
		String contextlink = "";
		String context = "";
		return this.logoper(null, severity, text, category, context, contextlink);
	}
	
	public Syslog logoper(
			String text,
			String category,
			Exception exception)
	{
		String contextlink = "";
		String context = "";
		if( exception != null )
		{
			contextlink = "syslog!showexception.action";
			ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
			PrintStream ps = new PrintStream(out);
			exception.printStackTrace(ps);
			context = out.toString();
		}
		return this.logoper(null, exception==null?LogSeverity.INFO:LogSeverity.ERROR, text, category, context, contextlink);
	}
	
	public Syslog logoper(
			String text,
			String category,
			String context,
			String contextlink)
	{
		return this.logoper(null, LogSeverity.INFO, text, category, context, contextlink);
	}
	
	public Syslog logoper(javax.servlet.http.HttpServletRequest request,
			LogSeverity severity,
			String text,
			String category,
			String context,
			String contextlink)
	{
		if( request == null )
		{
			org.directwebremoting.WebContext web = WebContextFactory.get();
			if( web == null )
				return null;
			request = web.getHttpServletRequest();
		}
	    String account = (String)request.getSession().getAttribute("account_security");
	    if (account == null) {
	      account = (String)request.getSession().getAttribute("account_name");
	    }
		if( account == null )
		{
			return null;
		}
		return SyslogClient.write(
				LogType.操作日志,
				severity,
				text,
				account,
				category,
				context,
				contextlink);
	}
	
	public JSONObject getAccount()
	{
		org.directwebremoting.WebContext web = WebContextFactory.get();
		if( web == null )
			return null;   
	    javax.servlet.http.HttpServletRequest request = web.getHttpServletRequest();
		JSONObject account = (JSONObject) request.getSession().getAttribute("account");
		return account;
	}

	public int getAccountRole()
	{
		org.directwebremoting.WebContext web = WebContextFactory.get();
		if( web == null )
			return -1;
	    javax.servlet.http.HttpServletRequest request = web.getHttpServletRequest();
		JSONObject account = (JSONObject) request.getSession().getAttribute("account");
		return account!=null&&account.has("roleid")?account.getInt("roleid"):-1;
	}

	public String getAccountName()
	{
		org.directwebremoting.WebContext web = WebContextFactory.get();   
		if( web == null )
			return null;
		javax.servlet.http.HttpServletRequest request = web.getHttpServletRequest();
		JSONObject account = (JSONObject) request.getSession().getAttribute("account");
	    String u = (account != null) && (account.has("username")) ? account.getString("username") : null;
	    if (u == null) {
	      u = (String)request.getSession().getAttribute("account_security");
	    }
	    return u != null ? u : "unknown";
	}


	public String getAccountRealname()
	{
		org.directwebremoting.WebContext web = WebContextFactory.get();   
		if( web == null )
			return null;
		javax.servlet.http.HttpServletRequest request = web.getHttpServletRequest();
		JSONObject account = (JSONObject) request.getSession().getAttribute("account");
		return account!=null&&account.has("realname")?account.getString("realname"):"unknown";
	}
	

	/**
	 * 在设置用户配置后发送系统通知
	 * @param rid
	 * @param pathRole
	 * @param request
	 * @param operator
	 * @param userMgr
	 * @param tips
	 * @param title
	 * @param action
	 * @param actionlink
	 * @throws Exception
	 */
	public void sendNotiefieToAccount(
			String account,
			String filter,
			String title,
			String context,
			String contextlink,
			String action,
			String actionlink) throws Exception
	{
		org.directwebremoting.WebContext web = WebContextFactory.get();   
		if( web == null )
			return;
		javax.servlet.http.HttpServletRequest request = web.getHttpServletRequest();
	    this.sendNotiefieToAccount(request, account, filter, title, context, contextlink, action, actionlink);
	}
	/**
	 * 发送通知给指定用户
	 * @param request
	 * @param account
	 * @param filter
	 * @param title
	 * @param context
	 * @param contextlink
	 * @param action
	 * @param actionlink
	 * @throws Exception
	 */
	public void sendNotiefieToAccount(
			HttpServletRequest request,
			String account,
			String filter,
			String title,
			String context,
			String contextlink,
			String action,
			String actionlink) throws Exception
	{
		if( account.startsWith("#") ) return;
		Sysnotify notify = new Sysnotify();
		if( action != null )
		{
			notify.setAction(action);
			notify.setActionlink(actionlink);
		}
		notify.setUseraccount(account);
		notify.setFilter(filter);
		notify.setNotifytime(new Date());
		notify.setPriority(0);
		notify.setTitle(title);
		notify.setContext(context);
		notify.setContextlink(contextlink);
		SysnotifyClient.send(notify);
	}	

	/**
	 * 向指定的角色权限组所有用户包括子权限组用户发送系统消息
	 * @param rid
	 * @param pathRole
	 * @param request
	 * @param operator
	 * @param userMgr
	 * @param tips
	 * @param title
	 * @param action
	 * @param actionlink
	 * @throws Exception
	 */
	public static void sendNotiefiesToRole(
			JSONObject role,
			String filter,
			String title,
			String context,
			String contextlink,
			String action,
			String actionlink) throws Exception
	{
		org.directwebremoting.WebContext web = WebContextFactory.get();
		if( web == null )
			return;
	    javax.servlet.http.HttpServletRequest request = web.getHttpServletRequest();
	    sendNotiefiesToRole(request, role, filter, title, context, contextlink, action, actionlink);
	}
	public static void sendNotiefiesToRole(
			HttpServletRequest request,
			JSONObject role,
			String filter,
			String title,
			String context,
			String contextlink,
			String action,
			String actionlink) throws Exception
	{
		ArrayList<Sysuser> users = SysuserClient.listUser( role.getInt("id"), -1, Status.Enable.getValue());
		for(Sysuser u : users )
		{
			Sysnotify notify = new Sysnotify();
			if( action != null )
			{
				notify.setAction(action);
				notify.setActionlink(actionlink);
			}
			notify.setUseraccount(u.getUsername());
			notify.setFilter(filter);
			notify.setNotifytime(new Date());
			notify.setPriority(0);
			notify.setTitle(title);
			notify.setContext(context);
			notify.setContextlink(contextlink);
			SysnotifyClient.send(notify);
		}
		if( !role.has("children") ) return;
		JSONArray children = role.getJSONArray("children");
		for( int i = 0; i < children.length(); i++ )
		{
			sendNotiefiesToRole(children.getJSONObject(i), filter, title, context, contextlink, action, actionlink);
		} 
	}
	/**
	 * 发送通知给系统管理员
	 * @param rid
	 * @param request
	 * @param operator
	 * @param userMgr
	 * @param title
	 * @param tips
	 * @param action
	 * @param actionlink
	 * @throws Exception
	 */
	public static void sendNotiefiesToSystemadmin(
			String filter,
			String title,
			String context,
			String contextlink,
			String action,
			String actionlink)
	{
		org.directwebremoting.WebContext web = WebContextFactory.get();
		if( web == null )
			return;
	    javax.servlet.http.HttpServletRequest request = web.getHttpServletRequest();
	    sendNotiefiesToSystemadmin(request, filter, title, context, contextlink, action, actionlink);
	}
	/**
	 * 发送系统通知给系统管理员
	 * @param request
	 * @param filter
	 * @param title
	 * @param context
	 * @param contextlink
	 * @param action
	 * @param actionlink
	 */
	public static Sysnotify sendNotiefiesToSystemadmin(
			HttpServletRequest request,
			String filter,
			String title,
			String context,
			String contextlink,
			String action,
			String actionlink)
	{
		Sysnotify notify = new Sysnotify();
		try
		{
			ArrayList<Sysuser> users = SysuserClient.listUser(1, -1, Status.Enable.getValue());
			Sysuser admin = new Sysuser();
			admin.setUsername("admin");
			users.add(admin);
			notify.setFilter(filter);
			notify.setNotifytime(new Date());
			notify.setPriority(0);
			notify.setTitle(title);
			notify.setContext(context);
			notify.setContextlink(contextlink);
			if( action != null )
			{
				notify.setAction(action);
				notify.setActionlink(actionlink);
			}
			for(Sysuser u : users )
			{
				notify.setUseraccount(u.getUsername());
				SysnotifyClient.send(notify);
			}
		}
		catch(Exception e)
		{
		}
		return notify;
	}
	
	/**
	 * 删除制定文件或目录
	 * @param ip
	 * @param port
	 * @param destpath
	 */
	protected void deleteFiles(String ip, int port, String destpath)
		throws Exception
	{
		byte[] payload = new byte[256];
    	DatagramSocket datagramSocket = null;
		try 
		{
			int offset;
			byte[] pathbuf = destpath.getBytes("UTF-8");
			offset = 0;
	    	payload[offset++] = Command.CONTROL_DELETEFILE;//删除指定路径
	    	payload[offset++] = (byte)pathbuf.length;//文件路径
	    	offset = Tools.copyByteArray(pathbuf, payload, offset);
        	datagramSocket = new DatagramSocket();
            datagramSocket.setSoTimeout(100000);
            DatagramPacket request = new DatagramPacket(payload, 0, offset, InetAddress.getByName( ip ), port );
            datagramSocket.send( request );
            DatagramPacket reponse = new DatagramPacket(payload, 0, payload.length, request.getAddress(), request.getPort() );
            datagramSocket.receive( reponse );
		}
		catch (Exception e)
		{
			throw new Exception(String.format("删除伺服器【%s:%s】文件[%s]出现异常%s", ip, port, destpath, e.getMessage()));
		}
        finally
        {
    		try
			{
    			if( datagramSocket != null ) datagramSocket.close();
			}
			catch (Exception e)
			{
			}
        }
	}
	/**
	 * 拷贝文件到目标路径
	 * @param tempfile
	 * @param destpath
	 */
	protected void copyFiles(String ip, int port, String destpath, File tempfile)
		throws Exception
	{
		byte[] buf = destpath.getBytes();
		int offset = 0;
    	byte[] payload = new byte[64*1024];
    	payload[offset++] = Command.CONTROL_COPYFILE;
    	payload[offset++] = (byte)buf.length;//文件路径
    	offset = Tools.copyByteArray(buf, payload, offset);
    	payload[offset++] = (byte)7;//是否自动解压缩文件
    	DatagramSocket datagramSocket = null;
    	Socket socket = null;
        try
        {
        	datagramSocket = new DatagramSocket();
            datagramSocket.setSoTimeout(15000);
            InetAddress addr = InetAddress.getByName( ip );
            DatagramPacket request = new DatagramPacket(payload, 0, offset, addr, port );
            datagramSocket.send( request );
			DatagramPacket response = new DatagramPacket(payload, payload.length);
			datagramSocket.receive(response);
			int port1 = Tools.bytesToInt(payload, 0, 4);
			InetSocketAddress endpoint = new InetSocketAddress(addr, port1);
			socket = new Socket();
			socket.connect(endpoint, 15000);
            OutputStream out = socket.getOutputStream();
            FileInputStream fis = new FileInputStream(tempfile);
            int len = 0;
            while( (len = fis.read(payload)) != -1 )
            {
            	out.write(payload, 0, len);
            	out.flush();
            }
            fis.close();
            out.close();
            log.info("Succeed to upload the file of "+tempfile+" to "+ip+":"+port1);
        }
        catch(Exception e)
        {
        	log.error("", e);
			throw new Exception(String.format("拷贝文件到伺服器【%s:%s】目录[%s]出现异常%s", ip, port, destpath, e.getMessage()));
        }
        finally
        {
    		try
			{
    			if( datagramSocket != null ) datagramSocket.close();
            	if( socket != null ) socket.close();
			}
			catch (IOException e)
			{
			}
        }
	}
	/**
	 * 拷贝
	 * @return
	 */
	protected File fetchFiles(String ip, int port, String srcpath)
		throws Exception
	{
		File srcdir = new File(srcpath);
		log.info("Fetch "+srcpath+" from "+ip+":"+port);
    	byte[] payload = new byte[64*1024];
    	int offset = 0;
    	FileOutputStream fos = null;
    	DatagramSocket datagramSocket = null;
    	Socket socket = null;
    	File tempdir = new File(PathFactory.getAppPath(), "temp/"+String.valueOf(System.currentTimeMillis()));
    	tempdir.mkdirs();
        File tempfile = new File(tempdir, srcdir.getName()+".zip" );
    	try
    	{
        	payload[offset++] = Command.CONTROL_GETFILE;
        	payload[offset++] = 1;//标记要压缩
        	byte buf[] = srcpath.getBytes("UTF-8");
        	Tools.intToBytes(buf.length, payload, offset, 2);
        	offset += 2;
        	offset = Tools.copyByteArray(buf, payload, offset);
    		payload[offset++] = 0;
    		payload[offset++] = 0;
        	datagramSocket = new DatagramSocket();
            datagramSocket.setSoTimeout(7000);
            InetAddress addr = InetAddress.getByName( ip );
            DatagramPacket request = new DatagramPacket(payload, 0, offset, addr, port );
            datagramSocket.send( request );
			DatagramPacket response = new DatagramPacket(payload, payload.length);
			datagramSocket.receive(response);
			port = Tools.bytesToInt(payload, 0, 4);
			InetSocketAddress endpoint = new InetSocketAddress(addr, port);
			datagramSocket.close();
			socket = new Socket();
			socket.connect(endpoint, 60000);
            InputStream is = socket.getInputStream();
            long flag = is.read();
            if( flag != 11 && flag != 1)
            {
            	throw new Exception("伺服器【"+ip+"】错误的返回值("+flag+")");
            }
            if( flag == 11 )
            {
            	is.read(payload, 0, 8);
            	flag = Tools.bytesToLong(payload, 0);
            }
			int len;
			fos = new FileOutputStream(tempfile); 
			while( (len = is.read(payload, 0, payload.length)) != -1  )
    		{
    			fos.write(payload, 0, len);
    			fos.flush();
    		}
    		is.close();
    		fos.close();
    	}
		catch(Exception e)
		{
			throw new Exception(String.format("从伺服器【%s:%s】目录[%s]下载文件出现异常 %s", ip, port, srcpath, e.getMessage()));
		}
    	finally
    	{
    		if( fos != null )
				try {
					fos.close();
				} catch (IOException e2) {
				}
    		if( socket != null )
				try {
					socket.close();
				} catch (IOException e1) {
				}
    	}
    	return tempfile;
	}
}
