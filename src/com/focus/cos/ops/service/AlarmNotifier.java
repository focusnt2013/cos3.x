package com.focus.cos.ops.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.zookeeper.data.Stat;
import org.json.JSONObject;

import com.focus.cos.CosServer;
import com.focus.cos.api.AlarmSeverity;
import com.focus.cos.api.AlarmType;
import com.focus.cos.api.Sysalarm;
import com.focus.cos.api.Sysemail;
import com.focus.cos.api.SysemailClient;
import com.focus.cos.api.Sysnotify;
import com.focus.cos.api.SysnotifyClient;
import com.focus.cos.api.Sysuser;
import com.focus.cos.api.SysuserClient;
import com.focus.cos.ops.dao.AlarmDao;
import com.focus.cos.ops.vo.Config;
import com.focus.sql.ConnectionPool;
import com.focus.util.ConfigUtil;
import com.focus.util.IOHelper;
import com.focus.util.Log;
import com.focus.util.Tools;
import com.focus.util.Zookeeper;

/**
 * <p>Title: 告警通知处理模块</p>
 *
 * <p>Description: 从数据库中查询未确认的告警执行处理</p>
 *
 * <p>Copyright: Copyright (c) 2008</p>
 *
 * <p>Company: focusnt.inc</p>
 *
 * @author focus lau
 * @version 2.0 @2016
 * below all properites from system
 * @control.port
 * @dbDriver
 * @dbUser
 * @dbPassword
 * @dbUrl
 * @WS_USER
 * @WS_EMAIL
 * @WS_NOTIFY
 * @WS_SMS
 */
public class AlarmNotifier extends CosServer
{
    public static String ModuleID = "SystemAlarm";
	private Zookeeper zookeeper;
	private HashMap<String, Config> config = new HashMap<String, Config>();
	private ArrayList<Sysuser> users;
	private HashMap<String, Sysuser> usernames = new HashMap<String, Sysuser>();
	private HashMap<String, Sysuser> realnames = new HashMap<String, Sysuser>();
	
	/**
	 * 加载告警配置
	 * @param zookeeper
	 * @param users
	 * @param config1
	 * @param config2
	 * @param logs
	 * @return 配置的版本号
	 * @throws Exception
	 */
	public static int loadConfig(Zookeeper zookeeper, ArrayList<Sysuser> users, HashMap<String, Config> config2, StringBuffer logs) throws Exception
	{
		String path = "/cos/config/alarm";
		Stat stat = zookeeper.exists(path, false); 
		if( stat != null )
		{
			List<String> list = zookeeper.getChildren(path, false);
			logs.append("Succeed to setup "+list.size()+" configs from "+path);
			for( String node : list )
			{
				String zkpath = path+"/"+node;
				stat = zookeeper.exists(zkpath, false);
				if( stat == null )
				{
					continue;
				}
				if(  node.length() >= 32 )
				{
					zookeeper.delete(zkpath, stat.getVersion());
					continue;
				}
				JSONObject c = null;
				JSONObject subscribers = null;
				String json = new String(zookeeper.getData(zkpath, false, stat), "UTF-8");
				c = new JSONObject(json);
				c.put("version", stat.getVersion());
				c.put("isAdmin", false);
				if( !c.has("SUBSCRIBER") ){
					if( !c.getString("ORGTYPE").equals(AlarmType.S.getValue()) &&
						!c.getString("ORGTYPE").equals(AlarmType.E.getValue()) &&
						!c.getString("ORGTYPE").equals(AlarmType.D.getValue()))
					{
						logs.append("\r\n\t["+c.getString("SVRSYSTEM")+"]["+c.getString("ORGTYPE")+"] discard for not found subscribers.");
						continue;
					}
					c.put("isAdmin", true);
					logs.append("\r\n\t["+c.getString("SVRSYSTEM")+"]["+c.getString("ORGTYPE")+"] subscribers are all system administrators.");
				}
				else
				{
					subscribers = (JSONObject)c.get("SUBSCRIBER");
					if( subscribers == null || subscribers.length() == 0 ) continue;
					logs.append("\r\n\t["+c.getString("SVRSYSTEM")+"]["+c.getString("ORGTYPE")+"] subscribers are "+subscribers.length());
				}
				
				if( config2 != null )
		            for(int i = 0; i < AlarmSeverity.values().length; i++ )
		            {
		            	String f = "FREQUENCY-"+i;
		            	String p = "PERIOD-"+i;
		            	if( c.has(f) && c.has(p) )
		            	{
		            		Config cfg = new Config();
		            		Object o = c.get(f);
		            		if( o instanceof Integer ){
		            			cfg.setFrequency((int)o);
		            		}
		            		else if( Tools.isNumeric(o.toString()) ){
		            			cfg.setFrequency(Integer.parseInt(o.toString()));
		            		}
		            		o = c.get(p);
		            		if( o instanceof Integer ){
		            			cfg.setPeriod((int)o);
		            		}
		            		else if( Tools.isNumeric(o.toString()) ){
		            			cfg.setPeriod(Integer.parseInt(o.toString()));
		            		}
	            			ArrayList<Integer> group = new ArrayList<Integer>();
		            		if( users != null )
		            		{
		            			Iterator<?> iteator = subscribers.keys();
		            			while(iteator.hasNext())
		            			{
		            				String key = iteator.next().toString();
		            				if( Tools.isNumeric(key) )
		            				{
		            					group.add(Integer.parseInt(key));
		            				}
		            			}
								for(Sysuser e : users )
								{
									if( e.getEmail() == null || e.getEmail().isEmpty() ) continue;
									//logs.append("\r\n\t\t\t"+e.getRoleid()+" "+e.getUsername()+", "+e.getEmail());
									if( subscribers == null && e.getRoleid() == 1 ) cfg.addSubscriber(e);
									else if( subscribers != null && subscribers.has(e.getUsername()) ) cfg.addSubscriber(e);
									if( !group.isEmpty() && group.indexOf((int)e.getRoleid()) != -1 )
									{
										cfg.addSubscriber(e);
									}
								}
		            		}
		            		String key = c.getString("id")+i;
		            		config2.put(key, cfg);
		            		logs.append("\r\n\t\t["+AlarmSeverity.get(i).getValue()+"]["+key+"] freguency "+cfg.getFrequency()+", period "+cfg.getPeriod()+", subscribers("+group.size()+") "+cfg.getSubscribers().size());
		            	}
		            }
			}
			return stat.getVersion();
		}
		else
		{
			logs.append("Not found config("+stat+") from "+path);
			return stat!=null?stat.getVersion():-1;
		}
	}
	/**
	 * 告警通知模块构造程序
	 * @throws Exception
	 */
	public AlarmNotifier() throws Exception
	{
		int port = 0;
		String mp = System.getProperty("control.port", "9081");
		if( Tools.isNumeric(mp) ) port = Integer.parseInt(mp);
			try
			{
				zookeeper = Zookeeper.getInstance("127.0.0.1:"+port);
			}
			catch(Exception e)
			{
				throw new Exception("Failed to build the zookeeper", e);
			}
        //启动数据库连接管理
        String dbDriver = System.getProperty("cos.jdbc.driver", "org.h2.Driver");
        String dbUser = System.getProperty("cos.jdbc.user", "sa");
        String dbPassword = System.getProperty("cos.jdbc.password", "");
        String dbUrl = System.getProperty("cos.jdbc.url", "jdbc:h2:tcp://localhost/../h2/cos");
		ConnectionPool.connect(dbUrl, dbDriver, dbUser, dbPassword);
		Log.msg("Succeed to initialize "+dbDriver+" from "+dbUrl);
		users = SysuserClient.list();
		for(Sysuser user : users)
		{
			usernames.put(user.getUsername(), user);
			realnames.put(user.getRealname(), user);
		}
		Log.msg("Succeed to get all users "+users.size());
		StringBuffer logs = new StringBuffer();
		loadConfig(zookeeper, users, config, logs);
		Log.msg(logs.toString());
	}
	
	public void close()
	{
		if( zookeeper != null )
			zookeeper.close();
	}

	public static String[][] Versions = {
		{"3.16.9.13",	"优化不能发送告警邮件的BUG。"},
		{"3.16.9.26",	"优化策略3分钟内的严重以下的告警不通知，增加日志。"},
		{"3.16.10.6",	"新的告警通知模板。"},
		{"3.16.10.13",	"解决邮件未发送问题。"},
		{"3.16.10.21",	"解决告警产生后触发通知因为3分钟延迟限制造成的通知延迟发送。"},
		{"3.16.10.24",	"解决告警系统通知的不合理显示问题。"},
		{"3.17.5.11",	"解决持续3个月不能发告警邮件的错误。"},
		{"3.17.5.12",	"根据系统配置设置邮件模板皮肤颜色。"},
		{"3.17.8.3",	"解决告警配置前后台不一致问题；增加告警负责人和联系方式字段；增加用户群组发送告警。"},
		{"3.18.11.28",	"解决读取告警配置异常的问题。"},
	};
	public static void main( String[] args )
    {
        //启动日志管理器
        Log.getInstance().setSubroot(ModuleID);
        Log.getInstance().setDebug(System.getProperty("cos.jdbc.driver")==null||System.getProperty("cos.jdbc.driver").toString().isEmpty());
        Log.getInstance().setLogable(true);
        Log.getInstance().start();
        String Version = Versions[Versions.length-1][0];
      	System.out.println("#Version:"+Version);
        StringBuffer info = new StringBuffer("================================================================================================");
		info.append("\r\n"+ModuleID+" "+Version);
		info.append("\r\n\tThe dir of user is "+System.getProperty("user.dir"));
		info.append("\r\n\tCopyright (C) 2008-2019 Focusnt.  All Rights Reserved.");
		info.append("\r\n================================================================================================");
		Log.msg( info.toString() );
        try
        {
        	AlarmNotifier notifier = new AlarmNotifier();
        	AlarmDao dao = new AlarmDao();
        	notifier.execute(dao);
        	dao.close();
        	notifier.close();
        	Log.msg("Finish " + ModuleID + " process.");
        }
        catch (Exception e)
        {
        	Log.err(e);
        }
        finally{
    		ConnectionPool.disconnect();
        }
    }
    
    /**
     * 执行告警
     */
    private void execute(AlarmDao dao) throws Exception
    {
    	List<Sysalarm> list = dao.getAllUnackAlarms();
    	StringBuffer log = new StringBuffer();
    	int countDiscard = 0, countNotify = 0, countEmail = 0, countSms = 0;
    	dao.prepareNotify();
    	for(Sysalarm alarm : list)
    	{
			String key = AlarmType.getLabel(alarm.getType())+"_"+alarm.getSysid();
			AlarmSeverity severity = AlarmSeverity.get(alarm.getSeverity());
			log.append("\r\n\t["+alarm.getAlarmid()+"]["+alarm.getSysid()+"]["+alarm.getType()+"]["+alarm.getSeverity()+"] "+alarm.getFrequnce()+" "+alarm.getTitle()+", "+alarm.getEventTime());
			if( severity == null )
			{
				log.append("\r\n\t\tUnknown severity(freq="+alarm.getFrequnce()+", time="+Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", alarm.getClearTime().getTime())+").");
				countDiscard += 1;
				dao.notified(alarm.getAlarmid(), alarm.getFrequnce()+1, alarm.getActiveStatus());
    			continue;
			}
			key += severity.getIntValue();
			Config cfg = config.get(key);
    		if( cfg == null ){
    			log.append("\r\n\t\tNot found config("+key+", freq="+alarm.getFrequnce()+", time="+Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", alarm.getClearTime().getTime())+").");
				countDiscard += 1;
//				dao.notified(alarm.getAlarmid(), alarm.getFrequnce()+1, alarm.getActiveStatus());
    			continue;
    		}
    		Date time = alarm.getClearTime();
    		Long m = null;
    		if( time != null ) m = (System.currentTimeMillis() - time.getTime())/Tools.MILLI_OF_MINUTE;
    		if( m != null && m < cfg.getPeriod() )
    		{
    			log.append("\r\n\t\tHas been triggered at "+Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", time.getTime())+", trigger after "+(cfg.getPeriod()-m)+" minutes.");
				countDiscard += 1;
    			continue;
    		}
    		if(	cfg.getFrequency() <= alarm.getFrequnce() )
    		{
    			log.append("\r\n\t\tFrequency has exceeded("+alarm.getFrequnce()+"/"+cfg.getFrequency()+").");
				countDiscard += 1;
    			continue;
    		}
    		
    		if( alarm.getFrequnce() == 0 && severity.getIntValue() > 1 && (System.currentTimeMillis() - alarm.getEventTime().getTime()) < 30000 )
    		{//低级别的告警，告警产生后3分钟不发告警
//				dao.notified(alarm.getAlarmid(), -1, alarm.getActiveStatus());
    			log.append("\r\n\t\tWait 3 minutes("+Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", alarm.getEventTime().getTime())+", "+cfg.getPeriod()+" minutes).");
				countDiscard += 1;
    			continue;
    		}
    		//发送系统通知
    		countNotify += sendSystemNotify(alarm, cfg, severity, log);
    		if( severity.getIntValue() < AlarmSeverity.YELLOW.getIntValue() )
    			countEmail += sendSystemEmail(alarm, cfg, severity, log);
//    		if( severity.getIntValue() < AlarmSeverity.ORANGE.getIntValue() )
//    			countSms += sendSystemSMS(alarm, cfg, severity, log);
			dao.notified(alarm.getAlarmid(), alarm.getFrequnce()==-1?1:alarm.getFrequnce()+1, alarm.getActiveStatus());
    	}
    	dao.batch();
    	dao.commit();
    	Log.msg("Handle "+list.size()+" alarms from "+config.size()+" configs, total "+countNotify+" notifies, "+countEmail+" emails, "+countSms+" sms, "+countDiscard+" discard."+log);
    }
    
    /**
     * 发送系统短信
     * @param alarm
     * @param cfg
     * @param severity
    public int sendSystemSMS(Alarm alarm, Config cfg, AlarmSeverity severity, StringBuffer log)
    {
    	ArrayList<User> subscribers = cfg.getSubscribers();
    	StringBuffer to = new StringBuffer();
    	for(User subscriber : subscribers)
    	{
    		if( to.length() > 0 ) to.append(";");
    		if( subscriber.getMobile() != null && Tools.isMobileNumber(subscriber.getMobile()))
    			to.append(subscriber.getEmail());
    	}
    	String wsUrl = ConfigUtil.getString("ws.sms", "http://127.0.0.1:8080/services/SMSService");
    	String ecId = alarm.getModule();//不填为空
		String appId = ConfigUtil.getString("cos.sms.appid", "Sysalarm_Sms");//向系统维护人员确认应用ID
		String extCode = "";//为空
		String destAddr = to.toString();//所携带的号码一般配置会是5~20,系统控制不超过50。
		boolean requestReport = true;
		String content = "【"+alarm.getType()+"】"+alarm.getTitle();
//		String sismsid = SMSClient.sendSms(wsUrl, ecId, appId, extCode, destAddr, requestReport, content);
//		log.append("\r\n\tSubmit sms to "+to+", the sismsid is "+sismsid);
		return 1;
    }
     */
    /**
     * 发送系统通知
     * @param alarm
     * @param cfg
     */
    public int sendSystemNotify(Sysalarm alarm, Config cfg, AlarmSeverity severity, StringBuffer log)
    {
    	ArrayList<Sysuser> subscribers = cfg.getSubscribers();
    	StringBuffer to = new StringBuffer();
    	try
    	{
	    	for(Sysuser subscriber : subscribers)
	    	{
	    		String account = subscriber.getUsername();
	    		if( to.length() > 0 ) to.append(";");
	    		to.append(account);
	    		String contextlink = null;
	        	String portalUrl = ConfigUtil.getString("cos.web.url", "http://opt.efida.com.cn/");
				contextlink = portalUrl+"sysalarm!preview.action?id="+alarm.getAlarmid();
	    		Sysnotify notify = new Sysnotify();
	    		notify.setUseraccount(account);
	    		notify.setFilter(alarm.getType());//消息类型
	    		notify.setNotifytime(new Date());
	    		notify.setPriority(severity.getIntValue());
	    		notify.setTitle(alarm.getTitle());
	    		notify.setIcon(portalUrl+"images/alarm.png");
	    		notify.setContext("");
	    		notify.setContextlink(contextlink);
	    		notify.setAction("确认告警");
	    		notify.setActionlink("sysalarm!ack.action?id="+alarm.getAlarmid());
	    		SysnotifyClient.send(notify);
	    	}
			log.append("\r\n\t\tSumit notify to "+to);
    	}
    	catch(Exception e)
    	{
			log.append("\r\n\t\tFailed to notify for "+e);
    	}
		return subscribers.size();
    }
    
    /**
     * 发送系统邮件
     * @param alarm
     * @param cfg
     * @param severity
     */
    public int sendSystemEmail(Sysalarm alarm, Config cfg, AlarmSeverity severity, StringBuffer log)
    {
    	ArrayList<Sysuser> subscribers = cfg.getSubscribers();
    	StringBuffer to = new StringBuffer();
    	StringBuffer cc = new StringBuffer();
    	StringBuffer receivers = to;
    	StringBuffer responser = new StringBuffer();
    	Log.msg("alarm.getResponser()="+alarm.getResponser());
    	if( alarm.getResponser() != null )
    	{
    		Sysuser user = usernames.get(alarm.getResponser());
    		if( user != null )
    		{
    			to.append(user.getEmail());
    			responser.append(user.getRealname());
    			receivers = cc;
        	}
    		if( user == null )
    		{
    			user = realnames.get(alarm.getResponser());
        		if( user != null )
        		{
        			to.append(user.getEmail());
        			responser.append(user.getRealname());
        			receivers = cc;
            	}			
    		}
    	}
    	for(Sysuser subscriber : subscribers)
    	{
    		if( subscriber.getEmail() != null && subscriber.getEmail().indexOf('@') != -1)
    		{
        		if( receivers.length() > 0 ) receivers.append(";");
    			receivers.append(subscriber.getEmail());
    		}
    		if( alarm.getResponser() == null )
    		{
        		if( responser.length() > 0 ) responser.append(",");
    			responser.append(subscriber.getRealname());
    		}
    	}
    	Log.msg("to:"+to.toString());
    	Log.msg("cc:"+cc.toString());
		InputStream is = AlarmNotifier.class.getResourceAsStream("/com/focus/cos/template_mail_alarm.html");
		try
		{
	    	JSONObject sysConfig = getEmailConfig("/cos/config/system", zookeeper);
	    	JSONObject cmpConfig = getEmailConfig("/cos/config/cmp/"+alarm.getSysid(), zookeeper);
	    	String portalUrl = sysConfig.has("PortalUrl")?sysConfig.getString("PortalUrl"):"";
			String template = new String(IOHelper.readAsByteArray(is), "UTF-8");
			String moduleName = sysConfig.getString("SysName");
			String logoUrl = portalUrl+"images/logo.png";
			String subject = moduleName+"告警通知("+alarm.getSeverity()+")";
			if( !"Sys".equals(alarm.getSysid()) )
			{
				if( cmpConfig != null )
				{
					moduleName = cmpConfig.getString("name");
					subject = moduleName+"告警通知("+alarm.getSeverity()+")";
					logoUrl = portalUrl+"images/cmp/"+alarm.getSysid()+".png";
				}
			}
			template = template.replaceAll("%module.name%", moduleName);
			template = template.replaceAll("%logo.url%", logoUrl);
			template = template.replaceAll("%logo.title%", "登录");
			template = template.replaceAll("%url.login%", portalUrl);
			template = template.replaceAll("%sys.name%", sysConfig.getString("SysName"));
			template = template.replaceAll("%responser%", responser.toString());
			template = template.replaceAll("%you%", subscribers.size()>1?"你们":"你");
			template = template.replaceAll("%module.title%", moduleName);
			template = template.replaceAll("%title%", alarm.getTitle());
			template = template.replaceAll("%type%", alarm.getType());
			template = template.replaceAll("%severity%", alarm.getSeverity());
			template = template.replaceAll("%severity.color%", AlarmSeverity.get(alarm.getSeverity()).toString());
			String color = "#fff";
			if( alarm.getSeverity().equals(AlarmSeverity.BLACK) ) color = "red";
			else if( alarm.getSeverity().equals(AlarmSeverity.YELLOW) ) color = "blue";
			template = template.replaceAll("%severity.font.color%", color);
			if( "127.0.0.1".equalsIgnoreCase(alarm.getDn()) ||
				"unknown".equalsIgnoreCase(alarm.getDn()))
			{
				if( alarm.getServerkey() != null && !alarm.getServerkey().isEmpty() )
				{
					String zkpath = "/cos/data/apiproxy/"+Tools.encodeMD5(alarm.getServerkey());
					JSONObject apiproxy = this.zookeeper.getJSONObject(zkpath);
					if( apiproxy != null && apiproxy.has("ip") && apiproxy.has("port") )
					{
						alarm.setDn(apiproxy.getString("ip")+":"+apiproxy.getInt("port"));
					}
				}
			}
			template = template.replaceAll("%dn%", alarm.getDn());
			template = template.replaceAll("%time%", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", alarm.getEventTime().getTime()));
			template = template.replaceAll("%cause%", alarm.getCause());
			template = template.replaceAll("%programer%", "<span style='color:gray'>N/A</span>");
			template = template.replaceAll("%content%", alarm.getText());
			template = template.replaceAll("%sys.descr%", sysConfig.getString("SysDescr"));
			template = template.replaceAll("%sys.contact%", sysConfig.getString("SysContact"));
			template = template.replaceAll("%sys.email%", sysConfig.getString("POP3Username"));

			final HashMap<String, String> themes = new HashMap<String, String>();
			themes.put("default", "#18bc9c");
			themes.put("blue", "#23bab5");
			themes.put("honey_flower", "#674172");
			themes.put("razzmatazz", "#DB0A5B");
			themes.put("ming", "#336E7B");
			themes.put("yellow", "#ffd800");
			String theme = "#18bc9c";
			if( sysConfig != null )
			{
				theme = sysConfig.has("Theme")?sysConfig.getString("Theme"):"default";
				theme =  themes.get(theme);
			}
			template = template.replaceAll("%skin.color%", theme);

			if( "设备告警".equals(alarm.getType()) )
			{
				template = template.replaceAll("%chart.pannel%", "");
				if( "SystemMonitor_Storage".equals(alarm.getId()) )
				{
					template = template.replaceAll("%chart.url%", portalUrl+"monitorload!serverchart.action?host="+alarm.getDn()+"&type=3");
				}
				else if( "SystemMonitor_Memory".equals(alarm.getId()) )
				{
					template = template.replaceAll("%chart.url%", portalUrl+"monitorload!serverchart.action?host="+alarm.getDn()+"&type=1");
				}
				else if( "SystemMonitor_Cpu".equals(alarm.getId()) )
				{
					template = template.replaceAll("%chart.url%", portalUrl+"monitorload!serverchart.action?host="+alarm.getDn()+"&type=0");
				}
				template = template.replaceAll("%chart.height%", "220");
			}
			else
			{
				template = template.replaceAll("%chart.pannel%", "none");
			}

			Sysemail outbox = SysemailClient.send(
                 to.toString(),
                 cc.toString(),
                 subject,
                 "html=="+template,
                 null,
                 alarm.getSysid());
			log.append("\r\n\t\tSubmit email to "+to+", id is "+outbox.getEid());
	    	return 1;
		}
		catch (Exception e)
		{
			Log.err("Failed to send the email of alarm to "+to+" for "+e.getMessage(), e);
			log.append("\r\n\t\tFailed to send the email of alarm to "+to+" for "+e.getMessage());
			return 0;
		}
    }

    /**
     * 发送微信
     * @param alarm
     * @param cfg
     * @param severity
     * @return
     */
    public int sendSystemWeixin(Sysalarm alarm, Config cfg, AlarmSeverity severity)
    {
    	return 1;
    }
}
