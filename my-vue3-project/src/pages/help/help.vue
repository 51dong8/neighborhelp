<template>
  <view class="help-container">
    <view class="header">
      <text class="title">常见问题</text>
      <text class="subtitle">为您解答使用中的疑惑</text>
    </view>

    <view class="faq-list">
      <view class="faq-item" v-for="(item, index) in faqList" :key="index">
        <view class="faq-question">
          <text class="q-icon">Q</text>
          <text class="q-text">{{ item.question }}</text>
        </view>
        <view class="faq-answer">
          <text class="a-icon">A</text>
          <text class="a-text">{{ item.answer }}</text>
        </view>
      </view>
    </view>

    <view class="bottom-tips">
      <text>没有找到您的问题？请前往“意见反馈”留言</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const faqList = ref([
  {
    question: '如何发布一个帮忙需求？',
    answer: '在首页点击底部的“+”号按钮，选择您需要的服务类型（如代拿快递、宠物代遛等），填写具体信息和悬赏积分后点击发布即可。'
  },
  {
    question: '如何赚取积分？',
    answer: '您可以通过帮助他人完成需求来赚取对方悬赏的积分，此外，每日签到、完成新手任务等也会奖励一定数量的积分。'
  },
  {
    question: '接单后如果遇到特殊情况无法完成怎么办？',
    answer: '请第一时间在订单详情页点击“取消订单”，并向发布者说明原因。如果是发布者的原因，您可以申请客服介入。'
  },
  {
    question: '积分可以在商城兑换实物吗？',
    answer: '可以的。在“我的”页面进入“积分商城”，您可以浏览并消耗积分兑换对应的礼品或社区服务抵扣券。'
  }
])
</script>

<style scoped>
.help-container {
  min-height: 100vh;
  background-color: #F5F7FA;
  padding: 30rpx;
}

.header {
  margin-bottom: 40rpx;
}

.title {
  font-size: 40rpx;
  font-weight: bold;
  color: #333;
  display: block;
}

.subtitle {
  font-size: 26rpx;
  color: #888;
  margin-top: 10rpx;
  display: block;
}

.faq-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.faq-item {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.03);
}

.faq-question {
  display: flex;
  align-items: flex-start;
  gap: 16rpx;
  margin-bottom: 20rpx;
}

.q-icon {
  background-color: #FFB74D;
  color: #fff;
  font-size: 24rpx;
  width: 36rpx;
  height: 36rpx;
  text-align: center;
  line-height: 36rpx;
  border-radius: 8rpx;
  flex-shrink: 0;
}

.q-text {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  line-height: 1.4;
}

.faq-answer {
  display: flex;
  align-items: flex-start;
  gap: 16rpx;
  background-color: #F9FAFC;
  padding: 20rpx;
  border-radius: 12rpx;
}

.a-icon {
  background-color: #4CAF50;
  color: #fff;
  font-size: 24rpx;
  width: 36rpx;
  height: 36rpx;
  text-align: center;
  line-height: 36rpx;
  border-radius: 8rpx;
  flex-shrink: 0;
}

.a-text {
  font-size: 28rpx;
  color: #666;
  line-height: 1.5;
}

.bottom-tips {
  margin-top: 60rpx;
  text-align: center;
  font-size: 24rpx;
  color: #AAA;
}
</style>