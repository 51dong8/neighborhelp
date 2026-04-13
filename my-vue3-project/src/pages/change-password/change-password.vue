<template>
  <view class="change-pwd-container">
    <view class="form-group">
      <view class="input-item">
        <text class="label">原密码</text>
        <input 
          class="input" 
          type="password" 
          v-model="form.oldPassword" 
          placeholder="请输入当前使用的密码" 
          placeholder-class="placeholder-style"
        />
      </view>

      <view class="input-item">
        <text class="label">新密码</text>
        <input 
          class="input" 
          type="password" 
          v-model="form.newPassword" 
          placeholder="请输入新密码 (6-20位字符)" 
          placeholder-class="placeholder-style"
          maxlength="20"
        />
      </view>

      <view class="input-item border-none">
        <text class="label">确认密码</text>
        <input 
          class="input" 
          type="password" 
          v-model="form.confirmPassword" 
          placeholder="请再次输入新密码" 
          placeholder-class="placeholder-style"
          maxlength="20"
        />
      </view>
    </view>

    <view class="submit-btn" :class="{ 'is-disabled': isSubmitting }" @click="handleSubmit">
      <text class="btn-text">{{ isSubmitting ? '提交中...' : '确认修改' }}</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { changePassword } from '@/api/user'

const form = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const isSubmitting = ref(false)

// 提交表单
const handleSubmit = async () => {
  // 防抖，防止重复点击
  if (isSubmitting.value) return

  // 1. 前端基础校验
  if (!form.value.oldPassword) {
    return uni.showToast({ title: '请输入原密码', icon: 'none' })
  }
  if (!form.value.newPassword) {
    return uni.showToast({ title: '请输入新密码', icon: 'none' })
  }
  if (form.value.newPassword.length < 6 || form.value.newPassword.length > 20) {
    return uni.showToast({ title: '新密码长度需在6-20个字符之间', icon: 'none' })
  }
  if (form.value.newPassword !== form.value.confirmPassword) {
    return uni.showToast({ title: '两次输入的新密码不一致', icon: 'none' })
  }
  if (form.value.oldPassword === form.value.newPassword) {
    return uni.showToast({ title: '新密码不能与原密码相同', icon: 'none' })
  }

  isSubmitting.value = true
  uni.showLoading({ title: '提交中' })

  try {
    // 2. 调用后端接口，只需要传 oldPassword 和 newPassword (对应 ChangePasswordDTO)
    const res = await changePassword({
      oldPassword: form.value.oldPassword,
      newPassword: form.value.newPassword
    })

    uni.hideLoading()

    // 假设你的响应结构是成功时进入此 block
    uni.showToast({
      title: '密码修改成功',
      icon: 'success'
    })

    // 3. 修改成功后，为了安全起见，清除本地 token，强制用户用新密码重新登录
    setTimeout(() => {
      uni.removeStorageSync('token')
      uni.reLaunch({
        url: '/pages/login/login'
      })
    }, 1500)

  } catch (error) {
    uni.hideLoading()
    // 错误提示由底层的 request 拦截器处理，或在这里手动捕获展示
    console.error('修改密码失败:', error)
    uni.showToast({
      title: error?.message || '修改失败，请检查原密码是否正确',
      icon: 'none'
    })
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style scoped>
.change-pwd-container {
  min-height: 100vh;
  background-color: #F5F7FA;
  padding: 30rpx;
}

.form-group {
  background-color: #FFFFFF;
  border-radius: 16rpx;
  padding: 0 30rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
}

.input-item {
  display: flex;
  align-items: center;
  padding: 32rpx 0;
  border-bottom: 1rpx solid #F0F0F0;
}

.border-none {
  border-bottom: none;
}

.label {
  width: 140rpx;
  font-size: 30rpx;
  color: #333;
  font-weight: 500;
}

.input {
  flex: 1;
  font-size: 30rpx;
  color: #333;
}

.placeholder-style {
  color: #999;
  font-size: 28rpx;
}

.submit-btn {
  margin-top: 60rpx;
  background: linear-gradient(135deg, #4CAF50, #66BB6A);
  border-radius: 48rpx;
  padding: 28rpx 0;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 6rpx 20rpx rgba(76, 175, 80, 0.3);
  transition: all 0.3s ease;
}

.submit-btn:active {
  transform: scale(0.98);
  box-shadow: 0 2rpx 10rpx rgba(76, 175, 80, 0.2);
}

.submit-btn.is-disabled {
  background: #A5D6A7;
  box-shadow: none;
  pointer-events: none;
}

.btn-text {
  color: #FFFFFF;
  font-size: 32rpx;
  font-weight: 600;
}
</style>