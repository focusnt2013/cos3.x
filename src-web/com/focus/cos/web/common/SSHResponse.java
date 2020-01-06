package com.focus.cos.web.common;

import java.util.ArrayList;

public class SSHResponse implements java.io.Serializable
{
	private static final long serialVersionUID = 5445979999462063533L;
	private boolean esc = false;
	private ArrayList<String> messages = new ArrayList<String>();
	private int offset;
	private String lastLine;//最后一行，保存发出的指令
	private String user;
	private String host;

	public boolean isEsc()
	{
		return esc;
	}

	public void setEsc(boolean esc)
	{
		this.esc = esc;
	}

	public String getUser()
	{
		return user;
	}

	public void setUser(String user)
	{
		this.user = user;
	}

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

	public String getLastLine()
	{
		return lastLine;
	}

	public void setLastLine(String lastLine)
	{
		this.lastLine = lastLine;
	}
}
