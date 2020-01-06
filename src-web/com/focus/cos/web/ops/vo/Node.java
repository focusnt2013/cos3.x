package com.focus.cos.web.ops.vo;

import org.apache.zookeeper.data.Stat;

import com.focus.cos.web.common.Kit;
import com.focus.util.Tools;

/**
 * 节点数据
 * @author focus
 */
public class Node
{
	private Stat stat;
	//地址
	private String path;
	//目录名称
	private String name;
	//节点目录
	private String dir;
	//预览的内容
	public Node(String name, String dir)
	{
		this.path = dir.endsWith("/")?(dir+name):(dir+"/"+name);
		this.name = name;
		this.dir = dir;
	}
	
	public String getPath()
	{
		return path;
	}
	public String getName()
	{
		return name;
	}
	public String getDir()
	{
		return dir;
	}
	public String getLastModified()
	{
		return Tools.getFormatTime("yy-MM-dd HH:mm:ss", stat.getMtime());
	}
	public int getChildren()
	{
		return stat.getNumChildren();
	}
	public long getLength()
	{
		return stat.getDataLength();
	}
	public String getVersion()
	{
		return String.valueOf(stat.getVersion());
	}
	public String getSize()
	{
		return Kit.bytesScale(stat.getDataLength());
	}
	public void setStat(Stat stat)
	{
		this.stat = stat;
	}
}
