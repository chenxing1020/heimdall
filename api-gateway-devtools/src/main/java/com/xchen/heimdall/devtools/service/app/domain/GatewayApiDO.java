package com.xchen.heimdall.devtools.service.app.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.xchen.heimdall.common.constant.AccessPoint;
import com.xchen.heimdall.common.constant.UpstreamChannelType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
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
public class GatewayApiDO implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer serviceId;

    private Integer methodId;

    private Boolean loginRequired;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<AccessPoint> accessPointList;

    private UpstreamChannelType upstreamChannelType;

    private Boolean synced;

    private String ownerUserId;

    private String remark;

    @TableField(select = false)
    private String createUserId;
    @TableField(select = false)
    private String updateUserId;
    @TableLogic(value = "'1970-01-01 00:00:00'", delval = "now()")
    @TableField(select = false)
    private LocalDateTime deleteTime;
}
