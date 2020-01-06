package com.focus.cos.api;

import java.io.Serializable;
import java.util.Date;

public class Sysemail implements Serializable
{
	private static final long serialVersionUID = -7994060634763532562L;
	private Long eid;
	private Date requestTime;
	private String mailTo;
	private String cc;
	private String bcc;
	private String module;//对应后台组件模块ID
	private String subject;
	private String content;
	private Short state = 0;
	private String attachments;
	private byte[] data;
	private String result;
	
	public static final Integer MailType_Text = 0;
	public static final Integer MailType_HTML = 1;
	public static final Integer MailType_MIX = 2; 
	
	public String getAttachments() {
		return attachments;
	}
	public void setAttachments(String attachments) {
		this.attachments = attachments;
	}
	public Long getEid()
	{
		return eid;
	}
	public void setEid(Long eid)
	{
		this.eid = eid;
	}
	public Date getRequestTime()
	{
		return requestTime;
	}
	public void setRequestTime(Date requestTime)
	{
		this.requestTime = requestTime;
	}
	public String getMailTo()
	{
		return mailTo;
	}
	public void setMailTo(String mailTo)
	{
		this.mailTo = mailTo;
	}
	public String getCc()
	{
		return cc;
	}
	public void setCc(String cc)
	{
		this.cc = cc;
	}
	public String getBcc()
	{
		return bcc;
	}
	public void setBcc(String bcc)
	{
		this.bcc = bcc;
	}
	public String getSubject()
	{
		return subject;
	}
	public void setSubject(String subject)
	{
		this.subject = subject;
	}
	public String getContent()
	{
		return content;
	}
	public void setContent(String content)
	{
		this.content = content;
	}
	public Short getState()
	{
		return state;
	}
	public void setState(Short state)
	{
		this.state = state;
	}
	public byte[] getData()
	{
		return data;
	}
	public void setData(byte[] data)
	{
		this.data = data;
	}
	public String getSysid()
	{
		return module;
	}
	public void setSysid(String sysid)
	{
		this.module = sysid;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
}
