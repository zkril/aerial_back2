package com.zkril.aerial_back.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName template_constraint_direction
 */
@TableName(value ="template_constraint_direction")
@Data
public class TemplateConstraintDirection implements Serializable {
    /**
     * 主键，自增 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联 template_configs.id 的外键
     */
    private Long templateConfigId;

    /**
     * 约束表达式/目标类型，如“侧向误差”
     */
    private String expression;

    /**
     * θ 起始角度 (°)
     */
    private Double thetaStart;

    /**
     * θ 终止角度 (°)
     */
    private Double thetaEnd;

    /**
     * φ 起始角度 (°)
     */
    private Double phiStart;

    /**
     * φ 终止角度 (°)
     */
    private Double phiEnd;

    /**
     * 约束符号，如 <=、>= 等
     */
    private String status;

    /**
     * 约束目标值，对应 Directionobj
     */
    private Double limitValue;

    /**
     * 误差值，对应 delta_Direction
     */
    private Double errorValue;

    /**
     * 权重值，对应 weight_Direction
     */
    private Double weightValue;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}