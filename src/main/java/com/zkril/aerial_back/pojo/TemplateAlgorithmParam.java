package com.zkril.aerial_back.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName template_algorithm_param
 */
@TableName(value ="template_algorithm_param")
@Data
public class TemplateAlgorithmParam implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 算法名称，如 DCNSGA_II_DE_MO
     */
    private String algorithm;

    /**
     * 参数中文名，如 最大代数
     */
    private String label;

    /**
     * 参数键，如 maxGenerations
     */
    private String paramKey;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 值类型，如 int、float、string
     */
    private String valueType;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}