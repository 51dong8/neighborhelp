package com.neighborhelp.controller;

import com.neighborhelp.common.Result;
import com.neighborhelp.common.ResultCode;
import com.neighborhelp.common.exception.BusinessException;
import com.neighborhelp.service.NotificationService;
import com.neighborhelp.vo.NotificationVO;
import com.neighborhelp.vo.PageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 通知控制器
 */
@Tag(name = "通知管理", description = "消息通知管理")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    private Long getRequiredCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }
        throw new BusinessException(ResultCode.UNAUTHORIZED);
    }

    @Operation(summary = "获取通知列表")
    @GetMapping
    public Result<PageVO<NotificationVO>> getNotifications(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit) {
        Long userId = getRequiredCurrentUserId();
        PageVO<NotificationVO> pageVO = notificationService.getUserNotifications(userId, page, limit);
        return Result.success(pageVO);
    }

    @Operation(summary = "获取未读数量")
    @GetMapping("/unread-count")
    public Result<Map<String, Long>> getUnreadCount() {
        Long userId = getRequiredCurrentUserId();
        Long count = notificationService.getUnreadCount(userId);
        Map<String, Long> result = new HashMap<>();
        result.put("count", count);
        return Result.success(result);
    }

    @Operation(summary = "标记通知为已读")
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        getRequiredCurrentUserId();
        notificationService.markAsRead(id);
        return Result.<Void>success("标记成功");
    }

    @Operation(summary = "标记所有通知为已读")
    @PutMapping("/read-all")
    public Result<Void> markAllAsRead() {
        Long userId = getRequiredCurrentUserId();
        notificationService.markAllAsRead(userId);
        return Result.<Void>success("标记成功");
    }
}
