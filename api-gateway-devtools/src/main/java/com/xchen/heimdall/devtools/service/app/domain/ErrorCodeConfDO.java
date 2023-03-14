package com.xchen.heimdall.devtools.service.app.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_error_code")
public class ErrorCodeConfDO extends BaseDO {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 错误码
     */
    private String errorCode;

    /**
     * 错误级别
     */
    private Integer level;

    /**
     * 提示文案
     */
    private String displayMessage;

    /**
     * 错误原因
     */
    private String errorReason;

    /**
     * 处理方法
     */
    private String solution;

    /**
     * 所属工程
     */
    private Integer projectId;

    /**
     * 版本
     */
    @TableField()
    private String version;
}
