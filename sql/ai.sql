CREATE TABLE IF NOT EXISTS ai_config
(
    id          bigint auto_increment comment 'id' primary key,
    config_key  varchar(128) NOT NULL comment '配置键',
    config_value text comment '配置值',
    description varchar(512) comment '配置描述',
    is_enable   tinyint default 1 not null comment '是否启用，0=禁用（回退代码默认值），1=启用',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    UNIQUE KEY uk_config_key (config_key)
    ) comment 'AI系统配置表' collate = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ai_chat_record
(
    id          bigint auto_increment comment 'id' primary key,
    user_id     bigint NOT NULL comment '用户id',
    question    text NOT NULL comment '用户问题',
    answer      text comment 'AI回答',
    chat_id     varchar(64) NOT NULL comment '会话id，用于区分多轮对话',
    used_tokens int default 0 comment '消耗token数',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    index idx_user_id (user_id),
    index idx_chat_id (chat_id)
    ) comment 'AI对话记录表' collate = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ai_code_analysis
(
    id              bigint auto_increment comment 'id' primary key,
    user_id         bigint NOT NULL comment '用户id',
    question_id     bigint NOT NULL comment '题目id',
    code            text NOT NULL comment '用户提交的代码',
    language        varchar(32) NOT NULL comment '代码语言',
    analysis_result text NOT NULL comment 'AI分析结果',
    score           int comment '代码评分',
    judge_result    varchar(32) comment '判题结果（AC/WA/TLE等）',
    createTime      datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    index idx_user_id (user_id),
    index idx_question_id (question_id)
    ) comment '代码AI分析记录表' collate = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS question_comment
(
    id          bigint auto_increment comment 'id' primary key,
    question_id bigint NOT NULL comment '题目id',
    user_id     bigint NOT NULL comment '评论用户id',
    content     text NOT NULL comment '评论内容',
    parent_id   bigint default 0 comment '父评论id，用于回复',
    like_num    int default 0 not null comment '点赞数',
    is_delete   tinyint default 0 not null comment '是否删除',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_question_id (question_id),
    index idx_user_id (user_id)
    ) comment '题目评论表' collate = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_profile
(
    id              bigint auto_increment comment 'id' primary key,
    user_id         bigint NOT NULL comment '用户id',
    nickname        varchar(128) comment '用户昵称',
    avatar          varchar(512) comment '头像地址',
    school          varchar(128) comment '学校',
    signature       varchar(512) comment '个性签名',
    solved_num      int default 0 not null comment '已解决题目数',
    submit_num      int default 0 not null comment '总提交数',
    rating          int default 1200 not null comment '用户评分',
    createTime      datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime      datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    UNIQUE KEY uk_user_id (user_id)
    ) comment '用户信息拓展表' collate = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ai_wrong_question
(
    id                  bigint auto_increment comment 'id' primary key,
    user_id             bigint NOT NULL comment '用户id',
    question_id         bigint NOT NULL comment '题目id',
    wrong_code          text NOT NULL comment '错误代码',
    wrong_judge_result  varchar(32) NOT NULL comment '错误判题结果',
    wrong_analysis      text comment 'AI错误分析',
    review_plan         text comment 'AI生成的复习计划',
    similar_questions   text comment 'AI推荐的同类题目（JSON数组）',
    is_reviewed         tinyint default 0 not null comment '是否已复习',
    review_count        int default 0 not null comment '复习次数',
    next_review_time    datetime comment '下次复习时间',
    createTime          datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime          datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_user_id (user_id),
    index idx_question_id (question_id),
    index idx_next_review_time (next_review_time)
    ) comment 'AI错题本表' collate = utf8mb4_unicode_ci;

INSERT INTO ai_config (config_key, config_value, description) VALUES
('ai.global.enable',          'true',                                                       'AI功能全局开关，false时所有AI接口返回关闭提示'),
('ai.model.base_url',         'https://dashscope.aliyuncs.com/compatible-mode/v1',          '百炼OpenAI兼容端点，通常无需修改'),
('ai.model.name',             'qwen-plus',                                                  '聊天模型名称（可选：qwen-turbo / qwen-plus / qwen-max）'),
('ai.model.embedding_name',   'text-embedding-v3',                                          '嵌入模型名称，修改后需重建Milvus向量索引'),
('ai.rag.top_k',              '3',                                                          'RAG检索返回条数（建议3-5）'),
('ai.rag.similarity_threshold','0.7',                                                       'RAG最小相似度阈值（0-1，值越高越严格）');

INSERT INTO rate_limit_rule (rule_key, rule_name, limit_count, window_seconds, is_enable, description) VALUES
('ai:user:minute',       'AI接口用户-分钟级限流',    10,  60,    1, 'AI接口用户每分钟最多调用10次，防突发请求'),
('ai:ip:minute',         'AI接口IP-分钟级限流',       30,  60,    1, 'AI接口同一IP每分钟最多调用30次，防代理滥用'),
('ai:chat:user:day',     'AI问答用户-每日限流',       100, 86400, 1, 'AI问答用户每天最多调用100次'),
('ai:question:user:day', 'AI题目解析用户-每日限流',    50,  86400, 1, 'AI题目解析用户每天最多调用50次'),
('ai:code:user:day',     'AI代码分析用户-每日限流',    30,  86400, 1, 'AI代码分析用户每天最多调用30次，单次成本最高'),
('ai:wrong:user:day',    'AI错题分析用户-每日限流',    30,  86400, 1, 'AI错题分析用户每天最多调用30次，单次成本最高');


/**.
  AI配置动态管理
 */
-- Prompt 动态管理：各模块 Prompt 模板，可由管理员在后台调整，无需重启
INSERT INTO ai_config (config_key, config_value, description, is_enable) VALUES
                                                                             ('ai.prompt.code_analysis',
                                                                              '你是一位资深 Java/算法教学助手，请对以下代码进行多维度分析：\n1. 代码风格与规范评分（10分制）；\n2. 逻辑正确性与边界情况；\n3. 时间/空间复杂度分析；\n4. 针对错误给出改进建议，不直接给出完整答案。\n回答语言：中文，格式清晰。',
                                                                              '代码智能分析模块 Prompt 前置引导语', 1),
                                                                             ('ai.prompt.wrong_analysis',
                                                                              '你是一位编程学习辅导专家，基于以下错题信息，请：\n1. 指出错误根本原因（逻辑错误/边界遗漏/算法选择不当）；\n2. 给出分步骤改正思路，不直接提供完整正确代码；\n3. 总结该类题目的通用解题规律。\n回答语言：中文，适合初学者理解。',
                                                                              '错题本分析模块 Prompt 前置引导语', 1),
                                                                             ('ai.prompt.question_parse',
                                                                              '你是XI OJ平台的题目解析助手，请对以下题目进行结构化分析：\n1. 考点分析：涉及哪些算法与数据结构；\n2. 分步骤解题思路，引导用户独立思考；\n3. 常见易错点与边界情况。\n回答格式结构清晰，语言通俗，适配编程初学者。',
                                                                              '题目解析模块 Prompt 前置引导语', 1);

/**
  question_submit 表扩展：新增 source 字段
  用途：区分用户正常提交 vs AI 工具（OJTools.judgeUserCode）调用产生的测试性提交
  - source IS NULL / ''  = 用户正常提交，计入 solved_num / submit_num / acceptedNum 统计
  - source = 'ai_tool'   = Agent 工具调用的沙箱测试，排除在所有统计之外
  注意事项：
  1. 执行前确认 question_submit 表已存在（来自 oj_db.sql）
  2. 若已执行过此脚本，重复执行会报 Duplicate column 错误，忽略即可（DDL 幂等保护见下方注释）
  3. 统计查询（QuestionSubmitMapper.countAcceptedByUser / countTotalByUser）均已加 WHERE 过滤，详见后端代码
 */
ALTER TABLE question_submit
    ADD COLUMN source varchar(32) DEFAULT NULL COMMENT '提交来源（NULL=用户正常提交；ai_tool=AI工具调用的测试性提交，排除在用户统计之外）'
    AFTER judgeInfo;

-- 为 source 字段建索引，便于统计查询时快速过滤 ai_tool 记录
-- 若 question_submit 数据量较大（>50万行），建议在业务低峰期执行
CREATE INDEX idx_source ON question_submit (source);