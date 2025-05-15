package com.petshop.common;

import com.petshop.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtInterceptor implements HandlerInterceptor {
    private static final String CREATE_PET_URI = "/api/pets";
    private static final String CREATE_PET_METHOD = "POST";
    private static final String ADMIN_PATH_PREFIX = "/api/admin/";
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/api/auth/login",
            "/api/auth/register"
    );

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) throws Exception {

        // 放行 OPTIONS 预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String requestURI = request.getRequestURI();

        // 前缀匹配放行
        if (WHITE_LIST.stream().anyMatch(requestURI::startsWith)) {
            return true;
        }

        // 校验 Token 是否存在
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendError(response, 401, "未提供有效的Token");
            return false;
        }

        String token = authHeader.substring(7); // 提取实际Token
        try {
            // 解析Token
            Claims claims = jwtUtils.parseClaims(token);
            // 提取用户信息
            Integer userId = parseUserId(claims);
            List<String> roles = parseRoles(claims);

            // 绑定用户信息到请求
            request.setAttribute("userId", userId);
            request.setAttribute("roles", roles);

            // 管理员接口权限校验
            if (isAdminEndpoint(request) && !hasAdminRole(roles)) {
                sendError(response, 403, "需要管理员权限");
                return false;
            }
            // 🔥 新增卖家接口权限校验
            if (isCreatePetEndpoint(request) && !hasSellerOrAdminRole(roles)) {
                sendError(response, 403, "需要卖家或管理员权限");
                return false;
            }
            return true;

        } catch (JwtException e) {
            sendError(response, 401, "Token无效: " + e.getMessage());
            return false;
        } catch (NumberFormatException e) {
            sendError(response, 401, "用户ID格式错误");
            return false;
        }
    }

    //======================= 工具方法 =======================//
    // 安全解析 userId
    private Integer parseUserId(Claims claims) {
        Object userIdValue = claims.get("userId");
        if (userIdValue == null) {
            throw new JwtException("Token缺少用户ID");
        }
        // 兼容数字类型和字符串类型的 userId
        if (userIdValue instanceof Integer) {
            return (Integer) userIdValue;
        } else if (userIdValue instanceof String) {
            try {
                return Integer.parseInt((String) userIdValue);
            } catch (NumberFormatException e) {
                throw new JwtException("用户ID必须是整数");
            }
        } else {
            throw new JwtException("用户ID类型无效");
        }
    }

    // 安全解析 roles
    private List<String> parseRoles(Claims claims) {
        Object rolesValue = claims.get("roles");
        if (rolesValue == null) {
            return Collections.emptyList();
        }

        // 确保 roles 是字符串列表
        if (rolesValue instanceof List<?> rawList) {
            return rawList.stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
        } else {
            throw new JwtException("角色信息格式错误");
        }
    }

    // 判断是否为管理员接口
    private boolean isAdminEndpoint(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith(ADMIN_PATH_PREFIX) || uri.contains("/admin/");
    }
    // 校验管理员权限
    private boolean hasAdminRole(List<String> roles) {
        return roles.stream()
                .anyMatch(role ->
                        role != null && (
                                role.equalsIgnoreCase("ADMIN") ||
                                        role.equalsIgnoreCase("ROLE_ADMIN") ||
                                        role.equalsIgnoreCase("admin") // 直接匹配大写的 ADMIN
                        )
                );
    }

    // 校验卖家或管理员权限
    private boolean hasSellerOrAdminRole(List<String> roles) {
        return roles.stream()
                .anyMatch(role ->
                        "SELLER".equalsIgnoreCase(role) ||
                                "ADMIN".equalsIgnoreCase(role)
                );
    }
    // 判断是否为上架宠物接口
    private boolean isCreatePetEndpoint(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        return uri.equalsIgnoreCase(CREATE_PET_URI)
                && method.equalsIgnoreCase(CREATE_PET_METHOD);
    }
    // 发送标准错误响应
    private void sendError(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(code);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                String.format("{\"code\": %d, \"message\": \"%s\"}", code, message)
        );
    }
}