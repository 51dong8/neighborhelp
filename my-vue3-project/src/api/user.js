/**
 * 用户管理模块API
 * 基于后端接口需求文档实现
 */

import api from '@/utils/api'

/**
 * 获取用户信息
 * @returns {Promise} 用户信息
 */
export const getUserProfile = () => {
  return api.get('/user/profile')
}

/**
 * 更新用户信息
 * @param {Object} userData 用户数据
 * @param {String} userData.username 用户名
 * @param {String} userData.email 邮箱
 * @param {String} userData.avatar 头像URL
 * @returns {Promise} 更新结果
 */
export const updateUserProfile = (userData) => {
  return api.put('/user/profile', userData)
}

/**
 * 修改密码
 * @param {Object} passwordData 密码数据
 * @param {String} passwordData.oldPassword 原密码
 * @param {String} passwordData.newPassword 新密码
 * @returns {Promise} 修改结果
 */
export const changePassword = (passwordData) => {
  return api.put('/user/password', passwordData)
}

/**
 * 获取当前登录用户积分概况
 * @returns {Promise} 积分信息
 */
export const getUserPoints = () => {
  return api.get('/users/points/current')
}

/**
 * 获取积分记录
 * @param {Number} page 页码
 * @param {Number} limit 每页数量
 * @returns {Promise} 积分记录
 */
export const getPointRecords = (page = 1, limit = 10) => {
  return api.get('/users/points/records', { page, limit })
}

/**
 * 积分商城兑换
 * @param {String} itemName 商品名称
 * @param {Number} cost 消耗积分数
 * @returns {Promise} 兑换结果
 */
export const redeemItem = (itemName, cost) => {
  return api.post('/users/points/redeem', {
    itemName,
    cost
  })
}

/**
 * 获取用户发布的需求
 * @param {Object} params 查询参数
 * @param {String} params.status 需求状态
 * @param {Number} params.page 页码
 * @param {Number} params.limit 每页数量
 * @returns {Promise} 用户需求列表
 */
export const getUserRequests = (params = {}) => {
  return api.get('/user/requests', params)
}

/**
 * 获取用户接收的订单
 * @param {Object} params 查询参数
 * @param {String} params.status 订单状态
 * @param {Number} params.page 页码
 * @param {Number} params.limit 每页数量
 * @returns {Promise} 用户订单列表
 */
export const getUserOrders = (params = {}) => {
  return api.get('/user/orders', params)
}

/**
 * 绑定手机号
 * @param {Object} data 
 * @param {String} data.phone 手机号
 * @param {String} data.code 验证码
 */
export const bindPhone = (data) => {
  return api.put('/user/bind-phone', data)
}

/**
 * 解除绑定手机号
 * 注意：请确保后端 UserController 中已实现对应的接口
 */
export const unbindPhone = () => {
  return api.put('/user/unbind-phone')
}

// ====== 小区与认证相关接口 ======

/**
 * 获取附近/支持的小区列表
 * @returns {Promise} 小区列表数组
 */
export const getCommunityList = () => {
  return api.get('/community/list')
}

/**
 * 提交小区认证 (AI OCR自动审核)
 * @param {Object} data 
 * @param {Number|String} data.communityId 小区ID
 * @param {String} data.materialUrl 物业单据图片URL
 * @returns {Promise} 认证结果
 */
export const submitCommunityAuth = (data) => {
  return api.post('/users/community/auth', data)
}