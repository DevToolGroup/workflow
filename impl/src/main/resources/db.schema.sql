-- 数据库
CREATE
DATABASE `workflow` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

-- 流程定义相关表结构
-- 流程定义
CREATE TABLE `wf_definition`
(
    `id`        int unsigned NOT NULL AUTO_INCREMENT COMMENT '数据库主键',
    `code`      varchar(64)  NOT NULL COMMENT '流程定义编码',
    `name`      varchar(255) NOT NULL COMMENT '流程定义名称',
    `version`   int unsigned NOT NULL COMMENT '流程定义版本',
    `state`     varchar(1)   NOT NULL COMMENT '流程定义状态',
    `node_code` varchar(64) DEFAULT NULL COMMENT '父流程节点定义编码',
    `root_code` varchar(64)  NOT NULL COMMENT '根流程定义编码',
    PRIMARY KEY (`id`),
    UNIQUE KEY `ix_definition_code_version` (`root_code`, `code`,`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 流程节点定义
CREATE TABLE `wf_node_definition`
(
    `id`                   int unsigned NOT NULL AUTO_INCREMENT COMMENT '数据库主键',
    `code`                 varchar(64) NOT NULL COMMENT '流程节点定义编码',
    `name`                 varchar(64) NOT NULL COMMENT '流程节点定义名称',
    `type`                 varchar(16) NOT NULL COMMENT '流程节点定义类型',
    `config`               blob COMMENT '流程节点定义配置',
    `version`              int unsigned NOT NULL COMMENT '流程节点定义关联的流程定义版本',
    `definition_code`      varchar(64) NOT NULL COMMENT '流程节点定义关联的流程定义编码',
    `root_definition_code` varchar(64) NOT NULL COMMENT '根流程定义编码',
    PRIMARY KEY (`id`),
    KEY                    `ix_node_definition_code_version` (`root_definition_code`, `definition_code`,`version`) USING BTREE,
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 流程连线定义
CREATE TABLE `wf_link_definition`
(
    `id`                   int unsigned NOT NULL AUTO_INCREMENT,
    `code`                 varchar(64) NOT NULL COMMENT '连线编码',
    `source`               varchar(64) NOT NULL COMMENT '连线起始节点定义编码',
    `target`               varchar(64) NOT NULL COMMENT '连线目标节点定义编码',
    `parser`               varchar(32) NOT NULL COMMENT '连线表达式解析器类型',
    `expression`           varchar(3000) COMMENT '连线条件表达式',
    `version`              int unsigned NOT NULL COMMENT '连线关联的流程定义的版本',
    `definition_code`      varchar(64) NOT NULL COMMENT '连线关联的流程定义的编码',
    `root_definition_code` varchar(64) NOT NULL COMMENT '连线关联的根流程定义的编码',
    PRIMARY KEY (`id`),
    KEY                    `ix_link_definition_code_version` (`root_definition_code`, `definition_code`,`version`) USING BTREE,
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 流程实例相关表结构
-- 流程实例
CREATE TABLE `wf_instance`
(
    `id`                   int unsigned NOT NULL AUTO_INCREMENT COMMENT '数据库主键',
    `instance_id`          varchar(64) NOT NULL COMMENT '流程实例ID',
    `state`                varchar(8)  NOT NULL COMMENT '流程实例状态',
    `scope`                varchar(8)  NOT NULL COMMENT '流程实例范围 ROOT，CHILD，FORK',
    `root_instance_id`     varchar(64) NOT NULL COMMENT '根流程实例ID',
    `parent_id`            varchar(64) DEFAULT NULL COMMENT '父实体ID，如果是嵌套子流程，则为子流程节点的任务ID，如果是分支子流程，则为分支子流程的父流程实例ID',
    `definition_code`      varchar(64) NOT NULL COMMENT '关联流程定义的编码',
    `root_definition_code` varchar(64) NOT NULL COMMENT '流程定义的编码',
    `definition_version`   int unsigned NOT NULL COMMENT '关联的流程定义版本',
    PRIMARY KEY (`id`),
    UNIQUE KEY `ix_instance_id` (`instance_id`) USING BTREE,
    KEY                    `ix_root_instance_id` (`root_instance_id`,`instance_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 流程节点
CREATE TABLE `wf_instance_node`
(
    `id`               int unsigned NOT NULL AUTO_INCREMENT COMMENT '数据库主键',
    `node_id`          varchar(64) NOT NULL COMMENT '流程节点实例关联的流程节点ID',
    `node_code`        varchar(64) NOT NULL COMMENT '流程节点实例关联的流程节点编码',
    `node_state`       varchar(8)  NOT NULL COMMENT '流程节点实例关联的节点状态',
    `node_class`       varchar(16) NOT NULL COMMENT '流程节点实例关联的节点类型',
    `config`           varchar(5000) COMMENT '流程节点实例配置',
    `version`          int unsigned NOT NULL default 0 COMMENT '流程节点实例版本',
    `instance_id`      varchar(64) NOT NULL COMMENT '流程节点实例关联的流程实例ID',
    `root_instance_id` varchar(64) NOT NULL COMMENT '流程节点实例关联的根流程实例ID',
    PRIMARY KEY (`id`),
    KEY                `ix_root_instance_id_node_code` (`root_instance_id`,`node_code`) USING BTREE,
    KEY                `ix_root_node_instance_id_node_id` (`root_instance_id`,`node_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 流程任务
CREATE TABLE `wf_instance_task`
(
    `id`               int unsigned NOT NULL AUTO_INCREMENT COMMENT '数据库主键',
    `task_id`          varchar(64) NOT NULL COMMENT '流程任务实例ID',
    `task_class`       varchar(16) NOT NULL COMMENT '流程任务实例类型',
    `task_state`       varchar(8)  NOT NULL COMMENT '流程任务实例状态',
    `complete_user`    varchar(255) DEFAULT NULL COMMENT '流程任务实例完成用户',
    `complete_time`    bigint unsigned DEFAULT NULL COMMENT '流程任务实例完成时间',
    `config`           varchar(5000) COMMENT '流程任务实例配置',
    `node_id`          varchar(64) NOT NULL COMMENT '流程任务实例关联的流程节点ID',
    `node_code`        varchar(64) NOT NULL COMMENT '流程任务实例关联的流程节点ID',
    `instance_id`      varchar(64) NOT NULL COMMENT '流程任务实例关联的流程实例ID',
    `root_instance_id` varchar(64) NOT NULL COMMENT '流程任务实例关联的根流程实例ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `ix_task_id` (`task_id`),
    KEY                `ix_root_instance_id_node_id` (`root_instance_id`,`node_id`) USING BTREE,
    KEY                `ix_root_instance_id_task_id` (`root_instance_id`,`task_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 流程延迟任务
CREATE TABLE `wf_instance_delay_task`
(
    `id`               int unsigned NOT NULL AUTO_INCREMENT COMMENT '数据库主键',
    `item_id`          varchar(64) NOT NULL COMMENT '流程延时任务ID',
    `delay`            BIGINT      NOT NULL COMMENT '流程延时任务时间',
    `state`            varchar(8)  NOT NULL COMMENT '流程延时任务状态',
    `task_id`          varchar(64) NOT NULL COMMENT '流程任务实例ID',
    `root_instance_id` varchar(64) NOT NULL COMMENT '流程任务实例关联的根流程实例ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `ix_delay_task_id` (`task_id`),
    KEY                `ix_root_instance_id_item_id` (`root_instance_id`,`item_id`) USING BTREE,
    KEY                `ix_delay_state` (`delay`,`state`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- 流程变量
CREATE TABLE `wf_instance_variable`
(
    `id`               int unsigned NOT NULL AUTO_INCREMENT COMMENT '数据库主键',
    `name`             varchar(255)  NOT NULL COMMENT '变量名',
    `value`            varchar(5000) NOT NULL COMMENT '变量值',
    `type`             varchar(6)    NOT NULL COMMENT '变量类型 GLOBAL：全局，LOCAL：局部',
    `root_instance_id` varchar(64)   NOT NULL COMMENT '变量关联的根流程实例ID',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- 流程操作
CREATE TABLE `wf_operation`
(
    `id`               int unsigned NOT NULL AUTO_INCREMENT COMMENT '数据库主键',
    `code`             varchar(64) NOT NULL COMMENT '流程操作唯一编码',
    `status`           int         NOT NULL COMMENT '流程操作状态',
    `type`             varchar(8)  NOT NULL COMMENT '流程操作类型. START 开始、 STOP 停止、 RUN 运行、CHILD 子流程、 NEXT 流转',
    `root_instance_id` varchar(64) NOT NULL COMMENT '根流程实例ID',
    `instance_id`      varchar(64) NOT NULL COMMENT '流程实例ID',
    `context`          json        NOT NULL COMMENT '流程上下文',
    `task_id`          varchar(64) NULL COMMENT '任务实例ID',
    `node_id`          varchar(64) NULL COMMENT '节点实例ID',
    `children`         varchar(256) NULL COMMENT '子流程实例ID',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 流程回调
CREATE TABLE `wf_callback_queue`
(
    `id`               int unsigned NOT NULL AUTO_INCREMENT COMMENT '数据库主键',
    `code`             varchar(64) NOT NULL COMMENT '回调唯一编码',
    `status`           int         NOT NULL COMMENT '回调状态 0 待运行 1 运行成功 -1 运行失败',
    `event`            varchar(8)  NOT NULL COMMENT '回调事件. START 流程启动事件、 CREATED 节点创建事件、 COMPLETE 节点完成事件、END 流程或子流程结束事件、 STOP 流程停止事件',
    `root_instance_id` varchar(64) NOT NULL COMMENT '根流程实例ID',
    `context`          json        NOT NULL COMMENT '流程上下文',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;