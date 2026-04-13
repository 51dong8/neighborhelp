<template>
  <view class="auth-container">
    <view class="header">
      <text class="title">小区认证</text>
      <text class="subtitle">通过AI智能识别您的物业单据，秒速完成实名认证，开启接单赚钱之旅！</text>
    </view>

    <view class="form-card">
      <view class="form-item">
        <text class="label">所在小区 <text class="required">*</text></text>
        <view class="location-box">
          <view class="location-value">
            <text class="location-icon">📍</text>
            <input 
              class="location-input" 
              v-model="selectedCommunityName" 
              :placeholder="isLocating ? '正在精准定位中...' : '定位失败，请直接输入小区名称'"
            />
          </view>
          <button class="relocate-btn" @click="getLocation" :disabled="isLocating">
            {{ isLocating ? '定位中...' : '重新定位' }}
          </button>
        </view>
        <text class="tips" style="margin-top: 12rpx;">基于高德地图提供位置服务</text>
      </view>

      <view class="form-item upload-section">
        <text class="label">物业凭证 <text class="required">*</text></text>
        <text class="tips">请上传带有“{{ selectedCommunityName || '小区名称' }}”的物业费、水电费账单或门禁卡照片</text>
        
        <view class="upload-box" @click="uploadMaterial">
          <image 
            v-if="materialUrl" 
            :src="materialUrl" 
            mode="aspectFit" 
            class="preview-image"
          ></image>
          <view v-else class="upload-placeholder">
            <text class="icon">📸</text>
            <text class="text">点击拍摄 / 上传单据</text>
          </view>
        </view>
      </view>
    </view>

    <view class="submit-section">
      <button 
        class="submit-btn" 
        :class="{ 'disabled': !isReadyToSubmit }"
        :loading="isSubmitting" 
        @click="submitAuth"
      >
        {{ isSubmitting ? 'AI智能审核中...' : '提交认证' }}
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
// 注意：移除了 getCommunityList 接口
import { submitCommunityAuth, getUserProfile } from '@/api/user'
import { apiConfig } from '@/utils/api'

const selectedCommunityName = ref('')
const materialUrl = ref('')
const isSubmitting = ref(false)
const isLocating = ref(false)

// 【重要配置】请填入你的高德地图 Web服务 API Key
const AMAP_KEY = '你的高德地图Key请填在这里' 

// 按钮是否可点击
const isReadyToSubmit = computed(() => {
  return selectedCommunityName.value && materialUrl.value
})

onMounted(() => {
  // 页面一加载就自动开始定位
  getLocation()
})

// 接入高德地图定位逻辑
const getLocation = () => {
  isLocating.value = true
  selectedCommunityName.value = ''

  // 1. 获取经纬度
  uni.getLocation({
    type: 'gcj02',
    isHighAccuracy: true,
    success: (res) => {
      const { longitude, latitude } = res
      
      // 2. 调用高德逆地理编码 API，将经纬度转为小区名字
      uni.request({
        url: `https://restapi.amap.com/v3/geocode/regeo?key=${AMAP_KEY}&location=${longitude},${latitude}&radius=1000&extensions=all`,
        method: 'GET',
        success: (amapRes) => {
          if (amapRes.data.status === '1') {
            const regeocode = amapRes.data.regeocode
            const pois = regeocode.pois || []
            
            // 尝试从周边兴趣点(POI)中筛选出“小区/住宅”类型
            let communityName = ''
            const estate = pois.find(p => p.type.includes('住宅') || p.type.includes('小区'))
            
            if (estate) {
              communityName = estate.name
            } else if (pois.length > 0) {
              communityName = pois[0].name // 实在没有住宅，取最近的标志性建筑
            } else {
              // 备用方案：用街道名
              communityName = regeocode.addressComponent.township + regeocode.addressComponent.streetNumber.street
            }
            
            selectedCommunityName.value = communityName
          } else {
            uni.showToast({ title: '高德解析失败: ' + amapRes.data.info, icon: 'none' })
          }
        },
        fail: () => {
          uni.showToast({ title: '请求高德接口超时', icon: 'none' })
        },
        complete: () => {
          isLocating.value = false
        }
      })
    },
    fail: (err) => {
      console.error('定位失败', err)
      isLocating.value = false
      uni.showToast({ title: '请在浏览器/手机设置中允许定位权限', icon: 'none' })
    }
  })
}

// 图片上传逻辑 (保持不变)
const uploadMaterial = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    success: (res) => {
      const tempFilePath = res.tempFilePaths[0]
      uni.showLoading({ title: '正在上传...', mask: true })

      const token = uni.getStorageSync('token')
      const baseUrl = apiConfig?.baseURL || 'http://172.21.176.1:8080/api'
      const uploadUrl = baseUrl.replace('/api', '') + '/api/file/upload'

      uni.uploadFile({
        url: uploadUrl,
        filePath: tempFilePath,
        name: 'file',
        header: {
          'Authorization': token ? `Bearer ${token}` : ''
        },
        success: (uploadRes) => {
          uni.hideLoading()
          if (uploadRes.statusCode !== 200) {
            return uni.showToast({ title: '请求失败', icon: 'none' })
          }
          try {
            const data = JSON.parse(uploadRes.data)
            if (data.code === 200) {
              materialUrl.value = data.data
              uni.showToast({ title: '上传成功', icon: 'success' })
            } else {
              uni.showToast({ title: data.message || '上传失败', icon: 'none' })
            }
          } catch (e) {
            uni.showToast({ title: '解析异常', icon: 'none' })
          }
        },
        fail: () => {
          uni.hideLoading()
          uni.showToast({ title: '网络请求失败', icon: 'none' })
        }
      })
    }
  })
}

// 提交认证请求
// 提交认证请求 (community-auth.vue)
const submitAuth = async () => {
  if (!isReadyToSubmit.value || isSubmitting.value) return
  isSubmitting.value = true
  uni.showLoading({ title: 'AI OCR识别中...', mask: true })

  try {
    const res = await submitCommunityAuth({
      communityName: selectedCommunityName.value, 
      materialUrl: materialUrl.value
    })

    if (res.code === 200) {
      // 获取后端返回的真实状态
      const actualStatus = res.data; 
      uni.setStorageSync('communityName', selectedCommunityName.value);
      
      if (actualStatus === 2) {
        uni.showToast({ title: 'AI秒批通过！', icon: 'success', duration: 2000 })
        uni.setStorageSync('authStatus', 2)
      } else {
        // 如果后端 OCR 失败，会返回 1
        uni.showToast({ title: '已转人工审核，请等待', icon: 'none', duration: 2500 })
        uni.setStorageSync('authStatus', 1)
      }
      
      await getUserProfile() 
      setTimeout(() => { uni.navigateBack() }, 2000)
    }
  } catch (error) {
    console.error('认证失败', error)
  } finally {
    isSubmitting.value = false
    uni.hideLoading()
  }
}
</script>

<style scoped>
/* 基础样式保持不变 */
.auth-container {
  min-height: 100vh;
  background: #F5F7FA;
  padding: 30rpx;
}
.header { margin-bottom: 40rpx; }
.title { font-size: 44rpx; font-weight: 700; color: #333; display: block; margin-bottom: 12rpx; }
.subtitle { font-size: 26rpx; color: #666; line-height: 1.5; }
.form-card { background: #FFFFFF; border-radius: 20rpx; padding: 30rpx; box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.05); margin-bottom: 60rpx; }
.form-item { margin-bottom: 40rpx; }
.form-item:last-child { margin-bottom: 0; }
.label { font-size: 30rpx; font-weight: 600; color: #333; margin-bottom: 16rpx; display: block; }
.required { color: #FF4D4F; }

/* 新增：定位栏专属样式 */
.location-box {
  display: flex;
  align-items: center;
  gap: 16rpx;
}
.location-value {
  flex: 1;
  background: #F8F9FA;
  padding: 24rpx;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #333;
  border: 1rpx solid #E4E7ED;
  display: flex;
  align-items: center;
  overflow: hidden;
}
.location-icon {
  margin-right: 12rpx;
  font-size: 32rpx;
}
.location-text {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.location-value.placeholder { color: #999; }

.relocate-btn {
  width: 180rpx;
  font-size: 26rpx;
  background: #E3F2FD;
  color: #1976D2;
  border-radius: 12rpx;
  padding: 0;
  height: 84rpx;
  line-height: 84rpx;
  font-weight: 600;
}
.relocate-btn::after { border: none; }
.relocate-btn[disabled] { background: #F5F5F5; color: #BDBDBD; }

.tips { font-size: 22rpx; color: #909399; display: block; margin-bottom: 20rpx; }
.upload-box { width: 100%; height: 360rpx; background: #F8F9FA; border: 2rpx dashed #DCDFE6; border-radius: 16rpx; display: flex; align-items: center; justify-content: center; overflow: hidden; }
.preview-image { width: 100%; height: 100%; }
.upload-placeholder { display: flex; flex-direction: column; align-items: center; gap: 16rpx; }
.icon { font-size: 60rpx; }
.text { font-size: 26rpx; color: #909399; }
.submit-btn { background: linear-gradient(135deg, #4CAF50, #66BB6A); color: white; border-radius: 50rpx; font-size: 32rpx; font-weight: 600; padding: 10rpx 0; box-shadow: 0 8rpx 20rpx rgba(76, 175, 80, 0.3); }
.submit-btn.disabled { background: #BDBDBD; box-shadow: none; color: #FFF; }
</style>