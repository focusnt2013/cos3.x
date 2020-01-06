package com.focus.cos.web.common;

import java.util.ArrayList;

public class DebugResponse implements java.io.Serializable
{
	private static final long serialVersionUID = 2573543016170985014L;
	private ArrayList<String> messages = new ArrayList<String>();
	private int offset;
	private boolean opened;//是否打开
	private String module;
	private String host;

	public String getHost()
	{
		return host;
	}

	public void setHost(String host)
	{
		this.host = host;
	}

	public ArrayList<String> getMessages()
	{
		return messages;
	}
	
	public int getSize()
	{
		return messages.size();
	}

	public int getOffset()
	{
		return offset;
	}

	public void setOffset(int offset)
	{
		this.offset = offset;
	}

	public String getModule()
	{
		return module;
	}

	public void setModule(String module)
	{
		this.module = module;
	}

	public boolean isOpened()
	{
		return opened;
	}

	public void setOpened(boolean opened)
	{
		this.opened = opened;
	}
}
