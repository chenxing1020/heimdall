package com.xchen.heimdall.api.gateway.app.aspect;

import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.xchen.heimdall.api.gateway.app.manager.ErrorCodeManager;
import com.xchen.heimdall.api.gateway.app.model.ResultModel;
import com.xchen.heimdall.common.exception.errorcode.AbstractErrorCodeException;
import com.xchen.heimdall.common.exception.errorcode.CustomException;
import com.xchen.heimdall.common.exception.errorcode.FrameworkException;
import com.xchen.heimdall.common.exception.errorcode.NotFoundException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import javax.validation.UnexpectedTypeException;
import java.util.Map;

/**
 * 全局异常处理
 *
 * @author xchen
 */
@Component
@Order(-2)
@RefreshScope
@Slf4j
public class GlobalErrorWebExceptionHandler extends AbstractErrorWebExceptionHandler {

    @Value("${error.detail.enable}")
    private Boolean enableErrorDetail;

    @Resource
    private ConfigurableApplicationContext applicationContext;

    public GlobalErrorWebExceptionHandler(ErrorAttributes errorAttributes,
                                          WebProperties webProperties,
                                          ServerCodecConfigurer serverCodecConfigurer,
                                          ApplicationContext applicationContext) {
        super(errorAttributes, webProperties.getResources(), applicationContext);
        this.setMessageWriters(serverCodecConfigurer.getWriters());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(
                RequestPredicates.all(), this::renderErrorResponse);
    }

    @NonNull
    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Throwable t = super.getError(request);
        ResultModel<Object> resultModel;

        if (t instanceof AbstractErrorCodeException) {
            resultModel = handleErrorCodeException(t);
        } else if (t instanceof ResponseStatusException) {
            resultModel = handleResponseStatusException(t);
        } else if (t instanceof UnexpectedTypeException) {
            log.error("UnexpectedType Exception", t);
            resultModel = ResultModel.fail(1502, "参数校验异常");
        } else if (t instanceof FlowException) {
            FlowRule flowRule = ((FlowException) t).getRule();
            log.error("Trigger flow control, url: {}, flow rule: {}", request.uri(), flowRule.toString());
            resultModel = ResultModel.fail(1506, "触发流控，流控值" + flowRule.getCount());
        } else {
            // 给一个随机编号方便排查日志
            log.error("Undefined Exception", t);
            resultModel = ResultModel.fail(1502, "服务异常");
        }

        return ServerResponse.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(resultModel));
    }

    private ResultModel<Object> handleErrorCodeException(Throwable t) {
        ResultModel<Object> resultModel;
        AbstractErrorCodeException errorCodeException = (AbstractErrorCodeException) t;

        // 组装错误文本
        String errorMsg = applicationContext.getBean(ErrorCodeManager.class)
                .covertCodeToDescription(errorCodeException.getErrorCode());
        if (t instanceof CustomException) {
            CustomException customException = (CustomException) errorCodeException;
            errorMsg = combineMsg(errorMsg, customException);
            // 打印不同级别的日志
            switch (customException.getLevel()) {
                case CustomException.LEVEL_ACCEPTABLE:
                    log.debug("Acceptable Exception: {}", errorMsg);
                    break;
                case CustomException.LEVEL_BUSINESS:
                    log.warn("Business Exception: {}", errorMsg);
                    break;
                case CustomException.LEVEL_UNEXPECTED:
                    // 信息都在异常message中
                    log.error("", t);
                    break;
                default:
                    log.error("unknown level: " + customException.getLevel(), t);
            }
        } else if (t instanceof FrameworkException) {
            log.error("框架处理异常", t);
        } else if (t instanceof NotFoundException) {
            // 生产有api安全扫描，降低该日志级别
            log.info("错误码异常, msg: {}", errorCodeException.getMessage());
        } else {
            log.error("错误码异常, msg:{}", errorCodeException.getMessage());
        }
        resultModel = ResultModel.fail(errorCodeException.getErrorCode(), errorMsg);
        if (Boolean.TRUE.equals(enableErrorDetail)) {
            resultModel.setData(errorCodeException.getMessage());
        }
        return resultModel;
    }

    private ResultModel<Object> handleResponseStatusException(Throwable t) {
        ResultModel<Object> resultModel;
        HttpStatus status = ((ResponseStatusException) t).getStatus();
        if (HttpStatus.NOT_FOUND.equals(status)) {
            // 可识别的http server错误不需要日志
            resultModel = ResultModel.fail(status.value(), status.getReasonPhrase());
        } else if (HttpStatus.BAD_REQUEST.equals(status)) {
            // 参数校验失败
            if (t instanceof WebExchangeBindException) {
                FieldError fieldError = ((WebExchangeBindException) t).getFieldError();
                resultModel = ResultModel.fail(status.value(),
                        fieldError == null ? t.toString() : fieldError.getDefaultMessage());
            } else {
                resultModel = ResultModel.fail(status.value(), t.toString());
            }
        } else {
            log.warn("HTTP ERROR [{}] {}", status.value(), t.toString());
            resultModel = ResultModel.fail(status.value(), status.getReasonPhrase());
        }
        return resultModel;
    }

    private String combineMsg(String errorMsg, CustomException ce) {
        Map<String, String> errorDescMap = ce.getErrorDescMap();
        if (errorDescMap == null || errorDescMap.isEmpty()) {
            return errorMsg;
        }
        // 动态替换提示内容
        StringSubstitutor sub = new StringSubstitutor(errorDescMap, "#{", "}", '#');
        return sub.replace(errorMsg);
    }

}