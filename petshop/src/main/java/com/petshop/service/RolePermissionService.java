package com.petshop.service;

import com.petshop.entity.Permission;

import java.util.List;

public interface RolePermissionService {
    /**
     * 为角色分配权限
     * @param roleCode 角色编码
     * @param permCode 权限编码
     */
    void assignPermissionToRole(String roleCode, String permCode);

    /**
     * 根据角色编码获取权限列表
     */
    List<Permission> getPermissionsByRole(String roleCode);
    /**
     * 移除角色与权限的关联
     * @param roleCode 角色编码
     * @param permCode 权限编码
     */
    void removePermissionFromRole(String roleCode, String permCode);

    /**
     * 替换角色的某个权限
     * @param roleCode     角色编码
     * @param oldPermCode 旧权限编码
     * @param newPermCode 新权限编码
     */
    void replacePermission(String roleCode, String oldPermCode, String newPermCode);
}

