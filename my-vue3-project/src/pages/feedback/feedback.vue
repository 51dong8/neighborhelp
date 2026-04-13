<template>
  <view class="feedback-container">
    <view class="form-card">
      <view class="label">问题类型</view>
      <view class="type-tags">
        <view 
          class="tag" 
          :class="{ active: currentType === tag }"
          v-for="tag in types" 
          :key="tag"
          @click="currentType = tag"
        >
          {{ tag }}
        </view>
      </view>

      <view class="label mt-4">问题描述</view>
      <view class="textarea-box">
        <textarea 
          class="textarea" 
          v-model="content" 
          placeholder="请详细描述您遇到的问题或您的建议，我们将不断改进..."
          maxlength="200"
        />
        <text class="word-count">{{ content.length }}/200</text>
      </view>
      
      <view class="label mt-4">联系方式 (选填)</view>
      <input 
        class="input" 
        v-model="contact" 
        placeholder="留下您的手机号或微信号，方便我们联系您" 
      />
    </view>

    <view class="submit-btn" :class="{ disabled: !content.trim() }" @click="submitFeedback">
      提交反馈
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
// 引入刚刚写好的 api 方法
import { submitFeedback as submitFeedbackApi } from '@/api/system' 

const types = ['功能异常', '优化建议', '服务投诉', '其他']
const currentType = ref('优化建议')
const content = ref('')
const contact = ref('')

const submitFeedback = async () => {
  if (!content.value.trim()) {
    uni.showToast({ title: '请填写反馈内容', icon: 'none' })
    return
  }

  uni.showLoading({ title: '提交中...', mask: true })
  
  try {
    // 组装要发送的真实数据
    const feedbackData = {
      type: currentType.value,
      content: content.value.trim(),
      contact: contact.value.trim()
    }
    
    // 调用接口
    const res = await submitFeedbackApi(feedbackData)
    
    uni.hideLoading()
    
    // 根据项目实际的 Result 返回结构进行判断 (这里假设成功返回 code 为 200)
    if (res && res.code === 200) {
      uni.showToast({ title: '感谢您的反馈！', icon: 'success' })
      
      // 延迟返回上一页
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    } else {
      uni.showToast({ title: res.message || '提交失败', icon: 'none' })
    }
  } catch (error) {
    uni.hideLoading()
    uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' })
    console.error('Submit feedback error:', error)
  }
}
</script>

<style scoped>
.feedback-container {
  min-height: 100vh;
  background-color: #F5F7FA;
  padding: 30rpx;
}

.form-card {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.03);
}

.label {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 20rpx;
}

.mt-4 {
  margin-top: 40rpx;
}

.type-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}

.tag {
  padding: 12rpx 30rpx;
  background-color: #F5F7FA;
  color: #666;
  font-size: 26rpx;
  border-radius: 30rpx;
  border: 1rpx solid transparent;
  transition: all 0.3s;
}

.tag.active {
  background-color: #E8F5E9;
  color: #4CAF50;
  border-color: #4CAF50;
}

.textarea-box {
  position: relative;
  background-color: #F9FAFC;
  border-radius: 12rpx;
  padding: 20rpx;
}

.textarea {
  width: 100%;
  height: 240rpx;
  font-size: 28rpx;
  line-height: 1.5;
  color: #333;
}

.word-count {
  position: absolute;
  bottom: 20rpx;
  right: 20rpx;
  font-size: 24rpx;
  color: #AAA;
}

.input {
  background-color: #F9FAFC;
  padding: 24rpx 20rpx;
  border-radius: 12rpx;
  font-size: 28rpx;
}

.submit-btn {
  margin-top: 60rpx;
  background: linear-gradient(135deg, #4CAF50, #66BB6A);
  color: #fff;
  text-align: center;
  padding: 28rpx 0;
  border-radius: 48rpx;
  font-size: 32rpx;
  font-weight: 600;
  box-shadow: 0 6rpx 20rpx rgba(76, 175, 80, 0.3);
  transition: all 0.3s;
}

.submit-btn:active {
  transform: scale(0.98);
}

.submit-btn.disabled {
  background: #A5D6A7;
  box-shadow: none;
  opacity: 0.7;
}
</style>