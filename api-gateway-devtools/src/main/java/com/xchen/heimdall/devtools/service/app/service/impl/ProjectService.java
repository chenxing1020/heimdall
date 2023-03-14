package com.xchen.heimdall.devtools.service.app.service.impl;

import org.apache.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xchen.heimdall.devtools.service.app.dao.ProjectDAO;
import com.xchen.heimdall.devtools.service.app.domain.ProjectDO;
import com.xchen.heimdall.devtools.service.app.dto.ProjectDTO;
import com.xchen.heimdall.devtools.service.app.service.IProjectService;
import com.xchen.heimdall.devtools.service.app.vo.ListProjectVO;
import com.xchen.heimdall.devtools.service.app.vo.ProjectIdVO;
import com.xchen.heimdall.devtools.service.app.vo.ProjectVO;
import com.xchen.heimdall.common.constant.ProjectType;
import com.xchen.heimdall.dubbo.support.wrapper.PagedWrapper;
import com.xchen.heimdall.dubbo.support.wrapper.PagingWrapper;
import com.xchen.heimdall.dubbo.util.PagingUtil;
import com.xchen.heimdall.common.util.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author xchen
 * @date 2022/4/6
 */
@Slf4j
@Service
public class ProjectService implements IProjectService {

    @Resource
    private ProjectDAO projectDAO;

    @Resource
    private PojoService pojoService;

    @Resource
    private DubboService dubboService;

    @Override
    public PagedWrapper<ProjectDTO> listProject(PagingWrapper<ListProjectVO> vo) {
        LambdaQueryWrapper<ProjectDO> query = new LambdaQueryWrapper<>();
        if (Objects.nonNull(vo.getData())) {
            if (StringUtils.isNotEmpty(vo.getData().getProjectName())) {
                query.like(ProjectDO::getProjectName, vo.getData().getProjectName());
            }

            if (Objects.nonNull(vo.getData().getProjectType())) {
                query.eq(ProjectDO::getProjectType, vo.getData().getProjectType());
            }

            if (Objects.nonNull(vo.getData().getProjectId())) {
                query.eq(ProjectDO::getId, vo.getData().getProjectId());
            }
        }

        IPage<ProjectDO> page = projectDAO.selectPage(
                new Page<>(vo.getPageNum(), vo.getPageSize()),
                query
        );
        return PagingUtil.convert(page, ProjectDTO::new);
    }

    @Override
    public ProjectDTO addProject(ProjectVO vo) {

        ProjectDO projectDO = BeanUtil.convert(vo, ProjectDO::new);
        projectDO.setOwnerUserId(vo.getUserId());
        projectDO.setCreateUserId(vo.getUserId());
        projectDAO.insert(projectDO);
        return BeanUtil.convert(projectDO, ProjectDTO::new);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProject(ProjectVO vo) {
        ProjectDO projectDO = BeanUtil.convert(vo, ProjectDO::new);
        projectDO.setUpdateUserId(vo.getUserId());
        projectDAO.updateById(projectDO);

        // 更新代码生成状态
        dubboService.updateCodeStatusByProjectId(vo.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProject(ProjectIdVO vo) {

        // 清理所有pojo
        pojoService.deletePojoByProjectId(vo);

        // 清理所有service（清理service时会清理所有method）
        dubboService.deleteServiceByProjectId(vo);

        projectDAO.delete(new LambdaQueryWrapper<ProjectDO>()
                .eq(ProjectDO::getId, vo.getProjectId())
        );
    }

    @Override
    public List<ProjectType> listProjectType() {
        return Arrays.asList(ProjectType.values());
    }
}
