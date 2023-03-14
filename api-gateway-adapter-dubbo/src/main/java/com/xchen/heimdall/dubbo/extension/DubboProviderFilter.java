package com.xchen.heimdall.dubbo.extension;

import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import com.google.gson.Gson;
import com.xchen.heimdall.common.constant.TraceConfig;
import com.xchen.heimdall.common.exception.errorcode.AbstractErrorCodeException;
import com.xchen.heimdall.common.exception.errorcode.BadRequestException;
import com.xchen.heimdall.common.exception.errorcode.CustomException;
import com.xchen.heimdall.common.util.UuidUtil;
import com.xchen.heimdall.common.util.ValidateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

/**
 * @author xchen
 */
@Activate(group = Constants.PROVIDER, order = -1)
@Slf4j
public class DubboProviderFilter implements Filter {

    private static final Gson GSON = new Gson();

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // 尝试设置为上游的traceId & span
        String traceId = invocation.getAttachment(TraceConfig.TRACE_ID);
        String spanString = invocation.getAttachment(TraceConfig.SPAN);
        int span = 0;
        if (StringUtils.isEmpty(traceId)) {
            traceId = UuidUtil.generate();
            log.warn("No traceId! Generate: {}", traceId);
        } else if (StringUtils.isNumeric(spanString)) {
            span = Integer.parseInt(spanString);
        }
        MDC.put(TraceConfig.TRACE_ID, traceId);
        MDC.put(TraceConfig.SPAN, String.valueOf(span + 1));

        // 对入参进行校验
        for (Object argument : invocation.getArguments()) {
            try {
                ValidateUtil.validate(argument);
            } catch (IllegalArgumentException e) {
                MDC.clear();
                return new RpcResult(new BadRequestException(e.getMessage(), e));
            }
        }

        long startTime = System.currentTimeMillis();

        Result appResponse;
        try {
            appResponse = invoker.invoke(invocation);
        } finally {
            String argument = null;
            try {
                // 用jackson序列化xstep时可能导致stackoverflow
                argument = GSON.toJson(invocation.getArguments());
            } catch (Exception e) {
                log.warn("print argument error", e);
            }
            log.info("invoked {} {} {}ms",
                    invoker.getInterface().getSimpleName() + "." + invocation.getMethodName(),
                    argument,
                    System.currentTimeMillis() - startTime);
        }

        // 处理相应的请求
        if (appResponse.hasException()) {
            Throwable t = appResponse.getException();
            // 这里做解除DubboProviderExceptionBypassFilter封装
            Throwable exception = t.getCause();
            if (exception instanceof AbstractErrorCodeException) {
                // 重新封装异常返回
                appResponse = new RpcResult(exception);

                // 打印错误日志便于分析
                String errorMsg = exception.getMessage()
                        + " [" + RpcContext.getContext().getRemoteHost()
                        + " -> " + invoker.getInterface().getSimpleName()
                        + "." + invocation.getMethodName() + "]";
                if (exception instanceof CustomException) {
                    CustomException customException = (CustomException) exception;
                    // 打印不同级别的日志
                    switch (customException.getLevel()) {
                        case CustomException.LEVEL_ACCEPTABLE:
                            log.debug(errorMsg);
                            break;
                        case CustomException.LEVEL_BUSINESS:
                            log.warn(errorMsg);
                            break;
                        case CustomException.LEVEL_UNEXPECTED:
                            log.error(errorMsg, customException);
                            break;
                        default:
                            log.error("unknown level: " + customException.getLevel(), customException);
                    }
                } else {
                    log.warn(errorMsg, exception);
                }
            } else {
                log.error(exception.getMessage(), exception);
            }
        } else {
            // 打印正常响应日志
            log.debug("app response: {}", appResponse);
        }

        MDC.clear();
        return appResponse;
    }

}
