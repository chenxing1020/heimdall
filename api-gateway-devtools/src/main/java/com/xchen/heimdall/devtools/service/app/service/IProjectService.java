package com.xchen.heimdall.devtools.service.app.service;

import com.xchen.heimdall.devtools.service.app.dto.ProjectDTO;
import com.xchen.heimdall.devtools.service.app.vo.ListProjectVO;
import com.xchen.heimdall.devtools.service.app.vo.ProjectIdVO;
import com.xchen.heimdall.devtools.service.app.vo.ProjectVO;
import com.xchen.heimdall.common.constant.ProjectType;
import com.xchen.heimdall.dubbo.support.wrapper.PagedWrapper;
import com.xchen.heimdall.dubbo.support.wrapper.PagingWrapper;

import java.util.List;

/**
 * @author xchen
 * @date 2022/4/5
 */
public interface IProjectService {

    /**
     * 根据 id、项目类型 或 项目名称检索
     *
     * @param vo 项目listVo
     * @return 分页查询结果
     */
    PagedWrapper<ProjectDTO> listProject(PagingWrapper<ListProjectVO> vo);

    /**
     * 新增项目
     *
     * @param vo 项目vo
     * @return 生成结果
     */
    ProjectDTO addProject(ProjectVO vo);

    /**
     * 更新项目
     *
     * @param vo 项目vo
     */
    void updateProject(ProjectVO vo);

    /**
     * 删除项目
     * 注：删除项目时，需要删除对应的 service、method以及pojo
     *
     * @param vo 项目idVo
     */
    void deleteProject(ProjectIdVO vo);

    /**
     * 查询工程类型
     * @return 查询结果
     */
    List<ProjectType> listProjectType();
}
