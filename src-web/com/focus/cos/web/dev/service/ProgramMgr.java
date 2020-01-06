package com.focus.cos.web.dev.service;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.data.Stat;
import org.json.JSONArray;
import org.json.JSONObject;

import com.focus.control.SystemPerf;
import com.focus.cos.control.ProgramLoader;
//import com.focus.cos.ops.vo.ProgramConfig;
import com.focus.cos.web.common.AjaxResult;
import com.focus.cos.web.common.Kit;
import com.focus.cos.web.common.ZKMgr;
import com.focus.cos.web.service.SvrMgr;
import com.focus.util.QuickSort;
import com.focus.util.Tools;
import com.focus.util.Zookeeper;

/**
 * 系统开发开发管理
 * @author focus
 * @version 重构于2017年2月21日
 */
public class ProgramMgr extends SvrMgr
{
	public static final Log log = LogFactory.getLog(ProgramMgr.class);
	
	/**
	 * 
	 * @param sysid
	 * @return
	 */
	public JSONArray getAllProgramConfigs(String sysid)
	{
        JSONArray trees = new JSONArray();
		Zookeeper zookeeper = null;
		String zkpath = null;
		JSONObject e = null;
		try 
		{
			zookeeper = ZKMgr.getZookeeper();
			JSONObject myProgram = new JSONObject();
			myProgram.put("id", "0");
			myProgram.put("name", "我的系统程序");
			myProgram.put("type", "dir");
			myProgram.put("icon", "images/icons/programmer.png");
	        trees.put(myProgram);

			JSONObject removedProgram = new JSONObject();
			removedProgram.put("id", "-1");
			removedProgram.put("name", "已删除的程序");
			removedProgram.put("type", "dir");
			removedProgram.put("icon", "images/icons/blocked.png");
	        trees.put(removedProgram);

	        JSONArray removedPrograms = new JSONArray();
	        JSONArray myPrograms = new JSONArray();
	        zookeeper.getJSONArray("/cos/config/modules/"+sysid+"/program", myPrograms);
			for( int i = 0; i < myPrograms.length(); i++)
	        {
				e = myPrograms.getJSONObject(i);
	    		e.put("icon", "images/icons/tile.png");
	    		e.put("title", e.getString("id"));
	        	loadProgramProfiles(e);
				if( e.has("removed") )
				{
					removedPrograms.put(e);
					myPrograms.remove(i);
					i -= 1;
					continue;
				}
			}
			myProgram.put("children", myPrograms);
			removedProgram.put("children", removedPrograms);
		}
		catch (Exception e1) 
		{
			log.error("Found zkpath "+zkpath+"\r\n"+(e!=null?e.toString(4):"Null"), e1);
		}
		return trees;
	}
	/**
	 * 保存程序配置
	 * @param sysid
	 * @param config
	 * @param operuser
	 * @return
	 * @throws Exception
	 */
	public String saveProgramConfig(String sysid, JSONObject config, JSONObject operlog)
		throws Exception
	{
		String rspmsg = null;
		Zookeeper zookeeper = null;
		operlog.put("time", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()));
		String id = config.getString("id");

		zookeeper = ZKMgr.getZookeeper();
		String zkpath = "/cos/config/modules/"+sysid+"/program/"+id;
		Stat stat = zookeeper.exists(zkpath, false);
		rspmsg = "新增系统【"+sysid+"】程序["+id+"]配置";
		if( stat != null )
		{//ProgramLoader.OPER_DELETING  ProgramLoader.OPER_ADDING  ProgramLoader.OPER_EDITING
			JSONObject config0 = ZKMgr.getZookeeper().getJSONObject(zkpath);
			String version = config.has("version")?config.getString("version"):(config0.has("version")?config0.getString("version"):"0.0.0.0");
			operlog.put("oper", ProgramLoader.OPER_EDITING);
			JSONArray timeline = config0.has("timeline")?config0.getJSONArray("timeline"):new JSONArray();
			int i = 0;
			for( ; i < timeline.length(); i++ )
			{
				JSONObject date = timeline.getJSONObject(i);
				if( !date.has("version") )
				{
					timeline.remove(i);
					i -= 1;
					continue;
				}
				if( version.equals(date.getString("version")) )
				{
					date.put("text", config.getString("versionremark"));
					break;
				}
			}
			if( i == timeline.length() )
			{
				JSONObject date = new JSONObject();
				date.put("version", config.getString("version"));
				date.put("time", Tools.getFormatTime("MM/dd/yyyy", System.currentTimeMillis()));
				date.put("text", config.getString("versionremark"));
				timeline.put(date);
			}
			config.remove("versionremark");
			config.put("timeline", timeline);
			if( config0!=null&&config0.has("publish") )	config.put("publish", config0.getJSONObject("publish"));
			if( config0!=null&&config0.has("publishlogs") )	config.put("publishlogs", config0.getJSONArray("publishlogs"));
			JSONArray operlogs = config0.has("operlogs")?config0.getJSONArray("operlogs"):new JSONArray();
			operlogs.put(operlog);
			config.put("operlogs", operlogs);
			zookeeper.setData(zkpath, config.toString().getBytes("UTF-8"), stat.getVersion());
		}
		else
		{
			operlog.put("oper", ProgramLoader.OPER_ADDING);
			
			JSONArray timeline = new JSONArray();
			JSONObject date = new JSONObject();
			date.put("version", config.has("version")?config.getString("version"):"0.0.0.0");
			date.put("time", Tools.getFormatTime("MM/dd/yyyy", System.currentTimeMillis()));
			date.put("text", config.has("versionremark")?config.getString("versionremark"):"没有版本说明");
			timeline.put(date);
			config.remove("versionremark");
			config.put("timeline", timeline);

			JSONArray operlogs = new JSONArray();
			operlogs.put(operlog);
			config.put("operlogs", operlogs);
			
			zookeeper.createObject(zkpath, config);
		}
		return rspmsg;
	}
	/**
	 * 设置程序发布
	 * @param sysid
	 * @param programid
	 * @param json
	 * @return
	 */
	public AjaxResult<String> setProgramPublish(String sysid, String programid, String json)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		Zookeeper zookeeper = null;
		try
		{
			zookeeper = ZKMgr.getZookeeper();
			JSONObject publish = new JSONObject(json);
			String zkpath = "/cos/config/modules/"+sysid+"/program/"+programid;
	        JSONObject config = zookeeper.getJSONObject(zkpath);
	        config.put("publish", publish);
	        zookeeper.setJSONObject(zkpath, config);
			rsp.setResult(config.toString());
			rsp.setMessage("设置系统【"+sysid+"】程序["+programid+"]发布配置，系统整体发布后提交管理员审核");
			rsp.setSucceed(true);
		}
		catch (Exception e)
		{
			log.error("", e);
			rsp.setMessage("配置系统【"+sysid+"】程序发布配置"+json+"出现异常:"+e.getMessage());
			logoper(rsp.getMessage(), "开发管理", e);
		}
		return rsp;
	}
	/**
	 * 得到所有配置中的程序数据
	 * @param sysid 如果是新的配置为null
	 * @param json 配置数据。
	 * @return
	 */
	public AjaxResult<String> setProgramConfig(String sysid, String json)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		try 
		{
			JSONObject config = this.getConstructProgramConfig(new JSONObject(json));
			String id = config.getString("id");
			JSONObject operlog = new JSONObject();
			operlog.put("remark", "用户通过界面操作配置");
			operlog.put("user", super.getAccountName());
			String rspmsg = this.saveProgramConfig(sysid, config, operlog);
			rsp.setMessage(rspmsg);
        	this.loadProgramProfiles(config);
        	config.put("programmer", "N/A");
        	if( config.has("maintenance") )
        	{
        		JSONObject maintenance = config.getJSONObject("maintenance");
        		if( maintenance.has("programmer") )
        		{
                	config.put("programmer", maintenance.getString("programmer"));
        		}
        	}
			rsp.setResult(config.toString());
			rsp.setSucceed(true);
			String contexturl = "program!preview.action?sysid="+sysid+"&id="+id;
			logoper(rsp.getMessage(), "开发管理", null, contexturl);
		}
		catch (Exception e)
		{
			log.error("", e);
			rsp.setMessage("配置系统【"+sysid+"】程序"+json+"出现异常:"+e.getMessage());
			logoper(rsp.getMessage(), "开发管理", e);
		}
		return rsp;
	}
	
	/**
	 * 设置容器信息
	 * @param sysPerf
	 * @param container
	 */
	public void setContainerInfo(SystemPerf sysPerf, JSONObject container)
	{
        if( sysPerf == null )
        {
        	container.put("OSName", "N/A");
        	container.put("runStateInfo", "N/A");
        	container.put("memoryUsageInfo", "N/A");
        	container.put("cpuUsageInfo", "N/A");
        	container.put("runState", 0);
        	container.put("memoryState", 0);
	        container.put("cpuState", 0);
        }
        else
        {
        	container.put("OSName", sysPerf.getPropertyValue("OSName"));
        	container.put("phyMemUsageDetail", sysPerf.getPhyMemUsageInof()+" "+sysPerf.getPhyMemUsageDetail());
        	container.put("phyDiskUsageDetail", sysPerf.getDiskUsageInfo()+" "+sysPerf.getDiskUsageDetail()+" "+sysPerf.getStoragesInfo());
        	container.put("runStateInfo", sysPerf.getProperty("RunStateInfo"));
        	container.put("memoryUsageInfo", sysPerf.getPropertyValue("TotalMemeory"));
        	container.put("cpuUsageInfo", sysPerf.getCpuLoadInfo());
        	container.put("netLoadInfo", sysPerf.getNetLoad());
        	container.put("ioLoadInfo", sysPerf.getIOLoad());
//        	Random random = new Random();
//        	this.command = random.nextInt(95);
        	container.put("runState", 1);
        	//TODO:如果程序占用内存小，物理内存占用高，要提示问题。
        	long runMemory = (Long)sysPerf.getProperty("RunMemory")*1024;
        	runMemory += (Long)sysPerf.getProperty("RunMemory0")*1024;
        	long useMemory = sysPerf.getPhyMemUsed();
        	int anaMemory = (int)((runMemory*100)/useMemory);
        	if( anaMemory < 60 )
        	{
        		container.put("memoryStateCheck", 2);
        		container.put("memoryStateCheckDesc", "配置的用户程序与系统程序合计使用的内存("+Kit.bytesScale(runMemory)+
        				")远远小操作系统物理使用内存("+Kit.bytesScale(useMemory)+")，占比"+anaMemory+
        				"%，请系统管理员检查操作系统相关配置做运维维护。");
//        		controlMgr.sendNotiefiesToSystemadmin(
//        				super.getRequest(),
//    					"开发管理",
//    					"系统【"+sysid+"】运行的程序使用内存相比实际物理使用内存低于30%",
//    					unconstructor.getString("memoryStateCheckDesc"),
//    					"control!open.action?id="+id,
//                        "情况确认", "#feedback?to="+super.getUserAccount());
            	if( anaMemory < 30 )
            	{
            		container.put("memoryStateCheck", 3);
            	}
        	}
        	container.put("memoryState", sysPerf.getMemoryState());
        	container.put("cpuState", sysPerf.getCpuState());
        	container.put("netState", sysPerf.getNetState());
        	container.put("diskState", sysPerf.getDiskState());
//        	System.err.println(server.toString(4));
        }
	}
	/**
	 * 删除程序配置
	 * @param sysid
	 * @param id
	 * @return
	 */
	public AjaxResult<String> delProgramConfig(String sysid, String id)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		Zookeeper zookeeper = null;
		try 
		{
			zookeeper = ZKMgr.getZookeeper();
			String zkpath = "/cos/config/modules/"+sysid+"/program/"+id;
	        JSONObject removing = zookeeper.getJSONObject(zkpath);
	        if( removing == null )
	        {
				throw new Exception("未找到要删除的程序配置");
	        }
	        removing.put("removed", true);

	        JSONObject operlog = new JSONObject();
	        operlog.put("remark", "通过界面管理删除了配置");
			operlog.put("user", super.getAccountName());
			operlog.put("time", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()));
			operlog.put("oper", ProgramLoader.OPER_DELETED);
			JSONArray operlogs = removing.has("operlogs")?removing.getJSONArray("operlogs"):new JSONArray();
			operlogs.put(operlog);
			removing.put("operlogs", operlogs);
			zookeeper.setJSONObject(zkpath, removing);
			//向集群开发管理发出删除指令
			this.loadProgramProfiles(removing);
			rsp.setResult(removing.toString());
			rsp.setMessage("删除系统【"+sysid+"】开发管理程序["+id+"]，系统已向伺服器发送删除指令");
			rsp.setSucceed(true);
		}
		catch (Exception e)
		{
			rsp.setMessage("删除系统【"+sysid+"】开发管理程序["+id+"]配置出现异常:"+e.getMessage());
			logoper(rsp.getMessage(), "开发管理", e);
		}
		return rsp;
	}

	/**
	 * 得到程序配置的展示对象数据
	 * @return
	 */
	public JSONObject getConstructProgramConfig(JSONObject unconstructor)
	{
		/*
		JSONObject config = new JSONObject();
		config.put("id", unconstructor.getString("id"));
		config.put("name", unconstructor.getString("name"));
		config.put("version", unconstructor.has("version")?unconstructor.getString("version"):"");
		config.put("description", unconstructor.has("description")?unconstructor.getString("description"):
			(unconstructor.has("remark")?unconstructor.getString("remark"):""));
		JSONObject maintenance = new JSONObject();
		maintenance.put("programmer", unconstructor.has("programmer")?unconstructor.getString("programmer"):"");
		maintenance.put("email", unconstructor.has("email")?unconstructor.getString("email"):"");
		maintenance.put("account", unconstructor.has("account")?unconstructor.getString("account"):"");
		maintenance.put("remark", unconstructor.has("remark")?unconstructor.getString("remark"):"");
		config.put("maintenance", maintenance);
		JSONObject control = new JSONObject();
		control.put("mode", unconstructor.has("mode")?unconstructor.getInt("mode"):0);
		control.put("logfile", unconstructor.has("pidfile")?unconstructor.getString("logfile"):"");
		control.put("restartup", unconstructor.has("restartup")?unconstructor.getInt("restartup"):0);
		control.put("delayed", unconstructor.has("delayed")?unconstructor.getInt("delayed"):0);
		control.put("starttime", unconstructor.has("starttime")?unconstructor.getString("starttime"):"");
		control.put("endtime", unconstructor.has("endtime")?unconstructor.getString("endtime"):"");
		control.put("dependence", unconstructor.has("dependence")?unconstructor.getString("dependence"):"");
		control.put("pidfile", unconstructor.has("pidfile")?unconstructor.getString("pidfile"):"");
		JSONObject forcereboot = new JSONObject();
		forcereboot.put("mode", unconstructor.has("frmode")?unconstructor.getString("frmode"):"");
		forcereboot.put("val", unconstructor.has("frval")?unconstructor.getString("frval"):"");
		forcereboot.put("time", unconstructor.has("frtime")?unconstructor.getString("frtime"):"");
		control.put("forcereboot", forcereboot);
		config.put("control", control);
		*/
		JSONObject startup = new JSONObject();
		if( unconstructor.has("startupCommands") )
		{
			JSONArray command = new JSONArray();
			JSONArray remark = new JSONArray();
			this.setCommands(
				unconstructor.getString("startupCommands"),
				unconstructor.has("startupCommandsRemark")?unconstructor.getString("startupCommandsRemark"):"",
				command, remark);
			startup.put("command", command);
			startup.put("remark", remark);
		}
		unconstructor.put("startup", startup);
		
		JSONObject shutdown = new JSONObject();
		if( unconstructor.has("shutdownCommands") )
		{
			JSONArray command = new JSONArray();
			JSONArray remark = new JSONArray();
			this.setCommands(
				unconstructor.getString("shutdownCommands"),
				unconstructor.has("shutdownCommandsRemark")?unconstructor.getString("shutdownCommandsRemark"):"",
					command, remark);
			shutdown.put("command", command);
			shutdown.put("remark", remark);
		}
		unconstructor.put("shutdown", shutdown);
		return unconstructor;
	}
	
	/**
	 * 得到程序配置进入编辑
	 * @return
	 */
	public static JSONObject getUnconstructProgramConfig(JSONObject config)
	{
		if( config.has("timeline") )
		{
			String version = config.has("version")?config.getString("version"):"0.0.0.0";
			JSONArray timeline = config.getJSONArray("timeline");
			for(int i =  0; i < timeline.length(); i++)
			{
				JSONObject date = timeline.getJSONObject(i);
				if( date.getString("version").equals(version))
				{
					config.put("versionremark",date.getString("text"));
					break;
				}
			}
		}
		if( config.has( "startup") )
		{
			JSONObject startup = config.getJSONObject("startup");
			if( startup.has("command") )
			{
	            StringBuffer sbCommand = new StringBuffer();
	            StringBuffer sbRemark = new StringBuffer();
	            JSONArray commands = startup.getJSONArray("command");
	            JSONArray remarks = startup.getJSONArray("remark");
	            for( int i = 0; i < commands.length(); i++)
	            {
	                String value = commands.getString(i);
	                sbCommand.append(value);
	                sbCommand.append(" ");
	                value = remarks.length()>i?remarks.getString(i):"";
	                sbRemark.append(value);
	                sbRemark.append("\n");
	            }
	            if( sbCommand.length() > 0 ) sbCommand.deleteCharAt(sbCommand.length()-1);
				config.put("startupCommands", Kit.chr2Unicode(sbCommand.toString()));
				config.put("startupCommandsRemark", Kit.chr2Unicode(sbRemark.toString()));
			}
		}

		if( config.has("shutdown") )
		{
			JSONObject shutdown = config.getJSONObject("shutdown");
			if( shutdown.has("command") )
			{
	            StringBuffer sbCommand = new StringBuffer();
	            StringBuffer sbRemark = new StringBuffer();
	            JSONArray commands = shutdown.getJSONArray("command");
	            JSONArray remarks = shutdown.getJSONArray("remark");
	            for( int i = 0; i < commands.length(); i++)
	            {
	                String value = commands.getString(i);
	                sbCommand.append(value);
	                sbCommand.append(" ");
	                value = remarks.length()>i?remarks.getString(i):"";
	                sbRemark.append(value);
	                sbRemark.append("\n");
	            }
	            if( sbCommand.length() > 0 ) sbCommand.deleteCharAt(sbCommand.length()-1);
				config.put("shutdownCommands", Kit.chr2Unicode(sbCommand.toString()));
				config.put("shutdownCommandsRemark", Kit.chr2Unicode(sbRemark.toString()));
			}
		}
		return config;
	}	
	/**
	 * 选择已配置好的版权数列表据
	 * @param ip
	 * @param port
	 * @param serverkey 服务器编码
	 * @param id
	 * @return
	 */
	public AjaxResult<String> getVersionSelection(String sysid, String id)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		String zkpath = "/cos/config/modules/"+sysid+"/program/"+id+"/version";
		Zookeeper zookeeper = null;
		try 
		{
			zookeeper = ZKMgr.getZookeeper();
			Stat stat = zookeeper.exists(zkpath, false);
			if( stat != null )
			{
				JSONObject timeline = new JSONObject(new String(zookeeper.getData(zkpath, false, stat), "UTF-8"));
				if( timeline.has("date") )
				{
					ArrayList<String> list = new ArrayList<String>();
					JSONArray date = timeline.getJSONArray("date");
					for(int i = 0; i < date.length(); i++)
					{
						JSONObject timeline0 = date.getJSONObject(i);
						String version0 = timeline0.getString("version");
						list.add(version0);
					}
					if( list.size() > 0 )
					{
						QuickSort sorter = new QuickSort() {
							public boolean compareTo(Object sortSrc, Object pivot) {
								String[] args = Tools.split(sortSrc.toString(), ".");
								int v0 = Integer.parseInt(args[0]);
								int v1 = Integer.parseInt(args[1]);
								int v2 = Integer.parseInt(args[2]);
								int v3 = Integer.parseInt(args[3]);
								long left = v0*100*100*100+v1*100*100+v2*100+v3;
								
								args = Tools.split(pivot.toString(), ".");
								v0 = Integer.parseInt(args[0]);
								v1 = Integer.parseInt(args[1]);
								v2 = Integer.parseInt(args[2]);
								v3 = Integer.parseInt(args[3]);
								long right = v0*100*100*100+v1*100*100+v2*100+v3;
								return left-right>0;
							}
						};
						sorter.sort(list);
//						System.err.println(list);
					}
					for(String v : list)
					{
						rsp.add(v);
					}
					list.clear();
				}
			}
			rsp.setSucceed(true);
		}
		catch (Exception e)
		{
			log.error("Failed to get the selection of version from zookeeper "+zkpath, e);
			rsp.setMessage("从系统【"+sysid+"】程序["+id+"]获得版本列表数据出现异常:"+e.getMessage());
		}
		return rsp;
	}
	/**
	 * 修改程序的名称和描述（允许直接修改，记录日志和发送系统消息即可）
	 * @param ip
	 * @param port
	 * @param servercode
	 * @param id
	 * @param name
	 * @param remark
	 * @param sid
	 * @return
	 */
	public AjaxResult<String> setProgramRemark(String sysid, String id, String name, String version, String description)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		Zookeeper zookeeper = null;
		try 
		{
			zookeeper = ZKMgr.getZookeeper();
			String zkpath = "/cos/config/modules/"+sysid+"/program/"+id;
			Stat stat = zookeeper.exists(zkpath, false);
			if( stat == null )
			{
				rsp.setMessage("未发现您要配置的系统【"+sysid+"】开发管理程序["+id+"]");
				return rsp;
			}

			byte[] payload = zookeeper.getData(zkpath, false, stat);
			JSONObject config = new JSONObject(new String(payload, "UTF-8"));
			String oldname = config.getString("name");
			String oldversion = config.getString("version");
			String olddesc = config.getString("description");
        	String release = "";
        	if( olddesc.isEmpty() ){
        		release = "添加了程序描述。";
        	}
        	else
        	{
        		if( description.equals(olddesc) )
        		{
        			release = "程序描述没有改变。";
        		}
        		else
        		{
        			release = "程序描述被重新设置。";
        		}
        	}
			config.put("name", name);
			config.put("description", description);
			config.put("version", version);
			zookeeper.setData(zkpath, config.toString().getBytes("UTF-8"), stat.getVersion());
			rsp.setMessage("设置系统【"+sysid+"】程序["+id+"]的名称和版本"+
					oldname+"-&gt;"+name+","+oldversion+"-&gt;"+version+","+release);
			rsp.setSucceed(true);
			String previewurl = "program!preview.action?sysid="+sysid+"&id="+id;
			logoper(rsp.getMessage(), "开发管理", "", previewurl);
			sendNotiefieToAccount(
					super.getAccountName(),
					"开发管理",
					"您"+rsp.getMessage(),
                    "您修改了程序名称或描述，简洁准确的名称和详细的描述将提升开发管理的效率。",
                    previewurl,
                    "开发管理", "program!config.action?sysid="+sysid+"&id="+id);
			sendNotiefiesToSystemadmin(
					"开发管理",
					String.format("用户[%s]"+rsp.getMessage(), getAccountName()),
                    "用户修改了程序名称或描述，简洁准确的名称和详细的描述将提升开发管理的效率。",
                    previewurl,
                    "开发管理", "program!config.action?sysid="+sysid+"&id="+id);
		}
		catch (Exception e)
		{
			rsp.setMessage("修改系统【"+sysid+"】程序["+id+"]的名称和描述出现异常:"+e.getMessage());
			logoper(rsp.getMessage(), "开发管理", e);
		}
		return rsp;
	}
	/**
	 * 设置开发管理员
	 * @param ip
	 * @param port
	 * @param id
	 * @param name
	 * @param account
	 * @param email
	 * @param sid
	 * @return
	 */
	public AjaxResult<String> setProgrammer(String sysid, String id, String programmer, String manager, String email)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		Zookeeper zookeeper = null;
		try 
		{
			zookeeper = ZKMgr.getZookeeper();
			String zkpath = "/cos/config/modules/"+sysid+"/program/"+id;
			Stat stat = zookeeper.exists(zkpath, false);
			if( stat == null )
			{
				rsp.setMessage("未发现您要配置的程序["+id+"]");
				return rsp;
			}

			byte[] payload = zookeeper.getData(zkpath, false, stat);
			JSONObject config = new JSONObject(new String(payload, "UTF-8"));
			JSONObject maintenance = new JSONObject();
			if( config.has("maintenance") ) maintenance = config.getJSONObject("maintenance");
			String oldprogrammer = maintenance.has("programmer")?maintenance.getString("programmer"):"未设置";
			maintenance.put("programmer", programmer );
			maintenance.put("manager", manager);
			maintenance.put("email", email);
			config.put("maintenance", maintenance);
			zookeeper.setData(zkpath, config.toString().getBytes("UTF-8"), stat.getVersion());
			rsp.setMessage("设置系统【"+sysid+"】程序["+id+"]的管理员"+oldprogrammer+"-&gt;"+programmer);
			rsp.setSucceed(true);
			String previewurl = "program!preview.action?sysid="+sysid+"&id="+id;
			logoper(rsp.getMessage(), "开发管理", "", previewurl);
			sendNotiefieToAccount(
					manager,
					"开发管理",
					String.format("用户[%s]将您设置为系统【"+sysid+"】程序["+id+"]的管理员", getAccountName()),
                    "开发管理员负责维护程序的正常运行，可以通过集群开发管理查看与配置程序。",
                    previewurl,
                    "开发管理", "program!config.action?sysid="+sysid+"&id="+id);
			sendNotiefieToAccount(
					super.getAccountName(),
					"开发管理",
					"您"+rsp.getMessage(),
                    "开发管理员负责维护程序的正常运行，可以通过集群开发管理查看与配置程序。",
                    previewurl,
                    "开发管理", "program!config.action?sysid="+sysid+"&id="+id);
			sendNotiefiesToSystemadmin(
					"开发管理",
					String.format("用户[%s]"+rsp.getMessage(), getAccountName()),
                    "开发管理员负责维护程序的正常运行，可以通过集群开发管理查看与配置程序。",
                    previewurl,
                    "开发管理", "program!config.action?sysid="+sysid+"&id="+id);
		}
		catch (Exception e)
		{
			rsp.setMessage("设置系统【"+sysid+"】程序["+id+"]的管理员出现异常:"+e.getMessage());
			logoper(rsp.getMessage(), "开发管理", e);
		}
		return rsp;
	}

	/**
	 * 加载程序的配置参数
	 * @param program
	 */
	public void loadProgramProfiles(JSONObject e)
	{
		e.put("mode", "[0]主控服务启动后自动启动进程，进程关闭根据重启间隔时延自动重启，主控服务停止关闭进程");
		if( e.has("control") )
		{
			JSONObject control = e.getJSONObject("control");
			if( control.has("mode") )
			{
				int mode = control.getInt("mode");
				switch(mode)
				{
				case 0:
					e.put("title","[0]主控服务启动后自动启动进程，进程关闭根据重启间隔时延自动重启，主控服务停止关闭进程");
					break;
				case 1:
					e.put("title","[1]主控服务启动后不启动进程，用户通过界面启动服务，进程关闭根据重启间隔时延自动重启，主控服务停止关闭进程");
					break;
				case 2:
					e.put("title","[2]主控服务启动后不启动进程，用户通过前台启动服务，进程关闭后不根据重启间隔时延自动重启，再次启动需要用户通过前台操作，主控服务停止关闭进程。可用于单步调试程序");
					break;
				case 3:
					e.put("title","[3]主控服务启动后先检查PID对应进程是否存在，如果进程不存在就启动该进程，主控不会重复启动该进程，进程关闭根据重启间隔时延自动重启，主控服务停止不会关闭该进程");
					break;
				case 4:
					e.put("title","[4]主控服务启动后不启动进程，用户通过前台启动服务，服务启动后先检查PID对应进程是否存在，如果进程不存在就启动该进程，主控不会重复启动该进程，进程关闭根据重启间隔时延自动重启，主控服务停止不会关闭该进程");
					break;
				}
			}
		}
//		if( program.has("ip") ) program.remove("ip");
//		JSONArray children = new JSONArray();
//		program.put("children", children);
//		JSONObject profile = new JSONObject();
//    	profile.put("name", "程序唯一标识："+program.getString("id"));
//    	profile.put("title", "唯一标志一个服务引擎，在服务集成中必须保证唯一");
//    	profile.put("icon", "images/icons/glass.png");
//    	children.put(profile);
//    	
//		if(program.has("version"))
//		{
//			String description = program.has("description")?program.getString("description"):"";
//			profile = new JSONObject();
//			profile.put("name","版本号："+program.getString("version"));
//			profile.put("title",description!=null&&!description.isEmpty()?description:"服务程序的版本号，如果设置的版本号与原有版本号不同，会触发服务自动重启");
//			profile.put("icon","images/icons/label_call_back.png");
//			children.put(profile);
//		}
//		
//		if(program.has("programmer"))
//		{
//			profile = new JSONObject();
//			profile.put("name","开发管理员："+program.getString("programmer"));
//			profile.put("title","该服务程序线上运行的维护负责人");
//			profile.put("icon","images/icons/boy.png");
//			children.put(profile);
//		}
//		
//		if(program.has("email"))
//		{
//			profile = new JSONObject();
//			profile.put("name","开发管理员邮箱："+program.getString("email"));
//			profile.put("title","该服务程序线上运行的维护负责人的联系方式");
//			profile.put("icon","images/icons/mail_attention.png");
//			children.put(profile);
//		}
//
//		if( program.has("maintenance"))
//		{
//			JSONObject maintenance = program.getJSONObject("maintenance");
//			if( maintenance.has("remark") )
//			{
//				String remark = maintenance.getString("remark");
//				profile = new JSONObject();
//		    	profile.put("name", "开发管理备注："+remark);
//		    	profile.put("title", "描述如何保障该程序运行");
//		    	children.put(profile);
//		    	profile.put("icon", "images/icons/properties.png");
//			}
//		}
//
//		if( program.has("control") )
//		{
//			JSONObject control = program.getJSONObject("control");
//			if(control.has("restartup"))
//			{
//				profile = new JSONObject();
//				profile.put("name","程序重启期间隔时间："+control.getInt("restartup")+"秒");
//				profile.put("title","如果服务中断间隔多少时间被主控重启。缺省不等候马上重启");
//				profile.put("icon","images/icons/clock.png");
//				children.put(profile);
//			}
//
//			if(control.has("logfile"))
//			{
//				String logfile = control.getString("logfile");
//				String id = program.getString("id");
//				profile = new JSONObject();
//				profile.put("name","日志文件路径:"+(logfile.isEmpty()?("../log/"+id+"/*.txt"):logfile));
//				profile.put("title","日志的输出路径，可以指定日志输出目录，或者指定目录下模糊匹配，如指定/hadoop/logs/hdfs*主控将从该路径下读取满足模糊匹配条件的文件");
//				profile.put("icon","images/icons/documents.png");
//				if(logfile.isEmpty())
//				{
//					profile.put("path","log/"+id);
//				}
//				else
//				{
//					StringBuffer path = new StringBuffer(logfile);
//					if(logfile.startsWith("../"))
//					{
//						path.delete(0,3);
//					}
//					if(path.indexOf("*")!=-1)
//					{
//						int i =path.lastIndexOf("/");
//						if(i>2)
//						{
//							path.delete(i,path.length());
//						}
//					}
//					else
//					{
//						if(path.charAt(path.length()-1)=='/')
//						{
//							path.deleteCharAt(path.length()-1);
//						}
//					}
//					profile.put("path",path.toString());
//				}			
//			}
//			
//			if(control.has("mode"))
//			{
//				profile = new JSONObject();
//				children.put(profile);
//				int mode = control.getInt("mode");
//				switch(mode)
//				{
//				case 0:
//					profile.put("name","主控启动");
//					profile.put("title","主控服务启动后自动启动进程，进程关闭根据重启间隔时延自动重启，主控服务停止关闭进程");
//					break;
//				case 1:
//					profile.put("name","前台启动");
//					profile.put("title","主控服务启动后不启动进程，用户通过界面启动服务，进程关闭根据重启间隔时延自动重启，主控服务停止关闭进程");
//					break;
//				case 2:
//					profile.put("name","单步运行程序");
//					profile.put("title","主控服务启动后不启动进程，用户通过前台启动服务，进程关闭后不根据重启间隔时延自动重启，再次启动需要用户通过前台操作，主控服务停止关闭进程。可用于单步调试程序");
//					break;
//				case 3:
//					profile.put("name","主控自动守护");
//					profile.put("title","主控服务启动后先检查PID对应进程是否存在，如果进程不存在就启动该进程，主控不会重复启动该进程，进程关闭根据重启间隔时延自动重启，主控服务停止不会关闭该进程");
//					break;
//				case 4:
//					profile.put("name","前台启动主控自动守护");
//					profile.put("title","主控服务启动后不启动进程，用户通过前台启动服务，服务启动后先检查PID对应进程是否存在，如果进程不存在就启动该进程，主控不会重复启动该进程，进程关闭根据重启间隔时延自动重启，主控服务停止不会关闭该进程");
//					break;
//				}
//				profile.put("icon","images/icons/home.png");
//			}        
//
//			if( control.has("pidfile") )
//			{
//				String pidfile = control.getString("pidfile");
//				int mode = control.getInt("mode");
//				if( mode > 2 )
//				{
//					profile = new JSONObject();
//					children.put(profile);
//					profile.put("name", "PID文件路径: "+pidfile);
//					profile.put("title", "在守护模式下，必须填写该字段。用于让主控进程读取该程序的进程ID");
//					profile.put("icon", "images/icons/documents.png");
//			    	if( !pidfile.isEmpty() )
//			    	{
//			    		StringBuffer path = new StringBuffer(pidfile);
//			    		if( pidfile.startsWith("../") )
//			    		{
//			    			path.delete(0, 3);
//			    		}
//			    		if( path.indexOf("*") != -1 )
//			    		{
//			    			int i = path.lastIndexOf("/");
//			    			if( i > 2 )
//			    			{
//			    				path.delete(i, path.length());
//			    			}
//			    		}
//			    		else
//			    		{
//			    			if(path.charAt(path.length()-1)=='/')
//			    			{
//			    				path.deleteCharAt(path.length()-1);
//			    			}
//			    		}
//			    		profile.put("path", path.toString());
//			    	}
//				}
//			}
//
//			if( control.has("dependence"))
//			{
//				String dependence = control.getString("dependence");
//				if( dependence != null && !dependence.isEmpty() )
//				{
//					profile = new JSONObject();
//					children.put(profile);
//					profile.put("name", "依赖程序: "+dependence);
//					profile.put("title", "本服务依赖于其它什么服务，程序将在依赖服务启动后启动");
//				}
//			}
//
//			if(control.has("delayed"))
//			{
//				profile = new JSONObject();
//		    	profile.put("name", "延迟启动时间："+control.getInt("restartup")+"秒 ");
//		    	profile.put("title", "程序将在主控启动后指定时间后启动");
//		    	profile.put("icon", "images/icons/programer.png");
//		    	children.put(profile);
//			}
//			
//			if( control.has("starttime") && control.has("endtime") )
//			{
//				String start = control.getString("starttime");
//				String end = control.getString("endtime");
//				profile = new JSONObject();
//		    	profile.put("name", "一天中允许运行时间："+start+" ~ "+end);
//		    	profile.put("title", "该参数可用于某些服务进程每天只在指定时间内运行的程序，例如08:00~09:00。同时配置在时间到期是否强制关闭");
//		    	children.put(profile);
//		    	profile.put("icon", "images/icons/history.png");
//			}
//
//			if( control.has("forcereboot") )
//			{
//				JSONObject forcereboot = control.getJSONObject("forcereboot");
//  			    profile = new JSONObject();
//				String value = "";
//				String frmode = forcereboot.getString("mode");
//				String frval = forcereboot.getString("val");
//				String frtime = forcereboot.getString("time");
//				if( "".equals(frval) )
//				{
//					value = "每"+frval+"秒";
//				}
//				else if( "h".equals(frmode) )
//				{
//					value = "每"+frval+"小时的"+frtime;
//				}
//				else if( "d".equals(frmode) )
//				{
//					value= "每"+frval+"天的"+frtime;
//				}
//				else if( "w".equals(frmode) )
//				{
//					value = "每星期"+frval+"的"+frtime;
//				}
//				else if( "m".equals(frmode))
//				{
//				 	value = "每月"+frval+"日的"+frtime;
//				}
//		    	profile.put("name", "进程强制重启时间："+value);
//		    	profile.put("title", "进程启动后等待这个时间之后强行重启，该参数用于控制某些特殊状况");
//		    	children.put(profile);
//			}
//		}
//
//		if( program.has("description"))
//		{
//			String description = program.getString("description");
//			profile = new JSONObject();
//	    	profile.put("name", "程序说明："+description);
//	    	profile.put("title", "程序的介绍说明描述");
//	    	children.put(profile);
//	    	profile.put("icon", "images/icons/properties.png");
//		}
////		if(program.has("debug"))
////		{
////			profile = new JSONObject();
////			profile.put("name","是否输出调试信息："+(program.getBoolean("switch")?"开启":"关闭"));
////			profile.put("title","输出OS底层信息，方便程序运行初期调试，输出信息在log/"+program.getString("id")+"/debug_"+program.getString("id")+".txt");
////			profile.put("icon","images/icons/label_funny.png");
////			children.put(profile);
////		}
	}
	
	/**
	 * 将配置中临时的参数去掉
	 * @param config
	 */
	public JSONObject resetConfig(JSONObject e)
	{
		JSONObject config = new JSONObject(e.toString());
		if( config.has("oper") ) config.remove("oper");
		if( config.has("startupCommands") ) config.remove("startupCommands");
		if( config.has("startupCommandsRemark") ) config.remove("startupCommandsRemark");
		if( config.has("shutdownCommands") ) config.remove("shutdownCommands");
		if( config.has("shutdownCommandsRemark") ) config.remove("shutdownCommandsRemark");
		if( config.has("opertime") ) config.remove("opertime");
		if( config.has("serverkey") ) config.remove("serverkey");
		if( config.has("ip") ) config.remove("ip");
		if( config.has("port") ) config.remove("port");
		if( config.has("operlog") ) config.remove("operlog");
		if( config.has("sid") ) config.remove("sid");
		if( config.has("operuser") ) config.remove("operuser");
		return config;
	}
	/**
	 * 
	 * @param config
	 * @param commands
	 * @param commandsRemark
	 */
	private void setCommands(String s, String r, JSONArray command, JSONArray remark)
	{
		if( !s.isEmpty() )
		{
			s = Kit.unicode2Chr(s);
			s = Tools.replaceStr(s, "\n", " ");
			String[] commands = Tools.split(s, " ");
			String[] commandsRemark = null;
			if( r != null && !r.isEmpty() )
			{
				if( !r.isEmpty() ){
					r = Kit.unicode2Chr(r);
					commandsRemark = Tools.split(r, "\n");
				}
			}
			for(int i = 0; i < commands.length; i++)
			{
				s = commands[i];
				if( commandsRemark != null && i < commandsRemark.length  )
				{
					if( !commandsRemark[i].trim().isEmpty() )
					{
						if( remark == null ) s += "\t//"+commandsRemark[i];
						else remark.put(commandsRemark[i]);
					}
				}
				command.put(s);
			}
		}
	}
}
