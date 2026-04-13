/**
 * 语音识别：统一解析后端 /speech/recognize 的 URL、响应与重试（H5 fetch + uni.uploadFile）
 */

import { getApiConfig } from './apiConfig'

export function getSpeechRecognizeUrl() {
  const cfg = getApiConfig()
  const base = (cfg.apiBaseUrl || 'http://localhost:8080/api').replace(/\/+$/, '')
  return `${base}/speech/recognize`
}

export function parseRecognizeResponseJson(responseData) {
  if (!responseData || typeof responseData !== 'object') {
    return { text: '', ok: false, message: '响应格式错误' }
  }
  if (responseData.code === 200 && responseData.data) {
    const text = responseData.data.text != null ? String(responseData.data.text) : ''
    return { text: text.trim(), ok: true, message: responseData.message }
  }
  return {
    text: '',
    ok: false,
    message: responseData.message || '识别失败'
  }
}

export async function recognizeSpeechWithFetch(audioBlob, fileName, mimeType, options = {}) {
  const maxRetries = options.maxRetries ?? 2
  const uploadUrl = getSpeechRecognizeUrl()
  let lastErr
  for (let attempt = 0; attempt <= maxRetries; attempt++) {
    try {
      const formData = new FormData()
      const audioFile = new File([audioBlob], fileName, { type: mimeType })
      formData.append('file', audioFile)
      const token = uni.getStorageSync('token')
      const headers = { Accept: 'application/json' }
      if (token) {
        headers.Authorization = `Bearer ${token}`
      }
      const response = await fetch(uploadUrl, {
        method: 'POST',
        headers,
        body: formData
      })
      const responseData = await response.json()
      const parsed = parseRecognizeResponseJson(responseData)
      if (response.ok && parsed.ok && parsed.text) {
        return parsed.text
      }
      lastErr = new Error(parsed.message || '识别失败')
    } catch (e) {
      lastErr = e
    }
    if (attempt < maxRetries) {
      await new Promise((r) => setTimeout(r, 400 * (attempt + 1)))
    }
  }
  throw lastErr || new Error('识别失败')
}

export async function recognizeSpeechWithUniUpload(filePath, maxRetries = 2) {
  const uploadUrl = getSpeechRecognizeUrl()
  const token = uni.getStorageSync('token')
  const header = {}
  if (token) {
    header.Authorization = `Bearer ${token}`
  }
  let lastErr
  for (let attempt = 0; attempt <= maxRetries; attempt++) {
    try {
      const text = await new Promise((resolve, reject) => {
        uni.uploadFile({
          url: uploadUrl,
          filePath,
          name: 'file',
          header,
          success: (res) => {
            try {
              const responseData = typeof res.data === 'string' ? JSON.parse(res.data) : res.data
              const parsed = parseRecognizeResponseJson(responseData)
              if (res.statusCode >= 200 && res.statusCode < 300 && parsed.ok && parsed.text) {
                resolve(parsed.text)
              } else {
                reject(new Error(parsed.message || '识别失败'))
              }
            } catch (e) {
              reject(e)
            }
          },
          fail: reject
        })
      })
      return text
    } catch (e) {
      lastErr = e
      if (attempt < maxRetries) {
        await new Promise((r) => setTimeout(r, 400 * (attempt + 1)))
      }
    }
  }
  throw lastErr || new Error('上传失败')
}
