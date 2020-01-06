package com.focus.cos.web.ops.vo;

/**
 * 桌面消息通知
 * @author focus
 *
 */
public class DescktopNotify
{
    private long nid;
    private String dn;
    private String account;
    private String title;
    private String filter;
    private String time;
    private String prettytime;
	private String icon;
    private boolean read;
    public boolean isRead() {
		return read;
	}
	public void setRead(boolean read) {
		this.read = read;
	}
	private long timestamp;
    
	public long getNid()
	{
		return nid;
	}
	public String getAccount()
	{
		return account;
	}
	public String getTitle()
	{
		return title;
	}
	public String getFilter()
	{
		return filter;
	}
	public String getIcon()
	{
		return icon;
	}
	public void setNid(long nid)
	{
		this.nid = nid;
	}
	public void setAccount(String account)
	{
		this.account = account;
	}
	public void setTitle(String title)
	{
		this.title = title;
	}
	public void setFilter(String filter)
	{
		this.filter = filter;
	}
	public void setIcon(String icon, String def)
	{
		this.icon = (icon!=null&&!icon.isEmpty())?icon:def;
	}
	public String getTime()
	{
		return time;
	}
	public void setTime(String time)
	{
		this.time = time;
	}
	public long getTimestamp()
	{
		return timestamp;
	}
	public void setTimestamp(long timestamp)
	{
		this.timestamp = timestamp;
	}
	public String getDn()
	{
		return dn;
	}
	public void setDn(String dn)
	{
		this.dn = dn;
	}
    public String getPrettytime() {
		return prettytime;
	}
	public void setPrettytime(String prettytime) {
		this.prettytime = prettytime;
	}
}
