package com.neighborhelp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neighborhelp.entity.Notification;
import com.neighborhelp.mapper.NotificationMapper;
import com.neighborhelp.service.NotificationService;
import com.neighborhelp.vo.NotificationVO;
import com.neighborhelp.vo.PageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 通知服务实现
 */
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {

    @Override
    public PageVO<NotificationVO> getUserNotifications(Long userId, Integer page, Integer limit) {
        Page<Notification> pageParam = new Page<>(page, limit);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreatedAt);

        IPage<Notification> pageResult = page(pageParam, wrapper);
        List<NotificationVO> voList = pageResult.getRecords().stream()
                .map(n -> {
                    NotificationVO vo = new NotificationVO();
                    BeanUtils.copyProperties(n, vo);
                    return vo;
                })
                .collect(Collectors.toList());

        return new PageVO<>(voList, pageResult.getTotal(), page, limit);
    }

    @Override
    public Long getUnreadCount(Long userId) {
        return count(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, false));
    }

    @Override
    public void markAsRead(Long notificationId) {
        Notification notification = getById(notificationId);
        if (notification != null) {
            notification.setIsRead(true);
            updateById(notification);
        }
    }

    @Override
    public void markAllAsRead(Long userId) {
        update(new LambdaUpdateWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, false)
                .set(Notification::getIsRead, true));
    }

    @Override
    public void sendNotification(Long userId, String type, String title, String content, Long relatedId, String relatedType) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setIsRead(false);
        notification.setRelatedId(relatedId);
        notification.setRelatedType(relatedType);
        save(notification);
    }
}

