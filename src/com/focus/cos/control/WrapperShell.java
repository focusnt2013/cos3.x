package com.focus.cos.control;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.Key;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

import org.apache.commons.io.FileUtils;
import org.h2.tools.Backup;
import org.h2.tools.CreateCluster;
import org.h2.tools.Server;

import com.focus.sql.BackupToSql;
import com.focus.util.Base64;
import com.focus.util.ConfigUtil;
import com.focus.util.F;
import com.focus.util.IOHelper;
import com.focus.util.Log;
import com.focus.util.Tools;
/**
 * <p>Title: 主控入口程序</p>
 *
 * <p>Description: 启动主控模块管理器，初始化h2数据库，启动命令行接收线程接收控制台发送过来的指令</p>
 *
 * <p>Copyright: Copyright (c) 2008</p>
 *
 * <p>Company: Focusnt</p>
 *
 * @author 刘学
 * @authkey 厂商授权标识
 * <厂商标识>.<授权时间20140116>.<版本号3_14_1_11>.<授权对象>
 * focusnt.00000000.3_14_1_11.free
 * focusnt.20140116.3_14_1_11.efida
 */
public class WrapperShell extends Thread
{
	public static final String Name = "主控引擎"; 
	public static boolean isWindows = false;
    public static String ModuleID = "COSControl";
    public static String License = "focusnt.20990516.3_16_12_2.free";
    public static boolean H2Switcher = false;
    public static String Pid;//主控引擎的PID
    public static final String Remark = 
		"【COS】云架构开放式应用服务框架，是一套基于DevOps框架思想能够用于开发与运维各种类型应用服务系统的软件框架容器。"+
		"该软件框架容器由前台【主界面框架系统】与后台【主控引擎】组成，本程序是后台【主控引擎】软件。"+
		"【主控引擎】具备跨平台、多进程、分布式等特性。能够在各种硬件服务器环境下运行，支持包括JAVA、C语言程序、服务器脚本多种程序。"+
		"实现多集群的网络、硬件、系统程序统一管理。";
	public static String[][] Versions = {
		{"3.13.4.12",	"3.0版本发布，稳定版本基本特性功能具备。"},
		{"3.13.7.3",	"增加了服务器状态的监控特性。"},
		{"3.13.7.7",	"解决了同步服务器消息阻塞造成的守护进程互斥文件没能及时更新引起的子进程中断System.exit for quite control;解决了进程服务内存采集问题。"},
		{"3.13.7.17",	"解决磁盘IO统计问题。"},
		{"3.13.11.13",	"启动H2。"},
		{"3.14.1.11",	"产品级邮件引擎。"},
		{"3.16.2.26",	"捆绑Zookeeper服务。"},
		{"3.16.3.12",	"实现对非JAVA进程内存使用的监控。"},
		{"3.16.3.26",	"实现清除日志功能。"},
		{"3.16.6.20",	"新版本Wrapper授权处理程序。"},
		{"3.16.7.1",	"升级功能。"},
		{"3.16.7.3",	"邮件组件功能升级。"},
		{"3.16.7.6",	"调整内存监控的算法。"},
		{"3.16.7.11",	"解决升级算法版本号取值不对的BUG。"},
		{"3.16.7.12",	"解决持续占用资源监控字段问题。"},
		{"3.16.7.31",	"调整主控启动系统监控等程序的逻辑。"},
		{"3.16.8.13",	"重构主控启动自带子程序的逻辑，重构系统告警通知模块。"},
		{"3.16.8.26",	"解决软件升级器网络通信数据传输连接超时问题大重构;删掉所有WAPPUSH功能;增加数据库监控功能;重构主控启动缺省程序逻辑;增加系统告警通知发送功能;解决ZK启动后节点数据不超过1M的问题,改为默认６４Ｍ;其它BUG修复。"},
		{"3.16.8.28",	"可在线升级版本，修改系统监控的BUG;将ZK的缺省节点数据大小改回1M，因为过大的节点数据将引起系统网络与内存过载。"},
		{"3.16.9.13",	"优化系统监控告警的策略,完善系统监控程序。"},
		{"3.16.9.21",	"重构主控启动机制，将主控程序与wrapper服务剥离降维，目的是实现主控程序自行升级并重启动，增加主控引擎内存监控功能。"},
		{"3.16.9.22",	"解决系统监控CPU等指标的BUG。"},
		{"3.16.9.23",	"解决11服务器无法正常关闭子进程的问题。"},
		{"3.16.9.24",	"解决程序过于频繁发送监控数据的问题。"},
		{"3.16.9.25",	"解决缺省编码引起的程序错误。"},
		{"3.16.9.26",	"解决告警误差。"},
		{"3.16.9.27",	"解决频繁次要告警的问题。"},
		{"3.16.9.28",	"跨域监控数据传输效率问题。"},
		{"3.16.10.6",	"重构版本管控机制。"},
		{"3.16.10.12",	"主控管理器监控同步器多线程冲突问题解决；增加模块负责程序员联系方式上传。"},
		{"3.16.10.22",	"重新编译的cos.jar包。"},
		{"3.16.10.23",	"解决根据PID查内存pmap命令使用不对问题；解决本身处于停止状态的进程暂停问题。"},
		{"3.16.10.24",	"增加系统消息通知模块。"},
		{"3.16.11.21",	"解决启动cos-web因为h2支持的不同jdk版本问题引起的故障。"},
		{"3.16.11.22",	"解决因为cos-web版本配置不一致问题造成的重启。"},
		{"3.16.11.26",	"解决server.xml中配置的是绝对路径的兼容问题。"},
		{"3.16.11.27",	"解决系统内置程序无法暂停的问题。"},
		{"3.16.11.29",	"h2数据库集群支持。"},
		{"3.16.12.2",	"解决系统程序错误信息捕获告警中数据不正确问题。"},
		{"3.17.2.6",	"适配配置文件全部迁移到了Zookeeper后的程序问题。"},
		{"3.17.3.3",	"修改获取文件的方法。"},
		{"3.17.3.19",	"调整服务启动与控制的框架为命令行做准备;调整优化了某些后台处理方法适应新架构。"},
		{"3.17.4.12",	"主控引擎命令行控制模式：主界面框架系统在线下载安装；Windows版本命令行控制，配置主控端口，服务程序配置。"},
//		{"3.17.4.?",	"集群同步管理：集成同步程序到内嵌服务；提供可视化界面配置管理目录文件同步；优化双向同步功能。"},
//		{"3.17.4.?",	"主控引擎命令行控制模式之Linux版本命令行控制，将主控引擎配置为Linux服务。"},
		{"3.17.5.7",	"解决因为CMD指令打开主控引擎所引起的相对路径失效的问题;改善优化停止服务等待时间。"},
		{"3.17.5.12",	"解决COS通过界面重启后确保能重启起来;解决服务器标识在ZK中有效识别问题。"},
		{"3.17.6.19",	"新架构程序配置引擎，实现程序配置发布全面管控;新架构COS接口引擎，全新安全加密的接口服务；实现集群文件管理对文件操作升级：伺服器目录压缩下载,伺服器压缩包在线解压,伺服器文件目录复制拷贝；优化启动重启逻辑，实现各种情况下重启，新引擎进一步让平台子系统微服务化，对平台模块之间运行尽可能解耦;解决文件传输中存在的问题;备服务器启动时删除数据库文件解决主备数据库同步问题;主控升级模块实现人工管理升级。"},
		{"3.17.7.19",	"解决程序发布接口的问题;解决程序配置管理引擎的问题;解决系统开发管理模块下的主控引擎适配。"},
		{"3.17.7.21",	"解决多网卡下识别Zookeeper集群的问题;取消ZookeeperRunner启动微信回调程序的功能。"},
		{"3.17.7.25",	"解决内置的COSPortal的tomcat虚拟机启动阻塞的问题"},
		{"3.17.8.3",	"系统告警不能稳定推送的问题；每个程序告警实现定向推送；扩展告警表和系统邮件表"},
		{"3.17.8.4",	"解决更新告警的时候将伺服器的密钥进行设置不成功的问题"},
		{"3.17.8.5",	"增加用户表字段记录上次登录IP和登录区域为实现用户安全管控新特性增加系统安全性"},
		{"3.17.9.29",	"解决因为xerces包冲突原因导致内置的微信回调程序模块无法正常接收post消息的问题"},
		{"3.17.10.6",	"H2.sql文件中竟然忘记了frequency字段."},
		{"3.17.10.23",	"解决设置缺省的日志文件和PID文件的路径问题."},
		{"3.17.11.17",	"修改用户表username字段长度，增加creator字段作为用户创建者."},
		{"3.18.1.6",	"H2数据库可视化管理，为主界面框架提供管理接口，完整控制H2数据库主备机制;修改某处代码打印日志的问题."},
		{"3.18.1.15",	"H2数据库自动备份功能。"},
		{"3.18.1.18",	"解决COSAPI读取cos.api.port参数配置容错控制导致无法启动API引擎的问题。"},
		{"3.18.3.9",	"配置支持GateOne组件;解决pid文件读取逻辑问题。"},
		{"3.18.3.12",	"模块程序的配置文件参数设置与传递;检查windows进程的内存使用"},
		{"3.18.3.23",	"创建目录和创建文件;优化删除文件或文件夹功能"},
		{"3.18.3.27",	"程序内存使用监听采集最大峰值的情况。"},
		{"3.18.3.30",	"处理内存检查BUG"},
		{"3.18.4.8",	"重大升级，解决JAVA僵尸进程的问题;解决debug日志在清除后就不能继续写的问题;自动安装JDK1.8。"},
		{"3.18.4.11",	"安装jdk后赋予权限;发现有重复的同步监控实例没有删除;僵尸进程告警错误问题；优化告警逻辑;解决COS-API接口读取本地IP地址127.0.0.1的问题"},
		{"3.18.5.17",	"解决读取pid文件失败的问题"},
		{"3.18.5.26",	"解决不能正确获取工作目录路径的小概率问题;解决因为主控报告失败而导致JDK安装失败问题;解决forceKill多余的日志打印问题;解决初始的时候没有证书的问题"},
		{"3.18.5.31",	"解决不能正确获取物理绑定的本地IP地址的问题"},
		{"3.18.6.12",	"解决上报程序的监控信息因为多线程操作造成序列化数据出错的问题"},
		{"3.18.6.21",	"解决致命错误JPS进程检查引起的线程死锁;增加对linux伺服下获取文件目录的信息的功能"},
		{"3.18.7.3",	"解决GC监控中数据单位问题;增加一个查询子用户的接口;告警去重弃用DN字段用serverkey;完善进程退出告警机制;完善Zookeeper初始化控制机制;捕获识别配置了PID文件的程序出现死进程的情况增加告警;增加模块在非启动状态杀死死进程的特性;增加网络监控特性；修改GC日志分析的BUG，增加查看历史GC分析的功能;智能查询伺服器目录情况;增加Zookeeper扩展参数配置;Debug日志文件对象没有初始化导致不能分页，Debug日志只能输出到默认日志目录;解决获取日志文件通配符错误解析问题;"},
		{"3.18.7.29",	"解决网络问题导致的主控死锁问题;增加JPS检查异常状况的容错;解决DEBUG日志清除不掉的问题;新增启动失败的程序上报异常信息功能;解决目录信息查询数据超出64K的传输控制问题;解决API引擎启动够API端口设置不到System生效问题;去掉目录扫描的线程监听;定位解决监控同步接口出现异常的情况;守护进程的kill逻辑优化;"},
		{"3.18.11.29",	"解决文件名读写编码问题；优化系统告警通知程序。"},
		{"3.19.7.4",	"增加JMAP和JSTACK获取堆栈数据。"},
		{"3.19.11.19",	"启动后检查数据库文件尺寸，如果发现文件超过1G，自动备份，清除历史数据文件。"},
	};
    private ModuleManager manager = null;
    private Server h2Server = null;
    private long h2ServerStarttime;
    /*伺服器会UDP监听h2端口，直到收到主服务器发来的信号告知*/
    private boolean h2Standby;//数据库服务就位信号
    private boolean h2Ok;//数据库表示否加载
    private String h2StandbyInfo;
    private DatagramSocket h2StandbySocket = null;

    public static void main( String[] args )
    {
    	isWindows = System.getProperty("os.name").toLowerCase().indexOf("window") != -1;
//    	Version = Versions[Versions.length-1][0];
    	License = "focusnt.20990516."+getVersion()+".free";
    	WrapperShell shell = new WrapperShell();
    	shell.startup();
    }
    
	public static String SetupSql[] = {
		"ALTER TABLE TB_SYSLOG ADD IF NOT EXISTS category int(4);",//增加日志分类，记录日志的分类类型
		"ALTER TABLE TB_SYSLOG ADD IF NOT EXISTS account VARCHAR(64);",//身份标识, 在不同日志类型下有不同含义
		"ALTER TABLE TB_SYSLOG ADD IF NOT EXISTS contextlink VARCHAR(1024);",
		"ALTER TABLE TB_SYSLOG ADD IF NOT EXISTS context TEXT;",
		"ALTER TABLE TB_NOTIFIES MODIFY COLUMN CONTEXTLINK VARCHAR(1024);",
		"ALTER TABLE TB_NOTIFIES MODIFY COLUMN CONTEXTIMG VARCHAR(1024);",
		"ALTER TABLE TB_NOTIFIES MODIFY COLUMN ACTIONLINK VARCHAR(1024);",
		"ALTER TABLE TB_SYSLOG MODIFY COLUMN CATEGORY VARCHAR(128);",//分类类型不同含义
		"ALTER TABLE TB_SYSALARM ADD IF NOT EXISTS responser VARCHAR(64);",
		"ALTER TABLE TB_SYSALARM ADD IF NOT EXISTS serverkey VARCHAR(64);",
		"ALTER TABLE TB_SYSALARM ADD IF NOT EXISTS contact VARCHAR(64);",
		"ALTER TABLE TB_EMAIL_OUTBOX ADD IF NOT EXISTS RESULT VARCHAR(1024);",
		"ALTER TABLE TB_USER ADD IF NOT EXISTS lastLoginIp VARCHAR(64);",
		"ALTER TABLE TB_USER ADD IF NOT EXISTS lastLoginRegion VARCHAR(256);",
		"ALTER TABLE TB_SYSALARM ADD IF NOT EXISTS frequency int(4);",
		"ALTER TABLE TB_USER ADD IF NOT EXISTS creator VARCHAR(256);",
		"ALTER TABLE TB_USER MODIFY COLUMN username VARCHAR(256);",
		"DROP TABLE TB_EMAIL_OUTBOX;",
		"DROP TABLE TB_SYSLOG;",
	};
	
	/**
	 * 安装H2服务器
	 * @return
	 * @throws SQLException 
	 */
	private void startH2Server() throws SQLException
	{
		String h2WebSwitch = ConfigUtil.getString("cos.database.h2.web", "false");
    	if( "true".equalsIgnoreCase(h2WebSwitch) )
    	{
			this.h2Server = Server.createTcpServer(new String[] { 
					"-tcpAllowOthers",
					"-tcpPort",
					ConfigUtil.getString("h2.tcp.port", "9092"),
					"-webAllowOthers",
					"-webPort",
					ConfigUtil.getString("h2.web.port", "8092"),
				}).start();
    	}
    	else
    	{
			this.h2Server = Server.createTcpServer(new String[] { 
					"-tcpPort",
					ConfigUtil.getString("h2.tcp.port", "9092"),
					"-tcpAllowOthers"
				}).start();
    	}
    	h2ServerStarttime = System.currentTimeMillis();
    	Log.msg("Startup h2.", true);
    	h2Standby = false;
    	Thread thread = new Thread(){
    		public void run(){
    	    	if( h2StandbySocket != null ){
    	    		h2StandbySocket.close();
    	    	}
    			byte[] payload = new byte[128];
				try {
					h2StandbySocket = new DatagramSocket(h2Server.getPort());
					h2StandbySocket.setSoTimeout(0);
	                DatagramPacket getPacket = new DatagramPacket( payload, payload.length );
	                Log.msg("Wait the notify of standby at "+h2Server.getPort());
	                h2StandbySocket.receive( getPacket );
	                String msg = new String(payload, 0, getPacket.getLength());
	                Log.msg("Recevie the notify of standby "+msg);
	                h2Standby = true;
				}
				catch (Exception e) {
					Log.err("Abort to listen from socket "+e.getMessage());
				}
    	        finally
    	        {
    	        	if( h2StandbySocket != null )
    	        	{
    	        		h2StandbySocket.close();
    	        	}
    	        	h2StandbySocket = null;
    	        }
    		}
    	};
    	thread.start();
	}
	
	/**
	 * 关闭h2服务器
	 */
	public void closeH2Server()
	{
		if( h2Server != null ) 
		{
			Log.war("Close h2("+h2Server.getStatus()+").");
			this.h2Server.stop();
			h2StandbySocket.close();
		}
	}
	/**
	 * 
	 */
	public void setupH2Server()
	{
    	try
		{
    		closeH2Server();
    		String h2Cluster = ConfigUtil.getString("cos.database.h2", "");
        	if( "standby".equalsIgnoreCase(h2Cluster) ){
    			F h2dir = new F(ConfigUtil.getWorkPath(), "h2");
    			if( h2dir.exists() )
    			{
	    			F destDir = new F(ConfigUtil.getWorkPath(), "backup/cosh2/"+Tools.getFormatTime("yyyyMMddHHmm", System.currentTimeMillis()));
	    			FileUtils.moveDirectoryToDirectory(h2dir, destDir, true);
    			}
				h2dir.mkdirs();
    			Log.msg("Setup the h2 for standby.", true);
    			startH2Server();
    			Log.msg("Wait the master to setup.", true);
    		}
    		else {
    			F h2dir = new F(ConfigUtil.getWorkPath(), "h2");
    			F dbfile = null;
    			if( h2dir.exists() )
    			{
    				F destDir = new F(ConfigUtil.getWorkPath(), "backup/cosh2/"+Tools.getFormatTime("yyyyMMddHHmm", System.currentTimeMillis()));
    				FileUtils.copyDirectoryToDirectory(h2dir, destDir);//一定要备份，万一出了问题还能回滚
    				F[] files = h2dir.listFiles(new FilenameFilter(){
						@Override
						public boolean accept(File dir, String name) {
							return name.startsWith("cos.");
						}
    				});
    				if( files.length > 0 ){
    					dbfile = files[0];
    					Log.war(String.format("Found the file of database %s for size %s", dbfile.getName(), dbfile.length()));
    				}
    			}
    			startH2Server();
    			setupH2Sql();
				Log.war(String.format("Do thin is %s", doThin));
    			//TOOD:数据库文件瘦身，将用户表导出，删除数据文件，重建数据库服务
    			if( doThin && dbfile != null ){
    				String s = "id,roleid,username,password,realname,email,status,sex,lastLogin,lastchangepassword,errorcount,historypassword,lastloginip,lastloginregion,creator";
    				String args[] = Tools.split(s, ",");
    				HashMap<String, String> filters = new HashMap<String,String>();
    				for(String arg : args){
    					filters.put(arg, null);
    				}
        			Log.war(String.format("Do thin the file of database %s for size %s", dbfile.getName(), dbfile.length()));
    		        String jdbcDriver = ConfigUtil.getString("cos.jdbc.driver", "");
    		        String jdbcUrl = ConfigUtil.getString("cos.jdbc.url", "");
    		    	ArrayList<String> sqls = BackupToSql.export(jdbcDriver, jdbcUrl, "sa", "", "TB_USER", filters);//备份用户表
    		    	StringBuilder sb = new StringBuilder(String.format("Backup the table of user %s from %s", sqls.size(), jdbcUrl));
    		    	for( int i = 0; i < sqls.size(); i++ ){
    		    		String sql = sqls.get(i);
    		    		if( sql.indexOf("1,1,'admin'") != -1 ){
    		    			sqls.remove(i); i -= 1;
    		    		}
    		    		sb.append("\r\n\t"+sql);
    		    	}
    		    	Log.war(sb.toString());
    		    	closeH2Server();//先停掉数据库
    		    	Log.war(String.format("Delete the file of database %s", dbfile.delete()));//删掉数据库文件
        			startH2Server();//再次启动数据库
        			setupH2Sql();//初始化数据库
        			int count = BackupToSql.restore(jdbcDriver, jdbcUrl, "sa", "", "TB_USER", sqls);
    		    	Log.war(String.format("Restore the table of user %s", count));
    			}
        		if( "master".equalsIgnoreCase(h2Cluster) )
        		{
        			String urlTarget = ConfigUtil.getString("h2.cluster.standby", "");
        			Log.msg("This is the master of database need to cluster with "+urlTarget);
        			setupH2Cluster(urlTarget);
            	}
        		//无论是否启动集群，都通知数据库就位
    			notifyStandby("127.0.0.1", h2Server.getPort());
    		}
        }
		catch (Exception e1)
		{
			Log.err("Failed to setup h2 database.", e1);
    		System.out.println("Failed to setup h2 database for "+e1);
		}
	}
	
	/**
	 * 通知对端自己OK了
	 * @param ip
	 * @param port
	 */
	public void notifyStandby(String addr){
		String[] args = Tools.split(addr, ":");
		this.notifyStandby(args[0], Integer.parseInt(args[1]));
	}
	public void notifyStandby(String ip, int port){
		DatagramSocket datagramSocket = null;
		try
		{
			byte[] buf = h2Server.getStatus().getBytes();
			datagramSocket = new DatagramSocket();
            datagramSocket.setSoTimeout(0);
            DatagramPacket request = new DatagramPacket(buf, 0, buf.length, InetAddress.getByName(ip), port );
            datagramSocket.send( request );
			Log.msg("Notify standby to "+ip+":"+port);
		}
		catch(Exception e){
			Log.err("Failed to notify standby to "+ip+":"+port, e);
		}
        finally
        {
        	if( datagramSocket != null )
        	{
        		datagramSocket.close();
        	}
        }
	}
	
	/**
	 * 备份数据文件
	 * @throws SQLException 
	 */
	private String backupH2()
	{
		String result = "";
    	F dbdir = new F("../h2");
    	if( dbdir.exists() )
    	{
    		F files[] = dbdir.listFiles(new FilenameFilter(){
				public boolean accept(java.io.File dir, String name) {
					return name.startsWith("cos.")&&name.endsWith(".db");
				}
    		});
    		if( files.length > 0 )
    		{
				String info = "数据文件["+files[0].getName()+"]["+Tools.bytesScale(files[0].length())+"]";
        		Log.msg("Backup the h2 dababase of cos from "+files[0].getName()+"("+files[0].length()+") ...", true);
        		F bakdbdir = new F("../data/h2/");
        		if( !bakdbdir.exists() )
        		{
        			bakdbdir.mkdirs();
        		}
        		F bakdbfile = new F(bakdbdir, "cosdb_bak_"+Tools.getFormatTime("yyMMddHHmm", System.currentTimeMillis())+".zip");
        		try {
        			long ts = System.currentTimeMillis();
					Backup.execute(bakdbfile.getAbsolutePath(), dbdir.getAbsolutePath(), "cos", false);
					ts = System.currentTimeMillis() - ts;
            		Log.msg("Succeed to backup, delay "+Tools.getDuration(ts), true);
            		result = info+"成功备份到"+bakdbfile.getPath()+", 备份数据"+Tools.bytesScale(bakdbfile.length())+"，耗时"+Tools.getDuration(ts);
				} catch (SQLException e) {
					Log.err("Failed to backup the file("+info+") for ", e);
            		result = info+"备份异常"+e;
				}
    		}
    		else{
        		result = "H2数据文件目录("+dbdir.getPath()+")下没有发现任何数据文件,请确认您是否已经删除了这些数据文件.";
    		}
    	}
    	else{
    		result = "H2数据文件目录("+dbdir.getPath()+")不存在,请确认您是否已经删除了该目录.";
    	}
    	return result;
	}
	/**
	 * 启动控制台
	 */
    public void startup()
    {
        //启动日志管理器
        Log.getInstance().setSubroot( ModuleID );
        Log.getInstance().setDebug( false );
        Log.getInstance().setLogable( true );
        Log.getInstance().start();
		this.start();//启动接受控制台指令的进程
		//设置COS的APPI参数到系统参数中。
//		setupApiPort();
		String h2Switch = ConfigUtil.getString("cos.database.h2", "none");
		if( h2Switch.equalsIgnoreCase("false") ) h2Switch = "none";
		H2Switcher = !"none".equalsIgnoreCase(h2Switch);
		
		String svrname = ConfigUtil.getString("service.name", "");
		String svrdesc = ConfigUtil.getString("service.desc", "");
		
		StringBuffer info = new StringBuffer("==========================================================");
		info.append("\r\nCOS "+version());
		info.append("\r\n\tCopyright (C) 2008-2019 Focusnt.  All Rights Reserved.");
		info.append("\r\nYour service of "+svrname+"("+svrdesc+") runtime below:");
		info.append("\r\n\tThe encoding of file is "+System.getProperty("file.encoding"));
		info.append("\r\n\tThe encoding of sun.jnu is "+System.getProperty("sun.jnu.encoding"));
		info.append("\r\n\tThe country of user is "+System.getProperty("user.country"));
		info.append("\r\n\tThe language of user is "+System.getProperty("user.language"));
		info.append("\r\n\tThe time of startup is "+Tools.getFormatTime("yyyy-MM-dd HH:mm:ss"));
		String identity = "";
		try
		{
	    	String runname = ManagementFactory.getRuntimeMXBean().getName();
	    	Pid = runname.split("@")[0];
			info.append("\r\n\tThe pid of program is "+Pid);
			F dataDir = new F(ConfigUtil.getWorkPath(), "data");
			F fileIdentity = new F(dataDir, "identity");
			if( !fileIdentity.exists() )
			{//如果没有公钥私钥，随机产生配对的密钥对
				if( !dataDir.exists() ){
					dataDir.mkdirs();
				}
				//随机产生一个数字证书
				IOHelper.writeSerializable(fileIdentity, KeyGenerator.getInstance("DES").generateKey());
			}
			Log.msg("Load the idendity from "+fileIdentity.getPath()+"("+fileIdentity.exists()+")");
			byte[] payload = IOHelper.readAsByteArray(fileIdentity);
			//读取数字证书并初始化
			Key key = (Key)IOHelper.readSerializableNoException(payload);
	    	Cipher c = Cipher.getInstance("DES");
	        c.init(Cipher.WRAP_MODE, key);//再用数字证书构建另外一个DES密码器
	        identity = Base64.encode(c.wrap(key));
		}
		catch (Exception e)
		{
			Log.err("Failed to generate the identity of cos for ", e);
			System.exit(1);
			return;
		}
    	try
		{
    		info.append("\r\n\tThe version of java is "+System.getProperty("java.version"));
    		info.append("\r\n\tThe switch of h2 database is "+h2Switch);
    		if( H2Switcher ) info.append("\r\n\tThe cos-api-port is "+ConfigUtil.getString("cos.api.port", "n/a"));
    		else info.append("\r\n\tThe cos-api is "+ConfigUtil.getString("cos.api", "n/a"));
    		info.append("\r\n\tThe dir of user is "+System.getProperty("user.dir"));
    		info.append("\r\n\tThe path of work is "+ConfigUtil.getWorkPath());
    		info.append("\r\n\tThe identity of your service is "+identity);
    		info.append("\r\n\tThe port of control is "+System.getProperty("control.port"));
    		ModuleManager.LocalIp = Tools.getLocalIP();
    		info.append("\r\n\tThe ip of localhost is "+ModuleManager.LocalIp);
		}
		catch (Exception e)
		{
			info.append("\r\n\t"+e.toString());
		}
		info.append("\r\n==========================================================");
		Log.msg( info.toString() );
		System.out.println(info.toString());
		//当程序退出的时候调用
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			public void run()
			{
				Log.msg("Release all programs, please wait moment...", true);
				shutdown(false);
			}
		});
        //启动H2数据库
		if( H2Switcher )
		{//如果是打开h2数据库
			setupH2Server();
		}
		try
		{
	        manager = new ModuleManager(identity)
	        {
	        	
				public void doRestart()
				{
					shutdown(true);
					System.out.println("@COS$ Restart");
					System.exit(0);
				}
				
				/**
				 * 数据库是否就位
				 */
				public boolean isDatabaseStandby()
				{
					return H2Switcher && h2Standby && h2Ok;
				}

				@Override
				public int getDatabasePort()
				{
					return h2Server!=null?h2Server.getPort():0;
				}
	
				@Override
				public String getDatabaseStatus() 
				{
					if( !H2Switcher ) return "未部署数据库";
					if( h2Server == null ) return "数据库未初始化";
					if(!isDatabaseRunning()){
						return "数据库未启动";
					}
					if(!h2Standby){
						return "待信号激活";
					}
					return h2StandbyInfo!=null?h2StandbyInfo:"未检测数据表加载";
				}

				@Override
				public boolean isDatabaseRunning() 
				{
					if( h2Server == null ) return false;
					return h2Server.isRunning(true);
				}

				@Override
				public String getDatabaseStarttime(){
					if( h2ServerStarttime == 0 ) return "";
					return Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", h2ServerStarttime);
				}

				@Override
				public String setDatabaseStatus(int mode, String param) 
				{
					String result = "";
		            switch(mode){
		            case 0:
		            	if( h2Server!= null ){
		            		long ts = System.currentTimeMillis();
		            		h2Server.stop();
		            		h2Standby = false;
		                	if( h2StandbySocket != null ){
		                		h2StandbySocket.close();
		                	}
		                	ts = System.currentTimeMillis() - ts;
				    		Log.msg("Succeed to stop h2 database("+h2Server.getStatus()+"), delay "+Tools.getDuration(ts), true);
				    		result = "主控引擎H2数据库("+h2Server.getStatus()+")已结停止，耗时"+Tools.getDuration(ts);
		            	}
		            	else{
				    		Log.msg("Failed to stop h2 database for not setup.", true);
				    		result = "主控引擎H2数据库没初始化";
		            	}
		            	break;
		            case 1:
		            	if( h2Server!= null ){
		            		h2Server.stop();
		                	if( h2StandbySocket != null ){
		                		h2StandbySocket.close();
		                	}
		            	}
		            	else{
				    		Log.msg("Not found the h2 steup.", true);
		            	}
		            	try {
		            		long ts = System.currentTimeMillis();
		            		F h2dir = new F(ConfigUtil.getWorkPath(), "h2");
		            		long size = IOHelper.getDirSize(h2dir);
							startH2Server();
							Thread.sleep(1000);
		                	ts = System.currentTimeMillis() - ts;
				    		String h2Cluster = ConfigUtil.getString("cos.database.h2", "");
		        			Log.msg(String.format("Succeed to restart h2 database(%s), the size of data file is %s, delay %s", 
		        				h2Server.getStatus(), Tools.bytesScale(size), Tools.getDuration(ts)), true);
			    			if( h2dir.exists() && size > 0 )
			    			{
			    				if("normal".equalsIgnoreCase(h2Cluster) ){
			    					notifyStandby("127.0.0.1", h2Server.getPort());
				    				result = String.format("伺服器主控引擎数据库[%s]基于数据文件(%s)完成重启，耗时%s", 
						    				h2Server.getStatus(), Tools.bytesScale(size), Tools.getDuration(ts));
			    				}
			    				else{
				    				result = String.format("伺服器主控引擎数据库[%s]基于数据文件(%s)完成重启，耗时%s等待创建集群后信号激活.", 
					    				h2Server.getStatus(), Tools.bytesScale(size), Tools.getDuration(ts));
			    				}
			    			}
			    			else{
					    		result = String.format("伺服器主控引擎数据库[%s]基于空数据文件完成重启，耗时%s等待创建集群后信号激活.", 
				    				h2Server.getStatus(), Tools.getDuration(ts));
			    			}
						} catch (Exception e) {
							Log.err("Failed to restart h2 database for exception.", e);
				    		System.out.println("Failed to restart h2 database for "+e);
				    		result = "主控引擎H2数据库("+h2Server.toString()+")出现异常"+e;
						}
		            	break;
		            case 2://启动集群
		            	if( h2Server!= null ){
		            		result = setupH2Cluster(param);
		        			notifyStandby("127.0.0.1", h2Server.getPort());
		            	}
		            	else{
				    		Log.msg("Failed to setup the cluster of h2 for not found the h2 steup.", true);
				    		result = "因为数据库服务没初始化不能启动主控引擎H2数据库集群.";
		            	}
		            	break;
		            case 3://系统备份
		            	result = backupH2();
		            	break;
		            case 4://文件移动备份
		            {
		    			F h2dir = new F(ConfigUtil.getWorkPath(), "h2");
		    			if( h2dir.exists() )
		    			{
			    			F destDir = new F(ConfigUtil.getWorkPath(), "backup/cosh2/"+Tools.getFormatTime("yyyyMMddHHmm", System.currentTimeMillis()));
			    			String info = "数据文件目录["+Tools.bytesScale(IOHelper.getDirSize(h2dir))+"]";
			    			try {
								FileUtils.moveDirectoryToDirectory(h2dir, destDir, true);
			            		result = info+"成功移动备份到"+destDir.getPath();
			            		h2dir.mkdirs();
							} catch (IOException e) {
			            		result = info+"移动备份出现到异常"+e;
							}
		    			}
		    			else{
		    	    		result = "H2数据文件目录("+h2dir.getPath()+")不存在,请确认您是否删除了该目录.";
		    			}
		            	break;
		            }
		            case 5://文件拷贝备份
		            {
		    			F h2dir = new F(ConfigUtil.getWorkPath(), "h2");
		    			if( h2dir.exists() )
		    			{
			    			F destDir = new F(ConfigUtil.getWorkPath(), "backup/cosh2/"+Tools.getFormatTime("yyyyMMddHHmm", System.currentTimeMillis()));
			    			String info = "数据文件目录["+Tools.bytesScale(IOHelper.getDirSize(h2dir))+"]";
			    			try {
								FileUtils.copyDirectoryToDirectory(h2dir, destDir);
			            		result = info+"成功拷贝到"+destDir.getPath();
							} catch (IOException e) {
			            		result = info+"拷贝出现到异常"+e;
							}
		    			}
		    			else{
		    	    		result = "H2数据文件目录("+h2dir.getPath()+")不存在,请确认您是否删除了该目录.";
		    			}
		            	break;
		            }
		            case 6://激活数据库
	        			notifyStandby("127.0.0.1", h2Server.getPort());
			    		result = this.getDatabaseStatus();
		            	break;
		            case 10://查看数据库状态
			    		result = this.getDatabaseStatus();
		            	break;
		            case 7:{
		            	//查自动备份是否存在
		            	F f = new F(ConfigUtil.getWorkPath(), "data/h2/autobackup");
		            	result = f.exists()?Tools.getFormatTime("yyyy-MM-dd HH:mm", f.lastModified()):"";
		            	break;
		            }
		            case 8://启动自动备份数据文件
			    		String h2Cluster = ConfigUtil.getString("cos.database.h2", "");
			    		if( "standby".equalsIgnoreCase(h2Cluster) ){
			    			result = "当前数据库是以备服务器工作模式运行，无需启动自动备份";
			    		}
			    		else{
			    			F f = new F(ConfigUtil.getWorkPath(), "data/h2/autobackup");
			            	if( !f.exists() ){
			            		try {
									f.createNewFile();
									Calendar c = Calendar.getInstance();
									c.set(Calendar.HOUR, 0);
									f.setLastModified(c.getTimeInMillis());
									long t = f.lastModified()+Tools.MILLI_OF_DAY;
						    		result = "自动备份备正式启动，首次备份时间将在"+Tools.getFormatTime("yyyy-MM-dd HH:mm", t);
								} catch (IOException e) {
						    		result = "启动自动备份出现失败，因为备份配置文件创建出现异常"+e;
								}
			            	}
			            	else{
			            		long t = f.lastModified()+Tools.MILLI_OF_DAY;
					    		result = "自动备份备已经启动，下次备份时间将在"+Tools.getFormatTime("yyyy-MM-dd HH:mm", t);
			            	}
			    		}
		            	break;
		            case -8://停止子自动备份数据文件
		            	F f = new F(ConfigUtil.getWorkPath(), "data/h2/autobackup");
		            	if( f.exists() ){
		            		if( f.delete() ){
					    		result = "自动备份备取消成功";
		            		}
		            		else{
		            			result = "自动备份备取消失败，因为备份配置文件删除失败，可能因为其他程序占用该文件或者权限不允许";
		            		}
		            	}
		            	else{
				    		result = "自动备份备已经取消";
		            	}
		            	break;
		            }
		            return result;
				}
	        };
	        if( H2Switcher )
	        {
	        	DatabaseChecker checker = new DatabaseChecker();
	        	checker.start();
				synchronized(manager){
					try{
						manager.wait();
					}
					catch(Exception e){
					}
				}
		        Log.msg("Finish to startup manager(database:"+manager.isDatabaseStandby()+").");
	        }
	        manager.start();
		}
		catch(Exception e)
		{
			Log.err("Failed to start manager for ", e);
			System.exit(2);
		}
    }
    
    /**
     * 执行数据库检查 ，同时备份数据库
     * @author think
     *
     */
    class DatabaseChecker extends Thread{
    	public void run(){
    		Connection connection = null;
			Statement statement = null;
			ResultSet result = null;
			long lastClear = 0;
			while(!closed){
				try
				{
					if( h2Server.isRunning(true) ){
						if( h2Standby )
						{
							if( connection == null ){
						        String jdbcDriver = ConfigUtil.getString("cos.jdbc.driver", "");
						        String jdbcUrl = ConfigUtil.getString("cos.jdbc.url", "");
					            Class.forName(jdbcDriver);
					            connection = DriverManager.getConnection(jdbcUrl ,"sa", ""); 
							}
							if( connection != null ){
								statement = connection.createStatement();
								result = statement.executeQuery("SELECT count(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME in ('TB_USER','TB_NOTIFIES')");  
								result.next();
								int count = result.getInt(1);
								h2Ok = count > 0;
								if( h2Ok ){
									h2StandbyInfo = "数据库表已加载";
					    			F f = new F(ConfigUtil.getWorkPath(), "data/h2/autobackup");
					    			if( f.exists() && System.currentTimeMillis() - f.lastModified() > Tools.MILLI_OF_DAY ) {
					    				Calendar now = Calendar.getInstance();
					    				if( now.get(Calendar.HOUR_OF_DAY) == 0 &&
					    					now.get(Calendar.MINUTE) > 50 ){
					    					Log.msg("Backup the files of database after "+Tools.getFormatTime("yyyy-MM-dd HH:mm", f.lastModified()));
					    	    			F h2dir = new F(ConfigUtil.getWorkPath(), "h2");
					    	    			if( h2dir.exists() )
					    	    			{
					    		    			F destDir = new F(ConfigUtil.getWorkPath(),
					    		    				"backup/cosh2/"+Tools.getFormatTime("yyyyMMddHHmm", System.currentTimeMillis()));
					    		    			FileUtils.copyDirectoryToDirectory(h2dir, destDir);
					    		    			Log.msg("Succeed to backup to "+destDir.getPath());
					    		    			now.set(Calendar.HOUR_OF_DAY, 0);
					    		    			f.setLastModified(now.getTimeInMillis());
					    	    			}
					    	    			else{
					    	    				Log.err("Not found the dir of h2.");
					    	    			}
					    				}
					    			}
								}
								else{
									h2StandbyInfo = "数据库表还未加载";
								}
							}
							else{
								h2Ok = false;
								h2StandbyInfo = "无法建立数据库连接";
							}
						}
						else{
							h2StandbyInfo = "数据库待信号激活...";
						}
					}
					else{
						h2StandbyInfo = "数据库未启动";
					}
				}
				catch (Exception e)
				{
					h2StandbyInfo = "数据库服务异常"+e.getMessage();
					h2Ok = false;
		        	if( connection != null )
						try {
							connection.close();
							connection = null;
						} catch (SQLException e1) {
						}
				}
		        finally
		        {
					try
					{
			        	if( result != null ) result.close();
			        	if( statement != null ) statement.close();
					}
					catch (SQLException e)
					{
						Log.err(e);
					}
		        }
				if( System.currentTimeMillis() - lastClear > Tools.MILLI_OF_DAY ){
					lastClear = System.currentTimeMillis();
					F cosh2Dir = new F(ConfigUtil.getWorkPath(), "backup/cosh2/");
					F[] args = cosh2Dir.listFiles();
					if( args != null ){
						final long d8 = 8*Tools.MILLI_OF_DAY;
						for(F f : args){
							if( f.isDirectory() ){
								long t = lastClear - f.lastModified();
								if( t > d8 ){//超过8天
									IOHelper.deleteDir(f);
								}
							}
						}
					}
				}
				synchronized(manager){
					try{
						manager.notify();
						manager.wait(15000);
					}
					catch(Exception e){
					}
				}
			}
        	if( connection != null )
				try {
					Log.msg("Close the connection of database...", true);
					connection.close();
					Log.msg("The connection of database closed.", true);
				} catch (SQLException e1) {
				}
    	}
    }
    /**
     * 按照集群模式
     */
    private String setupH2Cluster(String urlTarget){
    	String result = "";
        String urlSource = "127.0.0.1:"+ConfigUtil.getString("h2.tcp.port", "9092");
        /**
         * Creates a cluster.
         *
         * @param urlSource the database URL of the original database
         * @param urlTarget the database URL of the copy
         * @param user the user name
         * @param password the password
         * @param serverList the server list
         */
    	if( !urlTarget.isEmpty() && !urlSource.equals(urlTarget) )
    	{
    		String target = urlTarget;
        	String serverList = urlSource+","+urlTarget;
        	urlSource = "jdbc:h2:tcp://"+urlSource+"/../h2/cos";
        	urlTarget = "jdbc:h2:tcp://"+urlTarget+"/../h2/cos";
        	String user = "sa";
        	String password = "";
        	Log.msg("Setup cluster of h2 from "+serverList+" ...", true);
        	CreateCluster h = new CreateCluster();
        	Log.msg("Initialzie CreateCluster.", true);
        	try{
        		long ts = System.currentTimeMillis();
        		h.setOut(new PrintStream(System.out));
        		h2Standby = false;
        		h.execute(urlSource, urlTarget, user, password, serverList);
        		ts = System.currentTimeMillis() - ts;
        		Log.msg("Succeed to setup the cluster and notify "+target+", delay "+Tools.getDuration(ts), true);
        		result = "启动主控引擎H2数据库集群("+serverList+")成功，耗时"+Tools.getDuration(ts);
    			notifyStandby(target);
        	}
        	catch(Exception e){

    			Log.err("Failed to setup the cluster of h2 for exception: ", e);
        		System.out.println("Failed to setup the cluster of h2 for "+e);
        		result = "启动主控引擎H2数据库集群("+serverList+")出现异常: "+e;
        	}
        	h2Standby = true;
    	}
    	else
    	{
    		result = "启动主控引擎H2数据库集群失败，因为集群源地址("+urlSource+")和目的地址("+urlTarget+")没有正确配置.";
    		Log.war("Failed to setup the cluster for urlSource is "+urlSource+", urlTarget is "+urlTarget, true);
    	}
    	return result;
    }
    /**
     * 升级核心数据库，先执行备份，然后再把数据导出
     */
    /**
     * 执行SQL语句的安装
     */
    private void setupH2Sql()
    {
		Connection connection = null;
		Statement statement = null;
		ResultSet result = null;
        String jdbcDriver = ConfigUtil.getString("cos.jdbc.driver", "");
        String jdbcUrl = ConfigUtil.getString("cos.jdbc.url", "");
//        String jdbcUrl = "jdbc:h2:tcp://localhost:"+h2Server.getPort()+"/../h2/cos";
		try
		{
            Class.forName(jdbcDriver);
        	Log.msg("Setup connection form "+jdbcUrl+" ...", true);
            connection = DriverManager.getConnection(jdbcUrl ,"sa", ""); 
        	Log.msg("Succeed to setup connection "+connection.getCatalog(), true);
            statement = connection.createStatement();
        	Log.msg("Check the valid of database by query ...", true);
            result = statement.executeQuery("SELECT count(*) FROM INFORMATION_SCHEMA.TABLES  WHERE TABLE_NAME='TB_USER'");  
            result.next();
        	Log.msg("Succeed to check the valid of database.", true);
        	if( result.getInt(1) == 0 )
        	{
        		this.setupSql("h2", statement);
        	}
        	else
        	{
        		this.setupSqlUpgrade("h2", statement);
        	}
		}
		catch (Exception e)
		{
			Log.err("Failed to setup sql from "+jdbcUrl, e);
		}
        finally
        {
        	if( result != null )
				try
				{
					result.close();
				}
				catch (SQLException e)
				{
				}  
        	if( statement != null )
				try
				{
					statement.close();
				}
				catch (SQLException e)
				{
				} 
        	if( connection != null )
				try
				{
					connection.close();
				}
				catch (SQLException e)
				{
				}
        	Log.msg("Finish to setup the sql of h2.", true);
        }
    }
    
    /**
     * 安装H2数据库脚本
     * @param statement
     * @return
     * @throws Exception
     */
    private void setupSql(String type, Statement statement) throws Exception
    {
    	StringBuffer sb = new StringBuffer();
        sb.append("Setup the sql of "+type+":");
        InputStream is = null;
        try
        {
        	is = this.getClass().getResourceAsStream("/"+type+".sql");
    		String sqlscript = new String(IOHelper.readAsByteArray(is), "UTF-8");
    		String sqls[] = sqlscript.split(";");
    		for( String sql : sqls)
    		{
    			sql = sql.trim();
    			if( sql.isEmpty() ) continue;
    			int r = statement.executeUpdate(sql);
    			sb.append("\r\n\trows("+r+").");
    		}
            F file = new F("../"+type+"/_upgrade");
    		IOHelper.writeSerializable(file, SetupSql.length);
    		Log.msg(sb.toString());
        }
        catch(Exception e)
        {
    		Log.err(sb.toString(), e);
        }
        finally
        {
        	if( is != null ) is.close();
        }
    }
    
    /**
     * SQL脚本升级
     * @param statement
     * @throws Exception
     */
    private boolean doThin;
    private void setupSqlUpgrade(String type, Statement statement) throws Exception
    {
        F file = new F("../"+type+"/_upgrade");
		Integer I = (Integer)IOHelper.readSerializableNoException(file);
		I = I==null?0:I;
		if( SetupSql.length > I )
		{
    		//ALTER TABLE 【表名字】 CHANGE 【列名称】【新列名称（这里可以用和原来列同名即可）】 BIGINT NOT NULL  COMMENT '注释说明'
			for(int i = I; i < SetupSql.length; i++){
				statement.addBatch(SetupSql[i]);
				if(!doThin){
					doThin = SetupSql[i].startsWith("DROP");
				}
			}
    		int results[] = statement.executeBatch();
			StringBuffer sb = new StringBuffer("Succeed to execute the sql of upgrade at "+I+"("+file.getAbsolutePath()+").");
    		for( int i = I; i < results.length; i++ )
    			sb.append("\r\n\t"+SetupSql[i]+"\r\n\t\trows("+results[i]+").");

    		IOHelper.writeSerializable(file,SetupSql.length);
    		Log.msg(sb.toString());
		}
		else
		{
    		Log.msg("Not need to setup the sql of upgrade("+I+").");
		}
    }
    
    /**
     * 停止所有子程序有效运行
     */
    private boolean closed = false;
    public void shutdown(boolean exit)
    {
    		if( closed ) return;
    		closed = true;
    		Log.print( "Stop the controler of cos(identity:%s)", manager.identity );
    		manager.close();
    		long waitCloseCount = 0;
    		synchronized (manager)
    		{
    			manager.notify();
	    		while( !manager.isClosed() && waitCloseCount < 10 )
	    		{
	    			try
	    			{
	    				manager.wait( 10000 );
	    				waitCloseCount += 1;
	    				Log.war("Wait to stop the manager("+manager.status()+") after "+waitCloseCount+" seconds.");
	    			}
	    			catch( InterruptedException ex )
	    			{
	    			}
	    		}
    		}
    		if( h2Server != null ){
    			waitCloseCount = System.currentTimeMillis();
    			Log.msg( "Stop h2." );
    			h2Server.stop();
    			Log.msg( String.format("Succeed to stop h2 after %s ms.", System.currentTimeMillis() - waitCloseCount), true );
    		}
        	Log.msg( "Stop out.", true );
            Log.getInstance().close();
            System.out.println("Bye.");
    }

	/**
	 * 接受Wrapper发的指令
	 */
	public void run()
	{
		try
		{
			Thread.sleep(700);
//			InputStream in = System.in;
			Log.msg("Begin receive the command from console...");
    		BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( System.in ) );
    		String command = null;
    		long countHeartbeat = 0;
            while( ( command = bufferedReader.readLine().trim() ) != null )
    		{
            	Log.msg("Receive the command("+countHeartbeat+") from wrapper:"+command);
            	countHeartbeat += 1;
				if( command.equalsIgnoreCase("exit") )
				{
					break;
				}
				else if( command.equalsIgnoreCase("status") )
				{
					System.out.println("@COS$ "+manager.status());
				}
    		}
            this.shutdown(true);
		}
		catch( Exception e )
		{
			Log.err("Failed to receive the command from console for exception:"+e);
		}
		System.exit(0);
	}
	public static String version()
	{
		return Versions[Versions.length-1][0];
	}
	
	public static String getVersion()
	{
		return Tools.replaceStr(Versions[Versions.length-1][0], ".",  "_");
	}
}
