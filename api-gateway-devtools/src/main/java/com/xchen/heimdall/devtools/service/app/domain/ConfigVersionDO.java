package com.xchen.heimdall.devtools.service.app.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "t_config_version")
public class ConfigVersionDO {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 版本号
     */
    private String version;

    /**
     * 配置类型
     */
    private Integer type;

    /**
     * 工程ID
     */
    private Integer projectId;

    /**
     * 创建时间
     */
    private Date createTime;

}
