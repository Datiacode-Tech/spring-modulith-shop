package com.datiacode.shop.notification.internal;

import java.time.Duration;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import com.datiacode.shop.order.OrderCompleted;

/**
 * Second consumer of {@link OrderCompleted}: adding it did not require any change in the order module.
 */
@Component
class OrderConfirmationListener {

	private final EmailSender emailSender;
	private final Duration demoDelay;

	/**
	 * @param demoDelay artificial pause before sending, only set by the {@code demo} profile to have time to kill the
	 * application while the event is being handled
	 */
	OrderConfirmationListener(EmailSender emailSender,
			@Value("${shop.demo.notification-delay:0s}") Duration demoDelay) {
		this.emailSender = emailSender;
		this.demoDelay = demoDelay;
	}

	@ApplicationModuleListener
	void on(OrderCompleted event) throws InterruptedException {
		Thread.sleep(demoDelay);

		var items = event.lines().stream()
				.map(line -> line.quantity() + " x " + line.productName())
				.collect(Collectors.joining(", "));

		emailSender.send(event.customer() + "@example.com",
				"Order #%d confirmed".formatted(event.orderId()),
				"Thanks for your order: %s. Total: %s EUR.".formatted(items, event.total()));
	}
}
