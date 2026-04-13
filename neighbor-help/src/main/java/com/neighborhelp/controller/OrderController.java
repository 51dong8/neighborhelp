package com.neighborhelp.controller;

import com.neighborhelp.common.Result;
import com.neighborhelp.common.ResultCode;
import com.neighborhelp.common.exception.BusinessException;
import com.neighborhelp.dto.AcceptOrderDTO;
import com.neighborhelp.dto.UpdateOrderStatusDTO;
import com.neighborhelp.service.OrderService;
import com.neighborhelp.vo.OrderVO;
import com.neighborhelp.vo.PageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 订单控制器
 */
@Tag(name = "订单管理", description = "接单、订单状态管理")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    private Long getRequiredCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }
        throw new BusinessException(ResultCode.UNAUTHORIZED);
    }

    @Operation(summary = "接单")
    @PostMapping
    public Result<OrderVO> acceptOrder(@Valid @RequestBody AcceptOrderDTO acceptOrderDTO) {
        Long userId = getRequiredCurrentUserId();
        OrderVO orderVO = orderService.acceptOrder(userId, acceptOrderDTO);
        return Result.success("接单成功", orderVO);
    }

    @Operation(summary = "获取当前用户的接单列表（与 GET /api/user/orders 语义一致，仅本人订单）")
    @GetMapping
    public Result<PageVO<OrderVO>> getOrderList(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        Long userId = getRequiredCurrentUserId();
        PageVO<OrderVO> pageVO = orderService.getUserOrders(userId, status, page, limit);
        return Result.success(pageVO);
    }

    @Operation(summary = "获取订单详情（接单者或需求发布者可查看）")
    @GetMapping("/{id}")
    public Result<OrderVO> getOrderDetail(@PathVariable Long id) {
        Long userId = getRequiredCurrentUserId();
        OrderVO orderVO = orderService.getOrderDetail(id, userId);
        return Result.success(orderVO);
    }

    @Operation(summary = "开始服务")
    @PutMapping("/{id}/start")
    public Result<Void> startService(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusDTO updateOrderStatusDTO) {
        Long userId = getRequiredCurrentUserId();
        orderService.startService(id, userId, updateOrderStatusDTO);
        return Result.<Void>success("服务已开始");
    }

    @Operation(summary = "更新服务进度")
    @PutMapping("/{id}/progress")
    public Result<Void> updateProgress(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusDTO updateOrderStatusDTO) {
        Long userId = getRequiredCurrentUserId();
        orderService.updateProgress(id, userId, updateOrderStatusDTO);
        return Result.<Void>success("进度更新成功");
    }

    @Operation(summary = "完成服务")
    @PutMapping("/{id}/complete")
    public Result<Void> completeService(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusDTO updateOrderStatusDTO) {
        Long userId = getRequiredCurrentUserId();
        orderService.completeService(id, userId, updateOrderStatusDTO);
        return Result.<Void>success("服务已完成");
    }

    @Operation(summary = "取消订单")
    @PutMapping("/{id}/cancel")
    public Result<Void> cancelOrder(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusDTO updateOrderStatusDTO) {
        Long userId = getRequiredCurrentUserId();
        orderService.cancelOrder(id, userId, updateOrderStatusDTO);
        return Result.<Void>success("订单已取消");
    }
}

