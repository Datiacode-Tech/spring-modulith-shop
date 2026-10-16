package com.datiacode.shop.order;

import java.util.Optional;

/**
 * Public API of the order module.
 */
public interface OrderService {

	/**
	 * Places an order and publishes {@link OrderCompleted}. Stock is not checked here: the inventory module reacts to
	 * the event (eventual consistency).
	 *
	 * @param order customer and requested items
	 * @return the stored order
	 * @throws UnknownProductException if any item is not in the catalog
	 */
	OrderDetails create(NewOrder order);

	Optional<OrderDetails> findById(Long id);
}
