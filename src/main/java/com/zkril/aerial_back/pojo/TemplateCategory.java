package com.zkril.aerial_back.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 模板分类树表
 * @TableName template_category
 */
@TableName(value ="template_category")
@Data
public class TemplateCategory implements Serializable {
    /**
     * 唯一ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 节点名称
     */
    private String name;

    /**
     * 父节点ID，顶级为NULL
     */
    private Integer parentId;

    /**
     * 节点类型：folder或item
     */
    private Object type;

    /**
     * 图标标识
     */
    private String icon;

    /**
     * 排序字段
     */
    private Integer sortOrder;

    /**
     * 层级深度，可选字段
     */
    private Integer depth;

    /**
     * 模板图片
     */
    private String photo;

    /**
     * 节点名称_2
     */
    private String templateType;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}