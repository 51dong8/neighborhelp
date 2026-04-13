<template>
  <view class="my-activities-container">
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
        <text class="back-icon">←</text>
      </view>
      <view class="nav-center">
        <text class="page-title">我的活动</text>
      </view>
      <view class="nav-right">
        <view class="filter-btn" @click="showFilter">
          <text class="filter-icon">🔍</text>
        </view>
      </view>
    </view>

    <!-- 主内容区域 -->
    <view class="main-content">
      <!-- 活动统计 -->
      <view class="stats-card">
        <view class="stats-content">
          <view class="stat-item">
            <text class="stat-number">{{ myActivities.length }}</text>
            <text class="stat-label">已报名</text>
          </view>
          <view class="stat-item">
            <text class="stat-number">{{ completedActivities.length }}</text>
            <text class="stat-label">已完成</text>
          </view>
          <view class="stat-item">
            <text class="stat-number">{{ upcomingActivities.length }}</text>
            <text class="stat-label">即将开始</text>
          </view>
        </view>
      </view>

      <!-- 活动列表 -->
      <view class="activities-section">
        <view class="section-header">
          <text class="section-title">我的活动</text>
          <view class="sort-btn" @click="showSortOptions">
            <text class="sort-text">排序</text>
            <text class="sort-icon">↓</text>
          </view>
        </view>

        <!-- 空状态 -->
        <view v-if="myActivities.length === 0" class="empty-state">
          <view class="empty-icon">🎉</view>
          <text class="empty-title">还没有报名任何活动</text>
          <text class="empty-desc">去首页看看有什么有趣的活动吧</text>
          <view class="empty-action" @click="goToHome">
            <text class="action-text">去首页</text>
          </view>
        </view>

        <!-- 活动列表 -->
        <view v-else class="activities-list">
          <view 
            class="activity-item" 
            v-for="activity in myActivities" 
            :key="activity.id"
            @click="goToActivityDetail(activity.id)"
          >
            <view class="activity-image" :class="activity.type">
              <view class="activity-badge" :class="activity.status">
                {{ getStatusText(activity.status) }}
              </view>
            </view>
            <view class="activity-info">
              <text class="activity-title">{{ activity.title }}</text>
              <text class="activity-desc">{{ activity.desc }}</text>
              <text class="activity-time">{{ activity.time }}</text>
              <text class="activity-location">📍 {{ activity.location }}</text>
            </view>
            <view class="activity-actions">
              <view 
                class="action-btn" 
                :class="activity.status"
                @click.stop="handleActivityAction(activity)"
              >
                {{ getActionText(activity.status) }}
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

// 响应式数据
const myActivities = ref([])

// 计算属性
const completedActivities = computed(() => {
  return myActivities.value.filter(activity => activity.status === 'completed')
})

const upcomingActivities = computed(() => {
  return myActivities.value.filter(activity => activity.status === 'upcoming')
})

// 生命周期
onMounted(() => {
  loadMyActivities()
})

// 方法
const loadMyActivities = () => {
  // 从本地存储加载已报名的活动
  try {
    const savedActivities = uni.getStorageSync('myActivities')
    if (savedActivities) {
      myActivities.value = JSON.parse(savedActivities)
    }
  } catch (error) {
    console.error('加载活动数据失败:', error)
  }
}

const goBack = () => {
  uni.navigateBack()
}

const showFilter = () => {
  uni.showActionSheet({
    itemList: ['全部活动', '即将开始', '进行中', '已完成'],
    success: (res) => {
      const filters = ['全部活动', '即将开始', '进行中', '已完成']
      uni.showToast({
        title: `筛选：${filters[res.tapIndex]}`,
        icon: 'none'
      })
    }
  })
}

const showSortOptions = () => {
  uni.showActionSheet({
    itemList: ['按时间排序', '按状态排序', '按活动类型排序'],
    success: (res) => {
      const sorts = ['按时间排序', '按状态排序', '按活动类型排序']
      uni.showToast({
        title: `排序：${sorts[res.tapIndex]}`,
        icon: 'none'
      })
    }
  })
}

const goToHome = () => {
  uni.switchTab({
    url: '/pages/home/home'
  })
}

const goToActivityDetail = (activityId) => {
  uni.showToast({
    title: '活动详情',
    icon: 'none'
  })
}

const getStatusText = (status) => {
  const statusMap = {
    'upcoming': '即将开始',
    'ongoing': '进行中',
    'completed': '已完成',
    'cancelled': '已取消'
  }
  return statusMap[status] || '未知状态'
}

const getActionText = (status) => {
  const actionMap = {
    'upcoming': '查看详情',
    'ongoing': '参与中',
    'completed': '已完成',
    'cancelled': '已取消'
  }
  return actionMap[status] || '查看'
}

const handleActivityAction = (activity) => {
  switch (activity.status) {
    case 'upcoming':
      uni.showModal({
        title: '取消报名',
        content: '确定要取消报名这个活动吗？',
        success: (res) => {
          if (res.confirm) {
            cancelActivity(activity.id)
          }
        }
      })
      break
    case 'ongoing':
      uni.showToast({
        title: '活动进行中',
        icon: 'none'
      })
      break
    case 'completed':
      uni.showToast({
        title: '活动已完成',
        icon: 'none'
      })
      break
    default:
      goToActivityDetail(activity.id)
  }
}

const cancelActivity = (activityId) => {
  // 从本地存储中移除活动
  myActivities.value = myActivities.value.filter(activity => activity.id !== activityId)
  
  try {
    uni.setStorageSync('myActivities', JSON.stringify(myActivities.value))
    uni.showToast({
      title: '取消报名成功',
      icon: 'success'
    })
  } catch (error) {
    console.error('保存活动数据失败:', error)
    uni.showToast({
      title: '操作失败',
      icon: 'error'
    })
  }
}
</script>

<style scoped>
.my-activities-container {
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
  width: 80rpx;
  display: flex;
  justify-content: flex-start;
}

.back-icon {
  font-size: 32rpx;
  color: #8D6E63;
  font-weight: 600;
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
  justify-content: flex-end;
}

.filter-btn {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background: #F5F5F5;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.filter-btn:active {
  background: #E0E0E0;
  transform: scale(0.95);
}

.filter-icon {
  font-size: 28rpx;
}

/* 主内容区域 */
.main-content {
  flex: 1;
  padding: 0 20rpx;
  padding-bottom: 20rpx;
  overflow: hidden;
}

/* 统计卡片 */
.stats-card {
  background: white;
  border-radius: 16rpx;
  padding: 20rpx;
  margin: 12rpx 0;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
}

.stats-content {
  display: flex;
  justify-content: space-around;
  align-items: center;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}

.stat-number {
  font-size: 36rpx;
  color: #4CAF50;
  font-weight: 700;
}

.stat-label {
  font-size: 22rpx;
  color: #666;
}

/* 活动区域 */
.activities-section {
  margin: 16rpx 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #8D6E63;
}

.sort-btn {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 8rpx 16rpx;
  background: rgba(76, 175, 80, 0.1);
  border-radius: 16rpx;
  transition: all 0.3s ease;
}

.sort-btn:active {
  background: rgba(76, 175, 80, 0.2);
}

.sort-text {
  font-size: 22rpx;
  color: #4CAF50;
  font-weight: 500;
}

.sort-icon {
  font-size: 20rpx;
  color: #4CAF50;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 40rpx;
  background: white;
  border-radius: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
}

.empty-icon {
  font-size: 80rpx;
  margin-bottom: 20rpx;
}

.empty-title {
  font-size: 28rpx;
  color: #8D6E63;
  font-weight: 600;
  margin-bottom: 12rpx;
}

.empty-desc {
  font-size: 24rpx;
  color: #A1887F;
  margin-bottom: 32rpx;
  text-align: center;
}

.empty-action {
  padding: 16rpx 32rpx;
  background: linear-gradient(135deg, #4CAF50, #66BB6A);
  border-radius: 24rpx;
  transition: all 0.3s ease;
}

.empty-action:active {
  transform: scale(0.95);
}

.action-text {
  font-size: 24rpx;
  color: white;
  font-weight: 500;
}

/* 活动列表 */
.activities-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.activity-item {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background: white;
  border-radius: 16rpx;
  box-shadow: 0 3rpx 16rpx rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  gap: 16rpx;
}

.activity-item:active {
  transform: translateY(-2rpx);
  box-shadow: 0 6rpx 20rpx rgba(0, 0, 0, 0.12);
}

.activity-image {
  width: 80rpx;
  height: 80rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  position: relative;
  overflow: hidden;
}

.activity-image.dinner {
  background: linear-gradient(135deg, #E1F5FE, #B3E5FC);
}

.activity-image.volunteer {
  background: linear-gradient(135deg, #E8F5E8, #C8E6C9);
}

.activity-image.sports {
  background: linear-gradient(135deg, #FFF3E0, #FFE0B2);
}

.activity-badge {
  position: absolute;
  top: 4rpx;
  right: 4rpx;
  padding: 2rpx 6rpx;
  border-radius: 8rpx;
  font-size: 16rpx;
  font-weight: 500;
}

.activity-badge.upcoming {
  background: rgba(255, 193, 7, 0.9);
  color: white;
}

.activity-badge.ongoing {
  background: rgba(76, 175, 80, 0.9);
  color: white;
}

.activity-badge.completed {
  background: rgba(158, 158, 158, 0.9);
  color: white;
}

.activity-badge.cancelled {
  background: rgba(244, 67, 54, 0.9);
  color: white;
}

.activity-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.activity-title {
  font-size: 26rpx;
  font-weight: 600;
  color: #8D6E63;
}

.activity-desc {
  font-size: 22rpx;
  color: #A1887F;
}

.activity-time {
  font-size: 20rpx;
  color: #FF8F00;
  font-weight: 500;
}

.activity-location {
  font-size: 20rpx;
  color: #999;
}

.activity-actions {
  display: flex;
  align-items: center;
}

.action-btn {
  padding: 12rpx 20rpx;
  border-radius: 20rpx;
  font-size: 22rpx;
  font-weight: 500;
  transition: all 0.3s ease;
}

.action-btn.upcoming {
  background: rgba(244, 67, 54, 0.1);
  color: #F44336;
}

.action-btn.ongoing {
  background: rgba(76, 175, 80, 0.1);
  color: #4CAF50;
}

.action-btn.completed {
  background: rgba(158, 158, 158, 0.1);
  color: #9E9E9E;
}

.action-btn:active {
  transform: scale(0.95);
}

/* 底部间距 */
.bottom-spacing {
  height: 30rpx;
}
</style>
