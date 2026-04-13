package com.neighborhelp.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neighborhelp.config.OrderTimeoutProperties;
import com.neighborhelp.entity.Order;
import com.neighborhelp.mapper.OrderMapper;
import com.neighborhelp.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单超时回滚：
 * - 接单后未开始（order.status=active）超过阈值 => 取消订单 + 回滚需求状态
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderTimeoutScheduler {

    private final OrderMapper orderMapper;
    private final OrderService orderService;
    private final OrderTimeoutProperties props;

    @Scheduled(fixedDelayString = "${neighborhelp.order-timeout.scan-interval-ms:60000}")
    public void scanAndCancelUnresponsiveOrders() {
        long minutes = props.getAcceptUnresponsiveMinutes();
        if (minutes <= 0) return;

        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(minutes);

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getStatus, "active")
                .isNotNull(Order::getAcceptedAt)
                .lt(Order::getAcceptedAt, cutoff)
                .orderByAsc(Order::getAcceptedAt)
                // 防止一次扫描过多（生产建议根据业务量改成翻页）
                .last("LIMIT 200");

        List<Order> candidates = orderMapper.selectList(wrapper);
        if (candidates == null || candidates.isEmpty()) {
            return;
        }

        for (Order order : candidates) {
            if (order == null || order.getId() == null) continue;
            try {
                orderService.systemCancelOrderByTimeout(order.getId(), "接单超时未响应");
            } catch (Exception e) {
                // 单个订单失败不影响下一批
                log.warn("订单超时回滚失败: orderId={}, err={}", order.getId(), e.getMessage(), e);
            }
        }
    }
}

