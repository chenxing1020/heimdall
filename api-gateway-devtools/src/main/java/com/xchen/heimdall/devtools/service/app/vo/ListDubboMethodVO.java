package com.xchen.heimdall.devtools.service.app.vo;

import com.xchen.heimdall.dubbo.api.common.vo.UserIdVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * @author xchen
 * @date 2022/5/10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ListDubboMethodVO extends UserIdVO {

    @ApiModelProperty(value = "方法id")
    private Integer id;

    @ApiModelProperty(value = "服务id")
    private Integer serviceId;

    @ApiModelProperty(value = "项目id")
    private Integer projectId;

    @ApiModelProperty(value = "方法名")
    private String methodName;

    @ApiModelProperty(value = "项目名")
    private String projectName;

    @ApiModelProperty(value = "服务名")
    private String serviceName;

    @ApiModelProperty(value = "代码生成状态")
    private Integer codeStatus;

    @ApiModelProperty(value = "网关接口生成状态")
    private Boolean gatewayApiStatus;
}