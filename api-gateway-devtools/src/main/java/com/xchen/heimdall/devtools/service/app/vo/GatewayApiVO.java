package com.xchen.heimdall.devtools.service.app.vo;

import com.xchen.heimdall.common.constant.AccessPoint;
import com.xchen.heimdall.common.constant.UpstreamChannelType;
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
 * @date 2022/4/15
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class  GatewayApiVO implements Serializable {

    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "服务id")
    @NotNull
    private Integer serviceId;

    @ApiModelProperty(value = "方法id")
    @NotNull
    private Integer methodId;

    @ApiModelProperty(value = "后端通道类型")
    @NotNull
    private UpstreamChannelType upstreamChannelType;

    @ApiModelProperty(value = "登录态")
    @NotNull
    private Boolean loginRequired;

    @ApiModelProperty(value = "访问权限idList")
    private List<Integer> permissionIdList;

    @ApiModelProperty(value = "max tps")
    private Integer tps;

    @ApiModelProperty(value = "用户id")
    @NotNull
    private String userId;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "接入点")
    @NotNull
    private List<AccessPoint> accessPointList;
}
