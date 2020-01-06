package com.focus.cos.web.time;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TimeSeries
{
	public static final String HOUR = "小时";
	public static final String DAY = "天";
	public static final String WEEK = "周";
	public static final String MONTH = "月";
	
	private Date startTime;
	private Date endTime;
	private List<Date[]> dtList = new ArrayList<Date[]>();
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	
	public String toString()
	{
		return "TimeSerires:" + startTime + " to " + endTime;
	}
	public List<Date[]> getDtList() {
		return dtList;
	}
}
