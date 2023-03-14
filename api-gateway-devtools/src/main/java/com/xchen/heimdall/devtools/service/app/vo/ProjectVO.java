package com.xchen.heimdall.devtools.service.app.vo;

import com.xchen.heimdall.common.constant.ProjectType;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author xchen
 * @date 2022/3/29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectVO implements Serializable {

    @ApiModelProperty(value = "项目id")
    private Integer id;

    @ApiModelProperty(value = "用户id")
    @NotNull
    private String userId;

    @ApiModelProperty(value = "项目名称")
    private String projectName;

    @ApiModelProperty(value = "项目类型")
    private ProjectType projectType;

    @ApiModelProperty(value = "项目描述")
    private String projectDesc;

    @ApiModelProperty(value = "项目owner id")
    private String ownerUserId;
}