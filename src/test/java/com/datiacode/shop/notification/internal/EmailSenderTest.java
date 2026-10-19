package com.datiacode.shop.notification.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class EmailSenderTest {

	@Mock
	JavaMailSender mailSender;

	@Captor
	ArgumentCaptor<SimpleMailMessage> message;

	@Test
	@DisplayName("sends a plain text email from the configured address")
	void sendsPlainTextEmail() {
		var sender = new EmailSender(mailSender, "orders@shop.example");

		sender.send("alice@example.com", "Order #7 confirmed", "Dear Alice, thanks!");

		verify(mailSender).send(message.capture());
		assertThat(message.getValue().getFrom()).isEqualTo("orders@shop.example");
		assertThat(message.getValue().getTo()).containsExactly("alice@example.com");
		assertThat(message.getValue().getSubject()).isEqualTo("Order #7 confirmed");
		assertThat(message.getValue().getText()).isEqualTo("Dear Alice, thanks!");
	}
}
