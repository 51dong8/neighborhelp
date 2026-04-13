<template>
  <view class="service-requests-container">
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
        <view class="back-icon">←</view>
        <text class="back-text">返回</text>
      </view>
      <view class="nav-center">
        <text class="page-title">{{ serviceInfo.name }}待接收需求</text>
      </view>
      <view class="nav-right">
        <view class="filter-btn" @click="showFilter">
          <text class="filter-icon">🔍</text>
        </view>
      </view>
    </view>

    <!-- 服务信息卡片 -->
    <view class="service-info-card">
      <view class="service-header">
        <view class="service-icon" :class="serviceInfo.iconClass">
          <text class="service-emoji">{{ serviceInfo.emoji }}</text>
        </view>
        <view class="service-details">
          <text class="service-title">{{ serviceInfo.name }}</text>
          <text class="service-desc">{{ serviceInfo.description }}</text>
        </view>
        <view class="service-stats">
          <text class="stats-number">{{ requests.length }}</text>
          <text class="stats-label">个待接收需求</text>
        </view>
      </view>

      <!-- 顺路带 / 行程广播提示，仅当有行程且类型匹配时显示 -->
      <view 
        v-if="tripBannerVisible" 
        class="trip-banner"
      >
        <view class="trip-banner-left">
          <view class="trip-banner-tag">顺路带进行中</view>
          <text class="trip-banner-main">
            有邻居正要去 {{ activeTrip.destination }} ，
            {{ tripBannerText }}
          </text>
        </view>
        <view class="trip-banner-right">
          <text class="trip-banner-time">剩余约 {{ tripRemainingMinutes }} 分钟</text>
        </view>
      </view>
    </view>

    <!-- 筛选栏 -->
    <view class="filter-bar">
      <view class="filter-item" :class="{ active: currentFilter === 'all' }" @click="setFilter('all')">
        <text>全部</text>
      </view>
      <view class="filter-item" :class="{ active: currentFilter === 'urgent' }" @click="setFilter('urgent')">
        <text>紧急</text>
      </view>
      <view class="filter-item" :class="{ active: currentFilter === 'nearby' }" @click="setFilter('nearby')">
        <text>附近</text>
      </view>
      <view class="filter-item" :class="{ active: currentFilter === 'recent' }" @click="setFilter('recent')">
        <text>最新</text>
      </view>
    </view>

    <!-- 待接收需求列表 -->
    <view class="requests-list">
      <view class="request-card" v-for="request in filteredRequests" :key="request.id" @click="goToRequestDetail(request.id)">
        <!-- 紧急程度标识 -->
        <view class="urgency-badge" :class="request.urgency">
          <text class="urgency-text">{{ getUrgencyText(request.urgency) }}</text>
        </view>
        
        <!-- 待接收需求内容 -->
        <view class="request-content">
          <view class="request-header">
            <view class="title-section">
              <text class="request-title">{{ request.title }}</text>
            </view>
            <view class="reward-info">
              <text class="reward-amount">¥{{ request.reward }}</text>
            </view>
          </view>
          
          <text class="request-description">{{ request.description }}</text>
          
          <!-- 待接收需求详情 -->
          <view class="request-details">
            <view class="detail-item">
              <text class="detail-icon">⏰</text>
              <text class="detail-text">{{ request.expectedTime }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-icon">📍</text>
              <text class="detail-text">{{ request.location }}</text>
            </view>
            <view class="detail-item" v-if="request.itemInfo">
              <text class="detail-icon">📦</text>
              <text class="detail-text">{{ request.itemInfo }}</text>
            </view>
          </view>
          
          <!-- 发布者信息 -->
          <view class="publisher-info">
            <view class="publisher-avatar">
              <text class="avatar-text">{{ request.publisher.username.charAt(0) }}</text>
            </view>
            <view class="publisher-details">
              <text class="publisher-name">{{ request.publisher.username }}</text>
              <view class="publisher-rating">
                <text class="rating-stars">⭐⭐⭐⭐⭐</text>
                <text class="rating-score">{{ request.publisher.rating }}</text>
              </view>
            </view>
            <view class="request-status" :class="request.status">
              <text class="status-text">{{ getStatusText(request.status) }}</text>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 加载更多 -->
      <view class="load-more" v-if="hasMore">
        <text class="load-more-text">加载更多...</text>
      </view>
      
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getRequests } from '@/api/request'
import { acceptOrder as apiAcceptOrder } from '@/api/order'

// 响应式数据
const serviceType = ref('')
const currentFilter = ref('all')
const hasMore = ref(true)

// 顺路带 / 行程广播（从首页本地状态读取，不改接口）
const activeTrip = ref(null) // { destination, type, expireAt }
const tripRemainingMinutes = ref(0)

// 服务信息映射
const serviceInfoMap = {
  package: {
    name: '帮我取快递',
    emoji: '📦',
    iconClass: 'package-icon',
    description: '帮忙代收快递包裹'
  },
  pet: {
    name: '帮我照顾宠物',
    emoji: '🐕',
    iconClass: 'pet-icon',
    description: '照顾宠物日常护理'
  },
  shopping: {
    name: '帮我买菜',
    emoji: '🛒',
    iconClass: 'shopping-icon',
    description: '帮忙购买生活用品'
  },
  repair: {
    name: '帮我家电维修',
    emoji: '🔧',
    iconClass: 'repair-icon',
    description: '家电故障维修服务'
  },
  cleaning: {
    name: '帮我家清洁',
    emoji: '🧹',
    iconClass: 'cleaning-icon',
    description: '家庭清洁整理服务'
  },
  cooking: {
    name: '帮我做美食',
    emoji: '🍳',
    iconClass: 'cooking-icon',
    description: '代做各种美食料理'
  }
}

// 计算属性
const serviceInfo = computed(() => {
  return serviceInfoMap[serviceType.value] || serviceInfoMap.package
})

// 顺路带 Banner 是否展示
const tripBannerVisible = computed(() => {
  if (!activeTrip.value) return false
  // 行程类型与当前服务类型的简单映射
  if (activeTrip.value.type === 'parcel' && serviceType.value === 'package') return true
  if (activeTrip.value.type === 'market' && serviceType.value === 'shopping') return true
  if (activeTrip.value.type === 'vegetable' && serviceType.value === 'shopping') return true
  return false
})

const tripBannerText = computed(() => {
  if (!activeTrip.value) return ''
  if (activeTrip.value.type === 'parcel') {
    return '快递代取更适合走“顺路带”，可优先沟通是否同路'
  }
  if (activeTrip.value.type === 'market' || activeTrip.value.type === 'vegetable') {
    return '买菜/超市相关需求，可优先发布或接单，节省大家时间'
  }
  return '可以考虑是否将当前需求改为顺路带模式'
})

// 当前显示的需求列表（从后端获取）
const requests = ref([])

// 筛选后的需求列表
const filteredRequests = computed(() => {
  let filtered = requests.value
  
  switch (currentFilter.value) {
    case 'urgent':
      filtered = filtered.filter(req => req.urgency === 'urgent' || req.urgency === 'emergency')
      break
    case 'nearby':
      // 这里可以根据位置筛选，暂时显示全部
      break
    case 'recent':
      // 按时间排序，最新的在前
      filtered = [...filtered].sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
      break
  }
  
  return filtered
})

// 生命周期
onMounted(() => {
  // 获取页面参数
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options
  serviceType.value = options.type || 'package'
  
  // 从后端加载该类型的需求
  loadRequestsByType()

  // 恢复首页发起的顺路带行程（若未过期）
  restoreActiveTrip()
})

// 根据服务类型从后端加载需求数据
const loadRequestsByType = async () => {
  try {
    const res = await getRequests({
      serviceType: serviceType.value,
      status: 'active',
      page: 1,
      limit: 50,
      sort: 'latest'
    })
    // 后端返回 Result<PageVO<RequestVO>>
    const pageData = res.data || res?.data?.data || {}
    const list = pageData.records || pageData.list || []
    // 统一字段名，兼容原有模板
    requests.value = list.map(r => ({
      ...r,
      reward: r.rewardAmount,
      publisher: r.publisher || {}
    }))
  } catch (e) {
    console.error('加载服务类型需求失败', e)
    uni.showToast({
      title: '加载需求失败',
      icon: 'none'
    })
  }
}

// 恢复顺路带行程
const restoreActiveTrip = () => {
  try {
    const saved = uni.getStorageSync('activeTripBroadcast')
    if (!saved) return
    const trip = JSON.parse(saved)
    if (trip.expireAt && trip.expireAt > Date.now()) {
      activeTrip.value = trip
      updateTripRemaining()
    } else {
      uni.removeStorageSync('activeTripBroadcast')
    }
  } catch (e) {
    console.error('恢复顺路带行程失败', e)
  }
}

// 只在进入页面时计算一次剩余时间（无需长轮询）
const updateTripRemaining = () => {
  if (!activeTrip.value) {
    tripRemainingMinutes.value = 0
    return
  }
  const diff = activeTrip.value.expireAt - Date.now()
  if (diff <= 0) {
    activeTrip.value = null
    tripRemainingMinutes.value = 0
    return
  }
  tripRemainingMinutes.value = Math.max(1, Math.round(diff / 60000))
}

// 方法
const goBack = () => {
  uni.navigateBack()
}

const showFilter = () => {
  uni.showActionSheet({
    itemList: ['按时间排序', '按距离排序', '按紧急程度排序'],
    success: (res) => {
      const filters = ['按时间排序', '按距离排序', '按紧急程度排序']
      uni.showToast({
        title: filters[res.tapIndex],
        icon: 'none'
      })
    }
  })
}

const setFilter = (filter) => {
  currentFilter.value = filter
}

const getUrgencyText = (urgency) => {
  const urgencyMap = {
    low: '不紧急',
    normal: '一般',
    urgent: '紧急',
    emergency: '非常紧急'
  }
  return urgencyMap[urgency] || '一般'
}

const getStatusText = (status) => {
  const statusMap = {
    available: '可接收',
    in_progress: '进行中',
    completed: '已完成'
  }
  return statusMap[status] || '可接收'
}

const goToRequestDetail = (requestId) => {
  const request = requests.value.find(req => req.id === requestId)
  if (!request) {
    uni.showToast({
      title: '未找到该需求',
      icon: 'none'
    })
    return
  }

  // 业务逻辑校验：不能接自己发布的需求
  try {
    const userInfo = uni.getStorageSync('userInfo')
    const currentUser = typeof userInfo === 'string' ? JSON.parse(userInfo) : userInfo
    const publisherId = request.publisher?.userId || request.publisher?.id

    if (currentUser && publisherId && String(currentUser.userId) === String(publisherId)) {
      uni.showToast({
        title: '不能接自己发布的需求',
        icon: 'none'
      })
      return
    }
  } catch (e) {
    console.warn('解析当前用户信息失败', e)
  }

  uni.showModal({
    title: '接收需求确认',
    content: '确定要接这个单吗？',
    success: async (res) => {
      if (!res.confirm) return

      try {
        uni.showLoading({ title: '正在接单...' })
        // 调用后端接单接口，创建真实订单
        await apiAcceptOrder({
          requestId,
          message: '我可以帮你完成这个需求'
        })

        // 更新当前列表中的需求状态为进行中
        const req = requests.value.find(r => r.id === requestId)
        if (req) {
          req.status = 'in_progress'
        }

        uni.showToast({
          title: '接单成功！',
          icon: 'success'
        })

        // 可选：跳转到“我的订单”页面查看
        setTimeout(() => {
          uni.switchTab({
            url: '/pages/my-orders/my-orders'
          })
        }, 800)
      } catch (e) {
        console.error('接单失败', e)
        uni.showToast({
          title: e?.message || '接单失败，请稍后重试',
          icon: 'none'
        })
      } finally {
        uni.hideLoading()
      }
    }
  })
}

const loadMore = () => {
  if (hasMore.value) {
    uni.showToast({
      title: '加载更多',
      icon: 'none'
    })
    // 这里可以加载更多数据
  }
}
</script>

<style scoped>
.service-requests-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #FFF8E1 0%, #FFECB3 100%);
  display: flex;
  flex-direction: column;
  position: relative;
}

/* 状态栏 */
.status-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10rpx 30rpx;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10rpx);
  border-bottom: 1rpx solid rgba(255, 255, 255, 0.2);
}

.time {
  font-size: 28rpx;
  font-weight: 600;
  color: #8D6E63;
}

.status-icons {
  display: flex;
  gap: 12rpx;
  font-size: 24rpx;
}

/* 顶部导航栏 */
.top-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 30rpx;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20rpx);
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.05);
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 8rpx;
  border-radius: 8rpx;
  transition: all 0.3s ease;
}

.nav-left:active {
  background: rgba(0, 0, 0, 0.05);
}

.back-icon {
  font-size: 32rpx;
  color: #8D6E63;
  font-weight: bold;
}

.back-text {
  font-size: 28rpx;
  color: #8D6E63;
}

.nav-center {
  flex: 1;
  text-align: center;
}

.page-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #8D6E63;
}

.nav-right {
  width: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.filter-btn {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #FF8F00, #FFB74D);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 12rpx rgba(255, 143, 0, 0.3);
  transition: all 0.3s ease;
}

.filter-btn:active {
  transform: scale(0.95);
}

.filter-icon {
  font-size: 28rpx;
  color: white;
}

/* 服务信息卡片 */
.service-info-card {
  margin: 20rpx 24rpx;
  background: white;
  border-radius: 20rpx;
  box-shadow: 0 6rpx 24rpx rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.service-header {
  display: flex;
  align-items: center;
  padding: 24rpx;
  gap: 16rpx;
}

.service-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.15);
}

.service-emoji {
  font-size: 40rpx;
}

.service-details {
  flex: 1;
}

.service-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #8D6E63;
  margin-bottom: 8rpx;
  display: block;
}

.service-desc {
  font-size: 24rpx;
  color: #A1887F;
  display: block;
}

.service-stats {
  text-align: center;
}

.stats-number {
  font-size: 36rpx;
  font-weight: 700;
  color: #FF8F00;
  display: block;
}

.stats-label {
  font-size: 20rpx;
  color: #A1887F;
  display: block;
}

/* 顺路带提示条 */
.trip-banner {
  margin: 0 16rpx 16rpx;
  padding: 12rpx 14rpx;
  border-radius: 14rpx;
  background: linear-gradient(90deg, #FFF9C4, #FFE082);
  border: 2rpx solid #FFCA28;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
}

.trip-banner-left {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
  flex: 1;
}

.trip-banner-tag {
  align-self: flex-start;
  padding: 4rpx 10rpx;
  border-radius: 999rpx;
  background: rgba(255, 160, 0, 0.15);
  color: #F57C00;
  font-size: 20rpx;
  font-weight: 600;
}

.trip-banner-main {
  font-size: 22rpx;
  color: #5D4037;
  line-height: 1.5;
}

.trip-banner-right {
  text-align: right;
}

.trip-banner-time {
  font-size: 20rpx;
  color: #6D4C41;
  font-weight: 500;
}

/* 服务图标样式 */
.package-icon {
  background: linear-gradient(135deg, #FF8A65, #FFB74D);
}

.pet-icon {
  background: linear-gradient(135deg, #FFD54F, #FFECB3);
}

.shopping-icon {
  background: linear-gradient(135deg, #81C784, #A5D6A7);
}

.repair-icon {
  background: linear-gradient(135deg, #64B5F6, #90CAF9);
}

.cleaning-icon {
  background: linear-gradient(135deg, #BA68C8, #CE93D8);
}

.cooking-icon {
  background: linear-gradient(135deg, #FF7043, #FFAB91);
}

/* 筛选栏 */
.filter-bar {
  display: flex;
  padding: 0 24rpx;
  margin-bottom: 20rpx;
  gap: 12rpx;
}

.filter-item {
  padding: 12rpx 24rpx;
  background: white;
  border-radius: 20rpx;
  border: 2rpx solid #E0E0E0;
  transition: all 0.3s ease;
  text-align: center;
}

.filter-item.active {
  background: linear-gradient(135deg, #4CAF50, #66BB6A);
  border-color: #4CAF50;
  color: white;
}

.filter-item text {
  font-size: 24rpx;
  font-weight: 500;
  color: #8D6E63;
}

.filter-item.active text {
  color: white;
}

/* 需求列表 */
.requests-list {
  flex: 1;
  padding: 0 20rpx;
  overflow: hidden;
}

.request-card {
  position: relative;
  background: white;
  border-radius: 16rpx;
  margin-bottom: 12rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
  overflow: hidden;
  transition: all 0.3s ease;
}

.request-card:active {
  transform: translateY(-2rpx);
  box-shadow: 0 8rpx 28rpx rgba(0, 0, 0, 0.12);
}

/* 紧急程度标识 */
.urgency-badge {
  position: absolute;
  top: 8rpx;
  left: 16rpx;
  padding: 6rpx 12rpx;
  border-radius: 12rpx;
  z-index: 2;
}

.urgency-badge.low {
  background: white;
}

.urgency-badge.normal {
  background: white;
}

.urgency-badge.urgent {
  background: white;
}

.urgency-badge.emergency {
  background: white;
}

.urgency-text {
  font-size: 20rpx;
  font-weight: 600;
  color: #8D6E63;
}

.urgency-badge.urgent .urgency-text {
  color: #FF9800;
}

.urgency-badge.emergency .urgency-text {
  color: #F44336;
}

/* 需求内容 */
.request-content {
  padding: 24rpx;
  padding-top: 50rpx; /* 为左上角紧急程度标签留出空间 */
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
  color: #8D6E63;
  line-height: 1.4;
}

.reward-info {
  text-align: right;
}

.reward-amount {
  font-size: 24rpx;
  font-weight: 600;
  color: #FF8F00;
  background: rgba(255, 143, 0, 0.1);
  padding: 6rpx 12rpx;
  border-radius: 12rpx;
}

.request-description {
  font-size: 24rpx;
  color: #A1887F;
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
  width: 24rpx;
  text-align: center;
}

.detail-text {
  font-size: 22rpx;
  color: #666;
}

/* 发布者信息 */
.publisher-info {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid #F0F0F0;
}

.publisher-avatar {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #4CAF50, #66BB6A);
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-text {
  font-size: 24rpx;
  color: white;
  font-weight: 600;
}

.publisher-details {
  flex: 1;
}

.publisher-name {
  font-size: 24rpx;
  font-weight: 600;
  color: #8D6E63;
  margin-bottom: 4rpx;
  display: block;
}

.publisher-rating {
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.rating-stars {
  font-size: 18rpx;
}

.rating-score {
  font-size: 20rpx;
  color: #FF8F00;
  font-weight: 500;
}

.request-status {
  padding: 8rpx 16rpx;
  border-radius: 16rpx;
  font-size: 20rpx;
  font-weight: 500;
}

.request-status.available {
  background: rgba(76, 175, 80, 0.1);
  color: #4CAF50;
}

.request-status.in_progress {
  background: rgba(255, 152, 0, 0.1);
  color: #FF9800;
}

.request-status.completed {
  background: rgba(158, 158, 158, 0.1);
  color: #9E9E9E;
}

.status-text {
  font-size: 20rpx;
}

/* 加载更多 */
.load-more {
  text-align: center;
  padding: 20rpx;
}

.load-more-text {
  font-size: 24rpx;
  color: #A1887F;
}

/* 底部间距 */
.bottom-spacing {
  height: 30rpx;
}

/* 响应式设计 */
@media screen and (max-width: 750rpx) {
  .requests-list {
    padding: 0 20rpx;
  }
  
  .service-info-card {
    margin: 20rpx;
  }
  
  .filter-bar {
    padding: 0 20rpx;
  }
  
  .request-content {
    padding: 20rpx;
  }
}

@media screen and (max-width: 600rpx) {
  .service-header {
    padding: 20rpx;
  }
  
  .service-icon {
    width: 60rpx;
    height: 60rpx;
  }
  
  .service-emoji {
    font-size: 32rpx;
  }
  
  .service-title {
    font-size: 28rpx;
  }
  
  .request-title {
    font-size: 26rpx;
  }
  
  .request-description {
    font-size: 22rpx;
  }
}
</style>
