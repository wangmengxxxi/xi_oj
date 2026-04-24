-- 修复 question_data_2.sql 已入库后的数据问题
-- 目标题号：66, 68, 69, 80, 86, 87, 88

START TRANSACTION;

-- 1) 先做备份（按题号）
CREATE TABLE IF NOT EXISTS `question_fix_backup_20260424` LIKE `question`;
INSERT IGNORE INTO `question_fix_backup_20260424`
SELECT *
FROM `question`
WHERE `id` IN (66, 68, 69, 80, 86, 87, 88);

-- 2) 修复题号 66：answer 中字符字面量 '*'
UPDATE `question`
SET `answer` = REPLACE(
  `answer`,
  CONCAT('==', CHAR(92), CHAR(39), '*', CHAR(39), ')'),
  CONCAT('==', CHAR(92), CHAR(39), '*', CHAR(92), CHAR(39), ')')
)
WHERE `id` = 66;

-- 3) 修复题号 68：answer 中字符字面量 '+'
UPDATE `question`
SET `answer` = REPLACE(
  `answer`,
  CONCAT('==', CHAR(92), CHAR(39), '+', CHAR(39), '||'),
  CONCAT('==', CHAR(92), CHAR(39), '+', CHAR(92), CHAR(39), '||')
)
WHERE `id` = 68;

-- 4) 修复题号 69：隐藏样例错误（1000000 的结果）
UPDATE `question`
SET `judgeCase` = '[{"input":"1","output":"1"},{"input":"4","output":"8"},{"input":"10","output":"512"},{"input":"1000000","output":"617521033"}]'
WHERE `id` = 69;

-- 5) 修复题号 80：题面样例与判题样例错误
UPDATE `question`
SET
  `content` = REPLACE(
    `content`,
    '**输出样例**\n```\n4\n```',
    '**输出样例**\n```\n3\n```'
  ),
  `judgeCase` = '[{"input":"5 2\\n1 1 1 2 3","output":"3"},{"input":"3 3\\n1 2 3","output":"2"},{"input":"1 0\\n0","output":"1"}]'
WHERE `id` = 80;

-- 6) 修复题号 86：题面样例与判题样例错误
UPDATE `question`
SET
  `content` = REPLACE(
    `content`,
    '**输出样例**\n```\n6\n```',
    '**输出样例**\n```\n5\n```'
  ),
  `judgeCase` = '[{"input":"6\\n1 2 3 0 2 4","output":"5"},{"input":"1\\n1","output":"0"},{"input":"2\\n2 1","output":"0"}]'
WHERE `id` = 86;

-- 7) 修复题号 87：题面描述混入其他题意，重新写清
UPDATE `question`
SET
  `content` = '## 题目描述\n\n给定 n 个非负整数，求这 n 个整数的二进制表示中 1 的总个数。\n\n## 输入格式\n\n第一行：整数 n。\n\n第二行：n 个非负整数。\n\n## 输出格式\n\n输出所有整数二进制中 1 的总个数。\n\n## 示例\n\n**输入样例**\n```\n4\n1 3 7 0\n```\n**输出样例**\n```\n6\n```\n\n## 数据范围\n\n- 1 ≤ n ≤ 10^5\n- 0 ≤ 整数 ≤ 10^9',
  `tags` = '["位运算", "数组"]'
WHERE `id` = 87;

-- 8) 修复题号 88：输入格式与示例不一致
UPDATE `question`
SET `content` = REPLACE(
  `content`,
  '接下来 k 行每行一个单词。',
  '接下来一行给出 k 个单词（以空格分隔）。'
)
WHERE `id` = 88;

COMMIT;

-- 可选核验（执行后检查）
SELECT `id`, `title`, JSON_VALID(`judgeCase`) AS `judgeCaseIsValid`
FROM `question`
WHERE `id` IN (69, 80, 86);

SELECT `id`, `title`,
       JSON_UNQUOTE(JSON_EXTRACT(`judgeCase`, '$[0].output')) AS `firstCaseOutput`
FROM `question`
WHERE `id` IN (69, 80, 86)
  AND JSON_VALID(`judgeCase`) = 1;
