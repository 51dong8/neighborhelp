/**
 * API 配置管理
 */

// 获取 API 配置
export const getApiConfig = () => {
  const isDevelopment = process.env.NODE_ENV === 'development'

  return {
    apiBaseUrl: isDevelopment
      ? 'http://localhost:8080/api'
      : 'https://pyuirbqiqttu.sealosbja.site/api'
  }
}
