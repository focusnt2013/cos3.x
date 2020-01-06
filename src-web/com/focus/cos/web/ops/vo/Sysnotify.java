package com.focus.cos.web.ops.vo;

import java.util.Date;

import org.ocpsoft.prettytime.PrettyTime;

import com.focus.util.Tools;

public class Sysnotify 
{
    private long nid;
    private String useraccount;
    private String title;
    private String filter;
    private Date notifytime;
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
    
    public String getTime()
    {
    	if( this.notifytime != null )
    	{
    		return Tools.getFormatTime("MM-dd HH:mm", this.notifytime.getTime());
    	}
    	return "Unknown time.";
    }
    
    public String getPrettytime()
    {
		return Tools.replaceStr(PrettyTime.getInstance().format(notifytime), " ", "");
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
		return icon!=null?icon:(this.contextimg!=null?this.contextimg:null);
	}

	public void setIcon(String icon)
	{
		this.icon = icon;
	}

	public String getContextimg()
	{
		return !this.filter.equals("韩寒鸡汤")?contextimg:null;
	}

	public void setContextimg(String contextimg)
	{
		this.contextimg = contextimg;
	}
	
	public boolean isSame()
	{
		String md51 = "";
		String md52 = "";
		if( this.title != null ) md51 = Tools.encodeMD5(this.title);
		if( this.context != null ) md52 = Tools.encodeMD5(this.context);
		return md51.equals(md52);
	}
}
