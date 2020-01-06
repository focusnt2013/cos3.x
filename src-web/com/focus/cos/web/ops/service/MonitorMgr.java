package com.focus.cos.web.ops.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.json.JSONArray;
import org.json.JSONObject;

import com.focus.control.ModulePerf;
import com.focus.control.SystemPerf;
import com.focus.cos.control.Command;
import com.focus.cos.web.common.AjaxResult;
import com.focus.cos.web.common.Kit;
import com.focus.cos.web.common.PathFactory;
import com.focus.cos.web.common.ZKMgr;
import com.focus.cos.web.ops.service.Monitor.RunFetchMonitor;
import com.focus.cos.web.ops.vo.ModuleLog;
import com.focus.cos.web.ops.vo.ModuleTrack;
import com.focus.cos.web.ops.vo.MonitorServer;
import com.focus.cos.web.service.SvrMgr;
import com.focus.cos.web.user.service.RoleMgr;
import com.focus.util.IOHelper;
import com.focus.util.Tools;
import com.focus.util.Zookeeper;

public class MonitorMgr extends SvrMgr 
{
	public static final int STATE_INT = 0;//伺服器未启用
	public static final int STATE_RUN = 1;//伺服器工作正常
	public static final int STATE_WAR = 2;//伺服器工作有告警
	public static final int STATE_ERR = 3;//伺服器工作有错误
	private static final Log log = LogFactory.getLog(MonitorMgr.class);
	/*集群配置*/
	private JSONArray clusters;// = new JSONArray();
	/*集群分组ID，记录系统集群最大的分组ID，从/cos/config/monitor/cluster.id中获取数据，每次新增对该节点+1*/
	private int theClusterId = 0;
	/*监控主机对象*/
	private ArrayList<JSONObject> servers = null;//new ArrayList<JSONObject>();
	/*集群监控对象映射表*/
	private HashMap<Integer, JSONObject> map0 = null;
	/*集群监控对象映射表serverid : json */
	private HashMap<String, JSONObject> map1 = null;
	/*集群监控对象映射表serverkey : json */
	private HashMap<String, JSONObject> map3 = null;
	/*集群监控对象映射表*/
	private HashMap<String, Integer> map2 = null;
	/*集群伺服监控的状态映射表*/
	private HashMap<Integer, State> states = null;
	/*主控容器*/
	private Monitor monitor = null;
	/*本机监控*/
	private JSONObject local = new JSONObject();
	/*唯一实例句柄*/
	private static MonitorMgr Instance;

	/**
	 * 状态对象
	 * @author think
	 *
	 */
	class State{
		long timestamp = 0;
		int value = STATE_INT;
		long ts_cpuload_60;
		long ts_cpuload_85;
		long ts_memload_60;
		long ts_memload_85;
		
		public State(State o){
			this.value = o!=null?o.value:STATE_INT;
			this.ts_cpuload_60 = o!=null?o.ts_cpuload_60:0;
			this.ts_cpuload_85 = o!=null?o.ts_cpuload_85:0;
			this.ts_memload_60 = o!=null?o.ts_memload_60:0;
			this.ts_memload_85 = o!=null?o.ts_memload_85:0;
			this.timestamp = System.currentTimeMillis();
		}
		
		public String toString(){
			return String.format("状态：%s, 时间戳: %s", value, Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", timestamp));
		}
	}
	
	public MonitorMgr() throws Exception
	{
		log.info("Initialize the manager of monitor is "+ this.toString());
		if( Instance == null ){
			Instance = this;
			states = new HashMap<Integer, State>();
			map0 = new HashMap<Integer, JSONObject>();
			map1 = new HashMap<String, JSONObject>();
			map2 = new HashMap<String, Integer>();
			map3 = new HashMap<String, JSONObject>();
			servers = new ArrayList<JSONObject>();
			monitor = new Monitor()
			{
				public ArrayList<JSONObject> getClusterServers() 
				{
					return servers;
				}

				@Override
				public void setClusterServerState(RunFetchMonitor runner)
				{
					JSONObject server = map0.get(runner.getServerid());
					if( server == null )
					{
						return;
					}
					if( !runner.isConnect() )
					{
						server.put("stateinfo", "未监控连接");
						//setClusterState(clusters, runner.getClusterid(), "images/icons/gray.gif");
						states.put(runner.getServerid(), new State(null)); return;
					}
					if( runner.getSysDesc() == null )
					{
						server.put("stateinfo", "未能获得伺服器监控信息");
						states.put(runner.getServerid(), new State(null));return;
						//setClusterState(clusters, runner.getClusterid(), "images/icons/red.gif"); 
					}
					SystemPerf sysDesc = runner.getSysDesc();
					String myid = sysDesc.getPropertyValue("zookeeper.myid");
					server.put("zkmyid", myid);
					String serverid = sysDesc.getSecurityKey();
					if( serverid != null )
					{
						server.put("security-key", serverid);
						map1.put(serverid, server);
						map3.put(Tools.encodeMD5(serverid), server);
					}
					else
					{
						log.debug("Not found serverid from "+runner);
					}
					
					server.put("title", runner.getSysDesc().getDescript());
					if( runner.getServerid() != -1 )
					{
						if( local != null &&
							local.has("security-key") && 
							local.getString("security-key").equals(runner.getSysDesc().getSecurityKey()) )
						{
							map0.remove(-1);
							servers.remove(local);
							states.remove(-1);
							delMonitorRunner(local.getString("ip"), local.getInt("port"));
							local = null;
						}
					}
//					if( runner.getServerid() == 1021919 ){
//						System.err.println("[runner]["+runner.getServerid()+"] "+runner.getSysDesc()+" "+runner.toString()+"\r\n");
//					}
					if( runner.isGateone() ){
						server.put("gateone", true);
					}

					StringBuffer stateinfo = new StringBuffer();
					stateinfo.append("【伺服器状态】");
					stateinfo.append("\r\nCPU负载: "+runner.getSysDesc().getCpuLoadInfo());
					stateinfo.append("\r\n内存负载: "+runner.getSysDesc().getPhyMemUsageInof());
					stateinfo.append("\r\n存储空间: "+runner.getSysDesc().getDiskUsageInfo());
					State theState = new State(states.get(runner.getServerid()));
					int state = runner.getSysDesc().getCpuState(), state0 = 0;
					if(runner.getSysDesc().getCpuLoad() > 8500 )
					{
						theState.ts_cpuload_85 = theState.ts_cpuload_85==0?System.currentTimeMillis():theState.ts_cpuload_85;
					}
					else if(runner.getSysDesc().getCpuLoad() > 6000)
					{
						theState.ts_cpuload_85 = 0;
						theState.ts_cpuload_60 = theState.ts_cpuload_60==0?System.currentTimeMillis():theState.ts_cpuload_60;
					}
					else{
						theState.ts_cpuload_85 = 0;
						theState.ts_cpuload_60 = 0;
					}
					state0 = state;
					if(runner.getSysDesc().getPhyMemUsage() > 8500 )
					{
						theState.ts_memload_85 = theState.ts_memload_85==0?System.currentTimeMillis():theState.ts_memload_85;
					}
					else if(runner.getSysDesc().getPhyMemUsage() > 6000)
					{
						theState.ts_memload_85 = 0;
						theState.ts_memload_60 = theState.ts_memload_60==0?System.currentTimeMillis():theState.ts_memload_60;
					}
					else{
						theState.ts_memload_85 = 0;
						theState.ts_memload_60 = 0;
					}
					state = runner.getSysDesc().getMemoryState();
					state0 = state > state0  ? state : state0;

					state = runner.getSysDesc().getDiskState();
					state0 = state > state0  ? state : state0;

					state = runner.getSysDesc().getDatabaseState();
					if( state > state0 )
					{
						state0 = state;
						stateinfo.append("\r\n数据库有状况");
					}
					state = runner.getSysDesc().getZookeeperState();
					if( state > state0 )
					{
						state0 = state;
						ModuleTrack module = (ModuleTrack)runner.getSysDesc().getProperty("Zookeeper");
						if( module != null && module.getState() == 1 ){
							stateinfo.append("\r\nZookeeper启动于"+Tools.getFormatTime("yyyy-MM-dd HH:mm:ss", module.getStatupTime().getTime()));
						}
						else if( module == null ){
							stateinfo.append("\r\nZookeeper程序没有启动");
						}
						else{
							stateinfo.append("\r\nZookeeper工作不正常");
						}
					}
					state = runner.getSysDesc().getCoswsState();
					if( state > state0 )
					{
						state0 = state;
						stateinfo.append("\r\nCOS接口服务工作不正常【重要】");
					}
					state = runner.getSysDesc().getNetState();
					if( state > state0 )
					{
						state0 = state;
						stateinfo.append("\r\n网络有状况"+runner.getSysDesc().getNetLoad());
					}
					
					runner.getSysDesc().setProperty("DeadProgram", runner.getDeadModules().size());
					runner.getSysDesc().setProperty("ExceptionProgram", runner.getExceptionModules().size());
					state = runner.getSysDesc().getProgramState();
					if( state > state0 )
					{
						state0 = state;
						stateinfo.append("\r\n程序有状况"+runner.getSysDesc().getProgramInfo());
					}
					server.put("stateinfo", stateinfo.toString());
					theState.value = state0;
					states.put(runner.getServerid(), theState);
				}

				@Override
				public void releaseClusterServer(int id) {
					JSONObject delnode = null;
					if( map0.containsKey(id) )
					{
						delnode = map0.remove(id);
						if( delnode.getInt("id") == -1 )
						{
							return;
						}
						if( !delnode.has("ip") )
						{
							return;
						}
						servers.remove(delnode);
					}
					else
					{
						for(int i = 0; i < servers.size(); i++)
						{
							delnode = servers.get(i);
							if( delnode.getInt("id") == id )
							{
								servers.remove(i);
								break;
							}
						}
					}
					if( delnode != null && delnode.has("security-key") )
					{
						map1.remove(delnode.getString("security-key"));
						map3.remove(Tools.encodeMD5(delnode.getString("security-key")));
					}
					if( delnode != null )
					{
						map2.remove(delnode.getString("ip")+":"+delnode.getInt("port"));
						monitor.delMonitorRunner(delnode.getString("ip"), delnode.getInt("port"));
					}
					states.remove(id);
					etlClusterServer(clusters, id);
				}
			};
		}
	}

	private void etlClusterServer(JSONArray clusters, int id)
	{
		for(int i = 0; i < clusters.length(); i++)
		{
			JSONObject node = clusters.getJSONObject(i);
			if( node.has("children") )
			{
				JSONArray children = node.getJSONArray("children");
				etlClusterServer(children, id);
			}
			else if( node.has("ip") && id == node.getInt("id") )
			{
				clusters.remove(i);
				i -= 1;
			}
		}
	}
	
	/**
	 * 检查代理监听器是否存在，如果存在就添加
	 * @param proxyip
	 * @param controlport
	 * @param proxyid
	 */
	public synchronized void checkProxyMonitor()
	{
		StringBuffer logtxt = new StringBuffer();
		try
		{
			ZooKeeper zookeeper = ZKMgr.getZooKeeper();
			JSONObject parent = this.map0.get(0);
			if( parent == null  )
			{
				log.warn("Failed to check proxy for not found 0.");
				return;
			}
			JSONArray defaultclusters = null; 
			if( parent.has("children")) defaultclusters = parent.getJSONArray("children");
			else
			{
				defaultclusters = new JSONArray();
				parent.put("children", defaultclusters);
			}
			logtxt.append("Check the proxy");
			List<String> list = zookeeper.getChildren("/cos/data/apiproxy", true);
			for(String id : list)
			{
				String zkpath = "/cos/data/apiproxy/"+id;
				Stat stat = zookeeper.exists(zkpath, true);
				if( stat == null ) continue;
				byte[] payload = zookeeper.getData(zkpath, true, stat);
				JSONObject proxy = new JSONObject(new String(payload, "UTF-8"));
				int controlport = proxy.has("controlport")?proxy.getInt("controlport"):0;
				String proxyip = proxy.has("proxyip")?proxy.getString("proxyip"):"";
				String proxyid = proxy.has("id")?proxy.getString("id"):"";
				String realip = "";
				if( proxy.has("realip") ) realip = proxy.getString("realip");
				String remote = "";
				if( proxy.has("remote") ) remote = proxy.getString("remote");
				logtxt.append("\r\n\t"+proxyip+":"+controlport+" "+proxyid);
				if( controlport == 0 || proxyip.isEmpty() || proxyid.isEmpty() )
				{
					if( proxy.has("history") )	proxy.remove("history");
					logtxt.append("\tInvalid proxy"+proxy.toString());
					continue;
				}
				if( map1.containsKey(proxyid) )
				{
					JSONObject old = map1.get(proxyid);
					logtxt.append("\tFound exist("+old.getInt("id")+").");
					boolean isRealip = !realip.isEmpty()&&realip.equals(old.getString("ip"));
					if( isRealip ||
						(old.getString("ip").equals(proxyip) && realip.isEmpty()) || 
						(!remote.isEmpty() && old.getString("ip").equals(remote)) ||
						proxyip.equals("127.0.0.1") )
					{
						continue;
					}
					int oldid = old.getInt("id");
					if( oldid != -1 )
					{
						logtxt.append("\tReset the monitor("+old.getString("ip")+").");
						this.map0.remove(oldid);
						this.servers.remove(old);
						this.map1.remove(proxyid);
						this.map3.remove(Tools.encodeMD5(proxyid));
						map2.remove(old.getString("ip")+":"+old.getInt("port"));
						this.states.remove(oldid);
						this.monitor.delMonitorRunner(old.getString("ip"), old.getInt("port"));
						JSONObject parent1 = this.map0.get(old.getInt("pid"));
						if( parent1 != null  )
						{
							JSONArray children1 = null;
							if( parent1.has("children")) children1 = parent1.getJSONArray("children");
							if( children1 != null )
								for(int i = 0; i < children1.length(); i++)
								{
									old = children1.getJSONObject(i);
									if( old.getInt("id") == oldid )
									{
										children1.remove(i);
										break;
									}
								}
						}
					}
				}
				else if( map2.containsKey(proxyip+":"+controlport) )
				{
					logtxt.append("\tFound exist("+map2.get(proxyip+":"+controlport)+").");
					continue;
				}
				JSONObject server = new JSONObject();
				int cid = getNewClusterId();
				server.put("id", cid);
				server.put("pid", 0);
				server.put("security-key", proxyid);
				server.put("ip", realip.isEmpty()?proxyip:realip);
				server.put("port", controlport);
				defaultclusters.put(server);
				this.servers.add(server);
				map0.put(cid, server);
				map2.put(proxyip+":"+controlport, cid);
				map1.put(proxyid, server);
				map3.put(Tools.encodeMD5(proxyid), server);
				logtxt.append("\tCreate new server monitor("+cid+").");
			}
			ZKMgr.getZookeeper().setGZIPJSONArray("/cos/config/monitor/clusters", clusters);
		}
		catch (Exception e) 
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
			PrintStream ps = new PrintStream(out);
			e.printStackTrace(ps);
			logtxt.append("\r\nFailed to check proxy");
			logtxt.append(out.toString());
			ps.close();
		}
		log.info(logtxt);
	}
	/**
	 * 获得程序的各项监控信息
	 * @param ip
	 * @param port
	 * @return
	 */
	public SystemPerf getSystemPerf(String ip, int port, int id)
	{
		try{
			SystemPerf perf = this.monitor.getSystemPerf(ip, port);
			if( perf != null ){
				State state = states.get(id);
				if( state != null ){
					StringBuilder sb = new StringBuilder();
					if( state.ts_cpuload_85 > 0 ){
						sb.append(String.format("从[%s]负载持续超过85%%", Tools.getFormatTime("MM-dd HH:mm", state.ts_cpuload_85)));
					}
					if( state.ts_cpuload_60 > 0 ){
						if(sb.length()>0) sb.append(",");
						sb.append(String.format("从[%s]负载持续超过60%%", Tools.getFormatTime("MM-dd HH:mm", state.ts_cpuload_60)));
					}
					if( sb.length() == 0 ){
						sb.append("持续负载正常");
					}
					perf.setProperty("CpuLoadInfo", sb.toString());
					sb = new StringBuilder();
					if( state.ts_memload_85 > 0 ){
						sb.append(String.format("从[%s]负载持续超过85%%", Tools.getFormatTime("MM-dd HH:mm", state.ts_memload_85)));
					}
					if( state.ts_memload_60 > 0 ){
						if(sb.length()>0) sb.append(",");
						sb.append(String.format("从[%s]负载持续超过60%%", Tools.getFormatTime("MM-dd HH:mm", state.ts_memload_60)));
					}
					if( sb.length() == 0 ){
						sb.append("持续负载正常");
					}
					perf.setProperty("MemLoadInfo", sb.toString());
				}
				else{
					perf.setProperty("CpuLoadInfo", "状态信息不存在");
					perf.setProperty("MemLoadInfo", "状态信息不存在");
				}
			}
			return perf;
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * 刷新集群的状态
	 * @param rootid 从那个节点开始
	 * @param timestamp
	 * @return
	 */
	public synchronized AjaxResult<String> getClusterStates(int rootid, long timestamp)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		try
		{
			long ts = System.currentTimeMillis();
			JSONObject privileges = getClusterPrivileges(super.getAccountRole(), super.getAccountName());
			JSONArray clusters = this.getClusterTree(privileges, super.getAccountRole()==1, false, -1);
			JSONArray flush = new JSONArray();
			this.setClusterState(clusters, flush, timestamp);
			rsp.setResult(flush.toString());
//			System.err.println(flush.toString(4));
			rsp.setSucceed(true);
			ts = System.currentTimeMillis() - ts;
			rsp.setTimestamp(System.currentTimeMillis());
//			log.info("Get the states of cluster "+ts+" ms.");
		}
		catch(Exception e)
		{
			e.printStackTrace();
			String s = "刷新集群状态出现异常"+e;
			rsp.setMessage(s);
			log.error("Failed to flush the state of cluster for exception", e);
		}
		return rsp;
	}

	/**
	 * 刷新伺服器的状态
	 * @param rootid
	 * @return
	 */
	public synchronized AjaxResult<String> getServerStates(int rootid)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		try
		{
			JSONObject privileges = getClusterPrivileges(super.getAccountRole(), super.getAccountName());
			JSONArray clusters = this.getClusterTree(privileges, super.getAccountRole()==1, false, rootid);
			JSONArray flush = new JSONArray();
			this.setServerState(clusters, flush);
			rsp.setResult(flush.toString());
			rsp.setSucceed(true);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			String s = "刷新集群伺服器状态出现异常"+e;
			rsp.setMessage(s);
			log.error("Failed to flush the state of cluster for exception", e);
		}
		return rsp;
	}
	/**
	 * 获取服务器状态
	 * @param serverid
	 * @return
	 */
	public int getState(int serverid)
	{
		if( !states.containsKey(serverid) ) return STATE_INT;
		return states.get(serverid).value;
	}

	/**
	 * 
	 * @return
	 */
	public static MonitorMgr getInstance()
	{
		if( Instance == null ) return null;
//		log.debug("Get the instance of monitor-manager "+Instance.monitor.toString());
		return Instance;
	}
	/**
	 * 
	 * @return
	 */
	public static Monitor getTracker()
	{
		if( Instance == null ) return null;
		return Instance.getMonitor();
	}
	
	/**
	 * 新增集群目录
	 * @param parentId
	 * @param name
	 * @param timestamp
	 * @return
	 */
	public AjaxResult<String> addCluster(
			int parentId,
			String name,//集群名称
			long timestamp)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		try
		{
			JSONObject cluster = new JSONObject();
			int id = getNewClusterId();
			if( id == -1  )
			{
				rsp.setMessage("分配新的集群ID失败，请联系您的系统管理员.");
				return rsp;
			}
			
			cluster.put("id", id);
			cluster.put("pid", parentId);
			cluster.put("name", name);
			cluster.put("isParent", true);
			JSONArray children = clusters;
			JSONObject parent = this.map0.get(parentId);
			if( parent != null  )
			{
				if( parent.has("children"))
				{
					children = parent.getJSONArray("children");
				}
				else
				{
					children = new JSONArray();
					parent.put("children", children);
				}
			}
			for( int i = 0; i < children.length(); i++){
				JSONObject e= children.getJSONObject(i);
				if( !e.has("ip") && name.equals(e.getString("name")) ){
					rsp.setMessage(String.format("在相同目录下有相同的集群名称【%s】,ID是%s", name, e.getInt("id")));
					rsp.setResult(e.toString());
					return rsp;
				}
			}
			children.put(cluster);
			map0.put(id, cluster);
			ZKMgr.getZookeeper().setGZIPJSONArray("/cos/config/monitor/clusters", clusters);
			rsp.setMessage("成功新增集群分组【"+name+"】。");
			rsp.setSucceed(true);
			rsp.setResult(cluster.toString());
			logoper(rsp.getMessage(), "监控管理", "", "monitor!cluster.action?id="+id);
			sendNotiefiesToSystemadmin(
					"监控配置",
					String.format("系统管理员[%s]"+rsp.getMessage(), getAccountName()),
                    "该集群配置的伺服器如下表所示：",
                    "monitor!cluster.action?id="+id,
                    null, null);
		}
		catch(Exception e)
		{
			String s = "新增集群分组【"+name+"】出现异常"+e;
			rsp.setMessage(s);
			logoper(rsp.getMessage(), "监控管理", e);
//			log.error("Failed to drag&drop the menu of modules for exception", e);
		}
		return rsp;		
	}
	
	/**
	 * 集群分组名称修改
	 * @param parentId
	 * @param name
	 * @param timestamp
	 * @return
	 */
	public synchronized AjaxResult<String> renameCluster(
			int parentId,
			String name,//集群名称
			long timestamp)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		try
		{
			JSONObject renamer = this.map0.get(parentId);
			if( renamer == null  )
			{
				rsp.setMessage("未找到您要删除的集群");
				return rsp;
			}
			if( renamer.getString("name").equals(name) )
			{
				rsp.setMessage("提交的名称与原名称一致");
				return rsp;
			}
			rsp.setMessage("成功重命名集群分组【"+renamer.getString("name")+"】名称为【"+name+"】。");
			renamer.put("name", name);
			ZKMgr.getZookeeper().setGZIPJSONArray("/cos/config/monitor/clusters", clusters);
			rsp.setSucceed(true);
			logoper(rsp.getMessage(), "监控管理", "", "monitor!cluster.action?id="+parentId);
			sendNotiefiesToSystemadmin(
					"监控配置",
					String.format("系统管理员[%s]"+rsp.getMessage(), getAccountName()),
                    "该集群父节点配置的伺服器如下表所示：",
                    "monitor!cluster.action?id="+parentId,
                    null, null);
		}
		catch(Exception e)
		{
			String s = "重命名集群分组出现异常"+e;
			rsp.setMessage(s);
			logoper(rsp.getMessage(), "监控管理", e);
			log.error("Failed to drag&drop the menu of modules for exception", e);
		}
		return rsp;		
	}
	
	/**
	 * 删除指定伺服器
	 * @param parentId
	 * @param id
	 * @param delserver
	 * @param timestamp
	 * @return
	 */
	public synchronized AjaxResult<String> delServer(
			int id,
			int rootid,
			long timestamp)
	{

		AjaxResult<String> rsp = new AjaxResult<String>();
		try
		{
			if( !map0.containsKey(id) )
			{
				rsp.setMessage("未找到您要删除的伺服器");
				return rsp;
			}
			JSONObject delnode = this.map0.get(id);
			if( delnode == null )
			{
				rsp.setMessage("未找到您要删除的伺服器");
				return rsp;
			}
			if( !delnode.has("ip") )
			{
				rsp.setMessage("您要删除的不是伺服器节点");
				return rsp;
			}
			if( delnode.getInt("id") == -1 )
			{
				rsp.setMessage("缺省伺服器不允许删除");
				return rsp;
			}
			delnode = this.map0.remove(id);
			this.servers.remove(delnode);
			Zookeeper zookeeper = ZKMgr.getZookeeper();
			if( delnode != null ) log.info("Delete the monitor of server "+delnode.toString(4));
			if( delnode != null && delnode.has("security-key") )
			{
				String md5 = Tools.encodeMD5(delnode.getString("security-key"));
				this.map1.remove(delnode.getString("security-key"));
				log.info(String.format("Delete the map(%s).", delnode.getString("security-key")));
				this.map3.remove(md5);
				log.info(String.format("Delete the map(%s).", md5));
				zookeeper.delete("/cos/data/apiproxy/"+md5);
				log.info(String.format("Succeed to  delete the apiproxy(key:%s, md5:%s).", delnode.getString("security-key"), md5));
			}
			List<String> list = ZKMgr.getZookeeper().getChildren("/cos/data/apiproxy", true);
			String tag0 = delnode.getString("ip")+":"+delnode.getInt("port");
			for(String apiproxy : list)
			{
				String zkpath = "/cos/data/apiproxy/"+apiproxy;
				Stat stat = zookeeper.exists(zkpath, true);
				if( stat == null ) continue;
				byte[] payload = zookeeper.getData(zkpath, true, stat);
				JSONObject proxy = new JSONObject(new String(payload, "UTF-8"));
				int controlport = proxy.has("controlport")?proxy.getInt("controlport"):0;
				String proxyip = proxy.has("proxyip")?proxy.getString("proxyip"):"";
				String proxyid = proxy.has("id")?proxy.getString("id"):"";
				String tag1 = proxyip+":"+controlport;
				if( tag0.equals(tag1) ){
					String md5 = Tools.encodeMD5(proxyid);
					if( zookeeper.delete("/cos/data/apiproxy/"+md5) ){
						log.info(String.format("Succeed to delete the apiproxy(%s) for useroper.", proxyid));
					}
					break;
				}
			}
			Object ret = map2.remove(tag0);
			log.info(String.format("Remove the map to %s from %s.", ret, tag0));
			ret = this.states.remove(id);
			log.info(String.format("Remove the state(server-id:%s, info:%s).", id, ret));
			this.monitor.delMonitorRunner(delnode.getString("ip"), delnode.getInt("port"));
			log.info(String.format("Delete the monitor(addr:%s).", tag0));
			JSONObject parent = this.map0.get(delnode.getInt("pid"));
			if( parent != null  )
			{
				JSONArray children = null;
				if( parent.has("children")) children = parent.getJSONArray("children");
				if( children != null )
					for(int i = 0; i < children.length(); i++)
					{
						delnode = children.getJSONObject(i);
						if( delnode.getInt("id") == id )
						{
							children.remove(i);
							log.info(String.format("Remove the node from parent %s", parent.toString(4)));
							break;
						}
					}
			}
			rsp.setMessage("成功删除伺服器【"+delnode.getString("ip")+":"+delnode.getInt("port")+"】。");
			zookeeper.setGZIPJSONArray("/cos/config/monitor/clusters", clusters);
			rsp.setSucceed(true);
			JSONObject privileges = getClusterPrivileges(super.getAccountRole(), super.getAccountName());
			JSONArray clusters = getClusterTree(privileges, super.getAccountRole()==1, false, rootid);
			if( clusters == null )
			{
				clusters = new JSONArray();
			}
			else
			{
				setClusterState(clusters);
			}
			rsp.setResult(clusters.toString());
			logoper(rsp.getMessage(), "监控管理", "", "monitor!cluster.action?id="+delnode.getInt("pid"));
			sendNotiefiesToSystemadmin(
					"监控配置",
					String.format("系统管理员[%s]"+rsp.getMessage(), getAccountName()),
                    "该伺服器父节点配置的伺服器如下表所示：",
                    "monitor!cluster.action?id="+delnode.getInt("pid"),
                    null, null);
		}
		catch(Exception e)
		{
			String s = "移除伺服器出现异常"+e;
			rsp.setMessage(s);
			logoper(rsp.getMessage(), "监控管理", e);
			log.error("Failed to del the node of monitor for exception", e);
		}
		return rsp;	
	}

	/**
	 * 删除集群
	 * @param id
	 * @param timestamp
	 * @return
	 */
	public synchronized AjaxResult<String> delCluster(
			int id,
			boolean delserver,
			int rootid,
			long timestamp)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		try
		{
			if( !map0.containsKey(id) )
			{
				rsp.setMessage("未找到您要删除的集群");
				return rsp;
			}

			JSONObject delnode = this.map0.get(id);
			if( delnode == null )
			{
				rsp.setMessage("未找到您要删除的集群");
				return rsp;
			}
			if( delnode.has("ip") )
			{
				rsp.setMessage("您要删除的不是集群节点");
				return rsp;
			}
			if( delnode.getInt("id") == 0 )
			{
				rsp.setMessage("缺省集群不允许删除");
				return rsp;
			}
			delnode = this.map0.remove(id);
			JSONArray children = null;
			JSONObject parent = this.map0.get(delnode.getInt("pid"));
//			if( parent != null  )
//			{
				if( parent != null && parent.has("children")){
					children = parent.getJSONArray("children");
				}
				else{
					children = clusters;
				}
				if( children != null )
					for(int i = 0; i < children.length(); i++)
					{
						delnode = children.getJSONObject(i);
						if( delnode.getInt("id") == id )
						{
							children.remove(i);
							break;
						}
					}
//			}
			//因为指定集群或伺服器被删除了，将集群下的服务器挪移到这个列表中
			ArrayList<JSONObject> all = new ArrayList<JSONObject>();
			this.setClusterNodes(all, delnode);
			if( delserver )
			{//如果是删除标识
				for(JSONObject cluster : all)
				{
					map0.remove(cluster.getInt("id"));
					this.servers.remove(cluster);
					if( cluster.has("ip") ){
						map2.remove(cluster.getString("ip")+":"+cluster.getInt("port"));
						if( cluster.has("security-key") ){
							map3.remove(Tools.encodeMD5(cluster.getString("security-key")));
						}
						this.monitor.delMonitorRunner(cluster.getString("ip"), cluster.getInt("port"));
					}
				}
				rsp.setMessage("成功删除集群分组【"+delnode.getString("name")+"】，同时该集群下所有伺服器都被删除。");
			}
			else
			{//如果不要求删除集群下的伺服器，而是将它挪移到上级集群或者缺省集群下
				if( parent == null )
				{
					parent = map0.get(0);//获取缺省集群
					rsp.setMessage("成功移除集群分组【"+delnode.getString("name")+"】，同时该集群下所有伺服器都移动到缺省集群。");
				}
				else
				{
					rsp.setMessage("成功移除集群分组【"+delnode.getString("name")+"】，同时该集群下所有伺服器都被移动到集群分组【"+parent.getString("name")+"】。");
				}
				if( parent.has("children")) children = parent.getJSONArray("children");
				else {
					children = new JSONArray();
					parent.put("children", children);
				}
				for(JSONObject cluster : all)
				{
					if( cluster.has("ip") ){
						cluster.put("pid", parent.getInt("id"));
						children.put(cluster);
					}
				}
			}
//			System.err.println(clusters.toString(4));
			ZKMgr.getZookeeper().setGZIPJSONArray("/cos/config/monitor/clusters", clusters);
			rsp.setSucceed(true);
			JSONObject privileges = getClusterPrivileges(super.getAccountRole(), super.getAccountName());
			JSONArray clusters = getClusterTree(privileges, super.getAccountRole()==1, false, rootid);
			if( clusters == null )
			{
				clusters = new JSONArray();
			}
			else
			{
				setClusterState(clusters);
			}
			rsp.setResult(clusters.toString());
			logoper(rsp.getMessage(), "监控管理", "", "monitor!cluster.action?id="+delnode.getInt("pid"));
			sendNotiefiesToSystemadmin(
					"监控配置",
					String.format("系统管理员[%s]"+rsp.getMessage(), getAccountName()),
                    "该集群父节点配置的伺服器如下表所示：",
                    "monitor!cluster.action?id="+delnode.getInt("pid"),
                    null, null);
		}
		catch(Exception e)
		{
			String s = "移除集群分组出现异常"+e;
			rsp.setMessage(s);
			logoper(rsp.getMessage(), "监控管理", e);
			log.error("Failed to drag&drop the menu of modules for exception", e);
		}
		return rsp;		
	}
	/**
	 * 新增集群目录
	 * @param parentId
	 * @param ip 伺服器的IP地址
	 * @param port 伺服器端口
	 * @param synchmode 监控数据同步模式
	 * @param timestamp
	 * @return
	 */
	public AjaxResult<String> addServer(
			int parentId,
			String ip,
			int port,
			String synchmode,
			long timestamp)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		try
		{
			String serveraddr = ip+":"+port;
			JSONObject server = null;
			RunFetchMonitor runner = this.monitor.getRunFetchMonitor(ip, port);
			if( runner != null )
			{
				rsp.setMessage("集群伺服器监控【"+serveraddr+"】已经存在，无需再次添加。");
				server = map0.get(runner.getServerid());
				rsp.setResult(server.toString());
				return rsp;
			}
			JSONObject parent = this.map0.get(parentId);
			if( parent == null  )
			{
				rsp.setMessage("未找到您要新增伺服器监控的集群分组。");
				return rsp;
			}
			else{
				server = map0.get(parentId);
				if( server.has("ip") ){
					rsp.setMessage(String.format("您不能将伺服器模块添加到另外一个伺服器(%s:%s)模块下。", server.getString("ip"), server.getInt("port")));
					return rsp;
				}
			}
			
			if( map2.containsKey(serveraddr)){
				rsp.setMessage("要添加的集群伺服器配置已经存在。");
				rsp.setSucceed(true);
				server = new JSONObject();
				server.put("id", map2.get(serveraddr));
				rsp.setResult(server.toString());
				log.warn("Fond error from the data of map2:"+map2.toString());
				return rsp;
			}
			int id = getNewClusterId();
			if( id == -1  )
			{
				rsp.setMessage("分配新的集群ID失败，请联系您的系统管理员.");
				return rsp;
			}
			server = new JSONObject();
			server.put("id", id);
			server.put("pid", parent.getInt("id"));
			server.put("ip", ip);
			server.put("port", port);
			server.put("synchmode", synchmode);
			JSONArray children = null;
			if( parent.has("children")) children = parent.getJSONArray("children");
			else
			{
				children = new JSONArray();
				parent.put("children", children);
			}
			children.put(server);
			this.servers.add(server);
			map0.put(id, server);
			map2.put(ip+":"+port, id);
			ZKMgr.getZookeeper().setGZIPJSONArray("/cos/config/monitor/clusters", clusters);
			rsp.setMessage("成功在集群分组【"+parent.getString("name")+"】下新增伺服器监控【"+ip+":"+port+"】。");
			rsp.setSucceed(true);
			JSONObject newserver = new JSONObject(server.toString());
			newserver.put("name", server.getString("ip"));
			newserver.put("icon", "images/icons/cluster.png?v=1");
			rsp.setResult(newserver.toString());
			logoper(rsp.getMessage(), "监控管理", "", "monitor!server.action?id="+id);
			sendNotiefiesToSystemadmin(
					"监控配置",
					String.format("系统管理员[%s]"+rsp.getMessage(), getAccountName()),
                    "",
                    "monitor!server.action?id="+id,
                    null, null);
		}
		catch(Exception e)
		{
			log.error("Failed to drag&drop the menu of modules for exception", e);
			String s = "新增伺服器监控出现异常"+e;
			rsp.setMessage(s);
			logoper(rsp.getMessage(), "监控管理", e);
		}
		return rsp;		
	}

	/**
	 * 移动集群节点
	 * @param type
	 * @param pathSource
	 * @param pathTarget
	 * @param timestamp
	 * @param moduleid
	 * @return
	 */
	public synchronized AjaxResult<Integer> dragDropCluster(
			String type,
			int sourceId,
			int targetId,//可能是服务器节点，可能是集群节点，根据type来决定
			//int targetPid,//只能是集群节点，未负数表示根节点
			long timestamp)
	{
		AjaxResult<Integer> rsp = new AjaxResult<Integer>();
		try
		{
			JSONObject source = this.map0.get(sourceId);
			if( source == null  )
			{
				rsp.setMessage("未找到您要移动的集群分组或伺服器监控节点。");
				return rsp;
			}
			JSONObject target = this.map0.get(targetId);
			if( target == null  )
			{
				rsp.setMessage("未找到您要移动的目标集群分组或伺服器监控节点。");
				return rsp;
			}
			if( target.has("ip") && "inner".equals(type) )
			{
				rsp.setMessage("不能将集群节点移动到伺服器节点下。");
				return rsp;
			}

			JSONArray childrenSource = getClusterChildren(source.getInt("pid"));
			JSONArray childrenTarget = null;
			if( "inner".equals(type) )
			{
				childrenTarget = getClusterChildren(target.getInt("id"));
			}
			else
			{
				childrenTarget = getClusterChildren(target.getInt("pid"));
			}
			if( childrenTarget == this.clusters && source.has("ip") )
			{//根节点操作
				rsp.setMessage("伺服器监控不能拖拽到根目录下。");
				return rsp;
			}
			drag(childrenSource, source.getInt("id"));
			String sourceName = "";
			if( source.has("ip") )
			{
				sourceName = "伺服器【"+source.getString("ip")+":"+source.getInt("port")+"】";
			}
			else
			{
				sourceName = "集群分组【"+source.getString("name")+"】";
			}
			String targetName = "";
			if( target.has("ip") )
			{
				targetName = "伺服器【"+target.getString("ip")+":"+target.getInt("port")+"】";
			}
			else
			{
				targetName = "集群分组【"+target.getString("name")+"】";
			}

			if( "inner".equals(type) )
			{
				childrenTarget.put(source);
				source.put("pid", target.getInt("id"));
				rsp.setMessage("成功将"+sourceName+"移动到了"+targetName+"。");
				rsp.setResult(target.getInt("id"));
			}
			else
			{
				JSONArray children1 = new JSONArray();
				if( "prev".equals(type) )
				{
					for(int i = 0; i < childrenTarget.length(); i++)
					{
						JSONObject child = childrenTarget.getJSONObject(i);
						if( child.getInt("id") == target.getInt("id") )
						{
	        				children1.put(source);
						}
	        			children1.put(child);
					}
				}
				else if( "next".equals(type) )
				{
					for(int i = 0; i < childrenTarget.length(); i++)
					{
						JSONObject child = childrenTarget.getJSONObject(i);
	        			children1.put(child);
						if( child.getInt("id") == target.getInt("id") )
						{
	        				children1.put(source);
						}
					}
				}
				else
				{
					throw new Exception("未知移动类型"+type);
				}
				JSONObject node = this.map0.get(target.getInt("pid"));
				if( node == null  )
				{//根节点
					this.clusters = children1;
				}
				else
				{
					node.put("children", children1);
				}
				source.put("pid", target.getInt("pid"));
				rsp.setMessage("成功将"+sourceName+"移动到了"+targetName+"旁边。");
				rsp.setResult(target.getInt("pid"));
			}
			ZKMgr.getZookeeper().setGZIPJSONArray("/cos/config/monitor/clusters", clusters);
//			System.err.println(clusters.toString(4));
			rsp.setSucceed(true);
			String url = source.has("ip")?("monitor!server.action?id="+source.getInt("id")):("monitor!cluster.action?id="+source.getInt("id"));
			logoper(rsp.getMessage(), "监控管理", "", url);
			sendNotiefiesToSystemadmin(
					"监控配置",
					String.format("系统管理员[%s]"+rsp.getMessage(), getAccountName()),
                    "",
                    url,
                    null, null);
		}
		catch(Exception e)
		{
			String s = "移动集群分组或伺服器位置出现异常"+e;
			rsp.setMessage(s);
			logoper(rsp.getMessage(), "监控管理", e);
			log.error("Failed to drag&drop the menu of modules for exception", e);
		}
		return rsp;		
	}
	
	private void drag(JSONArray children, int id)
	{
		for(int i = 0; i < children.length(); i++)
		{
			JSONObject child = children.getJSONObject(i);
			if( child.getInt("id") == id )
			{
				children.remove(i);
				break;
			}
		}
	}

	
	/**
	 * 得到集群子节点
	 * @param id
	 * @return
	 * @throws Exception
	 */
	private JSONArray getClusterChildren(int id) throws Exception
	{
		JSONArray children = null;
		JSONObject node = this.map0.get(id);
		if( node == null  )
		{//根节点
			children = clusters;
		}
		else
		{//是某个集群
			if( !node.has("isParent") || !node.getBoolean("isParent") )
			{
				throw new Exception("得到的节点不是集群分组。");
			}
			if( node.has("children")) children = node.getJSONArray("children");
			else
			{
				children = new JSONArray();
				node.put("children", children);
			}
		}
		return children;
	}
	/**
	 * 得到监控服务器的列表
	 * @param clusterid
	 * @return
	 */
	public AjaxResult<String> getMonitorServers(int clusterid)
	{
		AjaxResult<String> response = new AjaxResult<String>();
		ArrayList<MonitorServer> monitorServers = this.getClusterMonitorServers(clusterid, super.getAccountRole());
		int c1 = 0, c0 = 0;
		for( MonitorServer server : monitorServers )
		{
            if( !"N/A".equals(server.getRunState()) )
			{
            	String args[] = Tools.split(server.getRunState(), "/");
				if( args.length == 2 )
				{
					if( Tools.isNumeric(args[0]) ) c0 += Integer.parseInt(args[0]);
					if( Tools.isNumeric(args[1]) ) c1 += Integer.parseInt(args[1]);
				}
			}
            response.add(server);
		}
		response.setResult(c0+"/"+c1);
		return response;
	}
	
	/**
	 * 得到Zookeeper的集群配置数据
	 * @param serverid
	 * @return
	 */
	public AjaxResult<String> getZookeeperConfig(int serverid)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		JSONObject node = this.map0.get(serverid);
		if( node == null )
		{
			rsp.setMessage("未找到对应的伺服器数据。");
			return rsp;	
		}
		RunFetchMonitor runner = this.monitor.getRunFetchMonitor(node.getString("ip"), node.getInt("port"));
		if( runner == null || runner.getSysDesc() == null )
		{
			rsp.setMessage("伺服器未监控连接。");
			return rsp;	
		}

		String[] servers = Tools.split(runner.getSysDesc().getPropertyValue("zookeeper.servers"), ",");
		StringBuffer sb = new StringBuffer();
		for( int i = 0; i < servers.length; i++ )
		{
			String ip = servers[i];
			if( ip.equals(node.getString("ip")) )
			{
				sb.append("<span style='color:red;font-weight:bold;'>");
				sb.append(ip);
				sb.append("(myid="+(i)+")");
				sb.append("</span>");
			}
			else
			{
				sb.append("<span>");
				sb.append(ip);
				sb.append("(myid="+(i)+")");
				sb.append("</span>");
			}
			sb.append("<br>");
		}
		rsp.setResult(sb.toString());
		rsp.setSucceed(true);
		return rsp;
	}
	/**
	 * 重置指定伺服器的监控
	 * @param serverid
	 * @return
	 */
	public AjaxResult<String> resetMonitor(int serverid)
	{
		AjaxResult<String> rsp = new AjaxResult<String>();
		JSONObject node = this.map0.get(serverid);
		if( node == null )
		{
			rsp.setMessage("未找到对应的伺服器数据。");
			return rsp;	
		}
		RunFetchMonitor runner = this.monitor.getRunFetchMonitor(node.getString("ip"), node.getInt("port"));
		if( runner == null || runner.getSysDesc() == null )
		{
			rsp.setMessage("伺服器未监控连接。");
			return rsp;	
		}
		runner.disconnect();
		rsp.setSucceed(true);
		return rsp;
	}

	/**
	 * 设置集群的状态到一个映射表返回给navigate页面用于刷新状态和名字
	 * @param cluster
	 * @param id
	 */
	private void setServerState(JSONArray clusters, JSONArray flush)
	{
		for(int i = 0; i < clusters.length(); i++)
		{
			Integer state = -1;
			JSONObject node = clusters.getJSONObject(i);
			if( !node.has("isParent") || !node.getBoolean("isParent") )
			{
				if( this.states.containsKey(node.getInt("id")))
				{
					state = this.states.get(node.getInt("id")).value;
					state = state!=null?state:0;
					node.put("state", state);
					node.put("dropRoot", false);
					node.put("dropInner", false);
					flush.put(node);
				}
			}
			else
			{
				if( node.has("children") )
				{
					JSONArray children = node.getJSONArray("children");
					setServerState(children, flush);
				}
			}
		}
	}	
	/**
	 * 设置集群的状态到一个映射表返回给navigate页面用于刷新状态和名字
	 * @param cluster
	 * @param id
	 */
	private int setClusterState(JSONArray clusters, JSONArray flush, long timestamp)
	{
		int upstate = -1;
		for(int i = 0; i < clusters.length(); i++)
		{
			Integer state = -1;
			JSONObject node = clusters.getJSONObject(i);
			if( !node.has("isParent") || !node.getBoolean("isParent") )
			{
				if( this.states.containsKey(node.getInt("id")))
				{
					State stat = this.states.get(node.getInt("id"));
					if( stat.timestamp >= timestamp ){
						state = stat.value;
						state = state!=null?state:0;
						node.put("state", state);
						node.put("dropRoot", false);
						node.put("dropInner", false);
						flush.put(node);
					}
				}
				else{
					if( timestamp == 0 ){
						state = STATE_INT;
						node.put("state", state);
						node.put("dropRoot", false);
						node.put("dropInner", false);
						flush.put(node);
					}
				}
				if( state > STATE_INT ){
					RunFetchMonitor runner = this.monitor.getRunFetchMonitor(node.getString("ip"), node.getInt("port"));
					if( runner == null || !runner.isConnect()  ){
						state = STATE_INT;
						node.put("state", state);
					}
				}
			}
			else
			{
				if( node.has("children") )
				{
					JSONArray children = node.getJSONArray("children");
					state = setClusterState(children, flush, timestamp);
				}
				if( state != -1 ){
					JSONObject cluster = new JSONObject();
					cluster.put("id", node.getInt("id"));
					cluster.put("name", node.getString("name"));
					cluster.put("state", state);
					flush.put(cluster);
				}
			}
			upstate = state>upstate?state:upstate;
		}
		return upstate;
	}

	/**
	 * 设置集群伺服的工作状态
	 * @param clusters
	 * @return
	 */
	public int setClusterState(JSONArray clusters)
	{
		int upstate = -1;
		for(int i = 0; i < clusters.length(); i++)
		{
			Integer state = -1;
			JSONObject node = clusters.getJSONObject(i);
			if( !node.has("isParent") || !node.getBoolean("isParent") )
			{
				if( states.containsKey(node.getInt("id"))){
					state = this.states.get(node.getInt("id")).value;
				}
				else{
					state = STATE_INT;
				}
				if( state > STATE_INT ){
					RunFetchMonitor runner = this.monitor.getRunFetchMonitor(node.getString("ip"), node.getInt("port"));
					if( runner == null || !runner.isConnect() ){
						state = STATE_INT;
						node.put("state", state);
					}
					if( runner != null && runner.isGateone() ){
						node.put("icon", "images/icons/ssh.png");
					}
				}
//				if( state == STATE_ERR ){
//					System.err.println(node.toString(4));
//				}
			}
			else
			{
				if( node.has("children") )
				{
					JSONArray children = node.getJSONArray("children");
					state = setClusterState(children);
				}
			}
			if( state != null )	node.put("state", state);
			upstate = state!=null&&state>upstate?state:upstate;
		}
		return upstate;
		
	}
	/**
	 * 设置集群下所有服务器
	 * @param servers
	 * @param cluster
	 */
	public void setClusterNodes(ArrayList<JSONObject> all, JSONObject cluster)
	{
		if( !cluster.has("children")) return;
		JSONArray children = cluster.getJSONArray("children");
		for(int i = 0; i < children.length(); i++)
		{
			JSONObject child = children.getJSONObject(i);
			all.add(child);
			if( child.has("isParent") && child.getBoolean("isParent") )
			{
				this.setClusterNodes(all, child);
			}
		}
	}
	
	/**
	 * 得到集群的权限配置
	 * @param roleid
	 * @return
	 * @throws Exception
	 */

	public JSONObject getClusterPrivileges(int roleid) throws Exception
	{
		JSONObject privileges = RoleMgr.getRolePrivileges(roleid, null);
		if( privileges != null )
		{
			privileges = privileges.has("##cluster")?privileges.getJSONObject("##cluster"):new JSONObject();
		}
		return privileges;
	}
	public JSONObject getClusterPrivileges(int roleid, String account) throws Exception
	{
		JSONObject privileges = RoleMgr.getRolePrivileges(roleid, account);
		if( privileges != null )
		{
			privileges = privileges.has("##cluster")?privileges.getJSONObject("##cluster"):new JSONObject();
		}
		return privileges;
	}

	/**
	 * 得到指定伺服器的权限
	 * @param roleid
	 * @param serverid
	 * @return
	 * @throws Exception
	 */
	public JSONObject getServerPrivileges(int roleid, String account, JSONObject server) throws Exception
	{
		String serverid = server!=null&&server.has("security-key")?server.getString("security-key"):"";
		return getServerPrivileges(roleid, account, serverid);
	}
	public JSONObject getServerPrivileges(int roleid, String account, String serverid) throws Exception
	{
		JSONObject privileges = this.getClusterPrivileges(roleid, account);
		if( privileges != null && privileges.has(serverid) )
		{
			return privileges.getJSONObject(serverid);
		}
		return new JSONObject();
		
	}
	
	/**
	 * 得到监控服务器的列表
	 * @param clusterid
	 * @return
	 * @throws Exception 
	 */
	public ArrayList<MonitorServer> getClusterMonitorServers(int clusterid, int roleid)
	{
		try {
			ArrayList<JSONObject> servers = this.getServers();
			HashMap<Integer, JSONObject> filter = new HashMap<Integer, JSONObject>();
			JSONObject privileges = getClusterPrivileges(super.getAccountRole(), super.getAccountName());
			this.setClusterFilter(filter, this.getCluster(clusterid));
			return this.getMonitor().getMonitorServers(servers, filter, privileges, roleid==1);
		} catch (Exception e) {
			log.error("Failed to get all monitors from cluster for ", e);
			return null;
		}
//		StringBuffer sb = new StringBuffer("Get the instance of monitor-manager from "+this.monitor.toString());
//		for(MonitorServer server : monitorServers)
//		{
//			sb.append("\r\n\t");
//			sb.append(server.getId());
//			sb.append(server.getName());
//		}
//		log.debug(sb.toString());
	}
	/**
	 * 加载监控主机配置表
	 * @param hosts
	 * @return
	 * @throws Exception 
	 */
	public ArrayList<JSONObject> getServers() throws Exception
	{
		setConfig();
		return servers;
	}

	/**
	 * 设置集群树数据
	 * @param array
	 * @param role
	 * @param sysadmin
	 * @return
	 */
	private JSONArray setClusterTree(JSONArray navigates, JSONObject privileges, boolean sysadmin, boolean flagDbtrack, int rootid)
	{
		JSONArray response = null;
		for(int i = 0; i < navigates.length(); i++)
		{
			JSONObject node = navigates.getJSONObject(i);
//			String id = "manager.cluster."+node.getInt("id");
			if( sysadmin && node.getInt("id") == 0 )
			{
				JSONObject local = map0.get(-1);
				if( local != null )
				{
					JSONArray children = null;
					if( node.has("children"))
						children = node.getJSONArray("children");
					else
					{
						children = new JSONArray();
						node.put("children", children);
					}
					local.put("dropInner", false);
					local.put("dropRoot", false);
					local.put("drop", false);
					children.put(local);
				}
			}
			
			
			if( node.has("children") )
			{
				JSONArray children = node.getJSONArray("children");
				if( node.getInt("id") == rootid ){
					response = children;
				}
				children = setClusterTree(children, privileges, sysadmin, flagDbtrack, rootid);
				if( (!sysadmin) && children.length() == 0 )
				{//如果不是系统管理员或者集群目录下没有数据，就移除该节点
					navigates.remove(i);
					i -= 1;
					continue;
				}
			}
			else if( node.has("ip") )
			{//如果节点是IP地址，表示是伺服模块
				String serverkey = node.has("security-key")?node.getString("security-key"):"";
				if( privileges == null ||
				   !privileges.has(serverkey) )
				{
					if( !sysadmin ){
						navigates.remove(i);
						i -= 1;
						continue;
					};
				}
				if( privileges != null && privileges.has(serverkey) )
					node.put("privileges", privileges.getJSONObject(serverkey));
//				String title = node.has("title")?node.getString("title"):"";
//				title = title.length()>4?title.substring(0, 4)+"...":title;
				node.put("name", node.getString("ip")+"("+node.getInt("port")+")");//+" "+title );
				node.put("icon", "images/icons/cluster.png?v=1");
				if( node.has("isParent") ) node.remove("isParent");
				if( node.has("open") ) node.remove("open");
				if( node.has("iconClose") ) node.remove("iconClose");
				if( node.has("iconOpen") ) node.remove("iconOpen");
				if( node.has("gateone") ){
					node.put("icon", "images/icons/ssh.png");
				}
				node.put("server", true);
				if( flagDbtrack )
				{
				}
			}
		}
		if( response == null ){
			response = navigates;
		}
		return response;
	}
	
	/**
	 * 得到集群监控的配置数据 
	 * @return
	 * @throws Exception 
	 */
	public static JSONArray getClusters() throws Exception
	{
		if( Instance == null ) return null;
		Instance.setConfig();
		return Instance.clusters;
	}
	
	/**
	 * 加载集群节点数据
	 * @param out
	 */
	public void loadClusters(ArrayList<JSONObject> out)
	{
		this.loadClusters(out, clusters);
	}
	
	private void loadClusters(ArrayList<JSONObject> out, JSONArray children)
	{
		for(int i = 0; i < children.length(); i++)
		{
			JSONObject e = children.getJSONObject(i);
			if( !e.has("ip") )
			{
				out.add(e);
				if( e.has("children") )
				{
					this.loadClusters(out, e.getJSONArray("children"));
				}
			}
		}
	}

	/**
	 * 得到集群的堡垒机
	 * @param privileges
	 * @param sysadmin
	 * @return
	 * @throws Exception
	 */
	public JSONArray getClusterGateone(JSONObject privileges, boolean sysadmin)
		throws Exception
	{
		setConfig();
//		System.err.println(map0);
		JSONArray gateones = new JSONArray();
		if( clusters != null )
		{
			this.setClusterGateone(clusters, privileges, sysadmin, gateones);
		}
		return gateones;
	}

	/**
	 * 
	 * @param array
	 * @param role
	 * @param sysadmin
	 * @return
	 */
	private void setClusterGateone(JSONArray navigates, JSONObject privileges, boolean sysadmin, JSONArray gateones)
	{
		for(int i = 0; i < navigates.length(); i++)
		{
			JSONObject node = navigates.getJSONObject(i);
			if( node.has("children") )
			{
				JSONArray children = node.getJSONArray("children");
				setClusterGateone(children, privileges, sysadmin, gateones);
			}
			else if( node.has("ip") )
			{//如果节点是IP地址，表示是伺服模块
				String serverkey = node.has("security-key")?node.getString("security-key"):"";
//				if( privileges == null ||
//				   !privileges.has(serverkey) )
//				{
//					if( !sysadmin ){
//						continue;
//					};
//				}
				if( node.has("isParent") ) node.remove("isParent");
				if( node.has("open") ) node.remove("open");
				if( node.has("iconClose") ) node.remove("iconClose");
				if( node.has("iconOpen") ) node.remove("iconOpen");
				if( node.has("gateone") ){
					JSONObject e = new JSONObject(node.toString());
					if( privileges != null && privileges.has(serverkey) )
						e.put("privileges", privileges.getJSONObject(serverkey));
					e.put("name", "[堡垒]"+node.getString("ip"));//+" "+title );
					e.put("icon", "images/icons/ssh.png");
					gateones.put(e);
					e.put("server", true);
				}
			}
		}
	}
	/**
	 * 根据权限配置，系统管理员是否等参数获取集群树
	 * @param privileges
	 * @param sysadmin
	 * @param flagDbtrack
	 * @param onlyGateone
	 * @return
	 * @throws Exception
	 */
	public JSONArray getClusterTree(JSONObject privileges, boolean sysadmin, boolean flagDbtrack)
		throws Exception
	{
		return this.getClusterTree(privileges, sysadmin, flagDbtrack, -1);
	}
	
	public JSONArray getClusterTree(JSONObject privileges, boolean sysadmin, boolean flagDbtrack, int rootid)
		throws Exception
	{
		setConfig();
//		System.err.println(map0);
		if( clusters != null )
		{
			return this.setClusterTree(new JSONArray(clusters.toString()), privileges, sysadmin, flagDbtrack, rootid);
		}
		else
		{
			return new JSONArray();
		}
	}
	/**
	 * 输入指定集群ID，返回集群列表
	 * @return
	 */
	public JSONArray getClusterTree(JSONArray clusters, int id)
	{
		JSONObject cluster = null;
		for(int i = 0; i < clusters.length(); i++)
		{
			JSONObject node = clusters.getJSONObject(i);
			if( node.getInt("id") == id )
			{
				cluster = node;
				break;
			}
			if( node.has("children") )
			{
				JSONArray children = node.getJSONArray("children");
				JSONArray out = getClusterTree(children, id);
				if( out != null ) return out;
			}
		}
		if( cluster == null ) return null;
		return new JSONArray().put(cluster);
	}
	/**
	 * 获取集群监控配置
	 * @return
	 */
	private synchronized void setConfig()
	{
		File file = new File(PathFactory.getDataPath(), "zktmp/cos/config/monitor/server");
		try
		{
			if( clusters != null ){
				if( !map0.get(0).has("pid") ) map0.get(0).put("pid", -7);
				return;
			}
			Stat stat = ZKMgr.getZookeeper().exists("/cos/config/monitor/clusters");
			if( stat != null ){
				byte[] payload = ZKMgr.getZookeeper().getData("/cos/config/monitor/clusters", false, stat, true);
				this.clusters = new JSONArray(new String(payload, "UTF-8"));
			}
//			System.err.println(this.clusters.toString(4));
			if( clusters == null )
			{
				if( file.exists() )
				{//从本地镜像得到监控树
					clusters = new JSONArray(new String(IOHelper.readAsByteArray(file), "UTF-8"));
					log.debug("Open the view of monitor for ops from path "+file.getPath());
				}
				else
				{
					throw new Exception("集群伺服器监控数据初始化失败请重启主界面框架系统");
				}
			}
			else{
				IOHelper.writeFile(file, clusters.toString().getBytes("UTF-8"));
			}
//			local.put("id", -1);
//			local.put("pid", 0);
//			local.put("title", "本地伺服器");
//			local.put("ip", "127.0.0.1");
//			local.put("server", true);
//			local.put("drop", false);
//			int port = COSConfig.getLocalControlPort();
//			local.put("port", port);
//			local.put("icon", "images/icons/cluster.png");
//			servers.add(local);
//			map0.put(-1, local);
//			map2.put("127.0.0.1:"+port, -1);
		}
		catch(Exception e)
		{
			log.error("Failed to set config of monitor from /cos/config/monitor/server for "+e);
			if( clusters == null ){
				try {
					String json = new String(IOHelper.readAsByteArray(file), "UTF-8");
					this.clusters = new JSONArray(json);
				} catch (UnsupportedEncodingException e1) {
				}
			}
		}
		loadMonitorConfig(clusters);
	}
	
	/**
	 * 得到集群映射表
	 * @param id
	 * @return
	 */
	public JSONObject getCluster(int id)
	{
		JSONObject cluster = this.map0.get(id);
		if( cluster != null && !cluster.has("isParent") ) cluster = null;
		return cluster;
	}

	/**
	 * 得到指定ID的伺服器对象
	 * @param id
	 * @return
	 */
	public JSONObject getServer(int id)
	{
		if( id == 0 ) return null;
		JSONObject cluster = this.map0.get(id);
		if( cluster.has("ip") ){
			if( cluster.has("isParent") ) cluster.remove("isParent");
			if( cluster.has("open") ) cluster.remove("open");
			if( cluster.has("iconClose") ) cluster.remove("iconClose");
			if( cluster.has("iconOpen") ) cluster.remove("iconOpen");
		}
		if( cluster != null && cluster.has("isParent") ) return null;
		JSONObject server = new JSONObject(cluster.toString());
		if( states.containsKey(id) ){
			int state = this.states.get(id).value;
			server.put("state", state);
		}
		else{
			server.put("state", STATE_INT);
		}
		return server;
	}
	/**
	 * 根据伺服器机器码得到服务器对象
	 * @param serverid 机器码源码
	 * @return
	 */
	public JSONObject getServer(String serverid)
	{
		if( serverid == null ) return null;
		JSONObject cluster = this.map1.get(serverid);
		if( cluster == null || (cluster != null && cluster.has("isParent")) ) return null;
		JSONObject server = new JSONObject(cluster.toString());
		int state = this.states.get(server.get("id")).value;
		server.put("state", state);
		return server;
	}
	/**
	 * 通过md5加密的key得到伺服器对象获取IP和端口
	 * @param serverkey
	 * @return
	 */
	public JSONObject getServerByKey(String serverkey)
	{
		JSONObject cluster = this.map3.get(serverkey);
		if( cluster != null && cluster.has("isParent") ) cluster = null;
		JSONObject server = new JSONObject(cluster.toString());
		int state = this.states.get(server.get("id")).value;
		server.put("state", state);
		return server;
	}
	/**
	 * 设置过滤数据
	 * @param array
	 */
	public void setClusterFilter(HashMap<Integer, JSONObject> filter, JSONObject cluster)
	{
		if( cluster == null || !cluster.has("isParent") ) return;
		filter.put(cluster.getInt("id"), cluster);
		if( cluster.has("children") )
		{
			JSONArray children = cluster.getJSONArray("children");
			for(int i = 0; i < children.length(); i++)
			{
				this.setClusterFilter(filter, children.getJSONObject(i));
			}
		}
	}
	
	/**
	 * 
	 * @return -1 表示获取失败
	 */
	private synchronized int getNewClusterId()
	{
		try
		{
			Zookeeper zookeeper = ZKMgr.getZookeeper();
			String zkpath = "/cos/config/monitor/clusters/maxid";
			Stat stat = zookeeper.exists(zkpath, false);
			if( stat == null )
			{
				theClusterId += 1;
				zookeeper.create(zkpath, String.valueOf(this.theClusterId).getBytes());
			}
			else
			{
				String val = new String(zookeeper.getData(zkpath, false, stat));
				theClusterId = Integer.parseInt(val);
				theClusterId += 1;
				zookeeper.setData(zkpath, String.valueOf(this.theClusterId).getBytes(), stat.getVersion());
			}
			return theClusterId;
		}
		catch(Exception e)
		{
			log.error("Failed to get the new id from zk for "+e);
			return -1;
		}
	}
	public int getTheClusterId() {
		return theClusterId;
	}
	
	public int getClusterServerCount(){
		return this.servers.size();
	}

	/**
	 * 设置配置
	 * @param array
	 */
	private void loadMonitorConfig(JSONArray array)
	{
		if( array == null ) return;
		for(int i = 0; i < array.length(); i++)
		{
			JSONObject node = array.getJSONObject(i);
			if( map0.containsKey(node.getInt("id")) || node.getInt("id") == -1 )
			{
				array.remove(i);
				i -= 1;
				continue;
			}
			map0.put(node.getInt("id"), node);
			if( node.has("ip") && node.has("port") )
			{
				if( node.has("security-key") ){
					map1.put(node.getString("security-key"), node);
					map3.put(Tools.encodeMD5(node.getString("security-key")), node);
				}
				map2.put(node.getString("ip")+":"+node.getInt("port"), node.getInt("id"));
			}
            if( node.getInt("id") > this.theClusterId )
            {
            	this.theClusterId = node.getInt("id");
            }
            if( !node.has("isParent") )
            {
            	servers.add(node);
            }
			if( node.has("children") )
			{
				JSONArray children = node.getJSONArray("children");
				loadMonitorConfig(children);
				for(int j = 0; j < children.length(); j++)
				{
					JSONObject child = children.getJSONObject(j);
					child.put("pid", node.getInt("id"));
				}
			}
		}
	}
	
	/**
	 * 关闭
	 */
	public void close()
	{
		ZKMgr.close();
		log.info("Close the manager of monitor from "+this);
		monitor.close();
	}
	
	/**
	 * 构建集群监控的缺省节点
	 */
	public static void buildMonitor()
	{
		try
		{
			Zookeeper zookeeper = ZKMgr.getZookeeper();
			String zkpath = "/cos/config/monitor";
			Stat stat = zookeeper.exists(zkpath, false); 
			if( stat == null)
			{
				zookeeper.create(zkpath, "集群监控配置".getBytes("UTF-8"));
			}
			
			zkpath = "/cos/config/monitor/clusters";
			stat = zookeeper.exists(zkpath, false); 
			if( stat != null )
			{
				return;
			}
			else
			{
				stat = zookeeper.exists("/cos/config/monitor/server", false); 
				JSONArray clusters = null;
				if( stat != null ){
					clusters = ZKMgr.getZookeeper().getJSONArrayObject("/cos/config/monitor/server", false);
//					zookeeper.deleteNode("/cos/config/monitor/server");
				}
				else{
					clusters = new JSONArray();
					JSONObject config = new JSONObject();
					config.put("id", 0);
					config.put("pid", -7);
					config.put("name", "缺省集群");
					config.put("isParent", true);
					clusters.put(config);
				}
				zookeeper.create(zkpath, clusters.toString().getBytes("UTF-8"), true);
			}
			log.info("Succed to build the config of monitor to zookeeper.");
		}
		catch(Exception e )
		{
			log.error("Failed to build the config of monitor to zookeeper for exception", e);
		}
	}
	
	/**
	 * 得到监控的运行信息
	 * @param host = <ip>:<port>
	 * @return
	 */
	public String getMonitorRunnerInfo(String ip, int port)
	{
		RunFetchMonitor runner = this.monitor.getRunFetchMonitor(ip, port);
		if( runner == null )
		{
			return null;
		}
		StringBuffer sb = new StringBuffer();
		sb.append("【主机监控对象】"+ip+":"+port);
		sb.append("\r\n\t名称="+(runner.getSysDesc()!=null?runner.getSysDesc().getDescript():""));
		sb.append("\r\n\t是否激活="+runner.isStartup());
		sb.append("\r\n\t连接状态="+runner.isConnect());
		sb.append("\r\n\t是否超时="+runner.headtbeatTimeout(49000));
		sb.append("\r\n\t启动时间="+Tools.getFormatTime("MM-dd HH:mm:ss.SSS", runner.getStartuptime()));
		sb.append("\r\n\t上次心跳="+Tools.getFormatTime("HH:mm:ss.SSS", runner.getHeartbeat()));
		sb.append("\r\n【监控数据流量】\r\n");
		sb.append(runner.getRunnerInfo());
		sb.append("\r\n【程序列表】");
		synchronized(runner.getModulePerfs())
		{
	        for( int i = 0; i < runner.getModulePerfs().size(); i++ )
	        {
	        	ModulePerf module = runner.getModulePerfs().get(i);
	        	sb.append("\r\n\t[");
	        	sb.append(module.getStartupTime()+"]");	        	
	        	sb.append(module.getId());
	        	sb.append("("+module.getState());
	        	sb.append(") "+module.getName());
	            sb.append(", "+module.getRemark());
			}
		}
		return sb.toString();
	}

	/**
	 * 重启监控
	 * @return
	 */
	public SystemPerf reloadMonitor(String ip, int port)
	{
		RunFetchMonitor runner = this.monitor.getRunFetchMonitor(ip, port);
		if( runner == null )
		{
			return null;
		}
		runner.disconnect();
		return runner.getSysDesc();
	}

	/**
	 * 得到模块日志信息
	 * @param host
	 * @param id
	 * @return
	 */
	public ArrayList<ModuleLog> getModuleLogs(String ip, int port, String id)
	{
    	byte[] payload = new byte[64*1024];
    	int offset = 0;
    	payload[offset++] = Command.CONTROL_LOGFILELIST;
    	payload[offset++] = (byte)id.length();
    	offset = Tools.copyByteArray(id.getBytes(), payload, offset);
    	DatagramSocket datagramSocket = null;
    	ArrayList<ModuleLog> out = new ArrayList<ModuleLog>();
        try
        {
        	datagramSocket = new DatagramSocket();
            datagramSocket.setSoTimeout(15000);
            DatagramPacket request = new DatagramPacket(payload, 0, offset, InetAddress.getByName( ip ), port );
            datagramSocket.send( request );
            DatagramPacket reponse = new DatagramPacket(payload, 0, payload.length, request.getAddress(), request.getPort() );
			datagramSocket.setSoTimeout(15000);
            datagramSocket.receive( reponse );
            offset = 0;
            while( payload[offset] != -1 )
            {
            	ModuleLog moduleLog = new ModuleLog();
	            int len = Tools.bytesToInt(payload, offset, 2);
	            offset += 2;
	            for( int i = 0; i < len; i++ )
	            {
	            	if( payload[i+offset] == '\\' )
	            	{
	            		payload[i+offset] = '/';
	            	}
	            }
	            String filepath = new String(payload, offset, len);
	            int beginIndex = filepath.lastIndexOf("/");
	            String filename = filepath.substring(beginIndex+1);
	            offset += len;
	            long length = Tools.bytesToLong(payload, offset, 8);
	            double size = length;
	            offset += 8;
	            
	            StringBuffer sb = new StringBuffer();
	            sb.append(filename);
            	sb.append("(");
	            if( size < 1024*1024 )
	            {
	            	size = size/1024;
	            	sb.append(Tools.DF.format(size));
	            	sb.append("K");
		            moduleLog.setScale("K");
	            }
	            else if( size < 1024*1024*1024 )
	            {
	            	sb.append(Tools.DF.format(size/(1024*1024)));
	            	sb.append("M");
		            moduleLog.setScale("M");
	            }
	            else
	            {
	            	sb.append(Tools.DF.format(size/(1024*1024*1024)));
	            	sb.append("G");
		            moduleLog.setScale("G");
	            }
	            sb.append(")");
	            moduleLog.setPath(filepath);
	            moduleLog.setName(sb.toString());
	            moduleLog.setId(id);
	            moduleLog.setLength(length);
	            moduleLog.setSize(Kit.bytesScale(length));
	            out.add(moduleLog);
            }
        }
        catch(Exception e)
        {
        	log.error("Failed to get the list of module-log from "+ip+":"+port+" for exception "+e.getMessage());
        }
        finally
        {
    		try
			{
    			if( datagramSocket != null ) datagramSocket.close();
			}
			catch (Exception e)
			{
			}
        }
		return out;
	}
	/**
	 * 主控
	 * @return
	 */
	public Monitor getMonitor()
	{
		return monitor;
	}

	/**
	 * 检查是否开始监听
	 */
	public void checkStart()
	{
		this.setConfig();
		if( !monitor.checkStart() )
		{
			Thread thread = new Thread(){
				public void run()
				{
					try {
						Thread.sleep(15000);
					} catch (InterruptedException e) {
					}
					checkProxyMonitor();
				}
			};
			thread.start();
		}
	}
	
	/**
	 * 得到指定伺服器容器的主控版本号
	 * @param ip
	 * @param port
	 * @return
	 */
	public String getControlVersion(String ip, int port)
	{
		RunFetchMonitor runner = this.monitor.getRunFetchMonitor(ip, port);
		if( runner == null ) return null;
		ModulePerf module = runner.getModulePerf("COSControl");
		if( module == null ) return null;
		return module.getVersion();
	}
}
