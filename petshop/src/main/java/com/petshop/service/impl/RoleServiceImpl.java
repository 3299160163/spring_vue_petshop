package com.petshop.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petshop.entity.Role;
import com.petshop.mapper.RoleMapper;
import com.petshop.mapper.RolePermissionMapper;
import com.petshop.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;


@Service
public class RoleServiceImpl 
    extends ServiceImpl<RoleMapper, Role>  // 继承 MyBatis-Plus 的通用实现
    implements RoleService {               // 实现自定义接口

    @Autowired
    private RolePermissionMapper rolePermissionMapper;  // 添加此行


    @Override
    public List<String> getPermissionsByUserId(Integer userId) {
        // 查询用户所有角色的权限（需实现 SQL）
        return baseMapper.selectPermissionsByUserId(userId);

    }

    @Override
    public List<String> getPermissionsByRoleCode(String roleCode) {
        return rolePermissionMapper.selectPermissionsByRoleCode(roleCode);
}}