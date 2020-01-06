package com.focus.cos.api.email.javamail;

import javax.mail.internet.MimeMessage;

public interface MimeMessagePreparator
{
	void prepare(MimeMessage mimeMessage) throws Exception;
}
