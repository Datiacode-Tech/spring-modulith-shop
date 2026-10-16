package com.datiacode.shop.order.internal;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.datiacode.shop.inventory.InventoryService;
import com.datiacode.shop.order.InsufficientStockException;
import com.datiacode.shop.order.NewOrder;
import com.datiacode.shop.order.OrderDetails;
import com.datiacode.shop.order.OrderService;

@Service
@Transactional
class DefaultOrderService implements OrderService {

	private final OrderRepository orders;
	private final InventoryService inventory;
	private final Clock clock;

	DefaultOrderService(OrderRepository orders, InventoryService inventory, Clock clock) {
		this.orders = orders;
		this.inventory = inventory;
		this.clock = clock;
	}

	@Override
	public OrderDetails create(NewOrder newOrder) {
		// Synchronous call: we must know there is stock *before* accepting the order
		List<OrderLine> lines = newOrder.items().stream().map(this::toAvailableLine).toList();

		var order = orders.save(Order.completed(newOrder.customer(), lines, Instant.now(clock)));
		return toDetails(order);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<OrderDetails> findById(Long id) {
		return orders.findById(id).map(DefaultOrderService::toDetails);
	}

	private OrderLine toAvailableLine(NewOrder.Item item) {
		return inventory.findProduct(item.sku())
				.filter(product -> inventory.isAvailable(item.sku(), item.quantity()))
				.map(product -> new OrderLine(product.sku(), product.name(), item.quantity(), product.unitPrice()))
				.orElseThrow(() -> new InsufficientStockException(item.sku(), item.quantity()));
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
