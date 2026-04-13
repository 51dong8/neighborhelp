/**
 * 通知模块 API（对接 NotificationController）
 */

import api from '@/utils/api'

export const getNotifications = (params = {}) => {
  return api.get('/notifications', params)
}

export const getUnreadCount = () => {
  return api.get('/notifications/unread-count')
}

export const markNotificationRead = (id) => {
  return api.put(`/notifications/${id}/read`, {})
}

export const markAllNotificationsRead = () => {
  return api.put('/notifications/read-all', {})
}
