package com.sap.periziafacile.pfgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.sap.periziafacile.pfgateway.config.UriConfiguration;
import com.sap.periziafacile.pfgateway.helpers.ServiceContainer;

@SpringBootApplication
@EnableConfigurationProperties(UriConfiguration.class)
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);

		if(!ServiceContainer.registerService("customerservice", "http://localhost:8082"))
			System.out.println("Error registering customerservice");

		if(!ServiceContainer.registerService("orderservice", "http://localhost:8083"))
			System.out.println("Error registering orderservice");

	}

}
