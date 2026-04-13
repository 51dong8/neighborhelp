<template>
  <view class="profile-container">
    <view class="status-bar">
      <view class="time">9:41</view>
      <view class="status-icons">
        <view class="signal-icon">📶</view>
        <view class="wifi-icon">📶</view>
        <view class="battery-icon">🔋</view>
      </view>
    </view>

    <view class="top-nav">
      <view class="nav-left">
        <view class="mini-avatar-btn" @click="changeAvatar">
          <image v-if="avatarUrl" :src="avatarUrl" class="mini-avatar-image" mode="aspectFill"></image>
          <text v-else class="mini-avatar-text">{{ avatarText }}</text>
        </view>
      </view>
      <view class="nav-center">
        <text class="page-title">我的</text>
      </view>
      <view class="nav-right">
        <view class="settings-btn" @click="goToSettings">
          <text class="settings-icon">⚙️</text>
        </view>
      </view>
    </view>

    <view class="main-content">
      
      <view class="user-card">
        <view class="user-avatar" @click="changeAvatar">
          <image v-if="avatarUrl" :src="avatarUrl" class="avatar-image" mode="aspectFill"></image>
          <text v-else class="avatar-text">{{ avatarText }}</text>
          
          <view class="avatar-badge">VIP</view>
          <view class="avatar-edit">
            <text class="edit-icon">📷</text>
          </view>
        </view>
        <view class="user-info">
          <view class="user-header">
            <text class="user-name">{{ userName }}</text>
            <view class="user-level" @click="showLevelDetail">
              <text class="level-icon">🏆</text>
              <text class="level-text">Lv.{{ userLevel }}</text>
            </view>
          </view>
          <text class="user-desc">阳光社区 · 活跃用户</text>
          <view class="user-stats">
            <view class="stat-item" @click="showStatDetail('help')">
              <text class="stat-number">{{ userStats.helpCount }}</text>
              <text class="stat-label">帮助次数</text>
              <view class="stat-trend up">↗</view>
            </view>
            <view class="stat-item" @click="showStatDetail('rating')">
              <text class="stat-number">{{ userStats.rating }}</text>
              <text class="stat-label">信用评分</text>
              <view class="stat-trend up">↗</view>
            </view>
            <view class="stat-item" @click="showStatDetail('days')">
              <text class="stat-number">{{ userStats.joinDays }}</text>
              <text class="stat-label">加入天数</text>
              <view class="stat-trend stable">→</view>
            </view>
          </view>
        </view>
      </view>

      <view class="wallet-card">
        <view class="wallet-header">
          <text class="wallet-title">💎 我的积分</text>
          <view class="wallet-action" @click="goToPointsDetail">
            <text class="action-text">查看详情</text>
            <text class="action-arrow">→</text>
          </view>
        </view>
        <view class="wallet-content">
          <view class="points-display">
            <text class="points-number">{{ userPoints }}</text>
            <text class="points-unit">积分</text>
            <view class="points-trend">
              <text class="trend-icon">📈</text>
              <text class="trend-text">+120 本周</text>
            </view>
          </view>
          <view class="points-actions">
            <view class="points-action" @click="goToPointsMall">
              <text class="action-icon">🛍️</text>
              <text class="action-text">积分商城</text>
            </view>
            <view class="points-action" @click="goToPointsHistory">
              <text class="action-icon">📊</text>
              <text class="action-text">积分明细</text>
            </view>
            <view class="points-action" @click="goToPointsTask">
              <text class="action-icon">🎯</text>
              <text class="action-text">积分任务</text>
            </view>
          </view>
        </view>
      </view>

      <view class="assistant-card">
        <view class="assistant-header">
          <text class="assistant-title">🤖 智能助手</text>
          <view class="assistant-status">
            <view class="status-dot online"></view>
            <text class="status-text">在线</text>
          </view>
        </view>
        <view class="assistant-content">
          <view class="assistant-suggestions">
            <view 
              class="suggestion-item" 
              v-for="suggestion in assistantSuggestions" 
              :key="suggestion.id"
              @click="applySuggestion(suggestion)"
            >
              <text class="suggestion-icon">{{ suggestion.icon }}</text>
              <text class="suggestion-text">{{ suggestion.text }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="menu-section">
        <view class="menu-group">
          <view class="menu-item" @click="goToMyRequests">
            <view class="menu-icon">📝</view>
            <text class="menu-text">我的需求</text>
            <view class="menu-badge" v-if="unreadRequests > 0">{{ unreadRequests }}</view>
            <text class="menu-arrow">→</text>
          </view>
          <view class="menu-item" @click="goToMyOrders">
            <view class="menu-icon">📋</view>
            <text class="menu-text">我的订单</text>
            <text class="menu-arrow">→</text>
          </view>
        </view>

        <view class="menu-group">
          <view class="menu-item" @click="goToCommunityAuth">
               <view class="menu-icon">🛡️</view>
               <text class="menu-text">小区认证</text>
               <text class="menu-badge" v-if="authStatus === 2" style="background: #4CAF50;">已认证</text>
               <text class="menu-badge" v-else-if="authStatus === 1" style="background: #FF9800;">审核中</text>
               <text class="menu-arrow">→</text>
          </view>

          <view class="menu-item" @click="goToMyActivities">
            <view class="menu-icon">🎉</view>
            <text class="menu-text">我的活动</text>
            <text class="menu-arrow">→</text>
          </view>
          <view class="menu-item" @click="goToMyFavorites">
            <view class="menu-icon">❤️</view>
            <text class="menu-text">我的收藏</text>
            <text class="menu-arrow">→</text>
          </view>
          <view class="menu-item" @click="goToMyReviews">
            <view class="menu-icon">⭐</view>
            <text class="menu-text">我的评价</text>
            <text class="menu-arrow">→</text>
          </view>
        </view>

        <view class="menu-group">
          <view class="menu-item" @click="goToHelp">
            <view class="menu-icon">❓</view>
            <text class="menu-text">帮助中心</text>
            <text class="menu-arrow">→</text>
          </view>
          <view class="menu-item" @click="goToFeedback">
            <view class="menu-icon">💭</view>
            <text class="menu-text">意见反馈</text>
            <text class="menu-arrow">→</text>
          </view>
          <view class="menu-item" @click="goToAbout">
            <view class="menu-icon">ℹ️</view>
            <text class="menu-text">关于我们</text>
            <text class="menu-arrow">→</text>
          </view>
        </view>
      </view>

      <view class="recent-section">
        <view class="section-header">
          <text class="section-title">最近活动</text>
          <view class="more-btn" @click="goToAllActivities">
            <text>查看更多</text>
            <text class="arrow">→</text>
          </view>
        </view>
        <view class="activity-list">
          <view class="activity-item" v-for="activity in recentActivities" :key="activity.id">
            <view class="activity-icon">{{ activity.icon }}</view>
            <view class="activity-content">
              <text class="activity-title">{{ activity.title }}</text>
              <text class="activity-desc">{{ activity.desc }}</text>
              <text class="activity-time">{{ activity.time }}</text>
            </view>
            <view class="activity-status" :class="activity.status">
              {{ activity.statusText }}
            </view>
          </view>
        </view>
      </view>

    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getUserProfile, getUserPoints, updateUserProfile } from '@/api/user'
import { getOrders } from '@/api/order'
import { onShow } from '@dcloudio/uni-app'

// 用户基础信息
const userName = ref('邻里用户')
const avatarText = ref('邻')
const avatarUrl= ref('')

// 响应式数据
const userPoints = ref(0)
const unreadRequests = ref(0)
const userLevel = ref(1)
const isLoading = ref(false)
const authStatus = ref(0)

// 用户统计
const userStats = ref({
  helpCount: 0,
  rating: 60,
  joinDays: 1
})

// 智能助手建议
const assistantSuggestions = ref([
  { id: 1, icon: '📦', text: '怎么发布代取快递需求？' },
  { id: 2, icon: '💎', text: '积分怎么获得和使用？' },
  { id: 3, icon: '⚠️', text: '遇到纠纷怎么联系物业？' }
])

// 最近活动
const recentActivities = ref([])

// ================= 新增：将获取数据的逻辑抽离为一个独立函数 =================
const loadUserProfileData = async () => {
  isLoading.value = true
  try {
    const [profileRes, pointsRes, ordersRes] = await Promise.all([
      getUserProfile(),
      getUserPoints(),
      getOrders({ page: 1, limit: 3 })
    ])

    const profile = profileRes?.data
    if (profile) {
      userName.value = profile.username || '邻里用户'
      const firstChar = (profile.username || '').trim().charAt(0)
      avatarText.value = firstChar || '邻'
      
      // 新增：如果有头像链接则赋值
      if (profile.avatar) {
        avatarUrl.value = profile.avatar
      }

      userLevel.value = profile.level ?? 1
      userStats.value = {
        helpCount: profile.helpCount ?? 0,
        rating: profile.rating ?? 60,
        joinDays: profile.joinDays ?? 1
      }
      // ================= 同步后端的真实认证状态 =================
      authStatus.value = profile.authStatus || 0
      uni.setStorageSync('authStatus', authStatus.value)
      
      if (profile.communityName) {
        uni.setStorageSync('communityName', profile.communityName)
      }
      // ==============================================================
    }

    const points = pointsRes?.data
    if (points) {
      userPoints.value = points.availablePoints ?? points.totalPoints ?? 0
      if (points.level != null) {
        userLevel.value = points.level
      }
    }

    const orders = ordersRes?.data?.records || []
    recentActivities.value = orders.map((order) => {
      const statusTextMap = {
        active: '已接单',
        started: '进行中',
        completed: '已完成',
        cancelled: '已取消'
      }
      const statusText = statusTextMap[order.status] || '处理中'
      return {
        id: order.id,
        icon: '📋',
        title: order.title || '订单',
        desc: order.description || order.acceptMessage || order.completeMessage || '',
        time: order.createdAt || '',
        status: order.status || 'active',
        statusText
      }
    })
  } catch (error) {
    console.error('加载个人资料失败:', error)
    // 静默失败即可，防止断网时一直弹窗打扰用户
  } finally {
    isLoading.value = false
  }
}

// ================= 修改：利用 onShow 每次进入页面时拉取最新数据 =================
onShow(() => {
  authStatus.value = uni.getStorageSync('authStatus') || 0
  loadUserProfileData()
})

// ================= 修改：onMounted 现在只负责挂载只需执行一次的时钟 =================
onMounted(() => {
  updateTime()
  setInterval(updateTime, 60000)
})

// 方法
const updateTime = () => {
  const now = new Date()
  const hours = now.getHours().toString().padStart(2, '0')
  const minutes = now.getMinutes().toString().padStart(2, '0')
  // 这里可以更新状态栏时间
}

// 导航方法
const goToSettings = () => {
  uni.navigateTo({
    url: '/pages/settings/settings',
    fail: (err) => {
      // 当页面不存在或未在 pages.json 中注册时，捕获错误并给出交互反馈
      console.warn('跳转设置页面失败:', err);
      uni.showToast({
        title: '设置功能暂未开放（或开发中）',
        icon: 'none',
        duration: 2000
      });
    }
  })
}

// 【修复2】：全新重构的头像上传逻辑 (原样保留)
const changeAvatar = () => {
  uni.chooseImage({
    count: 1, // 只能选择一张
    sizeType: ['compressed'], // 使用压缩图
    success: (res) => {
      const tempFilePath = res.tempFilePaths[0];
      uni.showLoading({ title: '上传中...', mask: true }); // 加 mask 防止连点

      const token = uni.getStorageSync('token'); 
      // 改为你之前提到的局域网IP，在真机/模拟器上才能联通后端
      const baseUrl = 'http://172.21.176.1:8080'; 

      uni.uploadFile({
        url: baseUrl + '/api/file/upload',
        filePath: tempFilePath,
        name: 'file',
        header: {
          'Authorization': token ? `Bearer ${token}` : '' 
        },
        success: async (uploadFileRes) => {
          // 成功回调第一件事：关掉 loading，避免后续 Toast 被冲掉
          uni.hideLoading();

          // 【调试专用】如果还报错，按 F12 看看控制台这两行打印了啥
          console.log('HTTP状态码:', uploadFileRes.statusCode);
          console.log('后端返回数据:', uploadFileRes.data);

          if (uploadFileRes.statusCode !== 200) {
            uni.showToast({ title: `请求失败: ${uploadFileRes.statusCode}`, icon: 'none' });
            return;
          }

          try {
            const data = JSON.parse(uploadFileRes.data);
            if (data.code === 200) {
              const newAvatarUrl = data.data;
              
              // 1. 调用更新用户资料接口保存新头像
              await updateUserProfile({ avatar: newAvatarUrl });
              
              // 2. 页面响应式更新
              avatarUrl.value = newAvatarUrl;
              uni.showToast({ title: '头像更新成功', icon: 'success' });
            } else {
              uni.showToast({ title: data.message || '上传失败', icon: 'none' });
            }
          } catch (e) {
            console.error("解析异常，可能后端报错返回了HTML:", e);
            uni.showToast({ title: '数据解析异常', icon: 'none' });
          }
        },
        fail: (err) => {
          // 彻底失败时第一件事：关掉 loading
          uni.hideLoading();
          console.error('网络请求失败:', err);
          uni.showToast({ title: '网络请求失败', icon: 'none' });
        }
      });
    }
  });
}

// ================= 修改：增加 fail 回调拦截报错 =================
const showLevelDetail = () => {
  uni.navigateTo({
    url: `/pages/level/level?level=${userLevel.value}`,
    fail: () => {
      uni.showToast({ title: '等级特权开发中...', icon: 'none' })
    }
  })
}

const showStatDetail = (type) => {
  uni.navigateTo({
    url: `/pages/user-stats/user-stats?type=${type}`,
    fail: () => {
      uni.showToast({ title: '统计明细开发中...', icon: 'none' })
    }
  })
}
// ==============================================================

const goToPointsTask = () => {
  uni.navigateTo({
    url: '/pages/points-center/points-center?tab=tasks'
  })
}

const applySuggestion = (suggestion) => {
  // TODO: 智能助手建议执行逻辑待接入
  uni.navigateTo({
    url: `/pages/assistant/assistant?text=${encodeURIComponent(suggestion.text)}`
  })
}

const goToPointsDetail = () => {
  uni.navigateTo({
    url: '/pages/points-center/points-center?tab=records'
  })
}

const goToPointsMall = () => {
  uni.navigateTo({
    url: '/pages/points-center/points-center?tab=shop'
  })
}

const goToPointsHistory = () => {
  uni.navigateTo({
    url: '/pages/points-center/points-center?tab=records'
  })
}

const goToMyRequests = () => {
  uni.navigateTo({
    url: '/pages/my-requests/my-requests'
  })
}

const goToMyOrders = () => {
  uni.navigateTo({
    url: '/pages/my-orders/my-orders'
  })
}

const goToMyActivities = () => {
  uni.navigateTo({
    url: '/pages/my-activities/my-activities'
  })
}

const goToMyFavorites = () => {
  uni.navigateTo({
    url: '/pages/my-favorites/my-favorites'
  })
}

const goToMyReviews = () => {
  uni.navigateTo({
    url: '/pages/my-reviews/my-reviews'
  })
}

const goToHelp = () => {
  uni.navigateTo({
    url: '/pages/help/help'
  })
}

const goToFeedback = () => {
  uni.navigateTo({
    url: '/pages/feedback/feedback'
  })
}

const goToAbout = () => {
  uni.navigateTo({
    url: '/pages/about/about'
  })
}

const goToAllActivities = () => {
  uni.navigateTo({
    url: '/pages/activities/activities'
  })
}

const goToCommunityAuth = () => {
  if (authStatus.value === 2) {
    // 如果已经认证，直接弹窗提示，不跳转！
    const name = uni.getStorageSync('communityName') || '您的专属小区'
    uni.showToast({ title: `您已认证：${name}`, icon: 'none' })
  } else if (authStatus.value === 1) {
    uni.showToast({ title: '人工审核中，请耐心等待', icon: 'none' })
  } else {
    // 没认证才放行跳转
    uni.navigateTo({
      url: '/pages/community-auth/community-auth'
    })
  }
}
</script>
<style scoped>
.profile-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #FFF8E1 0%, #FFECB3 100%);
  display: flex;
  flex-direction: column;
  position: relative;
}

.avatar-image {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover; /* 保证图片不变形 */
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

/* 左上角小头像 */
.mini-avatar-btn {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #4CAF50, #66BB6A);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 12rpx rgba(76, 175, 80, 0.3);
  transition: all 0.3s ease;
  overflow: hidden; /* 关键：让图片保持圆形 */
}

.mini-avatar-btn:active {
  transform: scale(0.95);
}

.mini-avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover; /* 保证图片不变形 */
}

.mini-avatar-text {
  font-size: 28rpx;
  color: white;
  font-weight: 700;
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

.settings-btn {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background: #F5F5F5;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.settings-btn:active {
  background: #E0E0E0;
  transform: scale(0.95);
}

.settings-icon {
  font-size: 28rpx;
}

/* 主内容区域 */
.main-content {
  flex: 1;
  padding: 0 20rpx;
  padding-bottom: 20rpx;
  overflow: hidden;
}

/* 用户信息卡片 */
.user-card {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background: white;
  border-radius: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
  margin: 12rpx 0;
  gap: 16rpx;
}

.user-avatar {
  position: relative;
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #4CAF50, #66BB6A);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 16rpx rgba(76, 175, 80, 0.3);
  transition: all 0.3s ease;
}

.user-avatar:active {
  transform: scale(0.95);
}

.avatar-text {
  font-size: 32rpx;
  color: white;
  font-weight: 700;
}

.avatar-badge {
  position: absolute;
  top: -8rpx;
  right: -8rpx;
  background: linear-gradient(135deg, #FF8F00, #FFB74D);
  color: white;
  padding: 4rpx 8rpx;
  border-radius: 12rpx;
  font-size: 18rpx;
  font-weight: 600;
}

.avatar-edit {
  position: absolute;
  bottom: -4rpx;
  right: -4rpx;
  width: 32rpx;
  height: 32rpx;
  border-radius: 50%;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.edit-icon {
  font-size: 16rpx;
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.user-header {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.user-name {
  font-size: 36rpx;
  color: #424242;
  font-weight: 700;
}

.user-level {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 6rpx 12rpx;
  background: linear-gradient(135deg, #FF8F00, #FFB74D);
  border-radius: 16rpx;
  transition: all 0.3s ease;
}

.user-level:active {
  transform: scale(0.95);
}

.level-icon {
  font-size: 20rpx;
}

.level-text {
  font-size: 20rpx;
  color: white;
  font-weight: 600;
}

.user-desc {
  font-size: 24rpx;
  color: #666;
}

.user-stats {
  display: flex;
  gap: 16rpx;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
  position: relative;
  transition: all 0.3s ease;
}

.stat-item:active {
  transform: scale(0.95);
}

.stat-number {
  font-size: 28rpx;
  color: #4CAF50;
  font-weight: 700;
}

.stat-label {
  font-size: 20rpx;
  color: #666;
}

.stat-trend {
  position: absolute;
  top: -8rpx;
  right: -8rpx;
  width: 20rpx;
  height: 20rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16rpx;
  font-weight: 600;
}

.stat-trend.up {
  background: #4CAF50;
  color: white;
}

.stat-trend.stable {
  background: #FFC107;
  color: white;
}


.progress-bar {
  width: 100%;
  height: 6rpx;
  background: #E0E0E0;
  border-radius: 3rpx;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(135deg, #4CAF50, #66BB6A);
  border-radius: 3rpx;
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 18rpx;
  color: #666;
  text-align: center;
}

/* 积分钱包 */
.wallet-card {
  background: linear-gradient(135deg, #FF8F00 0%, #FFB74D 100%);
  border-radius: 16rpx;
  padding: 20rpx;
  margin: 12rpx 0;
  box-shadow: 0 4rpx 16rpx rgba(255, 143, 0, 0.3);
}

.wallet-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.wallet-title {
  font-size: 28rpx;
  color: white;
  font-weight: 700;
}

.wallet-action {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 8rpx 16rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 16rpx;
  transition: all 0.3s ease;
}

.wallet-action:active {
  background: rgba(255, 255, 255, 0.3);
}

.action-text {
  font-size: 22rpx;
  color: white;
}

.action-arrow {
  font-size: 20rpx;
  color: white;
}

.wallet-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.points-display {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.points-number {
  font-size: 48rpx;
  color: white;
  font-weight: 700;
  display: flex;
  align-items: baseline;
  gap: 8rpx;
}

.points-unit {
  font-size: 24rpx;
  color: white;
  opacity: 0.9;
}

.points-trend {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 4rpx 8rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12rpx;
  align-self: flex-start;
}

.trend-icon {
  font-size: 16rpx;
}

.trend-text {
  font-size: 18rpx;
  color: white;
  font-weight: 500;
}

.points-actions {
  display: flex;
  gap: 12rpx;
}

.points-action {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12rpx;
  transition: all 0.3s ease;
  flex: 1;
}

.points-action:active {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(0.95);
}

.action-icon {
  font-size: 28rpx;
}

/* 智能助手 */
.assistant-card {
  background: linear-gradient(135deg, #E3F2FD 0%, #BBDEFB 100%);
  border-radius: 20rpx;
  padding: 24rpx;
  margin: 20rpx 0;
  box-shadow: 0 6rpx 24rpx rgba(33, 150, 243, 0.15);
}

.assistant-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.assistant-title {
  font-size: 28rpx;
  color: #1976D2;
  font-weight: 700;
}

.assistant-status {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 6rpx 12rpx;
  background: rgba(33, 150, 243, 0.1);
  border-radius: 16rpx;
}

.status-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
}

.status-dot.online {
  background: #4CAF50;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.5; }
  100% { opacity: 1; }
}

.status-text {
  font-size: 20rpx;
  color: #1976D2;
  font-weight: 500;
}

.assistant-content {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.assistant-suggestions {
  display: flex;
  gap: 12rpx;
}

.suggestion-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  padding: 16rpx 12rpx;
  background: white;
  border-radius: 12rpx;
  transition: all 0.3s ease;
}

.suggestion-item:active {
  transform: scale(0.95);
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.suggestion-icon {
  font-size: 28rpx;
}

.suggestion-text {
  font-size: 20rpx;
  color: #424242;
  font-weight: 500;
  text-align: center;
}


/* 功能菜单 */
.menu-section {
  margin: 16rpx 0;
}

.menu-group {
  background: white;
  border-radius: 16rpx;
  margin-bottom: 12rpx;
  overflow: hidden;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 16rpx 24rpx;
  border-bottom: 1rpx solid #F5F5F5;
  transition: all 0.3s ease;
  gap: 16rpx;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-item:active {
  background: #F8F9FA;
}

.menu-icon {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background: #F5F5F5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
}

.menu-text {
  flex: 1;
  font-size: 28rpx;
  color: #424242;
  font-weight: 500;
}

.menu-badge {
  background: #F44336;
  color: white;
  padding: 4rpx 8rpx;
  border-radius: 12rpx;
  font-size: 20rpx;
  font-weight: 600;
  margin-right: 12rpx;
}

.menu-arrow {
  font-size: 24rpx;
  color: #999;
}

/* 最近活动 */
.recent-section {
  margin: 32rpx 0;
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

.more-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 8rpx 16rpx;
  background: rgba(76, 175, 80, 0.1);
  border-radius: 16rpx;
  transition: all 0.3s ease;
}

.more-btn:active {
  background: rgba(76, 175, 80, 0.2);
}

.arrow {
  font-size: 20rpx;
  color: #4CAF50;
}

.activity-list {
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
  gap: 16rpx;
}

.activity-icon {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background: #E8F5E8;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
}

.activity-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.activity-title {
  font-size: 26rpx;
  color: #424242;
  font-weight: 600;
}

.activity-desc {
  font-size: 22rpx;
  color: #666;
}

.activity-time {
  font-size: 20rpx;
  color: #999;
}

.activity-status {
  padding: 8rpx 16rpx;
  border-radius: 16rpx;
  font-size: 20rpx;
  font-weight: 500;
}

.activity-status.completed {
  background: rgba(76, 175, 80, 0.1);
  color: #4CAF50;
}

.activity-status.in-progress {
  background: rgba(255, 193, 7, 0.1);
  color: #FF8F00;
}


/* 底部间距 */
.bottom-spacing {
  height: 30rpx;
}
</style>
