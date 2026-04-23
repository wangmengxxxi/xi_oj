package com.XI.xi_oj.mapper;

import com.XI.xi_oj.model.entity.AiConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AiConfigMapper extends BaseMapper<AiConfig> {
    @Select("SELECT id, config_key AS configKey, config_value AS configValue, description, is_enable AS isEnable, createTime, updateTime FROM ai_config WHERE config_key = #{configKey} LIMIT 1")
    AiConfig selectByConfigKey(@Param("configKey") String configKey);
    @Update("UPDATE ai_config SET config_value = #{configValue}, updateTime = NOW() " +
            "WHERE config_key = #{configKey}")
    int updateValueByKey(@Param("configKey") String configKey,
                         @Param("configValue") String configValue);
}