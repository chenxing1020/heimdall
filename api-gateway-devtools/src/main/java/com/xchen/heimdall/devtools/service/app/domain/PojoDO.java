package com.xchen.heimdall.devtools.service.app.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.xchen.heimdall.devtools.service.app.dto.FieldDTO;
import com.xchen.heimdall.devtools.service.app.utils.FieldListTypeHandler;
import com.xchen.heimdall.common.constant.PojoType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author 
 * @date 2022/4/12
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "pojo", autoResultMap = true)
public class PojoDO {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String pojoName;
    private String ownerUserId;
    private PojoType pojoType;
    private String pojoDesc;
    private String pojoPath;
    @TableField(typeHandler = FieldListTypeHandler.class)
    private List<FieldDTO> fieldList;
    private Integer parentId;
    private Integer projectId;

    @TableField(select = false)
    private String createUserId;
    @TableField(select = false)
    private String updateUserId;
    @TableLogic(value = "'1970-01-01 00:00:00'", delval = "now()")
    @TableField(select = false)
    private LocalDateTime deleteTime;
}
