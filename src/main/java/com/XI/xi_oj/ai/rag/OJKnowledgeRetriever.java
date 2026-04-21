package com.XI.xi_oj.ai.rag;

import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import com.XI.xi_oj.utils.TimeUtil;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class OJKnowledgeRetriever {

    @Resource
    private MilvusEmbeddingStore embeddingStore;

    @Resource
    private QwenEmbeddingModel EmbeddingModel;

    @Resource
    private StringRedisTemplate redisTemplate;

    /** 缓存前缀，与 ai:config: 命名空间隔离 */
    private static final String RAG_CACHE_PREFIX = "ai:rag:cache:";
    /** 缓存 TTL：1小时，知识库日常不变，同一题目多用户复用 */
    private static final long RAG_CACHE_TTL_MINUTES = 60;

    /**
     * 核心检索方法
     * @param query 用户问题/题目关键词
     * @param topK 返回条数
     * @param minScore 最小相似度阈值
     * @return 检索到的上下文内容
     */
    // ── 核心检索（带缓存） ───────────────────────────────────────
    public String retrieve(String query, int topK, double minScore) {
        //先获取缓存Key
        String cacheKey = RAG_CACHE_PREFIX + DigestUtils.md5DigestAsHex(
                (query + "|" + topK + "|" + minScore).getBytes(StandardCharsets.UTF_8));
        String cached = redisTemplate.opsForValue().get(cacheKey);
        //如果获取缓存就返回缓存结果
        if (cached != null) {
            log.debug("[RAG Cache] HIT key={}", cacheKey);
            return cached;
        }
        //未获取缓存就执行查询，然后存入数据库
        String result=doRetrieve(query,topK,minScore);
        redisTemplate.opsForValue().set(cacheKey, result, TimeUtil.minutes(RAG_CACHE_TTL_MINUTES));
        return result;
    }
    public String doRetrieve(String query, int topK, double minScore){
        // 1. 生成问题向量
        Embedding queryEmbedding = EmbeddingModel.embed(query).content();

        // 2. 构建向量库检索类
        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(topK)
                .minScore(minScore)
                .build();

        EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);
        List<EmbeddingMatch<TextSegment>> matches = searchResult.matches();

        // 3. 相似度过滤和内容拼接
        String context=matches.stream()
                .filter(match -> match.score() >= minScore)
                .map(EmbeddingMatch::embedded)
                .map(TextSegment::text)
                .collect(Collectors.joining("\n\n"));

        // 4.兜底返回
        return context.isBlank()?"无相关知识点":context;
    }


    /**
     * 相似题检索方法
     * @param questionId 题目id
     * @param questionContent 题目内容
     * @return 相似题目id列表
     */
    public List<Long> retrieveSimilarQuestions(Long questionId, String questionContent) {
        // 1. 构建缓存Key
        String cacheKey = RAG_CACHE_PREFIX + "similar:" + DigestUtils.md5DigestAsHex(
                (questionId + "|" + questionContent).getBytes(StandardCharsets.UTF_8));

        // 2. 查缓存
        String cached = redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            log.debug("[RAG Cache] HIT key={}", cacheKey);
            if (cached.isBlank()) return Collections.emptyList(); // 空列表的情况
            return Arrays.stream(cached.split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
        }

       // 3. 未命中，执行查询
        List<Long> result = doretrieveSimilarQuestions(questionId, questionContent);

        // 4. 结果存缓存（List<Long> 序列化为逗号分隔字符串）
        String toCache = result.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        redisTemplate.opsForValue().set(cacheKey, toCache, TimeUtil.minutes(RAG_CACHE_TTL_MINUTES));

        return result;
    }


    public List<Long> doretrieveSimilarQuestions(Long questionId, String questionContent) {
        // 3. 未命中，执行查询
        Embedding queryEmbedding = EmbeddingModel.embed(questionContent).content();
        EmbeddingSearchRequest embeddingSearchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(4)
                .minScore(0.75)
                .build();
        List<Long> result = embeddingStore.search(embeddingSearchRequest)
                .matches()
                .stream()
                .filter(match -> match.score() >= 0.75)
                .map(EmbeddingMatch::embedded)
                .map(segment -> segment.metadata().getLong("question_id"))
                .filter(id -> !id.equals(questionId))
                .collect(Collectors.toList());


        return result;
    }
    /**
     * 按 content_type 过滤的检索方法
     * 供 5.2 代码分析、5.5 错题分析等无状态模块手动调用，精准控制检索范围
     * @param query       检索关键词（题目标题+考点拼接）
     * @param contentTypes 内容类型过滤，逗号分隔（如 "代码模板,错题分析"）
     * @param topK        返回条数
     * @param minScore    最小相似度阈值
     * @return 过滤后的上下文内容
     */
    public String retrieveByType(String query, String contentTypes, int topK, double minScore) {
        // 1. 构建缓存Key
        String cacheKey = RAG_CACHE_PREFIX + "type:" + DigestUtils.md5DigestAsHex(
                (query + "|" + contentTypes + "|" + topK + "|" + minScore).getBytes(StandardCharsets.UTF_8));

        // 2. 查缓存
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("[RAG Cache] HIT key={}", cacheKey);
            return cached;
        }

        // 3. 未命中，执行查询
        List<String> typeList = Arrays.asList(contentTypes.split(","));
        List<EmbeddingMatch<TextSegment>>  matches = doretrieveByType(query, contentTypes, topK, minScore);
        String result = matches.stream()
                .filter(match -> match.score() >= minScore)
                .filter(match -> typeList.contains(
                        match.embedded().metadata().getString("content_type")))
                .limit(topK)
                .map(EmbeddingMatch::embedded)
                .map(TextSegment::text)
                .collect(Collectors.joining("\n\n"));
        result = result.isBlank() ? "无相关知识点" : result;

        // 4. 存缓存
        redisTemplate.opsForValue().set(cacheKey, result, TimeUtil.minutes(RAG_CACHE_TTL_MINUTES));

        return result;
    }

    public List<EmbeddingMatch<TextSegment>> doretrieveByType(String query, String contentTypes, int topK, double minScore){
        Embedding queryEmbedding = EmbeddingModel.embed(query).content();

        List<EmbeddingMatch<TextSegment>> matches = embeddingStore.search(
                EmbeddingSearchRequest.builder()
                        .queryEmbedding(queryEmbedding)
                        .maxResults(topK * 2)
                        .minScore(minScore)
                        .build()
        ).matches();
        return matches;
    }

    /**
     * 手动刷新 RAG 缓存（导入新知识库数据后调用）
     * 可在 Admin 接口或知识库导入完成事件中触发
     */
    public void clearRagCache() {
        Set<String> keys = redisTemplate.keys(RAG_CACHE_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("[RAG Cache] 已清除 {} 条 RAG 缓存", keys.size());
        }
    }

}
