package com.focus.cos.web.ops.action;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Node;

import com.focus.control.ModulePerf;
import com.focus.control.SystemPerf;
import com.focus.cos.control.Command;
import com.focus.cos.control.Module;
import com.focus.cos.control.ProgramLoader;
import com.focus.cos.web.Version;
import com.focus.cos.web.common.HelperMgr;
import com.focus.cos.web.common.Kit;
import com.focus.cos.web.common.ZKMgr;
import com.focus.cos.web.ops.service.ControlMgr;
import com.focus.cos.web.ops.service.FilesMgr;
import com.focus.cos.web.ops.service.Monitor.RunFetchMonitor;
import com.focus.cos.web.ops.service.MonitorMgr;
import com.focus.cos.web.ops.vo.ModuleTrack;
import com.focus.cos.web.service.SvrMgr;
import com.focus.cos.web.user.service.UserMgr;
import com.focus.util.IOHelper;
import com.focus.util.Item;
import com.focus.util.Tools;
import com.focus.util.XMLParser;
import com.focus.util.Zookeeper;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

/**
 * 主控配置，设置各个服务引擎程序
 * @author focus
 *
 */
public class ControlAction extends OpsAction
{
	private static final long serialVersionUID = 1L;
	public static final Log log = LogFactory.getLog(ControlAction.class);
	/*远过程调用*/
	private ControlMgr controlMgr;
	//用户管理器
	private UserMgr userMgr;
	/*文件管理器*/
	protected FilesMgr filesMgr;
	/*主控XML*/
	private String controlXml;
	/*服务器引擎配置
	private ProgramConfig serviceConfig;*/
	/*主控命令*/
	private int command = -1;
	/*主机的唯一标识*/
	private String serverkey;
	/*主机的操作系统类型*/
	private String servertype;
	
	public ControlAction()
	{
		editable = true;
	}
	
	/**
	 * 上传单个程序配置
	 * @return
	 */
	public String uploadcfg()
	{
		JSONObject rsp = new JSONObject();
		if( this.uploadfile == null || !uploadfile.exists() )
		{
			rsp.put("alt", "上传文件["+path+"]到集群伺服器【"+ip+"】失败，因为未能收到文件包");
			return response(super.getResponse(), rsp.toString());
		}
		if( serverkey == null || serverkey.isEmpty() || !Tools.isNumeric(id) )
		{
			rsp.put("alt", "上传程序配置脚本到集群伺服器【"+ip+"】失败，因为未能收到伺服器ID或编码");
			return response(super.getResponse(), rsp.toString());
		}

		log.info("Upload program.json from tempfile "+uploadfile+" to "+ip+":"+port+"+serverkey+");
		Zookeeper zookeeper = null;
		String context = "读取脚本文件出错";
        try
        {
        	context = new String(IOHelper.readAsByteArray(uploadfile), "UTF-8");
        	JSONObject config = new JSONObject(context);
            log.info("Succeed to parse the program.json.");
			zookeeper = Zookeeper.getInstance(ip, port);
			JSONObject e = null;
			HashMap<String, JSONObject> mapOld = new HashMap<String, JSONObject>();
			JSONArray userPrograms = new JSONArray();
        	this.controlMgr.setUserPrograms(serverkey, zookeeper.i(), userPrograms);
			for( int i = 0; i < userPrograms.length(); i++)
	        {
				e = userPrograms.getJSONObject(i);
				mapOld.put(e.getString("id"), e);
	        }
			e = this.controlMgr.getUnconstructProgramConfig(config);
			
	        e.put("oper", mapOld.containsKey(config.getString("id"))?ProgramLoader.OPER_EDITING:ProgramLoader.OPER_ADDING);
	        e.put("port", port);
	        e.put("operlog", "通过程序配置脚本上传");
			this.controlMgr.saveProgramConfig(zookeeper.i(), ip, serverkey, e, super.getUserAccount(), this.getServerId());
			this.controlMgr.loadProgramProfiles(e);
            
            this.responseMessage = "通过上传程序配置脚本添加程序配置到集群伺服器【"+ip+"】成功";
    		logoper(responseMessage, "程序管理", null, "control!preview.action?ip="+ip+"&port="+port+"&serverkey="+serverkey+"&id="+config.getString("id"));
			rsp.put("alt", responseMessage);
			rsp.put("succeed", true);
			rsp.put("result", config.toString());
    		SvrMgr.sendNotiefiesToSystemadmin(
    				super.getRequest(),
					"集群程序管理",
					String.format("用户[%s]"+responseMessage, super.getUserAccount()),
					"上传的程序配置如下图所示，程序配置申请待系统管理员审核",
					"control!preview.action?ip="+ip+"&port="+port+"&serverkey="+serverkey+"&id="+config.getString("id"),
                    "程序管理", "control!open.action?id="+id);
        }
        catch(Exception e)
        {
        	e.printStackTrace();
            this.responseException = "通过上传程序配置脚本添加程序配置到集群伺服器【"+ip+"】因为无法正确解析上传文件出现异常"+e.getMessage();
    		logoper(responseException, "程序管理", null, null, e);
			rsp.put("alt", responseException);
    		SvrMgr.sendNotiefiesToSystemadmin(
    				super.getRequest(),
					"集群程序管理",
					String.format("用户[%s]"+responseException, super.getUserAccount()),
					context,
					null,
                    "情况确认", "#feedback?to="+super.getUserAccount());
        }
		finally
		{
			if( zookeeper != null ) zookeeper.close();
		}
		return this.response(super.getResponse(), rsp.toString());
	}
	
	/**
	 * 上传主控配置，配置进入待审核
	 * @return
	 */
	public String uploadxml()
	{
		JSONObject rsp = new JSONObject();
		if( this.uploadfile == null || !uploadfile.exists() )
		{
			rsp.put("alt", "上传文件["+path+"]到集群伺服器【"+ip+"】失败，因为未能收到文件包");
			return response(super.getResponse(), rsp.toString());
		}
		if( serverkey == null || serverkey.isEmpty() || !Tools.isNumeric(id) )
		{
			rsp.put("alt", "上传主控配置到集群伺服器【"+ip+"】失败，因为未能收到伺服器ID或编码");
			return response(super.getResponse(), rsp.toString());
		}

		log.info("Upload control.xml from tempfile "+uploadfile+" to "+ip+":"+port+"+serverkey+");
		XMLParser xml = null;
		Zookeeper zookeeper = null;
        try
        {
        	xml = new XMLParser(uploadfile);
            log.info("Succeed to parse the control.xml.");
			zookeeper = Zookeeper.getInstance(ip, port);
			JSONObject e = null;
			HashMap<String, JSONObject> mapOld = new HashMap<String, JSONObject>();
			JSONArray userPrograms = new JSONArray();
        	this.controlMgr.setUserPrograms(serverkey, zookeeper.i(), userPrograms);
			for( int i = 0; i < userPrograms.length(); i++)
	        {
				e = userPrograms.getJSONObject(i);
				mapOld.put(e.getString("id"), e);
	        }
			
			JSONArray array = new JSONArray();
	        Node moduleNode = XMLParser.getElementByTag( xml.getRootNode(), "module" );
	        StringBuffer sb = new StringBuffer("上传的程序配置如下表所示详情请打开【程序管理】，程序配置申请待系统管理员审核");
	        for( ; moduleNode != null; moduleNode = XMLParser.nextSibling(moduleNode) )
	        {
            	JSONObject config = new JSONObject();
            	ProgramLoader.loadProgram(moduleNode, config);
				e = config;
				e.put("oper", mapOld.containsKey(config.getString("id"))?ProgramLoader.OPER_EDITING:ProgramLoader.OPER_ADDING);
				e.put("port", port);
				e.put("operlog", "通过主控配置文件上传");
				String rspmsg = this.controlMgr.saveProgramConfig(zookeeper.i(), ip, serverkey, e, super.getUserAccount(), this.getServerId());
				sb.append("\r\n\t"+rspmsg);
				JSONObject maintenance = e.has("maintenance")?e.getJSONObject("maintenance"):null;
				e.put("programmer", maintenance!=null?maintenance.getString("programmer"):"N/A");
	    		e.put("icon", "images/icons/tile.png");
	    		e.put("title", e.getString("id"));
				this.controlMgr.loadProgramProfiles(e);
				array.put(e);
	        }
            
            this.responseMessage = "通过上传主控配置文件添加程序配置到集群伺服器【"+ip+"】成功";
    		logoper(responseMessage, "程序管理", sb.toString(), null);
			rsp.put("alt", responseMessage);
			rsp.put("succeed", true);
			rsp.put("result", array.toString());
    		SvrMgr.sendNotiefiesToSystemadmin(
    				super.getRequest(),
					"集群程序管理",
					String.format("用户[%s]"+responseMessage, super.getUserAccount()),
					sb.toString(),
					null,
                    "程序管理", "control!open.action?id="+id);
        }
        catch(Exception e)
        {
            this.responseException = "通过上传主控配置文件添加程序配置到集群伺服器【"+ip+"】因为无法正确解析上传文件出现异常"+e.getMessage();
    		logoper(responseException, "程序管理", null, null, e);
			ByteArrayOutputStream out1 = new ByteArrayOutputStream(1024);
			rsp.put("alt", responseException);
			PrintStream ps = new PrintStream(out1);
    		e.printStackTrace(ps);
    		SvrMgr.sendNotiefiesToSystemadmin(
    				super.getRequest(),
					"集群程序管理",
					String.format("用户[%s]"+responseException, super.getUserAccount()),
					out1.toString(),
					null,
                    "情况确认", "#feedback?to="+super.getUserAccount());
        }
		finally
		{
			if( zookeeper != null ) zookeeper.close();
		}
		return this.response(super.getResponse(), rsp.toString());
	}


	/**
	 * 浏览日志
	 * 发送包:[命令字:1][模块ID字符串长度:1][模块ID:n]
	 * 响应包:[结束标识:1][字符串长度:2][字符串:n]
	 * 确认包：[标记:1](0表示继续，1表示中止)
	 * @return
	 */
	public String downloadcfg()
	{
		log.info("Download "+id+".cfg from "+ip+":"+port);
    	ZooKeeper zookeeper = null;
    	ServletOutputStream out = null;
        try
        {
        	zookeeper = loadProgramConfig();
	        if( localDataObject == null )
	        {
	        	this.responseException = "未找到您要下载的程序配置["+id+"]";
	        	return "alert";
	        }
        	String filename = id+".json";
    		JSONObject config = this.controlMgr.resetConfig(localDataObject);//.controlMgr.getConstructProgramConfig(this.localDataObject);
    		JSONObject startup = null;
    		if( !config.has("startup") )
    		{
    			startup = new JSONObject();
    			config.put("startup", startup);
    		}
    		else startup = config.getJSONObject("startup");
    		if( !startup.has("command") ) startup.put("command", new JSONArray());
    		if( !startup.has("remark") ) startup.put("remark", new JSONArray());

    		JSONObject shutdown = null;
    		if( !config.has("shutdown") )
    		{
    			shutdown = new JSONObject();
    			config.put("shutdown", shutdown);
    		}
    		else shutdown = config.getJSONObject("shutdown");
    		if( !shutdown.has("command") ) shutdown.put("command", new JSONArray());
    		if( !shutdown.has("remark") ) shutdown.put("remark", new JSONArray());
    		
			getResponse().setContentType("application/json;charset=UTF-8");
            getResponse().setHeader("Content-disposition", "attachment; filename*=UTF-8''"+URLEncoder.encode(filename,"UTF-8"));
    		out = getResponse().getOutputStream();
    		out.write(config.toString(4).getBytes("UTF-8"));			
    		out.flush();
            this.responseMessage = "从集群伺服器【"+ip+"】下载程序["+id+"]主控配置文件成功。";
    		logoper(responseMessage, "程序管理", localDataObject.toString(4), null);
    		SvrMgr.sendNotiefiesToSystemadmin(
    				super.getRequest(),
					"集群文件管理",
					String.format("用户[%s]"+responseMessage, super.getUserAccount()),
					localDataObject.toString(4),
					null,
                    "情况确认", "#feedback?to="+super.getUserAccount());
        }
        catch(Exception e)
        {
        	this.responseException ="从集群伺服器【"+ip+"】下载程序["+id+"]主控配置文件出现异常 "+e.getMessage();
    		logoper(responseException, "程序管理", null, null, e);
			ByteArrayOutputStream out1 = new ByteArrayOutputStream(1024);
			PrintStream ps = new PrintStream(out1);
    		e.printStackTrace(ps);
    		SvrMgr.sendNotiefiesToSystemadmin(
    				super.getRequest(),
					"集群文件管理",
					String.format("用户[%s]"+responseException, super.getUserAccount()),
					out1.toString(),
					null,
                    "情况确认", "#feedback?to="+super.getUserAccount());
        	return "alert";
        }
        finally
        {
    		try
			{
    			if( zookeeper != null ) zookeeper.close();
            	if( out != null ) out.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
        }
		return null;
	}
	/**
	 * 浏览日志
	 * 发送包:[命令字:1][模块ID字符串长度:1][模块ID:n]
	 * 响应包:[结束标识:1][字符串长度:2][字符串:n]
	 * 确认包：[标记:1](0表示继续，1表示中止)
	 * @return
	 */
	public String downloadxml()
	{
		log.info("Download control.xml from "+ip+":"+port);
    	Zookeeper zookeeper = null;
    	ServletOutputStream out = null;
        try
        {
			zookeeper = Zookeeper.getInstance(ip, port);
			String path = "/cos/config/program/"+serverkey;
			String title = zookeeper.getUTF8(path);
			JSONArray userPrograms = new JSONArray();
	        this.controlMgr.setUserPrograms(serverkey, zookeeper.i(), userPrograms);
	        StringBuffer xml = new StringBuffer("<?xml version='1.0' encoding='UTF-8'?>");
	        xml.append("\r\n<sys title='"+title+"'>");
	        for( int i = 0; i < userPrograms.length(); i++ )
	        {
	        	JSONObject program = userPrograms.getJSONObject(i);
				StringBuffer module = new StringBuffer();
				JSONObject control = program.has("control")?program.getJSONObject("control"):null;
				StringBuffer c = new StringBuffer();
				if( control != null )
				{
					c.append(" mode='"+(control.has("mode")?control.getInt("mode"):0)+"'");
					c.append(" restartup='"+(control.has("restartup")?control.getInt("restartup"):0)+"'");
					c.append(" delayed='"+(control.has("delayed")?control.getInt("delayed"):0)+"'");
					c.append(" dependence='"+(control.has("dependence")?control.getString("dependence"):"")+"'");
					c.append(" logfile='"+(control.has("logfile")?control.getString("logfile"):"")+"'");
					c.append(" pidfile='"+(control.has("pidfile")?control.getString("pidfile"):"")+"'");
					c.append(" cfgfile='"+(control.has("cfgfile")?control.getString("cfgfile"):"")+"'");
					JSONObject forcereboot = control.has("forcereboot")?control.getJSONObject("forcereboot"):null;
			        if( forcereboot != null )
			        {
			        	String frmode = forcereboot.has("mode")?forcereboot.getString("mode"):"";
			        	String frval = forcereboot.has("val")?forcereboot.getString("val"):"";
			        	String frtime = forcereboot.has("time")?forcereboot.getString("time"):"";
						c.append(" forcereboot='"+(frmode + frval + frtime)+"'");
			        }
				}
				module.append("\r\n\t<module id='"+program.getString("id")+"' name='"+program.getString("name")+"' enable='"+(program.getBoolean("switch")?"t":"f")+"'"+c+">");
				module.append("\r\n\t\t<release version='"+program.getString("version")+"'>"+program.getString("description")+"</release>");
				JSONObject maintenance = program.has("maintenance")?program.getJSONObject("maintenance"):null;
				if( maintenance != null )
				{
					module.append("\r\n\t\t<maintenance programmer='"+(maintenance.has("programmer")?maintenance.getString("programmer"):"")+
							"' manager='"+(maintenance.has("manager")?maintenance.getString("manager"):"")+
							"' email='"+(maintenance.has("email")?maintenance.getString("email"):"")+
							"'>"+(maintenance.has("remark")?maintenance.getString("remark"):"")+"</maintenance>");
				}
				JSONObject startup = program.has("startup")?program.getJSONObject("startup"):null;
				if( startup != null )
				{
					String se = "";
					if( control != null )
					{
			        	String starttime = control.has("starttime")?control.getString("starttime"):"";
			        	String endtime = control.has("endtime")?control.getString("endtime"):"";
			        	if( !starttime.isEmpty() )
			        	{
			        		se += "start='"+starttime+"'";
			        	}
			        	if( !endtime.isEmpty() )
			        	{
			        		se += "end='"+endtime+"'";
			        	}
					}
					
					module.append("\r\n\t\t<startup debug='"+(program.getBoolean("debug")?"1":"0")+"'"+se+">");
					JSONArray command = startup.has("command")?startup.getJSONArray("command"):null;
					if( command != null )
					{
						JSONArray remark = startup.has("remark")?startup.getJSONArray("remark"):null;
						for( int j = 0; j < command.length(); j++ )
						{
							String r = "";
							if( remark != null && j < remark.length() )
								r = " remark='"+remark.getString(j)+"'";
							module.append("\r\n\t\t\t<command"+r+">"+command.getString(j)+"</command>");
						}
					}
					module.append("\r\n\t\t</startup>");
				}

				JSONObject shutdown = program.has("shutdown")?program.getJSONObject("shutdown"):null;
				if( shutdown != null )
				{
					module.append("\r\n\t\t<shutdown>");
					JSONArray command = shutdown.has("command")?shutdown.getJSONArray("command"):null;
					if( command != null )
					{
						JSONArray remark = shutdown.has("remark")?shutdown.getJSONArray("remark"):null;
						for( int j = 0; j < command.length(); j++ )
						{
							String r = "";
							if( remark != null && j < remark.length() )
								r = " remark='"+remark.getString(j)+"'";
							module.append("\r\n\t\t\t<command"+r+">"+command.getString(j)+"</command>");
						}
					}
					module.append("\r\n\t\t</shutdown>");
				}
				module.append("\r\n\t</module>");
				xml.append(module.toString());
	        }
	        xml.append("\r\n</sys>");
			
	        byte[] payload = xml.toString().getBytes("UTF-8");
        	String filename = "control.xml";
			getResponse().setContentType("application/xml;charset=UTF-8");
            getResponse().setHeader("Content-disposition", "attachment; filename*=UTF-8''"+URLEncoder.encode(filename,"UTF-8"));
    		out = getResponse().getOutputStream();
    		out.write(payload);
    		out.flush();
            this.responseMessage = "从集群伺服器【"+ip+"】下载主控配置文件成功。";
    		logoper(responseMessage, "程序管理", new String(payload, "UTF-8"), null);
    		SvrMgr.sendNotiefiesToSystemadmin(
    				super.getRequest(),
					"集群文件管理",
					String.format("用户[%s]"+responseMessage, super.getUserAccount()),
					new String(payload, "UTF-8"),
					null,
                    "情况确认", "#feedback?to="+super.getUserAccount());
        }
        catch(Exception e)
        {
        	this.responseException ="从集群伺服器【"+ip+"】下载主控配置文件出现异常 "+e.getMessage();
    		logoper(responseException, "程序管理", null, null, e);
			ByteArrayOutputStream out1 = new ByteArrayOutputStream(1024);
			PrintStream ps = new PrintStream(out1);
    		e.printStackTrace(ps);
    		SvrMgr.sendNotiefiesToSystemadmin(
    				super.getRequest(),
					"集群文件管理",
					String.format("用户[%s]"+responseException, super.getUserAccount()),
					out.toString(),
					null,
                    "情况确认", "#feedback?to="+super.getUserAccount());
        	return "alert";
        }
        finally
        {
    		try
			{
    			if( zookeeper != null ) zookeeper.close();
            	if( out != null ) out.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
        }
		return null;
	}
	
	/**
	 * 集群伺服器的导航
	 * @return
	 */
	public String navigate()
	{
		JSONObject privileges;
		try {
			privileges = this.getMonitorMgr().getClusterPrivileges(super.getUserRole(), super.getUserAccount());
			JSONArray clusters = this.getMonitorMgr().getClusterTree(privileges, super.isSysadmin(), false);
			if( Tools.isNumeric(id) )
			{
				clusters = this.getMonitorMgr().getClusterTree(clusters, Integer.parseInt(id));
			}
			jsonData = "[]";
			if( clusters == null )
			{
				clusters = new JSONArray();
			}
			this.grant = super.isSysadmin();
			jsonData = clusters.toString();
		} catch (Exception e) {
			super.responseException = e.getMessage();
		}
		return "navigate";
	}

	/**
	 * 集群伺服器程序管理
	 * @return
	 */
	public String open()
	{
		JSONObject server = null;
		
		if( id != null ) server = this.getMonitorMgr().getServer(this.getServerId());
		else if( serverkey != null ) server = this.getMonitorMgr().getServer(serverkey);
		log.info("Open the programs of "+server+" by id "+id+" or "+serverkey);
		if( server == null )
		{
			this.setResponseException("未能找到您要打开的伺服器。");
			return "404";
		}
		ip = server.getString("ip");
		port = server.getInt("port");
		viewTitle = "【伺服器("+server.getString("title")+")】";
//				if( !server.has("state") || server.getInt("state") == 0 )
		if( this.getMonitorMgr().getState(this.getServerId()) == 0 )
		{
			this.setResponseException("伺服器监控未启动，不能打开其程序管理器。");
			return "alert";
		}
        JSONArray trees = new JSONArray();
		Zookeeper zookeeper = null;
		String zkpath = null;
		JSONObject e = null;
        JSONArray userPrograms = new JSONArray();
        JSONArray sysPrograms = new JSONArray();
        JSONArray cfgPrograms = new JSONArray();
		try 
		{
			this.grant = super.isSysadmin();
			if( !grant )
			{
				JSONObject privileges = this.getMonitorMgr().getServerPrivileges(super.getUserRole(), super.getUserAccount(), this.getMonitorMgr().getServer(this.getServerId()));
				grant = privileges.has("control")&&privileges.getBoolean("control");
			}
	        server.put("name", "已发布用户服务引擎程序");
	        server.put("type", "user");
	        server.put("icon", "images/icons/user.png");
	        server.put("children", userPrograms);
	        trees.put(server);
	        String serverTitle = server.getString("title");

	        String sk = "";
	        if( server.has("security-key") )
	        {
	        	sk = server.getString("security-key");
	        	this.serverkey = Tools.encodeMD5(sk);
	        }
	        SystemPerf sysPerf = getMonitorMgr().getSystemPerf(ip, port, server.getInt("id"));
	        servertype = sysPerf!=null?sysPerf.getPropertyValue("OSName"):"N/A";
	        servertype = servertype.isEmpty()?"N/A":servertype;
	        this.controlMgr.setContainerInfo(sysPerf, server);
	        this.localDataObject = server;
	        HashMap<String, ModuleTrack> mapModuleTrack = this.controlMgr.setSysPrograms(sysPerf, sysPrograms);
	        
			zookeeper = Zookeeper.getInstance(ip, port);
	        this.controlMgr.setUserPrograms(serverkey, zookeeper.i(), userPrograms);

			zkpath = "/cos/temp/program/publish";//+serverkey+;
			Stat stat = ZKMgr.getZookeeper().exists(zkpath, false);
			if( stat != null )
			{
				List<String> pubs = ZKMgr.getZookeeper().getChildren(zkpath, false);
				for(String pub : pubs)
				{
					path = zkpath+"/"+pub;
					if( !pub.startsWith(this.serverkey) ) continue;
					e = new JSONObject(new String(ZKMgr.getZookeeper().getData(path, false, stat), "UTF-8"));
					cfgPrograms.put(e);
				}
			}

			//获取伺服器程序容器下所有配置程序的管理员
	        HashMap<String, Boolean> filter = new HashMap<String, Boolean>();
	        ArrayList<Item> programers = new ArrayList<Item>();
        	this.listData = programers;	        
			for( int i = 0; i < userPrograms.length(); i++)
	        {
				e = userPrograms.getJSONObject(i);
				JSONObject maintenance = e.has("maintenance")?e.getJSONObject("maintenance"):null;
				e.put("programmer", maintenance!=null?maintenance.getString("programmer"):"N/A");
	    		e.put("icon", "images/icons/tile.png");
	    		e.put("title", e.getString("id"));
				if( maintenance != null && maintenance.has("programmer") && filter.containsKey(maintenance.getString("programmer")))
	        	{
	        		filter.put(maintenance.getString("programmer"), true);
	        		programers.add(new Item(maintenance.getString("programmer"), maintenance.has("email")?maintenance.getString("email"):""));
	        	}
	        	this.controlMgr.loadProgramProfiles(e);
		        e.put("state", Module.STATE_INIT);
		        e.put("startupTime", "N/A");
		        ModuleTrack module = mapModuleTrack.remove(e.getString("id"));
		        if( module == null ){
		        	continue;
		        }
	    		e.put("memoryInfo", module.getUsageMemory());
	    		if( module.getState() == Module.STATE_STARTUP )
	    		{
	    			e.put("cpuInfo", module.getRuntime()+", "+sysPerf.getCpuLoadInfo());
	    			e.put("netloadInfo", sysPerf.getNetLoad());
	    		}
	    		else
	    		{
	    			e.put("cpuInfo", "0s, 0.00%");
	    			e.put("netloadInfo", "I(0B)/O(0B)");
	    		}
		        if( module != null ){
		        	e.put("startupTime", module.getStatupTime()!=null?Tools.getFormatTime("yyyy-MM-dd HH:mm", module.getStatupTime().getTime()):"N/A");
		        	e.put("state", module.getState());
		        	e.put("stateInfo", module.getMonitorInfo());
		        }

		        zkpath = "/cos/config/program/"+this.serverkey+"/version/"+e.getString("id");
				if( zookeeper.exists(zkpath, false) == null )
				{
					JSONObject version = new JSONObject();
					version.put("version", e.getString("version"));
					version.put("name", e.getString("name"));
					version.put("remark", e.getString("description"));
					zookeeper.createObject(zkpath, version);
				}
				if( e.has("ip") ) e.remove("ip");
				if( e.has("port") ) e.remove("port");
	        }
			
            server = new JSONObject();
            server.put("title", serverTitle);
	        server.put("name", "配置中用户服务引擎程序");
	        server.put("type", "config");
			server.put("icon", "images/icons/programmer.png");
	        server.put("id", id);
	        server.put("ip", ip);
	        server.put("healthy", 5);
	        server.put("runStateInfo", sysPerf.getPropertyValue("RunStateInfo"));
	        server.put("memoryUsageInfo", sysPerf.getPropertyValue("TotalMemeory"));
	        server.put("children", cfgPrograms);
	        trees.put(server);
			//编辑中的程序
			for( int i = 0; i < cfgPrograms.length(); i++)
	        {
				e = cfgPrograms.getJSONObject(i);
				JSONObject maintenance = e.has("maintenance")?e.getJSONObject("maintenance"):null;
				e.put("programmer", maintenance!=null?maintenance.getString("programmer"):"N/A");
				if( maintenance != null && maintenance.has("programmer") && filter.containsKey(maintenance.getString("programmer")))
	        	{
	        		filter.put(maintenance.getString("programmer"), true);
	        		programers.add(new Item(maintenance.getString("programmer"), maintenance.has("email")?maintenance.getString("email"):""));
	        	}
	        	this.controlMgr.loadProgramProfiles(e);
				e.put("editing", true);
				if( e.has("ip") ) e.remove("ip");
				if( e.has("port") ) e.remove("port");
				if( e.has("state") ) e.remove("state");
//		        e.put("state", Module.STATE_INIT);
	    		e.put("icon", "images/icons/tile.png");
	    		e.put("title", e.getString("id"));
				zkpath = "/cos/config/program/"+serverkey+"/version/"+e.getString("id");
				stat = zookeeper.exists(zkpath, false);
				if( stat == null )
				{
					JSONObject version = new JSONObject();
					version.put("version", e.getString("version"));
					version.put("name", e.getString("name"));
					version.put("remark", e.getString("description"));
					zookeeper.createObject(zkpath, version);
				}
				e.put("id", e.getString("id")+"*");
			}

//			System.err.println(userPrograms.toString(4));
            server = new JSONObject();
            server.put("title", serverTitle);
	        server.put("name", "系统服务引擎程序");
	        server.put("inner", true);
	        server.put("type", "system");
			server.put("icon", "images/icons/home.png");
	        server.put("id", id);
	        server.put("ip", ip);
	        server.put("healthy", 5);
	        server.put("runStateInfo", sysPerf.getPropertyValue("RunStateInfo0"));
	        server.put("memoryUsageInfo", sysPerf.getPropertyValue("TotalMemeory0"));
	        server.put("children", sysPrograms);
	        trees.put(server);
	        this.jsonData = trees.toString();
	        jsonData = Tools.replaceStr(jsonData, "\\", "\\\\");
	        jsonData = Tools.replaceStr(jsonData, "'", "\\'");
	        JSONArray users = userMgr.getRoleUsers(super.getUserRole());
	        this.localData = users.toString();
			return "open";
		}
		catch (Exception e1) 
		{
			log.error("Found zkpath "+zkpath+"\r\n"+(e!=null?e.toString(4):"Null"), e1);
			super.getSession().setAttribute("referer", "files!open.action");
			super.getSession().setAttribute("exception", e1);
			this.responseException = "打开集群伺服器【"+ip+"】程序管理器异常"+e1.getMessage();
			super.getSession().setAttribute("exceptionTips", responseException);
			return "exception";
		}
		finally
		{
			if( zookeeper != null ) zookeeper.close();
		}
	}
	
	/**
	 * 根据链接才是加载程序配置
	 * 先从临时目录中加载，再从control.xml中加载
	 * @return
	 * @throws Exception
	 */
	private ZooKeeper loadProgramConfig() throws Exception
	{
		Zookeeper zookeeper = null;
    	this.localDataObject = ZKMgr.getZookeeper().getJSONObject("/cos/temp/program/publish/"+serverkey+":"+id);
    	if( localDataObject == null )
    	{
			zookeeper = Zookeeper.getInstance(ip, port);
			this.localDataObject = controlMgr.getProgramConfig(serverkey, zookeeper.i(), id);
			if( localDataObject.has("oper") ) localDataObject.remove("oper");
    	}
    	return zookeeper.i();
	}

	/**
	 * 预览程序配置
	 * @return
	 */
	public String preview()
	{
		this.editable = false;
		jsonData = "";
		ZooKeeper zookeeper = null;
		try 
		{
			if( id == null || id.isEmpty() )
			{
				this.responseMessage = "输入了错误的程序标识，无法预览该程序的程序配置";
				return "alert";
			}
			zookeeper = loadProgramConfig();
	        if( localDataObject == null )
	        {
	        	this.localDataObject = ZKMgr.getZookeeper().getJSONObject("/cos/temp/program/removed/"+serverkey+"/"+id);
	        }
	        if( localDataObject == null )
	        {
				this.responseMessage = "该程序["+id+"]配置已经丢失。";
				return "alert";
	        }
        	localDataObject = controlMgr.getUnconstructProgramConfig(localDataObject);
        	if( !localDataObject.has("oper") ) localDataObject.put("oper", ProgramLoader.OPER_NONE);
        	localDataObject.put("operuser", super.getUserAccount());
        	localDataObject.put("opertime", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()));
        	this.jsonData = localDataObject.toString();
	        jsonData = Tools.replaceStr(jsonData, "\\", "\\\\");
	        jsonData = Tools.replaceStr(jsonData, "'", "\\'");
    		return "preset";
		}
		catch (Exception e)
		{
			log.error("Failed to preview for path is "+path, e);
			this.responseException = "无法预览程序配置因为解析程序配置配置出现异常"+e.getMessage();
        	return "alert";
		}
		finally
		{
			if( zookeeper != null )
				try {
					zookeeper.close();
				} catch (InterruptedException e) {
					log.error("", e);
				}
		}
	}
	/**
	 * 配置指定服务的参数
	 * @return
	 */
	public String preset()
	{
		jsonData = "";
		Zookeeper zookeeper = null;
		JSONObject obj = null;
		try
		{
			this.servertype = this.servertype.toLowerCase();
			if( id != null && !id.isEmpty() )
			{
				this.localDataObject = ZKMgr.getZookeeper().getJSONObject("/cos/temp/program/publish/"+serverkey+":"+id);
			}

			zookeeper = Zookeeper.getInstance(ip, port);
			JSONArray userPrograms = new JSONArray();
	        this.controlMgr.setUserPrograms(serverkey, zookeeper.i(), userPrograms);
			ArrayList<Item> dependences = new ArrayList<Item>();
	        for( int i = 0; i < userPrograms.length(); i++ )
	        {
	        	obj = userPrograms.getJSONObject(i);
                String name = obj.getString("name");
                String value = obj.getString("id");
	            if( value.equals(this.id) )
	            {
	            	obj.put("oper", ProgramLoader.OPER_EDITING);
	            	this.localDataObject = localDataObject==null?obj:localDataObject;
	    			continue;
	            }
                dependences.add(new Item(name+"("+value+")", value));
	        }
	        if( localDataObject != null ){
	        	localDataObject = controlMgr.getUnconstructProgramConfig(localDataObject);
	        	if( !localDataObject.has("oper") ) localDataObject.put("oper", ProgramLoader.OPER_EDITING);
	        	localDataObject.put("operuser", super.getUserAccount());
	        	localDataObject.put("opertime", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()));
	        	this.jsonData = localDataObject.toString();
		        jsonData = Tools.replaceStr(jsonData, "\\", "\\\\");
		        jsonData = Tools.replaceStr(jsonData, "'", "\\'");
	        }
	        this.listData = dependences;
	        if( editable )
	        {
		        JSONArray users = userMgr.getRoleUsers(super.getUserRole());
		        this.localData = users.toString();
				JSONArray trees = new JSONArray();
				JSONObject treeNode = this.filesMgr.fetchFiles(ip, port, "", true, null);
				treeNode.put("name", "缺省工作目录");
				treeNode.put("iconClose", "images/icons/folder_closed.png");
				treeNode.put("iconOpen",  "images/icons/folder_opened.png");
				treeNode.put("open", true);
				trees.put(treeNode);

				treeNode = new JSONObject();
				treeNode.put("path", "/");//当前缺省节点
				treeNode.put("name", ip+"系统目录");
				treeNode.put("icon", "images/icons/folder_up.png");
				treeNode.put("isParent", true);
				trees.put(treeNode);
				this.rowSelect = trees.toString();
	        }
    		return "preset";
		}
		catch (Exception e)
		{
			log.error(obj!=null?obj.toString(4):"", e);
			this.responseException = "无法解析配置节点";
        	return "alert";
		}
		finally
		{
			if( zookeeper != null ) zookeeper.close();
		}
	}
	/**
	 * 以XML形式配置主控引擎参数
	 * @return
	 */
	public String configxml()
	{
		try
		{
			RunFetchMonitor runner = this.getMonitorMgr().getMonitor().getRunFetchMonitor(ip, port);
			if( runner == null || runner.getSysDesc() == null )
			{
				super.setResponseException("伺服器KEY未知不能打开主控配置界面");
				return "close";
			}
			serverkey = Tools.encodeMD5(runner.getSysDesc().getSecurityKey());
			this.controlXml = new String(controlMgr.execute(ip, port, Command.CONTROL_CONTROLXMLPREVIEW, null), "UTF-8");
		}
		catch (Exception e)
		{
			this.responseException = "Failed to preset control from "+ip+" for "+e.toString();
		}
		return "configxml";
	}
	/**
	 * 保存主控配置参数
	 * @return
	 */
	public String savexml()
	{
		try
		{
			if( serverkey == null || serverkey.isEmpty() )
			{
				super.setResponseException("伺服器KEY未知不能保存主控配置");
				return "close";
			}
			byte[] payload = this.controlXml.getBytes("UTF-8");
			XMLParser parser = new XMLParser(new ByteArrayInputStream(payload));
			parser.getRootElement();
			controlMgr.setControlxml(ip, port, payload);
			this.responseMessage = "成功保存伺服器【"+ip+":"+port+"】主控配置文件。";
    		logoper(responseMessage, "程序管理", controlXml, null);
    		return "close";
		}
		catch(Exception e)
		{
	    	this.responseException = "配置伺服器【"+ip+":"+port+"】主控配置文件出现异常("+e.toString()+")，可能是因为XML脚本错误或者网络不通.";
    		logoper(responseException, "程序管理", null, null, e);
    		return configxml();
		}
	}
	
	/**
	 * 执行指令获取数据返回
	 * @return
	 */
	public String jmap()
	{
    	ServletOutputStream out = null;
        try
        {
			RunFetchMonitor runner = this.getMonitorMgr().getMonitor().getRunFetchMonitor(ip, port);
			if( runner == null || runner.getSysDesc() == null )
			{
				super.setResponseException("伺服器KEY未知不能打开主控配置界面");
				return "close";
			}
			serverkey = Tools.encodeMD5(runner.getSysDesc().getSecurityKey());
        	byte[] payload = this.controlMgr.execute(ip, port, Command.CONTROL_JMAP, id);
        	if( payload[0] == 0 ){
    			getResponse().setContentType("application/octet-stream");
        		getResponse().setHeader("Content-disposition", "inline; filename=jmap.dump");
        	}
        	else{
    			getResponse().setContentType("text/plain;charset=utf8");
        		getResponse().setHeader("Content-disposition", "inline; filename=result.txt");
        	}
            out = getResponse().getOutputStream();
            out.write(payload, 1, payload.length-1);
    		out.flush();
        }
        catch(Exception e)
        {
			try
			{
				OutputStream out1 = getResponse().getOutputStream();
				getResponse().setContentType("text/plain;charset=ISO8859_1");
				PrintWriter writer = new PrintWriter(out1);
				e.printStackTrace(writer);
				writer.flush();
				writer.close();
				out1.close();
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
			}
        }
        finally
        {
    		try
			{
            	if( out != null ) out.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
        }
		return null;
	}
	
	/**
	 * 执行指令获取数据返回
	 * @return
	 */
	public String jstack()
	{
    	ServletOutputStream out = null;
        try
        {
			RunFetchMonitor runner = this.getMonitorMgr().getMonitor().getRunFetchMonitor(ip, port);
			if( runner == null || runner.getSysDesc() == null )
			{
				super.setResponseException("伺服器KEY未知不能打开主控配置界面");
				return "close";
			}
			serverkey = Tools.encodeMD5(runner.getSysDesc().getSecurityKey());
        	byte[] payload = this.controlMgr.execute(ip, port, Command.CONTROL_JSTACK, id);
			getResponse().setContentType("text/plain;charset=utf8");
    		getResponse().setHeader("Content-disposition", "inline; filename=result.txt");
            out = getResponse().getOutputStream();
            out.write(payload, 1, payload.length-1);
    		out.flush();
        }
        catch(Exception e)
        {
			try
			{
				OutputStream out1 = getResponse().getOutputStream();
				getResponse().setContentType("text/plain;charset=ISO8859_1");
				PrintWriter writer = new PrintWriter(out1);
				e.printStackTrace(writer);
				writer.flush();
				writer.close();
				out1.close();
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
			}
        }
        finally
        {
    		try
			{
            	if( out != null ) out.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
        }
		return null;
	}

	/**
	 * 控制程序
	 * @return
	 */
	public String program()
	{
    	byte[] payload = new byte[256];
    	payload[0] = (byte)command;
    	payload[1] = (byte)id.length();
    	Tools.copyByteArray(id.getBytes(), payload, 2);
//    	String binStr = Tools.getBinaryString(payload, 0, 2 + id.length());
    	DatagramSocket datagramSocket = null;
        try
        {
        	if( "COSControl".equals(id) || "EMAControl".equals(id) )
        	{//如果是主控重启，那么关闭监控模块
        		this.getMonitorMgr().reloadMonitor(ip, port);
        	}
        	datagramSocket = new DatagramSocket();
            datagramSocket.setSoTimeout(15000);
            DatagramPacket request = new DatagramPacket(payload, 0, 2 + id.length(), InetAddress.getByName( ip ), port );
            datagramSocket.send( request );
            if( command == 0 )
            {
    			this.responseMessage = "成功向主机["+ip+"]的模块["+id+"]发送重启指令。";
            }
            else if( command == 1 )
            {
                DatagramPacket response = new DatagramPacket( payload, 0, 2 + id.length(), request.getAddress(), request.getPort() );
                datagramSocket.receive( response );
                this.responseMessage = "成功向主机["+ip+"]的模块["+id+"]发送暂停指令。";
            }
            else
            {
                DatagramPacket response = new DatagramPacket( payload, 0, 2 + id.length(), request.getAddress(), request.getPort() );
                datagramSocket.receive( response );
                this.responseMessage = "成功向主机["+ip+"]的模块["+id+"]清除日志指令。";
            }
    		logoper(responseMessage, "程序管理", null, null);
        }
        catch(Exception e)
        {
        	if( command == 0 )
            {
        		responseException = "成功向主机["+ip+"]的模块["+id+"]发送重启指令出现异常"+ e.getMessage();
            }
            else if( command == 1 )
            {
            	this.responseException = "向主机["+ip+"]的模块["+id+"]发送指令("+command+")失败 ，原因是"+e.getMessage();
            }
            else
            {
            	this.responseException = "向主机["+ip+"]的模块["+id+"]发送指令("+command+")失败 ，原因是"+e.getMessage();
            }
    		logoper(responseException, "程序管理", null, null, e);
        }
        finally
        {
        	if( datagramSocket != null )
        	{
        		datagramSocket.close();
        	}
        }
		return "alert";
	}
	
	/**
	 * 
	 * @return
	 */
	public String checkupgrades()
	{
		try 
		{
			ArrayList<JSONObject> servers = MonitorMgr.getInstance().getServers();
			this.localDataArray = new JSONArray();
			for(int i = 0; i < servers.size(); i++)
			{
				JSONObject server = servers.get(i);
				server = new JSONObject(server.toString());
				RunFetchMonitor runner = MonitorMgr.getInstance().getMonitor().getRunFetchMonitor(server.getString("ip"), server.getInt("port"));
				if( runner != null && runner.getSysDesc() != null )
				{
					server.put("version", runner.getSysDesc().getProperty("cos.control.version"));
					serverkey = server.has("security-key")?server.getString("security-key"):runner.getSysDesc().getSecurityKey();
					serverkey = Tools.encodeMD5(serverkey);
					server.put("serverkey", serverkey);
					ModulePerf coscontrol = runner.getModulePerf("COSControl");
					if( coscontrol != null ){
						server.put("coscontrol", coscontrol.getStartupTime());
					}
					server.put("connect", runner.isConnect());
					server.put("status", runner.status());
					server.put("heartbeat", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", runner.getHeartbeat()));
					localDataArray.put(server);
				}
			}
	        return super.grid("/grid/local/controlupgrades.xml");
		}
		catch (Exception e) 
		{
			super.setResponseException("初始化集群升级管理界面失败，因为异常"+e);
			log.debug("Failed to initialize the view of checkupgrades for exception ", e);
			return "close";
		}
	}
	/**
	 * 执行升级检查
	 * @return
	 */
	public String checkupgrade()
	{
		id = "COSControl";
		RunFetchMonitor runner = MonitorMgr.getInstance().getMonitor().getRunFetchMonitor(ip, port);
		if( runner == null || !runner.isConnect() || runner.getSysDesc() == null )
		{
			this.responseException = "伺服器【"+ip+"】没有启动您不能执行升级检查。";
			return "close";
		}
		serverkey = runner.getSysDesc().getSecurityKey();
		serverkey = Tools.encodeMD5(serverkey);
		String dataurl = "control!versiontimeline.action?id="+id+"&ip="+ip+"&port="+port+"&serverkey="+serverkey;
		this.responseRedirect = "helper!timeline.action?dataurl="+Kit.chr2Unicode(dataurl);
//		System.err.println(dataurl);
		return "upgrade";
	}
	
	/**
	 * 执行升级指令
	 * @return
	 */
	public String upgrade()
	{
		DatagramSocket datagramSocket = null;
        try
        {
        	String yaochongqi = super.getRequest().getParameter("yaochongqi");
        	byte ind = (yaochongqi!=null&&yaochongqi.equals("4"))?(byte)4:0;
        	
        	this.getMonitorMgr().reloadMonitor(ip, port);
        	datagramSocket = new DatagramSocket();
            datagramSocket.setSoTimeout(15000);
            DatagramPacket request = new DatagramPacket(new byte[]{Command.CONTROL_UPGRADE, ind}, 0, 2, InetAddress.getByName( ip ), port );
            datagramSocket.send( request );
            byte[] payload = new byte[1024];
            DatagramPacket response = new DatagramPacket( payload, 0, payload.length, request.getAddress(), request.getPort() );
            JSONObject result = null;
            datagramSocket.receive(response);
            payload = getGZIPResult(response);
            String json = new String(payload, "UTF-8");
            result = new JSONObject(json);
        	this.setResponseMessage(result.getString("result"));
    		logoper(responseMessage, "系统升级", null, null);
//            this.responseMessage = "从集群伺服器【"+ip+"】执行文件"+filepath+"解压缩到"+destpath+"成功。";
//    		logoper(responseMessage, "文件管理", result.toString(4), "files!open.action?ip="+ip+"&port="+port0);
//    		SvrMgr.sendNotiefiesToSystemadmin(
//    				super.getRequest(),
//					"集群文件管理",
//					String.format("用户[%s]"+responseMessage, super.getUserAccount()),
//					null,
//					"files!open.action?ip="+ip+"&port="+port0,
//                    "情况确认", "#feedback?to="+super.getUserAccount());
        }
        catch(Exception e)
        {
        	this.responseException = "发送主控升级指令到伺服器【"+ip+"】失败 ，原因是"+e.toString();
    		logoper(responseException, "系统升级", null, null, e);
    		SvrMgr.sendNotiefiesToSystemadmin(
    				super.getRequest(),
					"系统升级",
					String.format("用户[%s]"+responseException, super.getUserAccount()),
					null,
					responseRedirect,
                    "情况确认", "#feedback?to="+super.getUserAccount());
        }
        finally
        {
        	if( datagramSocket != null )
        	{
        		datagramSocket.close();
        	}
        }
		String dataurl = "control!versiontimeline.action?ip="+ip+"&port="+port+"&serverkey="+serverkey;
		this.responseRedirect = "helper!timeline.action?dataurl="+Kit.chr2Unicode(dataurl);
		return "alert";
	}
	/**
	 * 升级下载
	 * @return
	 */
	public String upgradedownload()
	{
		DatagramSocket datagramSocket = null;
    	ServletOutputStream sos = null;
		String javascript = "";
		JSONObject result = null;
		String svrversion = "";
		String webversion = "";
		boolean newversion = false;
        try
        {
			getResponse().setContentType("text/html");
			getResponse().setCharacterEncoding("UTF-8");
			sos = getResponse().getOutputStream();
			sos.print("<html><body style='padding: 1px;font-size:9pt;background:#000;color:#fff;word-break:keep-all;white-space:pre;'>");
			pagereport(sos, -1, false, "执行伺服器【"+ip+"】升级版本检查.");
			
        	this.getMonitorMgr().reloadMonitor(ip, port);
        	datagramSocket = new DatagramSocket();
            datagramSocket.setSoTimeout(60000);//超时时间设长一点，因为下载升级文件可能会很长
            DatagramPacket request = new DatagramPacket(new byte[]{Command.CONTROL_UPGRADECHECKE}, 0, 1, InetAddress.getByName( ip ), port );
            datagramSocket.send( request );
            byte[] payload = new byte[1024];
            DatagramPacket response = new DatagramPacket( payload, 0, payload.length, request.getAddress(), request.getPort() );
            int progress = 0;
            int step = 0;
            do
            {
				datagramSocket.receive(response);
				payload = getGZIPResult(response);
	            String json = new String(payload, "UTF-8");
            	result = new JSONObject(json);
//        		System.err.println(result.toString());
            	if( result.has("progress") )
            	{
            		progress = result.getInt("progress");
            		if( progress == 0 )
            		{
                        sos.write("\r\n进度: |".getBytes("UTF-8"));
                        sos.flush();
                        step = 0;
            		}
        			step = writeProgress(sos, step, progress++);
            	}
            	else if( result.has("result") )
            	{
            		pagereport(sos, 0, true, result.getString("result"));
            		if( result.has("type"))
            		{
            			if( "COSControl".equals(result.get("type")) )
            			{
                            if( result.has("newversion") && result.getBoolean("newversion") )
                            {
                            	svrversion = result.has("version")?result.getString("version"):"";
                            }
            			}
            			else
            			{
                            if( result.has("newversion") && result.getBoolean("newversion") )
                            {
                            	webversion = result.has("version")?result.getString("version"):"";
                            }
            			}
            		}
            	}
            	
                if( !newversion && result.has("newversion") )
                {
                	newversion = result.getBoolean("newversion");
                }
            }
            while(result.has("result"));
    		logoper("完成升级下载", "系统升级", pr.toString(), null);
        }
        catch(Exception e)
        {
        	if( !newversion ){
            	this.responseException = "检查伺服器【"+ip+"】主控升级失败 ，原因是"+e.getMessage();
        		pagereport(sos, 0, true, responseException);
        		logoper(responseException, "系统升级", pr.toString(), null, e);
        		SvrMgr.sendNotiefiesToSystemadmin(
        				super.getRequest(),
    					"系统升级",
    					String.format("用户[%s]"+responseException, super.getUserAccount()),
    					null,
    					responseRedirect,
                        "情况确认", "#feedback?to="+super.getUserAccount());
        	}
        }
        finally
        {
            if( result != null && newversion )
            {
        		javascript = "parent.doUpgrade("+newversion+", '"+svrversion+"', '"+webversion+"');";
            }
            else
            {
            	javascript = "parent.skit_alert('执行升级操作出现异常，请联系系统管理员解决');";
            }
        	if( datagramSocket != null )
        	{
        		datagramSocket.close();
        	}
        	if( sos != null )
        	try {
				sos.println("</body>\r\n\r\n<script type='text/javascript'>"+javascript+"</script></html>");
				sos.flush();
				sos.close();
        	} catch (IOException e1) {
			}
        }
		return null;
	}
	/**
	 * 重启
	 * @return
	 */
	public String restartup()
	{
		DatagramSocket datagramSocket = null;
        try
        {
        	this.getMonitorMgr().reloadMonitor(ip, port);
        	datagramSocket = new DatagramSocket();
            datagramSocket.setSoTimeout(15000);
            DatagramPacket request = new DatagramPacket(new byte[]{Command.CONTROL_RESTARTALL}, 0, 1, InetAddress.getByName( ip ), port );
            datagramSocket.send( request );
        	this.setResponseMessage("发送主控重启指令到伺服器【"+ip+"】。");
    		logoper(responseMessage, "程序管理", null, null);
        }
        catch(Exception e)
        {
        	this.responseException = "发送主控重启指令到伺服器【"+ip+"】失败 ，原因是"+e.toString();
    		logoper(responseException, "程序管理", null, null, e);
        }
        finally
        {
        	if( datagramSocket != null )
        	{
        		datagramSocket.close();
        	}
        }
		return "alert";
	}
	/**
	 * 暂停所有服务
	 * @return
	 */
	public String suspend()
	{
		DatagramSocket datagramSocket = null;
        try
        {
        	datagramSocket = new DatagramSocket();
            datagramSocket.setSoTimeout(15000);
            DatagramPacket request = new DatagramPacket(new byte[]{Command.CONTROL_SUSPENDALL}, 0, 1, InetAddress.getByName( ip ), port );
            datagramSocket.send( request );
        	this.setResponseMessage("发送主控暂停指令到伺服器【"+ip+"】。");
    		logoper(responseMessage, "程序管理", null, null);
        }
        catch(Exception e)
        {
        	this.responseException = "发送主控暂停指令到伺服器【"+ip+"】失败 ，原因是"+e.toString();
    		logoper(responseException, "程序管理", null, null, e);
        }
        finally
        {
        	if( datagramSocket != null )
        	{
        		datagramSocket.close();
        	}
        }		
		return "alert";
	}
	
	public String untimeline(JSONObject data, String text)
	{
		String startDate = Tools.getFormatTime("yyyy,M", System.currentTimeMillis()-Tools.MILLI_OF_DAY);
		JSONObject timeline = new JSONObject();
		timeline = new JSONObject();
		timeline.put("headline", "伺服器【"+ip+"】程序["+id+"]");
		timeline.put("type", "default");
		timeline.put("startDate", startDate);
		timeline.put("text", "<span class='version'>N/A</span><span class='title'>N/A</span><br/>"+text);
		data.put("timeline", timeline);
		JSONObject asset = new JSONObject();
		asset.put("media", "images/notes.png");
		asset.put("credit", "");
		asset.put("caption", "");
		timeline.put("asset", asset);

		JSONArray date = new JSONArray();
		JSONObject timeline0 = new JSONObject();
		timeline0.put("headline", "N/A");
		timeline0.put("type", "default");
		timeline0.put("startDate", startDate);
		timeline0.put("text", text);
		asset = new JSONObject();
		asset.put("media", HelperMgr.getImgWallpaper());
		asset.put("credit", "");
		asset.put("caption", "");
		timeline0.put("asset", asset);
		date.put(timeline0);
		timeline.put("date", date);
		
		return response(super.getResponse(), data.toString());
	}
	/**
	 * 版本时间树
	 * @return
	 */
	public String costimeline()
	{
		log.info("Open the versions of COS from server "+Version.getCOSSecurityKey());
		this.serverkey = Tools.encodeMD5(Version.getCOSSecurityKey());
		return versiontimeline();
	}
	/**
	 * 版本时间树
	 * @return
	 */
	public String versiontimeline()
	{
		log.info("Open the timeline of versions "+id+" from "+ip+":"+port);
//		RunFetchMonitor runner = getMonitorMgr().getMonitor().getRunFetchMonitor(ip, port);
		JSONObject data = new JSONObject();
		Zookeeper zookeeper = null;
		String zkpath = "";
		try
		{
			zookeeper = Zookeeper.getInstance(ip, port);
			zkpath = "/cos/config/program/"+this.serverkey+"/version/"+id;
			Stat stat = zookeeper.exists(zkpath, false);
			if( stat == null )
			{
				return null;
			}
			JSONObject timeline = new JSONObject(new String(zookeeper.getData(zkpath, false, stat), "UTF-8"));
//			System.err.println(timeline);
			JSONObject asset = new JSONObject();
			asset.put("media", "images/notes.png");
			asset.put("credit", "");
			asset.put("caption", "");
			timeline.put("asset", asset);
			
			JSONArray date = null;
			String version = timeline.has("version")?timeline.getString("version"):null;//module.getVersion();
			long fistTs = System.currentTimeMillis();
			
			if( timeline.has("date") )
			{
				date = timeline.getJSONArray("date");
				int programVersion[] = new int[4];
				long _v = 0;
				for(int i = 0; i < date.length(); i++)
				{
					JSONObject timeline0 = date.getJSONObject(i);
					String version0 = timeline0.getString("version");
					String[] args = Tools.split(version0, ".");
					int v0 = Integer.parseInt(args[0]);
					int v1 = Integer.parseInt(args[1]);
					int v2 = Integer.parseInt(args[2]);
					int v3 = Integer.parseInt(args[3]);
					long v_ = v0*100*100*100+v1*100*100+v2*100+v3;
					if( _v < v_ )
					{
						programVersion[0] = v0;
						programVersion[1] = v1;
						programVersion[2] = v2;
						programVersion[3] = v3;
						_v = v_;
					}
					
					String time = timeline0.getString("time");//dd/MM/yyyy
					args = Tools.split(time, "/");
					Calendar calendar = Calendar.getInstance();
					calendar.setTimeInMillis(0);
					calendar.set(Integer.parseInt(args[2]), Integer.parseInt(args[0])-1, Integer.parseInt(args[1]));
					timeline0.put("startDate", Tools.getFormatTime("yyyy,M,d", calendar.getTimeInMillis()));
					timeline0.put("headline", "v"+version0);
					asset = new JSONObject();
					asset.put("media", HelperMgr.getImgWallpaper());
					asset.put("credit", "");
					asset.put("caption", "");
					timeline0.put("asset", asset);
					if( calendar.getTimeInMillis() < fistTs ) fistTs = calendar.getTimeInMillis();
				}
//				if( version.equals("0.0.0.0") )
				if( version == null ) version = programVersion[0]+"."+programVersion[1]+"."+programVersion[2]+"."+programVersion[3];
			}
			else
			{
				date = new JSONArray();
				timeline.put("date", date);
			}
			if( version == null ) version = "N/A";
			else version = "v"+version;
			timeline.put("headline", "伺服器【"+ip+"】程序["+id+"]");
			timeline.put("type", "default");
			timeline.put("startDate", Tools.getFormatTime("yyyy,M", fistTs));
			StringBuffer text = new StringBuffer();
			text.append("<span class='version'>"+version+"</span>");
			text.append("<span class='title'>"+timeline.getString("name")+"</span><br/>");
//			text.insert(0, "<span class='version'>v"+timeline.getString("version")+"</span>");
			text.append(timeline.getString("remark"));
			timeline.put("text", text.toString());
			data.put("timeline", timeline);
		}
		catch(Exception e)
		{
			log.error("Failed to get the version from "+zkpath+" for "+e);
			return untimeline(data, "构建程序["+id+"]的版本数据出现异常"+e.getMessage());
		}
		finally
		{
			if( zookeeper != null ) zookeeper.close();
		}
			
//		System.err.println(data.toString(4));
		return response(super.getResponse(), data.toString());
	}
	
	/**
	 * 设置缺省的版本时间线
	 * @param module
	 * @param date
	private void setDefaultVersionTimeline(ModulePerf module, JSONArray date)
	{
		JSONObject timeline0 = new JSONObject();
		if( module != null )
		{
			timeline0.put("version", module.getVersion());
			long ts = module.getStatupTime()!=null?module.getStartupDate().getTime():0;
			timeline0.put("time", Tools.getFormatTime("MM/dd/yyyy", ts));
			timeline0.put("text", module.getRemark());
			date.put(timeline0);
		}
	}*/
	/**
	 * 
	 * @return
	 */
	public String presetversion()
	{
		log.info("Preset the version of "+id+" from "+ip);
		Zookeeper zookeeper = null;
		String zkpath = "";
		try
		{
			zookeeper = Zookeeper.getInstance(ip, port);
			zkpath = "/cos/config/program/"+this.serverkey+"/version/"+id;
			Stat stat = zookeeper.exists(zkpath, false);
			JSONArray date = null;
			JSONObject timeline = null;
			JSONObject newRow = new JSONObject();
			if( stat != null )
			{
				timeline = new JSONObject(new String(zookeeper.getData(zkpath, false, stat), "UTF-8"));
			}
			else
			{
				timeline = new JSONObject();
				date = new JSONArray();
			}
			if( timeline.has("date") )
			{
				date = timeline.getJSONArray("date");
			}
			else
			{
				date = new JSONArray();
//				setDefaultVersionTimeline(this.getVersionModule(), date);
				timeline.put("date", date);
			}
			for(int i = 0; i < date.length(); i++)
			{
				JSONObject timeline0 = date.getJSONObject(i);
				timeline0.put("ip", ip);
				timeline0.put("port", port);
				timeline0.put("serverkey", serverkey);
				timeline0.put("id", id);
			}
//			System.err.println(date.toString(4));
			this.localData = date.toString();
			newRow.put("ip", ip);
			newRow.put("port", port);
			newRow.put("serverkey", serverkey);
			newRow.put("id", id);
			jsonData = newRow.toString();
			String rspstr = super.grid("/grid/local/controlversion.xml");
			return rspstr;
		}
		catch(Exception e)
		{
			super.setResponseException("打开程序版本配置界面失败，因为异常"+e.getMessage());
			log.debug("Failed to initialize the view of cmpcfg for exception ", e);
			return "alert";
		}
		finally
		{
			if( zookeeper != null ) zookeeper.close();
		}
	}

	/**
	 * 修改配置
	 * @return
	 */
	public String setversion()
	{
		HttpServletRequest req = super.getRequest();
		JSONObject response = new JSONObject();
		Zookeeper zookeeper = null;
		String zkpath = "";
		try
		{
			StringBuffer sb = new StringBuffer();
			sb.append("Receive the request of update from ");
			Iterator<Map.Entry<String, String[]>> iterator = super.getRequest().getParameterMap().entrySet().iterator();
			while(iterator.hasNext())
			{
				Map.Entry<String, String[]> e = iterator.next();
				sb.append("\r\n\t");
				sb.append(e.getKey());
				sb.append("=");
				for(String value : e.getValue())
					sb.append(value+"\t");
			}
			log.debug(sb.toString());
			ip = req.getParameter("ip");
			port = Integer.parseInt(req.getParameter("port"));
			id = req.getParameter("id");
			serverkey = req.getParameter("serverkey");
			String version = req.getParameter("version");

			zookeeper = Zookeeper.getInstance(ip, port);
			zkpath = "/cos/config/program/"+serverkey+"/version/"+id;
			Stat stat = zookeeper.exists(zkpath, false);
			JSONObject timeline = null;
			JSONArray date = null;
			if( stat == null)
			{
				timeline = new JSONObject();
			}
			else
			{
				timeline = new JSONObject(new String(zookeeper.getData(zkpath, false, stat), "UTF-8"));
			}
			
			if( timeline.has("date") )
			{
				date = timeline.getJSONArray("date");
			}
			else
			{
				date = new JSONArray();
				timeline.put("date", date);
			}
			JSONObject timeline1 = null;
			for(int i = 0; i < date.length(); i++)
			{
				JSONObject timeline0 = date.getJSONObject(i);
				String version0 = timeline0.getString("version");
				if( timeline1 == null && version.equals(version0) )
				{
					timeline1 = timeline0;
				}
			}
			if( timeline1 == null ) {
				timeline1 = new JSONObject();
				date.put(timeline1);
			}
			timeline1.put("version", version);
			timeline1.put("time", req.getParameter("time"));
			timeline1.put("text", req.getParameter("text"));
//			timeline.put("version", programVersion[0]+"."+programVersion[1]+"."+programVersion[2]+"."+programVersion[3]);
//			System.err.println(timeline.toString(4));
			if( stat == null )
			{
				zookeeper.createObject(zkpath, timeline);
			}
			else
			{
				zookeeper.setData(zkpath, timeline.toString().getBytes("UTF-8"), stat);
			}
            this.responseMessage = "设置集群伺服器【"+ip+"】程序【"+id+"】的版本[v"+version+"]成功。";
			response.put("message", responseMessage);
			String dataurl = "control!versiontimeline.action?id="+id+"&ip="+ip+"&port="+port;
    		logoper(responseMessage, "程序管理", null, "helper!timeline.action?dataurl="+Kit.chr2Unicode(dataurl));
    		SvrMgr.sendNotiefiesToSystemadmin(
    				super.getRequest(),
					"集群程序管理",
					responseMessage,
					null,
					"helper!timeline.action?dataurl="+Kit.chr2Unicode(dataurl),
                    "情况确认", "#feedback?to="+super.getUserAccount());
		}
		catch (Exception e)
		{
			log.error("Failed to set the version of "+id, e);
            this.responseException = "设置集群伺服器【"+ip+"】程序【"+id+"】的版本出现异常，因为"+e.getMessage();
    		logoper(responseException, "程序管理", null, null, e);
			response.put("hasException", true);
			response.put("message", responseException);
		}
		return response(super.getResponse(), response.toString());		
	}

	/**
	 * 修改配置
	 * @return
	 */
	public String delversion()
	{
		HttpServletRequest req = super.getRequest();
		JSONObject response = new JSONObject();
		Zookeeper zookeeper = null;
		String zkpath = "";
		ip = req.getParameter("data[ip]");
		id = req.getParameter("data[id]");
		serverkey = req.getParameter("data[serverkey]");
		String version = req.getParameter("data[version]");
		try
		{
			StringBuffer sb = new StringBuffer();
			sb.append("Receive the request of delete from ");
			Iterator<Map.Entry<String, String[]>> iterator = super.getRequest().getParameterMap().entrySet().iterator();
			while(iterator.hasNext())
			{
				Map.Entry<String, String[]> e = iterator.next();
				sb.append("\r\n\t");
				sb.append(e.getKey());
				sb.append("=");
				for(String value : e.getValue())
					sb.append(value+"\t");
			}
			log.debug(sb.toString());
			port = Integer.parseInt(req.getParameter("data[port]"));
			zookeeper = Zookeeper.getInstance(ip+":"+port);
			zkpath = "/cos/config/program/"+serverkey+"/version/"+id;
			Stat stat = zookeeper.exists(zkpath, false);
			JSONObject timeline = null;
			JSONArray date = null;
			if( stat == null)
			{
				throw new Exception("版本数据不存在");
			}
			timeline = new JSONObject(new String(zookeeper.getData(zkpath, false, stat), "UTF-8"));
			if( !timeline.has("date") )
			{
				throw new Exception("版本记录不存在");
			}
			date = timeline.getJSONArray("date");
			for(int i = 0; i < date.length(); i++)
			{
				JSONObject timeline0 = date.getJSONObject(i);
				String version0 = timeline0.getString("version");
				if( version.equals(version0) )
				{
					date.remove(i);
					break;
				}
			}
//			System.err.println(timeline.toString(4));
			zookeeper.setData(zkpath, timeline.toString().getBytes("UTF-8"), stat);
            this.responseMessage = "删除集群伺服器【"+ip+"】程序【"+id+"】的版本[v"+version+"]成功。";
			response.put("message", responseMessage);
    		String dataurl = "control!versiontimeline.action?id="+id+"&ip="+ip+"&port="+port;
    		logoper(responseMessage, "程序管理", null, "helper!timeline.action?dataurl="+Kit.chr2Unicode(dataurl));
    		SvrMgr.sendNotiefiesToSystemadmin(
    				super.getRequest(),
					"集群程序管理",
					responseMessage,
					null,
					"helper!timeline.action?dataurl="+Kit.chr2Unicode(dataurl),
                    "情况确认", "#feedback?to="+super.getUserAccount());
		}
		catch (Exception e)
		{
			log.error("Failed to delete "+version+" for exception ", e);
            this.responseException = "删除集群伺服器【"+ip+"】程序【"+id+"】的版本出现异常，因为"+e.getMessage();
    		logoper(responseException, "程序管理", null, null, e);
			response.put("hasException", true);
			response.put("message", responseException);
		}
		return response(super.getResponse(), response.toString());
	}

	/**
	 * 发布配置程序管理
	 * @return
	 */
	public String doPublish()
	{
		try 
		{
			HttpSession session = super.getSession();
			ArrayList<JSONObject> servers = this.getMonitorMgr().getServers();
	    	BasicDBList options = new BasicDBList();
			for(JSONObject p : servers)
			{
				String title = p.has("title")?p.getString("title"):"";
				if( !title.isEmpty() )
				{
					title = "["+title+"]";
				}
				BasicDBObject option = new BasicDBObject();
				option.put("value", p.getString("ip"));
				option.put("label", p.getString("ip")+title);
				options.add(option);
			}
    		super.labelsModel.put("ip", options);

    		String xmlpath = "/grid/local/controlpublish.xml";
    		hiddens.put("publish", command>=0);
	        String rsp = super.grid(xmlpath);
	        if( command >= 0 )
			{
	        	this.hasToolbar = false;
	        	this.toolbars.clear();
	        	this.filterModel.put("on", false);
				session.setAttribute("QueryPublish", command);
			}	        
	        return rsp;
		}
		catch (Exception e) 
		{
			super.setResponseException("初始化集群程序发布管理界面失败，因为异常"+e);
			log.debug("Failed to initialize the view of control-publish for exception ", e);
			return "close";
		}
	}
	
	/**
	 * 程序待发布数据
	 * @return
	 */
	public String publishdata()
	{
    	ServletOutputStream out = null;
		JSONObject dataJSON = new JSONObject();
		try
		{
			Object a = super.getSession().getAttribute("QueryPublish");
			if( a != null ){
				command = (Integer)a;
				super.getSession().removeAttribute("QueryPublish");
			}
			
			StringBuffer sb = new StringBuffer();
			sb.append("Receive the request, the parameters of below ");
			HttpServletRequest req = super.getRequest();
			Iterator<Map.Entry<String, String[]>> iterator = req.getParameterMap().entrySet().iterator();
			while(iterator.hasNext())
			{
				Map.Entry<String, String[]> e = iterator.next();
				sb.append("\r\n\t");
				sb.append(e.getKey());
				sb.append("=");
				for(String value : e.getValue())
					sb.append(value+"\t");
			}
			log.info(sb.toString());
			
			HttpServletResponse response = super.getResponse();
            out = response.getOutputStream();
			response.setContentType("text/json;charset=utf8");
    		response.setHeader("Content-disposition", "inline; filename="+System.currentTimeMillis()+".json");

			String zkpath = "/cos/temp/program/publish";
			JSONArray datas = new JSONArray();
			Stat stat = ZKMgr.getZookeeper().exists(zkpath, false);
			if( stat != null )
			{
				List<String> pubs = ZKMgr.getZookeeper().getChildren(zkpath, false);
				String _ip = super.getRequest().getParameter("ip");
				String _oper = super.getRequest().getParameter("oper");
				String _programmer = super.getRequest().getParameter("programmer");
				String _id = super.getRequest().getParameter("id");
				String _name = super.getRequest().getParameter("name");
				String _startdate = super.getRequest().getParameter("startdate");
				String _enddate = super.getRequest().getParameter("enddate");				
				for(String pub : pubs)
				{
					path = zkpath+"/"+pub;
					JSONObject cfg = new JSONObject(new String(ZKMgr.getZookeeper().getData(path, false, stat), "UTF-8"));
					if( !cfg.has("ip") )
					{
						log.error("Found error config "+cfg.toString(4));
						continue;
					}
					ip = cfg.getString("ip");
					cfg.put("addr", ip);
					String name = cfg.getString("name");
					String time = cfg.getString("opertime");
					JSONObject maintenance = cfg.has("maintenance")?cfg.getJSONObject("maintenance"):(new JSONObject());
					String programmer = maintenance.has("programmer")?maintenance.getString("programmer"):"";
					int oper = cfg.getInt("oper");
					if( command >= 0 && oper != command ) continue;
					id = cfg.getString("id");
//					port = e.getInt("port");
//					serverkey = e.getString("serverkey");
					if( _ip != null &&  !ip.equals(_ip) ){
						continue;
					}
					if( _oper != null &&  !_oper.equals(String.valueOf(oper)) ){
						continue;
					}
					if( _name != null &&  !name.equals(_name) ){
						continue;
					}
					if( _id != null &&  !id.equals(_id) ){
						continue;
					}
					if( _programmer != null &&  !programmer.equals(_programmer) ){
						continue;
					}
					if( _startdate !=null && time.compareTo(_startdate) < 0 ){
						continue;
					}
					if( _enddate !=null  && time.compareTo(_enddate) > 0 ){
						continue;
					}
					datas.put(cfg);
				}
			}
//			System.err.println(datas.toString(4));
    		dataJSON.put("totalRecords", datas.length());
			dataJSON.put("curPage", 1);
			dataJSON.put("data", datas);
		}
		catch (Exception e)
		{
			log.error("Failed to query the alarms of instant for exception:", e);
			dataJSON.put("message", e.getMessage());
			dataJSON.put("hasException", true);
		}
        finally
        {
        	if( out != null )
	    		try
				{
	    			String json = dataJSON.toString();
					out.write(json.getBytes("UTF-8"));
	            	out.close();
				}
				catch (IOException e)
				{
					log.error("", e);
				}
        }
		return null;		
	}

	/**
	 * 历史审批记录
	 * @return
	 */
	public String navigatehistory()
	{
		try 
		{
			this.localDataArray = new JSONArray();
			String zkpath = "/cos/data/program/publish/history";
			List<String> dates = ZKMgr.getZookeeper().getChildren(zkpath);
			if( dates != null )
				for(String date : dates )
				{
					JSONObject e = new JSONObject();
					JSONArray children = new JSONArray();
					e.put("name", date);
					String path = zkpath+"/"+date;
					e.put("path", path);
					e.put("children", children);
					localDataArray.put(e);
					List<String> times = ZKMgr.getZookeeper().getChildren(path);
					if( times != null )
						for(String time : times)
						{
							path += "/"+time;
							e = new JSONObject();
							e.put("name", time);
							e.put("path", path);
							children.put(e);
						}
				}
			this.grant = super.isSysadmin();
			jsonData = this.localDataArray.toString();
		}
		catch (Exception e) 
		{
			super.responseException = e.getMessage();
		}
		return "navigatehistory";
	}
	
	/**
	 * 组件子系统菜单配置
	 * @return
	 */
	public String doPublishhistory()
	{
		try 
		{
    		String xmlpath = "/grid/local/controlpublishhistory.xml";
	        String rsp = super.grid(xmlpath);
			HttpSession session = super.getSession();
			session.setAttribute("QueryPublishhistory", id);
	        return rsp;
		}
		catch (Exception e) 
		{
			super.setResponseException("初始化集群程序管理历史界面失败，因为异常"+e);
			log.debug("Failed to initialize the view of control-publish for exception ", e);
			return "close";
		}
	}
	/**
	 * 程序发布历史数据
	 * @return
	 */
	public String publishhistorydata()
	{
    	ServletOutputStream out = null;
		JSONObject dataJSON = new JSONObject();
		try
		{
			Object a = super.getSession().getAttribute("QueryPublishhistory");
			if( a != null )
			{
				path = a.toString();
				super.getSession().removeAttribute("QueryPublishhistory");
			}
			
			HttpServletResponse response = super.getResponse();
            out = response.getOutputStream();
			response.setContentType("text/json;charset=utf8");
    		response.setHeader("Content-disposition", "inline; filename="+System.currentTimeMillis()+".json");

			String zkpath = new String(Kit.unicode2Chr(path));
			log.debug("Found the data of history for publish "+zkpath);
			Stat stat = ZKMgr.getZookeeper().exists(zkpath, false);
			if( stat == null )
			{
				return null;
			}
			JSONArray history = new JSONArray(new String(ZKMgr.getZookeeper().getData(zkpath, false, stat), "UTF-8"));
//			System.err.println(history.toString(4));
    		dataJSON.put("totalRecords", history.length());
			dataJSON.put("curPage", 1);
			dataJSON.put("data", history);
		}
		catch (Exception e)
		{
			log.error("Failed to query the alarms of instant for exception:", e);
		}
        finally
        {
        	if( out != null )
	    		try
				{
	    			String json = dataJSON.toString();
					out.write(json.getBytes("UTF-8"));
	            	out.close();
				}
				catch (IOException e)
				{
					log.error("", e);
				}
        }
		return null;		
	}
	/**
	 * 接受菜单修改
	 * @return String
	 */
	public String accept()
	{
		StringBuffer sb = new StringBuffer();
		try
		{
			HttpServletRequest req = super.getRequest();
			Iterator<Map.Entry<String, String[]>> iterator = req.getParameterMap().entrySet().iterator();
			while(iterator.hasNext())
			{
				Map.Entry<String, String[]> e = iterator.next();
				sb.append("\r\n\t");
				sb.append(e.getKey());
				sb.append("=");
				for(String value : e.getValue())
					sb.append(value+"\t");
			}
//			log.info(sb.toString());
			this.controlMgr.accept(super.getRequest(), super.getUserAccount(), new JSONArray(req.getParameter("acceptdata")));
		}
		catch(Exception e)
		{
			log.error("Failed to accept the publish of programs "+sb, e);
			super.setResponseException("审批集群程序配置出现异常"+e.getMessage()+",请联系系统管理员解决。");
		}
		return this.doPublish();
	}
	/**
	 * 拒绝菜单修改
	 * @return
	 */
	public String reject()
	{
		try
		{
			if( super.getMessage() == null || super.getMessage().isEmpty() || super.getMessage().length() < 10 )
			{
				super.setResponseMessage("请输入拒绝理由，不少于10个字。");
				return doPublish();
			}
			StringBuffer sb = new StringBuffer();
			HttpServletRequest req = super.getRequest();
			Iterator<Map.Entry<String, String[]>> iterator = req.getParameterMap().entrySet().iterator();
			while(iterator.hasNext())
			{
				Map.Entry<String, String[]> e = iterator.next();
				sb.append("\r\n\t");
				sb.append(e.getKey());
				sb.append("=");
				for(String value : e.getValue())
					sb.append(value+"\t");
			}
//			log.info(sb.toString());
			JSONArray rejectdata = new JSONArray(req.getParameter("rejectdata"));
			String operlog = Tools.getFormatTime("yy-MM-dd HH:mm", System.currentTimeMillis())+
					"["+super.getUserAccount()+"]拒绝程序配置："+super.getMessage();
			for( int i = 0; i < rejectdata.length(); i++ )
			{
				JSONObject removing = rejectdata.getJSONObject(i);
				serverkey = removing.getString("serverkey");
				id = removing.getString("id");
				ip = removing.getString("ip");
				port = removing.getInt("port");
				String operuser = removing.getString("operuser");
				JSONObject config = ZKMgr.getZookeeper().getJSONObject("/cos/temp/program/publish/"+serverkey+":"+id);
				String previewurl = "control!preview.action?ip="+ip+"&port="+port+"&serverkey="+serverkey+"&id="+id;

				if( config == null )
				{
					responseMessage = "拒绝用户["+operuser+"]提交的伺服器【"+ip+"】程序["+id+"]发布失败，因为对应程序配置不存在";
		    		logoper(operuser, responseMessage, "程序管理", "拒绝原因是："+this.getMessage(), null);
		    		if( !operuser.equals(super.getUserAccount()) )
		    		controlMgr.sendNotiefieToAccount(
		    				super.getRequest(),
		    				operuser,
							"集群程序管理",
							responseMessage,
							"拒绝原因是："+this.getMessage(),
							null,
		                    "程序管理", "control!open.action?id="+serverkey);
				}
				else
				{
					config.put("operlog", operlog);
					config.put("lastoper", config.has("lastoper")?config.getInt("lastoper"):config.getInt("oper"));
					config.put("oper", ProgramLoader.OPER_REJECT);
					config.put("remark", getMessage());
					config.put("rjcttime", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()));
					config.put("reviewer", super.getUserAccount());
					ZKMgr.getZookeeper().setJSONObject("/cos/temp/program/publish/"+serverkey+":"+id, config);
					responseMessage = "拒绝用户["+operuser+"]提交的伺服器【"+ip+"】程序["+id+"]发布";
		    		logoper(operuser, responseMessage, "程序管理", null, previewurl, null);
		    		if( !operuser.equals(super.getUserAccount()) )
		    		controlMgr.sendNotiefieToAccount(
		    				super.getRequest(),
		    				operuser,
							"集群程序管理",
							responseMessage,
							"拒绝原因是："+this.getMessage(),
							previewurl,
		                    "程序管理", "control!open.action?id="+serverkey);
				}
			}
			
			responseMessage = "拒绝"+rejectdata.length()+"程序配置发布";
    		logoper(responseMessage, "程序管理", null, "control!publish.action?command="+ProgramLoader.OPER_REJECT);
    		SvrMgr.sendNotiefiesToSystemadmin(
    				super.getRequest(),
					"集群程序管理",
					String.format("用户[%s]"+responseMessage, super.getUserAccount()),
					operlog,
					"control!publish.action?command="+ProgramLoader.OPER_REJECT,
                    "集群程序管理", "control!navigate.action");
		}
		catch(Exception e)
		{
			log.debug("Failed to accept modulescfg for ", e);
			super.setResponseException("审批集群伺服器程序配置出现异常"+e+", 请联系系统管理员解决。");
		}
		return this.doPublish();
	}
	
	public int getCommand()
	{
		return this.command;
	}

	public void setCommand(int command)
	{
		this.command = command;
	}
	
	public String getControlXml()
	{
		return controlXml;
	}

	public void setControlXml(String controlXml)
	{
		this.controlXml = controlXml;
	}

	public void setControlMgr(ControlMgr controlMgr) {
		this.controlMgr = controlMgr;
	}
	
	public void setUserMgr(UserMgr userMgr) {
		this.userMgr = userMgr;
	}

	public String getServerkey() {
		return serverkey;
	}

	public void setServerkey(String serverkey) {
		this.serverkey = serverkey;
	}
	
	public String getServertype() {
		return servertype;
	}

	public void setServertype(String servertype) {
		this.servertype = servertype;
	}

	public void setFilesMgr(FilesMgr filesMgr) {
		this.filesMgr = filesMgr;
	}
	
	public void setId(String id)
	{
		if( id != null && id.endsWith("*") )
		{
			id = id.substring(0, id.length()-1);
		}
		this.id = id;
	}
	
}
