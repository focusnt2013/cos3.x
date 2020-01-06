package com.focus.util;

import java.util.List;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.data.Stat;

public class ZookeeperTest {

	public static void main(String[] args) 
	{
		Zookeeper zookeeper = null;
		try
		{
			zookeeper = new Zookeeper("127.0.0.1:9076"){
				@Override
				public void watch(WatchedEvent event) {
					// TODO Auto-generated method stub
					
				}
			};
			List<String> list = zookeeper.getChildren("/", true);
			for(String id : list)
			{
				System.out.println("\t"+id);
			}
			Stat stat = zookeeper.exists("/test1", true);
			if( stat != null )
			{
				System.out.println(stat.toString());
				zookeeper.delete("/test1", stat.getVersion());
			}
			stat = zookeeper.exists("/test2", true);
			if( stat != null )
			{
				System.out.println(stat.toString());
				zookeeper.delete("/test2", stat.getVersion());
			}
			stat = zookeeper.exists("/test3", true);
			if( stat != null )
			{
				System.out.println(stat.toString());
				zookeeper.delete("/test3", stat.getVersion());
			}
			zookeeper.create("/test1", "".getBytes("UTF-8"));
			zookeeper.create("/test2", "".getBytes("UTF-8"));
			zookeeper.create("/test3", "".getBytes("UTF-8"));
			
			System.out.println("...输入任意键退出");
			System.in.read();
			zookeeper.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
