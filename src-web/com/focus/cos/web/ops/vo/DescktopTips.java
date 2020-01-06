package com.focus.cos.web.ops.vo;

import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.lang.time.DateFormatUtils;

public class DescktopTips
{
	private ArrayList<DescktopNotify> notifies = new ArrayList<DescktopNotify>();
	private ArrayList<DescktopMessage> messages = new ArrayList<DescktopMessage>();
	private int alarmTips = 0;
	private int notifyTips = 0;
	private long tsNotify;
	private long tsMessage;
	private String exception;
	
	public String getException() {
		return exception;
	}

	public void setException(String exception) {
		this.exception = exception;
	}

	public long getTsMessage() {
		return tsMessage;
	}

	public void setTsMessage(long tsMessage) {
		this.tsMessage = tsMessage;
	}

	public ArrayList<DescktopNotify> getNotifies()
	{
		return notifies;
	}
	
	public ArrayList<DescktopMessage> getMessages() {
		return messages;
	}

	public int getAlarmTips()
	{
		return alarmTips;
	}

	public int getNotifyTips()
	{
		return notifyTips;
	}

	public void setAlarmTips(int alarmTips)
	{
		this.alarmTips = alarmTips;
	}

	public void setNotifyTips(int notifyTips)
	{
		this.notifyTips = notifyTips;
	}

	public long getTsNotify()
	{
		return tsNotify;
	}

	public void setTsNotify(long tsNotify)
	{
		this.tsNotify = tsNotify;
	}

    /**
     * 同步top页日期时间，格式 EEE HH:mm 
     * @return String
     */
    public String getDatetime()
    {
    	Date date = new Date();
    	return DateFormatUtils.format(date, "EEE HH:mm:ss")+"<br>"+DateFormatUtils.format(date, "yyyy年M月d日");
    }
}
