package com.xchen.heimdall.devtools.service.app.vo;

import com.xchen.heimdall.devtools.service.app.common.constant.MethodException;
import com.xchen.heimdall.common.constant.DtoWrapperType;
import com.xchen.heimdall.common.constant.VoWrapperType;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author 
 * @date 2022/4/12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DubboMethodVO implements Serializable {

    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "项目id")
    @NotNull
    private Integer projectId;

    @ApiModelProperty(value = "服务id")
    @NotNull
    private Integer serviceId;

    @ApiModelProperty(value = "方法名称")
    @NotNull
    private String methodName;

    @ApiModelProperty(value = "VOId")
    @NotNull
    private Integer voId;

    @ApiModelProperty(value = "vo包装类型")
    private VoWrapperType voWrapperType;

    @ApiModelProperty(value = "DTOId")
    @NotNull
    private Integer dtoId;

    @ApiModelProperty(value = "dto包装类型")
    private DtoWrapperType dtoWrapperType;

    @ApiModelProperty(value = "方法异常")
    private MethodException methodException;

    @ApiModelProperty(value = "负责人")
    private String ownerUserId;

    @ApiModelProperty(value = "用户id")
    @NotNull
    private String userId;

    @ApiModelProperty(value = "接口名称")
    private String apiDesc;

    @ApiModelProperty(value = "接口描述")
    private String apiRemark;

}
