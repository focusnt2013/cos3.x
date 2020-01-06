package com.focus.cos.web.login.service;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.json.JSONObject;

import com.focus.cos.web.common.ZKMgr;
import com.focus.util.Tools;

public class ClearCookie
{
	private static final Log log = LogFactory.getLog(ClearCookie.class);
	/**
	 * 删除指定用户的Ｃｏｏｋｉｅ
	 * @param account
	 */
	public static void delete(String account)
	{
		StringBuffer str = new StringBuffer("Delete the cookie of "+account);
		try
		{
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
						if( c.has("username") && account.equals(c.getString("username")) )
						{
							zookeeper.delete(cookie, stat.getVersion());
							str.append("\r\n\t");
							str.append(cookie);
							if( c.has("token") )
							{
								String token = c.getString("token");
								token ="/cos/login/token/"+token;
								stat = zookeeper.exists(token, false); 
								if( stat != null )
								{
									zookeeper.delete(token, stat.getVersion());
									str.append("\r\n\t");
									str.append(token);
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
	 * 删除指定角色以及所有子角色的所有Ｃｏｏｋｉｅ
	 * @param roleid
	public static void delete(int roleid, HttpServletRequest request)
	{
		StringBuffer str = new StringBuffer("Delete the cookie of all roles "+roleid);
		try
		{
			HashMap<Integer, JSONObject> map = new HashMap<Integer, JSONObject>();
			ZooKeeper zookeeper = ZKMgr.getZooKeeper();
			if( roleid != 1 )
			{
				List<String> list = zookeeper.getChildren("/cos/config/role", false);
				for( String rolestr : list )
				{
					if( !Tools.isNumeric(rolestr) ) continue;
					String path = "/cos/config/role/"+rolestr;
					Stat stat = zookeeper.exists(path, false);
					if( stat != null )
					{
						String json = new String(zookeeper.getData("/cos/config/role/"+roleid, false, stat), "UTF-8");
						JSONObject role = new JSONObject(json);
						if( role.has("parent") && roleid == role.getInt("parent") )
						{
							map.put(role.getInt("id"), role);
						}
					}
				}
			}
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
						if( c.has("roleid") && ( c.getInt("roleid") == roleid || map.containsKey(c.getInt("roleid")) ) )
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
	 */
	
	/**
	 * 执行清除
	 * @throws Exception
	 */
	public static Exception execute()
	{
		//设置令牌
		ZooKeeper zookeeper = null;
		StringBuffer str = null;
		try
		{
			zookeeper = ZKMgr.getZooKeeper();
			String path = "/cos/login/token";
			log.info("ClearCookie execute to "+path);
			Stat stat = zookeeper.exists(path, false); 
			str = new StringBuffer("Clear the token of login:");
			if( stat != null)
			{
				List<String> list = zookeeper.getChildren(path, false);
				for( String token : list )
				{
					token = path+"/"+token;
					stat = zookeeper.exists(token, false); 
					if( stat != null && System.currentTimeMillis()-stat.getMtime()>Tools.MILLI_OF_DAY )
					{
						str.append("\r\n\tDelete the token of ");
						str.append(token);
						str.append(" from "+Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", stat.getCtime()));
						str.append(" moidfy "+Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", stat.getMtime()));
						str.append(" version "+stat.getVersion());
						zookeeper.delete(token, stat.getVersion());
					}
				}
			}
			log.debug(str.toString());
			
			path = "/cos/login/cookie";
			stat = zookeeper.exists(path, false); 
			str = new StringBuffer("Clear the cookie of login:");
			if( stat != null)
			{
				List<String> list = zookeeper.getChildren(path, false);
				for( String cookie : list )
				{
					cookie = path+"/"+cookie;
					stat = zookeeper.exists(cookie, false); 
					if( stat != null && System.currentTimeMillis()-stat.getCtime()>Tools.MILLI_OF_DAY )
					{
						str.append("\r\n\tDelete the cookie of ");
						str.append(cookie);
						str.append(" from "+Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", stat.getCtime()));
						str.append(" moidfy "+Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", stat.getMtime()));
						str.append(" version "+stat.getVersion());
						zookeeper.delete(cookie, stat.getVersion());
					}
				}
			}
			log.debug(str.toString());
//			path = "/cos/login/session";
//			stat = zookeeper.exists(path, false);
//			if( stat != null ) zookeeper.delete(path, stat.getVersion());
//			path = "/cos/login/log";
//			stat = zookeeper.exists(path, false);
//			if( stat != null ) zookeeper.delete(path, stat.getVersion());
			return null;
		}
		catch(Exception e)
		{
			log.error("Failed to clear the token for exception "+e.getMessage());
			if( str != null ) log.error("Show all the log:\r\n\t"+str);
			return e;
		}
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	public static void logout(String token, String cookie)
	{
		//设置令牌
		ZooKeeper zookeeper = null;
		StringBuffer str = null;
		int port = 0;
		try
		{
			log.info("ClearCookie.logout:"+port);
			str = new StringBuffer("Clear the token and cookie of login:");
			zookeeper = ZKMgr.getZooKeeper();
			String path = "/cos/login/token/"+token;
			Stat stat = zookeeper.exists(path, false); 
			str.append("\r\n\t"+path);
			if( stat != null)
			{
				str.append(" from "+Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", stat.getCtime()));
				str.append(" moidfy "+Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", stat.getMtime()));
				str.append(" version "+stat.getVersion());
				zookeeper.delete(path, stat.getVersion());
			}
			
			path = "/cos/login/cookie/"+cookie;
			stat = zookeeper.exists(path, false); 
			str.append("\r\n\t"+path);
			if( stat != null)
			{
				str.append(" from "+Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", stat.getCtime()));
				str.append(" moidfy "+Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", stat.getMtime()));
				str.append(" version "+stat.getVersion());
				zookeeper.delete(path, stat.getVersion());
			}
			log.debug(str.toString());
		}
		catch(Exception e)
		{
			log.error("Failed to clear the token for exception "+e);
			log.debug("Show all the log:\r\n"+str);
		}
	}
}
