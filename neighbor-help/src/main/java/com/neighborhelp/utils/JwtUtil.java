package com.neighborhelp.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 */
@Component
public class JwtUtil {

    private static final String DEFAULT_FALLBACK_SECRET = "neighbor-help-jwt-fallback-secret-2026-safe-key-9mN4xPq7Ls2T";
    private static final int MIN_SECRET_LENGTH = 32;

    @Value("${jwt.secret:neighbor-help-jwt-secret-2026-secure-key-7xQ9mLp2Vn8K}")
    private String secret;

    @Value("${jwt.expiration:604800000}") // 7天，单位：毫秒
    private Long expiration;

    @Value("${jwt.refresh-expiration:2592000000}") // 30天，单位：毫秒
    private Long refreshExpiration;

    /**
     * 生成密钥
     */
    private SecretKey getSigningKey() {
        String actualSecret = resolveSecret();
        return Keys.hmacShaKeyFor(actualSecret.getBytes(StandardCharsets.UTF_8));
    }

    private String resolveSecret() {
        if (StringUtils.hasText(secret) && secret.length() >= MIN_SECRET_LENGTH) {
            return secret;
        }
        return DEFAULT_FALLBACK_SECRET;
    }

    /**
     * 生成Token
     */
    public String generateToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        return generateToken(claims, expiration);
    }

    /**
     * 生成RefreshToken
     */
    public String generateRefreshToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("type", "refresh");
        return generateToken(claims, refreshExpiration);
    }

    /**
     * 生成Token（内部方法）
     */
    private String generateToken(Map<String, Object> claims, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 从Token中获取Claims
     */
    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从Token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims != null) {
            Object userId = claims.get("userId");
            if (userId instanceof Integer) {
                return ((Integer) userId).longValue();
            } else if (userId instanceof Long) {
                return (Long) userId;
            }
        }
        return null;
    }

    /**
     * 从Token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.get("username", String.class) : null;
    }

    /**
     * 验证Token是否有效
     */
    public Boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims != null && !isTokenExpired(claims);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断Token是否过期
     */
    private Boolean isTokenExpired(Claims claims) {
        Date expiration = claims.getExpiration();
        return expiration.before(new Date());
    }

    /**
     * 判断是否为RefreshToken
     */
    public Boolean isRefreshToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims != null) {
            Object type = claims.get("type");
            return "refresh".equals(type);
        }
        return false;
    }

    /**
     * 获取Token过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.getExpiration() : null;
    }

    /**
     * 获取Token剩余有效时长（毫秒）
     */
    public long getRemainingValidityMillis(String token) {
        Date expirationDate = getExpirationDateFromToken(token);
        if (expirationDate == null) {
            return 0L;
        }
        return Math.max(expirationDate.getTime() - System.currentTimeMillis(), 0L);
    }
}
