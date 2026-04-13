/**
 * 项目唯一 HTTP 客户端（uni.request + 统一响应处理）。
 * 业务代码请：import api from '@/utils/api' 或 import { xxx } from '@/api'
 */
import { getApiConfig as getRuntimeApiConfig } from './apiConfig'

// API 基础配置
const API_CONFIG = {
  production: {
    baseURL: 'https://pyuirbqiqttu.sealosbja.site/api',
    timeout: 10000
  },
  development: {
    baseURL: 'http://localhost:8080/api',
    timeout: 10000
  }
}

// 获取当前环境配置
const getApiConfig = () => {
  try {
    const runtimeConfig = getRuntimeApiConfig()
    if (runtimeConfig && runtimeConfig.apiBaseUrl) {
      return {
        baseURL: runtimeConfig.apiBaseUrl,
        timeout: 10000
      }
    }
  } catch (error) {
    console.warn('读取运行时 API 配置失败，使用默认配置:', error)
  }

  const isDevelopment = process.env.NODE_ENV === 'development'
  return isDevelopment ? API_CONFIG.development : API_CONFIG.production
}

const config = getApiConfig()

/**
 * 统一请求方法（真实网络请求）
 * @param {Object} options 请求配置
 * @returns {Promise}
 */
const request = (options) => {
  return new Promise((resolve, reject) => {
    const config = getApiConfig()
    const baseURL = config.baseURL
    const fullUrl = options.url.startsWith('http') ? options.url : `${baseURL}${options.url}`

    // 获取 token
    const token = uni.getStorageSync('token')

    // 构建请求头
    const headers = {
      'Content-Type': 'application/json',
      ...options.headers
    }

    // 注入认证头
    if (token) {
      headers.Authorization = `Bearer ${token}`
    }

    console.log('发送 API 请求:', {
      url: fullUrl,
      method: options.method,
      data: options.data,
      headers
    })

    uni.request({
      url: fullUrl,
      method: options.method || 'GET',
      data: options.data || {},
      header: headers,
      timeout: config.timeout,
      success: (res) => {
        console.log('API 响应:', res)
        handleResponse(res, resolve, reject)
      },
      fail: (err) => {
        console.error('API 请求失败详情:', {
          error: err,
          url: fullUrl,
          method: options.method,
          errMsg: err.errMsg,
          statusCode: err.statusCode
        })

        const statusCode = err.statusCode || err.status || 0
        const errMsg = err.errMsg || err.message || '网络请求失败'

        let errorMessage = '网络连接失败，请检查网络设置'

        if (errMsg) {
          if (errMsg.includes('timeout') || errMsg.includes('超时')) {
            errorMessage = '请求超时，请检查网络连接或后端服务是否运行'
          } else if (errMsg.includes('fail') && errMsg.includes('connect')) {
            errorMessage = '无法连接到服务器，请确认后端服务已启动（http://localhost:8080）'
          } else if (errMsg.includes('CORS') || errMsg.includes('跨域')) {
            errorMessage = '跨域请求被阻止，请检查后端 CORS 配置'
          } else {
            errorMessage = `网络错误: ${errMsg}`
          }
        }

        if (statusCode === 0) {
          reject({
            code: 0,
            message: errorMessage,
            statusCode: 0,
            originalError: err
          })
        } else {
          handleHttpError(statusCode, reject)
        }
      }
    })
  })
}

/**
 * 统一响应处理
 * @param {Object} res 响应对象
 * @param {Function} resolve Promise resolve
 * @param {Function} reject Promise reject
 */
const handleResponse = (res, resolve, reject) => {
  const { statusCode, data } = res

  if (statusCode >= 200 && statusCode < 300) {
    if (data && typeof data === 'object') {
      if (data.code === 200) {
        resolve(data)
      } else {
        handleBusinessError(data, reject)
      }
    } else {
      reject({
        code: 500,
        message: '响应数据格式错误',
        data
      })
    }
  } else {
    handleHttpError(statusCode, reject)
  }
}

/**
 * 处理业务错误
 * @param {Object} data 响应数据
 * @param {Function} reject Promise reject
 */
const handleBusinessError = (data, reject) => {
  const { code, message, details } = data

  switch (code) {
    case 400:
      console.error('请求参数错误:', message, details)
      uni.showToast({
        title: message || '请求参数错误',
        icon: 'none',
        duration: 2000
      })
      break

    case 422:
      console.error('参数校验失败:', message, details)
      uni.showToast({
        title: message || '参数校验失败',
        icon: 'none',
        duration: 2000
      })
      break

    case 401:
    case 1005:
    case 1006:
    case 1007:
      console.error('认证失败:', message)
      uni.removeStorageSync('token')
      uni.removeStorageSync('refreshToken')
      uni.removeStorageSync('userInfo')
      uni.showToast({
        title: '登录已过期，请重新登录',
        icon: 'none',
        duration: 2000
      })
      setTimeout(() => {
        uni.reLaunch({
          url: '/pages/login/login'
        })
      }, 2000)
      break

    case 403:
      console.error('无权限:', message)
      uni.showToast({
        title: message || '无权限操作',
        icon: 'none',
        duration: 2000
      })
      break

    case 404:
      console.error('资源不存在:', message)
      uni.showToast({
        title: message || '资源不存在',
        icon: 'none',
        duration: 2000
      })
      break

    case 500:
      console.error('服务器错误:', message)
      uni.showToast({
        title: '服务器错误，请稍后重试',
        icon: 'none',
        duration: 2000
      })
      break

    default:
      console.error('业务错误:', message, 'code=', code)
      uni.showToast({
        title: message || `请求失败(${code || 'UNKNOWN'})`,
        icon: 'none',
        duration: 2000
      })
  }

  reject(data)
}

/**
 * 处理 HTTP 错误
 * @param {Number} statusCode HTTP 状态码
 * @param {Function} reject Promise reject
 */
const handleHttpError = (statusCode, reject) => {
  let message = '网络请求失败'

  switch (statusCode) {
    case 400:
      message = '请求参数错误'
      break
    case 422:
      message = '参数校验失败'
      break
    case 401:
      message = '未授权访问'
      uni.removeStorageSync('token')
      uni.removeStorageSync('refreshToken')
      uni.removeStorageSync('userInfo')
      break
    case 403:
      message = '禁止访问'
      break
    case 404:
      message = '请求的资源不存在'
      break
    case 500:
      message = '服务器内部错误'
      break
    case 502:
      message = '网关错误'
      break
    case 503:
      message = '服务不可用'
      break
    case 504:
      message = '网关超时'
      break
  }

  uni.showToast({
    title: message,
    icon: 'none',
    duration: 2000
  })

  reject({
    code: statusCode,
    message,
    statusCode
  })
}

/**
 * GET 请求
 * @param {String} url 请求 URL
 * @param {Object} params 查询参数
 * @param {Object} options 其他选项
 */
const get = (url, params = {}, options = {}) => {
  const queryString = Object.keys(params)
    .filter((key) => params[key] !== undefined && params[key] !== null)
    .map((key) => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
    .join('&')

  const fullUrl = queryString ? `${url}?${queryString}` : url

  return request({
    url: fullUrl,
    method: 'GET',
    ...options
  })
}

/**
 * POST 请求
 * @param {String} url 请求 URL
 * @param {Object} data 请求数据
 * @param {Object} options 其他选项
 */
const post = (url, data = {}, options = {}) => {
  return request({
    url,
    method: 'POST',
    data,
    ...options
  })
}

/**
 * PUT 请求
 * @param {String} url 请求 URL
 * @param {Object} data 请求数据
 * @param {Object} options 其他选项
 */
const put = (url, data = {}, options = {}) => {
  return request({
    url,
    method: 'PUT',
    data,
    ...options
  })
}

/**
 * DELETE 请求
 * @param {String} url 请求 URL
 * @param {Object} options 其他选项
 */
const del = (url, options = {}) => {
  return request({
    url,
    method: 'DELETE',
    ...options
  })
}

export default {
  request,
  get,
  post,
  put,
  delete: del,
  config
}

export { config as apiConfig }
