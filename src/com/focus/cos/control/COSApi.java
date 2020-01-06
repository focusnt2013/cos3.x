package com.focus.cos.control;

import java.io.IOException;
import java.net.ServerSocket;
import java.security.Key;
import java.util.HashMap;

import javax.crypto.Cipher;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.json.JSONObject;

import com.focus.cos.api.AbaseServlet;
import com.focus.cos.api.AlarmServlet;
import com.focus.cos.api.ApiUtils;
import com.focus.cos.api.EmailServlet;
import com.focus.cos.api.LogServlet;
import com.focus.cos.api.NotifyServlet;
import com.focus.cos.api.PublishServlet;
import com.focus.cos.api.Syslog;
import com.focus.cos.api.Sysnotify;
import com.focus.cos.api.UserServlet;
import com.focus.sql.ConnectionPool;
import com.focus.util.Base64;
import com.focus.util.Base64X;
import com.focus.util.ConfigUtil;
import com.focus.util.F;
import com.focus.util.IOHelper;
import com.focus.util.Log;
import com.focus.util.Tools;
import com.focus.util.Zookeeper;

/**
 * COS的接口微服务
 * @author focus
 *
 */
public class COSApi implements Runnable
{
	public static String ModuleID = "COSApi";
	public static String[][] Versions = {
		{"3.17.5.16",	"初始版本构建完成。"},
		{"3.17.6.6",	"测试版本发布缺接口调用统计。"},
		{"3.17.10.23",	"上报COS的版本号。"},
		{"3.17.11.17",	"用户表添加了creator字段。"},
		{"3.18.1.5",	"读取API引擎监听端口改为从配置文件，增加日志检测。"},
		{"3.18.7.3",	"当收到未验证异常的时候就重启程序。"},
		{"3.19.12.25",	"根据配置定期清理系统日志和发件箱数据。"},
	};
	public static String LocalIp = "127.0.0.1";
	/*API代理服务*/
	protected boolean proxy;
	/*HTTP服务器*/
	protected Server server = null;
	/**/
	protected Zookeeper zookeeper;
	/*保管代理和本地的证书*/
	protected HashMap<String, Key> identities = new HashMap<String, Key>();
	/*当前服务器的唯一编号*/
	protected String proxyServerKey = null;
	/*当前服务器的唯一编号*/
	protected String agentServerKey = null;
	/*API的URL*/
	protected String cosapi = null;
	/*API本地的IP地址*/
	private String apiaddr;
	/*API本地监听端口*/
	private int apiport = 0;
	/*日志*/
	private LogServlet logServlet;
	/*日志*/
	private NotifyServlet notifyServlet;
	
	public String getCosapi() {
		return cosapi;
	}

	/**
	 * COS接口地址与端口
	 * @throws Exception
	 */
	public COSApi(String ip, int port) throws Exception
	{
        String Version = Versions[Versions.length-1][0];
        String dbDriver = System.getProperty("cos.jdbc.driver", "");
        String dbUser = System.getProperty("cos.jdbc.user", "");
        String dbPassword = System.getProperty("cos.jdbc.password", "");
        String dbUrl = System.getProperty("cos.jdbc.url", "");
        this.apiaddr = ip;
        String sqlcontips = "";
        StringBuffer info = new StringBuffer("================================================================================================");
        info.append("\r\n"+ModuleID+"\tv"+Version);
		info.append("\r\n\tuser.dir\t"+System.getProperty("user.dir", "?"));
		info.append("\r\n\tcos.jdbc.driver\t"+dbDriver);
		info.append("\r\n\tcos.jdbc.url\t"+dbUrl);
		info.append("\r\n\tcos.jdbc.username\t"+dbUser);
		info.append("\r\n\tcos.jdbc.password\t"+dbPassword);
		info.append("\r\n\tcos.api.addr\t"+apiaddr);
		info.append("\r\n\tcos.control.port\t"+System.getProperty("control.port", "?"));
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
        if( dbDriver.isEmpty() || dbUser.isEmpty() || dbUrl.isEmpty() )
        {
        	proxy = true;
        }
        else
        {
        	sqlcontips = "Setup the pool of connection from jdbc.";
        	ConnectionPool.connect(dbUrl, dbDriver, dbUser, dbPassword);
        }

		if( port == 0 )
		{
			if( proxy )
			{
				ServerSocket ss = new ServerSocket();
				ss.bind(null);
				port = ss.getLocalPort();
				ss.close();
			}
			else
			{
				port = 9079;
			}
		}
		this.apiport = port;
		System.setProperty("cos.api.port", String.valueOf(port));
		info.append("\r\n\tcos.api.port\t"+port);
		info.append("\r\n\tStartup by "+(proxy?"proxy":"agent")+" listener "+port+" ...");
		info.append("\r\n\tCopyright (C) 2008-2016 Focusnt.  All Rights Reserved.");
		info.append("\r\n================================================================================================");
		Log.msg( info.toString() );
		Log.msg( sqlcontips );
		server = new Server(port);
		server.setStopAtShutdown(true);
        
		context.addServlet(new ServletHolder(new AlarmServlet(this)), "/api/alarm");
		notifyServlet = new NotifyServlet(this);
		context.addServlet(new ServletHolder(notifyServlet), "/api/notify");
		context.addServlet(new ServletHolder(new EmailServlet(this)), "/api/email");
		logServlet = new LogServlet(this);
		context.addServlet(new ServletHolder(logServlet), "/api/log");
		context.addServlet(new ServletHolder(new UserServlet(this)), "/api/user");
		context.addServlet(new ServletHolder(new PublishServlet(this)), "/api/publish");
		F fileIdentity = new F(System.getProperty("cos.identity", "../data/identity"));
		Log.msg("Found cos.identity("+fileIdentity.exists()+") from "+fileIdentity.getAbsolutePath());
		if( !fileIdentity.exists() )
		{
			throw new Exception("Not found the identity of cos at "+fileIdentity.getAbsolutePath());
		}
		byte[] payload = IOHelper.readAsByteArray(fileIdentity);
		Key identity = (Key)IOHelper.readSerializableNoException(payload);
		if( identity == null )
		{
			throw new Exception("Not found the identity of cos at "+fileIdentity.getAbsolutePath());
		}
    	Cipher c = Cipher.getInstance("DES");
        c.init(Cipher.WRAP_MODE, identity);//再用数字证书构建另外一个DES密码器

        String serverkey = Base64.encode(c.wrap(identity));
        Log.msg("*Server-key is "+serverkey+", the length of payload is "+payload.length);
		if( proxy )
		{
			this.proxyServerKey = serverkey;
			this.cosapi = ConfigUtil.getString("cos.api");
			if( !cosapi.startsWith("http://") )
			{
				cosapi = "http://"+cosapi;
			}
			cosapi = cosapi.endsWith("/")?cosapi:(cosapi+"/");
			payload = ApiUtils.doPost(cosapi, identity);
			String json = new String(payload, "UTF-8");
			JSONObject result = new JSONObject(json);
			if( result.has("error") )
			{
				throw new Exception("Failed to register proxy to "+this.cosapi+" for "+result.getString("error"));
			}
			else if( !result.has("result") )
			{
				throw new Exception("Failed to register proxy to "+this.cosapi+" for "+result.toString());
			}
			else
			{
				JSONObject agent = result.getJSONObject("result");
				Log.msg("Succeed to register proxy to "+this.cosapi+"\r\n"+agent.toString());
				this.agentServerKey = agent.getString("id");
				registerApiAgent(agent);
			}
		}
		else
		{
			this.agentServerKey = serverkey;
			context.addServlet(new ServletHolder(new AbaseServlet(this){
				private static final long serialVersionUID = -1565376155931712301L;

				@Override
				public void save(HttpServletRequest request, byte[] payload, JSONObject response) throws Exception {
					// TODO Auto-generated method stub
			        String name = getRequestValue(request, "COS-Name");
			        String desc = getRequestValue(request, "COS-Desc");
			        String id = getRequestValue(request, "COS-ID");
			        String path = getRequestValue(request, "COS-Path");
			        String version = getRequestValue(request, "COS-Version");
			        String apiAddr = getRequestValue(request, "COS-IP");
			        String apiPort = getRequestValue(request, "COS-API-Port");
			        int port0 = Tools.isNumeric(apiPort)?Integer.parseInt(apiPort):0;
			        String controlport = getRequestValue(request, "COS-ControlPort");
			        int port1 = Tools.isNumeric(controlport)?Integer.parseInt(controlport):0;
			        String realip = request.getHeader("x-forwarded-for");
			        String remote = request.getRemoteAddr();
			        realip = realip == null ? request.getHeader("x-real-ip") : realip;
			        if (realip == null)  realip = "";
					Key identity = (Key)IOHelper.readSerializableNoException(payload);
			    	Cipher c = Cipher.getInstance("DES");
			        c.init(Cipher.WRAP_MODE, identity);//再用数字证书构建另外一个DES密码器
			        c.wrap(identity);
			        String _id = Base64.encode(c.wrap(identity));
					if( !id.equals(_id))
					{
						throw new Exception("Found the identity of proxy invalid("+_id+").");
					}
					JSONObject agent = server.register(id, identity, apiAddr, port0, port1, realip, remote, name, desc, path, version);
					response.put("result", agent);
				}

				@Override
				public void query(HttpServletRequest request, HttpServletResponse response, StringBuffer log)
						throws Exception {
					String ci = getRequestValue(request, "COS-ID");
					JSONObject register = new JSONObject();
					register.put("id", agentServerKey);
					register.put("controlport", Integer.parseInt(System.getProperty("control.port", "0")));
					register.put("name", ConfigUtil.getString("service.name", ""));
					register.put("desc", ConfigUtil.getString("service.desc", ""));
					super.write(ci, response, register);
				}
			}), "/");
			this.register(
				serverkey,
				identity,
				this.apiaddr,
				this.apiport,
				Integer.parseInt(System.getProperty("control.port", "0")),
				"",
				"lcaohost",
				ConfigUtil.getString("service.name", ""),
				ConfigUtil.getString("service.desc", ""),
				System.getProperty("user.dir"), 
				WrapperShell.getVersion());
		}
//		context.addServlet(new ServletHolder(new DeployServlet(this)), "/api/deploy");
		server.setHandler(context);
		server.start();
		// 当程序被关闭的时候钩子会被回调
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			public void run()
			{
				close();
			}
		});
		reportApiPort(port);
	}

	/**
	 * 
	 * 注册证书保存在内存中
	 * @param id	客户端的证书ID(Bae64编码）
	 * @param key	客户端发来的证书
	 * @param ip	proxy的IP地址
	 * @param controlport	主控端口
	 * @param name	客户端服务名称
	 * @param desc	客户端描述
	 * @param path	proxy对应的工作目录
	 * @param version	proxy对应的版本号
	 * @return 返回给proxy的服务端配置
	 */
	public JSONObject register(
			String id,
			Key identity,
			String proxyip,
			int proxyport,
			int controlport, 
			String realip,
			String remote,
			String name,
			String desc,
			String path,
			String version)
	{
		identities.put(id, identity);
		JSONObject config = new JSONObject();
		config.put("id", id);
		config.put("proxyip", proxyip);//代理的IP地址
		config.put("proxyport", proxyport);//代理的IP地址
		config.put("controlport", controlport);
		config.put("realip", realip);
		config.put("remote", remote);
		config.put("name", name);
		config.put("desc", desc);
		config.put("version", version);
		config.put("workpath", path);
		config.put("time", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss S", System.currentTimeMillis()));
		config.put("agent", id.equals(this.agentServerKey));
		JSONObject agent = new JSONObject();
		try
		{
			agent.put("id", this.agentServerKey);
			agent.put("name", ConfigUtil.getString("service.name", ""));
			agent.put("desc", ConfigUtil.getString("service.desc", ""));
			agent.put("addr", this.apiaddr);
			agent.put("port", this.apiport);
			String key = Base64X.encode(IOHelper.convertObjectToBytes(identity));
			config.put("identity", key);
			Log.msg("Succeed to register the proxy("+proxyip+") to server.\r\n\t"+config.toString(4));
			registerApiProxy(config);
		}
		catch(Exception e)
		{
			agent.put("error", e.getMessage());
			Log.err("Failed to register for ", e);
		}
		return agent;
	}
	
	/**
	 * 得到Zookeeper
	 * @return
	 * @throws IOException
	 */
	public Zookeeper getZookeeper() throws Exception
	{
		if( zookeeper != null && zookeeper.isConnected() ) return zookeeper;
		if( zookeeper != null ) zookeeper.close();
		zookeeper = Zookeeper.getInstance("127.0.0.1:"+System.getProperty("control.port", "0"));
		return zookeeper;
	}
	/**
	 * 调试开关
	 * @param id
	 * @param d
	 * @throws Exception
	 */
	private synchronized void reportApiPort(int port) throws Exception
	{
		System.out.write(0);
        String Version = Versions[Versions.length-1][0];
		System.out.write(Version.length());
		System.out.write(Version.getBytes());
		System.out.write(Tools.intToBytes(port));
		System.out.flush();
	}

	/**
	 * 向主控引擎报告多个API代理服务器的配置
	 * @param e
	 * @throws Exception
	 */
	private synchronized void registerApiProxy(JSONObject e) throws Exception
	{
		System.out.write(1);
		byte[] payload = e.toString(4).getBytes("UTF-8");
		System.out.write(Tools.intToBytes(payload.length, 4));
		System.out.write(payload);
		System.out.flush();
	}

	/**
	 * 向主控引擎报告API的主服务器配置
	 * @param e
	 * @throws Exception
	 */
	private synchronized void registerApiAgent(JSONObject e) throws Exception
	{
		System.out.write(2);
		byte[] payload = e.toString(4).getBytes("UTF-8");
		System.out.write(Tools.intToBytes(payload.length, 4));
		System.out.write(payload);
		System.out.flush();
	}
	/**
	 * 验证客户端
	 * @param request
	 * @return
	 */
	public boolean auth(HttpServletRequest request)
	{
		String version = getRequestValue(request, "COS-Version");
        String id = getRequestValue(request, "COS-ID");
//        String ip = getRequestValue(request, "COS-IP");
//        String port = getRequestValue(request, "COS-API-Port");
//        String controlport = getRequestValue(request, "COS-ControlPort");
//        String path = getRequestValue(request, "COS-Path");
        
        if( id == null || id.isEmpty() ||
    		version == null || version.isEmpty()
//			path == null || path.isEmpty() ||
//			port == null || port.isEmpty() ||
//			controlport == null || controlport.isEmpty() ||
//			ip == null || ip.isEmpty() 
		  )
        {
        	return false;
        }
        if( proxy )
        {
			return this.proxyServerKey!=null&&id!=null&&proxyServerKey.equals(id);
        }
        else
        {
        	return identities.containsKey(id); 
        }
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public Key getIdentity(String id)
	{
		return identities.get(id);
	}
	
	public HashMap<String, Key> getIdentities() {
		return identities;
	}

	/**
	 * 写日志
	 * @param log
	 */
	public void write(Syslog log)
	{
		logServlet.write(log);
	}

	/**
	 * 写通知
	 * @param log
	 */
	public void write(Sysnotify notify)
	{
		this.notifyServlet.write(notify);
	}
	
	/**
	 * 当前程序是否是代理
	 * @return
	 */
	public boolean isProxy()
	{
		return proxy;
	}

	/**
	 * 关闭服务
	 */
	public void close()
	{
		if( server != null )
			try {
				this.server.stop();
			} catch (Exception e) {
				Log.err("Failed to stop server.", e);
			}
		if( zookeeper != null ) zookeeper.close();
		Log.msg("Stop cosapi server.");
	}
	
	public void run()
	{
		try 
		{
			Log.msg("Join the server("+Server.getVersion()+") "+server.getState());
			server.join();
		}
		catch (InterruptedException e) {
			Log.err("Failed to join server.", e);
			System.exit(2);
		}
	}
	
	/**
	 * 代理计数
	 * @param servlet
	 */
	private int[][] counter;//计数器
	public void count(Servlet servlet)
	{
		int i = 0;
		if( servlet instanceof AlarmServlet )
		{
			i = 0;
		}
		else if( servlet instanceof NotifyServlet )
		{
			i = 1;
		}
		counter[i][0] += 1;
		counter[i][1] += 1;
	}
	/**
	 * 
	 * @param response
	 * @return
	 */
	public static void rsp404(HttpServletResponse response)
	{
		try
		{
			response.sendRedirect("http://115.29.243.100/ecw/404.png");
		}
		catch (IOException e)
		{
			Log.err(e);
		}
	}
	
	public static void main(String[] args)
	{
		ModuleID = args.length>0?args[0]:ModuleID;
		Log.getInstance().setSubroot(ModuleID);
		Log.getInstance().setDebug(false);
		Log.getInstance().setLogable(true);
		Log.getInstance().start();
		COSApi cosApi = null;
		try
		{
			LocalIp = Tools.getLocalIP();
		}
		catch (Exception e)
		{
			Log.err("The apiaddr has been set 127.0.0.1", e);
		}
		
		try
		{
			String port = ConfigUtil.getValue("cos.api.port", "0");//System.getProperty("cos.api.port", "0");
			cosApi = new COSApi(LocalIp, Integer.parseInt(port));
			Thread thread = new Thread(cosApi);
			thread.start();
			Log.msg("Start cosapi server.");
		}
		catch (Exception e)
		{
			Log.err("Failed to startup cosapi server", e);
			System.exit(-1);
		}
	}

    /**
     * 
     * @param request
     * @return
     */
    public static String getRequestValue(HttpServletRequest request, String id)
    {
    	try {
    		String val = request.getHeader(id);
    		if( val != null )
    		{
	    		byte[] buf = Base64.decode(request.getHeader(id));
				return new String(buf, "UTF-8");
    		}
		} catch (Exception e) {
		}
    	return null;
    }
}