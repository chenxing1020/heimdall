package com.xchen.heimdall.api.gateway.app.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xchen
 */
public enum ClientType {

    /**
     * 未定义
     */
    UNDEFINED(0),
    /**
     * 微信小程序
     */
    MP(1),
    /**
     * 移动端网页
     */
    H5(2),
    /**
     * PC端网页
     */
    WEB(3),
    /**
     * iOS移动端
     */
    IOS(4),
    /**
     * 安卓移动端
     */
    ANDROID(5),
    /**
     * 在IOS内的H5
     */
    H5_IN_IOS(6),
    /**
     * 在ANDROID内的H5
     */
    H5_IN_ANDROID(7);

    private final int code;

    ClientType(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    /**
     * 判断当前类型是否属于APP
     */
    public boolean isApp() {
        return ClientType.IOS.equals(this) || ClientType.ANDROID.equals(this);
    }

    /**
     * 判断是否在APP内的H5
     */
    public boolean isH5InApp() {
        return ClientType.H5_IN_IOS.equals(this) || ClientType.H5_IN_ANDROID.equals(this);
    }

    private static final Map<Integer, ClientType> CLIENT_TYPE_MAP;

    static {
        CLIENT_TYPE_MAP = new HashMap<>(8);
        for (ClientType ct : ClientType.values()) {
            CLIENT_TYPE_MAP.put(ct.getCode(), ct);
        }
    }

    /**
     * 对编码进行识别
     *
     * @param code 编码
     * @return 对应枚举值
     */
    public static ClientType decode(Integer code) {
        return CLIENT_TYPE_MAP.get(code);
    }

}
