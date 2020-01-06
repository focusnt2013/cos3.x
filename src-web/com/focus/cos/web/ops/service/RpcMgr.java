package com.focus.cos.web.ops.service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.data.Stat;
import org.json.JSONArray;
import org.json.JSONObject;

import com.focus.cos.web.common.AjaxResult;
import com.focus.cos.web.common.Debug;
import com.focus.cos.web.common.DebugResponse;
import com.focus.cos.web.common.PathFactory;
import com.focus.cos.web.common.SSH;
import com.focus.cos.web.common.SSHResponse;
import com.focus.cos.web.common.ZKMgr;
import com.focus.cos.web.ops.service.Monitor.RunFetchMonitor;
import com.focus.cos.web.service.SvrMgr;
import com.focus.util.Base64;
import com.focus.util.ClassUtils;

/**
 * 远端过程管理
 * @author focus
 * @version 重构于2017年2月21日
 */
public class RpcMgr extends SvrMgr
{
	public static final Log log = LogFactory.getLog(RpcMgr.class);
	private static HashMap<String, SSH> MapSSH = new HashMap<String, SSH>();
	private static HashMap<String, Debug> MapDebug = new HashMap<String, Debug>();

	public RpcMgr()
	{
		log.info("Initialize the manager of rpc is "+ this.toString());
	}
	/**
	 * 发送SSH
	 * @param host
	 * @param indication
	 * @return
	 */
	public String sendSsh(String host, int port, String command)
	{
		log.info("Send ssh("+host+":"+port+") "+command);
    	try
    	{
    		SSH ssh = MapSSH.get(host+":"+port);
        	if( command.equalsIgnoreCase("exit") && ssh != null )
        	{
	        	ssh.send(command);
        		ssh.close();
        	}
        	else
        	{
	        	if( ssh == null )
	        	{
	        		ssh = new SSH(host, port)
	        		{
						public void connect()
						{
				            MapSSH.put(this.toString(), this);
						}
						public void disconnect(Exception e)
						{
				            MapSSH.remove(this.toString());
						}
						public void receive(String line)
						{
						}
						public void executed()
						{
						}
	        		};
	        		ssh.waitConnect();
	        	}
	        	ssh.send(command);
        	}
    		return "";
    	}
    	catch(Exception e)
    	{
    		return "Failed to ssh for exception:"+e;
    	}
	}
	/**
	 * 得到SSH消息的列表
	 * @param host
	 * @param lastLine
	 * @return
	 */
	public SSHResponse getSshResponse(String host, int port, int offset)
	{
		SSHResponse response = new SSHResponse();
		response.setOffset(offset);
    	SSH ssh = MapSSH.get(host+":"+port);
    	if( ssh != null )
    	{
    		ssh.receive(response);
    	}
		return response;
	}

	/**
	 * 打开调测通道
	 * @param host
	 * @param indication
	 * @return
	 */
	public String openDebug(String host, int port, String module)
	{
		log.info("Open debug("+host+":"+port+" "+module+")");
    	try
    	{
    		Debug debug = MapDebug.get(host+":"+port+" "+module);
        	if( debug == null )
        	{
        		debug = new Debug(host, port, module)
        		{
					public void connect()
					{
						MapDebug.put(this.toString(), this);
					}
					public void disconnect(Exception e)
					{
						MapDebug.remove(this.toString());
					}
					public void receive(String line)
					{
					}
					public void executed()
					{
					}
        		};
        		debug.waitConnect();
        	}
    		return "";
    	}
    	catch(Exception e)
    	{
    		return "打开调测管道出现异常"+e;
    	}
	}
	
	/**
	 * 关闭调测通道
	 * @param host
	 * @param port
	 * @param module
	 * @return
	 */
	public String closeDebug(String host, int port, String module)
	{
		log.info("Close debug("+host+":"+port+" "+module+")");
    	try
    	{
    		Debug debug = MapDebug.get(host+":"+port+" "+module);
        	if( debug != null )
        	{
        		debug.close();
        		return "调测管道被关闭";
        	}
        	else
        		return "调测管道已经被关闭";
    	}
    	catch(Exception e)
    	{
    		return "关闭调测管道出现异常"+e;
    	}
	}
	
	/**
	 * 得到SSH消息的列表
	 * @param host
	 * @param lastLine
	 * @return
	 */
	public DebugResponse getDebugResponse(String host, int port, String module, int offset)
	{
		DebugResponse response = new DebugResponse();
		response.setOffset(offset);
    	Debug debug = MapDebug.get(host+":"+port+" "+module);
    	if( debug != null )
    	{
    		response.setOpened(true);
    		debug.receive(response);
    	}
		return response;
	}
	
	/**
	 * 查找类所在的路径
	 * @param host
	 * @param classCompare
	 * @return
	 */
	public List<String> findJars(String host, String classCompare)
	{
		ArrayList<String> buffer = new ArrayList<String>();
		if( classCompare == null )
		{
			return buffer;
		}
		if( host == null || host.isEmpty() )
		{
			File libPath = PathFactory.getAppPath();
			buffer.add("搜索"+libPath.getAbsolutePath().toString()+"……");
			try
			{
				ClassUtils.findJars(buffer, classCompare, libPath.getAbsoluteFile(), true);
			}
			catch (Exception e)
			{
				buffer.add(e.getMessage());
			}
		}
		else
		{//到指定HOST去查找
		}
		return buffer;
	}
	
	/**
	 * 关闭相关资源
	 */
	public void close()
	{
		log.debug("Close the manager of rpc from "+this.toString());
		Iterator<String> itr = MapSSH.keySet().iterator();
		while(itr.hasNext())
		{
			SSH ssh = (SSH)MapSSH.get(itr.next());
			ssh.close();
		}

		itr = MapDebug.keySet().iterator();
		while(itr.hasNext())
		{
			Debug debug = (Debug)MapDebug.get(itr.next());
			debug.close();
		}
	}

	/**
	 * 删除指定SSH终端
	 * @param serverid
	 * @param serverdesc
	 * @param sshid
	 * @return
	 */
	public synchronized AjaxResult<String> delTerminal(
			String serverkey,
			String serverdesc,
			String sshid)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		try
		{
			String serverid = Base64.encode(serverkey.getBytes());
			String zkpath = "/cos/config/gateone/"+serverid+"/"+sshid;
			Stat stat = ZKMgr.getZookeeper().exists(zkpath);
			if( stat == null ){
				rsp.setMessage("SSH终端【"+sshid+"】已经不存在。");
				return rsp;
			}
			ZKMgr.getZookeeper().delete(zkpath, stat.getVersion());
			rsp.setSucceed(true);
			rsp.setMessage(String.format("成功从堡垒机【%s】删除SSH终端【%s】。", serverdesc, sshid));
			logoper(rsp.getMessage(), "远程管理", null);
			sendNotiefiesToSystemadmin(
					"远程管理",
					String.format("系统管理员[%s]"+rsp.getMessage(), getAccountName()),
                    "",
                    "",
                    null, null);
		}
		catch(Exception e)
		{
			String s = String.format("从堡垒机【%s】删除SSH终端【%s】出现异常%s。", serverdesc, sshid, e.getMessage());
			rsp.setMessage(s);
			logoper(rsp.getMessage(), "远程管理", e);
		}
		return rsp;	
	}
	/**
	 * 得到伺服器列表
	 * @param ip
	 * @return
	 */
	public AjaxResult<String> getTerminalServerKey(String ip)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		try
		{
			List<RunFetchMonitor> avaliabes = MonitorMgr.getInstance().getMonitor().getAllRunFetchMonitor(ip);
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
			rsp.setSucceed(true);
			rsp.setResult(servers.toString());
		}
		catch(Exception e)
		{
			rsp.setMessage(String.format("获取指定IP[%s]的伺服器列表失败:%s", ip, e.getMessage()));
		}
		return rsp;		
	}
	/**
	 * 新增SSH终端
	 * @param parentId
	 * @param ip
	 * @param port
	 * @param user
	 * @param password
	 * @return
	 */
	public AjaxResult<String> addTerminal(
			int parentId,
			String ip,
			int port,
			String user,
			String password,
			String serverkey)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		String sshid = String.format("%s@%s:%s", user, ip, port);
		try
		{
			JSONObject terminal = null;
			JSONObject gateone = MonitorMgr.getInstance().getServer(parentId);
			if( gateone == null )
			{
				rsp.setMessage("未知堡垒机。");
				return rsp;
			}
			RunFetchMonitor monitor = MonitorMgr.getInstance().getMonitor().getRunFetchMonitor(gateone.getString("ip"), gateone.getInt("port"));
			if( monitor == null )
			{
				rsp.setMessage(String.format("堡垒机伺服器【%s:%s】未启动。",gateone.getString("ip"), gateone.getInt("port")));
				return rsp;
			}
			if( !monitor.isConnect() )
			{
				rsp.setMessage(String.format("堡垒机伺服器【%s:%s】监控连接不正常。",gateone.getString("ip"), gateone.getInt("port")));
				return rsp;
			}
			String serverid = monitor.getSysDesc().getServerid();
			if( serverid == null )
			{
				rsp.setMessage(String.format("堡垒机伺服器【%s:%s】唯一标识不存在。",gateone.getString("ip"), gateone.getInt("port")));
				return rsp;
			}
			
			monitor = MonitorMgr.getInstance().getMonitor().getRunFetchMonitor(ip);
			if( monitor == null )
			{
				rsp.setMessage(String.format("未知远程控制伺服器【%s】", ip));
				return rsp;
			}

//			String serverkey = "";
//			if( monitor.getSysDesc() != null )
//			{
//				serverkey = monitor.getSysDesc().getSecurityKey();
//			}
			if( serverkey == null || serverkey.isEmpty() )
			{
				rsp.setMessage(String.format("远程控制伺服器【%s】唯一标识不存在。", ip));
				return rsp;
			}
			terminal = new JSONObject();
			terminal.put("id", sshid);
			terminal.put("pid", parentId);
			terminal.put("ip", ip);
			terminal.put("port", port);
			terminal.put("user", user);
			terminal.put("password", password);
			terminal.put("name", user);
//			terminal.put("server_id", monitor.getServerid());
			terminal.put("security-key", serverkey);
			terminal.put("icon", "images/icons/cluster.png?v=1");
			String zkpath = "/cos/config/gateone/"+serverid+"/"+sshid;
			Stat stat = ZKMgr.getZookeeper().exists(zkpath);
			if( stat != null ){
				rsp.setMessage("SSH终端【"+sshid+"】已经配置，无需再次添加。");
				rsp.setResult(terminal.toString());
				return rsp;
			}
			ZKMgr.getZookeeper().createNode(zkpath, terminal.toString().getBytes("UTF-8"), true);
			rsp.setMessage(String.format("成功在堡垒机伺服器【%s:%s】下添加SSH终端【%s】",
				gateone.getString("ip"), gateone.getInt("port"), sshid));

			terminal.put("title", String.format("登录账号%s, SSH地址%s, SSH端口%s, 伺服器代码%s",
				terminal.getString("user"), terminal.getString("ip"), terminal.getInt("port"), terminal.getString("security-key")));
			
			rsp.setSucceed(true);
			rsp.setResult(terminal.toString());
			logoper(rsp.getMessage(), "远程管理", null);
			sendNotiefiesToSystemadmin(
					"远程管理",
					String.format("系统管理员[%s]"+rsp.getMessage(), getAccountName()),
                    "",
                    "",
                    null, null);
		}
		catch(Exception e)
		{
			String s = String.format("新增置堡垒机伺服器下SSH终端【%s】出现异常%s", sshid, e.getMessage());
			rsp.setMessage(s);
			logoper(rsp.getMessage(), "远程管理", e);
		}
		return rsp;		
	}

	/**
	 * 设置SSH终端
	 * @param parentId
	 * @param ip
	 * @param port
	 * @param user
	 * @param password
	 * @return
	 */
	public AjaxResult<String> setTerminal(
			int parentId,
			String ip,
			int port,
			String user,
			String password,
			String serverkey)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		String sshid = String.format("%s@%s:%s", user, ip, port);
		try
		{
			JSONObject terminal = null;
			JSONObject gateone = MonitorMgr.getInstance().getServer(parentId);
			if( gateone == null )
			{
				rsp.setMessage("未知堡垒机。");
				return rsp;
			}
			RunFetchMonitor monitor = MonitorMgr.getInstance().getMonitor().getRunFetchMonitor(gateone.getString("ip"), gateone.getInt("port"));
			if( monitor == null )
			{
				rsp.setMessage(String.format("堡垒机伺服器【%s:%s】未启动。",gateone.getString("ip"), gateone.getInt("port")));
				return rsp;
			}
			if( !monitor.isConnect() )
			{
				rsp.setMessage(String.format("堡垒机伺服器【%s:%s】监控连接不正常。",gateone.getString("ip"), gateone.getInt("port")));
				return rsp;
			}
			String serverid = monitor.getSysDesc().getServerid();
			if( serverid == null )
			{
				rsp.setMessage(String.format("堡垒机伺服器【%s:%s】唯一标识不存在。",gateone.getString("ip"), gateone.getInt("port")));
				return rsp;
			}
			
			monitor = MonitorMgr.getInstance().getMonitor().getRunFetchMonitor(ip);
			if( monitor == null )
			{
				rsp.setMessage(String.format("未知远程控制伺服器【%s】", ip));
				return rsp;
			}
//			String serverkey = monitor.getSysDesc()!=null?monitor.getSysDesc().getSecurityKey():null;
			if( serverkey == null )
			{
				rsp.setMessage(String.format("远程控制伺服器【%s】不存在，请检查伺服器是否上线。", ip));
				return rsp;
			}
			
			String zkpath = "/cos/config/gateone/"+serverid+"/"+sshid;
			Stat stat = ZKMgr.getZookeeper().exists(zkpath);
			if( stat == null ){
				rsp.setMessage("SSH终端【"+sshid+"】没有配置，无法设置密码。");
				return rsp;
			}
			terminal = ZKMgr.getZookeeper().getJSONObject(zkpath, true);
			terminal.put("password", password);
			terminal.put("security-key", serverkey);
			ZKMgr.getZookeeper().setJSONObject(zkpath, terminal, true);
			rsp.setMessage(String.format("成功设置了堡垒机伺服器【%s:%s】下SSH终端【%s】的密码与连接伺服器",
				gateone.getString("ip"), gateone.getInt("port"), sshid));
			rsp.setSucceed(true);
			rsp.setResult(terminal.toString());
			logoper(rsp.getMessage(), "远程管理", null);
			sendNotiefiesToSystemadmin(
					"远程管理",
					String.format("系统管理员[%s]"+rsp.getMessage(), getAccountName()),
                    "",
                    "",
                    null, null);
		}
		catch(Exception e)
		{
			String s = String.format("设置堡垒机伺服器下SSH终端【%s】的密码出现异常%s", sshid, e.getMessage());
			rsp.setMessage(s);
			logoper(rsp.getMessage(), "远程管理", e);
		}
		return rsp;		
	}
}
