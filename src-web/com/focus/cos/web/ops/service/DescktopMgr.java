package com.focus.cos.web.ops.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.directwebremoting.WebContextFactory;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.focus.cos.api.Status;
import com.focus.cos.api.SysnotifyClient;
import com.focus.cos.api.Sysuser;
import com.focus.cos.api.SysuserClient;
import com.focus.cos.web.common.Kit;
import com.focus.cos.web.common.PathFactory;
import com.focus.cos.web.common.QueryMeta;
import com.focus.cos.web.common.ZKMgr;
import com.focus.cos.web.common.jdbc.JDBCBaseDAO;
import com.focus.cos.web.ops.dao.SysnotifyDAO;
import com.focus.cos.web.ops.vo.DescktopMessage;
import com.focus.cos.web.ops.vo.DescktopNotify;
import com.focus.cos.web.ops.vo.DescktopTips;
import com.focus.cos.web.ops.vo.Sysnotify;
import com.focus.cos.web.service.SvrMgr;
import com.focus.util.HttpUtils;
import com.focus.util.IOHelper;
import com.focus.util.Tools;
import com.focus.util.Zookeeper;

/**
 * 桌面管理
 * @author focus
 *
 */
public class DescktopMgr extends SvrMgr
{
	private static final Log log = LogFactory.getLog(DescktopMgr.class);
	private SysnotifyDAO sysnotifyDao;
	private SysalarmMgr sysalarmMgr;
	private JDBCBaseDAO jdbcBaseDao;
	private ArrayList<DescktopMessage> descktopMessages = new ArrayList<DescktopMessage>();
	
	/**
	 * 记录用户行为记忆
	 * @param key
	 * @param value
	 */
	public synchronized void setUserActionMemory(String username, String key, String value)
	{
		String path = "/cos/data";
		try
		{
			
			ZooKeeper zookeeper = ZKMgr.getZooKeeper();
			Stat stat = zookeeper.exists(path, false); 
			if( stat == null)
			{
				zookeeper.create(path, "系统数据目录".getBytes("UTF-8"), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			path += "/user-action";
			stat = zookeeper.exists(path, false); 
			if( stat == null)
			{
				zookeeper.create(path, "用户操作行为记忆".getBytes("UTF-8"), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			path += "/"+username;
			stat = zookeeper.exists(path, false); 
			if( stat == null)
			{
				JSONObject data = new JSONObject();
				data.put(key, value);
				zookeeper.create(path, data.toString().getBytes("UTF-8"), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			else
			{
				String json = new String(zookeeper.getData(path, false, stat), "UTF-8");
				JSONObject data = new JSONObject(json);
				data.put(key, value);
				zookeeper.setData(path, data.toString().getBytes("UTF-8"), stat.getVersion());
			}
		}
		catch(Exception e)
		{
			e.getMessage();
		}

		File dir = new File(PathFactory.getDataPath(), "user-action/");
		if( dir.exists() )
		{
			IOHelper.deleteDir(dir);
		}
	}
	/**
	 * 得到用户的行为记忆
	 * @param username
	 * @param key
	 * @return
	 */
	public String getUserActionMemory(String username)
	{
		String path = "/cos/data/user-action/"+username;
		Zookeeper zookeeper = null;
		try
		{
			
			zookeeper = ZKMgr.getZookeeper();
			Stat stat = zookeeper.exists(path, false); 
			if( stat != null)
			{
				String json = new String(zookeeper.getData(path, false, stat), "UTF-8");
				new JSONObject(json);
				return json;
			}
		}
		catch(Exception e)
		{
			if( zookeeper != null )
				zookeeper.setJSONObject(path, new JSONObject());
		}
		return null;
	}
	
	/**
	 * 发送系统消息
	 * @param filter
	 * @param content
	 */
	public void sendSysnotify(String to, String filter, String title, String context, String contextlink, String action, String actionurl)
	{
	    if( to == null || to.isEmpty() )
	    {
			ArrayList<Sysuser> users = SysuserClient.listUser(1, -1, Status.Enable.getValue());
			for(Sysuser u : users )
			{
			    com.focus.cos.api.Sysnotify notify = new com.focus.cos.api.Sysnotify();
				notify.setUseraccount(u.getUsername());
				notify.setFilter(filter);
				notify.setTitle(title);
				notify.setPriority(5);
				notify.setNotifytime(new Date());
				notify.setContext(context);
				if( contextlink != null ) notify.setContextlink(contextlink);
				notify.setAction("回复");
				notify.setActionlink("#feedback?to="+super.getAccountName());
				if( action != null )
				{
					notify.setAction(action);
					notify.setActionlink(actionurl);
				}
				SysnotifyClient.submit(notify);
			}
	    }
	    else
	    {
	    	com.focus.cos.api.Sysnotify notify = new com.focus.cos.api.Sysnotify();
			notify.setUseraccount(to);
			notify.setFilter(filter);
			notify.setTitle(title);
			notify.setPriority(5);
			notify.setNotifytime(new Date());
			notify.setContext(context);
			if( contextlink != null ) notify.setContextlink(contextlink);
			notify.setAction("回复");
			notify.setActionlink("#feedback?to="+super.getAccountName());
			if( action != null )
			{
				notify.setAction(action);
				notify.setActionlink(actionurl);
			}
			SysnotifyClient.submit(notify);
	    }
	}
	/**
	 * 发送用户互动的消息
	 * @param content
	 * @return
	 */
	public DescktopMessage sendMessage(String content)
	{
		org.directwebremoting.WebContext web = WebContextFactory.get();   
	    javax.servlet.http.HttpServletRequest request = web.getHttpServletRequest();
		JSONObject user = (JSONObject)request.getSession().getAttribute("account");
		if( user == null ) return null;
		DescktopMessage message = new DescktopMessage();
		message.setAccount(user.getString("username"));
		message.setUsername(user.getString("realname"));
		message.setHead(user.getString("head"));
		message.setContent(content);
		synchronized(descktopMessages)
		{
			descktopMessages.add(0, message);
		}
		log.info("Succeed to send the message of descktop, all "+descktopMessages.size()+".");
		return message;
	}
	/**
	 * 设定通知状态
	 * @param nid
	 * @param state
	 * @return
	 */
	public Sysnotify setSysnotifyState(long nid, int state)
	{
		if( sysnotifyDao == null ) return null;
		Sysnotify instance = this.sysnotifyDao.findById(nid);
		instance.setState(state);
		this.sysnotifyDao.attachDirty(instance);
		if( instance.getContext() != null && 
			!instance.getContext().isEmpty() &&
			instance.getContext().toLowerCase().indexOf("<html") != -1 ){
			instance.setContext(Tools.delHTMLTag(instance.getContext()));
		}
		org.directwebremoting.WebContext web = WebContextFactory.get(); 
	    javax.servlet.http.HttpServletRequest request = web.getHttpServletRequest();
	    if( instance.getIcon() == null || instance.getIcon().isEmpty() ) instance.setIcon(Kit.URL_IMAGEPATH(request)+"cmp/34.png");
		if( (instance.getContextlink() == null || instance.getContextlink().isEmpty()) && 
		    (instance.getContextimg()  == null || instance.getContextimg().isEmpty()) )
		{
			loadWallpaper();
			instance.setContextimg(getImgWallpaper());
		}
		return instance;
	}
	
	public int countNotifies(String useraccount)
	{
		if( sysnotifyDao == null ) return 0;
		QueryMeta queryMeta = new QueryMeta();
		queryMeta.set("useraccount", useraccount);
		queryMeta.set("state", 0);
		return this.sysnotifyDao.count(queryMeta);
	}
	
	/**
	 * 查询系统消息数据
	 * @param queryMeta
	 * @return
	 */
	private List<?> find(QueryMeta queryMeta, int limit)
	{
        StringBuffer sql = new StringBuffer("SELECT a FROM Sysnotify a WHERE 1 = 1");   
        ArrayList<Object> listParameter = new ArrayList<Object>();
        if( queryMeta.exist("state") )
        {
        	sql.append(" AND a.state=?");   	
        	listParameter.add(queryMeta.get("state"));
        }
        if( queryMeta.exist("title") )
        {
        	sql.append(" AND a.title=?");   
        	listParameter.add(queryMeta.get("title"));	        	
        }
        if( queryMeta.exist("filter") )
        {
        	sql.append(" AND a.filter=?");   
        	listParameter.add(queryMeta.get("filter"));	        	
        }
        if( queryMeta.exist("useraccount") )
        {
        	sql.append(" AND a.useraccount=?"); 
        	listParameter.add(queryMeta.get("useraccount"));	      	        	
        }
        if( queryMeta.exist("priority") )
        {
        	sql.append(" AND a.priority>=?");   
        	listParameter.add(queryMeta.get("priority"));		        	
        }
        if(queryMeta.exist("startDate"))
    	{
        	sql.append(" AND a.notifytime >= ?");
        	listParameter.add(queryMeta.getStartDate());
    	}
        if(queryMeta.exist("endDate"))
    	{
        	sql.append(" AND a.notifytime < ?");
        	listParameter.add(queryMeta.getEndDate());
    	}        
        if(queryMeta.exist("keyword"))
    	{
    		sql.append(" AND (a.title LIKE '%"+queryMeta.getKeyword()+"%'");
    		sql.append(" OR");
    		sql.append(" a.context LIKE '%"+queryMeta.getKeyword()+"%')");
    	}
        sql.append(" ORDER BY a.notifytime DESC");
        if( limit > 0 ) sql.append(" LIMIT 0, "+limit);
    	return sysnotifyDao.getHibernateTemplate().find(sql.toString(),listParameter.toArray());
	}

	/**
	 * 查询系统消息通知
	 * @param tsSysnotify
	 * @param order true表示顺序查询，大于指定时间的所有消息；false表示逆序查询，小于指定时间的所有消息
	 * @param state
	 * @param keywords
	 * @return
	 */
	public DescktopTips getNotifies(long tsSysnotify, boolean order, int state, String filter, String keywords)
	{
		DescktopTips descktopTips = new DescktopTips();
		org.directwebremoting.WebContext web = WebContextFactory.get();   
	    javax.servlet.http.HttpServletRequest request = web.getHttpServletRequest();
		JSONObject user = (JSONObject)request.getSession().getAttribute("account");
		if( user == null ) return null;

		List<?> notifies = null;
		QueryMeta queryMeta = new QueryMeta();
		queryMeta.set("useraccount", user.getString("username"));
		if( state >= 0 ) queryMeta.set("state", state);
		if( order )	queryMeta.setStartDate(new Date(tsSysnotify+1));
		else queryMeta.setEndDate(new Date(tsSysnotify-1));
		queryMeta.setKeyword(keywords);
		if( filter != null && !filter.isEmpty() ) queryMeta.set("filter", filter);
		try
		{
			notifies = this.find(queryMeta, 20);
			if( notifies.size() > 0 ) log.info("Query the notifies("+notifies.size()+") of messenger by "+user.getString("username")+" from state is "+state+", filter is "+filter+", ts-notify is "+Tools.getFormatTime("MM-dd HH:mm:ss SSS", tsSysnotify));
			for(int i = 0; i < notifies.size(); i++)
			{
				if( i == 20 ) break;
				Sysnotify n = (Sysnotify)notifies.get(i);
				if( n.getTimestamp() > tsSysnotify ) tsSysnotify = n.getTimestamp();
				DescktopNotify dn = new DescktopNotify();
				dn.setAccount(n.getUseraccount());
				dn.setFilter(n.getFilter());
				dn.setIcon(n.getIcon(), Kit.URL_IMAGEPATH(request)+"cmp/34.png");
				dn.setNid(n.getNid());
				dn.setTime(n.getTime());
				dn.setTitle(n.getTitle());
				dn.setTimestamp(n.getTimestamp());
				dn.setRead(n.getState()!=0);
				dn.setPrettytime(n.getPrettytime());
				descktopTips.getNotifies().add(dn);
			}
			//查有多少系统消息未读
			queryMeta.set("state", 0);
			queryMeta.setStartDate(new Date(0));
			queryMeta.setEndDate(null);
			queryMeta.setKeyword(null);
			descktopTips.setNotifyTips(sysnotifyDao.count(queryMeta));
		}
		catch(Exception e)
		{
			log.error("Failed to get the notifies for exception", e);
		}
		return descktopTips;
	}
	/**
	 * 获取桌面所数据
	 * @param tsSysnotify
	 * @param tsMessage
	 * @return
	 */
	public DescktopTips getDescktopTips(long tsSysnotify, long tsMessage)
	{
		DescktopTips descktopTips = new DescktopTips();
		org.directwebremoting.WebContext web = WebContextFactory.get();   
	    javax.servlet.http.HttpServletRequest request = web.getHttpServletRequest();
		JSONObject account = (JSONObject)request.getSession().getAttribute("account");
		if( account == null ) return null;
		try{
			Zookeeper zookeeper = ZKMgr.getZookeeper();
			JSONObject user = zookeeper.getJSONObject("/cos/login/user/"+account.getString("username"));
			String token0 = account.has("token")?account.getString("token"):"";
			String token1 = user.has("token")?user.getString("token"):"";
			if( !token0.equals(token1) ){
				//有其他用户登录了推出
				descktopTips.setException("对不起，因为相同账号["+account.getString("username")+"]被其他人在"+user.getString("lastLogin")+"登录，您的当前登录回话将退出。");
				return descktopTips;
			}
		}
		catch(Exception e){
		}
		
		QueryMeta queryMeta = new QueryMeta();
		queryMeta.set("useraccount", account.getString("username"));
		queryMeta.set("state", 0);
		queryMeta.setStartDate(new Date(tsSysnotify+1));
		try
		{
			if( System.currentTimeMillis() - tsSysnotify > Tools.MILLI_OF_HOUR )
			{
				this.sysnotifyDao.deleteByFilter("集群程序管理", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()-1*Tools.MILLI_OF_DAY));
				this.sysnotifyDao.deleteByFilter("集群ZK管理", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()-1*Tools.MILLI_OF_DAY));
				this.sysnotifyDao.deleteByFilter("集群文件管理", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()-1*Tools.MILLI_OF_DAY));
				this.sysnotifyDao.deleteByFilter("系统升级", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()-1*Tools.MILLI_OF_DAY));
				this.sysnotifyDao.deleteByFilter("系统升级", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()-1*Tools.MILLI_OF_DAY));
				this.sysnotifyDao.deleteByFilter("数据管理", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()-1*Tools.MILLI_OF_DAY));
				this.sysnotifyDao.deleteByFilter("角色管理", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()-2*Tools.MILLI_OF_DAY));
				this.sysnotifyDao.deleteByFilter("监控配置", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()-2*Tools.MILLI_OF_DAY));
				this.sysnotifyDao.deleteByFilter("系统配置", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()-2*Tools.MILLI_OF_DAY));
				this.sysnotifyDao.deleteByFilter("用户账号安全", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()-1*Tools.MILLI_OF_DAY));
				this.sysnotifyDao.deleteByFilter("用户管理", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()-2*Tools.MILLI_OF_DAY));
				this.sysnotifyDao.deleteByFilter("休息一下", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()-3*Tools.MILLI_OF_DAY));
				this.sysnotifyDao.deleteByFilter("韩寒鸡汤", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()-15*Tools.MILLI_OF_DAY));
				this.sysnotifyDao.deleteByFilter("数据管理", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()-7*Tools.MILLI_OF_DAY));
				this.sysnotifyDao.deleteByFilter("系统告警", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()-Tools.MILLI_OF_DAY));
				this.sysnotifyDao.deleteByFilter("设备告警", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()-Tools.MILLI_OF_DAY));
				this.sysnotifyDao.deleteByFilter("环境告警", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()-Tools.MILLI_OF_DAY));
				}
			List<?> list = this.find(queryMeta, 0);
			if( list.size() > 0 ) log.info("Query the notifies("+list.size()+") of descktop by "+account.getString("username")+", ts-notify is "+Tools.getFormatTime("MM-dd HH:mm:ss SSS", tsSysnotify));
			for(int i = 0; i < list.size(); i++)
			{
				Sysnotify n = (Sysnotify)list.get(i);
				if( n.getTimestamp() > tsSysnotify ) tsSysnotify = n.getTimestamp();
				DescktopNotify dn = new DescktopNotify();
				dn.setAccount(n.getUseraccount());
				dn.setFilter(n.getFilter());
				dn.setIcon(n.getIcon(), Kit.URL_IMAGEPATH(request)+"cmp/34.png");
				dn.setNid(n.getNid());
				dn.setTime(n.getTime());
				dn.setTitle(n.getTitle());
				dn.setTimestamp(n.getTimestamp());
				dn.setRead(n.getState()!=0);
				dn.setPrettytime(n.getPrettytime());
				descktopTips.getNotifies().add(dn);
			}
			queryMeta.setStartDate(null);
			descktopTips.setTsNotify(tsSysnotify);
			
			synchronized (descktopMessages) {
				for(DescktopMessage e : descktopMessages)
				{
					if( e.getTimestamp() <= tsMessage ) break;
					descktopTips.getMessages().add(e);
				}
			}
			if( !descktopTips.getMessages().isEmpty() )
			{
				tsMessage = descktopTips.getMessages().get(0).getTimestamp();
			}
			descktopTips.setTsMessage(tsMessage);
			
			//查有多少系统消息未读
			queryMeta.set("state", 0);
			queryMeta.setStartDate(new Date(0));
			queryMeta.setEndDate(null);
			descktopTips.setNotifyTips(sysnotifyDao.count(queryMeta));
			descktopTips.setAlarmTips(sysalarmMgr.countInstantAlarms(account, 0));
		}
		catch(Exception e)
		{
			log.error("Failed to get the notifies for exception", e);
		}
		return descktopTips;
	}
	
	/**
	 * 产生演示用的系统消息通知
	 * @param user
	 * @param time
	 * @param request
	private void generateDemoSysnotify(JSONObject user, long time, HttpServletRequest request)
	{
	    StringBuffer sb = new StringBuffer();
		try
		{
			if( System.currentTimeMillis() - time > Tools.MILLI_OF_HOUR )
			{
				ArrayList<String> content = new ArrayList<String>();
				ArrayList<String> times = new ArrayList<String>();
				com.focus.cos.api.Sysnotify notify = new com.focus.cos.api.Sysnotify();
				notify.setUseraccount(user.getString("username"));
				notify.setFilter("休息一下");
				notify.setTitle("聪明的脑袋歇一下【"+Tools.getFormatTime("yyyy-MM-dd", System.currentTimeMillis())+"幽默集锦】");
				notify.setPriority(5);
				notify.setNotifytime(new Date());
				Document doc = HttpUtils.crwal("http://www.budejie.com/text/");
				Elements jrlist = doc.getElementsByClass("j-r-list-c-desc");
				for(Element e : jrlist )
				{
					content.add(e.html());
				}
				jrlist = doc.getElementsByClass("j-r-list-tool");
//				final SimpleDateFormat sdft = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				for(Element e : jrlist)
				{
//					Sysnotify notify = notifies.get(i++);
//					notify.setTitle(e.attr("data-title"));
					String data_date = e.attr("data-date");
					String data_time = e.attr("data-time");
					times.add(data_date+" "+data_time);
//					Date date = sdft.parse(data_date+" "+data_time);
				}

				sb.append("看看段子放松下吧……\r\n");
				for(int i = 0; i < content.size(); i++)
				{
					sb.append(content.get(i));
					sb.append("\r\n<span style='color:#ccc'>");
					sb.append(times.get(i));
					sb.append("</span>\r\n\r\n");
				}
				notify.setContext(sb.toString());
				QueryMeta queryMeta = new QueryMeta();
				queryMeta.set("useraccount", user.getString("username"));
				queryMeta.set("title", notify.getTitle());
				if( this.sysnotifyDao.count(queryMeta) == 0 )
				{
					SysnotifyClient.send(notify);
				}
			}
		}
		catch(Exception e)
		{
//			log.error("Failed to getNotifies from http://www.budejie.com/text/ for exception "+e+"\r\n"+sb);
		}
//		System.out.println(Tools.getFormatTime("d MMM yyyy", System.currentTimeMillis()));
		sb = new StringBuffer();
		try
		{
			if( System.currentTimeMillis() - time > Tools.MILLI_OF_DAY )
			{
				sb.append("getNotifies from http://wufazhuce.com/");
				Document doc = HttpUtils.crwal("http://wufazhuce.com/");
				Element carousel = HttpUtils.getElementByClass(doc, "carousel-inner");
				Elements children = carousel.children();
				final SimpleDateFormat sdft = new SimpleDateFormat("d MMM yyyy");
				for(Element e : children )
				{
					Element img = HttpUtils.getElementByTag(e, "img");
					Element txt = HttpUtils.getElementByClass(e, "fp-one-cita");
					Element dom = HttpUtils.getElementByClass(e, "dom");
					Element may = HttpUtils.getElementByClass(e, "may");
					Element a = HttpUtils.getElementByTag(e, "a");
					if( img == null || txt == null ) continue;
					com.focus.cos.api.Sysnotify notify = new com.focus.cos.api.Sysnotify();
					notify.setUseraccount(user.getString("username"));
					notify.setFilter("韩寒鸡汤");
					notify.setPriority(5);
					notify.setTitle(txt.text());
					notify.setContextimg(img.attr("src"));
					notify.setContext("韩寒“一个”之说。");
					notify.setContextlink(a!=null?a.attr("href"):"#");
					try
					{
						Date date = sdft.parse(dom.text()+" "+may.text());
						notify.setNotifytime(date);
					}
					catch(Exception e1)
					{
//						final HashMap<String, String> Month = new HashMap<String, String>();
//						Month.put("Jun", "六月");
//						String[] args = may.text().split(" ");
//						Date date = sdft.parse(dom.text()+" "+Month.get(args[0])+" "+args[1]);
						notify.setNotifytime(new Date());
					}
					QueryMeta queryMeta = new QueryMeta();
					queryMeta.set("useraccount", user.getString("username"));
					queryMeta.set("title", notify.getTitle());
					if( this.sysnotifyDao.count(queryMeta) > 0 )
					{
						continue;
					}
					log.info("\r\n");
					log.info(e.html());
					SysnotifyClient.send(notify);
					break;
				}
				log.info(sb.toString());
			}
		}
		catch(Exception e)
		{
			log.error("Failed to getNotifies from http://wufazhuce.com/\r\n"+sb, e);
		}
	}
	 */
	
	/**
	 * 保存系统消息通知
	 * @param notify
	 */
	public void doSave(Sysnotify notify)
	{
		if( sysnotifyDao == null || notify == null || notify.getTitle() == null || notify.getTitle().isEmpty() ) return;
		QueryMeta queryMeta = new QueryMeta();
		queryMeta.set("useraccount", notify.getUseraccount());
		queryMeta.set("filter", notify.getFilter());
		queryMeta.set("title", notify.getTitle());
		if( notify.getPriority() > 0 )
		{
			queryMeta.set("<state", 2);
		}
		else
		{
			queryMeta.set("state", 0);
		}
		List<?>list = sysnotifyDao.find(queryMeta);
		if( !list.isEmpty() )
		{
			Sysnotify notifyCopy = (Sysnotify)list.get(0);
			notifyCopy.setAction(notify.getAction());
			notifyCopy.setActionlink(notify.getActionlink());
			notifyCopy.setContext(notify.getContext());
			notifyCopy.setContextimg(notify.getContextimg());
			notifyCopy.setContextlink(notify.getContextlink());
			notifyCopy.setFilter(notify.getFilter());
			notifyCopy.setTitle(notify.getTitle());
			//notifyCopy.setNotifytime(notify.getNotifytime());
			notifyCopy.setState(notify.getState());
			notifyCopy.setPriority(notify.getPriority());
			notifyCopy.setUseraccount(notify.getUseraccount());
			sysnotifyDao.attachDirty(notifyCopy);
			log.debug("Update the notify of "+notifyCopy.getNid()+", user="+notifyCopy.getUseraccount());
		}
		else
		{
			log.debug("Add the notify(title="+notify.getTitle()+",filter"+notify.getFilter()+",account="+notify.getUseraccount()+",contextimg="+notify.getContextimg()+").");
			sysnotifyDao.save(notify);
		}
	}

	/**
	 * 获得动态墙纸"imageUrl"
	 */
	private String imgWallpaper;
	private ArrayList<String> arryWallpaper = null;
	public synchronized void loadWallpaper()
	{
		if( arryWallpaper != null && !arryWallpaper.isEmpty() )
		{
			return;
		}
		Thread thread = new Thread()
		{
			public void run()
			{
				final Random random = new Random();
				String html = (random.nextInt(30)+1)+".html";
				String link = "http://sj.zol.com.cn/bizhi/p2/"+html;//"http://image.baidu.com/data/imgs?pn=0&rn=30&col=%E5%A3%81%E7%BA%B8&tag=%E5%85%A8%E9%83%A8&tag3=&width=1600&height=900&ic=0&ie=utf8&oe=utf-8&image_id=&fr=channel&p=channel&from=1&app=img.browse.channel.wallpaper&t=0.8941218816879991";
				Document doc;
				try
				{
					StringBuffer sb = new StringBuffer();
					sb.append("loadWallpaper");
					if( arryWallpaper == null || arryWallpaper.isEmpty() )
					{
						arryWallpaper = new ArrayList<String>();
						doc = HttpUtils.crwal(link);
//						String json = doc.body().text();
						Elements ul = doc.getElementsByClass("photo-list-padding");
						for(Element e : ul )
						{
							Element img = HttpUtils.getElementByTag(e, "img");
							if( img != null )
							{
								String src = img.attr("src");
								if( src.endsWith(".jpg") && src.indexOf("208x312") != -1 )
								{
									src = src.replaceAll("208x312", "480x800");
									arryWallpaper.add(src);
									sb.append("\r\n\t"+src);
								}
							}
						}
					}
					log.info(sb.toString());
				}
				catch (Exception e)
				{
					log.error("Failed to load the images of wallpaper.");
				}
			}
		};
		thread.start();
	}
	/**
	 * 得到壁纸
	 * @return
	 */
	public synchronized String getImgWallpaper()
	{
		try
		{
			if( arryWallpaper != null && !arryWallpaper.isEmpty() )
			{
				Random r = new Random();
				int i = r.nextInt(arryWallpaper.size());
				imgWallpaper = arryWallpaper.get(i);
				arryWallpaper.remove(i);
			}
		}
		catch (Exception e)
		{
			log.error("Failed to load the images of wallpaper.", e);
		}
		return imgWallpaper;
	}	
	
	public List<?> listFilter()
	{
		if( sysnotifyDao == null ) return null;
		return jdbcBaseDao.queryNotifyFilter();
	}

	public void setJdbcBaseDao(JDBCBaseDAO jdbcBaseDao)
	{
		this.jdbcBaseDao = jdbcBaseDao;
	}

	public SysnotifyDAO getSysnotifyDao()
	{
		return sysnotifyDao;
	}

	public void setSysnotifyDao(SysnotifyDAO sysnotifyDao)
	{
		this.sysnotifyDao = sysnotifyDao;
	}

	public void setSysalarmMgr(SysalarmMgr sysalarmMgr)
	{
		this.sysalarmMgr = sysalarmMgr;
	}
}
