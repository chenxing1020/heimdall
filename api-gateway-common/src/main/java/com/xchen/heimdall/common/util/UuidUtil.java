package com.xchen.heimdall.common.util;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * @author xchen
 */
public class UuidUtil {

    private static final Pattern PATTERN = Pattern.compile("-");

    private UuidUtil() {
    }

    /**
     * 生成无横杠的UUID
     *
     * @return uuid
     */
    public static String generate() {
        return PATTERN.matcher(UUID.randomUUID().toString()).replaceAll("");
    }

}
