package com.focus.cos.ops.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.zookeeper.data.Stat;
import org.json.JSONObject;

import com.focus.cos.CosServer;
import com.focus.cos.api.Sysemail;
import com.focus.cos.api.SysemailClient;
import com.focus.cos.api.Sysnotify;
import com.focus.cos.api.Sysuser;
import com.focus.cos.api.SysuserClient;
import com.focus.cos.ops.dao.NotifyDao;
import com.focus.sql.ConnectionPool;
import com.focus.util.ConfigUtil;
import com.focus.util.F;
import com.focus.util.IOHelper;
import com.focus.util.Log;
import com.focus.util.Tools;
import com.focus.util.Zookeeper;

/**
 * 系统消息通知
 * @author focus
 *
 */
public class MessageNotify extends CosServer
{
    public static String ModuleID = "SystemNotify";
	private Zookeeper zookeeper;
	private HashMap<String, JSONObject> config = new HashMap<String, JSONObject>();
	private HashMap<String, Sysuser> users = new HashMap<String, Sysuser>();

	/**
	 * 告警通知模块构造程序
	 * @throws Exception
	 */
	public MessageNotify() throws Exception
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
		ArrayList<Sysuser> listUser = SysuserClient.list();
		for(Sysuser user : listUser )
		{
			users.put(user.getUsername(), user);
		}
		Log.msg("Succeed to get all users "+users.size());
		StringBuffer logs = new StringBuffer();
		String path = "/cos/config/notify";
		Stat stat = zookeeper.exists(path, false); 
		if( stat != null )
		{
			List<String> list = zookeeper.getChildren(path, false);
			logs.append("Succeed to setup "+list.size()+" configs from "+path);
			for( String nodepath : list )
			{
				nodepath = path+"/"+nodepath;
				stat = zookeeper.exists(nodepath, false);
				if( stat == null )
				{
					continue;
				}
				JSONObject c = null;
				String json = new String(zookeeper.getData(nodepath, false, stat), "UTF-8");
				c = new JSONObject(json);
				config.put(c.getString("NAME"), c);
			}
		}
		else
		{
			logs.append("Not found config("+stat+") from "+path);
		}
		Log.msg(logs.toString());
	}
	/*
	 * 将notify通过短信方式发送到目标手机
	private int sendSystemSMS(Notify notify, StringBuffer log)
	{
		User user = users.get(notify.getUseraccount());
		if( user == null )
    	{
    		log.append("\r\n\t\tFailed to submit the sms of system for not found user.");
    		return 0;
    	}
		if( user.getMobile() == null || user.getMobile().isEmpty() || !Tools.isNumeric(user.getMobile()) )
    	{
    		log.append("\r\n\t\tFailed to submit the sms of system for "+user.getUsername()+" not config mobile "+user.getMobile());
    		return 0;
    	}
		String applicationId = ConfigUtil.getString("cos.sms.appid", "Sysalarm_Sms");//向系统维护人员确认应用I
    	String wsUrl = ConfigUtil.getString("ws.sms", "http://127.0.0.1:8080/services/SMSService");
		StringBuffer content = new StringBuffer();
		content.append(notify.getTitle());
		if(content.length()<140)
		{
			content.append(":\n");
			int len = 140-content.length();
			if(notify.getContext() != null)
			{
				if(len>notify.getContext().length())
				{
					content.append(notify.getContext());
				}
				else
				{
					content.append(notify.getContext().substring(0,len));
				}
			}
		}
//		String sismsid = SMSClient.sendSms(wsUrl,"",applicationId,"",user.getMobile(),true,content.toString());
//		log.append("\r\n\t\tSubmit sms to "+user.getMobile()+", the sismsid is "+sismsid);
		return 1;
	}
	 */

	/*
	 *  将notify通过短信发送到目标邮箱
	 */
	public static int sendSystemEmail(Sysnotify notify, String wsurl, String email, String username, JSONObject profile, StringBuffer log)
	{
		String portalUrl = profile.has("PortalUrl")?profile.getString("PortalUrl"):"";
		InputStream is = MessageNotify.class.getResourceAsStream("/com/focus/cos/template_mail_notify.html");
		try
		{
			String template = new String(IOHelper.readAsByteArray(is), "UTF-8");
			String logoUrl = portalUrl+"images/logo.png";
			template = template.replaceAll("%logo.url%", logoUrl);
			template = template.replaceAll("%url.login%", portalUrl);
			template = template.replaceAll("%sys.name%", profile.getString("SysName"));
			template = template.replaceAll("%responser%", username);
			template = template.replaceAll("%title%", notify.getTitle());
			template = template.replaceAll("%time%", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", notify.getNotifytime().getTime()));
			template = template.replaceAll("%filter%", notify.getFilter());
			template = template.replaceAll("%priority%", String.valueOf(notify.getPriority()));
			template = template.replaceAll("%context%", notify.getContext());
			template = template.replaceAll("%sys.descr%", profile.getString("SysDescr"));
			template = template.replaceAll("%sys.contact%", profile.getString("SysContact"));
			template = template.replaceAll("%sys.email%", profile.getString("POP3Username"));

			final HashMap<String, String> themes = new HashMap<String, String>();
			themes.put("default", "#18bc9c");
			themes.put("blue", "#23bab5");
			themes.put("honey_flower", "#674172");
			themes.put("razzmatazz", "#DB0A5B");
			themes.put("ming", "#336E7B");
			themes.put("yellow", "#ffd800");
			String theme = "#18bc9c";
			if( profile != null )
			{
				theme = profile.has("Theme")?profile.getString("Theme"):"default";
				theme =  themes.get(theme);
			}
			template = template.replaceAll("%skin.color%", theme);

			Sysemail outbox = SysemailClient.send(
                 email,
                 "",
                 "系统消息通知("+notify.getFilter()+")",
                 "html=="+template,
                 null);
			log.append("\r\n\t\tSubmit email to "+email+", the id is "+outbox.getEid());
	    	return 1;
		}
		catch (Exception e)
		{
			Log.err(e);
			log.append("\r\n\t\tFailed to send the email of system to "+email+" for "+e.getMessage());
			return 0;
		}
		
	}
	/*
	 * 获取消息、处理方式、目标地址并处理
	 */
	private void execute() throws Exception
	{
		long cursor = 0;
		F cursorFile = new F("../data/notify");
		if( cursorFile.exists() )
		{
			cursor = (Long)IOHelper.readSerializableNoException(cursorFile);
		}
    	StringBuffer log = new StringBuffer();
		//如果最后一次处理的消息ID小于最大ID，说明这个ID区间的消息未被处理，那么处理未处理的消息，处理后的消息ID存档
		int countEmail = 0;
		int countSms = 0;
    	String wsUrl = ConfigUtil.getString("ws.email", "http://127.0.0.1:8080/services/EmailService");
		JSONObject profile = null;
		String path = "/cos/config/system";
		Stat stat = zookeeper.exists(path, false);
		if( stat != null )
		{
			profile = new JSONObject(new String(zookeeper.getData(path, false, stat), "UTF-8"));
		}
		if( profile == null )
		{
			log.append("\r\n\tFailed to submit the email of system for not fond the profile from "+path);
			Log.msg("Handle notifies from cursor "+cursor+", "+config.size()+" configs, total "+countEmail+" emails, "+countSms+" sms."+log);
			return;
		}
		if( !profile.has("SysContact") || !profile.has("SysName") || !profile.has("SysDescr") || !profile.has("POP3Username") )
		{
			log.append("\r\n\tFailed to submit the email of system for not config the property from "+path);
			Log.msg("Handle notifies from cursor "+cursor+", "+config.size()+" configs, total "+countEmail+" emails, "+countSms+" sms."+log);
			return;
		}
		List<Sysnotify> list = NotifyDao.loadNotifiesList(cursor);
		for(Sysnotify notify : list)
		{
			log.append("\r\n\t["+notify.getFilter()+"]["+notify.getNotifytime()+"] "+notify.getTitle());
			if( config.containsKey(notify.getFilter()) )
			{
				JSONObject c = config.get(notify.getFilter());
//				if( c.has("SMS") && c.getBoolean("SMS") )
//					countSms += this.sendSystemSMS(notify, log);
				if( c.has("EMAIL") && c.getBoolean("EMAIL") )
				{
					Sysuser user = users.get(notify.getUseraccount());
					if( user == null )
			    	{
			    		log.append("\r\n\t\tFailed to submit the email of system for not found user.");
			    		continue;
			    	}
			    	if( user.getUsername().equalsIgnoreCase("admin") )
			    	{
			    		user.setEmail(profile.getString("SysContact"));
			    		if( profile.has("SysContactName") )
			    			user.setRealname(profile.getString("SysContactName"));
			    	}
					if( user.getEmail() == null || user.getEmail().isEmpty() || user.getEmail().indexOf("@") == -1 )
			    	{
			    		log.append("\r\n\t\tFailed to submit the email of system for "+user.getUsername()+" not config email "+user.getEmail());
			    		continue;
			    	}
					countEmail += sendSystemEmail(notify, wsUrl, user.getEmail(), user.getRealname(), profile, log);
				}
			}
			else
				log.append("\r\n\t\tNot found the config of filter "+notify.getFilter());
			cursor = notify.getNid();
		}
		IOHelper.writeSerializable(cursorFile, cursor);
    	Log.msg("Handle "+list.size()+" notifies from cursor "+cursor+", "+config.size()+" configs, total "+countEmail+" emails, "+countSms+" sms."+log);
	}
	
	public void close()
	{
		if( zookeeper != null )
			this.zookeeper.close();
		ConnectionPool.disconnect();//关闭数据库连接
	}

	public static String[][] Versions = {
		{"v3.16.10.24",	"针对新的配置参数实现的新版本"},
		{"v3.17.5.12",	"根据系统配置设置邮件模板皮肤颜色。"},
	};
	public static void main(String[] args)
	{
        //启动日志管理器
        Log.getInstance().setSubroot(ModuleID);
        Log.getInstance().setDebug(false);
        Log.getInstance().setLogable(true);
        Log.getInstance().start();
        /**版本管理
         * v3.16.10.24 新版本发布
         */
        String Version = Versions[Versions.length-1][0];
      	System.out.println("#Version:"+Version);
        StringBuffer info = new StringBuffer("================================================================================================");
		info.append("\r\n"+ModuleID+" "+Version);
		info.append("\r\n\tThe dir of user is "+System.getProperty("user.dir"));
		info.append("\r\n\tCopyright (C) 2008-2016 Focusnt.  All Rights Reserved.");
		info.append("\r\n================================================================================================");
		Log.msg( info.toString() );
		try
		{
			MessageNotify manager = new MessageNotify();
			manager.execute();	
        	Log.msg("Finish " + ModuleID + " process.");
        	manager.close();
		}
		catch (Exception e)
		{
			Log.err(e);
		}	
	}
}
