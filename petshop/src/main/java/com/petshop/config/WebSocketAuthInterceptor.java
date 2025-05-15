package com.petshop.config;// 📁 src/main/java/com/petshop/config/WebSocketAuthInterceptor.java
import com.petshop.service.OnlineStatusService;
import com.petshop.utils.JwtUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@Slf4j
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private OnlineStatusService onlineStatusService;
//握手前
    @Override
    public boolean beforeHandshake(
            @NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response,
            @NonNull  WebSocketHandler wsHandler,
            @NonNull Map<String, Object> attributes) {

        // ✅ 从 Header 或 URL 参数提取 Token（不再处理 SockJS 的 /info 请求）
        String token = extractToken(request);
        if (!StringUtils.hasText(token)) {
            log.error("❌ Token 提取失败: URL={}", request.getURI());
            response.setStatusCode(HttpStatus.UNAUTHORIZED); // ✅ 明确返回 401
            return false;
        }
        log.info("✅ 提取 Token: {}", token);

        // ✅ 验证 Token 有效性
        if (!jwtUtils.validateToken(token)) {
            log.error("❌ Token 验证失败: token={}", token);
            response.setStatusCode(HttpStatus.UNAUTHORIZED); // ✅ 明确返回 401
            return false;
        }

        // ✅ 新增：存储原始 Token 到会话属性
        attributes.put("token", token); // 关键行

        try {
            // ✅ 解析用户信息
            Integer userId = jwtUtils.getUserIdFromToken(token);
            log.info("✅ 用户ID解析成功: userId={}", userId);

            // ✅ 将 userId 转为 String 存储
            attributes.put("userId", userId.toString());
            onlineStatusService.userOnline(userId.toString());

            return true;
        } catch (Exception e) {
            log.error("❌ 用户ID解析失败: token={}, 错误详情={}", token, e.getMessage(), e);
            return false;
        }
    }

    private String extractToken(ServerHttpRequest request) {
        // 1. 从 Authorization Header 提取
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        if (request instanceof ServletServerHttpRequest servletRequest) {
            return servletRequest.getServletRequest().getParameter("token");
        }

        return null;
    }

    @Override
    public void afterHandshake(
           @NonNull ServerHttpRequest request,
           @NonNull ServerHttpResponse response,
           @NonNull WebSocketHandler wsHandler,
            Exception exception) {
        log.debug("WebSocket 握手完成");
    }
}