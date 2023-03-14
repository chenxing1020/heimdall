package com.xchen.heimdall.dubbo.api.gateway.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @author xchen
 * @date 2022/1/18
 */
@Data
public class ClientInfoModel implements Serializable {
    /**
     * 客户端信息，用于安全日志
     */
    protected String userIp;
    protected String userMacAddr;
    protected String userHardDiskVolNumber;
    protected String terminalId;
    protected String terminalVersion;
    protected String brokerFlag;
    protected String operationTime;
}
