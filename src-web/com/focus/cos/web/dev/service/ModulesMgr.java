package com.focus.cos.web.dev.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Element;

import com.focus.cos.api.SyspublishClient;
import com.focus.cos.web.Version;
import com.focus.cos.web.common.AjaxResult;
import com.focus.cos.web.common.Kit;
import com.focus.cos.web.common.PathFactory;
import com.focus.cos.web.common.ZKMgr;
import com.focus.cos.web.service.SvrMgr;
import com.focus.util.Base64X;
import com.focus.util.IOHelper;
import com.focus.util.QuickSort;
import com.focus.util.Tools;
import com.focus.util.XMLParser;
import com.focus.util.Zookeeper;

public class ModulesMgr extends SvrMgr
{
	private static final Log log = LogFactory.getLog(ModulesMgr.class);
	
	/**
	 * 设置导航显示的节点
	 * @param array
	 */
	public void setNavigates(List<JSONObject> modules, boolean isSysadmin, String account)
	{
		for(int i = 0; i < modules.size(); i++)
		{
			JSONObject module = modules.get(i);
			if( !isSysadmin )
			{
				if( module.has("Developers") )
				{
					JSONObject develpers = module.getJSONObject("Developers");
					if( !develpers.has(account) ){
						modules.remove(i);
						i -= 1;
						continue;
					}
				}
				else {
					modules.remove(i);
					i -= 1;
					continue;
				};
			}
			if( module.has("Disabled") && module.getBoolean("Disabled") )
			{
				module.put("icon", "images/icons/blocked.png");
				modules.remove(i);
				i -= 1;
				continue;
			}
			String id = module.getString("id");
			module.put("type", "module");
			module.put("open", false);
			module.put("name", module.getString("SysName")+"("+id+")");
			module.put("href", this.getVersionTimeline(id));
			module.put("icon", "images/ico/sys.png?v=1");
//			module.put("icon", "images/icons/archives.png");
			JSONArray menus = new JSONArray();
			module.put("children", menus);
			JSONObject menu = null;

			if( isSysadmin )
			{
				menu = new JSONObject();
				menus.put(menu);
				menu.put("id", module.get("id")+".developers");
				menu.put("type", "developers");
				menu.put("name", "系统开发者");
				menu.put("sysid", id);
				menu.put("isParent", false);
				menu.put("href", "modules!developers.action");
				menu.put("icon", "images/icons/user.png");
//				menu.put("icon", "images/ico/developers.png?v=1");
			}

			menu = new JSONObject();
			menus.put(menu);
			menu.put("id", id+".datasource");
			menu.put("type", "datasource");
			menu.put("name", "数据源管理");
			menu.put("sysid", id);
			menu.put("href", "diggcfg!datasource.action");
//			menu.put("icon", "images/icons/spam.png");
			menu.put("icon", "images/ico/database.png?v=1");
			menu.put("isParent", false);
			menu.put("open", true);
			
			menu = new JSONObject();
			menus.put(menu);
			menu.put("id", id+".digg");
			menu.put("type", "digg");
			menu.put("name", "元数据模板开发");
			menu.put("sysid", id);
//			menu.put("abort", true);
			menu.put("href", "diggcfg!explorer.action");
			menu.put("icon", "images/icons/wand.png");
//			menu.put("icon", "images/ico/digg.png?v=2");
			menu.put("isParent", false);
			menu.put("open", true);
			{
				JSONObject submenu = new JSONObject();
				JSONArray submenus = new JSONArray();
				menu.put("children", submenus);
				submenu = new JSONObject();
				submenus.put(submenu);
		        submenu.put("id", id + ".api@digg");
		        submenu.put("type", "api@digg");
				submenu.put("name", "模板接口开发管理");
				submenu.put("sysid", id);
		        submenu.put("href", "diggcfg!api.action");
		        submenu.put("icon", "images/icons/label_funny.png");

				submenu = new JSONObject();
				submenus.put(submenu);
				submenu.put("id", id + ".usage@digg");
				submenu.put("type", "api@digg");
				submenu.put("name", "模板使用情况查询");
				submenu.put("sysid", id);
		        submenu.put("href", "diggcfg!usage.action");
		        submenu.put("icon", "images/icons/history.png");
			}
			
			if( "Sys".equalsIgnoreCase(id) ){
				menu = new JSONObject();
				menus.put(menu);
				menu.put("id", id+".digg_local");
				menu.put("type", "digg_local");
				menu.put("name", "主界面框架内置模板查看");
				menu.put("sysid", id);
//				menu.put("abort", true);
				menu.put("href", "diggcfg!local.action");
				menu.put("icon", "images/icons/wand_disabled.png");
//				menu.put("icon", "images/ico/digg.png?v=2");
				menu.put("isParent", false);
				menu.put("open", true);
			}
			
			menu = new JSONObject();
			menus.put(menu);
			menu.put("id", id+".weixin");
			menu.put("type", "weixin");
			menu.put("name", "微信公众号开发");
			menu.put("sysid", id);
//			menu.put("href", "dev!preset.action?gridxml="+Kit.chr2Unicode("/grid/local/weixincfg.xml"));
			menu.put("href", "weixin!config.action");
//			menu.put("icon", "images/icons/chat.png");
			menu.put("icon", "images/ico/weixin.png?v=2");
			menu.put("isParent", false);
			menu.put("open", true);
			{
				JSONObject submenu = new JSONObject();
				JSONArray submenus = new JSONArray();
				menu.put("children", submenus);
				submenu = new JSONObject();
				submenus.put(submenu);
				submenu.put("id", id+".history@menus");
				submenu.put("type", "history@menus");
				submenu.put("name", "工作状态查询");
				submenu.put("sysid", id);
				submenu.put("href", "weixin!query.action");
		        submenu.put("icon", "images/icons/history.png");
			}
			
//			menu = new JSONObject();
//			menus.put(menu);
//			menu.put("id", id+".report+");
//			menu.put("type", "report+");
//			menu.put("name", "元数据报表开发");
//			menu.put("sysid", id);
//			menu.put("href", "report!manager.action");
//			menu.put("abort", true);
//			menu.put("icon", "images/icons/clock.png");
//			menu.put("isParent", false);
//			menu.put("open", true);			
			
//			menu.put("children", submenus);
//			submenus.put(submenu);
//			submenu.put("type", "developdigg");
//			submenu.put("name", "配置查询");
//			submenu.put("href", "diggcfg!query.action");
//			submenu.put("icon", "images/icons/drafts.png");
//			submenu = new JSONObject();
//			submenus.put(submenu);
//			submenu.put("type", "datasoruce");
//			submenu.put("name", "数据源查询");
//			submenu.put("href", "diggcfg!datasource.action");
//			submenu.put("icon", "images/icons/links.png");

			menu = new JSONObject();
			menus.put(menu);
			menu.put("id", id+".menus");
			menu.put("type", "menus");
			menu.put("name", "后台菜单管理");
			menu.put("sysid", id);
			menu.put("open", false);
			menu.put("href", "menus!config.action");
//			menu.put("icon", "images/icons/folder_up.png");
			menu.put("icon", "images/ico/menus.png?v=2");
			{
				JSONObject submenu = new JSONObject();
				JSONArray submenus = new JSONArray();
				menu.put("children", submenus);
				submenus.put(submenu);
				submenu.put("id", id+".history@menus");
				submenu.put("type", "history@menus");
				submenu.put("name", "历史配置记录");
				submenu.put("sysid", id);
				submenu.put("abort", true);
				submenu.put("href", "#");
				submenu.put("icon", "images/icons/history.png");
			}
			menu = new JSONObject();
			menus.put(menu);
			menu.put("id", id+".programs");
			menu.put("type", "programs");
			menu.put("name", "程序管理");
			menu.put("sysid", id);
			menu.put("href", "program!config.action");
			menu.put("icon", "images/icons/tile.png");
//			menu.put("icon", "images/ico/program.png?v=1");
			menu.put("open", false);
			{
			}
//
//			menu = new JSONObject();
//			menus.put(menu);
//			menu.put("id", id+".deploy");
//			menu.put("id", "deploy");
//			menu.put("name", "部署管理");
//			menu.put("abort", true);
//			menu.put("sysid", id);
//			menu.put("href", "modules!deploy.action");
//			menu.put("icon", "images/icons/folder_up.png");
//			menu.put("open", false);
			
			if( isSysadmin) 
			{
				JSONObject submenu = new JSONObject();
				menu = new JSONObject();
				menus.put(menu);
				menu.put("id", id+".publish");
				menu.put("id", "publish");
				menu.put("name", "发布管理");
				menu.put("href", "modules!publish.action");
				menu.put("sysid", id);
				menu.put("isParent", true);
//				menu.put("icon", "images/icons/forwarded.png");
				menu.put("icon", "images/ico/publish.png?v=2");
				menu.put("open", true);
				JSONArray submenus = new JSONArray();
				menu.put("children", submenus);
	
//				submenu = new JSONObject();
//				submenus.put(submenu);
//				submenu.put("id", id+".approvaldeploy");
//				submenu.put("type", "approvaldeploy");
//				submenu.put("name", "部署审查");
//				submenu.put("abort", true);
//				submenu.put("sysid", id);
//				submenu.put("href", "developer!approvalprogramshistory.action");
//				submenu.put("icon", "images/icons/history.png");
				submenu = new JSONObject();
				submenus.put(submenu);
				submenu.put("id", id+".approvalmenus");
				submenu.put("type", "approvalmenus");
				submenu.put("name", "菜单审查");
				submenu.put("sysid", id);
				submenu.put("href", "menus!publish.action");
//				submenu.put("icon", "images/icons/folder_up.png");
				submenu.put("icon", "images/ico/menus_publish.png?v=1");
				submenu = new JSONObject();
				submenus.put(submenu);
				submenu.put("id", id+".approvalprograms");
				submenu.put("type", "approvalprograms");
				submenu.put("name", "程序审查");
				submenu.put("sysid", id);
				submenu.put("href", "control!publish.action");
//				submenu.put("icon", "images/icons/tile.png");
				submenu.put("icon", "images/ico/program.png?v=2");
			}
		}
	}
	/**
	 * 新增集群目录
	 * @param parentId
	 * @param ip 伺服器的IP地址
	 * @param port 伺服器端口
	 * @param timestamp
	 * @return
	public AjaxResult<String> addModule(
			String id,
			String name,
			String desc,
			String vendor,
			String contact,
			String email,
			String pop3username,
			String smtp,
			String pop3password,
			long timestamp)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		try
		{
			JSONObject module = null;
			if( id==null||id.isEmpty() )
			{
				rsp.setMessage("请为您的系统重新设置一个ID。"); return rsp;
			}
			if( name==null||name.isEmpty() )
			{
				rsp.setMessage("请为您的系统重新设置一个名称。"); return rsp;
			}
			if( desc==null||desc.isEmpty() )
			{
				rsp.setMessage("请您描述系统是实现什么服务的。"); return rsp;
			}
			if( vendor==null||vendor.isEmpty() )
			{
				rsp.setMessage("该系统软件开发商者是谁？"); return rsp;
			}
			if( contact==null||contact.isEmpty() )
			{
				rsp.setMessage("该系统软件开发商者联系人是谁？"); return rsp;
			}
			if( email==null||email.isEmpty() )
			{
				rsp.setMessage("该系统软件开发商者联系人邮箱请提供。"); return rsp;
			}
			Stat stat = ZKMgr.getZooKeeper().exists("/cos/config/modules/"+id, false);
			if( stat != null  )
			{
				rsp.setMessage("您要添加的模块子系统ID已经被其它系统使用，请为您的系统重新设置一个ID。"); return rsp;
			}
			module = new JSONObject();
			module.put("id", id);
			module.put("SysName", name);
			module.put("SysDescr", desc);
			module.put("SoftwareVendor", vendor);
			module.put("SysContactName", contact);
			module.put("SysContact", email);
			module.put("SysMailName", name);
			if( smtp!=null&&smtp.isEmpty() ) module.put("SMTP", smtp);
			if( pop3username!=null&&pop3username.isEmpty() ) module.put("POP3Username", pop3username);
			if( pop3password!=null&&pop3password.isEmpty() ) module.put("POP3PasswordEncrypt", Base64X.encode(pop3password.getBytes()));
			/*
			JSONObject version = new JSONObject();
			JSONArray date = new JSONArray();
			version.put("date", date);
			JSONObject timeline = new JSONObject();
			date.put(timeline);

			timeline.put("version", version);
			timeline.put("time", Tools.getFormatTime("MM/dd/yyyy", System.currentTimeMillis()));//发表时间
			timeline.put("text", "模块");
			
			ZKMgr.getZookeeper().setJSONObject("/cos/config/modules/"+id, module);
			rsp.setMessage("新增模块子系统【"+name+"("+id+")】。");
			rsp.setSucceed(true);
			rsp.setResult(module.toString());
			logoper(rsp.getMessage(), "系统开发", "", getVersionTimeline(id));
			sendNotiefiesToSystemadmin(
					"系统开发",
					String.format("系统管理员[%s]"+rsp.getMessage(), getAccountName()),
                    "",
                    getVersionTimeline(id),
                    null, null);
		}
		catch(Exception e)
		{
			String s = "新增模块子系统【"+name+"("+id+")】出现异常"+e;
			rsp.setMessage(s);
			logoper(rsp.getMessage(), "系统开发", e);
			log.error("Failed to add the module of "+id+"("+name+") for ", e);
		}
		return rsp;		
	}
	 * @throws Exception 
	 */
	/**
	 * 构建发布的配置
	 * @param sysid
	 * @param details
	 * @throws Exception
	 */
	public void buildPublish(String sysid, JSONArray details, boolean noprepublish) throws Exception
	{
		String zkpath = "/cos/config/modules/"+sysid+"/menus/current";//记录当前日志
		Zookeeper zookeeper = ZKMgr.getZookeeper();
		byte[] buf = zookeeper.getData(zkpath);
		String id = null;
		int sn = 0;
		if( buf != null )
		{
			id = new String(buf);
			zkpath = "/cos/config/modules/"+sysid+"/menus/history/"+id;
			JSONObject history = zookeeper.getJSONObject(zkpath, false);
			JSONArray log = history.has("log")?history.getJSONArray("log"):null;
			if( log != null)
			{
				for(int i = 0;i < log.length(); i++)
				{
					JSONObject slog = log.getJSONObject(i);
					if( slog.has("remark") )
					{
						String remark = slog.getString("remark");
						if( remark.startsWith("用户") )
						{
							int j = remark.indexOf('】');
							remark = remark.substring(j+1);
							slog.put("remark", remark);
						}
					}
					slog.put("publishable", 1);
					slog.put("type", "menus");
					slog.put("user", slog.has("username")?slog.getString("username"):"N/A");
					slog.put("version", "");
					slog.put("opertype", slog.getInt("oper"));
					slog.put("lastversion", "");
					String remark = slog.has("remark")?slog.getString("remark"):"";
					slog.put("publishremark", remark+"待系统管理员审批。");
					slog.put("sn", sn++);
					details.put(slog);
				}
			}
		}

		zkpath = "/cos/config/modules/"+sysid+"/program";//记录当前日志
		JSONArray programs = zookeeper.getJSONArray(zkpath, false);
		for( int i = 0; i < programs.length(); i++ )
		{
			JSONObject program = programs.getJSONObject(i);
			if( program.has("removed") )
			{
				programs.remove(i);
				i -= 1;
				continue;
			}
			program = ProgramMgr.getUnconstructProgramConfig(program);
			program.put("type", "programs");
			program.put("user", program.has("maintenance")?program.getJSONObject("maintenance").getString("programmer"):"N/A");
			program.put("publishable", 1);
			program.put("opertype", 2000);
			if( program.has("startupCommands") )
			{
				program.put("startupCommands", Kit.unicode2Chr(program.getString("startupCommands")));
			}
			if( program.has("shutdownCommands") )
			{
				program.put("shutdownCommands", Kit.unicode2Chr(program.getString("shutdownCommands")));
			}
			if( program.has("publish") )
			{
				JSONObject publish = program.getJSONObject("publish");
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
				program.put("publish", array);
			}
			
			String remark = "程序【"+program.getString("id")+"】 "+program.getString("name");
			program.put("publishremark", remark+"可发布");
			if( !program.has("version") )
			{
				program.put("opertype", 2001);
				program.put("publishable", 0);
				program.put("publishremark", remark+"因为不没有配置版本号而不允许发布");
			}
			else if( !program.has("maintenance") )
			{
				program.put("opertype", 2001);
				program.put("publishable", 0);
				program.put("publishremark", remark+"因为不没有配置程序管理者而不允许发布");
			}
			else if( !program.has("publish") )
			{
				program.put("opertype", 2001);
				program.put("publishable", 0);
				program.put("publishremark", remark+"因为不没有配置发布到那台伺服器而不允许发布");
			}
			String lastversion0 = "0.0.0.0";
			if( program.has("publishlogs") )
			{
				JSONArray publishlogs = program.getJSONArray("publishlogs");
				if( publishlogs.length() > 0 ) lastversion0 = publishlogs.getJSONObject(publishlogs.length()-1).getString("version");
			}
			program.put("lastversion", lastversion0);
			if( program.getInt("opertype") == 2000 )
			{
				String version = program.has("version")?program.getString("version"):"0.0.0.0";
				String args0[] = Tools.split(lastversion0, ".");
				String args1[] = Tools.split(version, ".");
				int v0 = 1000000*Integer.parseInt(args0[0])+10000*Integer.parseInt(args0[1])+100*Integer.parseInt(args0[2])+Integer.parseInt(args0[3]);
				int v1 = 1000000*Integer.parseInt(args1[0])+10000*Integer.parseInt(args1[1])+100*Integer.parseInt(args1[2])+Integer.parseInt(args1[3]);
				if( v0 >= v1 )
				{
					program.put("opertype", 2002);
					program.put("publishable", 0);
					program.put("publishremark", remark+"因为待发布版本已发布，请检查程序上个版本号");
				}
			}
			
			program.remove("startupCommandsRemark");
			program.remove("shutdownCommandsRemark");
			program.put("sn", sn++);
			
			if( noprepublish && program.getInt("opertype") == 2000 )
			{
				JSONObject publish = new JSONObject(program.toString());
				publish.remove("publish");
				publish.remove("timeline");
				publish.remove("operlogs");
				publish.remove("type");
				JSONArray servers = program.getJSONArray("publish");
				for(int j = 0; j < servers.length(); j++)
				{
					JSONObject e = servers.getJSONObject(j);
					publish.put("ip", e.getString("ip"));
					publish.put("port", e.getInt("port"));
					publish.put("serverkey", Tools.encodeMD5(e.getString("serverid")));
					SyspublishClient.publish(publish, lastversion0.equals("0.0.0.0"), "用户通过开发管理申请发布系统【"+sysid+"】程序", super.getAccountName());
				}
			}
			if( !noprepublish || program.getInt("opertype") == 2000 )
				details.put(program);
			program.remove("startup");
			program.remove("shutdown");
		}
		
//		//加载接口配置
//		zkpath = "/cos/config/modules/"+sysid+"/digg";//记录当前日志
//		JSONArray diggapis = new JSONArray();
//		diggConfigMgr.buildDiggApiPrepublish(zookeeper, zkpath, diggapis);
//		for( int i = 0; i < diggapis.length(); i++ )
//		{
//			JSONObject diggapi = diggapis.getJSONObject(i);
//			JSONObject template = diggapi.getJSONObject("template");
//			diggapi.put("publishable", 0);
//			diggapi.put("user", template.getString("developer"));
//			if( diggapi.getInt("status") == 0 ){
//				diggapi.put("opertype", 3000);
//				diggapi.put("publishremark", "申请开通模板【"+template.getString("cname")+"】的开放数据接口，模板ID是["+template.getString("id")+"]");
//			}
//			else if( diggapi.getInt("status") == 2 ){
//				diggapi.put("opertype", 3002);
//				diggapi.put("publishremark", "修改模板【"+template.getString("cname")+"】的开放数据接口的配置参数，模板ID是["+template.getString("id")+"]");
//			}
//			else if( diggapi.getInt("status") == 3 ){
//				diggapi.put("opertype", 3003);
//				diggapi.put("publishremark", "申请取消模板【"+template.getString("cname")+"】的开放数据接口，模板ID是["+template.getString("id")+"]");
//			}
//			diggapi.put("type", "diggapi");
//			diggapi.put("version", "");
//			diggapi.put("lastversion", "");
//			details.put(diggapi);
//		}
	}
	/**
	 * 发布系统
	 * @param sysid
	 * @param sysname
	 * @param version
	 * @param lastversion
	 * @param versionremark
	 * @return
	 */
	public AjaxResult<String> publish(
			String sysid,
			String sysname,
			String version,
			String lastversion,
			String versionremark)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		try
		{
			if( sysid==null||sysid.isEmpty() )
			{
				rsp.setMessage("未知发布的系统"); return rsp;
			}
			if( version==null||version.isEmpty() )
			{
				rsp.setMessage("请为您的系统发布设置版本号。"); return rsp;
			}
			if( versionremark==null||versionremark.isEmpty() )
			{
				rsp.setMessage("请您描述待发布系统的版本特性。"); return rsp;
			}
			JSONObject syspublish = new JSONObject();
			syspublish.put("sysid", sysid);
			syspublish.put("sysname", sysname);
			syspublish.put("version", version);
			syspublish.put("time", Tools.getFormatTime("yyyy-MM-dd HH:mm"));
			syspublish.put("status", 0);
			syspublish.put("lastversion", lastversion);
			syspublish.put("remark", versionremark);
			syspublish.put("publisher", super.getAccountName());//发布者
			JSONArray details = new JSONArray();//发布项明细
			syspublish.put("details", details);
			
			this.buildPublish(sysid, details, true);

			Zookeeper zookeeper = ZKMgr.getZookeeper();
			String zkpath = "/cos/config/modules/"+sysid+"/publish";
			JSONObject publish = ZKMgr.getZookeeper().getJSONObject(zkpath);
			if( publish == null )
			{
				publish = new JSONObject();
			}
			publish.put("publishing", version);
			zookeeper.setJSONObject(zkpath, publish);
			zkpath += "/"+version;
			zookeeper.setGzipJSONObject(zkpath, syspublish);
			rsp.setMessage("申请发布系统【"+sysname+"】 版本[v"+version+"]");
			rsp.setSucceed(true);
			rsp.setResult("");
			logoper(rsp.getMessage(), "系统开发", "", "modules!viewpublish.action?sysid="+sysid+"&id="+version);
			sendNotiefiesToSystemadmin(
					"系统开发",
					String.format("开发者[%s]"+rsp.getMessage(), getAccountName()),
                    "",
                    "modules!viewpublish.action?sysid="+sysid+"&id="+version,
                    "审批", "modules!navigate.action");
		}
		catch(Exception e)
		{
			String s = "申请发布系统【"+sysname+"】版本[v"+version+"]出现异常"+e.getMessage();
			rsp.setMessage(s);
			logoper(rsp.getMessage(), "系统开发", e);
			log.error("Failed to publish the module of "+sysid+"("+sysname+") for ", e);
		}
		return rsp;		
	}
	
	public String getVersionTimeline(String id)
	{
		String dataurl = "modules!versiontimeline.action?sysid="+id;
		return "helper!timeline.action?dataurl="+Kit.chr2Unicode(dataurl);
	}

	public void setLogo(String id, byte[] data)
	{
		String path = "/cos/config/modules/"+id+"/logo.png";
		try
		{
			ZooKeeper zookeeper = ZKMgr.getZooKeeper();
			Stat stat = zookeeper.exists(path, false); 
			if( stat != null)
			{
				zookeeper.setData(path, data, stat.getVersion());
			}
			else
			{
				zookeeper.create(path, data, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
		}
		catch(Exception e)
		{
		}
	}

	/**
	 * 
	 * @return
	 */
	public static JSONObject getConfig(String id)
	{
		String path = "/cos/config/modules/"+id;
		File file = new File(PathFactory.getCfgPath(), "modules/"+id+".tmp");
		try
		{
			JSONObject json = ZKMgr.getZookeeper().getJSONObject(path, false);
			IOHelper.writeSerializable(file, json.toString());
			return json;
		}
		catch(Exception e)
		{
			if( file.exists() )
			{
				Object json = IOHelper.readSerializableNoException(file);
				if( json != null && json instanceof String )
					return new JSONObject(json.toString());
			}
			return null;
		}
	}

	/**
	 * 返回保存在ZK中的组件配置
	 * @return
	 */
	public static List<JSONObject> getConfigs()
		throws Exception
	{
		JSONArray modules = new JSONArray();
		try
		{
			ZKMgr.getZookeeper().getJSONArray("/cos/config/modules", modules, false, false);
			for(int i = 0; i < modules.length(); i++ )
			{
				JSONObject module = modules.getJSONObject(i);
				String zkpath = "/cos/config/modules/"+module.getString("id")+"/publish";
				JSONObject version = ZKMgr.getZookeeper().getJSONObject(zkpath);
				if( version != null )
				{
					String current = version.has("current")?version.getString("current"):"0.0.0.0";
					if( version.has("publishing") )
					{
						zkpath = "/cos/config/modules/"+module.getString("id")+"/publish/"+version.getString("publishing");
						JSONObject publish = ZKMgr.getZookeeper().getUngzipJSONObject(zkpath);
						if( publish != null && publish.has("status") && publish.getInt("status") != 1 )
						{
							JSONObject publishing = new JSONObject();
							publishing.put("version", version.getString("publishing"));
							publishing.put("status", publish.getInt("status"));
							if( publish.getInt("status") == 2 || publish.getInt("status") == 3 )
							{
								if( publish.has("reason") )	publishing.put("reason", publish.getString("reason"));
								else publishing.put("reason", "未知异常或错误");
							}
							publishing.put("remark", publish.getString("remark"));//版本描述
							module.put("publishing", publishing);
						}
					}
					module.put("version", current);
				}
				else
				{
					module.put("version", "0.0.0.0");
				}
			}
		}
		catch(Exception e)
		{
//			throw new Exception("分布式应用协调程序不能正常工作");
			File dir = new File(PathFactory.getCfgPath(), "modules/");
			File[] files = dir.listFiles(new FileFilter(){
				public boolean accept(File file)
				{
					if(file.isDirectory()) return false;
					if(file.isHidden()) return false;
					if( file.getName().toLowerCase().endsWith(".tmp") ) return true;
					return false;
				}
			});
			for(File file : files)
			{
				Object json = IOHelper.readSerializableNoException(file);
				if( json != null && json instanceof String )
					modules.put(new JSONObject(json.toString()));
			}
		}
		List<JSONObject> list = new ArrayList<JSONObject>();
		for(int i = 0; i < modules.length(); i++)
			list.add(modules.getJSONObject(i));
		if( modules.length() >= 2 )
		{
			QuickSort sortor = new QuickSort()
	        {
	            public boolean compareTo( Object left, Object right )
	            {
	            	JSONObject l = (JSONObject)left;
	            	JSONObject r = (JSONObject)right;
	    			try
	    			{
	                	int li = l.getInt("PortalOrder");
	                	int ri = r.getInt("PortalOrder");
	                    return li < ri;
	    			}
	    			catch (Exception e)
	    			{
	    				return true;
	    			}            	
	            }
	        };
	        sortor.sort(list);			
		}
		return list;
	}

	/**
	 * 按照模块子系统开发重构modules配置
	 * @return
	 */
	public static void rebuildModules()
	{
		String path = "/cos/config/modules";
		StringBuffer sb = new StringBuffer();
		try
		{
			Zookeeper zookeeper = ZKMgr.getZookeeper();
			Stat stat = zookeeper.exists(path, false); 
			if( stat != null )// && !file.exists() )
			{
				sb.append("Not found modules.xml and rebuild ok.");
				//file.delete();
				stat = zookeeper.exists(path+"/Sys", false); 
				if( stat == null )
				{
					sb.append("\r\n\tCreate the default module of Sys.");
					createDefaultModule(zookeeper);
				}
				return;
			}
			sb.append("Need to rebuild the modules.");
			path = "/cos/config/modules.xml";
			stat = zookeeper.exists(path, false); 
			byte[] data = null;
			if( stat != null )
			{
				sb.append("\r\n\tFound the node of modules.xml.");
				data = zookeeper.getData(path, false, stat);
			}
			else
			{
				log.warn("\r\n\tNot found the node of modules.xml.");
				String xml = "<?xml version='1.0' encoding='UTF-8'?><sys name='COS' version='"+Version.getValue()+"'></sys>";
				data = xml.getBytes();
			}
			zookeeper.create("/cos/config/modules", data);
			sb.append("\r\n\tCreate the modules("+data.length+").");
//			sb.append("\r\n"+new String(data, "UTF-8"));
			
			XMLParser xml = new XMLParser(new ByteArrayInputStream(data));
			Element moduleNode = XMLParser.getFirstChildElement( xml.getRootNode() );
			HashMap<String, Element> map = new HashMap<String, Element>();
	        for( ; moduleNode != null; moduleNode = XMLParser.getNextSibling(moduleNode) )
	        {
	          	if( moduleNode.getTagName().equals("toolbar") ) 
	        	{
	        		continue;
	        	}
	        	String id = XMLParser.getElementAttr(moduleNode, "id");
	        	map.put(id, moduleNode);
	        }
			path = "/cos/config/cmp";
			stat = zookeeper.exists(path); 
			if( stat == null ){
				sb.append("\r\n\tCreate the default module of Sys.");
				createDefaultModule(zookeeper);
				return;
			}
			List<String> nodes = zookeeper.getChildren(path);
			if( nodes.isEmpty() )
			{
				sb.append("\r\n\tCreate the default module of Sys.");
				createDefaultModule(zookeeper);
				return;
			}
			for(String sysid : nodes)
			{
				if( sysid.endsWith(".png") )
				{
					continue;
				}
				String zkpath = path+"/"+sysid;
				JSONObject module = zookeeper.getJSONObject(zkpath, false);
				module.put("SysName", module.getString("name"));
				module.put("SoftwareVendor", module.has("CmpDevelopers")?module.getString("CmpDevelopers"):"");
				module.put("SysContactName", module.has("CmpDevelopersContact")?module.getString("CmpDevelopersContact"):"");
				module.put("SysContact", module.has("CmpDevelopersEmail")?module.getString("CmpDevelopersEmail"):"");
				module.put("Disabled", module.has("CmpDisabled")?"true".equals(module.get("CmpDisabled").toString()):false);
				String order = module.has("CmpPortalOrder")?module.get("CmpPortalOrder").toString():"100";
				module.put("PortalOrder", Tools.isNumeric(order)?Integer.parseInt(order):100 );
				module.put("POP3PasswordEncrypt", Base64X.encode((module.has("POP3Password")?module.getString("POP3Password"):"").getBytes()));
				zookeeper.setJSONObject("/cos/config/modules/"+sysid, module, false);
				sb.append("\r\n\t\tMove the module("+module+")");
//				 "id": "banquanjia-game-code", 
//				 "CmpDevelopers": "成都研发中心-版权家项目组", "CmpPortalOrder": "2"
//				"SMTP": ""
//				"SysMailName": ""
//				"name": "版权家版号系统"
//				"CmpDevelopersEmail": "guanzhenyong@efida.com.cn"
//				"CmpDevelopersContact": "管贞勇"
//				"POP3Username": ""
//				"POP3Password": "******"
//				"Remark": ""
//				"CmpDisabled": false
				String xmlstr = "<?xml version='1.0' encoding='UTF-8'?><sys name='COS' version='"+Version.getValue()+"'></sys>";
				xml = new XMLParser(new ByteArrayInputStream(xmlstr.getBytes()));
				moduleNode = map.get(sysid);
		        if( moduleNode != null )
		        {
		        	Element target = xml.createElement("module");
		        	target.setAttribute("id", sysid);
			        XMLParser.copyElement(moduleNode, target, xml);
		        }
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				xml.write(out);
				data = out.toByteArray();
				zookeeper.create("/cos/config/modules/"+sysid+"/menus", data);
				sb.append("\r\n\t\t\tCreate the menus.");
			}
			for(String logo : nodes)
			{
				if( !logo.endsWith(".png") )
				{
					continue;
				}
				logo = logo.replaceAll(".png", "/logo.png");
				data = zookeeper.getData(path+"/"+logo);
				zookeeper.create("/cos/config/modules/"+logo, data);
				sb.append("\r\n\t\tMove the logo of module("+logo+")");
			}
		}
		catch(Exception e)
		{
			log.error("Failed to rebuild the modules from xml and cmp.", e);
		}
		finally {
			log.info(sb.toString());
		}
	}
	
	/**
	 * 
	 * @param zookeeper
	 * @throws Exception
	 */
	public static void createDefaultModule(Zookeeper zookeeper) throws Exception
	{
		JSONObject module = new JSONObject();
		JSONObject syscfg = zookeeper.getJSONObject("/cos/config/system");
		JSONObject sftcfg = zookeeper.getJSONObject("/cos/config/software");
		if( sftcfg != null )
		{
			module.put("id", "Sys");
			module.put("SysName", "系统管理");
			module.put("SysDescr", "系统缺省生成的子系统模块，用于配置系统基础组件程序、系统维护、角色权限等配置菜单.");
			module.put("SoftwareVendor", sftcfg.getString("SoftwareVendor"));
			module.put("SysContactName", syscfg.getString("SysContactName"));
			module.put("SysContact", syscfg.getString("SysContact"));
			module.put("PortalOrder", 0);
			module.put("SysMailName", "系统管理");
			zookeeper.create("/cos/config/modules/Sys", module.toString().getBytes("UTF-8"));
			String xmlstr = "<?xml version='1.0' encoding='UTF-8'?><sys name='COS' version='"+Version.getValue()+"'></sys>";
			XMLParser xml = new XMLParser(new ByteArrayInputStream(xmlstr.getBytes()));
			Element moduleNode = xml.createElement("module");
			moduleNode.setAttribute("id", "Sys");
			xml.getRootElement().appendChild(moduleNode);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			xml.write(out);
			zookeeper.create("/cos/config/modules/Sys/menus", out.toByteArray());
		}
	}
}
