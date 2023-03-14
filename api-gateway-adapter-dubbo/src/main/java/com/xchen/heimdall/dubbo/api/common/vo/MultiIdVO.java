package com.xchen.heimdall.dubbo.api.common.vo;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xchen
 * @since 2022/7/18 19:49
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultiIdVO extends UserIdVO {
    private List<Integer> ids;
}
