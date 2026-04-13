package com.neighborhelp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知实体
 */
@Data
@TableName("notifications")
public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 通知ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 类型（order_accepted/order_completed/request_accepted等）
     */
    @TableField("type")
    private String type;

    /**
     * 标题
     */
    @TableField("title")
    private String title;

    /**
     * 内容
     */
    @TableField("content")
    private String content;

    /**
     * 是否已读
     */
    @TableField("is_read")
    private Boolean isRead;

    /**
     * 关联ID
     */
    @TableField("related_id")
    private Long relatedId;

    /**
     * 关联类型（order/request等）
     */
    @TableField("related_type")
    private String relatedType;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}

