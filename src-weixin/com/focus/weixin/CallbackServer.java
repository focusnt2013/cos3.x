package com.focus.weixin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.http.HttpServletResponse;

import org.apache.zookeeper.data.Stat;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;

import com.focus.cos.api.AlarmSeverity;
import com.focus.cos.api.AlarmType;
import com.focus.cos.api.Sysalarm;
import com.focus.cos.api.SysalarmClient;
import com.focus.util.Base64X;
import com.focus.util.HttpUtils;
import com.focus.util.Log;
import com.focus.util.MongoX;
import com.focus.util.Tools;
import com.focus.util.Zookeeper;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;

/**
 * 微信开发者模式服务端程序
 * @author focus
 *
 */
public class CallbackServer implements Runnable
{
	public static boolean Debug = true;
	public static String ModuleID = "CallbackServer";
	private static JSONObject Config;//配置的启动相关参数
	private static JSONObject Datasource;//配置的启动相关参数
	private static String Sysid = "Sys";//配置的ZK路径
	private static Server Server = null;
	private JSONObject accessToken;// 
	private String configpath;//配置的ZK路径
	private Zookeeper zookeeper;
	private int versionConfig;//配置的版本
	private int versionMenu = -1;//菜单配置的版本
	private String zkaddr;

	
	public static String[][] Versions = {
		{"3.16.9.11",	"初始版本"},
		{"3.18.5.15",	"没有配置菜单不做删除菜单操作"},
		{"3.18.5.16",	"获取自定义菜单数据;重构获取微信公众号统计数据的方法"},
		{"3.18.5.17",	"解决图文消息统计中去重问题;将令牌过期处理时间提前15分钟"},
		{"3.18.5.18",	"每次获取统计数据都从游标日期往前推7天"},
	};
	
	/**
	 * 得到ZK的操作对象句柄
	 * @return
	 * @throws Exception
	 */
	private Zookeeper getZooKeeper() throws Exception
	{
		if( zookeeper != null && zookeeper.isConnected() ){
			return zookeeper;
		}
		if( zookeeper != null ){
			zookeeper.close();
		}
		zookeeper = Zookeeper.getInstance(zkaddr);
		return zookeeper;
	}
	/**
	 * 告警通知模块构造程序
	 * @throws Exception
	 */
	public CallbackServer(String zkaddr, String configpath, String classz) throws Exception
	{
		if( !configpath.startsWith("/cos/config/modules/") )
		{
			throw new Exception("Unknown config "+configpath);
		}
		this.zkaddr = zkaddr;
		this.zookeeper = this.getZooKeeper();
		Stat stat = zookeeper.exists(configpath, false); 
		if( stat == null )
		{
			throw new Exception("Not found config from "+configpath);
		}
		this.configpath = configpath;
		versionConfig = stat.getVersion();
		Config = zookeeper.getJSONObject(configpath, true);
		int i = configpath.lastIndexOf("/weixin");
		Sysid = configpath.substring(0, i);
		i = Sysid.lastIndexOf("/");
		Sysid = configpath.substring(i+1);
		Datasource = zookeeper.getJSONObject(Config.getString("datasource"), true);
		String args[] = Tools.split(Datasource.getString("dbaddr"), ":");
		String mongoaddr = args[0];
		int mongoport = Integer.parseInt(args[1]);
		String dbusername = Datasource.getString("dbusername");
		String dbpassword = new String(Base64X.decode(Datasource.getString("dbpassword")));
		String dbname = Datasource.getString("dbname");
		
		String weixinno = Config.getString("weixinno");
		String appId = Config.getString("appId");
		String secret = Config.getString("secret");
		String name = Config.getString("name");
//		String ip = Config.getString("ip");
		int port = Config.getInt("port");
		String type = Config.getString("type");
		String token = Config.getString("token");
		String encodingAESKey = Config.has("encodingAESKey")?Config.getString("encodingAESKey"):"";
		String className = Config.has("class")?Config.getString("class"):null;

        String Version = Versions[Versions.length-1][0];
        System.out.println("#Version:"+Version);
        StringBuffer info = new StringBuffer("================================================================================================");
		info.append("\r\nVersion "+Version);
		info.append("\r\n\tCopyright (C) 2008-2018 Focusnt.  All Rights Reserved.");
		info.append("\r\n\tconfig:"+configpath+"("+versionConfig+")");
		info.append("\r\n\tname:"+weixinno);
		info.append("\r\n\tname:"+name);
		info.append("\r\n\tappId:"+appId);
		info.append("\r\n\tsecret:"+secret);
		info.append("\r\n\tport:"+port);
		info.append("\r\n\ttype:"+type);
		info.append("\r\n\ttoken:"+token);
		info.append("\r\n\tencodingAESKey:"+encodingAESKey);
		info.append("\r\n\tclass:"+className);
		
		info.append("\r\n\tmongodb.ip:"+mongoaddr);
		info.append("\r\n\tmongodb.port:"+mongoport);
		info.append("\r\n\tmongodb.username:"+dbusername);
		info.append("\r\n\tmongodb.password:*****");
		info.append("\r\n\tmongodb.database:"+dbname);
		info.append("\r\n================================================================================================");
		Log.msg( info.toString() );
		if( className == null || className.isEmpty() )
		{
			className = DefaultCallbackServlet.class.getName();
		}
		MongoX.setPort(mongoport);
		MongoX.setAddress(mongoaddr);
		MongoX.setUsername(dbusername);
		MongoX.setPassword(dbpassword);
		MongoX.setDatabase(dbname);

		// 启动accesstoken定时获取线程
		Server = new Server(port);
		Server.setStopAtShutdown(true);
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");

		Thread.setDefaultUncaughtExceptionHandler(new com.focus.weixin.CallbackUncaughtExceptionHandler());
		Log.msg("...listener "+port);
		Class<?> cls = Class.forName(className);
 	    Constructor<?> constructor = cls.getConstructor(CallbackServer.class);     //主要就是这句了
 	    CallbackServlet servlet = (CallbackServlet)constructor.newInstance(this);
		context.addServlet(new ServletHolder(servlet), "/callback");
		Server.setHandler(context);
		Server.start();
		// 当程序被关闭的时候钩子会被回调
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			public void run()
			{
				try
				{
					Server.stop();
					Log.msg("Stop callback server.");
				} 
				catch (Exception e)
				{
					Log.msg("Failed stop callback server "+e.getMessage());
				}
			}
		});
	}
	
	/**
	 * 是否激活多客服
	 * @return
	 */
	public boolean isCustom()
	{
		if( Config == null || !Config.has("custom") ) return false;
		return Config.getBoolean("custom");
	}
	/**
	 * 返回得到微信号
	 * @return
	 */
	public String getWeixinno()
	{
		if( Config == null || !Config.has("weixinno") ) return "";
		return Config.getString("weixinno");
	}
	/**
	 * 返回得到微信号
	 * @return
	 */
	public String getToken()
	{
		if( Config == null || !Config.has("token") ) return "";
		return Config.getString("token");
	}
	/**
	 * 返回得到微信号
	 * @return
	 */
	public String getEncodingAESKey()
	{
		if( Config == null || !Config.has("encodingAESKey") ) return null;
		return Config.getString("encodingAESKey");
	}
	/**
	 * 返回得到微信号
	 * @return
	 */
	public String getAccessToken()
	{
		if( this.accessToken == null || !this.accessToken.has("access_token") ) return null;
		return accessToken.getString("access_token");
	}
	
	/**
	 * 定期更新微信的token签名
	 */
	public void run()
	{
		String appId = Config.getString("appId");
		String secret = Config.getString("secret");
		Log.msg("Begin listener access-token.");
		JSONObject theAccessToken;
		StringBuffer log = new StringBuffer();
		while((theAccessToken=getAccessToken(appId, secret, log))!=null)
		{
			if( theAccessToken != this.accessToken )
			{
				this.accessToken = theAccessToken;
				String token = theAccessToken.getString("access_token");
				Log.msg("Get the ip of callback from weixin...");
				this.getCallbackip(token, log);
				Log.msg("Fetch the ticket of JSAPI from weixin...");
				fetchJSAPITicket(token, log);
				Log.msg("Synch the data of cube from weixin...");
				datacube(token, "getusercumulate", log);
				datacube(token, "getusersummary", log);
				datacube(token, "getarticlesummary", log);
				datacube(token, "getarticletotal", log);
				Log.msg("Synch the data of users from weixin...");
				while(getUsers(token, log)){
					synchronized (this)
					{
						try
						{
							this.wait(3000);//取一次用户间隔3秒
						}
						catch (InterruptedException e)
						{
						}
					}
				}
				log.append("\r\n\tTrigger to refresh at "+this.accessToken.getString("expire_date"));
				Log.msg("************************************************************************************************\r\n"+log.toString());
				log = new StringBuffer();
			}
			this.getMenu(this.getAccessToken());
			this.setMenu(this.getAccessToken());
			synchronized (this)
			{
				try
				{
					this.wait(Tools.MILLI_OF_MINUTE);//1分钟检查一次
				}
				catch (InterruptedException e)
				{
				}
			}
		}
		if( zookeeper != null )
		{
			zookeeper.close();
			zookeeper = null;
		}

		Log.war("Quite.");
		System.exit(0);
	}
	
	/**
	 * 
	 * @param response
	 * @return
	 */
	public static void rsp404(HttpServletResponse response)
	{
		try
		{
			response.sendRedirect("http://115.29.243.100/ecw/404.png");
		}
		catch (IOException e)
		{
			Log.err(e);
		}
	}
	
	/**
	 * 从微信服务器获取AccessToken
	 * @param appid
	 * @param secret
	 * @return
	 * @throws Exception
	 */
	public JSONObject getAccessToken(String appid, String secret, StringBuffer log)
	{
		try
		{
			if( zookeeper == null ){
				zookeeper = this.getZooKeeper();
				Stat stat = zookeeper.exists(configpath, false); 
				if( stat == null )
				{
					Log.err("Not found config from "+configpath);
					return null;
				}
				if( stat.getVersion() != this.versionConfig )
				{
					Log.war("Found config change("+stat.getVersion()+"/"+this.versionConfig+") from "+configpath);
					return null;
				}
			}
			if(accessToken != null && //访问令牌存在
			   accessToken.has("expire_time") && //到期时间存在，同时到期时间前一分钟大于当前时间
			   accessToken.getLong("expire_time") - 15*Tools.MILLI_OF_MINUTE > System.currentTimeMillis() )
			{//上次获取时间之后超时时间15分钟内重新更新token，否则不用重新更新
				return accessToken;
			}
			log.append("Refresh the token of access before "+(accessToken!=null?(accessToken.has("expire_date")?accessToken.getString("expire_date"):"error("+accessToken+")"):"first"));
			String path = configpath+"/access_token";
			String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="+appid+"&secret=" + secret;
			Document doc = HttpUtils.crwal(url, "utf-8");
			JSONObject dbo = new JSONObject(doc.body().text());
			long ts = System.currentTimeMillis();
			dbo.put("get_time", ts);
			dbo.put("get_date", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", ts));
			if(dbo.has("access_token"))
			{
				dbo.put("expire_time", ts + dbo.getInt("expires_in")*1000);
				dbo.put("expire_date", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", dbo.getLong("expire_time")));
				log.append("\r\n\tSucceed to get access_token "+dbo);
				SysalarmClient.autoconfirm("Sys", ModuleID+"_access_token", "已成功获取访问令牌");
			}
			else
			{
				log.append("\r\n\tFailed to get access_token for " + dbo);
				handleException("获取微信公众号开发者模式访问令牌失败",
					"获取微信公众号开发者模式访问令牌失败返回错误码"+dbo.getInt("errcode")+", "+dbo.getString("errmsg"), 
					dbo.getString("errmsg"), ModuleID+"_access_token", AlarmType.S, AlarmSeverity.RED);
			}
			Stat stat = zookeeper.exists(path, false); 
			if( stat == null )
			{
				zookeeper.create(path, dbo.toString().getBytes("UTF-8"));
			}
			else
			{
				zookeeper.setData(path, dbo.toString().getBytes("UTF-8"), stat.getVersion());
			}
			return dbo.has("access_token")?dbo:null;
		} 
		catch (Exception e)
		{
			Log.err("Failed to get access_token for exception", e);
			handleException("获取微信公众号开发者模式访问令牌异常",
				"获取微信公众号开发者模式访问令牌失败出现异常"+e.getMessage(), 
				e, ModuleID+"_access_token", AlarmType.S, AlarmSeverity.RED);
		}
		return null;
	}
	
	/**
	 * 
	 * @param accessToken
	 */
	private void getCallbackip(String token, StringBuffer log)
	{
		String path = configpath+"/callbackip";
		String url = "https://api.weixin.qq.com/cgi-bin/getcallbackip?access_token="+token;
		try
		{
			Document doc = HttpUtils.crwal(url, "utf-8");
			JSONObject dbo = new JSONObject(doc.body().text());
			long ts = System.currentTimeMillis();
			dbo.put("get_time", ts);
			dbo.put("get_date", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", ts));
			if(dbo.has("ip_list"))
			{
				log.append("\r\n\tSucceed to get callbackip "+dbo.toString());
			}
			else
			{
				log.append("\r\n\tFailed to get callbackip for " + dbo);
			}
			Stat stat = zookeeper.exists(path, false); 
			if( stat == null )
			{
				zookeeper.create(path, dbo.toString().getBytes("UTF-8"));
			}
			else
			{
				zookeeper.setData(path, dbo.toString().getBytes("UTF-8"), stat.getVersion());
			}
		} 
		catch (Exception e)
		{
			log.append("\r\n\tFailed to get callbackip for exception"+e);
		}
	}
	/**
	 * 获取JSAPITicket
	 * @param accessToken
	 * @return
	 * @throws Exception
	 */
	public void fetchJSAPITicket(String token, StringBuffer log)
	{
		String path = configpath+"/jsapi_ticket";
		String url = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token="+token+"&type=jsapi";
		try
		{
			Document doc = HttpUtils.crwal(url, "utf-8");
			JSONObject dbo = new JSONObject(doc.body().text());
			long ts = System.currentTimeMillis();
			dbo.put("get_time", ts);
			dbo.put("get_date", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", ts));
			if(dbo.has("ticket"))
			{
				dbo.put("expire_time", ts + dbo.getInt("expires_in")*1000);
				dbo.put("expire_date", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", dbo.getLong("expire_time")));
				log.append("\r\n\tSucceed to fetch jsapi_ticket "+dbo.toString());
				SysalarmClient.autoconfirm("Sys", ModuleID+"_jsapi", "已正常获取");
			}
			else
			{
				log.append("\r\n\tFailed to fetch jsapi_ticket for " + dbo);
				handleException("获取微信公众号开发者模式JSAPI失败",
						"获取微信公众号开发者模式JSAPI失败返回错误码"+dbo.getInt("errcode")+", "+dbo.getString("errmsg"), 
						dbo.getString("errmsg"), ModuleID+"_jsapi", AlarmType.S, AlarmSeverity.RED);
			}
			Stat stat = zookeeper.exists(path, false); 
			if( stat == null )
			{
				zookeeper.create(path, dbo.toString().getBytes("UTF-8"));
			}
			else
			{
				zookeeper.setData(path, dbo.toString().getBytes("UTF-8"), stat.getVersion());
			}
		} 
		catch (Exception e)
		{
			log.append("\r\n\tFailed to fetch jsapi_ticket for exception "+e);
			handleException("获取微信公众号开发者模式JSAPI异常",
					"获取微信公众号开发者模式JSAPI出现异常"+e.getMessage(), 
					e, ModuleID+"_jsapi", AlarmType.S, AlarmSeverity.RED);
		}
	}

	/**
	 * 
	 * @param openid
	 * @param user
		subscribe	用户是否订阅该公众号标识，值为0时，代表此用户没有关注该公众号，拉取不到其余信息。
		openid	用户的标识，对当前公众号唯一
		nickname	用户的昵称
		sex	用户的性别，值为1时是男性，值为2时是女性，值为0时是未知
		city	用户所在城市
		country	用户所在国家
		province	用户所在省份
		language	用户的语言，简体中文为zh_CN
		headimgurl	用户头像，最后一个数值代表正方形头像大小（有0、46、64、96、132数值可选，0代表640*640正方形头像），用户没有头像时该项为空。若用户更换头像，原有头像URL将失效。
		subscribe_time	用户关注时间，为时间戳。如果用户曾多次关注，则取最后关注时间
		unionid	只有在用户将公众号绑定到微信开放平台帐号后，才会出现该字段。
		remark	公众号运营者对粉丝的备注，公众号运营者可在微信公众平台用户管理界面对粉丝添加备注
		groupid	用户所在的分组ID（兼容旧的用户分组接口）
		tagid_list	用户被打上的标签ID列表	 
	 */
	public org.bson.Document getUserInfo(String openid)
	{
		String url = "https://api.weixin.qq.com/cgi-bin/user/info?access_token="+getAccessToken()+"&openid="+openid+"&lang=zh_CN";
		try
		{
			MongoCollection<org.bson.Document> col_user = MongoX.getDBCollection(getWeixinno()+"_users");
			Document doc = HttpUtils.crwal(url, "utf-8");
			org.bson.Document user = org.bson.Document.parse(doc.body().text());
			long ts = System.currentTimeMillis();
			user.put("get_time", ts);
			user.put("get_date", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", ts));
			user.put("bind", 1);
			org.bson.Document old = MongoX.findOne(col_user, new BasicDBObject("openid", openid));
			if( old != null )
			{
				MongoX.update(col_user, new BasicDBObject("_id", old.getObjectId("_id")), user);
			}
			else
			{
				col_user.insertOne(user);
			}
			return user;
		} 
		catch (Exception e)
		{
			Log.err("Failed to get the info of user("+openid+") for "+e.getMessage());
			return null;
		}
	}
	/**
	 * 获取用户列表
	 * 公众号可通过本接口来获取帐号的关注者列表，关注者列表由一串OpenID（加密后的微信号，每个用户对每个公众号的OpenID是唯一的）组成。一次拉取调用最多拉取10000个关注者的OpenID，可以通过多次拉取的方式来满足需求。
	 * @param token
	 * @param log
	 * @return 是否还有新的数据
	 */
	public boolean getUsers(String token, StringBuffer log)
	{
		String url = "https://api.weixin.qq.com/cgi-bin/user/get?access_token="+token+"&next_openid=";
		String openid = null;
		String path = configpath+"/getusers";
		JSONObject pathdata = null;
		boolean result = false;
		try
		{
			Stat stat = zookeeper.exists(path, false);
			if( stat != null )
			{
				String json = new String(zookeeper.getData(path, false, stat));
				pathdata = new JSONObject(json);
				if( pathdata.has("next_openid"))
					url += pathdata.getString("next_openid");
			}
			log.append("\r\n\tGet info fo users from "+url);
			Document doc = HttpUtils.crwal(url, "utf-8");
			long ts = System.currentTimeMillis();
			pathdata = new JSONObject(doc.body().text());
			pathdata.put("get_time", ts);
			pathdata.put("get_date", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", ts));
			if(pathdata.has("data") && pathdata.getJSONObject("data").has("openid"))
			{
				pathdata.put("get_time", ts);
				pathdata.put("get_date", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", ts));
				log.append("\r\n\tSucceed to get users total "+(pathdata.has("total")?pathdata.getInt("total"):"0")+", count "+(pathdata.has("count")?pathdata.getInt("count"):"0"));
				SysalarmClient.autoconfirm("Sys", ModuleID+"_getusers", "已正常获取");
				JSONArray array = pathdata.getJSONObject("data").getJSONArray("openid");
				pathdata.getJSONObject("data").remove("openid");
				for(int i = 0; i < array.length(); i++ )
				{
					openid = array.getString(i);
					org.bson.Document user = getUserInfo(openid);
					if( user == null ){
						log.append("\r\n\t\tQuite at "+openid);
						break;
					}
					pathdata.put("next_openid", openid);
					if( user.containsKey("subscribe") && user.getInteger("subscribe") == 1 )
						log.append("\r\n\t\t["+openid+"] "+user.getString("nickname"));
					else
						log.append("\r\n\t\t["+openid+"] unbind.");
				}
				result = pathdata.has("count")&&pathdata.getInt("count")==10000;
			}
			else if( pathdata.has("errcode") )
			{
				log.append("\r\n\tFailed to get users for " + pathdata.toString(4));
				handleException("获取微信公众号用户列表失败",
						"获取微信公众号用户列表失败返回错误码"+pathdata.getInt("errcode")+", "+pathdata.getString("errmsg"), 
						pathdata.getString("errmsg"), ModuleID+"_getusers", AlarmType.S, AlarmSeverity.BLUE);
				result = false;
			}
			else
			{
				log.append(pathdata.toString(4));
				result = true;
			}

			if( pathdata != null )
			{
				try
				{
					stat = zookeeper.exists(path, false);
					if( stat == null )
					{
						zookeeper.create(path, pathdata.toString().getBytes("UTF-8"));
					}
					else
					{
						zookeeper.setData(path, pathdata.toString().getBytes("UTF-8"), stat.getVersion());
					}
				}
				catch (Exception e)
				{
					handleException("获取微信公众号用户列表异常",
							"保存调用微信公众号开发者模式获取微信公众号用户列表结果到Zookeeper异常"+e.getMessage(), 
							e, ModuleID+"_getusers", AlarmType.S, AlarmSeverity.BLUE);
				}
			}
		}
		catch (Exception e)
		{
			log.append("\r\n\tFailed to get users for exception "+e);
			handleException("获取微信公众号用户列表异常",
					"调用微信公众号开发者模式获取微信公众号用户列表接口异常"+e.getMessage(), 
					e, ModuleID+"_getusers", AlarmType.S, AlarmSeverity.BLUE);
		}
		return result;
	}
	/**
	 * 调用微信统计接口
	 * @param token
	 * @param method
	 * @param log
	 */
	private void datacube(String token, String method, StringBuffer log)
	{
		String begin_date = Config.has("begin_date")?Config.getString("begin_date"):null;
		if( begin_date == null ){
			return;
		}
		String url = "https://api.weixin.qq.com/datacube/"+method+"?access_token="+token;
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try
		{
			MongoCollection<org.bson.Document> col_datacube = MongoX.getDBCollection(method.substring(3));
			String path = configpath+"/"+method;
			Stat stat = zookeeper.exists(path, false);
			Calendar calendar = Calendar.getInstance();
			String today = sdf.format(calendar.getTime());
			JSONObject pathdata = null;
			if( stat != null )
			{
				String json = new String(zookeeper.getData(path, false, stat));
				pathdata = new JSONObject(json);
				if( pathdata.has("cursor") ){
					begin_date =  pathdata.getString("cursor");//得到时间游标日期
				}
			}
			else{
				pathdata = new JSONObject();
			}
			calendar.setTime(sdf.parse(begin_date));
			calendar.add(Calendar.DAY_OF_MONTH, -7);//每次都从7天前取一次数据
			String cursor = sdf.format(calendar.getTime());
			log.append(String.format("\r\n\t#datacube!%s begin from %s to cursor %s, the count of database is %s", method, cursor, begin_date, col_datacube.count()));
			boolean getdata = false;
			while(today.compareTo((cursor=sdf.format(calendar.getTime())))>0)
			{
				BasicDBObject req = new BasicDBObject();
				req.put("begin_date", cursor);
				req.put("end_date", cursor);
				calendar.add(Calendar.DAY_OF_MONTH, 1);
				Document doc = HttpUtils.post(url, new HashMap<String, String>(), req.toString().getBytes("UTF-8"));
				JSONObject rsp = new JSONObject(doc.body().text());
				if(rsp.has("list"))
				{
					getdata = true;
					JSONArray array = rsp.getJSONArray("list");
					log.append(String.format("\r\n\t\tSucceed to %s %s rows from %s.", method, array.length(), cursor));
					for(int i = 0; i < array.length(); i++){
						JSONObject obj = array.getJSONObject(i);
						if( obj.has("details") && "getarticletotal".equals(method) ){
							this.setArticleDetails(obj.getJSONArray("details"), obj.getString("msgid"), obj.getString("ref_date"), obj.getString("title"), col_datacube);
						}
						else{
							obj.put("timestamp", System.currentTimeMillis());
							obj.put("weixinno", Config.getString("weixinno"));
							BasicDBObject where = new BasicDBObject("ref_date", obj.getString("ref_date"));
							where.put("weixinno", Config.getString("weixinno"));
							where.put("user_source", obj.getInt("user_source"));
							if( "getarticlesummary".equals(method) ){
								where.put("msgid", obj.getString("msgid"));
								where.remove("ref_date");
							}
							org.bson.Document old = MongoX.findOne(col_datacube, where);
							org.bson.Document data = org.bson.Document.parse(obj.toString());
							if( old != null )
							{
								if( "getarticlesummary".equals(method) ){
									data.put("ref_date", old.getString("ref_date"));
								}
								MongoX.update(col_datacube, new BasicDBObject("_id", old.getObjectId("_id")), data);
							}
							else
							{
								col_datacube.insertOne(data);
							}
							
						}
					}
				}
				else
				{
					log.append("\r\n\t\tFailed to "+method+" from "+cursor+" " + rsp.toString(4));
					log.append("\r\n\t\tTry again after 2 hours from cursor");
					break;
				}
			}
			if( getdata ){
				long ts = System.currentTimeMillis();
				pathdata.put("cursor", cursor);
				pathdata.put("get_time", ts);
				pathdata.put("get_date", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", ts));
				stat = zookeeper.exists(path, false);
				if( stat == null )
				{
					zookeeper.create(path, pathdata.toString().getBytes("UTF-8"));
				}
				else
				{
					zookeeper.setData(path, pathdata.toString().getBytes("UTF-8"), stat.getVersion());
				}
			}
		} 
		catch (Exception e)
		{
			Log.err("Failed to "+method+" for exception", e);
			log.append("\r\n\tFailed to "+method+" for exception "+e);
			handleException("调用微信公众号开发者模式数据统计接口异常", "接口调用异常详情",
				e, ModuleID+"_"+method, AlarmType.S, AlarmSeverity.ORANGE);
		}
	}
	
	/**
	 * 设置详情数据
	 * @param details
	 * @param msgid
	 * @param ref_date
	 * @param title
	 * @param col
	 * @throws Exception 
	 */
	private void setArticleDetails(JSONArray details, String msgid, String ref_date, String title, MongoCollection<org.bson.Document> col) throws Exception{
		JSONObject detail = null;
		JSONObject detail_last = null;
		JSONObject detail_day = null;
		for(int j = 0; j < details.length(); j++){
			detail = details.getJSONObject(j);
			if( detail_last != null ){
				detail_day = new JSONObject();
				detail_day.put("stat_date", detail.getString("stat_date"));
				Iterator<?> iterator = detail.keys();
				while( iterator.hasNext() )
				{
					String key = iterator.next().toString();
					Object val = detail.get(key);
					if( !(val instanceof String) ){
						if( detail_last.has(key) ){
							int count1 = detail.getInt(key);
							int count0 = detail_last.getInt(key);
							detail_day.put(key, count1-count0);
						}
					}
				}
			}
			else{
				detail_day = detail;
			}
			detail_day.put("msgid", msgid);
			detail_day.put("ref_date", ref_date);
			detail_day.put("title", title);
			detail_day.put("weixinno", Config.getString("weixinno"));
			detail_day.put("timestamp", System.currentTimeMillis());
			BasicDBObject where = new BasicDBObject("msgid", msgid);
			where.put("stat_date", detail.getString("stat_date"));
			where.put("weixinno", Config.getString("weixinno"));
			org.bson.Document old = MongoX.findOne(col, where);
			org.bson.Document data = org.bson.Document.parse(detail_day.toString());
			if( old != null )
			{
				MongoX.update(col, new BasicDBObject("_id", old.getObjectId("_id")), data);
			}
			else
			{
				col.insertOne(data);
			}
			detail_last = detail;
		}
	}
	/**
	 * 设置菜单配置
	 * @param access_token
	 * @param menu
	 * @return
	 */
	public void setMenu(String token)
	{
		String url = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token="+ token;
		try 
		{
			String path = configpath+"/menu";
			Stat stat = zookeeper.exists(path, false);
			if( stat == null )
			{
//				url = "https://api.weixin.qq.com/cgi-bin/menu/delete?access_token="+token;
//				HttpUtils.crwal(url);
//				Log.msg("No menus need to config.");
				return;//菜单没有配置
			}
			if( stat.getVersion() == this.versionMenu )
			{
				return;//版本没变化
			}
			versionMenu = stat.getVersion();
			JSONObject menu = new JSONObject(new String(zookeeper.getData(path, false, stat), "UTF-8"));
			Document doc = HttpUtils.post(url, null, menu.toString().getBytes("UTF-8"));

			long ts = System.currentTimeMillis();
			JSONObject dbo = new JSONObject(doc.body().text());
			if(dbo.has("errcode") && dbo.getInt("errcode") == 0)
			{
				dbo.put("set_time", ts);
				dbo.put("set_date", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", ts));
				Log.msg("Succeed to set menu "+dbo.toString(4));
				SysalarmClient.autoconfirm("Sys", ModuleID+"_menu", "已成功设置菜单");
			}
			else
			{
				Log.err("Failed to set menu for " + dbo);
				handleException("调用微信公众号开发者模式自定义菜单接口失败",
					"调用微信公众号开发者模式自定义菜单接口失败返回错误码"+dbo.getInt("errcode")+", "+dbo.getString("errmsg"), 
					dbo.getString("errmsg"), ModuleID+"_menu", AlarmType.S, AlarmSeverity.ORANGE);
			}
			path = configpath+"/menuset";
			stat = zookeeper.exists(path, false); 
			if( stat == null )
			{
				zookeeper.create(path, dbo.toString().getBytes("UTF-8"));
			}
			else
			{
				zookeeper.setData(path, dbo.toString().getBytes("UTF-8"), stat.getVersion());
			}
		}
		catch (Exception e) 
		{
			Log.err("Failed to set menu for exception", e);
			handleException("调用微信公众号开发者模式自定义菜单接口异常",
					"调用微信公众号开发者模式自定义菜单接口异常"+e.getMessage(), 
					e, ModuleID+"_menu", AlarmType.S, AlarmSeverity.ORANGE);
		}
	}

	/**
	 * 设置菜单配置
	 * @param access_token
	 * @param menu
	 * @return
	 */
	public void getMenu(String token)
	{
		String url = "https://api.weixin.qq.com/cgi-bin/get_current_selfmenu_info?access_token="+ token;
		try 
		{
			String current_selfmenu = null;
			String path = configpath+"/current_selfmenu";
			Stat stat = zookeeper.exists(path, false);
			if( stat != null )
			{
				current_selfmenu = new String(zookeeper.getData(path, false, stat));
			}
			Document doc = HttpUtils.crwal(url, "UTF-8");
			JSONObject dbo = new JSONObject(doc.body().text());
			String json = dbo.toString();
			if(!json.equals(current_selfmenu))
			{
				Log.msg("Found the selfmenu from "+url+dbo.toString(4));
			}
			if( stat == null )
			{
				zookeeper.create(path, json.getBytes("UTF-8"));
			}
			else
			{
				zookeeper.setData(path, json.getBytes("UTF-8"), stat.getVersion());
			}
		}
		catch (Exception e) 
		{
			Log.err("Failed to get selfmenu for exception", e);
		}
	}
	
	public static void main(String[] args)
	{
		ModuleID = args[0];
		Log.getInstance().setSubroot(ModuleID);
		Log.getInstance().setDebug(false);
		Log.getInstance().setLogable(true);
		Log.getInstance().start();
		try
		{
			String zkaddr = args[1];
			String configpath = args[2];
			String classz = args[3];
			Thread thread = new Thread(new CallbackServer(zkaddr, configpath, classz));
			thread.start();
			SysalarmClient.autoconfirm("Sys", ModuleID+"_exception", "已正常启动");
			Log.msg("Start callback server.");
			Server.join();
		}
		catch (Exception e)
		{
			Log.err("Failed to startup callback server.", e);
			handleException("微信公众号开发者模式回调程序引擎工作出现异常", 
					"启动微信公众号开发者模式回调程序引擎出现异常:"+e+"，请系统管理员检查相关配置参数是否正确设置。",
					e, ModuleID+"_exception", AlarmType.S, AlarmSeverity.RED);
			System.exit(-1);
		}
	}

	/**
	 * 处理异常
	 * @param e
	 */
	public static void handleException(String title, String text, String cause, String alarmId, AlarmType t, AlarmSeverity s)
	{
		Sysalarm alarm = new Sysalarm();
        alarm.setSysid(Sysid);
        alarm.setResponser(Config!=null?Config.getString("manager"):"");//告警负责
        alarm.setId(alarmId);
        alarm.setSeverity(s.getValue());
        alarm.setType(t.getValue());
        alarm.setCause(cause);
        if( Config != null ) alarm.setTitle("["+Config.getString("name")+"]"+title);
        else alarm.setTitle(title);
        if( Config != null ) text = "["+Config.getString("name")+Config.getString("weixinno")+"]"+text;
        alarm.setText(text);
		SysalarmClient.send(alarm);
	}
	
	public static void handleException(String title, String text, Exception e, String alarmId, AlarmType t, AlarmSeverity s)
	{
		Sysalarm alarm = new Sysalarm();
        alarm.setSysid(Sysid);
        alarm.setResponser(Config!=null?Config.getString("manager"):"");//告警负责
        alarm.setId(alarmId);
        alarm.setSeverity(s.getValue());
        alarm.setType(t.getValue());
        if( e != null ) alarm.setCause(e.getMessage());
        else alarm.setCause("未知原因");
        if( Config != null ) alarm.setTitle("["+Config.getString("name")+"]"+title);
        else alarm.setTitle(title);
		try
		{
			if( e != null )
			{
				ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
				PrintStream ps = new PrintStream(out);
				e.printStackTrace(ps);
		        if( Config != null ) text = "["+Config.getString("name")+Config.getString("weixinno")+"]"+text;
				text = text +"\r\n"+ out.toString();
				ps.close();
			}
	        alarm.setText(text);
		}
		catch(Exception e1)
		{
		}
		SysalarmClient.send(alarm);
	}
}