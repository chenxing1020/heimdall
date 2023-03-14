package com.xchen.heimdall.devtools.service.app.service;

import com.xchen.heimdall.devtools.service.app.DmtServiceApp;
import com.xchen.heimdall.devtools.service.app.service.impl.GatewayApiPermissionService;
import com.xchen.heimdall.devtools.service.app.vo.GatewayApiIdsVO;
import com.xchen.heimdall.devtools.service.app.vo.PermissionIdsVO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;

/**
 * @author xchen
 * @date 2022/7/18
 */
@Slf4j
@SpringBootTest(classes = {DmtServiceApp.class}, properties = "spring.profiles.active=test")
class GatewayApiPermissionServiceTest {

    @Resource
    private GatewayApiPermissionService gatewayApiPermissionService;

    @Test
    @Transactional
    void testUpdatePermissions() {
        gatewayApiPermissionService.updatePermissionsByGatewayApiId(
                new PermissionIdsVO(1, Arrays.asList(1, 2, 3, 4, 5))
        );
        Assertions.assertEquals(5,
                gatewayApiPermissionService.selectPermissionIds(1).size()
        );

        gatewayApiPermissionService.updatePermissionsByGatewayApiId(
                new PermissionIdsVO(1, Arrays.asList(1, 2, 3))
        );
        Assertions.assertEquals(3,
                gatewayApiPermissionService.selectPermissionIds(1).size()
        );

        gatewayApiPermissionService.updatePermissionsByGatewayApiId(new PermissionIdsVO(1, null));
        Assertions.assertEquals(0,
                gatewayApiPermissionService.selectPermissionIds(1).size()
        );
    }

    @Test
    @Transactional
    void testUpdateGatewayApis() {
        gatewayApiPermissionService.updateGatewayApisByPermissionId(
                new GatewayApiIdsVO(1, Arrays.asList(1, 2, 3, 4, 5))
        );
        Assertions.assertEquals(5,
                gatewayApiPermissionService.selectGatewayApiIds(1).size());

        gatewayApiPermissionService.updateGatewayApisByPermissionId(
                new GatewayApiIdsVO(1, Arrays.asList(1, 2, 3))
        );
        Assertions.assertEquals(3,
                gatewayApiPermissionService.selectGatewayApiIds(1).size());

        gatewayApiPermissionService.updateGatewayApisByPermissionId(new GatewayApiIdsVO(1, null));
        Assertions.assertEquals(0,
                gatewayApiPermissionService.selectGatewayApiIds(1).size());
    }
}
