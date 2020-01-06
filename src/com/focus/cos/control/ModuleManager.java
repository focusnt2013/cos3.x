package com.focus.cos.control;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TimeZone;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Element;

import com.focus.control.ModulePerf;
import com.focus.control.SystemPerf;
import com.focus.cos.CosServer;
import com.focus.cos.api.AlarmSeverity;
import com.focus.cos.api.AlarmType;
import com.focus.cos.api.ApiUtils;
import com.focus.cos.api.LogSeverity;
import com.focus.cos.api.LogType;
import com.focus.cos.api.Sysalarm;
import com.focus.cos.api.SysalarmClient;
import com.focus.cos.api.Syslog;
import com.focus.cos.api.SyslogClient;
import com.focus.cos.wrapper.WrapperUpdate;
import com.focus.cos.wrapper.WrapperUpgrade;
import com.focus.util.ConfigUtil;
import com.focus.util.Decompressor;
import com.focus.util.F;
import com.focus.util.IOHelper;
import com.focus.util.Log;
import com.focus.util.QuickSort;
import com.focus.util.Subprocessor;
import com.focus.util.Tools;
import com.focus.util.XMLParser;

/**
 * <p>Title: 主控模块</p>
 *
 * <p>Description: 实现对多进程引擎服务的控制</p>
 *
 * <p>Copyright: Copyright (c) 2008~2079</p>
 *
 * <p>Company: </p>
 *
 * @author Focus Lau
 * @version 1.13.4.12
 */
public abstract class ModuleManager extends Thread
{
	public static final long serialVersionUID = 158282877200444712L;
	private Hashtable<String, Module> modules = new Hashtable<String, Module>();
    public Hashtable<String, Module> getModules() {
		return modules;
	}

    private boolean opened = true;
    private boolean closed = false;
//    private F fileControl;
    public static String LocalIp;//本地IP地址
    //监控主控引擎的内存使用情况
    protected MonitorGC mainGC;
    //COS-ID
    protected String identity;
	//主控引擎的接口程序占用的端口COS-API-Port
    private int apiPort;
    //通过主控引擎的接口引擎向接口地址对应的主控引擎进行登记，对端的服务器将作为本机的主服务器完全开放
    private JSONObject apiAgent = null;
    //在本服务器完成注册并进行管理的客户端
    private HashMap<String, JSONObject> apiProxy = new HashMap<String, JSONObject>();
    //主端口池
    private LinkedList<Integer> controlports = new LinkedList<Integer>();
    //
    private int controlPort = 9075;
    //主界面框架系统端口
    private int portalPort = 0;
    //安装JDK
    private MyJDK myjdk;
    
    /**
     * 从控制端口队列中取一个端口用
     * 控制端口队列是从主控端口后数字开始
     * @return
     */
    private int getControlPort()
    {
		Integer port = controlports.poll();
    	if( port == null )
    	{
    		controlPort += 1;
    		if( apiPort == controlPort )
    		{//遇到API端口就加1
    			controlPort += 1;
    		}
    		if( portalPort > 0 )
    		{//遇到门户端口就加4
	    		if( portalPort == controlPort )
	    			controlPort += 1;
	    		if( portalPort+1 == controlPort )
	    			controlPort += 1;
	    		if( portalPort+2 == controlPort )
	    			controlPort += 1;
	    		if( portalPort+3 == controlPort )
	    			controlPort += 1;
	    		if( portalPort+4 == controlPort )
	    			controlPort += 1;
    		}
    		if( this.getDatabasePort() > 0 )
    		{
	    		if( this.getDatabasePort() == controlPort )
	    			controlPort += 1;
    		}
    		port = controlPort;
    		/*StringBuffer sb = new StringBuffer();
    		StackTraceElement[] stes = new Exception().getStackTrace();
    		if( stes != null && stes.length > 0 )
    		{
    			for(StackTraceElement e: stes)
    			{
    				 sb.append("\r\n\t");
    				 sb.append(e.getClassName());
    				 sb.append(".");
    				 sb.append(e.getMethodName());
    			}
    		}
			Log.war("Get the port "+port+" of control from"+sb.toString());*/
    	}
    	return port;
    }
    /**
     * 主控Socket链接
     * @author focus
     *
     */
    class ControlSocket extends ServerSocket
    {
    	private int port = 0;
		public ControlSocket() throws IOException {
			super(getControlPort());
			port = super.getLocalPort();
		}
		
		/**
		 * 关闭控制套接字
		 */
		public void close()
		{
			String logtxt = "Rlease the socket("+port+") of control, "+controlports.size()+" ports avaiable about...";
			try
			{
				super.close();
			}
			catch(Exception e)
			{
				logtxt += "\r\n\t"+e.getMessage();
				Log.war(logtxt);
			}
			controlports.push(port);
		}
    }
    /**
     * 确认拷贝文件然后重启
     * @param addr0
     * @param port0
     */
	public void doUpgrader(InetAddress addr0, int port0, boolean restartup)
	{
		F workpath = new F(ConfigUtil.getWorkPath());
    	String cosportal = ConfigUtil.getString("runner.cosportal", "");
		F webpath = null;
		if( !cosportal.isEmpty() ) webpath = new F(cosportal);
		F tempdir = null;
		F upgradedir = null;
		if( webpath != null )
		{
			F vf = new F(webpath, "workspace/cos/version.txt");
			upgradedir = new F(cosportal, "workspace/cos");
			if( !vf.exists() ){
				vf = new F(cosportal, "webapps/cos/version.txt");
				upgradedir = new F(cosportal, "webapps/cos");
			}
			tempdir = new F(workpath, "temp/upgrade/web");
			if( !tempdir.exists() ) tempdir.mkdirs();
			ArrayList<String> updates = new ArrayList<String>();
			ArrayList<String> deletes = new ArrayList<String>();
			WrapperUpgrade.loadUpgradeFiles(tempdir, updates, deletes);
			WrapperUpgrade.executeUpgrade(upgradedir, tempdir, updates, deletes);
		}

		tempdir = new F(workpath, "temp/upgrade/server");
		if( !tempdir.exists() ) tempdir.mkdirs();
		upgradedir = workpath;
		ArrayList<String> updates = new ArrayList<String>();
		ArrayList<String> deletes = new ArrayList<String>();
		WrapperUpgrade.loadUpgradeFiles(tempdir, updates, deletes);
		WrapperUpgrade.executeUpgrade(upgradedir, tempdir, updates, deletes);
		
		JSONObject rsp = new JSONObject();
		if( restartup )	rsp.put("result", "成功执行升级系统将在10秒钟后自动重启。");
		else rsp.put("result", "成功执行升级。");
		response(addr0, port0, rsp);
		Thread thread = new Thread(){
			public void run()
			{
				try {
					sleep(10000);
				} catch (InterruptedException e) {
				}
				doRestart();
			}
		};
		thread.start();
	}
    /**
     * 执行升级
     * @author focus
     *
     */
    class Upgrader extends Thread
    {
    	InetAddress addr0;
    	int port0;
		JSONObject rsp = new JSONObject();
		String oldversion;
		F upgradedir;
		int progress = 0;
		
		/**
		 * 升级
		 * @param addr0
		 * @param port0
		 */
    	public Upgrader(InetAddress addr0, int port0)
    	{
    		this.addr0 = addr0;
    		this.port0 = port0;
    	}
    	
    	public void run()
    	{
			F workpath = new F(ConfigUtil.getWorkPath());
	    	String cosportal = ConfigUtil.getString("runner.cosportal", "");
			F webpath = null;
			if( !cosportal.isEmpty() ) webpath = new F(cosportal);
			WrapperUpgrade wrapper = new WrapperUpgrade(workpath, webpath){
				@Override
				public void notifyDownloadResult(File tempdir, boolean succeed, boolean newvresion, String version, Exception e, boolean needReboot, String release, String logcontext)
				{
					rsp.remove("progress");
					String id = tempdir.getName().equals("server")?"COSControl":"COSPortal";
					String name = tempdir.getName().equals("server")?"主控引擎系统":"主界面框架系统";
					rsp.put("version", version);
					rsp.put("newversion", newvresion);
					if( succeed && newvresion )
					{
						sendReport("伺服器【"+LocalIp+"】的【"+name+"】发现有新版本"+version+"。");
						System.out.println("Succeed to download "+id+" "+version+" from "+oldversion+" at "+LocalIp+"("+System.getProperty("control.port")+")");
						ArrayList<String> updates = new ArrayList<String>();
						ArrayList<String> deletes = new ArrayList<String>();
						loadUpgradeFiles(tempdir, updates, deletes);
						sendReport("新版本"+(updates.size()+deletes.size())+"处变化。");
	
						Syslog syslog = new Syslog();
						syslog.setCategory("COSControl");
						syslog.setAccount(securityKey);
						syslog.setLogseverity(LogSeverity.INFO.getValue());
						syslog.setLogtype(LogType.运行日志.getValue());
						syslog.setContext(logcontext);
						syslog.setLogtext("伺服器【"+LocalIp+"】的【"+name+"】有新版本["+version+"]已下载并已完成升级");
						SyslogClient.submit(syslog);
					}
					else if( !newvresion )
					{
						System.out.println("@COS$ Not found new version.");
						sendReport("伺服器【"+LocalIp+"】的【"+name+"】当前版本是"+oldversion+"，未发现有新版本。");
					}
					if( e != null )
					{
						sendReport("该伺服器主控引擎执行升级出现异常"+e);
						System.out.println("@COS$ Failed to upgrade to for exception "+e);

						Syslog syslog = new Syslog();
						syslog.setCategory("COSControl");
						syslog.setAccount(securityKey);
						syslog.setLogseverity(LogSeverity.ERROR.getValue());
						syslog.setLogtype(LogType.运行日志.getValue());
						syslog.setLogtext("伺服器【"+LocalIp+"】的【"+name+"】执行升级出现异常["+e+"]");
						try
						{
							ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
							PrintStream ps = new PrintStream(out);
							e.printStackTrace(ps);
							syslog.setContext(logcontext+"\r\n"+out.toString());
							sendReport(out.toString());
							ps.close();
						}
						catch(Exception e1)
						{
						}
						SyslogClient.submit(syslog);
					}
				}
				@Override
				public void notifyDownloadProgress(int progress) 
				{
					rsp.put("progress", progress);
					response(addr0, port0, rsp);
				}
				@Override
				public void notifyLicenseExpired(String tips) {
				}
			};
			if( !wrapper.report() )
			{
				sendReport("伺服器安全证书("+identity+")校验失败，稍后请重新执行升级操作。");
				return;
			}
			sendReport("伺服器安全证书("+identity+")校验成功。");
			/**
			 * 以当前版本作为基线进行升级，每次打包cos.server的时候都对该版本号做修改。
			 */
			F tempdir = null;
			if( webpath != null )
			{
				rsp.put("type", "COSPortal");
				F vf = new F(webpath, "workspace/cos/version.txt");
				upgradedir = new F(cosportal, "workspace/cos");
				if( !vf.exists() ){
					vf = new F(cosportal, "webapps/cos/version.txt");
					upgradedir = new F(cosportal, "webapps/cos");
				}
				oldversion = "v3_17_5_16";
				if( vf.exists() ) oldversion = IOHelper.readFirstLine(vf);
				rsp.put("version", oldversion);
				tempdir = new F(workpath, "temp/upgrade/web");
				if( !tempdir.exists() ) tempdir.mkdirs();
				sendReport("检查【主界面框架系统】最新版本，当前版本是"+oldversion);
				System.out.println("@COS$ Check the version of COSPortal that old version is "+oldversion);
				rsp.put("progress", 0);
				wrapper.download("web", oldversion, tempdir, true);
			}

			rsp.put("type", "COSControl");
			tempdir = new F(workpath, "temp/upgrade/server");
			if( !tempdir.exists() ) tempdir.mkdirs();
			oldversion = WrapperUpdate.getServerVersion(workpath);
			rsp.put("version", oldversion);
			upgradedir = workpath;
			sendReport("检查【主控引擎系统】最新版本，当前版本是"+oldversion);
			System.out.println("@COS$ Check the version of COSControl that old version is "+oldversion);
			rsp.put("progress", 0);
			wrapper.download("server", oldversion, tempdir, true);
			rsp.remove("result");//, "完成伺服器【"+LocalIp+"】升级操作.");
    		response(addr0, port0, rsp);
    	}
    	
    	private void sendReport(String txt)
    	{
			rsp.put("result", txt);
    		response(addr0, port0, rsp);
    	}
    }
    /**
     * 重启主控
     */
    public abstract String setDatabaseStatus(int mode, String param);
    /**
     * 重启主控
     */
    public abstract void doRestart();
    /**
     * 数据库状态就位
     * @return
     */
    public abstract boolean isDatabaseStandby();
    /**
     * 得到数据库状态
     * @return
     */
    public abstract String getDatabaseStatus();
    /**
     * 数据库是否在运行
     * @return
     */
    public abstract boolean isDatabaseRunning();
    /**
     * 得到数据库启动时间
     * @return
     */
    public abstract String getDatabaseStarttime();
    /**
     * 得到数据库端口
     * @return
     */
    public abstract int getDatabasePort();

    public ModuleManager(String identity) throws Exception
    {
//    	fileControl = new F( ConfigUtil.getWorkPath() + "config/", "control.xml" );
        ModulePerf cosPerf = new ModulePerf();
        cosPerf.setName("主控引擎");
        cosPerf.setStartupTime(new Date(startTimestamp));
        cosPerf.setId("COSControl");
        cosPerf.setVersion(WrapperShell.version());
        cosPerf.setModuleLogPath(new java.io.File(ConfigUtil.getWorkPath(), "log/"+cosPerf.getId()));
        cosPerf.setRemark("负责监控系统配置的所有子系统模块工作。");
        cosPerf.setProgrammer("超级管理员");
        cosPerf.setState(Module.STATE_STARTUP);
        cosPerf.setMonitorInfo("Service Running.");
        mainGC = new MonitorGC(cosPerf);
        this.identity = identity;
    	String controlPort = System.getProperty("control.port");
    	if( Tools.isNumeric(controlPort) )
    	{
    		this.controlPort = Integer.parseInt(controlPort);
    	}
    	else
    	{
    		throw new Exception("未知主控端口"+controlPort);
    	}
    }
    
    /**
     * 当API程序完成启动后会调用这个方法
     * @param port
     */
    public void setApiPort(int port)
    {
    	apiPort = port;
    	String result = System.setProperty("cos.api.port", String.valueOf(port));
    	Log.print("Set the port of api-local to %s\r\n\tThe result is %s.\r\n\tThe value of read is %s.",
    		apiPort, result, ConfigUtil.getString("cos.api.port"));
    	ApiUtils.reset();
    	F file = new F(ConfigUtil.getWorkPath(), "data/identity");
        System.out.println();
    	System.out.println("@COS$ The api-port of your service is "+port);
    	System.out.println("@COS$ You can only use this api locally by http://127.0.0.1:"+port);
    	System.out.println("@COS$ The identity path of your service is "+file.getPath());
    	System.out.println("@COS$ Please ensure your program set the correct system-propery.");
    	System.out.println("\tSystem.setProperty(\"cos.api.port\", \""+port+"("+ConfigUtil.getString("cos.api.port")+")\");");
    	System.out.println("\tSystem.setProperty(\"cos.identity\", \""+System.getProperty("cos.identity")+"\");");
        System.out.println("@COS$ COSAPI work on.");
    }
    
    /**
     * 收到接口引擎的通知，当前主控引擎作为主伺服有那些客户端接入
     * @param payload
     * @param length
     */
    public void setApiProxy(byte[] payload, int length)
    {
    	JSONObject proxy = null;
    	try
    	{
	    	proxy = new JSONObject(new String(payload, 0, length, "UTF-8"));
	    	String proxyKey = proxy.getString("id");
	    	if(this.identity.equals(proxyKey) )
	    	{
	    		if( apiAgent == null ){
		        	System.out.println("@COS-API$ "+proxy.getString("id")+"@"+proxy.getString("name")+" setup from "+
		        			proxy.getString("proxyip")+":"+proxy.getInt("proxyport")+".");
	    		}
	    		apiAgent = proxy;
	    	}
	    	else
	    	{
	        	System.out.println("@COS-API$ "+proxy.getString("id")+"@"+proxy.getString("name")+" setup from "+
	        			proxy.getString("proxyip")+":"+proxy.getInt("proxyport")+".");
	            System.out.println("@COS$");
	    	}
			apiProxy.put(proxyKey, proxy);
	    	Log.msg("Succeed to setup the api-proxy("+proxyKey+") of your service: "+proxy.toString(4));
	    	ZookeeperRunner zookeeper = (ZookeeperRunner)modules.get("Zookeeper");
	    	zookeeper.setApiProxy(proxy);
    	}
    	catch(Exception e)
    	{
    		Log.err("Failed to setup the api-proxy"+(proxy!=null?proxy.toString(4):("payload:"+length)), e);
    	}
    }
    
    /**
     * 该方法当主控引擎作为API的代理客户端的时候会被调用
     * @param agent
     */
    public void setApiAgent(byte[] payload, int length)
    {
    	try
    	{
	    	JSONObject agent = new JSONObject(new String(payload, 0, length, "UTF-8"));
	    	this.apiAgent = agent;
        	System.out.println("Agent("+agent.getString("id")+"@"+agent.getString("name")+") setup.");
            System.out.println("@COS$");
	    	Log.msg("Succeed to setup the api-agent("+agent.getString("id")+") of your service: "+agent.toString());
    	}
    	catch(Exception e)
    	{
    		Log.err("Failed to setup the api-agent.", e);
    	}
	}
    
    /**
     * API出现退出的时候回调该方法
     */
    private boolean apiProxyClosedEmailed = false;
    public void apiProxyClosed()
    {
    	if( !apiProxyClosedEmailed )
    	{
	    	ZookeeperRunner zookeeper = (ZookeeperRunner)modules.get("Zookeeper");
	    	zookeeper.sendSystemEmail("伺服器【"+LocalIp+"】接口代理引擎程序异常关闭", 
	    		"机器码【"+this.identity+"】的伺服器接口代理引擎程序非正常情况退出。"+
				"\r\n\t集群主数据库是否激活: "+(WrapperShell.H2Switcher?"Yes":"None")+
	    		(WrapperShell.H2Switcher?
	    			("\r\n\tAPI接口守护端口: "+ConfigUtil.getString("cos.api.port", "n/a")):
	    			("\r\n\tAPI接口访问地址: "+ConfigUtil.getString("cos.api", "n/a"))
	    		)
	    	);
	    	apiProxyClosedEmailed = true;
    	}
    }

    private boolean portalNostartupEmailed = false;
    public void portalNostartup()
    {
    	if( !portalNostartupEmailed )
    	{
	    	ZookeeperRunner zookeeper = (ZookeeperRunner)modules.get("Zookeeper");
	    	zookeeper.sendSystemEmail("伺服器【"+LocalIp+"】主界面框架系统程序没能正常启动", 
	    		"机器码【"+this.identity+"】的伺服器主界面框架系统程序因为主数据库未正常工作而没能启动。主数据库状态如下: "+getDatabaseStatus());
	    	portalNostartupEmailed = true;
    	}
    }

    private boolean apiNostartupEmailed = false;
    public void apiNostartup()
    {
    	if( !apiNostartupEmailed )
    	{
	    	ZookeeperRunner zookeeper = (ZookeeperRunner)modules.get("Zookeeper");
	    	zookeeper.sendSystemEmail("伺服器【"+LocalIp+"】主控接口服务引擎没能正常启动", 
	    		"机器码【"+this.identity+"】的伺服器主控接口服务引擎因为主数据库未正常工作而没能启动。主数据库状态如下: "+getDatabaseStatus());
	    	apiNostartupEmailed = true;
    	}
    }

    public void close()
    {
        Log.war( "******************************************************************\r\n\tClose Module Manager When '"+status+
        		"' "+Tools.getFormatTime(status_timestamp)+"\r\n******************************************************************");
//        if( systemMonitorRunner != null ) systemMonitorRunner.close();
//        if( zookeeperRunner != null ) zookeeperRunner.close();
//        if( systemEmailRunner != null ) systemEmailRunner.close();
        opened = false;
        if( datagramSocket != null )
        {
        	datagramSocket.close();
        }
        synchronized(this){
//        org.tanukisoftware.wrapper.security.WrapperReport.getInstance().close();
        	this.notify();
			System.out.println("@COS$ notified after the socket of listener closed.");
        }
    }

    /**
     * 是否监控同步器超时
     * @param syncher
     * @return
     */
    private boolean isSyncherTimeout(SystemMonitorSyncher syncher){
		if(syncher.timeout()){
			Log.printf("Timeout(%s) the syncher of %s.", syncher.timestamp(), syncher.toString());
			syncher.disconnect();
			//这种情况一定要发送严重告警
			Sysalarm alarm = new Sysalarm();
        	alarm.setSysid("Sys");
        	alarm.setSeverity(AlarmSeverity.RED.getValue());
        	alarm.setType(AlarmType.S.getValue());
        	alarm.setId(WrapperShell.ModuleID+"_myjdk");
        	alarm.setCause("网络配置错误(网关路由)");
        	alarm.setTitle("发送监控数据到[监控界面]超时");
        	alarm.setText("从"+Tools.getFormatTime(syncher.timestamp())+
        			"主控引擎尝试发送监控数据到[监控界面]，该操作没有正常结束已经造成线程死锁。"+
        			"该问题可能是因为网络配置错误造成的，该错误将可能影响用户程序异常，请尽快检查并解决。");
        	SysalarmClient.send(alarm);
			return true;
		}
		return false;
    }
	/**
	 * 发送心跳
	 */
//    private Thread synchHeartThread;//同步心跳线程
	private void sendHeartbeat()
	{
    	if( opened ) //运行状态下才允许发送模块子系统
    	synchronized(systemMonitorSynchers)
    	{
	        for( int i = 0; i < systemMonitorSynchers.size(); i++ )
			{
				SystemMonitorSyncher syncher = systemMonitorSynchers.get(i);
				if(isSyncherTimeout(syncher)){
					i -= 1;
					continue;
				}
				syncher.heartbeat(subject);
			}
    	}
	}
    /**
     * 向各个需要监听连接发送某个引擎配置删除的指示
     * @param module
     * @param ind
     */
    public void sendModuleMonitorData(Module module, int ind)
    {
    	if( opened ) //运行状态下才允许发送模块子系统
        	synchronized(systemMonitorSynchers)
        	{
				for( int i = 0; i < systemMonitorSynchers.size(); i++ )
				{
					SystemMonitorSyncher syncher = systemMonitorSynchers.get(i);
					if(isSyncherTimeout(syncher)){
						i -= 1;
						continue;
					}
					syncher.send(module.getModulePerf(), ind);//2表示引擎撤销
				}
        	}
    }

    /**
     * 通知系统信息和历史运行信息
     */
    public void sendSystemMonitorData()
    {
    	if( opened && !systemMonitorSynchers.isEmpty() )
    	{
	    	SystemPerf systemPerf = getLocalSystemPerf();
	    	F file = new F(ConfigUtil.getWorkPath(), "data/monitor/host");
	    	java.io.Serializable history = IOHelper.readSerializableNoException(file);
	    	if( history == null )
	    	{
	    		Log.war("Not found the perf of host("+file.getPath()+" "+file.exists()+").");
	    		return;
	    	}
	    	synchronized(systemMonitorSynchers)
	    	{
				for( int i = 0; i < systemMonitorSynchers.size(); i++ )
				{
					SystemMonitorSyncher syncher = systemMonitorSynchers.get(i);
					if(isSyncherTimeout(syncher)){
						i -= 1;
						continue;
					}
					syncher.send(systemPerf, N_HU);//0表示SystemPerf
					if( history != null ) syncher.send(history, N_HHU);//发送历史主机信息
				}
	    	}
    	}
    }
    /**
     * 设置数据库的信息
     * @param h2
     */
    private void setDatabaseInfo(JSONObject h2){
		String h2Type = ConfigUtil.getString("cos.database.h2", "");
    	String h2Standby = ConfigUtil.getString("h2.cluster.standby", "");
    	h2.put("type", h2Type);
    	h2.put("port", this.getDatabasePort());
    	h2.put("controlport", System.getProperty("control.port"));//主控端口
    	h2.put("standby", h2Standby);
    	h2.put("rootdir", ConfigUtil.getWorkPath());
    	h2.put("working", this.isDatabaseStandby());
    	h2.put("running", this.isDatabaseRunning());
    	h2.put("status", this.getDatabaseStatus());
    	h2.put("starttime", this.getDatabaseStarttime());
		F h2dir = new F(ConfigUtil.getWorkPath(), "h2");
		long size = IOHelper.getDirSize(h2dir);
		h2.put("size", size);
    }
    
    /**
     * 向各个需要监听连接发送最新的监控引擎采集的数据
     */
    private SystemPerf getLocalSystemPerf()
    {
		F f = new F(ConfigUtil.getWorkPath(), "data/monitor/out");
		if( f.exists() ){
			SystemPerf systemPerf = (SystemPerf)IOHelper.readSerializableNoException(f);
	        try
	        {
	        	systemPerf.setProperty("cos.control.version", WrapperShell.version());
	        	systemPerf.setProperty("StartupTime", new Date(startTimestamp));
	        	systemPerf.setDescript(subject!=null?subject:"n/a");
	        	if( WrapperShell.H2Switcher ){
	        		JSONObject h2 = new JSONObject();
	        		this.setDatabaseInfo(h2);
	        		systemPerf.setProperty("h2", h2.toString());
	        	}
	        	systemPerf.setSecurityKey(new String(CosServer.getSecurityKey()));
	        }
	        catch(Exception e)
	        {
	        	Log.err("Failed to get the identity of your service", e);
	        }
	        return systemPerf;
		}
		return null;
    }
    
    /**
     * 加载运行前
     * @throws Exception
     */
    private void loadKernelRunner() throws Exception
    {
    	StringBuffer sb = new StringBuffer("Load kernel programs below:");
    	sb.append("\r\n\t"+this.addModule(new WrapperReportRunner(this){
			public void onFinish()
			{
		        super.printDebug( "["+id+"] Process finish to execute.", null );
		        sendSystemMonitorData();//从data/monitor/out读取最新采集的系统性能数据，发送给监控终端
				if(this.isSuspendWrapper())
				{
					closeAllModules();
				}
			}
    	}.setDependence("Zookeeper")));
    	//启动Zookeeper
    	sb.append("\r\n\t"+this.addModule(new ZookeeperRunner(this)));
    	//启动COS的接口引擎
    	sb.append("\r\n\t"+this.addModule(new COSApiRunner(this)));
    	//启动程序配置引起
    	sb.append("\r\n\t"+this.addModule(new ProgramLoaderRunner(this).setDependence("Zookeeper")));
    	//启动系统监控
    	sb.append("\r\n\t"+this.addModule(new SystemMonitorRunner(this){
			@Override
			public void onFinish() {
	        	if( count_run%1024 == 1){
	        		Log.print("Notify the perf of system to all monitors up to %s.", count_run);
	        	}
	        	sendSystemMonitorData();
			}
    		
    	}));
    	Log.msg(sb.toString());
    }
//    private void runProcess() throws Exception
//    {
//    	try
//    	{
//    		loadConfig();//加载control.xml配置文件
//    	}
//    	catch(Exception e)
//    	{
//    		Log.err("Failed to load the xml of control", e);
//    		Thread.sleep(1000);
//    		return;
//    	}
//    }

    /**
     * 响应预览的日志
     * @param datagramSocket
     * @param reqPacket
     * @param module
     * @param payload
     */
    class ConfigControlXml extends Thread
    {
    	ServerSocket ss = null;
    	ConfigControlXml(ServerSocket ss)
    	{
    		this.ss = ss;
    	}
    	
    	public void run()
    	{
    		Socket socket = null;
    		InputStream is = null;
    		FileOutputStream fos = null;
    		try
    		{
    			F fileControl = new F( ConfigUtil.getWorkPath() + "config/", "control.xml" );
    			ss.setSoTimeout(15000);
    			socket = ss.accept();
    			is = socket.getInputStream();
    			fos = new FileOutputStream( fileControl );
	    		int ch;
	    		while( (ch = is.read()) != -1  )
	    		{
	    			fos.write(ch);
	    		}
//	    		out = socket.getOutputStream();
//	    		out.write("Succeed to config control.xml".getBytes());
			}
			catch( Exception e )
			{
				Log.err(e);
			}
    		finally
    		{
				try
				{
					if( is != null )
					{
						is.close();
					}
                	if( socket != null )
                	{
	            		socket.close();
                	}
                	if( fos != null )
                	{
                		fos.close();
                	}
				}
				catch (IOException e)
				{
				}
				try
				{
					ss.close();
				}
				catch (IOException e)
				{
				}
    		}
    	}
    }

    /**
     * 解压
     * @author focus
     *
     */
    class DecompressFile extends Thread
    {
    	F thefile;//压缩文件
    	F destdir;
    	int decompression;
    	InetAddress addr0;
    	int port0;
    	JSONObject rsp = new JSONObject();
    	
    	DecompressFile(String filepath, String destpath, int decompression, InetAddress addr0, int port0)
    	{
    		this.addr0 = addr0;
    		this.port0 = port0;
    		filepath = filepath.replace("\\", "/");
    		if( filepath.startsWith("/") || filepath.indexOf(":") > 0 )
    		{
        		this.thefile = new F(filepath);
    		}
    		else
    		{
    			this.thefile = new F(ConfigUtil.getWorkPath(), filepath);
    		}
    		
    		if( !destpath.isEmpty() )
    		{
    			if( !destpath.startsWith("/") && destpath.indexOf(":") == -1 )
    			{
    				destdir = new F(ConfigUtil.getWorkPath(), destpath);
    			}
    			else
    			{
    				if( !destpath.isEmpty() ) destdir = new F(destpath);
    			}
    		}
    		this.decompression = decompression;
    	}

    	public void run()
    	{
            Syslog syslog = new Syslog();
    		syslog.setCategory("COSControl");
    		syslog.setAccount(identity);
    		syslog.setLogtype(LogType.运行日志.getValue());

        	StringBuffer logtxt = new StringBuffer("Receive control(decompress "+thefile+" to "+destdir+") from "+ addr0 +"("+ port0 +")" );
    		Decompressor.Progress progress = new Decompressor.Progress() {
				@Override
				public void report(int p) {
					// TODO Auto-generated method stub
					rsp.put("progress", p);
    				response(addr0, port0, rsp);
				}
			};
            try
            {
            	long ts = System.currentTimeMillis();
            	Decompressor.execute(thefile, destdir, progress);
            	rsp.put("decompression", progress.getDecompressFile().getAbsolutePath());
            	ts = System.currentTimeMillis() - ts;
	    		logtxt.append("\r\n\tDuration: "+ts+"毫秒.");
            	rsp.put("duration", ts);
            	rsp.put("details", progress.getLogtxt());
            	logtxt.append( "\r\n\tSucceed to decompress("+ts+"ms, "+progress.getDecompressFile().getAbsolutePath()+")." );
	    		syslog.setLogtext("伺服器【"+addr0+"】请求解压文件"+thefile+"成功");
	    		syslog.setLogseverity(LogSeverity.INFO.getValue());
            }
            catch(Exception e)
            {
    			ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
    			PrintStream ps = new PrintStream(baos);
    			e.printStackTrace(ps);
    			logtxt.append("\r\nFailed to process:");
    			logtxt.append(baos.toString());
    			ps.close();
            	rsp.put("error", e.getMessage());
	    		syslog.setLogseverity(LogSeverity.ERROR.getValue());
	    		syslog.setLogtext("伺服器【"+addr0+"】请求解压文件"+thefile+"出现异常:"+e.getMessage());
            }
            finally
            {
            	response(addr0, port0, rsp);
	    		syslog.setContext(progress.getLogtxt());
	            SyslogClient.submit(syslog);
            }
            Log.msg(logtxt.toString());
    	}
    }
    
    /**
     * 通过UDP接口返回JSON对象
     * @param addr0
     * @param port0
     * @param rsp
     */
	public void response(InetAddress addr0, int port0, JSONObject rsp)
	{
		if( rsp != null )
		try
		{
			byte[] payload = new byte[64*1024];
        	byte[] buffer = rsp.toString().getBytes("UTF-8");
        	Tools.intToBytes(buffer.length, payload, 0, 4);
        	ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gos = new GZIPOutputStream(baos);
            gos.write(buffer);
            gos.flush();
			gos.finish();
			gos.close();
			buffer = baos.toByteArray();
			baos.close();
			int off = 4;
			for(int i = 0; i < buffer.length; i++)
			{
				payload[off++] = buffer[i];
			}
			DatagramPacket response = new DatagramPacket( payload, 0, off, addr0, port0);
			datagramSocket.send(response);
		}
		catch(Exception e)
		{
			Log.err("Failed to response for ", e);
		}
	}
    /**
     * 接收上传文件的服务
     * @author focus
     *
     */
    class CopyFile extends Thread
    {
    	ServerSocket ss = null;
    	F filepath;
    	int decompression;
    	InetAddress addr0;
    	int port0;
    	JSONObject rsp = new JSONObject();
    	
    	CopyFile(ServerSocket ss, String filepath, int decompression, InetAddress addr0, int port0)
    	{
    		this.addr0 = addr0;
    		this.port0 = port0;
    		this.ss = ss;
    		filepath = filepath.replace("\\", "/");
    		if( filepath.startsWith("/") || filepath.indexOf(":") > 0 )
    		{
        		this.filepath = new F(filepath);
    		}
    		else
    		{
    			this.filepath = new F(ConfigUtil.getWorkPath(), filepath);
    		}
    		this.decompression = decompression;
    	}
    	
    	public void run()
    	{
            Syslog syslog = new Syslog();
    		syslog.setCategory("COSControl");
    		syslog.setAccount(identity);
    		syslog.setLogtype(LogType.运行日志.getValue());
    		Socket socket = null;
    		InputStream is = null;
    		FileOutputStream fos = null;
    		StringBuffer logtxt = new StringBuffer("Copy the file of "+filepath+" from cosportal.");
    		long ts = System.currentTimeMillis();
    		Decompressor.Progress progress = new Decompressor.Progress() {
				@Override
				public void report(int p) {
					// TODO Auto-generated method stub
					rsp.put("progress", p);
    				response(addr0, port0, rsp);
				}
			};
    		try
    		{
    			ss.setSoTimeout(15000);
	    		logtxt.append("\r\n\tAccept connect(timeout:15s)...");
    			socket = ss.accept();
    			is = socket.getInputStream();
	    		logtxt.append("\r\n\tSucceed to connect from "+socket.getRemoteSocketAddress());
	    		File dir = filepath.getParentFile();
	    		if( !dir.exists() ) dir.mkdirs();
    			fos = new FileOutputStream( filepath );
	    		int len;
	    		long length = 0;
	    		byte[] payload = new byte[64*1024];
	    		while( (len = is.read(payload, 0, payload.length)) != -1  )
	    		{
	    			length += len;
	    			fos.write(payload, 0, len);
	    			fos.flush();
	    		}
	    		logtxt.append("\r\n\tSucceed to save the file about "+length+" bytes.");
	    		rsp.put("import", true);
	    		syslog.setLogtext("伺服器【"+socket.getRemoteSocketAddress()+"】上传文件"+filepath+"成功");
	    		if( decompression > 0  )
	    		{
		    		logtxt.append("\r\n\tDecompress the file.");
	    			Decompressor.execute(filepath, null, progress);
		    		logtxt.append("\r\n\tSuceed to decompress to "+progress.getDecompressFile().getAbsolutePath()+".");
					rsp.put("progress", 100);
		    		rsp.put("decompression", progress.getDecompressFile().getAbsolutePath());
//	            	rsp.put("details", progress.getLogtxt().toString());
		    		syslog.setLogtext("伺服器【"+socket.getRemoteSocketAddress()+"】上传文件"+filepath+"并解压到"+progress.getDecompressFile().getAbsolutePath());
	    		}
	    		ts = System.currentTimeMillis() - ts;
            	rsp.put("duration", ts);
	    		logtxt.append("\r\n\t解压耗时: "+ts+"毫秒.");
	    		syslog.setLogseverity(LogSeverity.INFO.getValue());
	    		syslog.setContext(logtxt.toString());
	            SyslogClient.submit(syslog);
			}
    		catch( SocketException e )
    		{
				ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
				PrintStream ps = new PrintStream(baos);
				e.printStackTrace(ps);
				logtxt.append("\r\nFailed to handle for ");
				logtxt.append(baos.toString());
				ps.close();
				rsp = null;
    		}
			catch( Exception e )
			{
				String str = e.getMessage().replace("\\", "/");
				rsp.put("error", str);
				ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
				PrintStream ps = new PrintStream(baos);
				e.printStackTrace(ps);
				logtxt.append("\r\nFailed to handle for ");
				logtxt.append(baos.toString());
				ps.close();
			}
    		finally
    		{
				response(addr0, port0, rsp);
				try
				{
					if( is != null )
					{
						is.close();
					}
                	if( socket != null )
                	{
	            		socket.close();
                	}
                	if( fos != null )
                	{
                		fos.close();
                	}
				}
				catch (Exception e)
				{
				}
            	try
				{
					ss.close();
				}
				catch (IOException e)
				{
				}
				Log.msg(logtxt.toString());
	            if( decompression == 8 )
	            {
	            	filepath.delete();
	            }
    		}
    	}
    }

    /**
     * 执行JMAP
     * @param datagramSocket
     */
    class JMAP extends Thread
    {
    	ServerSocket ss = null;
    	private String pid;
    	private File jdkbin;
    	private File logpath;
    	JMAP(ServerSocket ss, Module module)
    	{
			this.ss = ss;
			this.logpath = module.getLogdir();
            JSONObject pids = module.getPids();
        	if( pids.length() > 0 ){
        		Iterator<?> allpids = pids.keys();
				while(allpids.hasNext()){
					this.pid = allpids.next().toString();
					break;
				}
        	}
        	String javabin = module.getJavaBinpath();
        	if( javabin != null ){
        		jdkbin = new File(javabin);
        		if( javabin.endsWith("jre/bin/") || javabin.endsWith("jre\\bin\\") ){
        			jdkbin = new File(jdkbin.getParentFile().getParentFile(), "bin");
        		}
        	}
    	}
    	public void run()
    	{
    		Socket socket = null;
    		OutputStream out = null;
    		InputStream is = null;
            Process process = null;
    		try
    		{
    			ss.setSoTimeout(15000);
    			socket = ss.accept();
    			out = socket.getOutputStream();
    			if( pid != null ){
    	    		File jmapfile = new File(jdkbin, "jmap");
    	    		File dumpfile = new File(logpath, "jmap.dump");
    				ArrayList<String> commands = new ArrayList<String>();
    				commands.add( jmapfile.getAbsolutePath() );
    				commands.add( "-dump:format=b,file="+dumpfile.getAbsolutePath());
    				commands.add( pid );
    				ProcessBuilder pb = new ProcessBuilder( commands );
    				process = pb.start();
    				is = process.getErrorStream();
    				Log.msg(jmapfile.getAbsolutePath()+ " " +pid+ " "+dumpfile.getAbsolutePath());
    				int status = process.waitFor();
    				int len;
    				if( status == 0 ){
        				Log.msg("Found "+dumpfile.getAbsolutePath()+" "+dumpfile.exists()+", "+dumpfile.length());
    					if( dumpfile.exists() ){
    	    				is = new FileInputStream(dumpfile);
    	    				out.write(0);
    	    	    		byte[] payload = new byte[64*1024];//开辟64K的缓冲区
    	    				while( (len = is.read(payload, 0, payload.length)) != -1  )
    	    				{
    	    					out.write(payload, 0, len);
    	    				}
    	    				out.flush();
    					}
    					else {
    						status = 127;
    					}
    				}
    				if( status > 0 ){
	    				out.write(status);
						out.write("执行jmap失败\r\n".getBytes("UTF-8"));
	    				while( (len = is.read()) != -1  )
	    				{
	    					out.write(len);
	    				}
    				}
    			}
    			else{
    				out.write(127);out.flush();
    				out.write("pid不存在".getBytes());
    			}
			}
			catch( Exception e )
			{
				if( out != null ){
					try{
						out.write(127);
						out.flush();
						PrintWriter writer = new PrintWriter(out);
						e.printStackTrace(writer);
						writer.flush();
						writer.close();
					}
					catch(Exception e1){
					}
				}
			}
    		finally
    		{
				try
				{
					if( is != null )
					{
						is.close();
					}
                	if( socket != null )
                	{
	            		socket.close();
                	}
                	if( out != null )
                	{
                		out.close();
                	}
				}
				catch (IOException e)
				{
				}
	        	try
				{
					ss.close();
				}
				catch (IOException e)
				{
				}
    		}
    	}
    }
    /**
     * 执行JMAP
     * @param datagramSocket
     */
    class JStack extends Thread
    {
    	ServerSocket ss = null;
    	private String pid;
    	private File jdkbin;
    	JStack(ServerSocket ss, Module module)
    	{
			this.ss = ss;
            JSONObject pids = module.getPids();
            if( module.getState() == Module.STATE_STARTUP ){
            	if( pids.length() > 0 ){
            		Iterator<?> allpids = pids.keys();
    				while(allpids.hasNext()){
    					this.pid = allpids.next().toString();
    					break;
    				}
            	}
            }
        	String javabin = module.getJavaBinpath();
        	if( javabin != null ){
        		jdkbin = new File(javabin);
        		if( javabin.endsWith("jre/bin/") || javabin.endsWith("jre\\bin\\") ){
        			jdkbin = new File(jdkbin.getParentFile().getParentFile(), "bin");
        		}
        	}
    	}
    	/**
    	 * 
    	 */
    	public void run() {
    		Socket socket = null;
    		OutputStream out = null;
    		InputStream is = null;
            Process process = null;
    		try
    		{
    			ss.setSoTimeout(15000);
    			socket = ss.accept();
    			out = socket.getOutputStream();
        		Log.msg( "Jstack "+ pid );
    			if( pid != null ){
    	    		File jstackfile = new File(jdkbin, "jstack");
    				ArrayList<String> commands = new ArrayList<String>();
    				commands.add( jstackfile.getAbsolutePath() );
    				commands.add( "-l");
    				commands.add( pid );
    				ProcessBuilder pb = new ProcessBuilder( commands );
    				pb.redirectErrorStream( true );
    				process = pb.start();
    				is = process.getInputStream();
    				int ch;
    				out.write(0);out.flush();
    				out.write((jstackfile.getAbsoluteFile()+" -l "+pid).getBytes());
    				out.write('\r');
    				out.write('\n');
    	    		byte[] payload = new byte[64*1024];//开辟64K的缓冲区
    				while( (ch = is.read(payload, 0, payload.length)) != -1  )
    				{
    					out.write(payload, 0, ch);
    				}
    				out.flush();
    			}
    			else{
    				out.write(9527);out.flush();
    				out.write("pid不存在".getBytes());
    			}
			}
			catch( Exception e )
			{
				if( out != null ){
					try{
						out.write(127);
						out.flush();
						PrintWriter writer = new PrintWriter(out);
						e.printStackTrace(writer);
						writer.flush();
						writer.close();
					}
					catch(Exception e1){
					}
				}
			}
    		finally
    		{
				try
				{
					if( is != null )
					{
						is.close();
					}
                	if( socket != null )
                	{
	            		socket.close();
                	}
                	if( out != null )
                	{
                		out.close();
                	}
				}
				catch (IOException e)
				{
				}
	        	try
				{
					ss.close();
				}
				catch (IOException e)
				{
				}
    		}
    	}
    }
    /**
     * 响应预览的日志
     * @param datagramSocket
     * @param reqPacket
     * @param module
     * @param payload
     */
    class PreviewControlXml extends Thread
    {
    	ServerSocket ss = null;
    	PreviewControlXml(ServerSocket ss)
    	{
			this.ss = ss;
    	}
    	public void run()
    	{
    		Socket socket = null;
    		OutputStream out = null;
    		InputStream is = null;
    		try
    		{
    			F fileControl = new F( ConfigUtil.getWorkPath() + "config/", "control.xml" );
    			ss.setSoTimeout(15000);
    			socket = ss.accept();
    			out = socket.getOutputStream();
	    		is = new FileInputStream( fileControl );
	    		int ch;
	    		while( (ch = is.read()) != -1  )
	    		{
	    			out.write(ch);
	    		}
			}
			catch( Exception e )
			{
				Log.err(e);
			}

    		finally
    		{
				try
				{
					if( is != null )
					{
						is.close();
					}
                	if( socket != null )
                	{
	            		socket.close();
                	}
                	if( out != null )
                	{
                		out.close();
                	}
				}
				catch (IOException e)
				{
				}
	        	try
				{
					ss.close();
				}
				catch (IOException e)
				{
				}
    		}
    	}
    }
    
    /**
     * 清除指定模块的日志
     * @param module
     * @param id
     */
    private void clearLogFiles(String id)
    {
    	ArrayList<F> files = this.getLogFiles(id);
    	for( int i = 0; i < files.size(); i++ )
    	{
    		F file = files.get(i);
    		if( file.getName().endsWith(".pid") ||
    			file.getName().endsWith(".gc") ){
    			continue;//PID文件和GC文件不删除
    		}
    		file.delete();
    	}
    }

	/**
	 * 返回最新的日志文件
	 * @param module 运行模块
	 * @param id 日志的目录
	 * @return
	 */
	private ArrayList<F> getLogFiles(String id)
	{
		Module module = modules.get(id);
    	ArrayList<F> logs = new ArrayList<F>();
    	if( module != null )
    	{
	    	if( module.getLogfile() != null && !module.getLogfile().isEmpty() )
	    	{
	    		String logpath = module.getLogfile();
	    		String p = null;
	    		int k = logpath.indexOf("*"); 
	    		if( k != -1 )
	    		{
	    			int i = logpath.lastIndexOf("/");
	    			if( i != -1 )
	    			{
	    				k += 1;
	    				int l = logpath.lastIndexOf('*');
	    				l += 1;
	    				//p = k<l?logpath.substring(k+1, l+1):logpath.substring(k+1);
	    				p = k<l?logpath.substring(k, l-1):(k==logpath.length()?logpath.substring(i+1, k-1):logpath.substring(k));
	    				logpath = logpath.substring(0, i);
	    			}
	    			else
	    			{
	    				logpath = null;
	    			}
	    		}
	    		F path = new F(logpath);
	    		try
	    		{
		    		if( path.exists() )
		    		{
		    			F files[] = path.listFiles();
		    			for( F file : files )
		    			{
		    				if( file.isFile() && ( p == null || p.isEmpty() || file.getName().indexOf(p) != -1))
		    				{
		    					logs.add(file);
		    				}
		    			}
		    		}
	    		}
	    		catch(Exception e)
	    		{
	    			Log.err(e);
	    		}
	    	}
	    	else{
		    	int i = module.getStartupCommands().indexOf("org.apache.catalina.startup.Bootstrap");
		    	if( i != -1 )
				{
					for( String command : module.getStartupCommands() )
					{
						if( command.startsWith("-Dcatalina.home=") )
						{
							F path = new F(command.substring("-Dcatalina.home=".length())+"/logs/");
							F files[] = path.listFiles();
							for( F file : files )
							{
								if( file.isFile() && !logs.contains(file) )
								{
									logs.add(file);
								}
							}
							break;
						}
					}
				}
	    	}
    	}
    	
		F path = new F(ConfigUtil.getWorkPath()+"log/"+id);
		if( path.exists() )
		{
			F files[] = path.listFiles(new FilenameFilter(){
				public boolean accept(java.io.File dir, String name)
				{
					return !name.endsWith(".gc");
				}
				
			});
			for( F file : files )
			{
				if( file.isFile() && !logs.contains(file) )
				{
					logs.add(file);
				}
			}
		}
		if( logs.isEmpty() )
		{
    		path = new F(ConfigUtil.getWorkPath()+"log/");
			F files[] = path.listFiles();
			for( F file : files )
			{
				if( file.isFile() && !logs.contains(file) )
				{
					logs.add(file);
				}
			}
		}
		if( logs.size() > 1 )
		{
    		QuickSort sorter = new QuickSort(){

    			public boolean compareTo(Object left, Object right)
    			{
    				java.io.File lfile = (java.io.File)left;
    				java.io.File rfile = (java.io.File)right;
    				
    				return lfile.lastModified() > rfile.lastModified();
    			}
    		};
    		sorter.sort(logs);
		}
		return logs;
	}

	/**
     * 创建目录
     * @param datagramSocket
     * @param addr
     * @param port
     * @param dirpath
     * @param payload
     */
    public void responseMakeDir(DatagramSocket datagramSocket, InetAddress addr0, int port0, String dirpath, byte[] payload)
    {
    	String responseMessage = "Succeed to make dir("+dirpath+").";
    	boolean result = false;
    	try
		{
	    	F path = new F(dirpath);
	    	if( path.exists() )
	    	{
	    		responseMessage = "您要创建的目录("+dirpath+")已经存在.";
	    		Log.war("Failed to mkdir for exists path("+dirpath+").");
	    	}
	    	else
	    	{
	    		if( path.mkdirs() ){
	    			result = true;
	    			responseMessage = "创建目录("+dirpath+")成功.";
	    			Log.war("Succeed to mkdir("+dirpath+").");
	    		}
	    		else{
	    			responseMessage = "创建目录("+dirpath+")失败，请联系系统管理员.";
	    			Log.war("Failed to mkdir("+dirpath+") for unknown reason.");
	    		}
	    	}
		}
		catch (Exception e)
		{
			Log.err("Failed to mkdir("+dirpath+").", e);
			responseMessage = "创建目录("+dirpath+")出现异常: "+e;
		}
    	
    	try{
        	int offset = 0;
        	byte[] rsp = responseMessage.getBytes("UTF-8");
        	payload[offset++] = (byte)(result?1:0);
        	payload[offset++] = (byte)rsp.length;
        	offset = Tools.copyByteArray(rsp, payload, offset);
        	DatagramPacket response = new DatagramPacket(payload, 0, offset, addr0, port0 );
			datagramSocket.send( response );
    	}
		catch (IOException e)
		{
			Log.err(e);
		}
    }
	/**
     * 创建文件
     * @param datagramSocket
     * @param addr
     * @param port
     * @param filepath
     * @param payload
     */
    public void responseMakeFile(DatagramSocket datagramSocket, InetAddress addr0, int port0, String filepath, byte[] payload)
    {
    	String responseMessage = "Succeed to make file("+filepath+").";
    	boolean result = false;
    	try
		{
	    	F path = new F(filepath);
	    	if( path.exists() )
	    	{
	    		responseMessage = "您要创建的文件("+filepath+")已经存在.";
	    		Log.war("Failed to mkfile("+filepath+") for exists path.");
	    	}
	    	else
	    	{
	    		if( path.createNewFile() ){
	    			responseMessage = "创建文件("+filepath+")成功.";
	    			result = true;
	    			Log.war("Succeed to mkfile("+filepath+").");
	    		}
	    		else{
	    			responseMessage = "创建文件("+filepath+")失败，请联系系统管理员.";
	    			Log.war("Failed to mkfile("+filepath+") for unknown reason.");
	    		}
	    	}
		}
		catch (Exception e)
		{
			Log.err("Failed to mkdir("+filepath+").", e);
			responseMessage = "创建文件("+filepath+")出现异常: "+e;
		}

    	try{
        	int offset = 0;
        	byte[] rsp = responseMessage.getBytes("UTF-8");
        	payload[offset++] = (byte)(result?1:0);
        	payload[offset++] = (byte)rsp.length;
        	offset = Tools.copyByteArray(rsp, payload, offset);
        	DatagramPacket response = new DatagramPacket(payload, 0, offset, addr0, port0 );
			datagramSocket.send( response );
    	}
		catch (IOException e)
		{
			Log.err(e);
		}
    }
	/**
     * 删除路径
     * @param datagramSocket
     * @param addr
     * @param port
     * @param pathdelete
     * @param payload
     */
    public void responseDeletePath(DatagramSocket datagramSocket, InetAddress addr0, int port0, String pathdelete, byte[] payload)
    {
    	String responseMessage = "Succeed to delete path("+pathdelete+").";
    	boolean result = false;
    	try
		{
	    	F path = new F(pathdelete);
	    	if( !path.exists() )
	    	{
	    		responseMessage = "不存在你要删除的文件或文件夹("+pathdelete+").";
	    		Log.war("Failed to delete for not exists path("+pathdelete+").");
	    	}
	    	else
	    	{
	    		int count = IOHelper.deleteFile(path);
	    		responseMessage = "一共删除"+count+"个文件从目录("+pathdelete+").";
	    		Log.msg("Total "+count+" files are deleted from "+pathdelete+".");
	    		result = true;
	    	}
		}
		catch (Exception e)
		{
			Log.err("Failed to delete.", e);
			responseMessage = "删除的文件或文件夹("+pathdelete+")出现异常: "+e;
		}
    	
    	try{
        	int offset = 0;
        	byte[] rsp = responseMessage.getBytes("UTF-8");
        	payload[offset++] = (byte)(result?1:0);
        	payload[offset++] = (byte)rsp.length;
        	offset = Tools.copyByteArray(rsp, payload, offset);
        	DatagramPacket response = new DatagramPacket(payload, 0, offset, addr0, port0 );
			datagramSocket.send( response );
    	}
		catch (IOException e)
		{
			Log.err(e);
		}
    }
    
    /**
     * 返回文件列表
     * @param datagramSocket
     * @param addr
     * @param port
     * @param module
     * @param id
     * @param payload
     */
    private void responseFiles(DatagramSocket datagramSocket, InetAddress addr0, int port0, String filepath, byte[] payload)
    {   
    	if( filepath.isEmpty() ) filepath = ConfigUtil.getWorkPath();
    	filepath = F.getRealpath(filepath);
    	File dir = new File(filepath);
    	StringBuilder dirQuery = new StringBuilder("ll the dir "+dir);
    	try
		{
    		DirState state = this.dirStateQuery.get(dir);
			if( state == null ){
				dirQuery.append(String.format("\r\n\t[%s] Not found the the state of dir.", dir));
			}
			else{
				dirQuery.append(String.format("\r\n\t[%s] The size of dir is %s, the count of fiels is %s.", dir, Tools.bytesScale(state.size()), state.count()));
			}
    		if( state == null ){
    			this.dirStateQuery.createScan(filepath);
    		}
	    	String[] filenames = dir.list();
	    	if( filenames == null )
	    	{
	    		dirQuery.append("\r\n  Not found the files.");
				payload[0] = (byte)0xFE;
		    	DatagramPacket response = new DatagramPacket(payload, 0, 1, addr0, port0 );
				datagramSocket.send( response );
	    		return;
	    	}
    		dirQuery.append("\r\n  Found "+filenames.length+" files or dirs... ...");
//	    	boolean isRoot = filepath.equals("/") || filepath.endsWith(":/");
			if( filenames.length > 1 )
			{
	    		QuickSort sorter = new QuickSort(){
	    			public boolean compareTo(Object left, Object right)
	    			{
	    				String lfile = left.toString();
	    				String rfile = right.toString();
	    				
	    				return lfile.compareTo(rfile)<0;
	    			}
	    		};
	    		sorter.sort(filenames);
			}
			int offset = 0;
			payload[offset++] = (byte)0xFF;
			int _offset = offset, count = 0;
			Tools.intToBytes(filenames.length, payload, offset, 4);
			offset += 4;
			long length = 0;
	    	for( String filename : filenames )
	    	{
	    		if( offset > 60*1024 ){
					dirQuery.append("\r\n  Skip offset "+offset+", count is "+count);
					Tools.intToBytes(count, payload, _offset, 4);
	    			break;
	    		}
	    		filename = new String(filename.getBytes("UTF-8"), "UTF-8");
	    		File file = new File(dir, filename);
	    		if( file.isHidden() ){
	    			continue;
	    		}
	    		count += 1;
	    		if( file.isFile() )
	    		{
	    			payload[offset++] = 0;
	    			int offset1 = Tools.copyByteArray(file.getPath().getBytes("UTF-8"), payload, offset+2);
	    			int len = offset1-offset-2;
	    			Tools.intToBytes(len, payload, offset, 2);
	    			offset += (2 + len);
	    			Tools.longToBytes(file.lastModified(), payload, offset, 8);
	    			offset += 8;
	    			Tools.longToBytes(file.length(), payload, offset, 8);
	    			offset += 8;
	    			length += file.length();
	    		}
	    		else
	    		{
	    			payload[offset++] = 1;
	    			int offset1 = Tools.copyByteArray(file.getPath().getBytes("UTF-8"), payload, offset+2);
	    			int len = offset1-offset-2;
	    			Tools.intToBytes(len, payload, offset, 2);
	    			offset += (2 + len);
	    			state = this.dirStateQuery.get(file);
	    			if( state == null ){
	    				dirQuery.append(String.format("\r\n\t[%s] Not found the the state of dir.", file.getPath()));
	    			}
	    			else{
	    				length += state.size();
	    				dirQuery.append(String.format("\r\n\t[%s] The size of dir is %s, the count of fiels is %s.", file.getPath(), Tools.bytesScale(state.size()), state.count()));
	    			}
	    			Tools.intToBytes(state!=null?state.count():0, payload, offset, 4);
	    			offset += 4;
	    			Tools.longToBytes(state!=null?state.size():0, payload, offset, 8);
	    			offset += 8;
	    		}
	    	}
			Tools.intToBytes(count, payload, _offset, 4);
			Tools.intToBytes(filenames.length, payload, offset, 4);
			offset += 4;
			Tools.longToBytes(length, payload, offset, 8);
			offset += 8;
			if(MyJDK.isLinux()){
				byte[] buffer = Tools._os_exec(new String[]{"ls","-l",filepath.endsWith("/")?filepath:(filepath+"/")});
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				GZIPOutputStream gos = new GZIPOutputStream(baos);
				gos.write(buffer);
				gos.flush();
				gos.finish();
				gos.close();
				buffer = baos.toByteArray();
				if( offset + buffer.length + 5 <= payload.length-offset ){
					payload[offset++] = 100;
		        	Tools.intToBytes(buffer.length, payload, offset, 4);
		        	offset += 4;
					baos.close();
					for(int i = 0; i < buffer.length; i++)
					{
						payload[offset++] = buffer[i];
					}
				}
				else{
					dirQuery.append("\r\n  Skip ls-l for offset "+offset+", gzip "+buffer.length);
					payload[offset++] = -1;
				}
			}
			else{
				payload[offset++] = -1;
			}
			dirQuery.append("\r\n  Found "+filenames.length+" files, the length of payload is "+offset);
	    	DatagramPacket response = new DatagramPacket(payload, 0, offset, addr0, port0 );
			datagramSocket.send( response );
		}
		catch (Exception e)
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
			PrintStream ps = new PrintStream(baos);
			e.printStackTrace(ps);
			ps.close();
			dirQuery.append("\r\n  Failed to ll the files for exception:");
			dirQuery.append(baos.toString());
		}
    	finally{
	    	Log.msg(dirQuery.toString());
    	}
    }
    /**
     * 返回文件数据
     * @param addr
     * @param port
     * @param filepath
     * @param zip
    private void responseFile(ServerSocket ss, String filepath, boolean zip)
    {
    	ResponseFile thread = new ResponseFile(ss, filepath, zip);
    	thread.start();
    }
     */

    /**
     * 受理COSPortal的下载文件或文件夹的请求
     * @param datagramSocket
     * @param reqPacket
     * @param module
     * @param payload
     */
    class FetchFile extends Thread
    {
    	ServerSocket ss;
    	String filepath;
    	boolean zip;//是否压缩
    	int page = -1;//页码0表示最后一页
    	long pageSize = -1;//分页大小
    	HashMap<String, Boolean> filenames = new HashMap<String, Boolean>();
    	
    	FetchFile(ServerSocket ss, String filepath, boolean zip, ArrayList<String> filenames)
    	{
			this.ss = ss;
    		this.filepath = filepath;
    		this.zip = zip;
    		if( filenames != null )
    			for(String filename : filenames)
    				this.filenames.put(filename, null);
    	}

    	FetchFile(ServerSocket ss, String filepath, int page, long pageSize)
    	{
			this.ss = ss;
    		this.filepath = filepath;
    		this.zip = false;
    		this.page = page - 1;
    		this.pageSize = pageSize;
    	}
    	/**
    	 * 
    	 * @param dir
    	 * @param path
    	 * @param zipOS
    	 * @param buffer
    	 * @throws IOException
    	 */
    	private void makeZipFile(java.io.File dir, String path, ZipOutputStream zipOS, byte[] buffer) throws IOException
    	{
    		java.io.File[] files = dir.listFiles();
    		if( files != null )
    			for(java.io.File file : files)
    			{
					if( !filenames.isEmpty() && !filenames.containsKey(file.getName()) )
						continue;
    				if( file.isFile() )
    				{
    					try{
    						FileInputStream is = new FileInputStream(file);
    						ZipEntry entry = new ZipEntry(path+file.getName());
    						entry.setTime(file.lastModified());
    						zipOS.putNextEntry(entry);
    						int len = 0;
    						while( (len = is.read(buffer, 0, buffer.length)) != -1 )
    						{
    							zipOS.write(buffer, 0, len); 
    						}
    						zipOS.flush();
    						zipOS.closeEntry();
    						is.close();
    					}
    					catch(Exception e){
    					}
    				}
    				else
    				{
    					this.makeZipFile(file, path+file.getName()+"/", zipOS, buffer);
    				}
    			}
    	}
    	
    	public void run()
    	{
    		F file = null;
    		if( filepath.startsWith("/") || filepath.indexOf(":/") == 1 ){
    			file = new F(filepath);
    		}
    		else{
    			file = new F(ConfigUtil.getWorkPath(), filepath);
    		}
    		Socket socket = null;
    		FileInputStream fis = null;
    		OutputStream out = null;
    		StringBuffer logtxt = new StringBuffer();
    		try
    		{
    			ss.setSoTimeout(15000);
    			socket = ss.accept();
    			logtxt.append("Succeed to connect "+socket.getInetAddress().getHostAddress()+":"+socket.getPort()+" and beign to fetch the data of file("+filepath+").");
    			out = socket.getOutputStream();
//    			String encoding = System.getProperty("file.encoding", "UTF-8");
	        	if( file != null && file.exists() )
	        	{
    	    		byte[] payload = new byte[64*1024];//开辟64K的缓冲区
	        		if( page == -1 )
	        		{//非分页
	                    Syslog syslog = new Syslog();
	            		syslog.setCategory("COSControl");
	            		syslog.setAccount(identity);
	            		syslog.setLogtype(LogType.运行日志.getValue());
	            		syslog.setLogseverity(LogSeverity.INFO.getValue());
	        			if( file.isDirectory() )
	        			{
	        				logtxt.append("\r\n\t"+filenames);
	    	        		out.write(11);
	    	        		Tools.longToBytes(IOHelper.getDirSize(file), payload, 0, 8);
	    	        		out.write(payload, 0, 8);
	    	        		out.flush();
	    	    			CheckedOutputStream cos = new CheckedOutputStream(out, new CRC32());
	    	    			ZipOutputStream zipOS = new ZipOutputStream(cos);
	    	    			this.makeZipFile(file, "", zipOS, payload);
	    	        		zipOS.close();
	    	        		logtxt.append("\r\n\tSucceed to make the zip of dir.");
	    		    		syslog.setLogtext("伺服器【"+socket.getRemoteSocketAddress()+"】请求下载目录"+filepath+"成功");
	        			}
	        			else
	        			{
	        	    		int len;
	        	    		try{
		    	    			fis = new FileInputStream(file);
		    	        		Tools.longToBytes(file.length(), payload, 0, 8);
			    	        	if( this.zip )
			    	        	{//如果文件小于了500K，那么网页直接打开，首字节设为0
			    	        		out.write(11);
			    	        		out.write(payload, 0, 8);
			    	        		out.flush();
			    	    			CheckedOutputStream cos = new CheckedOutputStream(out,new CRC32());
			    	    			ZipOutputStream zipOS = new ZipOutputStream(cos);
			    	    			zipOS.putNextEntry(new ZipEntry(file.getName()));
			        	    		while( (len = fis.read(payload, 0, payload.length)) != -1 )
			    	        		{
			    	        			zipOS.write(payload, 0, len); 
			    	        		}
			    	        		zipOS.flush();
			    	        		zipOS.closeEntry();
			    	        		zipOS.close();
			    	        		logtxt.append("\r\n\tSucceed to fetch the zip of file.");
			    		    		syslog.setLogtext("伺服器【"+socket.getRemoteSocketAddress()+"】请求压缩下载文件"+filepath+"成功");
			    	        	}
			    	        	else
			    	        	{//否则必须压缩后传送，首字节设为1
			    	        		out.write(10);
			    	        		out.write(payload, 0, 8);
			    	        		out.flush();
			        	    		while( (len = fis.read(payload, 0, payload.length)) != -1 )
			    	        		{
			    	        			out.write(payload, 0, len); 
			    	        		}
			        	    		out.flush();
			    	        		logtxt.append("\r\n\tSucceed to fetch the file of unzip.");
			    		    		syslog.setLogtext("伺服器【"+socket.getRemoteSocketAddress()+"】请求下载文件"+filepath+"成功");
			    	        	}
	        	    		}
			        		catch (FileNotFoundException fnfe){
			        			out.write(-1);
		    	        		out.write(fnfe.getMessage().getBytes("UTF-8"));
	        	        		logtxt.append("\r\n\tFailed to page the file for "+fnfe.getMessage());
			        		}
	        			}
	        			SyslogClient.submit(syslog);
	        		}
	        		else
	        		{
    	        		//Add by liuxue below 最新版本传文件分页大小(pageSize)和页码(page)，查看指定位置的文件
    	        		try{
    	        			fis = new FileInputStream( file );
        	        		out.write(0);
    	        			long skip = 0;//file.length() - pageSize;
//            	        	skip = page>-1?pageSize*page:0;
            	        	if( pageSize > 0 ) skip = page*pageSize;
            	        	skip = skip>file.length()?(file.length() - pageSize):skip;
            	        	// pageSize;//分页大小，每次只输出一页的信息
            	        	if( skip < 0 ) skip = 0;
//            	        	else
//            	        	{
//            	        		out.write(("<!-- below skip "+skip+", totoal "+file.length()+"-->\r\n\r\n").getBytes());
//            	        	}
            	        	fis.skip( skip );
            	    		int len;
        	        		logtxt.append("\r\n\tRead data(skip="+skip+",page="+page+",pageSize="+pageSize+").");
        	        		int size = (int)pageSize;
            	    		while( (len = fis.read(payload, 0, payload.length)) != -1 )
            	    		{
            	    			if( size > 0 )
            	    			{
            	    				size -= len;
            	    				if( size < 0 ) len = (int)pageSize;
            	    			}
            	    			out.write(payload, 0, len);
            	    			out.flush();
            	    			if( size <= 0 ) break;
            	    		}
        	        		logtxt.append("\r\n\tSucceed to page the file by skip "+skip);
    	        		}
		        		catch (FileNotFoundException fnfe){
	    	        		out.write(-1);
	    	        		out.write(fnfe.getMessage().getBytes("UTF-8"));
        	        		logtxt.append("\r\n\tFailed to page the file for "+fnfe.getMessage());
		        		}
        	    		out.flush();
	        		}
	        		logtxt.append("\r\nCOSPortal("+socket.getInetAddress().getHostAddress()+":"+socket.getPort()+") fetch the file.");
	        	}
	        	else
	        	{
	        		logtxt.append("Failed to response file("+filepath+") to "+socket.getInetAddress().getHostAddress()+":"+socket.getPort()+" for not exist.");
	        		out.write(0);
		    		out.write(("Failed to response file("+filepath+") for not exist.").getBytes());
	        	}
    		}
    		catch( Exception e )
    		{
				ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
				PrintStream ps = new PrintStream(baos);
				e.printStackTrace(ps);
				logtxt.append("\r\nFailed to fetch the file for ");
				logtxt.append(baos.toString());
				ps.close();
    			if( out != null )
    			{
    				e.printStackTrace(new PrintWriter(out));
    			}
    			Log.err(logtxt.toString());
    		}
    		finally
    		{
				try
				{
					if( fis != null )
					{
						fis.close();
					}
                	if( socket != null )
                	{
	            		socket.close();
                	}
                	if( out != null )
                	{
                		out.close();
                	}
				}
				catch (IOException e)
				{
				}
	        	try
				{
					ss.close();
				}
				catch (IOException e)
				{
				}
    		}
    	}
    }

    /**
     * 受理COSPortal的下载文件或文件夹的请求
     * @param datagramSocket
     * @param reqPacket
     * @param module
     * @param payload
     */
    class FetchGCHisotry extends Thread
    {
    	ServerSocket ss;
    	String id;
    	
    	FetchGCHisotry(ServerSocket ss, String id)
    	{
			this.ss = ss;
    		this.id = id;
    	}
    	
    	public void run()
    	{
    		Socket socket = null;
    		OutputStream out = null;
    		StringBuffer logtxt = new StringBuffer();
    		try
    		{
    			ss.setSoTimeout(15000);
    			socket = ss.accept();
    			logtxt.append("Succeed to connect "+socket.getInetAddress().getHostAddress()+":"+socket.getPort()+" and beign to response the data of gc("+id+").");
    			out = socket.getOutputStream();
        		MonitorGCHistory history = new MonitorGCHistory(id);
            	ObjectOutputStream oos = new ObjectOutputStream(out);
    	        oos.writeObject(history);
    	        oos.flush();
    		}
    		catch( Exception e )
    		{
    			Log.err("Failed to fetch the history of gc "+id, e);
    		}
    		finally
    		{
				try
				{
                	if( socket != null )
                	{
	            		socket.close();
                	}
                	if( out != null )
                	{
                		out.close();
                	}
				}
				catch (IOException e)
				{
				}
	        	try
				{
					ss.close();
				}
				catch (IOException e)
				{
				}
    		}
    	}
    }    
    /**
     * 向请求端返回日志列表
     * @param datagramSocket
     * @param addr
     * @param port
     * @param module
     * @param id
     * @param payload
     */
    private void responseLogList(DatagramSocket datagramSocket, InetAddress addr0, int port0, String id, byte[] payload)
    {
    	int offset = 0;
    	ArrayList<F> files = this.getLogFiles(id);
    	try
		{
    		int max = 60*1024;
        	for( int i = 0; i < files.size(); i++ )
        	{
        		if( offset > max ){
    				Log.war("["+id+"]Skip the response of log-list("+files.size()+", "+i+") for offset is "+offset);
        			break;
        		}
        		F file = files.get(i);
        		if( file.isFile() && file.length() > 0 )
        		{
        			byte[] buf = file.getAbsolutePath().getBytes();
        			int offset1 = Tools.copyByteArray(buf, payload, offset+2);
        			if( offset1 == -1 ){
        				Log.war("["+id+"]Skip the file(name:"+file.getAbsolutePath()+","+buf.length+") of logs for offset is "+offset);
        				break;
        			}
        			int len = offset1-offset-2;
        			Tools.intToBytes(len, payload, offset, 2);
        			offset += 2 ;
        			offset += len;
        			Tools.longToBytes(file.length(), payload, offset, 8);
        			offset += 8;
        		}
        	}
        	payload[offset++] = -1;
        	DatagramPacket response = new DatagramPacket(payload, 0, offset, addr0, port0 );
			datagramSocket.send( response );
		}
		catch (Exception e)
		{
			Log.err("["+id+"]Failed to response the log-list("+files.size()+") from "+addr0+":"+port0, e);
		}
    }
    /**
     * 启动控制监听
     */
	private DatagramSocket datagramSocket = null;//接收监听控制指令
    private void startControlListener()
    {
    	Thread thread = new Thread()
    	{
    	    public void run()
    	    {
    	        try
    	        {
    	        	byte[] payload = new byte[64*1024];
    	        	datagramSocket = new DatagramSocket( controlPort );
    	        	Log.msg( "Start control listener by "+controlPort+"." );
    	        	controlPort += 5;//别开ZK的两个端节点
	                SysalarmClient.autoconfirm("Sys", WrapperShell.ModuleID+"_Startup", "主控程序正常启动监控端口工作正常");
    	            while( opened )
    	            {
    	            	datagramSocket.setSoTimeout(0);
    	                DatagramPacket getPacket = new DatagramPacket( payload, payload.length );
    	                datagramSocket.receive( getPacket );
    	                byte command = payload[0];
    	                payload[getPacket.getLength()] = 0;
    	                InetAddress addr0 = getPacket.getAddress();
    	                int port0 = getPacket.getPort();
    	                receiveControl(command, payload, addr0, port0);
    	            }
    	        }
    	        catch( Exception e )
    	        {
    	            Log.err( "Close the listener of control for "+e );
    	            if( !isClosing() ){
    	            	Module module = modules.get("COSPortal");
    	            	if( module == null || module.isExecuting() )
    	            	{
    	            		Sysalarm alarm = new Sysalarm();
    	            		alarm.setSysid("Sys");
    	            		alarm.setSeverity(AlarmSeverity.ORANGE.getValue());
    	            		alarm.setType(AlarmType.S.getValue());
    	            		alarm.setId(WrapperShell.ModuleID+"_Startup");
    	            		alarm.setTitle("主控控制监听被关闭");
    	            		alarm.setText("【COS主控引擎"+WrapperShell.version()+"】 监听("+datagramSocket.getLocalPort()+")被关闭.");
    	            		alarm.setCause("系统管理员停止COS运行");
    	            		SysalarmClient.send(alarm);//向系统管理员发送告警
    	            	}
    	            }
    	        }
    	        finally
    	        {
    	        	if( datagramSocket != null )
    	        	{
    	        		datagramSocket.close();
    	        	}
    	        }
	        	Log.msg( "End control listener by "+datagramSocket.getLocalPort()+"." );
    	    }
    	};
    	thread.start();
    }
    
    /**
     * 接收并处理主控指令 
     * @param command
     * @param payload
     * @param addr0
     * @param port0
     */
    private void receiveControl(byte command, byte payload[], InetAddress addr0, int port0 )
    {
    	DatagramPacket response = null;
    	try
    	{
	    	switch( command )
	    	{
	    	//重启
			case Command.CONTROL_RESTART:
			{
	            byte length = payload[1];
	            String id = new String( payload, 2, length);
	            Log.msg( "Receive control(restart:"+id+") from "+ addr0 +"("+ port0 +")" );
	            if( "COSControl".equals(id) || "EMAControl".equals(id) )
	            {
	            	doRestart();
	            }
	            else
	            {
		            Module module = modules.get(id);
		            if( module != null )
		            {
		    			response = new DatagramPacket(payload, 0, 2 + length, addr0, port0 );
		    			datagramSocket.send( response );
		    			module.restartup();
		            }
	            }
				break;
			}
			//关闭所有的服务
			case Command.CONTROL_CLOSEALL:
			{
//	            Log.msg( "Receive control(close all modules) from "+ addr0 +"("+ port0 +")" );
	            Enumeration<?> enu = modules.elements();
	            while( enu.hasMoreElements() )
	            {
	                Module module = ( Module ) enu.nextElement();
	                module.close();
	            }
				break;
	    	}
			case Command.CONTROL_RESTARTALL:
			{
	            Log.msg( "Receive control(restart) from "+ addr0 +"("+ port0 +")" );
            	doRestart();
				break;
	    	}
			case Command.CONTROL_UPGRADE:
			{
	            Log.msg( "Receive control(upgrade: "+(payload[1]==4?"yes":"no")+") from "+ addr0 +"("+ port0 +")" );
	            doUpgrader(addr0, port0, payload[1]==4);
	            break;
			}
			case Command.CONTROL_UPGRADECHECKE:
			{
	            Log.msg( "Receive control(check-upgrade) from "+ addr0 +"("+ port0 +")" );
				Upgrader upgrader = new Upgrader(addr0, port0);
				upgrader.start();
				break;
			}
	    	//控制h2数据库
			case Command.CONTROL_H2:
			{//0停止，1启动，2创建数据库集群
				byte length = payload[2];
				String param = new String( payload, 3, length);
	            Log.msg( "Receive control(h2: "+(payload[1])+", "+param+") from "+ addr0 +"("+ port0 +")");
	            if( payload[1] == 0 || payload[1] == 1 || payload[1] == 2){
		            Module module = modules.get("COSApi");
		            if( module != null )
		            {
		            	module.shutdown();
		            }
	            }
	            String result = setDatabaseStatus(payload[1], param);
            	JSONObject h2 = new JSONObject();
            	this.setDatabaseInfo(h2);
    			h2.put("result", result);
    			String json = h2.toString();
    			byte[] buf = json.getBytes("UTF-8");
    			response = new DatagramPacket(buf, 0, buf.length, addr0, port0 );
    			datagramSocket.send( response );
	            break;
			}
			//暂停所有的服务
			case Command.CONTROL_SUSPENDALL:
			{
	            Log.msg( "Receive control(suspend all modules) from "+ addr0 +"("+ port0 +")" );
	            Enumeration<?> enu = modules.elements();
	            while( enu.hasMoreElements() )
	            {
	                Module module = ( Module ) enu.nextElement();
    				Log.msg( module.toString()+" suspend from control" );
	                module.suspend();
	            }
				break;
	    	}
			//暂停
			case Command.CONTROL_SUSPEND:
			{
	            byte length = payload[1];
	            String id = new String( payload, 2, length);
//	            Log.msg( "Receive control(suspend:"+id+") from "+ addr0 +"("+ port0 +")" );
	            Module module = modules.get(id);
	            if( module != null )
	            {
	    			response = new DatagramPacket(payload, 0, 2 + length, addr0, port0 );
	    			datagramSocket.send( response );
	    			module.suspend();
	            }
	            else if( "COSControl".equals(id) || "EMAControl".equals(id) )
	            {
					Log.msg( "Abort main-control" );
		            Log.msg( "Receive control(suspend all modules) from "+ addr0 +"("+ port0 +")" );
		            Enumeration<?> enu = modules.elements();
		            while( enu.hasMoreElements() )
		            {
		                module = ( Module ) enu.nextElement();
	    				Log.msg( "Suspend "+module.getId()+" from control" );
		                module.suspend();
		            }
	            }
				break;
			}
			//删除日志
			case Command.CONTROL_CLEARLOGS:
			{
	            byte length = payload[1];
	            String id = new String( payload, 2, length);
//	            Log.msg( "Receive control(suspend:"+id+") from "+ addr0 +"("+ port0 +")" );
	            Module module = modules.get(id);
	            if( module != null )
	            {
	            	module.closeDebugPrinter();//清除之前先关闭debug的输出流
	    			response = new DatagramPacket(payload, 0, 2 + length, addr0, port0 );
	    			datagramSocket.send( response );
	    			clearLogFiles(id);
					Log.msg( "Delete log "+module.getLogdir()+" from control" );
	            }
	            else if( "COSControl".equals(id) )
	            {
	    			response = new DatagramPacket(payload, 0, 2 + length, addr0, port0 );
	    			datagramSocket.send( response );
	    	    	F dir = new F(ConfigUtil.getWorkPath(), "log/COSControl");
	    			F[] files = dir.listFiles();
	    	    	StringBuilder sb = new StringBuilder();
	    			for( F file : files )
	    	    	{
	    	    		if( file.getName().endsWith(".gc") ){
	    	    			continue;//PID文件和GC文件不删除
	    	    		}
	    	    		sb.append("\r\n\t["+file.getName()+"] "+file.delete());
	    	    	}
					Log.print( "Delete log COSControl from control%s", sb.toString());
	            }
	            else if( "Wrapper".equals(id) )
	            {
	    			response = new DatagramPacket(payload, 0, 2 + length, addr0, port0 );
	    			datagramSocket.send( response );
	    			IOHelper.deleteFile(new F("../log/wrapper.log"));
					Log.msg( "Delete wrappler.log from control" );
	            }
				break;
			}
			case Command.CONTROL_GC:
			{
	            byte length = payload[1];
	            String id = new String( payload, 2, length);

	        	ServerSocket ss = new ControlSocket();
	            Log.msg( "Receive control(fetch the history of gc("+id+") from "+ addr0 +"("+ port0 +")" );
				Tools.intToBytes(ss.getLocalPort(), payload, 0, 4);
		        response = new DatagramPacket( payload, 0, 4, addr0, port0);
		        FetchGCHisotry thread = new FetchGCHisotry(ss, id);
            	thread.start();
            	datagramSocket.send(response);
				break;
			}
			//返回文件数据
			case Command.CONTROL_GETFILE:
			{
				int offset = 1;
				boolean zip = payload[offset++]==1;
	            int length = Tools.bytesToInt(payload, offset, 2);
	            offset += 2;
	            String filepath = new String( payload, offset, length, "UTF-8");
//	            String charset = "ISO-8859-1";
//	            if( !filepath.equals(new String(filepath.getBytes(charset), charset)) )
//	            {
//	            	charset = "UTF-8";
//            		filepath = new String(payload, offset, length, charset);
//	            	if( !filepath.equals(new String(filepath.getBytes(charset), charset)))
//		            {
//	            		charset = "GBK";
//	            		filepath = new String(payload, offset, length, charset);
//		            }
//	            }
	            offset += length;
	            int mode = payload[offset++];//-1分页,0普通,1指定目录文件夹压缩
	        	ServerSocket ss = new ControlSocket();
//	            Log.msg( "Receive control(fetch the file:"+filepath+", mode="+mode+", local_port="+ss.getLocalPort()+") from "+ addr0 +"("+ port0 +")" );
	            if( mode == -1 )
	            {//分页模式
	            	int page = Tools.bytesToInt(payload, offset, 4);
	            	offset += 4;
	            	long pageSize = Tools.bytesToLong(payload, offset, 8);
		        	
					Tools.intToBytes(ss.getLocalPort(), payload, 0, 4);
			        response = new DatagramPacket( payload, 0, 4, addr0, port0);
			        FetchFile thread = new FetchFile(ss, filepath, page, pageSize);
	            	thread.start();
	            	datagramSocket.send(response);
	            }
	            else
	            {
	            	ArrayList<String> filesnames = new ArrayList<String>();
	            	if( mode == 1 )
	            	{
		            	length = Tools.bytesToInt(payload, offset, 2);
		            	offset += 2;
		            	for( int i = 0; i < length; i++ )
		            	{
		            		int len = payload[offset++];
		    	            String filename = new String( payload, offset, len, "UTF-8");
		    	            offset += len;
		    	            if( filename.isEmpty() ) continue;
		    	            filesnames.add(filename);
		            	}
		            	if( filesnames.size() == 1 )
		            	{
		            		F file = new F(filepath, filesnames.get(0));
		            		filepath = file.getPath();
		            		filesnames.clear();
		            	}
	            	}
					Tools.intToBytes(ss.getLocalPort(), payload, 0, 4);
			        response = new DatagramPacket( payload, 0, 4, addr0, port0);
			        FetchFile thread = new FetchFile(ss, filepath, zip, filesnames);
	            	thread.start();
	            	datagramSocket.send(response);
	            }
				break;
			}
			//返回文件列表
			case Command.CONTROL_GETFILELIST:
			{
	            byte length = payload[1];
	            String path = new String( payload, 2, length);
	            String charset = "ISO-8859-1";
	            if( !path.equals(new String(path.getBytes(charset), charset)) )
	            {
	            	charset = "UTF-8";
	            	path = new String(payload, 2, length, charset);
	            	if( !path.equals(new String(path.getBytes(charset), charset)))
		            {
	            		charset = "GBK";
	            		path = new String(payload, 2, length, charset);
		            }
	            }
	            Log.msg( "Receive control(ll:"+path+") from "+ addr0 +"("+ port0 +")" );
				responseFiles(datagramSocket, addr0, port0, path, payload);
				break;
			}
			//创建文件
			case Command.CONTROL_MAKEFILE:
			{
	            byte length = payload[1];
	            String filemake = new String(payload, 2, length, "UTF-8");
	            Log.msg( "Receive control(make file:"+filemake+") from "+ addr0 +"("+ port0 +")" );
	            responseMakeFile(datagramSocket, addr0, port0, filemake, payload);
				break;
			}
			//创建目录
			case Command.CONTROL_MAKEDIR:
			{
	            byte length = payload[1];
	            String dirmake = new String(payload, 2, length, "UTF-8");
	            Log.msg( "Receive control(make dir:"+dirmake+") from "+ addr0 +"("+ port0 +")" );
	            responseMakeDir(datagramSocket, addr0, port0, dirmake, payload);
				break;
			}
			//删除指定路径
			case Command.CONTROL_DELETEFILE:
			{
	            byte length = payload[1];
	            String pathdelte = new String( payload, 2, length);
	            String charset = "ISO-8859-1";
	            if( !pathdelte.equals(new String(pathdelte.getBytes(charset), charset)) )
	            {
	            	charset = "UTF-8";
	            	pathdelte = new String(payload, 2, length, charset);
	            	if( !pathdelte.equals(new String(pathdelte.getBytes(charset), charset)))
		            {
	            		charset = "GBK";
	            		pathdelte = new String(payload, 2, length, charset);
		            }
	            }
	            Log.msg( "Receive control(delete path:"+pathdelte+") from "+ addr0 +"("+ port0 +")" );
				responseDeletePath(datagramSocket, addr0, port0, pathdelte, payload);
				break;
			}
			//预览日志目录文件列表
			case Command.CONTROL_LOGFILELIST:
			{
	            byte length = payload[1];
	            String id = new String( payload, 2, length);
				responseLogList(datagramSocket, addr0, port0, id, payload);
				break;
			}
			//预览配置文件
			case Command.CONTROL_CONTROLXMLPREVIEW:
			{
//	            Log.msg( "Receive control(view control.xml) from "+ addr0 +"("+ port0 +")" );
	        	ServerSocket ss = new ControlSocket();
	        	
				Tools.intToBytes(ss.getLocalPort(), payload, 0, 4);
		        response = new DatagramPacket( payload, 0, 4, addr0, port0);
		    	PreviewControlXml thread = new PreviewControlXml(ss);
		    	thread.start();
				datagramSocket.send(response);
				break;
	    	}
			//执行JMAP
			case Command.CONTROL_JMAP:
			{
	            byte length = payload[1];
	            String id = new String( payload, 2, length);
	            Log.msg( "Receive control(jmap, "+id+") from "+ addr0 +"("+ port0 +")" );
	        	ServerSocket ss = new ControlSocket();
		        Module module = modules.get(id);
	            if( module != null )
	            {
	            	if( module.java ){
	            		JMAP thread = new JMAP(ss, module);
	            		thread.start();
	            		Log.msg( "JMAP(port="+ss.getLocalPort()+") from control" );
	    				Tools.intToBytes(ss.getLocalPort(), payload, 0, 4);
	    		        response = new DatagramPacket( payload, 0, 4, addr0, port0);
	            	}
	            	else {
	    				Tools.intToBytes(0, payload, 0, 4);
	    				String tips = "该程序不是JAVA程序";
	    				byte[] buf = tips.getBytes("UTF-8");
	    				System.arraycopy(buf, 0, payload, 4, buf.length);
	    		        response = new DatagramPacket( payload, 0, buf.length+4, addr0, port0);
	            	}
	            }
	            else{
    				Tools.intToBytes(0, payload, 0, 4);
    				String tips = "该程序模块配置不存在";
    				byte[] buf = tips.getBytes("UTF-8");
    				System.arraycopy(buf, 0, payload, 4, buf.length);
    		        response = new DatagramPacket( payload, 0, buf.length+4, addr0, port0);
	            }
				datagramSocket.send(response);
				break;
	    	}
			//执行JSTACK
			case Command.CONTROL_JSTACK:
			{
	            byte length = payload[1];
	            String id = new String( payload, 2, length);
	            Log.msg( "Receive control(jstack, "+id+") from "+ addr0 +"("+ port0 +")" );
	        	ServerSocket ss = new ControlSocket();
		        Module module = modules.get(id);
	            if( module != null )
	            {
	            	if( module.java ){
	            		JStack thread = new JStack(ss, module);
	            		thread.start();
	            		Log.msg( "Jstack(port="+ss.getLocalPort()+") from control" );
	    				Tools.intToBytes(ss.getLocalPort(), payload, 0, 4);
	    		        response = new DatagramPacket( payload, 0, 4, addr0, port0);
	            	}
	            	else {
	    				Tools.intToBytes(0, payload, 0, 4);
	    				String tips = "该程序不是JAVA程序";
	    				byte[] buf = tips.getBytes("UTF-8");
	    				System.arraycopy(buf, 0, payload, 4, buf.length);
	    		        response = new DatagramPacket( payload, 0, buf.length+4, addr0, port0);
	            	}
	            }
	            else{
    				Tools.intToBytes(0, payload, 0, 4);
    				String tips = "该程序模块配置不存在";
    				byte[] buf = tips.getBytes("UTF-8");
    				System.arraycopy(buf, 0, payload, 4, buf.length);
    		        response = new DatagramPacket( payload, 0, buf.length+4, addr0, port0);
	            }
				datagramSocket.send(response);
				break;
	    	}
			//配置主控文件
			case Command.CONTROL_CONTROLXMLCONFIG:
			{
//	            Log.msg( "Receive control(set control.xml) from "+ addr0 +"("+ port0 +")" );
	        	ServerSocket ss = new ControlSocket();
	        	
				Tools.intToBytes(ss.getLocalPort(), payload, 0, 4);
		        response = new DatagramPacket( payload, 0, 4, addr0, port0);
	        	ConfigControlXml thread = new ConfigControlXml(ss);
	        	thread.start();
	            datagramSocket.send(response);
				break;
	    	}
			//覆盖文件
			case Command.CONTROL_COPYFILE:
			{
				int off = 1;
	            byte length = payload[off++];
	            String filepath = new String( payload, off, length);
	            String charset = "ISO-8859-1";
	            if( !filepath.equals(new String(filepath.getBytes(charset), charset)) )
	            {
	            	charset = "UTF-8";
            		filepath = new String(payload, off, length, charset);
	            	if( !filepath.equals(new String(filepath.getBytes(charset), charset)))
		            {
	            		charset = "GBK";
	            		filepath = new String(payload, off, length, charset);
		            }
	            }
	            off += length;
	            int decompress = payload[off++];
	            Log.msg( "Receive control(copy file:"+filepath+", decompress:"+decompress+") from "+ addr0 +"("+ port0 +")" );
	        	ServerSocket ss = new ControlSocket();
	        	
				Tools.intToBytes(ss.getLocalPort(), payload, 0, 4);
		        response = new DatagramPacket( payload, 0, 4, addr0, port0);
		        CopyFile copyFile = new CopyFile(ss, filepath, decompress, addr0, port0);
		        copyFile.start();
	            datagramSocket.send(response);
				break;
	    	}
			case Command.CONTROL_DECOMPRESS:
			{
	            Syslog syslog = new Syslog();
	    		syslog.setCategory("COSControl");
	    		syslog.setAccount(identity);
	    		syslog.setLogtype(LogType.运行日志.getValue());
				int off = 1;
	            byte length = payload[off++];
	            String filepath = new String( payload, off, length, "UTF-8");
	            off += length;
	            length = payload[off++];
	            String destpath = "";
	            if( length > 0 ) destpath = new String( payload, off, length, "UTF-8");
	            DecompressFile handler = new DecompressFile(filepath, destpath, 7, addr0, port0);
				handler.start();
				break;
			}
			case Command.CONTROL_GETMONITOR:
			{
				StringBuffer logtxt = new StringBuffer(String.format("Receive control(monitor) from %s(%s) at %s, current have %s syncher.",
					addr0, port0, Tools.getFormatTime("HH:mm:ss"), systemMonitorSynchers.size()));
	        	for( SystemMonitorSyncher syncher : systemMonitorSynchers )
	    		{
	        		logtxt.append("\r\n\t"+syncher.toString());
	        		if( systemMonitorMap.containsKey(syncher.getId()) )
	        		{
		        		logtxt.append("\t"+syncher.getId());
	        		}
	    		}
	        	int len = payload[1];
	        	String id = "";
	        	if( len > 0 && len < 32 )
	        	{
	        		id = new String(payload, 2, len);
	        	}
	        	ServerSocket ss = new ControlSocket();
				Tools.intToBytes(ss.getLocalPort(), payload, 0, 4);
        		logtxt.append("\r\n\t...wait the new monitor(port: "+ss.getLocalPort()+").");
		        response = new DatagramPacket( payload, 0, 4, addr0, port0);
				SystemMonitorSyncher syncher = new SystemMonitorSyncher(ss, id, logtxt){
					@Override
					public void run() {
			    		long ts = System.currentTimeMillis();
			    		try
			    		{
			    			ss.setSoTimeout(15000);
			    			socket = ss.accept();
			    			logtxt.append("\r\n\tSucceed to setup the syncher("+socket.getInetAddress().getHostAddress()+":"+socket.getPort()+
			    					", "+this.getId()+") of monitor(total:"+systemMonitorSynchers.size()+") and beign to response system monitor.");
			    			out = socket.getOutputStream();
			    	    	SystemPerf systemPerf = getLocalSystemPerf();
			    			send(systemPerf, N_HU);//0表示SystemPerf
		    				logtxt.append("\r\n\t\tFound "+modules.size()+" modules of monitor will been report.");
			    			Iterator<String> itr = modules.keySet().iterator();
			    			while(itr.hasNext())
			    			{
			    				Module module = modules.get(itr.next());
//			    				logtxt.append("\r\n\t\t"+module.getId()+"\tstartup: "+module.isStartup());
			    				send(module.getModulePerf(), N_MU);//0表示SystemPerf
			    			}
			    			synchronized(systemMonitorSynchers)
							{
			    				systemMonitorSynchers.add(this);
			    				if( !this.getId().isEmpty() )
			    				{
			    					SystemMonitorSyncher old = systemMonitorMap.get(this.getId());
			    					if( old != null )
			    					{
			    						logtxt.append("\r\n\tFound the syncher("+old+") connected and break it.");
			    						old.close(logtxt);
			    						systemMonitorMap.remove(this.getId());
			    						logtxt.append("\r\n\t\tRemove("+systemMonitorSynchers.remove(old)+").");
			    					}
			    					systemMonitorMap.put(getId(), this);
			    				}
							}
			    		}
			    		catch( Exception e )
			    		{
			    			try {
								ss.close();
							} catch (IOException e1) {
							}
			    			ts = System.currentTimeMillis() - ts;
			    			logtxt.append("\r\n\tFailed to setup(duration:"+ts+"ms) the syncher of monitor with cos-web for exception: ");
							try
							{
								ByteArrayOutputStream out = new ByteArrayOutputStream();
								PrintStream ps = new PrintStream(out);
								e.printStackTrace(ps);
								logtxt.append(out.toString());
								ps.close();
							}
							catch(Exception e1)
							{
							}
			    		}
			    		finally
			    		{
			    			Log.msg(logtxt.toString());
			    		}
			    		//执行排队发送监控数据
			    		try{
			    			while(true)
			    			{
			    				if(!opened) break;
			    				if(closed) break;
			    				if(!socket.isConnected()) break;
			    				if(socket.isClosed()) break;
			    				if(socket.isOutputShutdown()) break;
			    				this.execute();//执行同步
			    			}
			    			if( !closed && socket.isConnected() ){
			    				this.disconnect();
			    			}
			    		}
			    		catch( Exception e )
			    		{
			    			Log.err("Failed to send the data of monitor for "+e);
			    			this.disconnect();
			    		}
		    			Log.print("Finish syncher of %s", this.toString());
					}

					@Override
					public void remove(SystemMonitorSyncher remover) {
				    	synchronized(systemMonitorSynchers)
				    	{
				    		Log.war(this.toString()+" remove.");
				    		systemMonitorSynchers.remove(remover);
    						systemMonitorMap.remove(this.getId());
				    	}
					}
				};
				if( opened ){
					Thread thread = new Thread(syncher);
					thread.start();
					datagramSocket.send(response);
				}
				break;
			}
			case Command.CONTROL_GETSERVERSMONITOR:
			{
	            Log.msg( "Receive control(serversmonitor) from "+ addr0 +"("+ port0 +")" );
				break;
			}
			case 10:
			{
//				org.tanukisoftware.wrapper.security.WrapperReport.getInstance();
				break;
			}
			case Command.CONTROL_SSH:
			{
	            Log.msg( "Receive control(ssh) from "+ addr0 +"("+ port0 +")" );
				SSH ssh = new SSH(addr0, port0);
				ssh.start();
				break;
			}
			case Command.CONTROL_DEBUG:
			{
	            byte length = payload[1];
	            String id = new String( payload, 2, length);
	            Log.msg( "Receive control(debug:"+id+") from "+ addr0 +"("+ port0 +")" );
	            Module module = modules.get(id);
	            if( module != null )
	            {
	            	module.openSocketDebug(addr0, port0);
	            }
				break;
			}
			default:
				Log.msg( "Receive heart-beat from "+ addr0 +"("+ port0 +")" );
				break;
	    	}
    	}
    	catch(Exception e)
    	{
    		Log.err("Failed to receive control command("+command+") from controlport "+this.controlPort, e);
    	}
    }
    //系统监控的同步列表
    private ArrayList<SystemMonitorSyncher> systemMonitorSynchers = new ArrayList<SystemMonitorSyncher>();
    private HashMap<String, SystemMonitorSyncher> systemMonitorMap = new HashMap<String, SystemMonitorSyncher>();
	static final int N_HU = 0;//主机信息更新
	static final int N_MU = 1;//软件模块更新
	static final int N_MQ = 2;//软件模块退出
	static final int N_TU = 255;//主机标题更新
	static final int N_HHU = 10;//历史主机更新
    /**
     * 加载扩展的运行器
     */
    private void loadExtendRunner()
    {
    	StringBuffer sb = new StringBuffer("Load extend programs below:\r\n\t...");
    	String cosportal = ConfigUtil.getString("runner.cosportal", "");
    	if( !cosportal.isEmpty() ){
    		if( !modules.containsKey("COSPortal") )
    		{
    			sb.append("\r\n\t"+addModule(new COSPortalRunner(this)));
    		}
    		F webpath = new F(cosportal);
			File fileServerxml = new File(webpath, "conf/server.xml");
			try
			{
				XMLParser xml = new XMLParser(fileServerxml);
				Element e = XMLParser.getElementByTag(xml.getRootNode(), "Connector");
	        	if( e != null )
	        	{
	        		this.portalPort = Integer.parseInt(XMLParser.getElementAttr(e, "port"));
	        	}
        	}
        	catch(Exception e)
        	{
        	}
	   	}
    	File bindir = new File("");
    	boolean noide = true;
		if( !bindir.getPath().startsWith("d:\\focusnt\\cos\\trunk\\ide") )
			noide = false;
    	String email = ConfigUtil.getString("runner.email", "false");
    	if( "true".equalsIgnoreCase(email) ){
    		if( !modules.containsKey("SystemEmail") )
    		{
    			Module module = this.addModule(new SystemEmailRunner(this).setDependence("Zookeeper"));
    			if( !cosportal.isEmpty() && noide ){
    				sb.append("\r\n\t"+module.setDependence("COSPortal"));
    			}
    			else{
    				sb.append("\r\n\t"+module);
    			}
    		}
    	}
    	String alarm = ConfigUtil.getString("runner.alarm", "false");
    	if( "true".equalsIgnoreCase(alarm) ){
    		if( !modules.containsKey("SystemAlarm") )
    		{
    			Module module = this.addModule(new SystemAlarmRunner(this).setDependence("Zookeeper"));
    			if( !cosportal.isEmpty() && noide ){
    				sb.append("\r\n\t"+module.setDependence("COSPortal"));
    			}
    			else{
    				sb.append("\r\n\t"+module);
    			}
    		}
    	}
    	String notify = ConfigUtil.getString("runner.notify", "false");
    	if( "true".equalsIgnoreCase(notify)){
    		if( !modules.containsKey("SystemNotify") )
    		{
    			Module module = this.addModule(new SystemNotifyRunner(this).setDependence("Zookeeper"));
    			if( !cosportal.isEmpty() && noide ){
    				sb.append("\r\n\t"+module.setDependence("COSPortal"));
    			}
    			else{
    				sb.append("\r\n\t"+module);
    			}
    		}
	   	}
    	Log.msg(sb.toString());
    }

	private String status;
	private long status_timestamp;
    /**
     * 得到管理器当前状态信息
     * @return
     */
    public String status(){
    	return status;
    }
    public void status(String info, Object... args){
    	status_timestamp = System.currentTimeMillis();
    	status = String.format(info, args);
	}
    // 主控启动时间
	private long startTimestamp = System.currentTimeMillis();
	/**
	 * 主引擎，7秒检查一下各个模块尝试重启
	 */
	private DirStateQuery dirStateQuery;
	public void run()
    {
        Log.msg( "Start module control(opened="+opened+")." );
        try
        {
        	dirStateQuery = new DirStateQuery() {
				@Override
				public void notify(DirState state) {
					Log.print("[DirStateQuery] %s", state.toString());
				}
			};
			dirStateQuery.createScan(ConfigUtil.getWorkPath());
        	myjdk = new MyJDK(new F(ConfigUtil.getWorkPath()), ConfigUtil.getString("service.name"), ConfigUtil.getString("service.desc")) {
    			@Override
    			public void notifyDownloadResult(boolean succeed, String cosId, String logcontext) {
    				if( succeed ){
    					Log.msg(String.format("Succeed to download%s", logcontext));
    				}
    				else{
    					Log.err(String.format("Failed download%s", logcontext));
    				}
    			}
				@Override
				public void parse(String[] args) {
		            final String Tag = "-D"+ConfigUtil.getString("service.name")+".subprocess.id=";
                	JSONObject p = new JSONObject();
                	p.put("pid", args[0]);
                	p.put("class", args[1]);
            		JSONArray array = new JSONArray();
                	p.put("properties", array);
                	p.put("timestamp", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss"));
                	String id = null;
            		for(int i = 2; i < args.length; i++){
            			array.put(args[i]);
            			int j = args[i].indexOf(Tag);
            			if( j != -1 ){
                    		j += Tag.length();
                    		id = args[i].substring(j);
            			}
            		}
            		if( id != null ){
            			Module module = modules.get(id);
                		if( module != null ){
//                			System.out.println(String.format("\t$%s yes.", id));
                        	p.put("startup_command", new JSONArray(module.getStartupCommands()));
                        	p.put("shutdown_command", new JSONArray(module.getShutdownCommands()));
                        	module.setProcessInfo(p);
//                			System.out.println(String.format("\t@%s set.", id));
                		}
                		else{
                			Log.war(String.format("Found the program(%s) of outcontrol see below: %s", id, p.toString(4)));
                			Subprocessor.forcekill(args[0]);//强行杀死这个进程
                			System.out.println();
                			System.out.println(String.format("@COS$ Found program(%s) of outcontrol see below: %s", id, p.toString(4)));
                        	SysalarmClient.autoconfirm("Sys", id + "_Zombie", 
                        			String.format("程序[%s]的僵尸进程已经全部关闭: %s", id, p.toString(4)));
                		}
            		}
				}
    		};
    		F file = new F(ConfigUtil.getWorkPath(), "data/identity");
        	System.setProperty("cos.identity", file.getPath());
        	System.setProperty("cos.id", this.identity);
        	loadKernelRunner();//加载内核程序
    		loadExtendRunner();
    		startControlListener();//启动控制监听
    		if(myjdk.uninstall())
    		{
    			System.out.println("@COS$ Found jdk need to install from "+myjdk.getWorkdir());
    			Log.msg("Found jdk need to install from "+myjdk.getWorkdir());
    			myjdk.install();
    		}
    		else{
            	myjdk.jps();//执行JPS检查
            	//如果发现有模块进程需要强行清理
    		}
    		
    		int count = 0;
    		long maxtime_startup = 0, maxtime_check = 0;
            HashMap<String, ModulePerf> mapPid2Module = new HashMap<String, ModulePerf>();
    		while( opened )
            {
    			long time_startup = 0, time_check = 0;
                long now = System.currentTimeMillis();// 当前时间
                status = "Wait for execute startup for all modules";
                mapPid2Module.clear();
                synchronized (this)
                {
                	if( !opened ) break;
                    status = "Begin to execute startup for all modules";
                    Enumeration<?> enu = modules.elements();
                    int c = 0;
                    mapPid2Module.put(WrapperShell.Pid, mainGC.modulePerf);
                    while( enu.hasMoreElements() )
                    {
                        Module module = ( Module ) enu.nextElement();
                        JSONObject pids = module.getPids();
                        if( module.getState() == Module.STATE_STARTUP ){
                        	if( pids.length() > 0 ){
                        		Iterator<?> allpids = pids.keys();
                				while(allpids.hasNext()){
                					String pid = allpids.next().toString();
                					mapPid2Module.put(pid, module.getModulePerf());
                				}
                        	}
                        }
                        else {
                        	if( pids.length() > 0 ){
                        		//如果程序非启动状态发现有进程id还存在
                        		if(!module.isExecuting() && System.currentTimeMillis()-module.getFinishTimestamp()>7000){
                        			Log.war(String.format("Found %s pids at module(%s), %s\r\n%s",
                        				pids.length(), module.toString(), pids.toString(4), module.getLastQuiteLogs()));
                    				module.forcekill();
                        		}
                        	}
                        }
                        status = "Handle the execute of "+(c++)+"th module "+module.getId()+" at "+Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis());
                        if( "COSPortal".equals(module.getId()) )
                        {
                        	module.setEnabled("false".equals(ConfigUtil.getString("runner.cosportal.suspend", "false")));
                        	module.setSuspend("true".equals(ConfigUtil.getString("runner.cosportal.suspend", "false")));
                        }
                        Module moduleDependence = ( Module ) modules.get( module.getDependence() );
                        if( moduleDependence != null )
                        {
                        	if( moduleDependence.getState() == Module.STATE_STARTUP )
                        	{//只有依赖模块启动才能够启动自己
            	            	if( now - moduleDependence.getStartupTimestamp()>= module.getDelayedStartInterval() &&
            	            		now - startTimestamp >= module.getDelayedStartInterval() )
            	                {
            	                	if( !opened ) break;
            	            		module.setState(module.getState());
            	                    module.startup();
            	                }
                        	}
                        	else
                        	{//模块依赖
                        		module.setState(module.getState(), "依赖 "+moduleDependence.getId());
                        	}
                        }
                        else if(now - startTimestamp >= module.getDelayedStartInterval())//无依赖模块，主控启动一段时间后才允许启动
                        {
                        	if( !opened ) break;
                            module.startup();
                        }
                    }
                    time_startup = System.currentTimeMillis() - now;
                    status = "Finish execute startup for all modules time "+time_startup;
                	if( !opened ) break;
            		if(!myjdk.uninstall() )
            		{
                        status = "Begin to execute the check of jps.";
                        status_timestamp = System.currentTimeMillis();
                    	myjdk.jps();//执行JPS检查
                        status = "Finish execute the check of jps.";
                        status_timestamp = System.currentTimeMillis();
                    	StringBuilder sb = new StringBuilder();
                    	enu = modules.elements();
                        status = "Begin to check zombie for all modules.";
                        while( enu.hasMoreElements() )
                        {
                            Module module = ( Module ) enu.nextElement();
                            status = "Check the zombie of module\r\n"+module.toString();
                            if (module.hasZombie())
                            {
                                status = "Todo send the alarm of "+module.getId();
                            	sb.append(String.format("\r\n\t[%s] %s", module.getId(), module.getPids().toString(4)));
                            	Sysalarm alarm = new Sysalarm();
                            	alarm.setSysid("Sys");
                            	alarm.setResponser(module.getModulePerf().getProgrammer());
                            	alarm.setContact(module.getModulePerf().getProgrammerContact());
                            	alarm.setSeverity(AlarmSeverity.BLACK.getValue());
                            	alarm.setType(AlarmType.S.getValue());
                            	alarm.setId(module.getId() + "_Zombie");
                            	alarm.setCause("程序没有关闭释放资源机制，导致前次程序关闭退出不成功");
                            	alarm.setTitle("主控启动程序[" + module.getId() + " " + module.getName() + "]发现僵尸进程");
                            	alarm.setText("系统发现同一个程序服务有多个运行实例" + module.getPids().toString(4) + "，请检查");
                            	SysalarmClient.send(alarm);
                            }
                        }
                        status = "Finish to check zombie for all modules.";
                        if( sb.length() > 0 ){
                            sb.insert(0, "Found zomibes from below program of java:");
                            System.out.println(sb.toString());
                            Log.err(sb.toString());
                        }
                        time_check = System.currentTimeMillis() - now - maxtime_startup;
            		}
        		}

            	if( !opened ) break;
                status = "Begin to send the beat of heart.";
                //检查java程序的pid
                //向主界面框架发送心跳
                sendHeartbeat();
                status = "Finish send the beat of heart.";
            	if( !opened ) break;
                //监控本程序的内存使用情况
            	monitorCOSControl(mapPid2Module);
                status = "Finish to monitor memory of mine.";
//                System.err.println(String.format("\r\n[%s] %s", Tools.getFormatTime("HH:mm:ss"),status));
                status = "Wait to sleep.";
                count += 1;
            	synchronized(this)
            	{
            		if( time_startup > maxtime_startup || time_check > maxtime_check ){
            			maxtime_startup = maxtime_startup<time_startup?time_startup:maxtime_startup;
            			maxtime_check = maxtime_check<time_check?time_check:maxtime_check;
            			Log.msg(String.format("Finish %s wrappers, the maximum time of startup is %sms/%sms, the maxium time of check is %sms/%sms",
            				count, maxtime_startup, time_startup, maxtime_check, time_check));
            		}
            		long ts = System.currentTimeMillis() - now;
                    if( !opened ) Log.msg("Finish runprocess("+ts+"ms).");
                	if( !opened ) break;
                    status = "Begin to sleep.";
            		this.wait(7000);
                    status = "Finish sleep.";
            	}
            }
            status = "Finish run.";
        }
        catch( Exception e )
        {
            status = "Meeting exception.";
            Log.err( e );
            Sysalarm alarm = new Sysalarm();
            alarm.setSysid("Sys");
            alarm.setSeverity(AlarmSeverity.RED.getValue());
            alarm.setType(AlarmType.S.getValue());
            alarm.setId(WrapperShell.ModuleID+"_ha");
            alarm.setTitle("读取主控配置错误");
            alarm.setException("读取主控配置错误:", e);
            SysalarmClient.send(alarm);//向系统管理员发送告警
        }
        status = "Begin to close all modules.";
        closeAllModules();
        status = "Begin to close dir-query.";
        dirStateQuery.close();
        Log.msg( "Goodby baby, i love you niubao and yangbao, my son." );
    }

    public MyJDK getMyjdk() {
		return myjdk;
	}

	/**
     *  设置模块配置
     * @throws Exception
     */
	private String subject;//伺服器主控主要功能描述标题
    public void setSubject(String subject) 
    {
    	if( this.subject == null )
    	{
    		Log.msg("Load the subject of your service '"+subject+"'.");
    	}
    	else
    	{
    		Log.msg("Found the change of your service subject from '"+this.subject+"' to '"+subject+"'.");
    	}
		this.subject = subject;
	}
    
    /**
     * 返回主控引擎系统启动服务的描述
     * @return
     */
    public String getYourServiceDesc() {
    	
		return ConfigUtil.getString("service.desc")+"("+ConfigUtil.getString("service.name")+") "+(subject!=null?subject:"");
	}
    
    /**
     * 设置模块开关
     * @param id
     * @param enabled
     */
    protected synchronized void setModuleSwitch(String id, boolean enabled)
    {
    	Module module = this.getModule( id );
        if( module == null )
        {
        	Log.war("Not found module "+id+" to set switch "+enabled);
        	return;
        }
        module.setEnabled(enabled);
    }

    /**
     * 设置模块引擎的调试日志输出
     * @param id
     * @param debug
     */
    protected synchronized void setModuleDebug(String id, boolean debug)
    {
    	Module module = this.getModule( id );
        if( module == null )
        {
        	Log.war("Not found module "+id+" to set debug "+debug);
        	return;
        }
        module.setDebug(debug);
    }
    /**
     * 设置模块程序的配置
     * @param config
     */
	protected synchronized void setModuleConfig(byte[] payload, int length)
    {
		JSONObject config = null;
		String json = null;
		try
		{
			json = new String(payload, 0, length, Charset.forName("UTF-8"));
			config = new JSONObject(json);
		}
		catch(Exception e)
		{
        	Log.war("Failed to set config of program for error: "+json);
			return;
		}
        String id = config.getString("id");//XMLParser.getElementAttr( moduleNode, "id" );
        String name = config.getString("name");//XMLParser.getElementAttr( moduleNode, "name" );
        JSONObject control = config.has("control")?config.getJSONObject("control"):null;
        boolean enable = config.has("switch")?config.getBoolean("switch"):false;//XMLParser.getElementAttr( moduleNode, "enable" );
        boolean debug = config.has("debug")?config.getBoolean("debug"):false;
        if( control == null )
        {
        	Log.war("Failed to set config of program for not found control from config: \r\n"+config.toString(4));
        	return;
        }
        int restartup = control.has("restartup")?control.getInt("restartup"):0;//XMLParser.getElementAttr( moduleNode, "restartup" );
        String tmp = "";
        if( control.has("forcereboot") )
        {
        	JSONObject forcereboot = control.getJSONObject("forcereboot");
        	String frmode = forcereboot.has("mode")?forcereboot.getString("mode"):"";
        	String frval = forcereboot.has("val")?forcereboot.getString("val"):"";
        	String frtime = forcereboot.has("time")?forcereboot.getString("time"):"";
        	tmp = frmode + frval + frtime;
        }
        String forcereboot = tmp;//XMLParser.getElementAttr( moduleNode, "forcereboot" );
        int mode = control.has("mode")?control.getInt("mode"):Module.MODE_NORMAL;
        String logfile = control.has("logfile")?control.getString("logfile"):"";//XMLParser.getElementAttr( moduleNode, "logfile" );
        String pidfile = control.has("pidfile")?control.getString("pidfile"):"";//XMLParser.getElementAttr( moduleNode, "pidfile" );
        String cfgfile = control.has("cfgfile")?control.getString("cfgfile"):"";//XMLParser.getElementAttr( moduleNode, "cfgfile" );
        String dependence = control.has("dependence")?control.getString("dependence"):"";//XMLParser.getElementAttr( moduleNode, "dependence" );
        int delayed = control.has("delayed")?control.getInt("delayed"):0;//XMLParser.getElementAttr( moduleNode, "delayed" );//延迟启动
        Module module = this.getModule( id );
        if( module == null )
        {
            module = new Module(this){
				//状态发生改变
				public void onChangeStatus(Module module)
				{
					sendModuleMonitorData(module, N_MU);
				}

				@Override
				protected void handleProcessOutput(String line) {
				}

				@Override
				public void onFinish() {
					// TODO Auto-generated method stub
					
				}
            };
            module.setId( id );
            this.addModule( module );
        }
        else
        {
            module.clear();
        }
        module.setCfgfile(cfgfile);
        if( !pidfile.isEmpty() )
        {
	        if(pidfile.startsWith("..")){}
	        else if(pidfile.startsWith("/")){}
	        else if(pidfile.indexOf(":\\") == 1 ){}
	        else
	        {
	        	pidfile = "../"+pidfile;
	        }
        }
        module.setPidfile(pidfile);
        if( !logfile.isEmpty() )
        {
	        if(logfile.startsWith("..")){}
	        else if(logfile.startsWith("/")){}
	        else if(logfile.indexOf(":\\") == 1 ){}
	        else
	        {
	        	logfile = "../"+logfile;
	        }
        }
        module.setLogfile(logfile);
        module.setEnabled(enable);
        module.setMode(mode);
        module.setName( name );
        module.setInterruptTime( restartup );
        module.setDependence( dependence );
        module.setDelayedStartInterval(delayed);
        module.setForcereboot(forcereboot);
        String startTime = control.has("starttime")?control.getString("starttime"):"";
        String endTime = control.has("endtime")?control.getString("endtime"):"";
        module.setStartTime(startTime);
        module.setEndTime(endTime);
        module.setDebug( control.has("debug")?control.getBoolean("debug"):false );
        module.setShutdownRunning( true );
        module.setDebug(debug);

		if( config.has( "startup") )
		{
			JSONObject startup = config.getJSONObject("startup");
			if( startup.has("command") )
			{
	            JSONArray commands = startup.getJSONArray("command");
	            for( int i = 0; i < commands.length(); i++)
	            {
	                String value = commands.getString(i);
	            	if( i == 0 && value.indexOf("java") != -1 )
	            	{
	            		module.getModulePerf().setType(1);
	            	}
	                module.getStartupCommands().add( Module.setCommand( value ) );
	            }
			}
		}

		if( config.has("shutdown") )
		{
			JSONObject shutdown = config.getJSONObject("shutdown");
			if( shutdown.has("command") )
			{
	            JSONArray commands = shutdown.getJSONArray("command");
	            if( commands.length() > 0 )
	            {
		            module.setShutdownType( Module.Shutdown_Type_Command );
		            for( int i = 0; i < commands.length(); i++)
		            {
		                String value = commands.getString(i);
		                module.getShutdownCommands().add( Module.setCommand( value ) );
		            }
	            }
			}
		}

		if( !pidfile.isEmpty() && Module.Shutdown_Type_Command != module.getShutdownType() )
        {
            module.setShutdownType( Module.Shutdown_Type_Pid );
        }

		JSONObject maintenance = config.has("maintenance")?config.getJSONObject("maintenance"):null;
		if( maintenance != null )
    	{
			module.setProgramMaintenace(
					maintenance.has("programmer")?maintenance.getString("programmer"):"", 
					maintenance.has("email")?maintenance.getString("email"):"", 
					config.has("sysid")?config.getString("sysid"):"");
    	}
		String version = config.has("version")?config.getString("version"):"";
		String description = config.has("description")?config.getString("description"):"";
		module.setRemark(description);
        
        if( module.getVersion() == null )
        {
            module.setVersion( version );
        	module.setState(Module.STATE_INIT);
            module.shutdown();
            Log.msg(String.format("[%s][%s] Initialize the program of modules version %s %s",
            	module.getId(), module.getName(), version, config.has("control")?config.getJSONObject("control").toString(4):""));
        }
        else if( module.getVersion() != null && !module.getVersion().equals(version) &&
        		!"unknonwn".equalsIgnoreCase(version) && !"".equalsIgnoreCase(version) )
        {
        	//发现前后版本不一致，重启模块，首先模块需要避免版本不一致
        	Log.msg(String.format("[%s][%s] Restart the program of module for version change(%s -> %s ) %s",
        		module.getId(), module.getName(), module.getVersion(), version, config.has("control")?config.getJSONObject("control").toString(4):""));
        		module.setVersion( version );
        	module.shutdown();
        }
        else{
        	if( !"unknonwn".equalsIgnoreCase(version) && !"".equalsIgnoreCase(version) ){
        		module.setVersion( version );
        	}
    		Log.msg(String.format("[%s][%s] Reset the config of program version not change %s",
    			module.getId(), module.getName(), version, config.has("control")?config.getJSONObject("control").toString(4):""));
        }
        IOHelper.writeFile(new F(ConfigUtil.getWorkPath(), "log/"+module.getId()+"/program.json"), payload, 0, length);
//        if( module.isEnabled() )
//        {
//        }
//        Log.msg("Succeed to set the config of module "+config.toString(4));//(mode="+mode+",enable="+enable+",debug="+debug+") of module("+id+", "+version+", "+name+")."); 
    }
	/**
	 * 监控自己的内存使用情况
	 */
	private long lastCOSControlMonitorSend = 0;
	private void monitorCOSControl(HashMap<String, ModulePerf> mapPid2Module)
	{
        if( !this.mainGC.execute() && System.currentTimeMillis() - lastCOSControlMonitorSend < Tools.MILLI_OF_MINUTE )
        {//尝试进行GC统计，然后发送统计结果给主界面模块
        	return;
        }
        if( !mapPid2Module.isEmpty() ){
        	status = "Begin to execute netstat from "+mapPid2Module.size()+" process.";
//            System.err.println(String.format("\r\n[%s] %s", Tools.getFormatTime("HH:mm:ss"),status));
        	netstat(mapPid2Module);
        	status = "Finish execute netstat.";
        }
    	synchronized(systemMonitorSynchers)
    	{
        	for( SystemMonitorSyncher syncher : systemMonitorSynchers )
    		{
    			try
    			{
    				lastCOSControlMonitorSend = System.currentTimeMillis();
                    status = "Begin to send the monitor of COSControl.";
//                  System.err.println(String.format("\r\n[%s] %s", Tools.getFormatTime("HH:mm:ss"),status));
                    if( mainGC.getModulePerf().getPid() == null || mainGC.getModulePerf().getPid().isEmpty() ){
                    	JSONObject pid = new JSONObject();
                    	JSONObject info = new JSONObject();
                    	pid.put(WrapperShell.Pid, info);
                    	info.put("pid", WrapperShell.Pid);
                    	info.put("class", WrapperShell.class.getName());
                    	info.put("timestamp", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss"));
                    	mainGC.getModulePerf().setProcessInfo(pid.toString(), 1);
                    }
    				syncher.send(mainGC.getModulePerf(), N_MU);
    			}
    			catch(Exception e)
    			{
    				syncher.disconnect();
    				break;
    			}
    		}
    	}
	}
	/**
	 * 关闭所有的模块子系统
	 */
	private void closeAllModules()
	{
        Log.msg( "Close Module control." );        
        Enumeration<?> enu = modules.elements();
        ArrayList<Module> list = new ArrayList<Module>();
        while( enu.hasMoreElements() )
        {
            Module module = ( Module ) enu.nextElement();
            module.close();
	        try{
				Thread.sleep(module.getShutdownType()==Module.Shutdown_Type_Command?1000:100);
			}catch (InterruptedException e){}
            list.add(module);
        }
        Log.war( "Total "+list.size()+" modules need to shutdown..." );
		int tryCount = 0;
		while( !list.isEmpty() )
		{
			if( tryCount >= 16 )
			{
				Log.war("Not to do anything more than three times, last "+list.size()+" modules not shutdown.");
				break;
			}
			tryCount += 1;
	        try{
				Thread.sleep(1000);
			}catch (InterruptedException e){}
			
			StringBuffer sb = new StringBuffer( "The below modules had not been shutdown... " );
			for(int i = 0; i < list.size(); i++ )
	        {
	            Module module = ( Module ) list.get(i);
	            if( module.isExecuting() )
	            {
		    		sb.append("\r\n\t");
		            sb.append(module.toString());
	            }
	            else
	            {
	            	list.remove(i--);
	            }
	        }
	        sb.append("\r\n\t... ... "+list.size()+" modules not shutdown.");
	        Log.war(sb.toString());
		}
		
        try
        {
	        StringBuffer sb = new StringBuffer("Close all "+systemMonitorSynchers.size()+" system monitor syncher.");
	        synchronized (systemMonitorSynchers)
			{
	        	for( SystemMonitorSyncher syncher : systemMonitorSynchers )
	        	{
	        		syncher.close(sb);
	        	}
			}
	        Log.msg(sb.toString());
	        systemMonitorSynchers.clear();
	        systemMonitorMap.clear();
        }
        catch(Exception e)
        {
        	Log.err(e);
        }
        synchronized( this )
        {
            this.closed = true;
            this.notifyAll();
        }
	}
	/**
	 * 将模块子系统守护对象添加到监控列表中
	 * @param module
	 */
    public Module addModule( Module module )
    {
    	if( module != null )
    	{
//    		Log.msg("Load the module of "+module.getId());
    		this.modules.put( module.getId(), module );
    	}
    	return module;
    }

    public void delModule( Module module )
    {
    	if( module != null )
    	{
    		this.modules.remove( module.getId() );
    	}
    }

    public Module getModule( String id )
    {
        return( Module )this.modules.get( id );
    }
    
    /**
     * 正在关闭
     * @return
     */
    public boolean isClosing()
    {
    	return !opened;
    }

    /**
     * 已经关闭
     * @return
     */
    public boolean isClosed()
    {
        return closed;
    }
    
    /**
     * 
     * @param modules
     * @return
     * @throws Exception
     */
    public void netstat(HashMap<String, ModulePerf> mapPid2Module)
    {
        Process process = null;
        BufferedReader bufferedReader = null;
        try
        {
        	String[] cmds = null;
        	if( MyJDK.isLinux() ){
        		cmds = new String[]{"netstat", "-nap"};
        	}
        	else {
        		cmds = new String[]{"netstat", "-aon"};
        	}
            ProcessBuilder pb = new ProcessBuilder( cmds );
            pb.redirectErrorStream( true );
            process = pb.start();
            // 获取屏幕输出显示
            pb.redirectErrorStream( true );
            bufferedReader = new BufferedReader( new InputStreamReader( process.getInputStream() ) );
            String line = null;
            HashMap<String, JSONArray> data = new HashMap<String, JSONArray>();
            HashMap<String, JSONObject> netstats = new HashMap<String, JSONObject>();
            ArrayList<JSONObject> buffer = new ArrayList<JSONObject>();//先缓存下来
            while ((line = bufferedReader.readLine()) != null)
            {
            	String args[] = Tools.split(line, " ");
            	String type = null;
            	String local_port = null;
            	String local_ip = null;
            	String foreign = null;
            	String foreign_ip = null;
            	String foreign_port = null;
            	String state = null;
            	String pid = null;
            	for(String arg : args){
            		if(arg.isEmpty()){
            			continue;
            		}
            		if( type == null ){
            			type = arg.toUpperCase();
            			if( type.startsWith("UDP") ){
            				state = "LISTENING";
            			}
            			continue;
            		}
            		if(!type.startsWith("TCP") && !type.startsWith("UDP") ){
            			break;
            		}
            		if( local_port == null ){
            			int i = arg.lastIndexOf(":");
            			if( i == -1){
            				continue;
            			}
            			local_ip = arg.substring(0, i);
            			local_port = arg.substring(i+1);
            			continue;
            		}
            		if( foreign == null ){
            			foreign = arg;
            			int i = arg.lastIndexOf(":");
            			if( i != -1){
            				foreign_ip = arg.substring(0, i);
            				foreign_port = arg.substring(i+1);
            				foreign_port = foreign_port.equals("*")?"0":foreign_port;
            			}
            			continue;
            		}
            		if( state == null ){
            			state = arg.toUpperCase();//LISTEN CONNECTED TIME_WAIT ESTABLISHED LISTENING
            			state = state.equals("LISTEN")?"LISTENING":state;
            			continue;
            		}
            		if( pid == null ){
            			String[] pidinfo = Tools.split(arg, "/");
            			if( pidinfo.length == 0 || !Tools.isNumeric(pidinfo[0])){
            				break;
            			}
            			pid = pidinfo[0];
            			break;
            		}
            	}
            	if( pid == null || type == null ){
            		continue;
            	}
            	ModulePerf module = mapPid2Module.get(pid);
            	if( module == null ){
            		continue;
            	}
            	
            	JSONObject netstat = netstats.get(pid+"@"+module.getId());
            	if( netstat == null ){
            		netstat = new JSONObject();
            		netstat.put("id", module.getId());//模块ID
            		netstat.put("name", module.getName());//模块名称
            		netstat.put("pid", Integer.parseInt(pid));
            		netstats.put(pid+"@"+module.getId(), netstat);
            		netstat.put("listening", new JSONArray());
            		netstat.put("outgoing", new JSONArray());
            		netstat.put("timestamp", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()));
            		
            		if( !data.containsKey(module.getId()) ){
            			data.put(module.getId(), new JSONArray());
            		}
            		data.get(module.getId()).put(netstat);
            	}
            	JSONObject e = new JSONObject();
            	e.put("type", type);
            	e.put("local_ip", local_ip);
            	e.put("local_port", Integer.parseInt(local_port));
            	e.put("foreign", foreign);
            	e.put("foreign_ip", foreign_ip);
            	e.put("foreign_port", Integer.parseInt(foreign_port));
            	e.put("state", state);
            	e.put("pid", pid);
            	e.put("module", module.getId());

            	if( type.startsWith("TCP") ){
            		if( state.startsWith("LISTEN") ){
            			//表示某进程的监听
                		if( local_ip.indexOf("::") != -1 ){
                			e.put("local_ip", "::");
                			netstat.getJSONArray("listening").put(e);//该端口被某模块使用
                		}
//        				System.out.println("found listen "+e.toString(4));
            		}
            		else {
            			buffer.add(e);
            		}
            	}
            	else {
            		if( local_ip.indexOf("::") != -1 ){
            			e.put("local_ip", "::");
            			netstat.getJSONArray("listening").put(e);//该端口被某模块使用
            		}
            	}
            }
            for(JSONObject e:buffer){
            	String pid = e.getString("pid");
            	String module = e.getString("module");
            	int local_port = e.getInt("local_port");
            	JSONObject netstat = netstats.get(pid+"@"+module);
            	if( netstat == null ){
    				System.out.println("Not found netstat("+pid+"@"+module+") "+e.toString(4));
            		continue;
            	}
            	JSONArray listening = netstat.getJSONArray("listening");
            	int i = 0;
            	for(; i < listening.length(); i++){
            		JSONObject listen = listening.getJSONObject(i);
            		if( local_port == listen.getInt("local_port") &&
        				listen.getString("type").startsWith("TCP") ){
            			if( !listen.has("connections") ){
            				listen.put("connections", new JSONArray());
            			}
            			listen.getJSONArray("connections").put(e);
            			break;
            		}
            	}
            	if( i == listening.length() ){
            		//不归属于监听
            		netstat.getJSONArray("outgoing").put(e);
            	}
            }
            Iterator<String> iterator = data.keySet().iterator();
            while(iterator.hasNext()){
            	String id = iterator.next();//程序ID
            	JSONArray netstat = data.get(id);//根据程序id得到程序模块
            	Module module = modules.get(id);
            	if( module != null ){
            		module.getModulePerf().setNetstat(netstat.toString());
            	}
            	else if( "COSControl".equals(id) ){
            		mainGC.getModulePerf().setNetstat(netstat.toString());
            	}
            }
            process.waitFor(); // 等待编译完成
//            Log.msg("Succeed to netstat "+data.size()+" modules.");
        }
        catch (Exception e)
        {
        	Log.err("Failed to netstat", e);
        }
        finally
        {
        	if( bufferedReader != null ){
        		try {
					bufferedReader.close();
				} catch (IOException e) {
				}
        	}
            if( process != null )
            {
                try
                {
                    process.destroy();
                }
                catch (Exception e)
                {
                    Log.err(e);
                }
            }
        }
    }

    public static void main( String args[] )
    {
//        ModuleMananger mgr = new ModuleMananger();
//        System.out.println( mgr.setCommand( "%java.home%/bin/javaw" ) );
//        System.out.println( mgr.setCommand( "%catalina.home%/bin/bootstrap.jar" ) );
        System.out.println("TimeZone:"+TimeZone.getDefault());
        System.out.println("User.TimeZone:"+System.getProperty("user.timezone"));
        System.out.println((System.currentTimeMillis()%Tools.MILLI_OF_DAY)/1000);
    }
}
