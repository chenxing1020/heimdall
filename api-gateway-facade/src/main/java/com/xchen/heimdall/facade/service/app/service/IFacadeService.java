package com.xchen.heimdall.facade.service.app.service;

/**
 * facade service的dubbo接口
 *
 * @author xchen
 * @date 2022/4/7
 */
public interface IFacadeService {

    /**
     * 泛化调用dubbo接口
     *
     * @param requestPath 请求路径
     * @param data 请求入参
     * @return 泛化调用结果
     */
    String invokeDubbo(String requestPath, String data);

}
