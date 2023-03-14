package com.xchen.heimdall.api.gateway.app.util;

import org.springframework.http.HttpMethod;

import java.util.regex.Pattern;

/**
 * @author xchen
 * @date 2022/7/26
 */
public class RequestPathUtil {

    private static final Pattern MULTI_SEPARATOR_PATTERN = Pattern.compile("(?<!:)/{2,}");

    private RequestPathUtil() {}

    public static String getRpcRequestPath(String service, String method) {
        return String.format("/rpc/%s/%s", service, method);
    }

    public static String getStreamRequestPath(String service, String method) {
        return String.format("/stream/%s/%s", service, method);
    }

    public static String getSubscribeRequestPath(String service, String method) {
        return String.format("/sub/%s/%s", service, method);
    }

    public static String getRestRequestPath(String service, String restUrl) {
        // 防止出现重复多余的斜杠
        return MULTI_SEPARATOR_PATTERN.matcher(String.format("/rest/%s/%s", service, restUrl))
                .replaceAll("/");
    }

    public static String getRestRawRequestPath(String service, HttpMethod method) {
        return String.format("/restRaw/%s/%s", service, method.toString());
    }
}
