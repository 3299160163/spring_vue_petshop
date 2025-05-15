package com.petshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petshop.entity.Role;
import com.petshop.entity.User;
import com.petshop.entity.UserRole;
import com.petshop.exception.BusinessException;
import com.petshop.mapper.RoleMapper;
import com.petshop.mapper.UserMapper;
import com.petshop.mapper.UserRoleMapper;
import com.petshop.service.UserRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole>
        implements UserRoleService {
    @Autowired
    private  UserRoleMapper userRoleMapper;
    @Autowired
    private  UserMapper userMapper;
    @Autowired
    private  RoleMapper roleMapper;

//管理员使用

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignRoleToUser(Integer userId, String roleCode) {
        // 1. 校验用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户ID: " + userId + " 不存在", 404);
        }
        // 2. 校验角色是否存在
        LambdaQueryWrapper<Role> roleQueryWrapper = new LambdaQueryWrapper<>();
        roleQueryWrapper.eq(Role::getRoleCode, roleCode);
        Role role = roleMapper.selectOne(roleQueryWrapper);
        if (role == null) {
            throw new BusinessException("角色编码: " + roleCode + " 不存在", 404);
        }
        // 3. 检查是否已分配过该角色（防止重复分配）
        LambdaQueryWrapper<UserRole> userRoleQueryWrapper = new LambdaQueryWrapper<>();
        userRoleQueryWrapper.eq(UserRole::getUserId, userId)
                .eq(UserRole::getRoleCode, roleCode);
        if (this.baseMapper.exists(userRoleQueryWrapper)) {
            return false; // 角色已分配
        }
        // 4. 创建用户角色关联实体（直接设置字段）
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleCode(roleCode);
        // 5. 插入数据库
        return this.baseMapper.insert(userRole) > 0;
    }
//根据userId，roleCode查询特定用户角色关系
    @Override
    @Transactional
    public UserRole getByCompositeKey(Integer userId, String roleCode) {
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, userId)
                .eq(UserRole::getRoleCode, roleCode);

        // 如果允许返回多条中的第一条，设置 throwEx=false
        return getOne(wrapper, false);
    }
    //
    @Override
    public List<String> getRolesByUserId(Integer userId) {
        // 使用 LambdaQueryWrapper 构建查询条件
        LambdaQueryWrapper<UserRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserRole::getUserId, userId); // WHERE user_id = #{userId}
        // 查询 UserRole 列表
        List<UserRole> userRoles = userRoleMapper.selectList(queryWrapper);
        // 提取 role_code
        log.info("查询到的角色记录: {}", userRoles); // 关键日志
        return userRoles.stream()
                .map(UserRole::getRoleCode)
                .collect(Collectors.toList());
    }

    //数组
    @Override
    public List<UserRole> getAllRolesByUserId(Integer userId) { // 严格匹配接口的返回类型
        LambdaQueryWrapper<UserRole> query = new LambdaQueryWrapper<>();
        query.eq(UserRole::getUserId, userId);
        return userRoleMapper.selectList(query); // MyBatis-Plus 的 selectList 返回 List<T>，此处 T 是 UserRole
    }

    // UserRoleService.java
    @Override
    @Transactional
    public UserRole updateUserRoleStatus(Integer userId, String roleCode) {
        // 1. 校验用户存在性
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在: " + userId, 404);
        }
        // 2. 校验角色存在性
        LambdaQueryWrapper<Role> roleQuery = new LambdaQueryWrapper<>();
        roleQuery.eq(Role::getRoleCode, roleCode);
        if (roleMapper.selectCount(roleQuery) == 0) {
            throw new BusinessException("角色不存在: " + roleCode, 404);
        }
        // 3. 直接构建 UpdateWrapper 更新记录（仅保留必要字段）
        UpdateWrapper<UserRole> updateWrapper = new UpdateWrapper<>();
        updateWrapper
                .eq("user_id", userId)
                .eq("role_code", roleCode)
                .setSql("user_id = user_id"); // 合法语法：字段赋值为自身
        // 4. 执行更新并检查影响行数
        int affectedRows = userRoleMapper.update(null, updateWrapper);
        if (affectedRows == 0) {
            throw new BusinessException("用户角色关联不存在", 404);
        }

        // 5. 返回更新后的记录（可选）
        LambdaQueryWrapper<UserRole> query = new LambdaQueryWrapper<>();
        query.eq(UserRole::getUserId, userId)
                .eq(UserRole::getRoleCode, roleCode);
        return userRoleMapper.selectOne(query);
    }

//根据联合主键删除用户角色关系
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByCompositeKey(Integer userId, String roleCode) {
        // 构建联合主键查询条件
        QueryWrapper<UserRole> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .eq("role_code", roleCode);

        // 执行删除并返回结果
        int affectedRows = userRoleMapper.delete(wrapper);
        return affectedRows > 0;
    }


    @Override
    public List<String> getUserRoles(Integer userId) {
        return userRoleMapper.selectRoleCodesByUserId(userId);
    }

    @Override
    @Transactional
    public void saveUserRoles(Integer userId, List<String> roleCodes) {
        roleCodes.forEach(code ->
                userRoleMapper.insert(new UserRole(userId, code))
        );
    }

    @Override
    @Transactional
    public void updateUserRoles(Integer userId, List<String> roleCodes) {
        userRoleMapper.deleteByUserId(userId);
        saveUserRoles(userId, roleCodes);
    }
}