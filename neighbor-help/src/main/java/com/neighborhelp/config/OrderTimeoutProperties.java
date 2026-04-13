package com.neighborhelp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 订单超时回滚策略配置
 */
@Data
@ConfigurationProperties(prefix = "neighborhelp.order-timeout")
public class OrderTimeoutProperties {

    /**
     * 已接单但未开始（order.active）超过该阈值将被自动取消（单位：分钟）
     */
    private long acceptUnresponsiveMinutes = 30;

    /**
     * 定时任务的扫描间隔（单位：毫秒），默认 60s
     */
    private long scanIntervalMs = 60000;
}

