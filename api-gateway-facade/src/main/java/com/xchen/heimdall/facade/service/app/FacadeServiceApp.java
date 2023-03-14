package com.xchen.heimdall.facade.service.app;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ImportResource;

/**
 * @author xchen
 */
@SpringBootApplication(scanBasePackages = {"com.xchen.heimdall"})
@ImportResource(value = "classpath:dubbo.xml")
public class FacadeServiceApp {

    public static void main(String[] args) {

        new SpringApplicationBuilder(FacadeServiceApp.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

}