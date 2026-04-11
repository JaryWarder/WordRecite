-- ====================================
-- 背单词项目 - 完整数据库初始化脚本
-- 作者：基于XMLParser生成的SQL修复版
-- 日期：2025-10-24
-- ====================================

-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER_SET_CLIENT = utf8mb4;
SET CHARACTER_SET_RESULTS = utf8mb4;
SET collation_connection = utf8mb4_unicode_ci;

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS bs_project 
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE bs_project;

-- ====================================
-- 第1部分：用户表
-- ====================================

DROP TABLE IF EXISTS user;

CREATE TABLE user (
    username    VARCHAR(20) NOT NULL,
    password    VARCHAR(20) NOT NULL,
    email       VARCHAR(50),
    phone       VARCHAR(20),
    education   ENUM('primary', 'junior', 'senior', 'undergraduate', 'graduate'),
    studying    VARCHAR(50),
    studied     INT DEFAULT 0,
    plan        INT DEFAULT 20,
    lastDate    DATE,
    day1        INT DEFAULT 0,
    day2        INT DEFAULT 0,
    day3        INT DEFAULT 0,
    day4        INT DEFAULT 0,
    day5        INT DEFAULT 0,
    day6        INT DEFAULT 0,
    day7        INT DEFAULT 0,
    PRIMARY KEY (username),
    INDEX idx_studying (studying),
    INDEX idx_lastDate (lastDate)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 插入测试用户
INSERT INTO user VALUES 
('user01', '20072324', 'songcx2211@sina.com', '18867107419', 'undergraduate', 'TOEFLWordBook', 40, 20, NULL, 0, 0, 0, 0, 0, 0, 0),
('user02', '20072324', 'aaa@sina.com', '18867107419', 'undergraduate', 'none', 0, 20, NULL, 0, 0, 0, 0, 0, 0, 0),
('user03', '123456', 'bbb@sina.com', '18812341234', 'undergraduate', 'none', 0, 20, NULL, 0, 0, 0, 0, 0, 0, 0),
('user04', '123456', 'ccc@sina.com', '18812341234', 'senior', 'none', 0, 20, NULL, 0, 0, 0, 0, 0, 0, 0),
('user05', '654321', 'ddd@sina.com', '18812341234', 'senior', 'none', 0, 20, NULL, 0, 0, 0, 0, 0, 0, 0),
('user06', '654321', 'eee@sina.com', '18812341234', 'senior', 'none', 0, 20, NULL, 0, 0, 0, 0, 0, 0, 0),
('user07', '111111', 'fff@sina.com', '18812341234', 'senior', 'none', 0, 20, NULL, 0, 0, 0, 0, 0, 0, 0);

SELECT '✅ 用户表创建完成' AS Status;

-- ====================================
-- 第2部分：CET-6词汇表
-- ====================================

DROP TABLE IF EXISTS CET6WordBook;

CREATE TABLE CET6WordBook (
    id INT PRIMARY KEY AUTO_INCREMENT,
    word VARCHAR(100) NOT NULL UNIQUE,
    phonetic VARCHAR(100),
    pron VARCHAR(255),
    poses TEXT,              -- ✅ TEXT类型，解决字段长度问题
    sentences TEXT,          -- ✅ TEXT类型
    antonyms VARCHAR(500),
    synonyms VARCHAR(500),
    INDEX idx_word (word),
    INDEX idx_id (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SELECT '✅ CET6WordBook表创建完成，准备导入数据...' AS Status;

-- 注意：CET-6单词数据需要从 CET-6.sql 文件导入
-- 这里不包含INSERT语句，因为数据量太大
-- 请在执行完此脚本后，执行：SOURCE /path/to/CET-6.sql;

-- ====================================
-- 第3部分：GRE词汇表
-- ====================================

DROP TABLE IF EXISTS GREWordBook;

CREATE TABLE GREWordBook (
    id INT PRIMARY KEY AUTO_INCREMENT,
    word VARCHAR(100) NOT NULL UNIQUE,
    phonetic VARCHAR(100),
    pron VARCHAR(255),
    poses TEXT,              -- ✅ TEXT类型
    sentences TEXT,          -- ✅ TEXT类型
    antonyms VARCHAR(500),
    synonyms VARCHAR(500),
    INDEX idx_word (word),
    INDEX idx_id (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SELECT '✅ GREWordBook表创建完成，准备导入数据...' AS Status;

-- 注意：GRE单词数据需要从 GRE.sql 文件导入
-- 请在执行完此脚本后，执行：SOURCE /path/to/GRE.sql;

-- ====================================
-- 第4部分：TOEFL词汇表
-- ====================================

DROP TABLE IF EXISTS TOEFLWordBook;

CREATE TABLE TOEFLWordBook (
    id INT PRIMARY KEY AUTO_INCREMENT,
    word VARCHAR(100) NOT NULL UNIQUE,
    phonetic VARCHAR(100),
    pron VARCHAR(255),
    poses TEXT,              -- ✅ TEXT类型
    sentences TEXT,          -- ✅ TEXT类型
    antonyms VARCHAR(500),
    synonyms VARCHAR(500),
    INDEX idx_word (word),
    INDEX idx_id (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SELECT '✅ TOEFLWordBook表创建完成，准备导入数据...' AS Status;

-- 注意：TOEFL单词数据需要从 TOEFL.sql 文件导入
-- 请在执行完此脚本后，执行：SOURCE /path/to/TOEFL.sql;

-- ====================================
-- 验证表结构
-- ====================================

SELECT '========== 数据库表列表 ==========' AS '';
SHOW TABLES;

SELECT '========== 用户表结构 ==========' AS '';
DESCRIBE user;

SELECT '========== CET6WordBook表结构 ==========' AS '';
DESCRIBE CET6WordBook;

SELECT '========== GREWordBook表结构 ==========' AS '';
DESCRIBE GREWordBook;

SELECT '========== TOEFLWordBook表结构 ==========' AS '';
DESCRIBE TOEFLWordBook;

SELECT '✅ ✅ ✅ 所有表创建完成！' AS Status;
SELECT '⚠️ 接下来请执行以下命令导入单词数据：' AS NextStep;
SELECT 'SOURCE /path/to/CET-6.sql;' AS Command1;
SELECT 'SOURCE /path/to/GRE.sql;' AS Command2;
SELECT 'SOURCE /path/to/TOEFL.sql;' AS Command3;
