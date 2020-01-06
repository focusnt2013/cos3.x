package com.focus.cos.web.common;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import com.focus.util.Zookeeper;

/**
 * Zookeeper
 */
public class ZKMgr
{
	private static final Log log = LogFactory.getLog(ZKMgr.class);
	private static ZKMgr Instance;
	private Zookeeper zookeeper;
	private long timestampNotconnect;
	private ZKWatcher customWatcher = null;
	private static Boolean Connecting = false;
	
	public ZKMgr()
	{
		Instance = this;
	}
	/**
	 * 设置监听器
	 * @param watcher
	 */
	public void setCustomWatcher(ZKWatcher watcher)
	{
		customWatcher = watcher;
	}
	
	/**
	 * 构建
	 */
	private Zookeeper connect()
	{
		Connecting = true;
		int port = COSConfig.getLocalControlPort();
		long ts = System.currentTimeMillis();
		try
		{
			if( zookeeper != null ) zookeeper.close();
			zookeeper = new Zookeeper("127.0.0.1:"+port) {
				@Override
				public void watch(WatchedEvent event) {
					if( event.getState() != KeeperState.Disconnected )
					{
						if( !zookeeper.getState().isConnected() )
						{
							log.warn("Reconnect "+zookeeper+" disconnected.");
							connect();
							return;
						}
					}
					else if( event.getState() != KeeperState.Expired )
					{
						if( !zookeeper.getState().isConnected() )
						{
							log.warn("Reconnect "+zookeeper+" expired.");
							connect();
							return;
						}
					}
					if( customWatcher != null ) customWatcher.watch(event);
				}
			};
			log.info("Setup "+zookeeper+" from port "+port);
			if( customWatcher != null ) customWatcher.open();
		}
		catch(Exception e)
		{
			ts = System.currentTimeMillis() - ts;
			log.warn("Failed to setup the zookeeper("+port+") for "+e.getMessage()+", timeout:"+ts+").");
		}
		Connecting = false;
		return zookeeper;
	}
	
	private Zookeeper response() throws Exception
	{
		if( zookeeper != null && zookeeper.isConnected() ) return zookeeper;
		synchronized( Connecting )
		{
			if( zookeeper == null || ( timestampNotconnect > 0 && System.currentTimeMillis() - timestampNotconnect > 15000 ) )
			{
				zookeeper = connect();
				if( zookeeper != null ) return zookeeper;
			}
		}
		if( timestampNotconnect == 0 ) timestampNotconnect = System.currentTimeMillis();
		if( customWatcher != null ) customWatcher.close();
		throw new Exception("分布式协调程序连接没有建立("+zookeeper+")");
	}
	/**
	 * 得到可用的ZK连接器
	 */
	public static ZooKeeper getZooKeeper() throws Exception
	{
		Zookeeper zk = getZookeeper();
		return zk.i();
	}

	public static Zookeeper getZookeeper() throws Exception
	{
		if( Connecting ) throw new Exception("分布式协调程序连接中("+Instance.zookeeper+")");
		if( Instance == null ){
			 throw new Exception("分布式协调程序暂未初始化");
		}
		return Instance.response();
	}
	
	/**
	 * 判断指定端口是否存在ZK服务
	 * @param port
	 * @return
	 */
	public static boolean isZooKeeperStartup()
	{
		try
		{
			Zookeeper zookeeper = getZookeeper();
			List<String> nodes = zookeeper.getChildren("/"); 
			StringBuffer sb = new StringBuffer(zookeeper+" ontains the following child nodes: ");
			for(String node : nodes )
			{
				sb.append("\r\n\t"+node);
			}
			log.info(sb.toString());
			return true;
		} 
		catch (Exception e)
		{
			log.error("Found zookeeper(local) not setup for "+e.getMessage());
			return false;
		}
	}

	/**
	 * 创建节点
	 * 
	 * @param path
	 * @return
	 */
	public String create(String path, byte[] data, String ip, int port)
	{
		Zookeeper zookeeper = null;
		try
		{
			if( port == 0 )
			{
				port = COSConfig.getLocalControlPort();
			}
			zookeeper = Zookeeper.getInstance(ip+":"+port);
			Stat stat = zookeeper.exists(path, false); 
			if( stat == null)
			{
				return zookeeper.create(path, new byte[0]);
			}
		}
		catch (Exception e1)
		{
			return e1.getMessage();
		}
		return "创建节点成功.";
	}

    /**
     * 
     * @param path
     * @param ip
     * @param port
     * @return
    public String getPreview(String path, String ip, int port)
    {
		Zookeeper zookeeper = null;
		try
		{
			if( port == 0 )
			{
				port = COSConfig.getLocalControlPort();
			}
			zookeeper = Zookeeper.getInstance(ip+":"+port);
			byte[] data = zookeeper.getData(path, false);
			if( data != null)
			{
				String json = new String(data, "UTF-8");
				if( json.startsWith("{") && json.endsWith("}") )
				{
					JSONObject jsonobj = new JSONObject(json);
					return jsonobj.toString(4);
				}
				else if( json.startsWith("[") && json.endsWith("]"))
				{
					JSONArray jsonobj = new JSONArray(json);
					return jsonobj.toString(4);
				}
				return json;
			}
			return "该节点不存在";
		}
		catch (Exception e)
		{
			log.error("", e);
			return "获取该节点时候出现异常"+e.getMessage();
		}
		finally
		{
			if( zookeeper != null ) zookeeper.close();
		}
    }
     */
    
	/**
	 * 关闭
	 */
	public static void close()
	{
		if( Instance != null && Instance.zookeeper != null ){
			log.info("Release the connection of "+Instance.zookeeper+".");
			Instance.zookeeper.close();
		}
	}
}
