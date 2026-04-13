package com.neighborhelp.service.impl;

import com.neighborhelp.common.ResultCode;
import com.neighborhelp.common.exception.BusinessException;
import com.neighborhelp.dto.LoginDTO;
import com.neighborhelp.dto.RefreshTokenDTO;
import com.neighborhelp.entity.User;
import com.neighborhelp.service.AuthService;
import com.neighborhelp.service.UserService;
import com.neighborhelp.utils.JwtUtil;
import com.neighborhelp.utils.PasswordUtil;
import com.neighborhelp.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * 认证服务实现
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String TOKEN_BLACKLIST_PREFIX = "auth:blacklist:";

    private final UserService userService;
    private final PasswordUtil passwordUtil;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 查找用户
        User user = userService.findByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 验证密码
        if (!passwordUtil.matches(loginDTO.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

        // 构建响应
        LoginVO loginVO = new LoginVO();
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setEmail(user.getEmail());
        loginVO.setToken(token);
        loginVO.setRefreshToken(refreshToken);
        loginVO.setUserInfo(userService.toVO(user));

        return loginVO;
    }

    @Override
    public LoginVO refreshToken(RefreshTokenDTO refreshTokenDTO) {
        String refreshToken = refreshTokenDTO.getRefreshToken();

        // 验证RefreshToken
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        if (!jwtUtil.isRefreshToken(refreshToken)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        if (isTokenBlacklisted(refreshToken)) {
            throw new BusinessException(ResultCode.TOKEN_REVOKED);
        }

        // 获取用户信息
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        String username = jwtUtil.getUsernameFromToken(refreshToken);

        if (userId == null || username == null) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 生成新的Token
        String newToken = jwtUtil.generateToken(user.getId(), user.getUsername());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

        // 刷新成功后立即废弃旧refresh token，防止重复使用
        blacklistToken(refreshToken);

        // 构建响应
        LoginVO loginVO = new LoginVO();
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setEmail(user.getEmail());
        loginVO.setToken(newToken);
        loginVO.setRefreshToken(newRefreshToken);
        loginVO.setUserInfo(userService.toVO(user));

        return loginVO;
    }

    @Override
    public void logout(String accessToken, String refreshToken) {
        if (StringUtils.hasText(accessToken)) {
            blacklistToken(accessToken);
        }
        if (StringUtils.hasText(refreshToken)) {
            blacklistToken(refreshToken);
        }
    }

    private boolean isTokenBlacklisted(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }
        Boolean exists = stringRedisTemplate.hasKey(getBlacklistKey(token));
        return Boolean.TRUE.equals(exists);
    }

    private void blacklistToken(String token) {
        if (!StringUtils.hasText(token)) {
            return;
        }
        long ttlMs = jwtUtil.getRemainingValidityMillis(token);
        if (ttlMs <= 0) {
            return;
        }
        stringRedisTemplate.opsForValue().set(getBlacklistKey(token), "1", Duration.ofMillis(ttlMs));
    }

    private String getBlacklistKey(String token) {
        return TOKEN_BLACKLIST_PREFIX + DigestUtils.md5DigestAsHex(token.getBytes(StandardCharsets.UTF_8));
    }
}

