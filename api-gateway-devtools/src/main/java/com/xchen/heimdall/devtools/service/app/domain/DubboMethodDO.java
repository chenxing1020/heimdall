package com.xchen.heimdall.devtools.service.app.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.xchen.heimdall.devtools.service.app.common.constant.MethodException;
import com.xchen.heimdall.common.constant.DtoWrapperType;
import com.xchen.heimdall.common.constant.VoWrapperType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author xchen
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "dubbo_method")
public class DubboMethodDO implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer serviceId;
    private String methodName;
    private Integer voId;
    private VoWrapperType voWrapperType;
    private Integer dtoId;
    private DtoWrapperType dtoWrapperType;
    private MethodException methodException;
    private String apiDesc;
    private String apiRemark;
    private Integer codeStatus;
    private Boolean gatewayApiStatus;
    private String ownerUserId;

    @TableField(select = false)
    private String createUserId;
    @TableField(select = false)
    private String updateUserId;
    @TableLogic(value = "'1970-01-01 00:00:00'", delval = "now()")
    @TableField(select = false)
    private LocalDateTime deleteTime;

}
