package com.focus.pipe;

public class PipeObject implements java.io.Serializable
{
	private static final long serialVersionUID = -898113583787553063L;
	public String id;
	public long index = 0;
	public int cell = 0;
	
	public String toString()
	{
		return id+"["+index+"] "+cell;
	}
}
