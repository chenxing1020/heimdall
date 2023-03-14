package com.xchen.heimdall.devtools.service.app.dto;

import com.xchen.heimdall.common.constant.PojoType;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class PojoDTO implements Serializable {

    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "类型")
    private PojoType pojoType;

    @ApiModelProperty(value = "POJOName")
    private String pojoName;

    @ApiModelProperty(value = "POJO说明")
    private String pojoDesc;

    @ApiModelProperty(value = "父类id")
    private Integer parentId;

    @ApiModelProperty(value = "字段列表")
    private List<FieldDTO> fieldList;
}
