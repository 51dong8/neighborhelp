<template>
  <view class="api-status-container">
    <view class="header">
      <text class="title">API 与后端状态</text>
    </view>

    <view class="banner">
      <text class="banner-text">
        当前项目已移除 Mock，请求将始终走真实后端；后端状态可通过直连健康检查确认。
      </text>
    </view>

    <view class="status-section">
      <view class="status-card">
        <view class="status-item">
          <text class="status-label">数据模式</text>
          <text class="status-value api-mode">真实后端</text>
        </view>

        <view class="status-item">
          <text class="status-label">后端可达</text>
          <text
            class="status-value"
            :class="backendOnline ? 'online' : 'offline'"
          >{{ backendOnline ? '在线' : '离线' }}</text>
        </view>

        <view class="status-item column">
          <text class="status-label">apiBaseUrl</text>
          <text class="status-mono">{{ apiBaseUrl }}</text>
        </view>
      </view>
    </view>

    <view class="action-section">
      <button @click="checkBackend" :disabled="checking" class="action-btn primary">
        {{ checking ? '检测中...' : '检测后端连通性' }}
      </button>
    </view>

    <view class="info-section">
      <text class="info-title">说明</text>
      <text class="info-text">• 当前已移除 Mock，所有业务请求都会访问真实 API。</text>
      <text class="info-text">• 开发环境默认地址：http://localhost:8080/api。</text>
      <text class="info-text">• 生产环境保留线上域名配置。</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getApiConfig } from '@/utils/apiConfig'

const backendOnline = ref(false)
const checking = ref(false)
const apiBaseUrl = ref('')

const refreshConfig = () => {
  const c = getApiConfig()
  apiBaseUrl.value = c.apiBaseUrl
}

const checkBackend = async () => {
  checking.value = true
  try {
    const base = getApiConfig().apiBaseUrl.replace(/\/+$/, '')
    backendOnline.value = await new Promise((resolve) => {
      uni.request({
        url: `${base}/system/health`,
        method: 'GET',
        timeout: 8000,
        success: (res) => resolve(res.statusCode >= 200 && res.statusCode < 300),
        fail: () => resolve(false)
      })
    })
  } finally {
    checking.value = false
  }
}

onMounted(() => {
  refreshConfig()
  checkBackend()
})
</script>

<style scoped>
.api-status-container {
  padding: 20rpx;
  background: #f5f5f5;
  min-height: 100vh;
}

.banner {
  background: #fff8e1;
  padding: 16rpx 20rpx;
  border-radius: 12rpx;
  margin-bottom: 24rpx;
}

.banner-text {
  font-size: 22rpx;
  color: #5d4037;
  line-height: 1.5;
}

.header {
  text-align: center;
  margin-bottom: 24rpx;
}

.title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
}

.status-section {
  margin-bottom: 40rpx;
}

.status-card {
  background: white;
  border-radius: 16rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.status-item.column {
  flex-direction: column;
  align-items: flex-start;
}

.status-item:last-child {
  margin-bottom: 0;
}

.status-label {
  font-size: 28rpx;
  color: #666;
}

.status-mono {
  font-size: 22rpx;
  color: #333;
  word-break: break-all;
  margin-top: 8rpx;
}

.status-value {
  font-size: 28rpx;
  font-weight: bold;
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
}

.api-mode {
  background: #e8f5e8;
  color: #4caf50;
}

.online {
  background: #e8f5e8;
  color: #4caf50;
}

.offline {
  background: #ffebee;
  color: #f44336;
}

.action-section {
  margin-bottom: 40rpx;
}

.action-btn {
  width: 100%;
  padding: 20rpx;
  margin-bottom: 20rpx;
  border-radius: 12rpx;
  font-size: 28rpx;
  font-weight: bold;
  border: none;
}

.action-btn.primary {
  background: #4caf50;
  color: white;
}

.action-btn:disabled {
  background: #ccc;
  color: #666;
}

.info-section {
  background: white;
  border-radius: 16rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
}

.info-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
  display: block;
}

.info-text {
  font-size: 24rpx;
  color: #666;
  line-height: 1.6;
  margin-bottom: 12rpx;
  display: block;
}
</style>
