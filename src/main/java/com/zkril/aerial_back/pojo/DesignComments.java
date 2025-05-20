package com.zkril.aerial_back.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName design_comments
 */
@TableName(value ="design_comments")
@Data
public class DesignComments implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 评论用户ID
     */
    private Integer userId;

    /**
     * 评论目标
     */
    private Integer designId;

    /**
     * 评论内容
     */
    private String intro;

    /**
     * 评论图片URL, 多张以逗号分隔
     */
    private String photo;

    /**
     * 点赞数量
     */
    private Integer likeNumber;

    /**
     * 
     */
    private LocalDateTime createdTime;

    /**
     * 0未读 1已读
     */
    private Integer isread;

    /**
     * 0未通知 1已通知
     */
    private Integer notified;

    /**
     * 如果是回复，指向回复的目标评论ID
     */
    private Integer replyTo;

    /**
     * 点赞过的用户ID列表, 逗号分隔
     */
    private String likedUserIds;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}