package com.xchen.heimdall.api.gateway.app.controller;

import com.xchen.heimdall.api.gateway.app.constant.HeaderKey;
import com.xchen.heimdall.api.gateway.app.manager.AuthManager;
import com.xchen.heimdall.api.gateway.app.manager.ResponseManager;
import com.xchen.heimdall.api.gateway.app.model.*;
import com.xchen.heimdall.api.gateway.app.util.HttpHeaderUtils;
import com.xchen.heimdall.api.gateway.app.model.UserLoginModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;

/**
 * @author xchen
 */
@Slf4j
@RestController
public class AuthController {

    @Resource
    private AuthManager authManager;

    @PostMapping(value = "/login")
    public Mono<ResultModel<Object>> login(@RequestBody LoginReqeustModel request, ServerHttpRequest serverHttpRequest) {
        // 塞入ip
        UserLoginModel userLoginModel = request.getData();
        userLoginModel.setUserIp(HttpHeaderUtils.getClientIP(serverHttpRequest));
        return ResponseManager.pack(() ->
                authManager.login(userLoginModel)
        );
    }

    @PostMapping(value = "/logout")
    public Mono<ResultModel<Boolean>> logout(@RequestHeader(required = false, name = HeaderKey.ACCESS_TOKEN) String accessToken) {
        // loginRequired
        authManager.validateWithCache(accessToken);
        return ResponseManager.pack(() ->
                authManager.logout(accessToken)
        );
    }

    @PostMapping(value = "/validateToken")
    public Mono<ResultModel<String>> validateToken(@RequestHeader(required = false, name = HeaderKey.ACCESS_TOKEN) String accessToken) {
        return ResponseManager.pack(() ->
                authManager.validateWithCache(accessToken)
        );
    }
}
