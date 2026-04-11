/* =========================================================
   Insert 1,000,000 users into table `user`
   username: million0 ~ million999999
   Compatible with your schema in compete_init.sql
   ========================================================= */

USE bs_project;

-- 如果你要重复跑脚本，建议先清掉旧数据（可选）
-- DELETE FROM `user` WHERE username LIKE 'million%';

INSERT INTO `user`
(
  username, password, email, phone, education, studying,
  studied, plan, lastDate,
  day1, day2, day3, day4, day5, day6, day7
)
SELECT
  CONCAT('million', n)                               AS username,
  '123456'                                           AS password,
  CONCAT('million', n, '@example.com')               AS email,
  CONCAT('1', LPAD(n, 10, '0'))                      AS phone,      -- 11位字符串，<= VARCHAR(20)
  CASE (n % 5)
    WHEN 0 THEN 'primary'
    WHEN 1 THEN 'junior'
    WHEN 2 THEN 'senior'
    WHEN 3 THEN 'undergraduate'
    ELSE 'graduate'
  END                                                AS education,
  CASE (n % 4)
    WHEN 0 THEN 'none'
    WHEN 1 THEN 'CET6WordBook'
    WHEN 2 THEN 'GREWordBook'
    ELSE 'TOEFLWordBook'
  END                                                AS studying,
  0                                                  AS studied,
  20                                                 AS plan,
  NULL                                               AS lastDate,
  0, 0, 0, 0, 0, 0, 0
FROM
(
  /* 生成 0..999999 的数字序列（6位十进制：000000~999999） */
  SELECT
    a.d
    + 10      * b.d
    + 100     * c.d
    + 1000    * d.d
    + 10000   * e.d
    + 100000  * f.d AS n
  FROM
    (SELECT 0 d UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
     UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) a
    CROSS JOIN
    (SELECT 0 d UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
     UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) b
    CROSS JOIN
    (SELECT 0 d UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
     UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) c
    CROSS JOIN
    (SELECT 0 d UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
     UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) d
    CROSS JOIN
    (SELECT 0 d UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
     UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) e
    CROSS JOIN
    (SELECT 0 d UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
     UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) f
) nums
WHERE n BETWEEN 0 AND 999999;

-- 验证插入数量（可选）
-- SELECT COUNT(*) FROM `user` WHERE username LIKE 'million%';
