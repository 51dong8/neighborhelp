package com.neighborhelp.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 通知VO
 */
@Data
public class NotificationVO {
    private Long id;
    private Long userId;
    private String type;
    private String title;
    private String content;
    private Boolean isRead;
    private Long relatedId;
    private String relatedType;
    private LocalDateTime createdAt;
}

