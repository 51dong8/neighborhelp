<template>
  <view class="api-test-container">
    <view class="banner">
      <text class="banner-text">联调页：请求始终走 @/utils/api，已不再支持 Mock 数据。</text>
    </view>
    <view class="test-section">
      <text class="section-title">API 连接测试</text>

      <view class="test-item">
        <button @click="testHealthCheck" :disabled="loading">健康检查 /system/health</button>
        <text class="result">{{ healthResult }}</text>
      </view>

      <view class="test-item">
        <button @click="testServiceTypes" :disabled="loading">服务类型列表</button>
        <text class="result">{{ serviceTypesResult }}</text>
      </view>

      <view class="test-item">
        <button @click="testRegister" :disabled="loading">注册（随机账号）</button>
        <text class="result">{{ registerResult }}</text>
      </view>

      <view class="test-item">
        <button @click="testLogin" :disabled="loading">登录（上次注册账号或 demo）</button>
        <text class="result">{{ loginResult }}</text>
      </view>
    </view>

    <view class="config-section">
      <text class="section-title">当前配置</text>
      <text class="config-info">apiBaseUrl: {{ apiBaseUrl }}</text>
      <text class="config-info">mode: REAL_API</text>
      <text class="config-info">NODE_ENV: {{ environment }}</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { healthCheck } from '@/api/system'
import { getServiceTypes } from '@/api/service'
import { register, login } from '@/api/auth'
import { getApiConfig } from '@/utils/apiConfig'

const loading = ref(false)
const healthResult = ref('')
const serviceTypesResult = ref('')
const registerResult = ref('')
const loginResult = ref('')
const lastRegisterCred = ref(null)

const apiBaseUrl = ref('')
const environment = ref(process.env.NODE_ENV || 'unknown')

const refreshConfig = () => {
  const c = getApiConfig()
  apiBaseUrl.value = c.apiBaseUrl
}

onMounted(() => {
  refreshConfig()
})

const testHealthCheck = async () => {
  loading.value = true
  healthResult.value = '请求中...'
  try {
    const res = await healthCheck()
    healthResult.value = JSON.stringify(res.data ?? res)
  } catch (e) {
    healthResult.value = `失败: ${e?.message || JSON.stringify(e)}`
  } finally {
    loading.value = false
  }
}

const testServiceTypes = async () => {
  loading.value = true
  serviceTypesResult.value = '请求中...'
  try {
    const res = await getServiceTypes()
    const list = res.data
    const n = Array.isArray(list) ? list.length : 0
    serviceTypesResult.value = `条数 ${n}: ${JSON.stringify(res.data)}`
  } catch (e) {
    serviceTypesResult.value = `失败: ${e?.message || JSON.stringify(e)}`
  } finally {
    loading.value = false
  }
}

const testRegister = async () => {
  loading.value = true
  registerResult.value = '请求中...'
  try {
    const testData = {
      username: `testuser${Date.now()}`,
      email: `test${Date.now()}@example.com`,
      password: '123456'
    }
    const res = await register(testData)
    lastRegisterCred.value = { username: testData.username, password: testData.password }
    registerResult.value = JSON.stringify(res.data ?? res)
  } catch (e) {
    registerResult.value = `失败: ${e?.message || JSON.stringify(e)}`
  } finally {
    loading.value = false
  }
}

const testLogin = async () => {
  loading.value = true
  loginResult.value = '请求中...'
  try {
    const cred = lastRegisterCred.value || { username: 'demo', password: '123456' }
    const res = await login(cred)
    loginResult.value = JSON.stringify({
      code: res.code,
      hasToken: !!(res.data && res.data.token),
      message: res.message
    })
  } catch (e) {
    loginResult.value = `失败: ${e?.message || JSON.stringify(e)}`
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.api-test-container {
  padding: 20rpx;
  background: #f5f5f5;
  min-height: 100vh;
}

.banner {
  background: #e3f2fd;
  padding: 16rpx 20rpx;
  border-radius: 10rpx;
  margin-bottom: 20rpx;
}

.banner-text {
  font-size: 22rpx;
  color: #1565c0;
  line-height: 1.5;
}

.test-section,
.config-section {
  background: white;
  border-radius: 10rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
  display: block;
}

.test-item {
  margin-bottom: 20rpx;
  padding: 20rpx;
  background: #f8f8f8;
  border-radius: 8rpx;
}

.test-item button {
  background: #007aff;
  color: white;
  border: none;
  padding: 20rpx 40rpx;
  border-radius: 8rpx;
  margin-bottom: 10rpx;
}

.test-item button:disabled {
  background: #ccc;
}

.result {
  font-size: 24rpx;
  color: #666;
  display: block;
  margin-top: 10rpx;
  word-break: break-all;
}

.config-info {
  font-size: 24rpx;
  color: #666;
  display: block;
  margin-bottom: 10rpx;
}
</style>
