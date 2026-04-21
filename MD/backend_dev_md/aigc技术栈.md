场景一：简历/项目介绍（一行流）
AIGC 模块技术栈：Tool Calling · Chat Memory 多轮记忆 · 多实例 Agent 分工 · SSE 流式输出 · RAG 检索（content_type 元数据过滤精准召回 + tag 前置粗筛 & 向量排序精排的两阶段相似题推荐）

场景二：文档/README 展开描述

AIGC 核心技术栈：

• Tool Calling（工具调用）
  — 3个业务工具：题目查询 / 代码评测 / 错题查询
  — 使用 @P 注解独立参数，LLM 无需自拼格式字符串

• Chat Memory（多轮对话记忆）
  — Caffeine 本地缓存，TTL 30分钟，最大 1000 会话
  — 仅 OJChatAgent 携带记忆，其余 Agent 无状态，资源隔离

• 多实例 Agent 分工
  — OJChatAgent：有状态 + Tools + RAG + SSE（AI 问答）
  — OJQuestionParseAgent：无状态 + RAG + SSE（题目解析）
  — OJStreamingService：无状态纯推流，Prompt 由 Service 层完整构建（代码分析 / 错题分析）

• SSE 流式输出
  — Flux<String> 推流，Token 包装为 {"d":"<token>"} JSON 保留空白字符

• RAG 检索
  — 向量库：Milvus + 阿里百炼 text-embedding-v3（1024维）
  — content_type 元数据过滤：精准控制检索范围（知识点 / 代码模板 / 错题分析等）
  — 两阶段相似题推荐：tag 交集前置粗筛 → 向量相似度精排，规避语义相似但考点不同的误召回
如果把文档里新增的 4 项优化也算进去，可以补充：

• ReAct 思考规范（@SystemMessage 内置）
  — 引导模型先明确信息需求、再调用工具、再基于结果决策，减少幻觉跳结论

• RAG 结果 Redis 缓存（TTL 1小时）
  — 相同 query 复用检索结果，热门题目 Embedding API 调用从 N 次降为 1 次

• Prompt 动态管理（ai_config 表 + Redis 缓存）
  — Prompt 模板热更新，管理员后台修改 5分钟内全局生效，无需重启