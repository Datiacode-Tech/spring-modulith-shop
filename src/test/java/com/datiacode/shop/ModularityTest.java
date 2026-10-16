package com.datiacode.shop;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/**
 * Fails the build if any module reaches into another module's internals or if module dependencies form a cycle.
 */
class ModularityTest {

	static final ApplicationModules MODULES = ApplicationModules.of(ShopApplication.class);

	@Test
	void verifiesModularStructure() {
		MODULES.verify();
	}

	@Test
	void exposesCatalogModelAsNamedInterface() {
		var catalog = MODULES.getModuleByName("catalog").orElseThrow();

		assertThat(catalog.getNamedInterfaces().getByName("model")).isPresent();
	}

	@Test
	void orderDoesNotDependOnInventory() {
		var order = MODULES.getModuleByName("order").orElseThrow();

		// "Pedidos ya no conoce a inventario": inventory listens to order's events, never the other way round
		assertThat(order.getAllDependencies(MODULES).containsModuleNamed("inventory")).isFalse();
	}
}
