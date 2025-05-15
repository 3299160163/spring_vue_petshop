package com.petshop.utils;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;


@Component
@Getter
@Slf4j
public class JwtUtils {
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationMs;

    // 生成安全密钥
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // 方法二：修复后的生成 Token 方法
    public String generateToken(Integer userId, List<String> roles) {
        return Jwts.builder()
                .subject(String.valueOf(userId))  // 可选：将 userId 作为 subject
                .claim("userId", userId)          // 关键：必须添加 userId 声明
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSecretKey(), Jwts.SIG.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // 解析用户ID
    // ✅ 解析用户ID
    public Integer getUserIdFromToken(String token) {
        try {
            // ✅ 移除 "Bearer " 前缀
            String rawToken = token.replace("Bearer ", "");
            log.debug("解析原始 Token: {}", rawToken);

            SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(rawToken)
                    .getPayload();

            Integer userId = claims.get("userId", Integer.class);
            log.debug("解析结果: userId={}", userId);
            return userId;
        } catch (Exception e) {
            log.error("Token 解析失败: {}", e.getMessage());
            return null;
        }
    }




    // 解析用户名
    public String getUsernameFromToken(String token) {
        return parseClaims(token).get("sub", String.class);
    }

    // 解析角色列表（安全转换）
    public List<String> getRolesFromToken(String token) {
        List<?> rawRoles = parseClaims(token).get("roles", List.class);
        return convertToRoleList(rawRoles);
    }
    // 安全转换 List<?> 为 List<String>
    private List<String> convertToRoleList(List<?> rawList) {
        if (rawList == null) {
            return Collections.emptyList();
        }
        List<String> roles = new ArrayList<>();
        for (Object role : rawList) {

            if (role instanceof String) {
                roles.add((String) role);
            } else {
                throw new JwtException("角色类型无效: " + role.getClass());
            }
//            roles.add(String.valueOf(role));
        }
        return roles;
    }

    // 统一解析 Claims
    // 解析 Claims（带明确异常信息）
    public Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new JwtException("Token已过期", e);
        } catch (JwtException | IllegalArgumentException e) {
            throw new JwtException("无效的Token", e);
        }
    }

}