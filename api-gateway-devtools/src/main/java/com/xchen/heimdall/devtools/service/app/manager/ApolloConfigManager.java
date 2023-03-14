package com.xchen.heimdall.devtools.service.app.manager;

import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.dto.NamespaceReleaseDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import com.xchen.heimdall.devtools.service.app.common.constant.DmtConstant;
import com.xchen.heimdall.devtools.service.app.common.constant.ErrorCode;
import com.xchen.heimdall.common.exception.errorcode.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ApolloConfigManager {

    @Autowired
    private ApolloOpenApiClient apolloClient;

    @Value("${apollo.appId}")
    private String appId;

    @Value("${apollo.env:DEV}")
    private String env;

    @Value("${apollo.cluster:at}")
    private String clusterName;


    /**
     * 同步apollo
     *
     * @param nameSpace
     * @param configList
     */
    public void syncConfig(String nameSpace, List<OpenItemDTO> configList) {
        log.info("同步apollo：nameSpace={}，configList={}", nameSpace,
                configList.stream().map(OpenItemDTO::getKey).collect(Collectors.toList()));
        try {
            for (OpenItemDTO item : configList) {
                item.setDataChangeCreatedBy(DmtConstant.USER_SYSTEM);
                item.setDataChangeLastModifiedBy(DmtConstant.USER_SYSTEM);
                apolloClient.createOrUpdateItem(appId, env, clusterName, nameSpace, item);
            }
            NamespaceReleaseDTO releaseDTO = new NamespaceReleaseDTO();
            releaseDTO.setReleasedBy(DmtConstant.USER_SYSTEM);
            releaseDTO.setReleaseTitle("DMT config publish");
            apolloClient.publishNamespace(appId, env, clusterName, nameSpace, releaseDTO);
        } catch (Exception e) {
            log.error("同步apollo异常", e);
            throw new CustomException(ErrorCode.APOLLO_PUSH_ERROR, "同步apollo失败:" + e.getMessage());
        }
    }

    public void syncConfigForSit(String nameSpace, List<OpenItemDTO> configList) {
        log.info("同步SIT环境apollo：nameSpace={}，configList={}", nameSpace,
                configList.stream().map(OpenItemDTO::getKey).collect(Collectors.toList()));
        try {
            for (OpenItemDTO item : configList) {
                item.setDataChangeCreatedBy(DmtConstant.USER_SYSTEM);
                item.setDataChangeLastModifiedBy(DmtConstant.USER_SYSTEM);
                apolloClient.createOrUpdateItem(appId, "SIT", null, nameSpace, item);
            }
            NamespaceReleaseDTO releaseDTO = new NamespaceReleaseDTO();
            releaseDTO.setReleasedBy(DmtConstant.USER_SYSTEM);
            releaseDTO.setReleaseTitle("DMT config publish");
            apolloClient.publishNamespace(appId, "SIT", null, nameSpace, releaseDTO);
        } catch (Exception e) {
            log.error("同步SIT环境apollo异常", e);
            throw new CustomException(ErrorCode.APOLLO_PUSH_ERROR, "同步apollo失败:" + e.getMessage());
        }
    }
}
