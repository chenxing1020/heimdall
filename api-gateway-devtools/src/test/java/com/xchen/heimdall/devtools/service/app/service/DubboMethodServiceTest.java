package com.xchen.heimdall.devtools.service.app.service;

import com.xchen.heimdall.devtools.service.app.DmtServiceApp;
import com.xchen.heimdall.devtools.service.app.dto.DubboMethodDTO;
import com.xchen.heimdall.devtools.service.app.service.impl.DubboMethodService;
import com.xchen.heimdall.devtools.service.app.vo.*;
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

/**
 * @author 
 */
@Slf4j
@Sql({"/sql/init_test.sql"})
@SqlConfig(commentPrefix = "#")
@SpringBootTest(classes = {DmtServiceApp.class}, properties = "spring.profiles.active=test")
class DubboMethodServiceTest {

    @Resource
    private DubboMethodService dubboMethodService;

    @Test
    @Transactional
    void testListAllMethod() {
        PagedWrapper<DubboMethodDTO> methodList = dubboMethodService.listMethod(new PagingWrapper<>(
                ListDubboMethodVO.builder()
                        .build(),
                1, 10
        ));
        log.info("List result: {}", methodList);
        Assertions.assertEquals(3, methodList.getData().size());
    }

    @Test
    @Transactional
    void testListMethodByMethodName() {
        PagedWrapper<DubboMethodDTO> methodList = dubboMethodService.listMethod(new PagingWrapper<>(
                ListDubboMethodVO.builder()
                        .methodName("test")
                        .build(),
                1, 10
        ));
        log.info("List result: {}", methodList);
        Assertions.assertEquals(3, methodList.getData().size());
    }

    @Test
    @Transactional
    void testListMethodByProjectName() {
        PagedWrapper<DubboMethodDTO> methodList = dubboMethodService.listMethod(new PagingWrapper<>(
                ListDubboMethodVO.builder()
                        .projectName("test")
                        .build(),
                1, 10
        ));
        log.info("List result: {}", methodList);
        Assertions.assertEquals(3, methodList.getData().size());
    }

    @Test
    @Transactional
    void testListMethodByServiceName() {
        PagedWrapper<DubboMethodDTO> methodList = dubboMethodService.listMethod(new PagingWrapper<>(
                ListDubboMethodVO.builder()
                        .serviceName("test")
                        .build(),
                1, 10
        ));
        log.info("List result: {}", methodList);
        Assertions.assertEquals(3, methodList.getData().size());
    }

    @Test
    @Transactional
    void testListMethodByCodeStatus() {
        PagedWrapper<DubboMethodDTO> methodList = dubboMethodService.listMethod(new PagingWrapper<>(
                ListDubboMethodVO.builder()
                        .codeStatus(1)
                        .build(),
                1, 10
        ));
        log.info("List result: {}", methodList);
        Assertions.assertEquals(1, methodList.getData().size());
    }

    @Test
    @Transactional
    void testAddMethod() {
        dubboMethodService.addMethod(DubboMethodVO.builder()
                .serviceId(1)
                .methodName("testMethod4")
                .voId(1)
                .dtoId(3)
                .apiDesc("testMethod")
                .build()
        );
        PagedWrapper<DubboMethodDTO> methodList = dubboMethodService.listMethod(new PagingWrapper<>(
                new ListDubboMethodVO(), 1, 10
        ));
        log.info("List result: {}", methodList);
        Assertions.assertEquals(4, methodList.getData().size());
    }


    @Transactional
    @Test
    void testUpdateMethod() {

        dubboMethodService.updateMethod(DubboMethodVO.builder()
                .id(1)
                .serviceId(1)
                .methodName("testMethod4")
                .voId(1)
                .dtoId(3)
                .apiDesc("testMethod")
                .build()
        );
        PagedWrapper<DubboMethodDTO> methodList = dubboMethodService.listMethod(new PagingWrapper<>(
                ListDubboMethodVO.builder()
                        .methodName("Method4")
                        .build(),
                1, 10
        ));
        log.info("List result: {}", methodList);
        Assertions.assertEquals(1, methodList.getData().size());
    }

    @Transactional
    @Test
    void testDeleteMethod() {

        dubboMethodService.deleteMethod(new DubboMethodIdVO(1));
        PagedWrapper<DubboMethodDTO> methodList = dubboMethodService.listMethod(new PagingWrapper<>(
                ListDubboMethodVO.builder()
                        .methodName("Method1")
                        .build(),
                1, 10
        ));
        log.info("List result: {}", methodList);
        Assertions.assertEquals(0, methodList.getData().size());
    }

    @Transactional
    @Test
    void testDeletMethodByServiceId() {
        dubboMethodService.deleteMethodByServiceId(new DubboServiceIdVO(1));
        PagedWrapper<DubboMethodDTO> methodList = dubboMethodService.listMethod(new PagingWrapper<>(
                ListDubboMethodVO.builder()
                        .serviceName("Service")
                        .build(),
                1, 10
        ));
        log.info("List result: {}", methodList);
        Assertions.assertEquals(0, methodList.getData().size());
    }

    @Transactional
    @Test
    void testDeleteMethodByPojoId() {
        dubboMethodService.deleteMethodByPojoId(new PojoIdVO(1));
        PagedWrapper<DubboMethodDTO> methodList = dubboMethodService.listMethod(new PagingWrapper<>(
                new ListDubboMethodVO(), 1, 10
        ));
        log.info("List result: {}", methodList);
        Assertions.assertEquals(1, methodList.getData().size());
    }

}