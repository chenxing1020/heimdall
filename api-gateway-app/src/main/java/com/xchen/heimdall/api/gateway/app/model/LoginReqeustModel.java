package com.xchen.heimdall.api.gateway.app.model;

import com.xchen.heimdall.dubbo.api.gateway.model.UserLoginModel;
import lombok.Data;

/**
 * @author xchen
 * @date 2022/8/19
 */
@Data
public class LoginReqeustModel {
    private UserLoginModel data;
}
