package com.xchen.heimdall.api.gateway.app.manager;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author by xchen
 * @since 2023/3/4.
 */
@Component
public class PermissionDataManager {

    private static final ConcurrentHashMap<String, List<String>> USER_PERMISSION_MAP = new ConcurrentHashMap<>();

    @PostConstruct
    public void postInit() {
        // init
    }

    public boolean userHasAnyPermission(String userId, List<String> permissionKeyList) {
        // XXX: 自定义用户和permissionKey的关系，可通过复杂角色来定义
        List<String> userPermissions = USER_PERMISSION_MAP.get(userId);

        if (CollectionUtils.isNotEmpty(userPermissions)) {
            Optional<String> permissionKey = USER_PERMISSION_MAP.get(userId)
                    .stream()
                    .filter(permissionKeyList::contains)
                    .findAny();
            return permissionKey.isPresent();
        }
        return false;
    }
}
