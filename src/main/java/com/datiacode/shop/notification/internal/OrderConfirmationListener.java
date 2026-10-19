package com.datiacode.shop.notification.internal;

import java.time.Duration;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.datiacode.shop.ai.OrderConfirmationRequest;
import com.datiacode.shop.ai.TextGenerationException;
import com.datiacode.shop.ai.TextWriter;
import com.datiacode.shop.order.OrderCompleted;

/**
 * Second consumer of {@link OrderCompleted}: adding it did not require any change in the order module.
 */
@Component
class OrderConfirmationListener {

	private static final Logger log = LoggerFactory.getLogger(OrderConfirmationListener.class);

	private final TextWriter textWriter;
	private final EmailSender emailSender;
	private final Duration demoDelay;

	/**
	 * @param demoDelay artificial pause before sending, only set by the {@code demo} profile to have time to kill the
	 * application while the event is being handled
	 */
	OrderConfirmationListener(TextWriter textWriter, EmailSender emailSender,
			@Value("${shop.demo.notification-delay:0s}") Duration demoDelay) {
		this.textWriter = textWriter;
		this.emailSender = emailSender;
		this.demoDelay = demoDelay;
	}

	@ApplicationModuleListener
	void on(OrderCompleted event) throws InterruptedException {
		// Runs on another thread: user and trace id arrive through context propagation (see ContextPropagationConfig)
		var authentication = SecurityContextHolder.getContext().getAuthentication();
		log.info("Order {} confirmation requested by user {}", event.orderId(),
				authentication != null ? authentication.getName() : "<none: republished after restart>");

		Thread.sleep(demoDelay);

		emailSender.send(event.customer() + "@example.com",
				"Order #%d confirmed".formatted(event.orderId()),
				writeBody(event));
	}

	private String writeBody(OrderCompleted event) {
		try {
			return textWriter.writeOrderConfirmation(toRequest(event));
		}
		catch (TextGenerationException ex) {
			// The AI text is a nice-to-have: the confirmation must go out even if the model is down or misbehaves
			log.warn("Order {}: using the fixed confirmation text ({})", event.orderId(), ex.getMessage());
			return fallbackBody(event);
		}
	}

	private static OrderConfirmationRequest toRequest(OrderCompleted event) {
		var lines = event.lines().stream()
				.map(line -> new OrderConfirmationRequest.Line(line.productName(), line.quantity()))
				.toList();
		return new OrderConfirmationRequest(event.orderId(), event.customer(), lines, event.total());
	}

	private static String fallbackBody(OrderCompleted event) {
		var items = event.lines().stream()
				.map(line -> line.quantity() + " x " + line.productName())
				.collect(Collectors.joining(", "));
		return "Thanks for your order: %s. Total: %s EUR.".formatted(items, event.total());
	}
}
