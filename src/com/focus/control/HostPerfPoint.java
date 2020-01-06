package com.focus.control;

import java.util.Calendar;

public class HostPerfPoint implements java.io.Serializable
{
	private static final long serialVersionUID = 977910967808968187L;
	
	private int time;//采样时间点
	private int netload0;//网络负载出（每秒多少M）
	private int netload1;//网络负载入（每秒多少M）
	private int cpuload;//处理器负载（占比）
	private int memusage;//内存大小
	private long memused;//内存使用
	private int ioload0;//磁盘IO负载读（每秒多少M）
	private int ioload1;//磁盘IO负载写（每秒多少M）
	private int temperature ;//温度
	private int hour;//小时点
	private int minute;//分钟点
	
	public int getNetload0()
	{
		return netload0;
	}
	public void setNetload0(long netload)
	{
		this.netload0 = (int)netload;
	}
	public int getNetload1()
	{
		return netload1;
	}
	public void setNetload1(long netload)
	{
		this.netload1 = (int)netload;
	}
	public int getCpuload()
	{
		if( cpuload < 0 ){
			cpuload = Math.abs(cpuload);
		}
		if( cpuload > 10000 ){
			cpuload = 10000;
		}
		return cpuload;
	}
	public void setCpuload(long cpuload)
	{
		this.cpuload = (int)cpuload;
	}
	public int getIoload0()
	{
		return ioload0;
	}
	public void setIoload0(long ioload)
	{
		this.ioload0 = (int)ioload;
	}
	public int getIoload1()
	{
		return ioload1;
	}
	public void setIoload1(long ioload)
	{
		this.ioload1 = (int)ioload;
	}
	public int getTime()
	{
		return time;
	}
	public void setTime(long time)
	{
		this.time = (int)(time/1000);
    	Calendar c = Calendar.getInstance();
    	c.setTimeInMillis(time);
    	this.hour = c.get(Calendar.HOUR_OF_DAY);
    	this.minute = c.get(Calendar.MINUTE);
	}
	public int getMemusage()
	{
		if( memusage < 0 ){
			memusage = Math.abs(memusage);
		}
		if( memusage > 10000 ){
			memusage = 10000;
		}
		return memusage;
	}
	public void setMemusage(int memusage)
	{
		this.memusage = memusage;
	}
	public long getMemused()
	{
		return memused;
	}
	public void setMemused(long memused)
	{
		this.memused = memused;
	}
	public int getTemperature()
	{
		return temperature;
	}
	public void setTemperature(int temperature)
	{
		this.temperature = temperature;
	}
	public int getHour()
	{
		return hour;
	}
	public int getMinute()
	{
		return minute;
	}
}
