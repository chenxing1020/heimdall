package com.xchen.heimdall.devtools.service.app.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xchen.heimdall.devtools.service.app.domain.GatewayApiPermissionDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author xchen
 * @date 2022/7/18
 */
public interface GatewayApiPermissionDAO extends BaseMapper<GatewayApiPermissionDO> {

    List<Integer> selectPermissionIds(@Param("gatewayApiId") Integer gatewayApiId);

    List<Integer> selectGatewayApiIds(@Param("permissionId") Integer permissionId);

    void insertBatchPermission(@Param("gatewayApiId") Integer gatewayApiId,
                               @Param("permissionIds") List<Integer> permissionIds);

    void insertBatchGatewayApi(@Param("permissionId") Integer permissionId,
                               @Param("gatewayApiIds") List<Integer> gatewayApiIds);


}
