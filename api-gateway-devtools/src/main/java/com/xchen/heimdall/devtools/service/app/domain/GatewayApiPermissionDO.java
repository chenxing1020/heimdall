package com.xchen.heimdall.devtools.service.app.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xchen
 * @date 2022/7/18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "gateway_api_permission")
public class GatewayApiPermissionDO {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer gatewayApiId;

    private Integer permissionId;
}
