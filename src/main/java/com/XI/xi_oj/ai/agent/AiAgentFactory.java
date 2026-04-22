package com.XI.xi_oj.ai.agent;

import com.XI.xi_oj.ai.store.AiChatMemoryStore;
import com.XI.xi_oj.ai.tools.OJTools;
import com.XI.xi_oj.service.AiConfigService;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
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

@Configuration
public class AiAgentFactory {

    @Resource
    private OJTools ojTools;

    @Resource
    private AiConfigService aiConfigService;

    @Value("${ai.model.api-key}")
    private String apiKey;

    @Value("${milvus.host:localhost}")
    private String milvusHost;

    @Value("${milvus.port:19530}")
    private int milvusPort;

    /**
     * LangChain4j 1.0.0-beta3 下可用方法：
     * - autoFlushOnInsert(Boolean) 存在
     * - autoCreateCollection(Boolean) 不存在
     */
    @Bean
    public MilvusEmbeddingStore embeddingStore() {
        return MilvusEmbeddingStore.builder()
                .host(milvusHost)
                .port(milvusPort)
                .collectionName("oj_knowledge")
                .dimension(1024)
                .autoFlushOnInsert(true)
                .metricType(MetricType.COSINE)
                .build();
    }

    @Bean
    public ChatLanguageModel chatModel() {
        return QwenChatModel.builder()
                .apiKey(apiKey)
                .modelName(aiConfigService.getConfigValue("ai.model.name"))
                .temperature(0.2f)
                .maxTokens(2048)
                .build();
    }

    @Bean
    public StreamingChatLanguageModel streamingChatModel() {
        return QwenStreamingChatModel.builder()
                .apiKey(apiKey)
                .modelName(aiConfigService.getConfigValue("ai.model.name"))
                .temperature(0.2f)
                .maxTokens(2048)
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        return QwenEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName(aiConfigService.getConfigValue("ai.model.embedding_name"))
                .build();
    }

    @Bean
    public ChatMemoryStore chatMemoryStore(AiChatMemoryStore aiChatMemoryStore) {
        return aiChatMemoryStore;
    }

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
                .chatMemoryProvider(memoryId ->
                        dev.langchain4j.memory.chat.MessageWindowChatMemory.builder()
                                .id(memoryId)
                                .maxMessages(20)
                                .chatMemoryStore(chatMemoryStore)
                                .build())
                .build();
    }

    @Bean
    public OJStreamingService ojStreamingService(StreamingChatLanguageModel streamingChatModel) {
        return AiServices.builder(OJStreamingService.class)
                .streamingChatLanguageModel(streamingChatModel)
                .build();
    }

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
