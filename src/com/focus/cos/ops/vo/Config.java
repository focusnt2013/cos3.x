package com.focus.cos.ops.vo;

import java.util.ArrayList;

import com.focus.cos.api.Sysuser;

/**
 * 告警配置
 * @author focus
 *
 */
public class Config
{
	private int period;
	private int frequency;
	private ArrayList<Sysuser> subscribers = new ArrayList<Sysuser>();
	
	public int getPeriod()
	{
		return period;
	}
	public int getFrequency()
	{
		return frequency;
	}
	public void setPeriod(int period)
	{
		this.period = period;
	}
	public void setFrequency(int frequency)
	{
		this.frequency = frequency;
	}
	public ArrayList<Sysuser> getSubscribers()
	{
		return subscribers;
	}
	public void addSubscriber(Sysuser e)
	{
		this.subscribers.add(e);
	}
}
