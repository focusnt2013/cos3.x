package com.focus.cos.web.dev.action;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.data.Stat;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Node;

import com.focus.control.SystemPerf;
import com.focus.cos.control.Command;
import com.focus.cos.control.ProgramLoader;
import com.focus.cos.web.common.COSConfig;
import com.focus.cos.web.common.HelperMgr;
import com.focus.cos.web.common.Kit;
import com.focus.cos.web.common.ZKMgr;
import com.focus.cos.web.dev.service.ProgramMgr;
import com.focus.cos.web.ops.service.FilesMgr;
import com.focus.cos.web.ops.service.MonitorMgr;
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
public class ProgramAction extends DevAction
{
	private static final long serialVersionUID = 1L;
	public static final Log log = LogFactory.getLog(ProgramAction.class);
	/*远过程调用*/
	private ProgramMgr programMgr;
	//用户管理器
	private UserMgr userMgr;
	/*文件管理器*/
	protected FilesMgr filesMgr;
	/*服务器引擎配置
	private ProgramConfig serviceConfig;*/
	/*主控XML*/
	private String controlXml;
	
	public ProgramAction()
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
			rsp.put("alt", "上传程序配置到系统【"+sysid+"】程序管理失败，因为未能收到文件包");
			return response(super.getResponse(), rsp.toString());
		}	
		if( sysid == null || sysid.isEmpty() )
		{
			rsp.put("alt", "上传程序配置脚本到系统【"+sysid+"】程序管理失败，因为未知请求操作(sysid=null)");
			return response(super.getResponse(), rsp.toString());
		}

		log.info("Upload program.json from tempfile "+uploadfile+" "+sysid);
		String context = "读取脚本文件出错";
        try
        {
        	context = new String(IOHelper.readAsByteArray(uploadfile), "UTF-8");
        	JSONObject config = new JSONObject(context);
        	if( !config.has("id") )
        	{
    			rsp.put("alt", "上传程序配置脚本到系统【"+sysid+"】程序管理失败，因为没有配置程序ID");
    			return response(super.getResponse(), rsp.toString());
        	}
        	if( !config.has("name") )
        	{
    			rsp.put("alt", "上传程序配置脚本到系统【"+sysid+"】程序管理失败，因为没有配置程序名称");
    			return response(super.getResponse(), rsp.toString());
        	}
        	if( !config.has("startup") )
        	{
    			rsp.put("alt", "上传程序配置脚本到系统【"+sysid+"】程序管理失败，因为没有启动命令");
    			return response(super.getResponse(), rsp.toString());
        	}
        	if( !(config.get("startup") instanceof JSONObject) )
        	{
    			rsp.put("alt", "上传程序配置脚本到系统【"+sysid+"】程序管理失败，因为设置启动命令格式配置错误，请下载一个正确的配置作为模板");
    			return response(super.getResponse(), rsp.toString());
        	}
        	if( config.has("shutdown") && !(config.get("shutdown") instanceof JSONObject) )
        	{
    			rsp.put("alt", "上传程序配置脚本到系统【"+sysid+"】程序管理失败，因为设置停止命令格式配置错误，请下载一个正确的配置作为模板");
    			return response(super.getResponse(), rsp.toString());
        	}
            log.info("Succeed to parse the program.json.");
	        JSONObject operlog = new JSONObject();
	        operlog.put("remark", "通过程序配置脚本上传");
			operlog.put("user", super.getUserAccount());
	        this.programMgr.saveProgramConfig(sysid, config, operlog);
			this.programMgr.loadProgramProfiles(config);
            this.responseMessage = "通过上传程序配置脚本添加程序配置到系统【"+sysid+"】程序管理成功";
    		logoper(responseMessage, "程序管理", null, "program!preview.action?sysid="+sysid+"&id="+config.getString("id"));
			rsp.put("alt", responseMessage);
			rsp.put("succeed", true);
			rsp.put("result", programMgr.getAllProgramConfigs(sysid).toString());
        }
        catch(Exception e)
        {
        	e.printStackTrace();
            this.responseException = "通过上传程序配置脚本添加程序配置到系统【"+sysid+"】程序管理因为无法正确解析上传文件出现异常"+e.getMessage();
    		logoper(responseException, "程序管理", null, null, e);
			rsp.put("alt", responseException);
    		SvrMgr.sendNotiefiesToSystemadmin(
    				super.getRequest(),
					"开发管理",
					String.format("用户[%s]"+responseException, super.getUserAccount()),
					context,
					null,
                    "情况确认", "#feedback?to="+super.getUserAccount());
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
			rsp.put("alt", "上传程序配置到系统【"+sysid+"】程序管理失败，因为未能收到文件包");
			return response(super.getResponse(), rsp.toString());
		}
		if( sysid == null || sysid.isEmpty() )
		{
			rsp.put("alt", "上传程序配置脚本到系统【"+sysid+"】程序管理失败");
			return response(super.getResponse(), rsp.toString());
		}

		log.info("Upload control.xml from tempfile "+uploadfile+" to "+sysid);
		XMLParser xml = null;
		Zookeeper zookeeper = null;
        try
        {
        	xml = new XMLParser(uploadfile);
            log.info("Succeed to parse the control.xml.");
			zookeeper = ZKMgr.getZookeeper();
			HashMap<String, JSONObject> mapOld = new HashMap<String, JSONObject>();
	        JSONArray myPrograms = new JSONArray();
	        zookeeper.getJSONArray("/cos/config/modules/"+sysid+"/program", myPrograms);
			for( int i = 0; i < myPrograms.length(); i++)
	        {
				JSONObject e = myPrograms.getJSONObject(i);
				mapOld.put(e.getString("id"), e);
	        }
			
			JSONArray array = new JSONArray();
	        Node moduleNode = XMLParser.getElementByTag( xml.getRootNode(), "module" );
	        StringBuffer sb = new StringBuffer("上传的程序配置如下表所示详情请打开【程序管理】，程序配置申请待系统管理员审核");
	        for( ; moduleNode != null; moduleNode = XMLParser.nextSibling(moduleNode) )
	        {
            	JSONObject config = new JSONObject();
            	ProgramLoader.loadProgram(moduleNode, config);
		        JSONObject operlog = new JSONObject();
		        operlog.put("remark", "通过主控配置文件上传");
				operlog.put("user", super.getUserAccount());
				id = config.getString("id");
				this.programMgr.saveProgramConfig(sysid, config, operlog);
				sb.append("\r\n\t["+config.getString("id")+"] "+config.getString("name"));
				JSONObject maintenance = config.has("maintenance")?config.getJSONObject("maintenance"):null;
				config.put("programmer", maintenance!=null?maintenance.getString("programmer"):"N/A");
				config.put("icon", "images/icons/tile.png");
	    		config.put("title", config.getString("id"));
				this.programMgr.loadProgramProfiles(config);
				array.put(config);
	        }
            
            this.responseMessage = "通过上传主控配置文件添加程序配置到系统【"+sysid+"】程序管理成功";
    		logoper(responseMessage, "程序管理", sb.toString(), null);
			rsp.put("alt", responseMessage);
			rsp.put("succeed", true);
			rsp.put("result", programMgr.getAllProgramConfigs(sysid).toString());
    		SvrMgr.sendNotiefiesToSystemadmin(
    				super.getRequest(),
					"开发管理",
					String.format("用户[%s]"+responseMessage, super.getUserAccount()),
					sb.toString(),
					null,
                    "程序管理", "control!open.action?id="+id);
        }
        catch(Exception e)
        {
            this.responseException = "通过上传主控配置文件添加程序配置到系统【"+sysid+"】程序管理因为无法正确解析上传文件出现异常"+e.getMessage();
    		logoper(responseException, "程序管理", null, null, e);
			ByteArrayOutputStream out1 = new ByteArrayOutputStream(1024);
			rsp.put("alt", responseException);
			PrintStream ps = new PrintStream(out1);
    		e.printStackTrace(ps);
    		SvrMgr.sendNotiefiesToSystemadmin(
    				super.getRequest(),
					"开发管理",
					String.format("用户[%s]"+responseException, super.getUserAccount()),
					out1.toString(),
					null,
                    "情况确认", "#feedback?to="+super.getUserAccount());
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
		log.info("Download "+id+".cfg from "+sysid);
    	ServletOutputStream out = null;
        try
        {
	    	this.localDataObject = ZKMgr.getZookeeper().getJSONObject("/cos/config/modules/"+sysid+"/program/"+id);
	        if( localDataObject == null )
	        {
	        	this.responseException = "未找到您要下载的程序配置["+id+"]";
	        	return "alert";
	        }
        	String filename = id+".json";
    		JSONObject config = this.programMgr.resetConfig(localDataObject);//.programMgr.getConstructProgramConfig(this.localDataObject);
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
            this.responseMessage = "从系统【"+sysid+"】程序管理下载程序["+id+"]主控配置文件成功。";
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
        	this.responseException ="从系统【"+sysid+"】程序管理下载程序["+id+"]主控配置文件出现异常 "+e.getMessage();
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
	 * 得到程序配置的XML
	 * @param myPrograms
	 * @return
	 */
	private String getProgramXml(JSONArray myPrograms)
	{
        StringBuffer xml = new StringBuffer("<?xml version='1.0' encoding='UTF-8'?>");
        xml.append("\r\n<sys id='"+sysid+"'>");
        for( int i = 0; i < myPrograms.length(); i++ )
        {
        	JSONObject program = myPrograms.getJSONObject(i);
			StringBuffer module = new StringBuffer();
			JSONObject control = program.has("control")?program.getJSONObject("control"):null;
			StringBuffer c = new StringBuffer();
			if( control != null )
			{
				c.append(" mode='"+control.getInt("mode")+"'");
				c.append(" restartup='"+control.getInt("restartup")+"'");
				c.append(" delayed='"+control.getInt("delayed")+"'");
				c.append(" dependence='"+control.getString("dependence")+"'");
				c.append(" logfile='"+control.getString("logfile")+"'");
				c.append(" pidfile='"+control.getString("pidfile")+"'");
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
        return xml.toString();
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
		log.info("Download control.xml from "+sysid);
    	Zookeeper zookeeper = null;
    	ServletOutputStream out = null;
        try
        {
        	zookeeper = ZKMgr.getZookeeper();
	        JSONArray myPrograms = new JSONArray();
	        zookeeper.getJSONArray("/cos/config/modules/"+sysid+"/program", myPrograms);
			String xml = getProgramXml(myPrograms);
	        byte[] payload = xml.getBytes("UTF-8");
        	String filename = "control.xml";
			getResponse().setContentType("application/xml;charset=UTF-8");
            getResponse().setHeader("Content-disposition", "attachment; filename*=UTF-8''"+URLEncoder.encode(filename,"UTF-8"));
    		out = getResponse().getOutputStream();
    		out.write(payload);
    		out.flush();
            this.responseMessage = "从系统【"+sysid+"】程序管理下载主控配置文件成功。";
    		logoper(responseMessage, "开发管理", new String(payload, "UTF-8"), null);
    		SvrMgr.sendNotiefiesToSystemadmin(
    				super.getRequest(),
					"开发管理",
					String.format("用户[%s]"+responseMessage, super.getUserAccount()),
					new String(payload, "UTF-8"),
					null,
                    "情况确认", "#feedback?to="+super.getUserAccount());
        }
        catch(Exception e)
        {
        	this.responseException ="从系统【"+sysid+"】程序管理下载主控配置文件出现异常 "+e.getMessage();
    		logoper(responseException, "开发管理", null, null, e);
			ByteArrayOutputStream out1 = new ByteArrayOutputStream(1024);
			PrintStream ps = new PrintStream(out1);
    		e.printStackTrace(ps);
    		SvrMgr.sendNotiefiesToSystemadmin(
    				super.getRequest(),
					"开发管理",
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
	 * 集群伺服器程序管理
	 * @return
	 */
	public String config()
	{
        this.jsonData = programMgr.getAllProgramConfigs(sysid).toString();
        jsonData = Tools.replaceStr(jsonData, "\\", "\\\\");
        jsonData = Tools.replaceStr(jsonData, "'", "\\'");
		return "config";
	}
	
	/**
	 * 预览程序配置
	 * @return
	 */
	public String preview()
	{
		this.editable = false;
		jsonData = "";
		try 
		{
			if( id == null || id.isEmpty() )
			{
				this.responseMessage = "输入了错误的程序标识，无法预览该程序的程序配置";
				return "alert";
			}
	    	this.localDataObject = ZKMgr.getZookeeper().getJSONObject("/cos/config/modules/"+sysid+"/program/"+id);
	        if( localDataObject == null )
	        {
	        	this.localDataObject = ZKMgr.getZookeeper().getJSONObject("/cos/config/modules/"+sysid+"/program/removed/"+id);
	       }
	        if( localDataObject == null )
	        {
				this.responseMessage = "该程序["+id+"]配置已经丢失。";
				return "alert";
	        }
        	localDataObject = programMgr.getUnconstructProgramConfig(localDataObject);
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
			log.error("Failed to preview program "+id, e);
			this.responseException = "无法预览程序配置因为解析程序配置配置出现异常"+e.getMessage();
        	return "alert";
		}
	}
	
	/**
	 * 列出所有配置的程序配置
	 * @return
	 */
	public String list()
	{
		return grid("/grid/local/modulesprogramcfg.xml");
	}
	/**
	 * 配置数据JSON
	 * @return
	 */
	public String configdata()
	{
    	ServletOutputStream out = null;
		JSONObject dataJSON = new JSONObject();
		try
		{
			HttpServletResponse response = super.getResponse();
            out = response.getOutputStream();
			response.setContentType("text/json;charset=utf8");
    		response.setHeader("Content-disposition", "inline; filename="+System.currentTimeMillis()+".json");

    		String zkpath = "/cos/config/modules/"+sysid+"/program";//记录当前日志
			Zookeeper zookeeper = ZKMgr.getZookeeper();
			this.localDataArray = zookeeper.getJSONArray(zkpath, false);
			for(int i = 0; i < localDataArray.length(); i++)
			{
				JSONObject config = localDataArray.getJSONObject(i);
				config = ProgramMgr.getUnconstructProgramConfig(config);
				if( config.has("startupCommands") )
				{
					config.put("startupCommands", Kit.unicode2Chr(config.getString("startupCommands")));
				}
				if( config.has("shutdownCommands") )
				{
					config.put("shutdownCommands", Kit.unicode2Chr(config.getString("shutdownCommands")));
				}
				if( config.has("removed") )
				{
					if( !"-1".equals(id) )
					{
						localDataArray.remove(i);
						i -= 1;
					}
				}
				else
				{
					if( !"0".equals(id) )
					{
						localDataArray.remove(i);
						i -= 1;
					}
				}
				if( config.has("publish") )
				{
					JSONObject publish = config.getJSONObject("publish");
					JSONArray array = new JSONArray();
					Iterator<?> iterator = publish.keys();
					while(iterator.hasNext())
					{
						String serverid = iterator.next().toString();
						JSONObject e = publish.getJSONObject(serverid);
						array.put(e);
						e.put("address", e.getString("ip")+":"+e.getInt("port"));
						e.put("remark", "["+e.getString("serverid")+"] "+e.getString("desc"));
					}
					config.put("publish", array);
				}
			}
    		dataJSON.put("totalRecords", localDataArray.length());
			dataJSON.put("curPage", 1);
			dataJSON.put("data", localDataArray);
		}
		catch (Exception e)
		{
			log.error("Failed to query the alarms of instant for exception:", e);
    		dataJSON.put("totalRecords", 0);
			dataJSON.put("curPage", 0);
			dataJSON.put("hasException", true);
			dataJSON.put("message", "获取数据出现异常:"+e.getMessage());
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
			this.ip = "127.0.0.1";
			this.port = COSConfig.getLocalControlPort();
			String zkpath = "/cos/config/modules/"+sysid+"/program";
			zookeeper = ZKMgr.getZookeeper();
	    	this.localDataObject = zookeeper.getJSONObject(zkpath+"/"+id);
	    	this.localDataArray = zookeeper.getJSONArray(zkpath, false);
			ArrayList<Item> dependences = new ArrayList<Item>();
	        for( int i = 0; i < localDataArray.length(); i++ )
	        {
	        	obj = localDataArray.getJSONObject(i);
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
	        	obj = localDataObject;
	        	localDataObject = ProgramMgr.getUnconstructProgramConfig(localDataObject);
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
	        	zkpath = "/cos/config/modules/"+sysid;
	        	JSONObject module = zookeeper.getJSONObject(zkpath);
	        	if( module.has("Developers") )
	        	{
	        		JSONObject developers = module.getJSONObject("Developers");
		        	JSONArray users = userMgr.getRoleUsers(super.getUserRole(), developers);
			        this.localData = users.toString();
	        	}
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
			this.setResponseException("打开程序配置视图失败，因此异常:"+e.getMessage());
        	return "alert";
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
	        JSONArray myPrograms = new JSONArray();
	        ZKMgr.getZookeeper().getJSONArray("/cos/config/modules/"+sysid+"/program", myPrograms);
			this.controlXml = this.getProgramXml(myPrograms);
		}
		catch (Exception e)
		{
			this.responseException = "Failed to preset control from "+sysid+" for "+e.toString();
		}
		return "configxml";
	}
	/**
	 * 查看预览配置文件
	 */
	public String previewxml()
	{
    	byte[] payload = new byte[256];
    	payload[0] = Command.CONTROL_CONTROLXMLPREVIEW;
    	payload[1] = 0;    	
    	ServletOutputStream out = null;
        try
        {
			out = getResponse().getOutputStream();
	        JSONArray myPrograms = new JSONArray();
	        ZKMgr.getZookeeper().getJSONArray("/cos/config/modules/"+sysid+"/program", myPrograms);
	        controlXml = getProgramXml(myPrograms);
            previewxml(out, this.controlXml.getBytes());
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
	 * 程序版本Timeline
	 * @param data
	 * @param text
	 * @return
	 */
	public String untimeline(JSONObject data, String text)
	{
		String startDate = Tools.getFormatTime("yyyy,M", System.currentTimeMillis()-Tools.MILLI_OF_DAY);
		JSONObject timeline = new JSONObject();
		timeline = new JSONObject();
		timeline.put("headline", "系统【"+sysid+"】程序["+id+"]");
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
	public String versiontimeline()
	{
		log.info("Open the timeline of versions "+id+" from "+sysid);
//		RunFetchMonitor runner = getMonitorMgr().getMonitor().getRunFetchMonitor(ip, port);
		JSONObject data = new JSONObject();
		Zookeeper zookeeper = null;
		String zkpath = "";
		try
		{
			zookeeper = ZKMgr.getZookeeper();
			zkpath = "/cos/config/modules/"+sysid+"/program/"+id;
			Stat stat = zookeeper.exists(zkpath, false);
			if( stat == null )
			{
				return null;
			}
			long fistTs = System.currentTimeMillis();
			this.localDataObject = zookeeper.getJSONObject(zkpath);
			JSONArray timeline = localDataObject.has("timeline")?localDataObject.getJSONArray("timeline"):new JSONArray();
			for(int i = 0; i < timeline.length(); i++)
			{
				JSONObject timeline0 = timeline.getJSONObject(i);
				String time = timeline0.getString("time");//dd/MM/yyyy
				String[] args = Tools.split(time, "/");
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(0);
				calendar.set(Integer.parseInt(args[2]), Integer.parseInt(args[0])-1, Integer.parseInt(args[1]));
				timeline0.put("startDate", Tools.getFormatTime("yyyy,M,d", calendar.getTimeInMillis()));
				timeline0.put("headline", "v"+timeline0.getString("version"));
				JSONObject asset = new JSONObject();
				asset.put("media", HelperMgr.getImgWallpaper());
				asset.put("credit", "");
				asset.put("caption", "");
				timeline0.put("asset", asset);
				if( calendar.getTimeInMillis() < fistTs ) fistTs = calendar.getTimeInMillis();
			}
			JSONObject version = new JSONObject();
			JSONObject asset = new JSONObject();
			asset.put("media", "images/notes.png");
			asset.put("credit", "");
			asset.put("caption", "");
			version.put("asset", asset);
			version.put("date", timeline);
			version.put("headline", "系统【"+sysid+"】程序["+id+"]");
			version.put("type", "default");
			version.put("startDate", Tools.getFormatTime("yyyy,M", fistTs));
			StringBuffer text = new StringBuffer();
			text.append("<span class='version'>"+this.localDataObject.getString("version")+"</span>");
			text.append("<span class='title'>"+localDataObject.getString("name")+"</span><br/>");
			text.append(localDataObject.getString("description"));
			version.put("text", text.toString());
			data.put("timeline", version);
//			System.err.println(data.toString(4));
		}
		catch(Exception e)
		{
			log.error("Failed to get the version from "+zkpath, e);
			return untimeline(data, "构建程序["+id+"]的版本数据出现异常"+e.getMessage());
		}
			
//		System.err.println(data.toString(4));
		return response(super.getResponse(), data.toString());
	}
	/**
	 * 
	 * @return
	 */
	public String presetversion()
	{
		log.info("Preset the version of "+id+" from "+sysid);
		Zookeeper zookeeper = null;
		String zkpath = "";
		try
		{
			zookeeper = ZKMgr.getZookeeper();
			zkpath = "/cos/config/modules/"+sysid+"/program/"+id;
			this.localDataObject = zookeeper.getJSONObject(zkpath);
			JSONArray timeline = localDataObject.has("timeline")?localDataObject.getJSONArray("timeline"):new JSONArray();
//			JSONObject version = new JSONObject();
//			version.put("data", timeline);
//			version.put("version", localDataObject.getString("version"));
//			version.put("name", localDataObject.getString("name"));
//			version.put("remark", localDataObject.getString("description"));
			this.localData = timeline.toString();
			String rspstr = super.grid("/grid/local/modulesprogramversion.xml");
			return rspstr;
		}
		catch(Exception e)
		{
			super.setResponseException("打开程序版本配置界面失败，因为异常"+e.getMessage());
			log.debug("Failed to initialize the view of cmpcfg for exception ", e);
			return "alert";
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
			zookeeper = ZKMgr.getZookeeper();
			zkpath = "/cos/config/modules/"+sysid+"/program/"+id;
			this.localDataObject = zookeeper.getJSONObject(zkpath);
			JSONArray timeline = localDataObject.has("timeline")?localDataObject.getJSONArray("timeline"):new JSONArray();
			JSONObject version = new JSONObject();
			version.put("version", req.getParameter("version"));
			version.put("time", req.getParameter("time"));
			version.put("text", req.getParameter("text"));
			for(int i = 0; i < timeline.length(); i++)
			{
				JSONObject e = timeline.getJSONObject(i);
				if( e.getString("version").equals(version.getString("version")))
				{
					e.put("time", req.getParameter("time"));
					e.put("text", req.getParameter("text"));
					version = null;
					break;
				}
			}
			if( version != null )
			{
				timeline.put(version);
			}
			zookeeper.setJSONObject(zkpath, this.localDataObject);
            this.responseMessage = "设置系统【"+sysid+"】程序管理程序【"+id+"】的版本[v"+req.getParameter("version")+"]成功。";
			response.put("message", responseMessage);
			String dataurl = "program!versiontimeline.action?id="+id+"&sysid="+sysid;
    		logoper(responseMessage, "程序管理", null, "helper!timeline.action?dataurl="+Kit.chr2Unicode(dataurl));
    		SvrMgr.sendNotiefiesToSystemadmin(
    				super.getRequest(),
					"开发管理",
					responseMessage,
					null,
					"helper!timeline.action?dataurl="+Kit.chr2Unicode(dataurl),
                    "情况确认", "#feedback?to="+super.getUserAccount());
		}
		catch (Exception e)
		{
			log.error("Failed to set the version of "+id, e);
            this.responseException = "设置系统【"+sysid+"】程序管理程序【"+id+"】的版本出现异常，因为"+e.getMessage();
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
		id = req.getParameter("data[id]");
		sysid = req.getParameter("data[sysid]");
		String version = req.getParameter("data[version]");
		try
		{
			zookeeper = ZKMgr.getZookeeper();
			this.localDataObject = zookeeper.getJSONObject(zkpath);
			JSONArray timeline = localDataObject.has("timeline")?localDataObject.getJSONArray("timeline"):new JSONArray();
			for(int i = 0; i < timeline.length(); i++)
			{
				JSONObject e = timeline.getJSONObject(i);
				if( e.getString("version").equals(version))
				{
					timeline.remove(i);
					break;
				}
			}
			zookeeper.setJSONObject(zkpath, this.localDataObject);
            this.responseMessage = "删除系统【"+sysid+"】程序管理程序【"+id+"】的版本[v"+version+"]成功。";
			response.put("message", responseMessage);
    		String dataurl = "program!versiontimeline.action?id="+id+"&sysid="+sysid;
    		logoper(responseMessage, "程序管理", null, "helper!timeline.action?dataurl="+Kit.chr2Unicode(dataurl));
    		SvrMgr.sendNotiefiesToSystemadmin(
    				super.getRequest(),
					"开发管理",
					responseMessage,
					null,
					"helper!timeline.action?dataurl="+Kit.chr2Unicode(dataurl),
                    "情况确认", "#feedback?to="+super.getUserAccount());
		}
		catch (Exception e)
		{
			log.error("Failed to delete "+version+" for exception ", e);
            this.responseException = "删除系统【"+sysid+"】程序管理程序【"+id+"】的版本出现异常，因为"+e.getMessage();
    		logoper(responseException, "程序管理", null, null, e);
			response.put("hasException", true);
			response.put("message", responseException);
		}
		return response(super.getResponse(), response.toString());
	}

	/**
	 * 配置数据JSON
	 * @return
	 */
	public String publishdata()
	{
		JSONObject privileges = null;
    	ServletOutputStream out = null;
		JSONObject dataJSON = new JSONObject();
		try
		{
			JSONObject filter = null;
			if( pq_filter != null )
			{
				filter = new JSONObject(pq_filter);
			}
			HttpServletResponse response = super.getResponse();
            out = response.getOutputStream();
			response.setContentType("text/json;charset=utf8");
    		response.setHeader("Content-disposition", "inline; filename="+System.currentTimeMillis()+".json");

			String zkpath = "/cos/config/modules/"+sysid+"/program/"+id;
	        JSONObject config = ZKMgr.getZookeeper().getJSONObject(zkpath);
	        JSONObject publish = config.has("publish")?config.getJSONObject("publish"):new JSONObject();

			if( !"-1".equals(id) ) privileges = MonitorMgr.getInstance().getClusterPrivileges(super.getUserRole());
			ArrayList<JSONObject> servers = MonitorMgr.getInstance().getServers();
			localDataArray = new JSONArray();
    		HashMap<Integer, JSONObject> duplication = new HashMap<Integer, JSONObject>();
			for(JSONObject e : servers)
			{
				JSONObject server = new JSONObject(e.toString());
				if( !server.has("pid") ) continue;
				if( !server.has("security-key") ) continue;
				int pid = server.getInt("pid");
				String pname = null;
				if( !duplication.containsKey(pid) )
				{
					JSONObject cluster = MonitorMgr.getInstance().getCluster(pid);
					if( cluster == null ) continue;
					duplication.put(pid, cluster);
				}
				pname = duplication.get(pid).getString("name");
				
				String serverid = server.has("security-key")?server.getString("security-key"):"";
	            if( !isSysadmin() && ( privileges == null || !privileges.has(serverid)) )
	            	continue;//父级角色权限组权限不存在就排除过滤
	            ip = server.getString("ip");
	            port = server.getInt("port");
	            server.put("address", ip+":"+port);
	            server.put("pname", pname);
	            if( server.has("stateinfo") ) server.remove("stateinfo");
//	            if( server.has("port") ) port = (Integer)server.remove("port");
//	            if( server.has("ip") ) ip = (String)server.remove("ip");
		        SystemPerf sysPerf = getMonitorMgr().getSystemPerf(ip, port, server.getInt("id"));
				server.put("available", sysPerf != null);
	            server.put("published", 0);
		        if( sysPerf != null )
		        {
			        String servertype = sysPerf.getPropertyValue("OSName").toLowerCase();
			        if( servertype.indexOf("linux") != -1 )
			        {
			        	servertype = "linux";
			        }
			        else if( servertype.indexOf("windows") != -1 )
			        {
			        	servertype = "windows";
			        }
					server.put("servertype", servertype);
		            Zookeeper zk = null;
		            try
		            {
		            	zk = Zookeeper.getInstance(ip, port);
		    			String path = "/cos/config/program/"+Tools.encodeMD5(serverid)+"/publish/"+id;
		    			Stat stat = zk.exists(path);
		    			if( stat != null )
		    			{
			    			JSONObject program = zk.getJSONObject(path);
							program = programMgr.getUnconstructProgramConfig(program);
		    				server.put("version", program.getString("version"));
		    				server.put("publishtime", Tools.getFormatTime("yyyy-MM-dd HH:mm", stat.getMtime()));
		    				server.put("published", 1);
		    				server.put("description", program.getString("description"));
		    				server.put("control", program.getJSONObject("control"));
		    				server.put("maintenance", program.getJSONObject("maintenance"));
		    				if( program.has("startupCommands") )
		    				{
		    					server.put("startupCommands", Kit.unicode2Chr(program.getString("startupCommands")));
		    				}
		    				if( program.has("shutdownCommands") )
		    				{
		    					server.put("shutdownCommands", Kit.unicode2Chr(program.getString("shutdownCommands")));
		    				}
		    			}
		    		}
		    		catch(Exception e1)
		    		{
		    		}
		    		finally
		    		{
		    			if( zk != null ) zk.close();
		    		}
		        }

	            if( filter != null && filter.has("data") )
	            {
	            	JSONArray data = filter.getJSONArray("data");
	            	boolean f = false;
	            	for(int i = 0; i < data.length(); i++ )
	            	{
	            		JSONObject c = data.getJSONObject(i);
	            		if( "pid".equalsIgnoreCase(c.getString("dataIndx")) )
	            		{
	            			if( server.getInt("pid") != Integer.parseInt(c.getString("value")) )
	            			{
	            				f = true;
	            				continue;
	            			}
	            		}
	            		if( "servertype".equalsIgnoreCase(c.getString("dataIndx")) )
	            		{
	            			String val = c.getString("value");
	            			String servertype = server.has("servertype")?server.getString("servertype"):"";
	            			if( !val.isEmpty() && !servertype.equals(val) )
	            			{
	            				f = true;
	            				continue;
	            			}
	            		}
	            	}
	            	if( f ) continue;
	            }
	            localDataArray.put(server);
	            server.put("publish", publish.has(serverid));
			}
    		dataJSON.put("totalRecords", localDataArray.length());
			dataJSON.put("curPage", 1);
			dataJSON.put("data", localDataArray);
		}
		catch (Exception e)
		{
			log.error("Failed to query the data of publish for exception:", e);
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
	 * 发布配置
	 * @return
	 */
	public String publish()
	{
		try
		{
	    	BasicDBList options = new BasicDBList();
			labelsModel.put("pid", options);
			ArrayList<JSONObject> clusters = new ArrayList<JSONObject>();
			MonitorMgr.getInstance().loadClusters(clusters);
			for(JSONObject e: clusters)
			{
				BasicDBObject option = new BasicDBObject();
				option.put("value", String.valueOf(e.getInt("id")));
				option.put("label", e.getString("name"));
				options.add(option);
			}
		}
		catch (Exception e) 
		{
			log.error("Failed to initialize the view of modules config for exception ", e);
			super.setResponseException("初始化界面失败，因为异常"+e);
			return "alert";
		}
		this.showTitle = true;
		return super.grid("/grid/local/modulesprogrampublish.xml");
	}

	public String getControlXml()
	{
		return controlXml;
	}

	public void setControlXml(String controlXml)
	{
		this.controlXml = controlXml;
	}

	public void setProgramMgr(ProgramMgr programMgr) {
		this.programMgr = programMgr;
	}
	
	public void setUserMgr(UserMgr userMgr) {
		this.userMgr = userMgr;
	}

	public void setFilesMgr(FilesMgr filesMgr) {
		this.filesMgr = filesMgr;
	}
	
	public void setId(String id)
	{
		this.id = id;
	}
	
}
