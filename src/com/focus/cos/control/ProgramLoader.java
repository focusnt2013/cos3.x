package com.focus.cos.control;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.data.Stat;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Node;

import com.focus.cos.CosServer;
import com.focus.cos.api.SyspublishClient;
import com.focus.util.ConfigUtil;
import com.focus.util.F;
import com.focus.util.IOHelper;
import com.focus.util.Log;
import com.focus.util.Tools;
import com.focus.util.XMLParser;
import com.focus.util.Zookeeper;

public class ProgramLoader implements Runnable
{
	public static final String ModuleID = "ProgramLoader";
	public static String[][] Versions = {
		{"3.17.5.16",	"初始版本发布。"},
		{"3.17.6.10",	"老版本control.xml正常加载历史程序配置。"},
		{"3.18.3.12",	"支持配置文件加载。"},
	};
	public static final int OPER_NONE = -1;
	public static final int OPER_ADDING = 0;//新增
	public static final int OPER_EDITING = 1;//修改
	public static final int OPER_DELETING = 3;//删除
	public static final int OPER_DELETED = 4;//已删除
	public static final int OPER_REJECT = 5;//系统管理员拒绝，用户可以重新申请提交
	public static final int OPER_ACCEPT = 200;//系统管理员接受程序发布
	
    private File controlxml;
    private long reloadTimestamp = 0;
    /*ZK句柄*/
    private Zookeeper zookeeper;
    /**/
	private boolean opened;
	/*伺服器程序集群标题*/
	private String subject;
	/*伺服器程序集合*/
	private HashMap<String, JSONObject> programs = new HashMap<String, JSONObject>();
	/*服务实例的唯一标识*/
	private String serverkey = null;
	
	public static void main(String args[])
	{
        //启动日志管理器
        Log.getInstance().setSubroot(ModuleID);
        Log.getInstance().setDebug(false);
        Log.getInstance().setLogable(true);
        Log.getInstance().start();
		try
		{
			String cxpath = args.length>0?args[0]:(ConfigUtil.getWorkPath() + "config/control.xml");
			F file = new F( cxpath );
			ProgramLoader loader = new ProgramLoader(file);
			Thread thread = new Thread(loader);
			thread.start();
		}
		catch(Exception e)
		{
			Log.err("Failed to initialize: ", e);
			System.exit(2);
		}
	}
	
	public ProgramLoader(File controlxml) throws Exception
	{
        String Version = Versions[Versions.length-1][0];
		this.controlxml = controlxml;
//		reloadTimestamp = controlxml.lastModified();
        StringBuffer info = new StringBuffer("================================================================================================");
        info.append("\r\n"+ModuleID+"\tv"+Version);
		info.append("\r\n\tuser.dir\t"+System.getProperty("user.dir", "?"));
		info.append("\r\n\tcos.control.port\t"+System.getProperty("control.port", "?"));
		info.append("\r\n\tcos.control.xml\t"+controlxml.getAbsolutePath()+"("+controlxml.exists()+")");
		info.append("\r\n\tCopyright (C) 2008-2017 Focusnt.  All Rights Reserved.");
		info.append("\r\n================================================================================================");
		Log.msg( info.toString() );
		serverkey = Tools.encodeMD5(CosServer.getSecurityKey());
	}
	
	public String getServerkey() {
		return serverkey;
	}

	@Override
	public void run() 
	{
		StringBuffer logtxt = new StringBuffer();
		boolean temp = false;
        try
        {
        	if( isZookeeperConnected() )
        	{
        		initialize(logtxt);
        	}
        	else
        	{
        		temp = tempinit(logtxt);
        	}
        	Iterator<JSONObject> iterator = this.programs.values().iterator();
        	logtxt.append("\r\nNotify the configs of program("+this.programs.size()+") to control.");
        	while( iterator.hasNext() )
        	{
        		JSONObject config = iterator.next();
                String id = config.getString("id");//XMLParser.getElementAttr( moduleNode, "id" );
                String name = config.getString("name");//XMLParser.getElementAttr( moduleNode, "name" );
        		String version = config.has("version")?config.getString("version"):"";
                JSONObject control = config.has("control")?config.getJSONObject("control"):null;
    		    boolean enable = config.has("switch")?config.getBoolean("switch"):false;
    	        boolean debug = config.has("debug")?config.getBoolean("debug"):false;
                if( control != null )
                {
	    	        int mode = control.has("mode")?control.getInt("mode"):Module.MODE_NORMAL;
	    	        logtxt.append("\r\n\tSend this config(mode="+mode+",enable="+enable+",debug="+debug+") of module("+id+", "+version+", "+name+").");
	    	        sendConfig(config);
                }
                else
                {
                	logtxt.append("\r\n\tSkip this config of module("+id+", "+version+", "+name+") for control null.");
                }
    	      
        	}
    		Log.msg(logtxt.toString());
    		Runtime.getRuntime().addShutdownHook(new Thread()
    		{
    			public void run()
    			{
    				shutdown();
    			}
    		});
    		opened = true;
            while( opened )
            {
            	synchronized (this) {
					this.wait(7000);
				}
            	if( temp && isZookeeperConnected() )
            	{
            		Log.msg("Restartup current program for found zookeeper ok.");
            		break;
            	}
            	else
            	{
            		if( opened && controlxml.lastModified() > this.reloadTimestamp )
            		{//control.xml文件配置发生的更新
            			reload();
            		}
            	}
            }
        }
        catch( Exception e )
        {
        	Log.err("Failed to run for exception: ", e);
        	Log.err(logtxt.toString());
        }
        finally
        {
        	Log.war("Quite...");
        	if( zookeeper != null ) zookeeper.close();
        	zookeeper = null;
        }
	}
	/**
	 * 初始化
	 */
	private void initialize(StringBuffer logtxt) throws Exception
	{
		String path = "/cos/config/program";
		Stat stat = zookeeper.exists(path, false);
		if( stat != null )
		{
			F f = new F(ConfigUtil.getWorkPath(), "bin/monitor.out");
			logtxt.append("Delete the node of program if data-timestamp("+
					Tools.getFormatTime("yy-MM-dd HH:mm", stat.getMtime())+"<"+
					Tools.getFormatTime("yy-MM-dd HH:mm", f.lastModified())+").\r\n");
			if( f.exists() && stat.getMtime() < f.lastModified() )
			{
				zookeeper.deleteNode(path);
				zookeeper.deleteNode("/cos/temp/program");
				stat = null;
				f.delete();
			}
		}
		if( stat == null )
		{
			zookeeper.createNode(path, "伺服器程序配置".getBytes("UTF-8"));
		}
		logtxt.append("Initialize the config of control from "+serverkey);
		path += "/"+serverkey;
		stat = zookeeper.exists(path, true);
		XMLParser xml = null;
		if( stat == null )
		{
			zookeeper.create(path, "".getBytes());
			stat = zookeeper.exists(path, true);
		}
//		byte[] payload = IOHelper.readAsByteArray(this.controlxml);
//		String controlxml = new String(payload, "UTF-8");
//		InputStreamReader reader = new InputStreamReader(fis, "UTF-8");
		Stat stat0 = zookeeper.exists(path+"/publish", true);
    	Stat stat1 = zookeeper.exists(path+"/switch", true);
    	if( stat1 == null )
    	{
			zookeeper.create(path+"/switch", "程序开关".getBytes("UTF-8"));
    	}
    	Stat stat2 = zookeeper.exists(path+"/debug", true);
    	if( stat2 == null )
    	{
			zookeeper.create(path+"/debug", "调测开关".getBytes("UTF-8"));
    	}
		xml = new XMLParser(controlxml);
    	subject = XMLParser.getElementAttr(xml.getRootNode(), "title");
    	Log.war("Subject = "+subject);
		if( stat0 == null )
		{
			logtxt.append("\r\n\tFirst load control.xml("+subject+") to publish.");
			zookeeper.create(path+"/publish", "伺服器程序发布目录".getBytes("UTF-8"));
	    	stat = zookeeper.setData(path, subject.getBytes("UTF-8"), stat.getVersion());
	        Node moduleNode = XMLParser.getElementByTag( xml.getRootNode(), "module" );
	        for( ; moduleNode != null; moduleNode = XMLParser.nextSibling(moduleNode) )
	        {
	        	String enable = XMLParser.getElementAttr(moduleNode, "enable");
	        	if( enable.equals("d") )
	        	{
	        		logtxt.append("\r\n\tDiscard the program of "+XMLParser.getElementAttr(moduleNode, "id"));
	        		continue;
	        	}
	        	
	        	JSONObject config = new JSONObject();
	        	boolean[] switchAndDebug = loadProgram(moduleNode, config);
	        	if( !config.has("id") ) continue;
	        	if( this.exclude(config) ) continue;
	        	String id = config.getString("id");
	        	String zkpath = path + "/publish/"+ id;
//	        	COSApi.createNode(zookeeper, zkpath, config.toString().getBytes("UTF-8"));
	        	zookeeper.create(zkpath, config.toString().getBytes("UTF-8"));
	        	
				zkpath = path+"/switch/"+id;
				String s = switchAndDebug[0]?"true":"false";
//				COSApi.createNode(zookeeper, zkpath, s.getBytes());
				zookeeper.create(zkpath, s.getBytes());

				zkpath = path+"/debug/"+id;
				String d = switchAndDebug[1]?"true":"false";
//	        	COSApi.createNode(zookeeper, zkpath, d.getBytes());
				zookeeper.create(zkpath, d.getBytes());
	        }
	        stat0 = zookeeper.exists(path+"/publish", true);
		}
		else
		{
			logtxt.append("\r\n\tPublish from control.xml("+subject+") done.");
			String title = new String(zookeeper.getData(path, false, stat0), "UTF-8");
			sendSubject();
			if( !title.equals(subject) )
			{
//				zookeeper.setData(path, subject.getBytes("UTF-8"), stat0.getVersion());
				logtxt.append("\r\n\tFound the subject from "+title+" change to "+subject);
			}
		}
		reloadTimestamp = stat0.getMtime();
		logtxt.append("\r\n\tThe timestamp for reload is "+reloadTimestamp+" "+Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", reloadTimestamp));
		logtxt.append("\r\n\tThe title loaded is "+subject);
		List<String> list = zookeeper.getChildren(path+"/publish", true);
		logtxt.append("\r\n\tWatch the programs("+list.size()+"):");
		for(String id : list)
		{
			this.loadConfig(path, id, logtxt);
		}
//		list = zookeeper.getChildren(path+"/switch", true);
//		logtxt.append("\r\n\tWatch the switch("+list.size()+"):");
//		list = zookeeper.getChildren(path+"/debug", true);
//		logtxt.append("\r\n\tWatch the debug("+list.size()+"):");
	}

	/**
	 * 因为ZK没有正常工作，从临时文件加载启动程序项
	 * @param logtxt
	 */
	private boolean tempinit(StringBuffer logtxt) throws Exception
	{
		File dir = new File(ConfigUtil.getWorkPath(), "config/program");
		if( !dir.exists() ) throw new Exception("程序配置本地临时目录不存在("+dir.getPath()+")");
		File files[] = dir.listFiles();
		logtxt.append("Load the temp configs from "+dir.getAbsolutePath());
		for( File file : files )
		{
			try
			{
				byte[] payload = IOHelper.readAsByteArray(file);
				String json = new String(payload, "UTF-8");
				JSONObject e = new JSONObject(json);
				this.programs.put(e.getString("id"), e);
				logtxt.append("\r\n\t["+e.getString("id")+"][switch:"+e.getBoolean("switch")+",debug:"+e.getBoolean("debug")+", ] "+e.getString("name"));
			}
			catch(Exception e)
			{
				logtxt.append("\r\n\tFailed to load the config of "+file.getAbsolutePath()+" for "+e);
			}
		}
		return true;
	}

	/**
	 * 排除一些配置不加载
	 * @param config
	 * @return
	 */
	private boolean exclude(JSONObject config)
	{
    	String tomcatpath = null;
    	if( config.has("startup") )
    	{
    		JSONObject startup = config.getJSONObject("startup");
    		if( startup.has("command") )
    		{
    			JSONArray commands = startup.getJSONArray("command");
    			for(int i = 0; i < commands.length(); i++)
    			{
    				String command = commands.getString(i);
                	if( command.startsWith("-Dcatalina.base=") )
                	{//发现启动tomcat，检查是否存在cos
                		tomcatpath = command.substring("-Dcatalina.base=".length());
                		break;
                	}
    			}
    		}
    	
    	}
    	if( tomcatpath != null )
    	{
        	F server_xml = new F(tomcatpath, "conf/server.xml");
        	if( !server_xml.exists() )
        	{
        		return false;
        	}
        	try
        	{
    			XMLParser parser = new XMLParser(server_xml);
    			Node nodeHost = XMLParser.getElementByTag(parser.getRootNode(), "Host");
    			F dir = null;
    			if( nodeHost != null )
    			{
    				String appBase = XMLParser.getElementAttr(nodeHost, "appBase");
                	dir = new F(tomcatpath, appBase);
                	if( !dir.exists() ) return false;
    				Node nodeContext = XMLParser.getChildElementByTag(nodeHost, "Context");
    		        for( ; nodeContext != null; nodeContext = nodeContext.getNextSibling() )
    		        {
    		            if( !nodeContext.getNodeName().equalsIgnoreCase( "Context" ) )
    		            {
    		                continue;
    		            }
    		            String docBase = XMLParser.getElementAttr(nodeContext, "docBase");
    		            if( docBase.endsWith("cos") )
    		            {
    		            	return true;
    		            }
    		        }
    			}
        	} 
        	catch (Exception e) {
    			Log.err("Failed to exclute cos-web load for error "+server_xml.getAbsolutePath());
    		}
    	}
		return false;
	}
	
	/**
	 * 从ZK中加载程序运行配置
	 * @param path
	 * @param id
	 * @throws Exception
	 */
	private JSONObject loadConfig(String path, String id, StringBuffer logtxt)
		throws Exception
	{
		String zkpath = path+"/publish/"+id;
		Stat stat = zookeeper.exists(zkpath, true);
		if( stat == null ) return null;
		JSONObject oldcfg = programs.get(id);
		if( oldcfg != null )
		{
			if( oldcfg.getLong("timestamp") == stat.getMtime() )
			{
				return null;
			}
		}
		
		byte[] payload = zookeeper.getData(zkpath, true, stat);
		JSONObject config = new JSONObject(new String(payload, "UTF-8"));
		config.put("timestamp", stat.getMtime());
		boolean isNew = oldcfg==null;
		String result = null;
		if( isNew || (result=this.compare(config, oldcfg)) != null )
		{
			zkpath = path+"/debug/"+id;
			stat = zookeeper.exists(zkpath, true);
			boolean d = stat != null && "true".equals(new String(zookeeper.getData(zkpath, true, stat)));
			config.put("debug", d);
			config.put("debug", d);

			zkpath = path+"/switch/"+id;
			stat = zookeeper.exists(zkpath, true);
			boolean s = stat != null && "true".equals(new String(zookeeper.getData(zkpath, true, stat)));
			config.put("switch", s);
			config.put("switch", s);
			
			programs.put(id, config);
			if( isNew )	config.put("new", isNew);
			logtxt.append("\r\n\t["+id+"][switch:"+config.getBoolean("switch")+",debug:"+config.getBoolean("debug")+", "+(isNew?"add":"update")+"] "+config.getString("name"));
			if( result != null ) logtxt.append("\r\n\tFound change("+result+").");
			return config;
		}
		return null;
	}

	/**
	 * 重新加载control.xml
	 */
	private void reload()
	{
		XMLParser xml = null;
		StringBuffer logtxt = new StringBuffer("Reload the config of control from "+controlxml.getAbsolutePath());
    	try
    	{
    		Stat stat = null;
    		xml = new XMLParser(controlxml);
	    	String title = XMLParser.getElementAttr(xml.getRootNode(), "title");
			String zkpath = "/cos/config/program/"+serverkey;
			stat = zookeeper.exists(zkpath, true);
			if( stat != null )
			{
				stat = zookeeper.setData(zkpath, title.getBytes("UTF-8"), stat.getVersion());
				reloadTimestamp = stat.getMtime();
				if( !title.equals(subject) )
					logtxt.append("\r\n\tThe title change to "+title);
			}
	    	
	        Node moduleNode = XMLParser.getElementByTag( xml.getRootNode(), "module" );
	        for( ; moduleNode != null; moduleNode = XMLParser.nextSibling(moduleNode) )
	        {
        		String id = XMLParser.getElementAttr(moduleNode, "id");
        		String path = "/cos/config/program/"+serverkey+"/publish/"+id;
	        	String enable = XMLParser.getElementAttr(moduleNode, "enable");
	        	if( enable.equals("d") )
	        	{//control.xml配置的删除直接删
	        		programs.remove(id);
	    			stat = zookeeper.exists(path, true);
	        		if( stat != null )
	        		{
	        			logtxt.append("\r\n\tDelete the config of program("+id+") by control.xml.");
	        			zookeeper.delete(path, stat.getVersion());
	        		}
	        		else
	        		{
	        			logtxt.append("\r\n\tFailed to delete the config of program("+id+") by control.xml for not found "+path);
	        		}
	        		continue;
	        	}
	        	JSONObject config = new JSONObject();
	        	boolean[] switchAndDebug = loadProgram(moduleNode, config);
	        	if( !config.has("id") ) continue;
	        	if( this.exclude(config) ) continue;
	        	JSONObject oldcfg = programs.get(id);
	        	if( oldcfg != null )
	        	{
	        		String result = this.compare(config, oldcfg);
	        		if( result == null ) 
	        		{
	        			logtxt.append("\r\n\tNot found change of program("+id+").");
	        			continue;
	        		}
	        		logtxt.append("\r\n\tFound change of program("+id+") for "+result);
	        	}
	        	else
	        	{
	        		logtxt.append("\r\n\tFound new of program("+id+").");
	        	}
	        	String ip = Tools.getLocalIP();
	        	config.put("ip", ip);
	        	config.put("port", System.getProperty("control.port", "9075"));
	        	SyspublishClient.publish(
	        			config,
	        			!programs.containsKey(id),
	        			"伺服器["+ip+"]程序配置引擎通过control.xml加载程序配置("+id+")",
	        			"#程序配置引擎");
	        	logtxt.append("\r\n\t\t"+config.toString());
	        	
	        	if( oldcfg != null )
	        	{
		        	if( oldcfg.has("switch") && oldcfg.getBoolean("switch") != switchAndDebug[0] )
		        	{
		        		logtxt.append("\r\n\t\tSwitch to "+switchAndDebug[0]);
		        		oldcfg.put("switch", switchAndDebug[0]);
		        		this.sendSwitch(id, switchAndDebug[0]);
		        	}
		        	
		        	if( oldcfg.has("debug") && oldcfg.getBoolean("debug") == switchAndDebug[1] )
		        	{
		        		logtxt.append("\r\n\t\tDebug to "+switchAndDebug[1]);
		        		oldcfg.put("debug", switchAndDebug[1]);
		        		this.sendDebug(id, switchAndDebug[1]);
		        	}
	        	}
	        }
        }
		catch(Exception e)
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
			PrintStream ps = new PrintStream(out);
			e.printStackTrace(ps);
			logtxt.append("\r\nFailed to reload for exception");
			logtxt.append(out.toString());
			ps.close();
			opened = false;
			logtxt.append("\r\nRestartup this program.");
		}
		finally
		{
			Log.msg(logtxt.toString());
		}
	}
	
	/**
	 * 处理ZK节点变化，发送指令给主控引擎
	 */
	public void doProcess(WatchedEvent event)
	{
		StringBuffer logtxt = new StringBuffer("Receive event("+event.getType()+") from "+event.getPath());
		try
		{
			if( event.getPath() == null )
			{
				logtxt.append("\r\n\tDiscard for path null.");
				return;
			}
			if( event.getPath().endsWith(this.serverkey) )
			{//伺服器微服务架构名称发生改变
				if( event.getType() == EventType.NodeCreated ||
					event.getType() == EventType.NodeDataChanged )
				{
					Stat stat = this.zookeeper.exists(event.getPath(), true);
					if( stat != null )
					{
						byte[] data = this.zookeeper.getData(event.getPath(), true, stat);
						this.subject = new String(data, "UTF-8");
						if( !subject.isEmpty() ) this.sendSubject();
						logtxt.append("\r\n\tNotify the subject("+this.subject+") of server to control.");
					}
					else
					{
						logtxt.append("\r\n\tNot found the node of program-config.");
					}
				}
				else if( event.getType() == EventType.NodeDeleted )
				{
					logtxt.append("\r\n\tWill be remove all programs...");
		        	Iterator<JSONObject> iterator = this.programs.values().iterator();
		        	while( iterator.hasNext() )
		        	{
		        		JSONObject config = iterator.next();
		        		String id = config.getString("id");
		        		logtxt.append("\r\n\t\t["+id+"].");
		        	}
				}
				else if( event.getType() == EventType.NodeChildrenChanged )
				{
					logtxt.append("\r\n\tFound children-changed(It's impossible to come here).");
				}
				return;
			}
			if( event.getPath().endsWith("/publish") )
			{//publish节点发生变化
				String path = event.getPath().substring(0, event.getPath().length()-"/publish".length());
				if(event.getType() == EventType.NodeDeleted )
				{//如果publish节点被撤掉删除，那么通知所有程序下架，记录删除到/cos/config/program/<>/removed/
					logtxt.append("\r\n\tWill remove all programs...");
		        	Iterator<JSONObject> iterator = this.programs.values().iterator();
		        	while( iterator.hasNext() )
		        	{
		        		JSONObject config = iterator.next();
		        		String id = config.getString("id");
		        		logtxt.append("\r\n\t\t["+id+"].");
		        	}
				}
				else if( event.getType() == EventType.NodeChildrenChanged )
				{//publish目录下的节点发生改变，有3种情况新增和修改，以及删除
					List<String> list = zookeeper.getChildren(event.getPath(), true);
					for(String id : list)
					{//遍历所有节点，发现是否有新增或修改。还要注意一个情况是如果子节点被删除是否会收到事件通知。
						JSONObject config = loadConfig(path, id, logtxt);
						if( config != null )
						{
							if( config.has("new") )
							{
								logtxt.append("\r\n"+config.toString());
				        		sendConfig(config);
							}
						}
//						else
//						{
//							logtxt.append("\r\n\t\tNot found change of program("+id+").");
//						}
					}
					logtxt.append("\r\n\t..."+list.size()+" publsh.");
				}
				return;
			}
			/*if( event.getPath().endsWith("/switch") )
			{//switch节点发生变化
				if(event.getType() == EventType.NodeDeleted )
				{
					sb.append("\r\n\tWill be abort all programs running...");
		        	Iterator<JSONObject> iterator = this.programs.values().iterator();
		        	while( iterator.hasNext() )
		        	{
		        		JSONObject config = iterator.next();
		        		String id = config.getString("id");
		        		sb.append("\r\n\t\t["+id+"].");
		        	}
				}
				return;
			}
			if( event.getPath().endsWith("/debug") )
			{//debug节点发生变化
				if(event.getType() == EventType.NodeDeleted )
				{
					sb.append("\r\n\tWill be abort all programs debug...");
		        	Iterator<JSONObject> iterator = this.programs.values().iterator();
		        	while( iterator.hasNext() )
		        	{
		        		JSONObject config = iterator.next();
		        		String id = config.getString("id");
		        		sb.append("\r\n\t\t["+id+"].");
		        	}
				}
				return;
			}*/
			final String t0 = "/publish/";
			final String t1 = "/switch/";
			final String t2 = "/debug/";
			String tag = null;
			int i = 0, j = 0, k = -1;
			if( (i=event.getPath().indexOf(t0)) != -1 )
			{
				j = i + t0.length();
				k = 0;
				tag = t0.replace("/", "");
			}
			else if( (i=event.getPath().indexOf(t1)) != -1 )
			{
				j = i + t1.length();
				k = 1;
				tag = t1.replace("/", "");
			}
			else if( (i=event.getPath().indexOf(t2)) != -1 )
			{
				j = i + t2.length();
				k = 2;
				tag = t2.replace("/", "");
			}
			else
			{
				logtxt.append("\r\n\tNot found the handler for event.");
				return;
			}
			String id = event.getPath().substring(j);//程序编号
			JSONObject config = programs.get(id);
			if( config == null )
			{
				logtxt.append("\r\n\tNot found the config("+id+", "+tag+").");
				return;
			}
			logtxt.append("\r\n\tFound the config("+id+", "+tag+").");
			switch(k)
			{
			case 1:
			case 2:
				if(event.getType() == EventType.NodeDeleted )
				{
					config.put(tag, false);
					sendSwitch(id, false);
					logtxt.append("\r\n\t\tSucceed to set "+tag+" fasle.");
				}
				else if( event.getType() == EventType.NodeDataChanged ||
						 event.getType() == EventType.NodeCreated )
				{
					Stat stat = this.zookeeper.exists(event.getPath(), true);
					if( stat != null )
					{
						byte[] data = this.zookeeper.getData(event.getPath(), true, stat);
						String str = new String(data, "UTF-8");
						if( k == 1 ) this.sendSwitch(id, Boolean.valueOf(str));
						else this.sendDebug(id, Boolean.valueOf(str));
						logtxt.append("\r\n\t\tSucceed to set "+tag+" "+str+".");
					}
					else
					{
						logtxt.append("\r\n\tFailed to set "+tag+" for not found zkpath.");
					}
				}
				break;
			case 0:
				String path = event.getPath().substring(0, i);
				if(event.getType() == EventType.NodeDeleted )
				{
					this.programs.remove(id);
					this.sendRemove(id);
					logtxt.append("\r\n\t\tSucceed to send the indication of remove("+id+").");
					String zkpath = path+"/removed";
					Stat stat = zookeeper.exists(zkpath, false);
					if( stat == null )
					{
						zookeeper.create(zkpath, "已删除的程序配置".getBytes("UTF-8"));
					}
					zkpath += "/"+id;
					stat = zookeeper.exists(zkpath, false);
					if( stat == null )
					{
						zookeeper.create(zkpath, config.toString().getBytes("UTF-8"));
					}
					else
					{
						zookeeper.setData(zkpath, config.toString().getBytes("UTF-8"), stat.getVersion());
					}
				}
				else if( event.getType() == EventType.NodeDataChanged )
				{
					config = this.loadConfig(path, id, logtxt);
					if( config != null )
					{
						logtxt.append("\r\n"+config.toString());
						this.sendConfig(config);
						logtxt.append("\r\n\t\tSucceed to set "+tag+".");
					}
					else
					{
						logtxt.append("\r\n\t\tNot change.");
					}
				}
				break;
			default:
				logtxt.append("\r\n\tIt's impossible to come here.");
				break;
			}
		}
		catch(Exception e)
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
			PrintStream ps = new PrintStream(out);
			e.printStackTrace(ps);
			logtxt.append("\r\nFailed to process watch.");
			logtxt.append(out.toString());
			ps.close();
		}
		finally
		{
			Log.msg(logtxt.toString());
		}
	}
	
	/**
	 * 发送伺服器程序标题给主控引擎
	 * @param title
	 * @throws Exception 
	 */
	private synchronized void sendSubject() throws Exception
	{
		System.out.write(0);
        String Version = Versions[Versions.length-1][0];
		System.out.write(Version.length());
		System.out.write(Version.getBytes());
		byte[] payload = subject.getBytes("UTF-8");
		Log.msg("Send .... "+subject+",,,"+payload.length);
		System.out.write(payload.length);
		System.out.write(payload);
		System.out.flush();
	}

	/**
	 * 开关程序
	 * @param id
	 * @param d
	 * @throws Exception
	 */
	private synchronized void sendSwitch(String id, boolean s) throws Exception
	{
		System.out.write(2);
		System.out.write(id.length());
		System.out.write(id.getBytes());
		System.out.write(s?1:0);
		System.out.flush();
		File file = new File(ConfigUtil.getWorkPath(), "config/program/"+Tools.encodeMD5(id));
		if( file.exists() ){
			byte[] payload = IOHelper.readAsByteArray(file);
			String json = new String(payload, "UTF-8");
			JSONObject e = new JSONObject(json);
			e.put("switch", s);
			IOHelper.writeFile(file, e.toString(4).getBytes("UTF-8"));
		}
	}
	/**
	 * 调试开关
	 * @param id
	 * @param d
	 * @throws Exception
	 */
	private synchronized void sendDebug(String id, boolean d) throws Exception
	{
		System.out.write(3);
		System.out.write(id.length());
		System.out.write(id.getBytes());
		System.out.write(d?1:0);
		System.out.flush();
		File file = new File(ConfigUtil.getWorkPath(), "config/program/"+Tools.encodeMD5(id));
		if( file.exists() ){
			byte[] payload = IOHelper.readAsByteArray(file);
			String json = new String(payload, "UTF-8");
			JSONObject e = new JSONObject(json);
			e.put("debug", d);
			IOHelper.writeFile(file, e.toString(4).getBytes("UTF-8"));
		}
	}
	/**
	 * 调试开关
	 * @param id
	 * @param d
	 * @throws Exception
	 */
	private synchronized void sendRemove(String id) throws Exception
	{
		System.out.write(4);
		System.out.write(id.length());
		System.out.write(id.getBytes());
		System.out.flush();
		File file = new File(ConfigUtil.getWorkPath(), "config/program/"+Tools.encodeMD5(id));
		if( file.exists() ) file.delete();
	}
	/**
	 * 发送伺服器程序配置给主控引擎
	 * @param e
	 * @throws Exception
	 */
	private synchronized void sendConfig(JSONObject e) throws Exception
	{
		System.out.write(1);
		byte[] payload = e.toString(4).getBytes("UTF-8");
		System.out.write(Tools.intToBytes(payload.length, 4));
		System.out.write(payload);
		System.out.flush();
		File file = new File(ConfigUtil.getWorkPath(), "config/program/"+Tools.encodeMD5(e.getString("id")));
		IOHelper.writeFile(file, e.toString(4).getBytes("UTF-8"));
	}
	
	private synchronized void shutdown()
	{
		Log.war("Close ...");
		opened = false;
		this.notifyAll();
	}

	/**
	 * 
	 * @return
	 */
	private boolean isZookeeperConnected()
	{
		if( zookeeper == null )
			try
			{
				String controlport = System.getProperty("control.port");
				if( Tools.isNumeric(controlport) ){
					this.zookeeper = new Zookeeper("127.0.0.1:"+controlport){
						@Override
						public void watch(WatchedEvent event) {
							// TODO Auto-generated method stub
							doProcess(event);
						}
					};
					Log.war(this.zookeeper.toString());
					return true;
				}
				else
				{
					Log.war("Not set the port or control "+controlport+".");
				}
			}
			catch(Exception e)
			{
				Log.err("Failed to setup the connection of zookeeper for exceptoin "+e);
				zookeeper = null;
			}
		return zookeeper!=null&&zookeeper.isConnected();
	}
	
	/**
	 * 
	 * @param left
	 * @param right
	 * @return
	 */
	private String compare(JSONObject left, JSONObject right)
	{
		if( !left.getString("id").equals(right.getString("id")) ) return "id:"+left.getString("id")+"<>"+right.getString("id");
		if( !left.getString("name").equals(right.getString("name")) ) return "name:"+left.getString("name")+"<>"+right.getString("name");
		if( !left.getString("version").equals(right.getString("version")) ) return "version:"+left.getString("version")+"<>"+right.getString("version");
		if( !left.getString("description").equals(right.getString("description")) ) return "description:"+left.getString("description")+"<>"+right.getString("description");

        String l_control = left.has("control")?Tools.encodeMD5(left.getJSONObject("control").toString()):"";
        String r_control = right.has("control")?Tools.encodeMD5(right.getJSONObject("control").toString()):"";
        if( !l_control.equals(r_control)) return "control:"+left.getJSONObject("control")+"<>"+right.getJSONObject("control");

        String l_maintenance = left.has("maintenance")?Tools.encodeMD5(left.getJSONObject("maintenance").toString()):"";
        String r_maintenance = right.has("maintenance")?Tools.encodeMD5(right.getJSONObject("maintenance").toString()):"";
        if( !l_maintenance.equals(r_maintenance)) return "maintenance:"+left.getJSONObject("maintenance")+"<>"+right.getJSONObject("maintenance");

        String l_startup = left.has("startup")?Tools.encodeMD5(left.getJSONObject("startup").toString()):"";
        String r_startup = right.has("startup")?Tools.encodeMD5(right.getJSONObject("startup").toString()):"";
        if( !l_startup.equals(r_startup)) return "startup:"+left.getJSONObject("startup")+"<>"+right.getJSONObject("startup");

        String l_shutdown = left.has("shutdown")?Tools.encodeMD5(left.getJSONObject("shutdown").toString()):"";
        String r_shutdown = right.has("shutdown")?Tools.encodeMD5(right.getJSONObject("shutdown").toString()):"";
        if( !l_shutdown.equals(r_shutdown)) return "shutdown:"+left.getJSONObject("shutdown")+"<>"+right.getJSONObject("shutdown");
		return null;
	}
	/**
	 * 通过Control.xml文件加载配置
	 * @param moduleNode
	 * @param config
	 * @param contr
	 * @return 0 switch; 1 debug
	 */
	public static boolean[] loadProgram(Node moduleNode, JSONObject config)
	{
		boolean[] switchAndDebug = new boolean[2];
        String id = XMLParser.getElementAttr( moduleNode, "id" );
        config.put("id", id);
//        ProgramConfig programConfig = new ProgramConfig(id);
        String name = XMLParser.getElementAttr( moduleNode, "name" );
        config.put("name", name);

        JSONObject control = new JSONObject();
        config.put("control", control);
        String restartup = XMLParser.getElementAttr( moduleNode, "restartup" );
        String forcereboot = XMLParser.getElementAttr( moduleNode, "forcereboot" );
        String enable = XMLParser.getElementAttr( moduleNode, "enable" );
        String mode = XMLParser.getElementAttr( moduleNode, "mode" );
        String logfile = XMLParser.getElementAttr( moduleNode, "logfile" );
        String pidfile = XMLParser.getElementAttr( moduleNode, "pidfile" );
        String cfgfile = XMLParser.getElementAttr( moduleNode, "cfgfile" );
        String dependence = XMLParser.getElementAttr( moduleNode, "dependence" );
        String delayed = XMLParser.getElementAttr( moduleNode, "delayed" );//延迟启动
        control.put("mode", Tools.isNumeric(mode)?Integer.parseInt(mode):Module.MODE_NORMAL);
        switchAndDebug[0] = enable.equals("t");
		control.put("restartup", Tools.isNumeric(restartup)?Integer.parseInt(restartup):0);
		control.put("delayed", Tools.isNumeric(delayed)?Integer.parseInt(delayed):0);
		control.put("logfile", logfile);
        control.put("pidfile", pidfile);
        control.put("cfgfile", cfgfile);
        control.put("dependence", dependence);
        if( !forcereboot.isEmpty() )
        {
    		JSONObject e = new JSONObject();
			String frmode = "";
			String frval = "";
			String frtime = "";
			String args[] = forcereboot.split(":");
			if( args.length == 1 )
			{
				frval = args[0];
			}
			else
			{
				frmode = args[0];
				frval = args[1];
				frtime = args[2]+":"+args[3];
			}
			e.put("mode", frmode);
			e.put("val", frval);
			e.put("time", frtime);	
    		control.put("forcereboot", e);
        }
        
        Node releaseNode = XMLParser.getElementByTag( moduleNode, "release" );
        if( releaseNode != null )
        {
	        String version = XMLParser.getElementAttr( releaseNode, "version" );
			if( version != null )
			{
				version = version.trim().toLowerCase();
				for(int i = 0; i < version.length(); i++)
				{
					char c = version.charAt(i);
					if( c >= '0' && c <= '9' )
					{
						version = version.substring(i);
						break;
					}
				}
				String args[] = Tools.split(version, ".");
				if( args.length > 0 ) version = args[0];
				else version = "0";
				if( args.length > 1 ) version += "."+args[1];
				else version += ".0";
				if( args.length > 2 ) version += "."+args[2];
				else version += ".0";
				if( args.length > 3 ) version += "."+args[3];
				else version += ".0";
			}
			else version = "0.0.0.0";
			config.put("version", version);
			config.put("description", XMLParser.getElementValue(releaseNode));
        }
        
        Node maintenanceNode = XMLParser.getElementByTag( moduleNode, "maintenance" );
        if( maintenanceNode != null )
    	{
    		JSONObject maintenance = new JSONObject();
    		maintenance.put("programmer", XMLParser.getElementAttr( maintenanceNode, "programmer" ));
    		maintenance.put("manager", XMLParser.getElementAttr( maintenanceNode, "manager" ));
        	String email = XMLParser.getElementAttr( maintenanceNode, "email" );
        	String remark = XMLParser.getElementValue(maintenanceNode);
            if( email.isEmpty() ) email = XMLParser.getElementValue(maintenanceNode);
            if( email.indexOf("@") != -1 ) maintenance.put("email", email);
            if( remark.indexOf("@") == -1 ) maintenance.put("remark", remark);
         	config.put("maintenance", maintenance);
    	}


        Node startupNode = XMLParser.getElementByTag( moduleNode, "startup" );
        if( startupNode != null )
        {
            String debug = XMLParser.getElementAttr( startupNode, "debug" );
            if( debug != null && debug.length() > 0 )
            {
            	switchAndDebug[1] = debug.equals("1");
            }
            
            // 加上获取模块许可时间
            String startTime = XMLParser.getElementAttr( startupNode, "start" );
            String endTime = XMLParser.getElementAttr( startupNode, "end" );
            if(startTime == null || startTime.isEmpty())
            {
            	startTime = null;
            }
            if(endTime == null || endTime.isEmpty())
            {
            	endTime = null;
            }
        	control.put("startime", startTime );
        	control.put("endtime", endTime );

            JSONObject startup = new JSONObject();
            config.put("startup", startup);
            ArrayList<String> command = new ArrayList<String>();
            ArrayList<String> remark = new ArrayList<String>();
            Node commandNode = XMLParser.getElementByTag( startupNode, "command" );
            for( ; commandNode != null; commandNode = XMLParser.nextSibling( commandNode ) )
            {
                String value = XMLParser.getElementValue( commandNode );
                command.add(value);
                value = XMLParser.getElementAttr( commandNode, "remark" );
                remark.add(value);
            }
            startup.put("command", command);
            startup.put("remark", remark);
            // 获取是否到达endtime时要关闭正在运行的该模块(默认不关闭模块，只是不再启动模块)
        }
        Node shutdownNode = XMLParser.getElementByTag( moduleNode, "shutdown" );
        if( shutdownNode != null )
        {
            JSONObject shutdown = new JSONObject();
            config.put("shutdown", shutdown);
            ArrayList<String> command = new ArrayList<String>();
            ArrayList<String> remark = new ArrayList<String>();
//            StringBuffer sbCommand = new StringBuffer();
//            StringBuffer sbRemark = new StringBuffer();
            Node commandNode = XMLParser.getElementByTag( shutdownNode, "command" );
            for( ; commandNode != null; commandNode = XMLParser.nextSibling( commandNode ) )
            {
                String value = XMLParser.getElementValue( commandNode );
                command.add(value);
//                sbCommand.append(value);
//                sbCommand.append(" ");
                value = XMLParser.getElementAttr( commandNode, "remark" );
                remark.add(value);
//                sbRemark.append(value);
//                sbRemark.append("\n");
            }
//            if( sbCommand.length() > 0 ) sbCommand.deleteCharAt(sbCommand.length()-1);
            shutdown.put("command", command);
            shutdown.put("remark", remark);
//            config.put("ShutdownCommands", sbCommand.toString());
//            config.put("ShutdownCommandsRemark", sbRemark.toString());
        }
        return switchAndDebug;
	}
}
