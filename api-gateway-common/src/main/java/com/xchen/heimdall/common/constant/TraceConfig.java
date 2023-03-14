package com.xchen.heimdall.common.constant;

/**
 * @author xchen
 */
public class TraceConfig {
    private TraceConfig() {
    }

    /**
     * 每一次用户请求的uuid
     */
    public static final String TRACE_ID = "traceId";

    /**
     * 请求的跨度，从0开始
     */
    public static final String SPAN = "span";

    /**
     * 跳过json序列化，针对使用XStep协议的接口
     */
    public static final String SKIP_SER_LOG = "skipSerLog";
}
