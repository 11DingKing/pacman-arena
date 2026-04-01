-- 吃豆人游戏系统数据库初始化脚本

CREATE DATABASE IF NOT EXISTS pacman DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE pacman;

-- 设置连接字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `role` TINYINT NOT NULL DEFAULT 0 COMMENT '角色: 0-普通用户, 1-管理员',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
    `sound_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '音效开关: 0-关闭, 1-开启',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 游戏记录表
CREATE TABLE IF NOT EXISTS `game_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `score` INT NOT NULL DEFAULT 0 COMMENT '得分',
    `level` INT NOT NULL DEFAULT 1 COMMENT '关卡',
    `duration` INT NOT NULL DEFAULT 0 COMMENT '游戏时长(秒)',
    `played_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '游戏时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_score` (`score` DESC),
    KEY `idx_played_at` (`played_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏记录表';

-- 道具表
CREATE TABLE IF NOT EXISTS `item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '道具ID',
    `name` VARCHAR(50) NOT NULL COMMENT '道具名称',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '道具描述',
    `icon` VARCHAR(255) DEFAULT NULL COMMENT '图标URL',
    `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '价格',
    `effect_type` VARCHAR(50) NOT NULL COMMENT '效果类型: SPEED_UP, INVINCIBLE, DOUBLE_SCORE, EXTRA_LIFE',
    `effect_value` INT NOT NULL DEFAULT 0 COMMENT '效果数值',
    `duration` INT NOT NULL DEFAULT 0 COMMENT '持续时间(秒)',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-下架, 1-上架',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='道具表';

-- 用户道具表
CREATE TABLE IF NOT EXISTS `user_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `item_id` BIGINT NOT NULL COMMENT '道具ID',
    `quantity` INT NOT NULL DEFAULT 0 COMMENT '数量',
    `acquired_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '获取时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_item` (`user_id`, `item_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户道具表';

-- 支付订单表
CREATE TABLE IF NOT EXISTS `payment_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `item_id` BIGINT NOT NULL COMMENT '道具ID',
    `quantity` INT NOT NULL DEFAULT 1 COMMENT '购买数量',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '支付金额',
    `pay_type` VARCHAR(20) NOT NULL DEFAULT 'ALIPAY' COMMENT '支付方式',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-待支付, 1-已支付, 2-已取消, 3-已退款',
    `trade_no` VARCHAR(64) DEFAULT NULL COMMENT '第三方交易号',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `paid_at` DATETIME DEFAULT NULL COMMENT '支付时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付订单表';

-- 支付宝/支付配置表（管理端一键配置，支持真实+模拟）
CREATE TABLE IF NOT EXISTS `payment_config` (
    `id` INT NOT NULL DEFAULT 1 COMMENT '主键(单例)',
    `mock` TINYINT NOT NULL DEFAULT 1 COMMENT '是否模拟: 0-真实支付宝, 1-模拟',
    `app_id` VARCHAR(64) DEFAULT NULL COMMENT '支付宝应用ID',
    `private_key` TEXT DEFAULT NULL COMMENT '应用私钥',
    `public_key` TEXT DEFAULT NULL COMMENT '支付宝公钥',
    `gateway` VARCHAR(255) DEFAULT 'https://openapi-sandbox.dl.alipaydev.com/gateway.do' COMMENT '网关(沙箱/正式)',
    `notify_url` VARCHAR(512) DEFAULT NULL COMMENT '异步通知URL',
    `return_url` VARCHAR(512) DEFAULT NULL COMMENT '同步返回URL',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付配置';

INSERT INTO `payment_config` (`id`, `mock`, `gateway`) VALUES (1, 1, 'https://openapi-sandbox.dl.alipaydev.com/gateway.do')
ON DUPLICATE KEY UPDATE `id`=`id`;

-- 操作日志表
CREATE TABLE IF NOT EXISTS `operation_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '操作用户ID',
    `module` VARCHAR(50) NOT NULL COMMENT '模块',
    `action` VARCHAR(50) NOT NULL COMMENT '操作',
    `content` TEXT DEFAULT NULL COMMENT '操作内容',
    `ip` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 初始化管理员账号 (密码: admin123)
-- 应用启动时会自动校验并修复 admin 密码为 admin123，若无法登录请重启后端一次
INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `status`) VALUES
('admin', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '系统管理员', 1, 1)
ON DUPLICATE KEY UPDATE `username`=`username`;

-- 初始化模拟用户 (密码都是: 123456)
INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `status`) VALUES
('player1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '吃豆达人', 0, 1),
('player2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '游戏王者', 0, 1),
('player3', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '闪电侠', 0, 1),
('player4', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '无敌小明', 0, 1),
('player5', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '豆豆杀手', 0, 1),
('player6', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '幽灵克星', 0, 1),
('player7', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '迷宫大师', 0, 1),
('player8', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '速度之王', 0, 1),
('player9', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '新手小白', 0, 1),
('player10', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '老玩家', 0, 1),
('player11', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '街机王', 0, 1),
('player12', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '复古玩家', 0, 1),
('player13', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '挑战者', 0, 1),
('player14', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '高分猎人', 0, 1),
('player15', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '夜猫子', 0, 1),
('player16', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '周末玩家', 0, 1),
('player17', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '休闲达人', 0, 1),
('player18', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '氪金大佬', 0, 1),
('player19', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '平民玩家', 0, 1),
('player20', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '技术流', 0, 1);

-- 初始化道具数据
INSERT INTO `item` (`name`, `description`, `icon`, `price`, `effect_type`, `effect_value`, `duration`, `status`) VALUES
('加速药水', '使用后移动速度提升50%，持续10秒', '🚀', 1.00, 'SPEED_UP', 50, 10, 1),
('无敌护盾', '使用后获得无敌状态，持续5秒', '🛡️', 2.00, 'INVINCIBLE', 1, 5, 1),
('双倍积分', '使用后获得的分数翻倍，持续15秒', '⭐', 3.00, 'DOUBLE_SCORE', 2, 15, 1),
('额外生命', '使用后获得一条额外生命', '❤️', 5.00, 'EXTRA_LIFE', 1, 0, 1),
('磁铁道具', '自动吸引附近的豆子，持续8秒', '🧲', 2.50, 'MAGNET', 100, 8, 1);

-- 初始化游戏记录数据 (排行榜数据)
INSERT INTO `game_record` (`user_id`, `score`, `level`, `duration`, `played_at`) VALUES
(2, 15680, 12, 320, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(3, 14520, 11, 298, DATE_SUB(NOW(), INTERVAL 5 HOUR)),
(4, 13890, 10, 275, DATE_SUB(NOW(), INTERVAL 8 HOUR)),
(5, 12750, 10, 260, DATE_SUB(NOW(), INTERVAL 12 HOUR)),
(6, 11980, 9, 245, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(7, 11200, 9, 230, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(8, 10560, 8, 218, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9, 9870, 8, 205, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(10, 9150, 7, 192, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(11, 8620, 7, 180, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(12, 8100, 6, 168, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(13, 7580, 6, 155, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(14, 7020, 5, 142, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(15, 6480, 5, 130, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(16, 5920, 4, 118, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(17, 5380, 4, 105, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(18, 4850, 3, 92, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(19, 4320, 3, 80, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(20, 3780, 2, 68, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(21, 3250, 2, 55, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(2, 12500, 10, 280, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(3, 11800, 9, 265, DATE_SUB(NOW(), INTERVAL 11 DAY)),
(4, 10200, 8, 240, DATE_SUB(NOW(), INTERVAL 12 DAY)),
(5, 9600, 7, 220, DATE_SUB(NOW(), INTERVAL 13 DAY)),
(6, 8900, 7, 200, DATE_SUB(NOW(), INTERVAL 14 DAY));

-- 初始化一些用户道具数据
INSERT INTO `user_item` (`user_id`, `item_id`, `quantity`) VALUES
(2, 1, 5), (2, 2, 3), (2, 3, 2),
(3, 1, 3), (3, 4, 1),
(4, 2, 2), (4, 5, 4),
(5, 1, 8), (5, 3, 3);

-- 初始化一些订单数据
INSERT INTO `payment_order` (`order_no`, `user_id`, `item_id`, `quantity`, `amount`, `pay_type`, `status`, `trade_no`, `created_at`, `paid_at`) VALUES
('ORD20260125001', 2, 1, 5, 5.00, 'ALIPAY', 1, 'ALI20260125001', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
('ORD20260125002', 2, 2, 3, 6.00, 'ALIPAY', 1, 'ALI20260125002', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
('ORD20260125003', 3, 1, 3, 3.00, 'ALIPAY', 1, 'ALI20260125003', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
('ORD20260125004', 4, 2, 2, 4.00, 'ALIPAY', 1, 'ALI20260125004', DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),
('ORD20260125005', 5, 1, 8, 8.00, 'ALIPAY', 1, 'ALI20260125005', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
('ORD20260126001', 6, 3, 2, 6.00, 'ALIPAY', 0, NULL, NOW(), NULL);
