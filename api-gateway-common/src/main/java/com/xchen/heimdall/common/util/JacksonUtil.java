package com.xchen.heimdall.common.util;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.xchen.heimdall.common.exception.errorcode.InternalServerException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;

/**
 * @author xchen
 * @date 2022/4/18
 */
public class JacksonUtil {

    private static final String CLASS_FIELD_NAME = "class";
    private static final String CLASS_FILTER_NAME = "classFilter";

    /**
     * js maximum value: 2<sup>53</sup>-1.
     */
    public static final long JS_NUMBER_MAX_VALUE = 0x1fffffffffffffL;
    /**
     * js minimum value: -2<sup>53</sup>.
     */
    public static final long JS_NUMBER_MIN_VALUE = -0x20000000000000L;

    /**
     * 提供一个全局的long类型序列化器，将超过js精度的long类型转成string
     */
    public static final JsonSerializer<Long> LONG_JSON_SERIALIZER = new JsonSerializer<Long>() {
        @Override
        public void serialize(Long value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value <= JS_NUMBER_MIN_VALUE || value >= JS_NUMBER_MAX_VALUE) {
                gen.writeString(value.toString());
            } else {
                gen.writeNumber(value);
            }
        }
    };

    private static final SimpleModule SIMPLE_MODULE = new SimpleModule()
            .addSerializer(Long.class, LONG_JSON_SERIALIZER)
            .addSerializer(Long.TYPE, LONG_JSON_SERIALIZER);

    /**
     * 序列化时
     * 所有date类型均需format，long类型也会转换为String
     */
    private static ObjectMapper getDefaultObjectMapper() {
        return new ObjectMapper()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .registerModule(SIMPLE_MODULE)
                .setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"));
    }

    private static final ObjectMapper OBJECT_MAPPER = getDefaultObjectMapper();

    /**
     * 用于mixIn
     */
    @JsonFilter(CLASS_FILTER_NAME)
    interface ClassFieldFilter {
    }

    /**
     * 针对dubbo 泛化调用结果，过滤 class字段
     */
    private static final ObjectMapper DUBBO_GENERIC_OBJECT_MAPPER = getDefaultObjectMapper()
            .setFilterProvider(new SimpleFilterProvider()
                    .addFilter(CLASS_FILTER_NAME, SimpleBeanPropertyFilter.serializeAllExcept(CLASS_FIELD_NAME))
            )
            .addMixIn(HashMap.class, ClassFieldFilter.class);

    private JacksonUtil() {
    }

    public static <T> String encode(T object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new InternalServerException("Failed to encode object: " + object, e);
        }
    }

    public static <T> String encodeDubboGeneric(T object) {
        try {
            return DUBBO_GENERIC_OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new InternalServerException("Failed to encode dubbo object: " + object, e);
        }
    }

    public static <T> T decode(String json, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new InternalServerException("Failed to decode json: " + json, e);
        }
    }

    public static <T> T decode(String json, TypeReference<T> typeReference) {
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new InternalServerException("Failed to decode json: " + json, e);
        }
    }

}
