package com.focus.cos.api.email;

public class MailParseException extends MailException
{
	private static final long serialVersionUID = 1L;

	public MailParseException(String msg)
	{
		super(msg);
	}

	public MailParseException(String msg, Throwable cause)
	{
		super(msg, cause);
	}

	public MailParseException(Throwable cause)
	{
		super("Could not parse mail", cause);
	}
}
