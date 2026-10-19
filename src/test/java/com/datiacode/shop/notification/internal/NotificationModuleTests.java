package com.datiacode.shop.notification.internal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.datiacode.shop.TestcontainersConfiguration;
import com.datiacode.shop.ai.TextWriter;
import com.datiacode.shop.order.OrderCompleted;

/**
 * Boots only the notification module and checks it reacts to {@link OrderCompleted} without the order module running.
 */
@ApplicationModuleTest
@Import(TestcontainersConfiguration.class)
class NotificationModuleTests {

	@MockitoBean
	EmailSender emailSender;

	// Belongs to the ai module, which is not bootstrapped here
	@MockitoBean
	TextWriter textWriter;

	@Test
	@DisplayName("OrderCompleted sends a confirmation email to the customer")
	void orderCompletedSendsConfirmation(Scenario scenario) {
		var event = new OrderCompleted(7L, "alice",
				List.of(new OrderCompleted.Line("KB-001", "Mechanical keyboard", 1)), new BigDecimal("89.90"),
				Instant.now());
		given(textWriter.writeOrderConfirmation(any())).willReturn("Dear Alice, your Mechanical keyboard is confirmed.");

		scenario.publish(event)
				.andWaitForStateChange(() -> !mockingDetails(emailSender).getInvocations().isEmpty())
				.andVerify(sent -> verify(emailSender).send(eq("alice@example.com"), eq("Order #7 confirmed"),
						contains("Mechanical keyboard is confirmed")));
	}
}
