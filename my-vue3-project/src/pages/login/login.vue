<template>
  <view class="login-container">
    <view class="bg-decoration">
      <view class="bg-circle bg-circle-1"></view>
      <view class="bg-circle bg-circle-2"></view>
      <view class="bg-circle bg-circle-3"></view>
    </view>

    <view class="logo-section">
      <view class="logo-decoration">
        <text class="decoration-icon">🏠</text>
        <text class="decoration-icon">❤️</text>
        <text class="decoration-icon">🤝</text>
      </view>
      <view class="logo-container">
        <view class="logo">邻里帮帮</view>
        <view class="logo-subtitle">
          <text class="subtitle-line">让邻里更温暖</text>
          <text class="subtitle-line">让社区更美好</text>
        </view>
      </view>
      <view class="logo-accent">
        <view class="accent-line"></view>
        <text class="accent-text">Neighborhood Helper</text>
        <view class="accent-line"></view>
      </view>
    </view>

    <view class="login-card">
      <view class="tab-switcher">
        <button 
          class="tab-btn" 
          :class="{ active: currentTab === 'login' }" 
          @click="switchTab('login')"
        >
          登录
        </button>
        <button 
          class="tab-btn" 
          :class="{ active: currentTab === 'register' }" 
          @click="switchTab('register')"
        >
          注册
        </button>
          </view>


      <view v-if="currentTab === 'login'" class="auth-form">
        <view class="input-group">
          <input 
            type="text" 
            class="input-field" 
            v-model="loginForm.username"
            placeholder="用户名" 
            @input="clearError('loginUsername')"
          />
          <text class="input-icon">👤</text>
          <view v-if="errors.loginUsername" class="error-message">{{ errors.loginUsername }}</view>
        </view>

        <view class="input-group">
          <input 
            type="password" 
            class="input-field" 
            v-model="loginForm.password"
            placeholder="密码" 
            @input="clearError('loginPassword')"
          />
          <text class="input-icon">🔒</text>
          <view v-if="errors.loginPassword" class="error-message">{{ errors.loginPassword }}</view>
        </view>
              <view class="options-row">
                 <text class="forgot-pwd" @click="goToForgotPwd">忘记密码？</text>
             </view>
        

        <button 
          class="login-btn" 
          :class="{ loading: isLoading }"
          @click="handleLogin"
          :disabled="isLoading"
        >
          <text v-if="!isLoading">登录</text>
          <text v-else>登录中...</text>
        </button>
        
        <view class="agreement-text">
          登录即同意
          <text class="agreement-link" @click="showAgreement">用户协议</text>
          和
          <text class="agreement-link" @click="showPrivacy">隐私政策</text>
        </view>
      </view>

      <view v-if="currentTab === 'register'" class="auth-form">
        <view class="input-group">
          <input 
            type="text" 
            class="input-field" 
            v-model="registerForm.username"
            placeholder="用户名" 
            @input="clearError('registerUsername')"
          />
          <text class="input-icon">👤</text>
          <view v-if="errors.registerUsername" class="error-message">{{ errors.registerUsername }}</view>
        </view>

        <view class="input-group">
          <input 
            type="text" 
            class="input-field" 
            v-model="registerForm.email"
            placeholder="邮箱" 
            @input="clearError('registerEmail')"
          />
          <text class="input-icon">📧</text>
          <view v-if="errors.registerEmail" class="error-message">{{ errors.registerEmail }}</view>
        </view>

        <view class="input-group email-code-group">
          <input 
            type="text" 
            class="input-field code-input" 
            v-model="registerForm.emailCode"
            placeholder="邮箱验证码" 
            @input="clearError('registerEmailCode')"
          />
          <text class="input-icon">🛡️</text>
          
          <button 
            class="get-code-btn" 
            :class="{ 'disabled': countdown > 0 }"
            @click.stop="handleSendEmailCode"
            :disabled="countdown > 0"
          >
            {{ countdown > 0 ? `${countdown}s后重试` : '获取验证码' }}
          </button>
          
          <view v-if="errors.registerEmailCode" class="error-message">{{ errors.registerEmailCode }}</view>
        </view>
        <view class="input-group">
          <input 
            type="password" 
            class="input-field" 
            v-model="registerForm.password"
            placeholder="密码" 
            @input="clearError('registerPassword')"
          />
          <text class="input-icon">🔒</text>
          <view v-if="errors.registerPassword" class="error-message">{{ errors.registerPassword }}</view>
        </view>

        <view class="input-group">
          <input 
            type="password" 
            class="input-field" 
            v-model="registerForm.confirmPassword"
            placeholder="确认密码" 
            @input="clearError('confirmPassword')"
          />
          <text class="input-icon">🔒</text>
          <view v-if="errors.confirmPassword" class="error-message">{{ errors.confirmPassword }}</view>
        </view>


        <button 
          class="login-btn" 
          :class="{ loading: isLoading }"
          @click="handleRegister"
          :disabled="isLoading"
        >
          <text v-if="!isLoading">注册</text>
          <text v-else>注册中...</text>
        </button>

        <view class="agreement-text">
          注册即同意
          <text class="agreement-link" @click="showAgreement">用户协议</text>
          和
          <text class="agreement-link" @click="showPrivacy">隐私政策</text>
        </view>
      </view>
    </view>

    <view class="bottom-illustration">
      <view class="illustration-content">
        <text class="house">🏠</text>
        <text class="tree">🌳</text>
        <text class="person">👥</text>
        <text class="sun">☀️</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { login, register, saveLoginInfo, sendEmailCode } from '@/api/auth'

const currentTab = ref('login')
const isLoading = ref(false)

const countdown = ref(0)
let timer = null

const loginForm = reactive({
  username: '',
  password: ''
})

// 在响应式数据区加上 emailCode 和 倒计时
const registerForm = reactive({
  username: '',
  email: '',
  emailCode: '', // 新增
  password: '',
  confirmPassword: ''
})

const errors = reactive({
  loginUsername: '',
  loginPassword: '',
  registerUsername: '',
  registerEmail: '',
  registerPassword: '',
  confirmPassword: '',
  registerEmailCode: ''
})

const switchTab = (tab: string) => {
  currentTab.value = tab
  clearAllErrors()
}

const clearAllErrors = () => {
  Object.keys(errors).forEach((key) => {
    errors[key as keyof typeof errors] = ''
  })
}

const clearError = (field: string) => {
  errors[field as keyof typeof errors] = ''
}

const validateLoginForm = () => {
  clearAllErrors()
  let isValid = true

  if (!loginForm.username.trim()) {
    errors.loginUsername = '请输入用户名'
    isValid = false
  } else if (loginForm.username.trim().length < 2) {
    errors.loginUsername = '用户名至少2个字符'
    isValid = false
  }

  if (!loginForm.password.trim()) {
    errors.loginPassword = '请输入密码'
    isValid = false
  } else if (loginForm.password.length < 6) {
    errors.loginPassword = '密码至少6个字符'
    isValid = false
  }

  return isValid
}

const validateRegisterForm = () => {
  clearAllErrors()
  let isValid = true

  if (!registerForm.username.trim()) {
    errors.registerUsername = '请输入用户名'
    isValid = false
  } else if (registerForm.username.trim().length < 2) {
    errors.registerUsername = '用户名至少2个字符'
    isValid = false
  } else if (registerForm.username.trim().length > 50) {
    errors.registerUsername = '用户名长度不能超过50个字符'
    isValid = false
  }

  if (!registerForm.email.trim()) {
    errors.registerEmail = '请输入邮箱'
    isValid = false
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(registerForm.email.trim())) {
    errors.registerEmail = '邮箱格式不正确'
    isValid = false
  }
  if (!registerForm.emailCode.trim()) {
    errors.registerEmailCode = '请输入邮箱验证码'
    isValid = false
  }

  if (!registerForm.password.trim()) {
    errors.registerPassword = '请输入密码'
    isValid = false
  } else if (registerForm.password.length < 6) {
    errors.registerPassword = '密码至少6个字符'
    isValid = false
  } else if (registerForm.password.length > 20) {
    errors.registerPassword = '密码长度不能超过20个字符'
    isValid = false
  }

  if (!registerForm.confirmPassword.trim()) {
    errors.confirmPassword = '请确认密码'
    isValid = false
  } else if (registerForm.password !== registerForm.confirmPassword) {
    errors.confirmPassword = '两次输入的密码不一致'
    isValid = false
  }

  return isValid
}

type ApiError = {
  code?: number
  message?: string
  data?: Record<string, string> | null
  originalError?: {
    errMsg?: string
  }
  statusCode?: number
}

const getFirstValidationMessage = (data?: Record<string, string> | null) => {
  if (!data || typeof data !== 'object') {
    return ''
  }
  const firstKey = Object.keys(data)[0]
  return firstKey ? data[firstKey] : ''
}

const applyLoginValidationErrors = (data?: Record<string, string> | null) => {
  if (!data || typeof data !== 'object') {
    return
  }
  if (data.username) {
    errors.loginUsername = data.username
  }
  if (data.password) {
    errors.loginPassword = data.password
  }
}

const applyRegisterValidationErrors = (data?: Record<string, string> | null) => {
  if (!data || typeof data !== 'object') {
    return
  }
  if (data.username) {
    errors.registerUsername = data.username
  }
  if (data.email) {
    errors.registerEmail = data.email
  }
  if (data.password) {
    errors.registerPassword = data.password
  }
}

const extractBackendErrorMessage = (error: ApiError | unknown, fallback: string) => {
  if (!error) {
    return fallback
  }

  if (typeof error === 'string') {
    return error
  }

  const apiError = error as ApiError

  if (typeof apiError.message === 'string' && apiError.message.trim()) {
    return apiError.message.trim()
  }

  const validationMessage = getFirstValidationMessage(apiError.data)
  if (validationMessage) {
    return validationMessage
  }

  if (typeof apiError.originalError?.errMsg === 'string' && apiError.originalError.errMsg.trim()) {
    return apiError.originalError.errMsg.trim()
  }

  return fallback
}

const isNetworkErrorMessage = (message: string) => {
  return (
    message.includes('网络') ||
    message.includes('连接') ||
    message.includes('timeout') ||
    message.includes('超时') ||
    message.includes('CORS') ||
    message.includes('跨域')
  )
}

const showAuthError = (title: string, error: ApiError | unknown, fallback: string) => {
  const message = extractBackendErrorMessage(error, fallback)

  if (isNetworkErrorMessage(message)) {
    uni.showModal({
      title,
      content: message,
      showCancel: false,
      confirmText: '确定'
    })
    return
  }

  uni.showToast({
    title: message,
    icon: 'none',
    duration: 2500
  })
}

const handleLogin = async () => {
  if (!validateLoginForm()) {
    return
  }

  isLoading.value = true

  try {
    const result = await login({
      username: loginForm.username,
      password: loginForm.password
    })

    if (result && result.code === 200 && result.data) {
      saveLoginInfo(result.data)

      uni.showToast({
        title: result.message || '登录成功',
        icon: 'success'
      })

      setTimeout(() => {
        uni.switchTab({
          url: '/pages/home/home'
        })
      }, 1200)
      return
    }

    throw result
  } catch (error: any) {
    console.error('登录失败详情:', error)

    if (error?.code === 422) {
      applyLoginValidationErrors(error.data)
    }

    showAuthError('登录失败', error, '登录失败，请重试')
  } finally {
    isLoading.value = false
  }
}

const goToForgotPwd = () => {
  uni.navigateTo({ url: '/pages/forgot-password/forgot-password' })
}

const handleRegister = async () => {
  if (!validateRegisterForm()) {
    return
  }

  isLoading.value = true

  try {
    const result = await register({
      username: registerForm.username,
      password: registerForm.password,
      email: registerForm.email,
      emailCode: registerForm.emailCode // 新增传参
    })

    if (result && result.code === 200) {
      uni.showToast({
        title: result.message || '注册成功',
        icon: 'success'
      })

      setTimeout(() => {
        switchTab('login')
        loginForm.username = registerForm.username.trim()
        loginForm.password = ''
        registerForm.username = ''
        registerForm.email = ''
        registerForm.password = ''
        registerForm.confirmPassword = ''
      }, 1200)
      return
    }

    throw result
  } catch (error: any) {
    console.error('注册失败详情:', error)

    if (error?.code === 422) {
      applyRegisterValidationErrors(error.data)
    }

    showAuthError('注册失败', error, '注册失败，请重试')
  } finally {
    isLoading.value = false
  }
}

const showAgreement = () => {
  uni.showModal({
    title: '用户协议',
    content: '1. 用户应遵守社区规范\n2. 不得发布违法违规内容\n3. 保护他人隐私和权益\n4. 共建和谐邻里关系',
    showCancel: false
  })
}

const showPrivacy = () => {
  uni.showModal({
    title: '隐私政策',
    content: '1. 我们严格保护用户隐私\n2. 个人信息仅用于服务提供\n3. 不会向第三方泄露信息\n4. 用户可随时管理个人信息',
    showCancel: false
  })
}

const handleSendEmailCode = async () => {
  // 校验邮箱格式
  if (!registerForm.email.trim()) {
    errors.registerEmail = '请先输入邮箱'
    return
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(registerForm.email.trim())) {
    errors.registerEmail = '邮箱格式不正确'
    return
  }

  try {
    uni.showLoading({ title: '发送中...' })
    await sendEmailCode(registerForm.email.trim())
    uni.hideLoading()
    uni.showToast({ title: '验证码已发送', icon: 'success' })
    
    // 开启 60 秒倒计时
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
    
  } catch (err) {
    uni.hideLoading()
    showAuthError('发送失败', err, '验证码发送失败')
  }
}

</script>

<style scoped>

/* 验证码输入框专属样式 */
.email-code-group {
  display: flex;
  align-items: center;
}
.code-input {
  flex: 1;
  padding-right: 220rpx; /* 给右侧按钮留位置 */
}
.get-code-btn {
  position: absolute;
  right: 10rpx;
  top: 50%;
  transform: translateY(-50%);
  height: 80rpx;
  line-height: 80rpx;
  padding: 0 30rpx;
  background: linear-gradient(135deg, #FF8F00, #FFB74D);
  color: white;
  font-size: 26rpx;
  border-radius: 20rpx;
  z-index: 5;
}
.get-code-btn::after {
  border: none;
}
.get-code-btn.disabled {
  background: #E0E0E0;
  color: #999;
}

.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #FFF8E1 0%, #FFE0B2 50%, #FFCC80 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  padding: 60rpx 20rpx 40rpx;
  position: relative;
  overflow-x: hidden;
  box-sizing: border-box;
}
.options-row {  
  display: flex;
  justify-content: flex-end;
  margin-bottom: 30rpx;
}
.forgot-pwd {
  font-size: 26rpx;
  color: #666;
}

/* 背景装饰 */
.bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 0;
}

.bg-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 193, 7, 0.1);
  animation: float 6s ease-in-out infinite;
}

.bg-circle-1 {
  width: 400rpx;
  height: 400rpx;
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.bg-circle-2 {
  width: 300rpx;
  height: 300rpx;
  top: 60%;
  right: 15%;
  animation-delay: 2s;
}

.bg-circle-3 {
  width: 200rpx;
  height: 200rpx;
  top: 30%;
  right: 30%;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-40rpx) rotate(180deg); }
}

/* Logo区域 */
.logo-section {
  text-align: center;
  margin-bottom: 40rpx;
  margin-top: 60rpx;
  animation: slideInDown 0.8s ease-out;
  position: relative;
  z-index: 1;
  padding: 40rpx 20rpx;
}

.logo-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 10rpx;
}

.logo {
  font-size: 56rpx;
  font-weight: 900;
  background: linear-gradient(135deg, #8D6E63 0%, #A1887F 50%, #FF8F00 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 0 8rpx 16rpx rgba(141, 110, 99, 0.3);
  margin-bottom: 16rpx;
  letter-spacing: 6rpx;
  position: relative;
  display: inline-block;
}

.logo::before {
  content: '';
  position: absolute;
  top: -8rpx;
  left: -8rpx;
  right: -8rpx;
  bottom: -8rpx;
  background: linear-gradient(135deg, rgba(255, 143, 0, 0.1), rgba(255, 183, 77, 0.1));
  border-radius: 20rpx;
  z-index: -1;
  opacity: 0.6;
}

.logo-subtitle {
  position: relative;
  display: inline-block;
  padding: 12rpx 28rpx;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 20rpx;
  backdrop-filter: blur(10rpx);
  border: 1rpx solid rgba(255, 255, 255, 0.2);
  text-align: center;
}

.subtitle-line {
  display: block;
  font-size: 30rpx;
  color: #8D6E63;
  opacity: 0.9;
  font-weight: 600;
  line-height: 1.6;
  text-shadow: 0 2rpx 4rpx rgba(0,0,0,0.1);
  margin: 4rpx 0;
  letter-spacing: 1rpx;
}

/* Logo装饰图标 */
.logo-decoration {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20rpx;
  margin-bottom: 20rpx;
  animation: float 3s ease-in-out infinite;
}

.decoration-icon {
  font-size: 32rpx;
  opacity: 0.8;
  animation: bounce 2s ease-in-out infinite;
}

.decoration-icon:nth-child(1) {
  animation-delay: 0s;
}

.decoration-icon:nth-child(2) {
  animation-delay: 0.3s;
}

.decoration-icon:nth-child(3) {
  animation-delay: 0.6s;
}

/* Logo装饰线 */
.logo-accent {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  margin-top: 16rpx;
}

.accent-line {
  width: 60rpx;
  height: 2rpx;
  background: linear-gradient(90deg, transparent, #FF8F00, transparent);
  border-radius: 1rpx;
}

.accent-text {
  font-size: 24rpx;
  color: #8D6E63;
  opacity: 0.7;
  font-weight: 500;
  font-style: italic;
  letter-spacing: 2rpx;
}

/* 弹跳动画 */
@keyframes bounce {
  0%, 20%, 50%, 80%, 100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-10rpx);
  }
  60% {
    transform: translateY(-5rpx);
  }
}

/* 登录卡片 */
.login-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 40rpx;
  padding: 60rpx 50rpx;
  box-shadow: 0 40rpx 80rpx rgba(0,0,0,0.1), 0 0 0 2rpx rgba(255,255,255,0.2);
  backdrop-filter: blur(20rpx);
  border: 2rpx solid rgba(255,255,255,0.3);
  animation: slideInUp 0.8s ease-out 0.2s both;
  position: relative;
  overflow: hidden;
  width: 100%;
  max-width: 600rpx;
  min-width: 320rpx;
  z-index: 1;
  box-sizing: border-box;
}

.login-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 8rpx;
  background: linear-gradient(90deg, #FF8F00, #FFB74D, #FFCC80);
}

/* 切换标签 */
.tab-switcher {
  display: flex;
  background: #F5F5F5;
  border-radius: 24rpx;
  padding: 8rpx;
  margin-bottom: 60rpx;
  position: relative;
}

.tab-btn {
  flex: 1;
  padding: 24rpx 40rpx;
  border: none;
  background: transparent;
  color: #8D6E63;
  font-size: 32rpx;
  font-weight: 600;
  cursor: pointer;
  border-radius: 16rpx;
  transition: all 0.3s ease;
  position: relative;
  z-index: 2;
}

.tab-btn.active {
  background: white;
  color: #FF8F00;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.1);
}

/* 输入框样式 */
.input-group {
  position: relative;
  margin-bottom: 50rpx;
}

.input-field {
  width: 100%;
  height: 110rpx;
  padding: 0 40rpx 0 100rpx;
  border: 4rpx solid #E0E0E0;
  border-radius: 30rpx;
  font-size: 32rpx;
  color: #333;
  background: #FAFAFA;
  transition: all 0.3s ease;
  outline: none;
  box-sizing: border-box;
}

.input-field:focus {
  border-color: #FF8F00;
  background: #FFF;
  box-shadow: 0 0 0 6rpx rgba(255, 143, 0, 0.1);
  transform: translateY(-4rpx);
}

.input-icon {
  position: absolute;
  left: 36rpx;
  top: 50%;
  transform: translateY(-50%);
  color: #8D6E63;
  font-size: 36rpx;
  transition: all 0.3s ease;
  z-index: 2;
}


/* 登录按钮 */
.login-btn {
  width: 100%;
  height: 110rpx;
  background: linear-gradient(135deg, #8D6E63, #A1887F);
  border: none;
  border-radius: 30rpx;
  color: white;
  font-size: 36rpx;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  margin-bottom: 40rpx;
  box-shadow: 0 16rpx 40rpx rgba(141, 110, 99, 0.3);
}

.login-btn:hover {
  background: linear-gradient(135deg, #6D4C41, #8D6E63);
  transform: translateY(-4rpx);
  box-shadow: 0 24rpx 50rpx rgba(141, 110, 99, 0.4);
}

.login-btn:active {
  transform: translateY(0);
}

.login-btn.loading {
  pointer-events: none;
  opacity: 0.8;
}

/* 协议文字 */
.agreement-text {
  text-align: center;
  font-size: 28rpx;
  color: #8D6E63;
  opacity: 0.8;
  line-height: 1.5;
}

.agreement-link {
  color: #FF8F00;
  text-decoration: none;
  font-weight: 500;
  transition: all 0.3s ease;
}

.agreement-link:hover {
  color: #FF6F00;
  text-decoration: underline;
}

/* 错误信息 */
.error-message {
  color: #F44336;
  font-size: 24rpx;
  margin-top: 10rpx;
  margin-left: 10rpx;
}

/* 底部插画 */
.bottom-illustration {
  margin-top: 100rpx;
  text-align: center;
  animation: slideInUp 0.8s ease-out 0.4s both;
  position: relative;
  z-index: 1;
}

.illustration-content {
  display: flex;
  justify-content: space-around;
  align-items: center;
  font-size: 60rpx;
  opacity: 0.8;
}

/* 动画效果 */
@keyframes slideInDown {
  from {
    opacity: 0;
    transform: translateY(-100rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(100rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式设计 - 移动端优化 */
@media screen and (max-width: 750rpx) {
  .login-container {
    padding: 40rpx 20rpx 20rpx;
  }
  
  .logo-section {
    margin-bottom: 30rpx;
    margin-top: 40rpx;
    padding: 20rpx 10rpx;
  }
  
  .logo {
    font-size: 48rpx;
    letter-spacing: 4rpx;
    margin-bottom: 12rpx;
  }
  
  .logo-subtitle {
    padding: 10rpx 20rpx;
  }
  
  .subtitle-line {
    font-size: 26rpx;
    margin: 2rpx 0;
  }
  
  .decoration-icon {
    font-size: 28rpx;
  }
  
  .accent-text {
    font-size: 20rpx;
  }
  
  .login-card {
    padding: 40rpx 30rpx;
    margin: 0 10rpx;
  }
  
  .input-field {
    height: 100rpx;
    font-size: 30rpx;
    padding: 0 30rpx 0 90rpx;
  }
  
  .input-icon {
    font-size: 32rpx;
    left: 30rpx;
  }
  
  .login-btn {
    height: 100rpx;
    font-size: 32rpx;
  }
  
  .tab-btn {
    padding: 20rpx 30rpx;
    font-size: 28rpx;
  }
  
  
  .bottom-illustration {
    margin-top: 60rpx;
  }
  
  .illustration-content {
    font-size: 50rpx;
  }
}

/* 超小屏幕优化 */
@media screen and (max-width: 600rpx) {
  .logo {
    font-size: 42rpx;
    letter-spacing: 3rpx;
  }
  
  .subtitle-line {
    font-size: 24rpx;
  }
  
  .login-card {
    padding: 30rpx 20rpx;
  }
  
  .input-field {
    height: 90rpx;
    font-size: 28rpx;
  }
  
  .login-btn {
    height: 90rpx;
    font-size: 30rpx;
  }
}
</style>
