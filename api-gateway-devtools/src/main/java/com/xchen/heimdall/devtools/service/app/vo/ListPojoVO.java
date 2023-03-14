package com.xchen.heimdall.devtools.service.app.vo;

import com.xchen.heimdall.common.constant.PojoType;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author xchen
 * @date 2022/5/6
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListPojoVO implements Serializable {

    @ApiModelProperty(value = "类型")
    @NotNull
    private PojoType pojoType;

    @ApiModelProperty(value = "POJOName")
    private String pojoName;

    @ApiModelProperty(value = "项目id")
    private Integer projectId;
}
