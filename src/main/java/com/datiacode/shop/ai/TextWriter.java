package com.datiacode.shop.ai;

/**
 * Writes customer-facing texts. The rest of the application only sees this interface: which model (or whether there
 * is a model at all) is an internal detail of the ai module.
 */
public interface TextWriter {

	/**
	 * @return the body of the order confirmation email, as plain text
	 * @throws TextGenerationException if no usable text could be produced; callers are expected to fall back to a
	 * fixed text
	 */
	String writeOrderConfirmation(OrderConfirmationRequest request);
}
