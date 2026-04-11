USE bs_project;

-- CET-6 词汇表
CREATE TABLE CET6WordBook (
    id INT PRIMARY KEY AUTO_INCREMENT,
    word VARCHAR(100) NOT NULL UNIQUE,
    phonetic VARCHAR(100),
    pron VARCHAR(255),
    poses TEXT,
    sentences TEXT,
    antonyms VARCHAR(500),
    synonyms VARCHAR(500),
    INDEX idx_word (word)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- GRE 词汇表
CREATE TABLE GREWordBook (
    id INT PRIMARY KEY AUTO_INCREMENT,
    word VARCHAR(100) NOT NULL UNIQUE,
    phonetic VARCHAR(100),
    pron VARCHAR(255),
    poses TEXT,
    sentences TEXT,
    antonyms VARCHAR(500),
    synonyms VARCHAR(500),
    INDEX idx_word (word)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- TOEFL 词汇表
CREATE TABLE TOEFLWordBook (
    id INT PRIMARY KEY AUTO_INCREMENT,
    word VARCHAR(100) NOT NULL UNIQUE,
    phonetic VARCHAR(100),
    pron VARCHAR(255),
    poses TEXT,
    sentences TEXT,
    antonyms VARCHAR(500),
    synonyms VARCHAR(500),
    INDEX idx_word (word)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
