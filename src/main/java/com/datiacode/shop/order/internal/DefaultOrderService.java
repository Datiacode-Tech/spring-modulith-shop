package com.datiacode.shop.order.internal;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.datiacode.shop.catalog.CatalogService;
import com.datiacode.shop.order.NewOrder;
import com.datiacode.shop.order.OrderCompleted;
import com.datiacode.shop.order.OrderDetails;
import com.datiacode.shop.order.OrderService;
import com.datiacode.shop.order.UnknownProductException;

@Service
@Transactional
class DefaultOrderService implements OrderService {

	private final OrderRepository orders;
	private final CatalogService catalog;
	private final ApplicationEventPublisher events;
	private final Clock clock;

	DefaultOrderService(OrderRepository orders, CatalogService catalog, ApplicationEventPublisher events,
			Clock clock) {
		this.orders = orders;
		this.catalog = catalog;
		this.events = events;
		this.clock = clock;
	}

	@Override
	public OrderDetails create(NewOrder newOrder) {
		// Synchronous call: name and price are needed *before* the order can be stored
		List<OrderLine> lines = newOrder.items().stream().map(this::toLine).toList();

		var order = orders.save(Order.completed(newOrder.customer(), lines, Instant.now(clock)));

		// Published inside this transaction: Modulith stores it in event_publication on the same commit
		events.publishEvent(toEvent(order));
		return toDetails(order);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<OrderDetails> findById(Long id) {
		return orders.findById(id).map(DefaultOrderService::toDetails);
	}

	private OrderLine toLine(NewOrder.Item item) {
		return catalog.findProduct(item.sku())
				.map(product -> new OrderLine(product.sku(), product.name(), item.quantity(), product.unitPrice()))
				.orElseThrow(() -> new UnknownProductException(item.sku()));
	}

	private static OrderCompleted toEvent(Order order) {
		var lines = order.getLines().stream()
				.map(line -> new OrderCompleted.Line(line.getSku(), line.getProductName(), line.getQuantity()))
				.toList();
		return new OrderCompleted(order.getId(), order.getCustomer(), lines, order.getTotal(), order.getCreatedAt());
	}

	static OrderDetails toDetails(Order order) {
		var lines = order.getLines().stream()
				.map(line -> new OrderDetails.Line(line.getSku(), line.getProductName(), line.getQuantity(),
						line.getUnitPrice()))
				.toList();
		return new OrderDetails(order.getId(), order.getCustomer(), order.getStatus().name(), lines,
				order.getTotal(), order.getCreatedAt());
	}
}
