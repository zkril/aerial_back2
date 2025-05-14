package com.zkril.aerial_back.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @TableName users
 */
@TableName(value ="users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Users implements Serializable {
    @TableId(value = "user_id", type = IdType.AUTO)
    private Integer userId;

    private String username;

    private String password;

    private String email;

    private Date regTime;

    private Integer status;

    private Integer isAdmin;

    private String avatar;

    private static final long serialVersionUID = 1L;
}