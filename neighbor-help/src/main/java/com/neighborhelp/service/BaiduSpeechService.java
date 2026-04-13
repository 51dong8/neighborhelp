package com.neighborhelp.service;

import com.baidu.aip.speech.AipSpeech;
import com.neighborhelp.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;

/**
 * 基于百度语音识别的语音服务
 *
 * 封装与百度 AipSpeech 的交互逻辑，对外只暴露一个识别文本的方法。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BaiduSpeechService {

    private final AipSpeech aipSpeechClient;

    /**
     * 识别上传的音频文件并返回文本
     *
     * 注意：当前假定前端上传的是 16kHz 单声道 PCM 音频（format: pcm）。
     * 如果前端采用其他格式，需要在此处做转码或调整调用参数。
     */
    public String recognize(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("音频文件不能为空");
        }

        log.info("收到语音文件 fileName={}, size={}bytes, contentType={}",
                file.getOriginalFilename(), file.getSize(), file.getContentType());
        
        byte[] data;
        try {
            data = file.getBytes();
            if (data == null || data.length == 0) {
                throw new BusinessException("音频文件内容为空");
            }
            log.debug("读取音频数据成功 size={}bytes", data.length);
        } catch (IOException e) {
            log.error("读取音频文件失败: {}", e.getMessage());
            throw new BusinessException("读取音频文件失败：" + e.getMessage());
        }

        // 检测音频格式
        String format = detectAudioFormat(file.getOriginalFilename(), file.getContentType());
        
        // 如果是 WAV 格式，尝试从文件头读取实际采样率
        int sampleRate = 16000; // 默认采样率
        if ("wav".equals(format)) {
            try {
                int detectedRate = detectWavSampleRate(data);
                if (detectedRate > 0) {
                    sampleRate = detectedRate;
                    log.debug("从 WAV 文件头检测到采样率={}Hz", sampleRate);
                } else {
                    log.debug("无法从 WAV 文件头读取采样率，使用默认值={}Hz", sampleRate);
                }
            } catch (Exception e) {
                log.warn("读取 WAV 采样率失败，使用默认值: {}", e.getMessage());
            }
        }
        
        // 调用百度语音识别 API
        HashMap<String, Object> options = new HashMap<>();
        // 1537: 普通话输入法模型，支持中文
        options.put("dev_pid", 1537);
        
        log.info("调用百度语音识别 API format={}, sampleRate={}, payload={}bytes", format, sampleRate, data.length);

        JSONObject res = aipSpeechClient.asr(data, format, sampleRate, options);
        log.debug("百度API响应 err_no={}, err_msg={}", res.optInt("err_no", -1), res.optString("err_msg", ""));
        
        int errNo = res.optInt("err_no", -1);
        if (errNo != 0) {
            String errMsg = res.optString("err_msg", "未知错误");
            log.warn("百度语音识别失败 errNo={}, errMsg={}", errNo, errMsg);
            throw new BusinessException("语音识别失败（错误码：" + errNo + "）：" + errMsg);
        }

        JSONArray resultArray = res.optJSONArray("result");
        // org.json.JSONArray 在部分版本中没有 isEmpty 方法，这里使用 length() 判断
        if (resultArray == null || resultArray.length() == 0) {
            log.warn("语音识别结果为空");
            throw new BusinessException("语音识别结果为空，请重试");
        }

        // 百度通常会返回一个字符串数组，取第一条作为识别文本
        String text = resultArray.optString(0, "");
        if (text == null || text.trim().isEmpty()) {
            log.warn("识别文本为空");
            throw new BusinessException("识别文本为空，请重试");
        }

        log.info("语音识别成功 textLength={}", text.trim().length());
        return text.trim();
    }
    
    /**
     * 检测音频格式
     * 根据文件名和 Content-Type 判断音频格式
     * 
     * 注意：百度语音识别支持的格式：pcm, wav, amr, m4a
     * webm 格式不支持，需要在前端转换为 wav
     */
    private String detectAudioFormat(String fileName, String contentType) {
        if (fileName != null) {
            String lowerName = fileName.toLowerCase();
            if (lowerName.endsWith(".pcm")) {
                return "pcm";
            } else if (lowerName.endsWith(".wav")) {
                return "wav";
            } else if (lowerName.endsWith(".mp3")) {
                return "mp3";
            } else if (lowerName.endsWith(".m4a")) {
                return "m4a";
            } else if (lowerName.endsWith(".amr")) {
                return "amr";
            } else if (lowerName.endsWith(".webm")) {
                // webm 格式不支持，但尝试使用 wav 格式（如果前端已转换）
                log.warn("检测到 webm 文件扩展名，尝试按 wav 处理");
                return "wav";
            }
        }
        
        if (contentType != null) {
            String lowerType = contentType.toLowerCase();
            if (lowerType.contains("pcm") || lowerType.contains("audio/l16")) {
                return "pcm";
            } else if (lowerType.contains("wav")) {
                return "wav";
            } else if (lowerType.contains("mp3") || lowerType.contains("mpeg")) {
                return "mp3";
            } else if (lowerType.contains("m4a") || lowerType.contains("mp4")) {
                return "m4a";
            } else if (lowerType.contains("amr")) {
                return "amr";
            } else if (lowerType.contains("webm")) {
                // webm 格式不支持，但尝试使用 wav 格式
                log.warn("检测到 webm content-type，尝试按 wav 处理");
                return "wav";
            }
        }
        
        // 默认使用 wav（更通用）
        log.warn("无法检测音频格式，默认使用 wav");
        return "wav";
    }
    
    /**
     * 从 WAV 文件头读取采样率
     * WAV 文件格式：RIFF header (12 bytes) + fmt chunk (至少 24 bytes)
     * 采样率在偏移量 24-27 字节（小端序）
     */
    private int detectWavSampleRate(byte[] data) {
        if (data == null || data.length < 44) {
            return 0; // 文件太小，不是有效的 WAV 文件
        }
        
        // 检查 RIFF 头
        if (data[0] != 'R' || data[1] != 'I' || data[2] != 'F' || data[3] != 'F') {
            return 0; // 不是 WAV 文件
        }
        
        // 检查 WAVE 标识
        if (data[8] != 'W' || data[9] != 'A' || data[10] != 'V' || data[11] != 'E') {
            return 0; // 不是 WAV 文件
        }
        
        // 读取采样率（偏移量 24-27，小端序）
        int sampleRate = (data[24] & 0xFF) | 
                        ((data[25] & 0xFF) << 8) | 
                        ((data[26] & 0xFF) << 16) | 
                        ((data[27] & 0xFF) << 24);
        
        return sampleRate;
    }
}


