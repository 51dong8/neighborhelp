/**
 * 订单管理模块API
 * 基于后端接口需求文档实现
 */

import api from '@/utils/api'

/**
 * 接单
 * @param {Object} orderData 订单数据
 * @param {String} orderData.requestId 需求ID
 * @param {String} orderData.message 接单消息
 * @returns {Promise} 接单结果
 */
export const acceptOrder = (orderData) => {
  return api.post('/orders', orderData)
}

/**
 * 获取当前登录用户的接单列表（后端 GET /api/orders 与 /api/user/orders 均为本人维度）
 */
export const getOrders = (params = {}) => {
  return api.get('/orders', params)
}

/**
 * 获取订单详情
 * @param {String} orderId 订单ID
 * @returns {Promise} 订单详情
 */
export const getOrderDetail = (orderId) => {
  return api.get(`/orders/${orderId}`)
}

/**
 * 开始服务
 * @param {String} orderId 订单ID
 * @param {Object} startData 开始数据
 * @param {String} startData.message 开始消息
 * @returns {Promise} 开始结果
 */
export const startService = (orderId, startData) => {
  return api.put(`/orders/${orderId}/start`, startData)
}

/**
 * 更新服务进度
 * @param {String} orderId 订单ID
 * @param {Object} progressData 进度数据
 * @param {Number} progressData.progress 进度百分比
 * @param {String} progressData.message 进度消息
 * @returns {Promise} 更新结果
 */
export const updateProgress = (orderId, progressData) => {
  return api.put(`/orders/${orderId}/progress`, progressData)
}

/**
 * 完成服务
 * @param {String} orderId 订单ID
 * @param {Object} completeData 完成数据
 * @param {String} completeData.message 完成消息
 * @param {Array} completeData.images 完成图片
 * @returns {Promise} 完成结果
 */
export const completeService = (orderId, completeData) => {
  return api.put(`/orders/${orderId}/complete`, completeData)
}

/**
 * 取消订单
 * @param {String} orderId 订单ID
 * @param {Object} cancelData 取消数据
 * @param {String} cancelData.reason 取消原因
 * @returns {Promise} 取消结果
 */
export const cancelOrder = (orderId, cancelData) => {
  return api.put(`/orders/${orderId}/cancel`, cancelData)
}
