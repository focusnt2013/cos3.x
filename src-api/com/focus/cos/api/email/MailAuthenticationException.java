package com.focus.cos.api.email;

public class MailAuthenticationException extends MailException
{
	private static final long serialVersionUID = 1L;

	public MailAuthenticationException(String msg)
	{
		super(msg);
	}

	public MailAuthenticationException(String msg, Throwable cause)
	{
		super(msg, cause);
	}

	public MailAuthenticationException(Throwable cause)
	{
		super("Authentication failed", cause);
	}

}
