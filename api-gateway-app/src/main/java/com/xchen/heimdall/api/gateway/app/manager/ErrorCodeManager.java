package com.xchen.heimdall.api.gateway.app.manager;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfig;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import com.fasterxml.jackson.core.type.TypeReference;
import com.xchen.heimdall.common.exception.errorcode.*;
import com.xchen.heimdall.common.util.JacksonUtil;
import com.xchen.heimdall.proto.RpcModel.RpcResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.xchen.heimdall.common.constant.ReservedErrorCode.*;
import static com.xchen.heimdall.common.constant.ApolloNamespace.ERROR_CODE;

/**
 * @author xchen
 * @date 2022/2/19
 */
@Service
@Slf4j
public class ErrorCodeManager {

    @ApolloConfig(ERROR_CODE)
    private Config errorCodeConfig;

    @Value("${error.description.separator:  }")
    private String separator;

    private final Map<String, String> errorCodeMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void postConstruct() {
        errorCodeConfig.getPropertyNames().forEach(errorCode ->
                errorCodeMap.put(errorCode,
                        errorCodeConfig.getProperty(errorCode, "")
                )
        );
    }

    @ApolloConfigChangeListener(ERROR_CODE)
    private void onChangeErrorCodeDesc(ConfigChangeEvent changeEvent) {
        changeEvent.changedKeys().forEach(errorCode -> {
            ConfigChange change = changeEvent.getChange(errorCode);
            String desc = change.getNewValue();
            log.info(String.format("Found change - propertyName: %s, oldValue: %s, newValue: %s, changeType: %s",
                    change.getPropertyName(), change.getOldValue(), desc, change.getChangeType()));

            switch (change.getChangeType()) {
                case ADDED:
                case MODIFIED:
                    errorCodeMap.put(errorCode,
                            errorCodeConfig.getProperty(errorCode, "")
                    );
                    break;
                case DELETED:
                    errorCodeMap.remove(errorCode);
                    break;
                default:
                    break;
            }
        });
    }

    public String covertCodeToDescription(Integer errorCode) {
        return errorCodeMap.get(String.valueOf(errorCode));
    }

    /**
     * 查找错误码对应的描述
     * 如果没有对应的配置，则还返回错误码
     * @param errorCode 错误码
     * @return 错误描述
     */
    public String covertCodeToDescription(String errorCode) {
        if (StringUtils.isBlank(errorCode)) {
            return null;
        }
        List<String> descriptions = new ArrayList<>();
        String[] errorCodeList = StringUtils.split(errorCode, ',');
        for (String code : errorCodeList) {
            if (errorCodeMap.get(code) != null) {
                descriptions.add(errorCodeMap.get(code));
            }
        }
        if (CollectionUtils.isEmpty(descriptions)) {
            return null;
        }
        return StringUtils.join(descriptions, separator);
    }

    public static void convertCodeToException(RpcResponse response) {
        int errorCode = response.getResponseCode();
        String errorMsg = response.getErrorMsg();

        switch (errorCode) {
            case BAD_REQUEST_ERROR_CODE:
                throw new BadRequestException(errorMsg);
            case UNAUTHORIZED_ERROR_CODE:
                throw new UnauthorizedException(errorMsg);
            case FORBIDDEN_ERROR_CODE:
                throw new ForbiddenException(errorMsg);
            case INTERNAL_SERVER_ERROR_CODE:
                throw new InternalServerException(errorMsg);
            case THIRD_PARTY_ERROR_CODE:
                throw new ThirdPartyException(errorMsg);
            case FRAMEWORK_ERROR_CODE:
                throw new FrameworkException(errorMsg);
            default:
                // 对自定义的错误需要进行特殊处理，将错误描述提取出来
                CustomException.Builder builder = CustomException.builder().errorCode(errorCode).message(errorMsg);

                String errorDescMapJson = response.getData();
                if (StringUtils.isNotBlank(errorDescMapJson)) {
                    Map<String, String> errorDescMap = JacksonUtil.decode(errorDescMapJson, new TypeReference<Map<String, String>>(){});
                    errorDescMap.forEach(builder::errorDesc);
                }

                throw builder.build();
        }
    }
}
