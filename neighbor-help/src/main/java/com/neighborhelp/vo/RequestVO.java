package com.neighborhelp.vo;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 需求VO
 */
@Data
public class RequestVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;
    private String description;
    private String serviceType;
    private String status;
    private Integer rewardAmount;
    private String rewardType;
    private LocalDateTime expectedTime;
    private String location;
    private String urgency;
    private UserVO publisher;
    private UserVO acceptedBy;
    private LocalDateTime acceptedAt;
    private Map<String, Object> serviceDetails;

    // ===== 新增距离字段（仅前端展示用，数据库没有） =====
    private Double distance;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

