-- musicplatform_init.sql
-- 用途：
-- 1. 初始化 musicplatform 数据库结构
-- 2. 初始化三种基础角色：
--    1 ROLE_USER
--    2 ROLE_ADMIN
--    3 ROLE_SUPER_ADMIN
-- 3. 初始化一个超级管理员账号：
--    username = SuperAdmin
--    password = 2004ww514
--    email    = 114514@qq.com
--
-- 建议使用方式：
-- 1. 先在 MySQL 8.x 中执行本脚本
-- 2. 再启动 Spring Boot 项目
--
-- 说明：
-- - 本脚本按当前 Entity 设计整理，并补了业务上需要的唯一约束/索引
-- - 为兼容你当前代码里对角色 ID 的直接判断，角色 ID 固定为 1/2/3

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS `musicplatform`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE `musicplatform`;

CREATE TABLE IF NOT EXISTS `TB_ROLE` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `roleName` VARCHAR(30) NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tb_role_role_name` (`roleName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

CREATE TABLE IF NOT EXISTS `TB_USER` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(30) NOT NULL,
  `password` VARCHAR(60) NOT NULL,
  `email` VARCHAR(50) NULL,
  `register_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `avatar_url` VARCHAR(200) NULL,
  `gender` ENUM('male', 'female', 'unknown') NOT NULL DEFAULT 'unknown',
  `fan_count` BIGINT NOT NULL DEFAULT 0,
  `follow_count` BIGINT NOT NULL DEFAULT 0,
  `is_active` TINYINT(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tb_user_username` (`username`),
  UNIQUE KEY `uk_tb_user_email` (`email`),
  KEY `idx_tb_user_register_time` (`register_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `tb_song` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0,
  `song_name` VARCHAR(255) NOT NULL,
  `song_artist` VARCHAR(255) NOT NULL,
  `Audio_url` VARCHAR(255) NOT NULL,
  `lrc_url` VARCHAR(255) NULL,
  `upload_by` BIGINT NULL,
  `play_count` BIGINT NOT NULL DEFAULT 0,
  `avatar_url` VARCHAR(255) NULL,
  `comment_count` BIGINT NOT NULL DEFAULT 0,
  `favourite_count` BIGINT NOT NULL DEFAULT 0,
  `create_time` DATETIME NULL,
  `update_time` DATETIME NULL,
  `hot_score` DOUBLE NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_tb_song_upload_by` (`upload_by`),
  KEY `idx_tb_song_hot_score` (`hot_score`),
  KEY `idx_tb_song_create_time` (`create_time`),
  CONSTRAINT `fk_tb_song_upload_by`
    FOREIGN KEY (`upload_by`) REFERENCES `TB_USER` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='歌曲表';

CREATE TABLE IF NOT EXISTS `tb_post` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `title` VARCHAR(200) NOT NULL,
  `content` TEXT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `status` TINYINT NOT NULL DEFAULT 1,
  `view_count` BIGINT NOT NULL DEFAULT 0,
  `like_count` BIGINT NOT NULL DEFAULT 0,
  `comment_count` BIGINT NOT NULL DEFAULT 0,
  `favourite_count` BIGINT NOT NULL DEFAULT 0,
  `hot_score` DOUBLE NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_tb_post_user_id` (`user_id`),
  KEY `idx_tb_post_hot_score` (`hot_score`),
  KEY `idx_tb_post_create_time` (`create_time`),
  CONSTRAINT `fk_tb_post_user_id`
    FOREIGN KEY (`user_id`) REFERENCES `TB_USER` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子主表';

CREATE TABLE IF NOT EXISTS `tb_media` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `original_name` VARCHAR(100) NOT NULL,
  `user_id` BIGINT NOT NULL,
  `media_type` TINYINT NOT NULL COMMENT '0-audio, 1-image, 2-lrc',
  `size` BIGINT NOT NULL,
  `create_date` DATETIME NOT NULL,
  `url` VARCHAR(200) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tb_media_user_id` (`user_id`),
  KEY `idx_tb_media_media_type` (`media_type`),
  KEY `idx_tb_media_create_date` (`create_date`),
  CONSTRAINT `fk_tb_media_user_id`
    FOREIGN KEY (`user_id`) REFERENCES `TB_USER` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='媒体文件表';

CREATE TABLE IF NOT EXISTS `TB_USER_ROLE` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `role_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tb_user_role_user_role` (`user_id`, `role_id`),
  KEY `idx_tb_user_role_role_id` (`role_id`),
  CONSTRAINT `fk_tb_user_role_user_id`
    FOREIGN KEY (`user_id`) REFERENCES `TB_USER` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_tb_user_role_role_id`
    FOREIGN KEY (`role_id`) REFERENCES `TB_ROLE` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

CREATE TABLE IF NOT EXISTS `tb_follow` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `follow_user_id` BIGINT NOT NULL,
  `create_date` DATETIME NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tb_follow_user_follow` (`user_id`, `follow_user_id`),
  KEY `idx_tb_follow_follow_user_id` (`follow_user_id`),
  KEY `idx_tb_follow_create_date` (`create_date`),
  CONSTRAINT `fk_tb_follow_user_id`
    FOREIGN KEY (`user_id`) REFERENCES `TB_USER` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_tb_follow_follow_user_id`
    FOREIGN KEY (`follow_user_id`) REFERENCES `TB_USER` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='关注关系表';

CREATE TABLE IF NOT EXISTS `tb_post_media` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `postId` BIGINT NULL,
  `mediaId` BIGINT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tb_post_media_post_media` (`postId`, `mediaId`),
  KEY `idx_tb_post_media_media_id` (`mediaId`),
  CONSTRAINT `fk_tb_post_media_post_id`
    FOREIGN KEY (`postId`) REFERENCES `tb_post` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_tb_post_media_media_id`
    FOREIGN KEY (`mediaId`) REFERENCES `tb_media` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子媒体关联表';

CREATE TABLE IF NOT EXISTS `tb_post_comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `post_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `content` TEXT NOT NULL,
  `parent_id` BIGINT NULL,
  `reply_to_user_id` BIGINT NULL,
  `create_time` DATETIME NULL,
  `like_count` BIGINT NOT NULL DEFAULT 0,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_tb_post_comment_post_parent_delete` (`post_id`, `parent_id`, `is_delete`),
  KEY `idx_tb_post_comment_parent_id` (`parent_id`),
  KEY `idx_tb_post_comment_user_id` (`user_id`),
  KEY `idx_tb_post_comment_reply_to_user_id` (`reply_to_user_id`),
  KEY `idx_tb_post_comment_create_time` (`create_time`),
  CONSTRAINT `fk_tb_post_comment_post_id`
    FOREIGN KEY (`post_id`) REFERENCES `tb_post` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_tb_post_comment_user_id`
    FOREIGN KEY (`user_id`) REFERENCES `TB_USER` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_tb_post_comment_parent_id`
    FOREIGN KEY (`parent_id`) REFERENCES `tb_post_comment` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_tb_post_comment_reply_to_user_id`
    FOREIGN KEY (`reply_to_user_id`) REFERENCES `TB_USER` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子评论表';

CREATE TABLE IF NOT EXISTS `tb_song_comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `song_id` BIGINT NOT NULL,
  `content` TEXT NOT NULL,
  `parent_id` BIGINT NULL,
  `reply_to_user_id` BIGINT NULL,
  `like_count` BIGINT NOT NULL DEFAULT 0,
  `is_delete` TINYINT(1) NOT NULL DEFAULT 0,
  `create_time` DATETIME NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tb_song_comment_song_parent_delete` (`song_id`, `parent_id`, `is_delete`),
  KEY `idx_tb_song_comment_parent_id` (`parent_id`),
  KEY `idx_tb_song_comment_user_id` (`user_id`),
  KEY `idx_tb_song_comment_reply_to_user_id` (`reply_to_user_id`),
  KEY `idx_tb_song_comment_create_time` (`create_time`),
  CONSTRAINT `fk_tb_song_comment_song_id`
    FOREIGN KEY (`song_id`) REFERENCES `tb_song` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_tb_song_comment_user_id`
    FOREIGN KEY (`user_id`) REFERENCES `TB_USER` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_tb_song_comment_parent_id`
    FOREIGN KEY (`parent_id`) REFERENCES `tb_song_comment` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_tb_song_comment_reply_to_user_id`
    FOREIGN KEY (`reply_to_user_id`) REFERENCES `TB_USER` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='歌曲评论表';

CREATE TABLE IF NOT EXISTS `tb_user_favourite_post` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `post_id` BIGINT NOT NULL,
  `create_date` DATETIME NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tb_user_favourite_post_user_post` (`user_id`, `post_id`),
  KEY `idx_tb_user_favourite_post_post_id` (`post_id`),
  KEY `idx_tb_user_favourite_post_create_date` (`create_date`),
  CONSTRAINT `fk_tb_user_favourite_post_user_id`
    FOREIGN KEY (`user_id`) REFERENCES `TB_USER` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_tb_user_favourite_post_post_id`
    FOREIGN KEY (`post_id`) REFERENCES `tb_post` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏帖子表';

CREATE TABLE IF NOT EXISTS `tb_user_favourite_song` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `song_id` BIGINT NOT NULL,
  `create_date` DATETIME NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tb_user_favourite_song_user_song` (`user_id`, `song_id`),
  KEY `idx_tb_user_favourite_song_song_id` (`song_id`),
  KEY `idx_tb_user_favourite_song_create_date` (`create_date`),
  CONSTRAINT `fk_tb_user_favourite_song_user_id`
    FOREIGN KEY (`user_id`) REFERENCES `TB_USER` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_tb_user_favourite_song_song_id`
    FOREIGN KEY (`song_id`) REFERENCES `tb_song` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏歌曲表';

CREATE TABLE IF NOT EXISTS `tb_user_like_post` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `postid` BIGINT NOT NULL,
  `userid` BIGINT NOT NULL,
  `create_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tb_user_like_post_post_user` (`postid`, `userid`),
  KEY `idx_tb_user_like_post_userid` (`userid`),
  KEY `idx_tb_user_like_post_create_time` (`create_time`),
  CONSTRAINT `fk_tb_user_like_post_postid`
    FOREIGN KEY (`postid`) REFERENCES `tb_post` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_tb_user_like_post_userid`
    FOREIGN KEY (`userid`) REFERENCES `TB_USER` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子点赞表';

CREATE TABLE IF NOT EXISTS `tb_user_like_post_comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `post_comment_id` BIGINT NOT NULL,
  `userid` BIGINT NOT NULL,
  `create_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tb_user_like_post_comment_comment_user` (`post_comment_id`, `userid`),
  KEY `idx_tb_user_like_post_comment_userid` (`userid`),
  KEY `idx_tb_user_like_post_comment_create_time` (`create_time`),
  CONSTRAINT `fk_tb_user_like_post_comment_comment_id`
    FOREIGN KEY (`post_comment_id`) REFERENCES `tb_post_comment` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_tb_user_like_post_comment_userid`
    FOREIGN KEY (`userid`) REFERENCES `TB_USER` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子评论点赞表';

CREATE TABLE IF NOT EXISTS `tb_user_like_song_comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `song_comment_id` BIGINT NOT NULL,
  `userid` BIGINT NOT NULL,
  `create_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tb_user_like_song_comment_comment_user` (`song_comment_id`, `userid`),
  KEY `idx_tb_user_like_song_comment_userid` (`userid`),
  KEY `idx_tb_user_like_song_comment_create_time` (`create_time`),
  CONSTRAINT `fk_tb_user_like_song_comment_comment_id`
    FOREIGN KEY (`song_comment_id`) REFERENCES `tb_song_comment` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_tb_user_like_song_comment_userid`
    FOREIGN KEY (`userid`) REFERENCES `TB_USER` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='歌曲评论点赞表';

CREATE TABLE IF NOT EXISTS `tb_conversation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user1_id` BIGINT NOT NULL,
  `unread_count1` BIGINT NOT NULL DEFAULT 0,
  `user2_id` BIGINT NOT NULL,
  `unread_count2` BIGINT NOT NULL DEFAULT 0,
  `last_message_content` TEXT NULL,
  `last_message_sender_id` BIGINT NULL,
  `last_time` DATETIME NULL,
  `create_time` DATETIME NULL,
  `update_time` DATETIME NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tb_conversation_user_pair` (`user1_id`, `user2_id`),
  KEY `idx_user1` (`user1_id`),
  KEY `idx_user2` (`user2_id`),
  KEY `idx_last_time` (`last_time`),
  KEY `idx_tb_conversation_last_message_sender_id` (`last_message_sender_id`),
  CONSTRAINT `fk_tb_conversation_user1_id`
    FOREIGN KEY (`user1_id`) REFERENCES `TB_USER` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_tb_conversation_user2_id`
    FOREIGN KEY (`user2_id`) REFERENCES `TB_USER` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_tb_conversation_last_message_sender_id`
    FOREIGN KEY (`last_message_sender_id`) REFERENCES `TB_USER` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='私信会话表';

CREATE TABLE IF NOT EXISTS `tb_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `speaking_user_id` BIGINT NOT NULL,
  `receive_user_id` BIGINT NOT NULL,
  `content` TEXT NULL,
  `conversation_id` BIGINT NOT NULL,
  `create_time` DATETIME NULL,
  PRIMARY KEY (`id`),
  KEY `idx_tb_message_conversation_id` (`conversation_id`),
  KEY `idx_tb_message_speaking_user_id` (`speaking_user_id`),
  KEY `idx_tb_message_receive_user_id` (`receive_user_id`),
  KEY `idx_tb_message_create_time` (`create_time`),
  CONSTRAINT `fk_tb_message_conversation_id`
    FOREIGN KEY (`conversation_id`) REFERENCES `tb_conversation` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_tb_message_speaking_user_id`
    FOREIGN KEY (`speaking_user_id`) REFERENCES `TB_USER` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_tb_message_receive_user_id`
    FOREIGN KEY (`receive_user_id`) REFERENCES `TB_USER` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='私信消息表';

CREATE TABLE IF NOT EXISTS `tb_report` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `reporter_id` BIGINT NOT NULL,
  `target_type` TINYINT NOT NULL COMMENT '0-USER, 1-SONG, 2-POST, 3-SONG_COMMENT, 4-POST_COMMENT',
  `target_id` BIGINT NOT NULL,
  `reason` TEXT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  `handler_id` BIGINT NULL,
  `handled_time` DATETIME NULL,
  `handler_note` TEXT NULL,
  `result` VARCHAR(50) NULL,
  `create_time` DATETIME NULL,
  `version` BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_tb_report_reporter_id` (`reporter_id`),
  KEY `idx_tb_report_handler_id` (`handler_id`),
  KEY `idx_tb_report_status` (`status`),
  KEY `idx_tb_report_target` (`target_type`, `target_id`),
  KEY `idx_tb_report_create_time` (`create_time`),
  CONSTRAINT `fk_tb_report_reporter_id`
    FOREIGN KEY (`reporter_id`) REFERENCES `TB_USER` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_tb_report_handler_id`
    FOREIGN KEY (`handler_id`) REFERENCES `TB_USER` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='举报表';

INSERT INTO `TB_ROLE` (`id`, `roleName`) VALUES
  (1, 'ROLE_USER'),
  (2, 'ROLE_ADMIN'),
  (3, 'ROLE_SUPER_ADMIN')
ON DUPLICATE KEY UPDATE
  `roleName` = VALUES(`roleName`);

INSERT INTO `TB_USER` (
  `username`,
  `password`,
  `email`,
  `register_time`,
  `avatar_url`,
  `gender`,
  `fan_count`,
  `follow_count`,
  `is_active`
)
SELECT
  'SuperAdmin',
  '2004ww514',
  '114514@qq.com',
  NOW(),
  '/default/defaultUserAvatar.png',
  'unknown',
  0,
  0,
  1
WHERE NOT EXISTS (
  SELECT 1
  FROM `TB_USER`
  WHERE `username` = 'SuperAdmin' OR `email` = '114514@qq.com'
);

INSERT INTO `TB_USER_ROLE` (`user_id`, `role_id`)
SELECT u.`id`, r.`id`
FROM `TB_USER` u
JOIN `TB_ROLE` r ON r.`roleName` = 'ROLE_USER'
WHERE u.`username` = 'SuperAdmin'
  AND NOT EXISTS (
    SELECT 1
    FROM `TB_USER_ROLE` ur
    WHERE ur.`user_id` = u.`id` AND ur.`role_id` = r.`id`
  );

INSERT INTO `TB_USER_ROLE` (`user_id`, `role_id`)
SELECT u.`id`, r.`id`
FROM `TB_USER` u
JOIN `TB_ROLE` r ON r.`roleName` = 'ROLE_ADMIN'
WHERE u.`username` = 'SuperAdmin'
  AND NOT EXISTS (
    SELECT 1
    FROM `TB_USER_ROLE` ur
    WHERE ur.`user_id` = u.`id` AND ur.`role_id` = r.`id`
  );

INSERT INTO `TB_USER_ROLE` (`user_id`, `role_id`)
SELECT u.`id`, r.`id`
FROM `TB_USER` u
JOIN `TB_ROLE` r ON r.`roleName` = 'ROLE_SUPER_ADMIN'
WHERE u.`username` = 'SuperAdmin'
  AND NOT EXISTS (
    SELECT 1
    FROM `TB_USER_ROLE` ur
    WHERE ur.`user_id` = u.`id` AND ur.`role_id` = r.`id`
  );

SET FOREIGN_KEY_CHECKS = 1;
