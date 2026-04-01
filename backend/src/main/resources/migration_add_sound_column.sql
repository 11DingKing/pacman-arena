-- 数据库迁移脚本：添加音效开关字段
-- 执行此 SQL 后，用户音效设置将持久化到数据库

USE pacman;

-- 为 user 表添加 sound_enabled 字段
ALTER TABLE `user` 
ADD COLUMN `sound_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '音效开关: 0-关闭, 1-开启' 
AFTER `status`;
