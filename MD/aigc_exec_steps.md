# XI OJ AIGC 功能整合 — 施工级执行手册

> 版本：V2.1  
> 日期：2026-04-16  
> 适用对象：后端工程师 / 前端工程师 / 测试工程师（不依赖 AI 辅助开发）  
> 对齐基线：Java 21、Spring Boot 3.3.x、`server.servlet.context-path=/api`、单体架构

---

## 0. 使用说明（先读）

本手册是“施工文档”，不是方案文档。执行要求：

1. 必须按里程碑顺序推进，禁止跳阶段并行改动。
2. 每完成一个里程碑，必须通过该里程碑 DoD（Definition of Done）再进入下一阶段。
3. 每个里程碑必须单独提交一次 Git commit，便于回滚。
4. 若与 `MD/backend_dev_md/backend_dev.md` 冲突，以**本手册 + 当前代码基线**为准。

建议分支：

```bash
git checkout -b feature/aigc-manual-v2
```

---

## 1. 里程碑总览（必须按顺序）

| 里程碑 | 目标 | 负责人建议 | 预计耗时 |
|---|---|---|---|
| M0 | 环境与基线核对 | 后端 | 0.5 天 |
| M1 | 数据库与实体层落地 | 后端 | 0.5-1 天 |
| M2 | AI 配置读取与全局开关 | 后端 | 0.5 天 |
| M3 | AI 基础能力（模型、RAG、向量导入） | 后端 | 1-1.5 天 |
| M4 | AI 问答（阻塞版）+ 限流 | 后端 | 1 天 |
| M5 | AI 代码分析 / 题解 / 错题本 | 后端 | 1-2 天 |
| M6 | 前端页面与路由接入 | 前端 | 1-2 天 |
| M7 | 联调、压测、发布清单 | 后端+前端+测试 | 1 天 |

---

## 2. M0 环境与基线核对

### 2.1 必查项

在项目根目录执行：

```bash
java -version
mvn -v
git status
```

通过标准：

1. Java 版本为 21（最低 17）。
2. Maven 可用，能识别 `pom.xml`。
3. 工作区可控（确认已有改动是否可并行）。

### 2.2 基础服务检查

```bash
# MySQL / Redis 自行按本机方式检查
# Milvus（Docker）示例：
docker ps
```

通过标准：

1. MySQL 可连接到 `oj_db`。
2. Redis 可读写。
3. Milvus 可访问（19530）。

### 2.3 代码基线检查（必须确认）

1. `src/main/java/com/XI/xi_oj/ai` 当前为空目录（表示 AI 业务待实现）。
2. `pom.xml` 已有 `langchain4j`/`dashscope`/`milvus` 依赖。
3. `src/main/resources/application.yml` 已有 `ai.model.api-key` 和 `milvus.host/port`。
4. `MainApplication` 已有 `@EnableScheduling`，尚无 `@EnableAsync`。

### 2.4 M0 DoD

1. 所有检查通过。
2. 创建分支并首次提交（可为空提交）：

```bash
git commit --allow-empty -m "chore(aigc): bootstrap milestone M0"
```

---

## 3. M1 数据库与实体层落地

### 3.1 SQL 执行顺序

执行文件（首次执行）：

1. `sql/oj_db.sql`（基础表）
2. `sql/rate_limit.sql`（提交限流）
3. `sql/ai.sql`（AIGC 扩展表 + AI 限流规则）

注意：

1. `sql/ai.sql` 里的 `INSERT` 含唯一键，重复执行可能报主键/唯一键冲突。
2. 若重复执行，请改为手工校验后补数据，不要盲目重跑整文件。

### 3.2 后端实体/Mapper落地

在以下目录新增（若不存在）：

- `src/main/java/com/XI/xi_oj/model/entity/`
- `src/main/java/com/XI/xi_oj/mapper/`

至少创建：

1. `AiConfig`
2. `AiChatRecord`
3. `AiCodeAnalysis`
4. `AiWrongQuestion`
5. `QuestionComment`
6. `UserProfile`

Mapper 命名对应：

1. `AiConfigMapper`
2. `AiChatRecordMapper`
3. `AiCodeAnalysisMapper`
4. `AiWrongQuestionMapper`
5. `QuestionCommentMapper`
6. `UserProfileMapper`

### 3.3 映射规范（必须遵守）

当前项目 `mybatis-plus.configuration.map-underscore-to-camel-case=false`，因此必须二选一：

1. 实体字段用下划线风格（如 `config_key`）；
2. 或使用驼峰字段 + `@TableField("xxx")` 显式映射。

推荐：沿用项目已有风格，避免隐式映射错误。

### 3.4 M1 DoD

执行：

```bash
mvn -q -DskipTests compile
```

通过标准：

1. 编译通过。
2. 六张 AI 相关表可查询。
3. `MapperScan` 能扫描到新增 Mapper（启动不报 mapper bean 缺失）。

提交：

```bash
git add .
git commit -m "feat(aigc): add ai tables entities and mappers (M1)"
```

---

## 4. M2 AI 配置读取与全局开关

### 4.1 新增服务

新增：

- `AiConfigService`
- `AiConfigServiceImpl`

必要能力：

1. `getConfigValue(configKey)`：先 Redis、后 DB。
2. `isAiEnabled()`：读取 `ai.global.enable`。
3. `refreshConfigCache(configKey)`：配置变更后刷新。

### 4.2 新增管理接口

新增管理控制器（管理员权限）：

- `GET /admin/ai/config`
- `POST /admin/ai/config`

注意：

1. 仅在 Controller 上写相对路径（不要写 `/api/...`）。
2. API Key 不允许通过接口写入数据库，继续使用环境变量注入。

### 4.3 新增全局开关切面

新增 AOP（如 `AiGlobalSwitchAspect`）：

1. 拦截 AI 控制器入口。
2. `ai.global.enable=false` 时统一抛业务异常。

### 4.4 M2 DoD

通过标准：

1. 管理员能读取配置。
2. 关闭 `ai.global.enable` 后，AI 接口统一拒绝访问。
3. 普通业务接口不受影响。

提交：

```bash
git add .
git commit -m "feat(aigc): ai config service admin api and global switch aspect (M2)"
```

---

## 5. M3 AI 基础能力（模型/RAG/向量导入）

### 5.1 主启动类补齐

在 `MainApplication` 增加：

- `@EnableAsync`

保留已有：

- `@EnableScheduling`
- `@EnableAspectJAutoProxy`

### 5.2 新增 AI 基础包结构

建议创建：

- `src/main/java/com/XI/xi_oj/ai/config`
- `src/main/java/com/XI/xi_oj/ai/retriever`
- `src/main/java/com/XI/xi_oj/ai/service`
- `src/main/java/com/XI/xi_oj/ai/controller`
- `src/main/java/com/XI/xi_oj/ai/vector`

### 5.3 落地最小可运行组件

至少实现：

1. `AiAgentFactory`（或同等配置类）：构建 ChatModel / EmbeddingModel / MilvusEmbeddingStore。
2. `OJKnowledgeRetriever`：`retrieve` 与 `retrieveByType`。
3. `KnowledgeInitializer`：启动时导入 `src/main/resources/knowledge/*.md`。
4. `QuestionVectorSyncJob`：定时同步题目向量（先全量，后续再优化增量）。
5. `KnowledgeImportController`：管理员上传 md 导入知识。

### 5.4 关键数据约束（必须）

1. 向量化题目数据时，不写入标准答案。
2. `Question.tags` 当前是 JSON 字符串，处理时先按字符串存元数据，后续再做标签结构化。

### 5.5 M3 DoD

通过标准：

1. 启动后 Milvus 集合存在。
2. `src/main/resources/knowledge/algorithm_knowledge.md`、`src/main/resources/knowledge/error_analysis.md` 可导入。
3. 定时任务可执行且日志可见。

提交：

```bash
git add .
git commit -m "feat(aigc): model rag and vector import pipeline (M3)"
```

---

## 6. M4 AI 问答（阻塞版）+ AI 限流

### 6.1 问答优先做阻塞版

先实现：

- `POST /ai/chat`
- `GET /ai/chat/history`
- `POST /ai/chat/clear`

说明：

1. 先不做 SSE，降低落地复杂度。
2. 问答记录写入 `ai_chat_record`。

### 6.2 AI 限流扩展（基于现有限流体系）

在 `RateLimitTypeEnum` 与 `RateLimitInterceptor` 扩展 AI 枚举与分支：

1. `ai:user:minute`
2. `ai:ip:minute`
3. `ai:chat:user:day`
4. `ai:code:user:day`
5. `ai:question:user:day`
6. `ai:wrong:user:day`

并将 AI 接口加 `@RateLimit(types=...)`。

### 6.3 M4 DoD

通过标准：

1. AI 问答接口可返回内容。
2. 历史可查可清。
3. 人工压测触发 AI 限流后返回 429 业务码（与前端拦截器兼容）。

提交：

```bash
git add .
git commit -m "feat(aigc): ai chat and ai-specific rate limit (M4)"
```

---

## 7. M5 AI 代码分析 / 题解 / 错题本

### 7.1 代码分析

实现：

- `POST /ai/code/analysis`
- `GET /ai/code/history`

要求：

1. 输入：题目 + 代码 + 语言 + 判题结果。
2. 输出：结构化分析文本 + 持久化记录。

### 7.2 题目解析与相似题

实现：

- `GET /ai/question/parse`
- `GET /ai/question/similar`

要求：

1. `parse` 走 RAG。
2. `similar` 先向量检索，再查题库补全信息。

### 7.3 错题本

实现：

- `GET /ai/wrong-question/list`
- `GET /ai/wrong-question/analysis`
- `POST /ai/wrong-question/review`

并在 `JudgeServiceImpl#doJudge` 结果落库后追加“非 AC 自动收集”。

### 7.4 M5 DoD

通过标准：

1. 错误提交后自动生成错题记录。
2. 可对错题触发 AI 分析并保存。
3. 标记复习后状态字段正确更新。

提交：

```bash
git add .
git commit -m "feat(aigc): code analysis question parse and wrong-book (M5)"
```

---

## 8. M6 前端接入

### 8.1 新增 API 封装

新增前端 API 文件（建议）：

- `frontend/OJ_frontend/src/api/ai.ts`

至少封装：

1. chat/chatHistory/chatClear
2. codeAnalysis/codeHistory
3. questionParse/questionSimilar
4. wrongQuestionList/wrongQuestionAnalysis/wrongQuestionReview

### 8.2 页面与路由

新增页面（建议路径）：

- `frontend/OJ_frontend/src/views/ai/AiChatView.vue`
- `frontend/OJ_frontend/src/views/ai/AiCodeAnalysisView.vue`
- `frontend/OJ_frontend/src/views/ai/WrongQuestionView.vue`

并更新：

- `frontend/OJ_frontend/src/router/index.ts`
- `frontend/OJ_frontend/src/layouts/BasicLayout.vue`

### 8.3 前端开关策略

页面加载时读取 `GET /api/admin/ai/config` 的 `ai.global.enable`：

1. false：隐藏 AI 入口 + 已打开页面展示“功能关闭”。
2. true：正常展示。

### 8.4 M6 DoD

通过标准：

1. 普通用户可进入 AI 页面并调用接口。
2. 限流报错（42900）前端能正确展示。
3. 全局开关关闭时 UI 行为正确。

提交：

```bash
git add .
git commit -m "feat(frontend): integrate ai pages routes and api clients (M6)"
```

---

## 9. M7 联调、压测与发布

### 9.1 联调清单

1. 登录后 AI 聊天链路。
2. 提交错误代码 -> 错题收集 -> AI 分析。
3. 题目解析 + 相似题推荐。
4. 管理员配置修改即时生效。

### 9.2 最小压测建议

1. 对 `POST /api/ai/chat` 做 50-100 并发短压。
2. 观察限流是否生效，服务是否稳定。
3. 记录平均响应、P95、错误率。

### 9.3 发布前检查

1. `AI_API_KEY` 已配置在部署环境。
2. 生产库已执行 `sql/ai.sql`。
3. Redis 与 Milvus 地址为生产地址。
4. 管理员账号可进入 AI 配置页。

### 9.4 回滚方案

1. 快速回滚代码：`git revert <commit>`（按里程碑回滚）。
2. 紧急关闭 AI：`ai.global.enable=false`。
3. 必要时前端隐藏 AI 入口（开关兜底）。

---

## 10. 故障排查（施工现场常见）

### 10.1 启动报错：找不到 AI Bean

排查：

1. `@ComponentScan` 范围是否包含 `com.XI.xi_oj.ai`。
2. AI 配置类是否 `@Configuration`。
3. 依赖是否下载成功（`mvn -U clean compile`）。

### 10.2 向量导入无结果

排查：

1. Milvus 是否可连接。
2. `src/main/resources/knowledge/*.md` 是否满足当前约定格式（首 3 行分别为 `content_type` / `tag` / `title`，第 4 行空行，正文从第 5 行开始）。
3. Embedding 维度是否与集合维度一致。

### 10.3 AI 接口全量返回禁止

排查：

1. `ai.global.enable` 是否为 `false`。
2. AOP 切点是否误拦截了非 AI 接口。

### 10.4 限流误触发

排查：

1. `rate_limit_rule` 中 AI 规则阈值是否过低。
2. Redis key 是否与 submit 维度混用。
3. 前端重试逻辑是否导致重复请求。

---

## 11. 交付物清单（验收必须）

1. 后端：AI 相关 Controller / Service / Mapper / Entity 全部入库。
2. 前端：AI 三页面 + 路由 + 菜单接入。
3. 数据：`ai.sql` 落库、知识文件可导入。
4. 文档：
   - 方案文档：`MD/backend_dev_md/backend_dev.md`
   - 施工手册：`MD/aigc_exec_steps.md`（本文件）
5. 演示：按 M7 联调清单完成一次端到端演示。

---

*文档版本：V2.1 | 更新日期：2026-04-16*
