package com.xchen.heimdall.devtools.service.app.service.impl;

import org.apache.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xchen.heimdall.devtools.service.app.dao.DubboServiceDAO;
import com.xchen.heimdall.devtools.service.app.dao.ProjectDAO;
import com.xchen.heimdall.devtools.service.app.domain.DubboMethodDetailDO;
import com.xchen.heimdall.devtools.service.app.domain.DubboServiceDO;
import com.xchen.heimdall.devtools.service.app.domain.ProjectDO;
import com.xchen.heimdall.devtools.service.app.dto.DubboServiceDTO;
import com.xchen.heimdall.devtools.service.app.service.IDubboService;
import com.xchen.heimdall.devtools.service.app.utils.CommonUtils;
import com.xchen.heimdall.devtools.service.app.utils.ContextBuilder;
import com.xchen.heimdall.devtools.service.app.vo.*;
import com.xchen.heimdall.common.constant.DtoWrapperType;
import com.xchen.heimdall.common.constant.PojoType;
import com.xchen.heimdall.common.constant.VoWrapperType;
import com.xchen.heimdall.dubbo.support.wrapper.PagedWrapper;
import com.xchen.heimdall.dubbo.support.wrapper.PagingWrapper;
import com.xchen.heimdall.dubbo.util.PagingUtil;
import com.xchen.heimdall.common.exception.errorcode.BadRequestException;
import com.xchen.heimdall.common.util.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 
 * @date 2022/4/14
 */
@Slf4j
@Service
public class DubboService implements IDubboService {

    @Resource
    private ProjectDAO projectDAO;
    @Resource
    private DubboServiceDAO dubboServiceDAO;
    @Resource
    private PojoService pojoService;
    @Resource
    private DubboMethodService dubboMethodService;
    @Resource
    private GenerateService generateService;
    @Resource
    private CommonUtils commonUtils;

    @Override
    public PagedWrapper<DubboServiceDTO> listService(PagingWrapper<ListDubboServiceVO> vo) {
        LambdaQueryWrapper<DubboServiceDO> query = new LambdaQueryWrapper<>();

        if (Objects.nonNull(vo.getData())) {
            if (Objects.nonNull(vo.getData().getProjectId())) {
                query.eq(DubboServiceDO::getProjectId, vo.getData().getProjectId());
            }

            if (Objects.nonNull(vo.getData().getServiceId())) {
                query.eq(DubboServiceDO::getId, vo.getData().getServiceId());
            }

            if (StringUtils.isNotEmpty(vo.getData().getServiceName())) {
                query.like(DubboServiceDO::getServiceName, vo.getData().getServiceName());
            }
        }

        IPage<DubboServiceDO> page = dubboServiceDAO.selectPage(
                new Page<>(vo.getPageNum(), vo.getPageSize()),
                query
        );
        return PagingUtil.convert(page, DubboServiceDTO::new);
    }

    @Override
    public DubboServiceDTO addService(DubboServiceVO vo) {
        DubboServiceDO serviceDO = BeanUtil.convert(vo, DubboServiceDO::new);

        // 根据projectName生成service包路径
        ProjectDO projectDO = projectDAO.selectById(vo.getProjectId());
        serviceDO.setServicePath(commonUtils.getProjectPath(projectDO.getProjectName()));

        String userId = vo.getUserId();
        serviceDO.setCreateUserId(userId);
        serviceDO.setUpdateUserId(userId);
        dubboServiceDAO.insert(serviceDO);
        return BeanUtil.convert(serviceDO, DubboServiceDTO::new);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateService(DubboServiceVO vo) {
        DubboServiceDO serviceDO = BeanUtil.convert(vo, DubboServiceDO::new);

        // 根据projectName生成service包路径
        ProjectDO projectDO = projectDAO.selectById(vo.getProjectId());
        serviceDO.setServicePath(commonUtils.getProjectPath(projectDO.getProjectName()));

        serviceDO.setUpdateUserId(vo.getUserId());
        dubboServiceDAO.updateById(serviceDO);

        // 更新生成状态
        dubboMethodService.updateCodeStatusUnsyncedByServiceId(vo.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteService(DubboServiceIdVO vo) {

        // 清理前所有service下所有method
        dubboMethodService.deleteMethodByServiceId(vo);

        dubboServiceDAO.delete(new LambdaQueryWrapper<DubboServiceDO>()
                .eq(DubboServiceDO::getId, vo.getServiceId())
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteServiceByProjectId(ProjectIdVO vo) {
        dubboServiceDAO.delete(new LambdaQueryWrapper<DubboServiceDO>()
                .eq(DubboServiceDO::getProjectId, vo.getProjectId())
        );
    }

    @Override
    public String previewService(DubboServiceIdVO vo) {
        DubboServiceDO service = dubboServiceDAO.selectById(vo.getServiceId());
        return processServiceTemplate(service, vo.getUserId(), false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String generateAllService(AssigneeUserIdVO vo) {
        log.info("PreHandling generate code...");

        if (StringUtils.isEmpty(vo.getAssigneeUserId())) {
            vo.setAssigneeUserId("xchen");
        }

        generateService.preGenerate(vo.getUserId());

        log.info("Handling generate code...");
        Set<Integer> unsyncedServiceIds = dubboMethodService.getUnsyncedServiceIds();
        List<DubboServiceDO> services = dubboServiceDAO.selectBatchIds(unsyncedServiceIds);
        services.forEach(service -> processServiceTemplate(service, vo.getUserId(), true));

        log.info("PostHandling generating code...");
        String mrWebUrl = generateService.postGenerate(vo);
        // 生成后更新所有的codeStatus
        dubboMethodService.updateCodeStatusSyncedAll();
        return mrWebUrl;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateCodeStatusByProjectId(Integer projectId) {
        List<DubboServiceDO> services = dubboServiceDAO.selectList(new LambdaQueryWrapper<DubboServiceDO>()
                .eq(DubboServiceDO::getProjectId, projectId)
        );
        Set<Integer> serviceIdSet = services.stream()
                .map(DubboServiceDO::getId).collect(Collectors.toSet());
        dubboMethodService.updateCodeStatusUnsyncedByServiceIdSet(serviceIdSet);
    }

    private String processServiceTemplate(DubboServiceDO service, String userId, Boolean needGenerate) {
        List<DubboMethodDetailDO> methodDetails = dubboMethodService.listMethodDetailByServiceId(service.getId());
        String serviceName = service.getServiceName();
        String ownerUserId = service.getOwnerUserId();
        ProjectDO projectDO = projectDAO.selectById(service.getProjectId());
        String voPath = commonUtils.getPojoPath(projectDO, PojoType.VO);
        String dtoPath = commonUtils.getPojoPath(projectDO, PojoType.DTO);

        if (!CollectionUtils.isEmpty(methodDetails)) {
            Set<String> dependencySet = new HashSet<>();
            methodDetails.forEach(methodDetail -> {
                /* 处理 dependency 和 模板类名 */
                handleMethodVoWrapper(methodDetail, dependencySet);
                handleMethodDtoWrapper(methodDetail, dependencySet);
                // 加入异常类的dependency
                if (Objects.nonNull(methodDetail.getMethodException())) {
                    dependencySet.add(methodDetail.getMethodException().getClassName());
                }
            });

            Context context = new ContextBuilder()
                    .setVariable("voPath", voPath)
                    .setVariable("dtoPath", dtoPath)
                    .setVariable("ownerId", ownerUserId)
                    .setVariable("service", service)
                    .setVariable("dependencySet", dependencySet)
                    .setVariable("serviceName", serviceName)
                    .setVariable("methodDetails", methodDetails)
                    .getContext();

            // 模板制品
            String processResult = generateService.process(GenerateService.SERVICE_TEMPLATE, context);

            if (Boolean.TRUE.equals(needGenerate)) {
                // 生成service前，先生成所依赖的vo、dto
                pojoService.batchGeneratePojo(service.getProjectId(), userId);

                generateService.generate(service.getServicePath(),
                        commonUtils.getJavaFileName(serviceName),
                        processResult,
                        userId
                );
            }
            return processResult;
        }
        return null;
    }


    /**
     * 处理vo依赖以及类型模板
     *
     * @param methodDetail  方法详情
     * @param dependencySet 依赖集合
     */
    private void handleMethodVoWrapper(DubboMethodDetailDO methodDetail, Set<String> dependencySet) {
        // 处理vo类型
        VoWrapperType voWrapperType = methodDetail.getVoWrapperType();
        String voName = methodDetail.getVoName();
        switch (voWrapperType) {
            case DEFAULT:
                // 基础类型
                dependencySet.add(commonUtils.getVoFullName(methodDetail));
                methodDetail.setVoTemplateName(voName + " vo");
                break;
            case NONE:
                // 无参置空
                methodDetail.setVoTemplateName("");
                break;
            case PAGING:
            case LIST:
                // 列表、分页泛型
                dependencySet.add(voWrapperType.getWrapperFullName());
                methodDetail.setVoTemplateName(commonUtils.getPojoTemplate(voWrapperType.getWrapperName(), voName) + " vo");
                break;
            default:
                throw new BadRequestException("Unsupported voWrapperType: " + methodDetail);
        }
    }

    /**
     * 处理dto依赖以及类型模板
     *
     * @param methodDetail  方法详情
     * @param dependencySet 依赖集合
     */
    private void handleMethodDtoWrapper(DubboMethodDetailDO methodDetail, Set<String> dependencySet) {
        // 处理dto类型
        DtoWrapperType dtoWrapperType = methodDetail.getDtoWrapperType();
        String dtoName = methodDetail.getDtoName();
        switch (dtoWrapperType) {
            case DEFAULT:
                // 基础类型
                dependencySet.add(commonUtils.getDtoFullName(methodDetail));
                methodDetail.setDtoTemplateName(dtoName);
                break;
            case LIST:
            case PAGED:
                // 列表和分页泛型
                dependencySet.add(dtoWrapperType.getWrapperFullName());
                methodDetail.setDtoTemplateName(commonUtils.getPojoTemplate(dtoWrapperType.getWrapperName(), dtoName));
                break;
            case VOID:
                // 无参方法
                methodDetail.setDtoTemplateName("void");
                break;
            case SUBLIST:
                // 订阅请求
                dependencySet.add(dtoWrapperType.getWrapperFullName());
                methodDetail.setDtoTemplateName(dtoWrapperType.getWrapperName());
                break;
            default:
                throw new BadRequestException("Unsupported dtoWrapperType: " + methodDetail);
        }
    }
}
