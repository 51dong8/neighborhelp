package com.neighborhelp.vo;

import lombok.Data;
import java.io.Serializable;

/**
 * 登录响应VO
 */
@Data
public class LoginVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String username;
    private String email;
    private String token;
    private String refreshToken;
    private UserVO userInfo;
}

