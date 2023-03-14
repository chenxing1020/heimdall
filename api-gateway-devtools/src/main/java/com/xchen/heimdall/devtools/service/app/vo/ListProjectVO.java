package com.xchen.heimdall.devtools.service.app.vo;

import com.xchen.heimdall.common.constant.ProjectType;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * @author xchen
 * @date 2022/5/9
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ListProjectVO extends UserIdVO {

    @ApiModelProperty(value = "项目Id")
    private Integer projectId;

    @ApiModelProperty(value = "项目类型")
    private ProjectType projectType;

    @ApiModelProperty(value = "项目名称")
    private String projectName;
}
