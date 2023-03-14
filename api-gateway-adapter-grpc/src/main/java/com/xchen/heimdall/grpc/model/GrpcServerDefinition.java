package com.xchen.heimdall.grpc.model;

import io.grpc.Channel;
import lombok.Data;

import java.util.List;

/**
 * @author 016878
 * @date 2022/1/6
 * grpc服务定义，暂定义配置在properties中
 */
@Data
public class GrpcServerDefinition {
    /**
     * 服务名
     */
    private String serverName;

    /**
     * 服务主机ip地址，可设置多活
     */
    private List<String> hostnames;

    private Integer port;

    /**
     * 服务channel
     */
    private Channel channel;

}
