DROP TABLE if EXISTS `user`;
CREATE TABLE `user`
(
    `id`          varchar(64)  NOT NULL,
    `account`     varchar(64)  NOT NULL COMMENT '用户账号',
    `name`        varchar(200) NOT NULL COMMENT '用户名',
    `password`    varchar(200) NOT NULL COMMENT '密码',
    `sex`         varchar(64)  default NULL COMMENT '性别',
    `phone`       varchar(64)  default NULL COMMENT '手机号',
    `email`       varchar(200) default NULL COMMENT '邮箱',
    `portrait`    text         default NULL COMMENT '头像',
    `status`      varchar(500) COMMENT '用户状态',
    `create_time` bigint       DEFAULT 0 COMMENT '创建时间',
    `update_time` bigint       DEFAULT 0 COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表' row_format=dynamic;


DROP TABLE if EXISTS `room`;
CREATE TABLE `room`
(
    `id`          varchar(64)  NOT NULL,
    `number`      varchar(64)  NOT NULL COMMENT '房间号',
    `name`        varchar(200) NOT NULL COMMENT '房间名称',
    `type`        varchar(64)  NOT NULL COMMENT '房间类型',
    `password`    varchar(64)  NOT NULL COMMENT '房间密码',
    `status`      varchar(500) COMMENT '房间状态',
    `round`       int    DEFAULT 1 COMMENT '当前轮次',
    `create_time` bigint DEFAULT 0 COMMENT '创建时间',
    `update_time` bigint DEFAULT 0 COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='房间表' row_format=dynamic;

DROP TABLE if EXISTS `user_room`;
CREATE TABLE `user_room`
(
    `id`          varchar(64)  NOT NULL,
    `user_id`     varchar(64)  NOT NULL COMMENT '用户id',
    `room_id`     varchar(200) NOT NULL COMMENT '房间id',
    `score`       int COMMENT '总分',
    `round_score` int COMMENT '本轮次得分',
    `status`      varchar(500) COMMENT '当前状态',
    `is_dealers`  int COMMENT '是否庄家',
    `is_owner`    int COMMENT '是否房主',
    `create_time` bigint DEFAULT 0 COMMENT '创建时间',
    `update_time` bigint DEFAULT 0 COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户房间表' row_format=dynamic;
