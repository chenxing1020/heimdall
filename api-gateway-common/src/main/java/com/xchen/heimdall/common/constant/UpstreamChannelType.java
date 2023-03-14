package com.xchen.heimdall.common.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author xchen
 */
@AllArgsConstructor
@Getter
public enum UpstreamChannelType {

    /**
     * dubbo
     */
    DUBBO(1),

    /**
     * nats sub
     */
    NATS_SUB(2),
    /**
     * heimdall
     */
    HEIMDALL(3),
    /**
     * rest
     */
    REST(4),
    /**
     * internal
     */
    INTERNAL(5),

    /**
     * GRPC
     */
    GRPC(6),
    /**
     * Rest raw
     * 仅作代理
     */
    REST_RAW(7);

    @EnumValue
    private final Integer code;

}
