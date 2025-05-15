package com.petshop.controller;

import com.petshop.dto.LoginRequest;
import com.petshop.entity.User;
import com.petshop.exception.BizException;
import com.petshop.service.UserRoleService;
import com.petshop.service.UserService;
import com.petshop.utils.JwtUtils;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

// src/main/java/com/example/controller/AuthController.java
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRoleService userRoleService; // 注入角色服务

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        log.debug("收到登录请求，用户名: {}", request.getUsername());
        try {
            // 1. 验证用户名密码
            User user = userService.verifyUser(request.getUsername(), request.getPassword());
            // 2. 查询用户角色列表
            List<String> roles = userRoleService.getRolesByUserId(user.getId());
            // 3. 生成包含角色的 Token
            String token = jwtUtils.generateToken(user.getId(), roles); // 关键修改
            // 4. 返回 Token 和角色信息
            return ResponseEntity.ok(Map.of(
                    "code", 200,
                    "message", "登录成功",
                    "data", Map.of(
                            "token", token,
                            "expiresIn", jwtUtils.getExpirationMs() / 1000,
                            "roles", roles
                    )
            ));
        } catch (BizException e) {
            // 统一异常处理
            return ResponseEntity.status(e.getCode()).body(Map.of(
                    "code", e.getCode(),
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User user) {
        try {
            userService.registerUser(user);
            return ResponseEntity.ok(Map.of(
                    "code", 200,
                    "message", "注册成功"
            ));
        } catch (BizException e) {
            return ResponseEntity.status(e.getCode()).body(Map.of(
                    "code", e.getCode(),
                    "message", e.getMessage()
            ));
        }
    }

}