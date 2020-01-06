package com.focus.cos.web.user.action;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.json.JSONArray;
import org.json.JSONObject;

import com.focus.cos.web.action.GridAction;
import com.focus.cos.web.common.ZKMgr;
import com.focus.cos.web.dev.service.MenusMgr;
import com.focus.cos.web.ops.service.MonitorMgr;
import com.focus.cos.web.user.service.RoleMgr;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.opensymphony.xwork.ModelDriven;

public class RoleAction extends GridAction implements ModelDriven
{
	private static final long serialVersionUID = -8800879424853138829L;

	private static final Log log = LogFactory.getLog(RoleAction.class);
	//复用管理器
	private RoleMgr roleMgr;
	//JSON输出集群监控配置
	private String jsoncluster;
	//父节点ID
	private int pid;

	/**
	 * 角色管理入口界面 
	 * @return
	 */
	public String manager()
	{
		try
		{
			ZooKeeper zookeeper = ZKMgr.getZooKeeper();
			Stat stat = zookeeper.exists("/cos/config/role", false); 
			if( stat == null)
			{
				JSONObject roleObj = new JSONObject();
				roleObj.put("id", 1);
				roleObj.put("name", "系统管理员组");
				zookeeper.create("/cos/config/role", roleObj.toString().getBytes("UTF-8"), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				stat = zookeeper.exists("/cos/config/role", false);
			}
			JSONObject allRoles = ZKMgr.getZookeeper().getJSONObject("/cos/config/role");
			allRoles.put("drag", false);
			this.jsonData = roleMgr.getUserRoles(allRoles, getUserRole()).toString();
			this.timestamp = stat.getMtime();
		}
		catch(Exception e)
		{
			log.error("Failed to open the view of rolemgr for ", e);
			super.setResponseException("打开角色权限管理界面失败，因为异常"+e);
		}
		return "manager";
	}
	
	/**
	 * 菜单配置
	 * @return
	 */
	public String preset()
	{
		try 
		{
			JSONArray modules = new JSONArray();
			MenusMgr.loadModules(modules);
			JSONObject role = null;
			JSONObject roleParent = null;
			if( !"-1".equals(id) ) role = ZKMgr.getZookeeper().getJSONObject("/cos/config/role/"+id);
			if( pid > 1 )
			{
				roleParent = ZKMgr.getZookeeper().getJSONObject("/cos/config/role/"+pid);
			}
			if( pid < 1 && super.getUserRole() > 1 ) roleParent = role;
//			System.err.println(pid);
			roleMgr.setMenusPrivileges(modules, role, roleParent, isSysadmin());
			this.jsonData = modules.toString();
			if( role != null && role.has("##cluster") )
			{
				this.jsoncluster = role.getJSONObject("##cluster").toString();
			}
			if( jsoncluster == null ) jsoncluster = "{}";
//			System.err.println(modules.toString(4));
//			JSONArray clusters = roleMgr.getRoleCluster(role, roleParent, false);
//			System.err.println(id+":"+role.getInt("parent")+"/"+pid);
//			this.jsoncluster = clusters.toString();
		}
		catch (Exception e) 
		{
			log.error("Failed to initialize the view of modules config for exception ", e);
			super.setResponseException("初始化界面失败，因为异常"+e);
			return "close";
		}
		return "preset";
	}
	
	/**
	 * 
	 * @return
	 */
	public String controlprivileges()
	{
		JSONObject parentPrivileges = null;
		JSONObject privileges = null;
		try
		{
			if( !"-1".equals(id) ) privileges = MonitorMgr.getInstance().getClusterPrivileges(Integer.valueOf(id));
			if( pid > 1 )
			{
				parentPrivileges = MonitorMgr.getInstance().getClusterPrivileges(pid);
			}
			if( pid < 1 && super.getUserRole() > 1 ) parentPrivileges = privileges;
			ArrayList<JSONObject> servers = MonitorMgr.getInstance().getServers();
			localDataArray = new JSONArray();
			this.grant = this.isSysadmin();

	    	BasicDBList options = new BasicDBList();
    		labelsModel.put("pid", options);
    		HashMap<Integer, JSONObject> duplication = new HashMap<Integer, JSONObject>();
			for(JSONObject e : servers)
			{
				JSONObject server = new JSONObject(e.toString());
				if( !server.has("pid") ) continue;
				if( !server.has("security-key") ) continue;
				int pid = server.getInt("pid");
				String pname = null;
				if( !duplication.containsKey(pid) )
				{
					JSONObject cluster = MonitorMgr.getInstance().getCluster(pid);
					if( cluster == null ) continue;
					duplication.put(pid, cluster);
					BasicDBObject option = new BasicDBObject();
					option.put("value", String.valueOf(pid));
					option.put("label", cluster.getString("name"));
					options.add(option);
				}
				pname = duplication.get(pid).getString("name");
				
				String serverid = server.has("security-key")?server.getString("security-key"):"";
	            if( parentPrivileges != null && !parentPrivileges.has(serverid) )
	            	continue;//父级角色权限组权限不存在就排除过滤
	            server.put("address", server.getString("ip")+":"+server.getInt("port"));
	            server.put("pname", pname);
	            if( server.has("stateinfo") ) server.remove("stateinfo");
	            if( server.has("port") ) server.remove("port");
	            if( server.has("ip") ) server.remove("ip");
	            localDataArray.put(server);
	            if( privileges != null && !privileges.has(serverid) ) 
	            {
		            if( grant && "1".equals(id) )
		            {
			            server.put("readable", true);
			            server.put("control", true);
			            server.put("files", true);
			            server.put("zookeeper", true);
			            server.put("ssh", true);
			            continue;
		            }
	            	continue;
	            }
	            if( privileges == null || (id.equals("-1") && this.pid > 0) ) continue;
	            JSONObject serverPrivileges = privileges.getJSONObject(serverid);
	            server.put("readable", true);
	            server.put("control", serverPrivileges.has("control")&&serverPrivileges.getBoolean("control"));
	            server.put("files", serverPrivileges.has("files")&&serverPrivileges.getBoolean("files"));
	            server.put("zookeeper", serverPrivileges.has("zookeeper")&&serverPrivileges.getBoolean("zookeeper"));
	            server.put("ssh", serverPrivileges.has("ssh")&&serverPrivileges.getBoolean("ssh"));
			}
			this.jsonData = parentPrivileges!=null?parentPrivileges.toString():"";
			this.localData = localDataArray.toString();
			return super.grid("/grid/local/controlprivileges.xml");
		}
		catch (Exception e) 
		{
			log.error("Failed to initialize the view of modules config for exception ", e);
			super.setResponseException("初始化界面失败，因为异常"+e);
			return "alert";
		}
	}

	/**
	 * 菜单配置
	 * @return
	 */
	public String preview()
	{
		try 
		{
			if( id == null )
			{
				super.setResponseException("未知请求");//+super.getRequest().getRequestURI());
				return "close";
			}
			JSONArray modules = new JSONArray();
			MenusMgr.loadModules(modules);
			JSONObject role = ZKMgr.getZookeeper().getJSONObject("/cos/config/role/"+id);
			JSONObject roleParent = null;
			if( role.has("parent") && !id.equals(String.valueOf(role.getInt("parent"))) && role.getInt("parent") != 1 )
				roleParent = ZKMgr.getZookeeper().getJSONObject("/cos/config/role/"+role.getInt("parent"));
			if( roleParent != null && roleParent.length() == 0 ) roleParent = null;
			roleMgr.setMenusPrivileges(modules, role, roleParent, true);
			this.jsonData = modules.toString();
			JSONArray privileges = roleMgr.getClusterPrivileges(role, roleParent, isSysadmin());
			this.jsoncluster = privileges.toString();
		}
		catch (Exception e) 
		{
			super.setResponseException("初始化界面失败，因为异常"+e);
			log.error("Failed to initialize the preview of rolemgr for exception ", e);
			return "close";
		}
		return "preview";
	}

	public String query()
	{
		return "query";
	}
	
	public String getJsoncluster() {
		return jsoncluster;
	}

	public void setRoleMgr(RoleMgr roleMgr)
	{
		this.roleMgr = roleMgr;
	}

	@Override
	public Object getModel() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getPid() {
		return pid;
	}
	
	public void setPid(int pid) {
		this.pid = pid;
	}
}