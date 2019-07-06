package com.target.retail.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.context.annotation.ComponentScan;

@EnableCircuitBreaker
@SpringBootApplication
public class RetailApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(RetailApiApplication.class, args);
    }
}
