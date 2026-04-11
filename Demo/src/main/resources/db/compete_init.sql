-- ====================================
-- 背单词项目 - 完整数据库初始化脚本
-- ====================================

SET NAMES utf8mb4;
SET CHARACTER_SET_CLIENT = utf8mb4;
SET CHARACTER_SET_RESULTS = utf8mb4;
SET collation_connection = utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS bs_project CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bs_project;

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
    id INT PRIMARY KEY AUTO_INCREMENT,
    word VARCHAR(100) NOT NULL UNIQUE,
    phonetic VARCHAR(100),
    pron VARCHAR(255),
    poses TEXT,
    sentences TEXT,
    antonyms VARCHAR(500),
    synonyms VARCHAR(500),
    INDEX idx_word (word),
    INDEX idx_id (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS GREWordBook;
CREATE TABLE GREWordBook (
    id INT PRIMARY KEY AUTO_INCREMENT,
    word VARCHAR(100) NOT NULL UNIQUE,
    phonetic VARCHAR(100),
    pron VARCHAR(255),
    poses TEXT,
    sentences TEXT,
    antonyms VARCHAR(500),
    synonyms VARCHAR(500),
    INDEX idx_word (word),
    INDEX idx_id (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS TOEFLWordBook;
CREATE TABLE TOEFLWordBook (
    id INT PRIMARY KEY AUTO_INCREMENT,
    word VARCHAR(100) NOT NULL UNIQUE,
    phonetic VARCHAR(100),
    pron VARCHAR(255),
    poses TEXT,
    sentences TEXT,
    antonyms VARCHAR(500),
    synonyms VARCHAR(500),
    INDEX idx_word (word),
    INDEX idx_id (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
