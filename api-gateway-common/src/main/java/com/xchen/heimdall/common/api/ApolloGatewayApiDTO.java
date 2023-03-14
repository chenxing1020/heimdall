package com.xchen.heimdall.common.api;

import com.xchen.heimdall.common.constant.DtoWrapperType;
import com.xchen.heimdall.common.constant.UpstreamChannelType;
import com.xchen.heimdall.common.constant.VoWrapperType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @author xchen
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApolloGatewayApiDTO implements Serializable {

    @NotEmpty
    protected String requestPath;

    @NotNull
    protected UpstreamChannelType upstreamChannelType;

    protected String serviceName;

    protected String simpleServiceName;

    protected Integer timeout;

    protected Boolean loginRequired;

    protected List<String> permissionKeyList;

    /**
     * 入参类型：普通类型、分页类型、无参类型
     */
    protected VoWrapperType voWrapperType = VoWrapperType.DEFAULT;

    /**
     * 出参类型：普通类型、分页类型、无参类型
     */
    protected DtoWrapperType dtoWrapperType;

    /**
     * 接口tps参考值
     */
    protected Long tps;

    /**
     * 网关侧进行错误码转换的字段
     */
    protected List<String> errorCodeReplaceFields;

}
