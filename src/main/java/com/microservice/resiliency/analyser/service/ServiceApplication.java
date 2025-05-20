package com.microservice.resiliency.analyser.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.microservice.resiliency.analyser.service")
@ComponentScan("com.microservice.resiliency.analyser.service")
@EntityScan("com.microservice.resiliency.analyser.service.model")
@EnableJpaRepositories(basePackages = "com.microservice.resiliency.analyser.service.respository")
public class ServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceApplication.class, args);
	}

}
