package com.datiacode.shop.inventory.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import com.datiacode.shop.order.OrderCompleted;

/**
 * Takes the ordered units out of stock once the order transaction has committed.
 * <p>
 * Stock is not checked before accepting an order (eventual consistency), so a shortage is possible here. It is logged
 * and the event is considered handled: throwing would leave the publication incomplete and retry it forever.
 */
@Component
class OrderCompletedListener {

	private static final Logger log = LoggerFactory.getLogger(OrderCompletedListener.class);

	private final StockItemRepository stock;

	OrderCompletedListener(StockItemRepository stock) {
		this.stock = stock;
	}

	@ApplicationModuleListener
	void on(OrderCompleted event) {
		event.lines().forEach(line -> stock.findById(line.sku()).ifPresentOrElse(
				item -> take(item, line.quantity(), event.orderId()),
				() -> log.warn("Order {}: product {} is not tracked in inventory", event.orderId(), line.sku())));
	}

	private void take(StockItem item, int units, Long orderId) {
		if (item.hasAtLeast(units)) {
			item.decrease(units);
			log.info("Order {}: took {} x {} from stock, {} left", orderId, units, item.getSku(), item.getQuantity());
		}
		else {
			log.warn("Order {}: stock shortage for {} (requested {}, available {})", orderId, item.getSku(), units,
					item.getQuantity());
		}
	}
}
