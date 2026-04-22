package com.XI.xi_oj.mapper;

import com.XI.xi_oj.model.entity.AiWrongQuestion;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AiWrongQuestionMapper extends BaseMapper<AiWrongQuestion> {
    /**
     * 按用户ID + 题目ID 精确查询最近一条错题记录
     */
    @Select("SELECT * FROM ai_wrong_question WHERE user_id = #{userId} " +
            "AND question_id = #{questionId} ORDER BY createTime DESC, id DESC LIMIT 1")
    AiWrongQuestion selectByUserAndQuestion(@Param("userId") Long userId,
                                            @Param("questionId") Long questionId);

    @Select("SELECT * FROM ai_wrong_question WHERE id = #{wrongQuestionId} AND user_id = #{userId} LIMIT 1")
    AiWrongQuestion selectByIdAndUser(@Param("wrongQuestionId") Long wrongQuestionId,
                                      @Param("userId") Long userId);

    @Select("SELECT * FROM ai_wrong_question WHERE user_id = #{userId} ORDER BY updateTime DESC, id DESC")
    List<AiWrongQuestion> selectListByUser(@Param("userId") Long userId);
}
