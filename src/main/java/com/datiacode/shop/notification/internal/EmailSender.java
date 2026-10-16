package com.datiacode.shop.notification.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Sends emails. For now it only logs them; a real SMTP sender (Mailpit) replaces it in a later phase.
 */
@Component
class EmailSender {

	private static final Logger log = LoggerFactory.getLogger(EmailSender.class);

	void send(String to, String subject, String body) {
		log.info("Email to {} | {} | {}", to, subject, body);
	}
}
