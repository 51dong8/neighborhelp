/**
 * 用户认证模块 API
 * 与后端 AuthController 的 JSON RequestBody 保持一致。
 */

import api from '@/utils/api'

/**
 * 用户注册
 * @param {Object} userData
 * @param {String} userData.username
 * @param {String} userData.password
 * @param {String} userData.email
 * @returns {Promise}
 */
export const register = (userData = {}) => {
  return api.post('/auth/register', {
    username: (userData.username || '').trim(),
    password: userData.password || '',
    email: (userData.email || '').trim(),
    emailCode: (userData.emailCode || '').trim() // 新增这一行
  })
}

/**
 * 用户登录
 * @param {Object} loginData
 * @param {String} loginData.username
 * @param {String} loginData.password
 * @returns {Promise}
 */
export const login = (loginData = {}) => {
  return api.post('/auth/login', {
    username: (loginData.username || '').trim(),
    password: loginData.password || ''
  })
}

/**
 * 刷新 Token
 * @param {String} refreshToken
 * @returns {Promise}
 */
export const refreshToken = (refreshToken) => {
  return api.post('/auth/refresh', { refreshToken })
}

/**
 * 用户登出
 * @param {String} refreshToken
 * @returns {Promise}
 */
export const logout = (refreshToken) => {
  return api.post('/auth/logout', { refreshToken })
}

/**
 * 检查登录状态
 * @returns {Boolean}
 */
export const isLoggedIn = () => {
  const token = uni.getStorageSync('token')
  const userInfo = uni.getStorageSync('userInfo')
  return !!(token && userInfo)
}

/**
 * 获取当前用户信息
 * @returns {Object|null}
 */
export const getCurrentUser = () => {
  return uni.getStorageSync('userInfo') || null
}

/**
 * 保存用户登录信息
 * @param {Object} loginResult
 */
export const saveLoginInfo = (loginResult) => {
  const { token, refreshToken, userInfo } = loginResult

  uni.setStorageSync('token', token)
  uni.setStorageSync('refreshToken', refreshToken)
  uni.setStorageSync('userInfo', userInfo)
  
  // 【新增】登录时，如果后端userInfo里带有社区状态，一并保存
  if (userInfo) {
     uni.setStorageSync('authStatus', userInfo.authStatus || 0)
     // 如果后端userInfo没有返回小区名字，这里先清空，等首页或者其他地方再去查
     if (userInfo.communityName) {
         uni.setStorageSync('communityName', userInfo.communityName)
     } else {
         uni.removeStorageSync('communityName') 
     }
  }
}

/**
 * 清除用户登录信息
 */
export const clearLoginInfo = () => {
  uni.removeStorageSync('token')
  uni.removeStorageSync('refreshToken')
  uni.removeStorageSync('userInfo')
  
  // 【新增】登出时必须清空社区相关缓存，防止串号！
  uni.removeStorageSync('communityName')
  uni.removeStorageSync('authStatus')
}

/**
 * 发送手机验证码
 * @param {Object} data
 * @param {String} data.phone 手机号
 * @returns {Promise}
 */
export const sendCode = (data) => {
  return api.post('/auth/send-code', data)
}

/**
 * 通过手机号重置密码
 * @param {Object} data 
 * @param {String} data.phone 
 * @param {String} data.code 
 * @param {String} data.newPassword 
 * @returns {Promise}
 */
export const resetPasswordByPhone = (data) => {
  return api.post('/auth/reset-password-phone', data)
}
/**
 * 自动刷新 Token
 * @returns {Promise}
 */
export const autoRefreshToken = async () => {
  try {
    const refreshTokenValue = uni.getStorageSync('refreshToken')
    if (!refreshTokenValue) {
      throw new Error('No refresh token')
    }

    const result = await refreshToken(refreshTokenValue)
    if (result.code === 200) {
      uni.setStorageSync('token', result.data.token)
      uni.setStorageSync('refreshToken', result.data.refreshToken)
      return result.data
    }
  } catch (error) {
    console.error('Token refresh failed:', error)
    clearLoginInfo()
    throw error
  }
}

export const sendEmailCode = (email) => {
  return api.post('/auth/send-email-code', { email })
}
