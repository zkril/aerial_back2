package com.zkril.aerial_back.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName template_parameters
 */
@TableName(value ="template_parameters")
@Data
public class TemplateParameters implements Serializable {
    /**
     * 主键，自增ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 对应的项目ID，外键关联 projects(id)
     */
    private Integer projectId;

    /**
     * 优化算法名称，例如 DCNSGA_II_DE_MO
     */
    private String algorithmName;

    /**
     * 每次适应度评估的超时时间（毫秒）
     */
    private Integer evaluateTimeout;

    /**
     * 问题名称，例如 Ideal_circular
     */
    private String problemName;

    /**
     * 并行线程数
     */
    private Integer threadnum;

    /**
     * 断点续算
     */
    private Integer continuelastbreakpoint;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}