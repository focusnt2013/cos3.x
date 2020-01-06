package com.focus.cos.api.email;

public interface IMailSender
{
	void send(SimpleMailMessage simpleMessage) throws MailException;
	void send(SimpleMailMessage[] simpleMessages) throws MailException;
}
