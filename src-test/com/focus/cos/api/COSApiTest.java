package com.focus.cos.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.focus.cos.H2;
import com.focus.cos.control.COSApi;
public class COSApiTest 
{
	private COSApiAgent cosApiAgent;
	private COSApiProxy cosApiProxy;
	private String agentAddress;
	private String proxyAddress;
	
	public COSApiTest()
	{
		// 当程序被关闭的时候钩子会被回调
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			public void run()
			{
				if( cosApiAgent != null )
					cosApiAgent.close();
				if( cosApiProxy != null )
					cosApiProxy.close();
				System.out.println("Close the test of COS-API.");
			}
		});
	}
	
	@Before
	public void setUp() throws Exception 
	{
		System.out.println("Setup the test of COS-API#############.");
	}
	
	private void setRandomCosApi()
	{
		final Random random = new Random();
		if( random.nextBoolean() )
		{
			System.setProperty("cos.api", agentAddress);
//			ApiUtils.COSApi = "http://"+agentAddress+"/";
		}
		else
		{
			System.setProperty("cos.api", proxyAddress);
//			ApiUtils.COSApi = "http://"+proxyAddress+"/";
		}
	}
	

	@Test
	public void test() 
	{
		testByh2normal();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		testByH2cluster();
	}
	/**
	 * 启动测试API
	 * @throws Exception
	 */
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
            		if( proxy.has("agent") && !proxy.getBoolean("agent") )
            			assertEquals( proxyPort, proxy.getInt("proxyport"));
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
        		proxyPort = port;
    			assertNotEquals( proxyPort, 9079);
    			agentAddress = "127.0.0.1:9079";
    			proxyAddress = "127.0.0.1:"+proxyPort;
    			System.out.println("The address of proxy is "+proxyAddress);
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
	
	private int proxyPort = 0;
	private String testUsername = "test0";
	public void testByh2normal() 
	{
		System.out.println("####################Begin Test First###################");
		Process normal = null;
		try
		{
	    	System.setProperty("cos.jdbc.url", "jdbc:h2:tcp://localhost:9092/./h2/cos");
	        System.setProperty("cos.jdbc.driver", "org.h2.Driver");
	        System.setProperty("cos.jdbc.user", "sa");
	        System.setProperty("cos.jdbc.password", "");
			normal = H2.startup(9092, 0, null, null);
			H2.initialize(new File("h2"));
			startTestApi();
			testJustYouCan();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			assertNull(e);
		}
		finally
		{
			if( normal != null ) normal.destroy();
		}
	}
	
	public void testByH2cluster() 
	{
		System.out.println("####################Begin Test Again###################");
		try
		{
			File dir0 = new File("test/COSApi/h2.standby/h2");
			FileUtils.deleteDirectory(dir0);
			File dir1 = new File("test/COSApi/h2.standby/h2");
			FileUtils.deleteDirectory(dir1);

	    	System.setProperty("cos.jdbc.url", "jdbc:h2:tcp://localhost:"+9192+",127.0.0.1:"+9292+"/./h2/cos");
	        System.setProperty("cos.jdbc.driver", "org.h2.Driver");
	        System.setProperty("cos.jdbc.user", "sa");
	        System.setProperty("cos.jdbc.password", "");
	        
	        Runtime.getRuntime().exec("test/COSApi/h2.standby/start.bat");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
	        Runtime.getRuntime().exec("test/COSApi/h2.master/start.bat");
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
			}
	        H2.initialize(new File("test/COSApi/h2.master/h2"));
	        
	        startTestApi();
			testUsername = "test1";
			testJustYouCan();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			assertNull(e);
		}
		finally
		{
			this.closeH2(9192);
			this.closeH2(9292);
		}
	}
	
	/**
	 * 发送指令关闭h2程序
	 * @param port
	 */
	private void closeH2(int port)
	{
		byte[] buf = new byte[64];
		DatagramPacket p = new DatagramPacket(buf, buf.length);
		try
		{
			p.setAddress(InetAddress.getLocalHost());
			p.setPort(port);
			DatagramSocket socket = new DatagramSocket();
			socket.send(p);
			socket.close();
		}
		catch(Exception e)
		{
		}
	}
	
	public void testJustYouCan() 
	{
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		
//		ApiUtils.COSApi = "http://127.0.0.1:19079/";
		System.out.println("Begin the test of COS-API.");
		setRandomCosApi();
		testSysuserClient();
		System.out.println("---------------------End above SysuserClient---------------");
		setRandomCosApi();
		testSysnotifyClient();
		System.out.println("---------------------End above SysnotifyClient---------------");
		setRandomCosApi();
		testSyslogClient();
		System.out.println("---------------------End above SyslogClient---------------");
		setRandomCosApi();
		testSysemailClient();
		System.out.println("---------------------End above SysemailClient---------------");
		setRandomCosApi();
		testSysalarmClient();
		System.out.println("---------------------End above SysalarmClient---------------");
		setRandomCosApi();
		testSyspublishClient();
		System.out.println("---------------------End above SyspublishClient---------------");

		if( cosApiAgent != null )
			cosApiAgent.close();
		if( cosApiProxy != null )
			cosApiProxy.close();
		System.out.println("####################Finish testJustYouCan######################");
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
		}
	}


	private void testSyspublishClient()
	{
		
	}
	
	private void testSysalarmClient()
	{
		Sysalarm alarm = new Sysalarm();
		try
		{
			SysalarmClient.save(alarm);
		}
		catch(Exception e)
		{
			assertEquals("没有设置系统告警的标题(title)", e.getMessage());
		}
		try
		{
			alarm.setTitle("测试标题");
			SysalarmClient.save(alarm);
		}
		catch(Exception e)
		{
			assertEquals("没有设置系统告警的模块(module)", e.getMessage());
		}
		try
		{
			alarm.setSysid("Sys");
			SysalarmClient.save(alarm);
		}
		catch(Exception e)
		{
			assertEquals("没有设置系统告警的标识(id)", e.getMessage());
		}
		try
		{
			alarm.setId("ABC");
			SysalarmClient.save(alarm);
		}
		catch(Exception e)
		{
			assertEquals("没有设置系统告警的内容(text)", e.getMessage());
		}
		try
		{
			alarm.setText("ABC");
			SysalarmClient.save(alarm);
		}
		catch(Exception e)
		{
			assertEquals("没有设置系统告警的级别(severity)", e.getMessage());
		}
		try
		{
			alarm.setSeverity(AlarmSeverity.RED.getValue());
			SysalarmClient.save(alarm);
		}
		catch(Exception e)
		{
			assertEquals("没有设置系统告警的类型(type)", e.getMessage());
		}

		long alarmid = 0;
		try
		{
			alarm.setType(AlarmType.E.getValue());
			SysalarmClient.save(alarm);
			alarmid = alarm.getAlarmid();
			assertNotEquals(0, alarmid);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			assertNull(e);
		}
//		alarm.setAlarmid(0);
//		SysalarmClient.submit(alarm);
//		assertEquals(alarmid, alarm.getAlarmid());

		try
		{
			List<Sysalarm> list = SysalarmClient.close(alarm.getSysid(), alarm.getId(), "tet");
			assertEquals(1, list.size());
			assertEquals("测试标题", list.get(0).getTitle());
			assertEquals(alarmid, list.get(0).getAlarmid());
			list = SysalarmClient.confirm(String.valueOf(alarmid), "", "abcc");
			assertEquals(0, list.size());
			//assertEquals("测试标题", list.get(0).getTitle());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			assertNull(e);
		}
	}
	
	private void testSysemailClient()
	{
		Sysemail outbox = new Sysemail();
		try
		{
			SysemailClient.send(outbox);
		}
		catch(Exception e)
		{
			assertEquals("没有设置系统邮件的标题(subject)", e.getMessage());
		}
		try
		{
			outbox.setSubject("测试邮件");
			SysemailClient.send(outbox);
		}
		catch(Exception e)
		{
			assertEquals("没有设置系统邮件的内容(content)", e.getMessage());
		}
		try
		{
			outbox.setContent("测试邮件内容");
			SysemailClient.send(outbox);
		}
		catch(Exception e)
		{
			assertEquals("没有设置系统邮件的收件地址或邮箱格式不正确(mailTo)", e.getMessage());
		}

		try
		{
			outbox.setMailTo("liu3xe@163.com");
			SysemailClient.send(outbox);
			assertNotNull(outbox.getEid());
			assertNotEquals(Long.valueOf(0), outbox.getEid());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			assertNull(e);
		}
		
		
	}
	
	private void testSyslogClient()
	{
		Syslog log = new Syslog();
		try
		{
			SyslogClient.write(log);
		}
		catch(Exception e)
		{
			assertEquals("没有设置系统日志的内容(text)", e.getMessage());
		}
		try
		{
			log.setLogtext("测试日志");
			SyslogClient.write(log);
		}
		catch(Exception e)
		{
			assertEquals("没有设置系统日志的级别(severity)", e.getMessage());
		}
		try
		{
			log.setLogseverity(LogSeverity.DEBUG.getValue());
			SyslogClient.write(log);
		}
		catch(Exception e)
		{
			assertEquals("没有设置系统日志的类型(type)", e.getMessage());
		}

		try
		{
			log.setLogtype(LogType.操作日志.getValue());
			SyslogClient.write(log);
		}
		catch(Exception e)
		{
			assertEquals("没有设置系统日志的账户(account)", e.getMessage());
		}
		try
		{
			log.setAccount("AAAAAAAAAAAAAAAAAAAA");
			SyslogClient.write(log);
		}
		catch(Exception e)
		{
			assertEquals("没有设置系统日志的分类(category)", e.getMessage());
		}
		try
		{
			log.setCategory("BBBBBBBBBB");
			long id = SyslogClient.write(log);
			assertNotEquals(0, id);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			assertNull(e);
		}
	}
	
	private void testSysuserClient()
	{
		Sysuser user = SysuserClient.getUser("admin");
		assertNotNull(user);
		assertEquals("admin", user.getUsername());
		assertNull(user.getPassword());
		
		ArrayList<Sysuser> users = SysuserClient.list();
		assertNotEquals(0, users.size());

		try
		{
			SysuserClient.add(user);
		}
		catch(Exception e)
		{
			assertEquals("没有设置新增用户的账户密码或密码长度小于6位", e.getMessage());
		}
		try
		{
			user.setPassword("123456");
			user.setEmail("admin@163.com");
			SysuserClient.add(user);
		}
		catch(Exception e)
		{
			assertEquals("新增的用户已经存在", e.getMessage());
		}
		try
		{
			user.setUsername("");
			SysuserClient.add(user);
		}
		catch(Exception e)
		{
			assertEquals("没有设置新增用户的账户名称", e.getMessage());
		}
		try
		{
			user.setUsername(testUsername);
			user.setEmail("");
			SysuserClient.add(user);
		}
		catch(Exception e)
		{
			assertEquals("没有设置新增用户的账户邮箱或邮箱格式不正确", e.getMessage());
		}
		try
		{
			user.setEmail("test@163.com");
			user.setRealname("测试账号");
			user.setSex(Sex.Female.getValue());
			SysuserClient.add(user);
			Thread.sleep(1000);
			user = SysuserClient.getUser(testUsername);
			assertNotNull(user);
			assertEquals(testUsername, user.getUsername());
			assertNull(user.getPassword());
			assertEquals("test@163.com", user.getEmail());
			assertEquals(0, (int)user.getRoleid());
			assertEquals((int)Sex.Female.getValue(), (int)user.getSex());
			assertEquals((int)Status.Disable.getValue(), (int)user.getStatus());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			assertNull(e);
		}
	}
	
	private  void testSysnotifyClient()
	{
		Sysnotify notify = new Sysnotify();
		try
		{
			SysnotifyClient.send(notify);
		}
		catch(Exception e)
		{
			assertEquals("没有设置系统通知的标题(title)", e.getMessage());
		}
		try
		{
			notify.setTitle("测试标题");
			SysnotifyClient.send(notify);
		}
		catch(Exception e)
		{
			assertEquals("没有设置系统通知的分类标签(filter)", e.getMessage());
		}
		try
		{
			notify.setFilter("测试分类");
			SysnotifyClient.send(notify);
		}
		catch(Exception e)
		{
			assertEquals("没有设置系统通知的接收用户(useraccount)", e.getMessage());
		}

		try
		{
			notify.setUseraccount("admin");
			SysnotifyClient.send(notify);
			assertNotEquals(0, notify.getNid());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			assertNull(e);
		}
	}
}
