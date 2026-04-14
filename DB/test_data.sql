-- ============================================================
-- WordRecite 全链路功能测试数据
-- 测试账号: test_user / 密码: 123456（明文存储）
-- 执行前提: 已完成建表脚本，词书数据已导入
-- 生成时间: 2026-04-14
-- ============================================================

USE bs_project;

-- ============================================================
-- STEP 1: 用户表
-- ============================================================
INSERT INTO `user` (
    username, password, email, education,
    studying, studied, plan,
    lastDate, day1, day2, day3, day4, day5, day6, day7
) VALUES (
    'test_user', '123456', 'test_user@example.com', 'undergraduate',
    'CET6WordBook', 87, 20,
    CURDATE(), 15, 22, 18, 25, 20, 17, 12
) ON DUPLICATE KEY UPDATE
    password   = '123456',
    email      = 'test_user@example.com',
    studying   = 'CET6WordBook',
    studied    = 87,
    plan       = 20,
    lastDate   = CURDATE(),
    day1 = 15, day2 = 22, day3 = 18,
    day4 = 25, day5 = 20, day6 = 17, day7 = 12;

-- ============================================================
-- STEP 2: 词书注册（word_book）
-- ============================================================
INSERT INTO `word_book` (title, num) VALUES
    ('CET6WordBook',  2369),
    ('TOEFLWordBook', 1263),
    ('GREWordBook',   1861)
ON DUPLICATE KEY UPDATE num = VALUES(num);

-- ============================================================
-- STEP 3: 用户单词进度（user_word_progress）
-- 覆盖 5 种记忆阶段，确保 SRS 逻辑全面触发
-- ============================================================

-- 阶段说明：
--   stage=0  刚接触，今天的新词
--   stage=1  学过一次，需要今天复习（next_review_at <= NOW()）
--   stage=2  短期记忆，明天复习
--   stage=3  中期记忆，3天后复习
--   stage=4  长期记忆，7天后复习
--   stage=5  完全掌握，30天后复习

INSERT INTO `user_word_progress`
    (username, origin, word_id, word, stage, correct_cnt, wrong_cnt, streak,
     last_result, first_seen_at, last_seen_at, next_review_at)
VALUES
-- ① 今日新词（stage=0，尚未复习）
('test_user','CET6WordBook', 101,'abbreviate',  0, 0,0,0, NULL,
 NOW() - INTERVAL 2 HOUR,  NOW() - INTERVAL 2 HOUR,  NULL),
('test_user','CET6WordBook', 102,'abolish',     0, 0,0,0, NULL,
 NOW() - INTERVAL 1 HOUR,  NOW() - INTERVAL 1 HOUR,  NULL),
('test_user','CET6WordBook', 103,'abstract',    0, 0,0,0, NULL,
 NOW() - INTERVAL 30 MINUTE, NOW() - INTERVAL 30 MINUTE, NULL),

-- ② 需要今天复习（stage=1，next_review_at 已过期）
('test_user','CET6WordBook', 201,'accelerate',  1, 1,0,1, 1,
 NOW() - INTERVAL 2 DAY,   NOW() - INTERVAL 1 DAY,   NOW() - INTERVAL 3 HOUR),
('test_user','CET6WordBook', 202,'accommodate', 1, 1,1,0, 0,
 NOW() - INTERVAL 3 DAY,   NOW() - INTERVAL 1 DAY,   NOW() - INTERVAL 5 HOUR),
('test_user','CET6WordBook', 203,'accomplish',  1, 2,0,2, 1,
 NOW() - INTERVAL 2 DAY,   NOW() - INTERVAL 1 DAY,   NOW() - INTERVAL 1 HOUR),
('test_user','CET6WordBook', 204,'accumulate',  1, 1,0,1, 1,
 NOW() - INTERVAL 2 DAY,   NOW() - INTERVAL 1 DAY,   NOW() - INTERVAL 2 HOUR),

-- ③ 明天复习（stage=2）
('test_user','CET6WordBook', 301,'accurate',    2, 3,1,2, 1,
 NOW() - INTERVAL 4 DAY,   NOW() - INTERVAL 12 HOUR, NOW() + INTERVAL 12 HOUR),
('test_user','CET6WordBook', 302,'acknowledge', 2, 2,0,2, 1,
 NOW() - INTERVAL 3 DAY,   NOW() - INTERVAL 8 HOUR,  NOW() + INTERVAL 16 HOUR),

-- ④ 3天后复习（stage=3）
('test_user','CET6WordBook', 401,'acquire',     3, 5,1,3, 1,
 NOW() - INTERVAL 7 DAY,   NOW() - INTERVAL 1 DAY,   NOW() + INTERVAL 3 DAY),
('test_user','CET6WordBook', 402,'adapt',       3, 4,2,2, 1,
 NOW() - INTERVAL 6 DAY,   NOW() - INTERVAL 2 DAY,   NOW() + INTERVAL 2 DAY),

-- ⑤ 7天后复习（stage=4）
('test_user','CET6WordBook', 501,'adequate',    4, 8,1,5, 1,
 NOW() - INTERVAL 14 DAY,  NOW() - INTERVAL 3 DAY,   NOW() + INTERVAL 7 DAY),
('test_user','CET6WordBook', 502,'adjacent',    4, 7,0,7, 1,
 NOW() - INTERVAL 12 DAY,  NOW() - INTERVAL 4 DAY,   NOW() + INTERVAL 6 DAY),

-- ⑥ 完全掌握（stage=5，30天后复习）
('test_user','CET6WordBook', 601,'adjust',      5,12,2,8, 1,
 NOW() - INTERVAL 30 DAY,  NOW() - INTERVAL 7 DAY,   NOW() + INTERVAL 30 DAY),
('test_user','CET6WordBook', 602,'admire',      5,10,1,9, 1,
 NOW() - INTERVAL 28 DAY,  NOW() - INTERVAL 6 DAY,   NOW() + INTERVAL 28 DAY),
('test_user','CET6WordBook', 603,'adopt',       5,11,0,11,1,
 NOW() - INTERVAL 25 DAY,  NOW() - INTERVAL 5 DAY,   NOW() + INTERVAL 25 DAY)

ON DUPLICATE KEY UPDATE
    stage         = VALUES(stage),
    correct_cnt   = VALUES(correct_cnt),
    wrong_cnt     = VALUES(wrong_cnt),
    streak        = VALUES(streak),
    last_result   = VALUES(last_result),
    last_seen_at  = VALUES(last_seen_at),
    next_review_at= VALUES(next_review_at);

-- ============================================================
-- STEP 4: 事件流水（user_word_event）
-- 覆盖 learn / review / test 三种类型，近7天分布
-- ============================================================
INSERT INTO `user_word_event`
    (username, origin, word_id, result, event_type, created_at)
VALUES
-- Day -6: 初次学习一批词
('test_user','CET6WordBook', 601, 1,'learn', NOW() - INTERVAL 6 DAY + INTERVAL 9 HOUR),
('test_user','CET6WordBook', 602, 1,'learn', NOW() - INTERVAL 6 DAY + INTERVAL 9 HOUR),
('test_user','CET6WordBook', 603, 0,'learn', NOW() - INTERVAL 6 DAY + INTERVAL 9 HOUR),
('test_user','CET6WordBook', 501, 1,'learn', NOW() - INTERVAL 6 DAY + INTERVAL 10 HOUR),
('test_user','CET6WordBook', 502, 1,'learn', NOW() - INTERVAL 6 DAY + INTERVAL 10 HOUR),

-- Day -5: 复习 + 测试
('test_user','CET6WordBook', 601, 1,'review', NOW() - INTERVAL 5 DAY + INTERVAL 9 HOUR),
('test_user','CET6WordBook', 602, 1,'review', NOW() - INTERVAL 5 DAY + INTERVAL 9 HOUR),
('test_user','CET6WordBook', 603, 1,'review', NOW() - INTERVAL 5 DAY + INTERVAL 9 HOUR),
('test_user','CET6WordBook', 501, 1,'test',   NOW() - INTERVAL 5 DAY + INTERVAL 20 HOUR),
('test_user','CET6WordBook', 502, 0,'test',   NOW() - INTERVAL 5 DAY + INTERVAL 20 HOUR),

-- Day -4: 学习新词
('test_user','CET6WordBook', 401, 1,'learn', NOW() - INTERVAL 4 DAY + INTERVAL 8 HOUR),
('test_user','CET6WordBook', 402, 0,'learn', NOW() - INTERVAL 4 DAY + INTERVAL 8 HOUR),
('test_user','CET6WordBook', 501, 1,'review', NOW() - INTERVAL 4 DAY + INTERVAL 14 HOUR),
('test_user','CET6WordBook', 502, 1,'review', NOW() - INTERVAL 4 DAY + INTERVAL 14 HOUR),
('test_user','CET6WordBook', 601, 1,'test',   NOW() - INTERVAL 4 DAY + INTERVAL 21 HOUR),

-- Day -3: 复习 + 测试
('test_user','CET6WordBook', 401, 1,'review', NOW() - INTERVAL 3 DAY + INTERVAL 9 HOUR),
('test_user','CET6WordBook', 402, 1,'review', NOW() - INTERVAL 3 DAY + INTERVAL 9 HOUR),
('test_user','CET6WordBook', 301, 1,'learn',  NOW() - INTERVAL 3 DAY + INTERVAL 10 HOUR),
('test_user','CET6WordBook', 302, 1,'learn',  NOW() - INTERVAL 3 DAY + INTERVAL 10 HOUR),
('test_user','CET6WordBook', 601, 1,'test',   NOW() - INTERVAL 3 DAY + INTERVAL 20 HOUR),
('test_user','CET6WordBook', 602, 1,'test',   NOW() - INTERVAL 3 DAY + INTERVAL 20 HOUR),
('test_user','CET6WordBook', 603, 1,'test',   NOW() - INTERVAL 3 DAY + INTERVAL 20 HOUR),

-- Day -2: 学习 + 复习
('test_user','CET6WordBook', 201, 1,'learn',  NOW() - INTERVAL 2 DAY + INTERVAL 8 HOUR),
('test_user','CET6WordBook', 202, 0,'learn',  NOW() - INTERVAL 2 DAY + INTERVAL 8 HOUR),
('test_user','CET6WordBook', 203, 1,'learn',  NOW() - INTERVAL 2 DAY + INTERVAL 8 HOUR),
('test_user','CET6WordBook', 204, 1,'learn',  NOW() - INTERVAL 2 DAY + INTERVAL 8 HOUR),
('test_user','CET6WordBook', 301, 1,'review', NOW() - INTERVAL 2 DAY + INTERVAL 15 HOUR),
('test_user','CET6WordBook', 302, 1,'review', NOW() - INTERVAL 2 DAY + INTERVAL 15 HOUR),
('test_user','CET6WordBook', 401, 1,'test',   NOW() - INTERVAL 2 DAY + INTERVAL 21 HOUR),

-- Day -1: 复习 + 测试
('test_user','CET6WordBook', 201, 1,'review', NOW() - INTERVAL 1 DAY + INTERVAL 9 HOUR),
('test_user','CET6WordBook', 202, 1,'review', NOW() - INTERVAL 1 DAY + INTERVAL 9 HOUR),
('test_user','CET6WordBook', 203, 1,'review', NOW() - INTERVAL 1 DAY + INTERVAL 9 HOUR),
('test_user','CET6WordBook', 204, 0,'review', NOW() - INTERVAL 1 DAY + INTERVAL 9 HOUR),
('test_user','CET6WordBook', 501, 1,'test',   NOW() - INTERVAL 1 DAY + INTERVAL 20 HOUR),
('test_user','CET6WordBook', 502, 1,'test',   NOW() - INTERVAL 1 DAY + INTERVAL 20 HOUR),

-- Day 0 (今天): 学习新词 + 复习到期词
('test_user','CET6WordBook', 101, 1,'learn',  NOW() - INTERVAL 2 HOUR),
('test_user','CET6WordBook', 102, 1,'learn',  NOW() - INTERVAL 2 HOUR),
('test_user','CET6WordBook', 103, 0,'learn',  NOW() - INTERVAL 2 HOUR),
('test_user','CET6WordBook', 201, 1,'review', NOW() - INTERVAL 1 HOUR),
('test_user','CET6WordBook', 202, 1,'review', NOW() - INTERVAL 1 HOUR);

-- ============================================================
-- STEP 5: 每日足迹（daily）
-- 近7天每天都有打卡记录，支撑连续打卡与每日词量统计
-- status: 'yes'=已掌握, 'no'=待复习
-- ============================================================
INSERT INTO `daily`
    (username, review_date, origin, status, word, id)
VALUES
-- Day -6
('test_user', CURDATE() - INTERVAL 6 DAY, 'CET6WordBook', 'yes', 'adjust',      601),
('test_user', CURDATE() - INTERVAL 6 DAY, 'CET6WordBook', 'yes', 'admire',      602),
('test_user', CURDATE() - INTERVAL 6 DAY, 'CET6WordBook', 'no',  'adopt',       603),
('test_user', CURDATE() - INTERVAL 6 DAY, 'CET6WordBook', 'yes', 'adequate',    501),
('test_user', CURDATE() - INTERVAL 6 DAY, 'CET6WordBook', 'yes', 'adjacent',    502),

-- Day -5
('test_user', CURDATE() - INTERVAL 5 DAY, 'CET6WordBook', 'yes', 'adjust',      601),
('test_user', CURDATE() - INTERVAL 5 DAY, 'CET6WordBook', 'yes', 'admire',      602),
('test_user', CURDATE() - INTERVAL 5 DAY, 'CET6WordBook', 'yes', 'adopt',       603),
('test_user', CURDATE() - INTERVAL 5 DAY, 'CET6WordBook', 'yes', 'adequate',    501),
('test_user', CURDATE() - INTERVAL 5 DAY, 'CET6WordBook', 'no',  'adjacent',    502),
('test_user', CURDATE() - INTERVAL 5 DAY, 'CET6WordBook', 'yes', 'acquire',     401),
('test_user', CURDATE() - INTERVAL 5 DAY, 'CET6WordBook', 'yes', 'adapt',       402),

-- Day -4
('test_user', CURDATE() - INTERVAL 4 DAY, 'CET6WordBook', 'yes', 'acquire',     401),
('test_user', CURDATE() - INTERVAL 4 DAY, 'CET6WordBook', 'no',  'adapt',       402),
('test_user', CURDATE() - INTERVAL 4 DAY, 'CET6WordBook', 'yes', 'adequate',    501),
('test_user', CURDATE() - INTERVAL 4 DAY, 'CET6WordBook', 'yes', 'adjacent',    502),
('test_user', CURDATE() - INTERVAL 4 DAY, 'CET6WordBook', 'yes', 'adjust',      601),

-- Day -3
('test_user', CURDATE() - INTERVAL 3 DAY, 'CET6WordBook', 'yes', 'acquire',     401),
('test_user', CURDATE() - INTERVAL 3 DAY, 'CET6WordBook', 'yes', 'adapt',       402),
('test_user', CURDATE() - INTERVAL 3 DAY, 'CET6WordBook', 'yes', 'accurate',    301),
('test_user', CURDATE() - INTERVAL 3 DAY, 'CET6WordBook', 'yes', 'acknowledge', 302),
('test_user', CURDATE() - INTERVAL 3 DAY, 'CET6WordBook', 'yes', 'adjust',      601),
('test_user', CURDATE() - INTERVAL 3 DAY, 'CET6WordBook', 'yes', 'admire',      602),
('test_user', CURDATE() - INTERVAL 3 DAY, 'CET6WordBook', 'yes', 'adopt',       603),

-- Day -2
('test_user', CURDATE() - INTERVAL 2 DAY, 'CET6WordBook', 'yes', 'accelerate',  201),
('test_user', CURDATE() - INTERVAL 2 DAY, 'CET6WordBook', 'no',  'accommodate', 202),
('test_user', CURDATE() - INTERVAL 2 DAY, 'CET6WordBook', 'yes', 'accomplish',  203),
('test_user', CURDATE() - INTERVAL 2 DAY, 'CET6WordBook', 'yes', 'accumulate',  204),
('test_user', CURDATE() - INTERVAL 2 DAY, 'CET6WordBook', 'yes', 'accurate',    301),
('test_user', CURDATE() - INTERVAL 2 DAY, 'CET6WordBook', 'yes', 'acknowledge', 302),
('test_user', CURDATE() - INTERVAL 2 DAY, 'CET6WordBook', 'yes', 'acquire',     401),

-- Day -1
('test_user', CURDATE() - INTERVAL 1 DAY, 'CET6WordBook', 'yes', 'accelerate',  201),
('test_user', CURDATE() - INTERVAL 1 DAY, 'CET6WordBook', 'yes', 'accommodate', 202),
('test_user', CURDATE() - INTERVAL 1 DAY, 'CET6WordBook', 'yes', 'accomplish',  203),
('test_user', CURDATE() - INTERVAL 1 DAY, 'CET6WordBook', 'no',  'accumulate',  204),
('test_user', CURDATE() - INTERVAL 1 DAY, 'CET6WordBook', 'yes', 'adequate',    501),
('test_user', CURDATE() - INTERVAL 1 DAY, 'CET6WordBook', 'yes', 'adjacent',    502),

-- Day 0 (今天)
('test_user', CURDATE(), 'CET6WordBook', 'yes', 'abbreviate',  101),
('test_user', CURDATE(), 'CET6WordBook', 'yes', 'abolish',     102),
('test_user', CURDATE(), 'CET6WordBook', 'no',  'abstract',    103),
('test_user', CURDATE(), 'CET6WordBook', 'yes', 'accelerate',  201),
('test_user', CURDATE(), 'CET6WordBook', 'yes', 'accommodate', 202)

ON DUPLICATE KEY UPDATE status = VALUES(status);

-- ============================================================
-- STEP 6: 私人单词本（private_books）
-- 收藏了答错或重点关注的单词
-- ============================================================
INSERT INTO `private_books` (username, origin, id, word)
VALUES
('test_user', 'CET6WordBook', 103, 'abstract'),
('test_user', 'CET6WordBook', 202, 'accommodate'),
('test_user', 'CET6WordBook', 402, 'adapt'),
('test_user', 'CET6WordBook', 502, 'adjacent'),
('test_user', 'CET6WordBook', 603, 'adopt'),
('test_user', 'TOEFLWordBook', 88,  'ambiguous'),
('test_user', 'TOEFLWordBook', 156, 'coherent'),
('test_user', 'TOEFLWordBook', 234, 'empirical')
ON DUPLICATE KEY UPDATE word = VALUES(word);

-- ============================================================
-- 执行完毕，验证数据量
-- ============================================================
SELECT 'user'               AS tbl, COUNT(*) AS cnt FROM user              WHERE username='test_user'
UNION ALL
SELECT 'user_word_progress' AS tbl, COUNT(*) AS cnt FROM user_word_progress WHERE username='test_user'
UNION ALL
SELECT 'user_word_event'    AS tbl, COUNT(*) AS cnt FROM user_word_event    WHERE username='test_user'
UNION ALL
SELECT 'daily'              AS tbl, COUNT(*) AS cnt FROM daily              WHERE username='test_user'
UNION ALL
SELECT 'private_books'      AS tbl, COUNT(*) AS cnt FROM private_books      WHERE username='test_user';
