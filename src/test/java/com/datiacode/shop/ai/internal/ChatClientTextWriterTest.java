package com.datiacode.shop.ai.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;

import com.datiacode.shop.ai.OrderConfirmationRequest;
import com.datiacode.shop.ai.TextGenerationException;

/**
 * The {@link ChatModel} is mocked, the {@link ChatClient} on top of it is real: the test sees the exact prompt sent to
 * the model without stubbing the fluent API.
 */
@ExtendWith(MockitoExtension.class)
class ChatClientTextWriterTest {

	static final OrderConfirmationRequest REQUEST = new OrderConfirmationRequest(7L, "alice",
			List.of(new OrderConfirmationRequest.Line("Mechanical keyboard", 1),
					new OrderConfirmationRequest.Line("Wireless mouse", 2)),
			new BigDecimal("149.70"));

	@Mock
	ChatModel chatModel;

	@Captor
	ArgumentCaptor<Prompt> prompt;

	ChatClientTextWriter writer;

	@BeforeEach
	void setUp() {
		// ChatClient merges its options into the model's defaults, so the mock needs some
		given(chatModel.getOptions()).willReturn(ChatOptions.builder().build());
		writer = new ChatClientTextWriter(ChatClient.builder(chatModel));
	}

	@Test
	@DisplayName("sends a system prompt with the rules and a user message with the order details")
	void sendsSystemRulesAndOrderDetails() {
		modelAnswers("Dear Alice, thanks for your order.");

		writer.writeOrderConfirmation(REQUEST);

		verify(chatModel).call(prompt.capture());
		var system = textOf(MessageType.SYSTEM);
		var user = textOf(MessageType.USER);
		assertThat(system).contains("Spring Modulith Shop", "placeholder", "preamble");
		assertThat(user).contains("alice", "#7", "1 x Mechanical keyboard", "2 x Wireless mouse", "149.70 EUR");
	}

	@Test
	@DisplayName("returns the model's text trimmed")
	void returnsTrimmedText() {
		modelAnswers("  Dear Alice, thanks for your order.\n");

		assertThat(writer.writeOrderConfirmation(REQUEST)).isEqualTo("Dear Alice, thanks for your order.");
	}

	@Test
	@DisplayName("drops a chatty preamble line such as 'Here is a short email:'")
	void dropsPreamble() {
		modelAnswers("Here is a short, friendly order confirmation email:\n\nDear Alice, thanks for your order.");

		assertThat(writer.writeOrderConfirmation(REQUEST)).isEqualTo("Dear Alice, thanks for your order.");
	}

	@Test
	@DisplayName("rejects a text with template placeholders, so the caller falls back to its fixed text")
	void rejectsPlaceholders() {
		modelAnswers("Dear Alice, thanks for your order.\n\nBest regards,\n[Your Company Name]");

		assertThatThrownBy(() -> writer.writeOrderConfirmation(REQUEST))
				.isInstanceOf(TextGenerationException.class)
				.hasMessageContaining("placeholder");
	}

	@Test
	@DisplayName("rejects an empty answer")
	void rejectsEmptyAnswer() {
		modelAnswers("   ");

		assertThatThrownBy(() -> writer.writeOrderConfirmation(REQUEST))
				.isInstanceOf(TextGenerationException.class);
	}

	@Test
	@DisplayName("wraps model failures (timeout, connection refused...) in TextGenerationException")
	void wrapsModelFailures() {
		var failure = new IllegalStateException("I/O error: Read timed out");
		given(chatModel.call(any(Prompt.class))).willThrow(failure);

		assertThatThrownBy(() -> writer.writeOrderConfirmation(REQUEST))
				.isInstanceOf(TextGenerationException.class)
				.hasCause(failure);
	}

	private void modelAnswers(String text) {
		given(chatModel.call(any(Prompt.class)))
				.willReturn(new ChatResponse(List.of(new Generation(new AssistantMessage(text)))));
	}

	private String textOf(MessageType type) {
		return prompt.getValue().getInstructions().stream()
				.filter(message -> message.getMessageType() == type)
				.map(message -> message.getText())
				.findFirst()
				.orElseThrow();
	}
}
