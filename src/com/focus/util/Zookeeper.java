package com.focus.util;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooKeeper.States;
import org.apache.zookeeper.data.Stat;
import org.json.JSONArray;
import org.json.JSONObject;

import com.focus.cos.api.ApiUtils;

public abstract class Zookeeper implements Watcher 
{
	private static long Times = 0;
	private static final int SESSION_TIMEOUT = 7000;
	private CountDownLatch connectedLatch;
	protected ZooKeeper zookeeper = null;
	private long id = 0;
	private String from;
	private String zkaddr;
	private long starttime;//开始时间
	private long endtime;//开始时间
	/**
	 * 创建Zookeeper
	 * @param ip
	 * @param port
	 * @return
	 * @throws Exception
	 */
	public Zookeeper(String ip, int port) throws Exception
	{
		this(ip+":"+port);
	}
	public Zookeeper(String zkaddr) throws Exception
	{
		this.zkaddr = zkaddr;
		starttime = System.currentTimeMillis();
		id = Times++;
		connectedLatch = new CountDownLatch(1);

		StackTraceElement[] stes = new Exception().getStackTrace();
		StringBuffer sb = new StringBuffer();
		if( stes != null && stes.length > 0 )
		{
			for(int i = 0; i < stes.length; i++)
			{
				StackTraceElement e = stes[i];
				if( e.getClassName().endsWith("Zookeeper") ){
					continue;
				}
				if( !e.getClassName().startsWith("com.focus") ){
					break;
				}
				sb.append("\r\n\t");
				sb.append(String.format("%s.%s()", e.getClassName(), e.getMethodName()));
			}
		}
		from = sb.toString();

		zookeeper = new ZooKeeper(zkaddr, SESSION_TIMEOUT, this);
		if( zookeeper.getState() == States.CONNECTING )
		{
			connectedLatch.await(1, TimeUnit.SECONDS);
			if( connectedLatch.getCount() == 1 )
			{
				try
				{
					zookeeper.exists("/", false);
				}
				catch(Exception e)
				{
					long ts = System.currentTimeMillis();
					ts -= starttime;
					String time = Tools.getFormatTime("MM-dd HH:mm:ss", starttime);
					String duration = Tools.getDuration(ts);
					throw new Exception("连接ZooKeeper("+zkaddr+", 从"+time+"开始耗时"+duration+")失败: "+e.getMessage());
				}
			}
			System.out.println(this.toString());
		}
	}
	
	public String toString()
	{
		long ts = endtime==0?System.currentTimeMillis():endtime;
		ts -= starttime;
		String time = Tools.getFormatTime("MM-dd HH:mm:ss", starttime);
		String duration = Tools.getDuration(ts);
		if( zookeeper == null )
		{
			return "Zookeeper("+id+", begin at "+time+"["+duration+"], "+from+")";
		}
		return String.format("@Zookeeper$%s %s@%s %s, begin at %s[%s] from%s",
			id,zookeeper.getSessionId(), zkaddr, zookeeper.getState(), time, duration, from);
	}

	@Override
	public void process(WatchedEvent event) {
        if( connectedLatch.getCount() > 0 && event.getState() == KeeperState.SyncConnected) 
        {  
        	connectedLatch.countDown();
        	endtime = System.currentTimeMillis();
        }
        if( event.getType() != EventType.None )
		{
        	this.watch(event);
		}
	}
	public abstract void watch(WatchedEvent event);

	public ZooKeeper i() 
	{
		return zookeeper;
	}
	
	public boolean isConnected()
	{
		return zookeeper!=null?zookeeper.getState().isConnected():false;
	}

	public Stat exists(String path) throws Exception
	{
		if( zookeeper == null || !zookeeper.getState().isConnected() ) throw new Exception("ZooKeeper disconnect.");
		return zookeeper.exists(path, false); 
	}
	
	public Stat exists(String path, boolean watch) throws Exception
	{
		if( zookeeper == null || !zookeeper.getState().isConnected() ) throw new Exception("ZooKeeper disconnect.");
		return zookeeper.exists(path, watch); 
	}

	public List<String> getChildren(String path) throws Exception
	{
		if( zookeeper == null || !zookeeper.getState().isConnected() ) throw new Exception("ZooKeeper disconnect.");
		return this.zookeeper.getChildren(path, false);
	}
	
	public List<String> getChildren(String path, boolean watch) throws Exception
	{
		if( zookeeper == null || !zookeeper.getState().isConnected() ) throw new Exception("ZooKeeper disconnect.");
		return this.zookeeper.getChildren(path, watch);
	}
	
	public String create(String path, byte[] data) throws Exception
	{
		if( zookeeper == null || !zookeeper.getState().isConnected() ) throw new Exception("ZooKeeper disconnect.");
		return zookeeper.create(path, data, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
	}

	public String create(String path, byte[] data, boolean gzip) throws Exception
	{
		if( zookeeper == null || !zookeeper.getState().isConnected() ) throw new Exception("ZooKeeper disconnect.");
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream gos = new GZIPOutputStream(baos);
        gos.write(data);
        gos.flush();
		gos.finish();
		gos.close();
		data = baos.toByteArray();
		baos.close();
		return zookeeper.create(path, data, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
	}
	
	public Stat setData(String path, byte[] data, Stat stat) throws Exception
	{
		return this.setData(path, data, stat.getVersion());
	}

	public Stat setData(String path, byte[] data, int version) throws Exception
	{
		return this.setData(path, data, version, false);
	}
	
	public Stat setData(String path, byte[] data, int version, boolean gzip) throws Exception
	{
		if( zookeeper == null || !zookeeper.getState().isConnected() ) throw new Exception("ZooKeeper disconnect.");
		if( gzip )
		{
	    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        GZIPOutputStream gos = new GZIPOutputStream(baos);
	        gos.write(data);
	        gos.flush();
			gos.finish();
			gos.close();
			data = baos.toByteArray();
			baos.close();
		}
		return this.zookeeper.setData(path, data, version);
	}
	
	public byte[] getData(String path) throws Exception
	{
		return getData(path, false, false);
	}

	public byte[] getData(String path, boolean watch) throws Exception
	{
		return getData(path, watch, false);
	}

	public byte[] getUngzipData(String path) throws Exception
	{
		return getData(path, false, true);
	}
	
	public byte[] getData(String path, boolean watch, boolean gzip) throws Exception
	{
		if( zookeeper == null || !zookeeper.getState().isConnected() ) throw new Exception("ZooKeeper disconnect.");
		Stat stat = zookeeper.exists(path, watch); 
		return getData(path, watch, stat, gzip);
	}

	public byte[] getData(String path, boolean watch, Stat stat) throws Exception
	{
		return getData(path, watch, stat, false);
	}
	
	public byte[] getData(String path, boolean watch, Stat stat, boolean gzip) throws Exception
	{
		if( zookeeper == null || !zookeeper.getState().isConnected() ) throw new Exception("ZooKeeper disconnect.");
		if( stat != null)
		{
			byte[] payload = this.zookeeper.getData(path, watch, stat);
			if( !gzip ) return payload;
			GZIPInputStream gis = new GZIPInputStream(new java.io.ByteArrayInputStream(payload));
			payload = ApiUtils.readFullInputStream(gis);
			return payload;
		}
		return null;
	}

	/**
	 * 创建节点
	 * @param zookeeper
	 * @param zkpath
	 * @param payload
	 * @throws Exception
	 */
	public void createNode(String zkpath, byte[] payload)
		throws Exception
	{
		createNode(zkpath, payload, false);
	}
	public void createNode(String zkpath, byte[] payload, boolean encrypte)
		throws Exception
	{
		Stat stat = null;
		String[] args = zkpath.split("/");
		String path = "";
		for(String arg : args)
		{
			if( arg.isEmpty() ) continue;
			path += ("/"+arg);
			stat = zookeeper.exists(path, false);
			if( stat == null )
			{
				if( path.equals(zkpath) )
				{
					if( encrypte )
					{
						payload = Base64X.encode(payload).getBytes(); 
					}
					zookeeper.create(zkpath, payload, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				}
				else
					zookeeper.create(path, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
		}
	}

	/**
	 * 删除zk节点
	 * 
	 * @param path
	 * @return
	 * @throws InterruptedException 
	 * @throws KeeperException 
	 */
	public void deleteNode(String path) throws Exception
	{
		Stat stat = zookeeper.exists(path, false); 
		if( stat == null )
		{
			return;
		}
		List<String> list = zookeeper.getChildren(path, false);
		if( list != null )
			for(String child : list )
			{
				deleteNode(path+"/"+child);
			}
		zookeeper.delete(path, stat.getVersion());
	}
	/**
	 * 按照节点目录以次创建每个节点
	 * @param zookeeper
	 * @param zkpath
	 * @param obj
	 * @throws Exception
	 */
	public void createObject(String zkpath, Object obj)
			throws Exception
	{
		if( zookeeper == null || !zookeeper.getState().isConnected() ) throw new Exception("ZooKeeper disconnect.");
		Stat stat = null;
		String[] args = zkpath.split("/");
		String path = "";
		for(String arg : args)
		{
			if( arg.isEmpty() ) continue;
			path += ("/"+arg);
			stat = zookeeper.exists(path, false);
			if( stat == null )
			{
				if( path.equals(zkpath) )
				{
					zookeeper.create(zkpath, obj.toString().getBytes("UTF-8"), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				}
				else
					zookeeper.create(path, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
		}
	}
	
	/**
	 * 节点是字符串，得到该数据
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public String getUTF8(String path) throws Exception
	{
		if( zookeeper == null || !zookeeper.getState().isConnected() ) throw new Exception("ZooKeeper disconnect.");
		Stat stat = zookeeper.exists(path, false); 
		if( stat != null)
		{
			return new String(this.zookeeper.getData(path, false, stat), "UTF-8");
		}
		return "";
	}

	/**
	 * 
	 * @param path
	 * @param data
	 * @return
	 */
	public Stat setJSONObject(String path, JSONObject data)
	{
		return setJSONObject(path, data, false);
	}
	
	/**
	 * 设置JSON的对象
	 * @param path
	 * @param data
	 * @param encrypte
	 * @return
	 */
	public Stat setJSONObject(String path, JSONObject data, boolean encrypte)
	{
		if( zookeeper == null || !zookeeper.getState().isConnected() ) return null;
		try
		{
			Stat stat = zookeeper.exists(path, false);
			byte[] payload = data.toString().getBytes("UTF-8");
			if( encrypte )
			{
				payload = Base64X.encode(payload).getBytes(); 
			}
			if( stat == null)
			{
				create(path, payload);
			}
			else
			{
				return setData(path, payload, stat);
			}
		}
		catch(Exception e)
		{
		}
		return null;
	}
	
	public Stat setGzipJSONObject(String path, JSONObject data)
	{
		if( zookeeper == null || !zookeeper.getState().isConnected() ) return null;
		try
		{
			Stat stat = zookeeper.exists(path, false);
			byte[] payload = data.toString().getBytes("UTF-8");
			if( stat == null)
			{
				create(path, payload, true);
			}
			else
			{
				return setData(path, payload, stat.getVersion(), true);
			}
		}
		catch(Exception e)
		{
		}
		return null;
	}
	/**
	 * 根据路径参数和压缩获得指定路径中的array对象
	 * @param path
	 * @param decrypte
	 * @return
	 */
	public JSONArray getJSONArrayObject(String path, boolean decrypte)
	{
		return this.getJSONArrayObject(path, decrypte, false);
	}
	/**
	 * 根据完整参数获得指定路径中的array对象
	 * @param path
	 * @param decrypte
	 * @param gzip
	 * @return
	 */
	public JSONArray getJSONArrayObject(String path, boolean decrypte, boolean gzip)
	{
		if( zookeeper == null || !zookeeper.getState().isConnected() ) return null;
		try
		{
			byte[] payload = getData(path, false, gzip);
			if( payload != null)
			{
				if( decrypte )
				{
					payload = Base64X.decode(new String(payload));
				}
				String json = new String(payload, "UTF-8");
				return new JSONArray(json);
			}
		}
		catch(Exception e)
		{
		}
		return null;
	}
	
	/**
	 * 设置压缩的JSON
	 * @param path
	 * @param data
	 * @return
	 */
	public Stat setGZIPJSONArray(String path, JSONArray data)
	{
		if( zookeeper == null || !zookeeper.getState().isConnected() ) return null;
		try
		{
			Stat stat = exists(path, false);
			byte[] payload = data.toString().getBytes("UTF-8");
			if( stat == null)
			{
				create(path, payload, true);
			}
			else
			{
				return setData(path, payload, stat.getVersion(), true);
			}
		}
		catch(Exception e)
		{
		}
		return null;
	}
	/**
	 * 设置JSON数组到节点
	 * @param path
	 * @param data
	 * @param encrypte 加密开关
	 * @return
	 */
	public Stat setJSONArray(String path, JSONArray data)
	{
		return setJSONArray(path, data, false);
	}
	
	public Stat setJSONArray(String path, JSONArray data, boolean encrypte)
	{
		if( zookeeper == null || !zookeeper.getState().isConnected() ) return null;
		try
		{
			Stat stat = exists(path, false);
			byte[] payload = data.toString().getBytes("UTF-8");
			if( encrypte )
			{
				payload = Base64X.encode(payload).getBytes(); 
			}
			if( stat == null)
			{
				create(path, payload);
			}
			else
			{
				return setData(path, payload, stat);
			}
		}
		catch(Exception e)
		{
		}
		return null;
	}

	/**
	 * 设置字符串到节点
	 * @param path
	 * @param data
	 * @param encrypte 加密开关
	 * @return
	 */
	public Stat setString(String path, String data)
	{
		return setString(path, data, false);
	}
	
	public Stat setString(String path, String data, boolean encrypte)
	{
		try
		{
			Stat stat = exists(path, false);
			byte[] payload = data.toString().getBytes("UTF-8");
			if( encrypte )
			{
				payload = Base64X.encode(payload).getBytes(); 
			}
			if( stat == null)
			{
				create(path, payload);
			}
			else
			{
				return setData(path, payload, stat);
			}
		}
		catch(Exception e)
		{
		}
		return null;
	}

	/**
	 * 删除
	 * @param path
	 */
	public boolean delete(String path)
	{
		try
		{
			Stat stat = exists(path, false); 
			if( stat != null)
			{
				zookeeper.delete(path, stat.getVersion());
				return true;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}
	
	public void delete(String path, int version) throws Exception
	{
		zookeeper.delete(path, version);
	}

	/**
	 * 设置配置数据
	 * @return
	 */
	public JSONObject getJSONObject(String path)
	{
		return getJSONObject(path, false);
	}
	
	/**
	 * 得到
	 * @param path
	 * @param decrypte
	 * @return
	 */
	public JSONObject getJSONObject(String path, boolean decrypte)
	{
		return getJSONObject(path, decrypte, false);
	}

	public JSONObject getUngzipJSONObject(String path)
	{
		return getJSONObject(path, false, true);
	}
	/**
	 * 得到
	 * @param path
	 * @param decrypte
	 * @return
	 */
	public JSONObject getJSONObject(String path, boolean decrypte, boolean gzip)
	{
		try
		{
			Stat stat = zookeeper.exists(path, false); 
			byte[] payload = getData(path, false, stat, gzip);
			if( payload != null)
			{
				if( decrypte )
				{
					payload = Base64X.decode(new String(payload));
				}
				String json = new String(payload, "UTF-8");
				JSONObject data = new JSONObject(json);
				data.put("_timestamp", stat!=null?stat.getMtime():0);
				return data;
			}
		}
		catch(Exception e)
		{
		}
		return null;
	}

	/**
	 * 得到
	 * @param path
	 * @return
	 */
	public JSONArray getJSONArray(String path, JSONArray data)
	{
		return getJSONArray(path, data, false, false);
	}

	public JSONArray getJSONArray(String path, JSONArray data, boolean decrypte, boolean gzip)
	{
		try
		{
			Stat stat = exists(path, false); 
			if( stat != null)
			{
				List<String> nodes = zookeeper.getChildren(path, false);
				for(String node : nodes)
				{
					JSONObject obj = getJSONObject(path+"/"+node, decrypte, gzip);
					if( obj != null ) data.put(obj);
				}
			}
		}
		catch(Exception e)
		{
		}
		return data;
	}
	
	/**
	 * 设置配置数据
	 * @return
	 */
	public List<JSONObject> getJSONObjects(String path)
	{
		return getJSONObjects(path, false);
	}

	/**
	 * 设置配置数据
	 * @return
	 */
	public List<JSONObject> getJSONObjects(String path, boolean decrypte)
	{
		ArrayList<JSONObject> list = new ArrayList<JSONObject>();
		try
		{
			Stat stat = zookeeper.exists(path, false); 
			if( stat != null)
			{
				List<String> nodes = zookeeper.getChildren(path, false);
				for(String node : nodes)
				{
					node = path+"/"+node;
					JSONObject obj = this.getJSONObject(node, decrypte);
					if( obj != null ) list.add(obj);
				}
			}
		}
		catch(Exception e)
		{
		}
		return list;
	}

	/**
	 * 得到
	 * @param path
	 * @return
	 */
	public JSONArray getJSONArray(String path, boolean decrypte)
	{
		return this.getJSONArray(path, decrypte, null);
	}
	public JSONArray getJSONArray(String path, boolean decrypte, String exclude)
	{
		JSONArray list = new JSONArray();
		try
		{
			Stat stat = exists(path, false); 
			if( stat != null)
			{
				List<String> nodes = zookeeper.getChildren(path, false);
				for(String node : nodes)
				{
					if( exclude != null && node.indexOf(exclude) != -1 ) continue;
					
					JSONObject obj = getJSONObject(path+"/"+node, decrypte);
					if( obj != null ) list.put(obj);
				}
			}
		}
		catch(Exception e)
		{
		}
		return list;
	}

	public void close()
	{
		if( zookeeper != null )
			try {
				zookeeper.close();
			} catch (InterruptedException e) {
			}
	}
	/**
	 * 通过IP地址和端口获取ZK实例
	 * @param ip
	 * @param port
	 * @return
	 * @throws Exception
	 */
	public static Zookeeper getInstance(String ip, int port) throws Exception
	{
		return getInstance(ip+":"+port);
	}
	public static Zookeeper getInstance(String zkaddr) throws Exception
	{
		Zookeeper instance = new Zookeeper(zkaddr){
			@Override
			public void watch(WatchedEvent event) {
			}
		};
		return instance;
	}

	/**
	 * 指定ZK实例创建节点
	 * @param zookeeper
	 * @param zkpath
	 * @param obj
	 * @throws Exception
	 */
	public static void createObject(ZooKeeper zookeeper, String zkpath, Object obj)
		throws Exception
	{
		Stat stat = null;
		String[] args = zkpath.split("/");
		String path = "";
		for(String arg : args)
		{
			if( arg.isEmpty() ) continue;
			path += ("/"+arg);
			stat = zookeeper.exists(path, false);
			if( stat == null )
			{
				if( path.equals(zkpath) )
				{
					zookeeper.create(zkpath, obj.toString().getBytes("UTF-8"), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				}
				else
					zookeeper.create(path, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
		}
	}

	/**
	 * 创建节点
	 * @param zookeeper
	 * @param zkpath
	 * @param payload
	 * @throws Exception
	 */
	public static void createNode(ZooKeeper zookeeper, String zkpath, byte[] payload)
		throws Exception
	{
		Stat stat = null;
		String[] args = zkpath.split("/");
		String path = "";
		for(String arg : args)
		{
			if( arg.isEmpty() ) continue;
			path += ("/"+arg);
			stat = zookeeper.exists(path, false);
			if( stat == null )
			{
				if( path.equals(zkpath) )
				{
					zookeeper.create(zkpath, payload, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				}
				else
					zookeeper.create(path, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
		}
	}

	/**
	 * 删除zk节点
	 * 
	 * @param path
	 * @return
	 * @throws InterruptedException 
	 * @throws KeeperException 
	 */
	public static void deleteNode(ZooKeeper zookeeper, String path) throws Exception
	{
		Stat stat = zookeeper.exists(path, false); 
		if( stat == null )
		{
			return;
		}
		List<String> list = zookeeper.getChildren(path, false);
		if( list != null )
			for(String child : list )
			{
				deleteNode(zookeeper, path+"/"+child);
			}
		zookeeper.delete(path, stat.getVersion());
	}
}
