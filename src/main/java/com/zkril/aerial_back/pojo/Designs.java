package com.zkril.aerial_back.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName designs
 */
@TableName(value ="designs")
@Data
public class Designs implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer designId;

    /**
     * 
     */
    private String name;

    /**
     * 
     */
    private String shape;

    /**
     * 
     */
    private String intro;

    /**
     * 
     */
    private String link;

    /**
     * 
     */
    private String photo;

    /**
     * 
     */
    private String model3d;

    /**
     * 
     */
    private String enhance3d;

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