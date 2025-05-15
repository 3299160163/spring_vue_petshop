package com.petshop.service.impl;

import com.petshop.entity.Permission;
import com.petshop.mapper.PermissionMapper;
import com.petshop.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

// src/main/java/com/petshop/service/impl/PermissionServiceImpl.java
@Service
public class PermissionServiceImpl implements PermissionService {
    @Autowired
    private PermissionMapper permissionMapper;  // 注入 Mapper

    @Override
    public List<String> getPermissionsByUserId(Integer userId) {
        return permissionMapper.getPermissionsByUserId(userId);
    }

    @Override
    public List<Permission> getAllPermissions() {
        return permissionMapper.selectList(null);  // 调用 BaseMapper 方法
    }

    @Override
    public void addPermission(Permission permission) {
        permissionMapper.insert(permission);
    }
}