package com.neighborhelp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neighborhelp.common.ResultCode;
import com.neighborhelp.common.exception.BusinessException;
import com.neighborhelp.entity.ShopItem;
import com.neighborhelp.mapper.ShopItemMapper;
import com.neighborhelp.service.ShopService;
import com.neighborhelp.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 积分商城服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShopServiceImpl extends ServiceImpl<ShopItemMapper, ShopItem> implements ShopService {

    private final UserService userService;

    @Override
    public List<ShopItem> getAvailableItems() {
        List<ShopItem> items = list(new LambdaQueryWrapper<ShopItem>()
                .eq(ShopItem::getStatus, 1)
                .gt(ShopItem::getStock, 0)
                .orderByAsc(ShopItem::getPointsCost)
                .orderByDesc(ShopItem::getCreatedAt));
        log.info("查询可兑换商品成功，数量={}", items.size());
        return items;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void redeemItem(Long userId, Long itemId) {
        log.info("开始兑换商品，userId={}, itemId={}", userId, itemId);

        ShopItem item = getById(itemId);
        if (item == null || item.getStatus() == null || item.getStatus() != 1) {
            log.info("兑换失败，商品不存在或已下架，itemId={}", itemId);
            throw new BusinessException(ResultCode.NOT_FOUND, "商品不存在或已下架");
        }
        if (item.getStock() == null || item.getStock() <= 0) {
            log.info("兑换失败，商品库存不足，itemId={}, stock={}", itemId, item.getStock());
            throw new BusinessException(ResultCode.ERROR, "商品库存不足");
        }

        log.info("开始扣减积分，userId={}, itemId={}, pointsCost={}, itemName={}",
                userId, itemId, item.getPointsCost(), item.getName());

        userService.deductPoints(
                userId,
                item.getPointsCost(),
                "redeem",
                "兑换商品: " + item.getName(),
                item.getId(),
                "shop_item"
        );

        item.setStock(item.getStock() - 1);
        updateById(item);

        log.info("兑换成功，userId={}, itemId={}, remainStock={}", userId, itemId, item.getStock());
    }
}
