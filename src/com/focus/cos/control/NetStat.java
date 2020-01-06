package com.focus.cos.control;

public class NetStat implements java.io.Serializable
{
	private static final long serialVersionUID = -3203672257033803571L;
	
	private long received;
	private long sent;
	
	public long getReceived()
	{
		return received;
	}
	
	public void setReceived(long received)
	{
		this.received = received;
	}
	
	public long getSent()
	{
		return sent;
	}
	
	public void setSent(long sent)
	{
		this.sent = sent;
	}
}