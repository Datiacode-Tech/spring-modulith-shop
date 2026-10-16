package com.datiacode.shop.order.internal;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.datiacode.shop.order.InsufficientStockException;
import com.datiacode.shop.order.OrderNotFoundException;

/**
 * Maps the order module's domain exceptions to HTTP. The module owns its error contract; generic errors are left to
 * {@code shared.web.GlobalExceptionHandler}.
 */
@RestControllerAdvice(assignableTypes = OrderController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
class OrderExceptionHandler {

	@ExceptionHandler(InsufficientStockException.class)
	ProblemDetail handleInsufficientStock(InsufficientStockException ex) {
		var problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
		problem.setTitle("Insufficient stock");
		problem.setProperty("sku", ex.getSku());
		return problem;
	}

	@ExceptionHandler(OrderNotFoundException.class)
	ProblemDetail handleNotFound(OrderNotFoundException ex) {
		var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
		problem.setTitle("Order not found");
		return problem;
	}
}
