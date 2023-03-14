package com.xchen.heimdall.devtools.service.app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xchen.heimdall.devtools.service.app.DmtServiceApp;
import com.xchen.heimdall.devtools.service.app.common.enums.CommonFieldType;
import com.xchen.heimdall.devtools.service.app.dao.PojoDAO;
import com.xchen.heimdall.devtools.service.app.domain.PojoDO;
import com.xchen.heimdall.devtools.service.app.dto.FieldTypeDTO;
import com.xchen.heimdall.devtools.service.app.dto.PojoDTO;
import com.xchen.heimdall.devtools.service.app.service.impl.PojoService;
import com.xchen.heimdall.devtools.service.app.vo.*;
import com.xchen.heimdall.common.constant.PojoType;
import com.xchen.heimdall.dubbo.support.wrapper.PagedWrapper;
import com.xchen.heimdall.dubbo.support.wrapper.PagingWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author xchen
 * @date 2022/5/6
 */
@Slf4j
@Sql({"/sql/init_test.sql"})
@SqlConfig(commentPrefix = "#")
@SpringBootTest(classes = {DmtServiceApp.class}, properties = "spring.profiles.active=test")
class PojoServiceTest {

    @Resource
    private PojoService pojoService;

    @Resource
    private PojoDAO pojoDAO;

    @Transactional
    @Test
    void testListPojo() {

        PagedWrapper<PojoDTO> pojoList = pojoService.listPojo(new PagingWrapper<>(
                ListPojoVO.builder()
                        .pojoType(PojoType.VO)
                        .pojoName("TestVO1")
                        .projectId(1)
                        .build(),
                1, 10
        ));
        log.info("List result: {}", pojoList);
        Assertions.assertEquals(1, pojoList.getData().size());
    }

    @Transactional
    @Test
    void testAddPojo() {
        pojoService.addPojo(PojoVO.builder()
                .id(5)
                .projectId(1)
                .pojoName("testVO3")
                .pojoDesc("this is a testPojo")
                .pojoType(PojoType.VO)
                .build()
        );

        PagedWrapper<PojoDTO> pojoList = pojoService.listPojo(new PagingWrapper<>(
                ListPojoVO.builder()
                        .pojoType(PojoType.VO)
                        .projectId(1)
                        .build(),
                1, 10
        ));
        log.info("List result: {}", pojoList);
        Assertions.assertEquals(3, pojoList.getData().size());
    }

    @Transactional
    @Test
    void testUpdatePojo() {

        pojoService.updatePojo(PojoVO.builder()
                .id(1)
                .projectId(1)
                .pojoName("testVO3")
                .pojoDesc("this is a testPojo")
                .pojoType(PojoType.DTO)
                .build()
        );

        PagedWrapper<PojoDTO> pojoList = pojoService.listPojo(new PagingWrapper<>(
                ListPojoVO.builder()
                        .pojoType(PojoType.VO)
                        .projectId(1)
                        .build(),
                1, 10
        ));

        log.info("List result: {}", pojoList);
        Assertions.assertEquals(1, pojoList.getData().size());

        // 校验fieldList更新
        PojoDO pojo2 = pojoDAO.selectOne(new LambdaQueryWrapper<PojoDO>()
                        .eq(PojoDO::getId, 2)
        );
        log.info("Pojo2: {}", pojo2);
        Assertions.assertEquals("testVO3", pojo2.getFieldList().get(0).getFieldType());

        PojoDO pojo3 = pojoDAO.selectOne(new LambdaQueryWrapper<PojoDO>()
                .eq(PojoDO::getId, 3)
        );
        log.info("Pojo3: {}", pojo3);
        Assertions.assertEquals("Map<String, testVO3>", pojo3.getFieldList().get(0).getFieldType());
    }

    @Transactional
    @Test
    void testDeletePojo() {
        pojoService.deletePojo(new PojoIdVO(1));

        PagedWrapper<PojoDTO> pojoList = pojoService.listPojo(new PagingWrapper<>(
                ListPojoVO.builder()
                        .projectId(1)
                        .pojoType(PojoType.DTO)
                        .pojoName("updateTestDTO")
                        .build(),
                1, 10
        ));
        Assertions.assertEquals(0, pojoList.getData().size());
    }

    @Transactional
    @Test
    void testDeletePojoByProjectId() {
        pojoService.deletePojoByProjectId(new ProjectIdVO(1));

        PagedWrapper<PojoDTO> pojoList = pojoService.listPojo(new PagingWrapper<>(
                ListPojoVO.builder()
                        .pojoType(PojoType.VO)
                        .projectId(1)
                        .build(),
                1, 10
        ));
        Assertions.assertEquals(0, pojoList.getData().size());
    }

    @Test
    @Transactional
    void testPreviewPojo() {
        String processResult = pojoService.previewPojo(new PojoIdVO(1));
        log.info("process result: {}", processResult);
        Assertions.assertTrue(StringUtils.isNotEmpty(processResult));
    }

    @Test
    @Transactional
    void testListFieldTypeList() {
        List<FieldTypeDTO> pojoList = pojoService.listFieldType(new ProjectIdVO(0));
        log.info("pojoList: {}", pojoList);
        Assertions.assertEquals(CommonFieldType.values().length, pojoList.size());

        pojoList = pojoService.listFieldType(new ProjectIdVO(1));
        log.info("pojoList: {}", pojoList);
        Assertions.assertEquals(CommonFieldType.values().length + 4, pojoList.size());
    }

    @Test
    @Transactional
    void listPojoWrapperTypeList() {
        log.info("vo wrapper: {}", pojoService.listVoWrapperType());
        log.info("dto wrapper: {}", pojoService.listDtoWrapperType());
    }

    @Test
    @Transactional
    void listParentPojo() {
        ListParentPojoVO vo = new ListParentPojoVO(PojoType.DTO);
        vo.setProjectId(1);
        log.info("parent pojo: {}", pojoService.listParentPojo(vo));
    }
}
