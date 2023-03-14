package com.xchen.heimdall.devtools.service.app.model;

/**
 * @author xchen
 * @date 2022/3/29
 */

import io.swagger.annotations.ApiModelProperty;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.Objects;

import static com.xchen.heimdall.common.constant.TraceConfig.TRACE_ID;

/**
 * @author xchen
 */
@Slf4j
public class ResultModel<T> {

    private static final Integer SUCCESS = 200;

    /**
     * 返回码，也可以理解为错误码  200成功，其他失败（具体请查阅所有错误码定义）
     */
    @ApiModelProperty(value = "返回码", required = true)
    Integer code;

    @ApiModelProperty(value = "数据负载")
    T data;

    @ApiModelProperty(value = "错误描述")
    String msg;

    @ApiModelProperty(value = "请求跟踪ID")
    String traceId;

    @ApiModelProperty(value = "服务器时间", required = true)
    Long ts = System.currentTimeMillis();

    public static <V> ResultModel<V> success(V data) {
        return new ResultModel<>(SUCCESS, "", MDC.get(TRACE_ID), data);
    }

    public static <V> ResultModel<V> fail(Integer code, String msg) {
        if (Objects.equals(code, SUCCESS)) {
            log.warn("illegal failed return code");
        }
        return new ResultModel<>(code, msg, MDC.get(TRACE_ID), null);
    }

    private ResultModel(Integer code, String msg, String traceId, T data) {
        this.code = code;
        this.msg = msg;
        this.traceId = traceId;
        this.data = data;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    @Override
    public String toString() {
        return "ResultModel{" +
                "code=" + code +
                ", data=" + data +
                ", msg='" + msg + '\'' +
                ", traceId='" + traceId + '\'' +
                ", ts=" + ts +
                '}';
    }

}
