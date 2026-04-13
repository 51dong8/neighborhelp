<template>
  <view class="request-container">
    <!-- 状态栏 -->
    <view class="status-bar">
      <view class="time">9:41</view>
      <view class="status-icons">
        <view class="signal-icon">📶</view>
        <view class="wifi-icon">📶</view>
        <view class="battery-icon">🔋</view>
      </view>
    </view>

    <!-- 顶部导航栏 -->
    <view class="top-nav">
      <view class="nav-left" @click="goBack">
        <view class="back-icon">←</view>
        <text class="back-text">返回</text>
      </view>
      <view class="nav-center">
        <text class="page-title">发布需求</text>
      </view>
      <view class="nav-right">
        <view class="help-btn" @click="showHelp">
          <text class="help-icon">❓</text>
        </view>
        <view 
          class="voice-btn" 
          :class="{ recording: isRecording }" 
          @click="toggleVoiceInput"
          :aria-label="isRecording ? '停止录音' : '开始语音输入'"
        >
          <text class="voice-icon">{{ isRecording ? '⏺️' : '🎤' }}</text>
        </view>
      </view>
    </view>

    <!-- 主内容区域 -->
    <view class="main-content">
      <!-- 页面说明 -->
      <view class="page-description">
        <view class="description-header">
          <text class="description-title">选择服务类型</text>
          <text class="description-subtitle">选择你需要帮助的服务类型，我们将为你匹配最合适的邻居</text>
        </view>
      </view>

      <!-- AI 语音输入 -->
      <view class="ai-voice-card">
        <view class="ai-voice-header">
          <view class="ai-voice-title-wrapper">
            <text class="ai-voice-title">语音发布 · AI 识别</text>
            <text class="ai-voice-subtitle">免手动筛选，直接说出需求</text>
          </view>
          <view class="ai-voice-status" :class="voiceStatusClass">{{ voiceStatusText }}</view>
        </view>

        <view class="ai-voice-body">
          <view 
            class="voice-btn large" 
            :class="{ recording: isRecording }" 
            @click="toggleVoiceInput"
          >
            <text class="voice-icon">{{ isRecording ? '⏺️' : '🎤' }}</text>
          </view>
          <view class="voice-textarea">
            <textarea
              v-model="voiceText"
              class="voice-textarea-field"
              :disabled="isRecording"
              placeholder="按住麦克风或直接输入：例如“家里空调不制冷了，需要维修”"
              maxlength="200"
            ></textarea>
            <view class="voice-hint">
              <text>{{ aiHint }}</text>
              <text class="char-count">{{ voiceText.length }}/200</text>
            </view>
          </view>
        </view>

        <view class="ai-voice-actions">
          <button 
            class="ai-action-btn ghost" 
            @click="analyzeVoiceText"
            :disabled="isAnalyzing || !hasVoiceText"
          >
            {{ isAnalyzing ? '识别中...' : 'AI识别分类' }}
          </button>
          <button 
            class="ai-action-btn primary" 
            @click="applyAISuggestion"
            :disabled="!aiSuggestion"
          >
            一键进入表单
          </button>
        </view>

        <view v-if="aiSuggestion" class="ai-result">
          <view class="ai-result-row">
            <text class="ai-result-label">识别类型</text>
            <text class="ai-result-value">{{ aiSuggestion.label }} · {{ aiSuggestion.confidenceText }}</text>
          </view>
          <view class="ai-result-row">
            <text class="ai-result-label">识别理由</text>
            <text class="ai-result-value">{{ aiSuggestion.reason }}</text>
          </view>
          <view v-if="aiSuggestion.prefillSummary" class="ai-result-row">
            <text class="ai-result-label">预填提示</text>
            <text class="ai-result-value">{{ aiSuggestion.prefillSummary }}</text>
          </view>
        </view>
      </view>

      <!-- 服务类型选择 -->
      <view class="service-types-section">
        <view class="service-types-grid">
          <view 
            class="service-type-card" 
            v-for="service in serviceTypes" 
            :key="service.id"
            @click="goToServiceDetail(service.id)"
          >
            <view class="service-icon" :class="service.iconClass">
              <text class="service-emoji">{{ service.icon }}</text>
            </view>
            <view class="service-info">
              <text class="service-name">{{ service.name }}</text>
              <text class="service-desc">{{ service.description }}</text>
            </view>
            <view class="service-arrow">→</view>
          </view>
        </view>
      </view>

    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { recognizeSpeechWithFetch, recognizeSpeechWithUniUpload } from '@/utils/speechRecognition'

// 响应式数据
const currentTime = ref('9:41')

// 服务类型数据
const serviceTypes = ref([
         { 
           id: 'package', 
           name: '帮我取快递', 
           icon: '📦', 
           iconClass: 'package-icon',
           description: '帮忙代收快递包裹，安全保管'
         },
  { 
    id: 'pet', 
    name: '帮我照顾宠物', 
    icon: '🐕', 
    iconClass: 'pet-icon',
    description: '照顾宠物日常护理，陪伴玩耍'
  },
  { 
    id: 'repair', 
    name: '帮我家电维修', 
    icon: '🔧', 
    iconClass: 'repair-icon',
    description: '家电故障维修，专业技术服务'
  },
  { 
    id: 'shopping', 
    name: '帮我买菜', 
    icon: '🛒', 
    iconClass: 'shopping-icon',
    description: '帮忙购买生活用品，新鲜送达'
  },
  { 
    id: 'cleaning', 
    name: '帮我家清洁', 
    icon: '🧹', 
    iconClass: 'cleaning-icon',
    description: '家庭清洁整理，专业家政服务'
  },
  { 
    id: 'cooking', 
    name: '帮我做美食', 
    icon: '🍳', 
    iconClass: 'cooking-icon',
    description: '代做各种美食料理，美味可口'
  }
])

// 语音 + AI 识别状态
const isRecording = ref(false)
const isAnalyzing = ref(false)
const voiceText = ref('')
const aiSuggestion = ref(null)
const voiceStatus = ref('idle') // idle | recording | recognized | unsupported
const hasVoiceText = computed(() => voiceText.value.trim().length > 0)

// Web Speech API 识别器（H5 可用）
let speechRecognizer = null
// 统一录音管理器（小程序 / App / H5可用）
let recorderManager = null
// MediaRecorder API（H5 环境录音）
let mediaRecorder = null
let audioChunks = []
let mediaStream = null

// 生命周期
onMounted(() => {
  updateTime()
  setInterval(updateTime, 60000)
  initSpeechRecognizer()
  initRecorder()
  initMediaRecorder()
})

// 方法
const updateTime = () => {
  const now = new Date()
  const hours = now.getHours().toString().padStart(2, '0')
  const minutes = now.getMinutes().toString().padStart(2, '0')
  currentTime.value = `${hours}:${minutes}`
}

const goBack = () => {
  // 由于需求页面是tabBar页面，应该使用switchTab返回首页
  uni.switchTab({
    url: '/pages/home/home'
  })
}

const goToServiceDetail = (serviceType) => {
  // 跳转到对应的服务详情页面
  uni.navigateTo({
    url: `/pages/service-detail/service-detail?type=${serviceType}`
  })
}

const showHelp = () => {
  uni.showModal({
    title: '使用帮助',
    content: '选择你需要帮助的服务类型，然后填写详细信息即可发布需求',
    showCancel: false
  })
}

// 初始化 uni-app 录音管理器（用于后端语音识别）
const initRecorder = () => {
  console.log('开始初始化录音管理器...')
  try {
    // #ifdef MP-WEIXIN || APP-PLUS
    // 只在微信小程序和 App 中使用 uni.getRecorderManager
    if (typeof uni !== 'undefined' && uni.getRecorderManager) {
      try {
        recorderManager = uni.getRecorderManager()
        // 验证 recorderManager 是否真的可用
        if (!recorderManager || typeof recorderManager.onStart !== 'function') {
          console.warn('recorderManager 创建失败或方法不可用')
          recorderManager = null
          return
        }
        
        console.log('录音管理器创建成功')
        
        recorderManager.onStart(() => {
          console.log('录音开始')
          isRecording.value = true
          voiceStatus.value = 'recording'
          uni.showToast({
            title: '开始录音...',
            icon: 'none',
            duration: 1000
          })
        })
        
        recorderManager.onStop((res) => {
          console.log('录音停止，结果:', res)
          isRecording.value = false
          const { tempFilePath } = res
          if (tempFilePath) {
            console.log('录音文件路径:', tempFilePath)
            uploadVoiceForRecognition(tempFilePath)
          } else {
            console.error('录音文件路径为空')
            voiceStatus.value = 'unsupported'
            uni.showToast({
              title: '录音失败，未生成文件',
              icon: 'none'
            })
          }
        })
        
        recorderManager.onError((err) => {
          console.error('录音错误:', err)
          isRecording.value = false
          voiceStatus.value = 'unsupported'
          uni.showToast({
            title: '录音失败: ' + (err.errMsg || '请检查麦克风权限'),
            icon: 'none',
            duration: 3000
          })
        })
        
        console.log('录音管理器初始化完成')
      } catch (e) {
        console.error('创建 recorderManager 失败:', e)
        recorderManager = null
      }
    } else {
      console.warn('uni.getRecorderManager 不可用')
      recorderManager = null
    }
    // #endif
    
    // #ifdef H5
    // H5 环境下不使用 uni.getRecorderManager，只使用 Web Speech API
    console.log('H5 环境，跳过 uni.getRecorderManager，使用 Web Speech API')
    recorderManager = null
    // #endif
  } catch (e) {
    console.error('初始化录音管理器失败:', e)
    recorderManager = null
  }
}

const initSpeechRecognizer = () => {
  console.log('开始初始化 Web Speech API...')
  try {
    if (typeof window !== 'undefined') {
      const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
      if (SpeechRecognition) {
        console.log('Web Speech API 可用，创建识别器')
        speechRecognizer = new SpeechRecognition()
        speechRecognizer.continuous = false
        speechRecognizer.interimResults = false
        speechRecognizer.lang = 'zh-CN'
        
        speechRecognizer.onstart = () => {
          console.log('Web Speech 录音开始')
          isRecording.value = true
          voiceStatus.value = 'recording'
          uni.showToast({
            title: '正在聆听...',
            icon: 'none',
            duration: 1000
          })
        }
        
        speechRecognizer.onend = () => {
          console.log('Web Speech 录音结束')
          isRecording.value = false
          if (voiceStatus.value === 'recording') {
            voiceStatus.value = 'idle'
          }
        }
        
        speechRecognizer.onerror = (event) => {
          console.error('Web Speech 错误:', event.error)
          isRecording.value = false
          
          // 如果是网络错误，自动回退到 MediaRecorder + 后端识别
          if (event.error === 'network') {
            console.log('Web Speech 网络错误，自动切换到 MediaRecorder + 后端识别')
            voiceStatus.value = 'idle'
            // 自动启动 MediaRecorder
            if (typeof navigator !== 'undefined' && navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
              setTimeout(() => {
                console.log('自动启动 MediaRecorder 作为备用方案')
                startMediaRecorder()
              }, 500)
            } else {
              voiceStatus.value = 'unsupported'
              uni.showToast({
                title: '网络错误，且无法使用备用录音方案',
                icon: 'none',
                duration: 3000
              })
            }
            return
          }
          
          voiceStatus.value = 'unsupported'
          
          let errorMsg = '语音识别暂不可用'
          if (event.error === 'not-allowed') {
            errorMsg = '麦克风权限被拒绝，请在浏览器设置中允许'
          } else if (event.error === 'no-speech') {
            errorMsg = '未检测到语音，请重试'
          }
          
          uni.showToast({
            title: errorMsg,
            icon: 'none',
            duration: 3000
          })
        }
        
        speechRecognizer.onresult = (event) => {
          console.log('Web Speech 识别结果:', event)
          if (event.results && event.results[0] && event.results[0][0]) {
            const transcript = event.results[0][0].transcript || ''
            console.log('识别文本:', transcript)
            voiceText.value = transcript
            voiceStatus.value = 'recognized'
            uni.showToast({
              title: '识别成功',
              icon: 'success',
              duration: 2000
            })
          } else {
            console.warn('识别结果为空')
            voiceStatus.value = 'idle'
          }
        }
        
        console.log('Web Speech API 初始化完成')
      } else {
        console.warn('Web Speech API 不可用（浏览器不支持）')
        voiceStatus.value = 'unsupported'
      }
    } else {
      console.warn('window 对象不可用')
      voiceStatus.value = 'unsupported'
    }
  } catch (error) {
    console.error('初始化 Web Speech 失败:', error)
    voiceStatus.value = 'unsupported'
  }
}

// 使用 MediaRecorder 录音并上传到后端
const startMediaRecorder = async () => {
  try {
    console.log('开始使用 MediaRecorder 录音...')
    const stream = await navigator.mediaDevices.getUserMedia({ 
      audio: {
        sampleRate: 16000,
        channelCount: 1,
        echoCancellation: true,
        noiseSuppression: true
      } 
    })
    mediaStream = stream
    
    // 创建 MediaRecorder，优先使用高质量格式
    // 注意：我们会在上传前转换为 16kHz 单声道 WAV
    const options = { mimeType: 'audio/webm;codecs=opus' }
    // 如果不支持 webm，尝试其他格式
    if (!MediaRecorder.isTypeSupported(options.mimeType)) {
      options.mimeType = 'audio/webm'
      if (!MediaRecorder.isTypeSupported(options.mimeType)) {
        options.mimeType = 'audio/mp4'
      }
    }
    
    console.log('使用录音格式:', options.mimeType)
    
    mediaRecorder = new MediaRecorder(stream, options)
    audioChunks = []
    
    mediaRecorder.ondataavailable = (event) => {
      if (event.data.size > 0) {
        audioChunks.push(event.data)
      }
    }
    
    mediaRecorder.onstop = async () => {
      console.log('MediaRecorder 停止，准备上传')
      if (audioChunks.length === 0) {
        console.error('没有录音数据')
        uni.showToast({
          title: '录音失败，未生成数据',
          icon: 'none'
        })
        return
      }
      
      // 将录音数据转换为 Blob
      const audioBlob = new Blob(audioChunks, { type: mediaRecorder.mimeType })
      console.log('录音数据大小:', audioBlob.size, 'bytes, 类型:', audioBlob.type)
      
      // 停止媒体流
      if (mediaStream) {
        mediaStream.getTracks().forEach(track => track.stop())
        mediaStream = null
      }
      
      // 如果格式是 webm，需要转换为 wav（百度不支持 webm）
      if (audioBlob.type.includes('webm')) {
        console.log('检测到 webm 格式，转换为 wav 格式...')
        try {
          const wavBlob = await convertWebmToWav(audioBlob)
          console.log('转换成功，新大小:', wavBlob.size, 'bytes')
          uploadAudioBlob(wavBlob, 'recording.wav', 'audio/wav')
        } catch (error) {
          console.error('格式转换失败，尝试直接上传:', error)
          // 如果转换失败，尝试直接上传（后端会尝试处理）
          uploadAudioBlob(audioBlob)
        }
      } else {
        // 直接上传
        uploadAudioBlob(audioBlob)
      }
    }
    
    mediaRecorder.onerror = (event) => {
      console.error('MediaRecorder 错误:', event)
      uni.showToast({
        title: '录音失败',
        icon: 'none'
      })
    }
    
    mediaRecorder.start()
    isRecording.value = true
    voiceStatus.value = 'recording'
    console.log('MediaRecorder 已启动')
    uni.showToast({
      title: '开始录音...',
      icon: 'none',
      duration: 1000
    })
  } catch (error) {
    console.error('启动 MediaRecorder 失败:', error)
    voiceStatus.value = 'unsupported'
    uni.showToast({
      title: '录音启动失败: ' + (error.message || '请检查麦克风权限'),
      icon: 'none',
      duration: 3000
    })
  }
}

const stopMediaRecorder = () => {
  if (mediaRecorder && mediaRecorder.state !== 'inactive') {
    console.log('停止 MediaRecorder')
    mediaRecorder.stop()
    isRecording.value = false
  }
  if (mediaStream) {
    mediaStream.getTracks().forEach(track => track.stop())
    mediaStream = null
  }
}

// 将 webm 转换为 wav 格式，并重采样到 16kHz 单声道
const convertWebmToWav = async (webmBlob) => {
  return new Promise((resolve, reject) => {
    const audioContext = new (window.AudioContext || window.webkitAudioContext)()
    const fileReader = new FileReader()
    
    fileReader.onload = async (e) => {
      try {
        const arrayBuffer = e.target.result
        const audioBuffer = await audioContext.decodeAudioData(arrayBuffer)
        
        console.log('原始音频信息:', {
          sampleRate: audioBuffer.sampleRate,
          numberOfChannels: audioBuffer.numberOfChannels,
          length: audioBuffer.length,
          duration: audioBuffer.duration
        })
        
        // 重采样到 16kHz 单声道（百度要求）
        const targetSampleRate = 16000
        const targetChannels = 1
        
        // 如果已经是目标格式，直接转换
        if (audioBuffer.sampleRate === targetSampleRate && audioBuffer.numberOfChannels === targetChannels) {
          console.log('音频已经是 16kHz 单声道，直接转换')
          const wav = audioBufferToWav(audioBuffer)
          const wavBlob = new Blob([wav], { type: 'audio/wav' })
          resolve(wavBlob)
          return
        }
        
        // 需要重采样和转单声道
        console.log('开始重采样和转单声道...')
        const resampledBuffer = await resampleAudioBuffer(
          audioBuffer,
          targetSampleRate,
          targetChannels
        )
        
        console.log('重采样后音频信息:', {
          sampleRate: resampledBuffer.sampleRate,
          numberOfChannels: resampledBuffer.numberOfChannels,
          length: resampledBuffer.length,
          duration: resampledBuffer.duration
        })
        
        // 将 AudioBuffer 转换为 WAV
        const wav = audioBufferToWav(resampledBuffer)
        const wavBlob = new Blob([wav], { type: 'audio/wav' })
        resolve(wavBlob)
      } catch (error) {
        console.error('转换失败:', error)
        reject(error)
      }
    }
    
    fileReader.onerror = reject
    fileReader.readAsArrayBuffer(webmBlob)
  })
}

// 重采样 AudioBuffer 到指定采样率和声道数
const resampleAudioBuffer = async (audioBuffer, targetSampleRate, targetChannels) => {
  const sourceSampleRate = audioBuffer.sampleRate
  const sourceChannels = audioBuffer.numberOfChannels
  const sourceLength = audioBuffer.length
  
  // 计算目标长度
  const targetLength = Math.round(sourceLength * targetSampleRate / sourceSampleRate)
  
  // 创建新的 AudioContext（使用目标采样率）
  const offlineContext = new OfflineAudioContext(
    targetChannels,
    targetLength,
    targetSampleRate
  )
  
  // 创建源节点
  const source = offlineContext.createBufferSource()
  source.buffer = audioBuffer
  
  // 如果是多声道，转换为单声道
  if (sourceChannels > 1 && targetChannels === 1) {
    const merger = offlineContext.createChannelMerger(1)
    source.connect(merger)
    merger.connect(offlineContext.destination)
  } else {
    source.connect(offlineContext.destination)
  }
  
  source.start(0)
  
  // 渲染音频
  const resampledBuffer = await offlineContext.startRendering()
  return resampledBuffer
}

// 将 AudioBuffer 转换为 WAV 格式
const audioBufferToWav = (buffer) => {
  const length = buffer.length
  const numberOfChannels = buffer.numberOfChannels
  const sampleRate = buffer.sampleRate
  const bytesPerSample = 2
  const blockAlign = numberOfChannels * bytesPerSample
  const byteRate = sampleRate * blockAlign
  const dataSize = length * blockAlign
  const bufferSize = 44 + dataSize
  const arrayBuffer = new ArrayBuffer(bufferSize)
  const view = new DataView(arrayBuffer)
  
  // WAV 文件头
  const writeString = (offset, string) => {
    for (let i = 0; i < string.length; i++) {
      view.setUint8(offset + i, string.charCodeAt(i))
    }
  }
  
  writeString(0, 'RIFF')
  view.setUint32(4, bufferSize - 8, true)
  writeString(8, 'WAVE')
  writeString(12, 'fmt ')
  view.setUint32(16, 16, true) // fmt chunk size
  view.setUint16(20, 1, true) // audio format (1 = PCM)
  view.setUint16(22, numberOfChannels, true)
  view.setUint32(24, sampleRate, true)
  view.setUint32(28, byteRate, true)
  view.setUint16(32, blockAlign, true)
  view.setUint16(34, 16, true) // bits per sample
  writeString(36, 'data')
  view.setUint32(40, dataSize, true)
  
  // 写入音频数据
  let offset = 44
  for (let i = 0; i < length; i++) {
    for (let channel = 0; channel < numberOfChannels; channel++) {
      const sample = Math.max(-1, Math.min(1, buffer.getChannelData(channel)[i]))
      view.setInt16(offset, sample < 0 ? sample * 0x8000 : sample * 0x7FFF, true)
      offset += 2
    }
  }
  
  return arrayBuffer
}

// 上传音频 Blob 到后端
const uploadAudioBlob = async (audioBlob, fileName = 'recording.wav', mimeType = 'audio/wav') => {
  console.log('========== 开始上传音频 Blob ==========')
  isAnalyzing.value = true
  voiceStatus.value = 'idle'

  console.log('上传配置:', {
    blobSize: audioBlob.size,
    blobType: audioBlob.type || mimeType,
    fileName: fileName
  })

  try {
    const text = await recognizeSpeechWithFetch(audioBlob, fileName, mimeType, { maxRetries: 2 })
    console.log('✅ 识别成功，文本:', text)
    voiceText.value = text
    voiceStatus.value = 'recognized'
    uni.showToast({
      title: '语音识别成功: ' + text,
      icon: 'success',
      duration: 3000
    })
  } catch (error) {
    console.error('上传失败:', error)
    voiceStatus.value = 'unsupported'
    uni.showToast({
      title: '上传失败: ' + (error.message || '请检查网络'),
      icon: 'none',
      duration: 3000
    })
  } finally {
    isAnalyzing.value = false
    console.log('=====================================')
  }
}

const toggleVoiceInput = () => {
  console.log('========== 点击语音按钮 ==========')
  console.log('当前状态:', {
    isRecording: isRecording.value,
    hasSpeechRecognizer: !!speechRecognizer,
    hasRecorderManager: !!recorderManager,
    hasMediaRecorder: typeof MediaRecorder !== 'undefined',
    voiceStatus: voiceStatus.value
  })
  
  aiSuggestion.value = null
  
  // 如果正在录音，停止录音
  if (isRecording.value) {
    console.log('停止录音')
    if (speechRecognizer) {
      try {
        speechRecognizer.stop()
      } catch (e) {
        console.error('停止 Web Speech 失败:', e)
      }
    }
    if (mediaRecorder && mediaRecorder.state !== 'inactive') {
      stopMediaRecorder()
    }
    if (recorderManager) {
      try {
        recorderManager.stop()
      } catch (e) {
        console.error('停止 recorderManager 失败:', e)
      }
    }
    console.log('=====================================')
    return
  }
  
  // 优先尝试 Web Speech API（如果可用且没有网络问题）
  if (typeof window !== 'undefined' && speechRecognizer && voiceStatus.value !== 'unsupported') {
    console.log('✅ 尝试使用 Web Speech API')
    try {
      speechRecognizer.start()
      console.log('Web Speech 录音已启动')
      console.log('=====================================')
      return
    } catch (e) {
      console.warn('Web Speech 启动失败，尝试备用方案:', e)
    }
  }
  
  // 备用方案：使用 MediaRecorder + 后端识别（H5 环境）
  if (typeof navigator !== 'undefined' && navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
    console.log('✅ 使用 MediaRecorder + 后端识别')
    startMediaRecorder()
    console.log('=====================================')
    return
  }

  // 其次使用 uni 录音并走后端识别（小程序/App 环境）
  if (recorderManager && typeof recorderManager.start === 'function') {
    console.log('✅ 使用 uni 录音管理器')
    console.log('开始录音...')
    // 尝试使用 PCM 格式，如果不支持则使用 mp3
    try {
      console.log('尝试使用 PCM 格式启动录音')
      recorderManager.start({
        format: 'pcm', // 优先使用 PCM（百度识别效果最好）
        sampleRate: 16000,
        numberOfChannels: 1,
        encodeBitRate: 48000,
        duration: 60000
      })
      console.log('PCM 格式录音启动成功')
    } catch (e) {
      // 如果 PCM 不支持，尝试 mp3
      console.warn('PCM 格式不支持，尝试使用 mp3:', e)
      try {
        recorderManager.start({
          format: 'mp3',
          sampleRate: 16000,
          numberOfChannels: 1,
          encodeBitRate: 48000,
          duration: 60000
        })
        console.log('MP3 格式录音启动成功')
      } catch (e2) {
        console.error('录音启动失败:', e2)
        voiceStatus.value = 'unsupported'
        uni.showToast({
          title: '录音启动失败: ' + (e2.message || '请检查设备权限'),
          icon: 'none',
          duration: 3000
        })
      }
    }
    console.log('=====================================')
    return
  }

  // 最后提示不支持
  console.warn('❌ 没有可用的录音方式')
  console.log('可用性检查:', {
    window: typeof window !== 'undefined',
    SpeechRecognition: typeof window !== 'undefined' && (window.SpeechRecognition || window.webkitSpeechRecognition),
    MediaRecorder: typeof MediaRecorder !== 'undefined',
    navigatorMediaDevices: typeof navigator !== 'undefined' && navigator.mediaDevices,
    recorderManager: !!recorderManager
  })
  voiceStatus.value = 'unsupported'
  uni.showToast({
    title: '当前环境不支持语音，请直接输入文本',
    icon: 'none',
    duration: 3000
  })
  console.log('=====================================')
}

// 上传录音到后端进行语音识别
const uploadVoiceForRecognition = async (filePath) => {
  console.log('========== 开始上传语音文件 ==========')
  console.log('文件路径:', filePath)
  
  if (!filePath) {
    console.error('文件路径为空，无法上传')
    uni.showToast({
      title: '录音文件不存在',
      icon: 'none'
    })
    return
  }
  
  isAnalyzing.value = true
  voiceStatus.value = 'idle'

  console.log('开始调用 uni.uploadFile（含重试）...')

  try {
    const text = await recognizeSpeechWithUniUpload(filePath, 2)
    console.log('✅ 识别成功，文本:', text)
    voiceText.value = text
    voiceStatus.value = 'recognized'
    uni.showToast({
      title: '语音识别成功: ' + text,
      icon: 'success',
      duration: 3000
    })
  } catch (err) {
    console.error('========== 上传失败 ==========', err)
    voiceStatus.value = 'unsupported'
    const errorMsg = err.errMsg || err.message || '上传失败'
    let userMsg = '上传失败，请检查网络'
    if (errorMsg.includes('timeout')) {
      userMsg = '请求超时，请检查网络连接'
    } else if (errorMsg.includes('fail') || errorMsg.includes('error')) {
      userMsg = '网络错误，请检查后端服务是否启动（http://localhost:8080）'
    } else if (err.statusCode) {
      userMsg = `请求失败，状态码: ${err.statusCode}`
    } else if (errorMsg.includes('not found') || errorMsg.includes('404')) {
      userMsg = '接口不存在，请检查后端是否启动'
    }
    uni.showToast({
      title: userMsg,
      icon: 'none',
      duration: 3000
    })
  } finally {
    isAnalyzing.value = false
    console.log('=====================================')
  }
}

const aiHint = computed(() => {
  if (voiceStatus.value === 'recording') return '正在聆听，请清晰描述需求...'
  if (voiceStatus.value === 'unsupported') return '当前环境不支持语音，请直接输入文本'
  if (!hasVoiceText.value) return '按麦克风说出需求，或直接输入文字'
  return '点击 AI 识别分类，自动匹配服务类型'
})

const voiceStatusClass = computed(() => {
  return {
    recording: voiceStatus.value === 'recording',
    success: voiceStatus.value === 'recognized',
    warning: voiceStatus.value === 'unsupported'
  }
})

const voiceStatusText = computed(() => {
  if (voiceStatus.value === 'recording') return '录音中'
  if (voiceStatus.value === 'recognized') return '已识别'
  if (voiceStatus.value === 'unsupported') return '语音不可用'
  return '待识别'
})

const analyzeVoiceText = () => {
  if (!hasVoiceText.value) {
    uni.showToast({
      title: '请先输入或说出需求内容',
      icon: 'none'
    })
    return
  }

  isAnalyzing.value = true
  aiSuggestion.value = null

  setTimeout(() => {
    const result = classifyVoiceText(voiceText.value.trim())
    aiSuggestion.value = result
    isAnalyzing.value = false
    uni.showToast({
      title: `识别为：${result.label}`,
      icon: 'none'
    })
  }, 350)
}

const getServiceNameById = (id) => {
  const found = serviceTypes.value.find(item => item.id === id)
  return found ? found.name : '服务'
}

const classifyVoiceText = (text) => {
  const rules = [
    {
      type: 'repair',
      keywords: ['维修', '修', '坏了', '不工作', '不制冷', '漏水', '空调', '冰箱', '洗衣机', '电视', '热水器', '家电'],
      prefill: (raw) => ({
        damageDescription: raw,
        applianceType: detectAppliance(raw)
      }),
      reason: '包含家电/维修相关关键词'
    },
    {
      type: 'package',
      keywords: ['快递', '包裹', '代取', '取件', '驿站', '快件'],
      prefill: (raw) => ({
        pickupLocation: extractLocation(raw),
        pickupTime: '尽快'
      }),
      reason: '包含取件/快递相关词'
    },
    {
      type: 'shopping',
      keywords: ['买菜', '采购', '买水果', '买东西', '超市', '生鲜', '买牛奶', '帮我买'],
      prefill: (raw) => ({
        groceryList: raw,
        customerAddress: extractLocation(raw)
      }),
      reason: '包含购买/采购相关词'
    },
    {
      type: 'cleaning',
      keywords: ['打扫', '清洁', '保洁', '卫生', '擦窗', '拖地', '做卫生'],
      prefill: (raw) => ({
        cleaningItems: raw,
        cleaningTime: '尽快'
      }),
      reason: '包含家务/清洁相关词'
    },
    {
      type: 'cooking',
      keywords: ['做饭', '做菜', '做美食', '厨师', '宴席', '聚餐', '下厨', '做个菜'],
      prefill: (raw) => ({
        dishName: extractDish(raw),
        requirements: raw
      }),
      reason: '包含做饭/烹饪相关词'
    },
    {
      type: 'pet',
      keywords: ['宠物', '猫', '狗', '喂', '遛狗', '猫咪', '狗狗', '铲屎'],
      prefill: (raw) => ({
        animalType: detectAnimal(raw),
        animalName: '小可爱',
        adoptionLocation: extractLocation(raw),
        adoptionDate: '尽快',
        foodType: ''
      }),
      reason: '包含宠物照顾相关词'
    }
  ]

  let best = {
    type: 'package',
    score: 0,
    reason: '未命中特定关键词，默认快递代取'
  }

  rules.forEach(rule => {
    let score = 0
    const hits = []
    rule.keywords.forEach(k => {
      if (text.includes(k)) {
        score += 2
        hits.push(k)
      }
    })
    // 关键实体补分
    if (rule.type === 'repair' && detectAppliance(text)) score += 1.5
    if (rule.type === 'pet' && detectAnimal(text)) score += 1
    if (score > best.score) {
      best = {
        type: rule.type,
        score,
        reason: hits.length ? `命中关键词：${hits.join('、')}` : rule.reason,
        prefill: rule.prefill(text)
      }
    }
  })

  const confidence = Math.min(1, best.score / 6)
  return {
    type: best.type,
    label: getServiceNameById(best.type),
    confidence,
    confidenceText: `${Math.round(confidence * 100)}%`,
    reason: best.reason,
    prefill: best.prefill || {},
    prefillSummary: buildPrefillSummary(best.type, best.prefill || {}, text)
  }
}

const detectAppliance = (text) => {
  const map = ['空调', '冰箱', '洗衣机', '电视', '热水器', '微波炉', '油烟机', '燃气灶']
  const found = map.find(item => text.includes(item))
  return found || ''
}

const detectAnimal = (text) => {
  const map = ['猫', '狗', '龟', '鸟', '鱼']
  const found = map.find(item => text.includes(item))
  if (found === '猫') return '猫类'
  if (found === '狗') return '狗类'
  if (found === '龟') return '乌龟类'
  if (found === '鸟') return '小鸟类'
  if (found === '鱼') return '鱼类'
  return ''
}

const extractDish = (text) => {
  const patterns = ['做', '煮', '做个', '来份', '做一份']
  const keyword = patterns.find(p => text.includes(p))
  if (keyword) {
    const idx = text.indexOf(keyword)
    return text.slice(idx + keyword.length).replace(/[，。.!！]/g, '').slice(0, 12)
  }
  return ''
}

const extractLocation = (text) => {
  const markers = ['小区', '楼', '栋', '单元', '驿站', '超市', '门口']
  const marker = markers.find(m => text.includes(m))
  if (marker) {
    const idx = text.indexOf(marker)
    return text.slice(Math.max(0, idx - 6), idx + marker.length + 4).replace(/[，。.!！]/g, '')
  }
  return ''
}

const buildPrefillSummary = (type, prefill, text) => {
  if (type === 'repair') {
    const target = prefill.applianceType || '家电'
    return `已填充“${target}”及故障描述，确认后可补充微信等信息`
  }
  if (type === 'package') {
    return '已设置“尽快”取件，可补充取件码/位置'
  }
  if (type === 'shopping') {
    return '已将语音内容填入购物清单，可补充地址和联系方式'
  }
  if (type === 'cleaning') {
    return '已填入清洁事项，可补充时间与金额'
  }
  if (type === 'cooking') {
    return '已填入菜品/需求，可补充时间与地点'
  }
  if (type === 'pet') {
    return '已填入宠物类型与时间，可补充喂食信息'
  }
  return text
}

const applyAISuggestion = () => {
  if (!aiSuggestion.value) {
    uni.showToast({
      title: '请先完成 AI 识别',
      icon: 'none'
    })
    return
  }

  // 将 AI 结果暂存到本地，进入详情页读取并预填
  const cacheKey = `ai_prefill_${Date.now()}`
  const payload = {
    ...aiSuggestion.value,
    text: voiceText.value
  }
  uni.setStorageSync(cacheKey, JSON.stringify(payload))

  uni.navigateTo({
    url: `/pages/service-detail/service-detail?type=${aiSuggestion.value.type}&prefillKey=${cacheKey}`
  })
}

</script>

<style scoped>
.request-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #FFF8E1 0%, #FFECB3 100%);
  display: flex;
  flex-direction: column;
  position: relative;
}

/* 状态栏 */
.status-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10rpx 30rpx;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10rpx);
  border-bottom: 1rpx solid rgba(255, 255, 255, 0.2);
}

.time {
  font-size: 28rpx;
  font-weight: 600;
  color: #8D6E63;
}

.status-icons {
  display: flex;
  gap: 12rpx;
  font-size: 24rpx;
}

/* 顶部导航栏 */
.top-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 30rpx;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20rpx);
  border-bottom: 1rpx solid rgba(0, 0, 0, 0.05);
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 8rpx;
  border-radius: 8rpx;
  transition: all 0.3s ease;
}

.nav-left:active {
  background: rgba(0, 0, 0, 0.05);
}

.back-icon {
  font-size: 32rpx;
  color: #8D6E63;
  font-weight: bold;
}

.back-text {
  font-size: 28rpx;
  color: #8D6E63;
}

.nav-center {
  flex: 1;
  text-align: center;
}

.page-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #8D6E63;
}

.nav-right {
  width: auto;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
}

.voice-btn {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #FF8F00, #FFB74D);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 12rpx rgba(255, 143, 0, 0.3);
  transition: all 0.3s ease;
}

.voice-btn.recording {
  background: linear-gradient(135deg, #F44336, #EF5350);
  animation: pulse 1s infinite;
}

.voice-btn.large {
  width: 100rpx;
  height: 100rpx;
}

.voice-icon {
  font-size: 28rpx;
  color: white;
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

/* 主内容区域 */
.main-content {
  flex: 1;
  padding: 0 20rpx;
  padding-bottom: 20rpx;
  overflow: hidden;
}

/* 页面说明 */
.page-description {
  margin: 12rpx 0;
  background: white;
  border-radius: 16rpx;
  padding: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
}

/* AI 语音卡片 */
.ai-voice-card {
  margin: 16rpx 0 8rpx;
  background: white;
  border-radius: 20rpx;
  padding: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.ai-voice-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.ai-voice-title-wrapper {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.ai-voice-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #8D6E63;
}

.ai-voice-subtitle {
  font-size: 22rpx;
  color: #A1887F;
}

.ai-voice-status {
  padding: 8rpx 12rpx;
  border-radius: 12rpx;
  font-size: 22rpx;
  color: #8D6E63;
  background: #F5F5F5;
}

.ai-voice-status.recording {
  color: #F44336;
  background: #FFEBEE;
}

.ai-voice-status.success {
  color: #4CAF50;
  background: #E8F5E9;
}

.ai-voice-status.warning {
  color: #EF6C00;
  background: #FFF3E0;
}

.ai-voice-body {
  display: flex;
  gap: 16rpx;
  align-items: stretch;
}

.voice-textarea {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.voice-textarea-field {
  min-height: 140rpx;
  width: 100%;
  border: 2rpx solid #E0E0E0;
  border-radius: 16rpx;
  padding: 16rpx;
  font-size: 26rpx;
  color: #424242;
  background: #FAFAFA;
  box-sizing: border-box;
}

.voice-textarea-field:disabled {
  background: #F5F5F5;
  color: #9E9E9E;
}

.voice-hint {
  display: flex;
  justify-content: space-between;
  color: #A1887F;
  font-size: 22rpx;
}

.ai-voice-actions {
  display: flex;
  gap: 12rpx;
}

.ai-action-btn {
  flex: 1;
  height: 72rpx;
  border-radius: 16rpx;
  border: none;
  font-size: 26rpx;
  font-weight: 600;
  color: #424242;
  background: #E3F2FD;
}

.ai-action-btn.primary {
  background: linear-gradient(135deg, #4CAF50, #66BB6A);
  color: white;
}

.ai-action-btn.ghost {
  background: #F5F5F5;
  color: #616161;
}

.ai-action-btn:disabled {
  opacity: 0.6;
}

.ai-result {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  padding: 12rpx;
  background: #F8F9FA;
  border-radius: 12rpx;
}

.ai-result-row {
  display: flex;
  gap: 8rpx;
}

.ai-result-label {
  min-width: 140rpx;
  font-size: 24rpx;
  color: #666;
}

.ai-result-value {
  flex: 1;
  font-size: 24rpx;
  color: #424242;
}

.description-header {
  text-align: center;
}

.description-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #8D6E63;
  margin-bottom: 12rpx;
  display: block;
}

.description-subtitle {
  font-size: 24rpx;
  color: #A1887F;
  line-height: 1.5;
  display: block;
}

/* 服务类型区域 */
.service-types-section {
  margin: 32rpx 0;
}

.service-types-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12rpx;
}

.service-type-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16rpx 12rpx;
  background: white;
  border-radius: 20rpx;
  box-shadow: 0 6rpx 24rpx rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  gap: 16rpx;
  position: relative;
  overflow: hidden;
}

.service-type-card:active {
  transform: translateY(-4rpx);
  box-shadow: 0 12rpx 32rpx rgba(0, 0, 0, 0.15);
}

.service-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.service-emoji {
  font-size: 40rpx;
}

.service-info {
  text-align: center;
  flex: 1;
}

.service-name {
  font-size: 26rpx;
  font-weight: 600;
  color: #8D6E63;
  margin-bottom: 8rpx;
  display: block;
}

.service-desc {
  font-size: 22rpx;
  color: #A1887F;
  line-height: 1.4;
  display: block;
}

.service-arrow {
  font-size: 28rpx;
  color: #FF8F00;
  font-weight: bold;
  transition: all 0.3s ease;
}

.service-type-card:active .service-arrow {
  transform: translateX(4rpx);
}

/* 服务图标样式 */
.package-icon {
  background: linear-gradient(135deg, #FF8A65, #FFB74D);
}

.pet-icon {
  background: linear-gradient(135deg, #FFD54F, #FFECB3);
}

.shopping-icon {
  background: linear-gradient(135deg, #81C784, #A5D6A7);
}

.repair-icon {
  background: linear-gradient(135deg, #64B5F6, #90CAF9);
}

.cleaning-icon {
  background: linear-gradient(135deg, #BA68C8, #CE93D8);
}

.cooking-icon {
  background: linear-gradient(135deg, #FF7043, #FFAB91);
}
</style>
