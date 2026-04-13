package com.neighborhelp.service;

import com.neighborhelp.dto.LoginDTO;
import com.neighborhelp.dto.RefreshTokenDTO;
import com.neighborhelp.vo.LoginVO;

/**
 * 认证服务接口
 */
public interface AuthService {
    /**
     * 登录
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 刷新Token
     */
    LoginVO refreshToken(RefreshTokenDTO refreshTokenDTO);

    /**
     * 登出
     */
    void logout(String accessToken, String refreshToken);
}

