package com.xchen.heimdall.api.gateway.app.manager;

import com.xchen.heimdall.api.gateway.app.constant.VoField;
import com.xchen.heimdall.api.gateway.app.model.RequestModel;
import com.xchen.heimdall.common.exception.errorcode.BadRequestException;

import java.util.HashMap;
import java.util.Objects;

/**
 * @author xchen
 * @date 2022/2/9
 */
public class VoDecorateManager {

    /**
     * 对泛化的vo进行修改，默认所有的vo都是map
     *
     * @param request 请求
     * @param field   需要包装的字段
     * @param value   需要包装的字段值
     */
    public static void decorateVo(RequestModel request, String field, String value) {
        try {
            Object vo = request.getData();
            HashMap<String, Object> voMap = getMapByVo(vo);
            voMap.put(field, value);
            request.setData(voMap);
        } catch (Exception e) {
            throw new BadRequestException("Failed to parse vo", e);
        }
    }

    /**
     * 对泛化的包装vo进行修改
     *
     * @param request 请求
     * @param field   需要包装的字段
     * @param value   需要包装的字段值
     * @return 新的包装vo map
     */
    public static void decorateWrapperVo(RequestModel request, String field, String value) {
        try {
            Object wrapperVo = request.getData();
            HashMap<String, Object> wrapperVoMap = getMapByVo(wrapperVo);
            HashMap<String, Object> dataMap;
            if (wrapperVoMap.containsKey(VoField.DATA_KEY)) {
                dataMap = (HashMap<String, Object>) wrapperVoMap.get(VoField.DATA_KEY);
            } else {
                dataMap = new HashMap<>(1);
            }
            dataMap.put(field, value);
            wrapperVoMap.put(VoField.DATA_KEY, dataMap);
            request.setData(wrapperVoMap);
        } catch (Exception e) {
            throw new BadRequestException("Failed to parse wrapper vo", e);
        }
    }

    private static HashMap<String, Object> getMapByVo(Object vo) {
        if (Objects.isNull(vo)) {
            return new HashMap<>(1);
        } else {
            return (HashMap<String, Object>) vo;
        }
    }
}
