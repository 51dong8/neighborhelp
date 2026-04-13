package com.neighborhelp.config;

import com.baidu.aip.speech.AipSpeech;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Data
@Configuration
@ConfigurationProperties(prefix = "baidu.speech")
public class BaiduSpeechConfig {

    private String appId;
    private String apiKey;
    private String secretKey;

    @Bean
    public AipSpeech aipSpeechClient() {
        // 修改点：使用 StringUtils.hasText 检查 null 和空字符串
        if (!StringUtils.hasText(appId) || !StringUtils.hasText(apiKey) || !StringUtils.hasText(secretKey)) {
            // 如果缺少配置，给出明确的提示
            throw new IllegalStateException("百度语音配置不完整，请检查 application.yml 中的 baidu.speech 配置，或检查环境变量是否生效。当前 AppID: " + appId);
        }

        System.out.println("初始化百度语音客户端 - AppID: " + appId + ", APIKey: " + apiKey.substring(0, Math.min(8, apiKey.length())) + "...");
        AipSpeech client = new AipSpeech(appId, apiKey, secretKey);
        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);
        return client;
    }
}