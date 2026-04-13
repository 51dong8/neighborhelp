package com.neighborhelp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.neighborhelp.entity.Notification;
import com.neighborhelp.vo.NotificationVO;
import com.neighborhelp.vo.PageVO;

/**
 * 通知服务接口
 */
public interface NotificationService extends IService<Notification> {
    /**
     * 获取用户通知列表
     */
    PageVO<NotificationVO> getUserNotifications(Long userId, Integer page, Integer limit);

    /**
     * 获取未读通知数量
     */
    Long getUnreadCount(Long userId);

    /**
     * 标记通知已读
     */
    void markAsRead(Long notificationId);

    /**
     * 标记所有通知已读
     */
    void markAllAsRead(Long userId);

    /**
     * 发送通知
     */
    void sendNotification(Long userId, String type, String title, String content, Long relatedId, String relatedType);
}

