package com.xchen.heimdall.api.gateway.app.manager;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.xchen.heimdall.common.exception.errorcode.*;
import com.xchen.heimdall.dubbo.api.gateway.model.UserLoginModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 可自行配置认证校验
 *
 * @author xchen
 * @date 2022/1/12
 */
@Service
@Slf4j
public class AuthManager {

    @Resource
    private LoadBalanceManager loadBalanceManager;

    @Value("${authenticate.bearerToken.secret}")
    private String bearerTokenSecret;
    @Value("${authenticate.timeoutMillis:5000}")
    private long timeoutMillis;

    private final ScheduledExecutorService checkTokenScheduleService = Executors.newSingleThreadScheduledExecutor();

    /**
     * token和userId的caffeine缓存
     */
    private final LoadingCache<String, Optional<String>> tokenCache = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build(this::loadUserId);

    @PostConstruct
    private void postInit() {
        // 异步定时刷新本地token，暂定12s刷新一次
        checkTokenScheduleService.scheduleAtFixedRate(() -> {
            for (Map.Entry<String, Optional<String>> entry : tokenCache.asMap().entrySet()) {
                if (entry.getValue().isPresent()) {
                    try {
                        // 定时校验时失效用Optional.empty占位
                        Optional<String> userIdOptional = loadUserId(entry.getKey());
                        if (!userIdOptional.isPresent()) {
                            entry.setValue(Optional.empty());
                        }
                    } catch (Exception e) {
                        // 异步刷新本地缓存时，忽略heimdall异常，不影响已登录用户
                        log.warn("Failed to validate token, due to: {}", e.getMessage(), e);
                    }
                }
            }
        }, 12, 12, TimeUnit.SECONDS);
    }

    public String login(UserLoginModel request) {
        // XXX: 自定义
        return "accessToken";
    }

    public boolean logout(String accessToken) {
        // XXX: 自定义
        return true;
    }

    /**
     * 获取userId
     *
     * @param token 用户令牌
     * @return userId
     */
    public Optional<String> loadUserId(String token) {

        String userId = validateBearerToken(token);
        // 校验失败
        if (!StringUtils.isBlank(userId)) {
            // 用Option.empty占位，减少缓存穿透
            return Optional.empty();
        }

        // 从hams查询userId
        return Optional.of(userId);
    }

    /**
     * 利用本地caffeine进行校验
     *
     * @param token 认证令牌
     * @return 用户id
     */
    public String validateWithCache(String token) {
        if (Objects.isNull(token)) {
            throw new UnauthorizedException("No access token");
        }

        Optional<String> userIdOptional = tokenCache.get(token);
        if (Objects.isNull(userIdOptional) || !userIdOptional.isPresent()) {
            throw new UnauthorizedException("Token expired");
        }
        return userIdOptional.get();
    }

    /**
     * 鉴权jwt
     *
     * @param token jwt
     * @return 用户ID
     */
    public String validateBearerToken(String token) {
        JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(bearerTokenSecret)).build();
        try {
            DecodedJWT decodedJwt = jwtVerifier.verify(token);
            // 获取解析后的token中的userId
            return decodedJwt.getIssuer();
        } catch (JWTVerificationException e) {
            log.warn("Bearer-Token鉴权失败，token={}, msg={}", token, e.getMessage());
            throw new UnauthorizedException("Bearer-Token 无效");
        }
    }
}
