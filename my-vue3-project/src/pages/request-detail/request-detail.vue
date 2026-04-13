<template>
  <view class="request-detail-container">
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
        <text class="page-title">需求详情</text>
      </view>
      <view class="nav-right">
        <view class="more-btn" @click="showMoreOptions">
          <text class="more-icon">⋯</text>
        </view>
      </view>
    </view>

    <!-- 主内容区域 -->
    <view class="main-content">
      <!-- 需求状态卡片 -->
      <view class="status-card">
        <view class="status-header">
          <view class="status-icon" :class="getStatusClass(request.status)">
            <text class="status-emoji">{{ getStatusEmoji(request.status) }}</text>
          </view>
          <view class="status-info">
            <text class="status-title">{{ getStatusTitle(request.status) }}</text>
            <text class="status-desc">{{ getStatusDesc(request.status) }}</text>
          </view>
        </view>
        
        <!-- 紧急程度标签 -->
        <view class="urgency-badge" :class="getUrgencyClass(request.urgency)">
          <text class="urgency-text">{{ getUrgencyText(request.urgency) }}</text>
        </view>
      </view>

      <!-- 服务信息卡片 -->
      <view class="service-card">
        <view class="card-header">
          <view class="service-icon" :class="getServiceIconClass(request.serviceType)">
            <text class="service-emoji">{{ getServiceEmoji(request.serviceType) }}</text>
          </view>
          <view class="service-info">
            <text class="service-title">{{ request.title }}</text>
            <text class="service-type">{{ getServiceTypeName(request.serviceType) }}</text>
          </view>
          <view class="reward-info">
            <text class="reward-amount">{{ request.rewardAmount }}</text>
            <text class="reward-unit">元</text>
          </view>
        </view>
        
        <view class="service-description">
          <text class="description-text">{{ request.description }}</text>
        </view>
      </view>

      <!-- 时间地点信息 -->
      <view class="info-card">
        <view class="card-title">需求信息</view>
        <view class="info-list">
          <view class="info-item">
            <view class="info-icon">⏰</view>
            <view class="info-content">
              <text class="info-label">期望时间</text>
              <text class="info-value">{{ request.expectedTime }}</text>
            </view>
          </view>
          <view class="info-item">
            <view class="info-icon">📍</view>
            <view class="info-content">
              <text class="info-label">服务地点</text>
              <text class="info-value">{{ request.location }}</text>
            </view>
          </view>
          <view class="info-item">
            <view class="info-icon">📅</view>
            <view class="info-content">
              <text class="info-label">发布时间</text>
              <text class="info-value">{{ request.createdAt }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 服务详情 -->
      <view class="details-card" v-if="request.serviceDetails">
        <view class="card-title">服务详情</view>
        <view class="details-content">
          <!-- 帮我取快递详情 -->
          <view v-if="request.serviceType === 'package'" class="service-details">
            <view class="detail-item">
              <text class="detail-label">取件位置</text>
              <text class="detail-value">{{ request.serviceDetails.pickupLocation }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">取件码</text>
              <text class="detail-value">{{ request.serviceDetails.pickupCode }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">取件时间</text>
              <text class="detail-value">{{ request.serviceDetails.pickupTime }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">物品大小</text>
              <text class="detail-value">{{ getItemSizeText(request.serviceDetails.itemSize) }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">物品重量</text>
              <text class="detail-value">{{ getItemWeightText(request.serviceDetails.itemWeight) }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">快递公司</text>
              <text class="detail-value">{{ request.serviceDetails.courierCompany }}</text>
            </view>
          </view>

          <!-- 帮我照顾宠物详情 -->
          <view v-else-if="request.serviceType === 'pet'" class="service-details">
            <view class="detail-item">
              <text class="detail-label">动物类型</text>
              <text class="detail-value">{{ request.serviceDetails.animalType }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">动物名字</text>
              <text class="detail-value">{{ request.serviceDetails.animalName }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">食物类型</text>
              <text class="detail-value">{{ request.serviceDetails.foodType }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">喂食频率</text>
              <text class="detail-value">{{ getFeedingFrequencyText(request.serviceDetails.feedingFrequency) }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">主人联系方式</text>
              <text class="detail-value">{{ request.serviceDetails.ownerContact }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">主人微信</text>
              <text class="detail-value">{{ request.serviceDetails.ownerWechat }}</text>
            </view>
          </view>

          <!-- 帮我家电维修详情 -->
          <view v-else-if="request.serviceType === 'repair'" class="service-details">
            <view class="detail-item">
              <text class="detail-label">家电类型</text>
              <text class="detail-value">{{ request.serviceDetails.applianceType }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">损坏情况</text>
              <text class="detail-value">{{ request.serviceDetails.damageDescription }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">购买时间</text>
              <text class="detail-value">{{ request.serviceDetails.purchaseTime }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">品牌型号</text>
              <text class="detail-value">{{ request.serviceDetails.brandModel }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">联系方式</text>
              <text class="detail-value">{{ request.serviceDetails.phone }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">微信号</text>
              <text class="detail-value">{{ request.serviceDetails.wechat }}</text>
            </view>
          </view>

          <!-- 帮我买菜详情 -->
          <view v-else-if="request.serviceType === 'shopping'" class="service-details">
            <view class="detail-item">
              <text class="detail-label">购物清单</text>
              <text class="detail-value">{{ request.serviceDetails.groceryList }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">客户地址</text>
              <text class="detail-value">{{ request.serviceDetails.customerAddress }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">客户电话</text>
              <text class="detail-value">{{ request.serviceDetails.customerPhone }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">客户微信</text>
              <text class="detail-value">{{ request.serviceDetails.customerWechat }}</text>
            </view>
          </view>

          <!-- 帮我家清洁详情 -->
          <view v-else-if="request.serviceType === 'cleaning'" class="service-details">
            <view class="detail-item">
              <text class="detail-label">清洁项目</text>
              <text class="detail-value">{{ request.serviceDetails.cleaningItems }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">脏污程度</text>
              <text class="detail-value">{{ getDirtLevelText(request.serviceDetails.dirtLevel) }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">预估价格</text>
              <text class="detail-value">{{ request.serviceDetails.estimatedPrice }}元/小时</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">时间偏好</text>
              <text class="detail-value">{{ getTimePreferenceText(request.serviceDetails.timePreference) }}</text>
            </view>
          </view>

          <!-- 帮我做美食详情 -->
          <view v-else-if="request.serviceType === 'cooking'" class="service-details">
            <view class="detail-item">
              <text class="detail-label">美食名称</text>
              <text class="detail-value">{{ request.serviceDetails.dishName }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">制作时间</text>
              <text class="detail-value">{{ request.serviceDetails.cookingTime }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">制作地点</text>
              <text class="detail-value">{{ request.serviceDetails.cookingLocation }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">联系电话</text>
              <text class="detail-value">{{ request.serviceDetails.phone }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">微信号</text>
              <text class="detail-value">{{ request.serviceDetails.wechat }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">预估价格</text>
              <text class="detail-value">{{ request.serviceDetails.estimatedPrice }}元</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">美食要求</text>
              <text class="detail-value">{{ request.serviceDetails.requirements }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 发布者信息 -->
      <view class="publisher-card">
        <view class="card-title">发布者信息</view>
        <view class="publisher-info">
          <view class="publisher-avatar">
            <text class="avatar-text">{{ request.publisher.username.charAt(0) }}</text>
          </view>
          <view class="publisher-details">
            <view class="publisher-name-row">
              <text class="publisher-name">{{ request.publisher.username }}</text>
              <view class="rating-badge">
                <text class="rating-star">⭐</text>
                <text class="rating-score">{{ request.publisher.rating }}</text>
              </view>
            </view>
            <text class="publisher-phone">📞 {{ request.publisher.phone }}</text>
            <text class="publisher-wechat">💬 {{ request.publisher.wechat }}</text>
            <text class="publisher-stats">帮助次数: {{ request.publisher.helpCount }}次</text>
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

      <!-- 接单记录 -->
      <view class="acceptance-card" v-if="request.acceptedBy">
        <view class="card-title">接单信息</view>
        <view class="acceptance-info">
          <view class="acceptor-avatar">
            <text class="avatar-text">{{ request.acceptedBy.username.charAt(0) }}</text>
          </view>
          <view class="acceptor-details">
            <view class="acceptor-name-row">
              <text class="acceptor-name">{{ request.acceptedBy.username }}</text>
              <view class="rating-badge">
                <text class="rating-star">⭐</text>
                <text class="rating-score">{{ request.acceptedBy.rating }}</text>
              </view>
            </view>
            <text class="acceptance-time">接单时间: {{ request.acceptedAt }}</text>
          </view>
        </view>
      </view>

      <!-- 操作按钮区域 -->
      <view class="action-section">
        <view class="action-buttons">
          <!-- 可接单状态 -->
          <view v-if="request.status === 'active'" class="action-row">
            <view class="action-btn secondary" @click="contactPublisher">
              <text class="btn-text">联系发布者</text>
            </view>
            <view class="action-btn primary" @click="acceptRequest">
              <text class="btn-text">立即接单</text>
            </view>
          </view>

          <!-- 已接单状态 -->
          <view v-else-if="request.status === 'accepted'" class="action-row">
            <view class="action-btn secondary" @click="viewOrderDetail">
              <text class="btn-text">查看订单</text>
            </view>
            <view class="action-btn success" @click="startService">
              <text class="btn-text">开始服务</text>
            </view>
          </view>

          <!-- 进行中状态 -->
          <view v-else-if="request.status === 'in_progress'" class="action-row">
            <view class="action-btn secondary" @click="viewOrderDetail">
              <text class="btn-text">查看订单</text>
            </view>
            <view class="action-btn primary" @click="updateProgress">
              <text class="btn-text">更新进度</text>
            </view>
          </view>

          <!-- 已完成状态 -->
          <view v-else-if="request.status === 'completed'" class="action-row">
            <view class="action-btn secondary" @click="viewServiceResult">
              <text class="btn-text">查看结果</text>
            </view>
            <view class="action-btn primary" @click="rateService">
              <text class="btn-text">评价服务</text>
            </view>
          </view>

          <!-- 已取消状态 -->
          <view v-else-if="request.status === 'cancelled'" class="action-row">
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
import { getRequestDetail } from '@/api/request'

// 响应式数据
const requestId = ref('')
const request = ref({})
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
  loadRequestDetail()
})

// 方法
const loadRequestDetail = async () => {
  // 从URL参数获取需求ID
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options
  requestId.value = options.id || '1'

  isLoading.value = true
  try {
    const res = await getRequestDetail(requestId.value)
    // 后端返回 Result<RequestVO>
    request.value = res.data || res?.data?.data || {}
  } catch (e) {
    console.error('加载需求详情失败', e)
    uni.showToast({
      title: '加载需求失败',
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
    itemList: ['分享需求', '举报问题', '联系客服'],
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
    accepted: 'status-accepted',
    in_progress: 'status-progress',
    completed: 'status-completed',
    cancelled: 'status-cancelled'
  }
  return statusMap[status] || 'status-active'
}

const getStatusEmoji = (status) => {
  const statusMap = {
    active: '⏳',
    accepted: '✅',
    in_progress: '🔄',
    completed: '🎉',
    cancelled: '❌'
  }
  return statusMap[status] || '⏳'
}

const getStatusTitle = (status) => {
  const statusMap = {
    active: '待接单',
    accepted: '已接单',
    in_progress: '服务中',
    completed: '已完成',
    cancelled: '已取消'
  }
  return statusMap[status] || '待接单'
}

const getStatusDesc = (status) => {
  const statusMap = {
    active: '等待有人接单',
    accepted: '已有人接单，等待开始服务',
    in_progress: '服务正在进行中',
    completed: '服务已完成，感谢帮助',
    cancelled: '需求已取消'
  }
  return statusMap[status] || '等待有人接单'
}

const getUrgencyClass = (urgency) => {
  const urgencyMap = {
    low: 'urgency-low',
    normal: 'urgency-normal',
    urgent: 'urgency-urgent',
    emergency: 'urgency-emergency'
  }
  return urgencyMap[urgency] || 'urgency-normal'
}

const getUrgencyText = (urgency) => {
  const urgencyMap = {
    low: '不急',
    normal: '一般',
    urgent: '紧急',
    emergency: '非常紧急'
  }
  return urgencyMap[urgency] || '一般'
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
    phoneNumber: request.value.publisher.phone
  })
}

const copyWechat = () => {
  uni.setClipboardData({
    data: request.value.publisher.wechat,
    success: () => {
      uni.showToast({
        title: '微信号已复制',
        icon: 'success'
      })
    }
  })
}

const contactPublisher = () => {
  uni.showActionSheet({
    itemList: ['拨打电话', '发送微信', '查看资料'],
    success: (res) => {
      switch (res.tapIndex) {
        case 0:
          uni.makePhoneCall({
            phoneNumber: request.value.publisher.phone
          })
          break
        case 1:
          uni.setClipboardData({
            data: request.value.publisher.wechat,
            success: () => {
              uni.showToast({
                title: '微信号已复制',
                icon: 'success'
              })
            }
          })
          break
        case 2:
          uni.showModal({
            title: '用户信息',
            content: `用户名：${request.value.publisher.username}\n评分：${request.value.publisher.rating}⭐\n帮助次数：${request.value.publisher.helpCount}次\n电话：${request.value.publisher.phone}\n微信：${request.value.publisher.wechat}`,
            showCancel: false
          })
          break
      }
    }
  })
}

const acceptRequest = () => {
  uni.showModal({
    title: '接单确认',
    content: '确定要接这个单吗？接单后请及时联系发布者。',
    success: (res) => {
      if (res.confirm) {
        // 更新需求状态
        request.value.status = 'accepted'
        request.value.acceptedBy = {
          username: '当前用户',
          rating: 4.9
        }
        request.value.acceptedAt = '刚刚'
        
        uni.showToast({
          title: '接单成功！',
          icon: 'success'
        })
        
        setTimeout(() => {
          uni.showModal({
            title: '接单成功',
            content: '您已成功接单，请及时联系发布者开始服务。',
            showCancel: false
          })
        }, 1500)
      }
    }
  })
}

const viewOrderDetail = () => {
  uni.navigateTo({
    url: `/pages/order-detail/order-detail?id=${request.value.id}`
  })
}

const startService = () => {
  uni.showModal({
    title: '开始服务',
    content: '确定要开始服务吗？',
    success: (res) => {
      if (res.confirm) {
        request.value.status = 'in_progress'
        uni.showToast({
          title: '服务已开始',
          icon: 'success'
        })
      }
    }
  })
}

const updateProgress = () => {
  uni.showActionSheet({
    itemList: ['10% - 准备中', '30% - 进行中', '50% - 一半完成', '80% - 即将完成', '90% - 收尾中'],
    success: (res) => {
      const progressMap = [10, 30, 50, 80, 90]
      uni.showToast({
        title: `进度已更新为${progressMap[res.tapIndex]}%`,
        icon: 'success'
      })
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
    content: '需求因发布者取消而结束。',
    showCancel: false
  })
}
</script>

<style scoped>
.request-detail-container {
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

/* 需求状态卡片 */
.status-card {
  background: white;
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);
  position: relative;
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

.status-icon.status-accepted {
  background: linear-gradient(135deg, #2196F3, #1976D2);
}

.status-icon.status-progress {
  background: linear-gradient(135deg, #FF9800, #F57C00);
}

.status-icon.status-completed {
  background: linear-gradient(135deg, #9C27B0, #7B1FA2);
}

.status-icon.status-cancelled {
  background: linear-gradient(135deg, #F44336, #D32F2F);
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

/* 紧急程度标签 */
.urgency-badge {
  position: absolute;
  top: 16rpx;
  right: 16rpx;
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  background: white;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.urgency-badge.urgency-low {
  border: 2rpx solid #4CAF50;
}

.urgency-badge.urgency-normal {
  border: 2rpx solid #2196F3;
}

.urgency-badge.urgency-urgent {
  border: 2rpx solid #FF9800;
}

.urgency-badge.urgency-emergency {
  border: 2rpx solid #F44336;
}

.urgency-text {
  font-size: 22rpx;
  font-weight: 600;
  color: #333;
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

.publisher-phone, .publisher-wechat, .publisher-stats {
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

/* 接单信息卡片 */
.acceptance-card {
  background: white;
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);
}

.acceptance-info {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.acceptor-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #4CAF50, #45a049);
  display: flex;
  align-items: center;
  justify-content: center;
}

.acceptor-details {
  flex: 1;
}

.acceptor-name-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 8rpx;
}

.acceptor-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
}

.acceptance-time {
  display: block;
  font-size: 24rpx;
  color: #666;
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
  
  .status-card, .service-card, .info-card, .details-card, .publisher-card, .acceptance-card {
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
