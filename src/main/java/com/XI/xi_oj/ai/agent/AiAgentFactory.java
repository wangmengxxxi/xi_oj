package com.XI.xi_oj.ai.agent;

import com.XI.xi_oj.ai.store.AiChatMemoryStore;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import io.milvus.param.MetricType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiAgentFactory {

    @Value("${milvus.host:localhost}")
    private String milvusHost;

    @Value("${milvus.port:19530}")
    private int milvusPort;

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
    public MilvusEmbeddingStore questionEmbeddingStore() {
        return MilvusEmbeddingStore.builder()
                .host(milvusHost)
                .port(milvusPort)
                .collectionName("oj_question")
                .dimension(1024)
                .autoFlushOnInsert(true)
                .metricType(MetricType.COSINE)
                .build();
    }

    @Bean
    public ChatMemoryStore chatMemoryStore(AiChatMemoryStore aiChatMemoryStore) {
        return aiChatMemoryStore;
    }
}
