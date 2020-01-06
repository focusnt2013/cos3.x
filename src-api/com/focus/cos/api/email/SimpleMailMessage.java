package com.focus.cos.api.email;

import java.io.Serializable;
import java.util.Date;

public class SimpleMailMessage implements MailMessage, Serializable
{
	private static final long serialVersionUID = 1L;

	private String from;
	private String replyTo;
	private String[] to;
	private String[] cc;
	private String[] bcc;
	private Date sentDate;
	private String subject;
	private String text;

	public SimpleMailMessage()
	{
	}

	public SimpleMailMessage(SimpleMailMessage original)
	{
		this.from = original.getFrom();
		this.replyTo = original.getReplyTo();
		if (original.getTo() != null)
		{
			this.to = copy(original.getTo());
		}
		if (original.getCc() != null)
		{
			this.cc = copy(original.getCc());
		}
		if (original.getBcc() != null)
		{
			this.bcc = copy(original.getBcc());
		}
		this.sentDate = original.getSentDate();
		this.subject = original.getSubject();
		this.text = original.getText();
	}

	public void setFrom(String from)
	{
		this.from = from;
	}

	public String getFrom()
	{
		return this.from;
	}

	public void setReplyTo(String replyTo)
	{
		this.replyTo = replyTo;
	}

	public String getReplyTo()
	{
		return replyTo;
	}

	public void setTo(String to)
	{
		this.to = new String[] { to };
	}

	public void setTo(String[] to)
	{
		this.to = to;
	}

	public String[] getTo()
	{
		return this.to;
	}

	public void setCc(String cc)
	{
		this.cc = new String[] { cc };
	}

	public void setCc(String[] cc)
	{
		this.cc = cc;
	}

	public String[] getCc()
	{
		return cc;
	}

	public void setBcc(String bcc)
	{
		this.bcc = new String[] { bcc };
	}

	public void setBcc(String[] bcc)
	{
		this.bcc = bcc;
	}

	public String[] getBcc()
	{
		return bcc;
	}

	public void setSentDate(Date sentDate)
	{
		this.sentDate = sentDate;
	}

	public Date getSentDate()
	{
		return sentDate;
	}

	public void setSubject(String subject)
	{
		this.subject = subject;
	}

	public String getSubject()
	{
		return this.subject;
	}

	public void setText(String text)
	{
		this.text = text;
	}

	public String getText()
	{
		return this.text;
	}

	public void copyTo(MailMessage target)
	{
		if (getFrom() != null)
		{
			target.setFrom(getFrom());
		}
		if (getReplyTo() != null)
		{
			target.setReplyTo(getReplyTo());
		}
		if (getTo() != null)
		{
			target.setTo(getTo());
		}
		if (getCc() != null)
		{
			target.setCc(getCc());
		}
		if (getBcc() != null)
		{
			target.setBcc(getBcc());
		}
		if (getSentDate() != null)
		{
			target.setSentDate(getSentDate());
		}
		if (getSubject() != null)
		{
			target.setSubject(getSubject());
		}
		if (getText() != null)
		{
			target.setText(getText());
		}
	}

	private static String[] copy(String[] state)
	{
		String[] copy = new String[state.length];
		System.arraycopy(state, 0, copy, 0, state.length);
		return copy;
	}
}
