USE devtools_db;
CREATE TABLE project
(
    `id`             INT(11)       NOT NULL AUTO_INCREMENT,
    `project_name`   VARCHAR(32)   NOT NULL DEFAULT '' COMMENT '工程名称',
    `project_type`   INT(11)       NOT NULL DEFAULT 1  COMMENT '工程类型：service-1，gateway-2，proxy-3，sdk-4，job-5，web-6，client-7，client-8',
    `project_desc`   VARCHAR(1024) NOT NULL DEFAULT '' COMMENT '工程描述',
    `owner_user_id`  VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '责任人',
    `create_user_id` VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '创建人',
    `update_user_id` VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '更新人',
    `delete_time`    DATETIME      NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '删除时间，默认表示未删除',
    `db_create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `db_update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_project_name` (`project_name`, `delete_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='工程定义';

CREATE TABLE dubbo_service
(
    `id`                  INT(11)      NOT NULL AUTO_INCREMENT,
    `project_id`          INT(11)      NOT NULL DEFAULT 0  COMMENT '工程id',
    `service_name`        VARCHAR(32)  NOT NULL DEFAULT 0  COMMENT 'service名称',
    `simple_service_name` VARCHAR(32)  NOT NULL DEFAULT 0  COMMENT 'service简称',
    `service_path`        VARCHAR(128) NOT NULL DEFAULT '' COMMENT '接口路径',
    `registry_zk_cluster` INT(11)      NOT NULL DEFAULT 0  COMMENT '注册中心zk集群: 1 business集群, 2 投管, 3 heimdall',
    `provider_group`      VARCHAR(32)  NOT NULL DEFAULT '' COMMENT '接口分组',
    `provider_version`    VARCHAR(8)   NOT NULL DEFAULT '' COMMENT '接口版本',
    `timeout`             INT(11)      NOT NULL DEFAULT 0  COMMENT '接口超时时间，单位ms',
    `owner_user_id`       VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '创建人',
    `create_user_id`      VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '创建人',
    `update_user_id`      VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '更新人',
    `delete_time`         DATETIME     NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '删除时间，默认表示未删除',
    `db_create_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `db_update_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_service_name` (`service_name`, `delete_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='dubbo接口服务';

CREATE TABLE dubbo_method
(
    `id`                 INT(11)       NOT NULL AUTO_INCREMENT,
    `service_id`         INT(11)       NOT NULL DEFAULT 0 COMMENT '服务id',
    `method_name`        VARCHAR(32)   NOT NULL DEFAULT '' COMMENT '方法名称',
    `vo_id`              INT(11)       NOT NULL DEFAULT 0 COMMENT 'vo pojo id',
    `vo_wrapper_type`    INT(11)       NOT NULL DEFAULT 1 COMMENT 'vo包装类型',
    `dto_id`             INT(11)       NOT NULL DEFAULT 0 COMMENT 'dto pojo id',
    `dto_wrapper_type`   INT(11)       NOT NULL DEFAULT 1 COMMENT 'dto包装类型',
    `api_desc`           VARCHAR(32)   NOT NULL DEFAULT '' COMMENT ' ',
    `api_remark`         VARCHAR(1024) NOT NULL DEFAULT '' COMMENT '接口备注',
    `method_exception`   VARCHAR(32)   DEFAULT NULL COMMENT '接口异常',
    `code_status`        TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '代码同步状态：0-未同步，1-已同步',
    `gateway_api_status` TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '网关接口生成状态： 0-未生成，1-已生成',
    `owner_user_id`      VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '责任人',
    `create_user_id`     VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '创建人',
    `update_user_id`     VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '更新人',
    `delete_time`        DATETIME      NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '删除时间，默认表示未删除',
    `db_create_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `db_update_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_method_name_service_id` (`method_name`, `service_id`, `delete_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='dubbo方法';

CREATE TABLE pojo
(
    `id`                    INT(11)         NOT NULL AUTO_INCREMENT,
    `project_id`            INT(11)         NOT NULL DEFAULT 0 COMMENT '项目id',
    `pojo_name`             VARCHAR(32)     NOT NULL DEFAULT '' COMMENT 'pojo名称',
    `pojo_type`             TINYINT(4)      NOT NULL DEFAULT 0 COMMENT 'POJO类型：0-VO；1-DTO',
    `pojo_desc`             VARCHAR(32)     NOT NULL DEFAULT '' COMMENT 'pojo描述',
    `pojo_path`             VARCHAR(128)    NOT NULL DEFAULT '' COMMENT 'pojo路径',
    `parent_id`             INT(11)         DEFAULT NULL COMMENT '父类id',
    `field_list`            TEXT            NOT NULL DEFAULT '' COMMENT '字段列表',
    `owner_user_id`         VARCHAR(64)     NOT NULL DEFAULT '' COMMENT '责任人',
    `create_user_id`        VARCHAR(64)     NOT NULL DEFAULT '' COMMENT '创建人',
    `update_user_id`        VARCHAR(64)     NOT NULL DEFAULT '' COMMENT '更新人',
    `delete_time`           DATETIME        NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '删除时间，默认表示未删除',
    `db_create_time`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `db_update_time`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pojo_name` (`pojo_name`, `project_id`, `delete_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT = 'POJO实体';

CREATE TABLE gateway_api
(
    `id`                    INT(11)         NOT NULL AUTO_INCREMENT,
    `service_id`            INT(11)         NOT NULL DEFAULT 0 COMMENT '服务id',
    `method_id`             INT(11)         NOT NULL DEFAULT 0 COMMENT '方法id',
    `login_required`        TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '是否需要登录态：0-不需要；1-需要',
    `upstream_channel_type` INT(11)         NOT NULL DEFAULT 0 COMMENT '后端通道类型',
    `synced`                TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '接口是否同步: 0-未同步; 1-已同步',
    `remark`                VARCHAR(1024)   NOT NULL DEFAULT '' COMMENT '接口备注',
    `owner_user_id`         VARCHAR(64)     NOT NULL DEFAULT '' COMMENT '责任人',
    `create_user_id`        VARCHAR(64)     NOT NULL DEFAULT '' COMMENT '创建人',
    `update_user_id`        VARCHAR(64)     NOT NULL DEFAULT '' COMMENT '更新人',
    `access_point_list`     VARCHAR(64)      NOT NULL DEFAULT '' COMMENT '接入点',
    `delete_time`           DATETIME        NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '删除时间，默认表示未删除',
    `db_create_time`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `db_update_time`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_gateway_api` (`service_id`, `method_id`, `delete_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT = '网关api';

CREATE TABLE gateway_api_permission
(
    `id`             INT(11)     NOT NULL AUTO_INCREMENT,
    `gateway_api_id` INT(11)     NOT NULL DEFAULT 0 COMMENT '网关api id',
    `permission_id`  INT(11)     NOT NULL DEFAULT 0 COMMENT '权限id',
    `db_create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `db_update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_gateway_api_id_permission_id` (`gateway_api_id`, `permission_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='网关api访问权限';
