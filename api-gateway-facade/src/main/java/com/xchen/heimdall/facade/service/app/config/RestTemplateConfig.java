package com.xchen.heimdall.facade.service.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;


/**
 * @author xchen
 * @date 2022/3/16
 */
@Configuration
public class RestTemplateConfig {

    @Value("${rest.template.timeout.read}")
    private int readTimeoutMillis;

    @Value("${rest.template.timeout.connect}")
    private int connectTimeoutMillis;

    @Bean
    RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        // 超时设置，单位：ms
        factory.setReadTimeout(readTimeoutMillis);
        factory.setConnectTimeout(connectTimeoutMillis);
        return new RestTemplate(factory);
    }
}
