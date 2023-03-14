package com.xchen.heimdall.api.gateway.app.manager;

import com.xchen.heimdall.api.gateway.app.model.ResultModel;
import com.xchen.heimdall.common.util.JacksonUtil;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

/**
 * @author xchen
 */
public class ResponseManager {

    private ResponseManager() {
    }

    public static <T> Mono<ResultModel<T>> pack(Supplier<? extends T> supplier) {
        // 打包请求成功的返回包装
        return Mono.fromSupplier(() ->
                ResultModel.success(supplier.get())
        );
    }

    public static String packAsJson(Integer code, String msg) {
        // 将异常结果打包成json字符串
        return JacksonUtil.encode(ResultModel.fail(code, msg));
    }

    /**
     * 用于序列化bo的数据
     *
     * @param object
     * @return
     */
    public static String packAsJson(Object object) {
        return JacksonUtil.encode(ResultModel.success(object));
    }
}
