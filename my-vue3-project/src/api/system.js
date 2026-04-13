/**
 * 系统配置模块API
 * 基于后端接口需求文档实现
 */

import api from '@/utils/api'

/**
 * 获取系统配置
 * @returns {Promise} 系统配置
 */
export const getSystemConfig = () => {
  return api.get('/system/config')
}

/**
 * 获取系统统计
 * @returns {Promise} 系统统计
 */
export const getSystemStats = () => {
  return api.get('/system/stats')
}

/**
 * 健康检查
 * @returns {Promise} 健康状态
 */
export const healthCheck = () => {
  return api.get('/system/health')
}
/**
 * 提交意见反馈
 * @param {Object} data { type, content, contact }
 * @returns {Promise}
 */
export const submitFeedback = (data) => {
  return api.post('/system/feedback', data)
}