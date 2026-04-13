package com.neighborhelp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单实体
 */
@Data
@TableName("orders")
public class Order implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 需求ID
     */
    @TableField("request_id")
    private Long requestId;

    /**
     * 接单用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 状态（active/started/completed/cancelled）
     */
    @TableField("status")
    private String status;

    /**
     * 进度（0-100）
     */
    @TableField("progress")
    private Integer progress;

    /**
     * 接单消息
     */
    @TableField("accept_message")
    private String acceptMessage;

    /**
     * 接单时间
     */
    @TableField("accepted_at")
    private LocalDateTime acceptedAt;

    /**
     * 开始时间
     */
    @TableField("started_at")
    private LocalDateTime startedAt;

    /**
     * 完成时间
     */
    @TableField("completed_at")
    private LocalDateTime completedAt;

    /**
     * 取消时间
     */
    @TableField("cancelled_at")
    private LocalDateTime cancelledAt;

    /**
     * 取消原因
     */
    @TableField("cancel_reason")
    private String cancelReason;

    /**
     * 完成消息
     */
    @TableField("complete_message")
    private String completeMessage;

    /**
     * 完成图片（JSON数组）
     */
    @TableField("complete_images")
    private String completeImages;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

