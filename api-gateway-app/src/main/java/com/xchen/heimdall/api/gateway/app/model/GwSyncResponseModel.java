package com.xchen.heimdall.api.gateway.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author by xchen
 * @since 2023/3/4.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GwSyncResponseModel {
    private int responseCode;
    private String msg;
}
