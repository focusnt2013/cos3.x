package com.focus.cos.web.ops.action;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import com.focus.control.ModulePerf;
import com.focus.cos.web.common.Kit;
import com.focus.cos.web.ops.service.Monitor.RunFetchMonitor;
import com.focus.util.Tools;

/**
 * COS的监控管理器
 * @author focus lau
 * @create 2009-08-31
 */
public class MonitorAction extends OpsAction
{
	private static final long serialVersionUID = 4745006847372010934L;
	private static final Log log = LogFactory.getLog(MonitorAction.class);
	/*根节点id*/
	private int rootid = -1;
	/**
	 * 打开集群视图
	 * @return
	 */
	public String cluster() throws Exception
	{
		log.info("Preview the servers of cluster("+id+") by user("+super.getUserAccount()+", role "+super.getUserRole()+").");
		return "cluster";
	}
	/**
	 * 打开伺服器视图
	 * @return
	 */
	public String server() throws Exception
	{
		log.info("Preview the server("+id+") by user("+super.getUserAccount()+", role "+super.getUserRole()+").");
		JSONObject server = null;
		try{
			if( Tools.isNumeric(id) )
			{
				server = this.getMonitorMgr().getServer(Integer.parseInt(id));
			}
			else
			{
				server = this.getMonitorMgr().getServer(id);
			}
			if( server == null )
			{
				responseException = "未能查找到该伺服器(ID"+id+")监控数据，打开页面失败.";
				return "404";
			}
			this.grant = super.isSysadmin();
			JSONObject privileges = this.getMonitorMgr().getClusterPrivileges(super.getUserRole(), super.getUserAccount());
			if( !grant && !privileges.has(server.getString("security-key")) )
			{
				responseException = "服务拒绝 系统管理员不允许您查看该伺服器状态.";
				return "403";
			}
			if( !grant )
			{
				privileges = privileges.getJSONObject(server.getString("security-key"));
				grant = privileges.has("control")?privileges.getBoolean("control"):false;
			}
			id = String.valueOf(server.getInt("id"));
			ip = server.getString("ip");
			port = server.getInt("port");
			String command = super.getRequest().getParameter("command");
			if( "sysPrograms".equals(command) )
			{
				this.selectionModel = "inner";
			}
			else if( "userPrograms".equals(command) )
			{
				this.selectionModel = "outer";
			}
			else
			{
				this.selectionModel = null;
			}
			return "server";			
		}
		catch(Exception e){
        	log.error("Failed to open the monitor of server.", e);
        	getSession().setAttribute("referer", "files!pageshow.action");
        	getSession().setAttribute("exceptionTips", "打开伺服器监控视图异常"+e);
			super.getSession().setAttribute("exception", e.getCause());
			try
			{
				super.getResponse().sendRedirect(Kit.URL_PATH(super.getRequest())+ "500");
			} 
			catch (IOException e1) {
			}
			return null;
		}
	}
	/**
	 * 跟踪
	 * @return
	 */
	public String track(){
		navigate();
		return "track";
	}
	/**
	 * 打开监控集群导航界面
	 * @return
	 */
	public String navigate()
	{
		try
		{
			JSONObject privileges = getMonitorMgr().getClusterPrivileges(super.getUserRole(), super.getUserAccount());
			this.grant = super.isSysadmin();
			JSONArray clusters = this.getMonitorMgr().getClusterTree(privileges, grant, false, rootid);
			this.getMonitorMgr().setClusterState(clusters);
			jsonData = clusters.toString();
//			System.err.println(rootid+":"+clusters.toString(4));
			this.ww = this.getMonitorMgr().getClusterServerCount();
		} 
		catch (Exception e) 
		{
			log.error("Failed to initialize the navigate for ", e);
			super.responseException = "打开伺服器监控导航出现异常:"+e.getMessage();
		}
		return "navigate";
//		return this.getMonitorMgr().getTheClusterId() > 1024 ? "track":"navigate";
	}
	
	/**
	 * 
	 * @return
	 */
	public String netstat(){
		return super.grid("/grid/local/sysnetstat.xml");
	}
	/**
	 * 
	 * @return
	 */
	public String netstatdata(){

		log.info("Open the data of netstat from "+ip+":"+port);
		RunFetchMonitor runner = getMonitorMgr().getMonitor().getRunFetchMonitor(ip, port);
		JSONObject dataJSON = new JSONObject();
		String zkpath = "";
		try
		{
			this.localDataArray = new JSONArray();
	        for( int i = 0; i < runner.getModulePerfs().size(); i++ )
	        {
	        	ModulePerf module = runner.getModulePerfs().get(i);
	        	String json = module.getNetstat();
	        	if( json == null || json.isEmpty()){
	        		continue;
	        	}
	        	JSONArray array = new JSONArray(module.getNetstat());
	        	for( int j = 0; j < array.length(); j++){
	        		JSONObject netstat = array.getJSONObject(j);
	    			StringBuffer remark = new StringBuffer("该程序监听了端口");
	        		localDataArray.put(netstat);
	        		JSONArray listencon = new JSONArray();
	        		JSONArray listening = netstat.getJSONArray("listening");
	        		for( int k = 0; k < listening.length(); k++){
	        			JSONObject listen = listening.getJSONObject(k);
	        			if( k > 0 ){
	        				remark.append(",");
	        			}
	        			remark.append(listen.getInt("local_port"));
	        			remark.append("("+listen.getString("type")+")");
	        			if( listen.has("connections") ){
	        				JSONArray connections = listen.getJSONArray("connections");
	        				listen.put("size", connections.length());
	        				for(int n = 0; n < connections.length(); n++){
	        					JSONObject e = connections.getJSONObject(n);
	        					listencon.put(e);
	        				}
	        			}
	        		}
	        		JSONObject maintenance = new JSONObject();
	        		maintenance.put("programer", module.getProgrammer());
	        		maintenance.put("email", module.getProgrammerContact());
	        		netstat.put("maintenance", maintenance);
	        		if( module.getStartupCommands() != null ){
	        			StringBuffer sb = new StringBuffer();
	        			for(int m = 0; m < module.getStartupCommands().size(); m++){
	        				String arg = module.getStartupCommands().get(m);
	        				if( m > 0 ) sb.append("\r\n");
	        				sb.append(arg);
	        			}
	        			netstat.put("startupCommands", sb.toString());
	        		}
	        		if( module.getShutdownCommands() != null ){
	        			StringBuffer sb = new StringBuffer();
	        			for(int m = 0; m < module.getShutdownCommands().size(); m++){
	        				String arg = module.getShutdownCommands().get(m);
	        				if( m > 0 ) sb.append("\r\n");
	        				sb.append(arg);
	        			}
	        			netstat.put("shutdownCommands", sb.toString());
	        		}
	        		netstat.put("description", module.getRemark());
	        		netstat.put("remark", remark.toString());
	        		netstat.put("connections", listencon);
	        		netstat.put("startup_time", module.getStartupTime());
	        		netstat.put("programer", module.getProgrammer());
	        		netstat.put("ts", netstat.getString("timestamp").substring(11));
	        	}
	        }
			dataJSON.put("totalRecords", localDataArray.length());
			dataJSON.put("curPage", 1);
			dataJSON.put("data", localDataArray);
		}
		catch(Exception e)
		{
			log.error("Failed to get the version from "+zkpath+" for "+e);
			dataJSON.put("message", e.getMessage());
			dataJSON.put("hasException", true);
		}
		return response(super.getResponse(), dataJSON.toString());
	}
	
	public int getRootid() {
		return rootid;
	}
	public void setRootid(int rootid) {
		this.rootid = rootid;
	}
	
}