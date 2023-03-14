package com.xchen.heimdall.devtools.service.app.vo;

import lombok.*;

/**
 * @author xchen
 * @date 2022/5/23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GatewayApiIdVO extends UserIdVO {

    private Integer gatewayApiId;
}
