package com.xchen.heimdall.devtools.service.app.service;

import com.alibaba.fastjson.JSON;
import com.xchen.heimdall.devtools.service.app.DmtServiceApp;
import com.xchen.heimdall.devtools.service.app.dto.ErrorCodeConfDTO;
import com.xchen.heimdall.devtools.service.app.vo.ErrorCodeConfVO;
import com.xchen.heimdall.devtools.service.app.vo.qo.ErrorCodeConfQO;
import com.xchen.heimdall.dubbo.support.wrapper.PagedWrapper;
import com.xchen.heimdall.dubbo.support.wrapper.PagingWrapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author zhuzhou
 */
@Slf4j
@SpringBootTest(classes = {DmtServiceApp.class})
class ErrorCodeServiceTest {

    @Resource
    private ErrorCodeConfigService errorCodeConfigService;


    @Test
    @Transactional
    void testAddErrorCode() {
        ErrorCodeConfVO vo = ErrorCodeConfVO.builder()
                .errorCode("100142003")
                .projectId(14)
                .level(1)
                .displayMessage("错误提示文案33")
                .build();
        errorCodeConfigService.addErrorCode(vo);

        PagedWrapper<ErrorCodeConfDTO> errorCodeList = errorCodeConfigService.queryErrorCodeConfig(new PagingWrapper<>(
                ErrorCodeConfQO.builder().projectId(14).build(),
                1, 10
        ));
        log.info("List result: {}", JSON.toJSONString(errorCodeList));

    }

    @Test
    @Transactional
    void testUpdateErrorCode() {
        ErrorCodeConfVO vo = ErrorCodeConfVO.builder()
                .errorCode("100122002")
                .projectId(5)
                .level(1)
                .displayMessage("错误提示文案22-6")
                .build();
        errorCodeConfigService.updateErrorCode(vo);

    }

    @Test
    void testPublishErrorCode() {
        ErrorCodeConfQO qo = ErrorCodeConfQO.builder()
                .projectId(15)
                .build();
        errorCodeConfigService.publishConfig(qo);

        PagedWrapper<ErrorCodeConfDTO> errorCodeList = errorCodeConfigService.queryErrorCodeConfig(new PagingWrapper<>(
                ErrorCodeConfQO.builder().projectId(15).build(),
                1, 10
        ));
        errorCodeList.getData().forEach(
                a -> {
                    log.info("List result: {}", JSON.toJSONString(a));
                }
        );


    }

    @Test
    void testQueryErrorCode() {

        PagedWrapper<ErrorCodeConfDTO> errorCodeList = errorCodeConfigService.queryErrorCodeConfig(new PagingWrapper<>(
                ErrorCodeConfQO.builder().projectId(14).build(),
                1, 10
        ));
        log.info("List result: {}", JSON.toJSONString(errorCodeList));

    }

    @Test
    void testGenerateErrorCode() {
        ErrorCodeConfQO qo = ErrorCodeConfQO.builder()
                .projectId(15).level(1)
                .build();
        ErrorCodeConfDTO errorCode = errorCodeConfigService.generateErrorCode(qo);
        log.info("errorCode result: {}", JSON.toJSONString(errorCode));
    }


}