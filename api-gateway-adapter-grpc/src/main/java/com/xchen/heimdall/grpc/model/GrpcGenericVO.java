package com.xchen.heimdall.grpc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author 016878
 * @date 2022/1/6
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrpcGenericVO {

    private String packageName;

    private String serviceName;

    private String methodName;

    private String jsonParams;

    private Long timeoutMillis = 20000L;

    private String userId;

    private Map<String, String> headFields;
}
