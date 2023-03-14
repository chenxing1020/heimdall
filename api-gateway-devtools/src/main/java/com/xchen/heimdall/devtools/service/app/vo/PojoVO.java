package com.xchen.heimdall.devtools.service.app.vo;

import com.xchen.heimdall.devtools.service.app.dto.FieldDTO;
import com.xchen.heimdall.common.constant.PojoType;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @author 
 * @date 2022/4/12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PojoVO implements Serializable {

    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "类型")
    @NotNull
    private PojoType pojoType;

    @ApiModelProperty(value = "POJOName")
    @NotNull
    private String pojoName;

    @ApiModelProperty(value = "POJO说明")
    @NotNull
    private String pojoDesc;

    @ApiModelProperty(value = "父类id")
    private Integer parentId;

    @ApiModelProperty(value = "字段列表")
    @NotNull
    private List<FieldDTO> fieldList;

    @ApiModelProperty(value = "项目id")
    @NotNull
    private Integer projectId;
}
