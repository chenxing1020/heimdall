package com.xchen.heimdall.facade.service.app.config;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import org.springframework.context.annotation.Configuration;

import static com.xchen.heimdall.common.constant.ApolloNamespace.*;

/**
 * @author xchen
 * @date 2022/1/25
 */
@Configuration
@EnableApolloConfig(value = {API, ZK_REGISTRY, SERVER_ADDRESSES})
public class ApolloConfig {
}
