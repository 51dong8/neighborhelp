package com.neighborhelp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neighborhelp.common.ResultCode;
import com.neighborhelp.common.exception.BusinessException;
import com.neighborhelp.dto.AcceptOrderDTO;
import com.neighborhelp.dto.UpdateOrderStatusDTO;
import com.neighborhelp.entity.Order;
import com.neighborhelp.entity.Request;
import com.neighborhelp.entity.User;
import com.neighborhelp.mapper.OrderMapper;
import com.neighborhelp.service.NotificationService;
import com.neighborhelp.service.OrderService;
import com.neighborhelp.service.RequestService;
import com.neighborhelp.service.UserService;
import com.neighborhelp.vo.OrderVO;
import com.neighborhelp.vo.PageVO;
import com.neighborhelp.vo.RequestVO;
import com.neighborhelp.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单服务实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final RequestService requestService;
    private final UserService userService;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO acceptOrder(Long userId, AcceptOrderDTO acceptOrderDTO) {
        // 1. 基础安全监测：获取接单用户信息
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 必须完成本小区的身份认证
        if (user.getAuthStatus() == null || user.getAuthStatus() != 2) {
            throw new BusinessException(ResultCode.FORBIDDEN, "为了保障邻里安全，接单前请先完成本小区的身份认证");
        }

        // 2. 获取需求详情
        Request request = requestService.getById(acceptOrderDTO.getRequestId());
        if (request == null) {
            throw new BusinessException(ResultCode.REQUEST_NOT_FOUND);
        }

        // ================= 核心新增：社区与距离校验逻辑 =================

        // 逻辑 A：校验是否为同一社区
        boolean isSameCommunity = user.getCommunityId() != null &&
                user.getCommunityId().equals(request.getCommunityId());

        // 逻辑 B：校验是否在方圆 3km 以内
        boolean isWithinDistance = false;
        // 尝试从 Redis GEO 中获取接单者与需求点之间的距离
        // 注意：这要求接单者的位置也已存入 Redis，此处演示直接计算逻辑
        try {
            // 使用 Redis 的 geoDist 命令计算成员间的距离
            org.springframework.data.geo.Distance dist = stringRedisTemplate.opsForGeo()
                    .distance("neighbor:request:geo", userId.toString(), request.getId().toString(),
                            org.springframework.data.redis.connection.RedisGeoCommands.DistanceUnit.KILOMETERS);

            if (dist != null && dist.getValue() <= 3.0) {
                isWithinDistance = true;
            }
        } catch (Exception e) {
            // 如果 Redis 中没有接单者的位置信息，可以降级处理或通过前端传参校验
            log.warn("无法通过 Redis 计算距离，仅执行社区匹配校验: {}", e.getMessage());
        }

        // 最终判定：既不是同一社区，也不在 3km 范围内，则拒绝接单
        if (!isSameCommunity && !isWithinDistance) {
            throw new BusinessException(ResultCode.FORBIDDEN, "该需求距离较远且不属于您所在的社区，无法接单");
        }

        // ============================================================

        // 3. 检查需求状态：须为待接单
        if (!"active".equals(request.getStatus()) || request.getAcceptedBy() != null) {
            throw new BusinessException("该需求已被接单或已取消");
        }

        // 4. 不能接自己的需求
        if (request.getUserId().equals(userId)) {
            throw new BusinessException("不能接自己的需求");
        }

        // 5. 检查是否已经存在活跃订单
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getRequestId, acceptOrderDTO.getRequestId())
                .eq(Order::getStatus, "active");
        if (count(wrapper) > 0) {
            throw new BusinessException("该需求已被接单");
        }

        // 6. 创建订单
        Order order = new Order();
        order.setRequestId(acceptOrderDTO.getRequestId());
        order.setUserId(userId);
        order.setStatus("active");
        order.setProgress(0);
        order.setAcceptMessage(acceptOrderDTO.getMessage());
        order.setAcceptedAt(LocalDateTime.now());
        save(order);

        // 7. 更新需求状态
        request.setAcceptedBy(userId);
        request.setAcceptedAt(LocalDateTime.now());
        request.setStatus("accepted");
        requestService.updateById(request);

        // 8. 发送通知
        notificationService.sendNotification(
                request.getUserId(),
                "order_accepted",
                "您的需求已被接单",
                "您发布的需求「" + request.getTitle() + "」已被接单",
                order.getId(),
                "order"
        );

        return toVO(order);
    }
    @Override
    public OrderVO getOrderDetail(Long orderId, Long viewerUserId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        Request request = requestService.getById(order.getRequestId());
        boolean asHelper = viewerUserId != null && viewerUserId.equals(order.getUserId());
        boolean asPublisher = request != null && viewerUserId != null && viewerUserId.equals(request.getUserId());
        if (!asHelper && !asPublisher) {
            throw new BusinessException(ResultCode.RESOURCE_FORBIDDEN);
        }
        return toVO(order);
    }

    @Override
    public PageVO<OrderVO> getUserOrders(Long userId, String status, Integer page, Integer limit) {
        Page<Order> pageParam = new Page<>(page, limit);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);

        if (status != null && !status.isEmpty()) {
            wrapper.eq(Order::getStatus, status);
        }

        wrapper.orderByDesc(Order::getCreatedAt);

        IPage<Order> pageResult = page(pageParam, wrapper);
        List<OrderVO> voList = pageResult.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        return new PageVO<>(voList, pageResult.getTotal(), page, limit);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startService(Long orderId, Long operatorUserId, UpdateOrderStatusDTO updateOrderStatusDTO) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        if (operatorUserId == null || !operatorUserId.equals(order.getUserId())) {
            throw new BusinessException(ResultCode.RESOURCE_FORBIDDEN);
        }

        if (!"active".equals(order.getStatus())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }

        order.setStatus("started");
        order.setStartedAt(LocalDateTime.now());
        order.setProgress(0);
        updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProgress(Long orderId, Long operatorUserId, UpdateOrderStatusDTO updateOrderStatusDTO) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        if (operatorUserId == null || !operatorUserId.equals(order.getUserId())) {
            throw new BusinessException(ResultCode.RESOURCE_FORBIDDEN);
        }

        if (!"started".equals(order.getStatus())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }

        if (updateOrderStatusDTO.getProgress() != null) {
            order.setProgress(updateOrderStatusDTO.getProgress());
        }
        updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeService(Long orderId, Long operatorUserId, UpdateOrderStatusDTO updateOrderStatusDTO) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        if (operatorUserId == null || !operatorUserId.equals(order.getUserId())) {
            throw new BusinessException(ResultCode.RESOURCE_FORBIDDEN);
        }

        if (!"started".equals(order.getStatus())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }

        order.setStatus("completed");
        order.setCompletedAt(LocalDateTime.now());
        order.setProgress(100);
        order.setCompleteMessage(updateOrderStatusDTO.getMessage());

        // 保存完成图片
        if (updateOrderStatusDTO.getImages() != null && !updateOrderStatusDTO.getImages().isEmpty()) {
            List<String> validImages = updateOrderStatusDTO.getImages().stream()
                    .filter(image -> image != null && !image.trim().isEmpty())
                    .collect(Collectors.toList());
            try {
                order.setCompleteImages(objectMapper.writeValueAsString(validImages));
            } catch (Exception e) {
                log.error("完成服务图片序列化失败，orderId={}, images={}", orderId, validImages, e);
                order.setCompleteImages("[]");
            }
        }

        updateById(order);

        // 获取需求信息
        Request request = requestService.getById(order.getRequestId());
        if (request != null) {
            // 更新需求状态
            request.setStatus("completed");
            requestService.updateById(request);

            // 奖励积分
            if ("points".equals(request.getRewardType())) {
                userService.addPoints(order.getUserId(), request.getRewardAmount(),
                        "complete_service", "完成服务奖励", orderId, "order");
            }

            // 更新帮助次数
            userService.incrementHelpCount(order.getUserId());

            // 发送通知给需求发布者
            notificationService.sendNotification(
                    request.getUserId(),
                    "order_completed",
                    "服务已完成",
                    "您发布的需求「" + request.getTitle() + "」已完成",
                    orderId,
                    "order"
            );
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, Long operatorUserId, UpdateOrderStatusDTO updateOrderStatusDTO) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        Request request = requestService.getById(order.getRequestId());
        boolean isOrderOwner = operatorUserId != null && operatorUserId.equals(order.getUserId());
        boolean isRequestOwner = request != null && operatorUserId != null && operatorUserId.equals(request.getUserId());
        if (!isOrderOwner && !isRequestOwner) {
            throw new BusinessException(ResultCode.RESOURCE_FORBIDDEN);
        }

        if ("completed".equals(order.getStatus()) || "cancelled".equals(order.getStatus())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }

        order.setStatus("cancelled");
        order.setCancelledAt(LocalDateTime.now());
        order.setCancelReason(updateOrderStatusDTO.getReason());
        updateById(order);

        // 更新需求状态
        if (request != null) {
            request.setStatus("active");
            request.setAcceptedBy(null);
            request.setAcceptedAt(null);
            requestService.updateById(request);
        }

        // 接单者主动取消，扣除积分；发布者取消不扣接单者积分
        if (isOrderOwner) {
            userService.deductPoints(order.getUserId(), 5, "cancel_order", "取消订单", orderId, "order");
        }
    }

    /**
     * 系统超时回滚：接单后未开始（order.status=active）超过阈值 => 取消订单并把需求回滚为 active。
     * <p>
     * 该方法用于定时任务，不依赖 operatorUserId 的权限校验。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void systemCancelOrderByTimeout(Long orderId, String reason) {
        Order order = getById(orderId);
        if (order == null) {
            return;
        }

        // 只处理仍在「已接单未开始」的订单
        if (!"active".equals(order.getStatus())) {
            return;
        }

        Request request = requestService.getById(order.getRequestId());
        Long publisherUserId = request != null ? request.getUserId() : null;
        String requestTitle = request != null ? request.getTitle() : "";

        // 回滚需求状态到「待接单」
        if (request != null) {
            request.setStatus("active");
            request.setAcceptedBy(null);
            request.setAcceptedAt(null);
            requestService.updateById(request);
        }

        // 取消订单
        order.setStatus("cancelled");
        order.setCancelledAt(LocalDateTime.now());
        order.setCancelReason(reason);
        order.setProgress(0);
        updateById(order);

        // 扣除接单者积分（与用户手动取消一致的策略）
        if (order.getUserId() != null) {
            userService.deductPoints(
                    order.getUserId(),
                    5,
                    "cancel_order_timeout",
                    reason,
                    orderId,
                    "order"
            );
        }

        // 通知需求发布者
        if (publisherUserId != null) {
            notificationService.sendNotification(
                    publisherUserId,
                    "order_timeout_cancelled",
                    "接单超时未响应",
                    "您的需求「" + requestTitle + "」无人响应已退回待接单",
                    orderId,
                    "order"
            );
        }
    }

    @Override
    public OrderVO toVO(Order order) {
        if (order == null) {
            return null;
        }

        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        vo.setId(order.getId());

        // 获取需求信息
        if (order.getRequestId() != null) {
            RequestVO requestVO = requestService.getRequestDetail(order.getRequestId());
            if (requestVO != null) {
                vo.setRequestId(requestVO.getId());
                vo.setTitle(requestVO.getTitle());
                vo.setDescription(requestVO.getDescription());
                vo.setServiceType(requestVO.getServiceType());
                vo.setRewardAmount(requestVO.getRewardAmount());
                vo.setExpectedTime(requestVO.getExpectedTime());
                vo.setLocation(requestVO.getLocation());
                vo.setUrgency(requestVO.getUrgency());
                vo.setPublisher(requestVO.getPublisher());
                vo.setServiceDetails(requestVO.getServiceDetails());
            }
        }

        // 解析完成图片
        if (order.getCompleteImages() != null && !order.getCompleteImages().isEmpty()) {
            try {
                List<String> images = objectMapper.readValue(
                        order.getCompleteImages(),
                        new TypeReference<List<String>>() {}
                );
                vo.setCompleteImages(images);
            } catch (Exception e) {
                vo.setCompleteImages(new ArrayList<>());
            }
        }

        return vo;
    }
}

