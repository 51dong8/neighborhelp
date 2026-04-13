package com.neighborhelp.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 积分记录VO
 */
@Data
public class PointRecordVO {
    private Long id;
    private Long userId;
    private String type;
    private Integer amount;
    private String description;
    private Long relatedId;
    private String relatedType;
    private LocalDateTime createdAt;
}

