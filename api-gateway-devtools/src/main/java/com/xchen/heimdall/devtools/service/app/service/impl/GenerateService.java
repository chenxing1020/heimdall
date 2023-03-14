package com.xchen.heimdall.devtools.service.app.service.impl;

import com.xchen.heimdall.devtools.service.app.utils.CommonUtils;
import com.xchen.heimdall.devtools.service.app.utils.GitLabUtils;
import com.xchen.heimdall.devtools.service.app.utils.MavenInvokerUtils;
import com.xchen.heimdall.devtools.service.app.vo.AssigneeUserIdVO;
import com.xchen.heimdall.common.exception.errorcode.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import static com.xchen.heimdall.devtools.service.app.common.constant.ErrorCode.FILE_OPS_ERROR;

/**
 * @author xchen
 * @date 2022/4/25
 */
@Slf4j
@Service
public class GenerateService {

    @Resource
    private TemplateEngine templateEngine;
    @Resource
    private GitLabUtils gitLabUtils;
    @Resource
    private CommonUtils commonUtils;
    @Resource
    private MavenInvokerUtils mavenInvokerUtils;

    public static final String SERVICE_TEMPLATE = "Service";
    public static final String POJO_TEMPLATE = "Pojo";

    /**
     * 生成模板
     *
     * @param templateType 模板类型
     * @param context      模板context
     * @return 模板结果
     */
    public String process(String templateType, Context context) {
        return templateEngine.process(templateType, context);
    }

    public void generate(String relativeFilePath, String fileName, String processResult, String userId) {
        // 生成文件夹
        String absoluteFilePath = commonUtils.getAbsoluteFilePath(relativeFilePath, userId);
        makeDirectory(absoluteFilePath);

        // 生成文件
        File file = new File(absoluteFilePath, fileName);
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    throw CustomException.builder()
                            .errorCode(FILE_OPS_ERROR)
                            .message("Failed to create new file: " + file.getAbsolutePath())
                            .build();
                }
            } catch (IOException e) {
                throw CustomException.builder()
                        .errorCode(FILE_OPS_ERROR)
                        .exception(e)
                        .message("Failed to create new file: " + e.getMessage())
                        .build();
            }
        }

        // 写入文件
        try (Writer writer = new FileWriter(file)) {
            writer.write(processResult);
        } catch (IOException e) {
            throw CustomException.builder()
                    .errorCode(FILE_OPS_ERROR)
                    .exception(e)
                    .message("Failed to generate file: " + file.getAbsolutePath())
                    .build();
        }
    }

    public void preGenerate(String userId) {
        // 生成并清空userId文件夹
        String absoluteProjectPath = commonUtils.getAbsoluteGitProjectPath(userId);
        makeAndCleanDirectory(absoluteProjectPath);

        // 生成前调用，clone代码
        gitLabUtils.preGenerate(absoluteProjectPath);
    }

    public String postGenerate(AssigneeUserIdVO vo) {
        String absoluteProjectPath = commonUtils.getAbsoluteGitProjectPath(vo.getUserId());
        // maven编译，文件路径 userId/common-lib/pom.xml
        mavenInvokerUtils.invokeCompile(absoluteProjectPath);

        // 生成后调用，执行git add/commit/push。并发起merge request
        return gitLabUtils.postGenerate(absoluteProjectPath, vo);
    }

    private void makeDirectory(String directory) {
        try {
            File directoryFile = new File(directory);
            File voDirectoryFile = new File(FilenameUtils.concat(directory, "vo"));
            File dtoDirectoryFile = new File(FilenameUtils.concat(directory, "dto"));
            FileUtils.forceMkdir(directoryFile);
            // 生成pojo文件夹
            FileUtils.forceMkdir(voDirectoryFile);
            FileUtils.forceMkdir(dtoDirectoryFile);
        } catch (Exception e) {
            throw CustomException.builder()
                    .errorCode(FILE_OPS_ERROR)
                    .exception(e)
                    .message("Failed to make and clean directory: " + directory)
                    .build();
        }
    }

    private void makeAndCleanDirectory(String directory) {
        try {
            File directoryFile = new File(directory);
            FileUtils.forceMkdir(directoryFile);
            FileUtils.cleanDirectory(directoryFile);
        } catch (Exception e) {
            throw CustomException.builder()
                    .errorCode(FILE_OPS_ERROR)
                    .exception(e)
                    .message("Failed to make and clean directory: " + directory)
                    .build();
        }
    }
}