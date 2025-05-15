package com.petshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.petshop.entity.Role;

import java.util.List;

public interface RoleService extends IService<Role> {

    // 通过用户ID查询所有权限（复数方法名，单个参数）
    List<String> getPermissionsByUserId(Integer userId);

    /**
     * 根据角色编码查询权限列表
     * @param roleCode 角色编码
     * @return 权限编码列表
     */
    List<String> getPermissionsByRoleCode(String roleCode);
}