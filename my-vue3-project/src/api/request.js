/**
 * 需求管理模块API
 * 基于后端接口需求文档实现
 */

import api from '@/utils/api'

/**
 * 发布需求
 * @param {Object} requestData 需求数据
 * @param {String} requestData.serviceType 服务类型
 * @param {String} requestData.title 需求标题
 * @param {String} requestData.description 需求描述
 * @param {String} requestData.rewardType 报酬类型
 * @param {Number} requestData.rewardAmount 报酬金额
 * @param {String} requestData.expectedTime 期望时间
 * @param {String} requestData.location 地点
 * @param {String} requestData.urgency 紧急程度
 * @param {Object} requestData.serviceDetails 服务详情
 * @returns {Promise} 发布结果
 */
export const createRequest = (requestData) => {
  return api.post('/requests', requestData)
}

/**
 * 获取需求列表
 * @param {Object} params 查询参数
 * @param {String} params.serviceType 服务类型
 * @param {String} params.status 需求状态
 * @param {Number} params.page 页码
 * @param {Number} params.limit 每页数量
 * @param {String} params.sort 排序方式
 * @returns {Promise} 需求列表
 */
export const getRequests = (params = {}) => {
  return api.get('/requests', params)
}

/**
 * 获取需求详情
 * @param {String} requestId 需求ID
 * @returns {Promise} 需求详情
 */
export const getRequestDetail = (requestId) => {
  return api.get(`/requests/${requestId}`)
}

/**
 * 更新需求状态
 * @param {String} requestId 需求ID
 * @param {Object} statusData 状态数据
 * @param {String} statusData.status 新状态
 * @param {String} statusData.reason 原因
 * @returns {Promise} 更新结果
 */
export const updateRequestStatus = (requestId, statusData) => {
  return api.put(`/requests/${requestId}/status`, statusData)
}

/**
 * 删除需求
 * @param {String} requestId 需求ID
 * @returns {Promise} 删除结果
 */
export const deleteRequest = (requestId) => {
  return api.delete(`/requests/${requestId}`)
}

/**
 * 获取服务类型的需求列表
 * @param {String} serviceTypeId 服务类型ID
 * @param {Object} params 查询参数
 * @param {String} params.status 需求状态
 * @param {Number} params.page 页码
 * @param {Number} params.limit 每页数量
 * @returns {Promise} 服务类型需求列表
 */
export const getServiceTypeRequests = (serviceTypeId, params = {}) => {
  return api.get(`/service-types/${serviceTypeId}/requests`, params)
}
