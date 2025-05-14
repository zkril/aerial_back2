package com.zkril.aerial_back.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName template_objective_direction_target
 */
@TableName(value ="template_objective_direction_target")
@Data
public class TemplateObjectiveDirectionTarget implements Serializable {
    /**
     * 主键，自增 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 外键，关联 template_configs.id
     */
    private Long templateConfigId;

    /**
     * 目标类型，如：侧向误差、第二相关峰
     */
    private String target;

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
     * 优化类型，如 min、max
     */
    private String optimizeType;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}