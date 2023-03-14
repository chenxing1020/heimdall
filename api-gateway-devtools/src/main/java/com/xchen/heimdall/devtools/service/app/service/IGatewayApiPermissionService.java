package com.xchen.heimdall.devtools.service.app.service;

import com.xchen.heimdall.devtools.service.app.vo.GatewayApiIdsVO;
import com.xchen.heimdall.devtools.service.app.vo.PermissionIdsVO;

/**
 * @author xchen
 * @date 2022/7/20
 */
public interface IGatewayApiPermissionService {

    /**
     * 更新gateway api对应的权限id
     * @param vo
     */
    void updatePermissionsByGatewayApiId(PermissionIdsVO vo);

    /**
     * 更新权限项对应的gateway Api
     * @param vo
     */
    void updateGatewayApisByPermissionId(GatewayApiIdsVO vo);
}
