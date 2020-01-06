package com.focus.cos.web.ops.action;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.json.JSONObject;

import com.focus.cos.web.action.GridAction;
import com.focus.cos.web.common.QueryMeta;
import com.focus.cos.web.common.ZKMgr;
import com.focus.cos.web.ops.service.DescktopMgr;
import com.focus.cos.web.ops.vo.Sysnotify;
import com.mongodb.BasicDBObject;

public class SysnotifyAction extends GridAction
{
	private static final long serialVersionUID = 1926901134614075641L;
	private static final Log log = LogFactory.getLog(SysnotifyAction.class);
	
	private DescktopMgr sysnotifyMgr;
//	private NotifyConfigMgr notifyConfigMgr;
	/*过滤选项*/
	private String filter;
	/**/
	private Sysnotify notify;
	/*日期类型*/
	private String dateType;
	/**/
	private QueryMeta queryMeta;
	/*显示标签*/
	private int winwidth;
	private int winheight;
	
	/**
	 * 打开消息页面
	 * @return
	 */
	public String messenger()
	{
		if( queryMeta == null )
		{
			queryMeta = new QueryMeta();
		}
			queryMeta.set("useraccount", getUserAccount());
	//		queryMeta.set("state", 0);
			queryMeta.set("filter", filter);
			this.listData = sysnotifyMgr.listFilter();
			//this.listData = sysnotifyMgr.getSysnotifyDao().find(queryMeta);
		this.setViewTitle("我的系统消息");
		//timestamp = System.currentTimeMillis() - Tools.MILLI_OF_DAY;//查询1天内的系统消息
		return "messenger";
	}
	
	//配置列表
	public String config()
	{
		try
		{
			ZooKeeper zookeeper = ZKMgr.getZooKeeper();
			String path = "/cos/config";
			Stat stat = zookeeper.exists(path, false); 
			if( stat == null)
			{
				zookeeper.create(path, "配置管理".getBytes("UTF-8"), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			path = "/cos/config/notify";
			stat = zookeeper.exists(path, false); 
			if( stat == null)
			{
				zookeeper.create(path, "系统消息配置管理".getBytes("UTF-8"), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			String rsp = this.grid("/grid/local/sysnotifycfg.xml");
			List<?> filters = sysnotifyMgr.listFilter();
			if( filters != null )
			{
				HashMap<String,String> dup = new HashMap<String, String>();
				for(int i = 0; i < localDataArray.length(); i++ )
				{
					JSONObject e = localDataArray.getJSONObject(i);
					dup.put(e.getString("NAME"), null);
				}
				for(Object filter : filters )
				{
					if( filter == null || filter.toString().isEmpty()) continue;
					if( dup.containsKey(filter.toString()) ) continue;
					JSONObject e = new JSONObject();
					e.put("NAME", filter.toString());
					e.put("SMS", false);
					e.put("EMAIL", false);
					e.put("WEIXIN", false);
    				localDataArray.put(e);
				}
			}
			this.localData = localDataArray.toString();
			return rsp;
		}
		catch(Exception e)
		{
			log.error("Failed to query", e);
			return "alert";
		}
	}
	
	//删除配置
	public String doDeletecfg()
	{
		BasicDBObject response = new BasicDBObject();
		try
		{
			StringBuffer sb = new StringBuffer();
			Iterator<Map.Entry<String, String[]>> iterator = super.getRequest().getParameterMap().entrySet().iterator();
			while(iterator.hasNext())
			{
				Map.Entry<String, String[]> e = iterator.next();
				sb.append("\r\n\t");
				sb.append(e.getKey());
				sb.append("=");
				for(String value : e.getValue())
					sb.append(value+"\t");
			}
			log.debug(sb.toString());
			ZooKeeper zookeeper = ZKMgr.getZooKeeper();
			String path = "/cos/config/notify/"+id;
			Stat stat = zookeeper.exists(path, false); 
			if( stat != null)
			{
				zookeeper.delete(path, stat.getVersion());
			}
			String s = String.format(this.getText("user.log.0045"), getUserAccount());
    		logoper(s, "系统配置", null, null);
			response.put("hasException", false);
		}
		catch (Exception e)
		{
			String s = String.format(this.getText("user.log.1045"), getUserAccount(), e.toString());
			response.put("hasException", true);
			response.put("message", s);
    		logoper(s, "系统配置", null, null, e);
		}
		return response(super.getResponse(), response.toString());
	}
	
	/**
	 * 更新
	 * @return
	 */
	public String doUpdatecfg()
	{
		HttpServletRequest req = super.getRequest();
		BasicDBObject request = new BasicDBObject();
		BasicDBObject response = new BasicDBObject();
		try
		{
			StringBuffer sb = new StringBuffer();
			sb.append("Receive the request of update from ");
			Iterator<Map.Entry<String, String[]>> iterator = super.getRequest().getParameterMap().entrySet().iterator();
			while(iterator.hasNext())
			{
				Map.Entry<String, String[]> e = iterator.next();
				sb.append("\r\n\t");
				sb.append(e.getKey());
				sb.append("=");
				for(String value : e.getValue())
					sb.append(value+"\t");
			}
			log.debug(sb.toString());
			ZooKeeper zookeeper = ZKMgr.getZooKeeper();
			request.put("id", id);
			request.put("NAME", req.getParameter("NAME"));
			request.put("SMS", Boolean.parseBoolean(req.getParameter("SMS")));
			request.put("EMAIL", Boolean.parseBoolean(req.getParameter("EMAIL")));
			request.put("WEIXIN", Boolean.parseBoolean(req.getParameter("WEIXIN")));
			String path = "/cos/config/notify/"+id;
			Stat stat = zookeeper.exists(path, false); 
			if( stat == null)
			{
				zookeeper.create(path, request.toString().getBytes("UTF-8"), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			else
			{
				zookeeper.setData(path, request.toString().getBytes("UTF-8"), stat.getVersion());
			}
			String s = String.format(this.getText("user.log.0044"), getUserAccount());
    		logoper(s, "系统配置", null, null);
		}
		catch (Exception e)
		{
			String s = String.format(this.getText("user.log.1044"), getUserAccount(), e.toString());
			response.put("hasException", true);
			response.put("message", s);
    		logoper(s, "系统配置", null, null, e);
		}
		return response(super.getResponse(), response.toString());
	}
	public Object getModel()
	{
		// TODO Auto-generated method stub
		return null;
	}
	public String getFilter()
	{
		return filter;
	}
	public void setFilter(String filter)
	{
		this.filter = filter;
	}
	public Sysnotify getNotify()
	{
		return notify;
	}
	public void setNotify(Sysnotify notify)
	{
		this.notify = notify;
	}
	public QueryMeta getQueryMeta()
	{
		return queryMeta;
	}
	public void setQueryMeta(QueryMeta queryMeta)
	{
		this.queryMeta = queryMeta;
	}
	public String getDateType()
	{
		return dateType;
	}
	public void setDateType(String dateType)
	{
		this.dateType = dateType;
	}
	public boolean isShowTitle()
	{
		return showTitle;
	}
	public void setShowTitle(boolean showTitle)
	{
		this.showTitle = showTitle;
	}
	public int getWinwidth()
	{
		return winwidth;
	}
	public void setWinwidth(int winwidth)
	{
		this.winwidth = winwidth;
	}
	public int getWinheight()
	{
		return winheight;
	}
	public void setWinheight(int winheight)
	{
		this.winheight = winheight;
	}
	public void setSysnotifyMgr(DescktopMgr sysnotifyMgr)
	{
		this.sysnotifyMgr = sysnotifyMgr;
	}
}
