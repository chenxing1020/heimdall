package com.xchen.heimdall.dubbo.api.gateway.model;

import java.io.Serializable;
import lombok.Data;

/**
 * @author xchen
 * @since 2022/4/11 16:17
 */
@Data
public class LoginResultModel implements Serializable {

    private String token;

    private String userId;
}
