package com.focus.cos.web.ops.action;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import com.focus.cos.api.LogSeverity;
import com.focus.cos.web.common.Kit;
import com.focus.cos.web.common.PathFactory;
import com.focus.cos.web.common.ZKMgr;
import com.focus.cos.web.ops.service.FilesMgr;
import com.focus.cos.web.ops.service.Monitor.RunFetchMonitor;
import com.focus.cos.web.ops.service.MonitorMgr;
import com.focus.cos.web.service.SvrMgr;
import com.focus.util.Base64;
import com.focus.util.HttpUtils;
import com.focus.util.IOHelper;
import com.focus.util.QuickSort;
import com.focus.util.SHA1;

public class RpcAction extends OpsAction
{
	private static final long serialVersionUID = 1L;
	public static final Log log = LogFactory.getLog(RpcAction.class);
	
	/*文件管理器*/
	private FilesMgr filesMgr;

	/**
	 * 伺服器的资源管理器导航
	 * @return
	 */
	public String navigate()
	{
		try
		{
			this.onlyGateone = true;
			JSONObject privileges = getMonitorMgr().getClusterPrivileges(super.getUserRole(), super.getUserAccount());
//			System.err.println(privileges.toString(4));
			JSONArray gateones = this.getMonitorMgr().getClusterGateone(privileges, super.isSysadmin());
			jsonData = "[]";
			if( gateones == null )
			{
				gateones = new JSONArray();
			}
			HashMap<String,JSONArray> dirs = new HashMap<String, JSONArray>(); 
			for( int i = 0; i < gateones.length(); i++ ){
				JSONObject e = gateones.getJSONObject(i);
//				System.err.println(e.toString(4));
				if( id != null && !id.isEmpty() && !id.equals(String.valueOf(e.getInt("id"))) ){
					gateones.remove(i);
					i -= 1;
					continue;
				}
				e.put("isParent", true);
				e.put("iconClose", "images/icons/folder_closed.png");
				e.put("iconOpen",  "images/icons/folder_opened.png");
				e.put("open", true);
				ArrayList<JSONObject> list = new ArrayList<JSONObject>();
				JSONObject dir = null;
				/*if( super.isSysadmin() ){
					dir = new JSONObject();
					list.add(dir);
					dir.put("gateone", true);
					dir.put("name", "localhost(堡垒终端)");
					dir.put("id", e.getInt("id"));
					dir.put("ip", e.getString("ip"));
					dir.put("title", "堡垒机本地伺服器");
//					local.put("icon", "images/icons/ssh.png");
					dir.put("iconClose", "images/icons/folder_closed.png");
					dir.put("iconOpen",  "images/icons/folder_opened.png");
					JSONArray subchildren = new JSONArray();
					dir.put("children", subchildren);
					dirs.put(e.getString("ip"), subchildren);
				}*/
				String serverid = Base64.encode(e.getString("security-key").getBytes());
				JSONArray array = ZKMgr.getZookeeper().getJSONArray("/cos/config/gateone/"+serverid, true);
//				System.err.println(array.toString(4));
				for( int j = 0; j < array.length(); j++ ){
					JSONObject terminal = array.getJSONObject(j);
					if( !terminal.has("port") || !terminal.has("ip") ){
						log.error("Found the error config of terminal below "+terminal.toString(4));
						continue;
					}
//					System.out.println(terminal.toString(4));
					String serverkey = terminal.has("security-key")?terminal.getString("security-key"):"";
					if( serverkey.isEmpty() ){
						log.error("Found the security-key null from the config of terminal below"+terminal.toString(4));
						continue;
					}
					if( privileges == null ||
					   !privileges.has(serverkey) )
					{
						if( !isSysadmin() ){
							continue;
						};
					}
					else{
						JSONObject p = privileges.getJSONObject(serverkey);
						if( !p.has("ssh") || !p.getBoolean("ssh") ){
							continue;
						}
					}
					JSONArray subchildren = null;
					if( !dirs.containsKey(terminal.getString("ip"))){
						dir = new JSONObject();
						list.add(dir);
						dir.put("gateone", true);
						dir.put("name", terminal.getString("ip"));
						dir.put("ip", terminal.getString("ip"));
						dir.put("id", e.getInt("id"));
						dir.put("pid", terminal.getString("ip")+"@"+e.getInt("id"));
						dir.put("iconClose", "images/icons/folder_closed.png");
						dir.put("iconOpen",  "images/icons/folder_opened.png");
						dir.put("title", String.format("SSH端口%s, 伺服器代码%s", terminal.getInt("port"), serverkey));
						subchildren = new JSONArray();
						dir.put("children", subchildren);
						dirs.put(terminal.getString("ip"), subchildren);
						List<RunFetchMonitor> avaliabes = MonitorMgr.getInstance().getMonitor().getAllRunFetchMonitor(terminal.getString("ip"));
						JSONArray servers = new JSONArray();
						for(RunFetchMonitor monitor : avaliabes) {
							if( monitor.getSysDesc() == null ){
								continue;
							}
							JSONObject server = new JSONObject();
							server.put("security-key", monitor.getSysDesc().getSecurityKey());
							server.put("server-name", monitor.getSysDesc().getDescript());
							server.put("server-ip", monitor.getIp());
							server.put("server-port", monitor.getPort());
							servers.put(server);
						}
						dir.put("servers", servers);
					}
					else{
						subchildren = dirs.get(terminal.getString("ip"));
					}
					if( dir != null ){
						dir.put("port", terminal.getInt("port"));
					}
					else{
						log.error("Found the dir null from the config of terminal below"+terminal.toString(4));
					}
					String ssh = terminal.getString("id");
					ssh = ssh.substring(0, ssh.lastIndexOf(":"));
					terminal.put("ssh", ssh);
					terminal.put("name", terminal.getString("user"));
					subchildren.put(terminal);
					terminal.put("title", String.format("登录账号%s, SSH地址%s, SSH端口%s, 伺服器代码%s",
						terminal.getString("user"), terminal.getString("ip"), terminal.getInt("port"), terminal.getString("security-key")));
//					terminal.put("title", String.format("登录账号%s", terminal.getString("user")));
				}
				if(list.size()>1){
					QuickSort sorter = new QuickSort() {
						@Override
						public boolean compareTo(Object sortSrc, Object pivot) {
							String l = ((JSONObject)sortSrc).getString("ip");
							String r = ((JSONObject)pivot).getString("ip");
							return l.compareTo(r)<0;
						}
					};
					sorter.sort(list);
				}
				else if( !this.isSysadmin() && list.isEmpty() ){
					gateones.remove(i);
					i -= 1;
					continue;
				}
				JSONArray children = new JSONArray();
				for(JSONObject o : list){
					children.put(o);
				}
				e.put("children", children);
			}
			jsonData = gateones.toString();
			if( gateones.length() == 0 ){
				this.setResponseException("集群未配置任何伺服器作为堡垒机，不能使用集群SSH远程管理器");
			}
		} catch (Exception e) {
			log.error("", e);
			super.responseException = e.getMessage();
		}
		return "navigate";
	}
	/**
	 * 
	 * @return
	 */
	private String apiKey;
	private String urlOrigin;//访问的地址
	private String upn;//缺省的用户
	private String sshUrl;//SSHURL
	private String signature;//
	private boolean embedded;
	public String open()
	{
		JSONObject server = null;
		if( id != null )
		{
			server = this.getMonitorMgr().getServer(this.getServerId());
			log.info("Open the ssh of "+server+" by id "+id);
			if( server == null )
			{
				this.setResponseException(String.format("未能找到您要远程控制的堡垒机伺服器[%s]。", id));
				return "404";
			}
			ip = server.getString("ip");
			port = server.getInt("port");
//			id = server.getString("security-key");
			if( this.getMonitorMgr().getState(this.getServerId()) == 0 )
			{
				this.setResponseException("伺服器堡垒机监控未启动，不能打开SSH管理器。");
				return "404";
			}
		}
		else{
			this.setResponseException("远程堡垒机伺服器的ID未提供");
			return "404";
		}
		RunFetchMonitor runner = this.getMonitorMgr().getMonitor().getRunFetchMonitor(ip, port);
		if( runner == null )
		{
			this.setResponseException("您没有执行服务器SSH指令的权限");
			return "close";
		}
		JSONObject json = null;
		String sshid = super.getRequest().getParameter("sshid");
		String logmsg = String.format("用户通过伺服器【%s:%s】堡垒机打开远程控制[%s]", ip, port, sshid);
		try {
			JSONObject terminal = null;
			if( sshid != null && !sshid.isEmpty() && !sshid.equals("localhost") ){
				//嵌入式打开
				String serverid = Base64.encode(server.getString("security-key").getBytes());
				terminal = ZKMgr.getZookeeper().getJSONObject("/cos/config/gateone/"+serverid+"/"+sshid, true);
				if( terminal == null ){
					this.setResponseException("未知SSH终端【"+sshid+"】。");
					return "alert";
				}
				JSONObject privileges = this.getMonitorMgr().getServerPrivileges(
						super.getUserRole(), super.getUserAccount(), terminal.getString("security-key"));
				grant = privileges.has("ssh")&&privileges.getBoolean("ssh");
				if( !grant && !isSysadmin() ){
					this.setResponseException("您没有权限打开SSH终端【"+sshid+"】。");
					return "alert";
				}
			}
			else{
				//非嵌入式打开
				JSONObject privileges = this.getMonitorMgr().getServerPrivileges(
					super.getUserRole(), super.getUserAccount(), server);
				grant = privileges.has("ssh")&&privileges.getBoolean("ssh");
				if( !grant && !isSysadmin() ){
					this.setResponseException("您没有权限打开堡垒机伺服器("+ip+")。");
					return "alert";
				}
			}

			byte[] payload = filesMgr.fetchfile(ip, port, "plugins/GateOne/conf.d/30api_keys.conf");
			String str = new String(payload);
			int i = str.indexOf("{");
			str = str.substring(i);
			json = new JSONObject(str);
			JSONObject api_keys = json.getJSONObject("*").getJSONObject("gateone").getJSONObject("api_keys");
			String apiToken = null;
			Iterator<?> iterator = api_keys.keys();
			if( iterator.hasNext() ){
				apiKey = iterator.next().toString();
				apiToken = api_keys.getString(apiKey);
			}
			if( apiToken == null || apiKey == null ){
				this.setResponseException("无法安全签名，请联系系统管理员检查堡垒机伺服器("+ip+")配置30api_keys.conf");
				return "alert";
			}
			payload = filesMgr.fetchfile(ip, port, "plugins/GateOne/conf.d/10server.conf");
			str = new String(payload);
			str = str.substring(str.indexOf("{"));
			while( (i = str.indexOf("//")) != -1 ){
				int j = str.indexOf("\n", i);
				if( j != -1 ){
					str = str.substring(0, i)+str.substring(j);
				}
				else{
					break;
				}
			}
			json = new JSONObject(str);
			JSONObject gateone = json.getJSONObject("*").getJSONObject("gateone");
			upn = gateone.getString("default_upn");
			if( terminal != null ){
				sshUrl = "ssh://"+terminal.getString("user")+"@"+terminal.getString("ip")+":"+(terminal.getInt("port"));
				super.jdbcUserpswd = Kit.chr2Unicode(terminal.getString("password"));
			}
			else if( "localhost".equals(sshid) ){
				sshUrl = "ssh://"+upn+"@localhost:"+(gateone.has("ssh_port")?gateone.getInt("ssh_port"):22);
			}
			if( sshUrl != null ){
				logmsg = String.format("用户通过伺服器【%s:%s】堡垒机打开远程控制[%s]", ip, port, sshUrl);
			}
			else{
				logmsg = String.format("系统管理员打开伺服器【%s:%s】堡垒机总控界面", ip, port);
			}
			timestamp = System.currentTimeMillis();
			signature = SHA1.getHmacSHA1(apiKey+upn+timestamp, apiToken, "HmacSHA1");
			log.info("Succeed to signature from "+apiKey+upn+timestamp+"\r\n\t"+signature);
//			JSONArray origins = gateone.getJSONArray("origins");
			urlOrigin = gateone.getString("gateone_address");//origins.getString(origins.length()-1);
			String s = runner.getSysDesc().getSecurityKey();
			File dirGateone = new File(PathFactory.getDataPath(), "gateone/"+Base64.encode(s.getBytes()));
			if( !dirGateone.exists() ){
				dirGateone.mkdirs();
			}
			File file = new File(dirGateone, "gateone.css");
			payload = HttpUtils.getFile("https://"+ip+":10443/static/gateone.css");
			if( payload == null ){
				if( file.exists() ){
					payload = IOHelper.readAsByteArray(file);
				}
				else{
					throw new Exception("暂时无法连接GateOne堡垒机。");
				}
			}
			else if( !file.exists() && file.length() != payload.length ){
				IOHelper.writeFile(file, payload);//缓存
			}
			super.pageModel = new String(payload);
			
			file = new File(dirGateone, "gateone.js");
			payload = HttpUtils.getFile("https://"+ip+":10443/static/gateone.js");
			if( payload == null ){
				if( file.exists() ){
					payload = IOHelper.readAsByteArray(file);
				}
				else{
					throw new Exception("暂时无法连接GateOne堡垒机。");
				}
			}
			else if( !file.exists() && file.length() != payload.length ){
				IOHelper.writeFile(file, payload);//缓存
			}
			StringBuffer sb = new StringBuffer(new String(payload, "UTF-8"));
			i = sb.indexOf("go.Net.reauthenticate();");
			if( i != -1 ){
				sb.insert(i, "alert(response);");
			}
			final String script_old = "go.Visual.displayMessage(gettext(\"An SSL certificate must be accepted by your browser to continue.  Please click <a href='\"+acceptURL+\"' target='_blank'>here</a> to be redirected.\"));";
			i = sb.indexOf(script_old);
			if( i != -1 ) {
				sb.replace(i, i+script_old.length(), "tipsAcceptSSL(acceptURL);");
			}
			super.javascript = sb.toString();
			embedded = this.sshUrl!=null;
			return "gateone";
			
		} catch (Exception e) {
			log.error("Failed to pen ssh", e);
			this.setResponseException(e.getMessage());
			return "close";
		}
		finally{
			if( super.responseException != null ){
				super.logoper(LogSeverity.ERROR, logmsg+"出现异常", "远程控制", responseException, "");
			}
			else{
				super.logoper(logmsg, "远程控制", "", "");
				SvrMgr.sendNotiefiesToSystemadmin(super.getRequest(), "远程控制", String.format("[%s]%s", super.getUserName(), logmsg), 
					String.format("系统用户[%s]通过堡垒机【%s:%s】打开了SSH远程控制终端", super.getUserAccount(), ip, port), null, null, null);
			}
		}
	}
	public String getSignature() {
		return signature;
	}
	public boolean isEmbedded() {
		return embedded;
	}
	public String getUpn() {
		return upn;
	}
	public String getUrlOrigin() {
		return urlOrigin;
	}
	public String getApiKey() {
		return apiKey;
	}
	public String getSshUrl() {
		return sshUrl;
	}
	/**
	 *  
	 * @return
	 */
	public String debug()
	{
		return "debug";
	}

	/**
	 * 查找指定类所在jar包的路径
	 * @return
	 */
	public String findjars()
	{
		this.viewTitle = "查找指定类所在jar包的路径";
		try {
			this.listData = this.getMonitorMgr().getServers();
		} catch (Exception e) {
			responseException = e.getMessage();
		}
		return "findjars";
	}
	public void setFilesMgr(FilesMgr filesMgr) {
		this.filesMgr = filesMgr;
	}
}
