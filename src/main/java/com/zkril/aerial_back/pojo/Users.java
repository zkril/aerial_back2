package com.zkril.aerial_back.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName users
 */
@TableName(value ="users")
@Data
public class Users implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer userId;

    /**
     * 
     */
    private String username;

    /**
     * 
     */
    private String password;

    /**
     * 
     */
    private String email;

    /**
     * 
     */
    private Date regTime;

    /**
     * 
     */
    private Integer status;

    /**
     * 
     */
    private Integer isAdmin;

    /**
     * 
     */
    private String avatar;

    /**
     * 男1,女2，未知3
     */
    private Integer sex;

    /**
     * 
     */
    private String phone;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}