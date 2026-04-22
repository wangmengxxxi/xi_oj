package com.XI.xi_oj.ai.tools;

import com.XI.xi_oj.model.dto.judge.JudgeResultDTO;
import com.XI.xi_oj.model.dto.question.WrongQuestionVO;
import com.XI.xi_oj.model.vo.QuestionVO;
import com.XI.xi_oj.service.AiJudgeService;
import com.XI.xi_oj.service.QuestionService;
import com.XI.xi_oj.service.WrongQuestionService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OJTools {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private AiJudgeService judgeService;

    @Autowired
    private WrongQuestionService wrongQuestionService;

    @Tool(
            name = "query_question_info",
            value = "Query question details by id or keyword and return title, content, tags, difficulty, and answer."
    )
    public String queryQuestionInfo(String keyword) {
        QuestionVO question = questionService.getByKeyword(keyword);
        if (question == null) {
            return "Question not found. Please verify id or keyword.";
        }
        return String.format("""
                        Question ID: %d
                        Title: %s
                        Content: %s
                        Tags: %s
                        Difficulty: %s
                        Reference Answer: %s
                        """,
                question.getId(),
                question.getTitle(),
                question.getContent(),
                question.getTags(),
                question.getDifficulty(),
                question.getAnswer()
        );
    }

    @Tool(
            name = "judge_user_code",
            value = "Run judging for user code. Args: questionId, code, language, userId."
    )
    public String judgeUserCode(
            @P("Question id, Long type") Long questionId,
            @P("User code content") String code,
            @P("Language, e.g. java / python / cpp") String language,
            @P("Current user id, Long type") Long userId
    ) {
        JudgeResultDTO result = judgeService.submitCode(questionId, code, language, userId);
        return String.format("""
                        Judge Result: %s
                        Time Used: %sms
                        Memory Used: %sMB
                        Error Message: %s
                        """,
                result.getStatus(),
                result.getTimeUsed(),
                result.getMemoryUsed(),
                result.getErrorMsg()
        );
    }

    @Tool(
            name = "query_user_wrong_question",
            value = "Query one wrong-question record by userId and questionId."
    )
    public String queryUserWrongQuestion(
            @P("User id, Long type") Long userId,
            @P("Question id, Long type") Long questionId
    ) {
        WrongQuestionVO wrongQuestion = wrongQuestionService.getByUserAndQuestion(userId, questionId);
        if (wrongQuestion == null) {
            return "No wrong-question record found.";
        }
        return String.format("""
                        Wrong Code: %s
                        Wrong Judge Result: %s
                        Historical Analysis: %s
                        Review Count: %d
                        """,
                wrongQuestion.getWrongCode(),
                wrongQuestion.getWrongJudgeResult(),
                wrongQuestion.getWrongAnalysis(),
                wrongQuestion.getReviewCount()
        );
    }
}
