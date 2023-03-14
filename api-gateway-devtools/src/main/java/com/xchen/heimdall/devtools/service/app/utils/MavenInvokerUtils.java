package com.xchen.heimdall.devtools.service.app.utils;

import com.xchen.heimdall.common.exception.errorcode.CustomException;
import com.xchen.heimdall.devtools.service.app.common.constant.ErrorCode;
import org.apache.maven.shared.invoker.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * maven编译工具类
 *
 * @author xchen
 * @date 2022/5/23
 */
@Component
public class MavenInvokerUtils {

    private static final Logger log = LoggerFactory.getLogger(MavenInvokerUtils.class);

    @Value("${config.mavenHome}")
    private String mavenHome;

    private File mavenHomeFile;

    @PostConstruct
    public void postConstruct() {
        mavenHomeFile = new File(mavenHome);
    }

    public void invokeCompile(String filePath) {
        InvocationRequest request = new DefaultInvocationRequest()
                .setPomFile(new File(filePath, "/dubbo-api/pom.xml"))
                .setGoals(Arrays.asList("compile", "-T 4"))
                .setOutputHandler(new InvocationOutputHandler() {
                    @Override
                    public void consumeLine(String s) throws IOException {
                        log.info(s);
                    }
                });

        Invoker invoker = new DefaultInvoker();
        invoker.setMavenHome(mavenHomeFile);

        try {
            InvocationResult result = invoker.execute(request);
            if (0 == result.getExitCode()) {
                log.info("Success to compile");
            } else {
                log.error("Failed to compile: {}", result.getExecutionException().getMessage(), result.getExecutionException());
                throw new CustomException(ErrorCode.MAVEN_COMPILE_ERROR, result.getExecutionException().getMessage());
            }
        } catch (MavenInvocationException e) {
            throw CustomException.builder()
                    .errorCode(ErrorCode.MAVEN_COMPILE_ERROR)
                    .message("Failed to compile code, due to " + e.getMessage())
                    .exception(e)
                    .build();
        }
    }
}
