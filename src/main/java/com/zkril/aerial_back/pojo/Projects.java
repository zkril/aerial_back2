package com.zkril.aerial_back.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户项目表
 * @TableName projects
 */
@TableName(value ="projects")
@Data
public class Projects implements Serializable {
    /**
     * 项目ID，主键
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 所属用户ID，关联 users 表
     */
    private Integer userId;

    /**
     * 所属文件夹ID，关联 folders 表
     */
    private Integer folderId;

    /**
     * 项目名称
     */
    private String name;

    /**
     * 项目描述
     */
    private String description;

    /**
     * 封面图链接
     */
    private String imageUrl;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 最后更新时间
     */
    private Date updateTime;

    /**
     * 项目类型：私有、公有等
     */
    private String ptype;

    /**
     * 模板类型（如 Ideal_Circular）
     */
    private String templateType;

    /**
     * 关联模板配置表 template_configs.id
     */
    private Integer templateConfigId;

    /**
     * 关联模板配置表 template_parameters.id
     */
    private Integer templateParametersId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}