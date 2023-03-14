package com.xchen.heimdall.facade.service.app.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.xchen.heimdall.common.api.ApolloGrpcApiDTO;
import com.xchen.heimdall.facade.service.app.common.constant.ErrorCode;
import com.xchen.heimdall.facade.service.app.common.exception.ErrorCodeException;
import com.xchen.heimdall.common.util.JacksonUtil;
import com.xchen.heimdall.grpc.exception.GeneralGrpcException;
import com.xchen.heimdall.grpc.model.CallResults;
import com.xchen.heimdall.grpc.model.GrpcGenericVO;
import com.xchen.heimdall.grpc.service.GrpcProxyService;
import com.xchen.heimdall.proto.RpcModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class GrpcGenericService {


    private final static String HEAD_CLIENT_IP = "client_ip";

    private final static String HEAD_USER_ID = "user_id";

    private final static String HEAD_TRACE_ID = "trace_id";

    private static final String USER_ID_KEY = "userId";

    private static final String CLIENT_IP_KEY = "clientIp";

    @Resource
    private GrpcProxyService grpcProxyService;

    /**
     * 泛化调用GRPC服务
     *
     * @param grpcApi 接口配置
     * @param request 请求
     * @return r
     */
    public String invoke(ApolloGrpcApiDTO grpcApi, RpcModel.RpcRequest request) {
        GrpcGenericVO grpcGenericVO = buildGrpcGenericVoFromGrpcApi(grpcApi);
        grpcGenericVO.setJsonParams(request.getPayload());
        // userId放入消息头中
        Map<String, String> header = buildHeader(request);
        grpcGenericVO.setHeadFields(header);

        return doInvoke(grpcGenericVO, grpcApi);
    }

    /**
     * 适配从xgateway来的请求
     *
     * @param grpcApi 接口配置
     * @param data    请求参数
     * @return
     */
    public String invoke(ApolloGrpcApiDTO grpcApi, String data) {
        GrpcGenericVO grpcGenericVO = buildGrpcGenericVoFromGrpcApi(grpcApi);
        grpcGenericVO.setJsonParams(data);
        return doInvoke(grpcGenericVO, grpcApi);
    }

    private String doInvoke(GrpcGenericVO grpcGenericVO, ApolloGrpcApiDTO grpcApi) {
        log.info("grpc invoke request: {}", grpcGenericVO);
        try {
            CallResults callResults = grpcProxyService.invokeMethod(grpcGenericVO);
            String result;
            // 配置了返回对象类型，则取数组第一个元素返回；不配置则兼容之前的接口返回整个字符串数组。
            if (grpcApi.getDtoWrapperType() != null) {
                result = callResults.asList().get(0);
            } else {
                result = JacksonUtil.encode(callResults.asList());
            }
            log.info("grpc invoke result: {}", result);
            return result;
        } catch (GeneralGrpcException e) {
            throw new ErrorCodeException(e.getErrorCode(), e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ErrorCodeException(ErrorCode.ILLEGAL_PARAMETER, e.getMessage());
        }
    }

    private GrpcGenericVO buildGrpcGenericVoFromGrpcApi(ApolloGrpcApiDTO grpcApi) {
        GrpcGenericVO grpcGenericVO = new GrpcGenericVO();
        grpcGenericVO.setPackageName(grpcApi.getServicePath());
        grpcGenericVO.setServiceName(grpcApi.getServiceName());
        grpcGenericVO.setMethodName(grpcApi.getMethodName());
        if (grpcApi.getTimeout() != null) {
            grpcGenericVO.setTimeoutMillis(Long.valueOf(grpcApi.getTimeout()));
        }
        return grpcGenericVO;
    }

    /**
     * 该部分参数暂未使用
     *
     * @param request
     * @return
     */
    private Map<String, String> buildHeader(RpcModel.RpcRequest request) {
        Map<String, String> headFields = new HashMap<>();

        if (StringUtils.isNotBlank(request.getExtFields())) {
            Map<String, String> paramMap = JacksonUtil.decode(request.getExtFields(),
                    new TypeReference<Map<String, String>>() {
                    });
            String ip = paramMap.get(CLIENT_IP_KEY);
            if (StringUtils.isNotBlank(ip)) {
                headFields.put(HEAD_CLIENT_IP, ip);
            }
        }
        headFields.put(HEAD_TRACE_ID, request.getTraceInfo().getTraceId());
        return headFields;
    }

}
