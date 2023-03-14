package com.xchen.heimdall.devtools.service.app.service.impl;

import org.apache.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import com.xchen.heimdall.devtools.service.app.common.constant.DmtConstant;
import com.xchen.heimdall.devtools.service.app.common.constant.ErrorCode;
import com.xchen.heimdall.devtools.service.app.common.enums.VersionTypeEnum;
import com.xchen.heimdall.devtools.service.app.dao.ConfigVersionDAO;
import com.xchen.heimdall.devtools.service.app.dao.ErrorCodeConfigDAO;
import com.xchen.heimdall.devtools.service.app.domain.ConfigVersionDO;
import com.xchen.heimdall.devtools.service.app.domain.ErrorCodeConfDO;
import com.xchen.heimdall.devtools.service.app.dto.ErrorCodeConfDTO;
import com.xchen.heimdall.devtools.service.app.manager.ApolloConfigManager;
import com.xchen.heimdall.devtools.service.app.service.ErrorCodeConfigService;
import com.xchen.heimdall.devtools.service.app.vo.BaseVO;
import com.xchen.heimdall.devtools.service.app.vo.ErrorCodeConfVO;
import com.xchen.heimdall.devtools.service.app.vo.qo.ErrorCodeConfQO;
import com.xchen.heimdall.dubbo.support.wrapper.PagedWrapper;
import com.xchen.heimdall.dubbo.support.wrapper.PagingWrapper;
import com.xchen.heimdall.dubbo.util.PagingUtil;
import com.xchen.heimdall.common.exception.errorcode.CustomException;
import com.xchen.heimdall.common.util.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ErrorCodeConfigServiceImpl implements ErrorCodeConfigService {

    @Autowired
    private ErrorCodeConfigDAO errorCodeConfigDAO;

    @Autowired
    private ConfigVersionDAO configVersionDAO;

    @Autowired
    private ApolloConfigManager apolloConfigManager;

    /**
     * 新增错误码
     *
     * @param vo 错误码设置
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addErrorCode(ErrorCodeConfVO vo) {
        String codePrefix = "" + vo.getLevel() + vo.getProjectId();
        if (vo.getErrorCode().length() != 9 || !vo.getErrorCode().startsWith(codePrefix)) {
            throw new CustomException(ErrorCode.FORMAT_ERROR, "错误码编号格式错误："+vo.getErrorCode());
        }
        List<ErrorCodeConfDO> list = errorCodeConfigDAO.query(ErrorCodeConfDO.builder()
                .errorCode(vo.getErrorCode())
                .version(DmtConstant.VER_DRAFT).build());
        if (!CollectionUtils.isEmpty(list)) {
            throw new CustomException(ErrorCode.DUPLICATE_CODE, "编号重复");
        }
        ErrorCodeConfDO errorCodeConfDO = BeanUtil.convert(vo, ErrorCodeConfDO::new);
        errorCodeConfDO.setCreateUser(vo.getUserId());
        errorCodeConfDO.setUpdateUser(vo.getUserId());
        errorCodeConfDO.setVersion(DmtConstant.VER_DRAFT);
        errorCodeConfigDAO.insert(errorCodeConfDO);
        pushApollo(errorCodeConfDO);
    }

    private void pushApollo(ErrorCodeConfDO errorCodeConfDO) {
        List<OpenItemDTO> configList = new ArrayList<>();
        OpenItemDTO item = new OpenItemDTO();
        item.setKey(errorCodeConfDO.getErrorCode());
        item.setValue(errorCodeConfDO.getDisplayMessage());
        configList.add(item);
        apolloConfigManager.syncConfig(DmtConstant.NAMESPACE_ERROR_CODE, configList);
    }

    /**
     * 修改错误码
     *
     * @param vo 错误码设置
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateErrorCode(ErrorCodeConfVO vo) {
        List<ErrorCodeConfDO> list = errorCodeConfigDAO.query(ErrorCodeConfDO.builder()
                .errorCode(vo.getErrorCode())
                .version(DmtConstant.VER_DRAFT).build());
        if (CollectionUtils.isEmpty(list)) {
            throw new CustomException(ErrorCode.NOT_EXIST, "错误码不存在");
        }
        ErrorCodeConfDO errorCodeConfDO = BeanUtil.convert(vo, ErrorCodeConfDO::new);
        errorCodeConfDO.setUpdateUser(vo.getUserId());
        errorCodeConfDO.setId(list.get(0).getId());
        errorCodeConfigDAO.updateById(errorCodeConfDO);
        if (vo.getDisplayMessage() != null) {
            pushApollo(errorCodeConfDO);
        }
    }

    /**
     * 删除错误码
     *
     * @param vo 错误码ID
     */
    @Override
    public void deleteErrorCode(BaseVO vo) {
        errorCodeConfigDAO.deleteById(vo.getId());
    }

    /**
     * 分页查询错误码
     *
     * @param condition 查询条件
     * @return r
     */
    @Override
    public PagedWrapper<ErrorCodeConfDTO> queryErrorCodeConfig(PagingWrapper<ErrorCodeConfQO> condition) {
        if (condition.getData() == null) {
            return new PagedWrapper<>();
        }
        ErrorCodeConfQO data = condition.getData();
        Page<?> page = new Page<>(condition.getPageNum(), condition.getPageSize());
        IPage<ErrorCodeConfDTO> result = errorCodeConfigDAO.queryPage(page, data);

        return PagingUtil.convert(result, ErrorCodeConfDTO::new);
    }

    /**
     * 发布错误码
     * @param errorCodeConf
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void publishConfig(ErrorCodeConfQO errorCodeConf) {
        //单个错误码发布
        if (StringUtils.isNotBlank(errorCodeConf.getErrorCode())) {
            ErrorCodeConfDO condition = new ErrorCodeConfDO();
            condition.setVersion(DmtConstant.VER_DRAFT);
            condition.setErrorCode(errorCodeConf.getErrorCode());
            List<ErrorCodeConfDO> pubList = errorCodeConfigDAO.query(condition);
            releaseConfig(pubList, errorCodeConf);
            return;
        }
        //工程所属的错误码发布
        if (errorCodeConf.getProjectId() != null) {
            List<ErrorCodeConfDO> pubList = findErrorCodeConfig(errorCodeConf.getProjectId());
            releaseConfig(pubList, errorCodeConf);
            return;
        }
        // 发布所有工程
        List<Integer> projectList = errorCodeConfigDAO.queryAllErrorCodeProject();
        for (Integer projectId : projectList) {
            List<ErrorCodeConfDO> pubList = findErrorCodeConfig(projectId);
            errorCodeConf.setProjectId(projectId);
            try {
                releaseConfig(pubList, errorCodeConf);
            } catch(CustomException e) {
                log.info("project {} 未发布：{}", projectId, e.getMessage());
            }
        }
    }

    /**
     * 根据工程和错误级别生成一个错误码
     *
     * @param errorCodeConf 条件
     * @return r
     */
    @Override
    public ErrorCodeConfDTO generateErrorCode(ErrorCodeConfQO errorCodeConf) {
        Integer projectId = errorCodeConf.getProjectId();
        Integer level = errorCodeConf.getLevel();
        String maxErrorCode = errorCodeConfigDAO.getMaxErrorCodeOfProject(projectId, level);
        if (maxErrorCode == null) {
            maxErrorCode = String.format("%d%04d", level, projectId) + "0001";
        } else {
            String seq = StringUtils.substring(maxErrorCode, -4);
            int seqNo = Integer.parseInt(seq) + 1;
            maxErrorCode = String.format("%d%04d%04d", level, projectId, seqNo);
        }
        ErrorCodeConfDTO errorCodeConfDTO = new ErrorCodeConfDTO();
        errorCodeConfDTO.setErrorCode(maxErrorCode);
        return errorCodeConfDTO;
    }

    private void releaseConfig(List<ErrorCodeConfDO> pubList, ErrorCodeConfQO errorCodeConf) {
        if (CollectionUtils.isEmpty(pubList)) {
            throw new CustomException(ErrorCode.NO_UPDATE, "没有修改内容发布");
        }
        // 新增一个版本
        ConfigVersionDO newVersion = new ConfigVersionDO();
        newVersion.setVersion(DateFormatUtils.format(new Date(), "yyyyMMddHHmmssSSS"));
        newVersion.setProjectId(errorCodeConf.getProjectId());
        newVersion.setType(VersionTypeEnum.ERROR_CODE.getCode());
        configVersionDAO.insert(newVersion);

        // 复制修改错误码到已发布版本
        List<OpenItemDTO> configList = new ArrayList<>();
        for (ErrorCodeConfDO confDO : pubList) {
            confDO.setVersion(DmtConstant.VER_RELEASE);
            confDO.setUpdateTime(new Date());
            confDO.setUpdateUser(errorCodeConf.getUserId());
            errorCodeConfigDAO.insert(confDO);
            OpenItemDTO item = new OpenItemDTO();
            item.setKey(confDO.getErrorCode());
            item.setValue(confDO.getDisplayMessage());
            configList.add(item);
        }
        apolloConfigManager.syncConfigForSit(DmtConstant.NAMESPACE_ERROR_CODE, configList);
    }

    private List<ErrorCodeConfDO> findErrorCodeConfig(Integer projectId) {
        ConfigVersionDO version = getLastVersion(projectId);

        // 当前为第一个版本，查询全量草稿
        if (version == null) {
            ErrorCodeConfDO condition = new ErrorCodeConfDO();
            condition.setVersion(DmtConstant.VER_DRAFT);
            condition.setProjectId(projectId
            );
            return errorCodeConfigDAO.query(condition);
        }
        List<ErrorCodeConfDO> changeList = errorCodeConfigDAO.queryChangedConfig(projectId, version.getCreateTime());
        if (CollectionUtils.isEmpty(changeList)) {
            return null;
        }
        List<String> codeList = changeList.stream().map(ErrorCodeConfDO::getErrorCode).collect(Collectors.toList());
        errorCodeConfigDAO.deleteReleaseErrorCode(codeList, version.getVersion());

        return changeList;
    }

    private ConfigVersionDO getLastVersion(Integer projectId) {
        LambdaQueryWrapper<ConfigVersionDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConfigVersionDO::getProjectId, projectId)
                .eq(ConfigVersionDO::getType, 1);
        wrapper.orderByDesc(ConfigVersionDO::getVersion);
        wrapper.last("limit 1");
        return configVersionDAO.selectOne(wrapper);
    }

}
