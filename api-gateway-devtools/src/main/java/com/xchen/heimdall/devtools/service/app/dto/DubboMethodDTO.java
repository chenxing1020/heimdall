package com.xchen.heimdall.devtools.service.app.dto;

import com.xchen.heimdall.devtools.service.app.common.constant.MethodException;
import com.xchen.heimdall.common.constant.DtoWrapperType;
import com.xchen.heimdall.common.constant.VoWrapperType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 
 * @date 2022/4/12
 */
@ApiModel(description = "项目信息")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DubboMethodDTO implements Serializable {

    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "项目id")
    private Integer projectId;

    @ApiModelProperty(value = "项目名称")
    private String projectName;

    @ApiModelProperty(value = "服务id")
    private Integer serviceId;

    @ApiModelProperty(value = "服务名称")
    private String serviceName;

    @ApiModelProperty(value = "方法名称")
    private String methodName;

    @ApiModelProperty(value = "vo id")
    private Integer voId;

    @ApiModelProperty(value = "vo 名称")
    private String voName;

    @ApiModelProperty(value = "vo 包装类型")
    private VoWrapperType voWrapperType;

    @ApiModelProperty(value = "dto id")
    private Integer dtoId;

    @ApiModelProperty(value = "dto 名称")
    private String dtoName;

    @ApiModelProperty(value = "dto 包装类型")
    private DtoWrapperType dtoWrapperType;

    @ApiModelProperty(value = "方法异常")
    private MethodException methodException;

    @ApiModelProperty(value = "api名称")
    private String apiDesc;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "代码生成状态")
    private Integer codeStatus;

    @ApiModelProperty(value = "网关接口生成状态")
    private Boolean gatewayApiStatus;

    @ApiModelProperty(value = "责任人")
    private String ownerUserId;

    @ApiModelProperty(value = "接口备注")
    private String apiRemark;
}
