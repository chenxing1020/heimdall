package com.xchen.heimdall.devtools.service.app.utils;

import com.baomidou.mybatisplus.extension.handlers.AbstractJsonTypeHandler;
import com.fasterxml.jackson.core.type.TypeReference;
import com.xchen.heimdall.devtools.service.app.dto.FieldDTO;
import com.xchen.heimdall.common.util.JacksonUtil;

import java.util.List;

/**
 * @author xchen
 * @date 2022/5/25
 */
public class FieldListTypeHandler extends AbstractJsonTypeHandler<List<FieldDTO>> {
    @Override
    protected List<FieldDTO> parse(String json) {
        return JacksonUtil.decode(json, new TypeReference<List<FieldDTO>>() {
        });
    }

    @Override
    protected String toJson(List<FieldDTO> obj) {
        return JacksonUtil.encode(obj);
    }
}
