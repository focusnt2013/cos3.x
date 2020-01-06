package com.focus.cos.api.email;

public class MailPreparationException extends MailException
{
	private static final long serialVersionUID = 1L;

	public MailPreparationException(String msg)
	{
		super(msg);
	}

	public MailPreparationException(String msg, Throwable cause)
	{
		super(msg, cause);
	}

	public MailPreparationException(Throwable cause)
	{
		super("Could not prepare mail", cause);
	}
}
