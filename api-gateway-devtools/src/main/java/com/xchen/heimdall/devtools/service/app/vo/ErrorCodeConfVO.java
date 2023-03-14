package com.xchen.heimdall.devtools.service.app.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ErrorCodeConfVO extends BaseVO {

    @ApiModelProperty(value = "错误码")
    @NotNull
    private String errorCode;

    @ApiModelProperty(value = "提示文案")
    @NotNull
    private String displayMessage;

    @ApiModelProperty(value = "错误级别。 0-系统错误, 1-业务严重错误, 2-业务一般错误, 3-三方系统错误, 4-业务提示")
    private Integer level;

    @ApiModelProperty(value = "错误原因")
    private String errorReason;

    @ApiModelProperty(value = "处理方法")
    private String solution;

    @ApiModelProperty(value = "所属工程")
    @NotNull
    private Integer projectId;

    @ApiModelProperty(value = "版本")
    private String version;
}
