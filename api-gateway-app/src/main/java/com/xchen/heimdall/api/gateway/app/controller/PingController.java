package com.xchen.heimdall.api.gateway.app.controller;

import com.xchen.heimdall.api.gateway.app.manager.LoadBalanceManager;
import com.xchen.heimdall.api.gateway.app.manager.ResponseManager;
import com.xchen.heimdall.api.gateway.app.model.ResultModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;

/**
 * @author xchen
 * @date 2022/2/16
 */
@Slf4j
@RestController
public class PingController {

    @Resource
    private LoadBalanceManager loadBalanceManager;

    @GetMapping(value = "/ping")
    public Mono<ResultModel<String>> pingFacadeService() {
        return ResponseManager.pack(() ->
                loadBalanceManager.choose().sendPingRequest()
        );
    }
}
