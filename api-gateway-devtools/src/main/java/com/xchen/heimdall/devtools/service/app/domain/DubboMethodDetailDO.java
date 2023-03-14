package com.xchen.heimdall.devtools.service.app.domain;

import com.xchen.heimdall.devtools.service.app.common.constant.MethodException;
import com.xchen.heimdall.common.constant.DtoWrapperType;
import com.xchen.heimdall.common.constant.VoWrapperType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用于生成service代码的模型
 *
 * @author xchen
 * @date 2022/4/27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DubboMethodDetailDO {

    private Integer id;

    private String ownerUserId;

    /** project 信息 **/
    private Integer projectId;
    private String projectName;

    /** service信息 **/
    private Integer serviceId;
    private String serviceName;

    /** method信息 **/
    private String methodName;
    private MethodException methodException;
    private String apiDesc;
    private String apiRemark;
    private Integer codeStatus;
    private Boolean gatewayApiStatus;
    private Date updateTime;

    /** vo信息 **/
    private Integer voId;
    private String voName;
    private String voPath;
    private VoWrapperType voWrapperType;

    /** dto信息 **/
    private Integer dtoId;
    private String dtoName;
    private String dtoPath;
    private DtoWrapperType dtoWrapperType;

    /** 模板字段 **/
    private String voTemplateName;
    private String dtoTemplateName;
}
