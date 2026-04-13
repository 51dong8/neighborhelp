import api from '@/utils/api'

export const getShopItems = () => {
  return api.get('/shop/items')
}

export const redeemShopItem = (itemId) => {
  return api.post(`/shop/redeem/${itemId}`)
}
