package com.neighborhelp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 积分记录实体
 */
@Data
@TableName("point_records")
public class PointRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 类型（publish_request/complete_service/cancel_request/cancel_order等）
     */
    @TableField("type")
    private String type;

    /**
     * 积分数量（正数为增加，负数为减少）
     */
    @TableField("amount")
    private Integer amount;

    /**
     * 描述
     */
    @TableField("description")
    private String description;

    /**
     * 关联ID
     */
    @TableField("related_id")
    private Long relatedId;

    /**
     * 关联类型（request/order等）
     */
    @TableField("related_type")
    private String relatedType;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}

