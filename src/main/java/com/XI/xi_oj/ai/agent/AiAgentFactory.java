package com.XI.xi_oj.ai.agent;

import com.XI.xi_oj.ai.store.AiChatMemoryStore;
import com.XI.xi_oj.ai.tools.OJTools;
import com.XI.xi_oj.service.AiConfigService;
import com.XI.xi_oj.utils.TimeUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import io.milvus.param.MetricType;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// ─────────────────────────────────────────────
// AI工厂：统一构建所有 AI 实例，集中管理动态配置
// ─
@Configuration
public class AiAgentFactory {
    @Resource
    private OJTools ojTools;

    @Resource
    private AiConfigService aiConfigService;

    /** API Key 从环境变量注入，不走数据库，避免敏感凭证落库 */
    @Value("${ai.model.api-key}")
    private String apikey;

    @Value("${milvus.host:localhost}")
    private String milvusHost;

    @Value("${milvus.port:19530}")
    private int milvusPort;

    /**
     * Milvus 向量库连接 Bean
     * - collectionName：全局唯一集合，所有类型数据通过 content_type 字段区分
     * - dimension：必须与 text-embedding-v3 维度一致（默认 1024）
     * - 集合自动创建说明：在 langchain4j 1.0.0-beta3 中，集合不存在时会自动创建，无需额外开关
     * - metricType：COSINE 余弦相似度，适合文本语义匹配
     */
    @Bean
    public MilvusEmbeddingStore embeddingStore(){
        return MilvusEmbeddingStore.builder()
                .host(milvusHost)
                .port(milvusPort)
                .collectionName("oj_knowledge")
                .dimension(1024)
                .autoFlushOnInsert(true)
                .metricType(MetricType.COSINE)
                .build();
    }
    /**
     * 共享 ChatModel（阻塞式，供非流式调用使用）
     */
    @Bean
    public ChatLanguageModel chatModel() {
        return QwenChatModel.builder()
                .apiKey(apikey)
                .modelName(aiConfigService.getConfigValue("ai.model.name"))
                .temperature(0.2f)
                .maxTokens(2048)
                .build();
    }



    /**
     * 共享 StreamingChatModel（流式，供 SSE 接口使用）
     * 与 ChatModel 共享同一 API Key 和模型配置
     */
    @Bean
    public StreamingChatLanguageModel streamingChatModel() {
        return QwenStreamingChatModel.builder()
                .apiKey(apikey)
                .modelName(aiConfigService.getConfigValue("ai.model.name"))
                .temperature(0.2f)
                .maxTokens(2048)
                .build();
    }
    /**
     * 共享 EmbeddingModel（无状态，单例复用）
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        return QwenEmbeddingModel.builder()
                .apiKey(apikey)
                .modelName(aiConfigService.getConfigValue("ai.model.embedding_name"))
                .build();
    }


    /**
     * 会话记忆存储：Redis + MySQL 持久化
     * - Redis：会话热数据（低延迟读取）
     * - MySQL：历史真相源（Redis miss 时回源重建）
     */
    @Bean
    public ChatMemoryStore chatMemoryStore(AiChatMemoryStore aiChatMemoryStore) {
        return aiChatMemoryStore;
    }
    /**
     * 做备用如果压测不行再加
     * Caffeine 缓存：按 chatId 缓存 ChatMemory 对象
     * - 缓存的是轻量 ChatMemory（消息列表，几KB），而非 AiService 实例（数MB）
     * - expireAfterAccess(30min)：会话 30 分钟无活动自动释放，防止内存泄漏
     * - maximumSize(1000)：最多同时持有 1000 个活跃会话，超出时按 LRU 淘汰
     */
//    @Bean
//    public Cache<String, ChatMemory> chatMemoryCache() {
//        return Caffeine.newBuilder()
//                .expireAfterAccess(TimeUtil.minutes(30))
//                .maximumSize(1000)
//                .build();
//    }

    /**
     * 5.3 AI问答 Agent：多轮记忆 + Tools + RAG + 流式/非流式双模式
     * chatMemoryProvider 每次基于 chatId 构建 MessageWindowChatMemory
     * → maxMessages(20)：仅保留最近 20 条对话上下文，控制 token 成本
     * → chatMemoryStore(持久化)：Memory 读写落 Redis，Redis miss 自动回源 MySQL
     * → 框架根据方法返回值类型自动选择模型（String=阻塞，Flux=流式）
     */
    @Bean
    public OJChatAgent ojChatAgent(ChatLanguageModel chatModel,
                                   StreamingChatLanguageModel streamingChatModel,
                                   EmbeddingModel embeddingModel,
                                   MilvusEmbeddingStore embeddingStore,
                                   ChatMemoryStore chatMemoryStore) {
        return AiServices.builder(OJChatAgent.class)
                .chatLanguageModel(chatModel)
                .streamingChatLanguageModel(streamingChatModel)
                .tools(ojTools)
                .contentRetriever(buildRetriever(embeddingModel, embeddingStore))
                .chatMemoryProvider(chatId ->
                        dev.langchain4j.memory.chat.MessageWindowChatMemory.builder()
                                .id(chatId)
                                .maxMessages(20)
                                .chatMemoryStore(chatMemoryStore)
                                .build())
                .build();
    }

    /**
     * 5.2/5.5 无状态流式服务：只有 StreamingChatModel，无记忆无 RAG
     * Prompt 由 Service 层手动拼装（含 RAG 检索结果）后整体传入
     */
    @Bean
    public OJStreamingService ojStreamingService(StreamingChatLanguageModel streamingChatModel) {
        return AiServices.builder(OJStreamingService.class)
                .streamingChatLanguageModel(streamingChatModel)
                .build();
    }


    /**
     * 公共方法：构建 ContentRetriever，参数从 ai_config 动态读取
     */
    private ContentRetriever buildRetriever(EmbeddingModel embeddingModel,
                                            MilvusEmbeddingStore embeddingStore) {
        int topK = Integer.parseInt(aiConfigService.getConfigValue("ai.rag.top_k"));
        double minScore = Double.parseDouble(aiConfigService.getConfigValue("ai.rag.similarity_threshold"));
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(topK)
                .minScore(minScore)
                .build();
    }
}