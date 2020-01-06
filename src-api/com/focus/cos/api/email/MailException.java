package com.focus.cos.api.email;

public abstract class MailException extends RuntimeException
{
	private static final long serialVersionUID = 8115461968648930121L;

	public MailException(String msg)
	{
		super(msg);
	}
	
	public MailException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}
