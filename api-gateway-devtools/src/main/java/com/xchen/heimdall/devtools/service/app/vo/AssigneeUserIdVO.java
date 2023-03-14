package com.xchen.heimdall.devtools.service.app.vo;

import lombok.*;

/**
 * @author xchen
 * @date 2022/7/1
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AssigneeUserIdVO extends UserIdVO {

    private String assigneeUserId;
}
