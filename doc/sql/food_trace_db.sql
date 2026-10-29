-- ==========================================================
-- 区块链食品溯源平台 数据库初始化脚本
-- 适用数据库: MySQL 8.0+
-- 字符集: utf8mb4
-- ==========================================================

-- 1. 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `food_trace_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 2. 使用数据库
USE `food_trace_db`;

-- ==========================================================
-- 3. 创建食品基础信息表 (food_info)
-- 用于存储链下缓存数据，方便前端进行列表展示和快速查询
-- ==========================================================
DROP TABLE IF EXISTS `food_info`;
CREATE TABLE `food_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '数据库主键',
  `food_id` varchar(64) NOT NULL COMMENT '食品唯一溯源码 (如 APPLE_001)',
  `food_name` varchar(128) NOT NULL COMMENT '食品名称',
  `origin` varchar(128) NOT NULL COMMENT '原产地',
  `current_owner` varchar(64) NOT NULL COMMENT '当前所有者区块链地址',
  `status` int(11) NOT NULL DEFAULT '0' COMMENT '状态: 0-已生产, 1-运输中, 2-已送达, 3-销售中',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_food_id` (`food_id`) COMMENT '保证溯源码全局唯一'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='食品基础信息表';

-- ==========================================================
-- 4. 创建溯源流转记录表 (trace_record)
-- 用于记录每一次业务流转的详情，配合前端时间轴展示
-- ==========================================================
DROP TABLE IF EXISTS `trace_record`;
CREATE TABLE `trace_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '数据库主键',
  `food_id` varchar(64) NOT NULL COMMENT '关联的食品溯源码',
  `operator` varchar(64) NOT NULL COMMENT '操作人的区块链地址',
  `role_type` int(11) NOT NULL COMMENT '操作人角色: 1-供应商, 2-中间商, 3-销售商',
  `action` varchar(64) NOT NULL COMMENT '动作描述 (如: Production, Shipped)',
  `location` varchar(128) NOT NULL COMMENT '操作地点',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注信息',
  `tx_time` bigint(20) NOT NULL COMMENT '交易发生的时间戳',
  `tx_hash` varchar(66) DEFAULT NULL COMMENT '区块链交易哈希 (TxHash)',
  PRIMARY KEY (`id`),
  KEY `idx_food_id` (`food_id`) COMMENT '按溯源码查询的普通索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='溯源流转记录表';

-- ==========================================================
-- 5. 创建系统操作审计日志表 (sys_operation_log)
-- 由后端 AOP 切面自动记录，用于系统监控和排错
-- ==========================================================
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '数据库主键',
  `operator_name` varchar(64) DEFAULT NULL COMMENT '操作人名称',
  `operator_address` varchar(64) DEFAULT NULL COMMENT '操作人区块链地址',
  `request_url` varchar(255) NOT NULL COMMENT '请求URL',
  `request_method` varchar(10) NOT NULL COMMENT '请求方式 (GET/POST)',
  `request_params` text COMMENT '请求参数(JSON)',
  `response_result` text COMMENT '返回结果(JSON)',
  `cost_time` bigint(20) NOT NULL COMMENT '耗时(毫秒)',
  `client_ip` varchar(64) DEFAULT NULL COMMENT '客户端IP',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统操作审计日志';

-- ==========================================================
-- 6. 插入测试数据 (可选)
-- 用于快速测试系统的数据展示
-- ==========================================================
-- INSERT INTO `food_info` (`food_id`, `food_name`, `origin`, `current_owner`, `status`) 
-- VALUES ('APPLE_001', '烟台红富士苹果', '山东烟台', '0xc8d95843e76348ac85204e65b9230a9bfb6a9b55', 0);