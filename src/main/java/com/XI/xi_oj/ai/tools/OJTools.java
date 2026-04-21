package com.XI.xi_oj.ai.tools;

import com.XI.xi_oj.judge.JudgeService;
import com.XI.xi_oj.model.dto.judge.JudgeResultDTO;
import com.XI.xi_oj.model.vo.QuestionVO;
import com.XI.xi_oj.service.AiJudgeService;
import com.XI.xi_oj.service.QuestionService;
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
            value = "查询OJ题目的详细信息，入参为题目ID或题目关键词，返回题干、考点、难度、标准答案"
    )
    public String queryQuestionInfo(String keyword) {
        QuestionVO question = questionService.getByKeyword(keyword);
        if (question == null) {
            return "未找到对应题目，请确认题目ID/关键词是否正确";
        }
        return String.format("""
                        题目ID：%d
                        标题：%s
                        题干：%s
                        考点：%s
                        难度：%s
                        标准答案：%s
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
            value = "评测用户提交的代码，返回判题结果与错误信息。参数说明：questionId=题目ID（Long）；code=代码内容（String）；language=代码语言，如 java/python/cpp（String）"
    )
    public String judgeUserCode(
            @P("题目ID，Long类型") Long questionId,
            @P("用户代码内容，完整字符串") String code,
            @P("代码语言，如 java / python / cpp") String language,
            @P("当前登录用户ID，Long类型") Long userId
    )
    {
        JudgeResultDTO result = judgeService.submitCode(questionId, code, language,userId);
        return String.format("""
                        判题结果：%s
                        执行用时：%sms
                        内存占用：%sMB
                        错误信息：%s
                        """,
                result.getStatus(),
                result.getTimeUsed(),
                result.getMemoryUsed(),
                result.getErrorMsg()
        );
    }

    @Tool(
            name = "query_user_wrong_question",
            value = "查询用户的错题信息，返回错误代码、判题结果、历史分析。参数说明：userId=用户ID（Long）；questionId=题目ID（Long）"
    )
    public String queryUserWrongQuestion(
            @P("用户ID，Long类型") Long userId,
            @P("题目ID，Long类型") Long questionId
    ) {
        WrongQuestionVO wrongQuestion = wrongQuestionService.getByUserAndQuestion(userId, questionId);
        if (wrongQuestion == null) {
            return "未找到对应错题记录";
        }
        return String.format("""
                        错误代码：%s
                        错误判题结果：%s
                        历史错误分析：%s
                        复习次数：%d
                        """,
                wrongQuestion.getWrongCode(),
                wrongQuestion.getWrongJudgeResult(),
                wrongQuestion.getWrongAnalysis(),
                wrongQuestion.getReviewCount()
        );
    }
}
