/**
 * 业务 API 统一入口：各域模块 + 默认导出 api（见 @/utils/api.js）。
 * 可选：requestState（全局加载/错误与包装器），一般页面不需要引用。
 */

// 认证模块
export * from './auth'

// 用户管理模块
export * from './user'

// 需求管理模块
export * from './request'

// 订单管理模块
export * from './order'

// 服务类型模块
export * from './service'

// 系统配置模块
export * from './system'

// 通知模块
export * from './notification'

export { default as api } from '@/utils/api'

export * from '@/utils/requestState'
