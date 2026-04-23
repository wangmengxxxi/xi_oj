package com.XI.xi_oj.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("ai_config")
public class AiConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("config_key")
    private String configKey;
    @TableField("config_value")
    private String configValue;
    private String description;
    @TableField("is_enable")
    private Integer isEnable;
    private Date createTime;
    private Date updateTime;
}