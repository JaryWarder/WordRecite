-- ====================================
-- WordRecite - 缺失表结构补齐脚本
-- 说明：用于“老库/旧脚本导入”导致新模块表缺失时，补齐 Spring Boot + MyBatis-Plus 代码所需表结构
-- 数据库：bs_project
-- 字符集：utf8mb4
-- ====================================

SET NAMES utf8mb4;
SET CHARACTER_SET_CLIENT = utf8mb4;
SET CHARACTER_SET_RESULTS = utf8mb4;
SET collation_connection = utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS bs_project CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bs_project;

-- ==============================
-- 1) 词书元信息表：word_book
-- ==============================
CREATE TABLE IF NOT EXISTS word_book (
    title VARCHAR(20) NOT NULL COMMENT '词书表名/标题（如 TOEFLWordBook）',
    num INT NOT NULL COMMENT '词书单词总数',
    PRIMARY KEY (title)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='词书列表（title -> num）';

-- ==============================
-- 2) 私人单词本：private_books
-- ==============================
CREATE TABLE IF NOT EXISTS private_books (
    pid INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(20) NOT NULL COMMENT '用户名',
    origin VARCHAR(50) NOT NULL COMMENT '来源词书/表名（动态表名，需白名单校验）',
    id INT NOT NULL COMMENT '单词在词书中的ID（word_id）',
    word VARCHAR(100) NOT NULL COMMENT '单词拼写',
    UNIQUE KEY uk_private_books (username, origin, id),
    INDEX idx_private_user_origin (username, origin)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='私人单词本';

-- ==============================
-- 3) 每日打卡明细：daily
-- ==============================
CREATE TABLE IF NOT EXISTS daily (
    daily_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(20) NOT NULL COMMENT '用户名',
    review_date DATE NOT NULL COMMENT '打卡日期（yyyy-MM-dd）',
    origin VARCHAR(50) NOT NULL COMMENT '来源词书/表名（动态表名，需白名单校验）',
    status VARCHAR(4) NOT NULL COMMENT '状态：yes/no 等',
    word VARCHAR(100) NOT NULL COMMENT '单词拼写',
    id INT NOT NULL COMMENT '单词在词书中的ID（word_id）',
    UNIQUE KEY uk_daily_user_date_origin_id (username, review_date, origin, id),
    INDEX idx_daily_user_date (username, review_date),
    INDEX idx_daily_user_origin_date (username, origin, review_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='每日学习/复习打卡明细';

-- ==============================
-- 4) 用户-单词画像进度：user_word_progress
-- ==============================
CREATE TABLE IF NOT EXISTS user_word_progress (
    progress_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(20) NOT NULL COMMENT '用户名',
    origin VARCHAR(50) NOT NULL COMMENT '来源词书/表名（动态表名，需白名单校验）',
    word_id INT NOT NULL COMMENT '单词在词书中的ID（word_id）',
    word VARCHAR(100) NOT NULL COMMENT '单词拼写（冗余字段，便于列表展示）',
    stage TINYINT NOT NULL DEFAULT 0 COMMENT '熟练度阶段（0~5），上限5',
    correct_cnt INT NOT NULL DEFAULT 0 COMMENT '累计答对次数',
    wrong_cnt INT NOT NULL DEFAULT 0 COMMENT '累计答错次数',
    streak INT NOT NULL DEFAULT 0 COMMENT '连续答对次数（答错归零）',
    last_result TINYINT NULL COMMENT '最近一次结果：1=对，0=错，NULL=未判定',
    first_seen_at DATETIME NOT NULL COMMENT '首次出现时间',
    last_seen_at DATETIME NOT NULL COMMENT '最近一次出现时间',
    next_review_at DATETIME NULL COMMENT '下次复习时间（SRS调度）',
    UNIQUE KEY uk_user_origin_word (username, origin, word_id),
    INDEX idx_user_origin_next (username, origin, next_review_at),
    INDEX idx_user_origin_last (username, origin, last_seen_at),
    INDEX idx_user_origin_stage (username, origin, stage)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户-单词粒度画像（SRS进度）';

-- ==============================
-- 5) 用户-单词事件流水：user_word_event
-- ==============================
CREATE TABLE IF NOT EXISTS user_word_event (
    event_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(20) NOT NULL COMMENT '用户名',
    origin VARCHAR(50) NOT NULL COMMENT '来源词书/表名（动态表名，需白名单校验）',
    word_id INT NOT NULL COMMENT '单词在词书中的ID（word_id）',
    result TINYINT NOT NULL COMMENT '结果：1=对，0=错',
    event_type VARCHAR(10) NOT NULL COMMENT '事件类型：learn/test/review',
    created_at DATETIME NOT NULL COMMENT '事件发生时间',
    INDEX idx_user_origin_time (username, origin, created_at),
    INDEX idx_user_origin_word_time (username, origin, word_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户-单词事件流水（用于统计与追溯）';

