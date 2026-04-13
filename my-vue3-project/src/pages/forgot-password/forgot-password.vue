<template>
  <view class="forgot-container">
    <view class="header">
      <text class="title">找回密码</text>
      <text class="subtitle">通过已绑定的手机号重置登录密码</text>
    </view>

    <view class="form-group">
      <view class="input-item">
        <input v-model="phone" type="number" maxlength="11" placeholder="请输入已绑定的手机号" />
      </view>

      <view class="input-item">
        <input v-model="code" type="number" maxlength="6" placeholder="请输入6位验证码" />
        <button class="code-btn" :disabled="countdown > 0" @click="handleSendCode">
          {{ countdown > 0 ? `${countdown}s 后重新获取` : '获取验证码' }}
        </button>
      </view>

      <view class="input-item">
        <input v-model="newPassword" type="password" placeholder="请输入新密码(至少6位)" />
      </view>
    </view>

    <button class="submit-btn" :disabled="!isFormValid" @click="handleSubmit">
      确认重置
    </button>
  </view>
</template>

<script setup>
import { ref, computed, onUnmounted } from 'vue'
import { sendCode, resetPasswordByPhone } from '@/api/auth'

const phone = ref('')
const code = ref('')
const newPassword = ref('')
const countdown = ref(0)
let timer = null

const isFormValid = computed(() => {
  return /^1[3-9]\d{9}$/.test(phone.value) && code.value.length === 6 && newPassword.value.length >= 6
})

const handleSendCode = async () => {
  if (!/^1[3-9]\d{9}$/.test(phone.value)) {
    return uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
  }
  try {
    uni.showLoading({ title: '发送中...' })
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

const handleSubmit = async () => {
  try {
    uni.showLoading({ title: '重置中...' })
    await resetPasswordByPhone({ 
      phone: phone.value, 
      code: code.value,
      newPassword: newPassword.value
    })
    uni.hideLoading()
    uni.showToast({ title: '密码重置成功，请重新登录', icon: 'success' })
    
    setTimeout(() => {
      // 重置成功后返回登录页
      uni.navigateBack()
    }, 1500)
  } catch (error) {
    uni.hideLoading()
    uni.showToast({ title: error.message || '重置失败', icon: 'none' })
  }
}

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.forgot-container { padding: 40rpx; background-color: #fff; min-height: 100vh; }
.header { margin-bottom: 60rpx; }
.title { font-size: 48rpx; font-weight: bold; color: #333; display: block; margin-bottom: 16rpx; }
.subtitle { font-size: 28rpx; color: #999; }
.input-item { display: flex; align-items: center; border-bottom: 1rpx solid #eee; padding: 30rpx 0; margin-bottom: 20rpx; }
input { flex: 1; font-size: 32rpx; }
.code-btn { background: none; font-size: 28rpx; color: #4CAF50; padding: 0; margin: 0; border: none; }
.code-btn::after { border: none; }
.code-btn[disabled] { color: #ccc; background: none; }
.submit-btn { margin-top: 80rpx; background: #4CAF50; color: #fff; border-radius: 40rpx; font-size: 32rpx; }
.submit-btn[disabled] { background: #A5D6A7; color: rgba(255, 255, 255, 0.8); }
</style>