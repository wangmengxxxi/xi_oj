package com.XI.xi_oj.job.cycle;

import com.XI.xi_oj.ai.rag.QuestionVectorSyncService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class QuestionVectorSyncJob {

    @Resource
    private QuestionVectorSyncService questionVectorSyncService;

    @Scheduled(cron = "0 0 2 * * ?")
    public void syncQuestionsToMilvus() {
        int successCount = questionVectorSyncService.rebuildQuestionVectors();
        log.info("[Question Vector] scheduled sync completed, successCount={}", successCount);
    }
}
