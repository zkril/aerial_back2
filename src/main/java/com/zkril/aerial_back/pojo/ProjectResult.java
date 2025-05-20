package com.zkril.aerial_back.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName project_result
 */
@TableName(value ="project_result")
@Data
public class ProjectResult implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 
     */
    private Integer projectId;

    /**
     * 
     */
    private Object constraintsXy;

    /**
     * 
     */
    private Object detail;

    /**
     * 
     */
    private Integer efeasible;

    /**
     * 
     */
    private Object extraObjectives;

    /**
     * 
     */
    private Object extrainfo;

    /**
     * 
     */
    private Object genes;

    /**
     * 
     */
    private Integer modifypheno;

    /**
     * 
     */
    private Object nondomlayer;

    /**
     * 
     */
    private Object objectives;

    /**
     * 
     */
    private Integer valid;

    /**
     * 
     */
    private Object pheno;

    /**
     * 
     */
    private Object freq;

    /**
     * 
     */
    private Object phi;

    /**
     * 
     */
    private Object theta;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}