package com.focus.cos.web.config.service;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.directwebremoting.WebContextFactory;
import org.json.JSONObject;

import com.focus.cos.web.common.Kit;
import com.focus.cos.web.common.PathFactory;
import com.focus.cos.web.common.ZKMgr;
import com.focus.cos.web.service.SvrMgr;
import com.focus.util.Base64X;
import com.focus.util.IOHelper;

/**
 * 组件管理
 * @author focus
 *
 */
public class CfgMgr extends SvrMgr
{
	private static final Log log = LogFactory.getLog(CfgMgr.class);
	/**
	 * 修改属性
	 * @param key
	 * @param value
	 * @return
	 */
	protected String modifyProperty(JSONObject config, String id, String key, String value, String label)
	{
		String s = null;
		javax.servlet.http.HttpServletRequest request = null;
		try
		{
			org.directwebremoting.WebContext web = WebContextFactory.get();   
		    request = web.getHttpServletRequest();
			JSONObject account = (JSONObject) request.getSession().getAttribute("account");
			config.put(key, value);
			if( "POP3Password".equals(key) ){
				config.put(key, "[密码加密]");
				key = "POP3PasswordEncrypt";
				value = Base64X.encode(value.getBytes());
				config.put(key, value);
			}
			if( "EnableHttps".equals(key) ){
				Kit.EnableHttps = "true".equals(value);
				Kit.EnableHttps = "true".equals(value);
			}
			s = String.format("用户[%s]修改[%s]配置参数属性[%s]为[%s]", account.getString("username"), id, label, value);
			set(config, id);
			log.info("Succeed to set the property("+key+":"+value+") of sysconfig.");
		}
		catch(Exception e)
		{
			s += "出现异常"+e;
		}
		logoper(s, "系统配置", null);
		return s;
	}
	
	public static void set(JSONObject config, String id) throws Exception
	{
		String path = "/cos/config/"+id;
		ZooKeeper zookeeper = ZKMgr.getZooKeeper();
		Stat stat = zookeeper.exists(path, false); 
		if( stat == null)
		{
			zookeeper.create(path, config.toString().getBytes("UTF-8"), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		}
		else
		{
			zookeeper.setData(path, config.toString().getBytes("UTF-8"), stat.getVersion());
		}
	}

	/**
	 * 
	 * @return
	 */
	public static JSONObject getConfig(String id)
	{
		String path = "/cos/config/"+id;
		try
		{
			ZooKeeper zookeeper = ZKMgr.getZooKeeper();
			Stat stat = zookeeper.exists(path, false); 
			if( stat == null)
			{
				return null;
			}
			else
			{
				String json = new String(zookeeper.getData(path, false, stat), "UTF-8");
				File file = new File(PathFactory.getCfgPath(), id);
				IOHelper.writeSerializable(file, json);
				return new JSONObject(json);
			}
		}
		catch(Exception e)
		{
			File file = new File(PathFactory.getCfgPath(), id);
			if( file.exists() )
			{
				Object json = IOHelper.readSerializableNoException(file);
				if( json != null && json instanceof String )
					return new JSONObject(json.toString());
			}
			return null;
		}
	}

	/**
	 * 将原来用文件存储的组件配置转换成保存到ZK中
	public static void buildZkdata(File path, String id)
	{
		StringBuffer sb = new StringBuffer("Build the config of "+id+" from file to zookeeper.");
		try
		{
			ZooKeeper zookeeper = ZKMgr.getZooKeeper();
			File file = new File(path, id.equals("system")?"SysConfig":"SoftwareConfig");
			if( !file.exists() ) return;
			Profile profile = (Profile)IOHelper.readSerializableNoException(file);
			file.delete();
			if( profile == null ){
				sb.append("\r\n\tDelete the profile of "+file+" for cannot serializable.");
				return;
			}
			String zkpathcmp = "/cos/config/"+id;
			Stat stat = zookeeper.exists(zkpathcmp, false); 
			if( stat == null)
			{
				JSONObject e = new JSONObject();
				for( Property property : profile.getPropertyList() )
				{
					if( "false".equalsIgnoreCase(property.getValue()) || "true".equalsIgnoreCase(property.getValue()) )
					{
						e.put(property.getName(), Boolean.parseBoolean(property.getValue()));
					}
					else
						e.put(property.getName(), property.getValue());
					e.put(property.getName()+"_remark", property.getRemark());
				}
				zookeeper.create(zkpathcmp, e.toString().getBytes("UTF-8"), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				sb.append("\r\n\tBuild the profile of "+file+" to ");
				sb.append("\r\n\t\t=============================");
				sb.append(e.toString(4));
				sb.append("\r\n\t\t=============================");
			}
			else
			{
				sb.append("\r\n\tFound the profile of "+file+" for built.");
			}
		}
		catch(Exception e )
		{
			log.error("Failed to build the profile of sys to zookeeper for exception ", e);
		}
		log.error(sb.toString());
	}
	 */
}
