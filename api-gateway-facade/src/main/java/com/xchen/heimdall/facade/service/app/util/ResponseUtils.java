package com.xchen.heimdall.facade.service.app.util;

import com.xchen.heimdall.common.exception.errorcode.*;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.service.GenericException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.xchen.heimdall.facade.service.app.common.exception.ErrorCodeException;
import com.xchen.heimdall.common.util.JacksonUtil;
import com.xchen.heimdall.common.util.UuidUtil;
import com.xchen.heimdall.proto.RpcModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.xchen.heimdall.common.constant.ReservedErrorCode.*;
import static org.apache.dubbo.rpc.RpcException.*;
import static com.xchen.heimdall.common.constant.TraceConfig.SPAN;
import static com.xchen.heimdall.common.constant.TraceConfig.TRACE_ID;

/**
 * @author xchen
 * @date 2022/7/27
 */
@Slf4j
public class ResponseUtils {

    private static final String ERROR_MSG_TEMPLATE = "DUBBO调用异常: %s";
    private static final Pattern ERROR_CODE_PATTERN = Pattern.compile("^\\[(\\d+)]");
    private static final Pattern ERROR_DESC_MAP_PATTERN = Pattern.compile("Exception:.*?(\\{[\\S\\s]*)");
    private static final String ERROR_CODE_PACKAGE = "com.xchen.heimdall.common.exception.errorcode.";
    private static final Integer ERROR_MSG_LENGTH = 1024;
    private static final String ERROR_MSG_OVERLAY = "...";

    private ResponseUtils() {
    }

    public static RpcModel.RpcResponse.Builder getResponseBuilder(RpcModel.RpcRequest request) {
        RpcModel.RpcResponse.Builder responseBuilder = RpcModel.RpcResponse.newBuilder();
        // 塞入trace信息
        responseBuilder.setTraceInfo(extractTrace(request))
                .setRequestType(request.getRequestType());
        return responseBuilder;
    }

    public static void buildSuccessResponse(RpcModel.RpcResponse.Builder responseBuilder, String resultData) {
        responseBuilder.setResponseCode(200)
                .setData(resultData);
    }

    public static void buildErrorResponse(RpcModel.RpcResponse.Builder responseBuilder, Exception e) {
        log.error("Failed to invoke, due to {}", e.getMessage(), e);
        // 默认返回1500错误码
        responseBuilder.setResponseCode(INTERNAL_SERVER_ERROR_CODE)
                .setErrorMsg(e.getMessage());

        // 如果是自定义的报错类型，返回自定义的错误码
        if (e instanceof GenericException) {
            GenericException ge = (GenericException) e;
            if (StringUtils.startsWith(ge.getExceptionClass(), ERROR_CODE_PACKAGE)) {
                // 这是自定义错误码的报错
                Matcher matcher = ERROR_CODE_PATTERN.matcher(ge.getExceptionMessage());
                if (matcher.find()) {
                    String errorCode = matcher.group(1);
                    String errorMsg = ge.getExceptionMessage();
                    responseBuilder.setResponseCode(Integer.parseInt(errorCode))
                            // 发往网关的错误信息超长时截断
                            .setErrorMsg(overlayErrorMsg(errorMsg));
                }
                // 错误描述map
                Matcher errorDescMapMatcher = ERROR_DESC_MAP_PATTERN.matcher(ge.getExceptionMessage());
                if (errorDescMapMatcher.find()) {
                    String data = errorDescMapMatcher.group(1);
                    responseBuilder.setData(data);
                }
            }
        } else if (e instanceof RpcException) {
            // dubbo自身异常类，统一用1402错误码
            RpcException re = (RpcException) e;
            responseBuilder.setResponseCode(1402)
                    .setData(getErrorDescMap(re))
                    .setErrorMsg(overlayErrorMsg(getErrorDetailMsg(re)));
        } else if (e instanceof ErrorCodeException) {
            // grpc泛化调用的异常已经转换成错误码异常
            ErrorCodeException customExp = (ErrorCodeException) e;
            responseBuilder.setResponseCode(customExp.getErrorCode())
                    .setErrorMsg(overlayErrorMsg(customExp.getMessage()));
            if (customExp.getErrorDescMap() != null) {
                responseBuilder.setData(customExp.getErrorDescMap());
            }
        }
    }

    /**
     * 针对facade dubbo接口暴露的异常进行转换
     *
     * @param e 异常类
     */
    public static void rethrowException(Exception e) {
        log.error("Failed to invoke, due to {}", e.getMessage(), e);
        // 默认返回1500错误码
        RpcModel.RpcResponse.Builder errorResponseBuilder = RpcModel.RpcResponse.newBuilder();
        buildErrorResponse(errorResponseBuilder, e);
        throwExceptionByCode(errorResponseBuilder.getResponseCode(),
                errorResponseBuilder.getErrorMsg(),
                errorResponseBuilder.getData()
        );
    }

    public static void throwExceptionByCode(Integer errorCode, String errorMsg, String errorDescMapJson) {
        switch (errorCode) {
            case BAD_REQUEST_ERROR_CODE:
                throw new BadRequestException(errorMsg);
            case UNAUTHORIZED_ERROR_CODE:
                throw new UnauthorizedException(errorMsg);
            case FORBIDDEN_ERROR_CODE:
                throw new ForbiddenException(errorMsg);
            case NOT_FOUND_ERROR_CODE:
                throw new NotFoundException(errorMsg);
            case INTERNAL_SERVER_ERROR_CODE:
                throw new InternalServerException(errorMsg);
            case THIRD_PARTY_ERROR_CODE:
                throw new ThirdPartyException(errorMsg);
            case FRAMEWORK_ERROR_CODE:
                throw new FrameworkException(errorMsg);
            default:
                // 对自定义的错误需要进行特殊处理，将错误描述提取出来
                CustomException.Builder builder = CustomException.builder().errorCode(errorCode).message(errorMsg);

                if (StringUtils.isNotBlank(errorDescMapJson)) {
                    Map<String, String> errorDescMap = JacksonUtil.decode(errorDescMapJson, new TypeReference<Map<String, String>>() {
                    });
                    errorDescMap.forEach(builder::errorDesc);
                }
                throw builder.build();
        }
    }

    private static String overlayErrorMsg(String errorMsg) {
        return StringUtils.overlay(errorMsg, ERROR_MSG_OVERLAY, ERROR_MSG_LENGTH, errorMsg.length());
    }

    private static String getErrorDetailMsg(RpcException e) {
        if (Objects.nonNull(e.getCause())) {
            return e.getCause().getLocalizedMessage();
        }
        return e.getLocalizedMessage();
    }

    private static String getErrorDescMap(RpcException e) {
        HashMap<String, String> errorDescMap = new HashMap<>();
        errorDescMap.put("msg",
                String.format(ERROR_MSG_TEMPLATE, getErrorMsg(e))
        );
        return JacksonUtil.encode(errorDescMap);
    }

    private static String getErrorMsg(RpcException e) {
        switch (e.getCode()) {
            case NETWORK_EXCEPTION:
                return "网络异常";
            case TIMEOUT_EXCEPTION:
                return "调用超时";
            case BIZ_EXCEPTION:
                return "业务异常";
            case FORBIDDEN_EXCEPTION:
                return "禁止访问";
            case SERIALIZATION_EXCEPTION:
                return "序列化异常";
            case UNKNOWN_EXCEPTION:
            default:
                return "未知错误";
        }
    }

    /**
     * 提取trace信息
     *
     * @param request 请求
     * @return traceInfo
     */
    private static RpcModel.TraceInfo extractTrace(RpcModel.RpcRequest request) {
        String traceId = null;
        int span = 0;
        if (request.hasTraceInfo()) {
            traceId = request.getTraceInfo().getTraceId();
            span = request.getTraceInfo().getSpan();
        }
        if (StringUtils.isEmpty(traceId)) {
            traceId = UuidUtil.generate();
        }
        span = span + 1;

        // trace信息加入MDC
        MDC.put(TRACE_ID, traceId);
        MDC.put(SPAN, String.valueOf(span));

        return RpcModel.TraceInfo.newBuilder()
                .setTraceId(traceId)
                .setSpan(span)
                .build();
    }
}
