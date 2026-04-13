/**
 * 服务类型模块API
 * 基于后端接口需求文档实现
 */

import api from '@/utils/api'

/**
 * 获取服务类型列表
 * @returns {Promise} 服务类型列表
 */
export const getServiceTypes = () => {
  return api.get('/service-types')
}

/**
 * 获取服务类型详情
 * @param {String} serviceTypeId 服务类型ID
 * @returns {Promise} 服务类型详情
 */
export const getServiceTypeDetail = (serviceTypeId) => {
  return api.get(`/service-types/${serviceTypeId}`)
}

/**
 * 获取服务类型统计
 * @returns {Promise} 服务类型统计
 */
export const getServiceTypeStats = () => {
  return api.get('/service-types/stats')
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
