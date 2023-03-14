package com.xchen.heimdall.devtools.service.app.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GatewayApiDocDTO extends GatewayApiDTO {

    /**
     * 入参
     */
    private PojoDTO inputParam;

    /**
     * 出参
     */
    private PojoDTO returnParam;
}
