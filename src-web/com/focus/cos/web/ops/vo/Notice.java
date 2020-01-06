package com.focus.cos.web.ops.vo;

import java.util.Date;

public class Notice implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;
	private Long id;
	private String title;
	private String content;
	private Date addTime;
	private Date publishTime;
	private String attachName;
	private String attachUri;
	private Byte state;

	/** default constructor */
	public Notice()
	{
		addTime = new Date();
		this.state = 0;
	}

	/** minimal constructor */
	public Notice(String title, String content, Date addTime)
	{
		this.title = title;
		this.content = content;
		this.addTime = addTime;
	}

	/** full constructor */
	public Notice(String title, String content, Date addTime, Date publishTime, String attachName, String attachUri, Byte state)
	{
		this.title = title;
		this.content = content;
		this.addTime = addTime;
		this.publishTime = publishTime;
		this.attachName = attachName;
		this.attachUri = attachUri;
		this.state = state;
	}

	// Property accessors
	public Long getId()
	{
		return this.id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public String getTitle()
	{
		return this.title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getContent()
	{
		return this.content;
	}

	public void setContent(String content)
	{
		this.content = content;
	}

	public Date getAddTime()
	{
		return this.addTime;
	}

	public void setAddTime(Date addTime)
	{
		this.addTime = addTime;
	}

	public Date getPublishTime()
	{
		return publishTime;
	}

	public void setPublishTime(Date publishTime)
	{
		this.publishTime = publishTime;
	}

	public String getAttachName()
	{
		return this.attachName;
	}

	public void setAttachName(String attachName)
	{
		this.attachName = attachName;
	}

	public String getAttachUri()
	{
		return this.attachUri;
	}

	public void setAttachUri(String attachUri)
	{
		this.attachUri = attachUri;
	}

	public Byte getState()
	{
		return this.state;
	}

	public void setState(Byte state)
	{
		this.state = state;
	}

}