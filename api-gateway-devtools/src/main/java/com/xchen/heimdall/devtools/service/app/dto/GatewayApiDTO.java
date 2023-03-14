package com.xchen.heimdall.devtools.service.app.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.xchen.heimdall.common.constant.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author 
 * @date 2022/4/15
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "gateway_api", autoResultMap = true)
public class GatewayApiDTO implements Serializable {

    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "服务id")
    private Integer serviceId;

    @ApiModelProperty(value = "服务名称")
    private String serviceName;

    @ApiModelProperty(value = "服务简称")
    private String simpleServiceName;

    @ApiModelProperty(value = "项目id")
    private Integer projectId;

    @ApiModelProperty(value = "项目名称")
    private String projectName;

    @ApiModelProperty(value = "方法id")
    private Integer methodId;

    @ApiModelProperty(value = "方法名称")
    private String methodName;

    @ApiModelProperty(value = "接口简要描述")
    private String apiDesc;

    @ApiModelProperty(value = "前端请求接口")
    private String requestPath;

    @ApiModelProperty(value = "vo名称")
    private String voName;

    @ApiModelProperty(value = "vo路径")
    private String voPath;

    @ApiModelProperty(value = "vo包装类型")
    private VoWrapperType voWrapperType;

    @ApiModelProperty(value = "dto包装类型")
    private DtoWrapperType dtoWrapperType;

    @ApiModelProperty(value = "后端服务地址")
    private String servicePath;

    @ApiModelProperty(value = "后端通道类型")
    private UpstreamChannelType upstreamChannelType;

    @ApiModelProperty(value = "zk集群")
    private RegistryZkCluster registryZkCluster;

    @ApiModelProperty(value = "登录态")
    private Boolean loginRequired;

    @ApiModelProperty(value = "超时时间")
    private Integer timeout;

    @ApiModelProperty(value = "访问权限idList")
    private List<Integer> permissionIdList;

    @ApiModelProperty(value= "接入点")
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<AccessPoint> accessPointList;

    @ApiModelProperty(value = "项目owner id")
    private String ownerUserId;

    @ApiModelProperty(value = "同步状态")
    private Boolean synced;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "最后修改时间")
    private Date dbUpdateTime;
}
