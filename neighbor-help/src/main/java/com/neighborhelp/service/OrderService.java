package com.neighborhelp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.neighborhelp.dto.AcceptOrderDTO;
import com.neighborhelp.dto.UpdateOrderStatusDTO;
import com.neighborhelp.entity.Order;
import com.neighborhelp.vo.OrderVO;
import com.neighborhelp.vo.PageVO;

/**
 * 订单服务接口
 */
public interface OrderService extends IService<Order> {
    /**
     * 接单
     */
    OrderVO acceptOrder(Long userId, AcceptOrderDTO acceptOrderDTO);

    /**
     * 获取订单详情（viewer 须为接单者或需求发布者）
     */
    OrderVO getOrderDetail(Long orderId, Long viewerUserId);

    /**
     * 获取用户接收的订单
     */
    PageVO<OrderVO> getUserOrders(Long userId, String status, Integer page, Integer limit);

    /**
     * 开始服务
     */
    void startService(Long orderId, Long operatorUserId, UpdateOrderStatusDTO updateOrderStatusDTO);

    /**
     * 更新服务进度
     */
    void updateProgress(Long orderId, Long operatorUserId, UpdateOrderStatusDTO updateOrderStatusDTO);

    /**
     * 完成服务
     */
    void completeService(Long orderId, Long operatorUserId, UpdateOrderStatusDTO updateOrderStatusDTO);

    /**
     * 取消订单
     */
    void cancelOrder(Long orderId, Long operatorUserId, UpdateOrderStatusDTO updateOrderStatusDTO);

    /**
     * 实体转VO
     */
    OrderVO toVO(Order order);

    /**
     * 系统定时任务使用：接单超时未响应自动回滚
     */
    void systemCancelOrderByTimeout(Long orderId, String reason);
}

