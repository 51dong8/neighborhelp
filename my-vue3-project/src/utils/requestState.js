/**
 * 全局请求状态与可选的请求包装器（非 HTTP 核心逻辑）。
 * 业务 HTTP 请统一使用 @/utils/api.js；此处仅用于加载态/错误态/重试包装等可选能力。
 */

import { reactive } from 'vue'

export const requestState = reactive({
  globalLoading: false,
  authLoading: false,
  userLoading: false,
  requestLoading: false,
  orderLoading: false,
  serviceLoading: false,
  systemLoading: false,
  lastError: null,
  errorCount: 0,
  isOnline: true,
  lastRequestTime: null
})

export const setLoading = (module, loading) => {
  if (module === 'global') {
    requestState.globalLoading = loading
  } else {
    requestState[`${module}Loading`] = loading
  }
}

export const setError = (error) => {
  requestState.lastError = error
  requestState.errorCount++
  console.error('API Error:', error)
}

export const clearError = () => {
  requestState.lastError = null
  requestState.errorCount = 0
}

export const setNetworkStatus = (isOnline) => {
  requestState.isOnline = isOnline
}

export const updateLastRequestTime = () => {
  requestState.lastRequestTime = new Date().toISOString()
}

export const getModuleLoading = (module) => {
  return requestState[`${module}Loading`] || false
}

export const isAnyLoading = () => {
  return Object.keys(requestState).some(
    (key) => key.endsWith('Loading') && requestState[key]
  )
}

export const resetAllLoading = () => {
  Object.keys(requestState).forEach((key) => {
    if (key.endsWith('Loading')) {
      requestState[key] = false
    }
  })
}

export const createApiWrapper = (apiFunction, module, options = {}) => {
  const {
    showLoading = false,
    showError = true,
    retryCount = 0,
    retryDelay = 1000
  } = options

  return async (...args) => {
    let attempts = 0
    const maxAttempts = retryCount + 1

    while (attempts < maxAttempts) {
      try {
        if (showLoading) {
          setLoading(module, true)
        }
        updateLastRequestTime()
        const result = await apiFunction(...args)
        if (attempts > 0) {
          console.log(`API重试成功: ${module}`)
        }
        return result
      } catch (error) {
        attempts++
        setError({
          module,
          error,
          attempt: attempts,
          timestamp: new Date().toISOString()
        })
        const navOnline =
          typeof navigator !== 'undefined' && navigator.onLine !== false
        if (!navOnline) {
          setNetworkStatus(false)
          throw new Error('网络连接已断开，请检查网络设置')
        }
        setNetworkStatus(true)
        if (attempts >= maxAttempts) {
          if (showError) {
            console.error(`API请求失败 (${module}):`, error)
          }
          throw error
        }
        if (attempts < maxAttempts) {
          console.log(`API重试 ${attempts}/${maxAttempts}: ${module}`)
          await new Promise((resolve) => setTimeout(resolve, retryDelay * attempts))
        }
      } finally {
        if (showLoading) {
          setLoading(module, false)
        }
      }
    }
  }
}

export const batchApiCalls = async (apiCalls, options = {}) => {
  const { showLoading = true, showError = true, failFast = false } = options

  if (showLoading) {
    setLoading('global', true)
  }

  try {
    updateLastRequestTime()
    if (failFast) {
      return await Promise.all(apiCalls)
    }
    const results = await Promise.allSettled(apiCalls)
    return results.map((result, index) => {
      if (result.status === 'fulfilled') {
        return result.value
      }
      setError({
        module: `batch_${index}`,
        error: result.reason,
        timestamp: new Date().toISOString()
      })
      return null
    })
  } catch (error) {
    setError({
      module: 'batch',
      error,
      timestamp: new Date().toISOString()
    })
    throw error
  } finally {
    if (showLoading) {
      setLoading('global', false)
    }
  }
}

export const createCachedApiWrapper = (apiFunction, cacheKey, cacheTime = 300000) => {
  const cache = new Map()
  return async (...args) => {
    const key = `${cacheKey}_${JSON.stringify(args)}`
    const now = Date.now()
    if (cache.has(key)) {
      const { data, timestamp } = cache.get(key)
      if (now - timestamp < cacheTime) {
        console.log(`使用缓存数据: ${cacheKey}`)
        return data
      }
      cache.delete(key)
    }
    const result = await apiFunction(...args)
    cache.set(key, { data: result, timestamp: now })
    return result
  }
}

export const initNetworkListener = () => {
  const handleOnline = () => {
    setNetworkStatus(true)
    console.log('网络已连接')
  }
  const handleOffline = () => {
    setNetworkStatus(false)
    console.log('网络已断开')
  }
  if (typeof window !== 'undefined') {
    window.addEventListener('online', handleOnline)
    window.addEventListener('offline', handleOffline)
  }
  return () => {
    if (typeof window !== 'undefined') {
      window.removeEventListener('online', handleOnline)
      window.removeEventListener('offline', handleOffline)
    }
  }
}
