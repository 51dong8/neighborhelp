package com.neighborhelp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class AmapService {

    @Value("${amap.key}")
    private String amapKey;

    @Value("${amap.url:https://restapi.amap.com/v3/geocode/regeo}")
    private String amapRegeoUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AmapService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 根据经纬度获取真实详细地址
     *
     * @param longitude 经度 (例如: 116.481488)
     * @param latitude  纬度 (例如: 39.990464)
     * @return 完整的真实中文地址
     */
    public String getRealAddress(Double longitude, Double latitude) {
        if (longitude == null || latitude == null) {
            return null;
        }

        // 拼接高德 API 请求 URL
        // 格式: location=经度,纬度
        String url = String.format("%s?key=%s&location=%s,%s&output=JSON&radius=1000&extensions=all",
                amapRegeoUrl, amapKey, longitude, latitude);

        try {
            log.info("开始调用高德地图获取真实位置, URL: {}", url);
            // 发起 GET 请求
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // 使用 Jackson 解析返回的 JSON
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                
                // 检查状态码 1 表示成功
                String status = rootNode.path("status").asText();
                if ("1".equals(status)) {
                    // 提取格式化后的完整地址 (如: 北京市朝阳区阜通东大街6号)
                    JsonNode regeocodeNode = rootNode.path("regeocode");
                    String formattedAddress = regeocodeNode.path("formatted_address").asText();
                    
                    log.info("获取真实位置成功: {}", formattedAddress);
                    return formattedAddress;
                } else {
                    String info = rootNode.path("info").asText();
                    log.error("高德地图 API 调用失败, 错误信息: {}", info);
                }
            }
        } catch (Exception e) {
            log.error("调用高德地图接口异常", e);
        }
        
        return null; // 如果失败返回 null，你可以根据业务抛出 BusinessException
    }
}