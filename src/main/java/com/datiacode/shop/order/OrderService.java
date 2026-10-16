package com.datiacode.shop.order;

import java.util.Optional;

/**
 * Public API of the order module.
 */
public interface OrderService {

	/**
	 * Places an order if every item is in stock.
	 *
	 * @param order customer and requested items
	 * @return the stored order
	 * @throws InsufficientStockException if any item is unknown or not available in the requested quantity
	 */
	OrderDetails create(NewOrder order);

	Optional<OrderDetails> findById(Long id);
}
