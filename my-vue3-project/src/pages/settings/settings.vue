<template>
  <view class="settings-container">
    <view class="setting-group">
      <view class="group-title">账号与安全</view>
      <view class="setting-item" @click="navigateTo('/pages/change-password/change-password')">
        <text class="item-text">修改登录密码</text>
        <text class="item-arrow">→</text>
      </view>
      <view class="setting-item" @click="handleBindPhone">
        <text class="item-text">绑定手机号</text>
        <view class="item-right">
          <text class="item-value" :class="{ 'unbound': !userPhone }">{{ displayPhone }}</text>
          <text class="item-arrow">→</text>
        </view>
      </view>
    </view>

    <view class="setting-group">
      <view class="group-title">通用</view>
      <view class="setting-item">
        <text class="item-text">接收新消息通知</text>
        <switch :checked="true" color="#4CAF50" style="transform:scale(0.8)" />
      </view>
      <view class="setting-item" @click="clearCache">
        <text class="item-text">清除本地缓存</text>
        <view class="item-right">
          <text class="item-value">{{ cacheSize }}</text>
          <text class="item-arrow">→</text>
        </view>
      </view>
    </view>

    <view class="setting-group">
      <view class="group-title">关于</view>
      <view class="setting-item" @click="navigateTo('/pages/user-agreement/user-agreement')">
        <text class="item-text">用户协议与隐私政策</text>
        <text class="item-arrow">→</text>
      </view>
      <view class="setting-item" @click="checkUpdate">
        <text class="item-text">检查版本更新</text>
        <view class="item-right">
          <text class="item-value">v1.0.0</text>
          <text class="item-arrow">→</text>
        </view>
      </view>
    </view>

    <view class="logout-btn" @click="logout">
      <text class="logout-text">退出登录</text>
    </view>
  </view>
</template>
<script setup>
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getUserProfile, unbindPhone } from '@/api/user'

// --- 新增：手机号绑定逻辑 ---
const userPhone = ref('')

// 计算属性：如果有手机号则脱敏显示，否则显示未绑定
const displayPhone = computed(() => {
  if (!userPhone.value) return '未绑定'
  return userPhone.value.replace(/^(\d{3})\d{4}(\d{4})$/, '$1****$2')
})

// 每次进入设置页，都重新获取一下用户信息（确保绑定回来后状态更新）
onShow(async () => {
  try {
    const res = await getUserProfile()
    if (res.data && res.data.phone) {
      userPhone.value = res.data.phone
    } else {
      userPhone.value = ''
    }
  } catch (e) {
    console.error('获取用户信息失败', e)
  }
})

// 修改点击绑定手机号的逻辑
const handleBindPhone = () => {
  if (userPhone.value) {
    // 已经绑定时，弹出解除绑定提示
    uni.showModal({
      title: '解除绑定',
      content: `确定要解除与手机号 ${displayPhone.value} 的绑定吗？`,
      confirmColor: '#F44336',
      success: async (res) => {
        if (res.confirm) {
          try {
            uni.showLoading({ title: '解绑中...' })
            await unbindPhone() // 调用解绑接口
            userPhone.value = '' // 清空前端显示的手机号
            uni.hideLoading()
            uni.showToast({ title: '已成功解除绑定', icon: 'success' })
          } catch (error) {
            uni.hideLoading()
            uni.showToast({ title: '解绑失败', icon: 'none' })
          }
        }
      }
    })
  } else {
    // 未绑定时，去绑定页面
    uni.navigateTo({ url: '/pages/bind-phone/bind-phone' })
  }
}
// ------------------------

// 模拟缓存大小
const cacheSize = ref('12.5MB')

const navigateTo = (url) => {
  uni.navigateTo({
    url,
    fail: () => {
      uni.showToast({ title: '该功能正在开发中', icon: 'none' })
    }
  })
}

const clearCache = () => {
  if (cacheSize.value === '0.0MB') {
    uni.showToast({ title: '暂无缓存可清', icon: 'none' })
    return
  }
  uni.showLoading({ title: '清理中...' })
  setTimeout(() => {
    uni.hideLoading()
    cacheSize.value = '0.0MB'
    uni.showToast({ title: '清理完成', icon: 'success' })
  }, 800)
}

const checkUpdate = () => {
  uni.showLoading({ title: '检查中...' })
  setTimeout(() => {
    uni.hideLoading()
    uni.showToast({ title: '当前已是最新版本', icon: 'none' })
  }, 600)
}

const logout = () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    confirmColor: '#F44336',
    success: (res) => {
      if (res.confirm) {
        uni.removeStorageSync('token')
        uni.reLaunch({ url: '/pages/login/login' })
      }
    }
  })
}
</script>

<style scoped>
.settings-container {
  min-height: 100vh;
  background-color: #F5F7FA;
  padding-bottom: 60rpx;
}

.setting-group {
  background-color: #fff;
  margin-bottom: 24rpx;
}

.group-title {
  padding: 20rpx 30rpx 10rpx;
  font-size: 24rpx;
  color: #888;
  background-color: #F5F7FA;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx 30rpx;
  background-color: #fff;
  border-bottom: 1rpx solid #F0F0F0;
  transition: background-color 0.2s;
}

.setting-item:active {
  background-color: #F8F9FA;
}

.setting-item:last-child {
  border-bottom: none;
}

.item-text {
  font-size: 30rpx;
  color: #333;
}

.item-right {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.item-value {
  font-size: 26rpx;
  color: #999;
}

.item-arrow {
  color: #CCC;
  font-size: 28rpx;
}

/* 退出登录按钮独立样式 */
.logout-btn {
  margin: 60rpx 30rpx 0;
  background-color: #fff;
  border-radius: 16rpx;
  padding: 32rpx 0;
  text-align: center;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
  transition: transform 0.2s;
}

.logout-btn:active {
  transform: scale(0.98);
}

.logout-text {
  font-size: 32rpx;
  color: #F44336; /* 醒目的红色 */
  font-weight: 500;
}
.unbound {
  color: #F44336 !important; /* 未绑定时显示红色提示 */
}
</style>