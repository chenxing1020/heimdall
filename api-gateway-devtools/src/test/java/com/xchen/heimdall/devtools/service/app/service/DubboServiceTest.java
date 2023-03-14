package com.xchen.heimdall.devtools.service.app.service;

import com.xchen.heimdall.devtools.service.app.DmtServiceApp;
import com.xchen.heimdall.devtools.service.app.dto.DubboServiceDTO;
import com.xchen.heimdall.devtools.service.app.service.impl.DubboService;
import com.xchen.heimdall.devtools.service.app.utils.GitLabUtils;
import com.xchen.heimdall.devtools.service.app.vo.DubboServiceIdVO;
import com.xchen.heimdall.devtools.service.app.vo.DubboServiceVO;
import com.xchen.heimdall.devtools.service.app.vo.ListDubboServiceVO;
import com.xchen.heimdall.devtools.service.app.vo.ProjectIdVO;
import com.xchen.heimdall.common.constant.RegistryZkCluster;
import com.xchen.heimdall.dubbo.support.wrapper.PagedWrapper;
import com.xchen.heimdall.dubbo.support.wrapper.PagingWrapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author 
 * @date 2022/4/14
 */
@Slf4j
@Sql({"/sql/init_test.sql"})
@SqlConfig(commentPrefix = "#")
@SpringBootTest(classes = {DmtServiceApp.class}, properties = "spring.profiles.active=test")
class DubboServiceTest {

    @Resource
    private DubboService dubboService;



    @Resource
    private GitLabUtils gitLabUtils;

    @Test
    @Transactional
    void testListService() {
        PagedWrapper<DubboServiceDTO> serviceList = dubboService.listService(new PagingWrapper<>(
                ListDubboServiceVO.builder()
                        .projectId(1)
                        .serviceName("test")
                        .build(),
                1, 10
        ));
        log.info("List result: {}", serviceList);
        assertEquals(1, serviceList.getData().size());
    }

    @Test
    @Transactional
    void testAddService() {
        dubboService.addService(DubboServiceVO.builder()
                .id(2)
                .projectId(1)
                .serviceName("testService1")
                .simpleServiceName("testService1")
                .registryZkCluster(RegistryZkCluster.BUSINESS)
                .timeout(2000)
                .userId("testUser")
                .build()
        );

        PagedWrapper<DubboServiceDTO> serviceList = dubboService.listService(new PagingWrapper<>(
                ListDubboServiceVO.builder().projectId(1).serviceName("test").build(),
                1, 10
        ));
        log.info("List result: {}", serviceList);
        assertEquals(2, serviceList.getData().size());
    }

    @Test
    @Transactional
    void testUpdateService() {
        dubboService.updateService(DubboServiceVO.builder()
                .id(1)
                .projectId(1)
                .serviceName("testService1")
                .simpleServiceName("testService1")
                .registryZkCluster(RegistryZkCluster.BUSINESS)
                .timeout(2000)
                .userId("testUser")
                .build());
        PagedWrapper<DubboServiceDTO> serviceList = dubboService.listService(new PagingWrapper<>(
                ListDubboServiceVO.builder().projectId(1).serviceName("testService1").build(),
                1, 10
        ));
        log.info("List result: {}", serviceList);
        assertEquals(1, serviceList.getData().size());
    }

    @Test
    @Transactional
    void testDeleteService() {
        dubboService.deleteService(new DubboServiceIdVO(1));

        PagedWrapper<DubboServiceDTO> serviceList = dubboService.listService(new PagingWrapper<>(
                ListDubboServiceVO.builder()
                        .projectId(1)
                        .build(), 1, 10
        ));
        Assertions.assertEquals(0, serviceList.getData().size());
    }

    @Test
    @Transactional
    void testDeleteServiceByProjectId() {
        dubboService.deleteServiceByProjectId(new ProjectIdVO(1));

        PagedWrapper<DubboServiceDTO> serviceList = dubboService.listService(new PagingWrapper<>(
                ListDubboServiceVO.builder()
                        .projectId(1)
                        .build(), 1, 10
        ));
        Assertions.assertEquals(0, serviceList.getData().size());
    }

    @Test
    @Transactional
    void testPreviewService() {
//        DubboServiceIdVO vo = new DubboServiceIdVO(10);
//        vo.setUserId("xchen");
//        dubboService.generateAllService(vo);
    }
}
