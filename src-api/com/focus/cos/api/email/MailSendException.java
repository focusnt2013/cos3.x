package com.focus.cos.api.email;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.focus.cos.api.ObjectUtils;

public class MailSendException extends MailException
{
	private static final long serialVersionUID = 1L;

	private transient Map<?,?> failedMessages;

	private Exception[] messageExceptions;

	public MailSendException(String msg)
	{
		super(msg);
	}

	public MailSendException(String msg, Throwable cause)
	{
		super(msg, cause);
	}

	public MailSendException(Map<?,?> failedMessages)
	{
		super(null);
		this.failedMessages = new LinkedHashMap<Object,Object>(failedMessages);
		this.messageExceptions = (Exception[]) failedMessages.values().toArray(new Exception[failedMessages.size()]);
	}

	public final Map<?,?> getFailedMessages()
	{
		return (this.failedMessages != null ? this.failedMessages : Collections.EMPTY_MAP);
	}

	public final Exception[] getMessageExceptions()
	{
		return (this.messageExceptions != null ? this.messageExceptions : new Exception[0]);
	}

	public String getMessage()
	{
		if (ObjectUtils.isEmpty(this.messageExceptions))
		{
			return super.getMessage();
		}
		else
		{
			StringBuffer sb = new StringBuffer("Failed messages: ");
			for (int i = 0; i < this.messageExceptions.length; i++)
			{
				Exception subEx = this.messageExceptions[i];
				sb.append(subEx.toString());
				if (i < this.messageExceptions.length - 1)
				{
					sb.append("; ");
				}
			}
			return sb.toString();
		}
	}

	public String toString()
	{
		if (ObjectUtils.isEmpty(this.messageExceptions))
		{
			return super.toString();
		}
		else
		{
			StringBuffer sb = new StringBuffer(getClass().getName());
			sb.append("; nested exceptions (").append(this.messageExceptions.length).append(") are:");
			for (int i = 0; i < this.messageExceptions.length; i++)
			{
				Exception subEx = this.messageExceptions[i];
				sb.append('\n').append("Failed message ").append(i + 1).append(": ");
				sb.append(subEx);
			}
			return sb.toString();
		}
	}

	public void printStackTrace(PrintStream ps)
	{
		if (ObjectUtils.isEmpty(this.messageExceptions))
		{
			super.printStackTrace(ps);
		}
		else
		{
			ps.println(getClass().getName() + "; nested exception details (" + this.messageExceptions.length + ") are:");
			for (int i = 0; i < this.messageExceptions.length; i++)
			{
				Exception subEx = this.messageExceptions[i];
				ps.println("Failed message " + (i + 1) + ":");
				subEx.printStackTrace(ps);
			}
		}
	}

	public void printStackTrace(PrintWriter pw)
	{
		if (ObjectUtils.isEmpty(this.messageExceptions))
		{
			super.printStackTrace(pw);
		}
		else
		{
			pw.println(getClass().getName() + "; nested exception details (" + this.messageExceptions.length + ") are:");
			for (int i = 0; i < this.messageExceptions.length; i++)
			{
				Exception subEx = this.messageExceptions[i];
				pw.println("Failed message " + (i + 1) + ":");
				subEx.printStackTrace(pw);
			}
		}
	}
}
