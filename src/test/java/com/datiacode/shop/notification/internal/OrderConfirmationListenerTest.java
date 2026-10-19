package com.datiacode.shop.notification.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.datiacode.shop.ai.OrderConfirmationRequest;
import com.datiacode.shop.ai.TextGenerationException;
import com.datiacode.shop.ai.TextWriter;
import com.datiacode.shop.order.OrderCompleted;

@ExtendWith(MockitoExtension.class)
class OrderConfirmationListenerTest {

	static final OrderCompleted EVENT = new OrderCompleted(7L, "alice",
			List.of(new OrderCompleted.Line("KB-001", "Mechanical keyboard", 1),
					new OrderCompleted.Line("MS-001", "Wireless mouse", 2)),
			new BigDecimal("149.70"), Instant.parse("2026-10-19T10:00:00Z"));

	@Mock
	TextWriter textWriter;

	@Mock
	EmailSender emailSender;

	@Captor
	ArgumentCaptor<OrderConfirmationRequest> request;

	OrderConfirmationListener listener;

	@BeforeEach
	void setUp() {
		listener = new OrderConfirmationListener(textWriter, emailSender, Duration.ZERO);
	}

	@Test
	@DisplayName("sends the text written by the ai module")
	void sendsGeneratedText() throws Exception {
		given(textWriter.writeOrderConfirmation(any())).willReturn("Dear Alice, thanks!");

		listener.on(EVENT);

		verify(emailSender).send("alice@example.com", "Order #7 confirmed", "Dear Alice, thanks!");
	}

	@Test
	@DisplayName("maps the event to the ai module's own request type")
	void mapsEventToRequest() throws Exception {
		given(textWriter.writeOrderConfirmation(any())).willReturn("Dear Alice, thanks!");

		listener.on(EVENT);

		verify(textWriter).writeOrderConfirmation(request.capture());
		assertThat(request.getValue()).isEqualTo(new OrderConfirmationRequest(7L, "alice",
				List.of(new OrderConfirmationRequest.Line("Mechanical keyboard", 1),
						new OrderConfirmationRequest.Line("Wireless mouse", 2)),
				new BigDecimal("149.70")));
	}

	@Test
	@DisplayName("falls back to a fixed text when the ai module cannot write one, so the email still goes out")
	void fallsBackToFixedText() throws Exception {
		given(textWriter.writeOrderConfirmation(any())).willThrow(new TextGenerationException("Ollama is down"));

		listener.on(EVENT);

		verify(emailSender).send(eq("alice@example.com"), eq("Order #7 confirmed"),
				eq("Thanks for your order: 1 x Mechanical keyboard, 2 x Wireless mouse. Total: 149.70 EUR."));
	}
}
