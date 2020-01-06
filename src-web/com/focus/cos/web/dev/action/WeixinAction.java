package com.focus.cos.web.dev.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import com.focus.cos.api.LogSeverity;
import com.focus.cos.api.LogType;
import com.focus.cos.api.SyslogClient;
import com.focus.cos.web.common.ZKMgr;
import com.focus.cos.web.dev.service.DiggMgr;
import com.focus.cos.web.dev.service.WeixinMgr;
import com.focus.cos.web.dev.vo.WeixinSummary;
import com.focus.util.Base64X;
import com.focus.util.Tools;
import com.mongodb.client.MongoCollection;

public class WeixinAction extends DevAction
{
	private static final long serialVersionUID = 6518188874943924338L;
	private static final Log log = LogFactory.getLog(WeixinAction.class);
	/**/
	private WeixinMgr weixinMgr;
	/**
	 * 配置系统的微信公众号
	 * @return
	 */
	public String config()
	{
        return this.grid("/grid/local/weixincfg.xml");
	}
	/**
	 * 查询公众号实际运行情况
	 * @return
	 */
	public String query()
	{
		try
		{
			JSONArray data = weixinMgr.getRunners(sysid, super.getRequest());
			for(int i = 0; i < data.length(); i++)
			{
				JSONObject e = data.getJSONObject(i);
				JSONObject datasource = ZKMgr.getZookeeper().getJSONObject(e.getString("datasource"), true);
				String args[] = Tools.split(datasource.getString("dbaddr"), ":");
				String password = new String(Base64X.decode(datasource.getString("dbpassword")));
				MongoCollection<Document> collection = DiggMgr.getMongoCollection(
					args[0], 
					Integer.parseInt(args[1]),
					datasource.getString("dbusername"),
					password,
					datasource.getString("dbname"),
					datasource.getString("dbname"),
					e.getString("weixinno")+"_users");
				if( collection != null )
				{
					e.put("userscount", collection.count());
				}
			}
			this.localData = data.toString();
			String xmlpath = "/grid/local/weixinquery.xml";
	        return this.grid(xmlpath);
		}
		catch(Exception e)
		{
			log.error("Failed to query for ", e);
			super.setResponseException("打开微信公众号管理页面出现异常"+e.getMessage());
			return "alert";
		}
	}
	/**
	 * 总览页面
	 * @return
	 */
	private WeixinSummary weixinSummary;
	public String summary()
	{
		try 
		{
			JSONObject data = ZKMgr.getZookeeper().getJSONObject("/cos/config/modules/"+sysid+"/weixin/"+id, true);
			JSONObject datasource = ZKMgr.getZookeeper().getJSONObject(data.getString("datasource"), true);
			String args[] = Tools.split(datasource.getString("dbaddr"), ":");
			String password = new String(Base64X.decode(datasource.getString("dbpassword")));
			MongoCollection<Document> collection = DiggMgr.getMongoCollection(
				args[0], 
				Integer.parseInt(args[1]),
				datasource.getString("dbusername"),
				password,
				datasource.getString("dbname"),
				datasource.getString("dbname"),
				data.getString("weixinno")+"_users");
			if( collection != null )
			{
				data.put("userscount", collection.count());
			}
			weixinSummary = this.weixinMgr.getWeixinSummary(sysid, data, super.getRequest());
		}
		catch (Exception e)
		{
			log.error("Failed to summary", e);
			this.setResponseException("打开微信公众号("+id+")摘要视图出现异常:"+e);
		}
		return "summary";
	}
	public WeixinSummary getWeixinSummary() {
		return weixinSummary;
	}

	/**
	 * 
	 * @return
	 */
	public String chats()
	{
		return "chats";
	}
	/**
	 * 用户页面
	 * @return
	 */
	public String users()
	{
		String zkpath = "/cos/config/modules/"+sysid+"/weixin/"+id;
		try {
			JSONObject config = ZKMgr.getZookeeper().getJSONObject(zkpath, true);
			String datasource = config.getString("datasource");
			System.setProperty("datasource", datasource);
		} catch (Exception e) {
			e.printStackTrace();
		}
        return this.grid("/grid/local/weixinusers.xml");
	}

	/**
	 * 打开对应模版
	 * @return
	 */
	public String griddigg()
	{
		String zkpath = "/cos/config/modules/"+sysid+"/weixin/"+id;
		try {
			JSONObject config = ZKMgr.getZookeeper().getJSONObject(zkpath, true);
			String datasource = config.getString("datasource");
			System.setProperty("datasource", datasource);
		} catch (Exception e) {
			e.printStackTrace();
		}
        return this.grid("/grid/local/"+this.remark+".xml");
	}
	/**
	 * 清除回调
	 * @return
	 */
	public String callbackclear()
	{
		try
		{
			super.setResponseMessage("成功清除所有历史数据");
		}
		catch (Exception e)
		{
			log.error("Failed to clear for exception:", e);
			super.setResponseException("清除微信公众号回调记录失败"+id);
		}
		return "alert";
	}

	/**
	 * 查询回调
	 * @return
	 */
	public String callbackquery()
	{
		String zkpath = "/cos/config/modules/"+sysid+"/weixin/"+id;
		try {
			JSONObject config = ZKMgr.getZookeeper().getJSONObject(zkpath, true);
			String datasource = config.getString("datasource");
			System.setProperty("datasource", datasource);
		} catch (Exception e) {
			e.printStackTrace();
		}
        return this.grid("/grid/local/weixincallbackquery.xml");
	}
	/**
	 * 打开配置公众号菜单页面
	 * @return
	 */
	private String exampleMenuJson;
	private String menuJson;
	public String presetmenu()
	{
		JSONObject menu = new JSONObject();
		JSONArray buttons = new JSONArray();
		menu.put("button", buttons);
		JSONObject button0 = new JSONObject();buttons.put(button0);
		button0.put("name", "内置功能");
		JSONArray subbuttons = new JSONArray();
		button0.put("sub_button", subbuttons);
		JSONObject button01 = new JSONObject();subbuttons.put(button01);
		button01.put("name", "扫码带提示");
		button01.put("type", "scancode_waitmsg");
		button01.put("key", "rselfmenu_1");
		JSONObject button02 = new JSONObject();subbuttons.put(button02);
		button02.put("name", "扫码推事件");
		button02.put("type", "scancode_push");
		button02.put("key", "rselfmenu_2");
		JSONObject button03 = new JSONObject();subbuttons.put(button03);
		button03.put("name", "系统拍照发图");
		button03.put("type", "pic_sysphoto");
		button03.put("key", "rselfmenu_3");
//		JSONObject button04 = new JSONObject();subbuttons.put(button04);
//		button04.put("name", "拍照或者相册发图");
//		button04.put("type", "pic_photo_or_album");
//		button04.put("key", "rselfmenu_4");
//		JSONObject button05 = new JSONObject();subbuttons.put(button05);
//		button05.put("name", "微信相册发图");
//		button05.put("type", "pic_weixin");
//		button05.put("key", "rselfmenu_5");
		JSONObject button06 = new JSONObject();subbuttons.put(button06);
		button06.put("name", "发送位置");
		button06.put("type", "location_select");
		button06.put("key", "rselfmenu_6");
		JSONObject button07 = new JSONObject();subbuttons.put(button07);
		button07.put("name", "点击事件");
		button07.put("type", "click");
		button07.put("key", "AAA BBB CCC");
		
		
		JSONObject button1 = new JSONObject();buttons.put(button1);
		button1.put("name", "打开网页");
		subbuttons = new JSONArray();
		button1.put("sub_button", subbuttons);
		JSONObject button11 = new JSONObject();subbuttons.put(button11);
		button11.put("name", "打开QQ");
		button11.put("type", "view");
		button11.put("url", "http://m.qq.com/");
		JSONObject button12 = new JSONObject();subbuttons.put(button12);
		button12.put("name", "打开百度");
		button12.put("type", "view");
		button12.put("url", "http://m.baidu.com/");
		JSONObject button2 = new JSONObject();buttons.put(button2);
		button2.put("name", "咨询我们");
		button2.put("type", "click");
		button2.put("key", "custom");
		this.exampleMenuJson = menu.toString(4);
		try {
			JSONObject json = this.weixinMgr.getMenu(sysid, id);
			if( json != null )
			{
				this.menuJson = json.toString(4);
			}
			else
			{
				this.menuJson = this.exampleMenuJson;
			}
			return "presetmenu";
		}
		catch (Exception e)
		{
			super.setResponseException("打开微信公众号配置菜单界面出现异常"+e.getMessage());
			return "close";
		}
	}
	
	/**
	 * 设置菜单
	 * @return
	 */
	public String setmenu()
	{
		String path = null;
		ZooKeeper zookeeper = null;
		try
		{
			zookeeper = ZKMgr.getZooKeeper();
			path = "/cos/config/modules/"+sysid+"/weixin/"+id;
			Stat stat = zookeeper.exists(path, false); 
			if( stat == null)
			{
				throw new Exception("未发现模块子系统【"+sysid+"】微信公众号回调程序配置【"+id+"】。 ");
			}
			JSONObject json = new JSONObject(this.menuJson);
			path += "/menu";
			stat = zookeeper.exists(path, false); 
			if( stat == null)
			{
				zookeeper.create(path, json.toString().getBytes("UTF-8"), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			else
			{
				zookeeper.setData(path, json.toString().getBytes("UTF-8"), stat.getVersion());
			}
			SyslogClient.write(LogType.操作日志, LogSeverity.INFO,
					"设置模块子系统【"+sysid+"】微信公众号【"+id+"】。", super.getUserAccount(), "微信公众号管理", null, null);
			super.setResponseMessage("设置模块子系统【"+sysid+"】微信公众号【"+id+"】自定义菜单成功");
			return "close";
		}
		catch (Exception e)
		{
			log.error("Failed to set the menu of weixin-config", e);
			SyslogClient.write(LogType.操作日志, LogSeverity.ERROR,
					"设置微信公众号自定义菜单出现异常: "+e.getMessage(),
					super.getUserAccount(), 
					"微信公众号管理", null, null);
			super.setResponseException("设置微信公众号自定义菜单出现异常: "+e.getMessage());
			return this.presetmenu();
		}
	}

	public String getMenuJson() {
		return menuJson;
	}

	public void setMenuJson(String menuJson) {
		this.menuJson = menuJson;
	}
	
	public String getExampleMenuJson() {
		return exampleMenuJson;
	}
	
	public void setWeixinMgr(WeixinMgr weixinMgr) 
	{
		this.weixinMgr = weixinMgr;
	}
}
