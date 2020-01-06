package com.focus.cos.web.user.service;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.json.JSONArray;
import org.json.JSONObject;

import com.focus.cos.api.LogSeverity;
import com.focus.cos.web.common.AjaxResult;
import com.focus.cos.web.common.PathFactory;
import com.focus.cos.web.common.ZKMgr;
import com.focus.cos.web.ops.service.MonitorMgr;
import com.focus.cos.web.service.SvrMgr;
import com.focus.cos.web.user.dao.UserDAO;
import com.focus.util.Base64X;
import com.focus.util.IOHelper;
import com.focus.util.Tools;
import com.focus.util.Zookeeper;

/**
 * 
 * @author focus
 *
 */
public class RoleMgr extends SvrMgr
{
	private static final Log log = LogFactory.getLog(RoleMgr.class);
	private static HashMap<Integer, String> MapRoleNames = new HashMap<Integer, String>();
	private static HashMap<Integer, Boolean> MapRoleAbort = new HashMap<Integer, Boolean>();
	/*用户DAO*/
	private UserDAO userDao;
	
	public RoleMgr()
	{
		log.info("Initialize the manager of role is "+ this.toString());
	}
	
	public void close()
	{
		log.debug("Close the manager of role from "+this.toString());
		MapRoleNames.clear();
		MapRoleAbort.clear();
	}
	/**
	 * 得到用户的角色权限组树
	 * @param allRoles
	 * @return
	 */
	private JSONArray getUserRoles(JSONObject allRoles)
	{
		return this.getUserRoles(allRoles, super.getAccountRole());
	}
	public JSONArray getUserRoles(JSONObject allRoles, int roleid)
	{
		JSONObject myRole = getMyRole(allRoles, roleid);
		myRole = setRolesTree(myRole, roleid);
		JSONArray array = new JSONArray();
		myRole.put("open", true);
		if( myRole != null ) array.put(myRole);
		return array;
	}
	/**
	 * 第一次加载角色命名
	 */
	public static void loadRoleNames()
	{
		if( MapRoleNames.isEmpty() )
		{
			try
			{
				JSONObject role = ZKMgr.getZookeeper().getJSONObject("/cos/config/role");
				setRolesTree(role, 0);
			}
			catch (Exception e)
			{
				log.error("Faile dto load names of role for ", e);
			}
		}
	}
	
	/**
	 * 是否角色中止
	 * @param roleid
	 * @return
	 */
	public static boolean isRoleAbort(int roleid)
	{
		return MapRoleAbort.containsKey(roleid)?MapRoleAbort.get(roleid):false;
	}
	/**
	 * 
	 * @param roleid
	 * @return
	 */
	public static String getRoleName(int roleid)
	{
		String name = MapRoleNames.get(roleid);
		if( name == null )
		{
			name = "未知角色权限组";
		}
		return name;
	}
	/**
	 * 设置角色权限组的集群权限
	 * @param privileges
	 * @param hosts
	 * @param role
	 * @param roleParent
	 */
	public JSONArray getClusterPrivileges(JSONObject serverPrivileges, JSONObject parentServerPrivileges, boolean isSysadmin)
	{
		JSONArray clusters = null;;
		try
		{
			clusters = new JSONArray(MonitorMgr.getClusters().toString());
			this.getClusterPrivileges(clusters, serverPrivileges, parentServerPrivileges, isSysadmin);
		}
		catch (Exception e) 
		{
			log.error("Failed to get the privileges of cluster", e);
		}
		return clusters;
	}
	
	/**
	 * 是否有伺服器的配置权限
	 * @param role
	 * @param server
	 * @param isSysadmin
	 * @return
	 */
	private JSONObject getServerPrivileges(JSONObject serverPrivileges, JSONObject server, boolean isSysadmin)
	{
		if( isSysadmin ) return new JSONObject();
		if( serverPrivileges == null ) return null;
		String sid = server.has("security-key")?server.getString("security-key"):null;
		server.put("title", sid!=null?sid:"Unknown");
		if( sid == null ) return null;
		if( serverPrivileges.has(sid) )
		{
			return serverPrivileges.getJSONObject(sid);
		}
		else
		{
			return null;
		}
	}
	/**
	 * 设置角色的集群权限配置树
	 * @param clusters
	 * @param role
	 * @param roleParent
	 * @param chkDisabled
	 */
	private void getClusterPrivileges(JSONArray clusters, JSONObject serverPrivileges, JSONObject parentServerPrivileges, boolean isSysadmin)
	{
		for(int i = 0; i < clusters.length(); i++)
		{
			JSONObject cluster = clusters.getJSONObject(i);
			String pid = "manager.cluster."+cluster.getInt("id");
			cluster.put("id", pid);
            if( cluster.has("isParent") && cluster.getBoolean("isParent") ){
            	cluster.put("nocheck", true);
    			if( cluster.has("children") )
    			{
        			JSONArray children = cluster.getJSONArray("children");
        			this.getClusterPrivileges(children, serverPrivileges, parentServerPrivileges, isSysadmin);
        		}
            	continue;
            }
            if( !cluster.has("security-key") )
            {
            	clusters.remove(i);
            	i -= 1;
            	continue;
            }
            String ip = cluster.getString("ip");
			cluster.put("name", "["+ip+":"+cluster.getInt("port")+"]");
			cluster.put("cluster", true);
			cluster.put("icon", "images/icons/cluster.png");
            JSONObject privilegesParent = getServerPrivileges(parentServerPrivileges, cluster, isSysadmin);
            if( privilegesParent == null )
            {//父角色没有权限权限列表不出现
            	clusters.remove(i);
            	i -= 1;
            	continue;
            }
            JSONObject privileges = getServerPrivileges(serverPrivileges, cluster, isSysadmin);
            if( privileges == null )
            {//角色没有权限权限列表不出现
            	clusters.remove(i);
            	i -= 1;
            	continue;
            }
            pid = cluster.getString("security-key");
			cluster.put("id", pid);
			cluster.put("checked", true);
		
            JSONArray buttons = new JSONArray();
            JSONObject p = null;
            if( privileges.has("control") && privilegesParent.has("control") && privilegesParent.getBoolean("control") )
            {
				p = new JSONObject();
				p.put("name", "主控管理");
				p.put("id", "control@"+pid);
				p.put("icon", "images/icons/spell_check.png");
	        	p.put("checked", privileges.getBoolean("control"));
	            buttons.put(p);
            }
            if( privileges.has("files") && privilegesParent.has("files") && privilegesParent.getBoolean("files") )
            {
	            p = new JSONObject();
				p.put("name", "文件管理");
				p.put("id", "files@"+pid);
				p.put("icon", "images/icons/spell_check.png");
	        	p.put("checked", privileges.getBoolean("files"));
	            buttons.put(p);
            }

            if( privileges.has("zookeeper") && privilegesParent.has("zookeeper") && privilegesParent.getBoolean("zookeeper") )
            {
	            p = new JSONObject();
				p.put("name", "ZK管理");
				p.put("id", "zookeeper@"+pid);
				p.put("icon", "images/icons/spell_check.png");
	        	p.put("checked", privileges.getBoolean("zookeeper"));
	            buttons.put(p);
            }

            if( privileges.has("ssh") && privilegesParent.has("ssh") && privilegesParent.getBoolean("ssh") )
            {
	            p = new JSONObject();
				p.put("name", "远程管理");
				p.put("id", "ssh@"+pid);
				p.put("icon", "images/icons/spell_check.png");
	        	p.put("checked", privileges.getBoolean("ssh"));
	            buttons.put(p);
            }
            
            if( buttons.length() > 0 )
            {
				cluster.put("children", buttons);
            }
		}
	}
	/**
	 * 递归设置角色权限组权限
	 * @param modules
	 * @param role
	 * @param roleParent
	 */
	public void setMenusPrivileges(JSONArray modules, JSONObject role, JSONObject roleParent, boolean isSysadmin)
	{
		for(int i = 0; i < modules.length(); i++)
		{
			JSONObject menu = modules.getJSONObject(i);
			menu.remove("CmpDevelopers");
			menu.remove("moduleId");
			menu.remove("CmpPortal");
			menu.remove("dropRoot");
			menu.remove("moduleName");
			menu.remove("CmpDevelopersEmail");
			menu.remove("type");
			menu.remove("target");
			menu.remove("CmpDevelopersContact");
			menu.remove("POP3Username");
			menu.put("title", "");
			if( menu.has("href") )
			{
				menu.put("title", menu.getString("href"));
			}
			menu.remove("href");
			menu.remove("drag");
			menu.remove("nomenu");
			menu.remove("path");
            if( roleParent != null && !roleParent.has(menu.getString("id")) &&  !"toolbar".equals(menu.getString("id")) )
            {
            	modules.remove(i);
            	i -= 1;
            	continue;//父级角色权限组权限不存在就排除过滤
            }
			menu.put("checked", role != null && menu.has("id") && role.has(menu.getString("id")));
//        	if( menu.has("children") && menu.getBoolean("checked") ) menu.put("open", true);
//        	if( menu.getString("id").equals("toolbar") ) menu.put("open", true);
//			else if( role != null && role.getInt("id") != 1 )
//			{
//            	modules.remove(i);
//            	i -= 1;
//            	continue;//父级角色权限组权限不存在就排除过滤
//			}
			if( menu.has("children") )
			{
				JSONArray array = menu.getJSONArray("children");
				setMenusPrivileges(array, role, roleParent, isSysadmin);
				if( array.length() == 0 )
				{
					menu.remove("children");
					menu.remove("isParent");
					menu.put("icon", "images/icons/bookmarks.png");
				}
				else
				{
					boolean nocheck = true;
					for(int j = 0; j < array.length(); j++)
					{
						JSONObject child = array.getJSONObject(j);
						if( child.has("checked") && child.getBoolean("checked") ){
							nocheck = false;
							break;
						}
					}
					if( nocheck ) menu.remove("open");
				}
			}
			if( menu.has("buttons") )
			{
				JSONArray buttons = (JSONArray)menu.remove("buttons");
				for(int j = 0; j < buttons.length(); j++)
				{
					JSONObject button = buttons.getJSONObject(j);
					button.put("id", menu.getString("id")+"."+button.getString("id"));
				}
				menu.put("children", buttons);
				setMenusPrivileges(buttons, role, roleParent, isSysadmin);
			}
		}
	}

	/**
	 * 保存角色权限组权限配置数据
	 * @param id 如果是新增表示父节点ID，如果是修改表示修改节点的ID
	 * @param name
	 * @param privileges
	 * @param timestamp
	 * @return
	 */
	public synchronized AjaxResult<String> saveRole(int pid, int id, String name, String json, long timestamp)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		try
		{
			JSONObject privileges = new JSONObject(json);
			JSONObject role = preset(rsp, timestamp);
			JSONObject roleSet = this.setRoleData(id==-1?0:1, id==-1?pid:id, name, role);
			if( roleSet == null )
			{
				String s = "配置角色权限组【"+name+"】的权限失败，因为未找到ID为【"+id+"】对应的角色权限组。";
				if( id == -1 )
				{

					s = "新增角色权限组【"+name+"】的权限失败，因为未找到ID为【"+id+"】对应的父角色权限组。";
				}
				rsp.setMessage(s);
				logoper(LogSeverity.ERROR, rsp.getMessage(), "角色管理");
			}
			else
			{
				ZKMgr.getZookeeper().setJSONObject("/cos/config/role", role);
				if( id == -1 )
				{
					id = roleSet.getInt("id");
					rsp.setMessage("成功新增角色权限组【"+name+"】的权限配置，角色权限组ID是【"+roleSet.getInt("id")+"】。");
				}
				else
				{
					rsp.setMessage("成功修改角色权限组【"+name+"】的权限配置，角色权限组ID是【"+roleSet.getInt("id")+"】。");
					sendNotiefiesToRole(roleSet, 
							"角色管理",
		                    String.format("权限管理员[%s]"+rsp.getMessage(), getAccountName()),
		                    "登录请检查后权限情况，如有问题请联系您的权限管理员【"+getAccountRealname()+"】。该角色权限组权限如下表所示：",
		                    "role!preview.action?id="+id,
		                    "查看详情", "role!manager.action");
					clearCookie(roleSet);
				}
				privileges.put("parent", pid);
				privileges.put("id", id);
				privileges.put("name", name);
				ZKMgr.getZookeeper().setJSONObject("/cos/config/role/"+id, privileges);
				rsp.setSucceed(true);
				MapRoleNames.put(id, name);
				sendNotiefiesToSystemadmin(
						"角色管理",
						String.format("权限管理员[%s]"+rsp.getMessage(), getAccountName()),
	                    "请检查配置的权限情况，如有问题需协助用户解决。该角色权限组权限如下表所示：",
	                    "role!preview.action?id="+id,
	                    "反馈问题", "#feedback?to="+getAccountName());
			}
			rsp.setResult(getUserRoles(role).toString());
			logoper(rsp.getMessage(), "角色管理", null);
		}
		catch(Exception e)
		{
			String s = "角色权限组【"+name+"】的权限配置出现异常"+e;
			logoper(rsp.getMessage(), "角色管理", e);
			rsp.setMessage(s);
			log.error("Failed to set the menu of modules for exception", e);
		}
		return rsp;
	}
	
	/**
	 * 加载角色权限组设置下所有角色权限组
	 * @param role
	 * @param map
	 */
	private void loadRoleSet(JSONObject role, HashMap<Integer, String> map)
	{
		map.put(role.getInt("id"), role.getString("name"));
		if( !role.has("children") ) return;
		JSONArray children = role.getJSONArray("children");
		for( int i = 0; i < children.length(); i++ )
		{
			this.loadRoleSet(children.getJSONObject(i), map);
		}
	}
	
	/**
	 * 移动角色权限组
	 * @param source
	 * @param target
	 * @param type
	 * @param role
	 * @return
	 */
	private boolean moveRole(JSONObject source, int target, String type, JSONObject role, JSONObject parent)
	{
		if( role == null ) return false;
		JSONArray children = null;
        if( role.has("id") && role.getInt("id") == target )
        {
			if( "inner".equals(type) )
			{
        		if( role.has("children") )
        		{
        			children = role.getJSONArray("children");
        		}
        		else
        		{
        			children = new JSONArray();
        			role.put("children", children);
        		}
        		children.put(source);
        		return true;
			}
			else if( parent != null && parent.has("children") )
			{
				children = parent.getJSONArray("children");
				JSONArray children1 = new JSONArray();
				if( "prev".equals(type) )
				{
	        		for(int i = 0; i < children.length(); i++)
	        		{
	        			JSONObject child = children.getJSONObject(i);
	        			if( child.getInt("id") == target )
	        			{
	        				children1.put(source);
	        			}
	        			children1.put(child);
	        		}
				}
				else if( "next".equals(type) )
				{
	        		for(int i = 0; i < children.length(); i++)
	        		{
	        			JSONObject child = children.getJSONObject(i);
	        			children1.put(child);
	        			if( child.getInt("id") == target )
	        			{
	        				children1.put(source);
	        			}
	        		}
				}
    			parent.put("children", children1);
				return true;
			}
        	return false;
        }
		if( !role.has("children") ) return false;
		children = role.getJSONArray("children");
		for(int i = 0; i < children.length(); i++)
		{
			JSONObject child = children.getJSONObject(i);
			if( moveRole(source, target, type, child, role) )
			{
				return true;
			}
		}
		return false;
	}
	/**
	 * 拖动菜单
	 * @param type
	 * @param pathSource
	 * @param pathTarget
	 * @param timestamp
	 * @param moduleid
	 * @return
	 */
	public synchronized AjaxResult<String> dragDropRole(
			String type,
			int idSource,
			int idTarget,
			int idTargetParent,
			long timestamp)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		try
		{
			if( idSource == 1)
			{
				rsp.setMessage("角色权限组【系统管理员组】不能被移动。");
				return rsp;
			}
			JSONObject role = preset(rsp, timestamp);
			JSONObject roleSet = this.setRoleData(3, idSource, null, role);
			if( roleSet == null )
			{
				String s = "移动角色权限组失败，因为未找到ID为【"+idSource+"】对应的角色权限组。";
				rsp.setMessage(s);
				logoper(LogSeverity.ERROR, rsp.getMessage(), "角色管理");
			}
			else
			{
//				System.err.println(role.toString(4));
				log.info("Move the role to "+idTarget+"/"+idTargetParent+" by "+type);
				this.moveRole(roleSet, idTarget, type, role, null);
//				System.err.println(role.toString(4));
				Stat stat = ZKMgr.getZookeeper().setJSONObject("/cos/config/role", role);
				rsp.setTimestamp(stat.getMtime());
				JSONObject privileges = ZKMgr.getZookeeper().getJSONObject("/cos/config/role/"+roleSet.getInt("id"));
				privileges.put("parent", idTargetParent);
				ZKMgr.getZookeeper().setJSONObject("/cos/config/role/"+roleSet.getInt("id"), privileges);
				setRoleSate(roleSet, isRoleAbort(idTargetParent));
				rsp.setMessage("移动角色权限组【"+roleSet.getString("name")+"】所属成功。");
				rsp.setSucceed(true);
				logoper(rsp.getMessage(), "角色管理", null);
				sendNotiefiesToSystemadmin(
						"角色管理",
						String.format("权限管理员[%s]"+rsp.getMessage(), getAccountName()),
	                    "请检查配置的权限情况，如有问题需协助用户解决。该角色权限组权限如下表所示：",
	                    "role!preview.action?id="+idSource,
	                    "查看详情", "role!manager.action");
				sendNotiefiesToRole(roleSet,
					"角色管理", 
                    String.format("权限管理员[%s]"+rsp.getMessage(), getAccountName()),
                    "登录请检查后权限情况，如有问题请联系您的权限管理员【"+getAccountRealname()+"】。该角色权限组权限如下表所示：",
                    "role!preview.action?id="+idSource,
                    "反馈问题", "#feedback?to="+getAccountName());
			}
			rsp.setResult(getUserRoles(role).toString());
		}
		catch(Exception e)
		{
			String s = "移动角色权限组所属出现异常"+e;
			logoper(rsp.getMessage(), "角色管理", e);
			rsp.setMessage(s);
			log.error("Failed to drag&drop the role for exception", e);
		}
		return rsp;		
	}
	/**
	 * 重命名角色权限组
	 * @param id
	 * @param name
	 * @param timestamp
	 * @return
	 */
	public synchronized AjaxResult<String> renameRole(int id, String name, long timestamp)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		try
		{
			if( id == 1)
			{
				rsp.setMessage("角色权限组【系统管理员组】不能被重命名。");
				return rsp;
			}
			if( getAccountRole() == id )
			{
				rsp.setMessage("您是该角色权限组的被授权人，您只能对您的子角色权限组执行重命名操作。");
				return rsp;
			}
			JSONObject role = preset(rsp, timestamp);
			JSONObject roleSet = this.setRoleData(1, id, name, role);
			if( roleSet == null )
			{
				String s = "重命角色权限组名称【"+name+"】失败，因为未找到ID为【"+id+"】对应的角色权限组。";
				rsp.setMessage(s);
				rsp.setResult(getUserRoles(role).toString());
				logoper(LogSeverity.ERROR, rsp.getMessage(), "角色管理");
			}
			else
			{
//				System.err.println(roleSet.toString(4));
				Stat stat = ZKMgr.getZookeeper().setJSONObject("/cos/config/role", role);
				rsp.setTimestamp(stat.getMtime());
				rsp.setMessage("重命角色权限组名称【"+roleSet.getString("oldname")+">>"+name+"】成功。");
				rsp.setSucceed(true);
				MapRoleNames.put(id, name);
				logoper(rsp.getMessage(), "角色管理", null);
				sendNotiefiesToSystemadmin(
					"角色管理", 
					String.format("权限管理员[%s]"+rsp.getMessage(), getAccountName()),
                    "请检查配置的权限情况，如有问题需协助用户解决。该角色权限组权限如下表所示：",
                    "role!preview.action?id="+id,
                    "查看详情", "role!manager.action");
				sendNotiefiesToRole(roleSet, 
					"角色管理", 
                    String.format("权限管理员[%s]"+rsp.getMessage(), getAccountName()),
                    "登录请检查后权限情况，如有问题请联系您的权限管理员【"+getAccountRealname()+"】。该角色权限组权限如下表所示：",
                    "role!preview.action?id="+id,
                    "反馈问题", "#feedback?to="+getAccountName());
			}
		}
		catch(Exception e)
		{
			String s = "重命名角色权限组【"+name+"】出现异常"+e;
			logoper(rsp.getMessage(), "角色管理", e);
			rsp.setMessage(s);
			log.error("Failed to set the menu of modules for exception", e);
		}
		return rsp;
	}

	/**
	 * 设置角色权限组状态
	 * @param id
	 * @param state 2该状态
	 * @param timestamp
	 * @return
	 */
	public synchronized AjaxResult<String> setRoleState(int id, boolean abort, long timestamp)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		try
		{
			if( id == 1)
			{
				rsp.setMessage("角色权限组【系统管理员组】不能被启用禁用。");
				return rsp;
			}
			if( getAccountRole() == id )
			{
				rsp.setMessage("您是该角色权限组的被授权人，您只能对您的子角色权限组设置启动禁用。");
				return rsp;
			}
			JSONObject role = preset(rsp, timestamp);
			JSONObject roleSet = this.setRoleData(2, id, null, role);
			if( roleSet == null )
			{
				String s = "设置角色权限组状态失败，因为未找到ID为【"+id+"】对应的角色权限组。";
				rsp.setMessage(s);
				rsp.setResult(getUserRoles(role).toString());
				logoper(LogSeverity.ERROR, rsp.getMessage(), "角色管理");
			}
			else
			{
				setRoleSate(roleSet, abort);
				Stat stat = ZKMgr.getZookeeper().setJSONObject("/cos/config/role", role);
				rsp.setTimestamp(stat.getMtime());
				if( abort )
				{
					rsp.setMessage("禁用角色权限组【"+roleSet.getString("name")+"】成功。");
				}
				else
				{
					rsp.setMessage("启用角色权限组【"+roleSet.getString("name")+"】成功。");
				}
				JSONObject privileges = ZKMgr.getZookeeper().getJSONObject("/cos/config/role/"+roleSet.getInt("id"));
				privileges.put("_abort", abort);
				ZKMgr.getZookeeper().setJSONObject("/cos/config/role/"+roleSet.getInt("id"), privileges);
				clearCookie(roleSet);
				sendNotiefiesToSystemadmin(
					"角色管理", 
					String.format("权限管理员[%s]"+rsp.getMessage(), getAccountName()),
                    "请检查配置的权限情况，如有问题需协助用户解决。该角色权限组权限如下表所示：",
                    "role!preview.action?id="+id,
                    "查看详情", "role!manager.action");
				sendNotiefiesToRole(roleSet,
					"角色管理",  
                    String.format("权限管理员[%s]"+rsp.getMessage(), getAccountName()),
                    "登录请检查后权限情况，如有问题请联系您的权限管理员【"+getAccountRealname()+"】。该角色权限组权限如下表所示：",
                    "role!preview.action?id="+id,
                    "反馈问题", "#feedback?to="+getAccountName());
				rsp.setSucceed(true);
				logoper(rsp.getMessage(), "角色管理", null);
			}
		}
		catch(Exception e)
		{
			String s = "启用禁用角色权限组【"+id+"】出现异常"+e;
			logoper(rsp.getMessage(), "角色管理", e);
			rsp.setMessage(s);
			log.error("Failed to set the menu of modules for exception", e);
		}
		return rsp;
	}

	/**
	 * 删除角色权限组
	 * @param id
	 * @param timestamp
	 * @return
	 */
	public synchronized AjaxResult<String> delRole(int id, long timestamp)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		try
		{
			if( id == 1)
			{
				rsp.setMessage("角色权限组【系统管理员组】不能被启用禁用。");
				return rsp;
			}
			if( getAccountRole() == id )
			{
				rsp.setMessage("您是该角色权限组的被授权人，您只能对您的子角色权限组设置启动禁用。");
				return rsp;
			}
			JSONObject role = preset(rsp, timestamp);
			log.warn("Delete the role of "+id);
			JSONObject roleDeleted = this.setRoleData(3, id, null, role);
			if( roleDeleted == null )
			{
				String s = "删除角色权限组失败，因为未找到ID为【"+id+"】对应的角色权限组。";
				rsp.setMessage(s);
				rsp.setResult(getUserRoles(role).toString());
				logoper(LogSeverity.ERROR, rsp.getMessage(), "角色管理");
			}
			else
			{
				Zookeeper zk = ZKMgr.getZookeeper();
				log.warn("Save the change of role("+id+")");
				Stat stat = zk.setJSONObject("/cos/config/role", role);
				log.warn("Delete the change of role("+id+")");
				setRoleDeleted(zk, roleDeleted);
				log.warn("Succeed to delete the role("+id+")");
				rsp.setTimestamp(stat.getMtime());
				rsp.setMessage("删除角色权限组【"+roleDeleted.getString("name")+"】成功。");
				clearCookie(roleDeleted);
				sendNotiefiesToSystemadmin(
					"角色管理", 
					String.format("权限管理员[%s]"+rsp.getMessage(), getAccountName()),
                    "请检查配置的权限情况，如有问题需协助用户解决。该角色权限组权限如下表所示：",
                    "role!preview.action?id="+id,
                    "查看详情", "role!manager.action");
				sendNotiefiesToRole(roleDeleted,
					"角色管理",  
                    String.format("权限管理员[%s]"+rsp.getMessage(), getAccountName()),
                    "登录请检查后权限情况，如有问题请联系您的权限管理员【"+getAccountRealname()+"】。该角色权限组权限如下表所示：",
                    "role!preview.action?id="+id,
                    "反馈问题", "#feedback?to="+getAccountName());
				rsp.setSucceed(true);
				rsp.setResult(getUserRoles(role).toString());
				logoper(rsp.getMessage(), "角色管理", null);
				log.warn("Succeed to response.");
			}
		}
		catch(Exception e)
		{
			String s = "删除角色权限组【"+id+"】出现异常"+e;
			logoper(rsp.getMessage(), "角色管理", e);
			rsp.setMessage(s);
			log.error("Failed to set the menu of modules for exception", e);
		}
		return rsp;
	}
	/**
	 * 将原来用文件存储的组件配置转换成保存到ZK中
	public static void buildRoles()
	{
		StringBuffer sb = new StringBuffer();
		try
		{
			File path = new File(PathFactory.getCfgPath(), "role/");
			File[] files = path.listFiles(new FileFilter(){
				public boolean accept(File file)
				{
					if(file.isDirectory()) return false;
					if(file.isHidden()) return false;
					if( file.getName().toLowerCase().endsWith(".tmp") ) return false;
					return Tools.isNumeric(file.getName());
				}
			});
//			if( files == null || files.length == 0 )
//			{
//				path.delete();
//				return;
//			}
			sb.append("\r\n\tFound "+(files!=null?files.length:"null")+" files from "+path);
			ZooKeeper zookeeper = ZKMgr.getZooKeeper();
			String zkpath = "/cos/config/role";
			Stat stat = zookeeper.exists(zkpath, false); 
			if( stat != null)
			{
				return;
			}
			JSONObject roleObj = new JSONObject();
			roleObj.put("id", 1);
			roleObj.put("name", "系统管理员组");
			sb.append("\r\n");
			sb.append(roleObj.toString(4));
			zookeeper.create(zkpath, roleObj.toString().getBytes("UTF-8"), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			HashMap<Integer, JSONObject> map = new HashMap<Integer, JSONObject>();
			if( files!= null )
				for(File file : files)
				{
					com.focus.web.user.vo.Role role = (com.focus.web.user.vo.Role)IOHelper.readSerializableNoException(file);
					file.delete();
					if( role == null )
					{
						sb.append("\r\n\tDelete the profile of "+file.getPath()+" for cannot serializable.");
						continue;
					}
					sb.append("\r\n\tDelete the profile of "+file.getPath()+" for.");
					String zkpathrole = zkpath +"/"+ role.getRid();
					stat = zookeeper.exists(zkpathrole, false); 
					JSONObject e = new JSONObject();
					e.put("id", role.getRid());
					e.put("name", role.getName());
					Iterator<String> all = role.getAllPermissions().keySet().iterator();
					while(all.hasNext())
					{
						String id = all.next();
						e.put(id, role.getAllPermissions().get(id));
					}
					if( role.getRid() != 1 ) map.put(role.getRid(), e);
					if( stat == null)
					{
						zookeeper.create(zkpathrole, e.toString().getBytes("UTF-8"), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
					}
					else
					{
						zookeeper.setData(zkpathrole, e.toString().getBytes("UTF-8"), stat.getVersion());
					}
				}
			setRoleData(1, map, roleObj);
			ZKMgr.setJSONObject("/cos/config/role", roleObj);
			path.delete();
			log.info("Succed to build the profile of rolemgr to zookeeper:"+sb.toString());
		}
		catch(Exception e )
		{
			log.error("Failed to build the profile of rolemgr to zookeeper for exception \r\n"+sb, e);
		}
	}
	 */
	
	/**
	 * 设置角色权限组数据
	private static void setRoleData(int roleid, HashMap<Integer, JSONObject> map, JSONObject roleObj)
		throws Exception
	{
		Iterator<JSONObject> iterator = map.values().iterator();
		ArrayList<JSONObject> list = new ArrayList<JSONObject>();
		while(iterator.hasNext())
		{
			JSONObject role = iterator.next();
			list.add(role);
		}
		JSONArray children = new JSONArray();
		for(JSONObject role: list)
		{
			if( role.has("parent") && role.getInt("parent") == roleid )
			{
				JSONObject e = new JSONObject();
				e.put("id", role.getInt("id"));
				e.put("name", role.getString("name"));
				children.put(e);
				map.remove(role.getInt("id"));
				setRoleData(role.getInt("id"), map, e);
			}
		}
		if( children.length() > 0 )
		{
			roleObj.put("children", children);
		}
	}
	 */

	/**
	 * 清除cookie
	 * @param role
	 */
	private void clearCookie(JSONObject role)
	{
		StringBuffer str = new StringBuffer("Delete the cookie of all roles from ");
		try
		{
			str.append(role.getString("name")+"["+role.getInt("id")+"].");
			if( role.getInt("id") == 1 )
			{
				return;
			}
			HashMap<Integer, String> map = new HashMap<Integer, String>();
			loadRoleSet(role, map);
			ZooKeeper zookeeper = ZKMgr.getZooKeeper();
			String path = "/cos/login/cookie";
			Stat stat = zookeeper.exists(path, false); 
			if( stat != null)
			{
				List<String> list = zookeeper.getChildren(path, false);
				for( String cookie : list )
				{
					cookie = path+"/"+cookie;
					stat = zookeeper.exists(cookie, false); 
					if( stat != null )
					{
						String json = new String(zookeeper.getData(cookie, false, stat), "UTF-8");
						JSONObject c = new JSONObject(json);
						if( c.has("roleid") && map.containsKey(c.getInt("roleid")) )
						{
							zookeeper.delete(cookie, stat.getVersion());
							str.append("\r\n\t");
							str.append(c.getString("username"));
							str.append("("+c.getInt("roleid")+")\t");
							str.append(cookie);
							if( c.has("token") )
							{
								String token = c.getString("token");
								token = "/cos/login/token/"+token;
								stat = zookeeper.exists(token, false); 
								if( stat != null )
								{
									zookeeper.delete(token, stat.getVersion());
									str.append("\t");str.append(token);
								}
							}
						}
					}
					else
						str.append("\r\n\tNot found "+cookie);
				}
			}
			else
				str.append("\r\n\tNot found "+path);
		}
		catch(Exception e)
		{
			str.append("\r\n\tFailed to delete the cookie for exception"+e);
		}
		log.info(str.toString());
	}

	/**
	 * 递归设置角色权限组权限
	 * @param modules
	 * @param role
	 * @param roleParent
	 */
	public static JSONObject getMyRole(JSONObject role, int roleid)
	{
		if( role == null ) return null;
		if( role.has("id") && role.getInt("id") == roleid ) return role;
		if( !role.has("children") )
		{
			return null;
		}
		JSONArray children = role.getJSONArray("children");
		for(int i = 0; i < children.length(); i++)
		{
			role = getMyRole(children.getJSONObject(i), roleid);
			if( role != null ) return role;
		}
		return null;
	}

	/**
	 * 设置角色树
	 * @param role
	 */
	public static JSONObject setRolesTree(JSONObject role, int pid)
	{
		if( role == null ) return null;
		role.put("dropRoot", false);
		role.put("isParent", true);
		role.put("checked", pid > 0 && pid == role.getInt("id")); 
		MapRoleNames.put(role.getInt("id"), role.getString("name"));
		MapRoleAbort.put(role.getInt("id"), role.has("abort")?role.getBoolean("abort"):false);
		if( role.has("children") )
		{
			JSONArray children = role.getJSONArray("children");
			for(int i = 0; i < children.length(); i++)
			{
				setRolesTree(children.getJSONObject(i), pid);
			}
		}
		return role;
	}
	/**
	 * 获取最新的角色权限组ID
	 * @return
	 * @throws Exception
	 */
	private int getNewRoleId() throws Exception
	{
		ZooKeeper zookeeper = ZKMgr.getZooKeeper();
		for( int i = 2; i < 128; i++ )
		{
			Stat stat = zookeeper.exists("/cos/config/role/"+i, false);
			if( stat == null )
			{
				return i;
			}
		}
		throw new Exception("你设置的角色权限组分组个数超过了系统运行的最大个数。");
	}
	
	/**
	 * 创建缺省的角色权限组配置
	 * @return
	 */
	private JSONObject createDefaultRole()
	{
		JSONObject role = new JSONObject();
		role.put("id", 1);
		role.put("name", "系统管理员组");
		try 
		{
			ZKMgr.getZookeeper().setJSONObject("/cos/config/role", role);
		}
		catch (Exception e) 
		{
			log.error("Failed to create the default role.", e);
		}
		return role;
	}
	
	/**
	 * 预设置数据
	 * @param rsp
	 * @param timestamp
	 * @return
	 * @throws Exception
	 */
	private JSONObject preset(AjaxResult<String> rsp, long timestamp) throws Exception
	{
		JSONObject role = null;
		ZooKeeper zookeeper = ZKMgr.getZooKeeper();
		Stat stat = zookeeper.exists("/cos/config/role", false); 
		if( stat == null)
		{
			role = createDefaultRole();
			stat = zookeeper.exists("/cos/config/role", false); 
			String s = "角色权限组配置丢失，被系统重新创建。";
			rsp.setMessage(s);
			rsp.setResult(getUserRoles(role).toString());
		}
		else
		{
			role = new JSONObject(new String(zookeeper.getData("/cos/config/role", false, stat), "UTF-8"));
			if( timestamp != stat.getMtime() )
			{
				String s = "角色权限组配置在"+Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", stat.getMtime())+"被其它用户所修改。";
				rsp.setMessage(s);
				rsp.setResult(getUserRoles(role).toString());
			}
		}
		rsp.setTimestamp(stat.getMtime());
		return role;
	}
	/**
	 * 启用禁用
	 * @param role
	 * @param flag
	 */
	private void setRoleSate(JSONObject role, boolean abort)
	{
		if( role == null ) return;
		role.put("abort", abort);
		MapRoleAbort.put(role.getInt("id"), abort);
		JSONArray children = null;
		if( !role.has("children") ) return;
		children = role.getJSONArray("children");
		for(int i = 0; i < children.length(); i++)
		{
			JSONObject child = children.getJSONObject(i);
			setRoleSate(child, abort) ;
		}
	}
	
	/**
	 * 设置删除的角色
	 * @param role
	 */
	private void setRoleDeleted(Zookeeper zk, JSONObject role){
		if( role == null ) return;
		MapRoleAbort.remove(role.getInt("id"));
		zk.delete("/cos/config/role/"+role.getInt("id"));
		userDao.disableRole(role.getInt("id"));//禁用该橘色角色的用户
		JSONArray children = null;
		if( !role.has("children") ) return;
		children = role.getJSONArray("children");
		for(int i = 0; i < children.length(); i++)
		{
			JSONObject child = children.getJSONObject(i);
			setRoleDeleted(zk, child) ;
		}
	}
	
	/**
	 * 加载所有的角色ID
	 * @param role
	 * @param sb
	 */
	public void setRoleString(JSONObject role, StringBuffer sb)
	{
		if( role == null ) return;
		sb.append(",");sb.append(role.getInt("id"));
		JSONArray children = null;
		if( !role.has("children") ) return;
		children = role.getJSONArray("children");
		for(int i = 0; i < children.length(); i++)
		{
			JSONObject child = children.getJSONObject(i);
			setRoleString(child, sb) ;
		}
	}
	/**
	 * 
	 * @param method 0 新增 1修改 2改状态 3删除
	 * @param id
	 * @param name
	 * @param role
	 */

	private JSONObject setRoleData(int method, int id, String name, JSONObject role) throws Exception
	{
		return this.setRoleData(method, id, name, role, null, 0);
	}
	
	private JSONObject setRoleData(int method, int id, String name, JSONObject role, JSONArray parent, int index) throws Exception
	{
		if( role == null ) return null;
		JSONArray children = null;
        if( role.has("id") && role.getInt("id") == id )
        {
        	if( method == 0 )
        	{
        		JSONObject child = new JSONObject();
        		child.put("id", getNewRoleId());
        		child.put("name", name);
        		if( role.has("children") )
        		{
        			children = role.getJSONArray("children");
        		}
        		else
        		{
        			children = new JSONArray();
        			role.put("children", children);
        		}
        		children.put(child);
        		return child;
        	}
        	else if( method == 1 )
        	{
        		role.put("oldname", role.getString("name"));
        		role.put("name", name);
        	}
        	else if( method == 3 && parent != null )
        	{
        		parent.remove(index);
        	}
        	return role;
        }
		if( !role.has("children") ) return null;
		children = role.getJSONArray("children");
		for(int i = 0; i < children.length(); i++)
		{
			JSONObject child = children.getJSONObject(i);
			role = setRoleData(method, id, name, child, children, i) ;
			if( role != null )
			{
				return role;
			}
		}
		return null;
	}
	
	/**
	 * 得到角色权限
	 * @param roleid
	 * @return
	 */
	public static JSONObject getRolePrivileges(int roleid)
	{
		return getRolePrivileges(roleid, null);
	}
	public static JSONObject getRolePrivileges(int roleid, String account)
	{
		File dir = new File(PathFactory.getCfgPath(), "role");
		if( !dir.exists() ) dir.mkdir();
		File file = new File(dir, String.valueOf(roleid));
		JSONObject role = null;
		try
		{
			role = ZKMgr.getZookeeper().getJSONObject("/cos/config/role/"+roleid);
			if( role != null ){
				setUserPrivileges(account, role);
				IOHelper.writeFile(file, Base64X.encode(role.toString().getBytes()).getBytes());
			}
		}
		catch(Exception e)
		{
			if( file.exists() )
			{
				byte[] buf = IOHelper.readAsByteArray(file);
				String base64 = new String(buf);
				buf = Base64X.decode(base64);
				String json = new String(buf);
				role = new JSONObject(json);
			}
		}
		return role;
	}

	/**
	 * 得到用户的配置权限
	 * @param user
	 * @param role
	 * @return
	 */
	private static void setUserPrivileges(String account, JSONObject role)
	{
		if( account == null ) return;
		JSONObject userPrivileges = new JSONObject();
    	try
    	{
        	ZooKeeper zookeeper = ZKMgr.getZooKeeper();
			String path = "/cos/user/privileges/"+account;
			Stat stat = zookeeper.exists(path, false); 
			stat = zookeeper.exists(path, false); 
			if( stat != null)
			{
				String json = new String(zookeeper.getData(path, false, stat), "UTF-8");
				userPrivileges =  new JSONObject(json);
				JSONObject clusterPrivileges = role.has("##cluster")?role.getJSONObject("##cluster"):new JSONObject();
				Iterator<?> iterator = userPrivileges.keySet().iterator();
				while( iterator.hasNext() )
				{
					String key = iterator.next().toString();
					role.remove(key);
					if( clusterPrivileges.has(key) )
					{
						clusterPrivileges.remove(key);
						continue;
					}
					String args[] = Tools.split(key, "@");
					if( args.length != 2 )
					{
						continue;
					}
					if( clusterPrivileges.has(args[1]) )
					{
						clusterPrivileges.getJSONObject(args[1]).remove(args[0]);
					}
				}
			}
    	}
    	catch(Exception e)
    	{
    	}
	}
	
	public void setUserDao(UserDAO userDao) {
		this.userDao = userDao;
	}
}
