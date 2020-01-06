package com.focus.cos.api;

import java.io.Serializable;
import java.util.Date;

public class Sysnotify implements Serializable
{
	private static final long serialVersionUID = -9066055896570227474L;
	private long nid;
    private String useraccount;
    private String title;
    private String filter;
    private Date notifytime = new Date();
    private String context;
    private String contextlink;
    /*Add by liuxue at 2016-6-30*/
    private String contextimg;
    private String action;
    private String actionlink;
    private String icon;
    private int state;
    private int priority;
    public long getNid()
    {
        return nid;
    }

    public String getUseraccount()
    {
        return useraccount;
    }

    public String getTitle()
    {
        return title;
    }

    public String getFilter()
    {
        return filter;
    }

    public Date getNotifytime()
    {
        return notifytime;
    }

    public String getContext()
    {
        return context;
    }

    public String getContextlink()
    {
        return contextlink!=null?contextlink.trim():contextlink;
    }

    public String getAction()
    {
        return action;
    }

    public String getActionlink()
    {
        return actionlink!=null?actionlink.trim():actionlink;
    }

    public int getState()
    {
        return state;
    }

    public int getPriority()
    {
        return priority;
    }

    public void setNid(long nid)
    {
        this.nid = nid;
    }

    public void setUseraccount(String useraccount)
    {
        this.useraccount = useraccount;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public void setFilter(String filter)
    {
        this.filter = filter;
    }

    public void setNotifytime(Date notifytime)
    {
        this.notifytime = notifytime;
    }

    public void setContext(String context)
    {
        this.context = context;
    }

    public void setContextlink(String contextlink)
    {
        this.contextlink = contextlink;
    }

    public void setAction(String action)
    {
        this.action = action;
    }

    public void setActionlink(String actionlink)
    {
        this.actionlink = actionlink;
    }

    public void setState(int state)
    {
        this.state = state;
    }

    public void setPriority(int priority)
    {
        this.priority = priority;
    }
    
    public long getTimestamp()
    {
    	if( this.notifytime != null )
    	{
    		return notifytime.getTime();
    	}
    	return 0;
    }

    public String toString()
    {
    	return "nid="+this.nid+",useraccount="+this.useraccount+",filter="+this.filter+",title="+this.title;
    }

	public String getIcon()
	{
		return icon;
	}

	public void setIcon(String icon)
	{
		this.icon = icon;
	}

	public String getContextimg()
	{
		return contextimg;
	}

	public void setContextimg(String contextimg)
	{
		this.contextimg = contextimg;
	}
}
