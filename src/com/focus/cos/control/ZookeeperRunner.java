package com.focus.cos.control;

import java.io.FileInputStream;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.data.Stat;
import org.json.JSONArray;
import org.json.JSONObject;

import com.focus.cos.CosServer;
import com.focus.cos.api.SysalarmClient;
import com.focus.cos.ops.service.AlarmNotifier;
import com.focus.cos.ops.service.EmailSender;
import com.focus.cos.ops.service.MessageNotify;
import com.focus.util.ConfigUtil;
import com.focus.util.F;
import com.focus.util.IOHelper;
import com.focus.util.Log;
import com.focus.util.Tools;
import com.focus.util.Zookeeper;

/**
 * @author focus
参数名	说明
clientPort	客户端连接server的端口，即对外服务端口，一般设置为2181吧。
dataDir	存储快照文件snapshot的目录。默认情况下，事务日志也会存储在这里。建议同时配置参数dataLogDir, 事务日志的写性能直接影响zk性能。
tickTime	ZK中的一个时间单元。ZK中所有时间都是以这个时间单元为基础，进行整数倍配置的。例如，session的最小超时时间是2*tickTime。
dataLogDir	事务日志输出目录。尽量给事务日志的输出配置单独的磁盘或是挂载点，这将极大的提升ZK性能。 
	（No Java system property）
globalOutstandingLimit	最大请求堆积数。默认是1000。ZK运行的时候， 尽管server已经没有空闲来处理更多的客户端请求了，但是还是允许客户端将请求提交到服务器上来，以提高吞吐性能。当然，为了防止Server内存溢出，这个请求堆积数还是需要限制下的。 
	(Java system property:zookeeper.globalOutstandingLimit.)
preAllocSize	预先开辟磁盘空间，用于后续写入事务日志。默认是64M，每个事务日志大小就是64M。如果ZK的快照频率较大的话，建议适当减小这个参数。(Java system property:zookeeper.preAllocSize)
snapCount	每进行snapCount次事务日志输出后，触发一次快照 (snapshot), 此时，ZK会生成一个snapshot.*文件，同时创建一个新的事务日志文件log.*。默认是100000.（真正的代码实现中，会进行一定的随机数 处理，以避免所有服务器在同一时间进行快照而影响性能）(Java system property:zookeeper.snapCount)
traceFile	用于记录所有请求的log，一般调试过程中可以使用，但是生产环境不建议使用，会严重影响性能。(Java system property:? requestTraceFile)
maxClientCnxns	单个客户端与单台服务器之间的连接数的限制，是ip级别 的，默认是60，如果设置为0，那么表明不作任何限制。请注意这个限制的使用范围，仅仅是单台客户端机器与单台ZK服务器之间的连接数限制，不是针对指定 客户端IP，也不是ZK集群的连接数限制，也不是单台ZK对所有客户端的连接数限制。指定客户端IP的限制策略，这里有一个patch，可以尝试一下：http://rdc.taobao.com/team/jm/archives/1334（No Java system property）
clientPortAddress	对于多网卡的机器，可以为每个IP指定不同的监听端口。默认情况是所有IP都监听 clientPort指定的端口。 New in 3.3.0
minSessionTimeoutmaxSessionTimeout	Session超时时间限制，如果客户端设置的超时时间不在这个范围，那么会被强制设置为最大或最小时间。默认的Session超时时间是在2 * tickTime ~ 20 * tickTime 这个范围 New in 3.3.0
fsync.warningthresholdms	事务日志输出时，如果调用fsync方法超过指定的超时时间，那么会在日志中输出警告信息。默认是1000ms。(Java system property: fsync.warningthresholdms)New in 3.3.4
autopurge.purgeInterval	在上文中已经提到，3.4.0及之后版本，ZK提供了自动清理事务日志和快照文件的功能，这个参数指定了清理频率，单位是小时，需要配置一个1或更大的整数，默认是0，表示不开启自动清理功能。(No Java system property) New in 3.4.0
autopurge.snapRetainCount	这个参数和上面的参数搭配使用，这个参数指定了需要保留的文件数目。默认是保留3个。(No Java system property) New in 3.4.0
electionAlg	在之前的版本中， 这个参数配置是允许我们选择leader选举算法，但是由于在以后的版本中，只会留下一种“TCP-based version of fast leader election”算法，所以这个参数目前看来没有用了，这里也不详细展开说了。(No Java system property)
initLimit	Follower在启动过程中，会从Leader同步所有最新数据，然后确定自己能够对外服务的起始状态。Leader允许F在initLimit时间内完成这个工作。通常情况下，我们不用太在意这个参数的设置。如果ZK集群的数据量确实很大了，F在启动的时候，从Leader上同步数据的时间也会相应变长，因此在这种情况下，有必要适当调大这个参数了。(No Java system property)
syncLimit	在运行过程中，Leader负责与ZK集群中所有机器进行 通信，例如通过一些心跳检测机制，来检测机器的存活状态。如果L发出心跳包在syncLimit之后，还没有从F那里收到响应，那么就认为这个F已经不在 线了。注意：不要把这个参数设置得过大，否则可能会掩盖一些问题。(No Java system property)
leaderServes	默认情况下，Leader是会接受客户端连接，并提供正常的读写服务。但是，如果你想让Leader专注于集群中机器的协调，那么可以将这个参数设置为no，这样一来，会大大提高写操作的性能。(Java system property: zookeeper.leaderServes)。
server.x=[hostname]:nnnnn[:nnnnn]	这里的x是一个数字，与myid文件中的id是一致的。右边可以配置两个端口，第一个端口用于F和L之间的数据同步和其它通信，第二个端口用于Leader选举过程中投票通信。 
	(No Java system property)
group.x=nnnnn[:nnnnn]weight.x=nnnnn	对机器分组和权重设置，可以 参见这里(No Java system property)
cnxTimeout	Leader选举过程中，打开一次连接的超时时间，默认是5s。(Java system property: zookeeper. cnxTimeout)
zookeeper.DigestAuthenticationProvider	ZK权限设置相关，具体参见 《 使用super 身份对有权限的节点进行操作》 和 《 ZooKeeper 权限控制》
.superDigest	
skipACL	对所有客户端请求都不作ACL检查。如果之前节点上设置有权限限制，一旦服务器上打开这个开头，那么也将失效。(Java system property: zookeeper.skipACL)
forceSync	这个参数确定了是否需要在事务日志提交的时候调用FileChannel.force来保证数据完全同步到磁盘。(Java system property: zookeeper.forceSync)
jute.maxbuffer	每个节点最大数据量，是默认是1M。这个限制必须在server和client端都进行设置才会生效。(Java system property: jute.maxbuffer)

 */
public class ZookeeperRunner extends SystemRunner
{
	private int clientport;
    public ZookeeperRunner(ModuleManager manager) throws Exception
    {
    	super("Zookeeper", manager);
    	super.className = "org.apache.zookeeper.server.quorum.QuorumPeerMain";
        this.setName( "分布式应用协调程序(Zookeeper)" );
        this.setRemark("为分布式应用提供一致性服务的软件，提供的功能包括：配置维护、域名服务、分布式同步、组服务等。");
    }

	private String getResourceAsStream(String path)
	{
		try
		{
			return new String(IOHelper.readAsByteArray(ZookeeperRunner.class.getResourceAsStream(path)), "UTF-8");
		}
		catch(Exception e){
		}
		return "";
	}
	
	@Override
	public void initliaize() throws Exception
	{
		clientport = 9529;
    	String controlPort = System.getProperty("control.port");
    	if( Tools.isNumeric(controlPort) )
    	{
    		clientport = Integer.parseInt(controlPort);
    	}
    	F datadir = new F("../data/zkdat");
    	if( !datadir.exists() ) datadir.mkdirs();
    	F datalogdir = new F("../data/zklog");
    	if( !datalogdir.exists() ) datalogdir.mkdirs();
    	String dataDir = Tools.replaceStr(datadir.getAbsolutePath(), "\\", "/");
    	String dataLogDir = Tools.replaceStr(datalogdir.getAbsolutePath(), "\\", "/");
    	F zkcfg = new F("../config/zk/");
    	if( !zkcfg.exists() ) zkcfg.mkdirs();
    	F zoocfg = new F(zkcfg, "zoo.cfg");
    	
    	F zkcfg_prop = new F("../config/zoo_cfg.properties");
		Properties properties = new Properties();
    	if( !zkcfg_prop.exists() ){
			String txt = getResourceAsStream("/com/focus/cos/control/zoo_cfg.properties");
			IOHelper.writeFile(zkcfg_prop, txt.getBytes("UTF-8"));
    	}
		properties.load(new FileInputStream(zkcfg_prop));
		
		if( !properties.containsKey("tickTime") ){
			properties.put("tickTime", "2000");
		}
		if( !properties.containsKey("initLimit") ){
			properties.put("initLimit", "5");
		}
		if( !properties.containsKey("syncLimit") ){
			properties.put("syncLimit", "2");
		}
		if( !properties.containsKey("autopurge.snapRetainCount") ){
			properties.put("autopurge.snapRetainCount", "10");
		}
		if( !properties.containsKey("autopurge.purgeInterval") ){
			properties.put("autopurge.purgeInterval", "1");
		}
		
    	StringBuffer sbZoocfg = new StringBuffer();
    	sbZoocfg.append("clientPort="+clientport);
    	sbZoocfg.append("\r\ndataDir="+dataDir);
    	sbZoocfg.append("\r\ndataLogDir="+dataLogDir);
		Iterator<Object> iterator = properties.keySet().iterator();
		while(iterator.hasNext()){
			String key = iterator.next().toString();
			if( "dataDir".equalsIgnoreCase(key) ){
				continue;
			}
			if( "dataLogDir".equalsIgnoreCase(key) ){
				continue;
			}
			String val = properties.getProperty(key, "");
			if( val.isEmpty()  ){
				if( "tickTime".equalsIgnoreCase(key) ){
					val = "2000";
				}
				if( "initLimit".equalsIgnoreCase(key) ){
					val = "5";
				}
				if( "syncLimit".equalsIgnoreCase(key) ){
					val = "2";
				}
				if( "autopurge.snapRetainCount".equalsIgnoreCase(key) ){
					val = "10";
				}
				if( "autopurge.purgeInterval".equalsIgnoreCase(key) ){
					val = "1";
				}
			}
			if( val.isEmpty() ){
				continue;
			}
			sbZoocfg.append("\r\n");
	    	sbZoocfg.append(key+"="+val);
		}

    	String servers = ConfigUtil.getString("zookeeper.servers");
    	String[] addresses = Tools.split(servers, ",");
		F myid = new F(datadir, "myid");
    	if( addresses.length > 0 )
    	{
    		Map<String,NetworkInterface> local = Tools.getLocalIPs();
        	Log.msg("["+id+"] Create the cluster("+servers+")");
    		for( int i = 0; i < addresses.length; i++ )
    		{
    			String address = addresses[i];
    			String ip = address;
    			int port = clientport;
    			String args[] = Tools.split(address, ":");
    			if( args.length == 2 )
    			{
    				ip = args[0];
    				port = Integer.parseInt(args[1]);
    			}
    			sbZoocfg.append("\r\nserver.");
    			sbZoocfg.append(i);
    			sbZoocfg.append("=");
    			sbZoocfg.append(ip);
    			sbZoocfg.append(":");
    			sbZoocfg.append(port+1);
    			sbZoocfg.append(":");
    			sbZoocfg.append(port+2);
    			if( local.containsKey(ip) && port == clientport )
    			{
    				IOHelper.writeFile(myid, String.valueOf(i).getBytes());
    				Log.msg("Succeed to create myid("+i+") to "+myid.getAbsolutePath()+"("+myid.exists()+").");
    			}
    		}
    		if( !myid.exists() )
    		{
				Log.msg("Failed to create myid for "+local);
    		}
    	}
    	else
    	{
    		myid.deleteOnExit();
    	}
    	IOHelper.writeFile(zoocfg, sbZoocfg.toString().getBytes("UTF-8"));
//    	extendsCommands.add( "-Dzookeeper.log.dir=../log/Zookeeper");
//    	extendsCommands.add( "-Dzookeeper.log.file="+Tools.getFormatTime("yyyyMMddHHmm", System.currentTimeMillis())+".txt");
    	extendsCommands.add( "-Dzookeeper.root.logger=INFO,ROLLINGFILE,CONSOLE" );
//    	extendsCommands.add( "-Djute.maxbuffer=4194304" );
//    	extendsCommands.add( "-Dzookeeper.log.threshold=INFO" );
    	F logFile = new F(super.dirLog, "log.txt");
    	String logFileStr = Tools.replaceStr(logFile.getAbsoluteFile().getPath(), "\\", "/");
    	F zoolog = new F(zkcfg, "log4j.properties");
    	StringBuffer sbLog4j = new StringBuffer();
    	sbLog4j.append("log4j.rootLogger=INFO,ROLLINGFILE,CONSOLE\r\n");
//    	sbLog4j.append("log4j.appender.ROLLINGFILE=org.apache.log4j.RollingFileAppender\r\n");
    	sbLog4j.append("log4j.appender.ROLLINGFILE=org.apache.log4j.DailyRollingFileAppender\r\n");
    	sbLog4j.append("log4j.appender.ROLLINGFILE.Threshold=INFO\r\n");
    	sbLog4j.append("log4j.appender.ROLLINGFILE.File="+logFileStr+"\r\n");
//    	sbLog4j.append("log4j.appender.ROLLINGFILE.MaxFileSize=64MB\r\n");
    	sbLog4j.append("log4j.appender.ROLLINGFILE.DataPattern='.'yyyy-MM-dd\r\n");
    	sbLog4j.append("log4j.appender.ROLLINGFILE.layout=org.apache.log4j.PatternLayout\r\n");
    	sbLog4j.append("log4j.appender.ROLLINGFILE.layout.ConversionPattern=[%d{HH:mm:ss}]%5p %F:%L %m%n\r\n");
//    	sbLog4j.append("log4j.appender.ROLLINGFILE.layout.ConversionPattern=%d{ISO8601} %-5p [%t:%C{1}@%L] - %m%n\r\n");
    	sbLog4j.append("log4j.appender.CONSOLE=org.apache.log4j.ConsoleAppender\r\n");
    	sbLog4j.append("log4j.appender.CONSOLE.Threshold=INFO\r\n");
    	sbLog4j.append("log4j.appender.CONSOLE.layout=org.apache.log4j.PatternLayout\r\n");
//    	sbLog4j.append("log4j.appender.CONSOLE.layout.ConversionPattern=[%d{HH:mm:ss}]%5p %F:%L %m%n\r\n");
    	sbLog4j.append("log4j.appender.CONSOLE.layout.ConversionPattern=[%d{ISO8601}]%5p %F:%L %m%n");
    	sbLog4j.append("\r\n");
    	IOHelper.writeFile(zoolog, sbLog4j.toString().getBytes("UTF-8"));
    	super.cpTag = "-classpath";
    	String separator = System.getProperty("path.separator", ";");
    	cp = new StringBuffer();
    	cp.append("../lib/zookeeper-3.4.6.jar");
    	cp.append(separator);
    	cp.append("../lib/slf4j-api-1.7.5.jar");
    	cp.append(separator);
    	cp.append("../lib/slf4j-log4j12-1.7.5.jar");
    	cp.append(separator);
    	cp.append("../lib/log4j-1.2.17.jar");
    	cp.append(separator);
    	cp.append("../lib/commons-collections-3.2.1.jar");
    	cp.append(separator);
    	cp.append("../lib/jdiff-1.0.9.jar");
    	cp.append(separator);
    	cp.append("../lib/jline-0.9.94.jar");
    	cp.append(separator);
    	cp.append("../lib/jdeb-0.8.jar");
    	cp.append(separator);
    	cp.append("../lib/xerces-1.4.4.jar");
    	cp.append(separator);
    	cp.append(zkcfg.getAbsolutePath());
    	/*
    	# Define some default values that can be overridden by system properties
    	zookeeper.root.logger=INFO, ROLLINGFILE
    	zookeeper.console.threshold=INFO
    	zookeeper.log.dir=D:/focusnt/cos/trunk/IDE/log/Zookeeper
    	zookeeper.log.file=zookeeper.log
    	zookeeper.log.threshold=DEBUG
    	zookeeper.tracelog.dir=.
    	zookeeper.tracelog.file=zookeeper_trace.log

    	#
    	# ZooKeeper Logging Configuration
    	#

    	# Format is "<default threshold> (, <appender>)+

    	# DEFAULT: console appender only
    	log4j.rootLogger=${zookeeper.root.logger}

    	# Example with rolling log file
    	#log4j.rootLogger=DEBUG, CONSOLE, ROLLINGFILE

    	# Example with rolling log file and tracing
    	#log4j.rootLogger=TRACE, CONSOLE, ROLLINGFILE, TRACEFILE

    	#
    	# Log INFO level and above messages to the console
    	#
    	log4j.appender.CONSOLE=org.apache.log4j.ConsoleAppender
    	log4j.appender.CONSOLE.Threshold=${zookeeper.console.threshold}
    	log4j.appender.CONSOLE.layout=org.apache.log4j.PatternLayout
    	log4j.appender.CONSOLE.layout.ConversionPattern=%d{ISO8601} [myid:%X{myid}] - %-5p [%t:%C{1}@%L] - %m%n

    	#
    	# Add ROLLINGFILE to rootLogger to get log file output
    	#    Log DEBUG level and above messages to a log file
    	log4j.appender.ROLLINGFILE=org.apache.log4j.RollingFileAppender
    	log4j.appender.ROLLINGFILE.Threshold=${zookeeper.log.threshold}
    	log4j.appender.ROLLINGFILE.File=${zookeeper.log.dir}/${zookeeper.log.file}

    	# Max log file size of 10MB
    	log4j.appender.ROLLINGFILE.MaxFileSize=10MB
    	# uncomment the next line to limit number of backup files
    	#log4j.appender.ROLLINGFILE.MaxBackupIndex=10

    	log4j.appender.ROLLINGFILE.layout=org.apache.log4j.PatternLayout
    	log4j.appender.ROLLINGFILE.layout.ConversionPattern=%d{ISO8601} [myid:%X{myid}] - %-5p [%t:%C{1}@%L] - %m%n
    	*/
    	extendsProperties.clear();
    	extendsProperties.add( zoocfg.getAbsolutePath() );
	}
    private Zookeeper zookeeper;
//    private HashMap<String, Boolean> ips = new HashMap<String, Boolean>();
    /**
     * 启动自动过滤
     */
    protected void startupAutoConfirm()
    {
        Thread thread = new Thread(){//该线程监听ZK连接
        	public void run()
        	{
        		try {
					sleep(5000);
				} catch (InterruptedException e1) {
				}
        		long connectTimestamp = System.currentTimeMillis();//连接时间戳
        		boolean nostandby = true;
        		long closeAlarmTimestamp = System.currentTimeMillis();//关闭告警时间戳
    			Log.war("Startup the thread of zookeeper connect.");
    			while( subprocess != null && executing )
    			{
    				if( connectZookeeper() ) 
    				{
        				if( nostandby )
        				{
        					CosServer.buildTimeline(zookeeper, manager.identity, WrapperShell.ModuleID, WrapperShell.Name, WrapperShell.Remark, WrapperShell.Versions);
        		        	CosServer.buildTimeline(zookeeper, manager.identity, COSApi.ModuleID, COSApiRunner.Name, COSApiRunner.Remark, COSApi.Versions);
        		        	CosServer.buildTimeline(zookeeper, manager.identity, ProgramLoader.ModuleID, ProgramLoaderRunner.Name, ProgramLoaderRunner.Remark, ProgramLoader.Versions);
        					CosServer.buildTimeline(zookeeper, manager.identity, SystemMonitor.ModuleID, SystemMonitorRunner.Name, SystemMonitorRunner.Remark, SystemMonitor.Versions);
        					CosServer.buildTimeline(zookeeper, manager.identity, "WrapperReport", WrapperReportRunner.Name, WrapperReportRunner.Remark, EmailSender.Versions);
        					if(manager.getModule(AlarmNotifier.ModuleID)!=null){
        						CosServer.buildTimeline(zookeeper, manager.identity, AlarmNotifier.ModuleID, SystemAlarmRunner.Name, SystemAlarmRunner.Remark, AlarmNotifier.Versions);
        					}
        					if(manager.getModule(MessageNotify.ModuleID)!=null){
        						CosServer.buildTimeline(zookeeper, manager.identity, MessageNotify.ModuleID, SystemNotifyRunner.Name, SystemNotifyRunner.Remark, MessageNotify.Versions);
        					}
//        					if(manager.getModule(EmailSender.ModuleID)!=null){
//        						CosServer.buildTimeline(zookeeper, manager.identity, EmailSender.ModuleID, SystemEmailRunner.Name, SystemEmailRunner.Remark, EmailSender.Versions);
//        					}
        		        	Log.msg("Succeed to build the timeline of version for all programs-inner.");
        					nostandby = false;
        				}
        				saveApiProxy();//保存API代理对象
		        		connectTimestamp = System.currentTimeMillis();
		        		if( closeAlarmTimestamp > 0 && System.currentTimeMillis() - closeAlarmTimestamp > Tools.MILLI_OF_MINUTE ){
		        			SysalarmClient.autoconfirm("Sys", id+"_Startup", "程序再次启动后超过1分钟运行工作正常");
		        			closeAlarmTimestamp = 0;
		        		}
    				}
    				else if( subprocess != null && executing && System.currentTimeMillis()-connectTimestamp>Tools.MILLI_OF_MINUTE )
	        		{
	                    System.out.println("@COS$ Restartup the zookeeper disconnected after 1 mnute.");
	        			Log.err("Restartup the zookeeper disconnected after 1 mnute.");
	        			restartup();
	        			break;
	        		}
            		synchronized(instance)
            		{
		        		try
						{
		        			instance.wait(15000);
						}
						catch (InterruptedException e)
						{
						}
            		}
        		}
    			Log.war("End the thread of zookeeper connect.");
        	}
        };
        thread.start();
    }
    
    /**
     * 判断ZK连接是否存在，否就尝试连接
     * @return
     */
    private synchronized boolean connectZookeeper()
    {
        try
        {
			if( zookeeper!=null && zookeeper.isConnected() ) 
			{
				return true;
			}
        	if( zookeeper != null ) zookeeper.close();
        	Log.msg("Startup the watch of zookeeper for something...");
        	System.out.println();
            System.out.println("@COS$ Startup the watch of zookeeper for something...");
	        zookeeper = new Zookeeper("127.0.0.1", clientport) {
				@Override
				public void watch(WatchedEvent event) {
					Log.msg("Receive event("+event.getType()+") from "+event.getPath());
					if( event.getPath().endsWith("weixin") )
					{
						if( event.getType() == EventType.NodeChildrenChanged )
						{
//							loadWeixinCallback();
						}
					}
					if( event.getType() == EventType.NodeDeleted )
					{
						int i = event.getPath().indexOf("weixin/");
						if( i != -1 )
						{
							String weixinno = event.getPath().substring(i+"weixin/".length());
							String id = "weixin_"+weixinno;
							if( manager.getModules().containsKey(id) )
							{
								Module module = manager.getModule(id);
								module.close();
								manager.delModule(module);
						    	sendSystemEmail("您的系统"+manager.getYourServiceDesc()+"在伺服器【"+ModuleManager.LocalIp+"】启动的微信公众号回调程序("+module.getName()+")被停止并删除", 
						    		"请确认是否是系统管理员安排的配置。");
							}
						}
					}
				}
			};
	        System.out.println("@COS$ ZooKeeper work on.");
        	System.out.println();
        	Log.msg(zookeeper+" standby.");
        	//因为有JPS，所以不能在这里执行通知
//        	synchronized (manager) {
//            	this.manager.notify();
//			}
//	        System.out.println("@COS$ Controler notified.");
//    		if( loadLocalIP(ips) )
//    		{
//    			loadWeixinCallback();
//    		}
    		return true;
        }
        catch(Exception e)
        {
        	if( zookeeper != null ){
        		zookeeper.close();
        	}
	        System.out.println("@ERROR$ ZooKeeper not work. Please check the config of your serivce.");
        	Log.err("Failed to watch the event of weixin-runner from port "+clientport, e);
        	return false;
        }
    }
	/**
	 * 
	 * @param title
	 * @param content
	 */
	public void sendSystemEmail(String title, String content)
	{
		Zookeeper zookeeper = null;
		try {
			zookeeper = Zookeeper.getInstance("127.0.0.1:"+clientport);
			CosServer.sendSystemEmail(zookeeper, title, content);
		} catch (Exception e) {
			Log.err("Failed to send the email of system for "+e);
		}
		finally{
			if( zookeeper != null ) zookeeper.close();
		}
	}
	
	/**
	 * 设置APIProxy数据到apiproxy节点
	 * @param proxy
	 */
	private ArrayList<JSONObject> apiProxyQuene = new ArrayList<JSONObject>();
	public synchronized void setApiProxy(JSONObject proxy)
	{
		Log.war("Quene the apiproxy to "+zookeeper);
		apiProxyQuene.add(proxy);
		this.notify();
	}
	
	private synchronized void saveApiProxy()
	{
		if( zookeeper == null || !zookeeper.isConnected() )
		{
			return;
		}
		JSONObject proxy = null;
		try
		{
			for(int i = 0; i < this.apiProxyQuene.size(); i++){
				proxy = this.apiProxyQuene.get(i);
				String id = proxy.getString("id");
				String key = proxy.getString("identity");
				String zkpath = "/cos/data/apiproxy/"+Tools.encodeMD5(id);
				Stat stat = zookeeper.exists(zkpath, false);
				if( stat != null )
				{
					JSONObject old = new JSONObject(new String(zookeeper.getData(zkpath, false, stat), "UTF-8"));
					JSONArray history = null;
					if( old.has("history") )
					{
						history = old.getJSONArray("history");
						old.remove("history");
						while(history.length()>32){
							history.remove(0);
						}
					}
					else
					{
						history = new JSONArray();
					}
					old.remove("id");
					String key1 = old.getString("identity");
					old.put("identity", key1.equals(key));
					history.put(old);
					proxy.put("history", history);
					zookeeper.setData(zkpath, proxy.toString().getBytes("UTF-8"), stat.getVersion());
				}
				else
				{
					CosServer.createNode(zookeeper, zkpath, proxy.toString().getBytes("UTF-8"));
				}
				this.apiProxyQuene.remove(i);
				i -= 1;
			}
		}
		catch(Exception e)
		{
			Log.err("Failed to save the proxy of api "+proxy.toString(4), e);
		}
	}

	@Override
	protected void handleProcessOutput(String line) {
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		
	}
}
