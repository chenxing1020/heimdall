package com.xchen.heimdall.api.gateway.app.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.xchen.heimdall.common.util.JacksonUtil;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;

/**
 * @author xchen
 */
@Configuration
public class JacksonConfig {

    /**
     * Jackson全局转化long类型为String，解决jackson序列化时long类型缺失精度问题
     *
     * @return Jackson2ObjectMapperBuilderCustomizer 注入的对象
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return jacksonObjectMapperBuilder -> {
            // 不返回为null的字段
            jacksonObjectMapperBuilder.serializationInclusion(JsonInclude.Include.NON_NULL);
            // Long转成字符串，避免前端序列化的问题
            jacksonObjectMapperBuilder.serializerByType(Long.TYPE, JacksonUtil.LONG_JSON_SERIALIZER);
            jacksonObjectMapperBuilder.serializerByType(Long.class, JacksonUtil.LONG_JSON_SERIALIZER);
            jacksonObjectMapperBuilder.dateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"));
        };
    }

}