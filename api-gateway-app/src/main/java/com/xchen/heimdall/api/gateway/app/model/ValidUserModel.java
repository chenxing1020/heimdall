package com.xchen.heimdall.api.gateway.app.model;

import com.xchen.heimdall.dubbo.api.gateway.model.ClientInfoModel;
import lombok.Data;

/**
 * @author xchen
 * @date 2022/2/24
 */
@Data
public class ValidUserModel extends ClientInfoModel {
    private String cid;
    private String challenge;
    private String uniqueId;
    private String channelNum;

    private boolean offline = false;
}
