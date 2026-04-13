package com.neighborhelp.controller;

import com.neighborhelp.common.Result;
import com.neighborhelp.service.BaiduSpeechService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 语音识别控制器
 *
 * 当前实现基于百度语音识别 SDK：
 * - 接收前端上传的音频文件（建议 16kHz PCM 单声道）
 * - 调用百度语音服务将音频转换为文本
 */
@Tag(name = "语音识别", description = "接收语音并返回识别文本（百度语音识别）")
@RestController
@RequestMapping("/api/speech")
@RequiredArgsConstructor
@Slf4j
public class SpeechController {

    private final BaiduSpeechService baiduSpeechService;

    @Operation(summary = "测试语音识别服务状态")
    @PostMapping("/test")
    public Result<Map<String, Object>> test() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "服务正常");
        data.put("message", "语音识别服务已就绪");
        return Result.success("测试成功", data);
    }

    @Operation(summary = "上传语音并识别文本")
    @PostMapping("/recognize")
    public Result<Map<String, Object>> recognize(@RequestParam("file") MultipartFile file) {
        log.info("收到语音识别请求 fileName={}, size={}bytes, contentType={}",
                file != null ? file.getOriginalFilename() : "null",
                file != null ? file.getSize() : 0,
                file != null ? file.getContentType() : "null");
        
        if (file == null || file.isEmpty()) {
            log.warn("语音识别失败: 文件为空或未上传");
            return Result.error("请上传有效的音频文件");
        }
        
        if (file.getSize() == 0) {
            log.warn("语音识别失败: 文件大小为0");
            return Result.error("音频文件为空，请重新录制");
        }
        
        try {
            String text = baiduSpeechService.recognize(file);

            Map<String, Object> data = new HashMap<>();
            data.put("text", text);
            data.put("fileName", file.getOriginalFilename());
            data.put("size", file.getSize());

            log.info("语音识别成功 fileName={}, textLength={}",
                    file.getOriginalFilename(),
                    text != null ? text.length() : 0);
            return Result.success("语音识别成功", data);
        } catch (Exception e) {
            log.error("语音识别异常 type={}, message={}", e.getClass().getSimpleName(), e.getMessage());
            return Result.error("语音识别失败：" + e.getMessage());
        }
    }
}

