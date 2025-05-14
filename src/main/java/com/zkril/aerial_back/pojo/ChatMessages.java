package com.zkril.aerial_back.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @TableName chat_messages
 */
@TableName(value ="chat_messages")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessages implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 
     */
    private Integer fromUserId;

    /**
     * 
     */
    private Integer toUserId;

    /**
     * 
     */
    private String content;

    /**
     * 
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * 
     */
    private Integer isRead;

    /**
     * 
     */
    private String title;

    private String type;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}