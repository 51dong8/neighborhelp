package com.neighborhelp.controller;

import com.neighborhelp.common.Result;
import com.neighborhelp.common.ResultCode;
import com.neighborhelp.common.exception.BusinessException;
import com.neighborhelp.dto.BindPhoneDTO;
import com.neighborhelp.dto.ChangePasswordDTO;
import com.neighborhelp.dto.SubmitAuthDTO;
import com.neighborhelp.dto.UpdateProfileDTO;
import com.neighborhelp.entity.User;
import com.neighborhelp.service.OrderService;
import com.neighborhelp.service.RequestService;
import com.neighborhelp.service.UserService;
import com.neighborhelp.vo.OrderVO;
import com.neighborhelp.vo.PageVO;
import com.neighborhelp.vo.PointRecordVO;
import com.neighborhelp.vo.PointsVO;
import com.neighborhelp.vo.RequestVO;
import com.neighborhelp.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@Tag(name = "用户管理", description = "用户信息、积分管理")
@RestController
@RequestMapping({"/api/user", "/api/users"})
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final RequestService requestService;
    private final OrderService orderService;

    /**
     * 获取当前用户ID
     */
    private Long getRequiredCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }
        throw new BusinessException(ResultCode.UNAUTHORIZED);
    }

    @Operation(summary = "获取用户信息")
    @GetMapping("/profile")
    public Result<UserVO> getUserProfile() {
        Long userId = getRequiredCurrentUserId();
        UserVO userVO = userService.getUserProfile(userId);
        return Result.success(userVO);
    }

    @Operation(summary = "更新用户信息")
    @PutMapping("/profile")
    public Result<UserVO> updateUserProfile(@Valid @RequestBody UpdateProfileDTO updateProfileDTO) {
        Long userId = getRequiredCurrentUserId();
        UserVO userVO = userService.updateUserProfile(userId, updateProfileDTO);
        return Result.success("更新成功", userVO);
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
        Long userId = getRequiredCurrentUserId();
        userService.changePassword(userId, changePasswordDTO);
        return Result.<Void>success("密码修改成功");
    }

    @Operation(summary = "获取用户积分")
    @GetMapping("/points")
    public Result<PointsVO> getUserPoints() {
        Long userId = getRequiredCurrentUserId();
        PointsVO pointsVO = userService.getUserPoints(userId);
        return Result.success(pointsVO);
    }

    @Operation(summary = "获取当前登录用户积分概况")
    @GetMapping("/points/current")
    public Result<PointsVO> getCurrentUserPoints() {
        Long userId = getRequiredCurrentUserId();
        PointsVO pointsVO = userService.getUserPoints(userId);
        return Result.success(pointsVO);
    }

    @Operation(summary = "获取积分记录")
    @GetMapping("/points/records")
    public Result<PageVO<PointRecordVO>> getPointRecords(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        Long userId = getRequiredCurrentUserId();
        PageVO<PointRecordVO> pageVO = userService.getPointRecords(userId, page, limit);
        return Result.success(pageVO);
    }

    @Operation(summary = "获取用户发布的需求")
    @GetMapping("/requests")
    public Result<PageVO<RequestVO>> getUserRequests(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        Long userId = getRequiredCurrentUserId();
        PageVO<RequestVO> pageVO = requestService.getUserRequests(userId, status, page, limit);
        return Result.success(pageVO);
    }

    @Operation(summary = "获取用户接收的订单")
    @GetMapping("/orders")
    public Result<PageVO<OrderVO>> getUserOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        Long userId = getRequiredCurrentUserId();
        PageVO<OrderVO> pageVO = orderService.getUserOrders(userId, status, page, limit);
        return Result.success(pageVO);
    }

    @Operation(summary = "绑定手机号")
    @PutMapping("/bind-phone")
    public Result<Void> bindPhone(@Valid @RequestBody BindPhoneDTO dto) {
        Long userId = getRequiredCurrentUserId();
        userService.bindPhone(userId, dto.getPhone(), dto.getCode());
        return Result.success("手机号绑定成功");
    }
    @Operation(summary = "解除绑定手机号")
    @PutMapping("/unbind-phone")
    public Result<Void> unbindPhone() {
        Long userId = getRequiredCurrentUserId();
        User user = userService.getById(userId);
        user.setPhone(null); // 清空手机号
        userService.updateById(user);
        return Result.success("已解除绑定");
    }

    @PostMapping("/community/auth")
    @Operation(summary = "提交业主认证材料", description = "上传材料并触发AI自动比对")
    public Result<Integer> submitAuthentication(@Valid @RequestBody SubmitAuthDTO authDTO) {
        Long userId = getRequiredCurrentUserId();

        // 执行认证逻辑
        userService.submitAuthentication(userId, authDTO.getCommunityName(), authDTO.getMaterialUrl());

        // 重新查询用户，获取最新的认证状态 (1=审核中，2=已认证)并返回
        User user = userService.getById(userId);
        return Result.success("提交成功", user.getAuthStatus());
    }
}
