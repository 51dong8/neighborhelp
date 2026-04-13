package com.neighborhelp.controller;

import com.neighborhelp.common.Result;
import com.neighborhelp.dto.CreateFeedbackDTO;
import com.neighborhelp.entity.Feedback;
import com.neighborhelp.entity.Order;
import com.neighborhelp.entity.Request;
import com.neighborhelp.service.FeedbackService;
import com.neighborhelp.service.OrderService;
import com.neighborhelp.service.RequestService;
import com.neighborhelp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统：健康检查、公开配置、运行统计
 */
@Tag(name = "系统", description = "健康检查与系统信息")
@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
public class SystemController {

    private final UserService userService;
    private final RequestService requestService;
    private final OrderService orderService;
    private final FeedbackService feedbackService;

    @Operation(summary = "健康检查")
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("timestamp", System.currentTimeMillis());
        data.put("service", "neighbor-help");
        return Result.success(data);
    }

    @Operation(summary = "公开应用配置（不含密钥）")
    @GetMapping("/config")
    public Result<Map<String, Object>> config() {
        Map<String, Object> data = new HashMap<>();
        data.put("appName", "邻里帮帮");
        data.put("apiVersion", "1.0");
        Map<String, Object> features = new HashMap<>();
        features.put("speech", true);
        features.put("notifications", true);
        features.put("requests", true);
        features.put("orders", true);
        data.put("features", features);
        return Result.success(data);
    }

    @Operation(summary = "运行统计（聚合）")
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        long userCount = userService.count();
        long activeRequestCount = requestService.lambdaQuery()
                .eq(Request::getStatus, "active")
                .count();
        long orderCount = orderService.count();
        long activeOrderCount = orderService.lambdaQuery()
                .in(Order::getStatus, "active", "started")
                .count();

        Map<String, Object> data = new HashMap<>();
        data.put("totalUsers", userCount);
        data.put("activeRequests", activeRequestCount);
        data.put("totalOrders", orderCount);
        data.put("activeOrders", activeOrderCount);
        data.put("timestamp", System.currentTimeMillis());
        return Result.success(data);
    }
    @Operation(summary = "提交意见反馈")
    @PostMapping("/feedback")
    public Result<Boolean> submitFeedback(@RequestBody CreateFeedbackDTO dto) {
        Feedback feedback = new Feedback();
        feedback.setType(dto.getType());
        feedback.setContent(dto.getContent());
        feedback.setContact(dto.getContact());
        feedback.setStatus("pending");

        // 如果你的系统有上下文工具类(比如SecurityUtils/JwtUtil)获取当前登录用户ID，可以在这里设置：
        // Long currentUserId = SecurityUtils.getCurrentUserId();
        // feedback.setUserId(currentUserId);

        boolean saved = feedbackService.save(feedback);
        return Result.success(saved);
    }

}
