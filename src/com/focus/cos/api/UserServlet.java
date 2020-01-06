package com.focus.cos.api;

import java.io.Serializable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.zookeeper.data.Stat;
import org.json.JSONArray;
import org.json.JSONObject;

import com.focus.cos.control.COSApi;
import com.focus.cos.user.dao.UserDao;
import com.focus.sql.Dao;
import com.focus.util.IOHelper;
import com.focus.util.Tools;
import com.focus.util.Zookeeper;

public class UserServlet extends AbaseServlet
{
	private static final long serialVersionUID = -6082791442929441606L;
	private UserDao dao = null;
	public UserServlet(COSApi server) throws Exception
	{
		super(server);
		if( !this.server.isProxy() )
		{
			dao = new UserDao();
			dao.prepareInsert();
		}
	}
	
	/**
	 * 处理用户查询哟求
	 * @param request
	 * @param response
	 * @return
	 */
	public void query(HttpServletRequest request, HttpServletResponse response, StringBuffer log) throws Exception 
	{
		String method = COSApi.getRequestValue(request, "method");
		String ci = COSApi.getRequestValue(request, "COS-ID");
		log.append("\r\n\tmethod: "+method+"");
		if( method == null || method.isEmpty() )
		{
			log.append("\tunknown request.");
			unknown(request);
			return;
		}
		if( "listUser".equals(method) )
		{
			String roleid = COSApi.getRequestValue(request, "roleid");
			String status = COSApi.getRequestValue(request, "status");
			String sex = COSApi.getRequestValue(request, "sex");
			String creator = COSApi.getRequestValue(request, "creator");
			log.append("\r\n\troleid="+roleid+",status="+status+",sex="+sex+",creator="+creator);
			List<Sysuser> list = UserDao.query(roleid, status, sex, null, creator);
			log.append("\r\n\tfound "+list.size()+" users.");
			Serializable[] args = new Serializable[list.size()];
			list.toArray(args);
			super.write(ci, response, args, log);
		}
		else if( "getUser".equals(method) )
		{
			String username = COSApi.getRequestValue(request, "username");
			log.append("\r\n\tusername: "+username);
			List<Sysuser> list = UserDao.query(null, null, null, username, null);
			log.append("\r\n\tfound "+list.size()+" users.");
			Serializable[] args = new Serializable[list.size()];
			list.toArray(args);
			super.write(ci, response, args, log);
		}
		else if( "getRole".equals(method) )
		{
			JSONObject result = new JSONObject();
			String username = COSApi.getRequestValue(request, "username");
			log.append("\r\n\tusername: "+username);
			if( username == null || username.isEmpty() )
			{
				result.put("error", "没有设置查询角色数据的条件");
				log.append("\tnot found.");
			}
			else
			{
				int roleid = UserDao.getRoleid(username);
				if( roleid > 0 )
				{
					Zookeeper zookeeper = server.getZookeeper();
					log.append("\r\n\tLoad the config of role["+roleid+"] from zookeeper.");
					String zkpath = "/cos/config/role";
					Stat stat = zookeeper.exists(zkpath, false);
					if( stat != null )
					{
						log.append("\tok.");
						JSONObject role = new JSONObject(new String(zookeeper.getData(zkpath, false, stat), "UTF-8"));
						role = this.getRoles(role, roleid);
						if( role != null )
						{
							log.append("\r\n\tFound the config of role("+role.getString("name")+").");
							result.put("result", role);
						}
						else
						{
							
							log.append("\r\n\tNot found the config of role.");
							result.put("error", "在角色配置表中没有找到用户["+username+"]的角色");
						}
					}
					else
					{
						log.append("\tnot found.");
						result.put("error", "系统的角色配置表不存在");
						//TODO: 发送告警
					}
				}
				else
				{
					log.append("\tnot found "+roleid);
					result.put("error", "查询角色用户["+username+"]的角色ID失败"+roleid);
				}
			}
			super.write(ci, response, result);
		}
		else
		{
			unknown(request);
		}
	}
	
	/**
	 * 递归获取ID对应的角色对象
	 * @param config
	 * @param roleid
	 * @return
	 */
	private JSONObject getRoles(JSONObject config, int roleid)
	{
		if( !config.has("id") ) return null;
		int id = config.getInt("id");
		if( id == roleid ) return config;
		if( !config.has("children") ) return null;
		JSONArray children = config.getJSONArray("children");
		for(int i = 0; i < children.length(); i++)
		{
			config = this.getRoles(children.getJSONObject(i), roleid);
			if( config != null ) return config;
		}
		return null;
	}
	
	@Override
	public void save(HttpServletRequest request, byte[] payload, JSONObject response) throws Exception  
	{
		Serializable s = IOHelper.readSerializableNoException(payload);
		if( s != null && s instanceof Sysuser )
		{
			Sysuser user = (Sysuser)s;
			if( user.getUsername() == null || user.getUsername().isEmpty() )
			{
				throw new Exception("没有设置新增用户的账户名称");
			}

			if( user.getPassword() == null || user.getPassword().isEmpty() || user.getPassword().length() < 6 )
			{
				throw new Exception("没有设置新增用户的账户密码或密码长度小于6位");
			}

			if( user.getEmail() == null || user.getEmail().isEmpty() || user.getEmail().indexOf("@") == -1 )
			{
				throw new Exception("没有设置新增用户的账户邮箱或邮箱格式不正确");
			}

			List<Sysuser> users =  UserDao.query(null, null, null, user.getUsername(), null);
			if( !users.isEmpty() )
			{
				throw new Exception("新增的用户已经存在");
			}
			user.setPassword(Tools.encodeMD5(user.getPassword()));
//			String oper = getValue(request, "oper");
//			int roleid = UserDao.getRoleid(oper);
//			if( roleid < 1 )
//			{
//				throw new Exception("操作员用户不存在或者其角色未配置");
//			}
			String from = request.getParameter("from");
			if( "COSPortal".equals(from) ) 	user.setStatus(Status.Enable.getValue());
			else{
				user.setStatus(Status.Disable.getValue());
//				user.setRoleid((byte)0);
			}
//			if( zookeeper != null )
//			{
//				user.setRoleid((byte)roleid);
//				String zkpath = "/cos/config/role";
//				Stat stat = zookeeper.exists(zkpath, false);
//				if( stat != null )
//				{
//					JSONObject role = new JSONObject(new String(zookeeper.getData(zkpath, false, stat), "UTF-8"));
//					role = this.getRoles(role, roleid);
//					if( role != null )
//					{
//						role = getRoles(role, user.getRoleid());
//					}
//					if( role == null )
//					{
//						throw new Exception("在角色配置表中没有找到操作员的["+oper+"]的角色");
//					}
//				}
//				else
//				{
//					throw new Exception("系统的角色配置表不存在");
//				}
//			}
//			else
//			{
//				user.setRoleid((byte)0);
//			}
			long id = Dao.getMaxId("TB_USER", "ID") + 1;
			user.setId((int)id);
			dao.save(user);
			int r = dao.execute();
			if( r == 1 )
			{
				response.put("id", id);
			}
			else
			{
				response.put("error", "新增用户没生效");
			}
			
			response.put("from", from);
			Syslog log = new Syslog();
			log.setLogtype(LogType.运行日志.getValue());
			log.setAccount(from);
			log.setLogseverity(LogSeverity.INFO.getValue());
			log.setLogtype(LogType.运行日志.getValue());
			log.setCategory("自动用户管理");
			log.setLogtext("程序["+from+"]从客户端["+request.getRemoteAddr()+"/"+getIp(request)+"]新增用户["+user.getUsername()+"]");
			log.setContextlink("user!query.action");
			JSONObject context = new JSONObject();
			context.put("账户ID", user.getId());
			context.put("账户名称", user.getUsername());
			context.put("真实姓名", user.getRealname());
			context.put("账户邮箱", user.getEmail());
			context.put("性别", user.getSex()==1?"男":"女");
			log.setContext(context.toString(4));
			server.write(log);
		}
		else
		{
			response.put("error", "Unknown user("+s+").");
		}
	}
}
