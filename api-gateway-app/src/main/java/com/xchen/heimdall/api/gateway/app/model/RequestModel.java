package com.xchen.heimdall.api.gateway.app.model;

import lombok.Data;

/**
 * @author xchen
 * @date 2022/1/15
 */
@Data
public class RequestModel {

    /**
     * 业务字段
     */
    protected Object data;

    /**
     * 客户端信息，保留字段
     */
    protected ClientInfoModel info;
}
