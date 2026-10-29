/*
 Navicat Premium Dump SQL

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80045 (8.0.45)
 Source Host           : localhost:3306
 Source Schema         : ry-vue

 Target Server Type    : MySQL
 Target Server Version : 80045 (8.0.45)
 File Encoding         : 65001

 Date: 29/10/2026 23:19:37
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ai_points_log
-- ----------------------------
DROP TABLE IF EXISTS `ai_points_log`;
CREATE TABLE `ai_points_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `change_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '变动类型: charge充值/consume消耗/refund退还/gift赠送',
  `change_amount` int NOT NULL COMMENT '变动点数（正数增加，负数减少）',
  `balance_after` int NOT NULL COMMENT '变动后余额',
  `source` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '来源: text/voice/ocr_image/recharge/gift/rollback',
  `biz_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '关联业务ID（会话ID/充值单号）',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_tenant_time` (`tenant_id`,`create_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=214 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='AI点数变动流水表';

-- ----------------------------
-- Records of ai_points_log
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for gen_table
-- ----------------------------
DROP TABLE IF EXISTS `gen_table`;
CREATE TABLE `gen_table` (
  `table_id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `table_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '表名称',
  `table_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '表描述',
  `sub_table_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '关联子表的表名',
  `sub_table_fk_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '子表关联的外键名',
  `class_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '实体类名称',
  `tpl_category` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'crud' COMMENT '使用的模板（crud单表操作 tree树表操作）',
  `tpl_web_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '前端模板类型（element-ui模版 element-plus模版）',
  `package_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '生成包路径',
  `module_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '生成模块名',
  `business_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '生成业务名',
  `function_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '生成功能名',
  `function_author` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '生成功能作者',
  `gen_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '生成代码方式（0zip压缩包 1自定义路径）',
  `gen_path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '/' COMMENT '生成路径（不填默认项目路径）',
  `options` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '其它生成选项',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`table_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='代码生成业务表';

-- ----------------------------
-- Records of gen_table
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for gen_table_column
-- ----------------------------
DROP TABLE IF EXISTS `gen_table_column`;
CREATE TABLE `gen_table_column` (
  `column_id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `table_id` bigint DEFAULT NULL COMMENT '归属表编号',
  `column_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '列名称',
  `column_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '列描述',
  `column_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '列类型',
  `java_type` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'JAVA类型',
  `java_field` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'JAVA字段名',
  `is_pk` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '是否主键（1是）',
  `is_increment` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '是否自增（1是）',
  `is_required` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '是否必填（1是）',
  `is_insert` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '是否为插入字段（1是）',
  `is_edit` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '是否编辑字段（1是）',
  `is_list` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '是否列表字段（1是）',
  `is_query` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '是否查询字段（1是）',
  `query_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'EQ' COMMENT '查询方式（等于、不等于、大于、小于、范围）',
  `html_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）',
  `dict_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '字典类型',
  `sort` int DEFAULT NULL COMMENT '排序',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`column_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='代码生成业务表字段';

-- ----------------------------
-- Records of gen_table_column
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for qrtz_blob_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_blob_triggers`;
CREATE TABLE `qrtz_blob_triggers` (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `blob_data` blob COMMENT '存放持久化Trigger对象',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`) USING BTREE,
  CONSTRAINT `qrtz_blob_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='Blob类型的触发器表';

-- ----------------------------
-- Records of qrtz_blob_triggers
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for qrtz_calendars
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_calendars`;
CREATE TABLE `qrtz_calendars` (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '调度名称',
  `calendar_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '日历名称',
  `calendar` blob NOT NULL COMMENT '存放持久化calendar对象',
  PRIMARY KEY (`sched_name`,`calendar_name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='日历信息表';

-- ----------------------------
-- Records of qrtz_calendars
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for qrtz_cron_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_cron_triggers`;
CREATE TABLE `qrtz_cron_triggers` (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `cron_expression` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'cron表达式',
  `time_zone_id` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '时区',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`) USING BTREE,
  CONSTRAINT `qrtz_cron_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='Cron类型的触发器表';

-- ----------------------------
-- Records of qrtz_cron_triggers
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for qrtz_fired_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_fired_triggers`;
CREATE TABLE `qrtz_fired_triggers` (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '调度名称',
  `entry_id` varchar(95) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '调度器实例id',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `instance_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '调度器实例名',
  `fired_time` bigint NOT NULL COMMENT '触发的时间',
  `sched_time` bigint NOT NULL COMMENT '定时器制定的时间',
  `priority` int NOT NULL COMMENT '优先级',
  `state` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '状态',
  `job_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '任务名称',
  `job_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '任务组名',
  `is_nonconcurrent` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '是否并发',
  `requests_recovery` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '是否接受恢复执行',
  PRIMARY KEY (`sched_name`,`entry_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='已触发的触发器表';

-- ----------------------------
-- Records of qrtz_fired_triggers
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for qrtz_job_details
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_job_details`;
CREATE TABLE `qrtz_job_details` (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '调度名称',
  `job_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务名称',
  `job_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务组名',
  `description` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '相关介绍',
  `job_class_name` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '执行任务类名称',
  `is_durable` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '是否持久化',
  `is_nonconcurrent` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '是否并发',
  `is_update_data` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '是否更新数据',
  `requests_recovery` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '是否接受恢复执行',
  `job_data` blob COMMENT '存放持久化job对象',
  PRIMARY KEY (`sched_name`,`job_name`,`job_group`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='任务详细信息表';

-- ----------------------------
-- Records of qrtz_job_details
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for qrtz_locks
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_locks`;
CREATE TABLE `qrtz_locks` (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '调度名称',
  `lock_name` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '悲观锁名称',
  PRIMARY KEY (`sched_name`,`lock_name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='存储的悲观锁信息表';

-- ----------------------------
-- Records of qrtz_locks
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for qrtz_paused_trigger_grps
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_paused_trigger_grps`;
CREATE TABLE `qrtz_paused_trigger_grps` (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '调度名称',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  PRIMARY KEY (`sched_name`,`trigger_group`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='暂停的触发器表';

-- ----------------------------
-- Records of qrtz_paused_trigger_grps
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for qrtz_scheduler_state
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_scheduler_state`;
CREATE TABLE `qrtz_scheduler_state` (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '调度名称',
  `instance_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '实例名称',
  `last_checkin_time` bigint NOT NULL COMMENT '上次检查时间',
  `checkin_interval` bigint NOT NULL COMMENT '检查间隔时间',
  PRIMARY KEY (`sched_name`,`instance_name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='调度器状态表';

-- ----------------------------
-- Records of qrtz_scheduler_state
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for qrtz_simple_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_simple_triggers`;
CREATE TABLE `qrtz_simple_triggers` (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `repeat_count` bigint NOT NULL COMMENT '重复的次数统计',
  `repeat_interval` bigint NOT NULL COMMENT '重复的间隔时间',
  `times_triggered` bigint NOT NULL COMMENT '已经触发的次数',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`) USING BTREE,
  CONSTRAINT `qrtz_simple_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='简单触发器的信息表';

-- ----------------------------
-- Records of qrtz_simple_triggers
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for qrtz_simprop_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_simprop_triggers`;
CREATE TABLE `qrtz_simprop_triggers` (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `str_prop_1` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'String类型的trigger的第一个参数',
  `str_prop_2` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'String类型的trigger的第二个参数',
  `str_prop_3` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'String类型的trigger的第三个参数',
  `int_prop_1` int DEFAULT NULL COMMENT 'int类型的trigger的第一个参数',
  `int_prop_2` int DEFAULT NULL COMMENT 'int类型的trigger的第二个参数',
  `long_prop_1` bigint DEFAULT NULL COMMENT 'long类型的trigger的第一个参数',
  `long_prop_2` bigint DEFAULT NULL COMMENT 'long类型的trigger的第二个参数',
  `dec_prop_1` decimal(13,4) DEFAULT NULL COMMENT 'decimal类型的trigger的第一个参数',
  `dec_prop_2` decimal(13,4) DEFAULT NULL COMMENT 'decimal类型的trigger的第二个参数',
  `bool_prop_1` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'Boolean类型的trigger的第一个参数',
  `bool_prop_2` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'Boolean类型的trigger的第二个参数',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`) USING BTREE,
  CONSTRAINT `qrtz_simprop_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='同步机制的行锁表';

-- ----------------------------
-- Records of qrtz_simprop_triggers
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for qrtz_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_triggers`;
CREATE TABLE `qrtz_triggers` (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '触发器的名字',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '触发器所属组的名字',
  `job_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'qrtz_job_details表job_name的外键',
  `job_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'qrtz_job_details表job_group的外键',
  `description` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '相关介绍',
  `next_fire_time` bigint DEFAULT NULL COMMENT '上一次触发时间（毫秒）',
  `prev_fire_time` bigint DEFAULT NULL COMMENT '下一次触发时间（默认为-1表示不触发）',
  `priority` int DEFAULT NULL COMMENT '优先级',
  `trigger_state` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '触发器状态',
  `trigger_type` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '触发器的类型',
  `start_time` bigint NOT NULL COMMENT '开始时间',
  `end_time` bigint DEFAULT NULL COMMENT '结束时间',
  `calendar_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '日程表名称',
  `misfire_instr` smallint DEFAULT NULL COMMENT '补偿执行的策略',
  `job_data` blob COMMENT '存放持久化job对象',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`) USING BTREE,
  KEY `sched_name` (`sched_name`,`job_name`,`job_group`) USING BTREE,
  CONSTRAINT `qrtz_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `job_name`, `job_group`) REFERENCES `qrtz_job_details` (`sched_name`, `job_name`, `job_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='触发器详细信息表';

-- ----------------------------
-- Records of qrtz_triggers
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
  `config_id` int NOT NULL AUTO_INCREMENT COMMENT '参数主键',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  `config_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '参数名称',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '参数键名',
  `config_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '参数键值',
  `config_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'N' COMMENT '系统内置（Y是 N否）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`config_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=109 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='参数配置表';

-- ----------------------------
-- Records of sys_config
-- ----------------------------
BEGIN;
INSERT INTO `sys_config` (`config_id`, `tenant_id`, `config_name`, `config_key`, `config_value`, `config_type`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1, 1, '主框架页-默认皮肤样式名称', 'sys.index.skinName', 'skin-blue', 'Y', 'admin', '2026-01-23 11:53:06', '', NULL, '蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow');
INSERT INTO `sys_config` (`config_id`, `tenant_id`, `config_name`, `config_key`, `config_value`, `config_type`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2, 1, '用户管理-账号初始密码', 'sys.user.initPassword', '123456', 'Y', 'admin', '2026-01-23 11:53:06', '', NULL, '初始化密码 123456');
INSERT INTO `sys_config` (`config_id`, `tenant_id`, `config_name`, `config_key`, `config_value`, `config_type`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3, 1, '主框架页-侧边栏主题', 'sys.index.sideTheme', 'theme-dark', 'Y', 'admin', '2026-01-23 11:53:06', '', NULL, '深色主题theme-dark，浅色主题theme-light');
INSERT INTO `sys_config` (`config_id`, `tenant_id`, `config_name`, `config_key`, `config_value`, `config_type`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4, 1, '账号自助-验证码开关', 'sys.account.captchaEnabled', 'true', 'Y', 'admin', '2026-01-23 11:53:06', '', NULL, '是否开启验证码功能（true开启，false关闭）');
INSERT INTO `sys_config` (`config_id`, `tenant_id`, `config_name`, `config_key`, `config_value`, `config_type`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5, 1, '账号自助-是否开启用户注册功能', 'sys.account.registerUser', 'true', 'Y', 'admin', '2026-01-23 11:53:06', '', NULL, '是否开启注册用户功能（true开启，false关闭）');
INSERT INTO `sys_config` (`config_id`, `tenant_id`, `config_name`, `config_key`, `config_value`, `config_type`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (6, 1, '用户登录-黑名单列表', 'sys.login.blackIPList', '', 'Y', 'admin', '2026-01-23 11:53:06', '', NULL, '设置登录IP黑名单限制，多个匹配项以;分隔，支持匹配（*通配、网段）');
INSERT INTO `sys_config` (`config_id`, `tenant_id`, `config_name`, `config_key`, `config_value`, `config_type`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (7, 1, '用户管理-初始密码修改策略', 'sys.account.initPasswordModify', '1', 'Y', 'admin', '2026-01-23 11:53:06', '', NULL, '0：初始密码修改策略关闭，没有任何提示，1：提醒用户，如果未修改初始密码，则在登录时就会提醒修改密码对话框');
INSERT INTO `sys_config` (`config_id`, `tenant_id`, `config_name`, `config_key`, `config_value`, `config_type`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (8, 1, '用户管理-账号密码更新周期', 'sys.account.passwordValidateDays', '0', 'Y', 'admin', '2026-01-23 11:53:06', '', NULL, '密码更新周期（填写数字，数据初始化值为0不限制，若修改必须为大于0小于365的正整数），如果超过这个周期登录系统时，则在登录时就会提醒修改密码对话框');
COMMIT;

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept` (
  `dept_id` bigint NOT NULL AUTO_INCREMENT COMMENT '部门id',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  `parent_id` bigint DEFAULT '0' COMMENT '父部门id',
  `ancestors` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '祖级列表',
  `dept_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '部门名称',
  `order_num` int DEFAULT '0' COMMENT '显示顺序',
  `leader` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '负责人',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '联系电话',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '邮箱',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '部门状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`dept_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_dept_tenant` (`tenant_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=252 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='部门表';

-- ----------------------------
-- Records of sys_dept
-- ----------------------------
BEGIN;
INSERT INTO `sys_dept` (`dept_id`, `tenant_id`, `parent_id`, `ancestors`, `dept_name`, `order_num`, `leader`, `phone`, `email`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (100, 1, 0, '0', '若依科技', 0, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2026-01-23 11:53:05', '', NULL);
INSERT INTO `sys_dept` (`dept_id`, `tenant_id`, `parent_id`, `ancestors`, `dept_name`, `order_num`, `leader`, `phone`, `email`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (101, 1, 100, '0,100', '深圳总公司', 1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2026-01-23 11:53:05', '', NULL);
INSERT INTO `sys_dept` (`dept_id`, `tenant_id`, `parent_id`, `ancestors`, `dept_name`, `order_num`, `leader`, `phone`, `email`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (102, 1, 100, '0,100', '长沙分公司', 2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2026-01-23 11:53:05', '', NULL);
INSERT INTO `sys_dept` (`dept_id`, `tenant_id`, `parent_id`, `ancestors`, `dept_name`, `order_num`, `leader`, `phone`, `email`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (103, 1, 101, '0,100,101', '研发部门', 1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2026-01-23 11:53:05', '', NULL);
INSERT INTO `sys_dept` (`dept_id`, `tenant_id`, `parent_id`, `ancestors`, `dept_name`, `order_num`, `leader`, `phone`, `email`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (104, 1, 101, '0,100,101', '市场部门', 2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2026-01-23 11:53:05', '', NULL);
INSERT INTO `sys_dept` (`dept_id`, `tenant_id`, `parent_id`, `ancestors`, `dept_name`, `order_num`, `leader`, `phone`, `email`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (105, 1, 101, '0,100,101', '测试部门', 3, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2026-01-23 11:53:05', '', NULL);
INSERT INTO `sys_dept` (`dept_id`, `tenant_id`, `parent_id`, `ancestors`, `dept_name`, `order_num`, `leader`, `phone`, `email`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (106, 1, 101, '0,100,101', '财务部门', 4, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2026-01-23 11:53:05', '', NULL);
INSERT INTO `sys_dept` (`dept_id`, `tenant_id`, `parent_id`, `ancestors`, `dept_name`, `order_num`, `leader`, `phone`, `email`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (107, 1, 101, '0,100,101', '运维部门', 5, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2026-01-23 11:53:05', '', NULL);
INSERT INTO `sys_dept` (`dept_id`, `tenant_id`, `parent_id`, `ancestors`, `dept_name`, `order_num`, `leader`, `phone`, `email`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (108, 1, 102, '0,100,102', '市场部门', 1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2026-01-23 11:53:05', '', NULL);
INSERT INTO `sys_dept` (`dept_id`, `tenant_id`, `parent_id`, `ancestors`, `dept_name`, `order_num`, `leader`, `phone`, `email`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (109, 1, 102, '0,100,102', '财务部门', 2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', '2026-01-23 11:53:05', '', NULL);
INSERT INTO `sys_dept` (`dept_id`, `tenant_id`, `parent_id`, `ancestors`, `dept_name`, `order_num`, `leader`, `phone`, `email`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (246, 44, 0, '0', '总公司', 0, 'admin_test', '13900001111', NULL, '0', '0', 'system', '2026-06-25 20:36:52', '', NULL);
COMMIT;

-- ----------------------------
-- Table structure for sys_dict_data
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data` (
  `dict_code` bigint NOT NULL AUTO_INCREMENT COMMENT '字典编码',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  `dict_sort` int DEFAULT '0' COMMENT '字典排序',
  `dict_label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '字典标签',
  `dict_value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '字典键值',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '字典类型',
  `css_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '样式属性（其他样式扩展）',
  `list_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '表格回显样式',
  `is_default` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'N' COMMENT '是否默认（Y是 N否）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_code`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1002 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='字典数据表';

-- ----------------------------
-- Records of sys_dict_data
-- ----------------------------
BEGIN;
INSERT INTO `sys_dict_data` (`dict_code`, `tenant_id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1, 1, 1, '男', '0', 'sys_user_sex', '', '', 'Y', '0', 'admin', '2026-01-23 11:53:06', '', NULL, '性别男');
INSERT INTO `sys_dict_data` (`dict_code`, `tenant_id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2, 1, 2, '女', '1', 'sys_user_sex', '', '', 'N', '0', 'admin', '2026-01-23 11:53:06', '', NULL, '性别女');
INSERT INTO `sys_dict_data` (`dict_code`, `tenant_id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3, 1, 3, '未知', '2', 'sys_user_sex', '', '', 'N', '0', 'admin', '2026-01-23 11:53:06', '', NULL, '性别未知');
INSERT INTO `sys_dict_data` (`dict_code`, `tenant_id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4, 1, 1, '显示', '0', 'sys_show_hide', '', 'primary', 'Y', '0', 'admin', '2026-01-23 11:53:06', '', NULL, '显示菜单');
INSERT INTO `sys_dict_data` (`dict_code`, `tenant_id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5, 1, 2, '隐藏', '1', 'sys_show_hide', '', 'danger', 'N', '0', 'admin', '2026-01-23 11:53:06', '', NULL, '隐藏菜单');
INSERT INTO `sys_dict_data` (`dict_code`, `tenant_id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (6, 1, 1, '正常', '0', 'sys_normal_disable', '', 'primary', 'Y', '0', 'admin', '2026-01-23 11:53:06', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` (`dict_code`, `tenant_id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (7, 1, 2, '停用', '1', 'sys_normal_disable', '', 'danger', 'N', '0', 'admin', '2026-01-23 11:53:06', '', NULL, '停用状态');
INSERT INTO `sys_dict_data` (`dict_code`, `tenant_id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (8, 1, 1, '正常', '0', 'sys_job_status', '', 'primary', 'Y', '0', 'admin', '2026-01-23 11:53:06', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` (`dict_code`, `tenant_id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (9, 1, 2, '暂停', '1', 'sys_job_status', '', 'danger', 'N', '0', 'admin', '2026-01-23 11:53:06', '', NULL, '停用状态');
INSERT INTO `sys_dict_data` (`dict_code`, `tenant_id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (10, 1, 1, '默认', 'DEFAULT', 'sys_job_group', '', '', 'Y', '0', 'admin', '2026-01-23 11:53:06', '', NULL, '默认分组');
INSERT INTO `sys_dict_data` (`dict_code`, `tenant_id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (11, 1, 2, '系统', 'SYSTEM', 'sys_job_group', '', '', 'N', '0', 'admin', '2026-01-23 11:53:06', '', NULL, '系统分组');
INSERT INTO `sys_dict_data` (`dict_code`, `tenant_id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (12, 1, 1, '是', 'Y', 'sys_yes_no', '', 'primary', 'Y', '0', 'admin', '2026-01-23 11:53:06', '', NULL, '系统默认是');
INSERT INTO `sys_dict_data` (`dict_code`, `tenant_id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (13, 1, 2, '否', 'N', 'sys_yes_no', '', 'danger', 'N', '0', 'admin', '2026-01-23 11:53:06', '', NULL, '系统默认否');
INSERT INTO `sys_dict_data` (`dict_code`, `tenant_id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (14, 1, 1, '通知', '1', 'sys_notice_type', '', 'warning', 'Y', '0', 'admin', '2026-01-23 11:53:06', '', NULL, '通知');
INSERT INTO `sys_dict_data` (`dict_code`, `tenant_id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (15, 1, 2, '公告', '2', 'sys_notice_type', '', 'success', 'N', '0', 'admin', '2026-01-23 11:53:06', '', NULL, '公告');
INSERT INTO `sys_dict_data` (`dict_code`, `tenant_id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (16, 1, 1, '正常', '0', 'sys_notice_status', '', 'primary', 'Y', '0', 'admin', '2026-01-23 11:53:06', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` (`dict_code`, `tenant_id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (17, 1, 2, '关闭', '1', 'sys_notice_status', '', 'danger', 'N', '0', 'admin', '2026-01-23 11:53:06', '', NULL, '关闭状态');
INSERT INTO `sys_dict_data` (`dict_code`, `tenant_id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (18, 1, 99, '其他', '0', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2026-01-23 11:53:06', '', NULL, '其他操作');
INSERT INTO `sys_dict_data` (`dict_code`, `tenant_id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (19, 1, 1, '新增', '1', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2026-01-23 11:53:06', '', NULL, '新增操作');
INSERT INTO `sys_dict_data` (`dict_code`, `tenant_id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (20, 1, 2, '修改', '2', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2026-01-23 11:53:06', '', NULL, '修改操作');
INSERT INTO `sys_dict_data` (`dict_code`, `tenant_id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (21, 1, 3, '删除', '3', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2026-01-23 11:53:06', '', NULL, '删除操作');
INSERT INTO `sys_dict_data` (`dict_code`, `tenant_id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (22, 1, 4, '授权', '4', 'sys_oper_type', '', 'primary', 'N', '0', 'admin', '2026-01-23 11:53:06', '', NULL, '授权操作');
INSERT INTO `sys_dict_data` (`dict_code`, `tenant_id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (23, 1, 5, '导出', '5', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2026-01-23 11:53:06', '', NULL, '导出操作');
INSERT INTO `sys_dict_data` (`dict_code`, `tenant_id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (24, 1, 6, '导入', '6', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2026-01-23 11:53:06', '', NULL, '导入操作');
INSERT INTO `sys_dict_data` (`dict_code`, `tenant_id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (25, 1, 7, '强退', '7', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2026-01-23 11:53:06', '', NULL, '强退操作');
INSERT INTO `sys_dict_data` (`dict_code`, `tenant_id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (26, 1, 8, '生成代码', '8', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2026-01-23 11:53:06', '', NULL, '生成操作');
INSERT INTO `sys_dict_data` (`dict_code`, `tenant_id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (27, 1, 9, '清空数据', '9', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2026-01-23 11:53:06', '', NULL, '清空操作');
INSERT INTO `sys_dict_data` (`dict_code`, `tenant_id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (28, 1, 1, '成功', '0', 'sys_common_status', '', 'primary', 'N', '0', 'admin', '2026-01-23 11:53:06', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` (`dict_code`, `tenant_id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (29, 1, 2, '失败', '1', 'sys_common_status', '', 'danger', 'N', '0', 'admin', '2026-01-23 11:53:06', '', NULL, '停用状态');
INSERT INTO `sys_dict_data` (`dict_code`, `tenant_id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1000, 0, 1, '批发', '1', 'wms_customer_type', '', 'default', 'Y', '0', 'admin', '2026-02-05 23:51:14', '', NULL, '批发客户');
INSERT INTO `sys_dict_data` (`dict_code`, `tenant_id`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1001, 0, 2, '零售', '2', 'wms_customer_type', '', 'success', 'N', '0', 'admin', '2026-02-05 23:51:14', '', NULL, '零售客户');
COMMIT;

-- ----------------------------
-- Table structure for sys_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type` (
  `dict_id` bigint NOT NULL AUTO_INCREMENT COMMENT '字典主键',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  `dict_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '字典名称',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '字典类型',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_id`) USING BTREE,
  UNIQUE KEY `dict_type` (`dict_type`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=101 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='字典类型表';

-- ----------------------------
-- Records of sys_dict_type
-- ----------------------------
BEGIN;
INSERT INTO `sys_dict_type` (`dict_id`, `tenant_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1, 1, '用户性别', 'sys_user_sex', '0', 'admin', '2026-01-23 11:53:06', '', NULL, '用户性别列表');
INSERT INTO `sys_dict_type` (`dict_id`, `tenant_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2, 1, '菜单状态', 'sys_show_hide', '0', 'admin', '2026-01-23 11:53:06', '', NULL, '菜单状态列表');
INSERT INTO `sys_dict_type` (`dict_id`, `tenant_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3, 1, '系统开关', 'sys_normal_disable', '0', 'admin', '2026-01-23 11:53:06', '', NULL, '系统开关列表');
INSERT INTO `sys_dict_type` (`dict_id`, `tenant_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4, 1, '任务状态', 'sys_job_status', '0', 'admin', '2026-01-23 11:53:06', '', NULL, '任务状态列表');
INSERT INTO `sys_dict_type` (`dict_id`, `tenant_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5, 1, '任务分组', 'sys_job_group', '0', 'admin', '2026-01-23 11:53:06', '', NULL, '任务分组列表');
INSERT INTO `sys_dict_type` (`dict_id`, `tenant_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (6, 1, '系统是否', 'sys_yes_no', '0', 'admin', '2026-01-23 11:53:06', '', NULL, '系统是否列表');
INSERT INTO `sys_dict_type` (`dict_id`, `tenant_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (7, 1, '通知类型', 'sys_notice_type', '0', 'admin', '2026-01-23 11:53:06', '', NULL, '通知类型列表');
INSERT INTO `sys_dict_type` (`dict_id`, `tenant_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (8, 1, '通知状态', 'sys_notice_status', '0', 'admin', '2026-01-23 11:53:06', '', NULL, '通知状态列表');
INSERT INTO `sys_dict_type` (`dict_id`, `tenant_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (9, 1, '操作类型', 'sys_oper_type', '0', 'admin', '2026-01-23 11:53:06', '', NULL, '操作类型列表');
INSERT INTO `sys_dict_type` (`dict_id`, `tenant_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (10, 1, '系统状态', 'sys_common_status', '0', 'admin', '2026-01-23 11:53:06', '', NULL, '登录状态列表');
INSERT INTO `sys_dict_type` (`dict_id`, `tenant_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (100, 0, '客户类型', 'wms_customer_type', '0', 'admin', '2026-02-05 23:51:14', '', NULL, '客户类型列表');
COMMIT;

-- ----------------------------
-- Table structure for sys_job
-- ----------------------------
DROP TABLE IF EXISTS `sys_job`;
CREATE TABLE `sys_job` (
  `job_id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  `job_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '任务名称',
  `job_group` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'DEFAULT' COMMENT '任务组名',
  `invoke_target` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '调用目标字符串',
  `cron_expression` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT 'cron执行表达式',
  `misfire_policy` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '3' COMMENT '计划执行错误策略（1立即执行 2执行一次 3放弃执行）',
  `concurrent` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '1' COMMENT '是否并发执行（0允许 1禁止）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '状态（0正常 1暂停）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '备注信息',
  PRIMARY KEY (`job_id`,`job_name`,`job_group`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=104 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='定时任务调度表';

-- ----------------------------
-- Records of sys_job
-- ----------------------------
BEGIN;
INSERT INTO `sys_job` (`job_id`, `tenant_id`, `job_name`, `job_group`, `invoke_target`, `cron_expression`, `misfire_policy`, `concurrent`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1, 1, '系统默认（无参）', 'DEFAULT', 'ryTask.ryNoParams', '0/10 * * * * ?', '3', '1', '1', 'admin', '2026-01-23 11:53:06', '', NULL, '');
INSERT INTO `sys_job` (`job_id`, `tenant_id`, `job_name`, `job_group`, `invoke_target`, `cron_expression`, `misfire_policy`, `concurrent`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2, 1, '系统默认（有参）', 'DEFAULT', 'ryTask.ryParams(\'ry\')', '0/15 * * * * ?', '3', '1', '1', 'admin', '2026-01-23 11:53:06', '', NULL, '');
INSERT INTO `sys_job` (`job_id`, `tenant_id`, `job_name`, `job_group`, `invoke_target`, `cron_expression`, `misfire_policy`, `concurrent`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3, 1, '系统默认（多参）', 'DEFAULT', 'ryTask.ryMultipleParams(\'ry\', true, 2000L, 316.50D, 100)', '0/20 * * * * ?', '3', '1', '1', 'admin', '2026-01-23 11:53:06', '', NULL, '');
INSERT INTO `sys_job` (`job_id`, `tenant_id`, `job_name`, `job_group`, `invoke_target`, `cron_expression`, `misfire_policy`, `concurrent`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (100, 1, 'AI配额月度重置', 'DEFAULT', 'aiQuotaTask.resetMonthlyQuota()', '0 0 0 1 * ?', '2', '1', '0', 'admin', '2026-04-03 22:19:24', '', NULL, '每月1号0点重置所有租户月度已用次数');
INSERT INTO `sys_job` (`job_id`, `tenant_id`, `job_name`, `job_group`, `invoke_target`, `cron_expression`, `misfire_policy`, `concurrent`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (101, 1, 'AI配额年度重置', 'DEFAULT', 'aiQuotaTask.resetAnnualQuota()', '0 0 0 1 1 ?', '2', '1', '0', 'admin', '2026-04-03 22:19:24', '', NULL, '每年1月1号0点重置所有租户年度已用次数');
COMMIT;

-- ----------------------------
-- Table structure for sys_job_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_job_log`;
CREATE TABLE `sys_job_log` (
  `job_log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务日志ID',
  `job_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务名称',
  `job_group` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务组名',
  `invoke_target` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '调用目标字符串',
  `job_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '日志信息',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '执行状态（0正常 1失败）',
  `exception_info` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '异常信息',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  PRIMARY KEY (`job_log_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='定时任务调度日志表';

-- ----------------------------
-- Records of sys_job_log
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for sys_logininfor
-- ----------------------------
DROP TABLE IF EXISTS `sys_logininfor`;
CREATE TABLE `sys_logininfor` (
  `info_id` bigint NOT NULL AUTO_INCREMENT COMMENT '访问ID',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '用户账号',
  `ipaddr` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '登录IP地址',
  `login_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '登录地点',
  `browser` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '浏览器类型',
  `os` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '操作系统',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '登录状态（0成功 1失败）',
  `msg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '提示消息',
  `login_time` datetime DEFAULT NULL COMMENT '访问时间',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  PRIMARY KEY (`info_id`) USING BTREE,
  KEY `idx_sys_logininfor_s` (`status`) USING BTREE,
  KEY `idx_sys_logininfor_lt` (`login_time`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1857 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='系统访问记录';

-- ----------------------------
-- Records of sys_logininfor
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
  `menu_id` bigint NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `menu_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单名称',
  `parent_id` bigint DEFAULT '0' COMMENT '父菜单ID',
  `order_num` int DEFAULT '0' COMMENT '显示顺序',
  `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '路由地址',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '组件路径',
  `query` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '路由参数',
  `route_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '路由名称',
  `is_frame` int DEFAULT '1' COMMENT '是否为外链（0是 1否）',
  `is_cache` int DEFAULT '0' COMMENT '是否缓存（0缓存 1不缓存）',
  `menu_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '菜单类型（M目录 C菜单 F按钮）',
  `visible` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '菜单状态（0显示 1隐藏）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '菜单状态（0正常 1停用）',
  `perms` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '#' COMMENT '菜单图标',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`menu_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2645 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='菜单权限表';

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
BEGIN;
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1, '系统管理', 0, 75, 'system', NULL, '', '', 1, 0, 'M', '0', '0', '', 'system', 'admin', '2026-01-23 11:53:05', 'admin', '2026-10-28 22:54:59', '系统管理目录');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2, '系统监控', 0, 90, 'monitor', NULL, '', '', 1, 0, 'M', '0', '0', '', 'monitor', 'admin', '2026-01-23 11:53:05', '', NULL, '系统监控目录');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3, '系统工具', 0, 80, 'tool', NULL, '', '', 1, 0, 'M', '0', '0', '', 'tool', 'admin', '2026-01-23 11:53:05', 'admin', '2026-10-28 22:20:59', '系统工具目录');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (100, '用户管理', 1, 1, 'user', 'system/user/index', '', '', 1, 0, 'C', '0', '0', 'system:user:list', 'user', 'admin', '2026-01-23 11:53:05', '', NULL, '用户管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (101, '角色管理', 1, 2, 'role', 'system/role/index', '', '', 1, 0, 'C', '0', '0', 'system:role:list', 'peoples', 'admin', '2026-01-23 11:53:05', '', NULL, '角色管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (102, '菜单管理', 1, 3, 'menu', 'system/menu/index', '', '', 1, 0, 'C', '0', '0', 'system:menu:list', 'tree-table', 'admin', '2026-01-23 11:53:05', '', NULL, '菜单管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (103, '部门管理', 1, 4, 'dept', 'system/dept/index', '', '', 1, 0, 'C', '0', '0', 'system:dept:list', 'tree', 'admin', '2026-01-23 11:53:05', '', NULL, '部门管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (104, '岗位管理', 1, 5, 'post', 'system/post/index', '', '', 1, 0, 'C', '0', '0', 'system:post:list', 'post', 'admin', '2026-01-23 11:53:05', '', NULL, '岗位管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (105, '字典管理', 1, 6, 'dict', 'system/dict/index', '', '', 1, 0, 'C', '0', '0', 'system:dict:list', 'dict', 'admin', '2026-01-23 11:53:05', '', NULL, '字典管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (106, '参数设置', 1, 7, 'config', 'system/config/index', '', '', 1, 0, 'C', '0', '0', 'system:config:list', 'edit', 'admin', '2026-01-23 11:53:05', '', NULL, '参数设置菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (107, '通知公告', 1, 8, 'notice', 'system/notice/index', '', '', 1, 0, 'C', '0', '0', 'system:notice:list', 'message', 'admin', '2026-01-23 11:53:05', '', NULL, '通知公告菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (108, '日志管理', 1, 9, 'log', '', '', '', 1, 0, 'M', '0', '0', '', 'log', 'admin', '2026-01-23 11:53:05', '', NULL, '日志管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (109, '在线用户', 2, 1, 'online', 'monitor/online/index', '', '', 1, 0, 'C', '0', '0', 'monitor:online:list', 'online', 'admin', '2026-01-23 11:53:05', '', NULL, '在线用户菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (110, '定时任务', 2, 2, 'job', 'monitor/job/index', '', '', 1, 0, 'C', '0', '0', 'monitor:job:list', 'job', 'admin', '2026-01-23 11:53:05', '', NULL, '定时任务菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (111, '数据监控', 2, 3, 'druid', 'monitor/druid/index', '', '', 1, 0, 'C', '0', '0', 'monitor:druid:list', 'druid', 'admin', '2026-01-23 11:53:05', '', NULL, '数据监控菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (112, '服务监控', 2, 4, 'server', 'monitor/server/index', '', '', 1, 0, 'C', '0', '0', 'monitor:server:list', 'server', 'admin', '2026-01-23 11:53:05', '', NULL, '服务监控菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (113, '缓存监控', 2, 5, 'cache', 'monitor/cache/index', '', '', 1, 0, 'C', '0', '0', 'monitor:cache:list', 'redis', 'admin', '2026-01-23 11:53:05', '', NULL, '缓存监控菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (114, '缓存列表', 2, 6, 'cacheList', 'monitor/cache/list', '', '', 1, 0, 'C', '0', '0', 'monitor:cache:list', 'redis-list', 'admin', '2026-01-23 11:53:05', '', NULL, '缓存列表菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (115, '表单构建', 3, 1, 'build', 'tool/build/index', '', '', 1, 0, 'C', '0', '0', 'tool:build:list', 'build', 'admin', '2026-01-23 11:53:05', '', NULL, '表单构建菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (116, '代码生成', 3, 2, 'gen', 'tool/gen/index', '', '', 1, 0, 'C', '0', '0', 'tool:gen:list', 'code', 'admin', '2026-01-23 11:53:05', '', NULL, '代码生成菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (117, '系统接口', 3, 3, 'swagger', 'tool/swagger/index', '', '', 1, 0, 'C', '0', '0', 'tool:swagger:list', 'swagger', 'admin', '2026-01-23 11:53:05', '', NULL, '系统接口菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (500, '操作日志', 108, 1, 'operlog', 'monitor/operlog/index', '', '', 1, 0, 'C', '0', '0', 'monitor:operlog:list', 'form', 'admin', '2026-01-23 11:53:05', '', NULL, '操作日志菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (501, '登录日志', 108, 2, 'logininfor', 'monitor/logininfor/index', '', '', 1, 0, 'C', '0', '0', 'monitor:logininfor:list', 'logininfor', 'admin', '2026-01-23 11:53:05', '', NULL, '登录日志菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1000, '用户查询', 100, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:query', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1001, '用户新增', 100, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:add', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1002, '用户修改', 100, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:edit', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1003, '用户删除', 100, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:remove', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1004, '用户导出', 100, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:export', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1005, '用户导入', 100, 6, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:import', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1006, '重置密码', 100, 7, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:resetPwd', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1007, '角色查询', 101, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:query', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1008, '角色新增', 101, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:add', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1009, '角色修改', 101, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:edit', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1010, '角色删除', 101, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:remove', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1011, '角色导出', 101, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:export', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1012, '菜单查询', 102, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:query', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1013, '菜单新增', 102, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:add', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1014, '菜单修改', 102, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:edit', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1015, '菜单删除', 102, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:remove', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1016, '部门查询', 103, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:query', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1017, '部门新增', 103, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:add', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1018, '部门修改', 103, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:edit', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1019, '部门删除', 103, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:remove', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1020, '岗位查询', 104, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:query', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1021, '岗位新增', 104, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:add', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1022, '岗位修改', 104, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:edit', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1023, '岗位删除', 104, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:remove', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1024, '岗位导出', 104, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:export', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1025, '字典查询', 105, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:query', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1026, '字典新增', 105, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:add', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1027, '字典修改', 105, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:edit', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1028, '字典删除', 105, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:remove', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1029, '字典导出', 105, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:export', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1030, '参数查询', 106, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:query', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1031, '参数新增', 106, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:add', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1032, '参数修改', 106, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:edit', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1033, '参数删除', 106, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:remove', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1034, '参数导出', 106, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:export', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1035, '公告查询', 107, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:query', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1036, '公告新增', 107, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:add', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1037, '公告修改', 107, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:edit', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1038, '公告删除', 107, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:remove', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1039, '操作查询', 500, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:query', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1040, '操作删除', 500, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:remove', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1041, '日志导出', 500, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:export', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1042, '登录查询', 501, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:query', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1043, '登录删除', 501, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:remove', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1044, '日志导出', 501, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:export', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1045, '账户解锁', 501, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:unlock', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1046, '在线查询', 109, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:query', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1047, '批量强退', 109, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:batchLogout', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1048, '单条强退', 109, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:forceLogout', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1049, '任务查询', 110, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:query', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1050, '任务新增', 110, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:add', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1051, '任务修改', 110, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:edit', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1052, '任务删除', 110, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:remove', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1053, '状态修改', 110, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:changeStatus', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1054, '任务导出', 110, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:export', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1055, '生成查询', 116, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:query', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1056, '生成修改', 116, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:edit', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1057, '生成删除', 116, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:remove', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1058, '导入代码', 116, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:import', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1059, '预览代码', 116, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:preview', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1060, '生成代码', 116, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:code', '#', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2000, '基础资料', 0, 1, 'wms', NULL, NULL, 'wms', 1, 0, 'M', '0', '0', '', 'tree-table', 'admin', '2026-02-05 19:51:41', 'admin', '2026-02-05 19:54:57', '基础资料目录');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2001, '仓库管理', 2000, 1, 'warehouse', 'wms/warehouse/index', NULL, 'wms-warehouse', 1, 0, 'C', '0', '0', 'wms:warehouse:query', 'tree-table', 'admin', '2026-02-05 19:51:41', '', NULL, '仓库管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2002, '仓库查询', 2001, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'wms:warehouse:query', '#', 'admin', '2026-02-05 21:26:09', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2003, '仓库新增', 2001, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'wms:warehouse:add', '#', 'admin', '2026-02-05 21:26:09', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2004, '仓库修改', 2001, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'wms:warehouse:edit', '#', 'admin', '2026-02-05 21:26:09', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2005, '仓库删除', 2001, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'wms:warehouse:remove', '#', 'admin', '2026-02-05 21:26:09', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2006, '计量单位', 2000, 2, 'unit', 'wms/unit/index', NULL, '', 1, 0, 'C', '0', '0', '', 'component', 'admin', '2026-02-05 23:10:04', 'admin', '2026-02-05 23:15:24', '计量单位目录');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2007, '计量单位查询', 2006, 1, '#', NULL, NULL, '', 1, 0, 'F', '0', '0', 'wms:unit:query', '#', 'admin', '2026-02-05 23:10:04', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2008, '计量单位新增', 2006, 2, '#', NULL, NULL, '', 1, 0, 'F', '0', '0', 'wms:unit:add', '#', 'admin', '2026-02-05 23:10:04', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2009, '计量单位修改', 2006, 3, '#', NULL, NULL, '', 1, 0, 'F', '0', '0', 'wms:unit:edit', '#', 'admin', '2026-02-05 23:10:04', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2010, '计量单位删除', 2006, 4, '#', NULL, NULL, '', 1, 0, 'F', '0', '0', 'wms:unit:remove', '#', 'admin', '2026-02-05 23:10:04', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2011, '客户管理', 2000, 5, 'customer', 'wms/customer/index', NULL, NULL, 1, 0, 'C', '0', '0', 'wms:customer:query', 'user', 'admin', '2026-02-05 23:49:40', 'zhaoyun', '2026-02-07 00:40:01', '客户管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2012, '客户查询', 2011, 1, '#', '', NULL, NULL, 1, 0, 'F', '0', '0', 'wms:customer:query', '#', 'admin', '2026-02-05 23:49:40', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2013, '客户新增', 2011, 2, '#', '', NULL, NULL, 1, 0, 'F', '0', '0', 'wms:customer:add', '#', 'admin', '2026-02-05 23:49:40', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2014, '客户修改', 2011, 3, '#', '', NULL, NULL, 1, 0, 'F', '0', '0', 'wms:customer:edit', '#', 'admin', '2026-02-05 23:49:40', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2015, '客户删除', 2011, 4, '#', '', NULL, NULL, 1, 0, 'F', '0', '0', 'wms:customer:remove', '#', 'admin', '2026-02-05 23:49:40', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2021, '供应商管理', 2000, 6, 'supplier', 'wms/supplier/index', NULL, NULL, 1, 0, 'C', '0', '0', 'wms:supplier:query', 'shopping', 'admin', '2026-02-05 23:52:22', '', NULL, '供应商管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2022, '供应商查询', 2021, 1, '#', '', NULL, NULL, 1, 0, 'F', '0', '0', 'wms:supplier:query', '#', 'admin', '2026-02-05 23:52:22', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2023, '供应商新增', 2021, 2, '#', '', NULL, NULL, 1, 0, 'F', '0', '0', 'wms:supplier:add', '#', 'admin', '2026-02-05 23:52:22', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2024, '供应商修改', 2021, 3, '#', '', NULL, NULL, 1, 0, 'F', '0', '0', 'wms:supplier:edit', '#', 'admin', '2026-02-05 23:52:22', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2025, '供应商删除', 2021, 4, '#', '', NULL, NULL, 1, 0, 'F', '0', '0', 'wms:supplier:remove', '#', 'admin', '2026-02-05 23:52:22', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2031, '商品分类', 2000, 3, 'goodsCategory', 'wms/goodsCategory/index', NULL, NULL, 1, 0, 'C', '0', '0', 'wms:goodsCategory:query', 'tree-table', 'admin', '2026-02-05 23:55:01', '', NULL, '商品分类菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2032, '商品分类查询', 2031, 1, '#', '', NULL, NULL, 1, 0, 'F', '0', '0', 'wms:goodsCategory:query', '#', 'admin', '2026-02-05 23:55:01', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2033, '商品分类新增', 2031, 2, '#', '', NULL, NULL, 1, 0, 'F', '0', '0', 'wms:goodsCategory:add', '#', 'admin', '2026-02-05 23:55:01', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2034, '商品分类修改', 2031, 3, '#', '', NULL, NULL, 1, 0, 'F', '0', '0', 'wms:goodsCategory:edit', '#', 'admin', '2026-02-05 23:55:01', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2035, '商品分类删除', 2031, 4, '#', '', NULL, NULL, 1, 0, 'F', '0', '0', 'wms:goodsCategory:remove', '#', 'admin', '2026-02-05 23:55:01', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2041, '商品管理', 2000, 4, 'goods', 'wms/goods/index', NULL, NULL, 1, 0, 'C', '0', '0', 'wms:goods:query', 'table', 'admin', '2026-02-05 23:57:38', 'zhaoyun', '2026-02-07 00:39:50', '商品管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2042, '商品查询', 2041, 1, '#', '', NULL, NULL, 1, 0, 'F', '0', '0', 'wms:goods:query', '#', 'admin', '2026-02-05 23:57:38', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2043, '商品新增', 2041, 2, '#', '', NULL, NULL, 1, 0, 'F', '0', '0', 'wms:goods:add', '#', 'admin', '2026-02-05 23:57:38', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2044, '商品修改', 2041, 3, '#', '', NULL, NULL, 1, 0, 'F', '0', '0', 'wms:goods:edit', '#', 'admin', '2026-02-05 23:57:38', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2045, '商品删除', 2041, 4, '#', '', NULL, NULL, 1, 0, 'F', '0', '0', 'wms:goods:remove', '#', 'admin', '2026-02-05 23:57:38', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2046, '商品SKU', 2041, 3, 'sku', 'wms/goodsSku/index', NULL, '', 1, 0, 'C', '0', '0', 'wms:goodsSku:query', 'skill', 'admin', '2026-02-08 23:22:26', '', NULL, '商品SKU菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2047, '商品SKU查询', 2046, 1, NULL, NULL, NULL, '', 1, 0, 'F', '0', '0', 'wms:goodsSku:query', '#', 'admin', '2026-02-08 23:22:26', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2048, '商品SKU新增', 2046, 2, NULL, NULL, NULL, '', 1, 0, 'F', '0', '0', 'wms:goodsSku:add', '#', 'admin', '2026-02-08 23:22:26', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2049, '商品SKU修改', 2046, 3, NULL, NULL, NULL, '', 1, 0, 'F', '0', '0', 'wms:goodsSku:edit', '#', 'admin', '2026-02-08 23:22:26', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2050, '商品SKU删除', 2046, 4, NULL, NULL, NULL, '', 1, 0, 'F', '0', '0', 'wms:goodsSku:remove', '#', 'admin', '2026-02-08 23:22:26', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2051, '商品规格', 2041, 3, 'goodsSpec', 'wms/goodsSpec/index', '', '', 1, 0, 'C', '0', '0', 'wms:goodsSpec:query', 'form', 'admin', '2026-02-09 01:08:56', '', NULL, '商品规格菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2052, '商品规格查询', 2051, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:goodsSpec:query', '#', 'admin', '2026-02-09 01:08:56', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2053, '商品规格新增', 2051, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:goodsSpec:add', '#', 'admin', '2026-02-09 01:08:56', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2054, '商品规格修改', 2051, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:goodsSpec:edit', '#', 'admin', '2026-02-09 01:08:56', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2055, '商品规格删除', 2051, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'wms:goodsSpec:remove', '#', 'admin', '2026-02-09 01:08:56', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2100, '库存管理', 0, 4, 'stock', NULL, NULL, NULL, 1, 0, 'M', '0', '0', '', 'guide', 'admin', '2026-02-06 00:01:41', '', NULL, '库存管理目录');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2101, '库存查询', 2100, 1, 'stockQuery', 'wms/stock/index', NULL, NULL, 1, 0, 'C', '0', '0', 'wms:stock:query', 'list', 'admin', '2026-02-06 00:01:41', '', NULL, '库存查询菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2110, '库存变动日志', 2100, 1, 'stockLog', 'wms/stockLog/index', NULL, '', 1, 0, 'C', '0', '0', 'wms:stockLog:query', 'button', 'admin', '2026-02-08 11:28:38', 'admin', '2026-10-28 22:45:36', '库存查询菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2111, '库存调拨', 2100, 2, 'stockTransfer', 'wms/stockTransfer/index', NULL, '', 1, 0, 'C', '0', '0', 'wms:stockTransfer:query', 'drag', 'admin', '2026-02-15 14:32:12', 'admin', '2026-02-15 14:32:12', '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2113, '调拨新增', 2111, 2, '#', '', NULL, NULL, 1, 0, 'F', '0', '0', 'wms:stockTransfer:add', '#', 'admin', '2026-02-06 00:03:57', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2114, '调拨修改', 2111, 3, '#', '', NULL, NULL, 1, 0, 'F', '0', '0', 'wms:stockTransfer:edit', '#', 'admin', '2026-02-06 00:03:57', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2115, '调拨删除', 2111, 4, '#', '', NULL, NULL, 1, 0, 'F', '0', '0', 'wms:stockTransfer:remove', '#', 'admin', '2026-02-06 00:03:57', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2116, '调拨审核', 2111, 5, '#', '', NULL, NULL, 1, 0, 'F', '0', '0', 'wms:stockTransfer:approve', '#', 'admin', '2026-02-06 00:03:57', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2121, '库存盘点', 2100, 3, 'stockCheck', 'wms/stockCheck/index', NULL, NULL, 1, 0, 'C', '0', '0', 'wms:stockCheck:query', 'component', 'admin', '2026-02-06 00:06:18', '', NULL, '库存盘点菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2122, '盘点查询', 2121, 1, '#', '', NULL, NULL, 1, 0, 'F', '0', '0', 'wms:stockCheck:query', '#', 'admin', '2026-02-06 00:06:18', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2123, '盘点新增', 2121, 2, '#', '', NULL, NULL, 1, 0, 'F', '0', '0', 'wms:stockCheck:add', '#', 'admin', '2026-02-06 00:06:18', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2124, '盘点修改', 2121, 3, '#', '', NULL, NULL, 1, 0, 'F', '0', '0', 'wms:stockCheck:edit', '#', 'admin', '2026-02-06 00:06:18', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2125, '盘点删除', 2121, 4, '#', '', NULL, NULL, 1, 0, 'F', '0', '0', 'wms:stockCheck:remove', '#', 'admin', '2026-02-06 00:06:18', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2126, '盘点审核', 2121, 5, '#', '', NULL, NULL, 1, 0, 'F', '0', '0', 'wms:stockCheck:approve', '#', 'admin', '2026-02-06 00:06:18', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2200, '采购管理', 0, 2, 'purchase', NULL, NULL, NULL, 1, 0, 'M', '0', '0', '', 'shopping', 'admin', '2026-02-06 00:08:37', '', NULL, '采购管理目录');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2201, '采购订单', 2200, 1, 'purchaseOrder', 'wms/purchaseOrder/index', NULL, NULL, 1, 0, 'C', '0', '0', 'wms:purchaseOrder:query', 'list', 'admin', '2026-02-06 00:08:37', '', NULL, '采购订单菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2202, '订单查询', 2201, 1, '#', '', NULL, NULL, 1, 0, 'F', '0', '0', 'wms:purchaseOrder:query', '#', 'admin', '2026-02-06 00:08:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2203, '订单新增', 2201, 2, '#', '', NULL, NULL, 1, 0, 'F', '0', '0', 'wms:purchaseOrder:add', '#', 'admin', '2026-02-06 00:08:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2204, '订单修改', 2201, 3, '#', '', NULL, NULL, 1, 0, 'F', '0', '0', 'wms:purchaseOrder:edit', '#', 'admin', '2026-02-06 00:08:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2205, '订单删除', 2201, 4, '#', '', NULL, NULL, 1, 0, 'F', '0', '0', 'wms:purchaseOrder:remove', '#', 'admin', '2026-02-06 00:08:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2206, '订单审核', 2201, 5, '#', '', NULL, NULL, 1, 0, 'F', '0', '0', 'wms:purchaseOrder:approve', '#', 'admin', '2026-02-06 00:08:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2207, '订单关闭', 2201, 6, '#', '', NULL, NULL, 1, 0, 'F', '0', '0', 'wms:purchaseOrder:close', '#', 'admin', '2026-02-06 00:08:37', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2208, '采购入库', 2200, 2, 'purchaseIn', 'wms/purchaseIn/index', NULL, NULL, 1, 0, 'C', '0', '0', 'wms:purchaseIn:query', 'shopping', 'admin', '2026-02-06 00:11:47', '', NULL, '采购入库菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2209, '入库查询', 2208, 1, '#', '', NULL, NULL, 1, 0, 'F', '0', '0', 'wms:purchaseIn:query', '#', 'admin', '2026-02-06 00:11:47', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2210, '入库新增', 2208, 2, '#', '', NULL, NULL, 1, 0, 'F', '0', '0', 'wms:purchaseIn:add', '#', 'admin', '2026-02-06 00:11:47', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2211, '入库修改', 2208, 3, '#', '', NULL, NULL, 1, 0, 'F', '0', '0', 'wms:purchaseIn:edit', '#', 'admin', '2026-02-06 00:11:47', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2212, '入库删除', 2208, 4, '#', '', NULL, NULL, 1, 0, 'F', '0', '0', 'wms:purchaseIn:remove', '#', 'admin', '2026-02-06 00:11:47', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2213, '入库审核', 2208, 5, '#', '', NULL, NULL, 1, 0, 'F', '0', '0', 'wms:purchaseIn:approve', '#', 'admin', '2026-02-06 00:11:47', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2214, '采购退货', 2200, 3, 'purchaseReturn', 'wms/purchaseReturn/index', NULL, '', 1, 0, 'C', '0', '0', 'wms:purchaseReturn:list', 'component', 'admin', '2026-02-06 00:55:41', 'zhaoyun', '2026-02-07 00:39:19', '采购退货管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2215, '采购退货查询', 2214, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:purchaseReturn:query', '#', 'admin', '2026-02-06 00:55:41', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2216, '采购退货新增', 2214, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:purchaseReturn:add', '#', 'admin', '2026-02-06 00:55:41', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2217, '采购退货修改', 2214, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:purchaseReturn:edit', '#', 'admin', '2026-02-06 00:55:41', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2218, '采购退货删除', 2214, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:purchaseReturn:remove', '#', 'admin', '2026-02-06 00:55:41', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2219, '采购退货审核', 2214, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:purchaseReturn:approve', '#', 'admin', '2026-02-06 00:55:41', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2220, '销售订单', 2400, 1, 'salesOrder', 'wms/salesOrder/index', NULL, '', 1, 0, 'C', '0', '0', 'wms:salesOrder:list', 'shopping', 'admin', '2026-02-06 00:55:43', '', NULL, '销售订单管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2221, '销售订单查询', 2220, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:salesOrder:query', '#', 'admin', '2026-02-06 00:55:43', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2222, '销售订单新增', 2220, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:salesOrder:add', '#', 'admin', '2026-02-06 00:55:43', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2223, '销售订单修改', 2220, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:salesOrder:edit', '#', 'admin', '2026-02-06 00:55:43', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2224, '销售订单删除', 2220, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:salesOrder:remove', '#', 'admin', '2026-02-06 00:55:43', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2225, '销售订单审核', 2220, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:salesOrder:approve', '#', 'admin', '2026-02-06 00:55:43', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2226, '销售订单关闭', 2220, 6, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:salesOrder:close', '#', 'admin', '2026-02-06 00:55:43', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2227, '销售订单导出', 2220, 7, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:salesOrder:export', '#', 'admin', '2026-02-06 00:55:43', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2228, '销售出库', 2400, 2, 'salesOut', 'wms/salesOut/index', NULL, '', 1, 0, 'C', '0', '0', 'wms:salesOut:list', 'money', 'admin', '2026-02-06 00:55:44', '', NULL, '销售出库管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2229, '销售出库查询', 2228, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:salesOut:query', '#', 'admin', '2026-02-06 00:55:44', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2230, '销售出库新增', 2228, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:salesOut:add', '#', 'admin', '2026-02-06 00:55:44', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2231, '销售出库修改', 2228, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:salesOut:edit', '#', 'admin', '2026-02-06 00:55:44', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2232, '销售出库删除', 2228, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:salesOut:remove', '#', 'admin', '2026-02-06 00:55:44', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2233, '销售出库审核', 2228, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:salesOut:approve', '#', 'admin', '2026-02-06 00:55:44', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2234, '销售退货', 2400, 3, 'salesReturn', 'wms/salesReturn/index', NULL, '', 1, 0, 'C', '0', '0', 'wms:salesReturn:list', 'guide', 'admin', '2026-02-06 00:55:45', '', NULL, '销售退货管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2235, '销售退货查询', 2234, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:salesReturn:query', '#', 'admin', '2026-02-06 00:55:45', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2236, '销售退货新增', 2234, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:salesReturn:add', '#', 'admin', '2026-02-06 00:55:45', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2237, '销售退货修改', 2234, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:salesReturn:edit', '#', 'admin', '2026-02-06 00:55:45', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2238, '销售退货删除', 2234, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:salesReturn:remove', '#', 'admin', '2026-02-06 00:55:45', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2239, '销售退货审核', 2234, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:salesReturn:approve', '#', 'admin', '2026-02-06 00:55:45', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2300, '财务管理', 0, 5, 'finance', NULL, NULL, '', 1, 0, 'M', '0', '0', '', 'money', 'admin', '2026-02-06 00:55:47', '', NULL, '财务管理目录');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2301, '结算账户', 2300, 1, 'account', 'wms/account/index', NULL, '', 1, 0, 'C', '0', '0', 'wms:account:list', 'lock', 'admin', '2026-02-06 00:55:47', 'zhaoyun', '2026-02-07 00:39:01', '结算账户菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2302, '结算账户查询', 2301, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:account:query', '#', 'admin', '2026-02-06 00:55:47', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2303, '结算账户新增', 2301, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:account:add', '#', 'admin', '2026-02-06 00:55:47', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2304, '结算账户修改', 2301, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:account:edit', '#', 'admin', '2026-02-06 00:55:47', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2305, '结算账户删除', 2301, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:account:remove', '#', 'admin', '2026-02-06 00:55:47', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2306, '结算账户导出', 2301, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:account:export', '#', 'admin', '2026-02-06 00:55:47', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2307, '财务日志', 2300, 2, 'financeLog', 'wms/financeLog/index', NULL, '', 1, 0, 'C', '0', '0', 'wms:financeLog:list', 'list', 'admin', '2026-02-06 00:56:05', '', NULL, '财务日志菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2308, '财务日志查询', 2307, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:financeLog:query', '#', 'admin', '2026-02-06 00:56:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2309, '财务日志导出', 2307, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:financeLog:export', '#', 'admin', '2026-02-06 00:56:05', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2310, '费用类型', 2300, 3, 'expenseType', 'wms/expenseType/index', NULL, '', 1, 0, 'C', '0', '0', 'wms:expenseType:list', 'tree', 'admin', '2026-02-06 00:56:06', '', NULL, '费用类型菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2311, '费用类型查询', 2310, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:expenseType:query', '#', 'admin', '2026-02-06 00:56:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2312, '费用类型新增', 2310, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:expenseType:add', '#', 'admin', '2026-02-06 00:56:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2313, '费用类型修改', 2310, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:expenseType:edit', '#', 'admin', '2026-02-06 00:56:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2314, '费用类型删除', 2310, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:expenseType:remove', '#', 'admin', '2026-02-06 00:56:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2315, '费用管理', 2300, 4, 'expense', 'wms/expense/index', NULL, '', 1, 0, 'C', '0', '0', 'wms:expense:list', 'money', 'admin', '2026-02-06 00:56:06', '', NULL, '费用管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2316, '费用查询', 2315, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:expense:query', '#', 'admin', '2026-02-06 00:56:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2317, '费用新增', 2315, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:expense:add', '#', 'admin', '2026-02-06 00:56:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2318, '费用修改', 2315, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:expense:edit', '#', 'admin', '2026-02-06 00:56:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2319, '费用删除', 2315, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:expense:remove', '#', 'admin', '2026-02-06 00:56:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2320, '费用审核', 2315, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:expense:approve', '#', 'admin', '2026-02-06 00:56:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2321, '费用导出', 2315, 6, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:expense:export', '#', 'admin', '2026-02-06 00:56:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2322, '应收账款', 2300, 5, 'accountsReceivable', 'wms/accountsReceivable/index', NULL, '', 1, 0, 'C', '0', '0', 'wms:accountsReceivable:list', 'guide', 'admin', '2026-02-06 01:01:52', '', NULL, '应收账款管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2323, '应收账款查询', 2322, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:accountsReceivable:query', '#', 'admin', '2026-02-06 01:01:52', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2324, '应收账款收款', 2322, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:accountsReceivable:collect', '#', 'admin', '2026-02-06 01:01:52', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2325, '应收账款导出', 2322, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:accountsReceivable:export', '#', 'admin', '2026-02-06 01:01:52', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2328, '应付账款', 2300, 6, 'accountsPayable', 'wms/accountsPayable/index', NULL, '', 1, 0, 'C', '0', '0', 'wms:accountsPayable:list', 'shopping', 'admin', '2026-02-06 01:01:52', '', NULL, '应付账款管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2329, '应付账款查询', 2328, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:accountsPayable:query', '#', 'admin', '2026-02-06 01:01:52', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2330, '应付账款付款', 2328, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:accountsPayable:pay', '#', 'admin', '2026-02-06 01:01:52', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2331, '应付账款导出', 2328, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:accountsPayable:export', '#', 'admin', '2026-02-06 01:01:52', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2400, '销售管理', 0, 3, 'sales', NULL, NULL, '', 1, 0, 'M', '0', '0', '', 'documentation', 'admin', '2026-02-06 09:53:50', '', NULL, '销售管理目录');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2600, '报表中心', 0, 7, 'report', 'wms/report/index', NULL, '', 1, 0, 'C', '0', '0', 'wms:report:query', 'chart', 'admin', '2026-02-08 21:36:33', '', NULL, '报表中心菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2601, '报表查询', 2600, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:report:query', '#', 'admin', '2026-02-08 21:36:33', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2602, '报表导出', 2600, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:report:export', '#', 'admin', '2026-02-08 21:36:33', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2603, '销售报表', 2600, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:report:sales', '#', 'admin', '2026-02-08 21:36:33', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2604, '采购报表', 2600, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:report:purchase', '#', 'admin', '2026-02-08 21:36:33', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2605, '库存报表', 2600, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:report:stock', '#', 'admin', '2026-02-08 21:36:33', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2606, '财务报表', 2600, 6, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:report:finance', '#', 'admin', '2026-02-08 21:36:33', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2607, '商品OCR识别', 2041, 10, '', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:ocr:recognize', '#', 'admin', '2026-02-19 00:04:30', '', NULL, '商品OCR识别权限');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2608, '临时文件上传', 2041, 11, '', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:ocr:upload', '#', 'admin', '2026-02-19 00:04:30', '', NULL, '临时文件上传权限');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2609, '批量OCR识别', 2041, 12, '', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:ocr:batchRecognize', '#', 'admin', '2026-02-19 00:04:30', '', NULL, '批量OCR识别权限');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2610, '语音/文本解析', 2041, 13, '', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:ai:parse', '#', 'admin', '2026-02-19 00:04:30', '', NULL, '语音识别和文本解析权限');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2611, 'AI推荐', 2041, 14, '', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:ai:recommend', '#', 'admin', '2026-02-19 00:04:30', '', NULL, '采购/销售推荐权限');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2612, 'AI对话', 2041, 15, '', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:ai:chat', '#', 'admin', '2026-02-19 00:04:30', '', NULL, 'AI对话助手权限');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2613, 'AI健康检查', 2041, 16, '', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:ai:health', '#', 'admin', '2026-02-19 00:04:30', '', NULL, 'AI服务健康检查权限');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2614, '预警列表', 2100, 10, '', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:stockAlert:list', '#', 'admin', '2026-02-19 00:04:45', '', NULL, '库存预警列表查询权限');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2615, '预警统计', 2100, 11, '', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:stockAlert:statistics', '#', 'admin', '2026-02-19 00:04:45', '', NULL, '库存预警统计权限');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2616, '预警详情', 2100, 12, '', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:stockAlert:query', '#', 'admin', '2026-02-19 00:04:45', '', NULL, '库存预警详情查询权限');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2617, '概览统计', 2600, 10, '', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:statistics:overview', '#', 'admin', '2026-02-19 00:04:45', '', NULL, '概览统计数据权限');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2618, '趋势分析', 2600, 11, '', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:statistics:trend', '#', 'admin', '2026-02-19 00:04:45', '', NULL, '销售/采购趋势分析权限');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2619, '库存统计', 2600, 12, '', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:statistics:stock', '#', 'admin', '2026-02-19 00:04:45', '', NULL, '库存Top/预警统计权限');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2620, '商品统计', 2600, 13, '', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:statistics:goods', '#', 'admin', '2026-02-19 00:04:45', '', NULL, '热销商品统计权限');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2621, '仓库统计', 2600, 14, '', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:statistics:warehouse', '#', 'admin', '2026-02-19 00:04:45', '', NULL, '仓库库存分布统计权限');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2622, '财务统计', 2600, 15, '', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:statistics:finance', '#', 'admin', '2026-02-19 00:04:45', '', NULL, '财务汇总统计权限');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2623, '待办事项', 2600, 16, '', '', NULL, '', 1, 0, 'F', '0', '0', 'wms:statistics:tasks', '#', 'admin', '2026-02-19 00:04:45', '', NULL, '待办事项查询权限');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2624, '租户管理', 1, 11, 'tenant', 'system/tenant/index', NULL, '', 1, 0, 'C', '0', '0', 'system:tenant:list', 'peoples', 'admin', '2026-03-01 15:58:17', '', NULL, '租户管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2625, '租户查询', 2624, 1, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'system:tenant:query', '#', 'admin', '2026-03-01 15:58:17', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2626, '租户新增', 2624, 2, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'system:tenant:add', '#', 'admin', '2026-03-01 15:58:17', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2627, '租户修改', 2624, 3, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'system:tenant:edit', '#', 'admin', '2026-03-01 15:58:17', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2628, '租户删除', 2624, 4, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'system:tenant:remove', '#', 'admin', '2026-03-01 15:58:17', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2629, '租户导出', 2624, 5, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'system:tenant:export', '#', 'admin', '2026-03-01 15:58:17', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2630, '商品导入', 2041, 4, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'wms:goods:import', '#', 'admin', '2026-03-14 13:31:04', '', NULL, '商品导入功能');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2631, '下载模板', 2041, 5, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'wms:goods:importTemplate', '#', 'admin', '2026-03-14 13:31:04', '', NULL, '下载商品导入模板');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2632, '数据迁移中心', 2000, 93, 'basicImport', 'wms/basicImport/index', NULL, 'WmsBasicImport', 1, 0, 'C', '0', '0', 'wms:basicImport:query', 'upload', 'admin', '2026-03-14 14:31:04', '', NULL, '基础数据导入统一入口');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2633, '审核策略配置', 2000, 91, 'approvePolicy', 'wms/approvePolicy/index', NULL, 'WmsApprovePolicy', 1, 0, 'C', '0', '0', 'wms:approvePolicy:query', 'edit', 'admin', '2026-03-16 10:47:52', '', NULL, '租户单据审核策略配置');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2634, '审核策略修改', 2633, 1, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'wms:approvePolicy:edit', '#', 'admin', '2026-03-16 10:47:52', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2635, '审核日志查询', 2000, 92, 'approveLog', 'wms/approveLog/index', NULL, 'WmsApproveLog', 1, 0, 'C', '0', '0', 'wms:approveLog:query', 'form', 'admin', '2026-03-16 10:47:52', '', NULL, '单据审核日志查询');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2636, '打印', 2208, 8, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'wms:purchaseIn:print', '#', 'admin', '2026-03-17 00:01:24', '', NULL, '采购入库打印按钮');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2637, '打印', 2228, 8, '', NULL, NULL, '', 1, 0, 'F', '0', '0', 'wms:salesOut:print', '#', 'admin', '2026-03-17 00:01:24', '', NULL, '销售出库打印按钮');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2638, '企业设置', 1, 11, 'tenantPrintProfile', 'system/tenant/printProfile', NULL, 'TenantPrintProfile', 1, 0, 'C', '0', '0', 'system:tenant:printProfile', 'edit', 'admin', '2026-03-17 00:01:24', '', NULL, '当前租户企业设置页面（打印资料 + AI 配置）');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2639, 'AI助手', 0, 70, 'aiAssistant', NULL, NULL, 'AiAssistantRoot', 1, 0, 'M', '0', '0', '', 'message', 'admin', '2026-03-25 19:15:36', 'admin', '2026-10-28 22:20:51', '全局AI助手目录');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2640, 'AI助手', 2639, 1, 'aiAssistantPc', 'wms/aiAssistantPc/index', NULL, 'WmsAiAssistantPc', 1, 0, 'C', '0', '0', 'ai:assistantPc:query', 'message', 'admin', '2026-03-25 19:15:36', '', NULL, 'PC端AI助手V2（采购订单/采购入库）');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2641, '业务操作日志', 1, 10, 'wmsBizLog', 'wms/wmsBizLog/index', NULL, '', 1, 0, 'C', '0', '0', 'wms:bizLog:list', 'log', 'admin', '2026-03-29 08:42:39', '', NULL, 'WMS业务操作日志菜单');
COMMIT;

-- ----------------------------
-- Table structure for sys_notice
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice` (
  `notice_id` int NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  `notice_title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公告标题',
  `notice_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公告类型（1通知 2公告）',
  `notice_content` longblob COMMENT '公告内容',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '公告状态（0正常 1关闭）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`notice_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='通知公告表';

-- ----------------------------
-- Records of sys_notice
-- ----------------------------
BEGIN;
INSERT INTO `sys_notice` (`notice_id`, `tenant_id`, `notice_title`, `notice_type`, `notice_content`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1, 1, '温馨提醒：2018-07-01 若依新版本发布啦', '2', 0xE696B0E78988E69CACE58685E5AEB9, '0', 'admin', '2026-01-23 11:53:06', '', NULL, '管理员');
INSERT INTO `sys_notice` (`notice_id`, `tenant_id`, `notice_title`, `notice_type`, `notice_content`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2, 1, '维护通知：2018-07-01 若依系统凌晨维护', '1', 0xE7BBB4E68AA4E58685E5AEB9, '0', 'admin', '2026-01-23 11:53:06', '', NULL, '管理员');
COMMIT;

-- ----------------------------
-- Table structure for sys_oper_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_oper_log`;
CREATE TABLE `sys_oper_log` (
  `oper_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '模块标题',
  `business_type` int DEFAULT '0' COMMENT '业务类型（0其它 1新增 2修改 3删除）',
  `method` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '方法名称',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '请求方式',
  `operator_type` int DEFAULT '0' COMMENT '操作类别（0其它 1后台用户 2手机端用户）',
  `oper_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '操作人员',
  `dept_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '部门名称',
  `oper_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '请求URL',
  `oper_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '主机地址',
  `oper_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '操作地点',
  `oper_param` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '请求参数',
  `json_result` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '返回参数',
  `status` int DEFAULT '0' COMMENT '操作状态（0正常 1异常）',
  `error_msg` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '错误消息',
  `oper_time` datetime DEFAULT NULL COMMENT '操作时间',
  `cost_time` bigint DEFAULT '0' COMMENT '消耗时间',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  PRIMARY KEY (`oper_id`) USING BTREE,
  KEY `idx_sys_oper_log_bt` (`business_type`) USING BTREE,
  KEY `idx_sys_oper_log_s` (`status`) USING BTREE,
  KEY `idx_sys_oper_log_ot` (`oper_time`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=322 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='操作日志记录';

-- ----------------------------
-- Records of sys_oper_log
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for sys_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_post`;
CREATE TABLE `sys_post` (
  `post_id` bigint NOT NULL AUTO_INCREMENT COMMENT '岗位ID',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  `post_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '岗位编码',
  `post_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '岗位名称',
  `post_sort` int NOT NULL COMMENT '显示顺序',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`post_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='岗位信息表';

-- ----------------------------
-- Records of sys_post
-- ----------------------------
BEGIN;
INSERT INTO `sys_post` (`post_id`, `tenant_id`, `post_code`, `post_name`, `post_sort`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1, 1, 'ceo', '董事长', 1, '0', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_post` (`post_id`, `tenant_id`, `post_code`, `post_name`, `post_sort`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2, 1, 'se', '项目经理', 2, '0', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_post` (`post_id`, `tenant_id`, `post_code`, `post_name`, `post_sort`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3, 1, 'hr', '人力资源', 3, '0', 'admin', '2026-01-23 11:53:05', '', NULL, '');
INSERT INTO `sys_post` (`post_id`, `tenant_id`, `post_code`, `post_name`, `post_sort`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4, 1, 'user', '普通员工', 4, '0', 'admin', '2026-01-23 11:53:05', '', NULL, '');
COMMIT;

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `role_id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  `role_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称',
  `role_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色权限字符串',
  `role_sort` int NOT NULL COMMENT '显示顺序',
  `data_scope` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '1' COMMENT '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）',
  `menu_check_strictly` tinyint(1) DEFAULT '1' COMMENT '菜单树选择项是否关联显示',
  `dept_check_strictly` tinyint(1) DEFAULT '1' COMMENT '部门树选择项是否关联显示',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`role_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_role_tenant` (`tenant_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=149 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='角色信息表';

-- ----------------------------
-- Records of sys_role
-- ----------------------------
BEGIN;
INSERT INTO `sys_role` (`role_id`, `tenant_id`, `role_name`, `role_key`, `role_sort`, `data_scope`, `menu_check_strictly`, `dept_check_strictly`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1, 1, '超级管理员', 'admin', 1, '1', 1, 1, '0', '0', 'admin', '2026-01-23 11:53:05', '', NULL, '超级管理员');
INSERT INTO `sys_role` (`role_id`, `tenant_id`, `role_name`, `role_key`, `role_sort`, `data_scope`, `menu_check_strictly`, `dept_check_strictly`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2, 1, '普通角色', 'common', 2, '4', 1, 1, '0', '0', 'admin', '2026-01-23 11:53:05', '', '2026-02-05 10:39:23', '普通角色');
INSERT INTO `sys_role` (`role_id`, `tenant_id`, `role_name`, `role_key`, `role_sort`, `data_scope`, `menu_check_strictly`, `dept_check_strictly`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (143, 44, 'admin_test管理员', 'tenant_admin_44', 1, '1', 1, 1, '0', '0', 'system', '2026-06-25 20:36:52', 'admin', '2026-10-28 21:39:31', '租户默认管理员角色');
COMMIT;

-- ----------------------------
-- Table structure for sys_role_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_dept`;
CREATE TABLE `sys_role_dept` (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `dept_id` bigint NOT NULL COMMENT '部门ID',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  PRIMARY KEY (`role_id`,`dept_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='角色和部门关联表';

-- ----------------------------
-- Records of sys_role_dept
-- ----------------------------
BEGIN;
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`, `tenant_id`) VALUES (102, 205, 5);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`, `tenant_id`) VALUES (103, 207, 6);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`, `tenant_id`) VALUES (104, 208, 7);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`, `tenant_id`) VALUES (105, 209, 8);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`, `tenant_id`) VALUES (107, 211, 9);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`, `tenant_id`) VALUES (108, 212, 10);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`, `tenant_id`) VALUES (109, 213, 11);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`, `tenant_id`) VALUES (110, 214, 12);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`, `tenant_id`) VALUES (111, 215, 13);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`, `tenant_id`) VALUES (112, 216, 14);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`, `tenant_id`) VALUES (113, 217, 15);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`, `tenant_id`) VALUES (114, 218, 16);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`, `tenant_id`) VALUES (115, 219, 17);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`, `tenant_id`) VALUES (116, 220, 18);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`, `tenant_id`) VALUES (117, 221, 19);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`, `tenant_id`) VALUES (119, 222, 20);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`, `tenant_id`) VALUES (120, 223, 21);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`, `tenant_id`) VALUES (121, 224, 22);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`, `tenant_id`) VALUES (122, 225, 23);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`, `tenant_id`) VALUES (123, 226, 24);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`, `tenant_id`) VALUES (124, 227, 25);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`, `tenant_id`) VALUES (125, 228, 26);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`, `tenant_id`) VALUES (126, 229, 27);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`, `tenant_id`) VALUES (127, 230, 28);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`, `tenant_id`) VALUES (128, 231, 29);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`, `tenant_id`) VALUES (129, 232, 30);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`, `tenant_id`) VALUES (130, 233, 31);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`, `tenant_id`) VALUES (131, 234, 32);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`, `tenant_id`) VALUES (132, 235, 33);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`, `tenant_id`) VALUES (133, 236, 34);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`, `tenant_id`) VALUES (134, 237, 35);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`, `tenant_id`) VALUES (135, 238, 36);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`, `tenant_id`) VALUES (136, 239, 37);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`, `tenant_id`) VALUES (137, 240, 38);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`, `tenant_id`) VALUES (138, 241, 39);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`, `tenant_id`) VALUES (139, 242, 40);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`, `tenant_id`) VALUES (140, 243, 41);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`, `tenant_id`) VALUES (141, 244, 42);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`, `tenant_id`) VALUES (142, 245, 43);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`, `tenant_id`) VALUES (143, 246, 44);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`, `tenant_id`) VALUES (144, 247, 45);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`, `tenant_id`) VALUES (145, 248, 46);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`, `tenant_id`) VALUES (146, 249, 47);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`, `tenant_id`) VALUES (147, 250, 48);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`, `tenant_id`) VALUES (148, 251, 49);
COMMIT;

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  PRIMARY KEY (`role_id`,`menu_id`,`tenant_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='角色和菜单关联表';

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
BEGIN;
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (1, 2607, 0);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (1, 2608, 0);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (1, 2609, 0);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (1, 2610, 0);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (1, 2611, 0);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (1, 2612, 0);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (1, 2613, 0);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (1, 2614, 0);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (1, 2615, 0);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (1, 2616, 0);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (1, 2617, 0);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (1, 2618, 0);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (1, 2619, 0);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (1, 2620, 0);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (1, 2621, 0);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (1, 2622, 0);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (1, 2623, 0);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (2, 2607, 0);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (2, 2608, 0);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (2, 2609, 0);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (2, 2610, 0);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (2, 2611, 0);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (2, 2612, 0);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (2, 2613, 0);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (2, 2614, 0);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (2, 2615, 0);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (2, 2616, 0);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (2, 2617, 0);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (2, 2618, 0);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (2, 2619, 0);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (2, 2620, 0);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (2, 2621, 0);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (2, 2622, 0);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (2, 2623, 0);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (1, 2624, 1);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (1, 2625, 1);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (1, 2626, 1);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (1, 2627, 1);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (1, 2628, 1);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (1, 2629, 1);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (1, 2630, 1);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (1, 2631, 1);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (1, 2632, 1);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (1, 2633, 1);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (1, 2634, 1);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (1, 2635, 1);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (1, 2636, 1);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (1, 2637, 1);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (1, 2638, 1);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (1, 2640, 1);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (102, 2636, 1);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (102, 2637, 1);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (102, 2638, 1);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 1, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2000, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2001, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2002, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2003, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2004, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2005, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2006, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2007, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2008, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2009, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2010, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2011, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2012, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2013, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2014, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2015, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2021, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2022, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2023, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2024, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2025, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2031, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2032, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2033, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2034, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2035, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2041, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2042, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2043, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2044, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2045, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2046, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2047, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2048, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2049, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2050, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2051, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2052, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2053, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2054, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2055, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2100, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2101, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2110, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2111, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2113, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2114, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2115, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2116, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2121, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2122, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2123, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2124, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2125, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2126, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2200, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2201, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2202, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2203, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2204, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2205, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2206, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2207, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2208, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2209, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2210, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2211, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2212, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2213, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2214, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2215, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2216, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2217, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2218, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2219, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2220, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2221, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2222, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2223, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2224, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2225, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2226, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2227, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2228, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2229, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2230, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2231, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2232, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2233, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2234, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2235, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2236, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2237, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2238, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2239, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2300, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2301, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2302, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2303, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2304, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2305, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2306, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2307, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2308, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2309, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2310, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2311, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2312, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2313, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2314, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2315, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2316, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2317, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2318, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2319, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2320, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2321, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2322, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2323, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2324, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2325, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2328, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2329, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2330, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2331, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2400, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2600, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2601, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2602, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2603, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2604, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2605, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2606, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2607, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2608, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2609, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2610, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2611, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2612, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2613, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2614, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2615, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2616, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2617, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2618, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2619, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2620, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2621, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2622, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2623, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2630, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2631, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2632, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2633, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2634, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2635, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2636, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2637, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2639, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2640, 44);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`, `tenant_id`) VALUES (143, 2641, 44);
COMMIT;

-- ----------------------------
-- Table structure for sys_tenant
-- ----------------------------
DROP TABLE IF EXISTS `sys_tenant`;
CREATE TABLE `sys_tenant` (
  `tenant_id` bigint NOT NULL AUTO_INCREMENT COMMENT '租户编号',
  `tenant_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '租户名称',
  `contact_user` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '联系电话',
  `account_count` int DEFAULT '0' COMMENT '账号数量',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '租户状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `edition` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'free' COMMENT '版本类型（free免费版/pro专业版/enterprise企业版）',
  `ai_enabled` tinyint(1) DEFAULT '0' COMMENT 'AI功能开关（0禁用 1启用）',
  `voice_enabled` tinyint(1) DEFAULT '0' COMMENT '语音功能开关（0禁用 1启用）',
  `ocr_enabled` tinyint(1) DEFAULT '0' COMMENT 'OCR功能开关（0禁用 1启用）',
  `ai_points` int NOT NULL DEFAULT '0' COMMENT 'AI点数余额',
  `ai_points_total_charged` int NOT NULL DEFAULT '0' COMMENT '累计充值点数',
  `ai_points_total_used` int NOT NULL DEFAULT '0' COMMENT '累计消耗点数',
  `scan_enabled` tinyint(1) DEFAULT '0' COMMENT '扫码功能开关（0禁用 1启用）',
  `print_company_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '打印抬头公司名称',
  `print_company_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '打印抬头公司地址',
  `print_company_phone` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '打印抬头联系电话',
  `print_logo_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '打印抬头Logo路径',
  `print_font_scale` decimal(3,1) DEFAULT '1.0' COMMENT '打印字体缩放倍数（0或1=原始大小）',
  `print_page_size` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'A4' COMMENT '打印纸张大小（A4/SLIP_241X279/SLIP_241X140）',
  `ai_api_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'AI API Key（租户级自定义，空则使用全局配置）',
  `ai_model` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'AI 模型名称（租户级自定义，空则使用全局配置）',
  `ai_api_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'AI API 地址（租户级自定义，空则使用全局配置）',
  `ai_vision_model` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'AI 视觉模型名称（截图识别，租户级自定义，空则使用全局配置）',
  `ai_voice_model` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'AI 语音模型名称（语音识别，租户级自定义，空则使用全局配置）',
  `monthly_quota_text` int NOT NULL DEFAULT '0' COMMENT '每月文字解析限额',
  `monthly_quota_voice` int NOT NULL DEFAULT '0' COMMENT '每月语音识别限额',
  `monthly_quota_ocr` int NOT NULL DEFAULT '0' COMMENT '每月OCR识别限额',
  `annual_quota_text` int NOT NULL DEFAULT '0' COMMENT '每年文字解析限额',
  `annual_quota_voice` int NOT NULL DEFAULT '0' COMMENT '每年语音识别限额',
  `annual_quota_ocr` int NOT NULL DEFAULT '0' COMMENT '每年OCR识别限额',
  `used_count_text` int NOT NULL DEFAULT '0' COMMENT '本月已用文字解析次数',
  `used_count_voice` int NOT NULL DEFAULT '0' COMMENT '本月已用语音识别次数',
  `used_count_ocr` int NOT NULL DEFAULT '0' COMMENT '本月已用OCR识别次数',
  `used_annual_text` int NOT NULL DEFAULT '0' COMMENT '本年已用文字解析次数',
  `used_annual_voice` int NOT NULL DEFAULT '0' COMMENT '本年已用语音识别次数',
  `used_annual_ocr` int NOT NULL DEFAULT '0' COMMENT '本年已用OCR识别次数',
  `quota_reset_date` datetime DEFAULT NULL COMMENT '月度限额重置时间',
  `quota_annual_reset_date` datetime DEFAULT NULL COMMENT '年度限额重置时间',
  `subscription_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '订阅类型：monthly月付/yearly年付，NULL表示未订阅(试用中或已过期)',
  `subscription_expire_time` datetime DEFAULT NULL COMMENT '订阅到期时间',
  `apple_original_transaction_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'Apple订阅原始交易ID（用于续订追踪）',
  `cancel_requested` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'Whether the tenant has submitted account cancel request',
  `cancel_requested_at` datetime DEFAULT NULL COMMENT 'Account cancel request submit time',
  `cancel_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'Account cancel request reason',
  PRIMARY KEY (`tenant_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=50 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='租户表';

-- ----------------------------
-- Records of sys_tenant
-- ----------------------------
BEGIN;
INSERT INTO `sys_tenant` (`tenant_id`, `tenant_name`, `contact_user`, `contact_phone`, `account_count`, `expire_time`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`, `edition`, `ai_enabled`, `voice_enabled`, `ocr_enabled`, `ai_points`, `ai_points_total_charged`, `ai_points_total_used`, `scan_enabled`, `print_company_name`, `print_company_address`, `print_company_phone`, `print_logo_path`, `print_font_scale`, `print_page_size`, `ai_api_key`, `ai_model`, `ai_api_url`, `ai_vision_model`, `ai_voice_model`, `monthly_quota_text`, `monthly_quota_voice`, `monthly_quota_ocr`, `annual_quota_text`, `annual_quota_voice`, `annual_quota_ocr`, `used_count_text`, `used_count_voice`, `used_count_ocr`, `used_annual_text`, `used_annual_voice`, `used_annual_ocr`, `quota_reset_date`, `quota_annual_reset_date`, `subscription_type`, `subscription_expire_time`, `apple_original_transaction_id`, `cancel_requested`, `cancel_requested_at`, `cancel_reason`) VALUES (1, '默认租户', '系统管理员', '13800138000', 100, NULL, '0', '0', 'admin', '2026-02-03 23:40:39', '', NULL, '系统默认租户，用于初始化数据', 'pro', 1, 1, 0, 50, 50, 0, 1, NULL, NULL, NULL, NULL, 1.0, 'A4', NULL, NULL, NULL, NULL, NULL, 1500, 60, 0, 15000, 600, 0, 0, 0, 0, 0, 0, 0, '2026-10-01 00:00:00', '2026-01-01 00:00:00', NULL, NULL, NULL, 0, NULL, NULL);
INSERT INTO `sys_tenant` (`tenant_id`, `tenant_name`, `contact_user`, `contact_phone`, `account_count`, `expire_time`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`, `edition`, `ai_enabled`, `voice_enabled`, `ocr_enabled`, `ai_points`, `ai_points_total_charged`, `ai_points_total_used`, `scan_enabled`, `print_company_name`, `print_company_address`, `print_company_phone`, `print_logo_path`, `print_font_scale`, `print_page_size`, `ai_api_key`, `ai_model`, `ai_api_url`, `ai_vision_model`, `ai_voice_model`, `monthly_quota_text`, `monthly_quota_voice`, `monthly_quota_ocr`, `annual_quota_text`, `annual_quota_voice`, `annual_quota_ocr`, `used_count_text`, `used_count_voice`, `used_count_ocr`, `used_annual_text`, `used_annual_voice`, `used_annual_ocr`, `quota_reset_date`, `quota_annual_reset_date`, `subscription_type`, `subscription_expire_time`, `apple_original_transaction_id`, `cancel_requested`, `cancel_requested_at`, `cancel_reason`) VALUES (44, 'tenant_1782391012549', 'admin_test', '13900001111', 1, '2029-07-01 20:36:53', '0', '0', '', '2026-06-25 20:36:52', 'admin', '2026-07-03 10:40:27', NULL, 'basic', 1, 1, 1, 103, 150, 47, 1, '演示科技有限公司', '演示市演示区', '13800000001', NULL, 1.0, 'A4', 'sk-***********', 'qwen3.7-flash', 'https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions', 'qwen3.8-omni-flash', 'fun-asr-realtime', 1500, 60, 0, 18000, 720, 0, 0, 0, 0, 0, 0, 0, '2026-10-01 00:00:00', NULL, NULL, NULL, NULL, 0, NULL, NULL);
COMMIT;

-- ----------------------------
-- Table structure for sys_tenant_bill
-- ----------------------------
DROP TABLE IF EXISTS `sys_tenant_bill`;
CREATE TABLE `sys_tenant_bill` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `request_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '请求幂等ID',
  `provider_request_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '第三方请求ID',
  `bill_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '账单类型: quota/upgrade/extra',
  `quota_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '限额类型: text/voice/ocr',
  `quantity` int NOT NULL DEFAULT '0' COMMENT '数量',
  `unit_price` decimal(10,4) DEFAULT NULL COMMENT '单价',
  `total_amount` decimal(10,2) DEFAULT NULL COMMENT '总金额',
  `success_flag` tinyint NOT NULL DEFAULT '1' COMMENT '是否成功: 1成功 0失败',
  `error_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '失败错误码',
  `status` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'paid' COMMENT '状态: unpaid/paid/refunded',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_request_id` (`request_id`) USING BTREE,
  KEY `idx_tenant_time` (`tenant_id`,`create_time`) USING BTREE,
  KEY `idx_tenant_quota_time` (`tenant_id`,`quota_type`,`create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='租户账单记录表';

-- ----------------------------
-- Records of sys_tenant_bill
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  `dept_id` bigint DEFAULT NULL COMMENT '部门ID',
  `user_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '用户账号',
  `nick_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '用户昵称',
  `user_type` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '00' COMMENT '用户类型（00系统用户）',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '用户邮箱',
  `phonenumber` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '手机号码',
  `sex` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '用户性别（0男 1女 2未知）',
  `avatar` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '头像地址',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '密码',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '账号状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `login_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '最后登录IP',
  `login_date` datetime DEFAULT NULL COMMENT '最后登录时间',
  `pwd_update_date` datetime DEFAULT NULL COMMENT '密码最后更新时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `permission_version` int DEFAULT '0' COMMENT '权限版本号，修改权限时递增，用于强制刷新本地权限缓存',
  PRIMARY KEY (`user_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_user_tenant` (`tenant_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=152 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='用户信息表';

-- ----------------------------
-- Records of sys_user
-- ----------------------------
BEGIN;
INSERT INTO `sys_user` (`user_id`, `tenant_id`, `dept_id`, `user_name`, `nick_name`, `user_type`, `email`, `phonenumber`, `sex`, `avatar`, `password`, `status`, `del_flag`, `login_ip`, `login_date`, `pwd_update_date`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`, `permission_version`) VALUES (1, 1, 103, 'admin', '若依', '00', 'ry@163.com', '15888888888', '1', '', '$2a$10$zxfaMxM8gP3tVrKifBd.su7n4cFWnRU.C5bx34HF9oQJZy9NVCbZC', '0', '0', '127.0.0.1', '2026-10-29 01:03:44', '2026-10-28 22:19:37', 'admin', '2026-01-23 11:53:05', '', '2026-10-28 22:19:37', '管理员', 7);
INSERT INTO `sys_user` (`user_id`, `tenant_id`, `dept_id`, `user_name`, `nick_name`, `user_type`, `email`, `phonenumber`, `sex`, `avatar`, `password`, `status`, `del_flag`, `login_ip`, `login_date`, `pwd_update_date`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`, `permission_version`) VALUES (108, 44, 246, 'zhaoyun', '赵云', '00', 'demo@ruoyi-ai-wms.com', '13800000002', '0', '/profile/avatar/2026/10/28/f0d2639a414346389158f1c62ee8d9ed.jpeg', '$2a$10$nRReW//TcbnEOfrz266uvOIzKXns0qTzlP/oB4dgn47s//5yXoU62', '0', '0', '127.0.0.1', '2026-10-29 01:13:48', '2026-10-28 22:57:43', 'Ui333', '2026-02-05 15:06:54', 'admin', '2026-10-28 22:57:43', NULL, 3);
COMMIT;

-- ----------------------------
-- Table structure for sys_user_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_post`;
CREATE TABLE `sys_user_post` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `post_id` bigint NOT NULL COMMENT '岗位ID',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  PRIMARY KEY (`user_id`,`post_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='用户与岗位关联表';

-- ----------------------------
-- Records of sys_user_post
-- ----------------------------
BEGIN;
INSERT INTO `sys_user_post` (`user_id`, `post_id`, `tenant_id`) VALUES (1, 1, 1);
COMMIT;

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  PRIMARY KEY (`user_id`,`role_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='用户和角色关联表';

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
BEGIN;
INSERT INTO `sys_user_role` (`user_id`, `role_id`, `tenant_id`) VALUES (1, 1, 1);
INSERT INTO `sys_user_role` (`user_id`, `role_id`, `tenant_id`) VALUES (108, 143, 1);
COMMIT;

-- ----------------------------
-- Table structure for user_onboarding_progress
-- ----------------------------
DROP TABLE IF EXISTS `user_onboarding_progress`;
CREATE TABLE `user_onboarding_progress` (
  `id` bigint NOT NULL COMMENT '主键 ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `task_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务编码：add_goods, purchase_in, sales_out, view_report',
  `is_completed` tinyint DEFAULT '0' COMMENT '是否完成：0-未完成，1-已完成',
  `completed_time` datetime DEFAULT NULL COMMENT '完成时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_tenant_user_task` (`tenant_id`,`user_id`,`task_code`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_task_code` (`task_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='新手引导任务进度表';

-- ----------------------------
-- Records of user_onboarding_progress
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_account
-- ----------------------------
DROP TABLE IF EXISTS `wms_account`;
CREATE TABLE `wms_account` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  `account_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '账户编码',
  `account_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '账户名称',
  `account_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '账户类型（1现金 2银行 3支付宝 4微信）',
  `balance` decimal(15,2) DEFAULT '0.00' COMMENT '账户余额',
  `bank_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '开户银行',
  `account_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '银行账号',
  `holder` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '持卡人',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '鍒犻櫎鏍囧織锛?鏈?垹闄?1宸插垹闄わ級',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_tenant_account_code` (`tenant_id`,`account_code`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=121 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='结算账户表';

-- ----------------------------
-- Records of wms_account
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_accounts_payable
-- ----------------------------
DROP TABLE IF EXISTS `wms_accounts_payable`;
CREATE TABLE `wms_accounts_payable` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  `supplier_id` bigint DEFAULT NULL,
  `customer_id` bigint DEFAULT NULL COMMENT '客户ID（销售退货时使用）',
  `bill_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '单据类型（1采购订单 2采购入库）',
  `bill_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '单据编号',
  `bill_id` bigint NOT NULL COMMENT '单据ID',
  `bill_amount` decimal(15,2) NOT NULL COMMENT '单据金额',
  `paid_amount` decimal(15,2) DEFAULT '0.00' COMMENT '已付金额',
  `ap_amount` decimal(15,2) NOT NULL COMMENT '应付金额',
  `ap_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '状态（0未结清 1已结清）',
  `bill_date` date NOT NULL COMMENT '单据日期',
  `due_date` date DEFAULT NULL COMMENT '到期日期',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `payment_proof` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '付款凭证',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标记(0正常,1删除)',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_supplier_id` (`supplier_id`) USING BTREE,
  KEY `idx_bill_no` (`bill_no`) USING BTREE,
  KEY `idx_ap_status` (`ap_status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1407 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='应付账款表';

-- ----------------------------
-- Records of wms_accounts_payable
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_accounts_receivable
-- ----------------------------
DROP TABLE IF EXISTS `wms_accounts_receivable`;
CREATE TABLE `wms_accounts_receivable` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  `customer_id` bigint DEFAULT NULL,
  `supplier_id` bigint DEFAULT NULL COMMENT '供应商ID（采购退货时使用）',
  `bill_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '单据类型（1销售订单 2销售出库）',
  `bill_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '单据编号',
  `bill_id` bigint NOT NULL COMMENT '单据ID',
  `bill_amount` decimal(15,2) NOT NULL COMMENT '单据金额',
  `received_amount` decimal(15,2) DEFAULT '0.00' COMMENT '已收金额',
  `ar_amount` decimal(15,2) NOT NULL COMMENT '应收金额',
  `ar_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '状态（0未结清 1已结清）',
  `bill_date` date NOT NULL COMMENT '单据日期',
  `due_date` date DEFAULT NULL COMMENT '到期日期',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `collection_proof` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '收款凭证',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标记(0正常,1删除)',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_customer_id` (`customer_id`) USING BTREE,
  KEY `idx_bill_no` (`bill_no`) USING BTREE,
  KEY `idx_ar_status` (`ar_status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2263 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='应收账款表';

-- ----------------------------
-- Records of wms_accounts_receivable
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_ai_chat_log
-- ----------------------------
DROP TABLE IF EXISTS `wms_ai_chat_log`;
CREATE TABLE `wms_ai_chat_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `conversation_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '会话ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `operation_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '操作类型',
  `input_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '输入类型(voice/text)',
  `input_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '输入内容',
  `voice_file_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '语音文件路径',
  `response_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '响应内容',
  `result_status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '处理状态(success/failed)',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '错误信息',
  `process_time` int DEFAULT NULL COMMENT '处理耗时(毫秒)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标记(0正常,1删除)',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_conversation_id` (`conversation_id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1136 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='AI会话日志表';

-- ----------------------------
-- Records of wms_ai_chat_log
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_approve_log
-- ----------------------------
DROP TABLE IF EXISTS `wms_approve_log`;
CREATE TABLE `wms_approve_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `tenant_id` bigint NOT NULL COMMENT 'Tenant ID',
  `bill_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'Bill type enum',
  `bill_id` bigint NOT NULL COMMENT 'Bill ID',
  `bill_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'Bill no',
  `action_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'APPROVE or CANCEL_APPROVE',
  `approve_mode` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'MANUAL or AUTO_POLICY',
  `result` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'SUCCESS or FAIL',
  `operator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'Operator username',
  `operator_id` bigint DEFAULT NULL COMMENT 'Operator user ID',
  `policy_snapshot` json DEFAULT NULL COMMENT 'Policy snapshot json',
  `error_msg` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'Error message if failed',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'Remark',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标记(0正常,1删除)',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_tenant_bill` (`tenant_id`,`bill_type`,`bill_id`) USING BTREE,
  KEY `idx_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=381 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='Approve action log';

-- ----------------------------
-- Records of wms_approve_log
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_bill_approve_policy
-- ----------------------------
DROP TABLE IF EXISTS `wms_bill_approve_policy`;
CREATE TABLE `wms_bill_approve_policy` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
  `tenant_id` bigint NOT NULL COMMENT 'Tenant ID',
  `bill_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'Bill type enum',
  `need_approve` tinyint(1) NOT NULL DEFAULT '1' COMMENT '1 require approve, 0 auto approve',
  `auto_print` tinyint(1) NOT NULL DEFAULT '0' COMMENT '免审时是否自动打印小票(1是,0否)',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0' COMMENT '0 enabled, 1 disabled',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'Remark',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT 'Created by',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT 'Updated by',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标记(0正常,1删除)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_tenant_bill_type` (`tenant_id`,`bill_type`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=392 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='Tenant bill approve policy';

-- ----------------------------
-- Records of wms_bill_approve_policy
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_biz_oper_log
-- ----------------------------
DROP TABLE IF EXISTS `wms_biz_oper_log`;
CREATE TABLE `wms_biz_oper_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户ID',
  `biz_module` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '业务模块(如：采购订单、销售出库)',
  `biz_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '操作类型(CREATE/UPDATE/DELETE/APPROVE/CANCEL_APPROVE/EXPORT/IMPORT/OTHER)',
  `biz_table` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '业务表名(wms_purchase_order等)',
  `biz_id` bigint DEFAULT NULL COMMENT '业务数据ID',
  `biz_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '业务单号',
  `oper_user_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `oper_user_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '操作人用户名',
  `oper_ip` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '操作IP',
  `oper_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '请求URL',
  `oper_method` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '请求方法(GET/POST/PUT/DELETE)',
  `oper_param` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '请求参数(JSON)',
  `oper_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '返回结果(JSON)',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '操作状态(0成功,1失败)',
  `error_msg` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '错误信息',
  `cost_time` bigint DEFAULT '0' COMMENT '耗时(毫秒)',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标记(0正常,1删除)',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_biz_table` (`biz_table`) USING BTREE,
  KEY `idx_biz_id` (`biz_id`) USING BTREE,
  KEY `idx_oper_user_id` (`oper_user_id`) USING BTREE,
  KEY `idx_create_time` (`create_time`) USING BTREE,
  KEY `idx_biz_no` (`biz_no`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2517 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='WMS业务操作日志';

-- ----------------------------
-- Records of wms_biz_oper_log
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_customer
-- ----------------------------
DROP TABLE IF EXISTS `wms_customer`;
CREATE TABLE `wms_customer` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  `is_default` tinyint DEFAULT '0' COMMENT '是否默认客户：0=否，1=是',
  `customer_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '客户编码',
  `customer_name` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '客户名称',
  `contact` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '联系人',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '联系电话',
  `mobile` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '手机号码',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '邮箱',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '地址',
  `customer_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '1' COMMENT '客户类型（1批发 2零售）',
  `credit_limit` decimal(15,2) DEFAULT '0.00' COMMENT '信用额度',
  `current_arrears` decimal(15,2) DEFAULT '0.00' COMMENT '当前欠款',
  `settlement_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '1' COMMENT '结算方式（1现结 2账期）',
  `payment_days` int DEFAULT '0' COMMENT '账期天数',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '鍒犻櫎鏍囧織锛?鏈?垹闄?1宸插垹闄わ級',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `default_flag` tinyint GENERATED ALWAYS AS ((case when (`is_default` = 1) then 1 else NULL end)) VIRTUAL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_tenant_customer_code` (`tenant_id`,`customer_code`) USING BTREE,
  UNIQUE KEY `idx_customer_code` (`customer_code`) USING BTREE,
  UNIQUE KEY `idx_tenant_default` (`tenant_id`,`default_flag`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=170 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='客户表';

-- ----------------------------
-- Records of wms_customer
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_expense
-- ----------------------------
DROP TABLE IF EXISTS `wms_expense`;
CREATE TABLE `wms_expense` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  `expense_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '费用单号',
  `expense_type` bigint DEFAULT NULL COMMENT '费用类型ID',
  `expense_date` date NOT NULL COMMENT '费用日期',
  `amount` decimal(15,2) NOT NULL COMMENT '费用金额',
  `account_id` bigint NOT NULL COMMENT '付款账户ID',
  `handler` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '经办人',
  `approve_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '审核状态（0待审核 1已审核）',
  `approve_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '审核人',
  `approve_time` datetime DEFAULT NULL COMMENT '审核时间',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '鍒犻櫎鏍囧織锛?鏈?垹闄?1宸插垹闄わ級',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_tenant_expense_no` (`tenant_id`,`expense_no`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_expense_type` (`expense_type`) USING BTREE,
  KEY `idx_expense_date` (`expense_date`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=104 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='费用表';

-- ----------------------------
-- Records of wms_expense
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_expense_type
-- ----------------------------
DROP TABLE IF EXISTS `wms_expense_type`;
CREATE TABLE `wms_expense_type` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  `type_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类型名称',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '鍒犻櫎鏍囧織锛?鏈?垹闄?1宸插垹闄わ級',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=111 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='费用类型表';

-- ----------------------------
-- Records of wms_expense_type
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_finance_log
-- ----------------------------
DROP TABLE IF EXISTS `wms_finance_log`;
CREATE TABLE `wms_finance_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `voucher_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'voucher no',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  `bill_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '单据类型（1收款 2付款 3费用）',
  `bill_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '单据编号',
  `bill_id` bigint DEFAULT NULL COMMENT '单据ID',
  `business_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务类型（1采购 2销售 3退货 4费用）',
  `original_bill_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'original bill type',
  `partner_id` bigint DEFAULT NULL COMMENT '往来单位ID',
  `partner_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '1' COMMENT '往来单位类型（1供应商 2客户）',
  `account_id` bigint NOT NULL COMMENT '账户ID',
  `before_balance` decimal(15,2) DEFAULT NULL COMMENT 'balance before',
  `after_balance` decimal(15,2) DEFAULT NULL COMMENT 'balance after',
  `amount` decimal(15,2) NOT NULL COMMENT '金额',
  `direction` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '方向（1收入 2支出）',
  `payment_method` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '1' COMMENT '支付方式（1现金 2银行 3支付宝 4微信）',
  `bill_date` date NOT NULL COMMENT '单据日期',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `summary` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'summary',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标记(0正常,1删除)',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_bill_no` (`bill_no`) USING BTREE,
  KEY `idx_account_id` (`account_id`) USING BTREE,
  KEY `idx_partner_id` (`partner_id`) USING BTREE,
  KEY `idx_bill_date` (`bill_date`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2544 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='收支流水表';

-- ----------------------------
-- Records of wms_finance_log
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_goods
-- ----------------------------
DROP TABLE IF EXISTS `wms_goods`;
CREATE TABLE `wms_goods` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  `goods_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品编码',
  `goods_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称',
  `category_id` bigint DEFAULT NULL COMMENT '分类ID',
  `unit_id` bigint DEFAULT NULL COMMENT '单位ID',
  `brand` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '品牌',
  `place` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '产地',
  `image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '商品主图',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '商品图片集（JSON）',
  `detail` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '商品详情',
  `has_spec` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '是否有规格（0无 1有）',
  `min_stock` int DEFAULT '0' COMMENT '最小库存（SKU合计）',
  `max_stock` int DEFAULT NULL COMMENT '最大库存（SKU合计）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '鍒犻櫎鏍囧織锛?鏈?垹闄?1宸插垹闄わ級',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_category_id` (`category_id`) USING BTREE,
  KEY `idx_wms_goods_unit_id` (`unit_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1836 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='商品表（SPU）';

-- ----------------------------
-- Records of wms_goods
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_goods_category
-- ----------------------------
DROP TABLE IF EXISTS `wms_goods_category`;
CREATE TABLE `wms_goods_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  `parent_id` bigint DEFAULT '0' COMMENT '父分类ID',
  `ancestors` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '祖级列表',
  `category_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
  `order_num` int DEFAULT '0' COMMENT '显示顺序',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '分类图片',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '鍒犻櫎鏍囧織锛?鏈?垹闄?1宸插垹闄わ級',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_parent_id` (`parent_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=255 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='商品分类表';

-- ----------------------------
-- Records of wms_goods_category
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_goods_market
-- ----------------------------
DROP TABLE IF EXISTS `wms_goods_market`;
CREATE TABLE `wms_goods_market` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户ID',
  `platform` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'xianyu' COMMENT '平台：xianyu/jd/pdd',
  `item_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '平台商品ID',
  `url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '原始链接',
  `keyword` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '搜索词',
  `snapshot_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '采集批次ID',
  `collected_at` datetime NOT NULL COMMENT '采集时间',
  `title` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '平台标题',
  `price` decimal(10,2) DEFAULT NULL COMMENT '挂牌价',
  `ship_from` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '发货地',
  `condition_level` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '成色',
  `warranty` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '保修',
  `edition` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '版本/规格描述',
  `normalized_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '规格标准化名称',
  `recognized_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'LLM识别商品名称（规范名：品牌+型号+配置；NULL=未识别）',
  `recognized_source` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '识别来源：regex=正则提取 llm=AI识别（NULL=未识别）',
  `group_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '分组键（型号+规格）',
  `relevance` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'keep' COMMENT '保留/剔除：keep/reject',
  `reject_reason` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '剔除原因',
  `linked_goods_id` bigint DEFAULT NULL COMMENT '关联真实商品ID',
  `linked_sku_id` bigint DEFAULT NULL COMMENT '关联真实SKU ID',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_goods_market` (`tenant_id`,`platform`,`item_id`,`collected_at`) USING BTREE,
  KEY `idx_goods_market_lookup` (`tenant_id`,`platform`,`item_id`) USING BTREE,
  KEY `idx_goods_market_snapshot` (`tenant_id`,`snapshot_id`) USING BTREE,
  KEY `idx_goods_market_group` (`tenant_id`,`group_key`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1923 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='商品行情观测表（append-only）';

-- ----------------------------
-- Records of wms_goods_market
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_goods_market_daily
-- ----------------------------
DROP TABLE IF EXISTS `wms_goods_market_daily`;
CREATE TABLE `wms_goods_market_daily` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户ID',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `spec_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '规格受控编码',
  `spec_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '规格显示名',
  `channel` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '渠道口径：xianyu/jd/pdd/paipai',
  `condition_grade` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '成色口径',
  `sample_cnt` int NOT NULL DEFAULT '0' COMMENT '参与统计样本数（verdict=normal）',
  `excluded_invalid_cnt` int NOT NULL DEFAULT '0' COMMENT '剔除：无效',
  `excluded_low_cnt` int NOT NULL DEFAULT '0' COMMENT '剔除：可疑低价',
  `excluded_high_cnt` int NOT NULL DEFAULT '0' COMMENT '剔除：可疑高价',
  `median_price` decimal(10,2) DEFAULT NULL COMMENT '行情价（中位数）',
  `p25_price` decimal(10,2) DEFAULT NULL COMMENT 'P25（主流区间下沿）',
  `p75_price` decimal(10,2) DEFAULT NULL COMMENT 'P75（主流区间上沿）',
  `min_price` decimal(10,2) DEFAULT NULL COMMENT '当日最低价',
  `max_price` decimal(10,2) DEFAULT NULL COMMENT '当日最高价',
  `min_trusted_price` decimal(10,2) DEFAULT NULL COMMENT '最低可信价',
  `confidence` decimal(4,3) DEFAULT NULL COMMENT '置信度（样本量 + 打标置信度均值）',
  `llm_summary` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'LLM 当日摘要文字（不含价格数字）',
  `calc_version` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '计算口径版本（与 label_version 对齐）',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_daily_dim` (`tenant_id`,`stat_date`,`spec_code`,`channel`,`condition_grade`) USING BTREE,
  KEY `idx_daily_spec_date` (`tenant_id`,`spec_code`,`stat_date`) USING BTREE,
  KEY `idx_daily_date` (`tenant_id`,`stat_date`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=213 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='商品行情日截面结论（分析页只读这张）';

-- ----------------------------
-- Records of wms_goods_market_daily
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_goods_market_labels
-- ----------------------------
DROP TABLE IF EXISTS `wms_goods_market_labels`;
CREATE TABLE `wms_goods_market_labels` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户ID',
  `market_id` bigint NOT NULL COMMENT '观测记录ID（wms_goods_market.id）',
  `label_version` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '打标规则版本，变更即递增',
  `source_channel` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'auto' COMMENT '打标来源：auto自动/cloud云端复核/manual人工',
  `verdict` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '判定：normal正常/suspicious-low可疑低价/suspicious-high可疑高价/invalid无效',
  `spec_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'unknown' COMMENT '规格受控编码，如 macmini-m6-16g-256g；解析不出填 unknown',
  `spec_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '规格显示名，如 Mac mini M6 16G+256G（与编码分离，仅展示用）',
  `channel` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'unknown' COMMENT '渠道口径：xianyu闲鱼/jd京东/pdd拼多多/paipai京东拍拍/unknown',
  `condition_grade` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'unknown' COMMENT '成色：new全新/like-new准新/90-new九成新/unknown',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '判定理由（人可读，供抽查对账）',
  `confidence` decimal(4,3) DEFAULT NULL COMMENT '置信度 0~1',
  `model_used` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '打标模型（取 LlmChatResponse.modelName）',
  `prompt_version` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '实际使用的 prompt 标识',
  `label_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ok' COMMENT '打标状态：ok成功/failed失败/skipped跳过',
  `error_msg` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '失败原因',
  `token_used` int DEFAULT NULL COMMENT 'Token 消耗（审计用）',
  `cost_ms` int DEFAULT NULL COMMENT 'LLM 耗时（审计用）',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_market_label_version` (`tenant_id`,`market_id`,`label_version`) USING BTREE,
  KEY `idx_labels_market` (`tenant_id`,`market_id`) USING BTREE,
  KEY `idx_labels_verdict` (`tenant_id`,`verdict`) USING BTREE,
  KEY `idx_labels_spec` (`tenant_id`,`spec_code`,`channel`,`condition_grade`) USING BTREE,
  KEY `idx_labels_status` (`tenant_id`,`label_status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=829 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='商品行情打标层（LLM 逐条定性，过程数据可审计）';

-- ----------------------------
-- Records of wms_goods_market_labels
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_goods_sku
-- ----------------------------
DROP TABLE IF EXISTS `wms_goods_sku`;
CREATE TABLE `wms_goods_sku` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  `goods_id` bigint NOT NULL COMMENT '商品ID',
  `sku_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'SKU编码',
  `sku_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'SKU名称',
  `barcode` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '条形码',
  `unit_id` bigint DEFAULT NULL COMMENT '单位ID',
  `unit_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '单位名称',
  `spec_info` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '规格信息（JSON：{"颜色":"红色","尺码":"L"}）',
  `spec_ids` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '规格值ID组合（逗号分隔：100,103）',
  `purchase_price` decimal(15,2) DEFAULT '0.00' COMMENT '采购价',
  `sale_price` decimal(15,2) DEFAULT '0.00' COMMENT '销售价',
  `wholesale_price` decimal(15,2) DEFAULT '0.00' COMMENT '批发价',
  `cost_price` decimal(15,2) DEFAULT '0.00' COMMENT '成本价（移动加权平均）',
  `min_stock` int DEFAULT '0' COMMENT '最小库存',
  `max_stock` int DEFAULT NULL COMMENT '最大库存',
  `image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'SKU图片',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '鍒犻櫎鏍囧織锛?鏈?垹闄?1宸插垹闄わ級',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_barcode` (`barcode`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_goods_id` (`goods_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2333 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='商品SKU表';

-- ----------------------------
-- Records of wms_goods_sku
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_goods_sku_price_log
-- ----------------------------
DROP TABLE IF EXISTS `wms_goods_sku_price_log`;
CREATE TABLE `wms_goods_sku_price_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `goods_id` bigint DEFAULT NULL COMMENT '商品ID',
  `sku_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT 'SKU编码',
  `sku_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT 'SKU名称',
  `order_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单类型：PURCHASE_ORDER/SALES_ORDER',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '订单号',
  `order_item_id` bigint DEFAULT NULL COMMENT '订单明细ID',
  `price_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '价格类型：PURCHASE/SALE',
  `old_price` decimal(18,6) DEFAULT NULL COMMENT '变更前价格',
  `new_price` decimal(18,6) NOT NULL COMMENT '变更后价格',
  `operator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '操作人',
  `operate_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标记(0正常,1删除)',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_wgsl_tenant` (`tenant_id`) USING BTREE,
  KEY `idx_wgsl_sku` (`sku_id`) USING BTREE,
  KEY `idx_wgsl_order` (`order_type`,`order_id`) USING BTREE,
  KEY `idx_wgsl_operate_time` (`operate_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2427 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='商品SKU价格变更日志（采购/销售通用）';

-- ----------------------------
-- Records of wms_goods_sku_price_log
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_goods_spec
-- ----------------------------
DROP TABLE IF EXISTS `wms_goods_spec`;
CREATE TABLE `wms_goods_spec` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  `goods_id` bigint NOT NULL COMMENT '商品ID',
  `spec_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '规格名称（如：颜色、尺寸）',
  `sort` int DEFAULT '0' COMMENT '排序',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标记(0正常,1删除)',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_goods_id` (`goods_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=157 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='商品规格表';

-- ----------------------------
-- Records of wms_goods_spec
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_goods_spec_value
-- ----------------------------
DROP TABLE IF EXISTS `wms_goods_spec_value`;
CREATE TABLE `wms_goods_spec_value` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  `spec_id` bigint NOT NULL COMMENT '规格ID',
  `spec_value` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '规格值（如：红色、L）',
  `sort` int DEFAULT '0' COMMENT '排序',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标记(0正常,1删除)',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_spec_id` (`spec_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=245 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='商品规格值表';

-- ----------------------------
-- Records of wms_goods_spec_value
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_import_task
-- ----------------------------
DROP TABLE IF EXISTS `wms_import_task`;
CREATE TABLE `wms_import_task` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `task_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `task_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'basic/purchase_in/sales_out',
  `status` tinyint NOT NULL COMMENT '0待执行/1执行中/2完成/3部分失败/4失败',
  `file_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `total_rows` int DEFAULT '0',
  `success_rows` int DEFAULT '0',
  `fail_rows` int DEFAULT '0',
  `strategy_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '重复、金额、库存策略',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `ended_time` datetime DEFAULT NULL,
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标记(0正常,1删除)',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_tenant_task_no` (`tenant_id`,`task_no`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of wms_import_task
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_import_task_error
-- ----------------------------
DROP TABLE IF EXISTS `wms_import_task_error`;
CREATE TABLE `wms_import_task_error` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `task_id` bigint NOT NULL,
  `row_num` int NOT NULL,
  `error_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `error_msg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `raw_data_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标记(0正常,1删除)',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_task_row` (`task_id`,`row_num`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3198 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC;

-- ----------------------------
-- Records of wms_import_task_error
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_market_replay_task
-- ----------------------------
DROP TABLE IF EXISTS `wms_market_replay_task`;
CREATE TABLE `wms_market_replay_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户ID（发起重放的用户所属租户）',
  `start_date` date NOT NULL COMMENT '重放起始日期（含）',
  `end_date` date NOT NULL COMMENT '重放结束日期（含）',
  `label_version` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '本次重放使用的打标规则版本',
  `recalc_daily` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否重算daily：1是 0否',
  `task_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending' COMMENT '状态：pending待执行/running执行中/success成功/failed失败/cancelled已取消',
  `total_count` int NOT NULL DEFAULT '0' COMMENT '待重打标的观测总数',
  `processed_count` int NOT NULL DEFAULT '0' COMMENT '已完成打标数',
  `success_count` int NOT NULL DEFAULT '0' COMMENT '成功打标数',
  `failed_count` int NOT NULL DEFAULT '0' COMMENT '打标失败数',
  `daily_written` int NOT NULL DEFAULT '0' COMMENT '重算daily 产出行数',
  `error_msg` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '失败原因',
  `start_time` datetime DEFAULT NULL COMMENT '开始执行时间',
  `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
  `cost_seconds` int DEFAULT NULL COMMENT '总耗时（秒）',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_replay_tenant` (`tenant_id`,`task_status`) USING BTREE,
  KEY `idx_replay_create` (`create_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='行情打标重放任务（进度可查）';

-- ----------------------------
-- Records of wms_market_replay_task
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_purchase_in
-- ----------------------------
DROP TABLE IF EXISTS `wms_purchase_in`;
CREATE TABLE `wms_purchase_in` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  `in_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '入库单号',
  `order_id` bigint DEFAULT NULL COMMENT '采购订单ID',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `source_type` tinyint DEFAULT '1' COMMENT '来源类型（1直接入库 2采购订单入库）',
  `in_date` date NOT NULL COMMENT '入库日期',
  `total_amount` decimal(15,2) DEFAULT '0.00' COMMENT '入库总额',
  `paid_amount` decimal(15,2) DEFAULT '0.00' COMMENT '已付金额',
  `payment_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '付款状态（0未付 1部分 2已付）',
  `account_id` bigint DEFAULT NULL COMMENT '结算账户ID',
  `handler` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '经办人',
  `approve_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '审核人',
  `approve_time` datetime DEFAULT NULL COMMENT '审核时间',
  `return_status` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '退货状态(0未退货 1部分退货 2全部退货)',
  `status` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '审核状态（0待审核 1已审核）',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '鍒犻櫎鏍囧織锛?鏈?垹闄?1宸插垹闄わ級',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `ocr_order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'OCR识别单据号',
  `ocr_image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'OCR识别图片URL',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_tenant_in_no` (`tenant_id`,`in_no`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_supplier_id` (`supplier_id`) USING BTREE,
  KEY `idx_warehouse_id` (`warehouse_id`) USING BTREE,
  KEY `idx_in_date` (`in_date`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1461 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='采购入库单主表';

-- ----------------------------
-- Records of wms_purchase_in
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_purchase_in_item
-- ----------------------------
DROP TABLE IF EXISTS `wms_purchase_in_item`;
CREATE TABLE `wms_purchase_in_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  `in_id` bigint NOT NULL COMMENT '入库单ID',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `goods_id` bigint NOT NULL COMMENT '商品ID',
  `sku_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'SKU编码',
  `sku_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'SKU名称',
  `spec_info` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '规格信息',
  `unit_id` bigint DEFAULT NULL COMMENT '单位ID',
  `unit_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '单位名称',
  `quantity` decimal(15,2) NOT NULL COMMENT '数量',
  `price` decimal(15,2) NOT NULL COMMENT '单价',
  `amount` decimal(15,2) NOT NULL COMMENT '金额',
  `returned_qty` decimal(15,2) DEFAULT '0.00' COMMENT '已退货数量',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标记(0正常,1删除)',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_in_id` (`in_id`) USING BTREE,
  KEY `idx_sku_id` (`sku_id`) USING BTREE,
  KEY `idx_goods_id` (`goods_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=7070 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='采购入库单明细表';

-- ----------------------------
-- Records of wms_purchase_in_item
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_purchase_order
-- ----------------------------
DROP TABLE IF EXISTS `wms_purchase_order`;
CREATE TABLE `wms_purchase_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单编号',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `order_date` date NOT NULL COMMENT '订单日期',
  `delivery_date` date DEFAULT NULL COMMENT '交货日期',
  `total_amount` decimal(15,2) DEFAULT '0.00' COMMENT '订单总额',
  `discount_amount` decimal(15,2) DEFAULT '0.00' COMMENT '优惠金额',
  `paid_amount` decimal(15,2) DEFAULT '0.00' COMMENT '已付金额',
  `payment_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '付款状态（0未付 1部分 2已付）',
  `order_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '订单状态（0待审核 1已审核 2部分入库 3全部入库 4已关闭）',
  `account_id` bigint DEFAULT NULL COMMENT '结算账户ID',
  `handler` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '经办人',
  `approve_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '审核人',
  `approve_time` datetime DEFAULT NULL COMMENT '审核时间',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '鍒犻櫎鏍囧織锛?鏈?垹闄?1宸插垹闄わ級',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_tenant_order_no` (`tenant_id`,`order_no`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_supplier_id` (`supplier_id`) USING BTREE,
  KEY `idx_order_date` (`order_date`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=6041 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='采购订单主表';

-- ----------------------------
-- Records of wms_purchase_order
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_purchase_order_item
-- ----------------------------
DROP TABLE IF EXISTS `wms_purchase_order_item`;
CREATE TABLE `wms_purchase_order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `goods_id` bigint NOT NULL COMMENT '商品ID',
  `sku_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'SKU编码',
  `sku_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'SKU名称',
  `spec_info` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '规格信息',
  `unit_id` bigint DEFAULT NULL COMMENT '单位ID',
  `unit_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '单位名称',
  `quantity` decimal(15,2) NOT NULL COMMENT '数量',
  `price` decimal(15,2) NOT NULL COMMENT '单价',
  `discount` decimal(15,2) DEFAULT '0.00' COMMENT '折扣额',
  `amount` decimal(15,2) NOT NULL COMMENT '金额',
  `received_qty` decimal(15,2) DEFAULT '0.00' COMMENT '已入库数量',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标记(0正常,1删除)',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_order_id` (`order_id`) USING BTREE,
  KEY `idx_sku_id` (`sku_id`) USING BTREE,
  KEY `idx_goods_id` (`goods_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=7659 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='采购订单明细表';

-- ----------------------------
-- Records of wms_purchase_order_item
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_purchase_return
-- ----------------------------
DROP TABLE IF EXISTS `wms_purchase_return`;
CREATE TABLE `wms_purchase_return` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  `return_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '退货单号',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `in_id` bigint DEFAULT NULL COMMENT '采购入库ID',
  `source_type` tinyint DEFAULT '1' COMMENT '来源类型（1直接退货 2订单退货）',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `return_date` date NOT NULL COMMENT '退货日期',
  `total_amount` decimal(15,2) DEFAULT '0.00' COMMENT '退货总额',
  `refund_amount` decimal(15,2) DEFAULT '0.00' COMMENT '已退金额',
  `payment_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '退款状态（0未退 1部分 2已退）',
  `account_id` bigint DEFAULT NULL COMMENT '结算账户ID',
  `handler` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '经办人',
  `approve_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '审核人',
  `approve_time` datetime DEFAULT NULL COMMENT '审核时间',
  `status` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '审核状态（0待审核 1已审核）',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '鍒犻櫎鏍囧織锛?鏈?垹闄?1宸插垹闄わ級',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_tenant_return_no` (`tenant_id`,`return_no`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_supplier_id` (`supplier_id`) USING BTREE,
  KEY `idx_warehouse_id` (`warehouse_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=265 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='采购退货单主表';

-- ----------------------------
-- Records of wms_purchase_return
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_purchase_return_item
-- ----------------------------
DROP TABLE IF EXISTS `wms_purchase_return_item`;
CREATE TABLE `wms_purchase_return_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  `return_id` bigint NOT NULL COMMENT '退货单ID',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `goods_id` bigint NOT NULL COMMENT '商品ID',
  `sku_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'SKU编码',
  `sku_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'SKU名称',
  `spec_info` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '规格信息',
  `unit_id` bigint DEFAULT NULL COMMENT '单位ID',
  `unit_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '单位名称',
  `quantity` decimal(15,2) NOT NULL COMMENT '数量',
  `price` decimal(15,2) NOT NULL COMMENT '单价',
  `amount` decimal(15,2) NOT NULL COMMENT '金额',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标记(0正常,1删除)',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_return_id` (`return_id`) USING BTREE,
  KEY `idx_sku_id` (`sku_id`) USING BTREE,
  KEY `idx_goods_id` (`goods_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=405 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='采购退货单明细表';

-- ----------------------------
-- Records of wms_purchase_return_item
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_report_config
-- ----------------------------
DROP TABLE IF EXISTS `wms_report_config`;
CREATE TABLE `wms_report_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '报表ID',
  `report_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '报表编码',
  `report_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '报表名称',
  `report_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '报表类型：sales-销售, purchase-采购, stock-库存, finance-财务, comprehensive-综合',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '报表分类',
  `data_source` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '数据源SQL/接口',
  `query_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '查询参数配置(JSON)',
  `column_config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '列配置(JSON)',
  `chart_config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '图表配置(JSON)',
  `export_config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '导出配置(JSON)',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '状态：0-正常,1-停用',
  `is_system` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '1' COMMENT '是否系统报表：0-自定义,1-系统',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '删除标志（0未删除 1已删除）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_report_code` (`report_code`,`tenant_id`) USING BTREE,
  KEY `idx_report_type` (`report_type`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='报表配置表';

-- ----------------------------
-- Records of wms_report_config
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_report_query_history
-- ----------------------------
DROP TABLE IF EXISTS `wms_report_query_history`;
CREATE TABLE `wms_report_query_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `report_id` bigint NOT NULL COMMENT '报表ID',
  `report_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '报表编码',
  `user_id` bigint NOT NULL COMMENT '查询用户ID',
  `user_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '查询用户名',
  `query_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '查询参数(JSON)',
  `result_count` int DEFAULT NULL COMMENT '结果数量',
  `query_time` int DEFAULT NULL COMMENT '查询耗时(ms)',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `create_time` datetime DEFAULT NULL COMMENT '查询时间',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标记(0正常,1删除)',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_report_user` (`report_id`,`user_id`) USING BTREE,
  KEY `idx_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='报表查询历史表';

-- ----------------------------
-- Records of wms_report_query_history
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_report_subscription
-- ----------------------------
DROP TABLE IF EXISTS `wms_report_subscription`;
CREATE TABLE `wms_report_subscription` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订阅ID',
  `report_id` bigint NOT NULL COMMENT '报表ID',
  `subscription_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订阅名称',
  `user_id` bigint NOT NULL COMMENT '订阅用户ID',
  `frequency` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '推送频率：daily-每日, weekly-每周, monthly-每月',
  `push_time` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '推送时间',
  `push_config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '推送配置(JSON)',
  `last_push_time` datetime DEFAULT NULL COMMENT '最后推送时间',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '状态：0-启用,1-停用',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '删除标志（0未删除 1已删除）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_report_user` (`report_id`,`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='报表订阅表';

-- ----------------------------
-- Records of wms_report_subscription
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_sales_order
-- ----------------------------
DROP TABLE IF EXISTS `wms_sales_order`;
CREATE TABLE `wms_sales_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单编号',
  `customer_id` bigint NOT NULL COMMENT '客户ID',
  `order_date` date NOT NULL COMMENT '订单日期',
  `delivery_date` date DEFAULT NULL COMMENT '交货日期',
  `total_amount` decimal(15,2) DEFAULT '0.00' COMMENT '订单总额',
  `discount_amount` decimal(15,2) DEFAULT '0.00' COMMENT '优惠金额',
  `received_amount` decimal(15,2) DEFAULT '0.00' COMMENT '已收金额',
  `payment_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '收款状态（0未收 1部分 2已收）',
  `order_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '订单状态（0待审核 1已审核 2部分发货 3全部发货 4已关闭）',
  `account_id` bigint DEFAULT NULL COMMENT '结算账户ID',
  `handler` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '经办人',
  `approve_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '审核人',
  `approve_time` datetime DEFAULT NULL COMMENT '审核时间',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '鍒犻櫎鏍囧織锛?鏈?垹闄?1宸插垹闄わ級',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_tenant_order_no` (`tenant_id`,`order_no`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_customer_id` (`customer_id`) USING BTREE,
  KEY `idx_order_date` (`order_date`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=713 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='销售订单主表';

-- ----------------------------
-- Records of wms_sales_order
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_sales_order_item
-- ----------------------------
DROP TABLE IF EXISTS `wms_sales_order_item`;
CREATE TABLE `wms_sales_order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `goods_id` bigint NOT NULL COMMENT '商品ID',
  `sku_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'SKU编码',
  `sku_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'SKU名称',
  `spec_info` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '规格信息',
  `unit_id` bigint DEFAULT NULL COMMENT '单位ID',
  `unit_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '单位名称',
  `quantity` decimal(15,2) NOT NULL COMMENT '数量',
  `price` decimal(15,2) NOT NULL COMMENT '单价',
  `discount` decimal(15,2) DEFAULT '0.00' COMMENT '折扣额',
  `amount` decimal(15,2) NOT NULL COMMENT '金额',
  `delivered_qty` decimal(15,2) DEFAULT '0.00' COMMENT '已出库数量',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标记(0正常,1删除)',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_order_id` (`order_id`) USING BTREE,
  KEY `idx_sku_id` (`sku_id`) USING BTREE,
  KEY `idx_goods_id` (`goods_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2243 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='销售订单明细表';

-- ----------------------------
-- Records of wms_sales_order_item
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_sales_out
-- ----------------------------
DROP TABLE IF EXISTS `wms_sales_out`;
CREATE TABLE `wms_sales_out` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  `out_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '出库单号',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '销售订单号',
  `source_bill_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '来源单号（飞书单据号等，导入防重）',
  `order_id` bigint DEFAULT NULL COMMENT '销售订单ID',
  `source_type` tinyint DEFAULT '1' COMMENT '来源类型(1直接出库 2订单出库)',
  `customer_id` bigint NOT NULL COMMENT '客户ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `out_date` date NOT NULL COMMENT '出库日期',
  `total_amount` decimal(15,2) DEFAULT '0.00' COMMENT '出库总额',
  `received_amount` decimal(15,2) DEFAULT '0.00' COMMENT '已收金额',
  `payment_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '收款状态（0未收 1部分 2已收）',
  `account_id` bigint DEFAULT NULL COMMENT '结算账户ID',
  `handler` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '经办人',
  `approve_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '审核人',
  `approve_time` datetime DEFAULT NULL COMMENT '审核时间',
  `return_status` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '退货状态(0未退货 1部分退货 2全部退货)',
  `status` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '审核状态（0待审核 1已审核）',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '鍒犻櫎鏍囧織锛?鏈?垹闄?1宸插垹闄わ級',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_tenant_out_no` (`tenant_id`,`out_no`) USING BTREE,
  UNIQUE KEY `uk_wms_sales_out_tenant_source_bill` (`tenant_id`,`source_bill_no`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_customer_id` (`customer_id`) USING BTREE,
  KEY `idx_warehouse_id` (`warehouse_id`) USING BTREE,
  KEY `idx_out_date` (`out_date`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=7805 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='销售出库单主表';

-- ----------------------------
-- Records of wms_sales_out
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_sales_out_item
-- ----------------------------
DROP TABLE IF EXISTS `wms_sales_out_item`;
CREATE TABLE `wms_sales_out_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  `out_id` bigint NOT NULL COMMENT '出库单ID',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `goods_id` bigint NOT NULL COMMENT '商品ID',
  `sku_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'SKU编码',
  `sku_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'SKU名称',
  `spec_info` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '规格信息',
  `unit_id` bigint DEFAULT NULL COMMENT '单位ID',
  `unit_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '单位名称',
  `quantity` decimal(15,2) NOT NULL COMMENT '数量',
  `price` decimal(15,2) NOT NULL COMMENT '单价',
  `amount` decimal(15,2) NOT NULL COMMENT '金额',
  `returned_qty` decimal(15,2) DEFAULT '0.00' COMMENT '已退货数量',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标记(0正常,1删除)',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_out_id` (`out_id`) USING BTREE,
  KEY `idx_sku_id` (`sku_id`) USING BTREE,
  KEY `idx_goods_id` (`goods_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=13438 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='销售出库单明细表';

-- ----------------------------
-- Records of wms_sales_out_item
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_sales_return
-- ----------------------------
DROP TABLE IF EXISTS `wms_sales_return`;
CREATE TABLE `wms_sales_return` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  `return_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '退货单号',
  `out_id` bigint DEFAULT NULL COMMENT '销售出库ID',
  `source_type` tinyint DEFAULT '1' COMMENT '来源类型(1直接入库 2出库单退货)',
  `customer_id` bigint NOT NULL COMMENT '客户ID',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `return_date` date NOT NULL COMMENT '退货日期',
  `total_amount` decimal(15,2) DEFAULT '0.00' COMMENT '退货总额',
  `refund_amount` decimal(15,2) DEFAULT '0.00' COMMENT '已退金额',
  `payment_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '退款状态（0未退 1部分 2已退）',
  `account_id` bigint DEFAULT NULL COMMENT '结算账户ID',
  `handler` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '经办人',
  `approve_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '审核人',
  `approve_time` datetime DEFAULT NULL COMMENT '审核时间',
  `status` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '审核状态（0待审核 1已审核）',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '鍒犻櫎鏍囧織锛?鏈?垹闄?1宸插垹闄わ級',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_tenant_return_no` (`tenant_id`,`return_no`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_customer_id` (`customer_id`) USING BTREE,
  KEY `idx_warehouse_id` (`warehouse_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=258 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='销售退货单主表';

-- ----------------------------
-- Records of wms_sales_return
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_sales_return_item
-- ----------------------------
DROP TABLE IF EXISTS `wms_sales_return_item`;
CREATE TABLE `wms_sales_return_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  `return_id` bigint NOT NULL COMMENT '退货单ID',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `goods_id` bigint NOT NULL COMMENT '商品ID',
  `sku_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'SKU编码',
  `sku_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'SKU名称',
  `spec_info` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '规格信息',
  `unit_id` bigint DEFAULT NULL COMMENT '单位ID',
  `unit_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '单位名称',
  `quantity` decimal(15,2) NOT NULL COMMENT '数量',
  `price` decimal(15,2) NOT NULL COMMENT '单价',
  `amount` decimal(15,2) NOT NULL COMMENT '金额',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标记(0正常,1删除)',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_return_id` (`return_id`) USING BTREE,
  KEY `idx_sku_id` (`sku_id`) USING BTREE,
  KEY `idx_goods_id` (`goods_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=230 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='销售退货单明细表';

-- ----------------------------
-- Records of wms_sales_return_item
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_stock
-- ----------------------------
DROP TABLE IF EXISTS `wms_stock`;
CREATE TABLE `wms_stock` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `goods_id` bigint NOT NULL COMMENT '商品ID',
  `quantity` decimal(15,2) DEFAULT '0.00' COMMENT '库存数量',
  `cost_price` decimal(15,2) DEFAULT '0.00' COMMENT '成本价（移动加权平均）',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标记(0正常,1删除)',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新人',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_tenant_warehouse_sku` (`tenant_id`,`warehouse_id`,`sku_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_sku_id` (`sku_id`) USING BTREE,
  KEY `idx_goods_id` (`goods_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5849 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='库存表（基于SKU）';

-- ----------------------------
-- Records of wms_stock
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_stock_check
-- ----------------------------
DROP TABLE IF EXISTS `wms_stock_check`;
CREATE TABLE `wms_stock_check` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  `check_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '盘点单号',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `check_date` date NOT NULL COMMENT '盘点日期',
  `check_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'FULL' COMMENT '盘点类型（FULL全盘 SAMPLE抽盘 DYNAMIC动态盘点）',
  `check_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '盘点状态（0待审核 1已审核）',
  `handler` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '经办人',
  `approve_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '审核人',
  `approve_time` datetime DEFAULT NULL COMMENT '审核时间',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '鍒犻櫎鏍囧織锛?鏈?垹闄?1宸插垹闄わ級',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_tenant_check_no` (`tenant_id`,`check_no`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_warehouse_id` (`warehouse_id`) USING BTREE,
  KEY `idx_check_date` (`check_date`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=133 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='库存盘点单主表';

-- ----------------------------
-- Records of wms_stock_check
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_stock_check_item
-- ----------------------------
DROP TABLE IF EXISTS `wms_stock_check_item`;
CREATE TABLE `wms_stock_check_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  `check_id` bigint NOT NULL COMMENT '盘点单ID',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `goods_id` bigint NOT NULL COMMENT '商品ID',
  `sku_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'SKU编码',
  `sku_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'SKU名称',
  `spec_info` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '规格信息',
  `unit_id` bigint DEFAULT NULL COMMENT '单位ID',
  `unit_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '单位名称',
  `before_qty` decimal(15,2) NOT NULL COMMENT '账面数量',
  `check_qty` decimal(15,2) NOT NULL COMMENT '实际数量',
  `diff_qty` decimal(15,2) NOT NULL COMMENT '差异数量',
  `cost_price` decimal(15,2) DEFAULT '0.00' COMMENT '成本价',
  `diff_amount` decimal(15,2) DEFAULT '0.00' COMMENT '差异金额',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标记(0正常,1删除)',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_check_id` (`check_id`) USING BTREE,
  KEY `idx_sku_id` (`sku_id`) USING BTREE,
  KEY `idx_goods_id` (`goods_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=457 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='库存盘点单明细表（基于SKU）';

-- ----------------------------
-- Records of wms_stock_check_item
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_stock_log
-- ----------------------------
DROP TABLE IF EXISTS `wms_stock_log`;
CREATE TABLE `wms_stock_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  `warehouse_id` bigint NOT NULL COMMENT '仓库ID',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `goods_id` bigint NOT NULL COMMENT '商品ID',
  `bill_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '单据类型（1采购入库 2销售出库 3采购退货 4销售退货 5盘点 6调拨）',
  `bill_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '单据编号',
  `bill_id` bigint DEFAULT NULL COMMENT '单据ID',
  `change_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '变动类型（1增加 2减少）',
  `quantity` decimal(15,2) NOT NULL COMMENT '变动数量',
  `before_qty` decimal(15,2) NOT NULL COMMENT '变动前数量',
  `after_qty` decimal(15,2) NOT NULL COMMENT '变动后数量',
  `cost_price` decimal(15,2) DEFAULT '0.00' COMMENT '成本价',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标记(0正常,1删除)',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_warehouse_sku` (`warehouse_id`,`sku_id`) USING BTREE,
  KEY `idx_sku_id` (`sku_id`) USING BTREE,
  KEY `idx_bill_no` (`bill_no`) USING BTREE,
  KEY `idx_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=15394 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='库存变动记录表（基于SKU）';

-- ----------------------------
-- Records of wms_stock_log
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_stock_transfer
-- ----------------------------
DROP TABLE IF EXISTS `wms_stock_transfer`;
CREATE TABLE `wms_stock_transfer` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  `transfer_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '调拨单号',
  `out_warehouse_id` bigint NOT NULL COMMENT '调出仓库ID',
  `in_warehouse_id` bigint NOT NULL COMMENT '调入仓库ID',
  `transfer_date` date NOT NULL COMMENT '调拨日期',
  `transfer_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '调拨状态（0待审核 1已审核）',
  `handler` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '经办人',
  `approve_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '审核人',
  `approve_time` datetime DEFAULT NULL COMMENT '审核时间',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '鍒犻櫎鏍囧織锛?鏈?垹闄?1宸插垹闄わ級',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_tenant_transfer_no` (`tenant_id`,`transfer_no`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_out_warehouse` (`out_warehouse_id`) USING BTREE,
  KEY `idx_in_warehouse` (`in_warehouse_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=157 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='库存调拨单主表';

-- ----------------------------
-- Records of wms_stock_transfer
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_stock_transfer_item
-- ----------------------------
DROP TABLE IF EXISTS `wms_stock_transfer_item`;
CREATE TABLE `wms_stock_transfer_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  `transfer_id` bigint NOT NULL COMMENT '调拨单ID',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `goods_id` bigint NOT NULL COMMENT '商品ID',
  `sku_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'SKU编码',
  `sku_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'SKU名称',
  `spec_info` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '规格信息',
  `unit_id` bigint DEFAULT NULL COMMENT '单位ID',
  `unit_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '单位名称',
  `quantity` decimal(15,2) NOT NULL COMMENT '数量',
  `cost_price` decimal(15,2) DEFAULT '0.00' COMMENT '成本价',
  `amount` decimal(15,2) DEFAULT '0.00' COMMENT '金额',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标记(0正常,1删除)',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_transfer_id` (`transfer_id`) USING BTREE,
  KEY `idx_sku_id` (`sku_id`) USING BTREE,
  KEY `idx_goods_id` (`goods_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=179 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='库存调拨单明细表（基于SKU）';

-- ----------------------------
-- Records of wms_stock_transfer_item
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_supplier
-- ----------------------------
DROP TABLE IF EXISTS `wms_supplier`;
CREATE TABLE `wms_supplier` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  `is_default` tinyint DEFAULT '0' COMMENT '是否默认供应商：0=否，1=是',
  `supplier_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '供应商编码',
  `supplier_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '供应商名称',
  `contact` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '联系人',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '联系电话',
  `mobile` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '手机号码',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '邮箱',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '地址',
  `bank_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '开户银行',
  `account_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '银行账号',
  `tax_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '税号',
  `credit_limit` decimal(15,2) DEFAULT '0.00' COMMENT '信用额度',
  `current_debt` decimal(15,2) DEFAULT '0.00' COMMENT '当前欠款',
  `settlement_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '1' COMMENT '结算方式（1现结 2账期）',
  `payment_days` int DEFAULT '0' COMMENT '账期天数',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '鍒犻櫎鏍囧織锛?鏈?垹闄?1宸插垹闄わ級',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `default_flag` tinyint GENERATED ALWAYS AS ((case when (`is_default` = 1) then 1 else NULL end)) VIRTUAL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_tenant_supplier_code` (`tenant_id`,`supplier_code`) USING BTREE,
  UNIQUE KEY `idx_supplier_code` (`supplier_code`) USING BTREE,
  UNIQUE KEY `idx_tenant_default` (`tenant_id`,`default_flag`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=167 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='供应商表';

-- ----------------------------
-- Records of wms_supplier
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_unit
-- ----------------------------
DROP TABLE IF EXISTS `wms_unit`;
CREATE TABLE `wms_unit` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  `is_default` tinyint DEFAULT '0' COMMENT '是否默认单位：0=否，1=是',
  `unit_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '单位名称',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '鍒犻櫎鏍囧織锛?鏈?垹闄?1宸插垹闄わ級',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `default_flag` tinyint GENERATED ALWAYS AS ((case when (`is_default` = 1) then 1 else NULL end)) VIRTUAL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `idx_tenant_default` (`tenant_id`,`default_flag`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=242 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='单位表';

-- ----------------------------
-- Records of wms_unit
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_user_onboarding_progress
-- ----------------------------
DROP TABLE IF EXISTS `wms_user_onboarding_progress`;
CREATE TABLE `wms_user_onboarding_progress` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `task_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务编码:add_goods,purchase_in,sales_out,view_report',
  `is_completed` tinyint NOT NULL DEFAULT '0' COMMENT '是否完成:0未完成,1已完成',
  `completed_time` datetime DEFAULT NULL COMMENT '完成时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标记(0正常,1删除)',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建人',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新人',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_tenant_user_task` (`tenant_id`,`user_id`,`task_code`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_task_code` (`task_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='WMS新手引导任务进度表';

-- ----------------------------
-- Records of wms_user_onboarding_progress
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for wms_warehouse
-- ----------------------------
DROP TABLE IF EXISTS `wms_warehouse`;
CREATE TABLE `wms_warehouse` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `tenant_id` bigint NOT NULL DEFAULT '1' COMMENT '租户编号',
  `is_default` tinyint DEFAULT '0' COMMENT '是否默认仓库：0=否，1=是',
  `warehouse_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库编码',
  `warehouse_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库名称',
  `contact` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '联系人',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '联系电话',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '仓库地址',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `sort` int DEFAULT '0' COMMENT '显示顺序',
  `del_flag` tinyint(1) DEFAULT '0' COMMENT '鍒犻櫎鏍囧織锛?鏈?垹闄?1宸插垹闄わ級',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `default_flag` tinyint GENERATED ALWAYS AS ((case when (`is_default` = 1) then 1 else NULL end)) VIRTUAL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_tenant_warehouse_code` (`tenant_id`,`warehouse_code`) USING BTREE,
  UNIQUE KEY `idx_tenant_default` (`tenant_id`,`default_flag`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=160 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='仓库表';

-- ----------------------------
-- Records of wms_warehouse
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Procedure structure for add_print_template_config
-- ----------------------------
DROP PROCEDURE IF EXISTS `add_print_template_config`;
delimiter ;;
CREATE PROCEDURE `add_print_template_config`()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'sys_tenant'
          AND COLUMN_NAME = 'print_template_config'
    ) THEN
        ALTER TABLE sys_tenant
        ADD COLUMN print_template_config JSON NULL COMMENT '打印模板配置JSON' AFTER print_page_size;
    END IF;
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for sp_init_tenant_bill_approve_policy
-- ----------------------------
DROP PROCEDURE IF EXISTS `sp_init_tenant_bill_approve_policy`;
delimiter ;;
CREATE PROCEDURE `sp_init_tenant_bill_approve_policy`(IN p_tenant_id BIGINT,
  IN p_create_by VARCHAR(64))
BEGIN
  DECLARE v_create_by VARCHAR(64) DEFAULT 'system_init';

  IF p_create_by IS NOT NULL AND p_create_by <> '' THEN
    SET v_create_by = p_create_by;
  END IF;

  INSERT IGNORE INTO `wms_bill_approve_policy`
  (
    `tenant_id`, `bill_type`, `need_approve`, `status`, `remark`, `create_by`
  )
  SELECT
    p_tenant_id,
    bt.`bill_type`,
    0 AS `need_approve`,
    '0' AS `status`,
    'Init default no-approve policy for new tenant' AS `remark`,
    v_create_by
  FROM (
    SELECT 'PURCHASE_ORDER' AS `bill_type`
    UNION ALL SELECT 'PURCHASE_IN'
    UNION ALL SELECT 'PURCHASE_RETURN'
    UNION ALL SELECT 'SALES_ORDER'
    UNION ALL SELECT 'SALES_OUT'
    UNION ALL SELECT 'SALES_RETURN'
    UNION ALL SELECT 'STOCK_CHECK'
    UNION ALL SELECT 'STOCK_TRANSFER'
  ) bt;
END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
