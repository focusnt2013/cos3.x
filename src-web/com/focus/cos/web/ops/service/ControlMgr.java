package com.focus.cos.web.ops.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.json.JSONArray;
import org.json.JSONObject;

import com.focus.control.SystemPerf;
import com.focus.cos.api.LogSeverity;
import com.focus.cos.control.Command;
import com.focus.cos.control.Module;
import com.focus.cos.control.ProgramLoader;
//import com.focus.cos.ops.vo.ProgramConfig;
import com.focus.cos.web.common.AjaxResult;
import com.focus.cos.web.common.Debug;
import com.focus.cos.web.common.DebugResponse;
import com.focus.cos.web.common.Kit;
import com.focus.cos.web.common.PathFactory;
import com.focus.cos.web.common.SSH;
import com.focus.cos.web.common.SSHResponse;
import com.focus.cos.web.common.ZKMgr;
import com.focus.cos.web.ops.vo.ModuleTrack;
import com.focus.cos.web.service.SvrMgr;
import com.focus.util.ClassUtils;
import com.focus.util.QuickSort;
import com.focus.util.Tools;
import com.focus.util.Zookeeper;

/**
 * 主控管理
 * 主控版本数据存放在每台伺服器本地的ZK集群（也可能是单点）：/cos/config/program/<服务器码>/version/<程序ID>，该配置用于管理程序的版本数据
 * 主控配置control.xml放在每台伺服器本地的ZK集群（也可能是单点）：/cos/config/program/<服务器码>/control.xml，该配置用于获取每台伺服器的配置（逐步替换control.xml的文件配置）
 * 主控编辑的临时配置放在主界面框架程序所在的ZK集群：/cos/temp/program/publish/<servercode>:<程序ID>
 * 
 * @author focus
 * @version 重构于2017年2月21日
 */
public class ControlMgr extends SvrMgr
{
	public static final Log log = LogFactory.getLog(ControlMgr.class);

	/**
	 * 得到所有配置中的程序数据
	 * @param oldid 如果是新的配置为null
	 * @param json 配置数据。
	 * @return
	 */
	public AjaxResult<String> setProgramConfig(String oldid, String json, int sid)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		Zookeeper zookeeper = null;
		try 
		{
			ZooKeeper zookeeper1 = ZKMgr.getZooKeeper();
			JSONObject config = this.getConstructProgramConfig(new JSONObject(json));
			String serverkey = config.getString("serverkey");
			String ip = config.getString("ip");
			int port = config.getInt("port");
			String id = config.getString("id");
			if( oldid != null && !id.equals(oldid) )
			{//删掉原来的配置
				String zkpath = "/cos/temp/program/publish/"+serverkey+":"+oldid;
				Stat stat = zookeeper1.exists(zkpath, false);
				if( stat != null ){
					JSONObject config0 = new JSONObject(new String(zookeeper1.getData(zkpath, false, stat), "UTF-8"));
					if( config0.getInt("oper") == ProgramLoader.OPER_ADDING )
						zookeeper1.delete(zkpath, stat.getVersion());
					else
					{
						config0.put("oper", ProgramLoader.OPER_DELETING);
						zookeeper1.setData(zkpath, config0.toString().getBytes("UTF-8"), stat.getVersion());
					}
				}
			}
			zookeeper = Zookeeper.getInstance(ip, port);
			config.put("operlog", "用户通过界面操作配置");
//			System.err.println(config.toString(4));
			String rspmsg = this.saveProgramConfig(zookeeper.i(), ip, serverkey, config, super.getAccountName(), sid);
			rsp.setMessage(rspmsg);
        	this.loadProgramProfiles(config);
        	config.put("id", config.getString("id")+"*");
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
			String contexturl = "control!preview.action?ip="+ip+"&port="+port+"&serverkey="+serverkey+"&id="+id;
			logoper(rsp.getMessage(), "程序管理", null, contexturl);
			sendNotiefieToAccount(
					super.getAccountName(),
					"集群程序管理",
					"您"+rsp.getMessage(),
                    "用户修改了程序配置，该配置参数生效需要系统管理员进行审核发布。",
                    contexturl,
                    "程序管理", "control!open.action?id="+sid);
			sendNotiefiesToSystemadmin(
					"集群程序管理",
					String.format("用户[%s]"+rsp.getMessage(), getAccountName()),
                    "用户修改了程序配置，该配置参数生效需要系统管理员进行审核发布。",
					"control!publish.action?command="+config.getInt("oper"),
                    "程序管理", "control!open.action?id="+sid);
		}
		catch (Exception e)
		{
			rsp.setMessage("配置集群伺服器程序"+json+"出现异常:"+e.getMessage());
			logoper(rsp.getMessage(), "程序管理", e);
		}
		finally
		{
			if( zookeeper != null ) zookeeper.close();
		}
		return rsp;
	}
	
	/**
	 * 保存程序配置
	 * @param ip
	 * @param port
	 * @param serverkey
	 * @param config
	 * @param operuser
	 * @return
	 */
	public String saveProgramConfig(ZooKeeper zookeeper, String ip, String serverkey, JSONObject config, String operuser, int sid )
		throws Exception
	{
		String rspmsg = null;
		try 
		{
			config.put("sid", sid);
			config.put("ip", ip);
			config.put("serverkey", serverkey);
			config.put("opertime", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()));
			config.put("operuser", operuser);
			String id = config.getString("id");

			String zkpath = "/cos/temp/program/publish/"+serverkey+":"+id;
			Stat stat = ZKMgr.getZookeeper().exists(zkpath, false);
			rspmsg = "新增集群伺服器【"+ip+"】程序["+id+"]配置";
			if( stat != null )
			{//ProgramLoader.OPER_DELETING  ProgramLoader.OPER_ADDING  ProgramLoader.OPER_EDITING
				JSONObject config0 = new JSONObject(new String(ZKMgr.getZookeeper().getData(zkpath, false, stat), "UTF-8"));
				if( config0.getInt("oper") == ProgramLoader.OPER_REJECT )
				{
					config0.put("oper", config0.has("lastoper")?config0.getInt("lastoper"):ProgramLoader.OPER_ADDING);
					rspmsg = "修改被发布审核拒绝的集群伺服器【"+ip+"】程序["+id+"]配置重新提交";
				}
				else if( config0.getInt("oper") != ProgramLoader.OPER_ADDING )
					rspmsg = "修改集群伺服器【"+ip+"】程序["+id+"]配置";
				
				config.put("oper", config0.getInt("oper"));
				ZKMgr.getZookeeper().setData(zkpath, config.toString().getBytes("UTF-8"), stat.getVersion());
			}
			else
			{
				if( !config.has("oper") ) config.put("oper", ProgramLoader.OPER_ADDING);
				ZKMgr.getZookeeper().createObject(zkpath, config);
			}
			zkpath = "/cos/config/program/"+serverkey+"/version/"+id;
			stat = zookeeper.exists(zkpath, false);
			if( stat != null )
			{
				JSONObject version = new JSONObject(new String(zookeeper.getData(zkpath, false, stat), "UTF-8"));
				version.put("version", config.getString("version"));
				version.put("name", config.getString("name"));
				version.put("remark", config.getString("description"));
				zookeeper.setData(zkpath, version.toString().getBytes("UTF-8"), stat.getVersion());
			}
			else
			{
				JSONObject version = new JSONObject();
				version.put("version", config.getString("version"));
				version.put("name", config.getString("name"));
				version.put("remark", config.getString("description"));
				Zookeeper.createObject(zookeeper, zkpath, version);
			}
			return rspmsg;
		}
		catch (Exception e)
		{
			throw e;
		}
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
//    					"集群程序管理",
//    					"集群伺服器【"+ip+"】运行的程序使用内存相比实际物理使用内存低于30%",
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
	 * 设置系统程序
	 * @param sysPerf
	 * @param sysPrograms
	 */
	public HashMap<String, ModuleTrack> setSysPrograms(SystemPerf sysPerf, JSONArray sysPrograms)
	{
		if( sysPerf == null ) return null;
        HashMap<String, ModuleTrack> mapModuleTrack = new HashMap<String, ModuleTrack>();
        ArrayList<ModuleTrack> modules = new ArrayList<ModuleTrack>();
        sysPerf.loadModuleTrack(modules);
        for( ModuleTrack module : modules )
        {
        	mapModuleTrack.put(module.getId(), module);
    		if( !"超级管理员".equals(module.getProgrammer()) )
    		{
    			continue;
    		}
        	JSONObject e = new JSONObject();
    		e.put("id", module.getId());
    		e.put("name", module.getName());
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
    		e.put("icon", "images/icons/tile.png");
	        e.put("state", Module.STATE_INIT);
	        e.put("startupTime", "N/A");
        	e.put("startupTime", module.getStatupTime()!=null?Tools.getFormatTime("yyyy-MM-dd HH:mm", module.getStatupTime().getTime()):"N/A");
        	e.put("state", module.getState());
        	e.put("stateInfo", module.getMonitorInfo());

        	JSONArray children = new JSONArray();
        	e.put("children", children);
            JSONObject profile = new JSONObject();
        	profile.put("name", "程序唯一标识："+module.getId());
        	profile.put("title", "唯一标志一个服务引擎，在服务集成中必须保证唯一");
        	profile.put("icon", "images/icons/clock.png");
        	children.put(profile);

    		profile = new JSONObject();
        	children.put(profile);
        	profile.put("path", "log/"+module.getId());
        	profile.put("name", "日志文件路径: "+("../log/"+module.getId()+"/*.txt"));
        	profile.put("title", "日志的输出路径，可以指定日志输出目录，或者指定目录下模糊匹配，如指定/hadoop/logs/hdfs*主控将从该路径下读取满足模糊匹配条件的文件");
        	profile.put("icon", "images/icons/documents.png");
        	
        	profile = new JSONObject();
        	profile.put("name", "是否输出调试信息："+(module.isDebug()?"是":"否"));
        	profile.put("title", "输出OS底层信息，方便程序运行初期调试，输出信息在debug_"+sysPerf.getId()+".txt");
        	profile.put("icon", "images/icons/label_funny.png");
        	children.put(profile);

        	
	        profile = new JSONObject();
	    	profile.put("name", "摘要："+module.getRemark());
	    	profile.put("title", "版本特性说明");
	    	profile.put("icon", "images/icons/properties.png");
	    	children.put(profile);
	    	e.put("title", module.getRemark());

	    	e.put("runtime", module.getRuntime());
	    	e.put("usageMemory", module.getUsageMemory());
	    	
	    	e.put("version", module.getVersion());
			profile = new JSONObject();
	    	profile.put("name", "版本号："+module.getVersion());
	    	profile.put("title", module.getRemark()!=null&&!module.getRemark().isEmpty()?module.getRemark():"服务程序的版本号，如果设置的版本号与原有版本号不同，会触发服务自动重启");
	    	profile.put("icon", "images/icons/label_call_back.png");
	    	children.put(profile);
	    	
			e.put("programmer", module.getProgrammer());
			profile = new JSONObject();
	    	profile.put("name", "程序管理员："+module.getProgrammer());
	    	profile.put("title", "该服务程序线上运行的维护负责人");
	    	profile.put("icon", "images/icons/boy.png");
	    	children.put(profile);
	    	
	    	sysPrograms.put(e);
        }
        return mapModuleTrack;
	}
	
	/**
	 * 加载主控配置的JSON数组
	 * @param ip
	 * @param port
	 * @param serverkey
	 * @param zookeeper
	 * @return
	 * @throws Exception
	 */
	public void setUserPrograms(String serverkey, ZooKeeper zookeeper, JSONArray userPrograms) 
		throws Exception
	{
		String path = "/cos/config/program/"+serverkey+"/publish";
		Stat stat = zookeeper.exists(path, false);
		if( stat == null )
		{
			return;
		}
		List<String> list = zookeeper.getChildren(path, false);
		for(String id : list)
		{
			String zkpath = path+"/"+id;
			stat = zookeeper.exists(zkpath, false);
			if( stat == null ) continue;
			byte[] payload = zookeeper.getData(zkpath, false, stat);
			JSONObject config = new JSONObject(new String(payload, "UTF-8"));
			config = this.getUnconstructProgramConfig(config);
        	userPrograms.put(config);
    		String pathdebug = "/cos/config/program/"+serverkey+"/debug/"+id;
			stat = zookeeper.exists(pathdebug, false);
			boolean d = stat != null && "true".equals(new String(zookeeper.getData(pathdebug, false, stat)));
			config.put("debug", d);
    		String pathswitch = "/cos/config/program/"+serverkey+"/switch/"+id;
			stat = zookeeper.exists(pathswitch, false);
			boolean s = stat != null && "true".equals(new String(zookeeper.getData(pathswitch, false, stat)));
			config.put("switch", s);
		}
	}
	
	/**
	 * 得到程序配置
	 * @param serverkey
	 * @param zookeeper
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public JSONObject getProgramConfig(String serverkey, ZooKeeper zookeeper, String id)
		throws Exception
	{
		String zkpath = "/cos/config/program/"+serverkey+"/publish/"+id;
		Stat stat = zookeeper.exists(zkpath, false);
		if( stat == null )
		{
			return null;
		}
		byte[] payload = zookeeper.getData(zkpath, false, stat);
		return new JSONObject(new String(payload, "UTF-8"));
	}
	/**
	 * 删除程序配置（需要系统管理员审核）
	 * @param ip
	 * @param port
	 * @param id
	 * @param name
	 * @param remark
	 * @param sid
	 * @return
	 */
	public AjaxResult<String> delProgramConfig(String ip, int port, String serverkey, String id, int sid)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		Zookeeper zookeeper = null;
		JSONObject config3 = null;
		int oper = ProgramLoader.OPER_DELETING;
		try 
		{
			zookeeper = Zookeeper.getInstance(ip, port);
	        config3 = this.getProgramConfig(serverkey, zookeeper.i(), id);
	        if( config3 == null )
	        {
				throw new Exception("未找到要删除的程序配置");
	        }

			String zkpath = "/cos/temp/program/publish/"+serverkey+":"+id;
			Stat stat = ZKMgr.getZookeeper().exists(zkpath);
			config3.put("sid", sid);
			config3.put("ip", ip);
			config3.put("port", port);
			config3.put("serverkey", serverkey);
			config3.put("opertime", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()));
			config3.put("operuser", super.getAccountName());
			String contexturl = "control!preview.action?ip="+ip+"&port="+port+"&serverkey="+serverkey+"&id="+id;
			String context = null;
			if( stat != null )
			{
				config3 = new JSONObject(new String(ZKMgr.getZooKeeper().getData(zkpath, false, stat), "UTF-8"));
				if( config3.getInt("oper") == ProgramLoader.OPER_ADDING )
				{
					config3.put("oper", ProgramLoader.OPER_DELETED);
					ZKMgr.getZooKeeper().delete(zkpath, stat.getVersion());
					oper = ProgramLoader.OPER_DELETED;//用于不会执行到这里来
			        rsp.setMessage("删除伺服器【"+ip+"】程序["+id+"]配置，该程序配置处于新增编辑状态，配置被直接删除。");
			        zkpath = "/cos/temp/program/removed/"+serverkey+"/"+id;
					stat = ZKMgr.getZookeeper().exists(zkpath);
					if( stat == null )
					{
						ZKMgr.getZookeeper().createObject(zkpath, config3);
					}
					else
					{
						ZKMgr.getZookeeper().setData(zkpath, config3.toString().getBytes("UTF-8"), stat.getVersion());
					}
				}
				else if( config3.getInt("oper") == ProgramLoader.OPER_EDITING )
				{
			        rsp.setMessage("删除伺服器【"+ip+"】程序["+id+"]配置，该程序配置处于修改编辑状态，需要系统管理员审批。");
				}
				else if( config3.getInt("oper") == ProgramLoader.OPER_DELETING )
				{
			        rsp.setMessage("删除伺服器【"+ip+"】程序["+id+"]配置的申请已经提交待系统管理员审批。");
				}
				config3.put("oper", oper);
				if( oper != ProgramLoader.OPER_DELETED )
				{
					ZKMgr.getZooKeeper().setData(zkpath, config3.toString().getBytes("UTF-8"), stat.getVersion());
				}
			}
			else
			{
				config3.put("oper", oper);
				Zookeeper.createNode(ZKMgr.getZookeeper().i(), zkpath, config3.toString().getBytes("UTF-8"));
		        rsp.setMessage("删除伺服器【"+ip+"】程序["+id+"]配置，需要系统管理员审核。");
			}
			this.loadProgramProfiles(config3);
			rsp.setResult(config3.toString());
			rsp.setSucceed(true);
			sendNotiefieToAccount(
					super.getAccountName(),
					"集群程序管理",
					"您"+rsp.getMessage(),
					context,
					contexturl,
                    "程序管理", "control!open.action?id="+sid);
			sendNotiefiesToSystemadmin(
					"集群程序管理",
					String.format("用户[%s]"+rsp.getMessage(), getAccountName()),
					context,
					"control!publish.action?command="+ProgramLoader.OPER_DELETING,
                    "程序管理", "control!open.action?id="+sid);
		}
		catch (Exception e)
		{
			rsp.setMessage("删除伺服器【"+ip+"】程序["+id+"]配置出现异常:"+e.getMessage());
			logoper(rsp.getMessage(), "程序管理", e);
		}
		finally
		{
			if( zookeeper != null ) zookeeper.close();
		}
		return rsp;
	}

	/**
	 * 撤销程序配置
	 * @param ip
	 * @param port
	 * @param serverkey
	 * @param id
	 * @param sid
	 * @return
	 */
	public AjaxResult<String> clearProgramConfig(String ip, int port, String serverkey, int sid)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		String zkpath = "/cos/temp/program/publish";
		try 
		{
			Stat stat = ZKMgr.getZookeeper().exists(zkpath);
			if( stat != null )
			{
				StringBuffer sb = new StringBuffer("撤销的程序配置如下表所示：");
				List<String> pubs = ZKMgr.getZooKeeper().getChildren(zkpath, false);
				for(String pub : pubs)
				{
					if( !pub.startsWith(serverkey) ) continue;
					String path = zkpath+"/"+pub;
					stat = ZKMgr.getZookeeper().exists(path);
					if( stat != null ){
						JSONObject config4 = new JSONObject(new String(ZKMgr.getZooKeeper().getData(path, false, stat), "UTF-8"));
						sb.append("\r\n\t撤销集群伺服器【"+ip+"】程序["+config4.getString("id")+"]配置.");
						ZKMgr.getZooKeeper().delete(path, stat.getVersion());
						config4.put("oper", ProgramLoader.OPER_DELETED);
						path = "/cos/temp/program/removed/"+serverkey+"/"+config4.getString("id");
						stat = ZKMgr.getZookeeper().exists(path);
						if( stat == null )
						{
							ZKMgr.getZookeeper().createObject(path, config4);
						}
						else
						{
							ZKMgr.getZooKeeper().setData(path, config4.toString().getBytes("UTF-8"), stat.getVersion());
						}
					}
				}
				rsp.setMessage("撤销集群伺服器【"+ip+"】所有待发布程序配置成功");
				logoper(rsp.getMessage(), "程序管理", sb.toString(), "");
		        rsp.setSucceed(true);
				sendNotiefieToAccount(
						super.getAccountName(),
						"集群程序管理",
						"您"+rsp.getMessage(),
						sb.toString(),
						null,
	                    "程序管理", "control!open.action?id="+sid);
				sendNotiefiesToSystemadmin(
						"集群程序管理",
						String.format("用户[%s]"+rsp.getMessage(), getAccountName()),
						sb.toString(),
						null,
	                    "程序管理", "control!open.action?id="+sid);
			}
			else
			{
		        rsp.setMessage("撤销集群伺服器【"+ip+"】所有待发布程序配置失败，因为不存在任何待发布程序配置。");
			}
		}
		catch (Exception e)
		{
			rsp.setMessage("撤销集群伺服器【"+ip+"】所有待发布程序配置出现异常:"+e.getMessage());
			logoper(rsp.getMessage(), "程序管理", e);
		}
		return rsp;
	}
	
	/**
	 * 取消程序删除
	 * @param ip
	 * @param port
	 * @param serverkey
	 * @param id
	 * @param sid
	 * @return
	 */
	public AjaxResult<String> cancelProgramConfig(String ip, int port, String serverkey, String id, int sid)
	{
		if( id != null && id.endsWith("*") )
		{
			id = id.substring(0, id.length()-1);
		}
		AjaxResult<String> rsp = new AjaxResult<String>();
		String zkpath = "/cos/temp/program/publish/"+serverkey+":"+id;
		try 
		{
			Stat stat = ZKMgr.getZookeeper().exists(zkpath);
			String context = null;
			if( stat != null )
			{
				JSONObject config4 = new JSONObject(new String(ZKMgr.getZooKeeper().getData(zkpath, false, stat), "UTF-8"));
				if( config4.getInt("oper") == ProgramLoader.OPER_ADDING )
				{
			        rsp.setMessage("撤销新增伺服器【"+ip+"】程序["+id+"]配置。");
				}
				else if( config4.getInt("oper") == ProgramLoader.OPER_EDITING )
				{
			        rsp.setMessage("撤销修改伺服器【"+ip+"】程序["+id+"]配置。");
				}
				else if( config4.getInt("oper") == ProgramLoader.OPER_DELETING )
				{
			        rsp.setMessage("撤销删除伺服器【"+ip+"】程序["+id+"]配置。");
				}
				else if( config4.getInt("oper") == ProgramLoader.OPER_REJECT )
				{
			        rsp.setMessage("撤销被管理员拒绝发布的伺服器【"+ip+"】程序["+id+"]配置。");
				}
				ZKMgr.getZooKeeper().delete(zkpath, stat.getVersion());
				config4.put("oper", ProgramLoader.OPER_DELETED);
				zkpath = "/cos/temp/program/removed/"+serverkey+"/"+config4.getString("id");
				stat = ZKMgr.getZookeeper().exists(zkpath);
				if( stat == null )
				{
					ZKMgr.getZookeeper().createObject(zkpath, config4);
				}
				else
				{
					ZKMgr.getZooKeeper().setData(zkpath, config4.toString().getBytes("UTF-8"), stat.getVersion());
				}
				
				context = getConstructProgramConfig(config4).toString(4);
				rsp.setResult(id);
		        rsp.setSucceed(true);
				sendNotiefieToAccount(
						super.getAccountName(),
						"集群程序管理",
						"您"+rsp.getMessage(),
						context,
						null,
	                    "程序管理", "control!open.action?id="+sid);
				sendNotiefiesToSystemadmin(
						"集群程序管理",
						String.format("用户[%s]"+rsp.getMessage(), getAccountName()),
						context,
						null,
	                    "程序管理", "control!open.action?id="+sid);
			}
			else
			{
		        rsp.setMessage("撤销伺服器【"+ip+"】程序["+id+"]配置失败，因为程序配置并不存在。");
			}
		}
		catch (Exception e)
		{
			rsp.setMessage("撤销集群伺服器【"+ip+"】程序["+id+"]配置出现异常:"+e.getMessage());
			logoper(rsp.getMessage(), "程序管理", e);
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
	public JSONObject getUnconstructProgramConfig(JSONObject config)
	{
		/*
		JSONObject unconstructor = new JSONObject();
		unconstructor.put("id", config.getString("id"));
		unconstructor.put("name", config.getString("name"));
		unconstructor.put("version", config.has("version")?config.getString("version"):"");
		unconstructor.put("description", config.has("description")?config.getString("description"):"");
		if( config.has("maintenance") )
		{
			JSONObject maintenance = config.getJSONObject("maintenance");
			unconstructor.put("manager", maintenance.has("manager")?maintenance.getString("manager"):"");
			unconstructor.put("email", maintenance.has("email")?maintenance.getString("email"):"");
			unconstructor.put("programmer", maintenance.has("programmer")?maintenance.getString("programmer"):"");
			unconstructor.put("remark", maintenance.has("remark")?maintenance.getString("remark"):"");
		}

		if( config.has("control") )
		{
			JSONObject control = config.getJSONObject("control");
			unconstructor.put("mode", control.has("mode")?control.getInt("mode"):0);
			unconstructor.put("logfile", control.has("pidfile")?control.getString("logfile"):"");
			unconstructor.put("restartup", control.has("restartup")?control.getInt("restartup"):0);
			unconstructor.put("delayed", control.has("delayed")?control.getInt("delayed"):0);
			unconstructor.put("starttime", control.has("starttime")?control.getString("starttime"):"");
			unconstructor.put("endtime", control.has("endtime")?control.getString("endtime"):"");
			unconstructor.put("dependence", control.has("dependence")?control.getString("dependence"):"");
			unconstructor.put("pidfile", control.has("pidfile")?control.getString("pidfile"):"");

			if( control.has("forcereboot") )
			{
				JSONObject forcereboot = control.getJSONObject("forcereboot");
				unconstructor.put("frmode", forcereboot.has("mode")?forcereboot.getString("mode"):"");
				unconstructor.put("frval", forcereboot.has("val")?forcereboot.getString("val"):"");
				unconstructor.put("frtime", forcereboot.has("time")?forcereboot.getString("time"):"");
			}
		}*/

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
	public AjaxResult<String> getVersionSelection(String ip, int port, String serverkey, String id, int sid)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		String zkpath = "/cos/config/program/"+serverkey+"/version/"+id;
		Zookeeper zookeeper = null;
		try 
		{
			zookeeper = Zookeeper.getInstance(ip, port);
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
			rsp.setMessage("从集群伺服器【"+ip+"】程序["+id+"]获得版本列表数据出现异常:"+e.getMessage());
		}
		finally
		{
			if( zookeeper != null ) zookeeper.close();
		}
		return rsp;
	}
	/**
	 * 开关程序，需要系统管理员审核
	 * @param ip
	 * @param port
	 * @param id
	 * @param enable
	 * @param sid
	 * @return
	 */
	public AjaxResult<Boolean> switchProgram(String ip, int port, String serverkey, String id, boolean enable, int sid)
	{
		AjaxResult<Boolean> rsp = new AjaxResult<Boolean>();
		Zookeeper zookeeper = null;
		try 
		{
			String zkpath = "/cos/config/program/"+serverkey+"/switch/"+id;
			zookeeper = Zookeeper.getInstance(ip, port);
			Stat stat = zookeeper.exists(zkpath, false);
			if( stat == null )
			{
				zookeeper.createNode(zkpath, enable?"true".getBytes():"false".getBytes());
			}
			else
			{
				zookeeper.setData(zkpath, enable?"true".getBytes():"false".getBytes(), stat.getVersion());
			}
			
			rsp.setMessage((enable?"启用":"禁用")+"集群伺服器【"+ip+"】程序["+id+"]运行");
			rsp.setResult(enable);
			rsp.setSucceed(true);
			logoper(rsp.getMessage(), "程序管理", "", "control!preview.action?ip="+ip+"&port="+port+"&serverkey="+serverkey+"&id="+id);
			sendNotiefieToAccount(
					super.getAccountName(),
					"集群程序管理",
					"您"+rsp.getMessage(),
                    "程序开关是控制程序是否在伺服器上运行的控制器。",
                    "control!preview.action?ip="+ip+"&port="+port+"&serverkey="+serverkey+"&id="+id,
                    "程序管理", "control!open.action?id="+sid);
			sendNotiefiesToSystemadmin(
					"集群程序管理",
					String.format("用户[%s]"+rsp.getMessage(), getAccountName()),
                    "程序开关是控制程序是否在伺服器上运行的控制器。",
                    "control!preview.action?ip="+ip+"&port="+port+"&serverkey="+serverkey+"&id="+id,
                    "程序管理", "control!open.action?id="+sid);
		}
		catch (Exception e)
		{
			rsp.setMessage((enable?"启用":"禁用")+"集群伺服器【"+ip+"】程序["+id+"]出现异常:"+e.getMessage());
			logoper(rsp.getMessage(), "程序管理", e);
		}
		finally
		{
			if( zookeeper != null ) zookeeper.close();
		}
		return rsp;
	}
	/**
	 * 开关程序，需要系统管理员审核
	 * @param ip
	 * @param port
	 * @param id
	 * @param enable
	 * @param sid
	 * @return
	 */
	public AjaxResult<Boolean> switchDebug(String ip, int port, String serverkey, String id, boolean active, int sid)
	{
		AjaxResult<Boolean> rsp = new AjaxResult<Boolean>();
		Zookeeper zookeeper = null;
		try 
		{
			String zkpath = "/cos/config/program/"+serverkey+"/debug/"+id;
			zookeeper = Zookeeper.getInstance(ip, port);
			Stat stat = zookeeper.exists(zkpath, false);
			if( stat == null )
			{
				zookeeper.createNode(zkpath, active?"true".getBytes():"false".getBytes());
			}
			else
			{
				zookeeper.setData(zkpath, active?"true".getBytes():"false".getBytes(), stat);
			}
			rsp.setMessage((active?"启用":"禁用")+"集群伺服器【"+ip+"】程序["+id+"]调试日志输出成功");
			rsp.setResult(active);
			rsp.setSucceed(true);
			logoper(rsp.getMessage(), "程序管理", "", "control!preview.action?ip="+ip+"&port="+port+"&serverkey="+serverkey+"&id="+id);
			
			sendNotiefieToAccount(
					super.getAccountName(),
					"集群程序管理",
					"您"+rsp.getMessage(),
                    "程序日志调试输出是配置程序初期检查问题的重要方法。",
                    "control!preview.action?ip="+ip+"&port="+port+"&serverkey="+serverkey+"&id="+id,
                    "程序管理", "control!open.action?id="+sid);
			sendNotiefiesToSystemadmin(
					"集群程序管理",
					String.format("用户[%s]"+rsp.getMessage(), getAccountName()),
                    "程序日志调试输出是配置程序初期检查问题的重要方法。",
                    "control!preview.action?ip="+ip+"&port="+port+"&serverkey="+serverkey+"&id="+id,
                    "程序管理", "control!open.action?id="+sid);
		}
		catch (Exception e)
		{
			rsp.setMessage((active?"启用":"禁用")+"集群伺服器【"+ip+"】程序["+id+"]调试日志输出出现异常:"+e.getMessage());
			logoper(rsp.getMessage(), "程序管理", e);
		}
		finally
		{
			if( zookeeper != null ) zookeeper.close();
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
	public AjaxResult<String> setProgramRemark(String ip, int port, String serverkey, String id, String name, String version, String description, int sid)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		Zookeeper zookeeper = null;
		try 
		{
			String zkpath = "/cos/config/program/"+serverkey+"/publish/"+id;
			zookeeper = Zookeeper.getInstance(ip, port);
			Stat stat = zookeeper.exists(zkpath, false);
			if( stat == null )
			{
				rsp.setMessage("未发现您要配置的伺服器【"+ip+"】程序["+id+"]");
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
			rsp.setMessage("设置集群伺服器【"+ip+"】程序["+id+"]的名称和版本"+
					oldname+"-&gt;"+name+","+oldversion+"-&gt;"+version+","+release);
			log.info("Set the remark of program "+id+" from "+ip);
			rsp.setSucceed(true);
			logoper(rsp.getMessage(), "程序管理", "", "control!preview.action?ip="+ip+"&port="+port+"&serverkey="+serverkey+"&id="+id);
			sendNotiefieToAccount(
					super.getAccountName(),
					"集群程序管理",
					"您"+rsp.getMessage(),
                    "您修改了程序名称或描述，简洁准确的名称和详细的描述将提升程序管理的效率。",
                    "control!preview.action?ip="+ip+"&port="+port+"&serverkey="+serverkey+"&id="+id,
                    "程序管理", "control!open.action?id="+sid);
			sendNotiefiesToSystemadmin(
					"集群程序管理",
					String.format("用户[%s]"+rsp.getMessage(), getAccountName()),
                    "用户修改了程序名称或描述，简洁准确的名称和详细的描述将提升程序管理的效率。",
                    "control!preview.action?ip="+ip+"&port="+port+"&serverkey="+serverkey+"&id="+id,
                    "程序管理", "control!open.action?id="+sid);
		}
		catch (Exception e)
		{
			rsp.setMessage("修改集群伺服器【"+ip+"】程序["+id+"]的名称和描述出现异常:"+e.getMessage());
			logoper(rsp.getMessage(), "程序管理", e);
		}
		finally
		{
			if( zookeeper != null ) zookeeper.close();
		}
		return rsp;
	}
	/**
	 * 设置程序管理员
	 * @param ip
	 * @param port
	 * @param id
	 * @param name
	 * @param account
	 * @param email
	 * @param sid
	 * @return
	 */
	public AjaxResult<String> setProgrammer(String ip, int port, String serverkey, String id, String programmer, String manager, String email, int sid)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		Zookeeper zookeeper = null;
		try 
		{
			String zkpath = "/cos/config/program/"+serverkey+"/publish/"+id;
			zookeeper = Zookeeper.getInstance(ip, port);
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
			rsp.setMessage("设置集群伺服器【"+ip+"】程序["+id+"]的管理员"+oldprogrammer+"-&gt;"+programmer);
			
			log.info("Set the remark of program "+id+" from "+ip);
			rsp.setSucceed(true);
			logoper(rsp.getMessage(), "程序管理", "", "control!preview.action?ip="+ip+"&port="+port+"&serverkey="+serverkey+"&id="+id);
			sendNotiefieToAccount(
					manager,
					"集群程序管理",
					String.format("用户[%s]将您设置为集群伺服器【"+ip+"】程序["+id+"]的管理员", getAccountName()),
                    "程序管理员负责维护程序的正常运行，可以通过集群程序管理查看与配置程序。",
                    "control!preview.action?ip="+ip+"&port="+port+"&serverkey="+serverkey+"&id="+id,
                    "程序管理", "control!open.action?id="+sid);
			sendNotiefieToAccount(
					super.getAccountName(),
					"集群程序管理",
					"您"+rsp.getMessage(),
                    "程序管理员负责维护程序的正常运行，可以通过集群程序管理查看与配置程序。",
                    "control!preview.action?ip="+ip+"&port="+port+"&serverkey="+serverkey+"&id="+id,
                    "程序管理", "control!open.action?id="+sid);
			sendNotiefiesToSystemadmin(
					"集群程序管理",
					String.format("用户[%s]"+rsp.getMessage(), getAccountName()),
                    "程序管理员负责维护程序的正常运行，可以通过集群程序管理查看与配置程序。",
                    "control!preview.action?ip="+ip+"&port="+port+"&serverkey="+serverkey+"&id="+id,
                    "程序管理", "control!open.action?id="+sid);
		}
		catch (Exception e)
		{
			rsp.setMessage("设置集群伺服器【"+ip+"】程序["+id+"]的管理员出现异常:"+e.getMessage());
			logoper(rsp.getMessage(), "程序管理", e);
		}
		finally
		{
			if( zookeeper != null ) zookeeper.close();
		}
		return rsp;
	}

	/**
	 * 修改集群程序管理的名称和描述（允许直接修改，记录日志和发送系统消息即可）
	 * @param ip
	 * @param port
	 * @param title
	 * @param sid
	 * @return
	 */
	public AjaxResult<String> setControlTitle(String ip, int port, String serverkey, String title, int sid)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		Zookeeper zookeeper = null;
		try 
		{
			String zkpath = "/cos/config/program/"+serverkey;
			zookeeper = Zookeeper.getInstance(ip, port);
			Stat stat = zookeeper.exists(zkpath, false);
			if( stat == null )
			{
				rsp.setMessage("伺服器【"+ip+"】主控配置不存在");
				return rsp;
			}
			String old = new String(zookeeper.getData(zkpath, false, stat), "UTF-8");
			zookeeper.setData(zkpath, title.getBytes("UTF-8"), stat.getVersion());
			log.info("Set the title of control from "+ip);
			rsp.setSucceed(true);
			rsp.setMessage("修改集群伺服器【"+ip+"】主控配置描述为【"+title+"】");
			logoper(rsp.getMessage(), "程序管理", "修改了集群伺服器主控描述，原描述为"+old+"，简洁准确的描述将提升集群程序管理的效率。", "");
			sendNotiefieToAccount(
					super.getAccountName(),
					"集群程序管理",
					"您"+rsp.getMessage(),
                    "您修改了集群伺服器主控描述，原描述为"+old+"，简洁准确的描述将提升集群程序管理的效率。",
                    null,
                    "程序管理", "control!open.action?id="+sid);
			sendNotiefiesToSystemadmin(
					"集群程序管理",
					String.format("用户[%s]"+rsp.getMessage(), getAccountName()),
                    "修改了集群伺服器主控描述，原描述为"+old+"，简洁准确的描述将提升集群程序管理的效率。",
                    null,
                    "程序管理", "control!open.action?id="+sid);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			rsp.setMessage("修改集群伺服器【"+ip+"】主控描述出现异常:"+e.getMessage());
			logoper(rsp.getMessage(), "程序管理", e);
		}
		finally
		{
			if( zookeeper != null ) zookeeper.close();
		}
		return rsp;
	}
	
	/**
	 * 加载程序的配置参数
	 * @param program
	 */
	public void loadProgramProfiles(JSONObject program)
	{
		if( program.has("ip") ) program.remove("ip");
		JSONArray children = new JSONArray();
		program.put("children", children);
		JSONObject profile = new JSONObject();
    	profile.put("name", "程序唯一标识："+program.getString("id"));
    	profile.put("title", "唯一标志一个服务引擎，在服务集成中必须保证唯一");
    	profile.put("icon", "images/icons/glass.png");
    	children.put(profile);
    	
		if( program.has("oper") )
		{
			profile = new JSONObject();
			String title = "";
	    	profile.put("title", "");
	    	switch(program.getInt("oper"))
	    	{
			case ProgramLoader.OPER_ADDING:
				title = "新增程序配置待系统管理员审批生效";
		    	profile.put("icon", "images/icons/new_item.png");
				break;
			case 1:
				title = "修改程序配置待系统管理员审批生效";
		    	profile.put("icon", "images/icons/drafts.png");
				break;
			case 3:
				title = "删除程序配置待系统管理员审批生效";
		    	profile.put("icon", "images/icons/trash.png");
				break;
	    	}
	    	profile.put("name", title);
	    	children.put(profile);
		}
		if( program.has("state") )
		{
			profile = new JSONObject();
			String title = "";
	    	profile.put("title", "");
	    	switch(program.getInt("state"))
	    	{
			case 0:
				title = "未运行";
		    	profile.put("icon", "images/icons/stop.png");
				break;
			case 1:
				title = "运行中";
		    	profile.put("icon", "images/icons/forwarded.png");
				break;
			case 2:
				title = "关闭中";
		    	profile.put("icon", "images/icons/blocked.png");
				break;
			case 3:
				title = "已关闭";
		    	profile.put("icon", "images/icons/stop.png");
				break;
			case 5:
				title = "暂停中";
		    	profile.put("icon", "images/icons/abort.png");
				break;
			case 6:
				title = "已暂停";
		    	profile.put("icon", "images/icons/abort.png");
				break;
			case 7:
				title = "已关闭";
		    	profile.put("icon", "images/icons/pause.png");
				break; 
			default:
				title = "未知状态";
		    	profile.put("icon", "images/icons/unknown.png");
				break; 
	    	}
	    	profile.put("name", "程序运行状态: "+title);
	    	children.put(profile);
		}
		
		if(program.has("switch"))
		{
			profile = new JSONObject();
			profile.put("name","程序开关："+(program.getBoolean("switch")?"启动":"禁用"));
			profile.put("title","当前服务是启动状态还是停止状态");
			profile.put("icon",program.getBoolean("switch")?"images/icons/selected.png":"images/icons/unselected.png");
			children.put(profile);
		}
		
		if(program.has("version"))
		{
			String description = program.has("description")?program.getString("description"):"";
			profile = new JSONObject();
			profile.put("name","版本号："+program.getString("version"));
			profile.put("title",description!=null&&!description.isEmpty()?description:"服务程序的版本号，如果设置的版本号与原有版本号不同，会触发服务自动重启");
			profile.put("icon","images/icons/label_call_back.png");
			children.put(profile);
		}
		
		if(program.has("programmer"))
		{
			profile = new JSONObject();
			profile.put("name","程序管理员："+program.getString("programmer"));
			profile.put("title","该服务程序线上运行的维护负责人");
			profile.put("icon","images/icons/boy.png");
			children.put(profile);
		}
		
		if(program.has("email"))
		{
			profile = new JSONObject();
			profile.put("name","程序管理员邮箱："+program.getString("email"));
			profile.put("title","该服务程序线上运行的维护负责人的联系方式");
			profile.put("icon","images/icons/mail_attention.png");
			children.put(profile);
		}

		if( program.has("maintenance"))
		{
			JSONObject maintenance = program.getJSONObject("maintenance");
			if( maintenance.has("remark") )
			{
				String remark = maintenance.getString("remark");
				profile = new JSONObject();
		    	profile.put("name", "程序管理备注："+remark);
		    	profile.put("title", "描述如何保障该程序运行");
		    	children.put(profile);
		    	profile.put("icon", "images/icons/properties.png");
			}
		}

		if( program.has("control") )
		{
			JSONObject control = program.getJSONObject("control");
			if(control.has("mode"))
			{
				profile = new JSONObject();
				children.put(profile);
				int mode = control.getInt("mode");
				switch(mode)
				{
				case 0:
					profile.put("name","主控启动");
					profile.put("title","主控服务启动后自动启动进程，进程关闭根据重启间隔时延自动重启，主控服务停止关闭进程");
					break;
				case 1:
					profile.put("name","前台启动");
					profile.put("title","主控服务启动后不启动进程，用户通过界面启动服务，进程关闭根据重启间隔时延自动重启，主控服务停止关闭进程");
					break;
				case 2:
					profile.put("name","单步运行程序");
					profile.put("title","主控服务启动后不启动进程，用户通过前台启动服务，进程关闭后不根据重启间隔时延自动重启，再次启动需要用户通过前台操作，主控服务停止关闭进程。可用于单步调试程序");
					break;
				case 3:
					profile.put("name","主控自动守护");
					profile.put("title","主控服务启动后先检查PID对应进程是否存在，如果进程不存在就启动该进程，主控不会重复启动该进程，进程关闭根据重启间隔时延自动重启，主控服务停止不会关闭该进程");
					break;
				case 4:
					profile.put("name","前台启动主控自动守护");
					profile.put("title","主控服务启动后不启动进程，用户通过前台启动服务，服务启动后先检查PID对应进程是否存在，如果进程不存在就启动该进程，主控不会重复启动该进程，进程关闭根据重启间隔时延自动重启，主控服务停止不会关闭该进程");
					break;
				}
				profile.put("icon","images/icons/home.png");
			}        

			if(control.has("cfgfile"))
			{
				profile = new JSONObject();
		    	profile.put("name", "配置文件路径："+control.getString("cfgfile"));
		    	profile.put("title", "程序的配置文件路径以及文件类型");
		    	profile.put("icon", "images/icons/documents.png");
		    	children.put(profile);
			}
			
			if( control.has("pidfile") )
			{
				String pidfile = control.getString("pidfile");
//				int mode = control.getInt("mode");
//				if( mode > 2 )
//				{
					profile = new JSONObject();
					children.put(profile);
					profile.put("name", "PID文件路径: "+pidfile);
					profile.put("title", "在守护模式下，必须填写该字段。用于让主控进程读取该程序的进程ID");
					profile.put("icon", "images/icons/documents.png");
			    	if( !pidfile.isEmpty() )
			    	{
			    		StringBuffer path = new StringBuffer(pidfile);
			    		if( pidfile.startsWith("../") )
			    		{
			    			path.delete(0, 3);
			    		}
			    		if( path.indexOf("*") != -1 )
			    		{
			    			int i = path.lastIndexOf("/");
			    			if( i > 2 )
			    			{
			    				path.delete(i, path.length());
			    			}
			    		}
			    		else
			    		{
			    			if(path.charAt(path.length()-1)=='/')
			    			{
			    				path.deleteCharAt(path.length()-1);
			    			}
			    		}
			    		profile.put("path", path.toString());
			    	}
//				}
			}
			if(control.has("logfile"))
			{
				String logfile = control.getString("logfile");
				String id = program.getString("id");
				profile = new JSONObject();
				profile.put("name","日志文件路径:"+(logfile.isEmpty()?("../log/"+id+"/*.txt"):logfile));
				profile.put("title","日志的输出路径，可以指定日志输出目录，或者指定目录下模糊匹配，如指定/hadoop/logs/hdfs*主控将从该路径下读取满足模糊匹配条件的文件");
				profile.put("icon","images/icons/documents.png");
		    	children.put(profile);
				if(logfile.isEmpty())
				{
					profile.put("path","log/"+id);
				}
				else
				{
					StringBuffer path = new StringBuffer(logfile);
					if(logfile.startsWith("../"))
					{
						path.delete(0,3);
					}
					if(path.indexOf("*")!=-1)
					{
						int i =path.lastIndexOf("/");
						if(i>2)
						{
							path.delete(i,path.length());
						}
					}
					else
					{
						if(path.charAt(path.length()-1)=='/')
						{
							path.deleteCharAt(path.length()-1);
						}
					}
					profile.put("path",path.toString());
				}			
			}
			
			if( control.has("dependence"))
			{
				String dependence = control.getString("dependence");
				if( dependence != null && !dependence.isEmpty() )
				{
					profile = new JSONObject();
					children.put(profile);
					profile.put("name", "依赖程序: "+dependence);
					profile.put("title", "本服务依赖于其它什么服务，程序将在依赖服务启动后启动");
				}
			}

			if(control.has("restartup"))
			{
				profile = new JSONObject();
				profile.put("name","程序重启期间隔时间："+control.getInt("restartup")+"秒");
				profile.put("title","如果服务中断间隔多少时间被主控重启。缺省不等候马上重启");
				profile.put("icon","images/icons/clock.png");
				children.put(profile);
			}

			if(control.has("delayed"))
			{
				profile = new JSONObject();
		    	profile.put("name", "延迟启动时间："+control.getInt("restartup")+"秒 ");
		    	profile.put("title", "程序将在主控启动后指定时间后启动");
		    	profile.put("icon", "images/icons/clock.png");
		    	children.put(profile);
			}

			if( control.has("starttime") && control.has("endtime") )
			{
				String start = control.getString("starttime");
				String end = control.getString("endtime");
				profile = new JSONObject();
		    	profile.put("name", "一天中允许运行时间："+start+" ~ "+end);
		    	profile.put("title", "该参数可用于某些服务进程每天只在指定时间内运行的程序，例如08:00~09:00。同时配置在时间到期是否强制关闭");
		    	children.put(profile);
		    	profile.put("icon", "images/icons/history.png");
			}

			if( control.has("forcereboot") )
			{
				JSONObject forcereboot = control.getJSONObject("forcereboot");
  			    profile = new JSONObject();
				String value = "";
				String frmode = forcereboot.getString("mode");
				String frval = forcereboot.getString("val");
				String frtime = forcereboot.getString("time");
				if( "".equals(frval) )
				{
					value = "每"+frval+"秒";
				}
				else if( "h".equals(frmode) )
				{
					value = "每"+frval+"小时的"+frtime;
				}
				else if( "d".equals(frmode) )
				{
					value= "每"+frval+"天的"+frtime;
				}
				else if( "w".equals(frmode) )
				{
					value = "每星期"+frval+"的"+frtime;
				}
				else if( "m".equals(frmode))
				{
				 	value = "每月"+frval+"日的"+frtime;
				}
		    	profile.put("name", "进程强制重启时间："+value);
		    	profile.put("title", "进程启动后等待这个时间之后强行重启，该参数用于控制某些特殊状况");
		    	children.put(profile);
			}
		}

		if( program.has("description"))
		{
			String description = program.getString("description");
			profile = new JSONObject();
	    	profile.put("name", "程序说明："+description);
	    	profile.put("title", "程序的介绍说明描述");
	    	children.put(profile);
	    	profile.put("icon", "images/icons/properties.png");
		}
//		if(program.has("debug"))
//		{
//			profile = new JSONObject();
//			profile.put("name","是否输出调试信息："+(program.getBoolean("switch")?"开启":"关闭"));
//			profile.put("title","输出OS底层信息，方便程序运行初期调试，输出信息在log/"+program.getString("id")+"/debug_"+program.getString("id")+".txt");
//			profile.put("icon","images/icons/label_funny.png");
//			children.put(profile);
//		}
	}
	/**
	 * 保存服务引擎程序(control.xml)配置
	 */
	public void setControl(String serverkey, List<JSONObject> programs) throws Exception
	{
		Zookeeper zookeeper = null;
		try
		{
			JSONObject server = MonitorMgr.getInstance().getServerByKey(serverkey);
			if( server == null ) throw new Exception("因为伺服器ID("+serverkey+")对应的会话不存在主控设置失败");
			if( server.getInt("state") == 0 ) throw new Exception("因为伺服器ID("+serverkey+")对应的会话("+server.getString("ip")+")未建立连接主控设置失败");
			String ip = server.getString("ip");
			int port = server.getInt("port"); 
			HashMap<String, JSONObject> filter = new HashMap<String, JSONObject>();
			zookeeper = Zookeeper.getInstance(ip, port);
			for(JSONObject e : programs)
			{
				filter.put(e.getString("id"), e);
    			if( e.has("timeline") )
    			{
    				String pathversion = "/cos/config/program/"+serverkey+"/version/"+e.getString("id");
    				JSONObject version = new JSONObject();
    				version.put("remark", e.getString("description"));
    				version.put("name", e.getString("name"));
    				version.put("date", e.getJSONArray("timeline"));
    				if( zookeeper.exists("/cos/config/program/"+serverkey+"/version") != null )
    				{
    					zookeeper.setJSONObject(pathversion, version);
    				}
    				else
    				{
    					zookeeper.createObject(pathversion, version);
    				}
    			}
			}
			
			String path = "/cos/config/program/"+serverkey+"/publish";
			Stat stat = zookeeper.exists(path, false);
			if( stat == null )
			{
				throw new Exception("配置出错请尝试重启伺服器【"+ip+"】主控引擎。");
			}

			List<String> list = zookeeper.getChildren(path);
			JSONObject e = null;
			for(String id : list)
			{
				String zkpath = path+"/"+id;
				stat = zookeeper.exists(zkpath, false);
				if( stat == null ) continue;
	        	if( filter.containsKey(id) )
	            {
	        		e = filter.remove(id);
	        		if( e.getInt("oper") == ProgramLoader.OPER_DELETING )
	        		{
	        			zookeeper.delete(zkpath, stat.getVersion());
	        			continue;
	        		}
	        		else
	        		{
	        			JSONObject config = resetConfig(e);
	        			zookeeper.setData(zkpath, config.toString().getBytes("UTF-8"), stat);
	        		}
	            }
			}

			Iterator<JSONObject> iter = filter.values().iterator();
			while( iter.hasNext() )
			{
				e = iter.next();
    			e = resetConfig(e);
				String zkpath = path+"/"+e.getString("id");
//    			e = this.setForSave(e);
				zookeeper.create(zkpath, e.toString().getBytes("UTF-8"));
			}
			log.info("Set the config of control from "+ip);
		}
		catch(Exception e)
		{
			throw e;
		}
		finally
		{
			if( zookeeper != null ) zookeeper.close();
		}
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
		if( config.has("user") ) config.remove("user");
		if( config.has("opertype") ) config.remove("opertype");
		if( config.has("lastversion") ) config.remove("lastversion");
		if( config.has("publishremark") ) config.remove("publishremark");
		if( config.has("apptime") ) config.remove("apptime");
		if( config.has("appuser") ) config.remove("appuser");
		if( config.has("publishable") ) config.remove("publishable");
		if( config.has("timeline") ) config.remove("timeline");
		if( config.has("sn") ) config.remove("sn");
		if( config.has("publishlogs") ) config.remove("publishlogs");
		return config;
	}

	/**
	 * 设置主控程序到control.xml
	 * @param xml
	 * @param moduleNode
	 * @param e
	private JSONObject setForSave(JSONObject e)
		throws Exception
	{
		JSONObject config = new JSONObject();
		//程序发布
		config.put("id", e.getString("id"));
		config.put("name", e.getString("name"));
    	if(e.has("version")) config.put("version", e.getString("version"));
    	if(e.has("description")) config.put("description", e.getString("description"));
    	if(e.has("programmer"))	config.put("programmer", e.getString("programmer"));
    	if(e.has("manager"))config.put("manager", e.getString("manager"));
    	if(e.has("email")) config.put("email", e.getString("email"));
    	//主控参数配置
		JSONObject control = new JSONObject();
		config.put("control", control);
    	if(e.has("mode")) control.put("mode", e.getInt("mode"));
    	if(e.has("frval")) {
    		String frmode = e.getString("frmode");
    		String frval = e.getString("frval");
    		String frtime = e.getString("frtime");
    		String forcereboot = frval;
    		if( !frmode.isEmpty() ) forcereboot += ":"+frmode;
    		if( !frtime.isEmpty() ) forcereboot += ":"+frtime;
    		control.put("forcereboot", forcereboot);
    	}
    	
    	if(e.has("restartup") ) control.put("restartup", e.getInt("restartup"));
    	if(e.has("delayed") ) control.put("delayed", e.getInt("delayed"));
    	if(e.has("pidfile")) control.put("pidfile", e.getString("pidfile"));
    	if(e.has("logfile")) control.put("logfile", e.getString("logfile"));
    	control.put("start", e.getString("starttime"));
    	control.put("end", e.getString("endtime"));
    	//启动指令
    	String startupCommands = "";
    	String startupCommandsRemark = "";
    	String shutdownCommands = "";
    	String shutdownCommandsRemark = "";
    	if( e.has("startupCommands") ) startupCommands = Kit.unicode2Chr(e.getString("startupCommands"));
    	if( e.has("startupCommandsRemark") ) startupCommandsRemark = Kit.unicode2Chr(e.getString("startupCommandsRemark"));
    	if( e.has("shutdownCommands") ) shutdownCommands = Kit.unicode2Chr(e.getString("shutdownCommands"));
    	if( e.has("shutdownCommandsRemark") ) shutdownCommandsRemark = Kit.unicode2Chr(e.getString("shutdownCommandsRemark"));
		if( !startupCommands.isEmpty() )
    	{
			JSONArray command = new JSONArray();
    		JSONArray remark = new JSONArray();
			this.setCommands(startupCommands,startupCommandsRemark, command, remark);
			JSONObject startup = new JSONObject();
			config.put("startup", startup);
			startup.put("command", command);
			startup.put("remark", remark);
    	}

    	if( !shutdownCommands.isEmpty() )
    	{
    		JSONArray command = new JSONArray();
    		JSONArray remark = new JSONArray();
			this.setCommands(shutdownCommands,shutdownCommandsRemark,command, remark);
			JSONObject shutdown = new JSONObject();
			config.put("shutdown", shutdown);
			shutdown.put("command", command);
			shutdown.put("remark", remark);
    	}
    	return config;
	}
	 */
	
	/**
	 * 为编辑修改设置指令
	 * @param config
	public JSONObject setForEdit(JSONObject e)
	{
		JSONObject config = new JSONObject();
		config.put("id", e.getString("id"));
		config.put("name", e.getString("name"));
    	if(e.has("version")) config.put("version", e.getString("version"));
    	if(e.has("description")) config.put("description", e.getString("description"));
    	if(e.has("programmer"))	config.put("programmer", e.getString("programmer"));
    	if(e.has("manager"))config.put("manager", e.getString("manager"));
    	if(e.has("email")) config.put("email", e.getString("email"));
		if( e.has("control") )
		{
			JSONObject control = e.getJSONObject("control");
	    	if(e.has("mode")) config.put("mode", control.getInt("mode"));
			if( control.has("forcereboot") )
			{
				String forcereboot = control.getString("forcereboot");
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
				config.put("frmode", frmode);
				config.put("frval", frval);
				config.put("frtime", frtime);				
			}
	    	if(control.has("restartup") ) config.put("restartup", control.getInt("restartup"));
	    	if(control.has("delayed") ) config.put("delayed", control.getInt("delayed"));
	    	if(control.has("pidfile")) config.put("pidfile", control.getString("pidfile"));
	    	if(control.has("logfile")) config.put("logfile", control.getString("logfile"));
	    	if(control.has("starttime")) config.put("starttime", control.getString("starttime"));
	    	if(control.has("endtime")) config.put("endtime", control.getString("endtime"));
			
		}
		if( e.has("startup") )
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
	            config.put("startupCommands", sbCommand.toString());
	            config.put("startupCommandsRemark", sbRemark.toString());
			}
		}
		if( e.has("shutdown") )
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
	            config.put("shutdownCommands", sbCommand.toString());
	            config.put("shutdownCommandsRemark", sbRemark.toString());
			}
		}
		return config;
	}
	 */
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
	
	/**
	 * 设置主控配置文件
	 * @param ip
	 * @param port
	 * @param serverkey
	 * @param payload
	 * @throws Exception
	public void setControlxml(
			String ip,
			int port, 
			String serverkey,
			byte[] payload)
		throws Exception
	{
		ZooKeeper zookeeper = Zookeeper.getInstance(ip, port);
		String zkpath = "/cos/config/program/"+serverkey+"/control.xml";
		Stat stat = zookeeper.exists(zkpath, false);
		if( stat != null )
		{
			zookeeper.setData(zkpath, payload, stat.getVersion());
		}
		else
		{
			ZKMgr.createNode(zookeeper, zkpath, payload);
		}
		this.setControlxml(ip, port, payload);
	}
	 */
	
	/**
	 * 保存主控配置到Zk
	 * @param zookeeper
	 * @param path
	 * @param data
	 * @param version
	 * @throws Exception
	private void setControlxml(ZooKeeper zookeeper, Stat stat, String zkpath, byte[] data)
		throws Exception
	{
		if( stat == null )
		{
			ZKMgr.createNode(zookeeper, zkpath, data);
		}
		else
		{
			zookeeper.setData(zkpath, data, stat.getVersion());
		}
	}
	 */
	/**
	 * 保存主控配置到伺服器磁盘
	 * @param ip
	 * @param port
	 * @param controlXml
	 * @throws Exception
	 */
	public void setControlxml(String ip, int port, byte[] controlXml) throws Exception
	{
    	byte[] payload = new byte[4];
    	payload[0] = Command.CONTROL_CONTROLXMLCONFIG;
    	payload[1] = 0;    	
    	DatagramSocket datagramSocket = null;
    	Socket socket = null;
        try
        {
        	datagramSocket = new DatagramSocket();
            datagramSocket.setSoTimeout(15000);
            DatagramPacket request = new DatagramPacket(payload, 0, 1, InetAddress.getByName( ip ), port );
            datagramSocket.send( request );
			DatagramPacket response = new DatagramPacket(payload, payload.length);
			datagramSocket.receive(response);
			port = Tools.bytesToInt(payload, 0, 4);
			datagramSocket.close();
			InetSocketAddress endpoint = new InetSocketAddress(InetAddress.getByName(ip), port);
			socket = new Socket();
			socket.connect(endpoint, 15000);
            OutputStream out = socket.getOutputStream();
			out.write(controlXml);
            out.flush();
            out.close();
        }
        catch(Exception e)
        {
			throw e;
        }
        finally
        {
    		try
			{
    			if( datagramSocket != null ) datagramSocket.close();
            	if( socket != null ) socket.close();
			}
			catch (IOException e)
			{
			}
        }
	}
	public byte[] execute(String ip, int port, int cmd, String id) throws Exception
	{
    	byte[] payload = new byte[64*1024];
    	payload[0] = (byte)cmd;
    	int len = 1;
    	if( id != null && !id.isEmpty() ){
    		payload[1] = (byte)id.length();
    		Tools.copyByteArray(id.getBytes(), payload, 2);
    		len += id.length();
    		len += 1;
    	}
    	DatagramSocket datagramSocket = null;
    	Socket socket = null;
    	ByteArrayOutputStream out = new ByteArrayOutputStream();
        try
        {
        	datagramSocket = new DatagramSocket();
            datagramSocket.setSoTimeout(15000);
            InetAddress addr = InetAddress.getByName( ip );
            DatagramPacket request = new DatagramPacket(payload, 0, len, addr, port );
            datagramSocket.send( request );
			DatagramPacket response = new DatagramPacket(payload, payload.length);
			datagramSocket.receive(response);
			port = Tools.bytesToInt(payload, 0, 4);
			datagramSocket.close();
			if( port > 0 ){
				InetSocketAddress endpoint = new InetSocketAddress(addr, port);
				socket = new Socket();
				socket.connect(endpoint, 15000);
	            InputStream is = socket.getInputStream();
	    		int ch;
	    		while( (ch = is.read(payload, 0, payload.length)) != -1  )
	    		{
	    			out.write(payload, 0, ch);
	    		}
	            is.close();
	            return out.toByteArray();
			}
			else{
				String tips = new String(payload, 4, response.getLength()-4, "UTF-8");
				byte[] buf = tips.getBytes("UTF-8");
				payload = new byte[1+buf.length];
				System.arraycopy(buf, 0, payload, 1, buf.length);
				payload[0] = 1;
				return payload;
			}
        }
        catch(Exception e)
        {
        	throw e;
        }
        finally
        {
    		try
			{
    			if( datagramSocket != null ) datagramSocket.close();
            	if( socket != null ) socket.close();
            	if( out != null ) out.close();
			}
			catch (IOException e)
			{
			}
        }
	}
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
    	payload[offset++] = Command.CONTROL_LOGFILELIST;
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
	 * 发送SSH
	 * @param host
	 * @param indication
	 * @return
	 */
	private HashMap<String, SSH> mapSSH = new HashMap<String, SSH>();
	public String sendSsh(String host, int port, String command)
	{
		log.info("Send ssh("+host+":"+port+") "+command);
    	try
    	{
    		SSH ssh = this.mapSSH.get(host+":"+port);
        	if( command.equalsIgnoreCase("exit") && ssh != null )
        	{
	        	ssh.send(command);
        		ssh.close();
        	}
        	else
        	{
	        	if( ssh == null )
	        	{
	        		ssh = new SSH(host, port)
	        		{
						public void connect()
						{
				            mapSSH.put(this.toString(), this);
						}
						public void disconnect(Exception e)
						{
				            mapSSH.remove(this.toString());
						}
						public void receive(String line)
						{
						}
						public void executed()
						{
						}
	        		};
	        		ssh.waitConnect();
	        	}
	        	ssh.send(command);
        	}
    		return "";
    	}
    	catch(Exception e)
    	{
    		return "Failed to ssh for exception:"+e;
    	}
	}
	/**
	 * 得到SSH消息的列表
	 * @param host
	 * @param lastLine
	 * @return
	 */
	public SSHResponse getSshResponse(String host, int port, int offset)
	{
		SSHResponse response = new SSHResponse();
		response.setOffset(offset);
    	SSH ssh = this.mapSSH.get(host+":"+port);
    	if( ssh != null )
    	{
    		ssh.receive(response);
    	}
		return response;
	}

	/**
	 * 打开调测通道
	 * @param host
	 * @param indication
	 * @return
	 */
	private HashMap<String, Debug> mapDebug = new HashMap<String, Debug>();
	public String openDebug(String host, int port, String module)
	{
		log.info("Open debug("+host+":"+port+" "+module+")");
    	try
    	{
    		Debug debug = this.mapDebug.get(host+":"+port+" "+module);
        	if( debug == null )
        	{
        		debug = new Debug(host, port, module)
        		{
					public void connect()
					{
						mapDebug.put(this.toString(), this);
					}
					public void disconnect(Exception e)
					{
						mapDebug.remove(this.toString());
					}
					public void receive(String line)
					{
					}
					public void executed()
					{
					}
        		};
        		debug.waitConnect();
        	}
    		return "";
    	}
    	catch(Exception e)
    	{
    		return "打开调测管道出现异常"+e;
    	}
	}
	
	/**
	 * 关闭调测通道
	 * @param host
	 * @param port
	 * @param module
	 * @return
	 */
	public String closeDebug(String host, int port, String module)
	{
		log.info("Close debug("+host+":"+port+" "+module+")");
    	try
    	{
    		Debug debug = this.mapDebug.get(host+":"+port+" "+module);
        	if( debug != null )
        	{
        		debug.close();
        		return "调测管道被关闭";
        	}
        	else
        		return "调测管道已经被关闭";
    	}
    	catch(Exception e)
    	{
    		return "关闭调测管道出现异常"+e;
    	}
	}
	
	/**
	 * 得到SSH消息的列表
	 * @param host
	 * @param lastLine
	 * @return
	 */
	public DebugResponse getDebugResponse(String host, int port, String module, int offset)
	{
		DebugResponse response = new DebugResponse();
		response.setOffset(offset);
    	Debug debug = this.mapDebug.get(host+":"+port+" "+module);
    	if( debug != null )
    	{
    		response.setOpened(true);
    		debug.receive(response);
    	}
		return response;
	}
	
	/**
	 * 查找类所在的路径
	 * @param host
	 * @param classCompare
	 * @return
	 */
	public List<String> findJars(String host, String classCompare)
	{
		ArrayList<String> buffer = new ArrayList<String>();
		if( classCompare == null )
		{
			return buffer;
		}
		if( host == null || host.isEmpty() )
		{
			File libPath = PathFactory.getAppPath();
			buffer.add("搜索"+libPath.getAbsolutePath().toString()+"……");
			try
			{
				ClassUtils.findJars(buffer, classCompare, libPath.getAbsoluteFile(), true);
			}
			catch (Exception e)
			{
				buffer.add(e.getMessage());
			}
		}
		else
		{//到指定HOST去查找
		}
		return buffer;
	}
	/**
	 * 主控配置
	 * @author focus
	 *
	class ControlSetting
	{
		String serverkey;
		ProgramConfig config;
		Element moduleNode;
		ZooKeeper zookeeper;
		String zkpath;
		Stat stat;//
	}
	 */
	/**
	 * 
	 * @param zookeeper
	 * @param ip
	 * @param port
	 * @param serverkey
	 * @param id
	 * @return
	public String fetchControlxml(
		   String ip,
		   int port, 
		   String serverkey)
		throws Exception
	{
		ControlSetting setting = new ControlSetting();
		return new String(this.fetchControlxml(ip, port, serverkey, setting), "UTF-8");
	}
	 */
	/**
	 * 
	 * @param ip
	 * @param port
	 * @param serverkey
	 * @param setting
	 * @return
	 * @throws Exception
	private byte[] fetchControlxml(
			String ip,
			int port, 
			String serverkey,
			ControlSetting setting)
		throws Exception
	{
		setting.zookeeper = Zookeeper.getInstance(ip, port);
		setting.zkpath = "/cos/config/program/"+serverkey+"/control.xml";
		setting.stat = setting.zookeeper.exists(setting.zkpath, false);
		byte[] buffer = null;
		if( setting.stat != null )
		{
			buffer = setting.zookeeper.getData(setting.zkpath, false, setting.stat);
			
		}
		else
		{
			buffer = fetchControlxml(ip, port);
		}
		return buffer;
	}
	 */
	/**
	 * 
	 * @param ip
	 * @param port
	 * @param serverkey
	 * @param setting
	 * @return
	 * @throws Exception
	private XMLParser getControlxml(
			String ip,
			int port, 
			String serverkey,
			ControlSetting setting)
		throws Exception
	{
		byte[] buffer = this.fetchControlxml(ip, port, serverkey, setting);
		XMLParser xml = new XMLParser( new ByteArrayInputStream(buffer));
		if( setting.config == null ) return xml;
		Element moduleNode = XMLParser.getElementByTag( xml.getRootNode(), "module" );
        for( ; moduleNode != null; moduleNode = XMLParser.nextSibling(moduleNode) )
        {
            if( setting.config.getId().equals(XMLParser.getElementAttr( moduleNode, "id" )) )
            {
            	setting.moduleNode = moduleNode;
            	return xml;
            }
        }
		throw new Exception("从Controlxml未找到对应的程序配置");
	}
	 */
	
	/**
	 * 关闭相关资源
	 */
	public void close()
	{
		log.debug("Close RpcMgr.");
		Iterator<String> itr = mapSSH.keySet().iterator();
		while(itr.hasNext())
		{
			SSH ssh = (SSH)mapSSH.get(itr.next());
			ssh.close();
		}

		itr = mapDebug.keySet().iterator();
		while(itr.hasNext())
		{
			Debug debug = (Debug)mapDebug.get(itr.next());
			debug.close();
		}
	}
	
	/**
	 * 接受程序发布
	 * @param req
	 * @param account 当前操作用户
	 * @param map
	 */
	public void accept(HttpServletRequest req, String account, JSONArray acceptdata)
		throws Exception
	{
		JSONArray history = new JSONArray();
		HashMap<String, ArrayList<JSONObject>> publishing = new HashMap<String, ArrayList<JSONObject>>();
		ArrayList<JSONObject> list = null;
		for( int i = 0; i < acceptdata.length(); i++ )
		{
			JSONObject accepting = acceptdata.getJSONObject(i);
			String serverkey = accepting.getString("serverkey");
			String id = accepting.getString("id");
			String ip = accepting.getString("ip");
//			int port = accepting.getInt("port");
			String operuser = accepting.getString("operuser");
			JSONObject e = ZKMgr.getZookeeper().getJSONObject("/cos/temp/program/publish/"+serverkey+":"+id);
			if( e == null )
			{
				throw new Exception("接受["+operuser+"]提交的伺服器【"+ip+"】程序["+id+"]发布失败，因为对应程序配置不存在");
			}
			else
			{
				e.put("appuser", account);
				e.put("apptime", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()));
				if( publishing.containsKey(serverkey) )
				{
					list = publishing.get(serverkey);
				}
				else
				{
					list = new ArrayList<JSONObject>();
					publishing.put(serverkey, list);
				}
				if( accepting.has("timeline") )
					e.put("timeline", accepting.getJSONArray("timeline"));
				list.add(e);
			}
		}
		Iterator<String> iter = publishing.keySet().iterator();
		while( iter.hasNext() )
		{
			String serverkey = iter.next();
			list = publishing.get(serverkey);
			Exception exception = null;
			try
			{
				this.setControl(serverkey, list);
			}
			catch(Exception e)
			{
				log.error("", e);
				exception = e;
			}
			
			for(JSONObject e: list)
			{
				String operuser = e.getString("operuser");
				String id = e.getString("id");
				String ip = e.getString("ip");
				int port = e.getInt("port"); 
				String previewurl = "control!preview.action?ip="+ip+"&port="+port+"&serverkey="+serverkey+"&id="+id;
				if( exception != null )
				{
					throw new Exception("接受["+operuser+"]提交的伺服器【"+ip+"】程序["+id+"]发布失败，因为异常"+exception.getMessage());
				}
				else
				{
					ZKMgr.getZookeeper().delete("/cos/temp/program/publish/"+serverkey+":"+id);
					String responseMessage = "接受["+operuser+"]提交的伺服器【"+ip+"】程序["+id+"]发布";
		    		logoper(req, LogSeverity.INFO, responseMessage, "程序管理", null, previewurl);
		    		sendNotiefieToAccount(
		    				req,
		    				operuser,
							"集群程序管理",
							"系统管理员["+account+"]"+responseMessage,
							null,
							previewurl,
		                    "程序管理", "control!open.action?serverkey="+serverkey);
					history.put(e);
				}
			}
		}
		if( history.length() > 0 )
		{
			String path = "/cos/data/program/publish/history/"+Tools.getFormatTime("yyyyMMdd", System.currentTimeMillis())+"/"+Tools.getFormatTime("HHmmss", System.currentTimeMillis());
			ZKMgr.getZookeeper().createObject(path, history);
			String id = Kit.chr2Unicode(path);
			String responseMessage = "审核接受"+acceptdata.length()+"个程序配置发布";
    		logoper(req, LogSeverity.INFO, responseMessage, "程序管理", null, "control!publishhistory.action?id="+id);
    		SvrMgr.sendNotiefiesToSystemadmin(
    				req,
					"集群程序管理",
					String.format("系统管理员[%s]"+responseMessage, account),
					null,
					"control!publishhistory.action?id="+id,
                    "集群程序管理",
                    "control!navigate.action");
		}
	}
}
