package com.zkril.aerial_back.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @TableName products
 */
@TableName(value ="products")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Products implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer productId;

    /**
     * 
     */
    private String name;

    /**
     * 
     */
    private String intro;

    /**
     * 
     */
    private String buylink;

    /**
     * 
     */
    private String photo;

    /**
     * 
     */
    private String data1Name;

    /**
     * 
     */
    private String data1Min;

    /**
     * 
     */
    private String data1Normal;

    /**
     * 
     */
    private String data1Max;

    /**
     * 
     */
    private String data1Unit;

    /**
     * 
     */
    private String data2Name;

    /**
     * 
     */
    private String data2Text;

    /**
     * 
     */
    private String homePhoto;

    /**
     * 
     */
    private String descp;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}