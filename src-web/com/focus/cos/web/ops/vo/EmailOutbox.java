package com.focus.cos.web.ops.vo;

import java.util.Date;

public class EmailOutbox implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;
	private Long eid;
	private Date requestTime;
	private String mailTo;
	private String cc;
	private String bcc;
	private String module;//对应后台组件模块ID
	private String subject;
	private String content;
	private Short state;
	private byte[] payload;
	private java.sql.Blob data;
	private String attachment;

	public String getAttachment()
	{
		return attachment;
	}

	public void setAttachment(String attachment)
	{
		this.attachment = attachment;
	}

	public java.sql.Blob getData()
	{
		return data;
	}

	/** default constructor */
	public EmailOutbox()
	{
		this.requestTime = new Date();
		this.state = 0;
	}

	/** full constructor */
	public EmailOutbox(Date requestTime, String mailTo, String cc, String bcc, String subject, String content, Short state)
	{
		this.requestTime = requestTime;
		this.mailTo = mailTo;
		this.cc = cc;
		this.bcc = bcc;
		this.subject = subject;
		this.content = content;
		this.state = state;
	}

	// Property accessors
	public Long getEid()
	{
		return this.eid;
	}

	public void setEid(Long eid)
	{
		this.eid = eid;
	}

	public Date getRequestTime()
	{
		return this.requestTime;
	}

	public void setRequestTime(Date requestTime)
	{
		this.requestTime = requestTime;
	}

	public String getMailTo()
	{
		return this.mailTo;
	}

	public void setMailTo(String mailTo)
	{
		this.mailTo = mailTo;
	}

	public String getCc()
	{
		return this.cc;
	}

	public void setCc(String cc)
	{
		this.cc = cc;
	}

	public String getBcc()
	{
		return this.bcc;
	}

	public void setBcc(String bcc)
	{
		this.bcc = bcc;
	}

	public String getSubject()
	{
		return this.subject;
	}

	public void setSubject(String subject)
	{
		this.subject = subject;
	}

	public String getContent()
	{
		return this.content;
	}

	public void setContent(String content)
	{
		this.content = content;
	}

	public Short getState()
	{
		return this.state;
	}

	public void setState(Short state)
	{
		this.state = state;
	}
	
	public byte[] getPayload()
	{
		return payload;
	}

	public void setPayload(byte[] payload)
	{
		this.payload = payload;
	}

	public void setData(java.sql.Blob data)
	{
		this.data = data;
	}
	public String getStatus()
	{
		switch(state)
		{
		case 0:
			return "待发送";
		case 1:
			return "已发送";
		case 2:
			return "邮件内容错误";
		case 3:
			return "邮件地址错误";
		default:
			return "未知错误";
		}
	}

	public String getModule()
	{
		return module;
	}

	public void setModule(String module)
	{
		this.module = module;
	}
}