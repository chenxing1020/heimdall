package com.xchen.heimdall.devtools.service.app.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author xchen
 * @date 2022/9/5
 */
@AllArgsConstructor
@Getter
public enum MethodException {
    /**
     * 入参不合法
     */
    BAD_REQUEST("BadRequestException", "入参不合法"),
    /**
     * 自定义异常
     */
    CUSTOM("CustomException", "自定义异常"),
    /**
     * 用户无权限
     */
    FORBIDDEN("ForbiddenException", "用户无权限"),
    /**
     * 框架异常
     */
    FRAMEWORK("FrameworkException", "框架异常"),
    /**
     * 服务内部异常
     */
    INTERNAL_SERVER("InternalServerException", "服务内部异常"),
    /**
     * 资源不存在
     */
    NOT_FOUND("NotFoundException", "资源不存在"),
    /**
     * 三方服务调用异常
     */
    THIRD_PARTY("ThirdPartyException", "三方服务调用异常"),
    /**
     * 用户未登录
     */
    UNAUTHORIZED("UnauthorizedException", "用户未登录");

    private final String key;
    private final String desc;

    public String getClassName() {
        return "com.xchen.heimdall.common.exception.errorcode." + this.getKey();
    }

}
