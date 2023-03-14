package com.xchen.heimdall.devtools.service.app.config;

import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * apollo连接config
 */
@Configuration
public class ApolloClientConfig {

    /**
     * apollo 访问地址
     **/
    @Value("${apollo.portalUrl}")
    private String portalUrl;

    /**
     * token apollo中添加的openapi token
     **/
    @Value("${apollo.token}")
    private String token;

    @Bean
    public ApolloOpenApiClient apolloOpenApiClient() {
        return ApolloOpenApiClient.newBuilder()
                .withPortalUrl(portalUrl)
                .withToken(token)
                .build();
    }
}
