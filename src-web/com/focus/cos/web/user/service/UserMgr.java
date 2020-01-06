package com.focus.cos.web.user.service;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.json.JSONArray;
import org.json.JSONObject;

import com.focus.cos.api.LogSeverity;
import com.focus.cos.api.Status;
import com.focus.cos.api.Sysuser;
import com.focus.cos.api.SysuserClient;
import com.focus.cos.web.common.AjaxResult;
import com.focus.cos.web.common.PathFactory;
import com.focus.cos.web.common.ZKMgr;
import com.focus.cos.web.common.paginate.PageBean;
import com.focus.cos.web.service.SvrMgr;
import com.focus.cos.web.user.dao.UserDAO;
import com.focus.cos.web.user.vo.User;
import com.focus.util.Zookeeper;

public class UserMgr extends SvrMgr
{
	private static final Log log = LogFactory.getLog(UserMgr.class);
	private UserDAO userDao;
	/**
	 * 得到指定用户组以上级别归属用户列表
	 * @param userRoleId
	 * @return
	 * @throws Exception
	 */
	public JSONObject getCreatorUsers(User theuser) throws Exception
	{
		JSONObject role = ZKMgr.getZookeeper().getJSONObject("/cos/config/role"); 
		if( role == null)
		{
			throw new Exception("角色权限组配置不存在，请先配置角色权限组配置");
		}
		role.put("open", true);
		setCreatorUsers(role, getUserDao().findAllUsers(), theuser);
		return role;
	}

	/**
	 * 执行用户记录的加载
	 * @param role
	 * @param users
	 * @param filters
	 * @param creator
	 * @return
	 */
	public int setCreatorUsers(JSONObject role, List<?> users, User theuser)
	{
		role.put("isParent", true);
		role.put("drag", false);
		role.put("open", true);
		role.put("ico", "fa-group");
		role.put("rname", role.getString("name"));
		role.put("nocheck", true);
		JSONArray children = new JSONArray();
		int usercount = 0;
		boolean yes = false;
		for(int i = 0; i < users.size(); i++)
		{
			User user = (User)users.get(i);
			if( Status.Enable.getValue() != user.getStatus() ){
				continue;
			}
			if( user.getRoleid() == role.getInt("id") )
			{
				JSONObject account = new JSONObject();
				account.put("id", user.getUsername());
				account.put("rname", user.getRealname());
				account.put("roleid", user.getRoleid());
				account.put("email", user.getEmail());
				account.put("name", user.getRealname()+"("+user.getUsername()+")");
				account.put("icon", user.getSex()==1?"images/icons/boy.png":"images/icons/girl.png");
				account.put("abort", user.getStatus()!=Status.Enable.getValue());
				account.put("ico", user.getStatus()==Status.Enable.getValue()?"":"fa-minus-circle");
				account.put("dropInner", false);
				account.put("checked", user.getUsername().equals(theuser.getCreator()));
				if(theuser.getUsername().equals(user.getUsername())){
					yes = true;
					continue;
				}
				usercount += 1;
				account.put("chkDisabled", false);
				children.put(account);
				users.remove(i);
				i -= 1;
			}
		}
		if( usercount > 0 )
		{
			role.put("usercount", usercount);
			role.put("name", role.getString("name")+"("+usercount+"人)");
		}
		if( theuser.getRoleid() != role.getInt("id") ){
			if( role.has("children") ){
				JSONArray array = role.getJSONArray("children");
				if( array.length() == 0 ){
					return -1;
				}
				JSONObject e = null;
				for(int i = 0; i < array.length(); i++)
				{
					e = array.getJSONObject(i);
					int c = setCreatorUsers(e, users, theuser);
					if( c >= 0 ){
						yes = true;
						usercount += c;
						break;
					}
					else{
						array.remove(i);
						i -= 1;
					}
				}
				if( yes ){
					children.put(e);
				}
			}
			else{
				return -1;
			}
		}
		role.put("children", children);
		return yes?usercount:-1;
	}
	/**
	 * 获得角色用户导航
	 * @return
	 */
	public JSONArray getRoleUsers(int roleid) throws Exception
	{
		return getRoleUsers(roleid, null);
	}

	public JSONArray getRoleUsers(int roleid, JSONObject filters) throws Exception
	{
		return getRoleUsers(roleid, filters, null);
	}
	
	public JSONArray getRoleUsers(int roleid, JSONObject filters, String username) throws Exception
	{
		JSONObject role = ZKMgr.getZookeeper().getJSONObject("/cos/config/role"); 
		if( role == null)
		{
			throw new Exception("角色权限组配置不存在，请先配置角色权限组配置");
		}
//		timestamp = stat.getMtime();
		JSONArray roles = new JSONArray();
		role = RoleMgr.getMyRole(role, roleid);
		if( role == null ){
			log.warn("Failed to get the users of role "+roleid);
			return new JSONArray();
		}
		List<?> users = getUserDao().findAllUsers();
		HashMap<String, String> creators = null;

		if( username != null ){
			creators = new HashMap<String, String>();
			creators.put(username, null);
		}
		role.put("open", true);
		ArrayList<User> removed = new ArrayList<User>();
		setUsers(role, users, removed, filters, creators);
		if( role != null ) roles.put(role);
		if( roleid == 1 && !users.isEmpty() && filters == null )
		{
			role = new JSONObject();
			role.put("id", -1);
			role.put("name", "未分配角色用户");
			role.put("ico", "fa-user-times");
			role.put("isParent", true);
			role.put("drag", false);
			role.put("nocheck", true);
			role.put("dropInner", false);
			role.put("iconClose", "images/icons/folder_closed.png");
			role.put("iconOpen",  "images/icons/folder_opened.png");
			JSONArray children = new JSONArray();
			for(int i = 0; i < users.size(); i++)
			{
				User user = (User)users.get(i);
				if( user.getUsername().equals("admin") ) continue;
//				if( !user.getUsername().equals(super.getAccountName())) continue;
				JSONObject account = new JSONObject();
				account.put("id", user.getUsername());
				account.put("roleid", user.getRoleid());
				account.put("rname", user.getRealname());
				account.put("email", user.getEmail());
				account.put("name", user.getRealname()+"("+user.getUsername()+")");
				account.put("icon", user.getSex()==1?"images/icons/boy.png":"images/icons/girl.png");
				account.put("abort", user.getStatus());
				account.put("ico", user.getStatus()==Status.Enable.getValue()?"":"fa-minus-circle");
				account.put("dropInner", false);
				children.put(account);
			}
			role.put("children", children);
//			role.put("open", true);
			role.put("rname", role.getString("name"));
			role.put("name", role.getString("name")+"("+children.length()+"人)");
			roles.put(role);
		}
		if( roleid == 1 && !removed.isEmpty() && filters == null )
		{
			role = new JSONObject();
			role.put("id", -2);
			role.put("name", "已停用账户");
			role.put("ico", "fa-user-times");
			role.put("isParent", true);
			role.put("drag", false);
			role.put("nocheck", true);
			role.put("dropInner", false);
			role.put("iconClose", "images/icons/folder_closed.png");
			role.put("iconOpen",  "images/icons/folder_opened.png");
			JSONArray children = new JSONArray();
			for(int i = 0; i < removed.size(); i++)
			{
				User user = removed.get(i);
//				if( !user.getUsername().equals(super.getAccountName())) continue;
				JSONObject account = new JSONObject();
				account.put("id", user.getUsername());
				account.put("roleid", user.getRoleid());
				account.put("rname", user.getRealname());
				account.put("email", user.getEmail());
				account.put("name", user.getRealname()+"("+user.getUsername()+")");
				account.put("icon", user.getSex()==1?"images/icons/boy.png":"images/icons/girl.png");
				account.put("abort", user.getStatus());
				account.put("ico", user.getStatus()==Status.Enable.getValue()?"":"fa-minus-circle");
				account.put("dropInner", false);
				children.put(account);
			}
			role.put("children", children);
//			role.put("open", true);
			role.put("rname", role.getString("name"));
			role.put("name", role.getString("name")+"("+children.length()+"人)");
			roles.put(role);
		}
		return roles;
	}

	/**
	 * 设置用户私有权限
	 * @param id
	 * @param name
	 * @param timestamp
	 * @return
	 */
	public synchronized AjaxResult<String> setPrivileges(String account, int pid, String json, long timestamp)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		try
		{
			String zkpath = "/cos/user/privileges/"+account;
			JSONObject privileges = ZKMgr.getZookeeper().getJSONObject(zkpath);
			if( privileges == null )
			{
				ZooKeeper zookeeper = ZKMgr.getZooKeeper();
				String path = "/cos/user";
				Stat stat = zookeeper.exists(path, false); 
				if( stat == null)
				{
					zookeeper.create(path, "用户管理相关数据".getBytes("UTF-8"), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				}
				path = "/cos/user/privileges";
				stat = zookeeper.exists(path, false); 
				if( stat == null)
				{
					zookeeper.create(path, "用户私有权限配置目录".getBytes("UTF-8"), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				}
			}
			privileges = new JSONObject(json);
			ZKMgr.getZookeeper().setJSONObject(zkpath, privileges);
			rsp.setMessage("设置用户【"+account+"】私有权限成功。");
			clearCookie(account);
			sendNotiefiesToSystemadmin(
				"用户管理", 
				String.format("权限管理员[%s]"+rsp.getMessage(), getAccountName()),
                "请检查配置的权限情况，如有问题需协助用户解决。该用户所属角色权限组情况下表所示：",
                "user!query.action?id="+pid,
                "查看详情",
                "user!manager.action");
			this.sendNotiefieToAccount(account,
				"用户管理",  
                String.format("权限管理员[%s]"+rsp.getMessage(), getAccountName()),
                "用户权限配置发生变化，请检查您的权限配置情况，如有问题请联系您的权限管理员【"+getAccountRealname()+"】。该用户信息如下表所示：",
                "user!preview.action?id="+account,
                "反馈问题",
                "#feedback?to="+getAccountName());
			rsp.setSucceed(true);
			logoper(rsp.getMessage(), "用户管理", null);
		}
		catch(Exception e)
		{
			String s = "设置用户【"+account+"】私有权限出现异常"+e;
			logoper(rsp.getMessage(), "用户管理", e);
			rsp.setMessage(s);
			log.error("Failed to set the menu of modules for exception", e);
		}
		return rsp;
	}

	/**
	 * 设置用户私有权限
	 * @param id
	 * @param name
	 * @param timestamp
	 * @return
	 */
	public synchronized AjaxResult<String> clearPrivileges(String account, int pid, long timestamp)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		try
		{
			String zkpath = "/cos/user/privileges/"+account;
			ZooKeeper zookeeper = ZKMgr.getZooKeeper();
			Stat stat = zookeeper.exists(zkpath, false);
			if( stat == null )
			{
				rsp.setMessage("要清除的用户【"+account+"】私有权限并不存在。");
				return rsp;
			}
			zookeeper.delete(zkpath, stat.getVersion());
			rsp.setMessage("清除的用户【"+account+"】私有权限成功。");
			clearCookie(account);
			sendNotiefiesToSystemadmin(
				"用户管理", 
				String.format("权限管理员[%s]"+rsp.getMessage(), getAccountName()),
                "请检查配置的权限情况，如有问题需协助用户解决。该用户所属角色权限组情况下表所示：",
                "user!query.action?id="+pid,
                "查看详情",
                "user!manager.action");
			this.sendNotiefieToAccount(account,
				"用户管理",  
                String.format("权限管理员[%s]"+rsp.getMessage(), getAccountName()),
                "用户权限配置发生变化，请检查您的权限配置情况，如有问题请联系您的权限管理员【"+getAccountRealname()+"】。该用户信息如下表所示：",
                "user!preview.action?id="+account,
                "反馈问题",
                "#feedback?to="+getAccountName());
			rsp.setSucceed(true);
			logoper(rsp.getMessage(), "用户管理", null);
		}
		catch(Exception e)
		{
			String s = "设置用户【"+account+"】私有权限出现异常"+e;
			logoper(rsp.getMessage(), "用户管理", e);
			rsp.setMessage(s);
			log.error("Failed to set the menu of modules for exception", e);
		}
		return rsp;
	}

	/**
	 * 改变用户所属角色权限组
	 * @param id
	 * @param name
	 * @param timestamp
	 * @return
	 */
	public synchronized AjaxResult<String> dragDropRole(String account, int roleid, long timestamp)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		try
		{
			User user = this.userDao.findByAccount(account);
			if( user == null )
			{
				String s = "改变用户所属角色权限组失败，因为未找到用户【"+account+"】。";
				rsp.setMessage(s);
				rsp.setResult(getRoleUsers(super.getAccountRole()).toString());
				logoper(LogSeverity.ERROR, rsp.getMessage(), "用户管理");
			}
			else
			{
				user.setRoleid(roleid);
				this.userDao.attachDirty(user);
				rsp.setMessage("改变用户【"+account+"】的角色权限组到【"+RoleMgr.getRoleName(roleid)+"】成功。");
				clearCookie(account);
				sendNotiefiesToSystemadmin(
					"用户管理", 
					String.format("权限管理员[%s]"+rsp.getMessage(), getAccountName()),
                    "请检查配置的权限情况，如有问题需协助用户解决。该用户所属角色权限组情况下表所示：",
                    "user!query.action?id="+user.getRoleid(),
                    "查看详情",
                    "user!manager.action");
				this.sendNotiefieToAccount(account,
					"用户管理",  
                    String.format("权限管理员[%s]"+rsp.getMessage(), getAccountName()),
                    "用户所属角色权限组发生改变，请检查您的权限配置情况，如有问题请联系您的权限管理员【"+getAccountRealname()+"】。该用户信息如下表所示：",
                    "user!preview.action?id="+account,
                    "反馈问题",
                    "#feedback?to="+getAccountName());
				rsp.setSucceed(true);
				logoper(rsp.getMessage(), "用户管理", null);
			}
		}
		catch(Exception e)
		{
			String s = "改变用户【"+account+"】的角色权限组到【"+RoleMgr.getRoleName(roleid)+"】出现异常"+e;
			logoper(rsp.getMessage(), "用户管理", e);
			rsp.setMessage(s);
			log.error("Failed to set the menu of modules for exception", e);
		}
		return rsp;
	}
	/**
	 * 新增或修改用户参数
	 * @param id
	 * @param name
	 * @param timestamp
	 * @return
	 */
	public synchronized AjaxResult<String> setUser(String account, String json, long timestamp)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		User user = null;
		try
		{
			boolean newUser = account==null||account.isEmpty();
			if( !newUser && (user = this.userDao.findByAccount(account)) == null ) 
			{
				String s = "修改用户配置属性，因为未找到用户【"+account+"】。";
				rsp.setMessage(s);
				rsp.setResult(getRoleUsers(super.getAccountRole()).toString());
				logoper(LogSeverity.ERROR, rsp.getMessage(), "用户管理");
				return rsp;
			}
			JSONObject theuser = new JSONObject(json);
			if( newUser )
			{
				if( this.userDao.findByAccount(theuser.getString("username")) != null )
				{
					String s = "新增用户设置的账号【"+theuser.getString("username")+"】已经被其他用户所使用，请尝试设置其他账户名称。";
					rsp.setMessage(s);
					rsp.setResult(getRoleUsers(super.getAccountRole()).toString());
					logoper(LogSeverity.ERROR, rsp.getMessage(), "用户管理");
					return rsp;
				}
				user = new User();
				user.setUsername(theuser.getString("username"));
				if( theuser.has("password") )
				{
					user.setPassword(DigestUtils.md5Hex(theuser.getString("password")));
					user.setLastChangePassword(new Date());
				}
				account = user.getUsername();
			}
			else
			{
				if( !account.equals(user.getUsername()) )
				{
					String s = "修改用户设置的账号【"+theuser.getString("username")+"】与原来的账号不一致，可能发生系统错误。";
					rsp.setMessage(s);
					rsp.setResult(getRoleUsers(super.getAccountRole()).toString());
					logoper(LogSeverity.ERROR, rsp.getMessage(), "用户管理");
					return rsp;
				}
			}
			account = user.getUsername();
			int roleid = theuser.getInt("roleid");
			user.setRealname(theuser.getString("realname"));
			user.setRoleid(roleid);
			user.setEmail(theuser.getString("email"));
			user.setSex((byte)theuser.getInt("sex"));
			if( newUser ){
				Sysuser sysuser = new Sysuser();
				sysuser.setUsername(account);
				sysuser.setRoleid(user.getRole());
				sysuser.setId(0);
				sysuser.setEmail(user.getEmail());
				sysuser.setPassword(theuser.getString("password"));
				sysuser.setRealname(user.getRealname());
				sysuser.setSex(user.getSex());
				sysuser.setCreator(super.getAccountName());
				sysuser.setStatus(user.getStatus());
				SysuserClient.add(sysuser);
				log.info("Succeed to add user "+sysuser.getId());
				if( sysuser.getId() > 0 ){
					user = this.userDao.findByAccount(account);
					user.setStatus(Status.Enable.getValue());
					this.userDao.attachDirty(user);
				}
			}
			else{
				if( theuser.has("creator") ){
					user.setCreator(theuser.getString("creator"));
				}
				if( user.getCreator() == null ){
					user.setCreator(super.getAccountName());
				}
				this.userDao.update(user);
			}
			ZKMgr.getZookeeper().setJSONObject("/cos/user/properties/"+account, theuser, true);
			if( newUser ) rsp.setMessage("新增用户【"+account+"】成功。");
			else rsp.setMessage("修改用户【"+account+"】属性成功。");
			sendNotiefiesToSystemadmin(
				"用户管理", 
				String.format("权限管理员[%s]"+rsp.getMessage(), getAccountName()),
                "请检查配置的用户情况，如有问题需协助用户解决。该用户所属角色权限组情况下表所示：",
                "user!query.action?id="+user.getRoleid(),
                "查看详情",
                "user!manager.action");
			this.sendNotiefieToAccount(account,
				"用户管理",  
                String.format("权限管理员[%s]"+rsp.getMessage(), getAccountName()),
                "用户属性发生变化，请检查您的配置情况，如有问题请联系您的权限管理员【"+getAccountRealname()+"】。该用户信息如下表所示：",
                "user!preview.action?id="+account,
                "反馈问题",
                "#feedback?to="+getAccountName());
			int roleAccount = super.getAccountRole();
			if( roleAccount != -1 ){
				if( super.getAccountRole() == 1 ){
					rsp.setResult(getRoleUsers(super.getAccountRole()).toString());
				}
				else{
					rsp.setResult(getRoleUsers(super.getAccountRole(), null, super.getAccountName()).toString());
				}
			}
			else{
				log.warn("Set user by peer.");
			}
			rsp.setSucceed(true);
			logoper(rsp.getMessage(), "用户管理", null);
		}
		catch(Exception e)
		{
			String s = "设置用户出现异常: "+e.getMessage();
			logoper(rsp.getMessage(), "用户管理", e);
			rsp.setMessage(s);
			log.error("Failed to set the user of "+account+" for exception", e);
		}
		return rsp;
	}
	
	/**
	 * 
	 * @param account
	 * @param creator
	 */
	public synchronized AjaxResult<String> changeCreator(String account, String creator){
		AjaxResult<String> rsp = new AjaxResult<String>();
		try
		{
			User user = this.userDao.findByAccount(account);
			if( user == null )
			{
				String s = "改变用户归属账号失败，因为未找到用户【"+account+"】。";
				rsp.setMessage(s);
				rsp.setResult(getRoleUsers(super.getAccountRole()).toString());
				logoper(LogSeverity.ERROR, rsp.getMessage(), "用户管理");
			}
			else
			{
				rsp.setMessage(String.format("设置用户【%s】的上级账户从[%s]变为[%s]成功", account, user.getCreator(), creator));
				user.setCreator(creator);
				this.userDao.attachDirty(user);
				sendNotiefiesToSystemadmin(
					"用户管理", 
					String.format("权限管理员[%s] %s", getAccountName(), rsp.getMessage()),
                    "请检查配置的权限情况，如有问题需协助用户解决。该用户所属角色权限组情况下表所示：",
                    "user!query.action?id="+user.getRoleid(),
                    "查看详情",
                    "user!manager.action");
				this.sendNotiefieToAccount(account,
					"用户管理",  
					String.format("权限管理员[%s] %s", getAccountName(), rsp.getMessage()),
                    "登录请检查后权限情况，如有问题请联系您的权限管理员【"+getAccountRealname()+"】。该用户信息如下表所示：",
                    "user!preview.action?id="+account,
                    "反馈问题",
                    "#feedback?to="+getAccountName());
				rsp.setSucceed(true);
				logoper(rsp.getMessage(), "用户管理", null);
			}
		}
		catch(Exception e)
		{
			String s = String.format("设置用户【%s】的上级账户变为[%s]出现异常%s", account, creator, e.getMessage());
			logoper(rsp.getMessage(), "用户管理", e);
			rsp.setMessage(s);
			log.error("Failed to change the creator of user("+account+") to "+creator, e);
		}
		return rsp;
	}

	/**
	 * 启用禁用用户
	 * @param id
	 * @param name
	 * @param timestamp
	 * @return
	 */
	public synchronized AjaxResult<String> setUserState(String account, boolean abort, long timestamp)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		try
		{
			User user = this.userDao.findByAccount(account);
			if( user == null )
			{
				String s = "设置用户状态失败，因为未找到用户【"+account+"】。";
				rsp.setMessage(s);
				rsp.setResult(getRoleUsers(super.getAccountRole()).toString());
				logoper(LogSeverity.ERROR, rsp.getMessage(), "用户管理");
			}
			else
			{
				if( abort ) rsp.setMessage("设置用户【"+account+"】禁止登录系统成功。");
				else rsp.setMessage("授权用户【"+account+"】可登录系统成功。");
				user.setStatus(abort?Status.Disable.getValue():Status.Enable.getValue());
				this.userDao.attachDirty(user);
				clearCookie(account);
				sendNotiefiesToSystemadmin(
					"用户管理", 
					String.format("权限管理员[%s]"+rsp.getMessage(), getAccountName()),
                    "请检查配置的权限情况，如有问题需协助用户解决。该用户所属角色权限组情况下表所示：",
                    "user!query.action?id="+user.getRoleid(),
                    "查看详情",
                    "user!manager.action");
				this.sendNotiefieToAccount(account,
					"用户管理",  
                    String.format("权限管理员[%s]"+rsp.getMessage(), getAccountName()),
                    "登录请检查后权限情况，如有问题请联系您的权限管理员【"+getAccountRealname()+"】。该用户信息如下表所示：",
                    "user!preview.action?id="+account,
                    "反馈问题",
                    "#feedback?to="+getAccountName());
				rsp.setSucceed(true);
				logoper(rsp.getMessage(), "用户管理", null);
			}
		}
		catch(Exception e)
		{
			String s = "启用禁用用户【"+account+"】出现异常"+e;
			logoper(rsp.getMessage(), "用户管理", e);
			rsp.setMessage(s);
			log.error("Failed to set the status of user("+account+") to "+abort, e);
		}
		return rsp;
	}
	/**
	 * 重置用户密码
	 * @param id
	 * @param name
	 * @param timestamp
	 * @return
	 */
	public synchronized AjaxResult<String> resetPassword(String account, String password, long timestamp)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		try
		{
			User user = this.userDao.findByAccount(account);
			if( user == null )
			{
				String s = "重置用户密码失败，因为未找到用户【"+account+"】。";
				rsp.setMessage(s);
				rsp.setResult(getRoleUsers(super.getAccountRole()).toString());
				logoper(LogSeverity.ERROR, rsp.getMessage(), "用户管理");
			}
			else
			{
				user.setPassword(DigestUtils.md5Hex(password));
				user.setLastChangePassword(new Date());
				this.userDao.attachDirty(user);
				rsp.setMessage("重置用户【"+account+"】密码成功。");
				clearCookie(account);
				sendNotiefiesToSystemadmin(
					"用户管理", 
					String.format("权限管理员[%s]"+rsp.getMessage(), getAccountName()),
                    "请检查配置的权限情况，如有问题需协助用户解决。该用户所属角色权限组情况下表所示：",
                    "user!query.action?id="+user.getRoleid(),
                    "查看详情",
                    "user!manager.action");
				this.sendNotiefieToAccount(account,
					"用户管理",  
                    String.format("权限管理员[%s]"+rsp.getMessage(), getAccountName()),
                    "密码被权限管理员重置，如有问题请联系您的权限管理员【"+getAccountRealname()+"】。该用户信息如下表所示：",
                    "user!preview.action?id="+account,
                    "反馈问题",
                    "#feedback?to="+getAccountName());
				rsp.setSucceed(true);
				logoper(rsp.getMessage(), "用户管理", null);
			}
		}
		catch(Exception e)
		{
			String s = "重置用户【"+account+"】密码出现异常"+e;
			logoper(rsp.getMessage(), "用户管理", e);
			rsp.setMessage(s);
			log.error("Failed to set the menu of modules for exception", e);
		}
		return rsp;
	}
	
	/**
	 * 得到我的所有用户
	 * @param mine
	 * @return
	 */
	public String getMyChildren(String mine){
		List<?> users = getUserDao().findAllUsers();
		HashMap<String, String> creators = new HashMap<String, String>();
		creators.put(mine, null);
		this.setMyChildren(users, creators);
		StringBuilder sb = new StringBuilder();
		Iterator<String> iterator = creators.keySet().iterator();
		while(iterator.hasNext()){
			if(sb.length() > 0 ) sb.append(",");
			sb.append(iterator.next());
		}
		return sb.toString();
	}
	
	/**
	 * 设置所有的子账户
	 * @param users
	 * @param creators
	 */
	private void setMyChildren(List<?> users, HashMap<String, String> creators){
		for(int i = 0; i < users.size(); i++)
		{
			User user = (User)users.get(i);
			if( creators.containsKey(user.getCreator()) ){
				creators.put(user.getUsername(), null);
			}
		}
	}
	/**
	 * 执行用户记录的加载
	 * @param role
	 * @param users
	 * @param filters
	 * @param creator
	 * @return
	 */
	public int setUsers(JSONObject role, List<?> users, List<User> removed, JSONObject filters, HashMap<String, String> creators)
	{
		role.put("isParent", true);
		role.put("drag", false);
		role.put("ico", "fa-group");
		role.put("rname", role.getString("name"));
		role.put("nocheck", true);
		role.put("iconClose", "images/icons/folder_closed.png");
		role.put("iconOpen",  "images/icons/folder_opened.png");
		JSONArray children1 = new JSONArray();
		int usercount = 0;
		for(int i = 0; i < users.size(); i++)
		{
			User user = (User)users.get(i);
			if( user.getStatus() == Status.Disable.getValue() ){
				if( !user.getUsername().equals("admin") ){
					removed.add((User)users.remove(i--));
					continue;
				}
			}
//			if( user.getUsername().equals("admin") ) continue;
			if( user.getRoleid() == role.getInt("id") )
			{
				if( filters != null ){
					if( !filters.has(user.getUsername()) ) continue;
				}
				if( creators != null && user.getCreator() != null && (!creators.containsKey(user.getCreator()) && !creators.containsKey(user.getUsername()) ) ){
					//如果配置了上级权限用户，用户只能够查询自己
					continue;
				}
				usercount += 1;
				if( creators != null ) creators.put(user.getUsername(), null);
				JSONObject account = new JSONObject();
				account.put("id", user.getUsername());
				account.put("rname", user.getRealname());
				account.put("roleid", user.getRoleid());
				account.put("email", user.getEmail());
				account.put("name", user.getRealname()+"("+user.getUsername()+")");
				account.put("icon", user.getSex()==1?"images/icons/boy.png":"images/icons/girl.png");
				account.put("abort", user.getStatus());
				account.put("ico", user.getStatus()==Status.Enable.getValue()?"":"fa-minus-circle");
				account.put("dropInner", false);
				children1.put(account);
				users.remove(i);
				i -= 1;
			}
		}
		if( role.has("children") && !users.isEmpty() )
		{
			JSONArray children = role.getJSONArray("children");
			for(int i = 0; i < children.length(); i++)
			{
				children1.put(children.getJSONObject(i));
			}
			for(int i = 0; i < children.length(); i++)
			{
				usercount += setUsers(children.getJSONObject(i), users, removed, filters, creators);
			}
		}
		if( usercount > 0 )
		{
			role.put("iconClose", "images/icons/folder_closed.png");
			role.put("iconOpen",  "images/icons/folder_opened.png");
			role.put("children", children1);
			role.put("usercount", usercount);
			role.put("name", role.getString("name")+"("+usercount+"人)");
		}
		return usercount;
	}
	/**
	 * 根据用户名判断密码是否正确
	 * 
	 * @param userName
	 * @param pwd
	 * @return boolean
	 * @since 1.0
	 */
	public boolean validPwd(String userName, String pwd)
	{
		User user = null;
		try {
			user = userDao.findByAccount(userName);
		} catch (Exception e) {
			log.error("Failed to find account "+userName, e);
			return false;
		}
		if (user != null)
		{
			return user.getPassword().equals(DigestUtils.md5Hex(pwd));
		}
		else
		{
			return false;
		}
	}

	/**
	 * 修改密码
	 * 
	 * @param userId
	 * @param pwd
	 * @return boolean
	 * @since 1.0
	 */
	public boolean modifyPwd(Integer userId, String pwd)
	{
		boolean bResult = false;
		try
		{
			bResult = userDao.modifyPwd(userId, DigestUtils.md5Hex(pwd));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return bResult;
	}

	/**
	 * 修改密码
	 * 
	 * @param userId
	 * @param pwd
	 * @return boolean
	 * @since 1.0
	 */
	public boolean disableRole(int roleid)
	{
		boolean bResult = false;
		try
		{
			bResult = userDao.disableRole(roleid);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return bResult;
	}
	/**
	 * 查询用户列表
	 * 
	 * @param pageBean
	 * @param keyword
	 * @return
	 */
	public List<?> queryUserList(PageBean pageBean, String keyword,String other) throws Exception
	{
		return userDao.queryUserList(pageBean, keyword,other);
	}

	/**
	 * 查询
	 * @param roleid
	 * @param status
	 * @param sex
	 * @return
	 * @throws Exception
	 */
	public ArrayList<User> listUser(int roleid, int status, int sex) throws Exception
	{
		return userDao.listUser(roleid, status, sex);
	}

	/**
	 * 用户名是否存在
	 * 
	 * @param userName
	 * @return boolean
	 */
	public boolean validUserName(String userName)
	{
		try {
			return userDao.findByAccount(userName) != null;
		} catch (Exception e) {
			log.error("Failed to find account "+userName, e);
		}
		return false;
	}

	/**
	 * 添加新用户，如果存在就更新修改
	 * 
	 * @param user
	 */
	public boolean addUser(User user)
	{
		try {
			if(userDao.findByAccount(user.getUsername()) != null)
			{
				return false;
			}
			else
			{
				user.setCreator(super.getAccountName());//设置用户的创建者
				user.setPassword(DigestUtils.md5Hex(user.getPassword()));
				this.userDao.save(user);
				return true;
			}
		} catch (Exception e) {
			log.error("Failed to find account "+user.getUsername(), e);
		}
		return false;
	}

	/**
	 * 删除用户
	 * 
	 * @param ids
	 */
	public void deleteByIds(String ids)
	{
		userDao.deleteByIds(ids);
	}

	/**
	 * 禁用用户
	 * 
	 * @param userName
	 */
	public boolean disable(String userName)
	{
		return userDao.disable(userName, Status.Disable.getValue());
	}

	
	/**
	 * 查询所有的部门
	 * @return
	 */
	public List<String> queryAllDepts(){
		return userDao.queryAllDepts();
	}
	
	/**
	 * 查询所有的地区
	 * @return
	 */
	public List<String> queryAllAddresses(){
		return userDao.queryAllAddresses();
	}
	
	public static void buildUser()
	{
		StringBuffer sb = new StringBuffer();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;
		try
		{
			Zookeeper zookeeper = ZKMgr.getZookeeper();
			String zkpath = "/cos/config/userpropcfg";
			Stat stat = zookeeper.exists(zkpath, false); 
			if( stat == null )
			{
				zookeeper.setString("/cos/config/userpropcfg", "用户扩展属性配置");
	        	JSONArray configs = new JSONArray();
	        	JSONObject cfg = null;
	        	cfg = new JSONObject();
	        	cfg.put("id", "mobile");
	        	cfg.put("name", "手机");
	        	cfg.put("sort", 1);
	        	cfg.put("use", true);
	        	cfg.put("nullable", true);
	        	configs.put(cfg);
	        	zookeeper.setJSONObject("/cos/config/userpropcfg/"+cfg.getString("id"), cfg);
	        	cfg = new JSONObject();
	        	cfg.put("id", "corp");
	        	cfg.put("name", "公司");
	        	cfg.put("sort", 2);
	        	cfg.put("use", true);
	        	cfg.put("nullable", true);
	        	configs.put(cfg);
	        	zookeeper.setJSONObject("/cos/config/userpropcfg/"+cfg.getString("id"), cfg);
	        	cfg = new JSONObject();
	        	cfg.put("id", "dept");
	        	cfg.put("name", "部门");
	        	cfg.put("sort", 3);
	        	cfg.put("use", true);
	        	cfg.put("nullable", true);
	        	configs.put(cfg);
	        	zookeeper.setJSONObject("/cos/config/userpropcfg/"+cfg.getString("id"), cfg);
	        	cfg = new JSONObject();
	        	cfg.put("id", "duty");
	        	cfg.put("name", "职务");
	        	cfg.put("sort", 4);
	        	cfg.put("use", true);
	        	cfg.put("nullable", true);
	        	configs.put(cfg);
	        	zookeeper.setJSONObject("/cos/config/userpropcfg/"+cfg.getString("id"), cfg);
	        	cfg = new JSONObject();
	        	cfg.put("id", "phone");
	        	cfg.put("name", "电话");
	        	cfg.put("sort", 5);
	        	cfg.put("use", true);
	        	cfg.put("nullable", true);
	        	configs.put(cfg);
	        	zookeeper.setJSONObject("/cos/config/userpropcfg/"+cfg.getString("id"), cfg);
	        	cfg = new JSONObject();
	        	cfg.put("id", "fax");
	        	cfg.put("name", "传真");
	        	cfg.put("sort", 6);
	        	cfg.put("use", true);
	        	cfg.put("nullable", true);
	        	configs.put(cfg);
	        	zookeeper.setJSONObject("/cos/config/userpropcfg/"+cfg.getString("id"), cfg);
	        	cfg = new JSONObject();
	        	cfg.put("id", "post");
	        	cfg.put("name", "邮编");
	        	cfg.put("sort", 7);
	        	cfg.put("use", true);
	        	cfg.put("nullable", true);
	        	configs.put(cfg);
	        	zookeeper.setJSONObject("/cos/config/userpropcfg/"+cfg.getString("id"), cfg);
	        	cfg = new JSONObject();
	        	cfg.put("id", "address");
	        	cfg.put("name", "地址");
	        	cfg.put("sort", 8);
	        	cfg.put("use", true);
	        	cfg.put("nullable", true);
	        	configs.put(cfg);
	        	zookeeper.setJSONObject("/cos/config/userpropcfg/"+cfg.getString("id"), cfg);
	        	cfg = new JSONObject();
	        	cfg.put("id", "remark");
	        	cfg.put("name", "备注");
	        	cfg.put("sort", 10000);
	        	cfg.put("use", true);
	        	cfg.put("nullable", true);
	        	configs.put(cfg);
	        	zookeeper.setJSONObject("/cos/config/userpropcfg/"+cfg.getString("id"), cfg);
			}
			zkpath = "/cos/user";
			stat = zookeeper.exists(zkpath, false); 
			if( stat == null)
			{
				zookeeper.create(zkpath, "用户管理相关数据".getBytes("UTF-8"));
			}

			zkpath += "/properties";
			stat = zookeeper.exists(zkpath, false); 
			if( stat != null)
			{
				return;
			}
			zookeeper.create(zkpath, "用户扩展属性存储表".getBytes("UTF-8"));
			
			Properties jdbcConfig = new Properties();
	    	File webapppath = new File(PathFactory.getWebappPath().getParentFile(), "cos");
	    	jdbcConfig.load(new FileInputStream(new File(webapppath, "WEB-INF/classes/config/jdbc.properties")));
	    	String jdbcUrl = jdbcConfig.getProperty("jdbc.url");
	    	String jdbcUsername = jdbcConfig.getProperty("jdbc.username");
	    	String jdbcUserpswd = jdbcConfig.getProperty("jdbc.password");
	    	String driverClass = jdbcConfig.getProperty("jdbc.driverClass");
    		if( jdbcUserpswd == null ) jdbcUserpswd = "";
    		if( jdbcUrl == null || jdbcUrl.isEmpty() || jdbcUsername == null || jdbcUsername.isEmpty() || driverClass == "" || driverClass.isEmpty() )
			{
    			log.warn("Not found jdbc from properties("+jdbcUrl+") ");
    			return; 
			}
			
            Class.forName(driverClass); 
            connection = DriverManager.getConnection(jdbcUrl ,jdbcUsername, jdbcUserpswd);
            statement = connection.createStatement();
			rs = connection.getMetaData().getColumns( null, "%", "TB_USER", "%");
			ArrayList<String> columns = new ArrayList<String>();
			HashMap<String, Boolean> map = new HashMap<String, Boolean>();
			map.put("corp", true);
			map.put("mobile", true);
			map.put("dept", true);
			map.put("duty", true);
			map.put("phone", true);
			map.put("fax", true);
			map.put("post", true);
			map.put("address", true);
			map.put("memo", true);
			while(rs.next())
			{
				String c = rs.getString("COLUMN_NAME");
				c = c.toLowerCase();
				if( map.containsKey(c) && map.remove(c) )
				{
					columns.add(c);
				}
			}
			rs.close();
			if( columns.isEmpty() )
			{
				return;
			}
            rs = statement.executeQuery("select * from TB_USER");
            while( rs.next() )
            {
            	JSONObject user = new JSONObject();
            	for(String c : columns )
            	{
            		String d = rs.getString(c);
            		user.put(c, d != null ? d: "");
            	}
            	zookeeper.setJSONObject("/cos/user/properties/"+rs.getString("username"), user, true);
            	sb.append("\r\n\t"+rs.getString("username")+"\t"+user.toString());
            }
			log.info("Succed to build the data of userprop to zookeeper:"+sb.toString());
		}
		catch(Exception e )
		{
			log.error("Failed to build the data of userprop to zookeeper for exception "+e+"\r\n"+sb);
		}
        finally
        {
        	if( rs != null )
				try
				{
					rs.close();
				}
				catch (SQLException e)
				{
				}
        	if( statement != null )
				try
				{
					statement.close();
				}
				catch (SQLException e)
				{
				}
        	if( connection != null )
				try
				{
					connection.close();
				}
				catch (SQLException e)
				{
				}
        }
	}

	/**
	 * 清除cookie
	 * @param role
	 */
	private void clearCookie(String account)
	{
		try
		{
			ZooKeeper zookeeper = ZKMgr.getZooKeeper();
			String path = "/cos/login/cookie";
			Stat stat = zookeeper.exists(path, false); 
			if( stat == null)
			{
				return;
			}
			List<String> list = zookeeper.getChildren(path, false);
			for( String cookie : list )
			{
				cookie = path+"/"+cookie;
				stat = zookeeper.exists(cookie, false); 
				if( stat == null ) continue;
				String json = new String(zookeeper.getData(cookie, false, stat), "UTF-8");
				JSONObject c = new JSONObject(json);
				if( !c.has("username") || !account.equals(c.getString("username")) )
					continue;
				zookeeper.delete(cookie, stat.getVersion());
				if( c.has("token") )
				{
					String token = c.getString("token");
					token = "/cos/login/token/"+token;
					stat = zookeeper.exists(token, false); 
					if( stat != null )
					{
						zookeeper.delete(token, stat.getVersion());
					}
				}
				break;
			}
		}
		catch(Exception e)
		{
		}
	}
	

	/**
	 * 递归设置用户的禁用权限
	 * @param modules
	 * @param role
	 * @param roleParent
	 */
	public void setUserPrivileges(JSONArray modules, JSONObject privileges)
	{
		for(int i = 0; i < modules.length(); i++)
		{
			JSONObject menu = modules.getJSONObject(i);
			if( "manager.cluster".equals(menu.getString("id")) )
			{
//				System.err.println(menu.toString());
			}
			if( (!menu.has("checked") || !menu.getBoolean("checked")) && !menu.has("nocheck") && !"toolbar".equals(menu.getString("id")) )
			{
				modules.remove(i);
				i -= 1;
				continue;
			}
			if( privileges != null && menu.has("id") && privileges.has(menu.getString("id")) )
			{
				menu.put("checked", false);
			}
			if( menu.has("children") )
			{
				JSONArray array = menu.getJSONArray("children");
				setUserPrivileges(array, privileges);
				if( array.length() == 0 )
				{
					menu.remove("children");
					if( menu.has("nocheck") && menu.getBoolean("nocheck") )
					{
						modules.remove(i);
						i -= 1;
						continue;
					}
					else if( menu.getString("id").indexOf("#") == -1)
					{
						menu.remove("isParent");
						menu.put("icon", "images/icons/bookmarks.png");
					}
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
				setUserPrivileges(buttons, privileges);
			}
		}
	}
	
	public User findById(Integer userId)
	{
		return userDao.findById(userId);
	}

	public User findByUserName(String userName)
	{
		try {
			return userDao.findByAccount(userName);
		} catch (Exception e) {
			log.error("Failed to find account "+userName, e);
			return null;
		}
	}

	public String getPageMenu()
	{
		PageBean p = userDao.getPageBean();
		return userDao.getPaginate().getPageMenu(p);
	}

	public UserDAO getUserDao() {
		return userDao;
	}
	
	public void setUserDao(UserDAO userDao)
	{
		this.userDao = userDao;
	}
}
