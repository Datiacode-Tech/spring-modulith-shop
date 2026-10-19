package com.datiacode.shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulithic;

// shared holds cross-cutting infrastructure (clock, security, tracing): it is bootstrapped in every module test
@Modulithic(sharedModules = "shared")
@SpringBootApplication
public class ShopApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShopApplication.class, args);
	}

}
