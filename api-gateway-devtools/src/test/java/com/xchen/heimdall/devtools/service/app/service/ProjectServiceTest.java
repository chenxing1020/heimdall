package com.xchen.heimdall.devtools.service.app.service;

import com.xchen.heimdall.devtools.service.app.DmtServiceApp;
import com.xchen.heimdall.devtools.service.app.service.impl.ProjectService;
import com.xchen.heimdall.common.constant.ProjectType;
import com.xchen.heimdall.devtools.service.app.dto.ProjectDTO;
import com.xchen.heimdall.devtools.service.app.vo.ListProjectVO;
import com.xchen.heimdall.devtools.service.app.vo.ProjectIdVO;
import com.xchen.heimdall.devtools.service.app.vo.ProjectVO;
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author 
 */
@Slf4j
@Sql({"/sql/init_test.sql"})
@SqlConfig(commentPrefix = "#")
@SpringBootTest(classes = {DmtServiceApp.class}, properties = "spring.profiles.active=test")
class ProjectServiceTest {

    @Resource
    private ProjectService projectService;

    @Transactional
    @Test
    void testListProjects() {
        PagedWrapper<ProjectDTO> projectList = projectService.listProject(new PagingWrapper<>(
                ListProjectVO.builder().projectName("test").build(), 1, 10
        ));
        log.info("List result: {}", projectList);
        assertEquals(1, projectList.getData().size());

    }

    @Transactional
    @Test
    void testAddProject() {
        ProjectVO vo = ProjectVO.builder()
                .id(2)
                .projectName("testProject1")
                .projectType(ProjectType.service)
                .build();
        projectService.addProject(vo);

        PagedWrapper<ProjectDTO> projectList = projectService.listProject(new PagingWrapper<>(
                ListProjectVO.builder().projectName("test").build(),
                1, 10
        ));
        log.info("List result: {}", projectList);
        assertEquals(2, projectList.getData().size());

    }

    @Transactional
    @Test
    void testUpdateProject() {
        assertDoesNotThrow(() -> projectService.updateProject(
                ProjectVO.builder()
                        .id(1)
                        .userId("testUser1")
                        .projectType(ProjectType.client)
                        .projectName("testProjectUpdate")
                        .build())
        );
    }

    @Transactional
    @Test
    void testDeleteProject() {

        projectService.deleteProject(new ProjectIdVO(1));

        PagedWrapper<ProjectDTO> projectList = projectService.listProject(new PagingWrapper<>(
                ListProjectVO.builder()
                        .projectId(1)
                        .projectName("testProject")
                        .build(), 1, 10
        ));
        log.info("List result: {}", projectList);
        Assertions.assertEquals(0, projectList.getData().size());
    }
}