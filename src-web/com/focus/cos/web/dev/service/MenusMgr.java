package com.focus.cos.web.dev.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.data.Stat;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.focus.cos.api.LogSeverity;
import com.focus.cos.api.Sysnotify;
import com.focus.cos.api.SysnotifyClient;
import com.focus.cos.web.Version;
import com.focus.cos.web.common.AjaxResult;
import com.focus.cos.web.common.Kit;
import com.focus.cos.web.common.PathFactory;
import com.focus.cos.web.common.ZKMgr;
import com.focus.cos.web.service.SvrMgr;
import com.focus.skit.tree.KAction;
import com.focus.skit.tree.KActionItem;
import com.focus.util.IOHelper;
import com.focus.util.Tools;
import com.focus.util.XMLParser;
import com.focus.util.Zookeeper;
/**
 * 组件管理
 * @author focus
 *
 */
public class MenusMgr extends SvrMgr
{
	private static final Log log = LogFactory.getLog(MenusMgr.class);
	/*模块子系统菜单版本号*/
	private static long ModulesVersionTimestamp;
	
	/**
	 * 设置前的操作
	 * @param rsp
	 * @param zookeeper
	 * @param timestamp
	 * @return
	 * @throws Exception
	 */
	private InputStream preSet(AjaxResult<String> rsp, String sysid, Zookeeper zookeeper, long timestamp) throws Exception
	{
		String id = sysid;
		if( "toolbar".equals(id) ) sysid = "Sys";
		rsp.setTimestamp(ModulesVersionTimestamp);
		JSONObject account = getAccount();
		if( account == null )
		{
			rsp.setMessage("未知操作用户");
			return null;
		}
		String zkpath = "/cos/config/modules/"+sysid+"/menus";
		Stat stat = zookeeper.exists(zkpath, false);
		if( stat == null )
		{
			zkpath = "/cos/config/modules";
			stat = zookeeper.exists(zkpath, false);
		}
		if( stat == null )
		{
			String xml = "<?xml version='1.0' encoding='UTF-8'?><sys name='COS' version='"+Version.getValue()+"'></sys>";
			return new ByteArrayInputStream(xml.getBytes());
		}
		/*ModulesVersionTimestamp = stat.getMtime();
		if( timestamp != ModulesVersionTimestamp )
		{
			String s = "该模块子系统菜单配置在"+Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", ModulesVersionTimestamp)+"被其它用户所修改。";
			rsp.setMessage(s);
			rsp.setTimestamp(ModulesVersionTimestamp);
			JSONArray zNodes = new JSONArray();
			loadMenus(zNodes, sysid, new ByteArrayInputStream(zookeeper.getData(zkpath, false, stat)));
			rsp.setResult(zNodes.toString());
			return null;
		}*/
		return new ByteArrayInputStream(zookeeper.getData(zkpath, false, stat));
	}
	/**
	 * 
	 * @param rsp
	 * @param xml
	 * @param zookeeper
	 * @param reload
	 * @param logs
	 * @throws Exception
	 */
	private void set(AjaxResult<String> rsp, String sysid, XMLParser xml, Zookeeper zookeeper, boolean reload, String logs, JSONObject slog)
		throws Exception
	{
		Element removeModuleNode = null;
		Element moduleNode = XMLParser.getFirstChildElement( xml.getRootNode() );
        while( moduleNode != null )
        {
        	String id = XMLParser.getElementAttr(moduleNode, "id");
        	if( sysid.equals(id) || ("toolbar".equals(moduleNode.getNodeName()) && "Sys".equals(sysid)) )
        	{
            	moduleNode = XMLParser.getNextSibling(moduleNode);
        	}
        	else
        	{
        		removeModuleNode = moduleNode;
            	moduleNode = XMLParser.getNextSibling(moduleNode);
        		xml.getRootNode().removeChild(removeModuleNode);//删除多余的配置
        	}
        }
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		xml.write(out);
		byte[] data = out.toByteArray();
		String zkpath = "/cos/config/modules/"+sysid+"/menus";
		Stat stat = zookeeper.exists(zkpath, false);
		if( stat == null )
		{
			zookeeper.create(zkpath, data);
			stat = zookeeper.exists(zkpath, false);
		}
		else
		{
			stat = zookeeper.setData(zkpath, data, stat.getVersion());
		}
		ModulesVersionTimestamp = stat!=null?stat.getMtime():0;
		rsp.setTimestamp(ModulesVersionTimestamp);
		logoper(LogSeverity.INFO, logs, "系统配置");
		if( reload )
		{
			JSONArray zNodes = new JSONArray();
			loadMenus(zNodes, sysid, new ByteArrayInputStream(data));
			rsp.setResult(zNodes.toString());
		}
		JSONObject account = getAccount();
		slog.put("operuser", account.getString("username"));
		slog.put("username", account.getString("realname"));
		StringBuffer title = new StringBuffer();
		StringBuffer content = new StringBuffer("配置汇总情况如下，或见下表所示。");
		switch( slog.getInt("oper") )
		{
		case 0:
			title.append("新增了【"+slog.getString("modulename")+"】的菜单【"+slog.getString("newname")+"】");
			content.append("\r\n\t菜单名称："+slog.getString("newname"));
			content.append("\r\n\t菜单URL："+slog.getString("newhref"));
			content.append("\r\n\t菜单图标："+slog.getString("newicon"));
			content.append("\r\n\t打开方式："+slog.getString("newtarget"));
			if( slog.has("buttons") )
			{
				content.append("\r\n\t菜单按钮：");
				JSONArray buttons = slog.getJSONArray("buttons");
				for(int i = 0;i < buttons.length(); i++)
				{
					JSONObject button = buttons.getJSONObject(i);
					content.append("\r\n\t\t");
					content.append(button.getString("newname"));
					content.append("/");
					content.append(button.getString("newid"));
				}
			}
			break;
		case 10:
			title.append("修改了【"+slog.getString("modulename")+"】的缺省参数");
			content.append("\r\n\t模块入口URL："+slog.getString("oldhref"));
			content.append("\t>>\t"+slog.getString("newhref"));
			content.append("\r\n\t打开方式："+slog.getString("oldtarget"));
			content.append("\t>>\t"+slog.getString("newtarget"));
			break;
		case 100:
			title.append("创建了【"+slog.getString("modulename")+"】的菜单组");
			content = new StringBuffer("初始化创建了模块子系统对应的菜单组。");
			break;
		case 1000:
			title.append("在【"+slog.getString("modulename")+"】下创建了"+slog.getString("title"));
			content.append("\r\n\t菜单组："+slog.getString("title"));
			content.append("\r\n\t目标位置："+slog.getString("targetpath"));
			break;
		case 1:
			title.append("修改了【"+slog.getString("modulename")+"】的菜单【"+slog.getString("oldname")+"】");
			if( slog.has("oldname") && slog.has("newname") && !slog.getString("oldname").equals(slog.getString("newname")) )
			{
				content.append("\r\n\t菜单名称："+slog.getString("oldname"));
				content.append("\t>>\t"+slog.getString("newname"));
			}
			if( slog.has("oldhref") && slog.has("newhref") && !slog.getString("oldhref").equals(slog.getString("newhref")) )
			{
				content.append("\r\n\t菜单URL："+slog.getString("oldhref"));
				content.append("\t>>\t"+slog.getString("newhref"));
			}

			if( slog.has("oldicon") && slog.has("newicon") && !slog.getString("oldicon").equals(slog.getString("newicon")) )
			{
				content.append("\r\n\t菜单图标："+slog.getString("oldicon"));
				content.append("\t>>\t"+slog.getString("newicon"));
			}

			if( slog.has("oldtarget") && slog.has("newtarget") && !slog.getString("oldtarget").equals(slog.getString("newtarget")) )
			{
				content.append("\r\n\t打开方式："+slog.getString("oldtarget"));
				content.append("\t>>\t"+slog.getString("newtarget"));
			}
			if( slog.has("buttons") )
			{
				content.append("\r\n\t菜单按钮：");
				JSONArray buttons = slog.getJSONArray("buttons");
				for(int i = 0;i < buttons.length(); i++)
				{
					JSONObject button = buttons.getJSONObject(i);
					String k1 = "";
					String k2 = "";
					if( button.has("oldid") )
						k1 = button.getString("oldname")+"/"+button.getString("oldid");
					if( button.has("newid") )
						k2 = button.getString("newname")+"/"+button.getString("newid");
					if( !button.has("oper") || k1.equals(k2) ) continue;
					content.append("\r\n\t\t");
					switch( button.getInt("oper") )
					{
					case 0:
						content.append("新增按钮\t");
						content.append(k2);
						break;
					case 1:
						content.append(k1);
						content.append("\t>>\t");
						content.append(k2);
						break;
					case 2:
						content.append("删除按钮\t");
						content.append(k1);
						break;
					}
				}
			}
			break;
		case 2:
			title.append("删除了【"+slog.getString("modulename")+"】的菜单【"+slog.getString("oldname")+"】");
			content.append("\r\n\t菜单名称："+slog.getString("oldname"));
			content.append("\r\n\t菜单URL："+slog.getString("oldhref"));
			content.append("\r\n该菜单下所有子菜单也被删除了。");
			break;
		case 20:
			title.append("删除了【"+slog.getString("modulename")+"】的菜单组");
			content = new StringBuffer("需要检查菜单组配置是否有用户使用，否则可禁用该模块子系统。");
			break;
		case 3://拖拽
			title.append("移动了【"+slog.getString("modulename")+"】的菜单【"+slog.getString("menuname")+"】");
			content.append("\r\n\t原位置："+slog.getString("oldpath"));
			content.append("\r\n\t新位置："+slog.getString("newpath")+" "+slog.getString("type"));
			break;
		}
//		org.directwebremoting.WebContext web = WebContextFactory.get();   
//	    javax.servlet.http.HttpServletRequest request = web.getHttpServletRequest();
		slog.put("remark", title.toString());
		zkpath = "/cos/config/modules/"+sysid+"/menus/current";//记录当前日志
		stat = zookeeper.exists(zkpath, false);
		data = zookeeper.getData(zkpath);
		String id = null;
		if( data != null )
		{
			id = new String(data);
		}
		else
		{
			id = Tools.getFormatTime("yyyyMM", System.currentTimeMillis())+"/"+Tools.getUniqueValue();
			zookeeper.create(zkpath, id.getBytes());
		}
		zkpath = "/cos/config/modules/"+sysid+"/menus/history/"+id;
		JSONObject history = zookeeper.getJSONObject(zkpath, false);
		if( history == null )
		{
			history = new JSONObject();
		}
		JSONArray log = history.has("log")?history.getJSONArray("log"):null;
		if( log == null )
		{
			log = new JSONArray();
			log.put(slog);
			history.put("log", log);
			zookeeper.createNode(zkpath, history.toString().getBytes("UTF-8"));
		}
		else
		{
			log.put(slog);
			zookeeper.setJSONObject(zkpath, history);
		}
		String gridxml = "/grid/local/modulesmenucfghistory.xml";
		gridxml = Kit.chr2Unicode(gridxml);
		super.sendNotiefiesToSystemadmin(
				"系统配置",
				"用户【"+super.getAccountName()+"】"+title.toString(), 
				content.toString(),
				"digg!query.action?sysid="+sysid+"&id="+id+"&gridxml="+gridxml,
				"菜单发布审批",
				"menus!publish.action?sysid="+sysid+"&id"+id);

		if( super.getAccountRole() != 1 )
		{
		    Sysnotify notify = new Sysnotify();
			notify.setUseraccount(super.getAccountName());
			notify.setFilter("系统配置");
			notify.setTitle("我"+title.toString());
			notify.setPriority(10);
			notify.setNotifytime(new Date());
			notify.setContext(content.toString());
			notify.setContextlink("digg!query.action?sysid="+sysid+"&id="+id+"&gridxml="+gridxml);
			notify.setAction("问题反馈");
			notify.setActionlink("#feedback");
			SysnotifyClient.send(notify);
		}
		rsp.setMessage(logs);
		rsp.setSucceed(true);
	}
	/**
	 * 添加模块子系统配置菜单
	 * @param path
	 * @param timestamp
	 * @param moduleid
	 * @return
	 */
	public synchronized AjaxResult<String> addDefaultMenu(
			String title,
			boolean hasdir,
			int path[],
			long timestamp,
			String sysid,
			String sysname)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		try
		{
			Zookeeper zookeeper = ZKMgr.getZookeeper();
			InputStream is = preSet(rsp, sysid, zookeeper, timestamp);
			if( is == null ) return rsp;
			XMLParser xml = new XMLParser(is);
			Element root = XMLParser.getChildElementByPath(xml.getRootNode(), path);
			if( root == null )
			{
				String s = "未找到您要添加菜单的上级菜单目录。";
				rsp.setMessage(s);
				rsp.setTimestamp(ModulesVersionTimestamp);
				JSONArray zNodes = new JSONArray();
				loadMenus(zNodes, sysid, is);
				rsp.setResult(zNodes.toString());
				return rsp;
			}
			Element parent = null;
			boolean sysmenu = "系统管理缺省权限菜单".equals(title);
			hasdir = sysmenu?true:hasdir;
			Element menu = null;
			if( "系统维护菜单组".equals(title) || sysmenu )
			{
				parent = root;
				if( hasdir )
				{
					menu = xml.createElement("menu");
		        	menu.setAttribute("name", "系统维护");
		        	menu.setAttribute("icon", "fa-server");
		        	menu.setAttribute("href", "#monitor");
		        	parent.appendChild(menu);
		        	parent = menu;
				}
				menu = xml.createElement("menu");
	        	menu.setAttribute("name", "系统监控");
	        	menu.setAttribute("icon", "fa-eye");
	        	menu.setAttribute("href", "monitor!navigate.action?u=1");
	        	parent.appendChild(menu);
	        	menu = xml.createElement("menu");
	        	menu.setAttribute("name", "系统日志");
	        	menu.setAttribute("icon", "fa-history");
	        	menu.setAttribute("href", "syslog!query.action");
	        	parent.appendChild(menu);
	        	menu = xml.createElement("menu");
	        	menu.setAttribute("name", "系统邮箱");
	        	menu.setAttribute("icon", "fa-envelope-o");
	        	menu.setAttribute("href", "email!outbox.action");
	        	parent.appendChild(menu);
	        	menu = xml.createElement("menu");
	        	menu.setAttribute("name", "实时告警");
	        	menu.setAttribute("icon", "fa-exclamation-triangle");
	        	menu.setAttribute("href", "sysalarm!manager.action");
	        	parent.appendChild(menu);
//	        	menu = xml.createElement("menu");
//	        	menu.setAttribute("name", "历史告警");
//	        	menu.setAttribute("href", "sysalarm!query.action?type=history");
//	        	parent.appendChild(menu);
//	        	menu = xml.createElement("menu");
//	        	menu.setAttribute("name", "JDBC数据库监控配置");
//	        	menu.setAttribute("href", "monitorcfg!databases.action");
//	        	parent.appendChild(menu);
			}
			if( "系统配置菜单组".equals(title) || sysmenu )
			{
				parent = root;
				if( hasdir )
				{
					menu = xml.createElement("menu");
		        	menu.setAttribute("name", "系统配置");
		        	menu.setAttribute("icon", "fa-cogs");
		        	menu.setAttribute("href", "#"+Tools.getUniqueValue());
		        	parent.appendChild(menu);
		        	parent = menu;
				}
	        	menu = xml.createElement("menu");
	        	menu.setAttribute("name", "系统参数配置");
	        	menu.setAttribute("icon", "fa-cog");
	        	menu.setAttribute("href", "syscfg!preset.action");
	        	parent.appendChild(menu);
	        	menu = xml.createElement("menu");
	        	menu.setAttribute("name", "系统告警配置");
	        	menu.setAttribute("icon", "fa-bell-o");
	        	menu.setAttribute("href", "sysalarm!config.action");
	        	parent.appendChild(menu);
	        	menu = xml.createElement("menu");
	        	menu.setAttribute("name", "系统消息配置");
	        	menu.setAttribute("icon", "fa-envelope-square");
	        	menu.setAttribute("href", "notify!config.action");
	        	parent.appendChild(menu);
//	        	menu = xml.createElement("menu");
//	        	menu.setAttribute("name", "系统公告管理");
//	        	menu.setAttribute("href", "notice!query.action");
//	        	parent.appendChild(menu);
	        	menu = xml.createElement("menu");
	        	menu.setAttribute("name", "系统程序管理");
	        	menu.setAttribute("icon", "fa-windows");
	        	menu.setAttribute("href", "control!navigate.action");
	        	parent.appendChild(menu);
	        	menu = xml.createElement("menu");
	        	menu.setAttribute("name", "系统主数据库管理");
	        	menu.setAttribute("icon", "fa-database");
	        	menu.setAttribute("href", "syscfg!database.action");
	        	parent.appendChild(menu);
			}
			if( "权限管理菜单组".equals(title) || sysmenu )
			{
				parent = root;
				if( hasdir )
				{
					menu = xml.createElement("menu");
		        	menu.setAttribute("name", "权限管理");
		        	menu.setAttribute("icon", "fa-group");
		        	menu.setAttribute("href", "#"+Tools.getUniqueValue());
		        	parent.appendChild(menu);
		        	parent = menu;
				}
	        	menu = xml.createElement("menu");
	        	menu.setAttribute("name", "角色权限管理");
	        	menu.setAttribute("icon", "fa-user-circle");
	        	menu.setAttribute("href", "role!manager.action");
	        	parent.appendChild(menu);

	        	menu = xml.createElement("menu");
	        	menu.setAttribute("name", "用户权限管理");
	        	menu.setAttribute("icon", "fa-user-circle-o");
	        	menu.setAttribute("href", "user!manager.action");
	        	parent.appendChild(menu);

	        	menu = xml.createElement("menu");
	        	menu.setAttribute("name", "用户查询");
	        	menu.setAttribute("icon", "fa-vcard");
	        	menu.setAttribute("href", "user!query.action");
	        	parent.appendChild(menu);
			}
			if( "集群SSH管理菜单".equals(title) || sysmenu )
			{
				parent = root;
				menu = xml.createElement("menu");
	        	menu.setAttribute("name", "集群SSH管理");
	        	menu.setAttribute("icon", "fa-terminal");
	        	menu.setAttribute("href", "rpc!navigate.action");
	        	parent.appendChild(menu);
			}
			if( "系统接口菜单组".equals(title) || sysmenu )
			{
				parent = root;
				if( hasdir )
				{
					menu = xml.createElement("menu");
		        	menu.setAttribute("name", "接口管理");
		        	menu.setAttribute("icon", "fa-assistive-listening-systems");
		        	menu.setAttribute("href", "#"+Tools.getUniqueValue());
		        	parent.appendChild(menu);
		        	parent = menu;
				}
        		String[][] values = new String[][]{
        		{"_sysuser", "系统用户接口"},
        		{"_syslog", "系统日志接口"},
        		{"_sysalarm", "系统告警接口"},
        		{"_sysnotify", "系统通知接口"},
        		{"_sysemail", "系统邮件接口"},
        		{"_programpublish", "系统程序发布接口"},
        		{"_diggapi", "元数据模板查询接口"},
        		{"_reportmonitor", "伺服模块监控上报接口"},
        		{"_configmonitor", "伺服模块监控配置接口"},
        		{"_synchfiles", "集群文件同步拷贝接口"}
        		};
        		for(int i = 0; i < values.length; i++){
        			String account = values[i][0];
        			String name = values[i][1];
        			JSONObject e = ZKMgr.getZookeeper().getJSONObject("/cos/config/security/"+account);
					if( e == null ) continue;
        			menu = xml.createElement("menu");
		        	menu.setAttribute("name", name);
		        	menu.setAttribute("href", "security!openttest.action?id="+e.getString("account")+"&datatype="+e.getString("type"));
		        	if( "griddigg".equals(e.getString("type")) ){
    		        	menu.setAttribute("href", "diggcfg!api.action?sysid=Sys");
		        	}
		        	parent.appendChild(menu);
	        	}
			}
			/*if( "元数据管理菜单组".equals(title) || sysmenu )
			{
				parent = root;
				if( hasdir )
				{
					menu = xml.createElement("menu");
		        	menu.setAttribute("name", "元数据管理");
		        	menu.setAttribute("icon", "fa-database");
		        	menu.setAttribute("href", "#"+Tools.getUniqueValue());
		        	parent.appendChild(menu);
		        	parent = menu;
				}
	        	menu = xml.createElement("menu");
	        	menu.setAttribute("name", "数据源配置");
	        	menu.setAttribute("href", "datacfg!metadata.action");
	        	parent.appendChild(menu);
	        	menu = xml.createElement("menu");
	        	menu.setAttribute("name", "元数据查询配置");
	        	menu.setAttribute("href", "datacfg!setquery.action");
	        	parent.appendChild(menu);
			}
			*/
			if( title.startsWith("微信管理菜单组:") )
			{
				String weixinno = title.substring("微信管理菜单组:".length());
				JSONObject obj = ZKMgr.getZookeeper().getJSONObject("/cos/config/modules/"+sysid+"/weixin/"+weixinno, true);
				parent = root;
				if( hasdir )
				{
					menu = xml.createElement("menu");
		        	menu.setAttribute("name", "【"+obj.getString("name")+"】管理");
		        	menu.setAttribute("icon", "fa-weixin");
		        	menu.setAttribute("href", "#"+Tools.getUniqueValue());
		        	parent.appendChild(menu);
		        	parent = menu;
				}
	        	menu = xml.createElement("menu");
	        	menu.setAttribute("name", "应用总览");
	        	menu.setAttribute("href", "weixin!summary.action?sysid="+sysid+"&id="+weixinno);
	        	parent.appendChild(menu);
	        	menu = xml.createElement("menu");
	        	menu.setAttribute("name", "用户管理");
	        	menu.setAttribute("href", "weixin!users.action?sysid="+sysid+"&id="+weixinno);
	        	parent.appendChild(menu);
	        	menu = xml.createElement("menu");
	        	menu.setAttribute("name", "菜单配置");
	        	menu.setAttribute("href", "weixin!presetmenu.action?sysid="+sysid+"&id="+weixinno);
	        	parent.appendChild(menu);
	        	menu = xml.createElement("menu");
	        	menu.setAttribute("name", "事件查询");
	        	menu.setAttribute("href", "weixin!callbackquery.action?sysid="+sysid+"&id="+weixinno);
	        	parent.appendChild(menu);
	        	menu = xml.createElement("menu");
	        	menu.setAttribute("name", "用户统计");
	        	menu.setAttribute("href", "weixin!griddigg.action?sysid="+sysid+"&id="+weixinno+"&remark=weixinusercumulate");
	        	parent.appendChild(menu);
	        	menu = xml.createElement("menu");
	        	menu.setAttribute("name", "群发统计");
	        	menu.setAttribute("href", "weixin!griddigg.action?sysid="+sysid+"&id="+weixinno+"&remark=weixinarticles");
	        	parent.appendChild(menu);
//	        	menu = xml.createElement("menu");
//	        	menu.setAttribute("name", "对话管理");
//	        	menu.setAttribute("href", "weixin!chat.action?sysid="+sysid+"&id="+weixinno);
//	        	parent.appendChild(menu);
			}
			String s = "成功设置"+title+"，需系统管理员审核发布生效。";

			JSONObject slog = new JSONObject();
			slog.put("time", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()));
			slog.put("moduleid", sysid);
			slog.put("modulename", sysname);
			slog.put("oper", 1000);
			slog.put("targetpath", this.getPathRemark(root));
			slog.put("title", title);
			this.set(rsp, sysid, xml, zookeeper, true, s, slog);
		}
		catch(Exception e)
		{
			String s = "设置模块子系统配置菜单组出现异常"+e;
			logoper(s, "系统配置", e);
			rsp.setMessage(s);
			log.error("Failed to set the node of modules for exception", e);
		}
		return rsp;
	}
	
	/**
	 * 添加不存在的节点
	 * @param type
	 * @param id
	 * @param timestamp
	 * @param moduleid
	 * @return
	 */
	public synchronized AjaxResult<String> addModulesNode(
			String type,
			long timestamp,
			String moduleid,
			String modulename)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		try
		{
			rsp.setTimestamp(ModulesVersionTimestamp);
			if( type == null || type.isEmpty() )
			{
				String s = "未知操作";
				rsp.setMessage(s);
				return rsp;
			}
			Zookeeper zookeeper = ZKMgr.getZookeeper();
			InputStream is = preSet(rsp, "toolbar".equals(type)?type:moduleid, zookeeper, timestamp);
			if( is == null ) return rsp;
			XMLParser xml = new XMLParser(is);
			Element node = null;
			if( type.equals("toolbar") )
			{
				node = XMLParser.getChildElementByTag(xml.getRootNode(), "toolbar");
			}
			else
			{
				node = XMLParser.findChildElementByTagAndAttr(xml.getRootNode(), "module", "id", moduleid);
			}
//			if( node != null )
//			{
//				String s = "该模块子系统菜单配置在"+Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", ModulesVersionTimestamp)+"被其它用户所设置。";
//				rsp.setMessage(s);
//				rsp.setTimestamp(ModulesVersionTimestamp);
//				JSONArray zNodes = new JSONArray();
//				loadMenus(zNodes, moduleid, is);
//				rsp.setResult(zNodes.toString());
//				return rsp;
//			}
			if( node == null ){
				node = xml.createElement(type);
				if( type.equals("module"))
				{
					node.setAttribute("id", moduleid);
				}
				xml.getRootElement().appendChild(node);	
			}

			JSONObject slog = new JSONObject();
			slog.put("time", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()));
			slog.put("moduleid", moduleid);
			slog.put("modulename", modulename);
			slog.put("oper", 100);
			String s = "成功设置模块菜单组"+type+"节点，需系统管理员审核发布生效。";
			this.set(rsp, moduleid, xml, zookeeper, true, s, slog);
		}
		catch(Exception e)
		{
			String s = "设置模块菜单组"+type+"节点出现异常"+e;
			logoper(s, "系统配置", e);
			rsp.setMessage(s);
			log.error("Failed to set the node of modules for exception", e);
		}
		return rsp;
	}
	/**
	 * 设置模块子系统缺省的入口网址
	 * @param id
	 * @param href
	 * @param target
	 * @param timestamp
	 * @param moduleid
	 * @return
	 */
	public synchronized AjaxResult<String> setModuleDefault(
			String href,
			String target,
			long timestamp,
			String moduleid,
			String modulename)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		try
		{
			rsp.setTimestamp(ModulesVersionTimestamp);
			if( moduleid == null || moduleid.isEmpty() )
			{
				String s = "未知模块。";
				rsp.setMessage(s);
				logoper(LogSeverity.ERROR, "设置模块子系统缺省参数失败，因为"+s, "系统配置");
				return rsp;
			}

			Zookeeper zookeeper = ZKMgr.getZookeeper();
			InputStream is = preSet(rsp, moduleid, zookeeper, timestamp);
			if( is == null ) return rsp;
			XMLParser xml = new XMLParser(is);
			
			Element module = XMLParser.findChildElementByTagAndAttr(xml.getRootNode(), "module", "id", moduleid);
			if( module == null )
			{
				String s = "未找到您要设置的模块子系统菜单配置项。";
				logoper(LogSeverity.ERROR, "设置模块子系统菜单失败，因为"+s, "系统配置");
				rsp.setMessage(s);
				rsp.setTimestamp(ModulesVersionTimestamp);
				JSONArray zNodes = new JSONArray();
				loadMenus(zNodes, moduleid, xml);
				rsp.setResult(zNodes.toString());
			}
			JSONObject slog = new JSONObject();
			slog.put("time", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()));
			slog.put("moduleid", moduleid);
			slog.put("modulename", modulename);
			slog.put("oper", 10);
			slog.put("oldhref", XMLParser.getElementAttr(module, "default"));
			slog.put("oldtarget", XMLParser.getElementAttr(module, "target"));
			slog.put("newhref", href);
			slog.put("newtarget", target);
			module.setAttribute("default", href);
			if( target != null && !target.isEmpty() ) module.setAttribute("target", target);

			String s = "成功设置模块子系统缺省参数配置，需系统管理员审核发布生效。";
			this.set(rsp, moduleid, xml, zookeeper, false, s, slog);
		}
		catch(Exception e)
		{
			String s = "设置模块子系统缺省参数配置出现异常"+e;
			logoper(s, "系统配置", e);
			rsp.setMessage(s);
			log.error("Failed to set the menu of modules for exception", e);
		}
		return rsp;
	}
	/**
	 * 删除模块菜单节点包括toolbar
	 * @param type
	 * @param id
	 * @param timestamp
	 * @param moduleid
	 * @return
	 */
	public synchronized AjaxResult<String> delModulesNode(
			long timestamp,
			String moduleid,
			String modulename)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		try
		{
			if( moduleid == null || moduleid.isEmpty() )
			{
				String s = "未知操作";
				rsp.setMessage(s);
				return rsp;
			}
			Zookeeper zookeeper = ZKMgr.getZookeeper();
			InputStream is = preSet(rsp, moduleid, zookeeper, timestamp);
			if( is == null ) return rsp;
			XMLParser xml = new XMLParser(is);
			Node node = null;
			if( moduleid.equals("toolbar") )
			{
				node = XMLParser.getChildElementByTag(xml.getRootNode(), "toolbar");
			}
			else
			{
				node = XMLParser.findChildElementByTagAndAttr(xml.getRootNode(), "module", "id", moduleid);
			}
			if( node == null )
			{
				String s = "未找到您要删除的模块菜单组配置项。";
				logoper(LogSeverity.ERROR, "删除模块菜单组失败，因为"+s, "系统配置");
				rsp.setMessage(s);
				rsp.setTimestamp(ModulesVersionTimestamp);
				JSONArray zNodes = new JSONArray();
				loadMenus(zNodes, moduleid, xml);
				rsp.setResult(zNodes.toString());
				return rsp;
			}
			xml.getRootNode().removeChild(node);

			JSONObject slog = new JSONObject();
			slog.put("time", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()));
			slog.put("moduleid", moduleid);
			slog.put("modulename", modulename);
			slog.put("oper", 20);
			
			String s = "删除模块菜单组【"+modulename+"】，需系统管理员审核发布生效。";
			this.set(rsp, moduleid, xml, zookeeper, true, s, slog);
		}
		catch(Exception e)
		{
			String s = "删除模块菜单组出现异常"+e;
			logoper(s, "系统配置", e);
			rsp.setMessage(s);
			log.error("Failed to delete the module of "+moduleid+" of modules for exception", e);
		}
		return rsp;
	}

    /**
     * 查找所有满足条件的节点
     * @param element
     * @param tag
     * @param attr
     * @param value
     * @param buffer
     */
    private void chkModulesMenu( Node element, HashMap<String, Element> memory, ArrayList<Element> buffer )
    {
        for( Node node = element.getFirstChild(); node != null; node = node.getNextSibling() )
        {
            if( node.getNodeType() == Node.ELEMENT_NODE )
            {
                //判断节点名称是否和输入的标签名称相同,如果相同就返回该节点
                if( node.getNodeName().equalsIgnoreCase( "menu" ) )
                {
                    Element e = (Element)node;
                    String href = e.getAttribute("href");
                    if( memory.containsKey(href) ){
                    	buffer.add(memory.get(href));
                    	buffer.add(e);
                    }
                    else{
                    	memory.put(href, e);
                    }
                    chkModulesMenu(e, memory, buffer);
                }
                else if( node.getNodeName().equalsIgnoreCase( "module" ) )
                {
                	chkModulesMenu(node, memory, buffer);
                }
            }
        }
    }
	/**
	 * 检查指定模块的重复标识和URL的情况
	 * @param moduleid
	 * @return
	 */
	public AjaxResult<String> chkModulesMenu(String moduleid){
		AjaxResult<String> rsp = new AjaxResult<String>();
		try
		{
			Zookeeper zookeeper = ZKMgr.getZookeeper();
			InputStream is = preSet(rsp, moduleid, zookeeper, timestamp);
			if( is == null ) return rsp;
			XMLParser xml = new XMLParser(is);
			ArrayList<Element> buffer = new ArrayList<Element>();
			HashMap<String, Element> memory = new HashMap<String, Element>();
			chkModulesMenu(xml.getRootNode(), memory, buffer);
			if( buffer.isEmpty() )
			{
				rsp.setMessage("未发现菜单配置中有重复的目录标识或者URL");
			}
			else{
				StringBuffer sb = new StringBuffer();
				JSONArray data = new JSONArray();
				for(Element e : buffer){
					String name = XMLParser.getElementAttr(e, "name");
					sb.append(",");
					sb.append(name);
					JSONObject o = new JSONObject();
					o.put("href", XMLParser.getElementAttr(e, "href"));
					data.put(o);
				}
//				Iterator<String> iterator = memory.keySet().iterator();
//				while(iterator.hasNext()){
//					JSONObject o = new JSONObject();
//					o.put("href", iterator.next());
//					data.put(o);
//				}
				rsp.setMessage("发现菜单配置中有重复的目录标识或者URL:"+sb.substring(1));
				rsp.setSucceed(true);
				rsp.setResult(data.toString());
			}
		}
		catch(Exception e)
		{
			String s = "检查模块子系统菜单出现异常"+e;
			rsp.setMessage(s);
			log.error("Failed to check the menu of modules for exception", e);
		}
		return rsp;
	}
	/**
	 * 删除指定路径的模块子系统菜单
	 * @param path
	 * @return
	 */
	public synchronized AjaxResult<String> delModulesMenu(
			int path[],
			long timestamp,
			String moduleid,
			String modulename)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		try
		{
			Zookeeper zookeeper = ZKMgr.getZookeeper();
			InputStream is = preSet(rsp, moduleid, zookeeper, timestamp);
			if( is == null ) return rsp;
			XMLParser xml = new XMLParser(is);
			Element node = XMLParser.getChildElementByPath(xml.getRootNode(), path);
			if( node == null )
			{
				String s = "未找到您要删除的模块子系统菜单配置项。";
				logoper(LogSeverity.ERROR, "删除模块子系统菜单失败，因为"+s, "系统配置");
				rsp.setMessage(s);
				rsp.setTimestamp(ModulesVersionTimestamp);
				JSONArray zNodes = new JSONArray();
				loadMenus(zNodes, moduleid, xml);
				rsp.setResult(zNodes.toString());
				return rsp;
			}
			String name = XMLParser.getElementAttr(node, "name");
			String href = XMLParser.getElementAttr(node, "href");

			JSONObject slog = new JSONObject();
			slog.put("time", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()));
			slog.put("moduleid", moduleid);
			slog.put("modulename", modulename);
			slog.put("oper", 2);
			slog.put("oldname", name);
			slog.put("oldhref", href);
			slog.put("oldpath", this.getPathRemark(node));
			
			node.getParentNode().removeChild(node);
			String s = "删除模块子系统菜单【"+slog.getString("oldpath")+"】成功。";
			this.set(rsp, moduleid, xml, zookeeper, false, s, slog);
		}
		catch(Exception e)
		{
			String s = "删除模块子系统菜单出现异常"+e;
			logoper(s, "系统配置", e);
			rsp.setMessage(s);
			log.error("Failed to delete the menu of modules for exception", e);
		}
		return rsp;
	}

	/**
	 * 得到路径描述
	 * @return
	 */
	private String getPathRemark(Node e)
	{
		if( e == null ) return "";
		String name = XMLParser.getElementAttr(e, "name");
		if( name.isEmpty() ){
			String id = XMLParser.getElementAttr(e, "id");
			if( id.isEmpty() )
			{
				if( "toolbar".equals(e.getNodeName()) )
					return "系统工具栏";
			}
			else
			{
				JSONObject config = ModulesMgr.getConfig(id);
				if( config != null ) return config.getString("SysName");
			}
			return "";
		}
		return getPathRemark(e.getParentNode())+" >> "+name;
		
	}
	/**
	 * 拖动菜单
	 * @param type
	 * @param pathSource
	 * @param pathTarget
	 * @param timestamp
	 * @param moduleid
	 * @return
	 */
	public synchronized AjaxResult<String> dragDropMenu(
			String type,
			int pathSource[],
			int pathTarget[],
			long timestamp,
			String moduleid,
			String modulename)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		try
		{
			Zookeeper zookeeper = ZKMgr.getZookeeper();
			InputStream is = preSet(rsp, moduleid, zookeeper, timestamp);
			if( is == null ) return rsp;
			XMLParser xml = new XMLParser(is);
			
			Node source = XMLParser.getChildElementByPath(xml.getRootNode(), pathSource);
			Node target = XMLParser.getChildElementByPath(xml.getRootNode(), pathTarget);
			if( source == null || target == null )
			{
				String s = "未找到您要拖拽的模块子系统菜单配置项。";
				logoper(LogSeverity.ERROR, "拖拽模块子系统菜单失败，因为"+s, "系统配置");
				rsp.setMessage(s);
				rsp.setTimestamp(ModulesVersionTimestamp);
				JSONArray zNodes = new JSONArray();
				loadMenus(zNodes, moduleid, xml);
				rsp.setResult(zNodes.toString());
				return rsp;
			}
			JSONObject slog = new JSONObject();
			slog.put("oldpath", getPathRemark(source));
			slog.put("newpath", getPathRemark(target));
			Node parentSource = source.getParentNode();
			Node parentTarget = target.getParentNode();
			String name = XMLParser.getElementAttr(source, "name");
			slog.put("menuname", name);
			source = parentSource.removeChild(source);
			if( "inner".equals(type) )
			{
				target.appendChild(source);
				slog.put("type", "之下");
			}
			else if( "menu".equalsIgnoreCase(target.getNodeName()) )
			{
				if( "prev".equals(type) )
				{
					parentTarget.insertBefore(source, target);
					slog.put("type", "之前");
				}
				else if( "next".equals(type) )
				{
					target = XMLParser.nextSibling(target);
			    	if( target != null )
			    	{
						slog.put("type", "之后");
			    		parentTarget.insertBefore(source, target);
			    	}
			    	else
			    	{
						slog.put("type", "之后");
			    		parentTarget.appendChild(source);
			    	}
				}
			}
			else
			{
				String s = "菜单不能拖拽到模块子系统之上。";
				logoper(LogSeverity.ERROR, "拖拽模块子系统菜单失败，因为"+s, "系统配置");
				rsp.setMessage(s);
				rsp.setTimestamp(ModulesVersionTimestamp);
				JSONArray zNodes = new JSONArray();
				loadMenus(zNodes, moduleid, xml);
				rsp.setResult(zNodes.toString());
				return rsp;
			}

			slog.put("time", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()));
			slog.put("moduleid", moduleid);
			slog.put("modulename", modulename);
			slog.put("oper", 3);
			this.set(rsp, moduleid, xml, zookeeper, false, "拖拽模块子系统菜单【"+name+"】成功，需系统管理员审核发布生效。", slog);
		}
		catch(Exception e)
		{
			String s = "拖拽模块子系统菜单出现异常"+e;
			logoper(s, "系统配置", e);
			rsp.setMessage(s);
			log.error("Failed to drag&drop the menu of modules for exception", e);
		}
		return rsp;		
	}
	/**
	 * 设置模块子系统菜单的配置
	 * @param newitem 是新节点还是修改节点
	 * @param path XML的路径偏移量，根据它可以定位到指定节点
	 * @param name
	 * @param href
	 * @param icon
	 * @param target
	 * @param buttons
	 * @param module
	 * @param timestamp
	 * @param moduleonly 唯一模块
	 * @param moduleid
	 * @param modulename
	 * @return
	 */
	public synchronized AjaxResult<String> setModulesMenu(
			boolean newitem, 
			int path[],
			String name,
			String href,
			String icon,
			String target,
			String buttons[],
			long timestamp,
			String moduleid,
			String modulename)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		try
		{
			if( name == null || name.isEmpty() )
			{
				String s = "菜单名称和菜单URL未正确设置不允许修改模块子系统菜单。";
				logoper(LogSeverity.ERROR, "设置模块子系统菜单失败，因为"+s, "系统配置");
				rsp.setMessage(s);
				return rsp;
			}
			Zookeeper zookeeper = ZKMgr.getZookeeper();
			InputStream is = preSet(rsp, moduleid, zookeeper, timestamp);
			if( is == null ) return rsp;
			XMLParser xml = new XMLParser(is);
//			System.err.println(XMLParser.getElementAttr(xml.getRootNode(), "name"));
			Element root = XMLParser.findChildElementByTagAndAttr(xml.getRootNode(), "module", "id", moduleid);
			if( root == null ){
				String s = String.format("未发现模块子系统【%s:%s】配置。", modulename, moduleid);
				rsp.setMessage(s);
				return rsp;
			}
			Element node = XMLParser.getChildElementByPath(xml.getRootNode(), path);
			if( node == null )
			{
				String s = "未找到您要设置的模块子系统菜单配置项。";
				logoper(LogSeverity.ERROR, "设置模块子系统菜单失败，因为"+s, "系统配置");
				rsp.setMessage(s);
				rsp.setTimestamp(ModulesVersionTimestamp);
				JSONArray zNodes = new JSONArray();
				loadMenus(zNodes, moduleid, xml);
				rsp.setResult(zNodes.toString());
				return rsp;
			}
			if( href == null || href.isEmpty() || href.equals("#") )
			{
				href = "#"+Tools.getUniqueValue();
			}
			Element menu = node;
			if( newitem )
			{
//				if( href.charAt(0) == '#' && XMLParser.findChildElementByTagAndAttr(menu, "menu", "href", href) != null )
				ArrayList<Element> buffer = new ArrayList<Element>();
				XMLParser.findAllChildElementByTagAndAttr(root, "menu", "href", href, buffer);
				if( !buffer.isEmpty() )
				{
					String menuname = XMLParser.getElementAttr(buffer.get(0), "name");
					String s = String.format("新增的模块子系统菜单目录的唯一标识或URL[%s]已经被其它菜单【%s】所使用，为了区别不同菜单目录请重新输入其它标识或URL。", href, menuname);
					rsp.setMessage(s);
					return rsp;
				}
				menu = xml.createElement("menu");
				node.appendChild(menu);
			}
			/*else if( href.charAt(0) == '#' )
			{
				Element old = XMLParser.findChildElementByTagAndAttr(menu.getParentNode(), "menu", "href", href);
				if( old != null && old != menu )
				{
					String s = "模块子系统菜单目录唯一标识已存在，为了区别不同目录请重新输入其它标识。";
					rsp.setMessage(s);
					return rsp;
				}
			}*/
			else{
				ArrayList<Element> buffer = new ArrayList<Element>();
				XMLParser.findAllChildElementByTagAndAttr(root, "menu", "href", href, buffer);
				for(Element old : buffer ){
					if( old != null && old != menu )
					{
						String menuname = XMLParser.getElementAttr(old, "name");
						String s = String.format("模块子系统菜单目录的唯一标识或URL[%s]已经被其它菜单【%s】所使用，为了区别不同菜单目录请重新输入其它标识或URL。", href, menuname);
						rsp.setMessage(s);
						return rsp;
					}
				}
			}
			JSONObject slog = new JSONObject();
			slog.put("time", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()));
			slog.put("moduleid", moduleid);
			slog.put("modulename", modulename);
			slog.put("oper", newitem?0:1);
			if( !newitem )
			{
				slog.put("oldname", XMLParser.getElementAttr(menu, "name"));
				slog.put("oldhref", XMLParser.getElementAttr(menu, "href"));
				slog.put("oldicon", XMLParser.getElementAttr(menu, "icon"));
				slog.put("oldtarget", XMLParser.getElementAttr(menu, "target"));
			}
			slog.put("newname", name);
			slog.put("newhref", href);
			slog.put("newicon", icon);
			slog.put("newtarget", target);
			menu.setAttribute("name", name);
			menu.setAttribute("href", href);
			if( icon != null && !icon.isEmpty() )
			{
				menu.setAttribute("icon", icon);
			}
			else
			{
				menu.removeAttribute("icon");
			}
			if( target != null && !target.isEmpty() )
			{
				menu.setAttribute("target", target);
			}
			else
			{
				menu.removeAttribute("target");
			}
			Element buttonNode = XMLParser.getChildElementByTag( menu, "button" ); 
			HashMap<String, Element> buttonMap = new HashMap<String, Element>();
			if( buttonNode != null )
			{
		        while( buttonNode != null )
		        {
		        	String id = XMLParser.getElementAttr(buttonNode, "id");
		        	if( id.isEmpty() ) continue;
		        	if( buttonMap.containsKey(id) )
		        	{
						Node r = buttonNode;
						buttonNode = XMLParser.nextSibling(buttonNode);
						menu.removeChild(r);
		        		continue;
		        	}
		        	buttonMap.put(id, buttonNode);
		        	buttonNode = XMLParser.nextSibling(buttonNode);
		        }
			}
			JSONArray btnlogs = new JSONArray();
			HashMap<String, Element> buttonMap1 = new HashMap<String, Element>();
			if( buttons != null && buttons.length > 0 )
			{
				for( String btn : buttons )
				{
					String args[] = Tools.split(btn, " ");
					if( args.length != 2 ) continue;
					String btnName = args[0];
					String btnId = args[1];
					if( buttonMap1.containsKey(btnId) )
					{
						continue;
					}
					buttonNode = xml.createElement("button");
					buttonMap1.put(btnId, buttonNode);
					menu.appendChild(buttonNode);
					buttonNode.setAttribute("name", btnName);
					buttonNode.setAttribute("id", btnId);

					buttonNode = buttonMap.get(btnId);
					if( buttonNode == null )
					{
						JSONObject blog = new JSONObject();
						blog.put("newname", btnName);
						blog.put("newid", btnId);
						blog.put("oper", 0);
						btnlogs.put(blog);
					}
					else
					{
						String btnName0 = XMLParser.getElementAttr(buttonNode, "name");
						String btnId0 = XMLParser.getElementAttr(buttonNode, "name");
						if( !btnName0.equals(btnName) || !btnId0.equals(btnId0) )
						{
							JSONObject blog = new JSONObject();
							blog.put("newname", btnName);
							blog.put("newid", btnId);
							blog.put("oper", 1);
							blog.put("oldname", btnName0);
							blog.put("oldid", btnId0);
							btnlogs.put(blog);
						}
					}
				}
			}
			
			if( !buttonMap.isEmpty() )
			{
				Iterator<Element> iterator = buttonMap.values().iterator();
				while(iterator.hasNext())
				{
					buttonNode = iterator.next();
					String id = XMLParser.getElementAttr(buttonNode, "id");
					if( !buttonMap1.containsKey(id) )
					{
						JSONObject blog = new JSONObject();
						blog.put("oper", 2);
						blog.put("oldname", XMLParser.getElementAttr(buttonNode, "name"));
						blog.put("oldid", XMLParser.getElementAttr(buttonNode, "id"));
						btnlogs.put(blog);
					}
					menu.removeChild(buttonNode);
				}
			}

			if( btnlogs.length() > 0 )
				slog.put("buttons", btnlogs);
			
			String s = newitem?("新增模块子系统菜单【"+name+"】成功，需系统管理员审核发布生效。"):
				("设置模块子系统菜单【"+name+"】成功，需系统管理员审核发布生效。");
			rsp.setResult(href);
			this.set(rsp, moduleid, xml, zookeeper, false, s, slog);
		}
		catch(Exception e)
		{
			String s = "设置模块子系统菜单出现异常"+e;
			logoper(s, "系统配置", e);
			rsp.setMessage(s);
			log.error("Failed to set the menu of modules for exception", e);
		}
		return rsp;
	}
	
	/**
	 * 
	 * @param menu
	 * @param parent
	private static void setMenuModuleInfo(JSONObject menu, JSONObject parent)
	{
		if( parent.has("moduleId") ) menu.put("moduleId", parent.getString("moduleId") );
		if( parent.has("moduleName") ) menu.put("moduleName", parent.getString("moduleName"));
		menu.put("POP3Username", parent.has("POP3Username")?parent.getString("POP3Username"):"");
		menu.put("CmpPortal", parent.has("CmpPortal")?parent.getString("CmpPortal"):"");
		menu.put("CmpDevelopersEmail", parent.has("CmpDevelopersEmail")?parent.getString("CmpDevelopersEmail"):"");
		menu.put("CmpDevelopersContact", parent.has("CmpDevelopersContact")?parent.getString("CmpDevelopersContact"):"");
		menu.put("CmpDevelopers", parent.has("CmpDevelopers")?parent.getString("CmpDevelopers"):"");
	}
	 */

	/**
	 * 加载菜单项
	 * @param parentNode
	 * @param parent
	 * @param index 位置索引编号
	 */
	private static void loadMenus(Node parentNode, JSONObject parent)
	{
		boolean debug = false;
//		String tag = PathFactory.getWebappPath().getAbsolutePath().toLowerCase();
//		boolean debug = tag.startsWith("d:\\focusnt\\cos\\trunk\\ide") ||
//			tag.startsWith("d:\\focusnt\\report\\trunk\\ide");
		JSONArray menus = new JSONArray();
		Node menuNode = XMLParser.getChildElementByTag( parentNode, "menu" ); 
        for( int i = 0; menuNode != null; menuNode = XMLParser.nextSibling(menuNode), i++ )
        {
        	String name = XMLParser.getElementAttr(menuNode, "name");
        	String href = XMLParser.getElementAttr(menuNode, "href");
        	if( name.isEmpty() || href.isEmpty() ){
        		System.err.println(i+":"+name+", "+href);
        		continue;
        	}
        	String target = XMLParser.getElementAttr(menuNode, "target");
        	String icon = XMLParser.getElementAttr(menuNode, "icon");
//			String mid = parent.has("moduleId")?parent.getString("moduleId"):"";
//        	if( mid.equals("huayunyy") )
//    		{
//    		}
        	if( icon.isEmpty() ) icon = XMLParser.getElementAttr(menuNode, "fa");
        	else if( icon.endsWith(".gif") ) icon = "";

			JSONObject menu = new JSONObject();
			menu.put("name", name+(debug?("_"+i):""));
        	if( href.indexOf(">") != -1 || href.indexOf("<") != -1 || href.indexOf("\"") != -1 )
        	{
        		href = href.replace('>', ' ');
        		href = href.replace('<', ' ');
        		href = href.replace('"', ' ');
//        		System.err.println(i+":"+name+", "+href);
    			menu.put("warn", true);
    			parent.put("warn", true);
        	}
			menu.put("href", href);
        	if( parent.has("id") )
        	{
    			menu.put("id", parent.getString("id")+"."+href);
        	}
			menu.put("ico", icon);
			menu.put("target", target);
			if( parent.has("type") ) menu.put("type", parent.getString("type"));
			if( parent.has("path") )
			{
				JSONArray parentPath = parent.getJSONArray("path");
				JSONArray path = new JSONArray();
				for(int j = 0; j < parentPath.length(); j++ )
					path.put(parentPath.getInt(j));
				menu.put("path", path.put(i));
			}
			menu.put("dropRoot", false);
			menus.put(menu);
			Node buttonNode = XMLParser.getChildElementByTag( menuNode, "button" ); 
			if( buttonNode != null )
			{
	    		JSONArray buttons = new JSONArray();
		        for( ; buttonNode != null; buttonNode = XMLParser.nextSibling(buttonNode) )
		        {
		        	name = XMLParser.getElementAttr(buttonNode, "name");
		        	if( name.isEmpty() ) continue;
		        	String id = XMLParser.getElementAttr(buttonNode, "id");
		        	if( id.isEmpty() ) continue;
					JSONObject button = new JSONObject();
					button.put("name", name);
					button.put("id", id);
					button.put("icon", "images/icons/spell_check.png");
		    		buttons.put(button);
		        }
		        if( buttons.length() > 0 ) menu.put("buttons", buttons);
			}
			loadMenus(menuNode, menu);
			if( href.startsWith("#") ) menu.put("isParent", true);
			else menu.put("icon", "images/icons/bookmarks.png");
        }
        if( menus.length() > 0 ) parent.put("children", menus);
	}

	/**
	 * 
	 * @param zNodes
	 * @param moduleid
	 * @param xml
	 * @return
	 * @throws Exception
	private static long loadModules(JSONArray zNodes, String[] modulesOnly, InputStream is)
		throws Exception
	{
		HashMap<String, JSONObject> cmpcfg = new HashMap<String, JSONObject>();
		for(String module:modulesOnly)
		{
			cmpcfg.put(module, null);
		}
		return loadModules(zNodes, cmpcfg, is);
	}
	 */
	/**
	 * 加载菜单配置文件
	 * @param file
	 * @param zNodes
	 */
	public static long loadMenus(JSONArray zNodes, String sysid, InputStream is)
		throws Exception
	{
		if( is == null ) throw new Exception("未发现模块子系统菜单配置");
//		byte[] data1 = IOHelper.readAsByteArray(is);
//		System.err.println(new String(data1, "UTF-8"));
		is.reset();
		XMLParser xml = null;
		try
		{
			xml = new XMLParser(is);
		}
		catch(Exception e)
		{
			File file = new File(PathFactory.getCfgPath(), "modules.xml");
			if( !file.exists() )
			{
				String str = "<?xml version='1.0' encoding='UTF-8'?><sys name='COS' version='"+Version.getValue()+"'></sys>";
				xml = new XMLParser(new ByteArrayInputStream(str.getBytes()));
			}
			else
			{
				XMLParser parser = new XMLParser(file);
				Element root = parser.getRootElement();
				String name = XMLParser.getElementAttr(root, "name");
				log.info("Found the modules xml valid from "+file+", "+name);
				if( !name.isEmpty() )
				{
					root.removeAttribute("name");
				}
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				parser.write(out);
				byte[] data = out.toByteArray();
				xml = new XMLParser(new ByteArrayInputStream(data));
			}
		}
		return loadMenus(zNodes, sysid, xml);
	}
	/**
	 * 
	 * @param zNodes
	 * @param moduleid
	 * @param xml
	 * @return
	 * @throws Exception
	 */
	private static long loadMenus(JSONArray menus, String sysid, XMLParser xml)
		throws Exception
	{
		boolean open = true;
		boolean debug = false;
		int indx = 0;
		if( sysid == null || "toolbar".equals(sysid) || "Sys".equals(sysid) )
		{
			Element toolbarNode = XMLParser.getFirstChildElement( xml.getRootNode() );
            for( ; toolbarNode != null; toolbarNode = XMLParser.getNextSibling(toolbarNode) )
            {
            	if( toolbarNode.getTagName().equals("toolbar") ) 
            	{
            		break;
            	}
            	indx += 1;
            }
            JSONObject toolbar = new JSONObject();
            toolbar.put("name", "系统工具栏"+(debug?("_"+(toolbarNode!=null?indx:-1)):""));
            toolbar.put("id", "toolbar");
            toolbar.put("moduleName", "系统工具栏");
            toolbar.put("moduleId", "toolbar");
            toolbar.put("type", "toolbar");
            toolbar.put("nomenu", true);
            toolbar.put("nocheck", true);
            if( toolbarNode != null )
            {
            	toolbar.put("ico", "skit_fa_icon fa-ellipsis-h");
            	toolbar.put("path", new JSONArray().put(indx++));
            	loadMenus(toolbarNode, toolbar);
            }
            else
            {
            	toolbar.put("ico", "skit_fa_icon_gray fa-ellipsis-h");
            }
            toolbar.put("drag", false);
            toolbar.put("isParent", true);
            menus.put(toolbar);
		}
		indx = 0;
		JSONObject module = ZKMgr.getZookeeper().getJSONObject("/cos/config/modules/"+sysid, false);
		Element moduleNode = XMLParser.getFirstChildElement( xml.getRootNode() );
        while( moduleNode != null )
        {
        	String id = XMLParser.getElementAttr(moduleNode, "id");
        	if( sysid.equals(id) )
        	{
            	break;
        	}
        	indx += 1;
        	moduleNode = XMLParser.getNextSibling(moduleNode);
        }
        String name = module.getString("SysName");
		module.put("name", name+(debug?("_"+(moduleNode!=null?indx:-1)):""));
		module.put("moduleName", name);
		module.put("moduleId", module.getString("id"));
		module.put("type", "module");
		module.put("nomenu", true);
		module.put("drag", false);
		menus.put(module);
		if( moduleNode != null )
		{
			module.put("path", new JSONArray().put(indx));
			module.put("ico", "skit_fa_icon fa-server");
        	String defhref = XMLParser.getElementAttr(moduleNode, "default");
        	module.put("default", defhref);
        	String target = XMLParser.getElementAttr(moduleNode, "target");
        	module.put("target", target);
			loadMenus(moduleNode, module);
			if( open && module.has("children") )
			{
				module.put("open", open);
				open = false;
			}
        }
		else
		{
			module.put("ico", "skit_fa_icon_gray fa-server");
		}
		module.put("isParent", true);
        return ModulesVersionTimestamp;
	}
	
	/**
	 * 价值模块菜单配置
	 * @param zNodes
	 * @return
	 * @throws Exception
	 */
//	public static long loadModules(JSONArray zNodes)
//			throws Exception
//	{
//		return loadModules(zNodes, null);
//	}
	
	public static long loadMenus(JSONArray zNodes, String sysid)
		throws Exception
	{
		return loadMenus(zNodes, sysid, MenusMgr.getTempModulesXml(sysid));
	}

	public static void setLogo(String id, byte[] data)
	{
		String path = "/cos/config/cmp/"+id+".png";
		try
		{
			Zookeeper zookeeper = ZKMgr.getZookeeper();
			Stat stat = zookeeper.exists(path, false); 
			if( stat != null)
			{
				zookeeper.setData(path, data, stat.getVersion());
			}
			else
			{
				zookeeper.create(path, data);
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
	public static byte[] getLogo(String id)
	{
		String path = "/cos/config/cmp/"+id+".png";
		try
		{
			Zookeeper zookeeper = ZKMgr.getZookeeper();
			Stat stat = zookeeper.exists(path, false); 
			if( stat != null)
			{
				return zookeeper.getData(path, false, stat);
			}
		}
		catch(Exception e)
		{
		}
		File tmp = new File(PathFactory.getWebappPath(), "images/cmp/"+id+".png");
		if( !tmp.exists() )
		{//随机创建PNG图片
			Random r = new Random();
			File rp = new File(PathFactory.getWebappPath(), "images/cmp/"+r.nextInt(95)+".png");
			return IOHelper.readAsByteArray(rp);
		}
		else
		{
			return IOHelper.readAsByteArray(tmp);
		}
	}

	/**
	 * 重构
	 * @param xml
	 */
	private static XMLParser rebuildModulesXml(XMLParser xml)
		throws Exception
	{
		Element root = xml.getRootElement();
		if( root.hasAttribute("name") ) root.removeAttribute("name");
		if( root.hasAttribute("version") ) root.setAttribute("version", "3.17.1.8");
		Element toolbarNode = XMLParser.getChildElementByTag( root, "toolbar" );
		if( toolbarNode != null )
		{
			rebuildModuleMenus(xml, toolbarNode, null);
		}
		
		Element moduleNode = XMLParser.getChildElementByTag( root, "module" ); 
        while( moduleNode != null )
        {
			HashMap<String, KAction> mapAction = new HashMap<String, KAction>();
			rebuildModuleAction(moduleNode, mapAction);
			if( moduleNode.hasAttribute("name") ) moduleNode.removeAttribute("name");
			if( moduleNode.hasAttribute("status") ) moduleNode.removeAttribute("status");
			Element navigationNode = XMLParser.getChildElementByTag( moduleNode, "navigation" ); 
			if( navigationNode == null )
			{
				Node r = moduleNode;
				moduleNode = XMLParser.nextSibling(moduleNode);
				root.removeChild(r);
				continue;
			}
			String defhref = XMLParser.getElementAttr(navigationNode, "default");
			if( !defhref.isEmpty() ) moduleNode.setAttribute("default", defhref);
			Element menuNode = null;
	        while( (menuNode = XMLParser.getChildElementByTag( navigationNode, "menu" )) != null )
	        {
	        	moduleNode.appendChild(navigationNode.removeChild(menuNode));
	        }
	        moduleNode.removeChild(navigationNode);
			rebuildModuleMenus(xml, moduleNode, mapAction);
			moduleNode = XMLParser.nextSibling(moduleNode);
        }
        return xml;
	}
	/**
	 * 
	 * @param moduleNode
	 * @param mapAction
	 * @throws Exception
	 */
	private static void rebuildModuleAction(Element moduleNode, HashMap<String, KAction> mapAction)
		throws Exception
	{
		Element actionNode = XMLParser.getChildElementByTag( moduleNode, "action" ); 
        while( actionNode != null )
        {
        	String id = XMLParser.getElementAttr(actionNode, "id");
			JSONObject obj = new JSONObject();
			loadMenus(actionNode, obj);
			JSONArray menus = obj.getJSONArray("children");
			KAction action = new KAction();
			for(int i = 0; i < menus.length(); i++)
			{
				JSONObject menu = menus.getJSONObject(i);
				action.addComponent(new KActionItem(menu.getString("name"), menu.getString("href"), menu.has("ico")?menu.getString("ico"):""));
			}
        	mapAction.put(id, action);
        	Node r = actionNode;
        	actionNode = XMLParser.nextSibling(actionNode);
        	moduleNode.removeChild(r);
        }
	}
	
	/**
	 * 重构模块的菜单配置
	 * @param e
	 * @param mapAction
	 */
	private static void rebuildModuleMenus(XMLParser xml, Element parentNode, HashMap<String, KAction> mapAction)
	{
		HashMap<String, Element> dup = new HashMap<String, Element>();
		Element menuNode = XMLParser.getChildElementByTag( parentNode, "menu" );
        while( menuNode != null )
        {
        	String id = XMLParser.getElementAttr(menuNode, "id");
        	String href = XMLParser.getElementAttr(menuNode, "href");
        	String action = XMLParser.getElementAttr(menuNode, "action");
        	String icon = XMLParser.getElementAttr(menuNode, "icon");
        	if( href.isEmpty() || href.equals("#") )
        	{
        		if( id.isEmpty() ) id = Tools.getUniqueValue();
        		href = "#"+id;
        		menuNode.setAttribute("href", href);
        	}
			if( menuNode.hasAttribute("id") ) menuNode.removeAttribute("id");
        	if(dup.containsKey(href)) parentNode.removeChild(dup.remove(href));
        	dup.put(href, menuNode);
        	if( icon.isEmpty() ) icon = XMLParser.getElementAttr(menuNode, "fa");
        	else if( icon.endsWith(".gif") ) icon = "";
        	if( icon.isEmpty() )
        	{
        		if( menuNode.hasAttribute("fa") ) menuNode.removeAttribute("fa");
        		if( menuNode.hasAttribute("icon") ) menuNode.removeAttribute("icon");
        	}
        	else
        	{
        		if( menuNode.hasAttribute("fa") ) menuNode.removeAttribute("fa");
        		menuNode.setAttribute("icon", icon);
        	}
    		if( menuNode.hasAttribute("module") ) menuNode.removeAttribute("module");
			if( !action.isEmpty() && mapAction != null )
			{
				if( mapAction.containsKey(action) )
				{
					KAction kaction = mapAction.get(action);
					for(int i = 0; i < kaction.size(); i++)
					{
						KActionItem item = (KActionItem)kaction.getComponent(i);
						Element e = xml.createElement("menu");
						menuNode.appendChild(e);
						e.setAttribute("name", item.getLabel());
						e.setAttribute("href", item.getViewHref());
						icon = item.getIcon();
			        	if( icon.endsWith(".gif") ) icon = "";
						if(!icon.isEmpty() ) e.setAttribute("icon", icon);
					}
				}
				menuNode.removeAttribute("action");
			}
			else
				rebuildModuleMenus(xml, menuNode, mapAction);
			menuNode = XMLParser.nextSibling(menuNode);
        }
	}
	
	/*
	 * 得到临时的菜单配置数据
	 */
	public static InputStream getTempModulesXml(String sysid)
	{
		String zkpath = "/cos/config/modules/"+sysid+"/menus";
		Stat stat = null;
		try
		{
			Zookeeper zookeeper = ZKMgr.getZookeeper();
			stat = zookeeper.exists(zkpath, false);
			if( stat == null )
			{
				zkpath = "/cos/config/modules";
				stat = zookeeper.exists(zkpath, false);
			}
			ModulesVersionTimestamp = stat.getMtime();
			return new ByteArrayInputStream(zookeeper.getData(zkpath, false, stat));
		}
		catch(Exception e)
		{
			return null;
		}
	}
	/**
	 * 得到模块子系统菜单配置，将文件存储的配置自动存储到Zookeeper，
	 * @return
	 */
	public static InputStream getModulesXml()
	{
		String path = "/cos/config/modules";
		try
		{
			Zookeeper zookeeper = ZKMgr.getZookeeper();
			Stat stat = zookeeper.exists(path, false); 
			if( stat != null)
			{
				byte[] payload = zookeeper.getData(path, false, stat);
				if( payload != null && payload.length > 0 && payload[0] == '<')
				{
					return new ByteArrayInputStream(payload);
				}
			}
		}
		catch(Exception e)
		{
			File file = new File(PathFactory.getCfgPath(), "modules.xml");
			if( !file.exists() ){
				String xml = "<?xml version='1.0' encoding='UTF-8'?><sys name='COS' version='"+Version.getValue()+"'></sys>";
				return new ByteArrayInputStream(xml.getBytes());
			}
			try {
				return new FileInputStream(file);
			} catch (FileNotFoundException e1) {
			}
		}
		return null;
	}
	
	public static void main(String args[])
	{
		try
		{
			File file = new File("test/", "modules.xml");
			System.out.println(file.getAbsolutePath());
			if( file.exists() )
			{
				XMLParser xml = rebuildModulesXml(new XMLParser(file));
				FileOutputStream out = new FileOutputStream(new File(file.getParentFile(), "m.xml"));
				xml.write(out);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * 发布指定模块子系统配置
	 * @param sysid
	 */
	public void publish(String sysid) throws Exception
	{
		XMLParser xmlt = new XMLParser(getTempModulesXml(sysid));
		XMLParser xmlp = new XMLParser(getModulesXml());
		Element toolbartempNode = null;
		Element toolbarNode = null;
		if( "Sys".equals(sysid) )
		{
			toolbartempNode = XMLParser.getChildElementByTag(xmlt.getRootNode(), "toolbar");
			toolbarNode = XMLParser.getChildElementByTag(xmlp.getRootNode(), "toolbar");
			while(toolbarNode != null )
			{
				Element e = XMLParser.nextSibling(toolbarNode);
				xmlp.getRootNode().removeChild(toolbarNode);
				toolbarNode = e;
			}
		}
		Element moduletempNode = XMLParser.findChildElementByTagAndAttr(xmlt.getRootNode(), "module", "id", sysid);
		Element moduleNode = null;
		do
		{
			moduleNode = XMLParser.findChildElementByTagAndAttr(xmlp.getRootNode(), "module", "id", sysid);
			if( moduleNode != null )
			{
				xmlp.getRootNode().removeChild(moduleNode);
			}
		}
		while(moduleNode!=null);
		
		if( moduletempNode != null ){
			moduleNode = xmlp.createElement("module");
			xmlp.getRootElement().appendChild(moduleNode);
			XMLParser.copyElement(moduletempNode, moduleNode, xmlp);
		}
		if( toolbartempNode != null ){
			toolbarNode = xmlp.createElement("toolbar");
			xmlp.getRootElement().appendChild(toolbarNode);
			XMLParser.copyElement(toolbartempNode, toolbarNode, xmlp);
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		xmlp.write(out);
		String path = "/cos/config/modules";
		Stat stat = ZKMgr.getZookeeper().exists(path);
		if( stat == null )
		{
			throw new Exception("模块子系统配置节点丢失");
		}
		byte[] buf = out.toByteArray();
		ZKMgr.getZookeeper().setData(path, buf, stat);
		File file = new File(PathFactory.getCfgPath(), "modules.xml");
		IOHelper.writeFile(file, buf);
	}
	
	/**
	 * 加载整个模块菜单
	 * @param menus
	 * @throws Exception
	 */
	public static void loadModules(JSONArray menus) throws Exception
	{
		XMLParser xml = new XMLParser(getModulesXml());
		List<JSONObject> modules = ModulesMgr.getConfigs();
		Element toolbarNode = XMLParser.getElementByTag(xml.getRootNode(), "toolbar");
		if( toolbarNode != null )
		{
            JSONObject toolbar = new JSONObject();
            toolbar.put("name", "系统工具栏");
            toolbar.put("id", "toolbar");
            toolbar.put("moduleName", "系统工具栏");
            toolbar.put("moduleId", "toolbar");
            toolbar.put("type", "toolbar");
            toolbar.put("nomenu", true);
            toolbar.put("nocheck", true);
        	toolbar.put("ico", "skit_fa_icon fa-ellipsis-h");
        	loadMenus(toolbarNode, toolbar);
            toolbar.put("drag", false);
            toolbar.put("isParent", true);
            menus.put(toolbar);    
		}
		for(JSONObject module : modules )
		{
			if( module.has("Disabled") && module.getBoolean("Disabled") ){
				continue;
			}
			Element moduleNode = XMLParser.findChildElementByTagAndAttr(xml.getRootNode(), "module", "id", module.getString("id"));
            if( moduleNode == null ) continue;
			String name = module.getString("SysName");
    		module.put("name", name);
    		module.put("moduleName", name);
    		module.put("moduleId", module.getString("id"));
    		module.put("type", "module");
    		module.put("nomenu", true);
    		module.put("drag", false);
    		menus.put(module);
			module.put("ico", "skit_fa_icon fa-server");
        	String defhref = XMLParser.getElementAttr(moduleNode, "default");
        	module.put("default", defhref);
        	String target = XMLParser.getElementAttr(moduleNode, "target");
        	module.put("target", target);
			loadMenus(moduleNode, module);
			if( module.has("children") )
			{
				module.put("open", true);
			}
    		module.put("isParent", true);
        }
	}

	/**
	 * 接受指定系统的菜单配置
	 * @param req
	 * @param sysid
	 * @throws Exception
	 */
	public void accept(HttpServletRequest req, String approver, String sysid)
		throws Exception
	{
		Zookeeper zookeeper = ZKMgr.getZookeeper();
		String zkpath = "/cos/config/modules/"+sysid+"/menus/current";
		byte[] buf = ZKMgr.getZookeeper().getData(zkpath);
		if( buf == null )
		{
			throw new Exception("没有系统菜单配置需要发布。");
		}
		//TODO:将模块子系统菜单做一次合并
		publish(sysid);
		zookeeper.delete(zkpath);
		String id = new String(buf);
		zkpath = "/cos/config/modules/"+sysid+"/menus/history/"+id;
		JSONObject history = zookeeper.getJSONObject(zkpath, false);
		JSONArray log = history.has("log")?history.getJSONArray("log"):null;
		if( log == null )
		{
			throw new Exception("没有系统菜单配置需要发布。");
		}
		history.put("approvaluser", approver);
		history.put("approvaltime", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()));
		history.put("approvalmsg", "同意菜单发布");//审批意见记录
		history.put("approvalresult", true);
		zookeeper.setJSONObject(zkpath, history);
		HashMap<String, JSONArray> map = new HashMap<String, JSONArray>();
		for( int i = 0; i < log.length(); i++ )
		{
			JSONObject slog = log.getJSONObject(i);
			JSONArray array = map.get(slog.getString("operuser"));
			if( array == null )
			{
				array = new JSONArray();
				map.put(slog.getString("operuser"), array);
			}
			array.put(slog);
		}
		String gridxml = "/grid/local/modulesmenucfghistory.xml";
		gridxml = Kit.chr2Unicode(gridxml);
		Iterator<JSONArray> iterator = map.values().iterator();
		while(iterator.hasNext())
		{
			JSONArray array = iterator.next();
			String title = "您提交的"+array.length()+"项系统菜单配置已被配置统管理员【"+approver+"】审核并发布。";
			StringBuffer content = new StringBuffer("您的配置请看下表");
			String operuser = "";
			for(int i = 0; i < array.length(); i++)
			{
				JSONObject slog = array.getJSONObject(i);
				operuser = slog.getString("operuser");
			}
		    Sysnotify notify = new Sysnotify();
			notify.setUseraccount(operuser);
			notify.setFilter("系统配置");
			notify.setTitle(title);
			notify.setPriority(10);
			notify.setNotifytime(new Date());
			notify.setContext(content.toString());
			notify.setAction("问题反馈");
			notify.setActionlink("#feedback");
			notify.setContextimg("");
			notify.setContextlink("digg!query.action?sysid="+sysid+"&id="+id+"&gridxml="+gridxml);
			SysnotifyClient.send(notify);
		}
		String s = String.format("确认并发布新的系统菜单配置，所有修改被应用上线。");
		logoper(req, LogSeverity.INFO, s, "系统配置", null, "digg!query.action?sysid="+sysid+"&id="+id+"&gridxml="+gridxml);
	}
}
