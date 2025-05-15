package com.petshop.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petshop.entity.Permission;
import com.petshop.entity.RolePermission;
import com.petshop.exception.BusinessException;
import com.petshop.mapper.PermissionMapper;
import com.petshop.mapper.RoleMapper;
import com.petshop.mapper.RolePermissionMapper;
import com.petshop.service.RolePermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RolePermissionServiceImpl implements RolePermissionService {
    @Autowired
    private RolePermissionMapper rolePermissionMapper;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private PermissionMapper permissionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissionToRole(String roleCode, String permCode) {
        // 1. 校验角色是否存在
        if (roleMapper.selectById(roleCode) == null) {
            throw new BusinessException("角色不存在: " + roleCode, 404);
        }

        // 2. 校验权限是否存在
        if (permissionMapper.selectById(permCode) == null) {
            throw new BusinessException("权限不存在: " + permCode, 404);
        }

        // 3. 检查是否已绑定
        LambdaQueryWrapper<RolePermission> query = new LambdaQueryWrapper<>();
        query.eq(RolePermission::getRoleCode, roleCode)
                .eq(RolePermission::getPermCode, permCode);
        if (rolePermissionMapper.selectCount(query) > 0) {
            throw new BusinessException("权限已分配", 400);
        }

        // 4. 绑定权限
        RolePermission rolePermission = new RolePermission();
        rolePermission.setRoleCode(roleCode);
        rolePermission.setPermCode(permCode);
        rolePermissionMapper.insert(rolePermission);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Permission> getPermissionsByRole(String roleCode) {
        // 1. 查询角色-权限关联表，获取权限编码列表
        List<RolePermission> rolePermissions = rolePermissionMapper.selectList(
                new LambdaQueryWrapper<RolePermission>()
                        .eq(RolePermission::getRoleCode, roleCode)
        );

        // 2. 提取权限编码
        List<String> permCodes = rolePermissions.stream()
                .map(RolePermission::getPermCode)
                .collect(Collectors.toList());

        // 3. 批量查询权限详情
        if (permCodes.isEmpty()) {
            return Collections.emptyList();
        }
        return permissionMapper.selectBatchIds(permCodes);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removePermissionFromRole(String roleCode, String permCode) {
        // 1. 校验角色和权限是否存在（可选，根据需求）
        // if (roleMapper.selectById(roleCode) == null) { ... }
        // if (permissionMapper.selectById(permCode) == null) { ... }

        // 2. 删除关联记录
        LambdaQueryWrapper<RolePermission> query = new LambdaQueryWrapper<>();
        query.eq(RolePermission::getRoleCode, roleCode)
                .eq(RolePermission::getPermCode, permCode);

        int deleted = rolePermissionMapper.delete(query);

        // 3. 检查是否成功删除
        if (deleted == 0) {
            throw new BusinessException("权限未分配", 400);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 事务保证原子性
    public void replacePermission(String roleCode, String oldPermCode, String newPermCode) {
        // 1. 删除旧权限
        LambdaQueryWrapper<RolePermission> deleteQuery = new LambdaQueryWrapper<>();
        deleteQuery.eq(RolePermission::getRoleCode, roleCode)
                .eq(RolePermission::getPermCode, oldPermCode);
        int deleted = rolePermissionMapper.delete(deleteQuery);
        if (deleted == 0) {
            throw new BusinessException("旧权限未分配", 400);
        }

        // 2. 添加新权限
        RolePermission newRelation = new RolePermission();
        newRelation.setRoleCode(roleCode);
        newRelation.setPermCode(newPermCode);
        rolePermissionMapper.insert(newRelation);
    }
}