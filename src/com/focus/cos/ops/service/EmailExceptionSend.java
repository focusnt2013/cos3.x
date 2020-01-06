package com.focus.cos.ops.service;

public class EmailExceptionSend extends Exception
{
	private static final long serialVersionUID = 3010793721693548218L;

	public EmailExceptionSend(Exception e)
	{
		super(e);
	}
}
