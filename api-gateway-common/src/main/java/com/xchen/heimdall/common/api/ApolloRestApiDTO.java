package com.xchen.heimdall.common.api;

import com.xchen.heimdall.common.constant.OperationType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author xchen
 * @date 2022/3/15
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ApolloRestApiDTO extends ApolloGatewayApiDTO {

    /**
     * rest api服务内路径
     */
    @NotEmpty
    private String restUrl;

    /**
     * rest方法：POST、GET
     */
    @NotNull
    private OperationType operationType;
}
