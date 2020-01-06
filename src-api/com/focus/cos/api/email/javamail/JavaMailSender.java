package com.focus.cos.api.email.javamail;

import java.io.InputStream;

import javax.mail.internet.MimeMessage;

import com.focus.cos.api.email.IMailSender;
import com.focus.cos.api.email.MailException;

public interface JavaMailSender extends IMailSender
{
	MimeMessage createMimeMessage();

	MimeMessage createMimeMessage(InputStream contentStream) throws MailException;

	void send(MimeMessage mimeMessage) throws MailException;

	void send(MimeMessage[] mimeMessages) throws MailException;

	void send(MimeMessagePreparator mimeMessagePreparator) throws MailException;

	void send(MimeMessagePreparator[] mimeMessagePreparators) throws MailException;
}
