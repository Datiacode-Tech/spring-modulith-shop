package com.datiacode.shop.ai;

/**
 * No usable text could be generated: the model failed, timed out or answered something that cannot be sent.
 */
public class TextGenerationException extends RuntimeException {

	public TextGenerationException(String message) {
		super(message);
	}

	public TextGenerationException(String message, Throwable cause) {
		super(message, cause);
	}
}
