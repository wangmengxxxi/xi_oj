-- ============================================================
-- AI schema patch
-- 日期：2026-04-22
-- 目标：
-- 1. 为 question 表补充 difficulty 字段
-- 2. 为 ai_wrong_question 表补充 language 字段
-- ============================================================

ALTER TABLE question
    ADD COLUMN difficulty varchar(16) DEFAULT NULL COMMENT '题目难度（建议值：easy / medium / hard）'
    AFTER answer;

ALTER TABLE ai_wrong_question
    ADD COLUMN language varchar(32) DEFAULT NULL COMMENT '错题记录对应的代码语言（java / python / cpp 等）'
    AFTER wrong_code;
