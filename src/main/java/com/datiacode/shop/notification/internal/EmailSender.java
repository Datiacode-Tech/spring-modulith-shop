package com.datiacode.shop.notification.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Sends plain text emails over SMTP (Mailpit in development).
 * <p>
 * A send failure is not caught: it fails the listener, so the Event Publication Registry keeps the publication
 * incomplete and the email is retried on restart.
 */
@Component
class EmailSender {

	private static final Logger log = LoggerFactory.getLogger(EmailSender.class);

	private final JavaMailSender mailSender;
	private final String from;

	EmailSender(JavaMailSender mailSender,
			@Value("${shop.notification.from:orders@spring-modulith-shop.example}") String from) {
		this.mailSender = mailSender;
		this.from = from;
	}

	void send(String to, String subject, String body) {
		var message = new SimpleMailMessage();
		message.setFrom(from);
		message.setTo(to);
		message.setSubject(subject);
		message.setText(body);
		mailSender.send(message);
		log.info("Email sent to {} | {}", to, subject);
	}
}
