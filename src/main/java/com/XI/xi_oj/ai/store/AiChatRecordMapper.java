package com.XI.xi_oj.ai.store;

import com.XI.xi_oj.ai.model.AiChatRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AiChatRecordMapper extends BaseMapper<AiChatRecord> {

    /**
     * Redis miss 时回源：按会话读取最近 N 轮（倒序）
     */
    @Select("SELECT * FROM ai_chat_record " +
            "WHERE chat_id = #{chatId} " +
            "ORDER BY createTime DESC, id DESC " +
            "LIMIT #{rounds}")
    List<AiChatRecord> selectLatestByChatId(@Param("chatId") String chatId,
                                            @Param("rounds") Integer rounds);

    @Select("SELECT * FROM ai_chat_record " +
            "WHERE user_id = #{userId} AND chat_id = #{chatId} " +
            "ORDER BY createTime DESC, id DESC " +
            "LIMIT #{rounds}")
    List<AiChatRecord> selectLatestByUserAndChat(@Param("userId") Long userId,
                                                 @Param("chatId") String chatId,
                                                 @Param("rounds") Integer rounds);

    @Select("SELECT * FROM ai_chat_record " +
            "WHERE user_id = #{userId} AND chat_id = #{chatId} " +
            "ORDER BY createTime ASC, id ASC")
    List<AiChatRecord> selectByUserAndChat(@Param("userId") Long userId,
                                           @Param("chatId") String chatId);

    /**
     * 历史记录游标分页（避免 LIMIT + OFFSET 重复/漏读）
     */
    @Select("<script>" +
            "SELECT * FROM ai_chat_record " +
            "WHERE user_id = #{userId} " +
            "  AND chat_id = #{chatId} " +
            "  <if test='cursorTime != null and cursorId != null'>" +
            "    AND (createTime &lt; #{cursorTime} OR (createTime = #{cursorTime} AND id &lt; #{cursorId})) " +
            "  </if>" +
            "ORDER BY createTime DESC, id DESC " +
            "LIMIT #{pageSize}" +
            "</script>")
    List<AiChatRecord> selectHistoryByCursor(@Param("userId") Long userId,
                                             @Param("chatId") String chatId,
                                             @Param("cursorTime") LocalDateTime cursorTime,
                                             @Param("cursorId") Long cursorId,
                                             @Param("pageSize") Integer pageSize);

    @Delete("DELETE FROM ai_chat_record WHERE user_id = #{userId} AND chat_id = #{chatId}")
    int deleteByUserAndChat(@Param("userId") Long userId, @Param("chatId") String chatId);
}
