package com.xchen.heimdall.api.gateway.app.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.socket.HandshakeInfo;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.Set;

/**
 * 工具类
 */
@Slf4j
public class HttpHeaderUtils {

    private HttpHeaderUtils() {
    }
    private static final String UNKNOWN = "unknown";

    /**
     * 判断客户端IP是否在列表中
     *
     * @param request 请求
     * @param ipList  l
     * @return b
     */
    public static boolean containClientIP(ServerHttpRequest request, Set<String> ipList) {

        String[] clientIps = null;
        HttpHeaders headers = request.getHeaders();

        log.info("[EIP单点]请求header：{}", headers);

        //X-Forwarded-For：Squid 服务代理
        String ipAddresses = headers.getFirst("x-forwarded-for");

        if (StringUtils.isEmpty(ipAddresses) || UNKNOWN.equalsIgnoreCase(ipAddresses)) {
            //Proxy-Client-IP：apache 服务代理
            ipAddresses = headers.getFirst("Proxy-Client-IP");
        }

        if (StringUtils.isEmpty(ipAddresses) || UNKNOWN.equalsIgnoreCase(ipAddresses)) {
            //X-Real-IP：nginx服务代理
            ipAddresses = headers.getFirst("X-Real-IP");
        }

        if (StringUtils.isEmpty(ipAddresses) || UNKNOWN.equalsIgnoreCase(ipAddresses)) {
            ipAddresses = getIpFromRemoteAddress(request);
        }

        log.info("[EIP单点]客户端IP地址：{}", ipAddresses);

        if (CollectionUtils.isEmpty(ipList)) {
            return true;
        }

        if (!StringUtils.isEmpty(ipAddresses)) {
            clientIps = ipAddresses.split(",");
            for (String client : clientIps) {
                if (ipList.contains(client)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 获取代理之前的客户端IP
     *
     * @param request
     * @return
     */
    public static String getClientIP(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        String ip = null;

        String ipAddresses = getIpFromHeaders(headers);

        //有些网络通过多层代理，那么获取到的ip就会有多个，一般都是通过逗号（,）分割开来，并且第一个ip为客户端的真实IP
        if (!StringUtils.isEmpty(ipAddresses)) {
            ip = ipAddresses.split(",")[0];
        }
        //还是不能获取到，最后再通过request.getRemoteAddr();获取
        if (StringUtils.isEmpty(ip) || UNKNOWN.equalsIgnoreCase(ipAddresses)) {
            ip = getIpFromRemoteAddress(request);
        }
        return ip;
    }

    private static String getIpFromRemoteAddress(ServerHttpRequest request) {
        if (Objects.nonNull(request)) {
            InetSocketAddress inetSocketAddress = request.getRemoteAddress();
            if (Objects.nonNull(inetSocketAddress)) {
                return inetSocketAddress.getHostString();
            }
        }
        return null;
    }

    private static String getIpFromHandshakeInfo(HandshakeInfo handshakeInfo) {
        if (Objects.nonNull(handshakeInfo)) {
            InetSocketAddress inetSocketAddress = handshakeInfo.getRemoteAddress();
            if (Objects.nonNull(inetSocketAddress)) {
                return inetSocketAddress.getHostString();
            }
        }
        return null;
    }

    public static String getWebSocketClientIp(WebSocketSession session) {
        HandshakeInfo handshakeInfo = session.getHandshakeInfo();
        HttpHeaders headers = handshakeInfo.getHeaders();

        String ip = null;
        String ipAddresses = getIpFromHeaders(headers);
        //有些网络通过多层代理，那么获取到的ip就会有多个，一般都是通过逗号（,）分割开来，并且第一个ip为客户端的真实IP
        if (!StringUtils.isEmpty(ipAddresses)) {
            ip = ipAddresses.split(",")[0];
        }
        //还是不能获取到，最后再通过request.getRemoteAddr();获取
        if (StringUtils.isEmpty(ip) || UNKNOWN.equalsIgnoreCase(ipAddresses)) {
            ip = getIpFromHandshakeInfo(handshakeInfo);
        }
        return ip;
    }

    private static String getIpFromHeaders(HttpHeaders headers) {
        if (Objects.isNull(headers)) {
            return null;
        }

        //X-Forwarded-For：Squid 服务代理
        String ipAddresses = headers.getFirst("X-Forwarded-For");
        if (StringUtils.isEmpty(ipAddresses) || UNKNOWN.equalsIgnoreCase(ipAddresses)) {
            //iv-remote-address 测试环境EIP代理
            ipAddresses = headers.getFirst("iv-remote-address");
        }
        if (StringUtils.isEmpty(ipAddresses) || UNKNOWN.equalsIgnoreCase(ipAddresses)) {
            //Proxy-Client-IP：apache 服务代理
            ipAddresses = headers.getFirst("Proxy-Client-IP");
        }

        if (StringUtils.isEmpty(ipAddresses) || UNKNOWN.equalsIgnoreCase(ipAddresses)) {
            //WL-Proxy-Client-IP：weblogic 服务代理
            ipAddresses = headers.getFirst("WL-Proxy-Client-IP");
        }

        if (StringUtils.isEmpty(ipAddresses) || UNKNOWN.equalsIgnoreCase(ipAddresses)) {
            //HTTP_CLIENT_IP：有些代理服务器
            ipAddresses = headers.getFirst("HTTP_CLIENT_IP");
        }

        if (StringUtils.isEmpty(ipAddresses) || UNKNOWN.equalsIgnoreCase(ipAddresses)) {
            //X-Real-IP：nginx服务代理
            ipAddresses = headers.getFirst("X-Real-IP");
        }

        return ipAddresses;
    }
}
