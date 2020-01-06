package com.focus.cos.web.ops.action;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.json.JSONArray;
import org.json.JSONObject;

import com.focus.control.ModulePerf;
import com.focus.cos.api.AlarmSeverity;
import com.focus.cos.api.AlarmType;
import com.focus.cos.api.Status;
import com.focus.cos.api.SysalarmClient;
import com.focus.cos.api.SysemailClient;
import com.focus.cos.api.Sysuser;
import com.focus.cos.api.SysuserClient;
import com.focus.cos.web.action.GridAction;
import com.focus.cos.web.common.Kit;
import com.focus.cos.web.common.ZKMgr;
import com.focus.cos.web.config.service.SysCfgMgr;
import com.focus.cos.web.dev.service.ModulesMgr;
import com.focus.cos.web.ops.service.MonitorMgr;
import com.focus.cos.web.ops.service.SysalarmMgr;
import com.focus.cos.web.ops.vo.Sysalarm;
import com.focus.util.IOHelper;
import com.focus.util.Tools;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.opensymphony.xwork.ModelDriven;

/**
 * 
 * @author focus
 *
 */
public class SysalarmAction extends GridAction implements ModelDriven
{
	private static final long serialVersionUID = -3849634526157401212L;
	private static final Log log = LogFactory.getLog(SysalarmAction.class);
	/*管理容器*/
	private SysalarmMgr sysalarmMgr;
	/*告警备注*/
	private String remark;
	
	public String history()
	{
		try 
		{
			String xmlpath = "/grid/local/sysalarmhistory.xml";
	    	BasicDBList options = new BasicDBList();
    		for(AlarmType e : AlarmType.values() )
        		options.add(e.getValue());
    		labelsModel.put("ORGTYPE", options);
    		
    		options = new BasicDBList();
    		for(AlarmSeverity e : AlarmSeverity.values() )
        		options.add(e.getValue());
    		labelsModel.put("ORGSEVERITY", options);
	        return this.grid(xmlpath);
    	}
		catch (Exception e) 
		{
			super.setResponseException("打开"+viewTitle+"界面失败，因为异常"+e);
			log.debug("Failed to initialize the view of grid for exception ", e);
			return "close";
		}
	}
	/**
	 * 利用grid架构查询数据
	 * @return String
	 */
	public String manager()
	{
		try 
		{
			String xmlpath = "/grid/local/sysalarmmanager.xml";
	    	BasicDBList options = new BasicDBList();
    		for(AlarmType e : AlarmType.values() )
        		options.add(e.getValue());
    		labelsModel.put("ORGTYPE", options);
    		
    		options = new BasicDBList();
    		for(AlarmSeverity e : AlarmSeverity.values() )
        		options.add(e.getValue());
    		labelsModel.put("ORGSEVERITY", options);

	        return this.grid(xmlpath);
		}
		catch (Exception e) 
		{
			super.setResponseException("打开"+viewTitle+"界面失败，因为异常"+e);
			log.debug("Failed to initialize the view of grid for exception ", e);
			return "close";
		}		
	}
	
	/**
	 * 实时告警数据
	 * @return
	 */
	public String instant()
	{
    	ServletOutputStream out = null;
		JSONObject dataJSON = new JSONObject();
		try
		{
//			AlarmhisotryQuery oltp = new AlarmhisotryQuery();
			HttpServletResponse response = super.getResponse();
            out = response.getOutputStream();
			response.setContentType("text/json;charset=utf8");
    		response.setHeader("Content-disposition", "inline; filename="+System.currentTimeMillis()+".json");
			List<?> alarms = sysalarmMgr.getInstantAlarms(super.getUserRole(), super.getUserAccount(), super.getUserName(), super.getUserEmail(), -1);
			String startdate = super.getRequest().getParameter("startdate");
			String enddate = super.getRequest().getParameter("enddate");
			String f_dn = super.getRequest().getParameter("dn");
			String f_type = super.getRequest().getParameter("type");
			String f_severity = super.getRequest().getParameter("severity");
			String f_title = super.getRequest().getParameter("title");
			String f_responser = super.getRequest().getParameter("responser");
			JSONArray data = new JSONArray();
			if( alarms != null )
				for(int i = 0; i < alarms.size(); i++)
				{
		    		Sysalarm alarm = (Sysalarm)alarms.get(i);
		    		String time = Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", alarm.getEventtime().getTime());
		    		if( startdate != null ){
		    			int c = time.compareTo(startdate);
		    			if( c < 0 ) continue;
		    		}
		    		if( enddate != null ){
		    			int c = time.compareTo(enddate);
		    			if( c >= 0 ) continue;
		    		}
		    		if( f_dn != null && alarm.getDn() != null && alarm.getDn().indexOf(f_dn) == -1 ){
		    			continue;
		    		}
		    		if( f_title != null && alarm.getAlarmTitle() != null && alarm.getAlarmTitle().indexOf(f_title) == -1 ){
		    			continue;
		    		}
		    		if( f_type != null && !f_type.equals(alarm.getOrgtype()) ){
		    			continue;
		    		}
		    		if( f_severity != null && !f_severity.equals(alarm.getOrgseverity()) ){
		    			continue;
		    		}
		    		if( f_responser != null ){
		    			if( alarm.getResponser() == null || !alarm.getResponser().contains(f_responser) ){
		    				continue;
		    			}
		    			continue;
		    		}
		    		JSONObject e = new JSONObject();
		    		e.put("_pk", alarm.getAlarmid());
		    		e.put("EVENTTIME", time);
		    		e.put("DN", alarm.getDn());
		    		JSONObject server = MonitorMgr.getInstance().getServer(alarm.getServerkey());
		    		if( server != null )
		    		{
		    			e.put("DN", server.getString("ip")+":"+server.getInt("port"));
		    			e.put("DNRemark", server.has("remark")?server.getString("remark"):"未知名称");
		    			e.put("DNId", server.has("id")?server.getInt("id"):0);
		    		}
		    		e.put("SERVERKEY", alarm.getServerkey());
		    		e.put("ORGSEVERITY", alarm.getOrgseverity());
		    		e.put("ORGTYPE", alarm.getOrgtype());
		    		e.put("ALARMTITLE", alarm.getAlarmTitle());
		    		e.put("ACTIVESTATUS", alarm.getActivestatus());
		    		e.put("MODULE", alarm.getModule());
		    		e.put("_MODULE", alarm.getModule());
		    		e.put("PROBABLECAUSE", alarm.getProbablecause());
		    		e.put("ID", alarm.getId());
		    		e.put("RESPONSER", alarm.getResponser());
		    		e.put("RESPONSERCONTACT", alarm.getContact());
		    		if( alarm.getResponser() == null || alarm.getResponser().isEmpty() || "超级管理员".equals(alarm.getResponser()) )
		    		{
			    		e.put("RESPONSER", SysCfgMgr.get("SysContactName"));
			    		e.put("RESPONSERCONTACT", SysCfgMgr.get("SysContact"));
		    		}
		    		e.put("ALARMTEXT", alarm.getAlarmText());
		    		e.put("CLEARTIME", alarm.getCleartime()!=null?Tools.getFormatTime("MM-dd HH:mm:ss", alarm.getCleartime().getTime()):"N/A");
		    		e.put("FREQUENCY", alarm.getFrequency());
//		    		e.put("RESPONSER", "");//之后扩展字段
//		    		e.put("RESPONSERCONTACT", "");//之后扩展字段
		    		data.put(e);
//		    		oltp.handle(e, null);
				}
			dataJSON.put("totalRecords", data.length());
			dataJSON.put("curPage", 1);
			dataJSON.put("data", data);
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
	 * 通知
	 */
	public String notifyto()
	{
		String title = "";
		try
		{
			if( snapshot == null )
			{
				this.setResponseException("请选择您要发送邮件通知的告警。");
			}
			else
			{
				JSONObject json = new JSONObject(snapshot);
				title = json.getString("ALARMTITLE");
//				System.err.println(snapshot);
				String template = encodeEmailTemplate();
				SysemailClient.send(
                   emailaddr,//主送
                   "",//抄送
                   json.getString("MODULE")+"告警通知("+json.getString("ORGSEVERITY")+")",
                   "html=="+template, 
				   "");
				this.setResponseMessage("成功提交邮件发送，请在系统邮件发件箱查询。");
			}
		}
		catch (Exception e)
		{
			this.setResponseException("生成或发送邮件通知的告警("+title+")出现异常"+e.getMessage());
			log.error("Failed to notify alarm "+id+" for exception:", e);
		}
		return "alert";	
	}

	/**
	 * 通知快照，调用模板，生成告警数据
	 * @return
	 */
	public String snapshotemail()
	{
		super.getResponse().setContentType("text/html");
		super.getResponse().setCharacterEncoding("UTF-8");
		super.getResponse().setHeader("Content-disposition", "inline; filename=snapshotemail.html");
		OutputStream out = null;
		try
		{
			out = super.getResponse().getOutputStream();
			if( snapshot == null )
			{
				this.setResponseException("请选择您要发送邮件通知的告警。");
			}
			else
			{
//				System.err.println(snapshot);
				out.write(encodeEmailTemplate().getBytes("UTF-8"));
				out.flush();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		if( out != null )
			try {
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return null;
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	private String encodeEmailTemplate() throws Exception
	{
		JSONObject json = new JSONObject(snapshot);
		InputStream is = this.getClass().getResourceAsStream("/com/focus/cos/template_mail_alarm.html");
		String template  = new String(IOHelper.readAsByteArray(is), "UTF-8");
		String moduleName = SysCfgMgr.get("SysName");
		String logoUrl = Kit.URL_IMAGEPATH(super.getRequest())+"logo.png";
		if( !"Sys".equals(json.getString("_MODULE")) )
		{
			moduleName = json.getString("MODULE");
			logoUrl = Kit.URL_IMAGEPATH(super.getRequest())+"cmp/"+json.getString("_MODULE")+".png";
		}
		template = template.replaceAll("%module.name%", moduleName);
		template = template.replaceAll("%logo.url%", logoUrl);
		template = template.replaceAll("%logo.title%", "登录");
		template = template.replaceAll("%url.login%", Kit.URL(super.getRequest()));
		template = template.replaceAll("%sys.name%", SysCfgMgr.get("SysName"));
		template = template.replaceAll("%responser%", json.getString("RESPONSER"));
		template = template.replaceAll("%you%", "你");
		template = template.replaceAll("%module.title%", json.getString("MODULE"));
		template = template.replaceAll("%title%", json.getString("ALARMTITLE"));
		template = template.replaceAll("%type%", json.getString("ORGTYPE"));
		template = template.replaceAll("%severity%", json.getString("ORGSEVERITY"));
		String color = AlarmSeverity.get(json.getString("ORGSEVERITY")).toString();
		template = template.replaceAll("%severity.color%", color);
		if( color.equalsIgnoreCase("BLACK") ) color = "red";
		else if( color.equalsIgnoreCase("YELLOW") ) color = "blue";
		else color = "#fff";
		template = template.replaceAll("%severity.font.color%", color);
		template = template.replaceAll("%dn%", json.getString("DN"));
		template = template.replaceAll("%time%", json.getString("EVENTTIME"));
		template = template.replaceAll("%cause%", json.getString("PROBABLECAUSE"));
		template = template.replaceAll("%programer%", json.getString("RESPONSER"));
		template = template.replaceAll("%sys.descr%", SysCfgMgr.get("SysDescr"));
		template = template.replaceAll("%sys.contact%", SysCfgMgr.get("SysContact"));
		template = template.replaceAll("%sys.email%", SysCfgMgr.get("POP3Username"));

		final HashMap<String, String> themes = new HashMap<String, String>();
		themes.put("default", "#18bc9c");
		themes.put("blue", "#23bab5");
		themes.put("honey_flower", "#674172");
		themes.put("razzmatazz", "#DB0A5B");
		themes.put("ming", "#336E7B");
		themes.put("yellow", "#ffd800");
		String theme = "#18bc9c";
		JSONObject sysConfig = SysCfgMgr.getConfig();
		if( sysConfig != null )
		{
			theme = sysConfig.has("Theme")?sysConfig.getString("Theme"):"default";
			theme =  themes.get(theme);
		}
		
		template = template.replaceAll("%skin.color%", theme);
		if( "设备告警".equals(json.getString("ORGTYPE")) )
		{
			template = template.replaceAll("%chart.pannel%", "");
			if( "SystemMonitor_Storage".equals(json.getString("ID")) )
			{
				template = template.replaceAll("%chart.url%", Kit.URL_PATH(super.getRequest())+"monitorload!serverchart.action?host="+json.getString("DN")+"&type=3");
			}
			else if( "SystemMonitor_Memory".equals(json.getString("ID")) )
			{
				template = template.replaceAll("%chart.url%", Kit.URL_PATH(super.getRequest())+"monitorload!serverchart.action?host="+json.getString("DN")+"&type=1");
			}
			else if( "SystemMonitor_Cpu".equals(json.getString("ID")) )
			{
				template = template.replaceAll("%chart.url%", Kit.URL_PATH(super.getRequest())+"monitorload!serverchart.action?host="+json.getString("DN")+"&type=0");
			}
			template = template.replaceAll("%chart.height%", "220");
		}
		else
		{
			template = template.replaceAll("%chart.pannel%", "none");
		}
		template = template.replaceAll("%content%", json.getString("ALARMTEXT"));
		return template;
	}
	
	/**
	 * 查看告警
	 * @return
	 */
	private Sysalarm alarm;
	public Sysalarm getAlarm() {
		return alarm;
	}
	public String preview()
	{
		long alarmid = 0;
		if( Tools.isNumeric(id) )
			alarmid = Long.parseLong(id);
		this.alarm = sysalarmMgr.getSysalarmDao().findById(alarmid);
		if( alarm == null )
		{
			super.setResponseException("查看告警详情出现异常");
			return "close";
		}

		String color = AlarmSeverity.get(alarm.getOrgseverity()).toString();
		alarm.setSeverityColor(color);
		if( color.equalsIgnoreCase("BLACK") ) color = "red";
		else if( color.equalsIgnoreCase("YELLOW") ) color = "blue";
		else color = "#fff";
		alarm.setSeverityFontColor(color);
		JSONObject module = ModulesMgr.getConfig(alarm.getModule());
		alarm.setModule(module!=null?module.getString("SysName"):"N/A");
		if( "设备告警".equals(alarm.getOrgtype()) )
		{
			if( "SystemMonitor_Storage".equals(alarm.getId()) )
			{
				alarm.setContextUrl(Kit.URL_PATH(super.getRequest())+"monitorload!serverchart.action?host="+alarm.getDn()+"&type=3");
			}
			else if( "SystemMonitor_Memory".equals(alarm.getId()) )
			{
				alarm.setContextUrl(Kit.URL_PATH(super.getRequest())+"monitorload!serverchart.action?host="+alarm.getDn()+"&type=1");
			}
			else if( "SystemMonitor_Cpu".equals(alarm.getId()) )
			{
				alarm.setContextUrl(Kit.URL_PATH(super.getRequest())+"monitorload!serverchart.action?host="+alarm.getDn()+"&type=0");
			}
		}
		String id = alarm.getId();
		account = null;
		if( id != null && !id.isEmpty() )
		{
			ModulePerf modulePerf = null;
			int k = alarm.getId().lastIndexOf('_');
			if( k != -1 )
			{
				modulePerf = MonitorMgr.getTracker().getProgramer(alarm.getDn(), id.substring(0, k));
			}
			if( modulePerf == null )
			{
				modulePerf = MonitorMgr.getTracker().getProgramer(alarm.getDn(), id);
			}
			
			if( modulePerf != null )
			{
				if( modulePerf.getProgrammer() != null && !modulePerf.getProgrammer().isEmpty() )
				{
					account = modulePerf.getProgrammer();
				}
			}
			
			if( account == null )
			{
				account = "超级管理员";
			}
		}
		return "preview";
	}
	
	/**
	 * 确认告警
	 * @return String
	 */
	public String confirm()
	{
		String alarms = super.getRequest().getParameter("alarms");
		try
		{
			SysalarmClient.confirm(alarms, super.getUserAccount(), remark);
			String s = String.format(this.getText("user.log.0052"), super.getUserAccount());
			logoper(s, "告警管理", null, null);
		}
		catch(Exception e)
		{
			String s = String.format(this.getText("user.log.1052"), getUserAccount());
			logoper(s, "告警管理", null, null, e);
		}		
		return redirect("sysalarm!manager.action");
	}

	/**
	 * 告警确认
	 * @return
	 */
	public String doAck()
	{
		try
		{
			SysalarmClient.confirm(id, super.getUserAccount(), "用户通过系统消息通知进行告警确认。");
			String s = String.format(this.getText("user.log.0052"), getUserAccount());
			logoper(s, "告警管理", null, null);
			super.setResponseMessage(s);
		}
		catch(Exception e)
		{
			String s = String.format(this.getText("user.log.1052"), getUserAccount());
			logoper(s, "告警管理", null, null, e);
			super.setResponseException(s);
		}		
		return "close";
	}
	/**
	 * 打开配置告警视图
	 */
	public String config()
	{
		try 
		{
			List<JSONObject> list = ZKMgr.getZookeeper().getJSONObjects("/cos/config/role");
			JSONArray data = new JSONArray();
			for(JSONObject group : list)
			{
				JSONObject e = new JSONObject();
				e.put("account", String.valueOf(group.getInt("id")));
				e.put("name", "["+group.getString("name")+"]");
				e.put("role", group.getString("name"));
				data.put(e);
				ArrayList<Sysuser> users = SysuserClient.listUser(group.getInt("id"), -1, Status.Enable.getValue());
				for(Sysuser user : users)
				{
					if( user.getUsername().equals("admin") ) continue;
					if( user.getEmail() == null || user.getEmail().isEmpty() ) continue;
					e = new JSONObject();
					e.put("account", user.getUsername());
					e.put("name", user.getRealname());
					e.put("email", user.getEmail());
					e.put("roleid", user.getRoleid());
					e.put("role", group.getString("name"));
					e.put("SUBSCRIBER", false);
					data.put(e);
				}
			}
			super.localDataObject.put("SUBSCRIBER", data);

			BasicDBList options = new BasicDBList();
    		for(AlarmType e : AlarmType.values() )
        		options.add(e.getValue());
    		labelsModel.put("ORGTYPE", options);
    		
			this.localDataPlus = localDataObject.toString();
			String xmlpath = "/grid/local/sysalarmcfg.xml";
	        String rsp = this.grid(xmlpath);
	        if( this.localDataArray.length() == 0 )
	        {
				ZooKeeper zookeeper = ZKMgr.getZooKeeper();
				String path = "/cos/config";
				Stat stat = zookeeper.exists(path, false); 
				if( stat == null)
				{
					zookeeper.create(path, "配置管理".getBytes("UTF-8"), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				}
				path = "/cos/config/alarm";
				stat = zookeeper.exists(path, false); 
				if( stat == null)
				{
					zookeeper.create(path, "告警配置管理".getBytes("UTF-8"), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				}
	        }
	        localDataArray = new JSONArray();
	        ZooKeeper zookeeper = ZKMgr.getZooKeeper();
			JSONObject e = this.sysalarmMgr.getAlarmConfig(zookeeper, AlarmType.S, "Sys");
			if( e != null ) localDataArray.put(e);
			e = sysalarmMgr.getAlarmConfig(zookeeper, AlarmType.E, "Sys");
			if( e != null ) localDataArray.put(e);
			e = sysalarmMgr.getAlarmConfig(zookeeper, AlarmType.D, "Sys");
			if( e != null ) localDataArray.put(e);
			BasicDBList listCmpCfg = (BasicDBList)this.labelsModel.get("SVRSYSTEM");
			for(int k = 0; k < listCmpCfg.size(); k++)
			{
				BasicDBObject p = (BasicDBObject)listCmpCfg.get(k);
				if( "Sys".equals(p.getString("value")) ) continue;
				e = sysalarmMgr.getAlarmConfig(zookeeper, AlarmType.B, p.getString("value"));
				if( e != null ) localDataArray.put(e);
				e = sysalarmMgr.getAlarmConfig(zookeeper, AlarmType.Q, p.getString("value"));
				if( e != null ) localDataArray.put(e);
			}
	        this.localData = localDataArray.toString();
	        return rsp;
		}
		catch (Exception e) 
		{
			super.setResponseException("打开"+viewTitle+"界面失败，因为异常"+e);
			log.debug("Failed to initialize the view of grid for exception ", e);
			return "close";
		}
	}
	
	public void setSysalarmMgr(SysalarmMgr sysalarmMgr)
	{
		this.sysalarmMgr = sysalarmMgr;
	}
	
	public void setRemark(String remark) {
		this.remark = remark;
	}
}
