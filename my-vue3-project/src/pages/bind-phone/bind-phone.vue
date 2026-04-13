<template>
  <view class="bind-container">
    <view class="header">
      <text class="title">绑定手机号</text>
      <text class="subtitle">为了保障您的账号安全，请绑定手机号码</text>
    </view>

    <view class="form-group">
      <view class="input-item">
        <text class="prefix">+86</text>
        <input v-model="phone" type="number" maxlength="11" placeholder="请输入真实手机号" />
      </view>

      <view class="input-item">
        <input v-model="code" type="number" maxlength="6" placeholder="请输入6位验证码" />
        <button class="code-btn" :disabled="countdown > 0" @click="handleSendCode">
          {{ countdown > 0 ? `${countdown}s 后重新获取` : '获取验证码' }}
        </button>
      </view>
    </view>

    <button class="submit-btn" :disabled="!isFormValid" @click="handleSubmit">
      确认绑定
    </button>
  </view>
</template>
<script setup>
import { ref, computed, onUnmounted } from 'vue'
// 修改这里：直接引入具体的函数，而不是整个 api 对象
import { sendCode } from '@/api/auth'
import { bindPhone } from '@/api/user'

const phone = ref('')
const code = ref('')
const countdown = ref(0)
let timer = null

const isFormValid = computed(() => {
  return /^1[3-9]\d{9}$/.test(phone.value) && code.value.length === 6
})

// 修改调用方式
const handleSendCode = async () => {
  if (!/^1[3-9]\d{9}$/.test(phone.value)) {
    return uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
  }

  try {
    uni.showLoading({ title: '发送中...' })
    
    // 【修改点】：直接调用 sendCode
    await sendCode({ phone: phone.value }) 
    
    uni.hideLoading()
    uni.showToast({ title: '验证码已发送', icon: 'success' })
    
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) clearInterval(timer)
    }, 1000)
  } catch (error) {
    uni.hideLoading()
    uni.showToast({ title: error.message || '发送失败', icon: 'none' })
  }
}

// 修改调用方式
const handleSubmit = async () => {
  try {
    uni.showLoading({ title: '绑定中...' })
    
    // 【修改点】：直接调用 bindPhone
    await bindPhone({ 
      phone: phone.value, 
      code: code.value 
    })
    
    uni.hideLoading()
    uni.showToast({ title: '绑定成功', icon: 'success' })
    // 延迟跳转
    setTimeout(() => {
      // 明确指定返回的页面，而不是依赖页面栈
      uni.redirectTo({
        url: '/pages/settings/settings'
      })
    }, 1500)
  } catch (error) {
    uni.hideLoading()
    uni.showToast({ title: error.message || '绑定失败', icon: 'none' })
  }
}

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.bind-container {
  padding: 40rpx;
  background-color: #fff;
  min-height: 100vh;
}

.header {
  margin-bottom: 60rpx;
}
.title {
  font-size: 48rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 16rpx;
}
.subtitle {
  font-size: 28rpx;
  color: #999;
}

.input-item {
  display: flex;
  align-items: center;
  border-bottom: 1rpx solid #eee;
  padding: 30rpx 0;
  margin-bottom: 20rpx;
}
.prefix {
  font-size: 32rpx;
  color: #333;
  margin-right: 20rpx;
  font-weight: 500;
}
input {
  flex: 1;
  font-size: 32rpx;
}
.code-btn {
  background: none;
  font-size: 28rpx;
  color: #4CAF50;
  padding: 0;
  margin: 0;
  border: none;
}
.code-btn::after {
  border: none;
}
.code-btn[disabled] {
  color: #ccc;
  background: none;
}

.submit-btn {
  margin-top: 80rpx;
  background: #4CAF50;
  color: #fff;
  border-radius: 40rpx;
  font-size: 32rpx;
}
.submit-btn[disabled] {
  background: #A5D6A7;
  color: rgba(255, 255, 255, 0.8);
}
</style>