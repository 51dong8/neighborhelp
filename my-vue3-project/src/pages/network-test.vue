<template>
  <view class="network-test">
    <view class="header">
      <text class="title">网络与后端联调</text>
    </view>

    <view class="banner">
      <text class="banner-text">
        当前项目已移除 Mock：直连探测与业务请求都会面向真实后端。
      </text>
    </view>

    <view class="test-section">
      <button @click="testDirectProbe" :disabled="loading" class="test-btn">
        {{ loading ? '测试中...' : '直连健康检查 (uni.request)' }}
      </button>

      <button @click="testCorsFetch" :disabled="loading" class="test-btn secondary">
        {{ loading ? '测试中...' : 'Fetch / CORS (仅 H5 有效)' }}
      </button>

      <view class="result" v-if="result">
        <text class="result-title">直连结果:</text>
        <text class="result-content">{{ result }}</text>
      </view>

      <view class="result" v-if="corsResult">
        <text class="result-title">Fetch 结果:</text>
        <text class="result-content">{{ corsResult }}</text>
      </view>
    </view>

    <view class="test-section">
      <button @click="testHealthViaApi" :disabled="loading" class="test-btn">
        {{ loading ? '测试中...' : '经 api.js 请求 /system/health' }}
      </button>

      <view class="result" v-if="apiHealthResult">
        <text class="result-title">api.js 结果:</text>
        <text class="result-content">{{ apiHealthResult }}</text>
      </view>
    </view>

    <view class="test-section">
      <button @click="testRegister" :disabled="loading" class="test-btn">
        {{ loading ? '测试中...' : '注册（随机账号）' }}
      </button>

      <view class="result" v-if="registerResult">
        <text class="result-title">注册:</text>
        <text class="result-content">{{ registerResult }}</text>
      </view>
    </view>

    <view class="test-section">
      <button @click="testLogin" :disabled="loading" class="test-btn">
        {{ loading ? '测试中...' : '登录（上次注册或 demo）' }}
      </button>

      <view class="result" v-if="loginResult">
        <text class="result-title">登录:</text>
        <text class="result-content">{{ loginResult }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { getApiConfig } from '@/utils/apiConfig'
import { healthCheck } from '@/api/system'
import { register, login } from '@/api/auth'

const loading = ref(false)
const result = ref('')
const corsResult = ref('')
const apiHealthResult = ref('')
const registerResult = ref('')
const loginResult = ref('')
const lastCred = ref(null)

const probeBackendHealth = () => {
  const base = getApiConfig().apiBaseUrl.replace(/\/+$/, '')
  return new Promise((resolve) => {
    uni.request({
      url: `${base}/system/health`,
      method: 'GET',
      timeout: 8000,
      success: (res) => {
        const httpOk = res.statusCode >= 200 && res.statusCode < 300
        let bodyOk = httpOk
        try {
          const d = typeof res.data === 'string' ? JSON.parse(res.data) : res.data
          bodyOk =
            httpOk &&
            d &&
            (d.code === 200 || d.data?.status === 'UP' || d.data?.status === 'healthy')
        } catch {
          bodyOk = httpOk
        }
        resolve(!!bodyOk)
      },
      fail: () => resolve(false)
    })
  })
}

const testDirectProbe = async () => {
  loading.value = true
  result.value = '测试中...'
  try {
    const ok = await probeBackendHealth()
    const base = getApiConfig().apiBaseUrl
    result.value = ok
      ? `成功: 可达 ${base}/system/health`
      : `失败: 无法访问 ${base}（请确认后端已启动）`
  } catch (e) {
    result.value = `失败: ${e?.message || e}`
  } finally {
    loading.value = false
  }
}

const testCorsFetch = async () => {
  loading.value = true
  corsResult.value = '测试中...'
  try {
    const base = getApiConfig().apiBaseUrl.replace(/\/+$/, '')
    if (typeof fetch === 'undefined') {
      corsResult.value = '当前环境无 fetch，已跳过（小程序端请用直连探测）'
      return
    }
    const r = await fetch(`${base}/system/health`, {
      method: 'GET',
      credentials: 'omit'
    })
    const text = await r.text()
    let parsed
    try {
      parsed = JSON.parse(text)
    } catch {
      parsed = text
    }
    corsResult.value = `HTTP ${r.status} ${typeof parsed === 'string' ? parsed : JSON.stringify(parsed)}`
  } catch (e) {
    corsResult.value = `失败: ${e?.message || e}（多为 CORS 或网络）`
  } finally {
    loading.value = false
  }
}

const testHealthViaApi = async () => {
  loading.value = true
  apiHealthResult.value = '测试中...'
  try {
    const res = await healthCheck()
    apiHealthResult.value = JSON.stringify(res.data ?? res)
  } catch (e) {
    apiHealthResult.value = `失败: ${e?.message || JSON.stringify(e)}`
  } finally {
    loading.value = false
  }
}

const testRegister = async () => {
  loading.value = true
  registerResult.value = '测试中...'
  try {
    const testData = {
      username: `netuser${Date.now()}`,
      email: `net${Date.now()}@example.com`,
      password: '123456'
    }
    const res = await register(testData)
    lastCred.value = { username: testData.username, password: testData.password }
    registerResult.value = JSON.stringify(res.data ?? res)
  } catch (e) {
    registerResult.value = `失败: ${e?.message || JSON.stringify(e)}`
  } finally {
    loading.value = false
  }
}

const testLogin = async () => {
  loading.value = true
  loginResult.value = '测试中...'
  try {
    const cred = lastCred.value || { username: 'demo', password: '123456' }
    const res = await login(cred)
    loginResult.value = JSON.stringify({
      code: res.code,
      hasToken: !!(res.data && res.data.token)
    })
  } catch (e) {
    loginResult.value = `失败: ${e?.message || JSON.stringify(e)}`
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.network-test {
  padding: 20rpx;
  background: #f5f5f5;
  min-height: 100vh;
}

.banner {
  background: #e8f5e9;
  padding: 16rpx 20rpx;
  border-radius: 10rpx;
  margin-bottom: 24rpx;
}

.banner-text {
  font-size: 22rpx;
  color: #2e7d32;
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

.test-section {
  background: white;
  border-radius: 10rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.test-btn {
  width: 100%;
  background: #007aff;
  color: white;
  border: none;
  padding: 20rpx;
  border-radius: 8rpx;
  font-size: 28rpx;
  margin-bottom: 20rpx;
}

.test-btn.secondary {
  background: #5c6bc0;
}

.test-btn:disabled {
  background: #ccc;
}

.result {
  background: #f8f8f8;
  padding: 20rpx;
  border-radius: 8rpx;
}

.result-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 10rpx;
}

.result-content {
  font-size: 24rpx;
  color: #666;
  word-break: break-all;
  line-height: 1.5;
}
</style>
