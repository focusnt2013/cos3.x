package com.focus.cos.web.ops.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import com.focus.cos.web.common.ZKMgr;
import com.focus.util.Tools;

/**
 * 监控页面
 * @author focus
 *
 */
public class MonitorConfigAction extends OpsAction
{
	private static final long serialVersionUID = 9092051816270938220L;
	private static final Log log = LogFactory.getLog(MonitorConfigAction.class);
	
	/**
	 * 
	 * @return
	 */
	public String apiproxy(){
		return this.grid("/grid/local/sysapiproxy.xml");
	}

	/**
	 * 
	 * @return
	 */
	public String apiproxydata(){
		JSONObject dataJSON = new JSONObject();
		try {
			this.localDataArray = ZKMgr.getZookeeper().getJSONArray("/cos/data/apiproxy", false);
    		dataJSON.put("totalRecords", localDataArray.length());
			dataJSON.put("curPage", 1);
			dataJSON.put("data", localDataArray);
		} catch (Exception e) {
		}
		return response(super.getResponse(), dataJSON.toString());
	}
	/**
	 * 导航系统监控配置
	 * @return
	 */
	public String databases()
	{
		try
		{
			JSONObject privileges = getMonitorMgr().getClusterPrivileges(super.getUserRole(), super.getUserAccount());
			JSONArray clusters = this.getMonitorMgr().getClusterTree(privileges, super.isSysadmin(), true);
			if( Tools.isNumeric(id) )
			{
				clusters = this.getMonitorMgr().getClusterTree(clusters, Integer.parseInt(id));
			}
			jsonData = "[]";
			if( clusters == null )
			{
				clusters = new JSONArray();
			}
//			System.err.println("naviagte "+clusters.toString(4));
			this.grant = super.isSysadmin();
			jsonData = clusters.toString();
		} catch (Exception e) {
			super.responseException = e.getMessage();
		}
		return "databases";
	}
	
	/**
	 * 配置监控数据库
	 * @return
	 */
	public String database()
	{
		JSONObject server = this.getMonitorMgr().getServer(Integer.parseInt(id));
		if( server == null )
		{
			responseException = "未能查找到该伺服器监(ID"+id+")控配置，打开页面失败.";
			return "404";
		}
		if( !server.has("security-key") )
		{
			responseException = "该伺服器(ID"+id+")未启动监听.";
		}
		this.id = Tools.encodeMD5(server.getString("security-key"));
		log.info("Open the view of jdbc.monitor.config by "+id);
		System.setProperty("serverkey", id);
		JSONObject e = new JSONObject();
		e.put("jdbc.driver", "com.mysql.jdbc.Driver");
		e.put("jdbc.username", "baike");
		e.put("jdbc.password", "testbaike#!&2017");
		e.put("jdbc.name", "百科数据库");
		e.put("jdbc.url", "jdbc:mysql://122.115.40.74:13310/baike?lastUpdateConnt=true&amp;useUnicode=true&amp;characterEncoding=UTF-8");
		return grid("/grid/local/sysjdbcmonitor.xml");
	}
}
