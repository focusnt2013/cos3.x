package com.focus.cos.web.user.action;

import java.util.Date;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import com.focus.cos.web.action.GridAction;
import com.focus.cos.web.common.ZKMgr;
import com.focus.cos.web.config.service.SysCfgMgr;
import com.focus.cos.web.dev.service.MenusMgr;
import com.focus.cos.web.service.SvrMgr;
import com.focus.cos.web.user.service.RoleMgr;
import com.focus.cos.web.user.service.UserMgr;
import com.focus.cos.web.user.vo.User;
import com.focus.util.QuickSort;
import com.focus.util.Tools;
import com.opensymphony.xwork.ModelDriven;

/**
 * 用户管理
 * @author think
 *
 */
public class UserAction extends GridAction implements ModelDriven
{
	private static final long serialVersionUID = -1913414185590474164L;
	private static final Log log = LogFactory.getLog(UserAction.class);
	//复用管理器
	private RoleMgr roleMgr;
	//用户管理器
	private UserMgr userMgr;
	//输出用户对象
	private User theuser;
	//创建者
	private User creator;
	//父节点ID
	private int pid;
	//登录机会
	private int chance;
	
	public int getChance() {
		return chance;
	}

	public void setChance(int chance) {
		this.chance = chance;
	}

	/**
	 * 管理用户
	 * @return
	 */
	public String manager()
	{
		try
		{
			if( isSysadmin() ){
				this.jsonData = userMgr.getRoleUsers(super.getUserRole()).toString();
			}
			else{
				this.jsonData = userMgr.getRoleUsers(super.getUserRole(), null, super.getUserAccount()).toString();
			}
		}
		catch(Exception e)
		{
			log.error("Failed to open the data of user for ", e);
			super.setResponseException("打开用户权限管理界面失败，因为"+e);
		}
		return "manager";
	}
	
	/**
	 * 设置用户
	 * @return
	 */
	public String preset()
	{
		try
		{
			String myaccont = super.getUserAccount();
			JSONObject role = ZKMgr.getZookeeper().getJSONObject("/cos/config/role"); 
			if( role == null)
			{
				super.setResponseException("读取角色权限组配置数据，请先进行角色权限组管理。");
				return "close";
			}
			if( pid == 0 )
			{
				theuser = this.userMgr.getUserDao().findByAccount(id);
				if( theuser == null )
				{
					super.setResponseException("读取用户数据失败。");
					return "alert";
				}
				this.account = theuser.getUsername();
				this.pid = theuser.getRoleid();
				this.creator = this.userMgr.getUserDao().findByAccount(theuser.getCreator());
				this.localDataObject = new JSONObject();
				if( this.isSysadmin() ){
					localDataObject = this.userMgr.getCreatorUsers(theuser);
				}
				this.rowSelect = localDataObject.toString();
//				System.out.println(localDataObject.toString(4));
			}
			else
			{
				theuser = new User();
				theuser.setRoleid(pid);
				String username = super.getRequest().getParameter("username");
				if( username != null )
				{
					theuser.setUsername(username);
				}
				String realname = super.getRequest().getParameter("realname");
				if( realname != null )
				{
					theuser.setRealname(realname);
				}
				String email = super.getRequest().getParameter("email");
				if( email != null )
				{
					theuser.setEmail(email);
				}
				account = null;
			}
			theuser.setRolename(RoleMgr.getRoleName(theuser.getRoleid()));
			JSONArray roles = new JSONArray();
			role = RoleMgr.setRolesTree(RoleMgr.getMyRole(role, getUserRole()), pid);
			if( role != null ){
				roles.put(role);
				if( !isSysadmin() ){
					role.put("checked", false);
					role.put("chkDisabled", true);
				}
			}
			this.jsonData = roles.toString();
			super.listData = ZKMgr.getZookeeper().getJSONObjects("/cos/config/userpropcfg");
			QuickSort sorter = new QuickSort() {
				public boolean compareTo(Object sortSrc, Object pivot) {
					JSONObject l = (JSONObject)sortSrc;
					JSONObject r = (JSONObject)pivot;
					if( !l.has("sort") || !	r.has("sort") ) return false;
					return l.getInt("sort")<r.getInt("sort");
				}
			};
			sorter.sort(listData);
			JSONObject parentprop = ZKMgr.getZookeeper().getJSONObject("/cos/user/properties/"+myaccont, true);
			JSONObject userprop = ZKMgr.getZookeeper().getJSONObject("/cos/user/properties/"+this.id, true);
			if( userprop != null ) {
				for(int i = 0; i < listData.size(); i++)
				{
					JSONObject e = (JSONObject)listData.get(i);
					String id = e.getString("id");
					if( userprop.has(id) )
					{
						e.put("value", userprop.getString(id));
						boolean inherit = userprop.has(id+"_inherit")?userprop.getBoolean(id+"_inherit"):false;
						if( !inherit && parentprop != null ){
							inherit = parentprop.has(id+"_inherit")?parentprop.getBoolean(id+"_inherit"):false;
							if(inherit){
								e.put("value", parentprop.getString(id));
								userprop.put(id+"_inherit", true);
								userprop.put(id, userprop.getString(id));
								ZKMgr.getZookeeper().setJSONObject("/cos/user/properties/"+this.id, userprop, true);
							}
						}
						e.put("inherit", inherit?"checked='true'":"");
					}
				}
			}
			else {
				String _inherit = super.getRequest().getParameter("inherit");
				for(int i = 0; i < listData.size(); i++)
				{
					JSONObject e = (JSONObject)listData.get(i);
					String id = e.getString("id");
					String value = super.getRequest().getParameter(id);
					if( value != null )
					{
						e.put("value", value);
					}
					if( _inherit != null ){
						if( _inherit.indexOf(id) != -1 ){
							e.put("inherit", "checked='true'");
						}
					}
					if( parentprop != null && parentprop.has(id) )
					{
						boolean inherit = parentprop.has(id+"_inherit")?parentprop.getBoolean(id+"_inherit"):false;
						if(inherit){
							e.put("value", parentprop.getString(id));
							e.put("inherit", "checked='true'");
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			super.setResponseException("打开用户权限配置界面失败，因为异常: "+e.toString());
		}
		return "preset";
	}

	/**
	 * 设置用户
	 * @return
	 */
	public String preview()
	{
		try
		{
			theuser = this.userMgr.getUserDao().findByAccount(id);
			if( theuser != null )
			{
				theuser.setRolename(RoleMgr.getRoleName(theuser.getRoleid()));
			}
			super.listData = ZKMgr.getZookeeper().getJSONObjects("/cos/config/userpropcfg");
			QuickSort sorter = new QuickSort() {
				public boolean compareTo(Object sortSrc, Object pivot) {
					JSONObject l = (JSONObject)sortSrc;
					JSONObject r = (JSONObject)pivot;
					if( !l.has("sort") || !	r.has("sort") ) return false;
					return l.getInt("sort")<r.getInt("sort");
				}
			};
			sorter.sort(listData);
			JSONObject userprop = ZKMgr.getZookeeper().getJSONObject("/cos/user/properties/"+id, true);
			if( userprop != null )
				for(int i = 0; i < listData.size(); i++)
				{
					JSONObject e = (JSONObject)listData.get(i);
					if( userprop.has(e.getString("id")) )
					{
						e.put("value", userprop.getString(e.getString("id")));
					}
				}
		}
		catch(Exception e)
		{
			super.setResponseException("打开用户权限配置界面失败，因为异常"+e);
		}
		return "preview";
	}

	//新密码
	private String oldpassword;
	//新密码
	private String newpassword;
	//
	private String authPasswordRegexp;
	//
	private boolean authUnincludeUsername;
	//
	private int authPasswordLength;

	/**
	 * 修改密码页面
	 * @return
	 */
	public String password()
	{
		try
		{
			theuser = this.userMgr.getUserDao().findByAccount(super.getUserAccount());
			if( theuser == null )
			{
				super.setResponseException("未知用户");
				return "close";
			}
			JSONObject sysConfig = SysCfgMgr.getConfig();
			String str = sysConfig.has("AuthPasswordRegexp")?sysConfig.getString("AuthPasswordRegexp"):"";
			if( "true".equalsIgnoreCase(str) )
			{
				authPasswordRegexp = "1";
			}
			str = sysConfig.has("AuthUnincludeUsername")?sysConfig.getString("AuthUnincludeUsername"):"";
				authUnincludeUsername = "true".equalsIgnoreCase(str);
			str = sysConfig.has("AuthPasswordLength")?sysConfig.getString("AuthPasswordLength"):"";
			if( Tools.isNumeric(str) )
			{
				authPasswordLength = Integer.parseInt(str);
			}
			else
			{
				authPasswordLength = 6;
			}
			return "password";
		}
		catch(Exception e)
		{
			super.setResponseException("打开密码页面出错，因为异常"+e);
			return "close";
		}
	}
	
	/**
	 * 修改密码
	 * @return
	 */
	public String changepassword()
	{
		try
		{
			theuser = this.userMgr.getUserDao().findByAccount(super.getUserAccount());
			if( theuser == null )
			{
				super.setResponseException("未知用户");
			}
			else
			{
				oldpassword = DigestUtils.md5Hex(oldpassword);
				if( !theuser.getPassword().equals(oldpassword) )
				{
					super.setResponseException("您输入的旧密码验证不通过。");
					return "password";
				}
				theuser.setPassword(DigestUtils.md5Hex(newpassword));
				theuser.setLastChangePassword(new Date());
				this.userMgr.getUserDao().attachDirty(theuser);
				if( theuser.getId() != 1 )
				{
					SvrMgr.sendNotiefiesToSystemadmin(
						super.getRequest(),
						"用户管理", 
						"用户【"+theuser.getRealname()+"】修改了自己的密码。",
	                    "请检查该用户配置的权限情况，如有问题需协助用户解决。该用户所属角色权限组情况下表所示：",
	                    "user!preview.action?id="+theuser.getUsername(),
	                    "查看详情",
	                    "user!manager.action");
				}
				this.userMgr.sendNotiefieToAccount(
					super.getRequest(),
					theuser.getUsername(),
					"用户管理",  
					"我修改了自己的密码",
                    "密码被自己重置，如有问题请联系您的权限管理员。该用户信息如下表所示：",
                    "user!preview.action?id="+theuser.getUsername(),
                    "反馈问题",
                    "#feedback");
				logsecurity("用户【"+theuser.getRealname()+"】修改了自己的密码。", "用户管理", null, null);
				super.setResponseMessage("修改密码成功");
			}
			return "close";
		}
		catch(Exception e)
		{
			super.setResponseException("打开密码页面出错，因为异常"+e);
			log.error("", e);
			return "password";
		}
	}
	
	/**
	 * 用户私有权限
	 * @return
	 */
	public String privileges()
	{
		try 
		{
			JSONArray modules = new JSONArray();
			MenusMgr.loadModules(modules);
			theuser = this.userMgr.getUserDao().findByAccount(id);
			if( theuser != null )
			{
				theuser.setRolename(RoleMgr.getRoleName(theuser.getRoleid()));
			}
			JSONObject role = RoleMgr.getRolePrivileges(theuser.getRoleid());
//			log.info("Load th role("+(role!=null)+") from "+theuser.getRoleid());
			JSONObject roleParent = null;
			if( role != null && role.has("parent") && theuser.getRoleid() != role.getInt("parent") && role.getInt("parent") != 1 )
			{
				roleParent = RoleMgr.getRolePrivileges(role.getInt("parent"));
//				log.info("Load th role("+(roleParent!=null)+") of parent from "+role.getInt("parent"));
			}
			if( roleParent != null && roleParent.length() == 0 ){
//				log.info("Not found the data from parent.");
				roleParent = null;
			}
//			log.info("Set the role of privileges from modules "+modules.length());
			roleMgr.setMenusPrivileges(modules, role, roleParent, isSysadmin());
			JSONObject serverPrivileges = null;
			JSONObject parentServerPrivileges = null;
			if( role != null )
			{
				serverPrivileges = role.has("##cluster")?role.getJSONObject("##cluster"):new JSONObject();
			}
			if( roleParent != null )
			{
				parentServerPrivileges = roleParent.has("##cluster")?roleParent.getJSONObject("##cluster"):new JSONObject();
			}
			else
			{
				parentServerPrivileges = serverPrivileges;
			}
			JSONArray clusters = roleMgr.getClusterPrivileges(serverPrivileges, parentServerPrivileges, theuser.getRoleid()==1);
			if( clusters.length() > 0 )
			{
				JSONObject node = new JSONObject();
				node.put("name", "集群管控");
				node.put("id", "manager.cluster");
				node.put("checked", true);
				node.put("open", true);
				node.put("nocheck", true);
				node.put("children", clusters);
				modules.put(node);
			}
			JSONObject userPrivileges = ZKMgr.getZookeeper().getJSONObject("/cos/user/privileges/"+id);
//			log.info("Succeed to set the privileges("+id+", role is "+theuser.getRoleid()+") form role\r\n"+modules.toString(4));
			userMgr.setUserPrivileges(modules, userPrivileges);
//			System.err.println(modules.toString(4));
//			super.hasToolbar = privileges != null && privileges.length() > 0;
			this.jsonData = modules.toString();
//			System.err.println(modules.toString(4));
		}
		catch (Exception e) 
		{
			super.setResponseException("初始化界面失败，因为异常"+e);
			log.error("Failed to initialize the preview of rolemgr for exception ", e);
			return "close";
		}
		return "privileges";
	}

	/**
	 * 
	 * @return
	 */
	public String query()
	{
		try 
		{
			if( id != null && !id.equals("-1") )
			{
				if( id.startsWith(",") )
					values.put("#roles#", id.substring(1));
				else
					values.put("#roles#", id);
			}
			else if( super.getUserRole() != 1 )
			{
				JSONObject role = ZKMgr.getZookeeper().getJSONObject("/cos/config/role");
				role = RoleMgr.getMyRole(role, super.getUserRole());
				if( role != null )
				{
					StringBuffer sb = new StringBuffer();
					roleMgr.setRoleString(role, sb);
					if( sb.length()>0 )sb.deleteCharAt(0);
					sql += " AND ROLEID IN("+sb+")";
					values.put("#roles#", sb.toString());
				}
				else
					values.put("#roles#", String.valueOf(getUserRole()));
			}
			if( !isSysadmin() ){
				values.put("#creators#", this.userMgr.getMyChildren(super.getUserAccount()));
				values.put("#account#", super.getUserAccount());
			}
			values.put("account", super.getUserAccount());
			String xmlpath = "/grid/local/userquery.xml";
	        String rsp = this.grid(xmlpath);
	        return rsp;
		}
		catch (Exception e) 
		{
			super.setResponseException("打开用户查询界面界面失败，因为异常"+e);
			log.error("Failed to initialize the view of grid for exception ", e);
			return "close";
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public String propmgr()
	{
		try 
		{
	        String rspstr = super.grid("/grid/local/userpropcfg.xml");
//	        if( localData == null || localData.isEmpty() || localData.length() < 5 )
//	        {//加载缺省的数据
//	        }
	        return rspstr;
		}
		catch (Exception e) 
		{
			super.setResponseException("初始化界面失败，因为异常"+e);
			log.debug("Failed to initialize the view of cmpcfg for exception ", e);
			return "close";
		}
	}

	/**
	 * 修改配置
	 * @return
	 */
	public String setprop()
	{
		super.setGridxml("/grid/local/userpropcfg.xml");
		return super.doUpdate();
	}

	/**
	 * 修改配置
	 * @return
	 */
	public String delprop()
	{
		super.setGridxml("/grid/local/userpropcfg.xml");
		return super.doDelete();
	}
	
	public void setUserMgr(UserMgr userMgr) {
		this.userMgr = userMgr;
	}
	
	public void setRoleMgr(RoleMgr roleMgr)
	{
		this.roleMgr = roleMgr;
	}

	public User getTheuser()
	{
		return theuser;
	}

	public User getCreator() {
		return creator;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public void setOldpassword(String oldpassword) {
		this.oldpassword = oldpassword;
	}
	public void setNewpassword(String newpassword) {
		this.newpassword = newpassword;
	}

	public String getAuthPasswordRegexp() {
		return authPasswordRegexp;
	}

	public boolean isAuthUnincludeUsername() {
		return authUnincludeUsername;
	}

	public int getAuthPasswordLength() {
		return authPasswordLength;
	}
}
