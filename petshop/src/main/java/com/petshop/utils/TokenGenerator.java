package com.petshop.utils;

import com.petshop.PetshopApplication;
import com.petshop.entity.Role;
import com.petshop.entity.User;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import java.util.List;
import java.util.stream.Collectors;

public class TokenGenerator {
    public static void main(String[] args) {
        // 1. 加载 Spring 上下文
        ApplicationContext context = SpringApplication.run(PetshopApplication.class, args);
        JwtUtils jwtUtils = context.getBean(JwtUtils.class);

        // 2. 生成管理员 Token
        User adminUser = new User();
        adminUser.setUsername("ADMIN");
        adminUser.setId(1);
        adminUser.setRoles(List.of(new Role("ADMIN", "管理员"))); // 角色对象列表

        // ✅ 关键：从 User 对象中提取 userId 和 roles
        Integer adminUserId = adminUser.getId();
        List<String> adminRoles = adminUser.getRoles().stream()
                .map(Role::getRoleCode) // 提取角色代码（如 "ADMIN"）
                .collect(Collectors.toList());

        // ✅ 调用新方法生成 Token
        String adminToken = jwtUtils.generateToken(adminUserId, adminRoles);
        System.out.println("管理员 Token: " + adminToken);

        // 3. 生成普通用户 Token
        User normalUser = new User();
        normalUser.setUsername("user");
        normalUser.setId(2);
        normalUser.setRoles(List.of(new Role("USER", "普通用户"))); // 角色对象列表

        // ✅ 提取 userId 和 roles
        Integer normalUserId = normalUser.getId();
        List<String> normalRoles = normalUser.getRoles().stream()
                .map(Role::getRoleCode)
                .collect(Collectors.toList());

        // ✅ 调用新方法生成 Token
        String userToken = jwtUtils.generateToken(normalUserId, normalRoles);
        System.out.println("普通用户 Token: " + userToken);
    }
}