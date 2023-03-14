package com.xchen.heimdall.devtools.service.app.vo.qo;

import com.xchen.heimdall.dubbo.api.common.vo.UserIdVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * 错误码查询对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ErrorCodeConfQO extends UserIdVO {

    @ApiModelProperty(value = "错误码")
    private String errorCode;

    @ApiModelProperty(value = "错误级别。 0-系统错误, 1-业务严重错误, 2-业务一般错误, 3-三方系统错误, 4-业务提示")
    private Integer level;

    @ApiModelProperty(value = "所属工程ID")
    private Integer projectId;

    @ApiModelProperty(value = "版本")
    private String version;
}
