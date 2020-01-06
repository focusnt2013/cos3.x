package com.focus.cos.web.common.listener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.data.Stat;
import org.h2.tools.Server;
import org.json.JSONObject;

import com.focus.cos.H2;
import com.focus.cos.control.WrapperShell;
import com.focus.cos.web.Version;
import com.focus.cos.web.common.COSConfig;
import com.focus.cos.web.common.Kit;
import com.focus.cos.web.common.PathFactory;
import com.focus.cos.web.common.Skin;
import com.focus.cos.web.common.SubprocessReader;
import com.focus.cos.web.common.ZKMgr;
import com.focus.cos.web.dev.service.ModulesMgr;
import com.focus.cos.web.login.service.ClearCookie;
import com.focus.cos.web.ops.service.MonitorMgr;
import com.focus.cos.web.ops.service.ProxyWatcher;
import com.focus.cos.web.ops.service.SecurityMgr;
import com.focus.cos.web.user.service.RoleMgr;
import com.focus.cos.web.user.service.UserMgr;
import com.focus.util.ConfigUtil;
import com.focus.util.IOHelper;
import com.focus.util.QuickSort;
import com.focus.util.Tools;
import com.focus.util.Zookeeper;

/**
 *  初始化COS的数据库与ZK（开发环境下），在配置需要的情况下
 * @author focus
 * @date 2010-01-01
 */
public class Initializer implements ServletContextListener
{
	private static final Log log = LogFactory.getLog(Initializer.class);
    private Server h2Server = null;
    private StringBuffer syslogtext = new StringBuffer();
	/**
	 * Tomcat关闭是会回调该函数
	 */
	public void contextDestroyed(ServletContextEvent arg0)
	{
		try
		{
			log.warn("Notify the wrapper of upgrader to abort.");
			if( upgradeWrapper != null )
			{
				upgradeWrapper.runing = false;
				synchronized(upgradeWrapper)
				{
					upgradeWrapper.notify();
				}
			}
			if( this.h2Process != null ) h2Process.destroy();
			if( this.cosApiProcess != null ) cosApiProcess.destroy();;
			if( this.zookeeperProcess != null ) zookeeperProcess.destroy();
			if( h2Server != null ) h2Server.stop();
		}
		catch (Exception e)
		{
		}
	}
	
	/**
	 * 初始化线程
	 * @author focus
	 *
	 */
	class Executer extends Thread
	{
		public void run()
		{
			PathFactory.getCfgPath();
			try
			{
				File skinsDir = new File(PathFactory.getWebappPath(), "skin/");
				File skinFile = new File(skinsDir, "config");
				String skinName = "defone";//缺省皮肤
				if( skinFile.exists() )
				{
					Skin skin = (Skin)IOHelper.readSerializableNoException(skinFile);
					if( skin != null )
					{
						skinName = skin.getName();
					}
				}
				File binDir = new File(PathFactory.getAppPath(), "bin");
				System.setProperty("user.dir", binDir.getAbsolutePath());
				if( Skin.changeSkin(skinName) )
				{//执行成功表示不是war，则执行其它加载
			        Iterator<?> itr = System.getProperties().keySet().iterator();
			        while(itr.hasNext())
			        {
			        	syslogtext.append("\r\n\t");
			        	String key = itr.next().toString();
			        	syslogtext.append(key);
			        	syslogtext.append("=");
			        	syslogtext.append(System.getProperty(key));
			        }
					/*启动自动升级程序*/
					upgradeWrapper = new UpgradeWrapper();
					upgradeWrapper.start();
					File shellpid = new File(PathFactory.getAppPath(), "bin/shell.pid");
					if( shellpid.exists() )
					{
				    	syslogtext.append("\r\nFound cos-server("+shellpid.getAbsolutePath()+") not need to startup test-handler.");
				    	setupApiPort();
					}
					else
					{
						int port = COSConfig.getLocalControlPort();
						if( ZKMgr.isZooKeeperStartup() )
						{
					    	syslogtext.append("\r\nFound exists zookeeper from "+port);
					    	setupApiPort();
						}
						else
						{
					    	syslogtext.append("\r\nNot found exists zookeeper from "+port+" to startup tester.");
							startupTestZooKeeper();
							startupTestH2();
							startupTestCOSApi();
						}
					}
					log.info(syslogtext);
				}
			}
			catch(Exception e)
			{
				log.error("Failed to initialized ", e);
			}
		}
	}
	
	/**
	 * 从config.properties文件中加载API的端口
	 * @throws Exception
	 */
	private void setupApiPort() throws Exception
	{
		String api = System.getProperty("cos.api", "");
		if( !api.isEmpty() ) return;
    	File configFile = new File(PathFactory.getCfgPath(), "config.properties");
    	if( configFile.exists() )
    	{
        	Properties config = new Properties();
        	config.load(new FileInputStream(configFile));
        	api = config.getProperty("cos.api", "127.0.0.1:9079");
			System.setProperty("cos.api", api);
			String[] args = Tools.split(api, ":");
			if( args.length == 2 )
			{
				System.setProperty("cos.api.port", args[1]);
			}
    		syslogtext.append("\r\nSetup the api for your service "+api+".");
    	}
    	else
    	{
    		syslogtext.append("\r\nInvalid IDE for not found "+configFile.getAbsolutePath()+".");
    	}
	}

	/**
	 * 构建各种cos需要的ZK目录
	 */
	public void preloadCOSFirstZookeeperNodes()
	{
		Zookeeper zookeeper = null;
		try
		{
			zookeeper = ZKMgr.getZookeeper();
			if( zookeeper != null ){
				String zkpath = "/cos";
				Stat stat = zookeeper.exists(zkpath, false); 
				if( stat == null)
				{
					zookeeper.create(zkpath, "云架构开放式应用服务框架".getBytes("UTF-8"));
				}
				zkpath = "/cos/config";
				stat = zookeeper.exists(zkpath, false); 
				if( stat == null)
				{
					zookeeper.create(zkpath, "云架构开放式应用服务框架配置目录".getBytes("UTF-8"));
				}
				zkpath = "/cos/data";
				stat = zookeeper.exists(zkpath, false); 
				if( stat == null)
				{
					zookeeper.create(zkpath, "云架构开放式应用服务框架数据目录".getBytes("UTF-8"));
				}
				zkpath = "/cos/login";
				stat = zookeeper.exists(zkpath, false); 
				if( stat == null)
				{
					zookeeper.create(zkpath, "云架构开放式应用服务框架系统用户登录目录".getBytes("UTF-8"));
				}
				log.info("Succeed to initialize the default nodes of cos from "+zookeeper);
			}
		}
		catch(Exception e)
		{
			log.error("Failed to initialize the default nodes of cos for exception from "+zookeeper, e);
		}
		File dir = new File(PathFactory.getDataPath(), "zkdat");
		File[] files = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".zip");
			}
		});
		if( files == null ){
			return;
		}
		log.info(String.format("Found %s files to build the nodes of zookeeper.", files.length));
		for(File file:files){
			ZipFile zip = null;
			StringBuffer sb = new StringBuffer();
			try{
		        zip = new ZipFile(file);
		        Enumeration<?> entries = zip.entries();
		        while(entries.hasMoreElements())
		        {
		            ZipEntry entry = (ZipEntry)entries.nextElement();
	            	String path = entry.getName();
	            	if(path.startsWith("_")){
	            		path = path.substring(1);
	            	}
	            	path = Kit.unicode2Chr(path);
	            	sb.append("\r\n\t"+path);
	            	byte[] payload = IOHelper.readAsByteArray(zip.getInputStream(entry));
	            	sb.append("\t"+payload.length);
	            	Stat stat = zookeeper.exists(path);
	            	if( stat != null ){
	            		sb.append("\trecoved.");
	            		stat = zookeeper.setData(path, payload, stat.getVersion());
	            	}
	            	else{
	            		sb.append("\tcreated.");
	            		zookeeper.create(path, payload);
	            	}
		        }
		        sb.insert(0, String.format("Succeed to build %s nodes of zookeeper from %s", zip.size(), file.getPath()));
			}
			catch(Exception e0){
		        sb.insert(0, String.format("Failed to build the nodes of zookeeper from %s for %s", file.getPath(), e0.getMessage()));
			}
			finally
			{
				if( zip != null )
					try
					{
						zip.close();
					}
					catch (IOException e) {
					}
				boolean r = file.delete();
				sb.append(String.format("\r\nDeleted %s", r));
			}
			log.info(sb.toString());
		}
	}
	/**
	 * 升级Wrapper程序
	 * @author focus
	 *
	 */
	class UpgradeWrapper extends Thread
	{
		private boolean runing = false;
		public void run()
		{
			try
			{
				synchronized(this)
				{
					log.warn("Wait to upgrade.");
					this.wait(7000);
				}
				String ip = Tools.getLocalIP();
				int port = COSConfig.getLocalControlPort();
				File dir = new File(PathFactory.getDataPath(), "zkbak/"+ip+"_"+port);
				if( !dir.exists() ){
					dir.mkdirs();
				}
			}
			catch (Exception e)
			{
			}
			ZKMgr zkmgr = new ZKMgr();
			preloadCOSFirstZookeeperNodes();
			ModulesMgr.rebuildModules();
			Version.buildTimeline();
//			CmpCfgMgr.buildCmpCfg();//将原来用文件存储的组件配置转换成保存到ZK中
//			CmpCfgMgr.buildModulesXml();
			MonitorMgr.buildMonitor();
			zkmgr.setCustomWatcher(new ProxyWatcher());
			SecurityMgr.buildDefaultSecurity();
//			File path = new File(PathFactory.getCfgPath(), "sys");
//			if( path.exists() )
//			{
//				SysCfgMgr.buildZkdata(path, SysCfgMgr.id);
//				SftCfgMgr.buildZkdata(path, SftCfgMgr.id);
//				path.delete();
//			}
//			RoleMgr.buildRoles();//构建角色
			UserMgr.buildUser();//重构用户数据
			RoleMgr.loadRoleNames();
			runing = true;
			int lasthour = -1, hour = 0;
			while(runing)
			{
				ClearCookie.execute();//利用升级现场顺带清理Cookie
//				String tag = PathFactory.getWebappPath().getAbsolutePath().toLowerCase();
//				if( !tag.startsWith("d:\\focusnt\\cos\\trunk\\ide") &&
//					!tag.startsWith("d:\\focusnt\\report\\trunk\\ide") )
//				{
				
				Calendar c = Calendar.getInstance();
				hour = c.get(Calendar.HOUR_OF_DAY);
				if( lasthour == -1 ) lasthour = hour;
				if( 4 == hour ) {
					//每天凌晨4点执行升级检查
					Thread thread = new Thread(new Version());
					thread.start();
				}
				
				if( lasthour != hour ){
					lasthour = hour;
					File dir = new File(PathFactory.getDataPath(), "zkbak");
					File[] dirs = dir.listFiles(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							return true;
						}
					});
					StringBuilder sb = new StringBuilder();
					if( dirs != null ){
						sb.append(String.format("Found %s backups of zookeeper:", dirs.length));
						for(File zkdir : dirs){
							String[] args = Tools.split(zkdir.getName(), "_");
							if( args.length != 2 || !Tools.isNumeric(args[1]) || zkdir.isFile() ){
								sb.append("\r\n\tThe file("+zkdir.getName()+") is not the config of backup.");
								continue;
							}
							String ip = args[0];
							int port = Integer.parseInt(args[1]);
							String time = Tools.getFormatTime("yyyy_MM_dd_HH", System.currentTimeMillis());
							sb.append(String.format("\r\n\t[%s] Snapshot %s.",
								zkdir.getName(), Tools.getFormatTime("yyyy-MM-dd HH", System.currentTimeMillis())));
							//执行ZK备份
							File zipfile = new File(zkdir, time+".zip");
							ZipOutputStream zos = null;
							Zookeeper zookeeper = null;
							long ts = System.currentTimeMillis();
							try{
								//每小时做一个ZK镜像，最多保存48个镜像文件
								File[] zipfiles = zkdir.listFiles(new FilenameFilter() {
									@Override
									public boolean accept(File dir, String name) {
										return name.endsWith(".zip");
									}
								});
								if( zkdir.length() > 48 ){
									QuickSort sorter = new QuickSort() {
										@Override
										public boolean compareTo(Object sortSrc, Object pivot) {
											return ((File)sortSrc).getName().compareTo(((File)pivot).getName()) > 0;
										}
									};
									sorter.sort(zipfiles);
									for(int i = 48; i < zipfiles.length; i++){
										boolean r = zipfiles[i].delete();
										sb.append(String.format("\r\n\t\tDiscard the snapshot of %s result %s.", zipfiles[i].getName(), r));
									}
								}
								zookeeper = Zookeeper.getInstance(ip, port);
								sb.append(String.format("\r\n\t\tSucceed to connect %s.", zookeeper.toString()));
								zos = new ZipOutputStream(new CheckedOutputStream(new FileOutputStream(zipfile), new CRC32()));
								int count = this.exportZookeeper(zookeeper, "/", zos);
								zos.close();
								sb.append(String.format("\r\n\t\tSucceed to backup %s nodes to %s duration %s.",
									count, zipfile.getPath(), System.currentTimeMillis() - ts));
								sb.append(String.format("\r\n\t\tThe size of file is %s", zipfile.length()));
							}
							catch(Exception e){
								log.error("", e);
								sb.append(String.format("\r\n\t\tFailed to backup for %s.", e.getMessage()));
								if( zos != null ){
									try {
										zos.close();
									} catch (IOException e1) {
										sb.append(String.format("\r\n\t\tFailed to close the outputstream of zip for %s.", e.getMessage()));
									}
									zipfile.delete();
								}
							}
							finally{
								if( zookeeper != null ){
									zookeeper.close();
								}
							}
						}
					}
					else{
						sb.append("Not found zookeeper need to snapshot.");
					}
					log.info(sb.toString());
				}
				synchronized(this)
				{
					try
					{
						Version.check(syslogtext.toString());
						log.warn("Wait to check upgrade at "+Tools.getFormatTime("MM-dd HH:mm:ss", Version.NextUpgrade)+".");
						this.wait(Tools.MILLI_OF_HOUR);
					}
					catch (InterruptedException e)
					{
					}
				}
			}
			log.info("Finish upgrade wrapper.");
		}

		/**
		 * 导出配置数据
		 * @param zkpath
		 * @param zos
		 * @param zookeeper
		 * @throws Exception 
		 */
		public int exportZookeeper(Zookeeper zookeeper, String zkpath, ZipOutputStream zos) throws Exception{
			Stat stat = zookeeper.exists(zkpath);
			if( stat == null ) {
				return 0;
			}
			int c = 1;
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
			}
			List<String> list = zookeeper.getChildren(zkpath);
			if( zkpath.equals("/") ){
				zkpath = "";
			}
			for(String name : list){
				c += exportZookeeper(zookeeper, zkpath+"/"+name, zos);
			}
			return c;
		}
	}
	/**
	 * 上下文初始化
	 */
	//升级守护程序
	private UpgradeWrapper upgradeWrapper;
//	private boolean noide = false;//是否是IDE开发环境
	public void contextInitialized(ServletContextEvent event)
	{
		this.syslogtext.append("\r\n==========================================================");
		this.syslogtext.append("\r\nCOS Portal "+Version.getValue());
		this.syslogtext.append("\r\n\tCopyright (C) 2008-2019 Focusnt.  All Rights Reserved.");
    	try
		{
    		ConfigUtil.setAppPath(PathFactory.getAppPath());
    		syslogtext.append("\r\n\tThe name of service is "+ConfigUtil.getString("service.name", "cos"));
    		syslogtext.append("\r\n\tThe desc of service is "+ConfigUtil.getString("service.desc", "可是系统IDE集成开发"));
    		syslogtext.append("\r\n\tThe version of java is "+System.getProperty("java.version"));
    		String ip = Tools.getLocalIP();
    		syslogtext.append("\r\n\tThe ip of localhost is "+ip);
		}
		catch (Exception e)
		{
			syslogtext.append("\r\n\t"+e.toString());
		}
    	syslogtext.append("\r\n==========================================================");
		Executer executer = new Executer();
		executer.start();
	}

	/**
	 * 强行修改jdbc的配置参数
	 * @param jdbcFile
	private void forceSetH2JdbcMode(File jdbcFile)
	{
		try
		{
			BufferedReader br = new BufferedReader(new java.io.InputStreamReader(new FileInputStream(jdbcFile)));
			String line = null;
			StringBuffer context = new StringBuffer();
			while((line = br.readLine()) != null )
			{
				if( line.startsWith("jdbc.url=jdbc:h2:") )
					context.append("\r\njdbc.url=jdbc:h2:tcp://localhost/../h2/cos");
				else
					context.append("\r\n"+line);
			}
			IOHelper.writeFile(jdbcFile, context.toString().getBytes());
			log.info("Succeed to set jdbc.properties.");
			br.close();
		}
		catch(Exception e)
		{
			log.error("Failed to load webwork.properties for "+e);
		}
	}
	 */
	/**
	 * 得到H2数据库的连接
	private Connection getH2Connection()
	{
		Properties jdbcConfig = new Properties();
    	URL url = Initializer.class.getClassLoader().getResource("/");
    	String jdbcUrl = null;
        try 
        {  
        	File jdbcFile = new File(url.getFile(), "config/jdbc.properties");
	    	jdbcConfig.load(new FileInputStream(jdbcFile));
	    	jdbcUrl = jdbcConfig.getProperty("jdbc.url");
	    	syslogtext.append("\r\nReady to setup connectoin from "+jdbcUrl);
			if( !jdbcUrl.startsWith("jdbc:h2:") ) return null;
			if( !jdbcUrl.startsWith("jdbc:h2:tcp:") && jdbcUrl.startsWith("jdbc:h2:../") )//如果是指定完整路径的配置，就不强行修改链接配置
			{
				forceSetH2JdbcMode(jdbcFile);
				jdbcUrl = "jdbc:h2:tcp://localhost/../h2/cos";
			}
			else if( Tools.countChar(jdbcUrl, ',') > 0 )
			{
				syslogtext.append("\r\nFound the h2 is the mode of cluster from jdbc "+jdbcUrl);
				return null;
			}
        }
        catch(Exception e)
        {
        	syslogtext.append("\r\nFailed to get the connection of h2 for exception "+e);
        	log.error("Failed to get the connection of h2", e);
        	return null;
        }
		Connection connection = null;
        int count = 0;
		while(connection==null)
	        try
	        {
	            Class.forName("org.h2.Driver");
	        	count += 1;
	            connection = DriverManager.getConnection(jdbcUrl ,jdbcConfig.getProperty("jdbc.username"), jdbcConfig.getProperty("jdbc.password")); 
	        }
	        catch(Exception e)
	        {
	        	if( count == 2 )
	        	{
		        	log.error("Not come here for failed to setup connection from "+jdbcUrl);
	        		return null;
	        	}
	        	log.warn("Not found connection from "+jdbcUrl+" for "+e);
	        	try
				{
					this.h2Server = Server.createTcpServer(new String[] { "-tcpPort", System.getProperty("h2.tcp.port", "9092"), "-tcpAllowOthers" }).start();
					log.info("Succeed to startup h2("+h2Server.getPort()+","+h2Server.getStatus()+","+h2Server.getService().getType()+")");
		        }
				catch (SQLException e1)
				{
					log.error("Failed to setup h2 database.", e1);
					return null;
				}
	        }
    	syslogtext.append("\r\nSucceed to setup connection from "+jdbcUrl);
	    log.info("Succeed to setup connection from "+jdbcUrl);
		//H2配置，表示缺省加载嵌入式数据库
//		jdbc.url=jdbc:h2:../h2/cos
//		##Config for COS-SHELL to startup h2 database by tcp below
//		#jdbc.url=jdbc:h2:tcp://localhost/../h2/cos
//		#jdbc.url=jdbc:h2:tcp://115.29.243.100/../h2/cos
	    if( h2Server != null )
	    {
			String dbpath = jdbcUrl.substring("jdbc:h2:".length());
			if(dbpath.startsWith("tcp://"))
			{
				dbpath = dbpath.substring("tcp://".length());
				int i = dbpath.indexOf('/') + 1;
				dbpath = dbpath.substring(i);
			}
			if( dbpath.endsWith("cos") )
			{
				dbpath = dbpath.substring(0, dbpath.length()-3)+"_upgrade";
			}
	    }
	    else
	    {
			h2upfile = new File(PathFactory.getAppPath(), "h2/_upgrade");
	    }
		File path = h2upfile.getParentFile();
		if( path.exists() )
		{
	    	syslogtext.append("\r\nFound h2 database path:"+path.getAbsolutePath());
			log.info("Found h2 database path:"+path.getAbsolutePath());
		}
		else
		{
	    	syslogtext.append("\r\nNot found h2 database path:"+path.getAbsolutePath());
			log.warn("Not found h2 database path:"+path.getAbsolutePath());
		}
		return connection;
	}*/
	/**
	 * 检查H2数据库是否启动
	 * @throws Exception
	 */
	private Process h2Process = null;
	public void startupTestH2() throws Exception
	{
		Connection connection = null;
		Statement statement = null;
		ResultSet result = null;
        try 
        {
        	URL url = Initializer.class.getClassLoader().getResource("/");
        	String classespath = url.getFile();
	    	File h2jar = new File(classespath, "../lib/h2.jar");
	    	h2Process = H2.startup(9092, 0, classespath, h2jar.getAbsolutePath());
//	    	h2Process = new H2(9092, classespath, h2jar.getAbsolutePath());
//	    	h2Process.start();
    		Properties jdbcConfig = new Properties();
        	File jdbcFile = new File(url.getFile(), "config/jdbc.properties");
	    	jdbcConfig.load(new FileInputStream(jdbcFile));
	    	String jdbcUrl = jdbcConfig.getProperty("jdbc.url");
            Class.forName("org.h2.Driver");
	    	System.setProperty("cos.jdbc.url", jdbcUrl);
	        System.setProperty("cos.jdbc.driver", jdbcConfig.getProperty("jdbc.driverClass"));
	        System.setProperty("cos.jdbc.user", jdbcConfig.getProperty("jdbc.username"));
	        System.setProperty("cos.jdbc.password", jdbcConfig.getProperty("jdbc.password"));
	        
            connection = DriverManager.getConnection(jdbcUrl ,jdbcConfig.getProperty("jdbc.username"), jdbcConfig.getProperty("jdbc.password")); 
        	if( connection == null ) return;
            statement = connection.createStatement();
            result = statement.executeQuery("SELECT count(*) FROM INFORMATION_SCHEMA.TABLES  WHERE TABLE_NAME='TB_USER'");  
            result.next();
	    	syslogtext.append("\r\nSetup test-h2 from "+jdbcUrl);
	    	File h2upfile = new File(PathFactory.getAppPath(), "h2/_upgrade");
            if( result.getInt(1) == 0 )
        	{//表没有创建
	    		File sqlFile = new File(url.getFile(), "h2.sql");
	    		if( !sqlFile.exists() )
	    		{
	    			syslogtext.append("\r\nNot found h2.sql from "+sqlFile.getAbsolutePath());
	    			return;
	    		}
	    		syslogtext.append("\r\nExecute h2.sql:");
	    		String sqlscript = new String(IOHelper.readAsByteArray(sqlFile), "UTF-8");
	    		String sqls[] = sqlscript.split(";");
	    		for( String sql : sqls)
	    			statement.addBatch(sql.trim());
	    		int results[] = statement.executeBatch();
	    		for( int i = 0; i < results.length; i++ )
	    		{
	    			String sql = sqls[i];
	    			int r = results[i];
	    			syslogtext.append("\r\n\t"+sql+"\r\n\trows("+r+").");
	    		}
	    		syslogtext.append("\r\nSucceed to execute the creat of h2.sql.");
	    		IOHelper.writeSerializable(h2upfile, WrapperShell.SetupSql.length);
        	}
        	else
        	{
        		Integer I = (Integer)IOHelper.readSerializableNoException(h2upfile);
        		I = I==null?0:I;
        		if( WrapperShell.SetupSql.length > I )
        		{
	        		//ALTER TABLE 【表名字】 CHANGE 【列名称】【新列名称（这里可以用和原来列同名即可）】 BIGINT NOT NULL  COMMENT '注释说明'
        			syslogtext.append("\r\nSucceed to execute the sql of h2.upgrade from version("+WrapperShell.version()+") at "+I+"("+h2upfile.getAbsolutePath()+").");
	    			for(int i = I; i < WrapperShell.SetupSql.length; i++)
		    			statement.addBatch(WrapperShell.SetupSql[i]);
		    		int results[] = statement.executeBatch();
		    		for( int i = 0; i < results.length; i++ )
		    			syslogtext.append("\r\n\t"+WrapperShell.SetupSql[i]+"\r\n\t\trows("+results[i]+").");
		    		IOHelper.writeSerializable(h2upfile, WrapperShell.SetupSql.length);
        		}
        		else
        		{
        			syslogtext.append("\r\nNot found the upgrade of sql need to execute("+I+").");
        		}
        	}
        }
        catch (Exception e)
        {
        	syslogtext.append("\r\nFailed to startup the test database for exception "+e);
			log.error("Failed to load default database", e);
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
        }
	}
	
	/**
	 * 启动测试的CSOApi
	 */
	private Process cosApiProcess;
	private void startupTestCOSApi()
	{
		/*COSApiAgent cosApiAgent = new COSApiAgent(9079){
			@Override
			public void setApiPort(int port) {
        		log.info("Succeed to startup api-agent from port "+port);
			}

			@Override
			public void setApiProxy(byte[] payload, int length) {
		    	try
		    	{
			    	JSONObject proxy = new JSONObject(new String(payload, 0, length, "UTF-8"));
	        		log.info("Succeed to register api-proxy: "+proxy);
		    	}
		    	catch(Exception e)
		    	{
		    		log.error("Failed to register api-proxy for ", e);
		    	}
			}

			@Override
			public void setVersion(String version) {
        		log.info("The version of api-agent is "+version);
				
			}
        };
        cosApiAgent.start();*/
		System.setProperty("cos.api.port", "9079");
		Thread thread = new Thread()
		{
		    public void run()
		    {
		    	try
		    	{
					int i = 0;
					String separator_file = System.getProperty("file.separator", "/");
			    	String separator_path = System.getProperty("path.separator", ";");
			    	String jdk = System.getProperty("java.home");
			    	if( !jdk.endsWith("bin"+separator_file+"java") )
			    		jdk += ""+separator_file+"bin"+separator_file+"java";
		        	URL url = Initializer.class.getClassLoader().getResource("/");
		        	String classespath = url.getFile();
			    	File weblibDir = new File(classespath, "../lib");
			    	File jettyjar = new File(PathFactory.getAppPath(), "lib/jetty.jar");


			    	StringBuffer cp = new StringBuffer();
			    	cp.append(classespath.replace("\\", "/"));
			    	cp.append(separator_path);
			    	cp.append(weblibDir.getAbsolutePath().replace("\\", "/")+"/*");
			    	cp.append(separator_path);
			    	cp.append(jettyjar.getAbsolutePath().replace("\\", "/"));
			    	ArrayList<String> startupCommands = new ArrayList<String>();
			    	startupCommands.add( i++, jdk );
			    	startupCommands.add( i++,  "-Xms16m" );
			    	startupCommands.add( i++,  "-Xmx64m" );
			    	startupCommands.add( i++,  "-XX:PermSize=8M" );
			    	startupCommands.add( i++,  "-XX:MaxPermSize=32m" );
			    	startupCommands.add( i++,  "-Dcos.jdbc.driver="+System.getProperty("cos.jdbc.driver", ""));
			    	startupCommands.add( i++,  "-Dcos.jdbc.url="+System.getProperty("cos.jdbc.url", "") );
			    	startupCommands.add( i++,  "-Dcos.jdbc.user="+System.getProperty("cos.jdbc.user", "") );
			    	startupCommands.add( i++,  "-Dcos.jdbc.password="+System.getProperty("cos.jdbc.password", "") );
			    	startupCommands.add( i++,  "-Dcos.api.port=9079" );
			    	startupCommands.add( i++,  "-Dcos.api=127.0.0.1:9079" );
			    	System.setProperty("cos.api.port", "9079");
			    	System.setProperty("cos.api", "127.0.0.1:9079");
			    	File cosIdentity = new File(PathFactory.getDataPath(), "identity");
			    	if( !cosIdentity.exists() )
			    	{
			    		syslogtext.append("\r\nNotf found the cod.indentity from "+cosIdentity.getAbsolutePath());
			    	}
			    	System.setProperty("cos.identity", cosIdentity.getAbsolutePath());
			    	startupCommands.add( i++,  "-Dcos.identity="+System.getProperty("cos.identity"));
		        	startupCommands.add( "-Duser.dir="+System.getProperty("user.dir") );
			    	startupCommands.add( "-cp" );
			    	startupCommands.add( cp.toString() );
			    	startupCommands.add("com.focus.cos.control.COSApi");
			    	startupCommands.add("COSApiTest");
			    	StringBuffer sb = new StringBuffer();
			    	for(String command : startupCommands)
			    	{
			    		sb.append("\r\n\t");
			    		sb.append(command);
			    	}
			    	ProcessBuilder pb = new ProcessBuilder( startupCommands );
			        //开启错误信息的流到标准输出流，在某种情况下由于错误输出流中的数据没有被读取，进程就不会结束
			    	cosApiProcess = pb.start();
		    		Thread thread = new Thread()
		    		{
		    		    public void run()
		    		    {
		    	            int method = -1;
		    	            int len = 0;
		    		    	InputStream is = cosApiProcess.getInputStream();
		    		        try
		    		        {
		    	            	byte[] payload = new byte[65536];
		    	                while( ( method = is.read() ) != -1 )
		    	                {
		    	                	switch(method)
		    	                	{
		    	                	case 0://标题
		    	                		len = is.read();
		    	                		IOHelper.read(is, payload, len);
		    	                		System.out.println("COSApiAgent version is "+new String(payload, 0, len));
		    	                		IOHelper.read(is, payload, 4);
		    	                		System.out.println("COSApiAgent port is "+Tools.bytesToInt(payload, 0, 4));
		    	                		break;
		    	                	case 1:
		    	                		IOHelper.read(is, payload, 4);
		    	                		len = Tools.bytesToInt(payload, 0, 4);
		    	                		IOHelper.read(is, payload, len);
		    	    			    	JSONObject proxy = new JSONObject(new String(payload, 0, len, "UTF-8"));
		    	                		System.out.println("COSApiAgent = "+proxy);
		    	                		break;
		    	                	}
		    	                }
		    		        }
		    		        catch( Exception e )
		    		        {
		    		        }
		    		        System.out.println("[COSApiAgent-Reader] Quite.");
		    		    }
		    		};
		    		thread.start();
	            	log.info("COSApi-Tester start."+sb.toString());
		            int status = cosApiProcess.waitFor();
	            	log.warn("COSApi-Tester quite("+status+").");
		    	}
		    	catch(Exception e)
		    	{
		    		log.error("Failed to startup test-COSApi", e);
		    	}
		        finally
		        {
		        	if( cosApiProcess != null ) cosApiProcess.destroy();
		        	cosApiProcess = null;
		        }
		    }
		};
		thread.start();
	}
	/**
	 * 启动测试的ZK进程
	 */
	private Process zookeeperProcess;
	public void startupTestZooKeeper()
	{
		Thread thread = new Thread()
		{
		    public void run()
		    {
				int port = COSConfig.getLocalControlPort();
		        try
		        {
		        	URL url = Initializer.class.getClassLoader().getResource("/");
		        	String classesspath = url.getFile();
		        	File zoolog = new File(PathFactory.getDataPath(), "zklog");
		        	if( !zoolog.exists() )
		        	{
		        		zoolog.mkdir();
		        	}
		        	File zoodata = new File(PathFactory.getDataPath(), "zkdat");
		        	if( !zoodata.exists() )
		        	{
		        		zoodata.mkdirs();
		        	}
		        	File zkcfg = new File(classesspath, "zk/");
		        	File zoocfg = new File(zkcfg, "zoo.cfg");
		        	StringBuffer sb = new StringBuffer();
		        	String dataDir = Tools.replaceStr(zoodata.getAbsolutePath(), "\\", "/");
		        	String dataLogDir = Tools.replaceStr(zoolog.getAbsolutePath(), "\\", "/");
		        	sb.append("tickTime=2000\r\n");
		        	sb.append("initLimit=5\r\n");
		        	sb.append("syncLimit=2\r\n");
		        	sb.append("dataDir="+dataDir+"\r\n");
		        	sb.append("dataLogDir="+dataLogDir+"\r\n");
		        	sb.append("clientPort="+port);
		        	sb.append("\r\nautopurge.snapRetainCount=10");
		        	sb.append("\r\nautopurge.purgeInterval=1");
		        	syslogtext.append(sb.toString());
		        	IOHelper.writeFile(zoocfg, sb.toString().getBytes("UTF-8"));
		        	File zoolib = new File(classesspath, "../lib");
		        	String jdk = System.getProperty("java.home");
		        	if( !jdk.endsWith("bin/java") )
		        		jdk += "/bin/java";
		        	syslogtext.append( "\r\n========================================\r\n"+
		        	          "\tZookeeper start by jdk("+jdk+") and cp("+zoolib.getPath()+")"+
		        	          "\r\n========================================");
		        	ArrayList<String> commands = new ArrayList<String>();//执行系统监控进程的启动指令
		        	commands.add( jdk );
		        	commands.add( "-Dzookeeper.log.dir="+zoolog.getPath());
		        	commands.add( "-Dzookeeper.root.logger=INFO,CONSOLE" );
		        	commands.add( "-cp");
		        	commands.add( zoolib.getPath()+"/*"+System.getProperty("path.separator", ";")+zkcfg.getPath());
		        	commands.add( "org.apache.zookeeper.server.quorum.QuorumPeerMain" );
		        	commands.add( zoocfg.getPath() );
		        	StringBuffer command = new StringBuffer();
		        	command.append( jdk );
		        	command.append( ' ' );
		        	command.append( "-Dzookeeper.log.dir="+zoolog.getPath());
		        	command.append( ' ' );
		        	command.append( "-Dzookeeper.root.logger=INFO,CONSOLE" );
		        	command.append( ' ' );
		        	command.append( "-cp");
		        	command.append( ' ' );
		        	command.append( zoolib.getPath()+"/*");
		        	command.append( ';' );
		        	command.append( zkcfg.getPath() );
		        	command.append( ' ' );
		        	command.append( "org.apache.zookeeper.server.quorum.QuorumPeerMain" );
		        	command.append( ' ' );
		        	command.append( zoocfg.getPath() );
		        	syslogtext.append("\r\n"+command);
		        	ProcessBuilder pb = new ProcessBuilder( commands );
		            //开启错误信息的流到标准输出流，在某种情况下由于错误输出流中的数据没有被读取，进程就不会结束
		            pb.redirectErrorStream( true );
		            zookeeperProcess = pb.start();
		            SubprocessReader reader = new SubprocessReader(zookeeperProcess, log);
		            reader.start();
	            	log.info("Zookeeper-Tester start.");
		            int status = zookeeperProcess.waitFor();
	            	log.warn("Zookeeper-Tester quite("+status+").");
		        }
		        catch( Exception e )
		        {
		        	syslogtext.append("\r\nZookeeper has been closed for exception "+e);
		        	log.error( "Zookeeper has been closed for exception:", e);
		        }
		        finally
		        {
		        	if( zookeeperProcess != null ) zookeeperProcess.destroy();
		        	zookeeperProcess = null;
		        }
		    }
		};
		thread.start();
	}
	
	public static void main(String args[]){
		File dir = new File("D:/focusnt/cos/trunk/CODE/main/WebContent/images/number");
		File[] files = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				boolean r0 = name.endsWith(".png");
				return r0;
			}
		});
		for(File file : files){
			String name = file.getName();
			int i = name.indexOf("(");
			name = name.substring(i+1);
			name = name.replace(")", "");
			File newname = new File(file.getParentFile(), name);
//			System.err.println(name);
			file.renameTo(newname);
		}
	}
}