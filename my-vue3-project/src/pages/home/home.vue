<template>
  <view class="home-container">
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
      <view class="nav-left">
        <view class="location-icon">📍</view>
        <text class="community-name">{{ currentCommunityName }}</text>
      </view>
      <view class="nav-right">
        <view class="points-display" @click="goToPoints">
          <text class="points-icon">💎</text>
          <text class="points-text">{{ userPoints }}</text>
        </view>
        <view class="notification-icon" @click="showNotifications">
          🔔
          <text v-if="unreadCount > 0" class="notification-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</text>
        </view>
      </view>
    </view>

    <!-- 主内容区域 -->
    <view class="main-content">
      <!-- 快速发布需求卡片 -->
      <view class="quick-publish-card" @click="goToRequest">
        <view class="publish-content">
          <view class="publish-icon">📝</view>
          <view class="publish-text">
            <text class="publish-title">发布需求</text>
            <text class="publish-subtitle">让邻居帮你解决生活难题</text>
          </view>
          <view class="publish-arrow">→</view>
        </view>
      </view>

      <!-- 顺路带 / 行程广播卡片 -->
      <view class="trip-broadcast-card" @click="openTripBroadcast">
        <view class="trip-main">
          <view class="trip-left">
            <view class="trip-icon">🚶‍♂️</view>
            <view class="trip-text">
              <text class="trip-title">顺路带 · 行程广播</text>
              <text class="trip-subtitle">
                我正要去超市 / 快递点，帮邻居顺手带一程
              </text>
            </view>
          </view>
          <view class="trip-right">
            <view class="trip-tag">{{ activeTrip ? '进行中' : '立即发起' }}</view>
            <text class="trip-arrow">→</text>
          </view>
        </view>
        <view class="trip-meta">
          <text class="trip-meta-text" v-if="activeTrip">
            已向附近邻居广播：{{ activeTrip.destination }} · 剩余 {{ remainingMinutes }} 分钟
          </text>
          <text class="trip-meta-text" v-else>
            提高接单效率，让“本来就要去”的路程更划算
          </text>
        </view>
      </view>

      <!-- 服务模块区域 -->
      <view class="services-section">
        <view class="section-header">
          <text class="section-title">服务分类</text>
          <text class="section-subtitle">选择你想要接收的服务类型</text>
        </view>
        <view class="services-grid">
           <view class="service-item" @click="goToServiceRequests('package')">
             <view class="service-icon package-icon">
               <text class="service-emoji">📦</text>
             </view>
             <text class="service-name">帮我取快递</text>
             <text class="service-count">{{ serviceCounts.package }}个待接收</text>
           </view>
          <view class="service-item" @click="goToServiceRequests('pet')">
            <view class="service-icon pet-icon">
              <text class="service-emoji">🐕</text>
            </view>
            <text class="service-name">帮我照顾宠物</text>
            <text class="service-count">{{ serviceCounts.pet }}个待接收</text>
          </view>
          <view class="service-item" @click="goToServiceRequests('shopping')">
            <view class="service-icon shopping-icon">
              <text class="service-emoji">🛒</text>
            </view>
            <text class="service-name">帮我买菜</text>
            <text class="service-count">{{ serviceCounts.shopping }}个待接收</text>
          </view>
          <view class="service-item" @click="goToServiceRequests('repair')">
            <view class="service-icon repair-icon">
              <text class="service-emoji">🔧</text>
            </view>
            <text class="service-name">帮我家电维修</text>
            <text class="service-count">{{ serviceCounts.repair }}个待接收</text>
          </view>
          <view class="service-item" @click="goToServiceRequests('cleaning')">
            <view class="service-icon cleaning-icon">
              <text class="service-emoji">🧹</text>
            </view>
            <text class="service-name">帮我家清洁</text>
            <text class="service-count">{{ serviceCounts.cleaning }}个待接收</text>
          </view>
          <view class="service-item" @click="goToServiceRequests('cooking')">
            <view class="service-icon cooking-icon">
              <text class="service-emoji">🍳</text>
            </view>
            <text class="service-name">帮我做美食</text>
            <text class="service-count">{{ serviceCounts.cooking }}个待接收</text>
          </view>
        </view>
      </view>

      <!-- 积分商城入口 -->
      <view class="points-mall-card" @click="goToPointsMall">
        <view class="mall-content">
          <view class="mall-icon">🛍️</view>
          <view class="mall-text">
            <text class="mall-title">积分商城</text>
            <text class="mall-subtitle">用积分兑换优惠券</text>
          </view>
          <view class="mall-arrow">→</view>
        </view>
      </view>

      <!-- 社区活动区域 -->
      <view class="activities-section">
        <view class="section-header">
          <text class="section-title">社区活动</text>
          <view class="more-btn" @click="goToAllActivities">
            <text>更多活动</text>
            <text class="arrow">→</text>
          </view>
        </view>
        <view class="activities-list">
          <view class="activity-item" @click="goToActivityDetail('1')">
            <view class="activity-image">
              <view class="activity-badge">进行中</view>
            </view>
            <view class="activity-info">
              <text class="activity-title">周末邻里聚餐</text>
              <text class="activity-desc">5月15日 18:00</text>
              <text class="activity-participants">已有12人报名</text>
            </view>
            <view class="activity-action">
              <view class="join-btn" @click.stop="joinActivity('1')">报名</view>
            </view>
          </view>
          <view class="activity-item" @click="goToActivityDetail('2')">
            <view class="activity-image volunteer">
              <view class="activity-badge">即将开始</view>
            </view>
            <view class="activity-info">
              <text class="activity-title">社区清洁志愿活动</text>
              <text class="activity-desc">5月20日 9:00</text>
              <text class="activity-participants">已有8人报名</text>
            </view>
            <view class="activity-action">
              <view class="join-btn" @click.stop="joinActivity('2')">报名</view>
            </view>
          </view>
        </view>
      </view>

    </view>

    <!-- 浮动发布按钮 -->
    <view class="fab" @click="goToRequest">
      <text class="fab-icon">+</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getCurrentUser } from '@/api/auth'
import { getServiceTypeStats } from '@/api/service'
import { getUnreadCount } from '@/api/notification'
import { getUserPoints } from '@/api/user' 
import { getUserProfile } from '@/api/user'

// 响应式数据
const currentTime = ref('9:41')
const unreadCount = ref(0)
const userPoints = ref(0)
const currentCommunityName = ref('未绑定小区')

// 顺路带 / 行程广播状态（本地模拟，不改动后端接口）
const activeTrip = ref(null) // { id, destination, type, expireAt }
const remainingMinutes = ref(0)

// 服务需求计数，初始为0，挂载后从后端获取
const serviceCounts = ref({
  package: 0,
  pet: 0,
  shopping: 0,
  repair: 0,
  cleaning: 0,
  cooking: 0
})

onMounted(() => {
  // 先从本地获取初始值，防止白屏
  const currentUser = getCurrentUser()
  if (currentUser) {
    userPoints.value = currentUser.points ?? 0
  }

  updateTime()
  setInterval(updateTime, 60000)
  restoreActiveTrip()
  loadServiceCounts()
  loadUnreadNotifications()
  // 挂载后立即同步一次最新积分
  loadUserPoints()
})

onShow(async () => {
  loadUnreadNotifications()
  loadUserPoints() 
  
  // 【修改】优先拉取后端真实状态，覆盖本地缓存
  try {
      const profileRes = await getUserProfile()
      if (profileRes && profileRes.data) {
          const profile = profileRes.data
          // 更新本地认证状态
          uni.setStorageSync('authStatus', profile.authStatus || 0)
          
          // 如果后端有返回小区名字（你需要确保后端的 UserVO 里加了 communityName 字段返回给前端）
          if (profile.communityName) {
              currentCommunityName.value = profile.communityName
              uni.setStorageSync('communityName', profile.communityName)
          } else {
              currentCommunityName.value = '未绑定小区'
              uni.removeStorageSync('communityName')
          }
      }
  } catch(e) {
      // 如果接口失败，再退回来读缓存
      console.error('获取用户信息失败', e)
      const savedName = uni.getStorageSync('communityName')
      if (savedName) {
        currentCommunityName.value = savedName
      } else {
        currentCommunityName.value = '未绑定小区'
      }
  }
})
// 方法
const updateTime = () => {
  const now = new Date()
  const hours = now.getHours().toString().padStart(2, '0')
  const minutes = now.getMinutes().toString().padStart(2, '0')
  currentTime.value = `${hours}:${minutes}`
}

const loadUnreadNotifications = async () => {
  const token = uni.getStorageSync('token')
  if (!token) {
    unreadCount.value = 0
    return
  }
  try {
    const res = await getUnreadCount()
    unreadCount.value = Number(res.data?.count ?? 0)
  } catch (e) {
    console.warn('获取未读通知数失败', e)
    unreadCount.value = 0
  }
}

const showNotifications = () => {
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.showToast({ title: '请先登录后查看通知', icon: 'none' })
    return
  }
  uni.navigateTo({
    url: '/pages/notifications/notifications'
  })
}

// 从后端获取各服务类型的待接收数量
const loadServiceCounts = async () => {
  try {
    const res = await getServiceTypeStats()
    const data = res.data || res?.data?.data || {}
    serviceCounts.value = {
      package: data.package ?? 0,
      pet: data.pet ?? 0,
      shopping: data.shopping ?? 0,
      repair: data.repair ?? 0,
      cleaning: data.cleaning ?? 0,
      cooking: data.cooking ?? 0
    }
  } catch (e) {
    console.error('加载服务统计失败', e)
  }
}

// 打开顺路带 / 行程广播
const openTripBroadcast = () => {
  uni.showActionSheet({
    itemList: ['我要去超市', '我要去快递点', '我要去菜市场'],
    success: (res) => {
      const map = ['社区超市', '菜鸟驿站 / 快递点', '菜市场']
      const typeMap = ['market', 'parcel', 'vegetable']
      const destination = map[res.tapIndex]
      const tripType = typeMap[res.tapIndex]
      launchTripBroadcast(destination, tripType)
    }
  })
}

// 发起行程广播（创建本地“限时拼单池”）
const launchTripBroadcast = (destination, tripType) => {
  const now = Date.now()
  const expireAt = now + 15 * 60 * 1000 // 默认 15 分钟有效期
  const tripId = `trip_${now}`
  activeTrip.value = {
    id: tripId,
    destination,
    type: tripType,
    expireAt
  }
  // 存储到本地，方便其它页面“收到通知”
  uni.setStorageSync('activeTripBroadcast', JSON.stringify(activeTrip.value))

  // 提示当前用户
  uni.showToast({
    title: `已广播行程：${destination}`,
    icon: 'success'
  })

  // 模拟“附近邻居收到通知”
  simulateNeighborNotification(destination)

  // 启动倒计时
  startTripCountdown()
}

// 恢复本地未过期行程
const restoreActiveTrip = () => {
  try {
    const saved = uni.getStorageSync('activeTripBroadcast')
    if (!saved) return
    const trip = JSON.parse(saved)
    if (trip.expireAt && trip.expireAt > Date.now()) {
      activeTrip.value = trip
      startTripCountdown()
    } else {
      uni.removeStorageSync('activeTripBroadcast')
    }
  } catch (e) {
    console.error('恢复行程广播失败', e)
  }
}


const loadUserPoints = async () => {
  const token = uni.getStorageSync('token')
  if (!token) return
  try {
    const res = await getUserPoints() // 调用后端接口 /users/points/current
    if (res.code === 200 && res.data) {
      // PointsVO 中包含了 availablePoints 和 totalPoints
      userPoints.value = res.data.availablePoints ?? res.data.totalPoints ?? 0
    }
  } catch (e) {
    console.error('获取首页积分失败:', e)
  }
}

// 行程倒计时 & 过期处理
let tripTimer = null
const startTripCountdown = () => {
  if (!activeTrip.value) return
  if (tripTimer) {
    clearInterval(tripTimer)
  }
  const update = () => {
    if (!activeTrip.value) {
      remainingMinutes.value = 0
      clearInterval(tripTimer)
      tripTimer = null
      return
    }
    const diff = activeTrip.value.expireAt - Date.now()
    if (diff <= 0) {
      activeTrip.value = null
      remainingMinutes.value = 0
      uni.removeStorageSync('activeTripBroadcast')
      clearInterval(tripTimer)
      tripTimer = null
      uni.showToast({
        title: '本次顺路带行程已结束',
        icon: 'none'
      })
    } else {
      remainingMinutes.value = Math.max(1, Math.round(diff / 60000))
    }
  }
  update()
  tripTimer = setInterval(update, 30000)
}

// 模拟“邻居收到广播”的通知逻辑（本地）
const simulateNeighborNotification = (destination) => {
  // 将简化版广播消息写入本地“通知中心”（与后端通知独立）
  try {
    let list = []
    const saved = uni.getStorageSync('neighborNotifications')
    if (saved) {
      list = JSON.parse(saved)
    }
    list.unshift({
      id: Date.now(),
      type: 'trip_broadcast',
      title: '邻居发布顺路带行程',
      message: `你的邻居正要去 ${destination}，是否需要顺手帮你带点什么？`,
      createdAt: new Date().toLocaleString('zh-CN'),
      read: false
    })
    uni.setStorageSync('neighborNotifications', JSON.stringify(list))
  } catch (e) {
    console.error('写入顺路带通知失败', e)
  }
}

const goToRequest = () => {
  // 由于需求页面是tabBar页面，应该使用switchTab
  uni.switchTab({
    url: '/pages/request/request'
  })
}

const goToPoints = () => {
  // 修改为跳转至积分中心
  uni.navigateTo({
    url: '/pages/points-center/points-center?tab=records'
  })
}

const goToServiceRequests = (serviceType) => {
  // 跳转到服务需求列表页面
  uni.navigateTo({
    url: `/pages/service-requests/service-requests?type=${serviceType}`
  })
}

const goToRequestDetail = (requestId) => {
  uni.showModal({
    title: '接单确认',
    content: '确定要接这个单吗？',
    success: (res) => {
      if (res.confirm) {
        // 保存订单到本地存储
        saveOrderToLocal(requestId)
        
        uni.showToast({
          title: '接单成功！',
          icon: 'success'
        })
        // 增加积分
        userPoints.value += 20
      }
    }
  })
}

// 保存订单到本地存储（首页接单）
const saveOrderToLocal = (requestId) => {
  try {
    // 获取现有订单数据
    let myOrders = []
    const savedOrders = uni.getStorageSync('myOrders')
    if (savedOrders) {
      myOrders = JSON.parse(savedOrders)
    }
    
    // 生成新的订单ID
    const newOrderId = Date.now()
    
    // 构建完整的订单对象（首页接单的简化版本）
    const newOrder = {
      id: newOrderId,
      requestId: requestId,
      title: '帮我取快递',
      description: '代收快递包裹服务',
      serviceType: 'package',
      status: 'in_progress',
      expectedTime: '尽快完成',
      location: '碧桂园某某栋几单元几号房',
      rewardAmount: 20,
      createdAt: new Date().toLocaleString('zh-CN'),
      acceptedAt: new Date().toLocaleString('zh-CN'),
      completedAt: null,
      publisher: {
        username: '张邻居',
        rating: 4.8,
        avatar: '👤'
      }
    }
    
    // 添加到订单列表
    myOrders.unshift(newOrder)
    
    // 保存到本地存储
    uni.setStorageSync('myOrders', JSON.stringify(myOrders))
    
    console.log('首页接单已保存到本地存储:', newOrder)
  } catch (error) {
    console.error('保存订单失败:', error)
  }
}

const goToPointsMall = () => {
  uni.navigateTo({
    url: '/pages/points-center/points-center?tab=shop'
  })
}

const goToAllActivities = () => {
  uni.showToast({
    title: '更多活动',
    icon: 'none'
  })
}

const goToActivityDetail = (activityId) => {
  uni.showToast({
    title: '活动详情',
    icon: 'none'
  })
}

const joinActivity = (activityId) => {
  uni.showModal({
    title: '报名确认',
    content: '确定要报名参加这个活动吗？',
    success: (res) => {
      if (res.confirm) {
        // 保存报名信息到本地存储
        saveActivityRegistration(activityId)
        
        uni.showToast({
          title: '报名成功！',
          icon: 'success'
        })
        // 增加积分
        userPoints.value += 10
      }
    }
  })
}

// 保存活动报名信息
const saveActivityRegistration = (activityId) => {
  try {
    // 获取现有活动数据
    let myActivities = []
    const savedActivities = uni.getStorageSync('myActivities')
    if (savedActivities) {
      myActivities = JSON.parse(savedActivities)
    }
    
    // 检查是否已经报名过
    const existingActivity = myActivities.find(activity => activity.id === activityId)
    if (existingActivity) {
      uni.showToast({
        title: '已经报名过了',
        icon: 'none'
      })
      return
    }
    
    // 根据活动ID创建活动信息
    const activityInfo = getActivityInfo(activityId)
    if (activityInfo) {
      myActivities.push(activityInfo)
      
      // 保存到本地存储
      uni.setStorageSync('myActivities', JSON.stringify(myActivities))
    }
  } catch (error) {
    console.error('保存活动报名信息失败:', error)
  }
}

// 获取活动信息
const getActivityInfo = (activityId) => {
  const activities = {
    '1': {
      id: '1',
      title: '周末邻里聚餐',
      desc: '5月15日 18:00',
      time: '2025年5月15日 18:00',
      location: '社区活动中心',
      type: 'dinner',
      status: 'upcoming',
      participants: 12,
      registeredAt: new Date().toISOString()
    },
    '2': {
      id: '2',
      title: '社区清洁志愿活动',
      desc: '5月20日 9:00',
      time: '2025年5月20日 9:00',
      location: '社区广场',
      type: 'volunteer',
      status: 'upcoming',
      participants: 8,
      registeredAt: new Date().toISOString()
    }
  }
  
  return activities[activityId] || null
}

const loadMore = () => {
  uni.showToast({
    title: '加载更多',
    icon: 'none'
  })
}

const showQuickActions = () => {
  uni.showActionSheet({
    itemList: ['发布需求', '发起活动', '联系物业'],
    success: (res) => {
      const actions = ['发布需求', '发起活动', '联系物业']
      uni.showToast({
        title: actions[res.tapIndex],
        icon: 'none'
      })
    }
  })
}

// 底部导航方法
const goToHome = () => {
  // 当前页面，无需跳转
}

</script>

<style scoped>
.home-container {
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
}

.location-icon {
  font-size: 28rpx;
}

.community-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #8D6E63;
}

.nav-right {
  position: relative;
}

.points-display {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 8rpx 12rpx;
  background: rgba(255, 193, 7, 0.1);
  border-radius: 20rpx;
  margin-right: 12rpx;
  transition: all 0.3s ease;
}

.points-display:active {
  background: rgba(255, 193, 7, 0.2);
}

.points-icon {
  font-size: 24rpx;
}

.points-text {
  font-size: 24rpx;
  color: #FF8F00;
  font-weight: 600;
}

.notification-icon {
  position: relative;
  font-size: 32rpx;
  padding: 6rpx;
  border-radius: 50%;
  background: rgba(255, 193, 7, 0.1);
  transition: all 0.3s ease;
}

.notification-badge {
  position: absolute;
  top: -4rpx;
  right: -4rpx;
  min-width: 28rpx;
  height: 28rpx;
  padding: 0 6rpx;
  line-height: 28rpx;
  text-align: center;
  font-size: 18rpx;
  color: #fff;
  background: #f44336;
  border-radius: 14rpx;
}

.notification-icon:active {
  transform: scale(0.95);
  background: rgba(255, 193, 7, 0.2);
}

/* 主内容区域 */
.main-content {
  flex: 1;
  padding: 0 20rpx;
  padding-bottom: 20rpx;
  overflow: hidden;
}

/* 快速发布需求卡片 */
.quick-publish-card {
  margin: 12rpx 0;
  background: linear-gradient(135deg, #4CAF50 0%, #66BB6A 100%);
  border-radius: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(76, 175, 80, 0.3);
  transition: all 0.3s ease;
}

.quick-publish-card:active {
  transform: translateY(-2rpx);
  box-shadow: 0 8rpx 28rpx rgba(76, 175, 80, 0.4);
}

.publish-content {
  display: flex;
  align-items: center;
  padding: 16rpx;
  gap: 12rpx;
}

.publish-icon {
  font-size: 40rpx;
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10rpx);
}

.publish-text {
  flex: 1;
  color: white;
}

.publish-title {
  font-size: 28rpx;
  font-weight: 700;
  margin-bottom: 4rpx;
  display: block;
}

.publish-subtitle {
  font-size: 22rpx;
  opacity: 0.9;
  display: block;
}

.publish-arrow {
  font-size: 32rpx;
  color: white;
  opacity: 0.8;
}

/* 顺路带 / 行程广播卡片 */
.trip-broadcast-card {
  margin: 12rpx 0;
  background: linear-gradient(135deg, #2563EB 0%, #3B82F6 100%);
  border-radius: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(37, 99, 235, 0.35);
  padding: 16rpx;
  color: white;
}

.trip-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12rpx;
}

.trip-left {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex: 1;
}

.trip-icon {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.18);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 34rpx;
}

.trip-text {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.trip-title {
  font-size: 28rpx;
  font-weight: 700;
}

.trip-subtitle {
  font-size: 22rpx;
  opacity: 0.9;
}

.trip-right {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.trip-tag {
  padding: 6rpx 12rpx;
  border-radius: 999rpx;
  background: rgba(15, 23, 42, 0.18);
  font-size: 20rpx;
}

.trip-arrow {
  font-size: 28rpx;
}

.trip-meta {
  margin-top: 8rpx;
}

.trip-meta-text {
  font-size: 20rpx;
  opacity: 0.9;
}

/* 服务区域 */
.services-section {
  margin: 16rpx 0;
}

.section-subtitle {
  font-size: 24rpx;
  color: #A1887F;
  margin-top: 4rpx;
  display: block;
}

.request-item {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background: white;
  border-radius: 16rpx;
  box-shadow: 0 3rpx 16rpx rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  gap: 16rpx;
}

.request-item:active {
  transform: translateY(-2rpx);
  box-shadow: 0 6rpx 20rpx rgba(0, 0, 0, 0.12);
}

.request-icon {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  color: white;
}

.package-icon {
  background: linear-gradient(135deg, #FF8A65, #FFB74D);
}

.pet-icon {
  background: linear-gradient(135deg, #FFD54F, #FFECB3);
}

.repair-icon {
  background: linear-gradient(135deg, #81C784, #A5D6A7);
}

.request-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.request-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #8D6E63;
}

.request-desc {
  font-size: 24rpx;
  color: #A1887F;
}

.request-reward {
  font-size: 22rpx;
  color: #FF8F00;
  font-weight: 500;
}

.request-status {
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  font-size: 20rpx;
  font-weight: 500;
}

.request-status.available {
  background: rgba(76, 175, 80, 0.1);
  color: #4CAF50;
}

/* 积分商城卡片 */
.points-mall-card {
  margin: 20rpx 0;
  background: linear-gradient(135deg, #FF8F00 0%, #FFB74D 100%);
  border-radius: 20rpx;
  box-shadow: 0 6rpx 24rpx rgba(255, 143, 0, 0.3);
  transition: all 0.3s ease;
}

.points-mall-card:active {
  transform: translateY(-2rpx);
  box-shadow: 0 8rpx 28rpx rgba(255, 143, 0, 0.4);
}

.mall-content {
  display: flex;
  align-items: center;
  padding: 24rpx;
  gap: 16rpx;
}

.mall-icon {
  font-size: 40rpx;
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10rpx);
}

.mall-text {
  flex: 1;
  color: white;
}

.mall-title {
  font-size: 28rpx;
  font-weight: 700;
  margin-bottom: 4rpx;
  display: block;
}

.mall-subtitle {
  font-size: 22rpx;
  opacity: 0.9;
  display: block;
}

.mall-arrow {
  font-size: 32rpx;
  color: white;
  opacity: 0.8;
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
  gap: 6rpx;
  color: #FF8F00;
  font-size: 24rpx;
  font-weight: 500;
}

.arrow {
  font-size: 24rpx;
  transition: transform 0.3s ease;
}

.more-btn:active .arrow {
  transform: translateX(4rpx);
}

.services-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12rpx;
}

.service-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6rpx;
  padding: 16rpx 8rpx;
  background: white;
  border-radius: 12rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  position: relative;
}

.service-item:active {
  transform: translateY(-2rpx);
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.12);
}

.service-icon {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  box-shadow: 0 3rpx 12rpx rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.service-icon:hover {
  transform: translateY(-2rpx);
  box-shadow: 0 6rpx 20rpx rgba(0, 0, 0, 0.2);
}

.icon-content {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
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

.package-box {
  width: 24rpx;
  height: 18rpx;
  background: #8D4513;
  border-radius: 3rpx;
  position: relative;
  transform: rotate(-15deg);
}

.package-tape {
  position: absolute;
  top: 6rpx;
  left: -3rpx;
  width: 30rpx;
  height: 6rpx;
  background: #D2691E;
  border-radius: 2rpx;
}

.package-label {
  position: absolute;
  top: 1rpx;
  left: 4rpx;
  width: 16rpx;
  height: 4rpx;
  background: #FFD700;
  border-radius: 1rpx;
}

/* 宠物图标样式 */
.pet-icon {
  background: linear-gradient(135deg, #FFD54F, #FFECB3);
}

.paw-print {
  width: 12rpx;
  height: 12rpx;
  background: #8B4513;
  border-radius: 50% 50% 50% 50% / 60% 60% 40% 40%;
  position: absolute;
  transform: rotate(45deg);
}

.paw-print.small {
  width: 8rpx;
  height: 8rpx;
  top: 6rpx;
  left: 8rpx;
}

.paw-print.tiny {
  width: 6rpx;
  height: 6rpx;
  top: 12rpx;
  left: 14rpx;
}

/* 维修图标样式 */
.repair-icon {
  background: linear-gradient(135deg, #81C784, #A5D6A7);
}

.wrench-handle {
  width: 18rpx;
  height: 3rpx;
  background: #2E7D32;
  border-radius: 2rpx;
  position: absolute;
  top: 15rpx;
  left: 12rpx;
  transform: rotate(30deg);
}

.wrench-head {
  width: 12rpx;
  height: 12rpx;
  border: 2rpx solid #2E7D32;
  border-radius: 50%;
  position: absolute;
  top: 9rpx;
  left: 18rpx;
  background: transparent;
}

.wrench-bolt {
  width: 4rpx;
  height: 4rpx;
  background: #4CAF50;
  border-radius: 50%;
  position: absolute;
  top: 13rpx;
  left: 22rpx;
}

/* 清洁图标样式 */
.cleaning-icon {
  background: linear-gradient(135deg, #64B5F6, #90CAF9);
}

.broom-handle {
  width: 2rpx;
  height: 24rpx;
  background: #8D6E63;
  border-radius: 1rpx;
  position: absolute;
  top: 6rpx;
  left: 28rpx;
  transform: rotate(-20deg);
}

.broom-bristles {
  width: 15rpx;
  height: 6rpx;
  background: #A1887F;
  border-radius: 0 0 8rpx 8rpx;
  position: absolute;
  top: 26rpx;
  left: 22rpx;
  transform: rotate(-20deg);
}

.sparkle {
  width: 3rpx;
  height: 3rpx;
  background: #FFD700;
  border-radius: 50%;
  position: absolute;
  top: 12rpx;
  left: 15rpx;
  animation: sparkle 2s infinite;
}

.sparkle.small {
  width: 2rpx;
  height: 2rpx;
  top: 18rpx;
  left: 20rpx;
  animation-delay: 1s;
}

@keyframes sparkle {
  0%, 100% { opacity: 0.3; transform: scale(0.8); }
  50% { opacity: 1; transform: scale(1.2); }
}

.service-name {
  font-size: 22rpx;
  color: #8D6E63;
  font-weight: 600;
  text-align: center;
}

.service-count {
  font-size: 18rpx;
  color: #FF8F00;
  font-weight: 500;
  text-align: center;
}

.service-emoji {
  font-size: 32rpx;
}

/* 活动区域 */
.activities-section {
  margin: 32rpx 0;
}

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
  background: linear-gradient(135deg, #E1F5FE, #B3E5FC);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  position: relative;
  overflow: hidden;
}

.activity-image.volunteer {
  background: linear-gradient(135deg, #E8F5E8, #C8E6C9);
}

.activity-badge {
  position: absolute;
  top: 4rpx;
  right: 4rpx;
  background: rgba(76, 175, 80, 0.9);
  color: white;
  padding: 2rpx 6rpx;
  border-radius: 8rpx;
  font-size: 16rpx;
  font-weight: 500;
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

.activity-participants {
  font-size: 20rpx;
  color: #FF8F00;
  font-weight: 500;
}

.activity-action {
  display: flex;
  align-items: center;
}

.join-btn {
  background: linear-gradient(135deg, #4CAF50, #66BB6A);
  color: white;
  padding: 12rpx 20rpx;
  border-radius: 20rpx;
  font-size: 22rpx;
  font-weight: 500;
  box-shadow: 0 3rpx 10rpx rgba(76, 175, 80, 0.3);
  transition: all 0.3s ease;
}

.join-btn:active {
  transform: scale(0.95);
  box-shadow: 0 2rpx 8rpx rgba(76, 175, 80, 0.4);
}


/* 浮动操作按钮 */
.fab {
  position: fixed;
  bottom: 140rpx;
  right: 30rpx;
  width: 100rpx;
  height: 100rpx;
  background: linear-gradient(135deg, #FF8F00, #FFB74D);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6rpx 20rpx rgba(255, 143, 0, 0.4);
  z-index: 99;
  transition: all 0.3s ease;
}

.fab:active {
  transform: scale(0.95);
  box-shadow: 0 3rpx 12rpx rgba(255, 143, 0, 0.5);
}

.fab-icon {
  font-size: 40rpx;
  color: white;
  font-weight: 300;
}

/* 底部间距 */
.bottom-spacing {
  height: 30rpx;
}

/* 响应式设计 */
@media screen and (max-width: 750rpx) {
  .main-content {
    padding: 0 20rpx;
  }
  
  .card-content {
    padding: 20rpx;
  }
  
  .services-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12rpx;
  }
  
  .service-item {
    padding: 16rpx 8rpx;
  }
  
  .service-icon {
    width: 50rpx;
    height: 50rpx;
  }
  
  .service-emoji {
    font-size: 28rpx;
  }
  
  .service-name {
    font-size: 20rpx;
  }
  
  .service-count {
    font-size: 16rpx;
  }
  
  .activities-grid {
    grid-template-columns: 1fr;
    gap: 12rpx;
  }
  
  .fab {
    bottom: 130rpx;
    right: 20rpx;
    width: 80rpx;
    height: 80rpx;
  }
  
  .fab-icon {
    font-size: 32rpx;
  }
}

@media screen and (max-width: 600rpx) {
  .services-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .service-item {
    padding: 16rpx 8rpx;
  }
  
  .service-icon {
    width: 50rpx;
    height: 50rpx;
  }
  
  .service-name {
    font-size: 18rpx;
  }
  
  .section-title {
    font-size: 28rpx;
  }
  
  .card-title {
    font-size: 24rpx;
  }
  
  .card-subtitle {
    font-size: 20rpx;
  }
}
</style>
