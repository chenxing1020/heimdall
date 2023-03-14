package com.xchen.heimdall.api.gateway.app.model;

import com.xchen.heimdall.dubbo.api.gateway.model.ClientInfoModel;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author xchen
 * @date 2022/1/22
 */
@Data
public class CheckPasswordModel extends ClientInfoModel {
    @NotNull
    private String password;
}
