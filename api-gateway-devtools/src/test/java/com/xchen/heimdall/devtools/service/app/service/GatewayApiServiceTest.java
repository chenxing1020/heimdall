package com.xchen.heimdall.devtools.service.app.service;

import com.xchen.heimdall.devtools.service.app.dao.DubboMethodDAO;
import com.xchen.heimdall.devtools.service.app.domain.DubboMethodDO;
import com.xchen.heimdall.devtools.service.app.dto.GatewayApiDTO;
import com.xchen.heimdall.devtools.service.app.service.impl.GatewayApiService;
import com.xchen.heimdall.devtools.service.app.vo.GatewayApiIdVO;
import com.xchen.heimdall.devtools.service.app.vo.GatewayApiVO;
import com.xchen.heimdall.devtools.service.app.vo.ListGatewayApiVO;
import com.xchen.heimdall.common.constant.AccessPoint;
import com.xchen.heimdall.common.constant.UpstreamChannelType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * @author 
 * @date 2022/4/15
 */
@Slf4j
@SqlConfig(commentPrefix = "#")
@Sql({"/sql/init_test.sql"})
@SpringBootTest(properties = "spring.profiles.active=test")
class GatewayApiServiceTest {

    @Resource
    private GatewayApiService gatewayApiService;

    @Resource
    private DubboMethodDAO dubboMethodDAO;

    @Test
    @Transactional
    void testListAllDubboGatewayApi() {
        List<GatewayApiDTO> gatewayApis = gatewayApiService.listAllDubboGatewayApi(new ListGatewayApiVO());
        log.info("result list: {}", gatewayApis);
        Assertions.assertEquals(2, gatewayApis.size());

        gatewayApis = gatewayApiService.listAllDubboGatewayApi(
                ListGatewayApiVO.builder()
                        .accessPoint(AccessPoint.INTRANET)
                        .build()
        );
        log.info("result list: {}", gatewayApis);
        Assertions.assertEquals(2, gatewayApis.size());

        gatewayApis = gatewayApiService.listAllDubboGatewayApi(
                ListGatewayApiVO.builder()
                        .accessPoint(AccessPoint.INTERNET)
                        .build()
        );
        log.info("result list: {}", gatewayApis);
        Assertions.assertEquals(1, gatewayApis.size());
    }

    @Test
    @Transactional
    void testAdd() {
        gatewayApiService.addGatewayApi(GatewayApiVO.builder()
                .serviceId(1)
                .methodId(2)
                .upstreamChannelType(UpstreamChannelType.DUBBO)
                .loginRequired(false)
                .accessPointList(Collections.singletonList(AccessPoint.INTERNET))
                .build()
        );

        List<GatewayApiDTO> gatewayApis = gatewayApiService.listAllDubboGatewayApi(new ListGatewayApiVO());
        log.info("result list: {}", gatewayApis);
        Assertions.assertEquals(3, gatewayApis.size());

        gatewayApis = gatewayApiService.listAllDubboGatewayApi(new ListGatewayApiVO(null, AccessPoint.INTERNET));
        log.info("result list: {}", gatewayApis);
        Assertions.assertEquals(2, gatewayApis.size());

        // 测试gateway api生成状态
        DubboMethodDO dubboMethod = dubboMethodDAO.selectById(2);
        Assertions.assertEquals(true, dubboMethod.getGatewayApiStatus());
    }

    @Test
    @Transactional
    void testUpdate() {
        gatewayApiService.updateGatewayApi(GatewayApiVO.builder()
                .id(1)
                .serviceId(1)
                .methodId(2)
                .upstreamChannelType(UpstreamChannelType.DUBBO)
                .loginRequired(true)
                .build()
        );

        // update后会将api转为未发布状态
        List<GatewayApiDTO> gatewayApis = gatewayApiService.listUnsyncedDubboGatewayApi();
        log.info("result list: {}", gatewayApis);
        Assertions.assertEquals(2, gatewayApis.size());
    }

    @Test
    @Transactional
    void testDelete() {
        gatewayApiService.deleteGatewayApi(GatewayApiIdVO.builder()
                .gatewayApiId(1)
                .build()
        );

        // update后会将api转为未发布状态
        List<GatewayApiDTO> gatewayApis = gatewayApiService.listAllDubboGatewayApi(new ListGatewayApiVO());
        log.info("result list: {}", gatewayApis);
        Assertions.assertEquals(1, gatewayApis.size());

        // 测试gateway api生成状态
        DubboMethodDO dubboMethod = dubboMethodDAO.selectById(1);
        Assertions.assertEquals(false, dubboMethod.getGatewayApiStatus());
    }
}
