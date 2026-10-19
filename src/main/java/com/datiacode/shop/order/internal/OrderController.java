package com.datiacode.shop.order.internal;

import java.net.URI;
import java.security.Principal;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.datiacode.shop.order.OrderNotFoundException;
import com.datiacode.shop.order.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/orders")
@Tag(name = "Orders")
class OrderController {

	private final OrderService orderService;

	OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@PostMapping
	@Operation(summary = "Place an order", description = "Prices the items against the catalog, stores the order and publishes OrderCompleted")
	ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request, Principal principal) {
		var order = OrderResponse.from(orderService.create(request.toCommand(principal.getName())));
		return ResponseEntity.created(URI.create("/orders/" + order.id())).body(order);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get an order by id")
	OrderResponse findById(@PathVariable Long id) {
		return orderService.findById(id)
				.map(OrderResponse::from)
				.orElseThrow(() -> new OrderNotFoundException(id));
	}
}
