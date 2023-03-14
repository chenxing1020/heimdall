package com.xchen.heimdall.devtools.service.app.service;

import com.xchen.heimdall.devtools.service.app.DmtServiceApp;
import com.xchen.heimdall.devtools.service.app.utils.CommonUtils;
import org.apache.commons.lang3.RegExUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author xchen
 * @date 2022/4/27
 */
@SpringBootTest(classes = {DmtServiceApp.class}, properties = "spring.profiles.active=test")
class CommonUtilsTest {

    @Resource
    private CommonUtils commonUtils;

    @Test
    void testServicePath() {
        Assertions.assertEquals("com.xchen.heimdall.dubbo.api.otc.bff.service",
                commonUtils.getProjectPath("otc-bff-service"));
    }

    @Test
    void testFormatProjectName() {
        Assertions.assertEquals("app.service", commonUtils.formatProjectName("app-service"));

    }

    @Test
    void testRegex() {
        String originText = "abcxx";
        String replacement = "abcd";
        Assertions.assertEquals(originText,
                RegExUtils.replaceAll(originText, "\\babc\\b", replacement));

        originText = "xxabc";
        Assertions.assertEquals(originText,
                RegExUtils.replaceAll(originText, "\\babc\\b", replacement));

        originText = "xxabcxx";
        Assertions.assertEquals(originText,
                RegExUtils.replaceAll(originText, "\\babc\\b", replacement));

        originText = "abc";
        Assertions.assertEquals("abcd",
                RegExUtils.replaceAll(originText, "\\babc\\b", replacement));

        originText = "Abc";
        Assertions.assertEquals(originText,
                RegExUtils.replaceAll(originText, "\\babc\\b", replacement));

        originText = "Map<abc, abc>";
        Assertions.assertEquals("Map<abcd, abcd>",
                RegExUtils.replaceAll(originText, "\\babc\\b", replacement));

        originText = "abc_xx";
        Assertions.assertEquals(originText,
                RegExUtils.replaceAll(originText, "\\babc\\b", replacement));

        originText = "abc_xx";
        Assertions.assertEquals(replacement,
                RegExUtils.replaceAll(originText, "\\babc_xx\\b", replacement));

        originText = "Map<abc_xx, ss>";
        Assertions.assertEquals("Map<abcd, ss>",
                RegExUtils.replaceAll(originText, "\\babc_xx\\b", replacement));

        originText = "abc1";
        Assertions.assertEquals(originText,
                RegExUtils.replaceAll(originText, "\\babc\\b", replacement));

        originText = "abc_1";
        Assertions.assertEquals(originText,
                RegExUtils.replaceAll(originText, "\\babc\\b", replacement));

        originText = "abc_1";
        Assertions.assertEquals("abcd",
                RegExUtils.replaceAll(originText, "\\babc_1\\b", replacement));
    }
}
