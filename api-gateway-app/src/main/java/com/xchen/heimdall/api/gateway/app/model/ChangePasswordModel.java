package com.xchen.heimdall.api.gateway.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author xchen
 * @date 2022/6/21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordModel implements Serializable {

    @NotNull
    private String userName;

    @NotNull
    private String oldPassword;

    @NotNull
    private String newPassword;
}
