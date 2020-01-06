package com.focus.cos.web.ops.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.security.Key;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.crypto.Cipher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.focus.control.HostPerfHisotry;
import com.focus.control.HostPerfPoint;
import com.focus.control.ModuleMemeory;
import com.focus.control.ModulePerf;
import com.focus.control.SystemPerf;
import com.focus.cos.control.Command;
import com.focus.cos.control.Module;
import com.focus.cos.web.Version;
import com.focus.cos.web.common.Kit;
import com.focus.cos.web.common.PathFactory;
import com.focus.cos.web.ops.vo.ChartDataset;
import com.focus.cos.web.ops.vo.ModuleTrack;
import com.focus.cos.web.ops.vo.MonitorServer;
import com.focus.cos.web.ops.vo.ServerMonitor;
import com.focus.util.Base64;
import com.focus.util.IOHelper;
import com.focus.util.QuickSort;
import com.focus.util.Tools;

/**
 * 监控器
 * @author focus
 *
 */
public abstract class Monitor
{
	public static final Log log = LogFactory.getLog(Monitor.class);
    /*当模块数据改变时，映射其数据*/
    private static HashMap<String, ModulePerf> mapModule = new HashMap<String, ModulePerf>();
	/*缓存系统监控信息 配置的IP地址与监控对象的映射*/
	private HashMap<String,RunFetchMonitor> mapMonitor = new HashMap<String, RunFetchMonitor>();
	/*运行监控引擎*/
	private RunGetMonitor runGetMonitor = null;
	/*主数据库映射表*/
	private static HashMap<String, RunFetchMonitor> H2Databases = new HashMap<String, RunFetchMonitor>();
	/*主数据库映射表*/
	private static HashMap<String, RunFetchMonitor> H2StandbyDatabases = new HashMap<String, RunFetchMonitor>();
	
	public RunFetchMonitor getRunFetchMonitorByDatabase(String addr){
		return H2Databases.get(addr);
	}

	public RunFetchMonitor getRunFetchMonitorByStandbyDatabase(String addr){
		return H2StandbyDatabases.get(addr);
	}
	/**
	 * 获取模块相关实时参数
	 */
	public RunFetchMonitor getRunFetchMonitor(String ip, int port)
	{
		if( port == 0 ) return this.getRunFetchMonitor(ip);
		return this.mapMonitor.get(ip+":"+port);
	}
	/**
	 * 获取模块相关实时参数
	 */
	public RunFetchMonitor getRunFetchMonitor(String ip)
	{
		Iterator<RunFetchMonitor> iter = mapMonitor.values().iterator();
		while( iter.hasNext() )
		{
			RunFetchMonitor runner = iter.next();
			if( runner.ip.equals(ip) ) return runner;
		}
		return null;
	}
	/**
	 * 获取模块相关实时参数
	 */
	public List<RunFetchMonitor> getAllRunFetchMonitor(String ip)
	{
		ArrayList<RunFetchMonitor> list = new ArrayList<RunFetchMonitor>();
		Iterator<RunFetchMonitor> iter = mapMonitor.values().iterator();
		while( iter.hasNext() )
		{
			RunFetchMonitor runner = iter.next();
			if( runner.ip.equals(ip) ) list.add(runner);
		}
		return list;
	}
	/**
	 * 获取模块相关实时参数
	 */
	public ModulePerf getModulePerf(String ip, int port, String id)
	{
		RunFetchMonitor runner = this.mapMonitor.get(ip+":"+port);
		if( runner == null )
		{
			return null;
		}

        for( int i = 0; i < runner.listModulePerfs.size(); i++ )
        {
        	ModulePerf module = runner.listModulePerfs.get(i);
        	if( id.equals(module.getId()) )
        	{
        		return module;
        	}
        }
        return null;
	}
	/**
	 * 获取模块相关实时参数
	 */
	public SystemPerf getPerf(String ip)
	{
		Iterator<RunFetchMonitor> iter = mapMonitor.values().iterator();
		while( iter.hasNext() )
		{
			RunFetchMonitor runner = iter.next();
			if( runner.ip.equals(ip) ){
				return this.getSystemPerf(runner.ip, runner.port);
			}
		}
		return null;
	}
	/**
	 * 根据IP得到系统性能数据对象
	 * @param ip
	 * @return
	 */
	public SystemPerf getSystemPerf(String ip, int port)
	{
		long ts = System.currentTimeMillis();
		RunFetchMonitor runner = this.mapMonitor.get(ip+":"+port);
		if( runner == null )
		{
			return null;
		}
		ArrayList<ModuleTrack> modules = new ArrayList<ModuleTrack>();
		long outerMemory0 = 0;
		long outerMemory1 = 0;
		long inner0 = 0;
		long inner1 = 0;
		int countRun1 = 0, countRunning1 = 0;
		int countRun0 = 0, countRunning0 = 0;
		synchronized(runner.listModulePerfs)
		{
	        for( int i = 0; i < runner.listModulePerfs.size(); i++ )
	        {
	        	ModulePerf module = runner.listModulePerfs.get(i);
	        	ModuleTrack e = new ModuleTrack();
	        	e.setDebug(module.isDebug());
	        	e.setDependence(module.getDependence());
	        	e.setEndTime(module.getEndTime());
	        	e.setId(module.getId());
	        	e.setDead(module.getDead());
	        	e.setMonitorInfo(module.getMonitorInfo());
	        	e.setName(module.getName());
	        	e.setRemark(module.getRemark());
	        	e.setRuntime(module.getRuntime());
	        	e.setStandby(module.getStandby());
	        	e.setStartTime(module.getStartTime());
	        	e.setStatupTime(module.getStartupDate());
	        	e.setState(module.getState());
	        	e.setUsageMemory(module.getUsageMemory());
	        	e.setVersion(module.getVersion());
	        	e.setProgrammer(module.getProgrammer());
	        	e.setCfgfile(module.getCfgfile());
	        	e.setPid(module.getPid());
	        	e.setType(module.getType());
	        	e.setNetstat(module.getNetstat());
	        	e.setDaemon(module.isDaemon());
	        	e.setCountException(module.getCountException());
	        	e.setPrintException(module.getPrintException());
	        	e.setExpiredException(!runner.exceptionModules.containsKey(module.getId()));//是否过期
	        	modules.add(e);
        		if( "超级管理员".equals(module.getProgrammer()) )
        		{
        			countRun0 += 1;
        			countRunning0 += module.getState() == Module.STATE_STARTUP?1:0;
        		}
        		else
        		{
        			countRun1 += 1;
        			countRunning1 += module.getState() == Module.STATE_STARTUP?1:0;
        		}
	        	String usageMemory = module.getUsageMemory();
//    			log.warn(String.format("[%s] The usage of memory is '%s' from %s:%s",
//    				module.getId(), usageMemory, ip, port));
        		if( usageMemory == null ) continue;
        		String args[] = Tools.split(usageMemory, "/");
        		if( args.length == 1 ){
        			if( !args[0].endsWith("K") ){
        				continue;
        			}
            		String s0 = args[0].substring(0,  args[0].length()-1);
            		if( Tools.isNumeric(s0) ) e.setRes(Long.parseLong(s0));
            		e.setUsageMemory(Kit.bytesScale(e.getRes()*1024));
        		}
        		else if( args.length > 1 ) {
        			if( !args[0].endsWith("K") || !args[1].endsWith("K") ){
        				continue;
        			}
            		String s0 = args[0].substring(0,  args[0].length()-1);
            		String s1 = args[1].substring(0,  args[1].length()-1);
            		if( Tools.isNumeric(s0) ) e.setRes(Long.parseLong(s0));
            		if( Tools.isNumeric(s1) ) e.setVirt(Long.parseLong(s1));
            		e.setUsageMemory(Kit.bytesScale(e.getRes()*1024)+"/"+Kit.bytesScale(e.getVirt()*1024));
        		}
        		else{
        			log.warn(String.format("[%s] The usage of memory is '%s' from %s:%s",
        				module.getId(), usageMemory, ip, port));
        		}
        		if( "超级管理员".equals(module.getProgrammer()) )
        		{
        			if( e.getState() == 1 || e.getState() == 2 )
	        			inner0 += e.getRes();
					inner1 += e.getVirt();
        		}
        		else
        		{
        			if( e.getState() == 1 || e.getState() == 2 )
        				outerMemory0 += e.getRes();
        			outerMemory1 += e.getVirt();
        		}
        		if( "Zookeeper".equals(module.getId()) )
        		{
                	runner.sysDesc.setProperty(module.getId(), e);
                	runner.flushClusterServerState();
        		}
        		else if( "COSPortal".equals(module.getId()) )
        		{
        			e.setVersion(Version.Versions[Version.Versions.length-1][0]);
        		}
        		else if( "COSControl".equals(module.getId()) && !runner.sysDesc.existProperty("cos.version"))
        		{
        			e.setType(1);
        			String version = module.getVersion();
        			runner.sysDesc.setProperty("cos.version", version);
        			args = Tools.split(version, ".");
        			version = "";
        			for( int k = 0; k < args.length; k++){
        				if( k > 0 ) version+=".";
        				version += (args[k].length()==2?args[k]:("0"+args[k]));
        			}
        			runner.sysDesc.setProperty("cosVersion", version);
        			
        		}
			}
		}
        QuickSort sorter = new QuickSort()
        {
			public boolean compareTo(Object sortSrc, Object pivot)
			{
				ModuleTrack left = (ModuleTrack)sortSrc;
				ModuleTrack right = (ModuleTrack)pivot;
				boolean b1 = left.getId().equals("COSControl");
				boolean b2 = right.getId().equals("COSControl");
				if( b1 || b2 ) return b2;
				boolean b0 = left.getName().compareTo(right.getName())>0;
				return b0;
			}
        };
        sorter.sort(modules);
        if( modules.size() > 0 )
        {
        	if( !"COSControl".equals(modules.get(modules.size()-1).getId()) )
        	{
	        	ModuleTrack e = new ModuleTrack();
	        	e.setType(1);
	        	e.setId("COSControl");
	        	e.setMonitorInfo("Service Running.");
	            e.setName("主控引擎");
	            e.setStatupTime((Date)runner.sysDesc.getProperty("StartupTime"));
	            e.setVersion(runner.sysDesc.getControlVersion());
	            e.setRemark("负责监控系统配置的所有子系统模块工作。");
	            e.setProgrammer("超级管理员");
	            e.setState(Module.STATE_STARTUP);
	            e.setMonitorInfo("Service Running.");
	        	e.setRuntime("");
	        	e.setUsageMemory("");
        		modules.add(e);
        	}
        }
        
        if( runner.sysDesc != null )
        {
        	if( runner.isConnect() ) runner.sysDesc.setProperty("Running", true);
        	else runner.sysDesc.setProperty("Running", false);
        	runner.sysDesc.setProperty("tracking", runner.isConnect());
        	runner.sysDesc.setProperty("HeartbeatTime", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", runner.getHeartbeat()));
        	runner.sysDesc.setProperty("PerfTime", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", runner.sysDesc.getPerfTime().getTime()));
        	runner.sysDesc.setProperty("ModuleTrack", modules);
        	runner.sysDesc.setProperty("RunMemory", outerMemory0);//记录运行的内存
        	runner.sysDesc.setProperty("RunMemory0", inner0);//记录运行的内存
        	runner.sysDesc.setProperty("RunStateInfo", countRunning1+"/"+countRun1);
        	runner.sysDesc.setProperty("RunStateInfo0", countRunning0+"/"+countRun0);
        	runner.sysDesc.setProperty("TotalMemeory", Kit.bytesScale(outerMemory0*1024)+"/"+Kit.bytesScale(outerMemory1*1024));
        	runner.sysDesc.setProperty("TotalMemeory0", Kit.bytesScale(inner0*1024)+"/"+Kit.bytesScale(inner1*1024));
        }
        ts = System.currentTimeMillis() - ts;
		return runner.sysDesc;
	}

	/**
	 * 得到监控主机列表
	 * @return
	public List<String> getHosts()
	{
		try
		{
			ZooKeeper zookeeper = ZKMgr.getZooKeeper();
			String path = "/cos/config/monitor/cluster";
			Stat stat = zookeeper.exists(path, false); 
			if( stat != null)
			{
				return zookeeper.getChildren(path, false);
			}
		}
		catch(Exception e )
		{
			log.error("Failed to query the nodes of cluster for exception "+e);
		}
		return null;
	}
	 */

	/**
	 * 得到模块日志信息
	 * @param host
	 * @param id
	 * @return
	 */
	public ArrayList<String[]> getModuleLogs(String host, int port, String id)
	{
    	byte[] payload = new byte[64*1024];
    	int offset = 0;
    	payload[offset++] = (byte)5;
    	payload[offset++] = (byte)id.length();
    	offset = Tools.copyByteArray(id.getBytes(), payload, offset);
    	DatagramSocket datagramSocket = null;
    	ArrayList<String[]> out = new ArrayList<String[]>();
        try
        {
        	datagramSocket = new DatagramSocket();
            datagramSocket.setSoTimeout(15000);
            DatagramPacket request = new DatagramPacket(payload, 0, offset, InetAddress.getByName( host ), port );
            datagramSocket.send( request );
            DatagramPacket reponse = new DatagramPacket(payload, 0, payload.length, request.getAddress(), request.getPort() );
			datagramSocket.setSoTimeout(15000);
            datagramSocket.receive( reponse );
            offset = 0;
            while( payload[offset] != -1 )
            {
            	String args[] = new String[7];
	            int len = Tools.bytesToInt(payload, offset, 2);
	            offset += 2;
	            for( int i = 0; i < len; i++ )
	            {
	            	if( payload[i+offset] == '\\' )
	            	{
	            		payload[i+offset] = '/';
	            	}
	            }
	            String filepath = new String(payload, offset, len);
	            int beginIndex = filepath.lastIndexOf("/");
	            String filename = filepath.substring(beginIndex+1);
	            offset += len;
	            long length = Tools.bytesToLong(payload, offset, 8);
	            double size = length;
	            offset += 8;
	            
	            StringBuffer sb = new StringBuffer();
	            sb.append(filename);
            	sb.append("(");
	            if( size < 1024*1024 )
	            {
	            	size = size/1024;
	            	sb.append(Tools.DF.format(size));
	            	sb.append("K");
		            args[4] = "K";
	            }
	            else if( size < 1024*1024*1024 )
	            {
	            	sb.append(Tools.DF.format(size/(1024*1024)));
	            	sb.append("M");
		            args[4] = "M";
	            }
	            else
	            {
	            	sb.append(Tools.DF.format(size/(1024*1024*1024)));
	            	sb.append("G");
		            args[4] = "G";
	            }
	            sb.append(")");
	            args[0] = filepath;
	            args[1] = sb.toString();
	            args[2] = host;
	            args[3] = id;
	            args[5] = String.valueOf(length);
	            args[6] = String.valueOf(port);
	            out.add(args);
            }
        }
        catch(Exception e)
        {
        	log.error("Failed to get the list of module-log from "+host+":"+port+" for exception "+e.getMessage());
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
		return out;
	}

	/**
	 * 检查是否开始监听
	 */
	public synchronized boolean checkStart()
	{
		if( runGetMonitor == null )
		{
			runGetMonitor = new RunGetMonitor();
			runGetMonitor.start();
			synchronized(runGetMonitor)
			{
				try
				{
					log.info("Startup the monitor of get.");
					this.wait(3000);
				}
				catch (InterruptedException e)
				{
				}
			}
			return false;
		}
		return true;
	}
	/**
	 * DWR返回集群监控数据
	 * @return
	 */
	public ArrayList<MonitorServer> getMonitorServers(
		ArrayList<JSONObject> servers, 
		HashMap<Integer, 
		JSONObject> filter,
		JSONObject privileges,
		boolean sysadmin)
	{
		checkStart();
		ArrayList<MonitorServer> monitors = new ArrayList<MonitorServer>();
		try
		{
			SystemPerf instance;
			for(int i = 0; i < servers.size(); i++)
			{
				JSONObject server = servers.get(i);
				if( !filter.containsKey(server.getInt("pid")) ) continue;
				RunFetchMonitor runner = mapMonitor.get(server.getString("ip")+":"+server.getInt("port"));
				if( runner == null || runner.sysDesc == null )
				{
					continue;
				}
				if( privileges == null || !privileges.has(runner.getSysDesc().getSecurityKey()) )
				{
					if( !sysadmin ) continue;
				}
				MonitorServer monitor = new MonitorServer(runner);
				monitors.add(monitor);
				instance = runner.sysDesc;
	            int countRun = 0;
	            for(ModulePerf module : runner.listModulePerfs )
	            {
	        		countRun += module.getState() == Module.STATE_STARTUP?1:0;
	            }
	            if( instance != null ) instance.setProperty("RunState", countRun+"/"+runner.listModulePerfs.size());
			}
		}
		catch(Exception e )
		{
			log.error("Failed to get the data of monitor from runner", e);
		}
		return monitors;
	}
	
	/**
	 * 返回集群服务器列表用于启动或停止服务
	 * @return
	 */
	public abstract ArrayList<JSONObject> getClusterServers();
	public abstract void setClusterServerState(RunFetchMonitor runner);
	public abstract void releaseClusterServer(int serverid);
	/**
	 * 检查所有监控运行器，如果没有的就创建并启动，如果在工作中的心跳超时就断开连接。
	 * @param runner
	 */
	private synchronized void chkMonitorRunner()
	{
		ArrayList<JSONObject> cluster = getClusterServers();
		if( cluster == null ) return;
		StringBuffer sb = new StringBuffer("Check the runner of montor.");
		HashMap<String, String> tempMap = new HashMap<String, String>();
		boolean printlog = false;
		for( int i = 0; i < cluster.size(); i++ )
		{
			JSONObject e = cluster.get(i);
			String ip = e.getString("ip");
			int port = e.getInt("port");
			String synchmode = e.has("synchmode")?e.getString("synchmode"):"tcp";
			String monitoraddr = ip+":"+port;
			sb.append("\r\n\t");
			sb.append(monitoraddr);
			sb.append("(");
			sb.append(synchmode);
			sb.append(")");
			if( tempMap.containsKey(monitoraddr) ){
				sb.append("\tfound repeat system monitor.");
				releaseClusterServer(e.getInt("id"));
				continue;
			}
			RunFetchMonitor runner = mapMonitor.get(monitoraddr);
			tempMap.put(monitoraddr, null);
			if( runner == null )
			{
				runner = new RunFetchMonitor(ip, port, e.getInt("id"), e.getInt("pid")){
					public void flushClusterServerState() {
						setClusterServerState(this);
					}
				};
				mapMonitor.put(runner.toString(), runner);//加映射
				sb.append("\tadd new system monitor.");
				printlog = true;
			}
			else
			{
				sb.append("\tthis is the current system monitor("+runner.status()+").");
			}
			
			if( "tcp".equals(synchmode))
			{
				if( !runner.isStartup() )
				{
					runner.start();//只有TCP模式才启动监控
				}
				else if( runner.isStartup() && runner.headtbeatTimeout(49000) )
				{
					sb.append("\ttimeout for heartbeat and break it.");
					runner.disconnect();
					printlog = true;
				}
			}
			else{
				if( !runner.isStartup() ){
					sb.append("\twait the request of http/json.");
					printlog = true;
				}
			}
		}
		if( printlog ) log.info(sb.toString());
	}
	
	/**
	 * 删除监控运行
	 * @param hosts
	 */
	public synchronized void delMonitorRunner(String ip, int port)
	{
		RunFetchMonitor runner = mapMonitor.remove(ip+":"+port);
		if( runner != null )
		{
			runner.disconnect();
			if( runner.sysDesc != null && runner.sysDesc.getSecurityKey() != null )
			{
//				java.io.File file = new java.io.File(PathFactory.getDataPath(), "monitor/perf/"+runner.sysDesc.getSecurityKey());
//				file.delete();
				java.io.File file = new java.io.File(PathFactory.getDataPath(), "monitor/history/"+Tools.encodeMD5(runner.sysDesc.getSecurityKey()));
				file.delete();
			}
		}
//		removeMonitor(runner);
	}

	private static boolean getMonitorRuning = true;//是否继续运行监控信息获取
	/**
	 * 运行监控信息获取线程
	 * @author Focus Lau
	 * @date 2012-04-13
	 */
	class RunGetMonitor extends Thread
	{
		public synchronized void close()
		{
			getMonitorRuning = false;
			this.notify();
		}
		
		public void run()
		{
			while( getMonitorRuning )
			{
				try
				{
					chkMonitorRunner();
					synchronized(this)
					{//如果在工作的
						try
						{
							this.notifyAll();
							this.wait(7000);//每7秒尝试检查连接一次
						}
						catch (InterruptedException e)
						{
						}
					}
				}
				catch (Exception e)
				{
					log.error("Failed to run get monitor:", e);
				}
			}
		}
	}
	/**
	 * 运行监控信息提取线程，针对某个IP+PORT的cos服务器
	 * @author Focus Lau
	 * @date 2012-04-13
	 */
	public static abstract class RunFetchMonitor implements Runnable
	{
		static final int N_HU = 0;//主机信息更新
		static final int N_MU = 1;//软件模块更新
		static final int N_MQ = 2;//软件模块退出
		static final int N_TU = 255;//心跳，兼主机标题更新
		static final int N_HHU = 10;//历史主机更新
    	private Socket socket = null;
    	private String status;
    	private InputStream is = null;
    	private String ip;
    	private int port;
		private int serverid;
    	private int pid;
    	private SystemPerf sysDesc;
    	private boolean gateone;
    	private boolean startup;
    	private boolean closed;
    	private boolean connect;
    	private ArrayList<ModulePerf> listModulePerfs = new ArrayList<ModulePerf>();
    	private HashMap<String, ModulePerf> mapModulePerfs = new HashMap<String, ModulePerf>();
		private long heartbeat = 0;
		private long binSize0;//二进制码流累计字节数前一周期
		private long binSize1;//二进制码流累计字节数当前
		private int rcvCount0;
		private int rcvCount1;
		private long nhuSize0;
		private long nhuSize1;
		private long nmSize0;
		private long nmSize1;
		private long ntuSize0;
		private long ntuSize1;
		private long nhhuSize0;
		private long nhhuSize1;
		private int nhuCount0;
		private int nhuCount1;
		private int nmCount0;
		private int nmCount1;
		private int ntuCount0;
		private int ntuCount1;
		private int nhhuCount0;
		private int nhhuCount1;
		private int errorCount;
		private long timestamp;
		private long startuptime;
		private boolean tcpmode;
		private HashMap<String, ModulePerf> deadModules = new HashMap<String, ModulePerf>();
		private HashMap<String, Long> exceptionModules = new HashMap<String, Long>();

		RunFetchMonitor(String host, int port, int serverid, int pid)
		{
			this.ip = host;
			this.port = port;
			this.pid = pid;
			this.serverid = serverid;
		}
		
		/**
		 * 得到监控的JSON数据对象
		 * @return
		 */
		public JSONObject getJsonMonitorData(){
			JSONObject data = new JSONObject();
			data.put("ip", this.getIp());
			data.put("port", this.getPort());
			data.put("system_uptime", sysDesc.getSystemUpTime());
			data.put("descript", sysDesc.getDescript());
			data.put("host_name", sysDesc.getHostName());
			data.put("os_name", sysDesc.existProperty("OSName")?sysDesc.getProperty("OSName").toString():"");
			data.put("os_version", sysDesc.existProperty("OSVersion")?sysDesc.getProperty("OSVersion").toString():"");
			data.put("server_no", sysDesc.getSecurityKey());
			
			//关于内存
			JSONObject mem = new JSONObject();
			data.put("mem", mem);
			double usage = sysDesc.getPhyMemUsage();
			mem.put("usage", usage/10000);
			mem.put("used", sysDesc.getPhyMemUsed());
			Long cached = (Long)this.sysDesc.getProperty("Cached");
			mem.put("cached", cached!=null?cached:0);
			Long swap = (Long)this.sysDesc.getProperty("SwapSpaceSize");
			mem.put("swap", swap!=null?swap:0);
			//关于CPU
			JSONObject cpu = new JSONObject();
			data.put("cpu", cpu);
			usage = sysDesc.getCpuLoad();
			cpu.put("usage", usage/10000);
			//关于网络流量
			JSONObject net = new JSONObject();
			data.put("net", net);
			net.put("input", sysDesc.existProperty("NetIOLoad1")?sysDesc.getProperty("NetIOLoad1"):0);
			net.put("output", sysDesc.existProperty("NetIOLoad1")?sysDesc.getProperty("NetIOLoad1"):0);
			
			
			JSONObject io = new JSONObject();
			data.put("io", io);
			io.put("read", sysDesc.existProperty("IOLoadrs")?sysDesc.getProperty("IOLoadrs"):0);
			io.put("write", sysDesc.existProperty("IOLoadws")?sysDesc.getProperty("IOLoadws"):0);
			
			JSONObject disk = new JSONObject();
			data.put("disk", disk);
			usage = sysDesc.getDiskUsage();
			disk.put("usage", usage/10000);
			disk.put("used", sysDesc.getDiskUsed());
			String diskInfo = (String)sysDesc.getProperty("DiskInfo");
			disk.put("storages_info", diskInfo!=null?diskInfo:"");

			data.put("host_info", sysDesc.existProperty("HostInfo")?sysDesc.getProperty("HostInfo").toString():"");
			data.put("ip_info", sysDesc.existProperty("IPInfo")?sysDesc.getProperty("IPInfo").toString():"");
			
			if( sysDesc.existProperty("PING") ){
				Object obj = sysDesc.getProperty("PING");
				if( obj != null )
				{
					JSONArray pings = data.getJSONArray("pings");
					ArrayList<?> array = (ArrayList<?>)obj;
					for(Object e : array )
					{
						if( e instanceof String ){
							pings.put(e.toString().toLowerCase());
						}
					}
					data.put("pings", pings);
				}
			}
			
			JSONArray modules = new JSONArray();
			data.put("modules", modules);
			for(ModulePerf module : listModulePerfs ){
				JSONObject m = new JSONObject();
				m.put("name", module.getName());
				m.put("startup_time", module.getStartupTime());
				m.put("id", module.getId());
				m.put("version", module.getVersion());
				m.put("log_path", module.getModuleLogPath().toString());
				m.put("cfg_path", module.getCfgfile());
				m.put("remark", module.getRemark());
				m.put("programer", module.getProgrammer());
				m.put("programer_email", module.getProgrammerContact());
				m.put("state", module.getState());
				m.put("run_info", module.getMonitorInfo());
				m.put("mem_usage", module.getUsageMemory());
				m.put("run_time", module.getCpuused());
				modules.put(m);
			}
			return data;
		}
		
		/**
		 * 设置数据通过JSON回报过来
		 * @param Object
		 * @throws ParseException 
		 * @throws JSONException 
		 */
		public void setJsonMonitorData(JSONObject data) throws Exception{
			if(!data.has("server_no")){
				throw new Exception("未提供伺服器唯一编号(server_no)");
			}
			this.sysDesc = new SystemPerf();
			heartbeat = System.currentTimeMillis();
			if( data.has("mem") ){
				JSONObject mem = data.getJSONObject("mem");
				double usage = mem.has("usage")?mem.getDouble("usage"):0;
				int usage1 = (int)(10000*usage);
				long used = mem.has("used")?mem.getLong("used"):0;
				this.sysDesc.setPhyMemUsage(usage1);//使用内存的百分比1表示0.01%
				this.sysDesc.setPhyMemUsed(used);//使用内存的具体量
				long total = usage1>0?(used*10000/usage1):0;//已使用的物理内存/可用的物理内存
				this.sysDesc.setProperty("PhysicalMemoryInfo", SystemPerf.getShowSpace(total));
				this.sysDesc.setPhyMemUsageInof(usage);
				if( mem.has("swap") ){
					this.sysDesc.setProperty("SwapSpaceSize", mem.getLong("swap"));
				}
				if( mem.has("cached") ){
					this.sysDesc.setProperty("Cached", mem.getLong("cached"));
				}
			}
			if( data.has("cpu") ){
				JSONObject cpu = data.getJSONObject("cpu");
				double usage = cpu.has("usage")?cpu.getDouble("usage"):0;
				this.sysDesc.setCpuLoadInfo(usage);
				int cpuLoad = (int)(10000*usage);
				this.sysDesc.setCpuLoad(cpuLoad);
			}
			if( data.has("net") ){
				JSONObject net = data.getJSONObject("net");
				this.sysDesc.setProperty("NetIOLoad0", net.has("input")?net.getLong("input"):0);
				this.sysDesc.setProperty("NetIOLoad1", net.has("output")?net.getLong("output"):0);
			}
			if( data.has("io") ){
				JSONObject io = data.getJSONObject("io");
				this.sysDesc.setProperty("IOLoadrs", io.has("read")?io.getLong("read"):0);
				this.sysDesc.setProperty("IOLoadws", io.has("write")?io.getLong("write"):0);
			}
			if( data.has("disk") ){
				JSONObject disk = data.getJSONObject("disk");
				double usage = disk.has("usage")?disk.getDouble("usage"):0;
				int usage1 = (int)(10000*usage);
				long used = disk.has("used")?disk.getLong("used"):0;
				this.sysDesc.setDiskUsage(usage1);
				long total = usage>0?(used*10000/(usage1)):0;
				this.sysDesc.setDiskUsed(((int)total-used));
				this.sysDesc.setDiskUsageInfo(usage);
				this.sysDesc.setProperty("DiskInfo", disk.has("storages_info")?disk.getString("storages_info"):"");
			}
			if( data.has("pings") ){
				ArrayList<String> pings = new ArrayList<String>();
				JSONArray _pings = data.getJSONArray("pings");
				for(int i = 0; i < _pings.length(); i++ ){
					pings.add(_pings.getString(i));
				}
				this.sysDesc.setProperty("PING", pings);
			}
			this.sysDesc.setPerfTime(Calendar.getInstance().getTime());
			String name = data.has("descript")?data.getString("descript"):"";
			this.sysDesc.setDescript(name);
			this.sysDesc.setSecurityKey(data.has("server_no")?data.getString("server_no"):"");
			this.sysDesc.setHostName(data.has("host_name")?data.getString("host_name"):"");
			this.sysDesc.setProperty("OSName", data.has("os_name")?data.getString("os_name"):"");
			this.sysDesc.setProperty("OSVersion", data.has("os_version")?data.getString("os_version"):"");
			this.sysDesc.setProperty("HostInfo", data.has("host_info")?data.getString("host_info"):"");
			this.sysDesc.setSystemUpTime(data.has("system_uptime")?data.getString("system_uptime"):"");
			this.sysDesc.setProperty("IPInfo", data.has("ip_info")?data.getString("ip_info"):"");
			this.sysDesc.setProperty("Running", true);
			
			listModulePerfs.clear();
			if( data.has("modules") ) {
				JSONArray modules = data.getJSONArray("modules");
				for(int i = 0; i < modules.length(); i++ ){
					JSONObject module = modules.getJSONObject(i);
					ModulePerf modulePerf = new ModulePerf();
					modulePerf.setName(module.has("name")?module.getString("name"):"");
					if( module.has("startup_time") ){
						String time = module.getString("startup_time");
						if( time.length() == 14 ){
							final SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss");
							modulePerf.setStartupTime(sdf.parse(time));
						}
						else if( time.length() > 14 ){
							final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							modulePerf.setStartupTime(sdf.parse(time));
						}
					}
					modulePerf.setId(module.has("id")?module.getString("id"):"");
					modulePerf.setVersion(module.has("version")?module.getString("version"):"");
					if( module.has("log_path") ){
						modulePerf.setModuleLogPath(new File(module.getString("log_path")));
					}
					modulePerf.setRemark(module.has("remark")?module.getString("remark"):"");
					modulePerf.setProgrammer(module.has("programer")?module.getString("programer"):"");
					modulePerf.setProgrammerContact(module.has("programer_email")?module.getString("programer_email"):"");
					modulePerf.setState(module.has("state")?module.getInt("state"):Module.STATE_INIT);//Module.STATE_STARTUP;
					modulePerf.setMonitorInfo(module.has("run_info")?module.getString("run_info"):"");
					//设置使用内存
					modulePerf.setUsageMemory(module.has("mem_usage")?module.getString("mem_usage"):"");
					modulePerf.setRuntime(module.has("run_time")?module.getInt("run_time"):0);
		        	
					listModulePerfs.add(modulePerf);
	    			mapModulePerfs.put(modulePerf.getId(), modulePerf);
	    			
	    			if( module.has("mem_usage") ){
	    				String args[] = Tools.split(module.getString("mem_usage"), "/");
	    				if( args.length == 2 ){
	    	        		ModuleMemeory memory = new ModuleMemeory(modulePerf.getId(), "");
	    	    			memory.setId(rcvCount1);
	    	    			memory.setRuntime(System.currentTimeMillis());
	    	    			args[0] = args[0].replace("K", "");
	    	    			args[1] = args[1].replace("K", "");
	    	    			if( Tools.isNumeric(args[0]) ){
	    	    				memory.setHc(Integer.parseInt(args[0]));
	    	    			}
	    	    			if( Tools.isNumeric(args[1]) ){
	    	    				memory.setHe(Integer.parseInt(args[1]));
	    	    			}
	    	    			modulePerf.setUsageMemory(args[0]+"K/"+args[1]+"K");
	    	    			if( modulePerf.getMemories().size() > 2048 )
	    	    			{
	    	    				modulePerf.getMemories().remove(0);
	    	    			}
	    	    			modulePerf.addModuleMemeory(memory);		
	    				}
	    			}
	    			
				}
			}
			HostPerfHisotry history = null;
    		java.io.File file = new java.io.File(PathFactory.getDataPath(), "monitor/history/"+Tools.encodeMD5(sysDesc.getSecurityKey()));
			if( file.exists() )
	        {
	        	history = (HostPerfHisotry)IOHelper.readSerializableNoException(file);
	        }
	        if( history == null )
	        {
	        	history = new HostPerfHisotry();
	        }

	    	HostPerfPoint point = new HostPerfPoint();
	    	point.setTime(sysDesc.getPerfTime().getTime());
	    	point.setCpuload(sysDesc.getCpuLoad());
	    	point.setMemusage(sysDesc.getPhyMemUsage());
	    	point.setMemused(sysDesc.getPhyMemUsed());
	    	point.setNetload0((Long)this.sysDesc.getProperty("NetIOLoad0"));
	    	point.setNetload1((Long)this.sysDesc.getProperty("NetIOLoad1"));
	    	point.setIoload0((Long)this.sysDesc.getProperty("IOLoadrs"));
	    	point.setIoload1((Long)this.sysDesc.getProperty("IOLoadws"));
	        history.add(point);
	        //删除7天前的历史数据，只保留7天内的数据
	        int d = point.getTime() - 8*Tools.SECOND_OF_DAY;
	        if( history.get(0).getTime() < d )
	        {
	        	HostPerfHisotry history0 = new HostPerfHisotry();
	        	for( HostPerfPoint p : history )
	        		if( p.getTime() > d ) history0.add(p);
	        	history = history0;
	        }
    		IOHelper.writeSerializable(file, history);

			this.binSize1 += data.toString().length();
			this.rcvCount1 += 1;

			flushClusterServerState();
		}
		/**
		 * 充值流信息
		 */
		public synchronized void resetFlowInfo()
		{
			binSize1 = 0;//二进制码流累计字节数前一周期
			rcvCount1 = 0;//二进制码流累计字节数当前
			nhuSize1 = 0;
			nmSize1 = 0;
			ntuSize1 = 0;
			nhhuSize1 = 0;
			nhuCount1 = 0;
			nmCount1 = 0;
			ntuCount1 = 0;
			nhhuCount1 = 0;
			binSize0 = binSize1;
			rcvCount0 = rcvCount1;
			nhuSize0 = nhuSize1;
			nmSize0 = nmSize1;
			ntuSize0 = ntuSize1;
			nhhuSize0 = nhhuSize1;
			nhuCount0 = nhuCount1;
			nmCount0 = nmCount1;
			ntuCount0 = ntuCount1;
			nhhuCount0 = nhhuCount1;
			timestamp = System.currentTimeMillis();
			heartbeat = timestamp;
			startuptime = timestamp;
		}
		
		public synchronized void loadFlowInfo(JSONObject e)
		{
			if( this.sysDesc == null ) return;
			e.put("host", this.ip);
			e.put("name", this.sysDesc.getDescript());
			e.put("version", this.sysDesc.getProperty("cos.control.version"));
			e.put("startuptime", Tools.getFormatTime("MM-dd HH:mm:ss", startuptime));
			long ts = System.currentTimeMillis()-this.startuptime;
			e.put("totalsize", binSize1);
			e.put("totalcount", rcvCount1);
			e.put("duration", Kit.getDurationMs(ts));
			e.put("avgload_", binSize1*1000/ts);
			e.put("avgload", Kit.bytesScale(e.getLong("avgload_"))+"/秒");

			ts = System.currentTimeMillis() - this.timestamp;
			long size = binSize1 - binSize0;
			int count = rcvCount1 - rcvCount0;
			e.put("currsize", size);
			e.put("currcount", count);
			e.put("period", Kit.getDurationMs(ts));
			e.put("load_", size*1000/ts);
			e.put("load", Kit.bytesScale(e.getLong("load_"))+"/秒");

			e.put("errorcount", errorCount);
			e.put("hearttime", Tools.getFormatTime("HH:mm:ss", heartbeat));
			
			if( size == 0 ) return;
			e.put("info00", nhuCount1);
			e.put("info10", nmCount1);
			e.put("info20", nhhuCount1);
			e.put("info30", ntuCount1);
			
			count = nhuCount1 - nhuCount0;
			e.put("info01", count+"/"+nhuCount1);
			e.put("info02", Kit.showPercent((nhuSize1-nhuSize0)*10000/size));
			e.put("info03", nhuSize1);
			e.put("info04", Kit.showPercent(nhuSize1*10000/binSize1));
			
			count = nmCount1 - nmCount0;
			e.put("info11", count+"/"+nmCount1);
			e.put("info12", Kit.showPercent((nmSize1-nmSize0)*10000/size));
			e.put("info13", nmSize1);
			e.put("info14", Kit.showPercent(nmSize1*10000/binSize1));
			
			count = nhhuCount1 - nhhuCount0;
			e.put("info21", count+"/"+nhhuCount1);
			e.put("info22", Kit.showPercent((nhhuSize1-nhhuSize0)*10000/size));
			e.put("info23", nhhuSize1);
			e.put("info24", Kit.showPercent(nhhuSize1*10000/binSize1));
			
			count = ntuCount1 - ntuCount0;
			e.put("info31", count+"/"+ntuCount1);
			e.put("info32", Kit.showPercent((ntuSize1-ntuSize0)*10000/size));
			e.put("info33", ntuSize1);
			e.put("info34", Kit.showPercent(ntuSize1*10000/binSize1));
		}
		/**
		 * 得到运行信息数据
		 * @return
		 */
		public synchronized String getRunnerInfo()
		{
			long ts = System.currentTimeMillis() - this.timestamp;
			long size = binSize1 - binSize0;
			if( size == 0 ) return "";
			StringBuffer sb = new StringBuffer();
			int count = rcvCount1 - rcvCount0;
			sb.append(rcvCount1);
			sb.append("条 ");
			sb.append(Kit.bytesScale(binSize1));
			sb.append(" ");
			sb.append(count);
			sb.append("条 ");
			sb.append(Kit.bytesScale(size));
			sb.append(" ");
			sb.append(Kit.bytesScale(size*1000/ts));
			sb.append("/秒 ");
			sb.append(" 当前周期 ");
			sb.append(Kit.getDurationMs(ts));
			sb.append("\r\n\t运行周期 ");
			ts = System.currentTimeMillis()-this.startuptime;
			sb.append(Kit.getDurationMs(ts));
			sb.append(" ");
			sb.append(Kit.bytesScale(binSize1*1000/ts));
			sb.append("/秒 ");
			sb.append("\r\n\t累计错误次数 ");
			sb.append(errorCount);
			
			count = nhuCount1 - nhuCount0;
			sb.append("\r\n\t主机信息("+count+"/"+nhuCount1+")占 ");
			sb.append(Kit.showPercent((nhuSize1-nhuSize0)*10000/size));
			sb.append("，累计"+Kit.bytesScale(nhuSize1)+" 占比");
			sb.append(Kit.showPercent(nhuSize1*10000/binSize1));
			count = nmCount1 - nmCount0;
			sb.append("\r\n\t程序信息("+count+"/"+nmCount1+")占 ");
			sb.append(Kit.showPercent((nmSize1-nmSize0)*10000/size));
			sb.append("，累计"+Kit.bytesScale(nmSize1)+" 占比");
			sb.append(Kit.showPercent(nmSize1*10000/binSize1));
			count = nhhuCount1 - nhhuCount0;
			sb.append("\r\n\t负载历史("+count+"/"+nhhuCount1+")占 ");
			sb.append(Kit.showPercent((nhhuSize1-nhhuSize0)*10000/size));
			sb.append("，累计"+Kit.bytesScale(nhhuSize1)+" 占比");
			sb.append(Kit.showPercent(nhhuSize1*10000/binSize1));
			count = ntuCount1 - ntuCount0;
			sb.append("\r\n\t心跳("+count+"/"+ntuCount1+")占 ");
			sb.append(Kit.showPercent((ntuSize1-ntuSize0)*10000/size));
			sb.append("，累计"+Kit.bytesScale(ntuSize1)+" 占比");
			sb.append(Kit.showPercent(ntuSize1*10000/binSize1));
			return sb.toString();
		}
		/**
		 * 心跳超时，1分钟没有收到心跳消息算超时
		 * @return
		 */
		public boolean headtbeatTimeout(int ts)
		{
			return System.currentTimeMillis() - this.heartbeat > ts;
		}
		private Thread thread;
		public void start()
		{
			this.listModulePerfs.clear();
    		thread = new Thread(this);
    		thread.start();
		}
		
		public boolean isStartup()
		{
			if( !startup && !tcpmode ){
				startup = true;
				return false;
			}
			return startup;
		}
		
		public boolean isConnect()
		{
			if( tcpmode ){
				return connect && !headtbeatTimeout(49000);
			}
			else{
				return !headtbeatTimeout(600000);	
			}
		}
		
		public boolean equals(Object o)
		{
			return ip.equals(o.toString());
		}
		
		public String toString()
		{
			return ip+":"+port;
		}
		
		public String status()
		{
			return "connect:"+isConnect()+" "+status;
		}
		
		public String getKey()
		{
			return ip+"_"+port;
		}

		public long getStartuptime() {
			return startuptime;
		}

		public long getHeartbeat() {
			return heartbeat;
		}
		/**
		 * 关闭监控，不能被重启
		public void close()
		{
			log.debug("Close monitor("+ip+":"+port+").");
			disconnect();
		}
		 */
		
		public synchronized void disconnect()
		{
			if( socket != null )
			try
			{
				log.debug("Disconnect monitor("+ip+":"+port+").");
				closed = true;
				is.close();
				socket.close();
				this.deadModules.clear();
				this.exceptionModules.clear();
			}
			catch(Exception e)
			{
				log.error("Failed to disconnect:", e);
			}
		}
		
		public void run()
		{
			tcpmode = true;
			startup = true;//一开始就要设为活动
			connect = false;
			resetFlowInfo();
			byte[] payload = new byte[8*1024*1024];
	    	DatagramSocket datagramSocket = null;
	    	int remotePort = 0;
	    	int length = 0;
	    	int offset = 0;
	    	int type = 0;
	        try
	        {
		    	File cosIdentity = new File(PathFactory.getDataPath(), "identity");
		    	String cosId = "";
		    	if( cosIdentity.exists() )
		    	{
		    		//读取数字证书并初始化
		    		Key identity = (Key)IOHelper.readSerializable(cosIdentity);
		        	Cipher c = Cipher.getInstance("DES");
		            c.init(Cipher.WRAP_MODE, identity);//再用数字证书构建另外一个DES密码器
		            cosId = Base64.encode(c.wrap(identity));
		    	}
		    	payload[offset++] = Command.CONTROL_GETMONITOR;
		    	payload[offset++] = (byte)cosId.length();
		    	offset = Tools.copyByteArray(cosId.getBytes(), payload, offset);
	        	datagramSocket = new DatagramSocket();
	            datagramSocket.setSoTimeout(15000);
	            InetAddress addr = InetAddress.getByName( ip );
	            DatagramPacket request = new DatagramPacket(payload, 0, offset, addr, port );
	            datagramSocket.send( request );
				DatagramPacket response = new DatagramPacket(payload, payload.length);
				datagramSocket.setSoTimeout(15000);
				status = "等待伺服器返回监听端口";
				datagramSocket.receive(response);
				remotePort = Tools.bytesToInt(payload, 0, 4);
				datagramSocket.close();
				InetSocketAddress endpoint = new InetSocketAddress(InetAddress.getByName(ip), remotePort);
				socket = new Socket();
				status = String.format("发起远程监控连接[%s]", remotePort);
				socket.connect(endpoint, 3000);
	            connect = true;
	            closed = false;
				status = String.format("远程监控连接[%s]已建立", remotePort);
				log.info(this+" setup system monitor connect("+thread.getId()+", remotePort="+remotePort+").");
	            is = socket.getInputStream();
				while( getMonitorRuning )
				{
					//TODO:每隔3秒钟向指定服务器发送控制指令，获取最新的监控数据
					type = is.read();
					/** 这里出现一个关键错误，当对端断开连接的时候这里会返回-1，这时要跳出循环 **/
					if( type == -1 )
					{
						errorCount += 1;
						break;
					}
					long ts = System.currentTimeMillis() - timestamp;
					is.read(payload, 0, 4);
					length = Tools.bytesToInt(payload, 0, 4);
					if( length > payload.length )
					{
						log.warn(this+" unknonw input(type="+type+", length="+length+") from remote "+remotePort);
						errorCount += 1;
						break;//收到很大的数据要跳出
					}
					offset = this.read(is, payload, length);
					if( offset != length )
					{
						log.warn(this+" failed to read monitor-data(type="+type+",offset="+offset+",length="+length+").");
						errorCount += 1;
						break;
					}
					this.binSize1 += length+5;
					this.rcvCount1 += 1;
					if( ts > 3 * Tools.MILLI_OF_MINUTE ) errorCount = 0;
					
					if( ts  > 35000 )
					{
						synchronized (this)
						{
							binSize0 = binSize1;
							rcvCount0 = rcvCount1;
							nhuSize0 = nhuSize1;
							nmSize0 = nmSize1;
							ntuSize0 = ntuSize1;
							nhhuSize0 = nhhuSize1;
							nhuCount0 = nhuCount1;
							nmCount0 = nmCount1;
							ntuCount0 = ntuCount1;
							nhhuCount0 = nhhuCount1;
							timestamp = System.currentTimeMillis();
						}
					}

					if( type == N_TU )
					{
						this.ntuCount1 += 1;
						this.ntuSize1 += length+5;
						String title = new String(payload, 0, length, "UTF-8");
						if( sysDesc != null ) sysDesc.setDescript(title);
						heartbeat = System.currentTimeMillis();
//						System.err.println(String.format("%s:%s %s", ip, port, Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", this.heartbeat)));
					}
					else if( type == N_HU )
					{
						this.nhuCount1 += 1;
						this.nhuSize1 += length+5;
						try
						{
					    	ByteArrayInputStream bais = new ByteArrayInputStream(payload, 0, length);
					    	ObjectInputStream oos = new ObjectInputStream(bais);
					        Object serializable = oos.readObject();
					    	if( serializable instanceof SystemPerf )
					    	{
					    		SystemPerf sysDescOld = this.sysDesc;
					    		this.sysDesc = (SystemPerf)serializable;
								String h2 = this.sysDesc.getPropertyValue("h2");
								if( h2 != null & !h2.isEmpty() ){
									try{
										JSONObject e = new JSONObject(h2);
										String key = this.ip+":"+e.getInt("port");
										H2Databases.put(key, this);
										String standby = e.getString("standby");
										if( !standby.isEmpty() ){
											H2StandbyDatabases.put(standby, this);
										}
									}
									catch(Exception e){
										log.error("Failed to load the database("+h2+") of h2 from "+this.toString());
									}
								}
					    		if( sysDescOld != null )
					    		{
					    			ModuleTrack module = (ModuleTrack)sysDescOld.getProperty("Zookeeper");
					    			if( module != null ) 
					    				this.sysDesc.setProperty("Zookeeper", module);
					    		}
//					    		if( sysDesc.getSecurityKey() != null )
//					    		{
//						    		java.io.File file = new java.io.File(PathFactory.getDataPath(), "monitor/perf/"+sysDesc.getSecurityKey());
//						    		IOHelper.writeSerializable(file, sysDesc);
//					    		}
					    		ArrayList<String> removed = new ArrayList<String>();
					    		Iterator<String> iterator = exceptionModules.keySet().iterator();
					    		while(iterator.hasNext()){
					    			String key = iterator.next();
					    			long val = exceptionModules.get(key);
					    			if( System.currentTimeMillis() - val > Tools.MILLI_OF_DAY ){
					    				removed.add(key);
					    			}
					    		}
					    		for(String key: removed){
					    			exceptionModules.remove(key);
					    		}
					    	}
						}
				        catch(Exception e)
				        {
				        	e.printStackTrace();
				        }
					}
					else if( type == N_MU || type == N_MQ )
					{
						this.nmCount1 += 1;
						this.nmSize1 += length+5;
						try
						{
					    	ByteArrayInputStream bais = new ByteArrayInputStream(payload, 0, length);
					    	ObjectInputStream oos = new ObjectInputStream(bais);
					        Object serializable = oos.readObject();
							synchronized(listModulePerfs)
							{
								if( sysDesc == null ) sysDesc = new SystemPerf();
						    	if( serializable instanceof ModulePerf )
						    	{
						    		ModulePerf modulePerf = (ModulePerf)serializable;
//						    		if( "testdb".equalsIgnoreCase(modulePerf.getId())){
//						    			log.warn(String.format("[%s] The usage of memory is '%s' from %s:%s \r\n%s",
//						    					modulePerf.getId(), modulePerf.getUsageMemory(), ip, port, modulePerf.getPid()));
//						    		}
						    		if( modulePerf.getCountException() > 8 ){
						    			if( !"Zookeeper".equals(modulePerf.getId()) ||
						    				("Zookeeper".equals(modulePerf.getId()) &&
						    				 modulePerf.getState() != Module.STATE_STARTUP) ){
						    				exceptionModules.put(modulePerf.getId(), System.currentTimeMillis());
						    			}
						    		}
						    		else{
						    			if(modulePerf.getDead()!=null&&!modulePerf.getDead().isEmpty()){
							    			exceptionModules.put(modulePerf.getId(), System.currentTimeMillis());
						    			}
						    			else{
						    				exceptionModules.remove(modulePerf.getId());
						    			}
						    		}
						    		if(modulePerf.getPcount()>1 ){
					    				deadModules.put(modulePerf.getId(), modulePerf);
						    		}
						    		else if( (modulePerf.getState() == Module.STATE_SHUTDOWN ||
			    				  		modulePerf.getState() == Module.STATE_STARTUP ||
			    						modulePerf.getState() == Module.STATE_CLOSE) &&
			    						modulePerf.getPcount()==0 && modulePerf.isDaemon() ) {
						    			deadModules.put(modulePerf.getId(), modulePerf);
				    				}
						    		else{
						    			deadModules.remove(modulePerf.getId());
						    		}
						    		if( type == N_MU )
						    		{
						    			int i = 0;
						        		if( "Zookeeper".equals(modulePerf.getId()) )
						        		{
						    	        	ModuleTrack e = new ModuleTrack();
						    	        	e.setDebug(modulePerf.isDebug());
						    	        	e.setDependence(modulePerf.getDependence());
						    	        	e.setEndTime(modulePerf.getEndTime());
						    	        	e.setId(modulePerf.getId());
						    	        	e.setMonitorInfo(modulePerf.getMonitorInfo());
						    	        	e.setName(modulePerf.getName());
						    	        	e.setRemark(modulePerf.getRemark());
						    	        	e.setRuntime(modulePerf.getRuntime());
						    	        	e.setStandby(modulePerf.getStandby());
						    	        	e.setStartTime(modulePerf.getStartTime());
						    	        	e.setStatupTime(modulePerf.getStartupDate());
						    	        	e.setState(modulePerf.getState());
						    	        	e.setUsageMemory(modulePerf.getUsageMemory());
						    	        	e.setVersion(modulePerf.getVersion());
						    	        	e.setProgrammer(modulePerf.getProgrammer());
						        			sysDesc.setProperty("Zookeeper", e);
						        		}
						        		else if( "GateOne".equals(modulePerf.getId()) ){
						        			this.gateone = true;//sysDesc.setProperty("gateone", true);
						        		}
						        		else if( "COSControl".equals(modulePerf.getId()) ){
						        			modulePerf.setType(1);
						        		}
							    		for( i = 0; i < listModulePerfs.size(); i++ )
							    		{
							    			ModulePerf m = listModulePerfs.get(i);
								    		if( m.getId().equals(modulePerf.getId()) )
								    		{
								    			listModulePerfs.set(i, modulePerf);
								    			break;
								    		}
							    		}
							    		if( i == listModulePerfs.size() ){
							    			mapModulePerfs.put(modulePerf.getId(), modulePerf);
							    			listModulePerfs.add(modulePerf);
							    		}
							    		synchronized(mapModule)
							    		{
							    			mapModule.put(ip+":"+modulePerf.getId(), modulePerf);
							    		}
						    		}
						    		else if( type == N_MQ )
						    		{
							    		for( int i = 0; i < listModulePerfs.size(); i++ )
							    		{
							    			ModulePerf m = listModulePerfs.get(i);
								    		if( m.getId().equals(modulePerf.getId()) )
								    		{
								    			mapModulePerfs.remove(modulePerf.getId());
								    			listModulePerfs.remove(i);
								    			i -= 1;
								    			break;
								    		}
							    		}
							    		synchronized(mapModule)
							    		{
							    			mapModule.remove(ip+":"+modulePerf.getId());
							    		}
						    			log.info(this+" The module("+modulePerf.getId()+") cancel quite.");
						    		}
						    	}
							}
						}
				        catch(Exception e)
				        {
				        	log.error("Failed to decode(length="+length+") ModulePerf from "+ip+":"+port+" for "+e);
				        }
					}
					else if( type == N_HHU )
					{
						this.nhhuCount1 += 1;
						this.nhhuSize1 += length+5;
						try
						{
					    	ByteArrayInputStream bais = new ByteArrayInputStream(payload, 0, length);
					    	ObjectInputStream oos = new ObjectInputStream(bais);
					        Object serializable = oos.readObject();
					    	if( serializable instanceof HostPerfHisotry )
					    	{
					    		HostPerfHisotry hostPerfHistory = (HostPerfHisotry)serializable;
//					    		saveHostPerf(this);
					    		java.io.File file = new java.io.File(PathFactory.getDataPath(), "monitor/history/"+Tools.encodeMD5(sysDesc.getSecurityKey()));
					    		IOHelper.writeSerializable(file, hostPerfHistory);
//					    		mapMonitor1.put(sysPerf.getHost(), this);
					    	}
						}
				        catch(Exception e)
				        {
				        }
					}
		        	if( isConnect() && sysDesc != null ) sysDesc.setProperty("Running", null);
					flushClusterServerState();
				}
	        }
	        catch(SocketTimeoutException e)
	        {
				status = String.format("远程监控连接[%s]已断开因为异常%s", remotePort, e.getMessage());
	        }
	        catch(BindException e)
	        {
				status = String.format("远程监控连接[%s]已断开因为异常%s", remotePort, e.getMessage());
	        	log.warn(this+" failed to get monitor from remote port "+remotePort+" for port already in use. Suspend monitor for 15 seconds.");
	        	try
				{
					Thread.sleep(15000);
				}
				catch (InterruptedException e1)
				{
				}
	        }
	        catch(SocketException e)
	        {
				status = String.format("远程监控连接[%s]已断开因为异常%s", remotePort, e.getMessage());
	        	if( !closed )
	        	{
	        		log.warn("["+this+"] Failed to get monitor(type="+type+", length="+length+", offset="+offset+") from "+remotePort+" for "+e.getMessage());
	        	}
	        }
	        catch(Exception e)
	        {
				status = String.format("远程监控连接[%s]已断开因为异常%s", remotePort, e.getMessage());
	        	if( !closed )
	        	{
	        		log.warn("["+this+"] Failed to get monitor(type="+type+", length="+length+", offset="+offset+") from "+remotePort+" for exception", e);
	        	}
	        }
	        finally
	        {
				synchronized( this )
				{
		    		try
					{
		    			if( datagramSocket != null ) datagramSocket.close();
		            	if( socket != null ) socket.close();
		    			if( is != null ) is.close();
					}
					catch (Exception e)
					{
						log.error(this +" faield to close resource", e);
					}
					is = null;
					socket = null;
					startup = false;
					connect = false;
				}
				flushClusterServerState();
	        }
		}
        
        public boolean isGateone() {
			return gateone;
		}

		public abstract void flushClusterServerState();
		
		/**
		 * 读取数据
		 * @param is
		 * @param payload
		 * @param length
		 * @return
		 * @throws Exception
		 */
		public int read(InputStream is, byte[] payload, int length)
		    throws Exception
		{
			int offset = 0;
			while( length > 0 )
			{
				int len = is.read(payload, offset, length);
				offset += len;
				length -= len;
			}
			return offset;
		}

		public SystemPerf getSysDesc()
		{
			return sysDesc;
		}

		public String getIp()
		{
			return ip;
		}

    	public int getPort() {
			return port;
		}
    	
		public ModulePerf getModulePerf(String id)
		{
			return mapModulePerfs.get(id);
		}

		public ArrayList<ModulePerf> getModulePerfs()
		{
			return listModulePerfs;
		}

		public HashMap<String, ModulePerf> getDeadModules() {
			return deadModules;
		}

		public HashMap<String, Long> getExceptionModules() {
			return exceptionModules;
		}

		public void setClusterid(int pid)
		{
			this.pid = pid;
		}

		public int getServerid() 
		{
			return serverid;
		}
	}

	/**
	 * 关闭相关资源
	 */
	public void close()
	{
		log.debug("Close GetMonitorRunner.");
		runGetMonitor.close();
	}
	
	/**
	 * 加载服务器监控信息数据
	 * @param serverMonitorInfos
	 * @throws Exception
	 */
	public void loadServerMonitorInof(ArrayList<ServerMonitor> serverMonitorInfos)
		throws Exception
	{
		long netload0 = 0;//网络负载出（每秒多少M）
		long netload1 = 0;//网络负载入（每秒多少M）
		long cpuload = 0;//处理器负载（占比）
		long memusage = 0;//内存大小
		long memused = 0;//内存使用
		int ioload0 = 0;//磁盘IO负载读（每秒多少M）
		int ioload1 = 0;//磁盘IO负载写（每秒多少M）
		int temperature = 0;//温度
		int i = 0;
		Iterator<RunFetchMonitor> iter = mapMonitor.values().iterator();
		while( iter.hasNext() )
		{
			RunFetchMonitor runner = iter.next();
			ServerMonitor e = new ServerMonitor();
			e.setHost(runner.ip);
			serverMonitorInfos.add(e);
    		
			if( runner.sysDesc == null || runner.sysDesc.getSecurityKey() == null )
			{
				continue;
			}
			java.io.File file = new java.io.File(PathFactory.getDataPath(), "monitor/history/"+Tools.encodeMD5(runner.sysDesc.getSecurityKey()));
			HostPerfHisotry hostPerfHistory = (HostPerfHisotry)IOHelper.readSerializableNoException(file);
			if( hostPerfHistory == null ) continue;
			int size = hostPerfHistory.size();
			e.setCurrent(hostPerfHistory.get(size-1));//设置当前情况
			//估算忙时，用CPU指标作为参照
			long hour7[][] = new long[24][4];//第一位是次数，第二位是值
			//求1日平均
			int tp = (int)((System.currentTimeMillis() - Tools.MILLI_OF_DAY)/1000);
			for( i = 0; i < size; i++ )
			{
				HostPerfPoint p = hostPerfHistory.get( size - i - 1 );
				if( p.getTime() < tp )
				{
					break;
				}
				netload0 += p.getNetload0();
				netload1 += p.getNetload1();
				cpuload += p.getCpuload();
				memusage += p.getMemusage();
				memused += p.getMemused();
				ioload0 += p.getIoload0();
				ioload1 += p.getIoload1();
				temperature += p.getTemperature();
			}
			HostPerfPoint oneday = new HostPerfPoint();
			oneday.setCpuload(cpuload/i);
			oneday.setIoload0(ioload0/i);
			oneday.setIoload1(ioload1/i);
			oneday.setMemusage((int)memusage/i);
			oneday.setMemused(memused/i);
			oneday.setTemperature(temperature/i);
			oneday.setNetload0(netload0/i);
			oneday.setNetload1(netload1/i);
			e.setOneday(oneday);
			//求7日平均
			tp = (int)((System.currentTimeMillis() - 7*Tools.MILLI_OF_DAY)/1000);
			for( ; i < size; i++ )
			{
				HostPerfPoint p = hostPerfHistory.get( size - i - 1 );
				if( p.getTime() < tp )
				{
					break;
				}
				hour7[p.getHour()][0] += 1;
				hour7[p.getHour()][1] += p.getCpuload();
				hour7[p.getHour()][2] += p.getNetload0();
				hour7[p.getHour()][2] += p.getNetload1();
				hour7[p.getHour()][3] += p.getMemusage();
				netload0 += p.getNetload0();
				netload1 += p.getNetload1();
				cpuload += p.getCpuload();
				memusage += p.getMemusage();
				memused += p.getMemused();
				ioload0 += p.getIoload0();
				ioload1 += p.getIoload1();
				temperature += p.getTemperature();
			}
			HostPerfPoint sevenday = new HostPerfPoint();
			sevenday.setCpuload(cpuload/i);
			sevenday.setIoload0(ioload0/i);
			sevenday.setIoload1(ioload1/i);
			sevenday.setMemusage((int)memusage/i);
			sevenday.setMemused(memused/i);
			sevenday.setTemperature(temperature/i);
			sevenday.setNetload0(netload0/i);
			sevenday.setNetload1(netload1/i);
			e.setSevenday(sevenday);
			//计算忙时
			StringBuffer busyhours = new StringBuffer();
			final DecimalFormat df = (DecimalFormat)NumberFormat.getPercentInstance();
			long am0 = -1, pm0 = -1, am1 = -1, pm1 = -1, am2 = -1, pm2 = -1, cpuload0 = 0, memusage0 = 0;
			for( i = 0; i < 12; i++ )
			{
				if( hour7[i][0] == 0 ) continue;
				cpuload = hour7[i][1]/hour7[i][0];
				if( cpuload == 0 ) continue;
				am0 = cpuload>cpuload0&&cpuload>3000?i:am0;
				cpuload0 = cpuload>cpuload0?cpuload:cpuload0;
			}
			if( am0 >= 0 ) busyhours.append(",AM"+am0+":00(CPU"+df.format((double)cpuload0/10000)+")");
			for( cpuload0 = 0; i < 24; i++ )
			{
				if( hour7[i][0] == 0 ) continue;
				cpuload = hour7[i][1]/hour7[i][0];
				if( cpuload == 0 ) continue;
				pm0 = cpuload>cpuload0&&cpuload>3000?i:pm0;//超过50%的才定义为忙时
				cpuload0 = cpuload>cpuload0?cpuload:cpuload0;
			}
			if( pm0 >= 0 ) busyhours.append(",PM"+pm0+":00(CPU"+df.format((double)cpuload0/10000)+")");
			
			for( i = 0; i < 12; i++ )
			{
				if( hour7[i][0] == 0 ) continue;
				memusage = hour7[i][3]/hour7[i][0];
				if( memusage == 0 ) continue;
				am2 = memusage>memusage0&&memusage>0?i:am2;
				memusage0 = memusage>memusage0?memusage:memusage0;
			}
			if( am2 >= 0 ) busyhours.append(",AM"+am2+":00(MEM"+df.format((double)memusage0/10000)+")");
			for( memusage0 = 0; i < 24; i++ )
			{
				if( hour7[i][0] == 0 ) continue;
				memusage = hour7[i][3]/hour7[i][0];
				if( memusage == 0 ) continue;
				pm2 = memusage>memusage0&&memusage>0?i:pm2;//超过50%的才定义为忙时
				memusage0 = memusage>memusage0?memusage:memusage0;
			}
			if( pm2 >= 0 ) busyhours.append(",PM"+pm2+":00(MEM"+df.format((double)memusage0/10000)+")");
			
			final long BUSY_NET_LOAD = 30*1024*1024;
			for( i = 0, netload1 = 0; i < 12; i++ )
			{
				if( hour7[i][0] == 0 ) continue;
				netload0 = hour7[i][2]/hour7[i][0];
				if( netload0 == 0 ) continue;
				am1 = netload0>netload1&&netload0>BUSY_NET_LOAD?i:am1;//超10M/s的才定义为忙时
				netload1 = netload0>netload1?netload0:netload1;
			}
			if( am1 >= 0 ) busyhours.append(",AM"+am1+":00(NET"+Kit.bytesScale(netload1)+"/s)");
			for( netload1 = 0; i < 24; i++ )
			{
				if( hour7[i][0] == 0 ) continue;
				netload0 = hour7[i][2]/hour7[i][0];
				if( netload0 == 0 ) continue;
				pm1 = netload0>netload1&&netload0>BUSY_NET_LOAD?i:pm1;
				netload1 = netload0>netload1?netload0:netload1;
			}
			if( pm1 >= 0 ) busyhours.append(",PM"+pm1+":00(NET"+Kit.bytesScale(netload1)+"/s)");
			if(busyhours.length()==0 ) busyhours.append("平均CPU低于30%,NET小于30M/s");
			else busyhours.deleteCharAt(0);
			e.setBusyhours(busyhours.toString());
		}
	}
	
	/**
	 * 装置服务器监控图表数据
	 * @param serverMonitorInfos
	 * @throws Exception
	 */
	public void loadServerMonitorChartData(ArrayList<ChartDataset> serverMonitorChartData, int chartType)
		throws Exception
	{
		Iterator<RunFetchMonitor> iter = mapMonitor.values().iterator();
		while( iter.hasNext() )
		{
			RunFetchMonitor runner = iter.next();
			serverMonitorChartData.add(this.loadServerMonitorChartData(runner, chartType));
		}
	}
	
	/**
	 * 加载指定服务器所有负载数据集
	 * @param host
	 * @param port
	 * @return
	public void loadServerMonitorChartData(ArrayList<ChartDataset> serverMonitorChartData, String host, int port)
		throws Exception
	{
			RunFetchMonitor runner = this.mapMonitor.get(host+":"+port);
			if( runner == null ) return;
			serverMonitorChartData.add(this.loadServerMonitorChartData(runner, 0));
			serverMonitorChartData.add(this.loadServerMonitorChartData(runner, 1));
			serverMonitorChartData.add(this.loadServerMonitorChartData(runner, 2));
			serverMonitorChartData.add(this.loadServerMonitorChartData(runner, 3));
	}
	 */

	/**
	 * 装置服务器监控图表数据
	 * @param serverMonitorInfos
	 * @throws Exception
	 */
	public ChartDataset loadServerMonitorChartData(RunFetchMonitor runner, int chartType)
		throws Exception
	{
		ChartDataset e = new ChartDataset();
		int i = 0;
		e.setId(runner.ip+"_"+chartType);
		switch(chartType)
		{
		case 0:
			e.setTitle(runner.ip+"CPU负载率");
			e.setYtitle("CPU负载率(%)");
			e.setUnit("%");
			break;
		case 1:
			e.setTitle(runner.ip+"内存负载率");
			e.setYtitle("内存负载率(%)");
			e.setUnit("%");
			break;
		case 2:
			e.setTitle(runner.ip+"网络负载");
			e.setYtitle("网络负载(字节/秒)");
			e.setUnit("bytes/s");
			break;
		case 3:
			e.setTitle(runner.ip+"磁盘负载");
			e.setYtitle("磁盘负载(字节/秒)");
			e.setUnit("bytes/s");
			break;
		case 4:
			e.setTitle(runner.ip+"温度");
			e.setYtitle("温度");
			e.setUnit("℃");
			break;
		}

		if( runner.sysDesc == null || runner.sysDesc.getSecurityKey() == null )
		{
			return e;
		}
		java.io.File file = new java.io.File(PathFactory.getDataPath(), "monitor/history/"+Tools.encodeMD5(runner.sysDesc.getSecurityKey()));
		HostPerfHisotry hostPerfHistory = (HostPerfHisotry)IOHelper.readSerializableNoException(file);
		if( hostPerfHistory == null ) return e;
//	    	System.out.println(runner.history.size());
		int size = hostPerfHistory.size();
		//估算忙时，用CPU指标作为参照
		long data0[][][] = new long[24][60][4];//第一位是次数，第二位是值
		long data7[][][] = new long[24][60][4];//第一位是次数，第二位是值
		long[] load0 = new long[4], load1 = new long[4];
		int endHour = -1;//最后一个时间刻度
		int endMinute = -1;
		//求1日平均
		int tp = (int)((System.currentTimeMillis() - Tools.MILLI_OF_DAY)/1000);
		ModulePerf cosControl = runner.getModulePerf("COSControl");
		for( i = 0; i < size; i++ )
		{
			HostPerfPoint p = hostPerfHistory.get( size - i - 1 );
			if( p.getTime() < tp )
			{
				//取当日数据
				break;
			}
			endHour = endHour==-1?p.getHour():endHour;
			endMinute = endMinute==-1?p.getMinute():endMinute;
			data0[p.getHour()][p.getMinute()][0] += 1;
			data0[p.getHour()][p.getMinute()][3] = p.getTime();
			switch(chartType)
			{
			case 0:
				data0[p.getHour()][p.getMinute()][1] += p.getCpuload();
				break;
			case 1:
				data0[p.getHour()][p.getMinute()][1] += p.getMemusage();
				break;
			case 2:
				data0[p.getHour()][p.getMinute()][1] += p.getNetload0();
				data0[p.getHour()][p.getMinute()][2] += p.getNetload1();
				break;
			case 3:
				data0[p.getHour()][p.getMinute()][1] += p.getIoload0();
				data0[p.getHour()][p.getMinute()][2] += p.getIoload1();
				break;
			}
		}
		tp -= 7*Tools.SECOND_OF_DAY;
		for( ; i < size; i++ )
		{
			HostPerfPoint p = hostPerfHistory.get( size - i - 1 );
			if( p.getTime() < tp )
			{
				break;
			}
			data7[p.getHour()][p.getMinute()][0] += 1;
			data7[p.getHour()][p.getMinute()][3] = p.getTime();
			switch(chartType)
			{
			case 0:
				data7[p.getHour()][p.getMinute()][1] += p.getCpuload();
				break;
			case 1:
				data7[p.getHour()][p.getMinute()][1] += p.getMemusage();
				break;
			case 2:
				data7[p.getHour()][p.getMinute()][1] += p.getNetload0();
				data7[p.getHour()][p.getMinute()][2] += p.getNetload1();
				break;
			case 3:
				data7[p.getHour()][p.getMinute()][1] += p.getIoload0();
				data7[p.getHour()][p.getMinute()][2] += p.getIoload1();
				break;
			}
		}
		
		JSONArray annotations = new JSONArray();
		String startupTime = null;
		if( cosControl != null ){
			startupTime = Tools.getFormatTime("yyyy-MM-dd HH:mm", cosControl.getStatupTime().getTime());
		}
		//时间序列
		JSONArray timeSeries = new JSONArray();
		for( i = 0; i < 24; i++ )
		{
			for( int j = 0; j < 60; j++ )
			{
				if( (i == endHour && j > endMinute) || i > endHour ){
					this.setTime(i, j, data0, timeSeries, startupTime, annotations);
					if(timeSeries.length()==1){
						timeSeries.put(0,  "昨天");
					}
				}
			}
		}
		for( i = 0; i <= endHour; i++ )
		{
			for( int j = 0; j < 60; j++ )
			{
				if( i == endHour && j > endMinute ){
					break;//结束那一分钟
				}
				this.setTime(i, j, data0, timeSeries, startupTime, annotations);
				if(i == 0 && j == 0 ){
					timeSeries.put(timeSeries.length()-1,  "今天");
				}
			}
		}
		
		e.setTimeSeries(timeSeries.toString());
		StringBuffer dataSeries = new StringBuffer();
		JSONArray dataSeries0 = new JSONArray();
		JSONArray dataSeries1 = new JSONArray();
		this.setDataSeries(chartType, endHour, endMinute, data0, dataSeries0, dataSeries1, load0, load1);
//		System.out.println("The end hour is "+endHour+":"+endMinute);
//		for( i = 0; i < 8; i++){
//			System.out.print(timeSeries.getString(i));
//			System.out.print(" ");
//		}
//		System.out.println();
//		System.out.println("...");
//		for( i = timeSeries.length()-8; i < timeSeries.length(); i++){
//			System.out.print(timeSeries.getString(i));
//			System.out.print(" ");
//		}
//		System.out.println();
//		System.out.println("timeSeries="+timeSeries.length());
//		System.out.println("dataSeries0="+dataSeries0.length());
//		System.out.println("dataSeries1="+dataSeries1.length());
		switch(chartType)
		{
		case 0:
		case 1:
			dataSeries.append("{name:'当日',");
//			dataSeries.append("lineColor: Highcharts.getOptions().colors[1],");
			dataSeries.append("lineColor: '#fa8072',");
			dataSeries.append("color: '#fa8072',");
			dataSeries.append("fillOpacity: 0.5,");
			dataSeries.append("marker: {enabled: false},");
			dataSeries.append("threshold: null,");
			dataSeries.append("data:");
			dataSeries.append(dataSeries0.toString());
			dataSeries.append("}");
			break;
		case 2:
		case 3:
			dataSeries.append("{name:'当日输入',");
//			dataSeries.append("lineColor: Highcharts.getOptions().colors[1],");
			dataSeries.append("color: '#fa8072',");
			dataSeries.append("fillOpacity: 0.5,");
			dataSeries.append("marker: {enabled: false},");
			dataSeries.append("threshold: null,");
			dataSeries.append("data:");
			dataSeries.append(dataSeries0.toString());
			dataSeries.append("},{name:'当日输出',");
//			dataSeries.append("lineColor: Highcharts.getOptions().colors[1],");
			dataSeries.append("color: '#48d1cc',");
			dataSeries.append("fillOpacity: 0.5,");
			dataSeries.append("marker: {enabled: false},");
			dataSeries.append("threshold: null,");
			dataSeries.append("data:");
			dataSeries.append(dataSeries1.toString());
			dataSeries.append("}");
			break;
		}
		dataSeries0 = new JSONArray();
		dataSeries1 = new JSONArray();
		this.setDataSeries(chartType, endHour, endMinute, data7, dataSeries0, dataSeries1, null, null);
		switch(chartType)
		{
		case 0:
		case 1:
			dataSeries.append(",{name:'7日均值',");
//			dataSeries.append("lineColor: Highcharts.getOptions().colors[1],");
			dataSeries.append("color: '#ffd700',");
			dataSeries.append("fillOpacity: 0.5,");
			dataSeries.append("marker: {enabled: false},");
			dataSeries.append("threshold: null,");
			dataSeries.append("data:");
			dataSeries.append(dataSeries0.toString());
			dataSeries.append("}");
			break;
		case 2:
		case 3:
			dataSeries.append(",{name:'7日输入均值',");
//			dataSeries.append("lineColor: Highcharts.getOptions().colors[1],");
			dataSeries.append("color: '#ffd700',");
			dataSeries.append("fillOpacity: 0.5,");
			dataSeries.append("marker: {enabled: false},");
			dataSeries.append("threshold: null,");
			dataSeries.append("data:");
			dataSeries.append(dataSeries0.toString());
			dataSeries.append("},{name:'7日输出均值',");
//			dataSeries.append("lineColor: Highcharts.getOptions().colors[1],");
			dataSeries.append("color: '#8470ff',");
			dataSeries.append("fillOpacity: 0.5,");
			dataSeries.append("marker: {enabled: false},");
			dataSeries.append("threshold: null,");
			dataSeries.append("data:");
			dataSeries.append(dataSeries1.toString());
			dataSeries.append("}");
			break;
		}
		this.setTips(chartType, timeSeries, load0, load1, annotations);
		e.setTips(annotations.toString());
//		System.err.println(annotations.getJSONObject(1).getJSONArray("labels").toString(4));
		e.setDataSeries(dataSeries.toString());
		return e;
	}
	
	/**
	 * 设置图表上的备注
	 * @param chartType
	 * @param timeSeries
	 * @param load0
	 * @param load1
	 * @param annotations
	 */
	private void setTips(int chartType, JSONArray timeSeries, long load0[], long load1[], JSONArray annotations){
		if( load0[0] > 0 ){
	    	JSONObject annotation = new JSONObject();
	    	JSONObject labelOptions = new JSONObject();
	    	labelOptions.put("shape", "connector");
	    	labelOptions.put("align", "right");
	    	labelOptions.put("justify", "false");
	    	labelOptions.put("crop", "true");
	    	JSONObject style = new JSONObject();
	    	labelOptions.put("style", style);
	    	style.put("fontSize", "0.98em");
	    	style.put("textOutline", "1px white");
	    	annotation.put("labelOptions", labelOptions);
	    	
	    	JSONArray labels = new JSONArray();
			JSONObject label = new JSONObject();
			if( chartType == 2 ){
				label.put("text", "在["+load0[1]+":"+load0[2]+"]网络输入流量<br/>达到当日峰值"+Kit.bytesScale(load0[0]));
			}
			else{
				label.put("text", "在["+load0[1]+":"+load0[2]+"]磁盘写入<br/>达到当日峰值"+Kit.bytesScale(load0[0]));
			}
			JSONObject point = new JSONObject();
			label.put("point", point);
			point.put("xAxis", 0);
			point.put("yAxis", 0);
			point.put("x", load0[3]);
			point.put("y", load0[0]);
			point.put("time", timeSeries.getString((int)load0[3]));
			labels.put(label);
    		annotation.put("labels", labels);
//System.out.println(netLoad1[0]+", "+netLoad1[1]+":"+netLoad1[2]+", "+netLoad1[3]);
    		if( load1[0] > 0 ){
    			label = new JSONObject();
    			if( chartType == 2 ){
    				label.put("text", "在"+load1[1]+":"+load1[2]+"网络输出流量<br/>达到当日峰值"+Kit.bytesScale(load1[0]));
    			}
    			else{
        			label.put("text", "在"+load1[1]+":"+load1[2]+"磁盘读取<br/>达到当日峰值"+Kit.bytesScale(load1[0]));
    			}
    			point = new JSONObject();
    			label.put("point", point);
    			point.put("xAxis", 0);
    			point.put("yAxis", 0);
    			point.put("x", load1[3]);
    			point.put("y", load1[0]);
    			point.put("time", timeSeries.getString((int)load1[3]));
    			labels.put(label);
        		annotation.put("labels", labels);
    		}
    		annotations.put(annotation);
		}
	}
	
	/**
	 * 设置时间
	 * @param i
	 * @param j
	 * @param data0
	 * @param timeSeries
	 * @param startupTime
	 * @param annotations
	 */
	private void setTime(int i, int j, long[][][] data0, JSONArray timeSeries, String startupTime, JSONArray annotations){
		String value = (i>9?i:("0"+i))+":"+(j>9?j:("0"+j));
		timeSeries.put(value);
		if( data0[i][j][3] > 0 ){
			String pointTime = Tools.getFormatTime("yyyy-MM-dd HH:mm", data0[i][j][3]*1000);
			if( pointTime.equals(startupTime) ){
		    	JSONObject annotation = new JSONObject();
		    	JSONArray labels = new JSONArray();
				JSONObject label = new JSONObject();
				label.put("text", "主控引擎在"+pointTime+"启动");
				JSONObject point = new JSONObject();
				label.put("point", point);
				point.put("xAxis", 0);
				point.put("yAxis", 0);
				point.put("x", timeSeries.length());
				point.put("y", 0);
				point.put("time", timeSeries.get(timeSeries.length()-1));
				labels.put(label);
	    		annotation.put("labels", labels);
	    		annotations.put(annotation);
			}
		}
	}
	
	/**
	 * 设置数据
	 * @param chartType
	 * @param endHour
	 * @param endMinute
	 * @param data0
	 * @param dataSeries0
	 * @param dataSeries1
	 */
	private void setDataSeries(
			int chartType, 
			int endHour,
			int endMinute,
			long[][][] data0,
			JSONArray dataSeries0,
			JSONArray dataSeries1,
			long[] load0,
			long[] load1){
		//当日情况
		for( int i = 0; i < 24; i++ )
		{
			for( int j = 0; j < 60; j++ )
			{
				if( (i == endHour && j > endMinute) || i > endHour ){
					this.setData(chartType, i, j, data0, dataSeries0, dataSeries1, load0, load1);
				}					
			}
		}
		for( int i = 0; i <= endHour; i++ )
		{
			for( int j = 0; j < 60; j++ )
			{
				if( i == endHour && j > endMinute ){
					break;//结束那一分钟
				}
				this.setData(chartType, i, j, data0, dataSeries0, dataSeries1, load0, load1);
			}
		}
	}
	
	/**
	 * 设置数据
	 * @param chartType
	 * @param i
	 * @param j
	 * @param data0
	 * @param dataSeries0
	 * @param dataSeries1
	 * @param load0
	 * @param load1
	 */
	private void setData(int chartType, int i, int j, long data0[][][], JSONArray dataSeries0, JSONArray dataSeries1, long load0[], long load1[]){

		long h = data0[i][j][0]>0?(data0[i][j][1]/data0[i][j][0]):0;
		switch(chartType)
		{
		case 0:
		case 1:
			dataSeries0.put(((double)h)/100);
			break;
		case 2:
		case 3:
			if( load0 != null && h > load0[0] ){
				load0[0] = h;
				load0[1] = i;
				load0[2] = j;
				load0[3] = dataSeries0.length();
			}
			dataSeries0.put(h);
			h = data0[i][j][0]>0?(data0[i][j][2]/data0[i][j][0]):0;
			if( load1 != null && h > load1[0] ){
				load1[0] = h;
				load1[1] = i;
				load1[2] = j;
				load1[3] = dataSeries1.length();
			}
			dataSeries1.put(h);
			break;
		}
	}
	/**
	 * 
	 */
	public int resetMonitorFlowInof()
	{
		Iterator<RunFetchMonitor> iter = mapMonitor.values().iterator();
		while( iter.hasNext() )
		{
			RunFetchMonitor runner = iter.next();
			runner.resetFlowInfo();
		}
		return mapMonitor.size();
	}

	/**
	 * 加载监控流量信息
	 * @param data
	 */
	HashMap<String, JSONObject> memoryFlowInfo = new HashMap<String, JSONObject>();
	public void loadMonitorFlowInfo(JSONArray data)
	{
		JSONObject summary = new JSONObject();
		long totalsize = 0, totalcount = 0, currsize = 0, currcount = 0, errorCount = 0;
		long info03 = 0, info13 = 0, info23 = 0, info33 = 0, info00 = 0, info10 = 0, info20 = 0, info30 = 0;
		long avgload = 0, load = 0;
		Iterator<RunFetchMonitor> iter = mapMonitor.values().iterator();
		while( iter.hasNext() )
		{
			RunFetchMonitor m = iter.next();
			JSONObject e = memoryFlowInfo.get(m.getKey());
			if( e == null )
			{
				e = new JSONObject();
				memoryFlowInfo.put(m.getKey(), e);
			}
			m.loadFlowInfo(e);
			if( !e.has("name") ) continue;
			if( e.has("errorcount") )
			{
				if( e.getInt("errorcount") > 16 ) e.put("pq_cellcls", new JSONObject().put("errorcount", "red"));
				else if( e.getInt("errorcount") > 8 ) e.put("pq_cellcls", new JSONObject().put("errorcount", "orange"));
				else if( e.getInt("errorcount") > 8 ) e.put("pq_cellcls", new JSONObject().put("errorcount", "yellow"));
				else e.put("pq_cellcls", new JSONObject().put("errorcount", "gray"));
			}
			totalsize += e.getLong("totalsize");
			totalcount += e.has("totalcount")?e.getInt("totalcount"):0;
			currsize += e.has("currsize")?e.getLong("currsize"):0;
			currcount += e.has("currcount")?e.getInt("currcount"):0;
			errorCount += e.has("errorCount")?e.getInt("errorCount"):0;
			info03 += e.has("info03")?e.getLong("info03"):0;
			info13 += e.has("info13")?e.getLong("info13"):0;
			info23 += e.has("info23")?e.getLong("info23"):0;
			info33 += e.has("info33")?e.getLong("info33"):0;
			avgload += e.has("avgload_")?e.getLong("avgload_"):0;
			load += e.has("load_")?e.getLong("load_"):0;
			info00 += e.has("info00")?e.getLong("info00"):0;
			info10 += e.has("info10")?e.getLong("info10"):0;
			info20 += e.has("info20")?e.getLong("info20"):0;
			info30 += e.has("info30")?e.getLong("info30"):0;
			
			JSONObject r = new JSONObject(e.toString());
			if(e.has("info03")) r.put("info03", Kit.bytesScale(e.getLong("info03")));
			if(e.has("info13")) r.put("info13", Kit.bytesScale(e.getLong("info13")));
			if(e.has("info23")) r.put("info23", Kit.bytesScale(e.getLong("info23")));
			if(e.has("info33")) r.put("info33", Kit.bytesScale(e.getLong("info33")));
			if(e.has("totalsize")) r.put("totalsize", Kit.bytesScale(e.getLong("totalsize")));
			if(e.has("currsize")) r.put("currsize", Kit.bytesScale(e.getLong("currsize")));
			data.put(r);
		}
		//JSONArray array = new JSONArray();
		summary.put("host", "");
		//array.put(String.valueOf("----"));
		summary.put("version", "");
		//array.put(String.valueOf("----"));
		summary.put("startuptime", "合计/平均");
		//array.put(String.valueOf("合计/平均"));
		summary.put("totalsize", Kit.bytesScale(totalsize));
		//array.put(String.valueOf(totalsize));
		summary.put("totalcount", totalcount);
		//array.put(String.valueOf(totalcount));
		summary.put("duration", "");
		//array.put(String.valueOf("----"));
		summary.put("avgload", Kit.bytesScale(avgload)+"/秒");
		//array.put(String.valueOf(Kit.bytesScale(avgload)+"/秒"));
		summary.put("currsize", Kit.bytesScale(currsize));
		//array.put(String.valueOf(currsize));
		summary.put("currcount", currcount);
		//array.put(String.valueOf(currcount));
		summary.put("period", "");
		//array.put(String.valueOf("----"));
		summary.put("load", Kit.bytesScale(load)+"/秒");
		//array.put(String.valueOf(Kit.bytesScale(load)+"/秒"));
		summary.put("hearttime", "");
		//array.put(String.valueOf("----"));
		summary.put("info01", info00);
		//array.put(String.valueOf(info00));
		summary.put("info02", "");
		//array.put(String.valueOf("----"));
		summary.put("info03", Kit.bytesScale(info03));
		//array.put(String.valueOf(info03));
		summary.put("info04", Kit.showPercent(info03*10000/totalsize));
		//array.put(String.valueOf("----"));
		summary.put("info11", info10);
		//array.put(String.valueOf(info10));
		summary.put("info12", "");
		//array.put(String.valueOf("----"));
		summary.put("info13", Kit.bytesScale(info13));
		//array.put(String.valueOf(info13));
		summary.put("info14", Kit.showPercent(info13*10000/totalsize));
		//array.put(String.valueOf("----"));
		summary.put("info21", info20);
		//array.put(String.valueOf(info20));
		summary.put("info22", "");
		//array.put(String.valueOf("----"));
		summary.put("info23", Kit.bytesScale(info23));
		//array.put(String.valueOf(info13));
		summary.put("info24", Kit.showPercent(info23*10000/totalsize));
		//array.put(String.valueOf("----"));
		summary.put("info31", info30);
		//array.put(String.valueOf(info30));
		summary.put("info32", "");
		//array.put(String.valueOf("----"));
		summary.put("info33", Kit.bytesScale(info33));
		//array.put(String.valueOf(info33));
		summary.put("info34", Kit.showPercent(info33*10000/totalsize));
		//array.put(String.valueOf("----"));
		summary.put("name", "");
		//array.put(String.valueOf("----"));
		summary.put("errorcount", errorCount);
		//array.put(String.valueOf(errorCount));
		data.put(summary);
//		return array;
	}

	/**
	 * 加载服务器监控信息数据
	 * @param serverMonitorInfos
	 * @throws Exception
	 */
	public void loadClusterLoadInof(JSONArray data, HashMap<Integer, JSONObject> filter, JSONObject role, boolean sysadmin)
		throws Exception
	{
		long netload0 = 0;//网络负载出（每秒多少M）
		long netload1 = 0;//网络负载入（每秒多少M）
		long cpuload = 0;//处理器负载（占比）
		long memusage = 0;//内存大小
		long memused = 0;//内存使用
		int ioload0 = 0;//磁盘IO负载读（每秒多少M）
		int ioload1 = 0;//磁盘IO负载写（每秒多少M）
		int temperature = 0;//温度
		int i = 0;
		Iterator<RunFetchMonitor> iter = mapMonitor.values().iterator();
		while( iter.hasNext() )
		{
			RunFetchMonitor runner = iter.next();
			JSONObject o = new JSONObject();
			o.put("host", runner.ip);
			if( runner.sysDesc == null || runner.sysDesc.getSecurityKey() == null )
			{
				continue;
			}
			java.io.File file = new java.io.File(PathFactory.getDataPath(), "monitor/history/"+Tools.encodeMD5(runner.sysDesc.getSecurityKey()));
			HostPerfHisotry hostPerfHistory = (HostPerfHisotry)IOHelper.readSerializableNoException(file);
			if( hostPerfHistory == null ) continue;
			if( !filter.containsKey(runner.pid) ) continue;
			String id = "manager.cluster."+runner.getServerid();
			if( role == null || !role.has(id) )
			{
				if( !sysadmin ) continue;
			}
			data.put(o);
			int size = hostPerfHistory.size();
			ServerMonitor e = new ServerMonitor();
			e.setCurrent(hostPerfHistory.get(size-1));//设置当前情况
			//估算忙时，用CPU指标作为参照
			long hour7[][] = new long[24][4];//第一位是次数，第二位是值
			//求1日平均
			int tp = (int)((System.currentTimeMillis() - Tools.MILLI_OF_DAY)/1000);
			for( i = 0; i < size; i++ )
			{
				HostPerfPoint p = hostPerfHistory.get( size - i - 1 );
				if( p.getTime() < tp )
				{
					break;
				}
				netload0 += p.getNetload0();
				netload1 += p.getNetload1();
				cpuload += p.getCpuload();
				memusage += p.getMemusage();
				memused += p.getMemused();
				ioload0 += p.getIoload0();
				ioload1 += p.getIoload1();
				temperature += p.getTemperature();
			}
			HostPerfPoint oneday = new HostPerfPoint();
			oneday.setCpuload(cpuload/i);
			oneday.setIoload0(ioload0/i);
			oneday.setIoload1(ioload1/i);
			oneday.setMemusage((int)memusage/i);
			oneday.setMemused(memused/i);
			oneday.setTemperature(temperature/i);
			oneday.setNetload0(netload0/i);
			oneday.setNetload1(netload1/i);
			e.setOneday(oneday);
			//求7日平均
			tp = (int)((System.currentTimeMillis() - 7*Tools.MILLI_OF_DAY)/1000);
			for( ; i < size; i++ )
			{
				HostPerfPoint p = hostPerfHistory.get( size - i - 1 );
				if( p.getTime() < tp )
				{
					break;
				}
				hour7[p.getHour()][0] += 1;
				hour7[p.getHour()][1] += p.getCpuload();
				hour7[p.getHour()][2] += p.getNetload0();
				hour7[p.getHour()][2] += p.getNetload1();
				hour7[p.getHour()][3] += p.getMemusage();
				netload0 += p.getNetload0();
				netload1 += p.getNetload1();
				cpuload += p.getCpuload();
				memusage += p.getMemusage();
				memused += p.getMemused();
				ioload0 += p.getIoload0();
				ioload1 += p.getIoload1();
				temperature += p.getTemperature();
			}
			HostPerfPoint sevenday = new HostPerfPoint();
			sevenday.setCpuload(cpuload/i);
			sevenday.setIoload0(ioload0/i);
			sevenday.setIoload1(ioload1/i);
			sevenday.setMemusage((int)memusage/i);
			sevenday.setMemused(memused/i);
			sevenday.setTemperature(temperature/i);
			sevenday.setNetload0(netload0/i);
			sevenday.setNetload1(netload1/i);
			e.setSevenday(sevenday);
			//计算忙时
			StringBuffer busyhours = new StringBuffer();
			final DecimalFormat df = (DecimalFormat)NumberFormat.getPercentInstance();
			long am0 = -1, pm0 = -1, am1 = -1, pm1 = -1, am2 = -1, pm2 = -1, cpuload0 = 0, memusage0 = 0;
			for( i = 0; i < 12; i++ )
			{
				if( hour7[i][0] == 0 ) continue;
				cpuload = hour7[i][1]/hour7[i][0];
				if( cpuload == 0 ) continue;
				am0 = cpuload>cpuload0&&cpuload>3000?i:am0;
				cpuload0 = cpuload>cpuload0?cpuload:cpuload0;
			}
			if( am0 >= 0 ) busyhours.append(",AM"+am0+":00(CPU"+df.format((double)cpuload0/10000)+")");
			for( cpuload0 = 0; i < 24; i++ )
			{
				if( hour7[i][0] == 0 ) continue;
				cpuload = hour7[i][1]/hour7[i][0];
				if( cpuload == 0 ) continue;
				pm0 = cpuload>cpuload0&&cpuload>3000?i:pm0;//超过50%的才定义为忙时
				cpuload0 = cpuload>cpuload0?cpuload:cpuload0;
			}
			if( pm0 >= 0 ) busyhours.append(",PM"+pm0+":00(CPU"+df.format((double)cpuload0/10000)+")");
			
			for( i = 0; i < 12; i++ )
			{
				if( hour7[i][0] == 0 ) continue;
				memusage = hour7[i][3]/hour7[i][0];
				if( memusage == 0 ) continue;
				am2 = memusage>memusage0&&memusage>0?i:am2;
				memusage0 = memusage>memusage0?memusage:memusage0;
			}
			if( am2 >= 0 ) busyhours.append(",AM"+am2+":00(MEM"+df.format((double)memusage0/10000)+")");
			for( memusage0 = 0; i < 24; i++ )
			{
				if( hour7[i][0] == 0 ) continue;
				memusage = hour7[i][3]/hour7[i][0];
				if( memusage == 0 ) continue;
				pm2 = memusage>memusage0&&memusage>0?i:pm2;//超过50%的才定义为忙时
				memusage0 = memusage>memusage0?memusage:memusage0;
			}
			if( pm2 >= 0 ) busyhours.append(",PM"+pm2+":00(MEM"+df.format((double)memusage0/10000)+")");
			
			final long BUSY_NET_LOAD = 30*1024*1024;
			for( i = 0, netload1 = 0; i < 12; i++ )
			{
				if( hour7[i][0] == 0 ) continue;
				netload0 = hour7[i][2]/hour7[i][0];
				if( netload0 == 0 ) continue;
				am1 = netload0>netload1&&netload0>BUSY_NET_LOAD?i:am1;//超10M/s的才定义为忙时
				netload1 = netload0>netload1?netload0:netload1;
			}
			if( am1 >= 0 ) busyhours.append(",AM"+am1+":00(NET"+Kit.bytesScale(netload1)+"/s)");
			for( netload1 = 0; i < 24; i++ )
			{
				if( hour7[i][0] == 0 ) continue;
				netload0 = hour7[i][2]/hour7[i][0];
				if( netload0 == 0 ) continue;
				pm1 = netload0>netload1&&netload0>BUSY_NET_LOAD?i:pm1;
				netload1 = netload0>netload1?netload0:netload1;
			}
			if( pm1 >= 0 ) busyhours.append(",PM"+pm1+":00(NET"+Kit.bytesScale(netload1)+"/s)");
			if(busyhours.length()==0 ) busyhours.append("平均CPU低于30%,NET小于30M/s");
			else busyhours.deleteCharAt(0);
			e.setBusyhours(busyhours.toString());
			/*
		<td><table width='100%' cellpadding=0 cellspacing=0><tr>
			<td class='skit_table_cell' width='60'><ww:property value=""/></td>
			<td class='skit_table_cell' width='120'><ww:property value=""/></td>
			<td class='skit_table_cell' width='40'><ww:property value=""/></td></tr>
			</table>	  		
  		</td>
		<td class='skit_table_cell' width='1000'><ww:property value=""/></td>
		*/
			o.put("currentCpuload", e.getCurrentCpuload());
			o.put("currentMemload", e.getCurrentMemload());
			o.put("currentNetloadI", e.getCurrentNetloadI());
			o.put("currentNetloadO", e.getCurrentNetloadO());
			o.put("currentIOloadI", e.getCurrentIOloadI());
			o.put("currentIOloadO", e.getCurrentIOloadO());
			o.put("currentTemperature", e.getCurrentTemperature());

			o.put("onedayCpuload", e.getOnedayCpuload());
			o.put("onedayMemload", e.getOnedayMemload());
			o.put("onedayNetloadI", e.getOnedayNetloadI());
			o.put("onedayNetloadO", e.getOnedayNetloadO());
			o.put("onedayIOloadI", e.getOnedayIOloadI());
			o.put("onedayIOloadO", e.getOnedayIOloadO());
			o.put("onedayTemperature", e.getOnedayTemperature());
			

			o.put("sevendayCpuload", e.getSevendayCpuload());
			o.put("sevendayMemload", e.getSevendayMemload());
			o.put("sevendayNetloadI", e.getSevendayNetloadI());
			o.put("sevendayNetloadO", e.getSevendayNetloadO());
			o.put("sevendayIOloadI", e.getSevendayIOloadI());
			o.put("sevendayIOloadO", e.getSevendayIOloadO());
			o.put("sevendayTemperature", e.getSevendayTemperature());
			o.put("busyhours", e.getBusyhours());
			
		}
	}
	
	/**
	 * 得到指定模块的程序员
	 * @param dn
	 * @param id
	 * @return
	 */
	public ModulePerf getProgramer(String host, String id)
	{
		synchronized(mapModule)
		{
			return mapModule.get(host+":"+id);
		}
	}
}
