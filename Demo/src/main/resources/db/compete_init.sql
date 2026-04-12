-- ====================================
-- 背单词项目 - 完整数据库初始化脚本
-- ====================================

SET NAMES utf8mb4;
SET CHARACTER_SET_CLIENT = utf8mb4;
SET CHARACTER_SET_RESULTS = utf8mb4;
SET collation_connection = utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS bs_project CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bs_project;

DROP TABLE IF EXISTS word_book;
CREATE TABLE word_book (
    title VARCHAR(20) NOT NULL COMMENT '词书表名/标题（如 TOEFLWordBook）',
    num INT NOT NULL COMMENT '词书单词总数',
    PRIMARY KEY (title)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='词书列表（title -> num）';

INSERT INTO word_book (title, num) VALUES
('TOEFLWordBook', 1300),
('CET6WordBook', 2083),
('GREWordBook', 3063);

DROP TABLE IF EXISTS private_books;
CREATE TABLE private_books (
    pid INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(20) NOT NULL COMMENT '用户名',
    origin VARCHAR(50) NOT NULL COMMENT '来源词书/表名（动态表名，需白名单校验）',
    id INT NOT NULL COMMENT '单词在词书中的ID（word_id）',
    word VARCHAR(100) NOT NULL COMMENT '单词拼写',
    UNIQUE KEY uk_private_books (username, origin, id),
    INDEX idx_private_user_origin (username, origin)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='私人单词本';

DROP TABLE IF EXISTS daily;
CREATE TABLE daily (
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

DROP TABLE IF EXISTS user_word_progress;
CREATE TABLE user_word_progress (
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

DROP TABLE IF EXISTS user_word_event;
CREATE TABLE user_word_event (
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

DROP TABLE IF EXISTS user;
CREATE TABLE user (
    username    VARCHAR(20) NOT NULL COMMENT '用户名',
    password    VARCHAR(20) NOT NULL COMMENT '密码（演示版明文存储）',
    email       VARCHAR(50) COMMENT '邮箱',
    phone       VARCHAR(20) COMMENT '手机号',
    education   ENUM('primary', 'junior', 'senior', 'undergraduate', 'graduate') COMMENT '教育阶段',
    studying    VARCHAR(50) COMMENT '当前学习词书（动态表名）',
    studied     INT DEFAULT 0 COMMENT '已学习单词数（旧模块遗留统计）',
    plan        INT DEFAULT 20 COMMENT '每日计划学习数量（旧模块遗留）',
    lastDate    DATE COMMENT '最近打卡日期（旧模块遗留）',
    day1        INT DEFAULT 0 COMMENT '近7天打卡统计（旧模块遗留）',
    day2        INT DEFAULT 0 COMMENT '近7天打卡统计（旧模块遗留）',
    day3        INT DEFAULT 0 COMMENT '近7天打卡统计（旧模块遗留）',
    day4        INT DEFAULT 0 COMMENT '近7天打卡统计（旧模块遗留）',
    day5        INT DEFAULT 0 COMMENT '近7天打卡统计（旧模块遗留）',
    day6        INT DEFAULT 0 COMMENT '近7天打卡统计（旧模块遗留）',
    day7        INT DEFAULT 0 COMMENT '近7天打卡统计（旧模块遗留）',
    PRIMARY KEY (username),
    INDEX idx_studying (studying),
    INDEX idx_lastDate (lastDate)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

INSERT INTO user VALUES 
('user01', '20072324', 'songcx2211@sina.com', '18867107419', 'undergraduate', 'TOEFLWordBook', 40, 20, NULL, 0, 0, 0, 0, 0, 0, 0),
('user02', '20072324', 'aaa@sina.com', '18867107419', 'undergraduate', 'none', 0, 20, NULL, 0, 0, 0, 0, 0, 0, 0),
('user03', '123456', 'bbb@sina.com', '18812341234', 'undergraduate', 'none', 0, 20, NULL, 0, 0, 0, 0, 0, 0, 0),
('user04', '123456', 'ccc@sina.com', '18812341234', 'senior', 'none', 0, 20, NULL, 0, 0, 0, 0, 0, 0, 0),
('user05', '654321', 'ddd@sina.com', '18812341234', 'senior', 'none', 0, 20, NULL, 0, 0, 0, 0, 0, 0, 0),
('user06', '654321', 'eee@sina.com', '18812341234', 'senior', 'none', 0, 20, NULL, 0, 0, 0, 0, 0, 0, 0),
('user07', '111111', 'fff@sina.com', '18812341234', 'senior', 'none', 0, 20, NULL, 0, 0, 0, 0, 0, 0, 0);

DROP TABLE IF EXISTS CET6WordBook;
CREATE TABLE CET6WordBook (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    word VARCHAR(100) NOT NULL UNIQUE COMMENT '单词拼写（唯一）',
    phonetic VARCHAR(100) COMMENT '音标',
    pron VARCHAR(255) COMMENT '发音资源',
    poses TEXT COMMENT '释义与词性（可能较长）',
    sentences TEXT COMMENT '例句（可能较长）',
    antonyms VARCHAR(500) COMMENT '反义词',
    synonyms VARCHAR(500) COMMENT '同义词',
    INDEX idx_word (word),
    INDEX idx_id (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CET6词库';

DROP TABLE IF EXISTS GREWordBook;
CREATE TABLE GREWordBook (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    word VARCHAR(100) NOT NULL UNIQUE COMMENT '单词拼写（唯一）',
    phonetic VARCHAR(100) COMMENT '音标',
    pron VARCHAR(255) COMMENT '发音资源',
    poses TEXT COMMENT '释义与词性（可能较长）',
    sentences TEXT COMMENT '例句（可能较长）',
    antonyms VARCHAR(500) COMMENT '反义词',
    synonyms VARCHAR(500) COMMENT '同义词',
    INDEX idx_word (word),
    INDEX idx_id (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='GRE词库';

DROP TABLE IF EXISTS TOEFLWordBook;
CREATE TABLE TOEFLWordBook (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    word VARCHAR(100) NOT NULL UNIQUE COMMENT '单词拼写（唯一）',
    phonetic VARCHAR(100) COMMENT '音标',
    pron VARCHAR(255) COMMENT '发音资源',
    poses TEXT COMMENT '释义与词性（可能较长）',
    sentences TEXT COMMENT '例句（可能较长）',
    antonyms VARCHAR(500) COMMENT '反义词',
    synonyms VARCHAR(500) COMMENT '同义词',
    INDEX idx_word (word),
    INDEX idx_id (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='TOEFL词库';
