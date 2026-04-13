package com.neighborhelp.vo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户VO
 */
@Data
public class UserVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String username;
    private String email;
    private String avatar;
    private Integer level;
    private Integer points;
    private Integer helpCount;
    private BigDecimal rating;
    private Integer joinDays;
    private String phone;
    private String wechat;
    private String address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer authStatus;    // 认证状态：0未认证，1审核中，2已认证
    private Long communityId;      // 社区ID
    private String communityName;  // 社区名称
}

