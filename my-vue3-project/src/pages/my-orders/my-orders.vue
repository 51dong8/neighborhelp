<template>
  <view class="my-orders-container">
    <!-- 状态栏 -->
    <view class="status-bar">
      <view class="time">9:41</view>
      <view class="status-icons">
        <view class="signal-icon">📶</view>
        <view class="wifi-icon">📶</view>
        <view class="battery-icon">🔋</view>
      </view>
    </view>

    <!-- 顶部导航栏 -->
    <view class="top-nav">
      <view class="nav-left" @click="goBack">
        <view class="back-btn">
          <text class="back-icon">←</text>
        </view>
      </view>
      <view class="nav-center">
        <text class="page-title">我的订单</text>
      </view>
      <view class="nav-right">
        <view class="refresh-btn" @click="refreshOrders">
          <text class="refresh-icon">🔄</text>
        </view>
      </view>
    </view>

    <!-- 主内容区域 -->
    <view class="main-content">
      <!-- 统计信息 -->
      <view class="stats-section">
        <view class="stats-card">
          <view class="stat-item">
            <text class="stat-number">{{ totalOrders }}</text>
            <text class="stat-label">总订单</text>
          </view>
          <view class="stat-item">
            <text class="stat-number">{{ activeOrders }}</text>
            <text class="stat-label">进行中</text>
          </view>
          <view class="stat-item">
            <text class="stat-number">{{ completedOrders }}</text>
            <text class="stat-label">已完成</text>
          </view>
              <view class="stat-item">
            <text class="stat-number">{{ totalEarnings }}</text>
            <text class="stat-label">总收益</text>
          </view>
        </view>
      </view>

      <!-- 筛选栏 -->
      <view class="filter-section">
        <view class="filter-bar">
          <view 
            class="filter-item" 
            :class="{ active: currentFilter === 'all' }"
            @click="setFilter('all')"
          >
            <text>全部</text>
          </view>
          <view 
            class="filter-item" 
            :class="{ active: currentFilter === 'active' }"
            @click="setFilter('active')"
          >
            <text>进行中</text>
          </view>
          <view 
            class="filter-item" 
            :class="{ active: currentFilter === 'completed' }"
            @click="setFilter('completed')"
          >
            <text>已完成</text>
          </view>
          <view 
            class="filter-item" 
            :class="{ active: currentFilter === 'cancelled' }"
            @click="setFilter('cancelled')"
          >
            <text>已取消</text>
          </view>
        </view>
      </view>

      <!-- 订单列表 -->
      <view class="orders-list">
        <view 
          class="order-card" 
          v-for="order in filteredOrders" 
          :key="order.id"
          @click="goToOrderDetail(order.id)"
        >
          <!-- 状态标识 -->
          <view class="status-badge" :class="order.status">
            <text class="status-text">{{ getStatusTextWithReason(order) }}</text>
          </view>

          <!-- 订单内容 -->
          <view class="order-content">
            <view class="order-header">
              <view class="title-section">
                <text class="order-title">{{ order.title }}</text>
              </view>
              <view class="reward-section">
                <text class="reward-amount">+¥{{ order.rewardAmount }}</text>
              </view>
            </view>

            <text class="order-description">{{ order.description }}</text>

            <!-- 订单详情 -->
            <view class="order-details">
              <view class="detail-item">
                <text class="detail-icon">⏰</text>
                <text class="detail-text">{{ order.expectedTime }}</text>
              </view>
              <view class="detail-item">
                <text class="detail-icon">📍</text>
                <text class="detail-text">{{ order.location }}</text>
              </view>
              <view class="detail-item">
                <text class="detail-icon">📦</text>
                <text class="detail-text">{{ getServiceTypeName(order.serviceType) }}</text>
              </view>
            </view>

            <!-- 发布者信息 -->
            <view class="publisher-info">
              <view class="publisher-header">
                <text class="publisher-label">发布者</text>
                <text class="accept-time">接单时间：{{ order.acceptedAt }}</text>
              </view>
              <view class="publisher-details">
                <view class="publisher-avatar">
                  <text class="avatar-text">{{ order.publisher.username.charAt(0) }}</text>
                </view>
                <view class="publisher-info-text">
                  <text class="publisher-name">{{ order.publisher.username }}</text>
                  <text class="publisher-rating">⭐ {{ order.publisher.rating }}</text>
                </view>
                <view class="contact-btn" @click.stop="contactPublisher(order.publisher)">
                  <text class="contact-text">联系</text>
                </view>
              </view>
            </view>

            <!-- 取消原因（含系统超时取消） -->
            <view v-if="order.status === 'cancelled' && order.cancelReason" class="cancel-reason">
              <text class="cancel-label">取消原因：</text>
              <text class="cancel-text">{{ order.cancelReason }}</text>
            </view>

            <!-- 操作按钮 -->
            <view class="action-buttons">
              <view class="action-btn secondary" @click.stop="viewOrderDetail(order.id)">
                <text class="btn-text">查看详情</text>
              </view>
              <view 
                class="action-btn primary" 
                v-if="order.status === 'active'"
                @click.stop="startOrder(order.id)"
              >
                <text class="btn-text">开始服务</text>
              </view>
              <view 
                class="action-btn success" 
                v-if="order.status === 'in_progress'"
                @click.stop="completeOrder(order.id)"
              >
                <text class="btn-text">完成服务</text>
              </view>
              <view 
                class="action-btn danger" 
                v-if="order.status === 'active' || order.status === 'in_progress'"
                @click.stop="cancelOrder(order.id)"
              >
                <text class="btn-text">取消接单</text>
              </view>
            </view>

            <!-- 进度条 -->
            <view class="progress-section" v-if="order.status === 'in_progress'">
              <view class="progress-header">
                <text class="progress-label">服务进度</text>
                <text class="progress-percent">{{ order.progress }}%</text>
              </view>
              <view class="progress-bar">
                <view class="progress-fill" :style="{ width: order.progress + '%' }"></view>
              </view>
            </view>
          </view>
        </view>

        <!-- 空状态 -->
        <view v-if="filteredOrders.length === 0" class="empty-state">
          <view class="empty-icon">📋</view>
          <text class="empty-title">暂无订单</text>
          <text class="empty-desc">您还没有接收过任何订单，快去首页看看有什么可以帮忙的吧！</text>
          <view class="empty-action" @click="goToHome">
            <text class="action-text">去首页看看</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getUserOrders } from '@/api/user'
import { startService as apiStartService, completeService as apiCompleteService, cancelOrder as apiCancelOrder } from '@/api/order'

// 响应式数据
const currentFilter = ref('all')
const orders = ref([])
const isLoading = ref(false)

// 计算属性
const totalOrders = computed(() => orders.value.length)
const activeOrders = computed(() => orders.value.filter(o =>
  o.status === 'active' || o.status === 'in_progress'
).length)
const completedOrders = computed(() => orders.value.filter(o => o.status === 'completed').length)
const totalEarnings = computed(() => {
  return orders.value
    .filter(o => o.status === 'completed')
    .reduce((sum, o) => sum + o.rewardAmount, 0)
})

const filteredOrders = computed(() => {
  if (currentFilter.value === 'all') {
    return orders.value
  }
  return orders.value.filter(order => order.status === currentFilter.value)
})

// 服务类型映射
const serviceTypeMap = {
  package: '帮我取快递',
  pet: '帮我照顾宠物',
  repair: '帮我家电维修',
  shopping: '帮我买菜',
  cleaning: '帮我家清洁',
  cooking: '帮我做美食'
}

// 方法
const goBack = () => {
  uni.navigateBack()
}

const goToHome = () => {
  uni.switchTab({
    url: '/pages/home/home'
  })
}

const refreshOrders = () => {
  loadOrders()
}

const setFilter = (filter) => {
  currentFilter.value = filter
}

const getStatusText = (status) => {
  const statusMap = {
    active: '已接单',
    in_progress: '进行中',
    completed: '已完成',
    cancelled: '已取消'
  }
  return statusMap[status] || '未知'
}

const isTimeoutCancelled = (order) => {
  const r = (order?.cancelReason || '').toString()
  return r.includes('超时') || r.includes('timeout')
}

const getStatusTextWithReason = (order) => {
  if (!order) return '未知'
  if (order.status === 'cancelled' && isTimeoutCancelled(order)) return '超时取消'
  return getStatusText(order.status)
}

const getServiceTypeName = (serviceType) => {
  return serviceTypeMap[serviceType] || '未知服务'
}

const goToOrderDetail = (orderId) => {
  uni.navigateTo({
    url: `/pages/order-detail/order-detail?id=${orderId}`
  })
}

const viewOrderDetail = (orderId) => {
  uni.navigateTo({
    url: `/pages/order-detail/order-detail?id=${orderId}`
  })
}

const contactPublisher = (publisher) => {
  uni.showActionSheet({
    itemList: ['拨打电话', '发送微信', '查看资料'],
    success: (res) => {
      switch (res.tapIndex) {
        case 0:
          uni.makePhoneCall({
            phoneNumber: publisher.phone
          })
          break
        case 1:
          uni.showToast({
            title: `微信号：${publisher.wechat}`,
            icon: 'none',
            duration: 3000
          })
          break
        case 2:
          uni.showModal({
            title: '用户信息',
            content: `用户名：${publisher.username}\n评分：${publisher.rating}⭐\n电话：${publisher.phone}\n微信：${publisher.wechat}`,
            showCancel: false
          })
          break
      }
    }
  })
}

// 从列表直接开始服务
const startOrder = (orderId) => {
  uni.showModal({
    title: '开始服务',
    content: '确定要开始这条订单的服务吗？',
    success: async (res) => {
      if (!res.confirm) return
      try {
        uni.showLoading({ title: '正在开始...' })
        await apiStartService(orderId, {})
        // 本地状态更新为进行中
        orders.value = orders.value.map(o =>
          o.id === orderId ? { ...o, status: 'in_progress' } : o
        )
        uni.showToast({ title: '服务已开始', icon: 'success' })
      } catch (e) {
        console.error('开始服务失败:', e)
        uni.showToast({ title: '操作失败，请稍后重试', icon: 'none' })
      } finally {
        uni.hideLoading()
      }
    }
  })
}

// 从列表直接完成订单
const completeOrder = (orderId) => {
  uni.showModal({
    title: '完成服务',
    content: '确认该服务已经完成吗？完成后将计入已完成订单。',
    success: async (res) => {
      if (!res.confirm) return
      try {
        uni.showLoading({ title: '正在完成...' })
        await apiCompleteService(orderId, { message: '已在我的订单中完成' })
        // 本地状态更新为已完成
        orders.value = orders.value.map(o =>
          o.id === orderId ? { ...o, status: 'completed', completedAt: new Date().toISOString() } : o
        )
        uni.showToast({ title: '订单已完成', icon: 'success' })
      } catch (e) {
        console.error('完成订单失败:', e)
        uni.showToast({ title: '操作失败，请稍后重试', icon: 'none' })
      } finally {
        uni.hideLoading()
      }
    }
  })
}

// 从列表直接取消订单
const cancelOrder = (orderId) => {
  uni.showModal({
    title: '取消接单',
    content: '确定要取消这条订单吗？取消后需求将重新开放给他人接单。',
    success: async (res) => {
      if (!res.confirm) return
      try {
        uni.showLoading({ title: '正在取消...' })
        await apiCancelOrder(orderId, { reason: '用户在我的订单中取消' })
        // 本地状态更新为已取消
        orders.value = orders.value.map(o =>
          o.id === orderId ? { ...o, status: 'cancelled' } : o
        )
        uni.showToast({ title: '订单已取消', icon: 'success' })
      } catch (e) {
        console.error('取消订单失败:', e)
        uni.showToast({ title: '操作失败，请稍后重试', icon: 'none' })
      } finally {
        uni.hideLoading()
      }
    }
  })
}

// 从后端加载当前用户的订单列表
const loadOrders = async () => {
  isLoading.value = true
  try {
    const res = await getUserOrders({
      status: currentFilter.value === 'all' ? undefined : currentFilter.value,
      page: 1,
      limit: 50
    })
    // 后端返回 Result<PageVO<OrderVO>>
    const pageData = res.data || res?.data?.data || {}
    const list = pageData.records || pageData.list || []
    // 将后端的 started 状态统一映射为前端的 in_progress，便于筛选与展示
    orders.value = list.map(o => ({
      ...o,
      // 将后端的 started 状态统一映射为前端的 in_progress，便于筛选与展示
      status: o.status === 'started' ? 'in_progress' : o.status
    }))
  } catch (error) {
    console.error('加载订单失败:', error)
    uni.showToast({
      title: '加载订单失败',
      icon: 'none'
    })
  } finally {
    isLoading.value = false
  }
}

// 生命周期
onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
/* 容器 */
.my-orders-container {
  min-height: 100vh;
  background: #F5F5F5;
  display: flex;
  flex-direction: column;
}

/* 状态栏 */
.status-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8rpx 24rpx;
  background: #000;
  color: white;
  font-size: 24rpx;
}

.status-icons {
  display: flex;
  gap: 8rpx;
}

/* 顶部导航栏 */
.top-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 24rpx;
  background: white;
  border-bottom: 1rpx solid #E0E0E0;
}

.nav-left, .nav-right {
  width: 80rpx;
  display: flex;
  justify-content: center;
}

.back-btn, .refresh-btn {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background: #F5F5F5;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.back-btn:active, .refresh-btn:active {
  background: #E0E0E0;
  transform: scale(0.95);
}

.back-icon, .refresh-icon {
  font-size: 28rpx;
  color: #424242;
}

.page-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #424242;
}

/* 主内容区域 */
.main-content {
  flex: 1;
  padding: 0 20rpx;
  overflow: hidden;
}

/* 统计信息 */
.stats-section {
  margin: 16rpx 0;
}

.stats-card {
  background: white;
  border-radius: 16rpx;
  padding: 20rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
  display: flex;
  justify-content: space-around;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}

.stat-number {
  font-size: 28rpx;
  font-weight: 700;
  color: #FF8F00;
}

.stat-label {
  font-size: 20rpx;
  color: #666;
}

/* 筛选栏 */
.filter-section {
  margin: 16rpx 0;
}

.filter-bar {
  display: flex;
  background: white;
  border-radius: 16rpx;
  padding: 8rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
  gap: 8rpx;
}

.filter-item {
  flex: 1;
  padding: 12rpx 16rpx;
  border-radius: 12rpx;
  text-align: center;
  transition: all 0.3s ease;
}

.filter-item text {
  font-size: 24rpx;
  color: #666;
}

.filter-item.active {
  background: #FF8F00;
}

.filter-item.active text {
  color: white;
  font-weight: 600;
}

/* 订单列表 */
.orders-list {
  flex: 1;
  overflow: hidden;
}

.order-card {
  position: relative;
  background: white;
  border-radius: 16rpx;
  margin-bottom: 12rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
  overflow: hidden;
  transition: all 0.3s ease;
}

.order-card:active {
  transform: translateY(-2rpx);
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.12);
}

/* 状态标识 */
.status-badge {
  position: absolute;
  top: 8rpx;
  right: 16rpx;
  padding: 6rpx 12rpx;
  border-radius: 12rpx;
  z-index: 2;
}

.status-badge.active {
  background: #E3F2FD;
}

.status-badge.in_progress {
  background: #FFF3E0;
}

.status-badge.completed {
  background: #E8F5E8;
}

.status-badge.cancelled {
  background: #FFEBEE;
}

.status-text {
  font-size: 20rpx;
  font-weight: 600;
}

.status-badge.active .status-text {
  color: #1976D2;
}

.status-badge.in_progress .status-text {
  color: #F57C00;
}

.status-badge.completed .status-text {
  color: #388E3C;
}

.status-badge.cancelled .status-text {
  color: #D32F2F;
}

/* 订单内容 */
.order-content {
  padding: 20rpx;
  padding-top: 50rpx;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12rpx;
}

.title-section {
  flex: 1;
  margin-right: 16rpx;
}

.order-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #424242;
  line-height: 1.4;
}

.reward-section {
  background: linear-gradient(135deg, #4CAF50, #45A049);
  padding: 8rpx 16rpx;
  border-radius: 12rpx;
}

.reward-amount {
  font-size: 22rpx;
  color: white;
  font-weight: 600;
}

.order-description {
  font-size: 24rpx;
  color: #666;
  line-height: 1.5;
  margin-bottom: 16rpx;
  display: block;
}

/* 订单详情 */
.order-details {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  margin-bottom: 16rpx;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.detail-icon {
  font-size: 20rpx;
  color: #8D6E63;
}

.detail-text {
  font-size: 22rpx;
  color: #666;
}

/* 发布者信息 */
.publisher-info {
  background: #F8F9FA;
  border-radius: 12rpx;
  padding: 16rpx;
  margin-bottom: 16rpx;
}

/* 取消原因（含系统超时取消） */
.cancel-reason {
  margin-bottom: 16rpx;
  padding: 16rpx;
  background: #fff3e0;
  border-radius: 12rpx;
  display: flex;
  flex-wrap: wrap;
}

.cancel-label {
  font-size: 22rpx;
  color: #8d6e63;
  font-weight: 600;
}

.cancel-text {
  font-size: 22rpx;
  color: #5d4037;
  word-break: break-all;
}

.publisher-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.publisher-label {
  font-size: 22rpx;
  color: #424242;
  font-weight: 600;
}

.accept-time {
  font-size: 20rpx;
  color: #666;
}

.publisher-details {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.publisher-avatar {
  width: 50rpx;
  height: 50rpx;
  border-radius: 50%;
  background: #4CAF50;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-text {
  font-size: 20rpx;
  color: white;
  font-weight: 600;
}

.publisher-info-text {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.publisher-name {
  font-size: 24rpx;
  color: #424242;
  font-weight: 600;
}

.publisher-rating {
  font-size: 20rpx;
  color: #FF8F00;
}

.contact-btn {
  background: #FF8F00;
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
  transition: all 0.3s ease;
}

.contact-btn:active {
  transform: scale(0.95);
  background: #F57C00;
}

.contact-text {
  font-size: 20rpx;
  color: white;
  font-weight: 600;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 12rpx;
  justify-content: flex-end;
  margin-bottom: 16rpx;
}

.action-btn {
  padding: 12rpx 20rpx;
  border-radius: 12rpx;
  transition: all 0.3s ease;
}

.action-btn.secondary {
  background: #F5F5F5;
}

.action-btn.primary {
  background: #2196F3;
}

.action-btn.success {
  background: #4CAF50;
}

.action-btn.danger {
  background: #FF5722;
}

.btn-text {
  font-size: 22rpx;
  font-weight: 600;
}

.action-btn.secondary .btn-text {
  color: #666;
}

.action-btn.primary .btn-text,
.action-btn.success .btn-text,
.action-btn.danger .btn-text {
  color: white;
}

.action-btn:active {
  transform: scale(0.95);
}

/* 进度条 */
.progress-section {
  background: #F8F9FA;
  border-radius: 12rpx;
  padding: 16rpx;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.progress-label {
  font-size: 22rpx;
  color: #424242;
  font-weight: 600;
}

.progress-percent {
  font-size: 20rpx;
  color: #FF8F00;
  font-weight: 600;
}

.progress-bar {
  width: 100%;
  height: 8rpx;
  background: #E0E0E0;
  border-radius: 4rpx;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #4CAF50, #45A049);
  border-radius: 4rpx;
  transition: width 0.3s ease;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 40rpx;
  text-align: center;
}

.empty-icon {
  font-size: 80rpx;
  margin-bottom: 24rpx;
  opacity: 0.5;
}

.empty-title {
  font-size: 28rpx;
  color: #424242;
  font-weight: 600;
  margin-bottom: 12rpx;
}

.empty-desc {
  font-size: 24rpx;
  color: #666;
  line-height: 1.5;
  margin-bottom: 32rpx;
}

.empty-action {
  background: #FF8F00;
  padding: 16rpx 32rpx;
  border-radius: 12rpx;
  transition: all 0.3s ease;
}

.empty-action:active {
  transform: scale(0.95);
  background: #F57C00;
}

.action-text {
  font-size: 24rpx;
  color: white;
  font-weight: 600;
}

/* 响应式设计 */
@media screen and (max-width: 750rpx) {
  .main-content {
    padding: 0 16rpx;
  }
  
  .stats-card {
    padding: 16rpx;
  }
  
  .order-content {
    padding: 16rpx;
    padding-top: 40rpx;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 8rpx;
  }
  
  .action-btn {
    width: 100%;
    text-align: center;
  }
}
</style>
