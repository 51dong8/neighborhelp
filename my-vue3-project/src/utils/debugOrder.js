/**
 * 订单调试工具
 * 用于排查订单显示问题
 */

// 检查Token是否正确保存
export const checkToken = () => {
  const token = uni.getStorageSync('token')
  const refreshToken = uni.getStorageSync('refreshToken')
  const userInfo = uni.getStorageSync('userInfo')
  
  console.log('=== Token检查 ===')
  console.log('Token:', token ? '已保存' : '未保存')
  console.log('RefreshToken:', refreshToken ? '已保存' : '未保存')
  console.log('UserInfo:', userInfo ? '已保存' : '未保存')
  
  if (token) {
    console.log('Token内容:', token.substring(0, 20) + '...')
  }
  
  return {
    hasToken: !!token,
    hasRefreshToken: !!refreshToken,
    hasUserInfo: !!userInfo,
    token: token
  }
}

// 测试获取用户订单API
export const testGetUserOrders = async () => {
  console.log('=== 测试获取用户订单API ===')
  
  const tokenInfo = checkToken()
  if (!tokenInfo.hasToken) {
    console.error('❌ 没有找到Token，请先登录')
    return null
  }
  
  try {
    const result = await uni.request({
      url: '/api/user/orders',
      method: 'GET',
      header: {
        'Authorization': `Bearer ${tokenInfo.token}`,
        'Content-Type': 'application/json'
      },
      timeout: 10000
    })
    
    console.log('API响应:', result)
    
    if (result.statusCode === 200) {
      console.log('✅ API调用成功')
      console.log('订单数据:', result.data)
      return result.data
    } else {
      console.error('❌ API调用失败:', result.statusCode, result.data)
      return null
    }
  } catch (error) {
    console.error('❌ API调用异常:', error)
    return null
  }
}

// 测试接单API
export const testAcceptOrder = async (requestId) => {
  console.log('=== 测试接单API ===')
  
  const tokenInfo = checkToken()
  if (!tokenInfo.hasToken) {
    console.error('❌ 没有找到Token，请先登录')
    return null
  }
  
  try {
    const result = await uni.request({
      url: '/api/orders',
      method: 'POST',
      header: {
        'Authorization': `Bearer ${tokenInfo.token}`,
        'Content-Type': 'application/json'
      },
      data: {
        requestId: requestId,
        message: '我可以帮您完成这个需求，请放心交给我！'
      },
      timeout: 10000
    })
    
    console.log('接单API响应:', result)
    
    if (result.statusCode === 200) {
      console.log('✅ 接单API调用成功')
      console.log('接单结果:', result.data)
      return result.data
    } else {
      console.error('❌ 接单API调用失败:', result.statusCode, result.data)
      return null
    }
  } catch (error) {
    console.error('❌ 接单API调用异常:', error)
    return null
  }
}

// 完整的订单调试流程
export const debugOrderFlow = async () => {
  console.log('=== 开始订单调试流程 ===')
  
  // 1. 检查Token
  const tokenInfo = checkToken()
  if (!tokenInfo.hasToken) {
    console.error('❌ 调试终止：没有找到Token')
    return
  }
  
  // 2. 测试获取订单
  console.log('\n--- 步骤1：获取当前订单列表 ---')
  const orders = await testGetUserOrders()
  
  if (orders && orders.code === 200) {
    console.log(`✅ 当前有 ${orders.data.orders.length} 个订单`)
    orders.data.orders.forEach((order, index) => {
      console.log(`订单${index + 1}:`, {
        id: order.id,
        title: order.title,
        status: order.status,
        createdAt: order.createdAt
      })
    })
  } else {
    console.log('❌ 获取订单失败或没有订单')
  }
  
  // 3. 提供调试建议
  console.log('\n--- 调试建议 ---')
  console.log('1. 如果Token正常但获取不到订单，可能是后端数据问题')
  console.log('2. 如果接单后订单没有出现，可能是接单API没有正确调用')
  console.log('3. 检查浏览器Network标签，查看API请求是否成功')
  console.log('4. 检查Console标签，查看是否有JavaScript错误')
}

// 在浏览器控制台中使用的全局函数
if (typeof window !== 'undefined') {
  window.debugOrder = {
    checkToken,
    testGetUserOrders,
    testAcceptOrder,
    debugOrderFlow
  }
}

