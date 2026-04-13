package com.neighborhelp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 需求实体
 */
@Data
@TableName("requests")
public class Request implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 需求ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 发布用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 服务类型
     */
    @TableField("service_type")
    private String serviceType;

    /**
     * 标题
     */
    @TableField("title")
    private String title;

    /**
     * 描述
     */
    @TableField("description")
    private String description;

    /**
     * 报酬类型（points/money）
     */
    @TableField("reward_type")
    private String rewardType;

    /**
     * 报酬金额
     */
    @TableField("reward_amount")
    private Integer rewardAmount;

    /**
     * 期望时间
     */
    @TableField("expected_time")
    private LocalDateTime expectedTime;

    /**
     * 地点
     */
    @TableField("location")
    private String location;

    /**
     * 紧急程度（normal/urgent）
     */
    @TableField("urgency")
    private String urgency;

    /**
     * 状态：active 待接单；accepted 已接单；completed 已完成；cancelled 已取消
     */
    @TableField("status")
    private String status;

    /**
     * 服务详情（JSON格式）
     */
    @TableField("service_details")
    private String serviceDetails;

    /**
     * 接单用户ID
     */
    @TableField("accepted_by")
    private Long acceptedBy;

    /**
     * 接单时间
     */
    @TableField("accepted_at")
    private LocalDateTime acceptedAt;

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

    /**
     * 社区id
     */
    private Long communityId;
}

