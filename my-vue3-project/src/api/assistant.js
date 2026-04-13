import api from '@/utils/api'

/**
 * 智能助手对话接口
 * @param {String} message 用户发送的消息
 * @returns {Promise}
 */
export const chatWithAssistant = (message) => {
  return api.post('/assistant/chat', { message })
}