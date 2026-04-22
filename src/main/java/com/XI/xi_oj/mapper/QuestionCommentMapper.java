package com.XI.xi_oj.mapper;

import com.XI.xi_oj.model.entity.QuestionComment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 问题评论数据访问层接口
 * 继承BaseMapper以获得基础的CRUD操作能力
 */
@Mapper
public interface QuestionCommentMapper extends BaseMapper<QuestionComment> {

    /**
     * 根据问题ID查询评论列表
     * 查询条件：问题ID匹配且未被删除
     * 排序规则：按点赞数降序、创建时间升序、ID升序排列
     *
     * @param questionId 问题ID
     * @return 返回该问题的所有有效评论列表
     */
    @Select("SELECT * FROM question_comment " +
            "WHERE question_id = #{questionId} AND is_delete = 0 " +
            "ORDER BY like_num DESC, createTime ASC, id ASC")
    List<QuestionComment> selectByQuestionId(@Param("questionId") Long questionId);

    /**
     * 增加评论的点赞数
     * 使用原子操作确保线程安全
     *
     * @param commentId 评论ID
     * @return 返回更新的行数，成功为1
     */
    @Update("UPDATE question_comment SET like_num = like_num + 1 WHERE id = #{commentId} AND is_delete = 0")
    int incrementLike(@Param("commentId") Long commentId);

    /**
     * 减少评论的点赞数
     * 使用GREATEST函数确保点赞数不会小于0
     *
     * @param commentId 评论ID
     * @return 返回更新的行数，成功为1
     */
    @Update("UPDATE question_comment SET like_num = GREATEST(like_num - 1, 0) WHERE id = #{commentId} AND is_delete = 0")
    int decrementLike(@Param("commentId") Long commentId);
}
