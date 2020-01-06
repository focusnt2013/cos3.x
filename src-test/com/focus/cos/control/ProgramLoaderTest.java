package com.focus.cos.control;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.focus.cos.CosServer;
import com.focus.cos.H2;
import com.focus.cos.api.COSApiAgent;
import com.focus.cos.api.COSApiProxy;
import com.focus.util.IOHelper;
import com.focus.util.Tools;
import com.focus.util.Zookeeper;

public class ProgramLoaderTest extends Thread
{
	private COSApiAgent cosApiAgent;
	private COSApiProxy cosApiProxy;
	private Process processProgramLoader = null;
	private Process processZooKeeper = null;
	private Process processH2 = null;
//	private String agentAddress;
//	private String proxyAddress;
	private File controlxml;
	@Before
	public void setUp() throws Exception 
	{
		System.setProperty("control.port", "9527");
		startupTestZooKeeper();
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			public void run()
			{
				if( cosApiAgent != null ) cosApiAgent.close();
				if( cosApiProxy != null ) cosApiProxy.close();
				if( processZooKeeper != null ) processZooKeeper.destroy();
				if( processH2 != null ) processH2.destroy();
				if( processProgramLoader != null ) processProgramLoader.destroy();
			}
		});
		try 
		{
	    	System.setProperty("cos.jdbc.url", "jdbc:h2:tcp://localhost:9092/./h2/cos");
	        System.setProperty("cos.jdbc.driver", "org.h2.Driver");
	        System.setProperty("cos.jdbc.user", "sa");
	        System.setProperty("cos.jdbc.password", "");
	        processH2 = H2.startup(9092, 0, null, null);
			H2.initialize(new File("h2"));

			startTestApi();
			File cxfile = new File("test/ProgramLoader/case/1.xml");
			controlxml = new File("test/ProgramLoader/control.xml");
			FileUtils.copyFile(cxfile, controlxml);
			//启动程序加载器
			startupTestProgramLoader();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			assertNotNull(e);
			throw e;
		}
	}

	/**
	 * 启动测试API
	 * @throws Exception
	 */
	private int proxyApiPort = 0;
	private void startTestApi() throws Exception
	{
        System.setProperty("cos.identity", "data/identity");
        cosApiAgent = new COSApiAgent(9079){
			@Override
			public void setApiPort(int port) {
        		System.out.println("COSApiAgent port is "+port);
    			assertEquals( 9079, port);
    			synchronized (this) {
					this.notify();
				}
			}

			@Override
			public void setApiProxy(byte[] payload, int length) {
		    	try
		    	{
			    	JSONObject proxy = new JSONObject(new String(payload, 0, length, "UTF-8"));
            		System.out.println("COSApiAgent = "+proxy);
		    	}
		    	catch(Exception e)
		    	{
		    		e.printStackTrace();
        			assertEquals( e, null);
		    	}
			}

			@Override
			public void setVersion(String version) {
        		System.out.println("COSApiAgent version is "+version);
    			assertEquals( COSApi.Versions[COSApi.Versions.length-1][0], version);
			}
        	
        };
        cosApiAgent.start();
        synchronized (cosApiAgent) {
        	cosApiAgent.wait(7000);
		}
		assertEquals( true, cosApiAgent.isRunning());
		
		cosApiProxy = new COSApiProxy(9079){
			@Override
			public void setApiPort(int port) {
        		System.out.println("COSApiProxy port is "+port);
        		proxyApiPort = port;
    			synchronized (this) {
					this.notify();
				}
			}

			@Override
			public void setApiAgent(byte[] payload, int length) {
		    	try
		    	{
			    	JSONObject agent = new JSONObject(new String(payload, 0, length, "UTF-8"));
            		System.out.println("COSApiProxy = "+agent);
            		assertEquals( agent.getString("name"), "Agent111" );
            		assertEquals( agent.getString("desc"), "测试Agent" );
        			assertEquals( agent.getInt("port"), 9079);
		    	}
		    	catch(Exception e)
		    	{
		    		e.printStackTrace();
        			assertEquals( e, null);
		    	}
			}
			@Override
			public void setVersion(String version) {
        		System.out.println("COSApiProxy version is "+version);
    			assertEquals( COSApi.Versions[COSApi.Versions.length-1][0], version);
			}
		};
		cosApiProxy.start();
		synchronized(cosApiProxy)
		{
			cosApiProxy.wait(7000);
		}
		assertEquals( true, cosApiProxy.isRunning());
	}
	
	private boolean beginTest = false;
	private String testId = null;
	private String testExcepted = null;
	@Test
	public void testJustYouCan() 
	{
		try {
			System.out.println("Begin test of Program-Loader.");
			Thread.sleep(3000);
			beginTest = true;
			assertEquals(14, programs.size());
			ZooKeeper zooKeeper = Zookeeper.getInstance("127.0.0.1:"+System.getProperty("control.port")).i();
			String serverkey = CosServer.getSecurityKey();
			String ci = Tools.encodeMD5(serverkey);
			String zkpath = "/cos/config/program/"+ci;
			List<String> list = zooKeeper.getChildren(zkpath+"/publish", false);
			Random random = new Random();
			testId = list.get(random.nextInt(list.size()));
			String path = zkpath+"/switch/"+testId;
			Stat stat = zooKeeper.exists(path, false);
			assertNotNull(stat);
			testExcepted = new String(zooKeeper.getData(path, false, stat));
			assertEquals(true, testExcepted.equals("true")||testExcepted.equals("false"));
			if( testExcepted.equals("true") ) testExcepted = "false";
			else testExcepted = "true";
			System.out.println("#####测试程序开关("+testId+", "+testExcepted+")#####");
			zooKeeper.setData(path, testExcepted.getBytes(), stat.getVersion());
			Thread.sleep(1000);
			
			testId = list.get(random.nextInt(list.size()));
			path = zkpath+"/debug/"+testId;
			stat = zooKeeper.exists(path, false);
			assertNotNull(stat);
			testExcepted = new String(zooKeeper.getData(path, false, stat));
			assertEquals(true, testExcepted.equals("true")||testExcepted.equals("false"));
			if( testExcepted.equals("true") ) testExcepted = "false";
			else testExcepted = "true";
			System.out.println("#####测试程序调试("+testId+", "+testExcepted+")#####");
			zooKeeper.setData(path, testExcepted.getBytes(), stat.getVersion());
			Thread.sleep(1000);

			testId = list.get(random.nextInt(list.size()));
			path = zkpath+"/publish/"+testId;
			stat = zooKeeper.exists(path, false);
			assertNotNull(stat);
			String json = new String(zooKeeper.getData(path, false, stat));
			JSONObject config = new JSONObject(json);
			if( config.has("control") )
			{
				JSONObject control = config.getJSONObject("control");
				control.put("restartup", 100);
			}
			System.out.println("#####测试程序配置("+testId+")#####");
			zooKeeper.setData(path, config.toString().getBytes("UTF-8"), stat.getVersion());
			Thread.sleep(1000);
			

			testId = list.get(random.nextInt(list.size()));
			path = zkpath+"/publish/"+testId;
			stat = zooKeeper.exists(path, false);
			System.out.println("#####测试程序配置("+testId+")删除#####");
			zooKeeper.delete(path, stat.getVersion());
			Thread.sleep(1000);
			
			testId = "Portal";
			path = "/cos/temp/program/publish/"+ci+":"+testId;
			
			System.out.println("#####通过control.xml测试程序配置(用例2测试, "+serverkey+", "+path+")删除#####");
			File cxfile = new File("test/ProgramLoader/case/2.xml");
			FileUtils.copyFile(cxfile, controlxml);
			controlxml.setLastModified(System.currentTimeMillis());
			Thread.sleep(7000);
			stat = zooKeeper.exists(path, false);
			assertNull(stat);
			
			testId = "Portal";
			System.out.println("#####通过control.xml测试程序配置(用例3测试, "+serverkey+", "+path+")设置#####");
			cxfile = new File("test/ProgramLoader/case/3.xml");
			FileUtils.copyFile(cxfile, controlxml);
			controlxml.setLastModified(System.currentTimeMillis());
			Thread.sleep(7000);
			stat = zooKeeper.exists(path, false);
			assertNotNull(stat);
			byte[] data = zooKeeper.getData(path, false, stat);
			config = new JSONObject(new String(data, "UTF-8"));
			System.out.println(config.toString(4));
			assertEquals(ProgramLoader.OPER_ADDING, config.getInt("oper"));
			
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("---------------------End Test---------------");
	}
	
	private HashMap<String, JSONObject> programs = new HashMap<String, JSONObject>();
	public void run()
	{
		System.out.println("--------------------- Start The Receiver Of Program-Loader ---------------");
        int method = -1;
        int len = 0;
        InputStream is = this.processProgramLoader.getInputStream();
        try
        {
        	byte[] payload = new byte[65536];
        	JSONObject config = null;
        	String json = null;
        	int r = 0;
            while( ( method = is.read() ) != -1 )
            {
            	switch(method)
            	{
            	case 0://标题
            		len = is.read();
            		IOHelper.read(is, payload, len);
            		json = new String(payload, 0, len);
            		System.out.println("*收到程序配置引擎发送来的版本号("+len+"): "+json);
            		len = is.read();
            		IOHelper.read(is, payload, len);
            		json = new String(payload, 0, len, "UTF-8");
            		System.out.println("*收到程序配置引擎发送来的伺服器标题("+len+"): "+json);
            		assertEquals("应用服务器(老网站接入备机)", json);
            		break;
            	case 1://配置
            		IOHelper.read(is, payload, 4);
            		len = Tools.bytesToInt(payload, 0, 4);
            		IOHelper.read(is, payload, len);
            		json = new String(payload, 0, len, "UTF-8");
            		try
            		{
            			config = new JSONObject(json); 
	            		System.out.println("*收到程序配置引擎加载的程序配置: "+config.getString("id"));
            			programs.put(config.getString("id"), config);
	            		if( beginTest )
	            		{
	            			assertEquals(testId, config.getString("id"));
	        				JSONObject control = config.getJSONObject("control");
	            			assertEquals(100, control.getInt("restartup"));
	            		}
            		}
            		catch(Exception e)
            		{
            			e.printStackTrace();
            			System.err.println(json);
            			assertNotNull(e);
            		}
            		break;
            	case 2://运行开关
            	case 3://DEBUG开关
            		len = is.read();
            		IOHelper.read(is, payload, len);
            		json = new String(payload, 0, len);
            		r = is.read();
            		System.out.println("*收到程序配置引擎发送来的开关通知: "+json+"("+r+")");
            		assertEquals(testId, json);
            		assertEquals(testExcepted, r==1?"true":"false");
            		break;
            	case 4://删除
            		len = is.read();
            		IOHelper.read(is, payload, len);
            		json = new String(payload, 0, len);
            		System.out.println("*收到程序配置引擎发送来的删除程序通知: "+json+"("+r+")");
            		assertEquals(testId, json);
            		programs.remove(json);
            		break;
            	}
            }
        }
        catch( Exception e )
        {
        	e.printStackTrace();
        }
        finally
        {
    		System.out.println("--------------------- End The Receiver Of Program-Loader ---------------");
        }
	}
	
	public void startup()
	{
		start();
	}
	
	public void startupTestProgramLoader()
	{
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
		        	File classesDir = new File("build/classes");
		        	File libDir = new File("lib");
		        	File weblibDir = new File("WebContent/WEB-INF/lib");

		        	StringBuffer cp = new StringBuffer();
		        	cp.append(classesDir.getAbsolutePath().replace("\\", "/"));
		        	cp.append(separator_path);
		        	cp.append(libDir.getAbsolutePath().replace("\\", "/")+"/*");
		        	cp.append(separator_path);
		        	cp.append(weblibDir.getAbsolutePath().replace("\\", "/")+"/*");
		        	ArrayList<String> startupCommands = new ArrayList<String>();
		        	startupCommands.add( i++, jdk );
		        	startupCommands.add( i++,  "-Xms16m" );
		        	startupCommands.add( i++,  "-Xmx64m" );
		        	startupCommands.add( i++,  "-XX:PermSize=8M" );
		        	startupCommands.add( i++,  "-XX:MaxPermSize=32m" );
		        	startupCommands.add( i++,  "-Dcos.identity=data/identity");
		        	startupCommands.add( i++,  "-Dcontrol.port="+System.getProperty("control.port") );
		        	startupCommands.add( i++,  "-Dcos.api.port="+proxyApiPort );
		        	startupCommands.add( "-cp" );
		        	startupCommands.add( cp.toString() );
		        	startupCommands.add("com.focus.cos.control.ProgramLoader");
		        	startupCommands.add("test/ProgramLoader/control.xml");
		        	StringBuffer sb = new StringBuffer();
		        	for(String command : startupCommands)
		        	{
		        		sb.append("\r\n\t");
		        		sb.append(command);
		        	}
		        	System.out.println(sb.toString());

		        	ProcessBuilder pb = new ProcessBuilder( startupCommands );
		            //开启错误信息的流到标准输出流，在某种情况下由于错误输出流中的数据没有被读取，进程就不会结束
		            pb.redirectErrorStream( true );
		            processProgramLoader = pb.start();
		            System.out.println("Startup Program-Loader.");
		            startup();
		            processProgramLoader.waitFor();
		            System.out.println("Quite Program-Loader.");
		        }
		        catch( Exception e )
		        {
		        	e.printStackTrace();
		        }
		    }
		};
		thread.start();
	}
	
	
	/**
	 * 启动测试的ZK进程
	 */
	public void startupTestZooKeeper()
	{
		Thread thread = new Thread()
		{
		    public void run()
		    {
		        try
		        {
		        	File testdir = new File("test/ProgramLoader");
		        	File zoolog = new File(testdir, "zklog");
		        	FileUtils.deleteDirectory(zoolog);
	        		zoolog.mkdirs();
		        	File zoodata = new File(testdir, "zkdat");
		        	FileUtils.deleteDirectory(zoodata);
	        		zoodata.mkdirs();
		        	File zkcfg = new File(testdir, "zk");
		        	FileUtils.deleteDirectory(zkcfg);
		        	File zoocfg = new File(zkcfg, "zoo.cfg");
		        	StringBuffer sb = new StringBuffer();
		        	String dataDir = Tools.replaceStr(zoodata.getAbsolutePath(), "\\", "/");
		        	String dataLogDir = Tools.replaceStr(zoolog.getAbsolutePath(), "\\", "/");
		        	sb.append("tickTime=2000\r\n");
		        	sb.append("initLimit=5\r\n");
		        	sb.append("syncLimit=2\r\n");
		        	sb.append("dataDir="+dataDir+"\r\n");
		        	sb.append("dataLogDir="+dataLogDir+"\r\n");
		        	sb.append("clientPort="+System.getProperty("control.port"));
		        	sb.append("\r\nautopurge.snapRetainCount=10");
		        	sb.append("\r\nautopurge.purgeInterval=1");
		        	IOHelper.writeFile(zoocfg, sb.toString().getBytes("UTF-8"));
		        	File zoolib = new File("WebContent/WEB-INF/lib");
		        	String jdk = System.getProperty("java.home");
		        	if( !jdk.endsWith("bin/java") )
		        		jdk += "/bin/java";
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
		        	ProcessBuilder pb = new ProcessBuilder( commands );
		            //开启错误信息的流到标准输出流，在某种情况下由于错误输出流中的数据没有被读取，进程就不会结束
		            pb.redirectErrorStream( true );
		            processZooKeeper = pb.start();
	            	System.out.println("Zookeeper-Tester start.");
		            int status = processZooKeeper.waitFor();
		            System.out.println("Zookeeper-Tester quite("+status+").");
		        }
		        catch( Exception e )
		        {
		        	e.printStackTrace();
		        }
		        finally
		        {
		        	if( processZooKeeper != null ) processZooKeeper.destroy();
		        	processZooKeeper = null;
		        }
		    }
		};
		thread.start();
	}	
	/**
	 * 
	 * @param zookeeper
	 * @param path
	 * @return
	 */
	public String getZKValue(ZooKeeper zookeeper, String path)
	{
		try
		{
			Stat stat = zookeeper.exists(path, false);
			if( stat != null )
			{
				return new String(zookeeper.getData(path, false, stat), "UTF-8");
			}
		}
		catch(Exception e)
		{
		}
		return "";
	}
}
