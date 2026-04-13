package com.neighborhelp.controller;

import com.neighborhelp.common.Result;
import com.neighborhelp.common.ResultCode;
import com.neighborhelp.common.exception.BusinessException;
import com.neighborhelp.dto.CreateRequestDTO;
import com.neighborhelp.service.RequestService;
import com.neighborhelp.vo.PageVO;
import com.neighborhelp.vo.RequestVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 需求控制器
 */
@Tag(name = "需求管理", description = "发布、查询、管理需求")
@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;

    private Long getRequiredCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }
        throw new BusinessException(ResultCode.UNAUTHORIZED);
    }

    @Operation(summary = "发布需求")
    @PostMapping
    public Result<RequestVO> createRequest(@Valid @RequestBody CreateRequestDTO createRequestDTO) {
        Long userId = getRequiredCurrentUserId();
        RequestVO requestVO = requestService.createRequest(userId, createRequestDTO);
        return Result.success("需求发布成功", requestVO);
    }

    @Operation(summary = "获取需求列表")
    @GetMapping
    public Result<PageVO<RequestVO>> getRequestList(
            @RequestParam(required = false) String serviceType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(defaultValue = "latest") String sort) {
        PageVO<RequestVO> pageVO = requestService.getRequestList(serviceType, status, page, limit, sort);
        return Result.success(pageVO);
    }

    @Operation(summary = "获取需求详情")
    @GetMapping("/{id}")
    public Result<RequestVO> getRequestDetail(@PathVariable Long id) {
        RequestVO requestVO = requestService.getRequestDetail(id);
        return Result.success(requestVO);
    }

    @Operation(summary = "更新需求状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateRequestStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRequestStatusDTO statusDTO) {
        Long userId = getRequiredCurrentUserId();
        requestService.updateRequestStatus(id, userId, statusDTO.getStatus(), statusDTO.getReason());
        return Result.<Void>success("状态更新成功");
    }

    @Operation(summary = "删除需求")
    @DeleteMapping("/{id}")
    public Result<Void> deleteRequest(@PathVariable Long id) {
        Long userId = getRequiredCurrentUserId();
        requestService.deleteRequest(id, userId);
        return Result.<Void>success("删除成功");
    }

    /**
     * 更新需求状态DTO（内部类）
     */
    @lombok.Data
    static class UpdateRequestStatusDTO {
        @NotBlank(message = "状态不能为空")
        @Pattern(regexp = "^(completed|cancelled)$", message = "状态仅支持 completed/cancelled")
        private String status;
        private String reason;
    }
    /**
     * 获取附近的需求
     * 示例：GET /api/requests/nearby?longitude=116.397&latitude=39.908&radius=2.0
     */
    @GetMapping("/nearby")
    public Result<List<RequestVO>> getNearbyRequests(
            @RequestParam Double longitude,
            @RequestParam Double latitude,
            @RequestParam(defaultValue = "2.0") Double radius,
            HttpServletRequest request) { // 新增 HttpServletRequest 获取上下文
        // 1. 获取当前登录用户的ID
        // 注意：这里获取 userId 的方式取决于你的鉴权拦截器 (如 JwtAuthenticationFilter)
        // 如果你的拦截器把解析出的 userId 放到了 request 的 Attribute 中，可以直接这样取：
        Long currentUserId = (Long) request.getAttribute("userId");
        // 2. 将 currentUserId 作为第四个参数传给 Service
        List<RequestVO> list = requestService.getNearbyRequests(longitude, latitude, radius, currentUserId);
        return Result.success(list);
    }
}

