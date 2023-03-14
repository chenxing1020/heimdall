package com.xchen.heimdall.dubbo.api.common.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xchen
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryUserIdVO extends UserIdVO {

    @ApiModelProperty(value = "被查询的用户的userId", example = "017101")
    private String queryUserId;

    @ApiModelProperty(value = "uid", example = "1")
    private Integer id;

}
