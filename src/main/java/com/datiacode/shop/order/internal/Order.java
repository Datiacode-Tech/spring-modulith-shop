package com.datiacode.shop.order.internal;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String customer;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OrderStatus status;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "order_lines", joinColumns = @JoinColumn(name = "order_id"))
	private List<OrderLine> lines = new ArrayList<>();

	@Column(nullable = false)
	private BigDecimal total;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected Order() {
		// for JPA
	}

	/**
	 * Creates an order that is already completed: stock was checked before building it.
	 */
	static Order completed(String customer, List<OrderLine> lines, Instant now) {
		var order = new Order();
		order.customer = customer;
		order.status = OrderStatus.COMPLETED;
		order.lines = new ArrayList<>(lines);
		order.total = lines.stream().map(OrderLine::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
		order.createdAt = now;
		return order;
	}

	Long getId() {
		return id;
	}

	String getCustomer() {
		return customer;
	}

	OrderStatus getStatus() {
		return status;
	}

	List<OrderLine> getLines() {
		return List.copyOf(lines);
	}

	BigDecimal getTotal() {
		return total;
	}

	Instant getCreatedAt() {
		return createdAt;
	}
}
