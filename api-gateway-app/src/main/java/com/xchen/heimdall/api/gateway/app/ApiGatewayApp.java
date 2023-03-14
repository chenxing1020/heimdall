package com.xchen.heimdall.api.gateway.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author xchen
 */
@SpringBootApplication(scanBasePackages = {"com.xchen.heimdall"})
public class ApiGatewayApp {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApp.class, args);
    }

}