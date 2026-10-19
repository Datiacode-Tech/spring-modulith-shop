package com.datiacode.shop.ai.internal;

import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import com.datiacode.shop.ai.OrderConfirmationRequest;
import com.datiacode.shop.ai.TextGenerationException;
import com.datiacode.shop.ai.TextWriter;

/**
 * {@link TextWriter} backed by Spring AI. The only class in the application that knows about {@link ChatClient}.
 * <p>
 * The HTTP timeout to the model is {@code spring.http.clients.read-timeout}: Spring AI builds its client from the
 * {@code RestClient.Builder} that Spring Boot configures.
 */
@Component
class ChatClientTextWriter implements TextWriter {

	static final String SYSTEM_PROMPT = """
			You write order confirmation emails for an online shop called Spring Modulith Shop.
			Rules:
			- Plain text only, at most 80 words, friendly and concise.
			- Start directly with the greeting. No preamble such as "Here is the email", no subject line.
			- Mention every product with its quantity and the total exactly as given.
			- Sign as "The Spring Modulith Shop team".
			- Never use placeholders in square brackets such as [Your Name]; leave out anything you do not know.
			- Do not invent delivery dates, tracking numbers or discounts.
			""";

	static final String USER_PROMPT = """
			Customer: {customer}
			Order: #{orderId}
			Products: {products}
			Total: {total} EUR
			""";

	// Small models often open with "Here is a short email:" even when told not to
	private static final Pattern PREAMBLE = Pattern.compile("^(here is|here's|sure)[^\\n]*:\\s*",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern PLACEHOLDER = Pattern.compile("\\[[^\\]\\n]{2,40}]");

	private final ChatClient chatClient;

	ChatClientTextWriter(ChatClient.Builder chatClientBuilder) {
		this.chatClient = chatClientBuilder.defaultSystem(SYSTEM_PROMPT).build();
	}

	@Override
	public String writeOrderConfirmation(OrderConfirmationRequest request) {
		var products = request.lines().stream()
				.map(line -> line.quantity() + " x " + line.productName())
				.collect(Collectors.joining(", "));

		String text;
		try {
			text = chatClient.prompt()
					.user(user -> user.text(USER_PROMPT)
							.param("customer", request.customer())
							.param("orderId", request.orderId())
							.param("products", products)
							.param("total", request.total().toPlainString()))
					.call()
					.content();
		}
		catch (RuntimeException ex) {
			throw new TextGenerationException("The model could not write the confirmation of order #"
					+ request.orderId(), ex);
		}
		return clean(text, request.orderId());
	}

	private static String clean(String text, Long orderId) {
		var cleaned = PREAMBLE.matcher(text == null ? "" : text.strip()).replaceFirst("").strip();
		if (cleaned.isEmpty()) {
			throw new TextGenerationException("The model returned an empty text for order #" + orderId);
		}
		var placeholder = PLACEHOLDER.matcher(cleaned);
		if (placeholder.find()) {
			throw new TextGenerationException("The model left the placeholder %s in the text for order #%d"
					.formatted(placeholder.group(), orderId));
		}
		return cleaned;
	}
}
