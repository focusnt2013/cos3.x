package com.focus.cos.web.dev.action;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.data.Stat;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Element;

import com.focus.cos.api.Sysnotify;
import com.focus.cos.api.SysnotifyClient;
import com.focus.cos.web.common.Kit;
import com.focus.cos.web.common.ZKMgr;
import com.focus.cos.web.dev.service.MenusMgr;
import com.focus.cos.web.service.SvrMgr;
import com.focus.util.IOHelper;
import com.focus.util.Tools;
import com.focus.util.XMLParser;
import com.focus.util.Zookeeper;
import com.opensymphony.webwork.ServletActionContext;

public class MenusAction extends DevAction 
{
	private static final long serialVersionUID = 6763380428452952597L;
	private static final Log log = LogFactory.getLog(MenusAction.class);
	private String xmlEdit;
	private String menuName;
	private String menuUrl;
	/**
	 * 导出配置
	 * @return
	 */
	public String downloadxml()
	{
		HttpServletResponse response = null;
		OutputStream out = null;
		byte[] payload = null;
		try
		{
			payload = IOHelper.readAsByteArray(MenusMgr.getTempModulesXml(sysid));
			response = ServletActionContext.getResponse();
			response.setContentType("text/xml");
			response.setHeader("Content-disposition", "attachment; filename=modules_"+sysid+".xml");
        	out = response.getOutputStream();
    		int k = 0;
    		for( int i = 0; i < payload.length;  )
    		{
    			byte b = payload[i];
				char c = (char)payload[i++];
				if( c == '<' )
				{
					char c1 = (char)payload[i];
					if( c1 == '/' && k > 0 )
					{
						k -= 1;
					}
					for(int j = 0; j < k; j++)
					{
        				out.write('\t');
					}
    				out.write('<');
					if( c1 != '/' && c1 != '?' ) k += 1;
				}
				else if( c == '>' && i < payload.length)
				{
    				out.write('>');
					if( k > 0 && payload[i-2] == '/' )
						k -= 1;
					char c1 = (char)payload[i];
					if( c1 != '\r' && c1 != '\n' )
					{
        				out.write('\r');
        				out.write('\n');
					}
				}
				else
				{
					if( c != '\r' && c != '\n' )
						out.write(b);
				}
    		}
			out.flush();
			out.close();
			super.responseMessage = "下载模块子系统菜单配置成功。";
			logoper(responseMessage, "系统配置", null, null);
		}
		catch( Exception e )
		{
			responseException = "下载模块子系统菜单配置出现异常:"+e.getMessage();
			logoper(responseException, "系统配置", null, null, e);
			return "alert";
		}
		return null;
	}
	/**
	 * 显示modules.xml的
	 * @return
	 */
	public String modulesxml()
	{
		HttpServletResponse response = null;
		OutputStream out = null;
		byte[] payload = null;
		try
		{
			payload = IOHelper.readAsByteArray(MenusMgr.getTempModulesXml(sysid));
			response = ServletActionContext.getResponse();
			response.setContentType("text/xml");
			response.setHeader("Content-disposition", "inline; filename=modules.xml");
        	out = response.getOutputStream();
        	for(int i = 0; i < payload.length;i++)
        	{
        		out.write(payload[i]);
        		if( payload[i] == '>' )
        		{
        			out.write('\r');
        			out.write('\n');
        			out.flush();
        		}
        		
        	}
			out.flush();
			out.close();
		}
		catch( Exception e )
		{
			try
			{
				out = getResponse().getOutputStream();
				getResponse().setContentType("text/plain;charset=ISO8859_1");
				PrintWriter writer = new PrintWriter(out);
				e.printStackTrace(writer);
				writer.flush();
				writer.close();
				out.close();
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * 导入配置
	 * @return
	 */
	public String uploadxml()
	{
		JSONObject rsp = new JSONObject();
		if( this.uploadfile == null || !uploadfile.exists() )
		{
			rsp.put("alt", "上传菜单配置文件失败，因为未能收到文件包");
			return response(super.getResponse(), rsp.toString());
		}
		XMLParser xml = null;
		try
		{
			xml = new XMLParser(uploadfile);
		}
		catch(Exception e)
		{
			responseException = "上传模块子系统菜单配置文件失败，因为无法正确解析其上传的文件";
			rsp.put("alt", responseException);
			logoper(responseException, "系统配置", null, null, e);
			return response(super.getResponse(), rsp.toString());
		}

		Element removeModuleNode = null;
		Element thisModuleNode = null;
		Element moduleNode = XMLParser.getFirstChildElement( xml.getRootNode() );
        while( moduleNode != null )
        {
        	String id = XMLParser.getElementAttr(moduleNode, "id");
        	if( sysid.equals(id) || "toolbar".equalsIgnoreCase(moduleNode.getNodeName()) )
        	{
        		thisModuleNode = moduleNode;
            	moduleNode = XMLParser.getNextSibling(moduleNode);
        	}
        	else
        	{
        		removeModuleNode = moduleNode;
            	moduleNode = XMLParser.getNextSibling(moduleNode);
        		xml.getRootNode().removeChild(removeModuleNode);//删除多余的配置
        	}
        }
        if( thisModuleNode == null )
        {
			responseException = "上传模块子系统菜单配置文件失败，因为配置中不存在该系统【"+sysid+"】的菜单配置";
			rsp.put("alt", responseException);
			return response(super.getResponse(), rsp.toString());
        }
		
		String zkpath = "/cos/config/modules/"+sysid+"/menus";
		try
		{
			Zookeeper zookeeper = ZKMgr.getZookeeper();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			xml.write(out);
			byte[] data = out.toByteArray();
			Stat stat = zookeeper.exists(zkpath, false);
			if( stat != null )
			{
				zookeeper.setData(zkpath, data, stat.getVersion());
			}
			else
			{
				zookeeper.create(zkpath, data);
			}
			rsp.put("succeed", true);
			super.responseMessage = "上传模块子系统菜单配置文件成功，需要系统管理员确认并发布才能生效。";
			rsp.put("alt", responseMessage);
			JSONObject slog = new JSONObject();
			slog.put("time", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()));
			slog.put("moduleid", sysid);
			slog.put("oper", 30);
			slog.put("targetpath", "");
			slog.put("operuser", super.getUserAccount());
			slog.put("username", super.getUserName());
			slog.put("remark", "模块子系统菜单文件导入");

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
			SvrMgr.sendNotiefiesToSystemadmin(super.getRequest(),
					"系统配置", 
					"用户【"+super.getUserAccount()+"】导入了模块子系统菜单文件",
					"请审核确认并发布", 
					"digg!query.action?sysid="+sysid+"&id="+id+"&gridxml="+gridxml,
					"菜单发布审批",
					"cmpcfg!publish.action");
			logoper(responseMessage, "系统配置", null, "digg!query.action?sysid="+sysid+"&id="+id+"&gridxml="+gridxml);
		}
		catch(Exception e)
		{
			responseException = "上传菜单配置文件失败，因为无法正确解析配置文件";
			rsp.put("alt", responseException);
			logoper(responseException, "系统配置", null, null, e);
		}
		return response(super.getResponse(), rsp.toString());
	}
	
	/**
	 * 系统管理员保存菜单配置
	 * @return
	public String savemodules()
	{
		String zkpath = "/cos/config/modules.xml.tmp";
		try
		{
			ZooKeeper zookeeper = ZKMgr.getZooKeeper();
			Stat stat = zookeeper.exists(zkpath, false);
			if( stat == null )
			{
				super.setResponseMessage("没有需要保存的配置");
			}
		}
		catch(Exception e)
		{
		}
		return modules();
	}
	 */

	/**
	 * 查历史配置数据
	 * @return
	 */
	public String confighistory()
	{
        return super.grid("/grid/local/modulesmenucfghistory.xml");
	}
	/**
	 * 配置数据
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

    		String zkpath = "/cos/config/modules/"+sysid+"/menus/current";//记录当前日志
			Zookeeper zookeeper = ZKMgr.getZookeeper();
			byte[] buf = zookeeper.getData(zkpath);
			String id = null;
			JSONArray data = null;
			if( buf != null )
			{
				id = new String(buf);
				zkpath = "/cos/config/modules/"+sysid+"/menus/history/"+id;
				JSONObject history = zookeeper.getJSONObject(zkpath, false);
				data = history.has("log")?history.getJSONArray("log"):null;
				if( data != null)
				{
					for(int i = 0;i < data.length(); i++)
					{
						JSONObject slog = data.getJSONObject(i);
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
					}
				}
			}
			if( data == null )
			{
				data = new JSONArray();
			}
//			System.err.println(data.toString(4));
    		dataJSON.put("totalRecords", data.length());
			dataJSON.put("curPage", 1);
			dataJSON.put("data", data);
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
	 * 组件子系统菜单配置
	 * @return
	 */
	public String publish()
	{
        return super.grid("/grid/local/modulesmenucfg.xml");
	}
	/**
	 * 接受菜单修改
	 * @return String
	 */
	public String accept()
	{
		try
		{
			this.menusMgr.accept(super.getRequest(), super.getUserAccount(), sysid);
			super.setResponseMessage("模块子系统菜单配置已发布生效，请立刻打开角色权限组配置。");
			super.setResponseConfirm("role!manager.action");
			super.viewTitle = "角色权限组配置: 菜单配置发生变化需要检查角色权限配置";
		}
		catch(Exception e)
		{
			log.info("Failed to accept modulescfg for ", e);
			super.setResponseException("审批系统菜单配置出现异常"+e+",请联系系统管理员解决。");
			logoper(responseException, "系统配置", null, null, e);
		}
		return this.publish();
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
				return publish();
			}
			Zookeeper zookeeper = ZKMgr.getZookeeper();
			JSONObject user = (JSONObject) super.getRequest().getSession().getAttribute("account");
			String zkpath = "/cos/config/modules/"+sysid+"/menus/current";
			byte[] buf = zookeeper.getData(zkpath);
			if( buf == null )
			{
				super.setResponseMessage("没有系统菜单配置需要发布。");
				return publish();
			}
			id = new String(buf);
			zkpath = "/cos/config/modules/"+sysid+"/menus/history/"+id;
			JSONObject history = zookeeper.getJSONObject(zkpath, false);
			JSONArray log = history.has("log")?history.getJSONArray("log"):null;
			history.put("approvaluser", super.getUserAccount());
			history.put("approvaltime", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()));
			history.put("approvalmsg", super.getMessage());//审批意见记录
			history.put("approvalresult", false);
			JSONArray rejects = history.has("rejects")?history.getJSONArray("rejects"):null;
			if( rejects == null )
			{
				rejects = new JSONArray();
				history.put("rejects", rejects);
			}
			JSONObject reject = new JSONObject();
			history.put("approvaluser", super.getUserAccount());
			history.put("approvaltime", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()));
			history.put("approvalmsg", super.getMessage());//审批意见记录
			rejects.put(reject);
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
				String title = "您提交的"+array.length()+"项系统菜单配置已被系统管理员【"+user.getString("realname")+"】拒绝。";
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
				notify.setContext(super.getMessage());
				notify.setAction("问题反馈");
				notify.setActionlink("#feedback");
				notify.setContextimg("");
				notify.setContextlink("digg!query.action?sysid="+sysid+"&id="+id+"&gridxml="+gridxml);
				SysnotifyClient.send(notify);
			}
			logoper("系统菜单配置已被系统管理员拒绝。", "系统配置", null, "digg!query.action?sysid="+sysid+"&id="+id+"&gridxml="+gridxml);
			super.setResponseMessage("系统菜单配置已被拒绝。");
		}
		catch(Exception e)
		{
			log.debug("Failed to accept modulescfg for ", e);
			super.setResponseException("审批系统菜单配置出现异常"+e+", 请联系系统管理员解决。");
			logoper(responseException, "系统配置", null, null, e);
		}
		return this.publish();
	}
	/**
	 * 配置指定模块子系统的菜单
	 * @return
	 */
	public String config()
	{
		account = super.getUserAccount();
		log.info("Config the menus of "+sysid+" by "+account);
		try 
		{
			JSONObject module = ZKMgr.getZookeeper().getJSONObject("/cos/config/modules/"+sysid, false);
			if( module == null )
			{
				throw new Exception("未知模块子系统");
			}
			grant = super.isSysadmin();
			if( !grant && !module.has("Developers") )
			{
				throw new Exception("用户【"+account+"】不是系统管理员该模块子系统还未安排开发者，请联系您的系统管理员");
			}
			if( !grant )
			{
				JSONObject develpers = module.getJSONObject("Developers");
				if( !develpers.has(account) )
				{
					throw new Exception("用户【"+account+"】不是开发者");
				}
			}
			this.localDataArray = new JSONArray();
			timestamp = MenusMgr.loadMenus(localDataArray, sysid);
			this.sysname = module.getString("SysName");
    		this.jsonData = localDataArray.toString();
    		this.xmlEdit = new String(IOHelper.readAsByteArray(MenusMgr.getTempModulesXml(sysid)), "UTF-8");
			listData = ZKMgr.getZookeeper().getJSONObjects("/cos/config/modules/"+sysid+"/weixin", true);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			super.setResponseException("打开模块子系统菜单["+sysid+"]配置视图失败因为"+e.getMessage());
			return "alert";
		}
		return "config";
	}
	
	private MenusMgr menusMgr;

	public void setMenusMgr(MenusMgr menusMgr) {
		this.menusMgr = menusMgr;
	}
	public String getXmlEdit() {
		return xmlEdit;
	}

	public void setXmlEdit(String xmlEdit) {
		this.xmlEdit = xmlEdit;
	}
	public String getMenuName() {
		return menuName;
	}
	public void setMenuName(String menuName) {
		this.menuName = menuName;
	}
	public String getMenuUrl() {
		return menuUrl;
	}
	public void setMenuUrl(String menuUrl) {
		this.menuUrl = menuUrl;
	}
}
