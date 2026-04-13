<template>
  <view class="order-detail-container">
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
        <text class="page-title">订单详情</text>
      </view>
      <view class="nav-right">
        <view class="more-btn" @click="showMoreOptions">
          <text class="more-icon">⋯</text>
        </view>
      </view>
    </view>

    <!-- 主内容区域 -->
    <view class="main-content">
      <!-- 订单状态卡片 -->
      <view class="status-card">
        <view class="status-header">
          <view class="status-icon" :class="getStatusClass(order.status)">
            <text class="status-emoji">{{ getStatusEmoji(order.status) }}</text>
          </view>
          <view class="status-info">
            <text class="status-title">{{ getStatusTitle(order.status) }}</text>
            <text class="status-desc">{{ getStatusDesc(order.status) }}</text>
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

        <view v-if="order.status === 'cancelled' && order.cancelReason" class="cancel-banner">
          <text class="cancel-banner-title">取消原因</text>
          <text class="cancel-banner-text">{{ order.cancelReason }}</text>
        </view>
      </view>

      <!-- 服务信息卡片 -->
      <view class="service-card">
        <view class="card-header">
          <view class="service-icon" :class="getServiceIconClass(order.serviceType)">
            <text class="service-emoji">{{ getServiceEmoji(order.serviceType) }}</text>
          </view>
          <view class="service-info">
            <text class="service-title">{{ order.title }}</text>
            <text class="service-type">{{ getServiceTypeName(order.serviceType) }}</text>
          </view>
          <view class="reward-info">
            <text class="reward-amount">{{ order.rewardAmount }}</text>
            <text class="reward-unit">元</text>
          </view>
        </view>
        
        <view class="service-description">
          <text class="description-text">{{ order.description }}</text>
        </view>
      </view>

      <!-- 时间地点信息 -->
      <view class="info-card">
        <view class="card-title">服务信息</view>
        <view class="info-list">
          <view class="info-item">
            <view class="info-icon">⏰</view>
            <view class="info-content">
              <text class="info-label">服务时间</text>
              <text class="info-value">{{ order.expectedTime }}</text>
            </view>
          </view>
          <view class="info-item">
            <view class="info-icon">📍</view>
            <view class="info-content">
              <text class="info-label">服务地点</text>
              <text class="info-value">{{ order.location }}</text>
            </view>
          </view>
          <view class="info-item">
            <view class="info-icon">📅</view>
            <view class="info-content">
              <text class="info-label">接单时间</text>
              <text class="info-value">{{ order.acceptedAt }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 服务详情 -->
      <view class="details-card" v-if="order.serviceDetails">
        <view class="card-title">服务详情</view>
        <view class="details-content">
          <!-- 帮我取快递详情 -->
          <view v-if="order.serviceType === 'package'" class="service-details">
            <view class="detail-item">
              <text class="detail-label">取件位置</text>
              <text class="detail-value">{{ order.serviceDetails.pickupLocation }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">取件码</text>
              <text class="detail-value">{{ order.serviceDetails.pickupCode }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">取件时间</text>
              <text class="detail-value">{{ order.serviceDetails.pickupTime }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">物品大小</text>
              <text class="detail-value">{{ getItemSizeText(order.serviceDetails.itemSize) }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">物品重量</text>
              <text class="detail-value">{{ getItemWeightText(order.serviceDetails.itemWeight) }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">快递公司</text>
              <text class="detail-value">{{ order.serviceDetails.courierCompany }}</text>
            </view>
          </view>

          <!-- 帮我照顾宠物详情 -->
          <view v-else-if="order.serviceType === 'pet'" class="service-details">
            <view class="detail-item">
              <text class="detail-label">动物类型</text>
              <text class="detail-value">{{ order.serviceDetails.animalType }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">动物名字</text>
              <text class="detail-value">{{ order.serviceDetails.animalName }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">食物类型</text>
              <text class="detail-value">{{ order.serviceDetails.foodType }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">喂食频率</text>
              <text class="detail-value">{{ getFeedingFrequencyText(order.serviceDetails.feedingFrequency) }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">主人联系方式</text>
              <text class="detail-value">{{ order.serviceDetails.ownerContact }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">主人微信</text>
              <text class="detail-value">{{ order.serviceDetails.ownerWechat }}</text>
            </view>
          </view>

          <!-- 帮我家电维修详情 -->
          <view v-else-if="order.serviceType === 'repair'" class="service-details">
            <view class="detail-item">
              <text class="detail-label">家电类型</text>
              <text class="detail-value">{{ order.serviceDetails.applianceType }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">损坏情况</text>
              <text class="detail-value">{{ order.serviceDetails.damageDescription }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">购买时间</text>
              <text class="detail-value">{{ order.serviceDetails.purchaseTime }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">品牌型号</text>
              <text class="detail-value">{{ order.serviceDetails.brandModel }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">联系方式</text>
              <text class="detail-value">{{ order.serviceDetails.phone }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">微信号</text>
              <text class="detail-value">{{ order.serviceDetails.wechat }}</text>
            </view>
          </view>

          <!-- 帮我买菜详情 -->
          <view v-else-if="order.serviceType === 'shopping'" class="service-details">
            <view class="detail-item">
              <text class="detail-label">购物清单</text>
              <text class="detail-value">{{ order.serviceDetails.groceryList }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">客户地址</text>
              <text class="detail-value">{{ order.serviceDetails.customerAddress }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">客户电话</text>
              <text class="detail-value">{{ order.serviceDetails.customerPhone }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">客户微信</text>
              <text class="detail-value">{{ order.serviceDetails.customerWechat }}</text>
            </view>
          </view>

          <!-- 帮我家清洁详情 -->
          <view v-else-if="order.serviceType === 'cleaning'" class="service-details">
            <view class="detail-item">
              <text class="detail-label">清洁项目</text>
              <text class="detail-value">{{ order.serviceDetails.cleaningItems }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">脏污程度</text>
              <text class="detail-value">{{ getDirtLevelText(order.serviceDetails.dirtLevel) }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">预估价格</text>
              <text class="detail-value">{{ order.serviceDetails.estimatedPrice }}元/小时</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">时间偏好</text>
              <text class="detail-value">{{ getTimePreferenceText(order.serviceDetails.timePreference) }}</text>
            </view>
          </view>

          <!-- 帮我做美食详情 -->
          <view v-else-if="order.serviceType === 'cooking'" class="service-details">
            <view class="detail-item">
              <text class="detail-label">美食名称</text>
              <text class="detail-value">{{ order.serviceDetails.dishName }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">制作时间</text>
              <text class="detail-value">{{ order.serviceDetails.cookingTime }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">制作地点</text>
              <text class="detail-value">{{ order.serviceDetails.cookingLocation }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">联系电话</text>
              <text class="detail-value">{{ order.serviceDetails.phone }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">微信号</text>
              <text class="detail-value">{{ order.serviceDetails.wechat }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">预估价格</text>
              <text class="detail-value">{{ order.serviceDetails.estimatedPrice }}元</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">美食要求</text>
              <text class="detail-value">{{ order.serviceDetails.requirements }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 发布者信息 -->
      <view class="publisher-card">
        <view class="card-title">发布者信息</view>
        <view class="publisher-info">
          <view class="publisher-avatar">
            <text class="avatar-text">{{ order.publisher.username.charAt(0) }}</text>
          </view>
          <view class="publisher-details">
            <view class="publisher-name-row">
              <text class="publisher-name">{{ order.publisher.username }}</text>
              <view class="rating-badge">
                <text class="rating-star">⭐</text>
                <text class="rating-score">{{ order.publisher.rating }}</text>
              </view>
            </view>
            <text class="publisher-phone">📞 {{ order.publisher.phone }}</text>
            <text class="publisher-wechat">💬 {{ order.publisher.wechat }}</text>
          </view>
          <view class="contact-actions">
            <view class="contact-btn" @click="makePhoneCall">
              <text class="contact-icon">📞</text>
            </view>
            <view class="contact-btn" @click="copyWechat">
              <text class="contact-icon">💬</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 操作按钮区域 -->
      <view class="action-section">
        <view class="action-buttons">
          <!-- 已接单状态 -->
          <view v-if="order.status === 'active'" class="action-row">
            <view class="action-btn secondary" @click="cancelOrder">
              <text class="btn-text">取消接单</text>
            </view>
            <view class="action-btn primary" @click="startService">
              <text class="btn-text">开始服务</text>
            </view>
          </view>

          <!-- 进行中状态 -->
          <view v-else-if="order.status === 'in_progress'" class="action-row">
            <view class="action-btn secondary" @click="updateProgress">
              <text class="btn-text">更新进度</text>
            </view>
            <view class="action-btn success" @click="completeService">
              <text class="btn-text">完成服务</text>
            </view>
          </view>

          <!-- 已完成状态 -->
          <view v-else-if="order.status === 'completed'" class="action-row">
            <view class="action-btn secondary" @click="viewServiceResult">
              <text class="btn-text">查看结果</text>
            </view>
            <view class="action-btn primary" @click="rateService">
              <text class="btn-text">评价服务</text>
            </view>
          </view>

          <!-- 已取消状态 -->
          <view v-else-if="order.status === 'cancelled'" class="action-row">
            <view class="action-btn secondary" @click="viewCancelReason">
              <text class="btn-text">查看原因</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getOrderDetail, startService as apiStartService, updateProgress as apiUpdateProgress, completeService as apiCompleteService, cancelOrder as apiCancelOrder } from '@/api/order'

// 响应式数据
const orderId = ref('')
const order = ref({})
const isLoading = ref(false)

// 服务类型映射
const serviceTypeMap = {
  package: '帮我取快递',
  pet: '帮我照顾宠物',
  repair: '帮我家电维修',
  shopping: '帮我买菜',
  cleaning: '帮我家清洁',
  cooking: '帮我做美食'
}

// 生命周期
onMounted(() => {
  loadOrderDetail()
})

// 方法
const loadOrderDetail = async () => {
  // 从URL参数获取订单ID
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options
  orderId.value = options.id || '1'

  isLoading.value = true
  try {
    const res = await getOrderDetail(orderId.value)
    // 后端返回 Result<OrderVO>
    order.value = res.data || res?.data?.data || {}
  } catch (e) {
    console.error('加载订单详情失败', e)
    uni.showToast({
      title: '加载订单失败',
      icon: 'none'
    })
  } finally {
    isLoading.value = false
  }
}

const goBack = () => {
  uni.navigateBack()
}

const showMoreOptions = () => {
  uni.showActionSheet({
    itemList: ['分享订单', '举报问题', '联系客服'],
    success: (res) => {
      switch (res.tapIndex) {
        case 0:
          uni.showToast({
            title: '分享功能开发中',
            icon: 'none'
          })
          break
        case 1:
          uni.showModal({
            title: '举报问题',
            content: '请描述您遇到的问题',
            success: (res) => {
              if (res.confirm) {
                uni.showToast({
                  title: '举报已提交',
                  icon: 'success'
                })
              }
            }
          })
          break
        case 2:
          uni.showModal({
            title: '联系客服',
            content: '客服电话：400-123-4567',
            success: (res) => {
              if (res.confirm) {
                uni.makePhoneCall({
                  phoneNumber: '400-123-4567'
                })
              }
            }
          })
          break
      }
    }
  })
}

const getStatusClass = (status) => {
  const statusMap = {
    active: 'status-active',
    in_progress: 'status-progress',
    completed: 'status-completed',
    cancelled: 'status-cancelled'
  }
  return statusMap[status] || 'status-active'
}

const getStatusEmoji = (status) => {
  const statusMap = {
    active: '⏳',
    in_progress: '🔄',
    completed: '✅',
    cancelled: '❌'
  }
  return statusMap[status] || '⏳'
}

const getStatusTitle = (status) => {
  const statusMap = {
    active: '已接单',
    in_progress: '服务中',
    completed: '已完成',
    cancelled: '已取消'
  }
  return statusMap[status] || '已接单'
}

const getStatusDesc = (status) => {
  const statusMap = {
    active: '请及时开始服务',
    in_progress: '服务正在进行中',
    completed: '服务已完成，感谢您的帮助',
    cancelled: '订单已取消'
  }
  return statusMap[status] || '请及时开始服务'
}

const getServiceIconClass = (serviceType) => {
  return `${serviceType}-icon`
}

const getServiceEmoji = (serviceType) => {
  const emojiMap = {
    package: '📦',
    pet: '🐕',
    repair: '🔧',
    shopping: '🛒',
    cleaning: '🧹',
    cooking: '🍳'
  }
  return emojiMap[serviceType] || '📦'
}

const getServiceTypeName = (serviceType) => {
  return serviceTypeMap[serviceType] || '未知服务'
}

const getItemSizeText = (size) => {
  const sizeMap = {
    small: '小件',
    medium: '中件',
    large: '大件'
  }
  return sizeMap[size] || '中件'
}

const getItemWeightText = (weight) => {
  const weightMap = {
    light: '轻',
    medium: '中等',
    heavy: '重'
  }
  return weightMap[weight] || '中等'
}

const getFeedingFrequencyText = (frequency) => {
  const frequencyMap = {
    once: '一天一次',
    twice: '一天两次',
    three: '一天三次'
  }
  return frequencyMap[frequency] || '一天两次'
}

const getDirtLevelText = (level) => {
  const levelMap = {
    light: '轻微',
    normal: '一般',
    severe: '严重'
  }
  return levelMap[level] || '一般'
}

const getTimePreferenceText = (preference) => {
  const preferenceMap = {
    morning: '上午',
    afternoon: '下午',
    evening: '晚上',
    flexible: '灵活'
  }
  return preferenceMap[preference] || '灵活'
}

const makePhoneCall = () => {
  uni.makePhoneCall({
    phoneNumber: order.value.publisher.phone
  })
}

const copyWechat = () => {
  uni.setClipboardData({
    data: order.value.publisher.wechat,
    success: () => {
      uni.showToast({
        title: '微信号已复制',
        icon: 'success'
      })
    }
  })
}

const startService = () => {
  uni.showModal({
    title: '开始服务',
    content: '确定要开始服务吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await apiStartService(orderId.value, {})
          await loadOrderDetail()
          uni.showToast({
            title: '服务已开始',
            icon: 'success'
          })
        } catch (e) {
          uni.showToast({ title: '操作失败', icon: 'none' })
        }
      }
    }
  })
}

const updateProgress = () => {
  uni.showActionSheet({
    itemList: ['10% - 准备中', '30% - 进行中', '50% - 一半完成', '80% - 即将完成', '90% - 收尾中'],
    success: async (res) => {
      const progressMap = [10, 30, 50, 80, 90]
      const progress = progressMap[res.tapIndex]
      try {
        await apiUpdateProgress(orderId.value, { progress })
        await loadOrderDetail()
        uni.showToast({
          title: '进度已更新',
          icon: 'success'
        })
      } catch (e) {
        uni.showToast({ title: '操作失败', icon: 'none' })
      }
    }
  })
}

const completeService = () => {
  uni.showModal({
    title: '完成服务',
    content: '确定要完成服务吗？完成后将获得积分奖励。',
    success: async (res) => {
      if (res.confirm) {
        try {
          await apiCompleteService(orderId.value, { message: '已完成服务' })
          await loadOrderDetail()
          uni.showToast({
            title: '服务已完成！',
            icon: 'success'
          })
        } catch (e) {
          uni.showToast({ title: '操作失败', icon: 'none' })
        }
      }
    }
  })
}

const cancelOrder = () => {
  uni.showModal({
    title: '取消接单',
    content: '确定要取消接单吗？取消后订单将重新开放。',
    success: async (res) => {
      if (res.confirm) {
        try {
          await apiCancelOrder(orderId.value, { reason: '用户主动取消' })
          await loadOrderDetail()
          uni.showToast({
            title: '订单已取消',
            icon: 'success'
          })
        } catch (e) {
          uni.showToast({ title: '操作失败', icon: 'none' })
        }
      }
    }
  })
}

const viewServiceResult = () => {
  uni.showModal({
    title: '服务结果',
    content: '服务已成功完成，发布者已确认收货。',
    showCancel: false
  })
}

const rateService = () => {
  uni.showActionSheet({
    itemList: ['⭐ 1分 - 很差', '⭐⭐ 2分 - 较差', '⭐⭐⭐ 3分 - 一般', '⭐⭐⭐⭐ 4分 - 很好', '⭐⭐⭐⭐⭐ 5分 - 优秀'],
    success: (res) => {
      const rating = res.tapIndex + 1
      uni.showModal({
        title: '评价成功',
        content: `您给发布者打了 ${rating} 分`,
        showCancel: false
      })
    }
  })
}

const viewCancelReason = () => {
  uni.showModal({
    title: '取消原因',
    content: '订单因发布者取消而结束。',
    showCancel: false
  })
}
</script>

<style scoped>
.order-detail-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
}

/* 状态栏 */
.status-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8rpx 24rpx;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20rpx);
}

.time {
  color: white;
  font-size: 28rpx;
  font-weight: 600;
}

.status-icons {
  display: flex;
  gap: 8rpx;
}

.signal-icon, .wifi-icon, .battery-icon {
  color: white;
  font-size: 24rpx;
}

/* 顶部导航栏 */
.top-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 24rpx;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20rpx);
  border-bottom: 1rpx solid rgba(255, 255, 255, 0.1);
}

.nav-left, .nav-right {
  width: 80rpx;
  display: flex;
  justify-content: center;
}

.back-btn, .more-btn {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(20rpx);
}

.back-icon, .more-icon {
  color: white;
  font-size: 32rpx;
  font-weight: bold;
}

.page-title {
  color: white;
  font-size: 36rpx;
  font-weight: 600;
}

/* 主内容区域 */
.main-content {
  flex: 1;
  padding: 20rpx;
  overflow: hidden;
}

/* 订单状态卡片 */
.status-card {
  background: white;
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);
}

.status-header {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-bottom: 20rpx;
}

.status-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.status-icon.status-active {
  background: linear-gradient(135deg, #4CAF50, #45a049);
}

.status-icon.status-progress {
  background: linear-gradient(135deg, #2196F3, #1976D2);
}

.status-icon.status-completed {
  background: linear-gradient(135deg, #FF9800, #F57C00);
}

.status-icon.status-cancelled {
  background: linear-gradient(135deg, #F44336, #D32F2F);
}

.cancel-banner {
  margin-top: 18rpx;
  padding: 16rpx;
  background: #fff3e0;
  border-radius: 16rpx;
}

.cancel-banner-title {
  display: block;
  font-size: 24rpx;
  color: #8d6e63;
  font-weight: 600;
  margin-bottom: 6rpx;
}

.cancel-banner-text {
  display: block;
  font-size: 24rpx;
  color: #5d4037;
  word-break: break-all;
  line-height: 1.5;
}

.status-emoji {
  font-size: 36rpx;
}

.status-info {
  flex: 1;
}

.status-title {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 8rpx;
}

.status-desc {
  display: block;
  font-size: 26rpx;
  color: #666;
}

/* 进度条 */
.progress-section {
  margin-top: 20rpx;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.progress-label {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.progress-percent {
  font-size: 28rpx;
  color: #2196F3;
  font-weight: 600;
}

.progress-bar {
  height: 12rpx;
  background: #E0E0E0;
  border-radius: 6rpx;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #2196F3, #21CBF3);
  border-radius: 6rpx;
  transition: width 0.3s ease;
}

/* 服务信息卡片 */
.service-card {
  background: white;
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-bottom: 16rpx;
}

.service-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.service-icon.package-icon {
  background: linear-gradient(135deg, #FF6B6B, #FF8E8E);
}

.service-icon.pet-icon {
  background: linear-gradient(135deg, #4ECDC4, #44A08D);
}

.service-icon.repair-icon {
  background: linear-gradient(135deg, #45B7D1, #96C93D);
}

.service-icon.shopping-icon {
  background: linear-gradient(135deg, #F093FB, #F5576C);
}

.service-icon.cleaning-icon {
  background: linear-gradient(135deg, #4FACFE, #00F2FE);
}

.service-icon.cooking-icon {
  background: linear-gradient(135deg, #43E97B, #38F9D7);
}

.service-emoji {
  font-size: 36rpx;
}

.service-info {
  flex: 1;
}

.service-title {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 8rpx;
}

.service-type {
  display: block;
  font-size: 24rpx;
  color: #666;
}

.reward-info {
  text-align: right;
}

.reward-amount {
  display: block;
  font-size: 36rpx;
  font-weight: 600;
  color: #FF6B6B;
}

.reward-unit {
  display: block;
  font-size: 24rpx;
  color: #666;
}

.service-description {
  margin-top: 16rpx;
}

.description-text {
  font-size: 28rpx;
  color: #666;
  line-height: 1.6;
}

/* 信息卡片 */
.info-card {
  background: white;
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);
}

.card-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 20rpx;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.info-icon {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  background: #F5F5F5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
}

.info-content {
  flex: 1;
}

.info-label {
  display: block;
  font-size: 24rpx;
  color: #666;
  margin-bottom: 4rpx;
}

.info-value {
  display: block;
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

/* 服务详情卡片 */
.details-card {
  background: white;
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);
}

.details-content {
  margin-top: 16rpx;
}

.service-details {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12rpx 0;
  border-bottom: 1rpx solid #F0F0F0;
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-label {
  font-size: 26rpx;
  color: #666;
  font-weight: 500;
}

.detail-value {
  font-size: 26rpx;
  color: #333;
  text-align: right;
  flex: 1;
  margin-left: 20rpx;
}

/* 发布者信息卡片 */
.publisher-card {
  background: white;
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);
}

.publisher-info {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.publisher-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-text {
  color: white;
  font-size: 32rpx;
  font-weight: 600;
}

.publisher-details {
  flex: 1;
}

.publisher-name-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 8rpx;
}

.publisher-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
}

.rating-badge {
  display: flex;
  align-items: center;
  gap: 4rpx;
  background: #FFF3E0;
  padding: 4rpx 12rpx;
  border-radius: 20rpx;
}

.rating-star {
  font-size: 20rpx;
}

.rating-score {
  font-size: 22rpx;
  color: #FF9800;
  font-weight: 600;
}

.publisher-phone, .publisher-wechat {
  display: block;
  font-size: 24rpx;
  color: #666;
  margin-bottom: 4rpx;
}

.contact-actions {
  display: flex;
  gap: 12rpx;
}

.contact-btn {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: #F5F5F5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.contact-icon {
  font-size: 28rpx;
}

/* 操作按钮区域 */
.action-section {
  margin-top: 20rpx;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.action-row {
  display: flex;
  gap: 16rpx;
}

.action-btn {
  flex: 1;
  height: 88rpx;
  border-radius: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30rpx;
  font-weight: 600;
  transition: all 0.3s ease;
}

.action-btn.primary {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.action-btn.secondary {
  background: #F5F5F5;
  color: #666;
}

.action-btn.success {
  background: linear-gradient(135deg, #4CAF50, #45a049);
  color: white;
}

.action-btn.danger {
  background: linear-gradient(135deg, #F44336, #D32F2F);
  color: white;
}

.btn-text {
  font-size: 30rpx;
  font-weight: 600;
}

/* 响应式设计 */
@media screen and (max-width: 750rpx) {
  .main-content {
    padding: 16rpx;
  }
  
  .status-card, .service-card, .info-card, .details-card, .publisher-card {
    padding: 20rpx;
    margin-bottom: 16rpx;
  }
  
  .card-title {
    font-size: 28rpx;
  }
  
  .service-title {
    font-size: 28rpx;
  }
  
  .action-btn {
    height: 80rpx;
    font-size: 28rpx;
  }
}
</style>
