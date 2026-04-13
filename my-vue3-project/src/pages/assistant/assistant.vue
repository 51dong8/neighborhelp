<template>
  <view class="chat-container">
    <scroll-view 
      class="message-list" 
      scroll-y="true" 
      :scroll-top="scrollTop"
      :scroll-with-animation="true"
    >
      <view class="message-item assistant">
        <view class="avatar">🤖</view>
        <view class="bubble">你好！我是邻里帮帮智能助手，有什么生活上的问题或者需要跑腿的，都可以问我哦！</view>
      </view>

      <view 
        v-for="(msg, index) in messages" 
        :key="index" 
        class="message-item" 
        :class="msg.role"
      >
        <view class="avatar" v-if="msg.role === 'assistant'">🤖</view>
        <view class="bubble">{{ msg.content }}</view>
        <view class="avatar user-avatar" v-if="msg.role === 'user'">👤</view>
      </view>

      <view class="message-item assistant" v-if="isTyping">
        <view class="avatar">🤖</view>
        <view class="bubble typing-indicator">
          <text class="dot"></text><text class="dot"></text><text class="dot"></text>
        </view>
      </view>
      
      <view class="bottom-spacer"></view>
    </scroll-view>

    <view class="input-area">
      <input 
        class="chat-input" 
        v-model="inputText" 
        placeholder="输入你的问题..." 
        confirm-type="send" 
        @confirm="sendMessage"
        :cursor-spacing="20"
      />
      <button 
        class="send-btn" 
        :class="{ active: inputText.trim().length > 0 }" 
        @click="sendMessage"
      >发送</button>
    </view>
  </view>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { chatWithAssistant } from '@/api/assistant'

const messages = ref([])
const inputText = ref('')
const isTyping = ref(false)
const scrollTop = ref(0)

// 如果从个人中心点击了建议，会自动带入参数并发送
onLoad((options) => {
  if (options.text) {
    inputText.value = decodeURIComponent(options.text)
    sendMessage()
  }
})

const scrollToBottom = async () => {
  await nextTick()
  // 简单粗暴的方式，将 scrollTop 设置为一个极大值来滚到底部
  scrollTop.value = 99999 + Math.random() 
}

const sendMessage = async () => {
  const text = inputText.value.trim()
  if (!text || isTyping.value) return

  // 1. 插入用户消息
  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  scrollToBottom()

  // 2. 显示加载状态
  isTyping.value = true
  scrollToBottom()

  try {
    // 3. 调用后端接口
    const res = await chatWithAssistant(text)
    if (res && res.code === 200) {
      messages.value.push({ role: 'assistant', content: res.data })
    } else {
      messages.value.push({ role: 'assistant', content: '抱歉，我现在有点晕，没听懂你的话。' })
    }
  } catch (error) {
    messages.value.push({ role: 'assistant', content: '网络好像出了点问题，请稍后再试。' })
  } finally {
    isTyping.value = false
    scrollToBottom()
  }
}
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #F5F7FA;
}

.message-list {
  flex: 1;
  padding: 30rpx 20rpx;
  box-sizing: border-box;
}

.message-item {
  display: flex;
  margin-bottom: 30rpx;
  align-items: flex-start;
}

/* 助手在左边 */
.message-item.assistant {
  flex-direction: row;
}

.message-item.assistant .bubble {
  background-color: #FFFFFF;
  color: #333;
  margin-left: 20rpx;
  border-radius: 0 20rpx 20rpx 20rpx;
  box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.05);
}

/* 用户在右边 */
.message-item.user {
  flex-direction: row;
  justify-content: flex-end;
}

.message-item.user .bubble {
  background: linear-gradient(135deg, #4CAF50, #66BB6A);
  color: #FFFFFF;
  margin-right: 20rpx;
  border-radius: 20rpx 0 20rpx 20rpx;
  box-shadow: 0 2rpx 10rpx rgba(76, 175, 80, 0.3);
}

.avatar {
  width: 70rpx;
  height: 70rpx;
  border-radius: 50%;
  background-color: #E3F2FD;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
  flex-shrink: 0;
}

.user-avatar {
  background-color: #E8F5E8;
}

.bubble {
  max-width: 65%;
  padding: 20rpx 30rpx;
  font-size: 28rpx;
  line-height: 1.5;
  word-break: break-all;
}

.bottom-spacer {
  height: 120rpx;
}

/* 输入区 */
.input-area {
  display: flex;
  align-items: center;
  padding: 20rpx 30rpx;
  background-color: #FFFFFF;
  border-top: 1rpx solid #EEEEEE;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
}

.chat-input {
  flex: 1;
  height: 72rpx;
  background-color: #F5F5F5;
  border-radius: 36rpx;
  padding: 0 30rpx;
  font-size: 28rpx;
}

.send-btn {
  margin-left: 20rpx;
  width: 120rpx;
  height: 72rpx;
  line-height: 72rpx;
  background-color: #E0E0E0;
  color: #999;
  font-size: 28rpx;
  border-radius: 36rpx;
  padding: 0;
}

.send-btn.active {
  background: linear-gradient(135deg, #FF8F00, #FFB74D);
  color: #FFFFFF;
}

.send-btn::after {
  border: none;
}

/* 打字动画 */
.typing-indicator {
  display: flex;
  align-items: center;
  height: 40rpx;
}
.dot {
  width: 8rpx;
  height: 8rpx;
  background-color: #999;
  border-radius: 50%;
  margin: 0 4rpx;
  animation: typing 1.4s infinite ease-in-out both;
}
.dot:nth-child(1) { animation-delay: -0.32s; }
.dot:nth-child(2) { animation-delay: -0.16s; }
@keyframes typing {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}
</style>