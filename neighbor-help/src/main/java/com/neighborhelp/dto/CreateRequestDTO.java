package com.neighborhelp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 创建需求DTO
 */
@Data
public class CreateRequestDTO {
    @NotBlank(message = "服务类型不能为空")
    private String serviceType;

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "描述不能为空")
    private String description;

    @NotBlank(message = "报酬类型不能为空")
    @Pattern(regexp = "^(points|money)$", message = "报酬类型仅支持 points 或 money")
    private String rewardType;

    @NotNull(message = "报酬金额不能为空")
    @Positive(message = "报酬金额必须大于0")
    private Integer rewardAmount;

    private LocalDateTime expectedTime;

    @NotBlank(message = "地点不能为空")
    private String location; // 前端填写的门牌号等补充信息

    @Pattern(regexp = "^(normal|urgent)$", message = "紧急程度仅支持 normal 或 urgent")
    private String urgency = "normal";

    /**
     * 服务详情
     */
    private Map<String, Object> serviceDetails;

    // ================= 新增经纬度字段 =================
    private Double longitude;
    private Double latitude;
}