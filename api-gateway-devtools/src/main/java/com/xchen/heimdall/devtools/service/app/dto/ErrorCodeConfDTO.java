package com.xchen.heimdall.devtools.service.app.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@ApiModel(description = "错误码信息")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorCodeConfDTO implements Serializable {
    @ApiModelProperty(value = "ID")
    private Integer id;

    @ApiModelProperty(value = "错误码")
    private String errorCode;

    @ApiModelProperty(value = "提示文案")
    private String displayMessage;

    @ApiModelProperty(value = "错误级别。 0-系统错误, 1-业务严重错误, 2-业务一般错误, 3-三方系统错误, 4-业务提示")
    private Integer level;

    @ApiModelProperty(value = "错误原因")
    private String errorReason;

    @ApiModelProperty(value = "处理方法")
    private String solution;

    @ApiModelProperty(value = "所属工程ID")
    private String project;

    @ApiModelProperty(value = "所属工程名称")
    private String projectName;

    @ApiModelProperty(value = "版本")
    private String version;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "修改人")
    private String updateUser;

    @ApiModelProperty(value = "修改时间")
    private Date updateTime;
}
