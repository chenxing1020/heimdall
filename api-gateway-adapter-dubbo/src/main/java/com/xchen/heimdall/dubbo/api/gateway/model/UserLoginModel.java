package com.xchen.heimdall.dubbo.api.gateway.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author xchen
 * @date 2022/1/14
 */
@Data
public class UserLoginModel extends ClientInfoModel {
    /**
     * 登录凭证
     */
    @NotNull
    private String userName;
    @NotNull
    private String password;

    /**
     * 认证系统。目前只有heimdall，并且自营和外部只能拥有一个。
     */
    @ApiModelProperty(value = "认证系统。目前支持HEIMDALL_SELF和HEIMDALL_CLIENT", example = "HEIMDALL_SELF")
    private String authenticate;
    /**
     * 外部用户邮箱必填
     */
    private String emailName;
    /**
     * 外部用户邮箱必填
     */
    private String emailCode;

    /**
     * 业务场景。根据不同的业务场景，配置是否开启mac地址白名单验证，以及对应的白名单。<p></p>
     * 1. OTC_PUB 可能开启也可能不开启白名单验证
     * 2. SELF 自营默认不开启白名单验证
     *
     * 此值不应该由前端传入，而应该不同的业务由不同的网关接入，网关回填此值。
     * 初期只有一个网关，直接按自营用户和场外用户进行区分业务场景，后续可能需要整改。
     */
    private String business;
}
