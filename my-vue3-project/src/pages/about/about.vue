<template>
  <view class="about-container">
    <view class="logo-section">
      <image class="logo" src="/static/logo.png" mode="aspectFit"></image>
      <text class="app-name">邻里帮帮</text>
      <text class="version">Version 1.0.0</text>
    </view>

    <view class="desc-section">
      <text class="desc-text">
        “邻里帮帮”是一款致力于打造温情、互助社区环境的综合服务平台。我们希望通过技术的力量，拉近邻里之间的距离，让互帮互助成为社区生活的新常态。无论是代拿快递、宠物代遛，还是二手闲置交换，您都能在这里找到热心的邻居。
      </text>
    </view>

    <view class="links-section">
      <view class="link-item" @click="navigateTo('/pages/user-agreement/user-agreement?type=service')">
        <text>服务协议</text>
        <text class="arrow">→</text>
      </view>
      <view class="link-item" @click="navigateTo('/pages/user-agreement/user-agreement?type=privacy')">
        <text>隐私政策</text>
        <text class="arrow">→</text>
      </view>
      <view class="link-item" @click="contactService">
        <text>联系客服</text>
        <text class="arrow">→</text>
      </view>
    </view>

    <view class="footer">
      <text>Copyright © 2026 邻里帮帮 团队</text>
      <text>All Rights Reserved</text>
    </view>
  </view>
</template>

<script setup>
// 【修改点4】：这里添加了页面跳转的逻辑
const navigateTo = (url) => {
  uni.navigateTo({ url })
}

// 【修改点5】：这里添加了联系客服的弹窗提示
const contactService = () => {
  uni.showModal({
    title: '联系客服',
    content: '客服电话：400-123-4567\n工作时间：9:00 - 18:00',
    showCancel: false,
    confirmText: '知道啦',
    confirmColor: '#4CAF50'
  })
}
</script>

<style scoped>
.about-container {
  min-height: 100vh;
  background-color: #F5F7FA;
  display: flex;
  flex-direction: column;
}

.logo-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 0 60rpx;
}

.logo {
  width: 160rpx;
  height: 160rpx;
  border-radius: 32rpx;
  margin-bottom: 24rpx;
  background-color: #fff; /* 如果图片透明，给个底色 */
  box-shadow: 0 8rpx 24rpx rgba(0,0,0,0.05);
}

.app-name {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 8rpx;
}

.version {
  font-size: 24rpx;
  color: #888;
}

.desc-section {
  padding: 0 40rpx;
  margin-bottom: 60rpx;
}

.desc-text {
  font-size: 28rpx;
  color: #666;
  line-height: 1.8;
  text-align: justify;
}

.links-section {
  background-color: #fff;
  border-top: 1rpx solid #F0F0F0;
  border-bottom: 1rpx solid #F0F0F0;
}

.link-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx 40rpx;
  border-bottom: 1rpx solid #F5F7FA;
  font-size: 30rpx;
  color: #333;
}

.link-item:last-child {
  border-bottom: none;
}

.link-item:active {
  background-color: #F9FAFC;
}

.arrow {
  color: #CCC;
  font-size: 32rpx;
}

.footer {
  margin-top: auto;
  padding: 60rpx 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}

.footer text {
  font-size: 22rpx;
  color: #AAA;
}
</style>