package com.focus.cos.web.time;

public class DiscreteTime implements java.io.Serializable
{
	private static final long serialVersionUID = -5147225936636385141L;
	
	private String date;//日期
	private int hour0;//开始几点钟
	private int hour1;//结束几点钟
	
	public String getDate()
	{
		return date;
	}
	public void setDate(String date)
	{
		this.date = date;
	}
	public int getHour0()
	{
		return hour0;
	}
	public void setHour0(int hour0)
	{
		this.hour0 = hour0;
	}
	public int getHour1()
	{
		return hour1;
	}
	public void setHour1(int hour1)
	{
		this.hour1 = hour1;
	}
	public String getTimestamp()
	{
		return hour0<10?("0"+hour0):String.valueOf(hour0);
	}
	//日期戳yyyyMMdd格式的
	public String getDatestamp()
	{
		return date.substring(0, 4)+date.substring(5, 7)+ date.substring(8);
	}
}
