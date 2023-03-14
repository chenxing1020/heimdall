package com.xchen.heimdall.devtools.service.app.utils;

import com.xchen.heimdall.devtools.service.app.domain.DubboMethodDetailDO;
import com.xchen.heimdall.devtools.service.app.domain.ProjectDO;
import com.xchen.heimdall.devtools.service.app.dto.FieldDTO;
import com.xchen.heimdall.devtools.service.app.dto.GatewayApiDTO;
import com.xchen.heimdall.common.constant.PojoType;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

import static com.xchen.heimdall.common.constant.DtoWrapperType.SUBLIST;

/**
 * @author xchen
 * @date 2022/4/27
 */
@Component
public class CommonUtils {

    @Value("${config.absoluteHomePath}")
    private String absoluteHomePath;

    @Value("${config.modulePath}")
    private String modulePath;

    @Value("${config.basePackagePath}")
    private String basePackagePath;
    @Value("${config.gateway.contextPath}")
    private String gatewayContextPath;

    @Value("${config.git.projectName}")
    private String gitProjectName;

    private static final String BAR_SEPARATOR = "-";
    private static final String DOT_SEPARATOR = ".";
    private static final String JAVA_SUFFIX = ".java";
    private static final String LINE_BREAK = "\n";
    private static final String SPACES = "    ";
    private static final String RPC_PREFIX = "/rpc";
    private static final String SUB_PREFIX = "/sub";

    public void setRpcRequestPath(GatewayApiDTO gatewayApi) {
        gatewayApi.setRequestPath(gatewayContextPath
                + getRpcRequestPathWithoutContextPath(gatewayApi)
        );
    }

    public void setRpcRequestPathWithoutContextPath(GatewayApiDTO gatewayApi) {
        gatewayApi.setRequestPath(getRpcRequestPathWithoutContextPath(gatewayApi));
    }

    private String getRpcRequestPathWithoutContextPath(GatewayApiDTO gatewayApi) {
        String prefix = RPC_PREFIX;
        if (SUBLIST.equals(gatewayApi.getDtoWrapperType())) {
            prefix = SUB_PREFIX;
        }
        return String.format("%s/%s/%s",
                prefix,
                gatewayApi.getSimpleServiceName(),
                gatewayApi.getMethodName()
        );
    }

    /**
     * 根据项目名获取项目packagePath
     * 如 oms-service，packagePath为 com.xchen.heimdall.dubbo.api.oms.service
     *
     * @param projectName 项目名
     * @return 项目路径
     */
    public String getProjectPath(String projectName) {
        return basePackagePath + formatProjectName(projectName);
    }

    /**
     * 获取pojoPath，默认所有project以projectType结尾，此处只需要讲projectType替换为vo/dto目录
     * 如 oms-service，voPath为 com.xchen.heimdall.dubbo.api.oms.vo
     *
     * @param projectDO 工程信息
     * @param pojoType  pojo类型
     * @return pojoPath
     */
    public String getPojoPath(ProjectDO projectDO, PojoType pojoType) {
        return StringUtils.replace(getProjectPath(projectDO.getProjectName()),
                projectDO.getProjectType().getType(),
                StringUtils.lowerCase(pojoType.getType())
        );
    }

    public String getAbsoluteFilePath(String filePath, String userId) {
        // 将a.b.c替换成a/b/c
        String formatFilePath = StringUtils.replace(filePath, DOT_SEPARATOR, File.separator);
        String absoluteFilePath = FilenameUtils.concat(getAbsoluteModulePath(userId),
                formatFilePath);
        // 规范分隔符
        return FilenameUtils.separatorsToSystem(absoluteFilePath);
    }

    public String getAbsoluteUserPath(String userId) {
        return FilenameUtils.concat(absoluteHomePath, userId);
    }

    public String getAbsoluteGitProjectPath(String userId) {
        return FilenameUtils.concat(getAbsoluteUserPath(userId), gitProjectName);
    }

    public String getAbsoluteModulePath(String userId) {
        return FilenameUtils.concat(getAbsoluteGitProjectPath(userId), modulePath);
    }

    public String getJavaFileName(String fileName) {
        return fileName + JAVA_SUFFIX;
    }

    public String getVoFullName(DubboMethodDetailDO methodDetail) {
        return getJavaFullName(methodDetail.getVoPath(), methodDetail.getVoName());
    }

    public String getDtoFullName(DubboMethodDetailDO methodDetail) {
        return getJavaFullName(methodDetail.getDtoPath(), methodDetail.getDtoName());
    }

    public String getJavaFullName(String path, String className) {
        return String.format("%s.%s", path, className);
    }

    public String getPojoTemplate(String wrapperName, String pojoName) {
        return String.format("%s<%s>", wrapperName, pojoName);
    }

    public void setFieldTemplateAnnotation(FieldDTO field) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("@ApiModelProperty(value = \"%s\")", field.getFieldDesc()));

        if (Boolean.TRUE.equals(field.getLogExcluded())) {
            sb.append(LINE_BREAK).append(SPACES).append("@ToString.Exclude");
        }

        if (Boolean.TRUE.equals(field.getNotNull())) {
            sb.append(LINE_BREAK).append(SPACES).append("@NotNull");
        }

        if (StringUtils.isNotEmpty(field.getAnnotation())) {
            sb.append(LINE_BREAK).append(SPACES).append(field.getAnnotation());
        }

        field.setTemplateAnnotation(sb.toString());
    }

    public String formatProjectName(String projectName) {
        return StringUtils.replace(projectName, BAR_SEPARATOR, DOT_SEPARATOR);
    }

}
