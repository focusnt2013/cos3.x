package com.focus.cos.web.ops.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import com.focus.cos.web.common.ZKMgr;
import com.focus.cos.web.common.ZKWatcher;

public class ProxyWatcher implements ZKWatcher
{
	public static final Log log = LogFactory.getLog(ProxyWatcher.class);
	private boolean opened = false;
	@Override
	public void watch(WatchedEvent event)
	{
		open();
		log.info("Found "+event.getType()+" from "+event.getPath());
		if( !"/cos/data/apiproxy".equals(event.getPath()) ) return;
		if( event.getType() == EventType.NodeChildrenChanged )
		{
			MonitorMgr.getInstance().checkProxyMonitor();
		}
	}

	@Override
	public void close()
	{
		opened = false;
	}

	@Override
	public void open()
	{
		if( opened ) return;
		String zkpath = "/cos/data/apiproxy";
		try 
		{
			ZooKeeper zookeeper = ZKMgr.getZooKeeper();
			Stat stat = zookeeper.exists(zkpath, false);
			if( stat != null )
			{
				log.info("Suceed to watch "+zkpath);
				zookeeper.getChildren(zkpath, true);
				opened = true;
			}
			else
			{
				log.warn("Failed to watch "+zkpath+" for not exist.");
			}
		}
		catch (Exception e) 
		{
			log.error("Failed to watch "+zkpath, e);
		}
	}
}
