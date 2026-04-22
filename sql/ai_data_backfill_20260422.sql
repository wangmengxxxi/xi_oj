-- ============================================================
-- AI historical data backfill
-- 日期：2026-04-22
-- 目标：
-- 1. 回填 ai_wrong_question.language
-- 2. 回填 question.difficulty
-- 说明：
-- - language 可以依据历史提交自动回填
-- - difficulty 当前没有绝对权威来源，默认采用“先安全初始化，再人工校准”的方案
-- ============================================================

-- ------------------------------------------------------------
-- 一、回填 ai_wrong_question.language
-- ------------------------------------------------------------
-- 策略 1：优先按 user_id + question_id + wrong_code 精确匹配历史普通提交
-- 说明：
-- - 排除 source='ai_tool'，避免把 AI 工具测试提交回填进正式错题本
-- - 排除逻辑删除记录
-- - 同一代码命中多条时，取最近一次
UPDATE ai_wrong_question awq
SET awq.language = (
    SELECT qs.language
    FROM question_submit qs
    WHERE qs.userId = awq.user_id
      AND qs.questionId = awq.question_id
      AND qs.isDelete = 0
      AND (qs.source IS NULL OR qs.source <> 'ai_tool')
      AND qs.code = awq.wrong_code
    ORDER BY qs.updateTime DESC, qs.id DESC
    LIMIT 1
)
WHERE awq.language IS NULL;

-- 策略 2：若仍未命中，再按同用户同题目的最近一次普通提交兜底
UPDATE ai_wrong_question awq
SET awq.language = (
    SELECT qs.language
    FROM question_submit qs
    WHERE qs.userId = awq.user_id
      AND qs.questionId = awq.question_id
      AND qs.isDelete = 0
      AND (qs.source IS NULL OR qs.source <> 'ai_tool')
    ORDER BY qs.updateTime DESC, qs.id DESC
    LIMIT 1
)
WHERE awq.language IS NULL;

-- 若历史上确实找不到对应提交，统一兜底为 unknown，避免后续空值判断分散
UPDATE ai_wrong_question
SET language = 'unknown'
WHERE language IS NULL OR TRIM(language) = '';

-- ------------------------------------------------------------
-- 二、回填 question.difficulty
-- ------------------------------------------------------------
-- 当前 question 表原本没有 difficulty 历史来源，因此这里采用保守初始化：
-- - 先将空值统一初始化为 medium
-- - 后续由运营/出题人再按实际题目质量手工修正
UPDATE question
SET difficulty = 'medium'
WHERE difficulty IS NULL OR TRIM(difficulty) = '';

-- ------------------------------------------------------------
-- 三、difficulty 可选启发式修正（默认注释，不自动执行）
-- ------------------------------------------------------------
-- 使用建议：
-- 1. 只有在 submitNum / acceptedNum 统计可信时再启用
-- 2. 建议先在测试环境观察结果，再放到正式库执行
-- 3. 执行后仍建议人工抽样复核

-- 规则示例：
-- - submitNum >= 20 且通过率 >= 70% -> easy
-- - submitNum >= 20 且通过率 < 35%  -> hard
-- - 其余保留 medium

-- UPDATE question
-- SET difficulty = 'easy'
-- WHERE submitNum >= 20
--   AND acceptedNum / submitNum >= 0.70;

-- UPDATE question
-- SET difficulty = 'hard'
-- WHERE submitNum >= 20
--   AND acceptedNum / submitNum < 0.35;

-- ------------------------------------------------------------
-- 四、执行后建议校验
-- ------------------------------------------------------------
-- SELECT language, COUNT(*) FROM ai_wrong_question GROUP BY language ORDER BY COUNT(*) DESC;
-- SELECT difficulty, COUNT(*) FROM question GROUP BY difficulty ORDER BY COUNT(*) DESC;
