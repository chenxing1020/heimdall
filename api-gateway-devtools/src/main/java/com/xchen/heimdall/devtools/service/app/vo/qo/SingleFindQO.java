package com.xchen.heimdall.devtools.service.app.vo.qo;

import com.xchen.heimdall.dubbo.api.common.vo.UserIdVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SingleFindQO extends UserIdVO {

    @ApiModelProperty(value = "查询ID")
    @NotNull
    private Integer id;
}
