<template>
  <view class="my-requests-container">
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
        <text class="page-title">我的需求</text>
      </view>
      <view class="nav-right">
        <view class="add-btn" @click="goToPublishRequest">
          <text class="add-icon">+</text>
        </view>
      </view>
    </view>

    <!-- 主内容区域 -->
    <view class="main-content">
      <!-- 统计信息 -->
      <view class="stats-section">
        <view class="stats-card">
          <view class="stat-item">
            <text class="stat-number">{{ totalRequests }}</text>
            <text class="stat-label">总需求</text>
          </view>
          <view class="stat-item">
            <text class="stat-number">{{ activeRequests }}</text>
            <text class="stat-label">进行中</text>
          </view>
          <view class="stat-item">
            <text class="stat-number">{{ completedRequests }}</text>
            <text class="stat-label">已完成</text>
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

      <!-- 需求列表 -->
      <view class="requests-list">
        <view 
          class="request-card" 
          v-for="request in filteredRequests" 
          :key="request.id"
          @click="goToRequestDetail(request.id)"
        >
          <!-- 状态标识 -->
          <view class="status-badge" :class="request.status">
            <text class="status-text">{{ getStatusText(request.status) }}</text>
          </view>

          <!-- 需求内容 -->
          <view class="request-content">
            <view class="request-header">
              <view class="title-section">
                <text class="request-title">{{ request.title }}</text>
              </view>
              <view class="service-type">
                <text class="service-text">{{ getServiceTypeName(request.serviceType) }}</text>
              </view>
            </view>

            <text class="request-description">{{ request.description }}</text>

            <!-- 需求详情 -->
            <view class="request-details">
              <view class="detail-item">
                <text class="detail-icon">⏰</text>
                <text class="detail-text">{{ request.expectedTime }}</text>
              </view>
              <view class="detail-item">
                <text class="detail-icon">📍</text>
                <text class="detail-text">{{ request.location }}</text>
              </view>
              <view class="detail-item" v-if="request.rewardAmount > 0">
                <text class="detail-icon">💰</text>
                <text class="detail-text">¥{{ request.rewardAmount }}</text>
              </view>
            </view>

            <!-- 接单信息 -->
            <view class="accepted-info" v-if="request.acceptedBy">
              <view class="accepted-user">
                <text class="user-avatar">👤</text>
                <text class="user-name">{{ request.acceptedBy.username }}</text>
                <text class="user-rating">⭐ {{ request.acceptedBy.rating }}</text>
              </view>
              <view class="accept-time">
                <text class="time-text">接单时间：{{ request.acceptedAt }}</text>
              </view>
            </view>

            <!-- 操作按钮 -->
            <view class="action-buttons">
              <view class="action-btn secondary" @click.stop="viewRequestDetail(request.id)">
                <text class="btn-text">查看详情</text>
              </view>
              <view 
                class="action-btn primary" 
                v-if="request.status === 'active'"
                @click.stop="cancelRequest(request.id)"
              >
                <text class="btn-text">取消需求</text>
              </view>
              <view 
                class="action-btn success" 
                v-if="request.status === 'active' && request.acceptedBy"
                @click.stop="completeRequest(request.id)"
              >
                <text class="btn-text">完成需求</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 空状态 -->
        <view v-if="filteredRequests.length === 0" class="empty-state">
          <view class="empty-icon">📝</view>
          <text class="empty-title">暂无需求</text>
          <text class="empty-desc">您还没有发布过需求，快去发布一个吧！</text>
          <view class="empty-action" @click="goToPublishRequest">
            <text class="action-text">发布需求</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getUserRequests } from '@/api/user'
import { deleteRequest } from '@/api/request'

// 响应式数据
const currentFilter = ref('all')
const requests = ref([])
const isLoading = ref(false)

// 计算属性
const totalRequests = computed(() => requests.value.length)
const activeRequests = computed(() => requests.value.filter(r => r.status === 'active').length)
const completedRequests = computed(() => requests.value.filter(r => r.status === 'completed').length)

const filteredRequests = computed(() => {
  if (currentFilter.value === 'all') {
    return requests.value
  }
  return requests.value.filter(request => request.status === currentFilter.value)
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

const goToPublishRequest = () => {
  uni.switchTab({
    url: '/pages/request/request'
  })
}

const setFilter = (filter) => {
  currentFilter.value = filter
}

const getStatusText = (status) => {
  const statusMap = {
    active: '进行中',
    completed: '已完成',
    cancelled: '已取消'
  }
  return statusMap[status] || '未知'
}

const getServiceTypeName = (serviceType) => {
  return serviceTypeMap[serviceType] || '未知服务'
}

const goToRequestDetail = (requestId) => {
  uni.navigateTo({
    url: `/pages/request-detail/request-detail?id=${requestId}`
  })
}

const viewRequestDetail = (requestId) => {
  uni.navigateTo({
    url: `/pages/request-detail/request-detail?id=${requestId}`
  })
}

// 直接在列表中取消需求（后端同步删除记录）
const cancelRequest = async (requestId) => {
  // 二次确认
  const res = await uni.showModal({
    title: '确认删除需求',
    content: '删除后该需求将从列表中移除，且无法恢复，确定要删除吗？',
    confirmText: '确定删除',
    cancelText: '再想想'
  })

  if (!res || !res.confirm) {
    return
  }

  try {
    uni.showLoading({ title: '正在删除...' })
    // 调用后端删除接口，物理删除数据库中的记录
    await deleteRequest(requestId)

    // 本地列表同步删除
    requests.value = requests.value.map(r =>
      r.id === requestId ? null : r
    )
    // 过滤掉已删除的 null
    requests.value = requests.value.filter(Boolean)

    uni.showToast({
      title: '删除成功',
      icon: 'success'
    })
  } catch (error) {
    console.error('删除需求失败:', error)
    uni.showToast({
      title: error?.message || '删除失败，请稍后重试',
      icon: 'none'
    })
  } finally {
    uni.hideLoading()
  }
}

// 完成需求操作仍跳转详情页，由详情页统一处理
const completeRequest = (requestId) => {
  uni.navigateTo({
    url: `/pages/request-detail/request-detail?id=${requestId}`
  })
}

// 从后端加载当前用户发布的需求
const loadRequests = async () => {
  isLoading.value = true
  try {
    const res = await getUserRequests({
      status: currentFilter.value === 'all' ? undefined : currentFilter.value,
      page: 1,
      limit: 50
    })
    // 后端返回 Result<PageVO<RequestVO>>
    const pageData = res.data || res?.data?.data || {}
    requests.value = pageData.records || pageData.list || []
  } catch (error) {
    console.error('加载需求失败:', error)
    uni.showToast({
      title: '加载需求失败',
      icon: 'none'
    })
  } finally {
    isLoading.value = false
  }
}

// 生命周期
onMounted(() => {
  loadRequests()
})
</script>

<style scoped>
/* 容器 */
.my-requests-container {
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

.back-btn, .add-btn {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background: #F5F5F5;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.back-btn:active, .add-btn:active {
  background: #E0E0E0;
  transform: scale(0.95);
}

.back-icon, .add-icon {
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
  font-size: 32rpx;
  font-weight: 700;
  color: #FF8F00;
}

.stat-label {
  font-size: 22rpx;
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

/* 需求列表 */
.requests-list {
  flex: 1;
  overflow: hidden;
}

.request-card {
  position: relative;
  background: white;
  border-radius: 16rpx;
  margin-bottom: 12rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
  overflow: hidden;
  transition: all 0.3s ease;
}

.request-card:active {
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

.status-badge.completed .status-text {
  color: #388E3C;
}

.status-badge.cancelled .status-text {
  color: #D32F2F;
}

/* 需求内容 */
.request-content {
  padding: 20rpx;
  padding-top: 50rpx;
}

.request-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12rpx;
}

.title-section {
  flex: 1;
  margin-right: 16rpx;
}

.request-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #424242;
  line-height: 1.4;
}

.service-type {
  background: #F5F5F5;
  padding: 6rpx 12rpx;
  border-radius: 8rpx;
}

.service-text {
  font-size: 20rpx;
  color: #666;
}

.request-description {
  font-size: 24rpx;
  color: #666;
  line-height: 1.5;
  margin-bottom: 16rpx;
  display: block;
}

/* 需求详情 */
.request-details {
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

/* 接单信息 */
.accepted-info {
  background: #F8F9FA;
  border-radius: 12rpx;
  padding: 16rpx;
  margin-bottom: 16rpx;
}

.accepted-user {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 8rpx;
}

.user-avatar {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  background: #4CAF50;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20rpx;
  color: white;
}

.user-name {
  font-size: 24rpx;
  color: #424242;
  font-weight: 600;
}

.user-rating {
  font-size: 20rpx;
  color: #FF8F00;
}

.accept-time {
  margin-left: 52rpx;
}

.time-text {
  font-size: 20rpx;
  color: #666;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 12rpx;
  justify-content: flex-end;
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
  background: #FF5722;
}

.action-btn.success {
  background: #4CAF50;
}

.btn-text {
  font-size: 22rpx;
  font-weight: 600;
}

.action-btn.secondary .btn-text {
  color: #666;
}

.action-btn.primary .btn-text,
.action-btn.success .btn-text {
  color: white;
}

.action-btn:active {
  transform: scale(0.95);
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
  
  .request-content {
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
