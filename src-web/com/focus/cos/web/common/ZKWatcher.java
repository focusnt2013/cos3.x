package com.focus.cos.web.common;

import org.apache.zookeeper.WatchedEvent;

public interface ZKWatcher
{
	public void watch(WatchedEvent event);
	public void open();
	public void close();
}
