package com.neighborhelp.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应码枚举
 */
@Getter
@AllArgsConstructor
public enum ResultCode {
    /**
     * 成功
     */
    SUCCESS(200, "操作成功"),

    /**
     * 失败
     */
    ERROR(500, "操作失败"),

    /**
     * 参数错误
     */
    PARAM_ERROR(400, "参数错误"),

    /**
     * 参数校验失败
     */
    VALIDATION_ERROR(422, "参数校验失败"),

    /**
     * 未授权
     */
    UNAUTHORIZED(401, "未授权，请先登录"),

    /**
     * 无权限
     */
    FORBIDDEN(403, "无权限访问"),

    /**
     * 资源不存在
     */
    NOT_FOUND(404, "资源不存在"),

    /**
     * 用户不存在
     */
    USER_NOT_FOUND(1001, "用户不存在"),

    /**
     * 用户名已存在
     */
    USERNAME_EXISTS(1002, "用户名已存在"),

    /**
     * 邮箱已存在
     */
    EMAIL_EXISTS(1003, "邮箱已存在"),

    /**
     * 密码错误
     */
    PASSWORD_ERROR(1004, "密码错误"),

    /**
     * Token无效
     */
    TOKEN_INVALID(1005, "Token无效或已过期"),

    /**
     * Token过期
     */
    TOKEN_EXPIRED(1006, "Token已过期"),

    /**
     * Token已失效（主动登出或轮换）
     */
    TOKEN_REVOKED(1007, "Token已失效，请重新登录"),

    /**
     * 需求不存在
     */
    REQUEST_NOT_FOUND(2001, "需求不存在"),

    /**
     * 需求状态错误
     */
    REQUEST_STATUS_ERROR(2002, "需求状态错误"),

    /**
     * 订单不存在
     */
    ORDER_NOT_FOUND(3001, "订单不存在"),

    /**
     * 订单状态错误
     */
    ORDER_STATUS_ERROR(3002, "订单状态错误"),

    /**
     * 积分不足
     */
    POINTS_INSUFFICIENT(4001, "积分不足"),

    /**
     * 无权操作该资源
     */
    RESOURCE_FORBIDDEN(4003, "无权操作该资源");

    /**
     * 响应码
     */
    private final Integer code;

    /**
     * 响应消息
     */
    private final String message;
}

