package com.xchen.heimdall.devtools.service.app.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.xchen.heimdall.common.constant.ProjectType;
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
@TableName(value = "project")
public class ProjectDO implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String projectName;
    private ProjectType projectType;
    private String projectDesc;
    private String ownerUserId;

    @TableField(select = false)
    private String createUserId;
    @TableField(select = false)
    private String updateUserId;
    @TableLogic(value = "'1970-01-01 00:00:00'", delval = "now()")
    @TableField(select = false)
    private LocalDateTime deleteTime;

}
