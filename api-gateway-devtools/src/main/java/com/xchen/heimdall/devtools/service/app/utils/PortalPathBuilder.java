package com.xchen.heimdall.devtools.service.app.utils;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;

import java.util.*;

/**
 * apollo portal门户path工具
 * 参考 {@link com.ctrip.framework.apollo.openapi.client.url.OpenApiPathBuilder}
 * @author xchen
 * @date 2022/8/25
 */
public class PortalPathBuilder {
    private static final String ENVS_PATH = "envs";
    private static final String APPS_PATH = "apps";
    private static final String CLUSTERS_PATH = "clusters";
    private static final String NAMESPACES_PATH = "namespaces";
    private static final String ITEMS_PATH = "items";
    private static final String ITEM_PATH = "item";
    private static final String RELEASE_PATH = "releases";

    private static final List<String> SORTED_PATH_KEYS = Arrays.asList(APPS_PATH, ENVS_PATH, CLUSTERS_PATH, NAMESPACES_PATH, ITEMS_PATH, ITEM_PATH, RELEASE_PATH);

    private static final Escaper PATH_ESCAPER = UrlEscapers.urlPathSegmentEscaper();
    private static final Escaper QUERY_PARAM_ESCAPER = UrlEscapers.urlFormParameterEscaper();
    private static final Joiner PATH_JOIN = Joiner.on("/");

    private final Map<String, String> pathVariable;
    private final Map<String, String> params;

    private String customResource;

    public static PortalPathBuilder newBuilder() {
        return new PortalPathBuilder();
    }

    private PortalPathBuilder() {
        this.pathVariable = new HashMap<>();
        this.params = new HashMap<>();
    }

    public PortalPathBuilder envsPathVal(String envs) {
        pathVariable.put(ENVS_PATH, escapePath(envs));
        return this;
    }

    public PortalPathBuilder appsPathVal(String apps) {
        pathVariable.put(APPS_PATH, escapePath(apps));
        return this;
    }

    public PortalPathBuilder clustersPathVal(String clusters) {
        pathVariable.put(CLUSTERS_PATH, escapePath(clusters));
        return this;
    }

    public PortalPathBuilder namespacesPathVal(String namespaces) {
        pathVariable.put(NAMESPACES_PATH, escapePath(namespaces));
        return this;
    }

    public PortalPathBuilder itemsPathVal(String items) {
        pathVariable.put(ITEMS_PATH, escapePath(items));
        return this;
    }

    public PortalPathBuilder itemPathVal(String item) {
        pathVariable.put(ITEM_PATH, escapePath(item));
        return this;
    }

    public PortalPathBuilder releasesPathVal(String releases) {
        pathVariable.put(RELEASE_PATH, escapePath(releases));
        return this;
    }

    public PortalPathBuilder customResource(String customResource) {
        this.customResource = customResource;
        return this;
    }

    public PortalPathBuilder addParam(String key, Object value) {
        if (Strings.isNullOrEmpty(key)) {
            throw new IllegalArgumentException("Param key should not be null or empty");
        }
        this.params.put(key, escapeParam(String.valueOf(value)));
        return this;
    }

    public String buildPath(String baseUrl) {
        if (Strings.isNullOrEmpty(baseUrl)) {
            throw new IllegalArgumentException("Base url should not be null or empty");
        }
        List<String> parts = new ArrayList<>();
        parts.add(baseUrl);

        for (String k : SORTED_PATH_KEYS) {
            if (pathVariable.containsKey(k)) {
                parts.add(k);
                String v = pathVariable.get(k);
                if (!Strings.isNullOrEmpty(v)) {
                    parts.add(v);
                }
            }
        }

        if (!Strings.isNullOrEmpty(this.customResource)) {
            parts.add(this.customResource);
        }

        String path = PATH_JOIN.join(parts);

        if (!params.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> kv : params.entrySet()) {
                if (sb.length() > 0) {
                    sb.append("&");
                }
                sb.append(kv.getKey()).append("=").append(kv.getValue());
            }
            path += "?" + sb;
        }
        return path;
    }

    protected String escapePath(String path) {
        return PATH_ESCAPER.escape(path);
    }

    protected String escapeParam(String param) {
        return QUERY_PARAM_ESCAPER.escape(param);
    }

}