<template>
  <view class="points-page">
    <view class="hero-card">
      <text class="hero-label">当前总积分</text>
      <text class="hero-points">{{ pointsInfo.totalPoints }}</text>
      <view class="hero-row">
        <text class="hero-tag">可用 {{ pointsInfo.availablePoints }}</text>
        <text class="hero-tag">Lv.{{ pointsInfo.level }}</text>
        <text class="hero-tag">下级 {{ pointsInfo.nextLevelPoints }}</text>
      </view>
    </view>

    <view class="tab-bar">
      <view
        v-for="tab in tabs"
        :key="tab.key"
        class="tab-item"
        :class="{ active: activeTab === tab.key }"
        @click="switchTab(tab.key)"
      >
        {{ tab.label }}
      </view>
    </view>

    <view v-if="activeTab === 'tasks'" class="panel-card">
      <text class="panel-title">积分任务</text>
      <view v-for="task in pointTasks" :key="task.title" class="list-card">
        <view class="list-main">
          <text class="list-icon">{{ task.icon }}</text>
          <view>
            <text class="list-title">{{ task.title }}</text>
            <text class="list-desc">{{ task.desc }}</text>
          </view>
        </view>
        <view class="list-side">
          <text class="reward-text">+{{ task.reward }}</text>
          <button class="action-btn ghost">去完成</button>
        </view>
      </view>
    </view>

    <view v-if="activeTab === 'shop'" class="panel-card">
      <text class="panel-title">积分商城</text>
      <view v-if="loadingShop" class="empty-box">商品加载中...</view>
      <view v-else-if="shopItems.length === 0" class="empty-box">暂无可兑换商品</view>
      <view v-else>
        <view v-for="item in shopItems" :key="item.id" class="list-card">
          <view class="list-main">
            <text class="list-icon">{{ getShopEmoji(item) }}</text>
            <view>
              <text class="list-title">{{ item.name }}</text>
              <text class="list-desc">{{ item.description || '社区精选积分好物' }}</text>
              <text class="stock-text">库存：{{ item.stock }}</text>
            </view>
          </view>
          <view class="list-side">
            <text class="reward-text">{{ item.pointsCost }} 积分</text>
            <button class="action-btn" :disabled="redeeming || item.stock <= 0" @click="handleRedeem(item)">
              {{ redeeming ? '处理中...' : item.stock > 0 ? '兑换' : '已售罄' }}
            </button>
          </view>
        </view>
      </view>
    </view>

    <view v-if="activeTab === 'records'" class="panel-card">
      <text class="panel-title">积分明细</text>
      <view v-if="loadingRecords" class="empty-box">加载中...</view>
      <view v-else-if="records.length === 0" class="empty-box">暂无积分明细</view>
      <view v-else>
        <view
          v-for="record in records"
          :key="record.id || `${record.type}-${record.createdAt}-${record.amount}`"
          class="record-card"
        >
          <view class="record-main">
            <text class="record-type">{{ formatType(record.type) }}</text>
            <text class="record-desc">{{ record.description || '积分变动' }}</text>
            <text class="record-time">{{ formatTime(record.createdAt) }}</text>
          </view>
          <text class="record-amount" :class="record.amount >= 0 ? 'plus' : 'minus'">
            {{ record.amount >= 0 ? '+' : '' }}{{ record.amount }}
          </text>
        </view>
        <button v-if="hasMore" class="load-more" :disabled="loadingMore" @click="loadMoreRecords">
          {{ loadingMore ? '加载中...' : '加载更多' }}
        </button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { onLoad } from '@dcloudio/uni-app'
import { onMounted, ref } from 'vue'
import { getPointRecords, getUserPoints } from '@/api/user'
import { getShopItems, redeemShopItem } from '@/api/shop'

const tabs = [
  { key: 'tasks', label: '积分任务' },
  { key: 'shop', label: '积分商城' },
  { key: 'records', label: '积分明细' }
]

const pointTasks = [
  { icon: '☀️', title: '每日签到', desc: '每日签到可获得 5 积分', reward: 5 },
  { icon: '📝', title: '发布一次互助需求', desc: '发布一条真实需求可获得 20 积分', reward: 20 },
  { icon: '🤝', title: '完成一次帮助', desc: '完成一次帮助可获得 50 积分', reward: 50 }
]

const activeTab = ref('tasks')
const loadingPoints = ref(false)
const loadingShop = ref(false)
const loadingRecords = ref(false)
const loadingMore = ref(false)
const redeeming = ref(false)
const page = ref(1)
const limit = ref(10)
const hasMore = ref(true)
const records = ref([])
const shopItems = ref([])
const pointsInfo = ref({
  totalPoints: 0,
  availablePoints: 0,
  frozenPoints: 0,
  level: 1,
  nextLevelPoints: 200
})

const getErrorMessage = (error, fallback) => {
  if (!error) return fallback
  if (typeof error === 'string' && error.trim()) return error.trim()
  if (typeof error.message === 'string' && error.message.trim()) return error.message.trim()
  if (error.data && typeof error.data === 'object') {
    const firstKey = Object.keys(error.data)[0]
    if (firstKey && error.data[firstKey]) return error.data[firstKey]
  }
  if (error.originalError?.errMsg) return error.originalError.errMsg
  return fallback
}

const getShopEmoji = (item) => {
  const name = item?.name || ''
  if (name.includes('代金券')) return '🧾'
  if (name.includes('帆布袋')) return '👜'
  if (name.includes('勋章')) return '🏅'
  return '🎁'
}

const refreshPoints = async () => {
  console.log('开始获取积分数据...')
  loadingPoints.value = true
  try {
    const res = await getUserPoints()
    console.log('获取到的积分数据:', res)
    const data = res?.data || {}
    pointsInfo.value = {
      totalPoints: data.totalPoints ?? 0,
      availablePoints: data.availablePoints ?? 0,
      frozenPoints: data.frozenPoints ?? 0,
      level: data.level ?? 1,
      nextLevelPoints: data.nextLevelPoints ?? 200
    }
  } catch (error) {
    console.error('API调用报错:', error)
    uni.showToast({ title: getErrorMessage(error, '获取积分失败'), icon: 'none' })
  } finally {
    loadingPoints.value = false
  }
}

const loadShopItems = async () => {
  loadingShop.value = true
  try {
    const res = await getShopItems()
    console.log('获取到的商城数据:', res.data)
    shopItems.value = Array.isArray(res?.data) ? res.data : []
  } catch (error) {
    console.error('API调用报错:', error)
    uni.showToast({ title: getErrorMessage(error, '获取商城商品失败'), icon: 'none' })
  } finally {
    loadingShop.value = false
  }
}

const loadRecords = async (reset = false) => {
  if (reset) {
    page.value = 1
    records.value = []
    hasMore.value = true
  }

  if (!hasMore.value && !reset) return

  if (reset) {
    loadingRecords.value = true
  } else {
    loadingMore.value = true
  }

  try {
    const res = await getPointRecords(page.value, limit.value)
    console.log('获取到的商城数据:', res.data)
    const data = res?.data || {}
    const newRecords = Array.isArray(data.records) ? data.records : []
    records.value = reset ? newRecords : [...records.value, ...newRecords]
    const total = Number(data.total || 0)
    hasMore.value = records.value.length < total && newRecords.length > 0
    if (newRecords.length > 0) {
      page.value += 1
    } else {
      hasMore.value = false
    }
  } catch (error) {
    console.error('API调用报错:', error)
    uni.showToast({ title: getErrorMessage(error, '获取明细失败'), icon: 'none' })
  } finally {
    loadingRecords.value = false
    loadingMore.value = false
  }
}

const loadMoreRecords = async () => {
  await loadRecords(false)
}

const switchTab = async (tab) => {
  console.log('切换到标签:', tab)
  activeTab.value = tab
  if (tab === 'shop' && shopItems.value.length === 0) {
    await loadShopItems()
  }
  if (tab === 'records' && records.value.length === 0) {
    await loadRecords(true)
  }
}

onLoad(async (options) => {
  const targetTab = options?.tab
  if (!targetTab || !tabs.some((tab) => tab.key === targetTab)) {
    return
  }

  activeTab.value = targetTab

  if (targetTab === 'shop') {
    await loadShopItems()
  }

  if (targetTab === 'records') {
    await loadRecords(true)
  }
})

const handleRedeem = (item) => {
  uni.showModal({
    title: '确认兑换',
    content: `确定使用 ${item.pointsCost} 积分兑换“${item.name}”吗？`,
    confirmText: '兑换',
    success: async (res) => {
      if (!res.confirm) return
      console.log('发起兑换请求:', item)
      redeeming.value = true
      try {
        const result = await redeemShopItem(item.id)
        console.log('兑换成功响应:', result)
        uni.showToast({ title: result.message || '兑换成功', icon: 'success' })
        await refreshPoints()
        await loadShopItems()
        if (activeTab.value === 'records') {
          await loadRecords(true)
        }
      } catch (error) {
        console.error('API调用报错:', error)
        uni.showModal({
          title: '兑换失败',
          content: getErrorMessage(error, '兑换失败，请稍后重试'),
          showCancel: false
        })
      } finally {
        redeeming.value = false
      }
    }
  })
}

const formatType = (type) => {
  const map = {
    register: '注册奖励',
    redeem: '积分兑换',
    publish_request: '发布需求',
    complete_service: '完成帮助'
  }
  return map[type] || type || '积分变动'
}

const formatTime = (time) => {
  if (!time) return '时间未知'
  return String(time).replace('T', ' ')
}

onMounted(async () => {
  await refreshPoints()
  if (activeTab.value === 'shop' && shopItems.value.length === 0) {
    await loadShopItems()
  }
})
</script>

<style scoped>
.points-page {
  min-height: 100vh;
  padding: 24rpx;
  background: linear-gradient(180deg, #fff8ef 0%, #fff3e0 45%, #fffdf8 100%);
  box-sizing: border-box;
}

.hero-card {
  background: linear-gradient(135deg, #ffb74d 0%, #ff8a65 100%);
  border-radius: 34rpx;
  padding: 30rpx;
  color: #fff;
  box-shadow: 0 20rpx 40rpx rgba(255, 138, 101, 0.22);
  margin-bottom: 24rpx;
}

.hero-label {
  display: block;
  font-size: 24rpx;
  opacity: 0.9;
}

.hero-points {
  display: block;
  font-size: 72rpx;
  font-weight: 900;
  margin: 12rpx 0 20rpx;
}

.hero-row {
  display: flex;
  gap: 12rpx;
  flex-wrap: wrap;
}

.hero-tag {
  padding: 10rpx 18rpx;
  border-radius: 999rpx;
  background: rgba(255, 255, 255, 0.18);
  font-size: 22rpx;
}

.tab-bar {
  display: flex;
  gap: 12rpx;
  background: rgba(255, 255, 255, 0.88);
  padding: 12rpx;
  border-radius: 28rpx;
  box-shadow: 0 16rpx 32rpx rgba(141, 110, 99, 0.08);
  margin-bottom: 24rpx;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 18rpx 8rpx;
  border-radius: 22rpx;
  font-size: 26rpx;
  font-weight: 700;
  color: #8d6e63;
}

.tab-item.active {
  background: linear-gradient(135deg, #ffd180 0%, #ffb74d 100%);
  color: #6d4c41;
}

.panel-card {
  background: rgba(255, 255, 255, 0.92);
  border-radius: 30rpx;
  padding: 26rpx;
  box-shadow: 0 18rpx 36rpx rgba(141, 110, 99, 0.08);
}

.panel-title {
  display: block;
  font-size: 32rpx;
  font-weight: 800;
  color: #6d4c41;
  margin-bottom: 20rpx;
}

.list-card,
.record-card {
  background: linear-gradient(180deg, #fffdfa 0%, #fff7f0 100%);
  border-radius: 26rpx;
  padding: 22rpx;
  margin-bottom: 18rpx;
  box-shadow: inset 0 0 0 2rpx rgba(255, 204, 128, 0.18);
}

.list-card {
  display: flex;
  justify-content: space-between;
  gap: 16rpx;
  align-items: center;
}

.list-main {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex: 1;
}

.list-icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: 22rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #ffe0b2 0%, #ffcc80 100%);
  font-size: 32rpx;
}

.list-title,
.record-type {
  display: block;
  font-size: 28rpx;
  font-weight: 700;
  color: #5d4037;
}

.list-desc,
.record-desc,
.record-time,
.stock-text {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: #8d6e63;
  line-height: 1.5;
}

.list-side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12rpx;
}

.reward-text {
  font-size: 24rpx;
  font-weight: 800;
  color: #ef6c00;
}

.action-btn,
.load-more {
  border: none;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #ffb74d 0%, #ff8a65 100%);
  color: #fff;
  font-size: 24rpx;
  font-weight: 700;
}

.action-btn.ghost {
  background: #fff3e0;
  color: #bf6d00;
}

.record-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16rpx;
}

.record-main {
  flex: 1;
}

.record-amount {
  font-size: 30rpx;
  font-weight: 900;
}

.record-amount.plus {
  color: #43a047;
}

.record-amount.minus {
  color: #e53935;
}

.empty-box {
  text-align: center;
  padding: 40rpx 0;
  color: #a1887f;
  font-size: 24rpx;
}

.load-more {
  width: 100%;
  margin-top: 8rpx;
}
</style>
