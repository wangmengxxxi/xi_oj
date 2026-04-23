package com.XI.xi_oj.ai.agent;

import com.XI.xi_oj.ai.event.AiConfigChangedEvent;
import com.XI.xi_oj.ai.tools.OJTools;
import com.XI.xi_oj.service.AiConfigService;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Set;

/**
 * AI模型持有者类，负责管理和初始化各种AI模型和代理
 * 该类使用Spring框架的组件注解，并提供了模型和代理的获取方法
 */
@Component
@Slf4j
public class AiModelHolder {

    // 定义模型名称的配置键集合
    private static final Set<String> MODEL_NAME_KEYS = Set.of("ai.model.name");
    // 定义嵌入模型名称的配置键集合
    private static final Set<String> EMBEDDING_NAME_KEYS = Set.of("ai.model.embedding_name");
    // 定义RAG（检索增强生成）相关的配置键集合
    private static final Set<String> RAG_KEYS = Set.of("ai.rag.top_k", "ai.rag.similarity_threshold");

    // 依赖注入的配置服务
    private final AiConfigService aiConfigService;
    // OJ工具服务
    private final OJTools ojTools;
    // Milvus向量存储
    private final MilvusEmbeddingStore embeddingStore;
    // 聊天记忆存储
    private final ChatMemoryStore chatMemoryStore;

    // 从配置文件中注入API密钥
    @Value("${ai.model.api-key}")
    private String apiKey;



    // 可变类型的AI模型和代理
    private volatile ChatLanguageModel chatModel;              // 聊天语言模型
    private volatile StreamingChatLanguageModel streamingChatModel;  // 流式聊天语言模型
    private volatile EmbeddingModel embeddingModel;            // 嵌入模型
    private volatile OJChatAgent ojChatAgent;                  // OJ聊天代理
    private volatile OJQuestionParseAgent ojQuestionParseAgent;  // OJ问题解析代理
    private volatile OJStreamingService ojStreamingService;    // OJ流式服务

    /**
     * 构造函数，注入必要的依赖项
     * @param aiConfigService AI配置服务
     * @param ojTools OJ工具服务
     * @param embeddingStore 嵌入模型存储
     * @param chatMemoryStore 聊天记忆存储
     */
    public AiModelHolder(AiConfigService aiConfigService,
                         OJTools ojTools,
                         @Qualifier("embeddingStore") MilvusEmbeddingStore embeddingStore,
                         ChatMemoryStore chatMemoryStore) {
        this.aiConfigService = aiConfigService;
        this.ojTools = ojTools;
        this.embeddingStore = embeddingStore;
        this.chatMemoryStore = chatMemoryStore;
    }

    /**
     * 初始化方法，在Bean创建后自动调用
     * 用于初始化所有AI模型和代理
     */
    @PostConstruct
    public void init() {
        this.chatModel = buildChatModel();
        this.streamingChatModel = buildStreamingChatModel();
        this.embeddingModel = buildEmbeddingModel();
        this.ojStreamingService = buildStreamingService(this.streamingChatModel);
        this.ojChatAgent = buildChatAgent();
        this.ojQuestionParseAgent = buildQuestionParseAgent();
        log.info("[AiModelHolder] all AI models and agents initialized");
    }

    /**
     * 配置变更事件监听器
     * 当AI配置发生变化时，重新构建相应的模型和代理
     * @param event 配置变更事件
     */
    @EventListener
    public void onConfigChanged(AiConfigChangedEvent event) {
        String key = event.getConfigKey();

        // 如果是模型名称相关配置变更
        if (MODEL_NAME_KEYS.contains(key)) {
            this.chatModel = buildChatModel();
            this.streamingChatModel = buildStreamingChatModel();
            this.ojStreamingService = buildStreamingService(this.streamingChatModel);
            this.ojChatAgent = buildChatAgent();
            this.ojQuestionParseAgent = buildQuestionParseAgent();
            log.info("[AiModelHolder] chat models and all agents rebuilt for config: {}", key);

        // 如果是嵌入模型名称相关配置变更
        } else if (EMBEDDING_NAME_KEYS.contains(key)) {
            this.embeddingModel = buildEmbeddingModel();
            this.ojChatAgent = buildChatAgent();
            this.ojQuestionParseAgent = buildQuestionParseAgent();
            log.info("[AiModelHolder] embedding model and agents rebuilt for config: {}", key);

        // 如果是RAG相关配置变更
        } else if (RAG_KEYS.contains(key)) {
            this.ojChatAgent = buildChatAgent();
            this.ojQuestionParseAgent = buildQuestionParseAgent();
            log.info("[AiModelHolder] agents rebuilt for RAG config: {}", key);
        }
    }

    // ── getters ──

    /**
     * 获取聊天语言模型
     * @return ChatLanguageModel 聊天语言模型实例
     */
    public ChatLanguageModel getChatModel() {
        return chatModel;
    }

    /**
     * 获取流式聊天语言模型
     * @return StreamingChatLanguageModel 流式聊天语言模型实例
     */
    public StreamingChatLanguageModel getStreamingChatModel() {
        return streamingChatModel;
    }

    /**
     * 获取嵌入模型
     * @return EmbeddingModel 嵌入模型实例
     */
    public EmbeddingModel getEmbeddingModel() {
        return embeddingModel;
    }

    /**
     * 获取OJ聊天代理
     * @return OJChatAgent OJ聊天代理实例
     */
    public OJChatAgent getOjChatAgent() {
        return ojChatAgent;
    }

    /**
     * 获取OJ问题解析代理
     * @return OJQuestionParseAgent OJ问题解析代理实例
     */
    public OJQuestionParseAgent getOjQuestionParseAgent() {
        return ojQuestionParseAgent;
    }

    /**
     * 获取OJ流式服务
     * @return OJStreamingService OJ流式服务实例
     */
    public OJStreamingService getOjStreamingService() {
        return ojStreamingService;
    }

    // ── builders ──

    /**
     * 构建聊天语言模型
     * @return ChatLanguageModel 构建好的聊天语言模型
     */
    private ChatLanguageModel buildChatModel() {
        String modelName = aiConfigService.getConfigValue("ai.model.name");
        return QwenChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(0.2f)
                .maxTokens(2048)
                .build();
    }

    /**
     * 构建流式聊天语言模型
     * @return StreamingChatLanguageModel 构建好的流式聊天语言模型
     */
    private StreamingChatLanguageModel buildStreamingChatModel() {
        String modelName = aiConfigService.getConfigValue("ai.model.name");
        return QwenStreamingChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(0.2f)
                .maxTokens(2048)
                .build();
    }

    /**
     * 构建嵌入模型
     * @return EmbeddingModel 构建好的嵌入模型
     */
    private EmbeddingModel buildEmbeddingModel() {
        String embeddingName = aiConfigService.getConfigValue("ai.model.embedding_name");
        return QwenEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName(embeddingName)
                .build();
    }

    /**
     * 构建流式服务
     * @param model 流式聊天语言模型
     * @return OJStreamingService 构建好的流式服务
     */
    private OJStreamingService buildStreamingService(StreamingChatLanguageModel model) {
        return fullPrompt -> Flux.create(sink -> model.chat(
                fullPrompt,
                new StreamingChatResponseHandler() {
                    @Override
                    public void onPartialResponse(String partialResponse) {
                        sink.next(partialResponse == null ? "" : partialResponse);
                    }

                    @Override
                    public void onCompleteResponse(ChatResponse chatResponse) {
                        sink.complete();
                    }

                    @Override
                    public void onError(Throwable error) {
                        sink.error(error);
                    }
                }
        ));
    }

    /**
     * 构建聊天代理
     * @return OJChatAgent 构建好的聊天代理
     */
    private OJChatAgent buildChatAgent() {
        return AiServices.builder(OJChatAgent.class)
                .chatLanguageModel(this.chatModel)
                .streamingChatLanguageModel(this.streamingChatModel)
                .tools(ojTools)
                .contentRetriever(buildRetriever())
                .chatMemoryProvider(memoryId ->
                        dev.langchain4j.memory.chat.MessageWindowChatMemory.builder()
                                .id(memoryId)
                                .maxMessages(20)
                                .chatMemoryStore(chatMemoryStore)
                                .build())
                .build();
    }

    /**
     * 构建问题解析代理
     * @return OJQuestionParseAgent 构建好的问题解析代理
     */
    private OJQuestionParseAgent buildQuestionParseAgent() {
        return AiServices.builder(OJQuestionParseAgent.class)
                .chatLanguageModel(this.chatModel)
                .streamingChatLanguageModel(this.streamingChatModel)
                .contentRetriever(buildRetriever())
                .build();
    }

    /**
     * 构建内容检索器
     * @return EmbeddingStoreContentRetriever 构建好的内容检索器
     */
    private EmbeddingStoreContentRetriever buildRetriever() {
        int topK = Integer.parseInt(aiConfigService.getConfigValue("ai.rag.top_k"));
        double minScore = Double.parseDouble(aiConfigService.getConfigValue("ai.rag.similarity_threshold"));
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(this.embeddingModel)
                .maxResults(topK)
                .minScore(minScore)
                .build();
    }
}
