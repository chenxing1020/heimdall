package com.xchen.heimdall.facade.service.app.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.xchen.heimdall.common.api.ApolloRestApiDTO;
import com.xchen.heimdall.facade.service.app.common.exception.ErrorCodeException;
import com.xchen.heimdall.facade.service.app.util.RestUtils;
import com.xchen.heimdall.common.exception.errorcode.InternalServerException;
import com.xchen.heimdall.common.util.JacksonUtil;
import com.xchen.heimdall.proto.RpcModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.xchen.heimdall.facade.service.app.util.RestUtils.*;

/**
 * @author xchen
 * @date 2022/3/16
 */
@Service
@Slf4j
public class RestGenericService {

    private static final String ERRORCODE_RETURN_KEY = "ERROR_CODE.CustomException";

    @Resource
    private GatewayApiService gatewayApiService;

    @Resource
    private RestTemplate restTemplate;

    public String invoke(RpcModel.RpcRequest request) {
        ApolloRestApiDTO restApi = gatewayApiService.getRestApiData(request.getService(), request.getMethod(), request.getRequestType());
        if (Objects.isNull(restApi)) {
            throw new InternalServerException("Failed to get rest api");
        }

        switch (request.getRequestType()) {
            case REST:
                return invoke(restApi, request.getPayload());
            case REST_RAW:
                // rest raw类型的子路径通过extFields获取
                return invoke(restApi, extractRawUrl(request.getExtFields()), request.getPayload());
            default:
                throw new InternalServerException("Unsupported requestType: " + restApi);
        }
    }

    public String invoke(ApolloRestApiDTO restApi, String data) {
        return invoke(restApi, restApi.getRestUrl(), data);
    }

    public String invoke(ApolloRestApiDTO restApi, String subUrl, String data) {
        return parseErrorMsg(invokeWithRetries(restApi, subUrl, data));
    }

    public String invokeWithRetries(ApolloRestApiDTO restApi, String subUrl, String data) {
        List<String> serverAddresses = gatewayApiService.getServerAddresses(restApi.getServiceName());
        if (CollectionUtils.isEmpty(serverAddresses)) {
            throw new InternalServerException("Failed to invoke rest, due to no server address");
        }

        // TODO：当前逻辑顺序retry，后续考虑接入spring cloud loadbalancer
        for (String serverAddress : serverAddresses) {
            String actualUrl = normalizeUrl(serverAddress + subUrl);
            try {
                switch (restApi.getOperationType()) {
                    case GET:
                        return restTemplate.getForObject(RestUtils.appendQueryString(actualUrl, data), String.class);
                    case POST:
                        return restTemplate.postForObject(actualUrl, generateHttpRequest(data), String.class);
                    default:
                        throw new InternalServerException("Unsupported rest operation type");
                }
            } catch (Exception e) {
                log.info("Failed to invoke rest: {}, due to {}", actualUrl, e.getMessage());
            }
        }
        // 所有的server都未成功响应
        throw new InternalServerException("Failed to invoke rest after all tries");
    }

    /**
     * 返回的消息中如果有ErrorCodeExceptionHandler设置的CustomException关键字，则解析其中的错误码和错误描述。
     * 解析后作为ErrorCodeException抛出。
     * @param response rest接口response
     * @return 未解析出错误码原样返回
     */
    private String parseErrorMsg(String response) {
        if (response != null && response.contains(ERRORCODE_RETURN_KEY)) {
            ErrorCodeException exception = null;
            try {
                Map<String, String> errorDescMap = JacksonUtil.decode(response,
                                                    new TypeReference<Map<String, String>>() {});
                exception = new ErrorCodeException(Integer.valueOf(errorDescMap.get("errorCode")),
                        errorDescMap.get("message"), errorDescMap.get("errorDescMap"));
            } catch (Exception e) {
                log.warn("parse REST error code response Exception. response={}", response, e);
            }
            if (exception != null) {
                throw exception;
            }
        }
        return response;
    }
}
