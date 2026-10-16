package com.datiacode.shop.order.internal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.datiacode.shop.order.OrderDetails;
import com.datiacode.shop.order.OrderService;
import com.datiacode.shop.order.UnknownProductException;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

	static final OrderDetails ORDER = new OrderDetails(1L, "alice", "COMPLETED",
			List.of(new OrderDetails.Line("KB-001", "Mechanical keyboard", 1, new BigDecimal("89.90"))),
			new BigDecimal("89.90"), Instant.parse("2026-10-16T10:00:00Z"));

	static final String VALID_REQUEST = """
			{"customer": "alice", "items": [{"sku": "KB-001", "quantity": 1}]}
			""";

	@Autowired
	MockMvc mvc;

	@MockitoBean
	OrderService orderService;

	@Test
	@DisplayName("POST /orders returns 201 with Location header and the stored order")
	void createReturns201() throws Exception {
		given(orderService.create(any())).willReturn(ORDER);

		mvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(VALID_REQUEST))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "/orders/1"))
				.andExpect(jsonPath("$.status").value("COMPLETED"))
				.andExpect(jsonPath("$.total").value(89.90));
	}

	@Test
	@DisplayName("POST /orders returns 400 when the request has no items")
	void createReturns400OnInvalidRequest() throws Exception {
		mvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"customer": "alice", "items": []}
								"""))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("POST /orders returns 422 with the SKU when the product is not in the catalog")
	void createReturns422WhenProductUnknown() throws Exception {
		given(orderService.create(any())).willThrow(new UnknownProductException("KB-001"));

		mvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(VALID_REQUEST))
				.andExpect(status().isUnprocessableContent())
				.andExpect(jsonPath("$.title").value("Unknown product"))
				.andExpect(jsonPath("$.sku").value("KB-001"));
	}

	@Test
	@DisplayName("GET /orders/{id} returns the order")
	void findByIdReturns200() throws Exception {
		given(orderService.findById(1L)).willReturn(Optional.of(ORDER));

		mvc.perform(get("/orders/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.customer").value("alice"));
	}

	@Test
	@DisplayName("GET /orders/{id} returns 404 when the order does not exist")
	void findByIdReturns404() throws Exception {
		given(orderService.findById(99L)).willReturn(Optional.empty());

		mvc.perform(get("/orders/99"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value("Order not found"));
	}
}
