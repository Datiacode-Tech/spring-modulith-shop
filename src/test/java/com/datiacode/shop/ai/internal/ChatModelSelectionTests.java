package com.datiacode.shop.ai.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.datiacode.shop.TestcontainersConfiguration;

/**
 * Two Spring AI providers are on the classpath; the profile alone decides which {@link ChatModel} exists. No model is
 * called: only the wiring is checked.
 */
class ChatModelSelectionTests {

	@Nested
	@SpringBootTest
	@Import(TestcontainersConfiguration.class)
	@DisplayName("by default")
	class Default {

		@Autowired
		ApplicationContext context;

		@Test
		@DisplayName("the only chat model is the local Ollama one")
		void usesOllama() {
			assertThat(context.getBeansOfType(ChatModel.class).values())
					.singleElement().isInstanceOf(OllamaChatModel.class);
		}
	}

	@Nested
	@SpringBootTest(properties = "OPENROUTER_API_KEY=test-key")
	@ActiveProfiles({ "test", "cloud" })
	@Import(TestcontainersConfiguration.class)
	@DisplayName("with the cloud profile")
	class Cloud {

		@Autowired
		ApplicationContext context;

		@Test
		@DisplayName("the only chat model is the OpenAI-compatible one pointing at OpenRouter")
		void usesOpenRouter() {
			var chatModels = context.getBeansOfType(ChatModel.class).values();
			assertThat(chatModels).singleElement().isInstanceOf(OpenAiChatModel.class);
			assertThat(context.getEnvironment().getProperty("spring.ai.openai.base-url"))
					.isEqualTo("https://openrouter.ai/api/v1");
			assertThat(((OpenAiChatModel) chatModels.iterator().next()).getDefaultOptions().getModel())
					.isEqualTo("mistralai/mistral-small-24b-instruct-2501");
		}
	}
}
