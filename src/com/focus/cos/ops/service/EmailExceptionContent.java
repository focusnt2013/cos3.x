package com.focus.cos.ops.service;

public class EmailExceptionContent extends Exception
{
	private static final long serialVersionUID = -4576565876722389057L;
	
	public EmailExceptionContent(Exception e)
	{
		super(e);
	}
	public EmailExceptionContent(String remark)
	{
		super(remark);
	}
}
