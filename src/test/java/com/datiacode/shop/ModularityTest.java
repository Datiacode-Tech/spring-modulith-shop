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
	void exposesInventoryCatalogAsNamedInterface() {
		var inventory = MODULES.getModuleByName("inventory").orElseThrow();

		assertThat(inventory.getNamedInterfaces().getByName("catalog")).isPresent();
	}
}
