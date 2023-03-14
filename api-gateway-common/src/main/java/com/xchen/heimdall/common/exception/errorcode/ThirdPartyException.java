package com.xchen.heimdall.common.exception.errorcode;

import com.xchen.heimdall.common.constant.ReservedErrorCode;

/**
 * 充当网关或代理的服务器，从远端服务器接收到了一个无效的请求。
 * 常见场景就是请求参数错误导致后端处理失败，三方服务异常，或者后端服务掉线。
 * 通常返回错误为：HTTP 502。
 *
 * @author xchen
 */
public class ThirdPartyException extends AbstractErrorCodeException {

    private static final Integer ERROR_CODE = ReservedErrorCode.THIRD_PARTY_ERROR_CODE;

    public ThirdPartyException(String message) {
        super(ERROR_CODE, message);
    }

    public ThirdPartyException(String message, Throwable t) {
        super(ERROR_CODE, message, t);
    }

}