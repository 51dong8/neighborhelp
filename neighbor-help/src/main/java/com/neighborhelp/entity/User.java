package com.neighborhelp.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@TableName("users")
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 密码（加密后）
     */
    @TableField("password_hash")
    private String passwordHash;

    /**
     * 头像URL
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 等级
     */
    @TableField("level")
    private Integer level;

    /**
     * 积分
     */
    @TableField("points")
    private Integer points;

    /**
     * 帮助次数
     */
    @TableField("help_count")
    private Integer helpCount;

    /**
     * 评分
     */
    @TableField("rating")
    private BigDecimal rating;

    /**
     * 加入天数
     */
    @TableField("join_days")
    private Integer joinDays;

    /**
     * 手机号
     */
    @TableField("phone")
    private String phone;

    /**
     * 微信
     */
    @TableField("wechat")
    private String wechat;

    /**
     * 地址
     */
    @TableField("address")
    private String address;

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
    /**
     * 认证状态: 0未认证, 1审核中, 2已认证
     */
    private Integer authStatus;
    private String authMaterial;
}

