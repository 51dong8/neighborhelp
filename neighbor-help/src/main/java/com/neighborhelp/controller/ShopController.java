package com.neighborhelp.controller;

import com.neighborhelp.common.Result;
import com.neighborhelp.common.ResultCode;
import com.neighborhelp.common.exception.BusinessException;
import com.neighborhelp.entity.ShopItem;
import com.neighborhelp.service.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 积分商城控制器
 */
@Tag(name = "积分商城", description = "积分商城商品与兑换")
@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    private Long getRequiredCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }
        throw new BusinessException(ResultCode.UNAUTHORIZED);
    }

    @Operation(summary = "获取可兑换商品列表")
    @GetMapping("/items")
    public Result<List<ShopItem>> getAvailableItems() {
        return Result.success(shopService.getAvailableItems());
    }

    @Operation(summary = "兑换商品")
    @PostMapping("/redeem/{itemId}")
    public Result<Void> redeemItem(@PathVariable Long itemId) {
        Long userId = getRequiredCurrentUserId();
        shopService.redeemItem(userId, itemId);
        return Result.<Void>success("兑换成功");
    }
}
