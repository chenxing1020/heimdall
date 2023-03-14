package com.xchen.heimdall.devtools.service.app.dto;

import com.xchen.heimdall.common.constant.ProjectType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author xchen
 * @date 2022/4/5
 */
@ApiModel(description = "项目信息")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDTO implements Serializable {
    @ApiModelProperty(value = "项目id")
    private Integer id;

    @ApiModelProperty(value = "项目名称")
    private String projectName;

    @ApiModelProperty(value = "项目类型")
    private ProjectType projectType;

    @ApiModelProperty(value = "项目描述")
    private String projectDesc;

    @ApiModelProperty(value = "项目owner id")
    private String ownerUserId;
}
