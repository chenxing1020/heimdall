package com.xchen.heimdall.facade.service.app.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.xchen.heimdall.common.exception.errorcode.FrameworkException;
import com.xchen.heimdall.common.util.JacksonUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author xchen
 * @date 2022/3/23
 */
public class RestUtils {

    private RestUtils() {
        throw new FrameworkException("Utility class");
    }

    private static final Pattern MULTI_SEPARATOR_PATTERN = Pattern.compile("(?<!:)/{2,}");

    public static String appendQueryString(String url, String data) {
        if (StringUtils.isEmpty(data)) {
            return url;
        }

        Map<String, Object> parameters = JacksonUtil.decode(data, new TypeReference<Map<String, Object>>() {
        });
        String queryString = buildQueryString(parameters, null);

        return url + '?' + queryString;
    }

    private static String buildQueryString(Map<String, Object> parameterMap, String prefixKey) {
        return parameterMap.entrySet().stream()
                .map(p -> getCondition(
                        getKey(prefixKey, p.getKey()),
                        p.getValue())
                )
                .reduce((p1, p2) -> String.format("%s&%s", p1, p2))
                .orElse("");
    }

    private static String getCondition(String key, Object value) {
        if (value instanceof List) {
            return key + "=" + StringUtils.join((ArrayList) value, ",");
        } else if (value instanceof Map) {
            return buildQueryString((HashMap) value, key);
        } else {
            return key + "=" + value;
        }
    }

    private static String getKey(String prefixKey, String key) {
        if (StringUtils.isNotEmpty(prefixKey)) {
            return String.format("%s.%s", prefixKey, key);
        }
        return key;
    }

    public static String normalizeUrl(String url) {
        // 反向否定预查，避免url中由于拼接引入的多个分隔符，同时排除 "://" 这种模式
        url = MULTI_SEPARATOR_PATTERN.matcher(url).replaceAll("/");
        // 如果最后一个字符为"/"，需去除，避免拼接queryString
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    public static HttpEntity<String> generateHttpRequest(String data) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(data, headers);
    }

    public static String extractRawUrl(String extFields) {
        Map<String, String> extFieldsMap = JacksonUtil.decode(extFields, new TypeReference<Map<String, String>>() {
        });
        return extFieldsMap.get("RawUrl");
    }
}
