package com.XI.xi_oj.mapper;

import com.XI.xi_oj.model.entity.AiWrongQuestion;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AiWrongQuestionMapper extends BaseMapper<AiWrongQuestion> {
    /**
     * 按用户ID + 题目ID 精确查询最近一条错题记录
     */
    @Select("SELECT * FROM ai_wrong_question WHERE user_id = #{userId} " +
            "AND question_id = #{questionId} ORDER BY create_time DESC LIMIT 1")
    AiWrongQuestion selectByUserAndQuestion(@Param("userId") Long userId,
                                            @Param("questionId") Long questionId);
}