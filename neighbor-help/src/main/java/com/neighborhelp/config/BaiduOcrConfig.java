// 文件路径：src/main/java/com/neighborhelp/config/BaiduOcrConfig.java
package com.neighborhelp.config;

import com.baidu.aip.ocr.AipOcr;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Data
@Configuration
@ConfigurationProperties(prefix = "baidu.ocr") // 复用你现有的百度 API 密钥配置
public class BaiduOcrConfig {

    private String appId;
    private String apiKey;
    private String secretKey;

    @Bean
    public AipOcr aipOcrClient() {
        if (!StringUtils.hasText(appId) || !StringUtils.hasText(apiKey) || !StringUtils.hasText(secretKey)) {
            throw new IllegalStateException("百度AI配置不完整，请检查 application.yml");
        }
        AipOcr client = new AipOcr(appId, apiKey, secretKey);
        // 可选：设置网络连接参数
        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);
        return client;
    }
}