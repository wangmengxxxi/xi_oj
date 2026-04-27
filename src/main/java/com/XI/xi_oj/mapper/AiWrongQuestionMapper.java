package com.XI.xi_oj.mapper;

import com.XI.xi_oj.model.entity.AiWrongQuestion;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AiWrongQuestionMapper extends BaseMapper<AiWrongQuestion> {

    String COLUMN_MAPPING =
            "id, user_id AS userId, question_id AS questionId, wrong_code AS wrongCode, " +
            "language, wrong_judge_result AS wrongJudgeResult, wrong_analysis AS wrongAnalysis, " +
            "review_plan AS reviewPlan, similar_questions AS similarQuestions, " +
            "is_reviewed AS isReviewed, review_count AS reviewCount, " +
            "next_review_time AS nextReviewTime, createTime, updateTime";

    @Select("SELECT " + COLUMN_MAPPING + " FROM ai_wrong_question WHERE user_id = #{userId} " +
            "AND question_id = #{questionId} ORDER BY createTime DESC, id DESC LIMIT 1")
    AiWrongQuestion selectByUserAndQuestion(@Param("userId") Long userId,
                                            @Param("questionId") Long questionId);

    @Select("SELECT " + COLUMN_MAPPING + " FROM ai_wrong_question WHERE id = #{wrongQuestionId} AND user_id = #{userId} LIMIT 1")
    AiWrongQuestion selectByIdAndUser(@Param("wrongQuestionId") Long wrongQuestionId,
                                      @Param("userId") Long userId);

    @Select("SELECT " + COLUMN_MAPPING + " FROM ai_wrong_question WHERE user_id = #{userId} ORDER BY updateTime DESC, id DESC")
    List<AiWrongQuestion> selectListByUser(@Param("userId") Long userId);

    @Select("SELECT " + COLUMN_MAPPING + " FROM ai_wrong_question WHERE user_id = #{userId} " +
            "AND ((next_review_time IS NOT NULL AND next_review_time <= NOW()) " +
            "OR (next_review_time IS NULL AND is_reviewed = 0)) " +
            "ORDER BY COALESCE(next_review_time, createTime) ASC, updateTime DESC, id DESC")
    List<AiWrongQuestion> selectDueReviewList(@Param("userId") Long userId);
}
