package com.xchen.heimdall.dubbo.api.common.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author xchen
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserIdVO implements Serializable {

    @ApiModelProperty(value = "当前登录的用户（即当前操作人）", example = "017101")
    String userId;

}
