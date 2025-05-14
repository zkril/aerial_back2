package com.zkril.aerial_back.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName template_optimize_variables
 */
@TableName(value ="template_optimize_variables")
@Data
public class TemplateOptimizeVariables implements Serializable {
    /**
     * 主键，自增ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联模板配置ID，外键，指向template_configs(id)
     */
    private Long templateConfigId;

    /**
     * 变量名称，示例：圆阵半径 r1(mm)、阵元角度 point_phe1(°)
     */
    private String name;

    /**
     * 变量允许的最小值，字符串格式，前端定义的 min 字段
     */
    private String minValue;

    /**
     * 变量允许的最大值，字符串格式，前端定义的 max 字段
     */
    private String maxValue;

    /**
     * 变量默认值，字符串格式，对应 default 字段（注意：default 为保留字，字段命名为 default_value）
     */
    private String defaultValue;

    /**
     * 是否参与优化，1 表示参与，0 表示不参与，对应前端 optimize 字段
     */
    private Integer optimize1;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}