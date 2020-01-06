package com.focus.cos.web.dev.service;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.json.JSONArray;
import org.json.JSONObject;

import com.focus.cos.api.Sysuser;
import com.focus.cos.api.SysuserClient;
import com.focus.cos.web.common.AjaxResult;
import com.focus.cos.web.common.COSConfig;
import com.focus.cos.web.common.Kit;
import com.focus.cos.web.common.ZKMgr;
import com.focus.cos.web.dev.vo.WeixinSummary;
import com.focus.cos.web.service.SvrMgr;
import com.focus.util.Tools;
import com.focus.util.Zookeeper;

/**
 * 微信管理器
 * @author focus
 *
 */
public class WeixinMgr extends SvrMgr
{
	private static final Log log = LogFactory.getLog(WeixinMgr.class);

	/**
	 * 设置微信的运行地址
	 * @param sysid
	 * @param weixin
	 * @param request
	 * @throws Exception
	 */
	private void setWeixinRunurl(String sysid, JSONObject weixin, HttpServletRequest request) throws Exception
	{
		JSONObject program = ZKMgr.getZookeeper().getJSONObject("/cos/config/modules/"+sysid+"/program/"+weixin.getString("weixinno"));
		if( program == null ) weixin.put("runurl", "回调程序未配置访问地址未知");
		else if( !program.has("publish") ) weixin.put("runurl", "回调程序未发布访问地址未知");
		else
		{
			JSONObject publish = program.getJSONObject("publish");
			if( publish.length() == 0 ) weixin.put("runurl", "回调程序未发布访问地址未知");
			else
			{
				Iterator<?> iterator = publish.keys();
				StringBuffer sb = new StringBuffer();
				while( iterator.hasNext() )
				{
					String serverid = iterator.next().toString();
					JSONObject e = publish.getJSONObject(serverid);
					if( sb.length() > 0 ) sb.append(",");
					sb.append("http://"+e.getString("ip")+":"+weixin.getInt("port")+"/callback");
				}
				weixin.put("runurl", sb.toString());
			}
		}
		weixin.put("summaryurl", Kit.URL_PATH(request)+"weixin!summary.action?sysid="+sysid+"&id="+weixin.getString("weixinno"));
	}
	/**
	 * 得到微信运行数据
	 * @param sysid
	 * @param ip 回调程序启动的伺服器地址
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public JSONArray getRunners(String sysid, HttpServletRequest request) throws Exception
	{
		JSONArray data = new JSONArray();
		Zookeeper zookeeper = ZKMgr.getZookeeper();
		String zkpath = "/cos/config/modules/"+sysid+"/weixin";
		List<JSONObject> list = zookeeper.getJSONObjects(zkpath, true);
		for( JSONObject weixin : list )
		{
			data.put(weixin);
			this.setWeixinRunurl(sysid, weixin, request);
			String access_token_path = "/cos/config/modules/"+sysid+"/weixin/"+weixin.getString("weixinno")+"/access_token";
			JSONObject access_token = zookeeper.getJSONObject(access_token_path);
			if( access_token != null )
			{
				if( access_token.has("get_date") )
				{
					weixin.put("get_date", access_token.getString("get_date"));
				}
				if( access_token.has("expire_date") )
				{
					weixin.put("expire_date", access_token.getString("expire_date"));
				}
				if( access_token.has("access_token") )
				{
					weixin.put("status", "正常");
					weixin.put("access_token", access_token.getString("access_token"));
				}
				else if( access_token.has("errmsg") )
				{
					weixin.put("status", "异常");
					weixin.put("access_token", access_token.getString("errmsg"));
				}
				else
					weixin.put("status", "未知");
			}
			else
			{
				weixin.put("status", "未初始化");
			}
		}
		return data;
	}
	/**
	 * 设置程序发布
	 * @param sysid
	 * @param programid
	 * @param json
	 * @return
	 */
	public AjaxResult<String> setWeixinProgram(String sysid, String weixinno)
	{
		log.debug("Set the callback program("+weixinno+") of weixin for "+sysid);
		AjaxResult<String> rsp = new AjaxResult<String>();
		Zookeeper zookeeper = null;
		try
		{
			zookeeper = ZKMgr.getZookeeper();
			String zkpath = "/cos/config/modules/"+sysid+"/program/"+weixinno;
	        JSONObject program0 = zookeeper.getJSONObject(zkpath);
	        if( program0 != null )
	        {
	        	if( program0.has("startup") )
	        	{
	        		String str = program0.getJSONObject("startup").toString();
	        		if( str.indexOf("com.focus.weixin.CallbackServer") == -1 )
	        		{
	    	        	rsp.setMessage("设置系统【"+sysid+"】微信回调程序["+weixinno+"]失败，因为微信号与已配合的程序【"+program0.getString("name")+"】ID一致，请确保微信号与已配置的程序ID不同。");
	    	        	return rsp;
	        		}
	        	}
	        }
			zkpath = "/cos/config/modules/"+sysid+"/weixin/"+weixinno;
	        JSONObject weixin = zookeeper.getJSONObject(zkpath, true);
	        log.info("The config of weixin is "+weixin);
	        JSONObject program = new JSONObject();
	        program.put("id", weixinno);
	        program.put("name", "微信公众号【"+weixin.getString("name")+"】回调程序");
	        program.put("description", "实现微信公众号回调消息处理，通过实现处理类["+weixin.getString("class")+"]");
	        program.put("version", program0!=null?program0.getString("version"):"0.0.0.0");
			String account = weixin.has("manager")?weixin.getString("manager"):"";
			if( !account.isEmpty() )
			{
				Sysuser user = SysuserClient.getUser(account);
				if( user != null )
				{
					JSONObject maintenance = new JSONObject();
					maintenance.put("programmer", user.getRealname());
					maintenance.put("email", user.getEmail());
					maintenance.put("manager", account);
					maintenance.put("remark", "");
					program.put("maintenance", maintenance);
				}
			}
			JSONObject control = new JSONObject();
			control.put("mode", 0);
			control.put("logfile", "");
			control.put("restartup", 0);
			control.put("delayed", 0);
			control.put("dependence", "Zookeeper");
			control.put("pidfile", "");
			program.put("control", control);

			JSONObject startup = new JSONObject();
			JSONArray command = new JSONArray();
			JSONArray remark = new JSONArray();
			startup.put("command", command);
			startup.put("remark", remark);
			command.put("%java.home%/bin/java");
			remark.put("JVA运行程序: %java.home%根据系统环境变量变化");
			command.put( "-Xms64m" );
			remark.put("启动初始化的内存分配");
			command.put( "-Xmx1024m" );
			remark.put("该程序最大的内存使用上限");
			command.put( "-cp" );
			remark.put("classpath");
			command.put( "../config"+System.getProperty("path.separator", ";")+"../lib/*");
			remark.put("使用系统lib库中jar");
			command.put( "com.focus.weixin.CallbackServer" );
			remark.put("微信回调主框架程序类");
			command.put( weixinno );
			remark.put("公众号的微信号程序唯一ID");
			command.put( Tools.getLocalIP()+":"+COSConfig.getLocalControlPort() );
			remark.put("读取配置连接ZK的连接地址");
			command.put( zkpath );
			remark.put("微信的配置参数ZK路径");
			command.put( weixin.getString("class") );
			remark.put("回调实现的处理类");
			program.put("startup", startup);
			program.put("weixinno", weixinno);
			if( program0!=null&&program0.has("publish") )
				program.put("publish", program0.getJSONObject("publish"));
			if( program0!=null&&program0.has("timeline") )
				program.put("timeline", program0.getJSONArray("timeline"));
			JSONArray operlogs = new JSONArray();
			if( program0!=null&&program0.has("operlogs"))
				operlogs = program0.getJSONArray("operlogs");
			JSONObject operlog = new JSONObject();
			operlog.put("time", Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()));
			operlog.put("remark", "用户通过微信公众号开发管理生成程序配置");
			operlog.put("oper", program0!=null?1:0);
			operlog.put("user", super.getAccountName());
			program.put("operlogs", operlogs);
			zkpath = "/cos/config/modules/"+sysid+"/program";
			if( zookeeper.exists(zkpath) == null )
			{
				zookeeper.create(zkpath, new byte[0]);
			}
			zkpath = "/cos/config/modules/"+sysid+"/program/"+weixinno;
	        zookeeper.setJSONObject(zkpath, program);
			rsp.setResult(weixinno);
			rsp.setMessage("设置系统【"+sysid+"】微信回调程序["+weixinno+"]成功，请查看程序管理对应的配置项。");
			rsp.setSucceed(true);
		}
		catch (Exception e)
		{
			log.error("", e);
			rsp.setMessage("设置系统【"+sysid+"】微信回调程序["+weixinno+"]失败，因为"+e.getMessage()+", 请联系系统管理员。");
		}
		return rsp;
	}
	/**
	 * 得到微信菜单 
	 * @param sysid
	 * @param weixinno
	 * @return
	 * @throws Exception
	 */
	public JSONObject getMenu(String sysid, String weixinno) throws Exception
	{
		ZooKeeper zookeeper = ZKMgr.getZooKeeper();
		String path = "/cos/config/modules/"+sysid+"/weixin/"+weixinno+"/menu";
		Stat stat = zookeeper.exists(path, false); 
		if( stat != null)
		{
			String json = new String(zookeeper.getData(path, false, stat), "UTF-8");
			return new JSONObject(json);
		}
		else
		{
			return null;
		}
	}

	/**
	 * 得到微信运行数据
	 * @return
	 * @throws Exception 
	 */
	public WeixinSummary getWeixinSummary(String sysid, JSONObject weixin, HttpServletRequest request) throws Exception
	{
		String weixinno = weixin.getString("weixinno");
		Zookeeper zookeeper = ZKMgr.getZookeeper();
		String path = "/cos/config/modules/"+sysid+"/weixin/"+weixinno;
		this.setWeixinRunurl(sysid, weixin, request);

		String access_token_path = path+"/access_token";
		JSONObject access_token = zookeeper.getJSONObject(access_token_path);
		if( access_token != null )
		{
			if( access_token.has("get_date") )
			{
				weixin.put("get_date", access_token.getString("get_date"));
			}
			if( access_token.has("expire_date") )
			{
				weixin.put("expire_date", access_token.getString("expire_date"));
			}
			if( access_token.has("access_token") )
			{
				weixin.put("status", "<span class='skit_fa_icon_green fa fa-refresh fa-spin'></span>正常");
				weixin.put("access_token", access_token.getString("access_token"));
			}
			else if( access_token.has("errmsg") )
			{
				weixin.put("status", "<span class='skit_fa_icon_yellow fa fa-warning'></span>异常");
				weixin.put("access_token", access_token.getString("errmsg"));
			}
			else
				weixin.put("status", "未知");
		}
		else
		{
			weixin.put("status", "<span class='skit_fa_icon_red fa fa-ban'></span>未启动");
		}

		String jsapi_ticket_path = path+"/jsapi_ticket";
		JSONObject jsapi_ticket = zookeeper.getJSONObject(jsapi_ticket_path);
		if( jsapi_ticket != null )
		{
			if( jsapi_ticket.has("get_date") )
			{
				weixin.put("jsapi_ticket_get_date", jsapi_ticket.getString("get_date"));
			}
			if( jsapi_ticket.has("expire_date") )
			{
				weixin.put("jsapi_ticket_expire_date", jsapi_ticket.getString("expire_date"));
			}
			if( jsapi_ticket.has("ticket") )
			{
				weixin.put("jsapi_ticket", jsapi_ticket.getString("ticket"));
			}
			else if( jsapi_ticket.has("errmsg") )
			{
				weixin.put("jsapi_ticket", jsapi_ticket.getString("errmsg"));
			}
		}
		return new WeixinSummary(weixin);
	}
}
