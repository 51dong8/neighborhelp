package com.neighborhelp.vo;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 订单VO
 */
@Data
public class OrderVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long requestId;
    private String title;
    private String description;
    private String serviceType;
    private String status;
    private Integer rewardAmount;
    private LocalDateTime expectedTime;
    private String location;
    private String urgency;
    private UserVO publisher;
    private LocalDateTime acceptedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private Integer progress;
    private String acceptMessage;
    private String cancelReason;
    private String completeMessage;
    private List<String> completeImages;
    private Map<String, Object> serviceDetails;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

