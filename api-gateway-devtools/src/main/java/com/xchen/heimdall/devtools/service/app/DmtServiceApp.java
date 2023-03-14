package com.xchen.heimdall.devtools.service.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

/**
 * @author xchen
 */
@SpringBootApplication(scanBasePackages = {"com.xchen.heimdall"})
@ImportResource({"classpath:dubbo.xml"})
public class DmtServiceApp {

    public static void main(String[] args) {
        SpringApplication.run(DmtServiceApp.class, args);
    }

}