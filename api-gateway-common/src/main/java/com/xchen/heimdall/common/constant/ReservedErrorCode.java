package com.xchen.heimdall.common.constant;

/**
 * 内部保留的异常码
 *
 * @author xchen
 * @date 2023/1/13
 */
public class ReservedErrorCode {

    private ReservedErrorCode() {
    }

    /**
     * 请求入参不合法
     */
    public static final int BAD_REQUEST_ERROR_CODE = 1400;
    /**
     * 用户未登录
     */
    public static final int UNAUTHORIZED_ERROR_CODE = 1401;
    /**
     * 用户无权限
     */
    public static final int FORBIDDEN_ERROR_CODE = 1403;
    /**
     * 请求的资源不存在
     */
    public static final int NOT_FOUND_ERROR_CODE = 1404;
    /**
     * 内部服务处理出错
     */
    public static final int INTERNAL_SERVER_ERROR_CODE = 1500;
    /**
     * 远程调用出错
     */
    public static final int THIRD_PARTY_ERROR_CODE = 1502;
    /**
     * 内部框架错误
     */
    public static final int FRAMEWORK_ERROR_CODE = 1503;
    /**
     *
     */
    public static final int SUBSCRIBE_ERROR_CODE = 1504;
    /**
     * 后台响应超时
     */
    public static final int TIMEOUT_ERROR_CODE = 1505;
    /**
     * 触发流控
     */
    public static final int FLOW_CONTROL_ERROR_CODE = 1506;
}
