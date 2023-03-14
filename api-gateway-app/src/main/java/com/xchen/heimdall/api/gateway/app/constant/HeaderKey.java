package com.xchen.heimdall.api.gateway.app.constant;

/**
 * @author xchen
 * @date 2022/1/22
 */
public final class HeaderKey {
    public static final String ACCESS_TOKEN = "AccessToken";
    public static final String USER_ID = "UserId";

    public static final String SEC_WEBSOCKT_PROTOCOL = "Sec-WebSocket-Protocol";

    /**
     * EIP鉴权通过携带的用户名key
     */
    public static final String EIP_USER_ID = "iv-user";

    /**
     * 铃客鉴权的session
     */
    public static final String LINK_MOA_SESSION = "Session-Moa";

    /**
     * 网关自己生成的JWT
     */
    public static final String BEARER_TOKEN = "Bearer-Token";
}
