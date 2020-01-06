package com.focus.cos.web.common;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import com.focus.util.Zookeeper;

public class ZKDeleter extends Thread
{
	private static final Log log = LogFactory.getLog(ZKDeleter.class);
	private int progress = 0;
	private boolean running = false;
	private ArrayList<String> pathDeletes = new ArrayList<String>();
	private ArrayList<Integer> statDeletes = new ArrayList<Integer>();
	private ZooKeeper zookeeper = null;
	private String deletepath = "";
	private Exception exception = null;
	private StringBuffer sb = new StringBuffer();
	
	public Exception getException() {
		return exception;
	}

	public ZKDeleter(String ip, int port, String path) throws Exception
	{
		running = true;
		zookeeper = Zookeeper.getInstance(ip+":"+port).i();
		deletepath = path;
	}
	
	public ZKDeleter(ZooKeeper zookeeper, String path) throws Exception
	{
		running = true;
		this.zookeeper = zookeeper;
		deletepath = path;
	}
	
	public String toString()
	{
		return sb.toString();
	}
	
	public void run()
	{
		String pathDelete = null;
		try
		{
			progress = 0;
			deleteNode(deletepath);
			log.debug("Found "+pathDeletes.size()+" nodes need to delete.");
			progress += 20;
			for(int i = 0; i < pathDeletes.size(); i++)
			{
				if( progress < 100 ) progress += 1;
				pathDelete = pathDeletes.get(i);
				zookeeper.delete(pathDelete, statDeletes.get(i));
				sb.append("\r\n\t");sb.append(pathDelete);
			}
			progress = 100;
			log.debug("Succeed to delete "+sb.toString());
		}
		catch (Exception e)
		{
			exception = e;
			log.error("Failed to delete "+pathDelete, e);
		}
		finally
		{
			statDeletes.clear();
			if( zookeeper != null )
				try
				{
					zookeeper.close();
				}
				catch (InterruptedException e)
				{
				}
			running = false;
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
		if( stat != null)
		{
			pathDeletes.add(0, path);
			statDeletes.add(0, stat.getVersion());
			if( stat.getNumChildren() > 0 )
			{
				List<String> list = zookeeper.getChildren(path, false);
				for(String child : list )
				{
					deleteNode(path+"/"+child);
				}
			}
		}
	}
	
	public String getIp()
	{
		if( zookeeper == null ) return null;
		if( zookeeper.getState() == null ) return "";
		return this.zookeeper.getState().name();
	}

	public String getDeletepath() {
		return deletepath;
	}

	public ArrayList<String> getPathDeletes() {
		return pathDeletes;
	}

	public int getProgress() {
		return progress;
	}
	
	public boolean isRunning() {
		return running;
	}
}
