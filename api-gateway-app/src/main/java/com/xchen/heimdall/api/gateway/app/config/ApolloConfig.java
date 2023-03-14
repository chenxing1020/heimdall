package com.xchen.heimdall.api.gateway.app.config;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import org.springframework.context.annotation.Configuration;

import static com.xchen.heimdall.common.constant.ApolloNamespace.*;

/**
 * @author xchen
 * @date 2022/1/25
 */
@Configuration
@EnableApolloConfig(value = {API, ERROR_CODE,  APP_ACCESSTOKEN, FLOW_CONFIG})
public class ApolloConfig {
}
