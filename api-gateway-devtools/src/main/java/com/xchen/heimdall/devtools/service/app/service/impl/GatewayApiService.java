package com.xchen.heimdall.devtools.service.app.service.impl;

import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import com.xchen.heimdall.common.util.JacksonUtil;
import com.xchen.heimdall.devtools.service.app.manager.ApolloConfigManager;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xchen.heimdall.devtools.service.app.dao.DubboMethodDAO;
import com.xchen.heimdall.devtools.service.app.dao.GatewayApiDAO;
import com.xchen.heimdall.devtools.service.app.dao.PojoDAO;
import com.xchen.heimdall.devtools.service.app.domain.DubboMethodDO;
import com.xchen.heimdall.devtools.service.app.domain.GatewayApiDO;
import com.xchen.heimdall.devtools.service.app.domain.PojoDO;
import com.xchen.heimdall.devtools.service.app.dto.GatewayApiDTO;
import com.xchen.heimdall.devtools.service.app.dto.GatewayApiDocDTO;
import com.xchen.heimdall.devtools.service.app.dto.PojoDTO;
import com.xchen.heimdall.devtools.service.app.service.IGatewayApiService;
import com.xchen.heimdall.devtools.service.app.utils.CommonUtils;
import com.xchen.heimdall.devtools.service.app.vo.*;
import com.xchen.heimdall.common.api.ApolloDubboApiDTO;
import com.xchen.heimdall.common.util.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.xchen.heimdall.common.constant.ApolloNamespace.API;

/**
 * @author 
 * @date 2022/4/15
 */
@Slf4j
@Service
public class GatewayApiService implements IGatewayApiService {
    @Resource
    private GatewayApiDAO gatewayApiDAO;

    @Resource
    private GatewayApiPermissionService gatewayApiPermissionService;

    @Resource
    private CommonUtils commonUtils;

    @Resource
    private DubboMethodDAO dubboMethodDAO;

    @Resource
    private PojoDAO pojoDAO;

    @Resource
    private GenerateService generateService;
    @Resource
    private ApolloConfigManager apolloConfigManager;

    @Value("${apollo.portalUsername}")
    private String portalUsername;
    @Value("${apollo.portalPassword}")
    private String portalPassword;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    @Override
    public List<GatewayApiDTO> listAllDubboGatewayApi(ListGatewayApiVO vo) {
        List<GatewayApiDTO> gatewayApis = gatewayApiDAO.listDubboGatewayApi(vo);
        // 填充permissionIds和requestPath
        decorateGatewayApis(gatewayApis);
        return gatewayApis;
    }

    @Override
    public List<GatewayApiDTO> listUnsyncedDubboGatewayApi() {
        List<GatewayApiDTO> gatewayApis = gatewayApiDAO.listUnsyncedDubboGatewayApi();
        // 填充permissionIds和requestPath
        decorateGatewayApis(gatewayApis);
        return gatewayApis;
    }

    @Override
    public void publishGatewayApi() {
        List<GatewayApiDTO> gatewayApis = gatewayApiDAO.listAllDubboGatewayApi();
        // 填充permissionIds和requestPath
        List<OpenItemDTO> configList = new ArrayList<>();

        for (GatewayApiDTO gatewayApi : gatewayApis) {
            commonUtils.setRpcRequestPathWithoutContextPath(gatewayApi);
            gatewayApi.setPermissionIdList(gatewayApiPermissionService.selectPermissionIds(gatewayApi.getId()));
            ApolloDubboApiDTO apolloDubboApi = BeanUtil.convert(gatewayApi, ApolloDubboApiDTO::new);
            apolloDubboApi.setRegistryZkCluster(gatewayApi.getRegistryZkCluster().getValue());
            OpenItemDTO item = new OpenItemDTO();
            item.setKey(gatewayApi.getRequestPath());
            item.setValue(JacksonUtil.encode(gatewayApi));
            configList.add(item);
        }

        apolloConfigManager.syncConfig(API, configList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GatewayApiDTO addGatewayApi(GatewayApiVO vo) {
        GatewayApiDO gatewayApiDO = BeanUtil.convert(vo, GatewayApiDO::new);
        gatewayApiDO.setOwnerUserId(vo.getUserId());
        gatewayApiDO.setCreateUserId(vo.getUserId());
        gatewayApiDAO.insert(gatewayApiDO);

        // 更新网关接口生成状态
        updateDubboMethodStatus(vo.getMethodId(), true);

        // 生成gateway permission关联关系
        gatewayApiPermissionService.updatePermissionsByGatewayApiId(new PermissionIdsVO(gatewayApiDO.getId(), vo.getPermissionIdList()));

        return BeanUtil.convert(gatewayApiDO, GatewayApiDTO::new);
    }

    @Override
    public void updateGatewayApi(GatewayApiVO vo) {
        GatewayApiDO gatewayApiDO = BeanUtil.convert(vo, GatewayApiDO::new);
        gatewayApiDO.setUpdateUserId(vo.getUserId());
        // 更新时将发布状态置为false
        gatewayApiDO.setSynced(false);
        gatewayApiDAO.updateById(gatewayApiDO);

        // 生成gateway permission关联关系
        gatewayApiPermissionService.updatePermissionsByGatewayApiId(new PermissionIdsVO(gatewayApiDO.getId(), vo.getPermissionIdList()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGatewayApi(GatewayApiIdVO vo) {
        GatewayApiDO deletedGatewayApi = gatewayApiDAO.selectById(vo.getGatewayApiId());
        gatewayApiDAO.delete(new LambdaQueryWrapper<GatewayApiDO>()
                .eq(GatewayApiDO::getId, vo.getGatewayApiId())
        );

        // 更新网关接口生成状态
        updateDubboMethodStatus(deletedGatewayApi.getMethodId(), false);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateDubboMethodStatus(Integer methodId, Boolean gatewayApiStatus) {
        dubboMethodDAO.update(null, new LambdaUpdateWrapper<DubboMethodDO>()
                .set(DubboMethodDO::getGatewayApiStatus, gatewayApiStatus)
                .eq(DubboMethodDO::getId, methodId)
        );
    }

    private void decorateGatewayApis(List<GatewayApiDTO> gatewayApis) {
        if (CollectionUtils.isNotEmpty(gatewayApis)) {
            gatewayApis.forEach(gatewayApi -> {
                commonUtils.setRpcRequestPath(gatewayApi);
                gatewayApi.setPermissionIdList(gatewayApiPermissionService.selectPermissionIds(gatewayApi.getId()));
            });
        }
    }

    @Override
    public GatewayApiDocDTO findGatewayApiDetails(BaseVO vo) {
        GatewayApiDTO gatewayApi = gatewayApiDAO.findDetails(vo.getId());
        GatewayApiDocDTO apiDoc = BeanUtil.convert(gatewayApi, GatewayApiDocDTO::new);
        DubboMethodDO dubboMethod = dubboMethodDAO.selectById(gatewayApi.getMethodId());
        PojoDO voPojo = pojoDAO.selectById(dubboMethod.getVoId());
        PojoDO dtoPojo = pojoDAO.selectById(dubboMethod.getDtoId());
        apiDoc.setInputParam(BeanUtil.convert(voPojo, PojoDTO::new));
        apiDoc.setReturnParam(BeanUtil.convert(dtoPojo, PojoDTO::new));
        return apiDoc;
    }
}