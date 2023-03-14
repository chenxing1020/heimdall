package com.xchen.heimdall.common.exception.errorcode;

import com.xchen.heimdall.common.util.JacksonUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 自定义的错误，用于抛出特定的业务逻辑错误。
 * 错误码必须指定，需要预先申请，从10000开始。
 *
 * @author xchen
 */
@Slf4j
public class CustomException extends AbstractErrorCodeException {

    /**
     * 常规性业务逻辑错误，打印warn日志
     */
    public static final int LEVEL_BUSINESS = 0;

    /**
     * 可接受的异常，利用异常机制返回正常结果，打印debug日志
     */
    public static final int LEVEL_ACCEPTABLE = 1;

    /**
     * 出现非预期的情况，打印stack日志
     */
    public static final int LEVEL_UNEXPECTED = 2;


    /**
     * 前端返回的错误文本中占位符的替换内容
     */
    private final Map<String, String> errorDescMap;

    /**
     * 严重等级
     */
    private final int level;

    public CustomException(Integer errorCode, String message) {
        super(errorCode, message);
        this.level = LEVEL_BUSINESS;
        this.errorDescMap = null;
    }

    private CustomException(Integer errorCode, int level, String message,
                            Map<String, String> errorDescMap, Throwable t) {
        super(errorCode, message, t);
        this.level = level;
        this.errorDescMap = errorDescMap;
    }

    public int getLevel() {
        return level;
    }

    public Map<String, String> getErrorDescMap() {
        return errorDescMap;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        Integer errorCode;
        int level;
        String message;
        Map<String, String> errorDescMap;
        Throwable t;

        public Builder errorCode(Integer errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public Builder level(int level) {
            this.level = level;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder errorDesc(String key, String value) {
            if (Objects.isNull(errorDescMap)) {
                errorDescMap = new HashMap<>();
            }
            errorDescMap.put(key, value);
            return this;
        }

        public Builder exception(Throwable t) {
            this.t = t;
            return this;
        }

        public CustomException build() {
            StringBuilder sb = new StringBuilder();
            switch (level) {
                case CustomException.LEVEL_ACCEPTABLE:
                    sb.append("Acceptable Exception: ");
                    break;
                case CustomException.LEVEL_BUSINESS:
                    sb.append("Business Exception: ");
                    break;
                case CustomException.LEVEL_UNEXPECTED:
                    sb.append("Unexpected Exception: ");
                    break;
                default:
                    log.warn("wrong level: " + level);
                    level = LEVEL_UNEXPECTED;
                    sb.append("Unexpected Exception: ");
            }
            if (message != null) {
                sb.append(message).append(" ");
            }
            if (errorDescMap != null) {
                sb.append(JacksonUtil.encode(errorDescMap));
            }
            return new CustomException(errorCode, level, sb.toString(), errorDescMap, t);
        }

    }
}