// src/main/java/com/petshop/service/impl/UserServiceImpl.java
package com.petshop.service.impl;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petshop.common.PageResult;
import com.petshop.dto.UserDTO;
import com.petshop.dto.UserQueryDTO;
import com.petshop.dto.UserUpdateRequest;
import com.petshop.entity.User;
import com.petshop.entity.UserRole;
import com.petshop.exception.BizException;
import com.petshop.exception.BusinessException;
import com.petshop.mapper.UserMapper;
import com.petshop.mapper.UserRoleMapper;
import com.petshop.service.RoleService;
import com.petshop.service.UserRoleService;
import com.petshop.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RoleService roleService; // 注入 RoleService
    @Autowired
    private UserRoleService userRoleService;
    @Autowired
    private UserRoleMapper userRoleMapper; // ✅ 正确注入

    //判断user存在
    public boolean checkUsernameExists(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name", username);
        return this.baseMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public boolean hasPermission(Integer userId, String permCode) {
        // 查询用户权限逻辑
        return roleService.getPermissionsByUserId(userId).contains(permCode);
    }

    @Override
    public User verifyUser(String username, String rawPassword) {
        log.debug("开始验证用户: {}", username);
        // 1. 查询用户
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            log.warn("用户不存在: {}", username);
            return null; // 用户不存在时返回 null
        }
        // 2. 记录安全日志（避免记录敏感信息）
        log.debug("数据库中的密码哈希: {}", user.getPassword());
        // 3. 验证密码
        boolean isValid = BCrypt.checkpw(rawPassword, user.getPassword());
        log.debug("BCrypt验证结果: {}", isValid);
        return isValid ? user : null; // 验证通过返回用户对象，否则返回 null
    }

    @Override
    public void registerUser(User user) throws BizException {
        // 1. 校验用户名唯一性
        if (userMapper.existsByUsername(user.getUsername())) {
            throw new BizException("用户名已存在", 409);
        }
        // 2. 生成BCrypt哈希密码
        String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(12));
        user.setPassword(hashedPassword);
        user.setCreateTime(LocalDateTime.now());
        // 3. 插入用户
        if (userMapper.insert(user) <= 0) {
            throw new BizException("注册失败", 500);
        }
        // 4. 分配默认角色（例如：BUYER）
        Integer userId = user.getId(); // 假设用户表主键为自增ID
        if (userId == null) {
            throw new BizException("获取用户ID失败", 500);
        }
        // 将用户与默认角色关联（假设存在 UserRoleMapper 和 user_role 表）
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleCode("BUYER");
        if (userRoleMapper.insert(userRole) <= 0) {
            throw new BizException("角色分配失败", 500);
        }
    }

    @Override
    public boolean updatePassword(Integer userId, String oldPassword, String newPassword) {
        // 1. 查询用户
        User user = userMapper.selectById(userId);
        log.info("从数据库读取的用户数据: {}", user.toString()); // 确保包含密码字段

        boolean isMatch = BCrypt.checkpw(oldPassword, user.getPassword());
        log.info("BCrypt验证结果: {}", isMatch);

        if (!isMatch) {
            throw new RuntimeException("旧密码错误");
        }
        // 3. 生成新密码哈希（BCrypt 自动加盐）
        String hashedNewPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        user.setPassword(hashedNewPassword);
        // 4. 更新数据库
        return userMapper.updateById(user) > 0;
    }
    //修改个人信息
    @Override
    public boolean updateProfile(Integer userId, UserUpdateRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("失败在", 404);
        }
        // 仅更新允许修改的字段
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        return userMapper.updateById(user) > 0;
    }


    @Override
    @Transactional(readOnly = true)
    public PageResult<UserDTO> getUsers(UserQueryDTO queryDTO) {
        // 1. 创建分页参数
        IPage<User> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());

        // 2. 执行原生分页查询
        IPage<User> userPage = userMapper.selectUsersPage(
                page,
                queryDTO.getUsername(),
                queryDTO.getRole()
        );
        // 3. 转换为DTO分页
        IPage<UserDTO> dtoPage = userPage.convert(user -> {
            UserDTO dto = new UserDTO();
            BeanUtils.copyProperties(user, dto);
            dto.setRoles(userRoleService.getUserRoles(user.getId()));
            return dto;
        });
        // 4. 转换为统一分页格式
        return PageResult.from(dtoPage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserDTO createUser(UserDTO userDTO) {
        // 1. 校验用户名唯一性
        if (userMapper.existsByUsername(userDTO.getUsername())) {
            throw new BusinessException("用户名已存在", HttpStatus.CONFLICT.value()); // 409 Conflict
        }// 2. 校验密码非空
        if (StringUtils.isBlank(userDTO.getPassword())) {
            throw new BusinessException("密码不能为空", HttpStatus.BAD_REQUEST.value()); // 400 Bad Request
        }// 3. DTO 转 Entity，并加密密码
        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        // 直接内联密码加密逻辑（与注册方法一致）
        String hashedPassword = BCrypt.hashpw(userDTO.getPassword(), BCrypt.gensalt(12));
        user.setPassword(hashedPassword);
        user.setCreateTime(LocalDateTime.now());// 4. 插入用户
        try {
            if (userMapper.insert(user) <= 0) {
                throw new BusinessException("用户创建失败", HttpStatus.INTERNAL_SERVER_ERROR.value()); // 500
            }
        } catch (DuplicateKeyException e) { // 捕获唯一键冲突（如并发注册）
            throw new BusinessException("用户名已存在", HttpStatus.CONFLICT.value());
        }
//      5. 分配角色
        try {
            userRoleService.saveUserRoles(user.getId(), userDTO.getRoles());
        } catch (Exception e) {
            throw new BusinessException("角色分配失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
//         6. 返回完整用户信息（包含角色）
        return getFullUserDTO(user);
    }


    @Override
    @Transactional
    public void updateRoles(Integer userId, List<String> roleCodes) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在", 401);
        }

        userRoleService.updateUserRoles(userId, roleCodes);
    }

    // 返回基础用户信息（不包含角色）
    private UserDTO getFullUserDTO(User user) {
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto);
        dto.setRoles(userRoleService.getUserRoles(user.getId()));
        return dto;
    }

    @Override
    @Transactional
    public UserDTO updateUser(UserDTO userDTO) {
        // 1. 校验用户存在
        User user = userMapper.selectById(userDTO.getId());
        if (user == null) {
            throw new BusinessException("用户不存在", 401);
        }
        // 仅更新允许修改的字段（排除密码）
        BeanUtils.copyProperties(userDTO, user, "id", "password", "createTime");
        userMapper.updateById(user);

        // 3. 更新角色（如果DTO包含角色信息）
        if (userDTO.getRoles() != null) {
            userRoleService.updateUserRoles(user.getId(), userDTO.getRoles());
        }

        // 4. 返回更新后的完整信息
        return getFullUserDTO(user);
    }

    // UserServiceImpl.java
    //管理员调用
    @Override
    @Transactional
    public boolean deleteUser(Integer id) {
        // 1. 检查用户是否存在
        User user = userMapper.selectById(id);
        if (user == null) {
            log.warn("删除用户失败: 用户不存在, ID={}", id);
            return false; // 用户不存在
        }
        try {
            // 2. 执行删除
            userMapper.deleteById(id);
            log.info("用户删除成功: ID={}", id);
            return true;
        } catch (DataIntegrityViolationException e) {
            // 3. 处理外键约束等数据完整性错误
            log.error("删除用户失败: 存在关联数据, ID={}", id, e);
            throw new BusinessException("存在关联数据，无法删除用户", 409); // 使用 409 Conflict 状态码
        } catch (Exception e) {
            // 4. 其他未知错误
            log.error("删除用户失败: 未知错误, ID={}", id, e);
            throw new BusinessException("用户删除失败: " + e.getMessage(), 500);
        }
    }

    @Override
    @Transactional
    public void updateUserAvatar(Integer userId, String avatarPath) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在", 404);
        }
        user.setAvatarUrl(avatarPath);
        userMapper.updateById(user);
    }
}





