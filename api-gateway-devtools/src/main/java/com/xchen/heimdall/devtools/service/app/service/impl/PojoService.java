package com.xchen.heimdall.devtools.service.app.service.impl;

import org.apache.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xchen.heimdall.devtools.service.app.common.enums.CommonFieldType;
import com.xchen.heimdall.devtools.service.app.dao.PojoDAO;
import com.xchen.heimdall.devtools.service.app.dao.ProjectDAO;
import com.xchen.heimdall.devtools.service.app.domain.PojoDO;
import com.xchen.heimdall.devtools.service.app.domain.ProjectDO;
import com.xchen.heimdall.devtools.service.app.dto.FieldDTO;
import com.xchen.heimdall.devtools.service.app.dto.FieldTypeDTO;
import com.xchen.heimdall.devtools.service.app.dto.ParentPojoDTO;
import com.xchen.heimdall.devtools.service.app.dto.PojoDTO;
import com.xchen.heimdall.devtools.service.app.service.IPojoService;
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
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author 
 * @date 2022/4/13
 */
@Slf4j
@Service
public class PojoService implements IPojoService {

    @Resource
    private PojoDAO pojoDAO;
    @Resource
    private ProjectDAO projectDAO;
    @Resource
    private CommonUtils commonUtils;
    @Resource
    private GenerateService generateService;
    @Resource
    private DubboMethodService dubboMethodService;

    private static final String WORD_META_CHARACTER = "\\b";
    private static final String LINE_BREAK = "\n";

    @Override
    public PojoDTO addPojo(PojoVO vo) {
        PojoDO pojoDO = BeanUtil.convert(vo, PojoDO::new);

        // 填写pojoPath
        ProjectDO projectDO = projectDAO.selectById(pojoDO.getProjectId());
        pojoDO.setPojoPath(commonUtils.getPojoPath(projectDO, pojoDO.getPojoType()));

        pojoDAO.insert(pojoDO);
        return BeanUtil.convert(pojoDO, PojoDTO::new);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePojo(PojoVO vo) {
        if (Objects.isNull(vo.getId())) {
            throw new BadRequestException("Id not allow null");
        }

        PojoDO oldPojo = pojoDAO.selectById(vo.getId());
        String oldPojoName = oldPojo.getPojoName();
        String newPojoName = vo.getPojoName();

        // 如果变更pojoName，需要遍历当前project下的所有pojo的fieldList，并做全词替换
        if (!StringUtils.equals(oldPojoName, vo.getPojoName())) {
            Pattern oldPojoNamePattern = Pattern.compile(WORD_META_CHARACTER + oldPojoName + WORD_META_CHARACTER);
            List<PojoDO> pojoList = pojoDAO.selectList(new LambdaQueryWrapper<PojoDO>()
                    .ne(PojoDO::getId, vo.getId())
            );
            pojoList.forEach(pojo -> {
                List<FieldDTO> fields = pojo.getFieldList();
                fields.forEach(field -> replaceFieldName(field, oldPojoNamePattern, newPojoName));
                pojoDAO.updateById(pojo);
            });
        }

        PojoDO pojoDO = BeanUtil.convert(vo, PojoDO::new);

        // 填写pojoPath
        ProjectDO projectDO = projectDAO.selectById(pojoDO.getProjectId());
        pojoDO.setPojoPath(commonUtils.getPojoPath(projectDO, pojoDO.getPojoType()));

        pojoDAO.updateById(pojoDO);

        // 改变pojo时有可能对其他pojo种的fieldList产生影响，保险起见将project下的method状态都更新
        dubboMethodService.updateCodeStatusUnsyncedByProjectId(pojoDO.getProjectId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePojo(PojoIdVO vo) {
        // 删除相关联的method
        dubboMethodService.deleteMethodByPojoId(vo);

        pojoDAO.delete(new LambdaQueryWrapper<PojoDO>()
                .eq(PojoDO::getId, vo.getPojoId())
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public void deletePojoByProjectId(ProjectIdVO vo) {
        // 根据projectId清理pojo时也会清除method数据，故此处不用考虑级联删除
        pojoDAO.delete(new LambdaQueryWrapper<PojoDO>()
                .eq(PojoDO::getProjectId, vo.getProjectId())
        );
    }

    @Override
    public PagedWrapper<PojoDTO> listPojo(PagingWrapper<ListPojoVO> vo) {
        LambdaQueryWrapper<PojoDO> query = new LambdaQueryWrapper<PojoDO>()
                .eq(PojoDO::getPojoType, vo.getData().getPojoType());
        if (Objects.nonNull(vo.getData().getProjectId())) {
            query.and(queryWrapper -> {
                queryWrapper.eq(PojoDO::getProjectId, vo.getData().getProjectId())
                        .or()
                        // 基础字段的projectId默认小于0
                        .le(PojoDO::getProjectId, 0);
            });
        }

        if (StringUtils.isNotEmpty(vo.getData().getPojoName())) {
            query.like(PojoDO::getPojoName, vo.getData().getPojoName());
        }

        IPage<PojoDO> page = pojoDAO.selectPage(
                new Page<>(vo.getPageNum(), vo.getPageSize()),
                query
        );
        return PagingUtil.convert(page, PojoDTO::new);
    }

    @Override
    public List<ParentPojoDTO> listParentPojo(ListParentPojoVO vo) {
        List<PojoDO> pojos = pojoDAO.selectList(new LambdaQueryWrapper<PojoDO>()
                .eq(PojoDO::getPojoType, vo.getPojoType())
                .eq(PojoDO::getProjectId, vo.getProjectId())
        );
        return BeanUtil.convertList(pojos, ParentPojoDTO::new);
    }

    @Override
    public List<FieldTypeDTO> listFieldType(ProjectIdVO vo) {
        List<FieldTypeDTO> fieldTypeList = CommonFieldType.getCommonFieldTypeList();
        LambdaQueryWrapper<PojoDO> query = new LambdaQueryWrapper<PojoDO>()
                .eq(PojoDO::getProjectId, vo.getProjectId());

        List<PojoDO> pojoList = pojoDAO.selectList(query);
        List<FieldTypeDTO> result = pojoList.stream().map(pojo -> new FieldTypeDTO(pojo.getId(), pojo.getPojoName())).collect(Collectors.toList());

        fieldTypeList.addAll(result);

        return fieldTypeList;
    }

    @Override
    public List<Map<String, String>> listVoWrapperType() {
        return Arrays.stream(VoWrapperType.values()).map(item -> {
            Map<String, String> itemMap = new HashMap<>();
            itemMap.put("key", item.name());
            itemMap.put("value", item.getListType());
            return itemMap;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, String>> listDtoWrapperType() {
        return Arrays.stream(DtoWrapperType.values()).map(item -> {
            Map<String, String> itemMap = new HashMap<>();
            itemMap.put("key", item.name());
            itemMap.put("value", item.getListType());
            return itemMap;
        }).collect(Collectors.toList());
    }

    @Override
    public String previewPojo(PojoIdVO vo) {
        PojoDO pojo = pojoDAO.selectById(vo.getPojoId());
        return processPojoTemplate(pojo, vo.getUserId(), false);
    }

    public void batchGeneratePojo(Integer projectId, String userId) {
        List<PojoDO> pojoList = pojoDAO.selectList(new LambdaQueryWrapper<PojoDO>()
                .eq(PojoDO::getProjectId, projectId)
        );
        pojoList.forEach(pojo -> processPojoTemplate(pojo, userId, true));
    }

    private String processPojoTemplate(PojoDO pojo, String userId, Boolean needGenerate) {
        ProjectDO projectDO = projectDAO.selectById(pojo.getProjectId());
        String voPath = commonUtils.getPojoPath(projectDO, PojoType.VO);
        String dtoPath = commonUtils.getPojoPath(projectDO, PojoType.DTO);
        voPath = pojo.getPojoPath().equals(voPath) ? null : voPath;
        dtoPath = pojo.getPojoPath().equals(dtoPath) ? null : dtoPath;

        pojo.getFieldList().forEach(field -> {
            // 生成字段的模板注解
            commonUtils.setFieldTemplateAnnotation(field);
        });

        String parentPojoName = null;
        String parentPojoPath = null;
        if (Objects.nonNull(pojo.getParentId())) {
            // 特殊处理：
            // 父类id如果小于0，则为基础bo
            PojoDO parentPojo = pojoDAO.selectById(pojo.getParentId());
            parentPojoName = parentPojo.getPojoName();

        }

        Context context = new ContextBuilder()
                .setVariable("pojo", pojo)
                .setVariable("fields", pojo.getFieldList())
                .setVariable("parentPojoName", parentPojoName)
                .setVariable("parentPojoPath", parentPojoPath)
                .setVariable("voPath", voPath)
                .setVariable("dtoPath", dtoPath)
                .setVariable("lineBreak", LINE_BREAK)
                .getContext();

        // 模板制品
        String processResult = generateService.process(GenerateService.POJO_TEMPLATE, context);

        if (Boolean.TRUE.equals(needGenerate)) {
            generateService.generate(pojo.getPojoPath(),
                    commonUtils.getJavaFileName(pojo.getPojoName()),
                    processResult,
                    userId);
        }
        return processResult;
    }

    private void replaceFieldName(FieldDTO field, Pattern oldPojoNamePattern, String newPojoName) {
        field.setFieldType(RegExUtils.replaceAll(field.getFieldType(), oldPojoNamePattern, newPojoName));
    }
}
