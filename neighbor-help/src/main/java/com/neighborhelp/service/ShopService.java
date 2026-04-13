package com.neighborhelp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.neighborhelp.entity.ShopItem;

import java.util.List;

/**
 * 积分商城服务接口
 */
public interface ShopService extends IService<ShopItem> {

    /**
     * 获取可兑换商品列表
     */
    List<ShopItem> getAvailableItems();

    /**
     * 兑换商品
     */
    void redeemItem(Long userId, Long itemId);
}
