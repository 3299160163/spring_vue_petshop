package com.petshop.service;

import com.petshop.entity.Permission;

import java.util.List;

// src/main/java/com/petshop/service/PermissionService.java
public interface PermissionService {
    List<String> getPermissionsByUserId(Integer userId);
    List<Permission> getAllPermissions();
    void addPermission(Permission permission);
}