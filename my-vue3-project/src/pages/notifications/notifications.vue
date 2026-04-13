<template>
  <view class="notifications-page">
    <view class="toolbar">
      <text class="toolbar-title">通知</text>
      <text
        v-if="list.length && unreadCount > 0"
        class="mark-all"
        @click="markAllRead"
      >全部已读</text>
    </view>

    <scroll-view
      scroll-y
      class="scroll"
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <view v-if="!loading && list.length === 0" class="empty">暂无通知</view>
      <view
        v-for="item in list"
        :key="item.id"
        class="item"
        :class="{ unread: item.isRead === false }"
        @click="onItemTap(item)"
      >
        <view class="row">
          <text class="title">{{ item.title }}</text>
          <view v-if="item.isRead === false" class="dot" />
        </view>
        <text v-if="getTypeLabel(item)" class="type">{{ getTypeLabel(item) }}</text>
        <text class="content">{{ item.content }}</text>
        <text class="time">{{ formatTime(item.createdAt) }}</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import {
  getNotifications,
  markNotificationRead,
  markAllNotificationsRead,
  getUnreadCount
} from '@/api/notification'

const list = ref([])
const loading = ref(true)
const refreshing = ref(false)
const unreadCount = ref(0)

const loadUnread = async () => {
  const token = uni.getStorageSync('token')
  if (!token) {
    unreadCount.value = 0
    return
  }
  try {
    const res = await getUnreadCount()
    unreadCount.value = Number(res.data?.count ?? 0)
  } catch {
    unreadCount.value = 0
  }
}

const loadList = async () => {
  const token = uni.getStorageSync('token')
  if (!token) {
    list.value = []
    loading.value = false
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  try {
    const res = await getNotifications({ page: 1, limit: 50 })
    const page = res.data || {}
    list.value = page.records || []
    await loadUnread()
  } catch (e) {
    console.error('加载通知失败', e)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

const onRefresh = async () => {
  refreshing.value = true
  await loadList()
}

const onItemTap = async (item) => {
  if (!item?.id) return
  if (item.isRead === false) {
    try {
      await markNotificationRead(item.id)
      item.isRead = true
      await loadUnread()
    } catch (e) {
      console.warn('标记已读失败', e)
    }
  }

  // 轻量跳转：订单类通知跳订单详情
  if (item.relatedType === 'order' && item.relatedId) {
    uni.navigateTo({
      url: `/pages/order-detail/order-detail?id=${item.relatedId}`
    })
  }
}

const markAllRead = async () => {
  try {
    await markAllNotificationsRead()
    list.value = list.value.map((n) => ({ ...n, isRead: true }))
    unreadCount.value = 0
    uni.showToast({ title: '已全部标记为已读', icon: 'success' })
  } catch (e) {
    console.error('全部已读失败', e)
  }
}

const formatTime = (t) => {
  if (!t) return ''
  try {
    const d = typeof t === 'string' ? new Date(t) : new Date(t)
    if (Number.isNaN(d.getTime())) return String(t)
    return d.toLocaleString('zh-CN')
  } catch {
    return String(t)
  }
}

const getTypeLabel = (item) => {
  const t = (item?.type || '').toString()
  if (!t) return ''
  const map = {
    order_accepted: '订单 · 已接单',
    order_completed: '订单 · 已完成',
    order_timeout_cancelled: '订单 · 超时自动取消'
  }
  return map[t] || ''
}

onShow(() => {
  loadList()
})
</script>

<style scoped>
.notifications-page {
  min-height: 100vh;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 32rpx;
  background: #fff;
  border-bottom: 1rpx solid #eee;
}

.toolbar-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #333;
}

.mark-all {
  font-size: 26rpx;
  color: #ff8f00;
}

.scroll {
  flex: 1;
  height: 0;
  padding: 16rpx;
}

.empty {
  text-align: center;
  color: #999;
  padding: 80rpx 0;
  font-size: 28rpx;
}

.item {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
}

.item.unread {
  border-left: 6rpx solid #ff8f00;
}

.row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.title {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  flex: 1;
}

.type {
  display: block;
  margin-top: -4rpx;
  margin-bottom: 8rpx;
  font-size: 22rpx;
  color: #ff8f00;
}

.dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background: #ff5722;
  margin-left: 12rpx;
}

.content {
  font-size: 26rpx;
  color: #666;
  line-height: 1.5;
  display: block;
}

.time {
  font-size: 22rpx;
  color: #aaa;
  margin-top: 12rpx;
  display: block;
}
</style>
