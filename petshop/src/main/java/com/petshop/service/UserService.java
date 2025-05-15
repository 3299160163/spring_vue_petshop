
// src/main/java/com/petshop/service/UserService.java
package com.petshop.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.petshop.common.PageResult;
import com.petshop.dto.UserDTO;
import com.petshop.dto.UserQueryDTO;
import com.petshop.dto.UserUpdateRequest;
import com.petshop.entity.User;
import com.petshop.exception.BizException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserService extends IService<User> {
    @Transactional
    PageResult<UserDTO> getUsers(UserQueryDTO queryDTO);

        UserDTO createUser(UserDTO userDTO);

        UserDTO updateUser(UserDTO userDTO);

    boolean deleteUser(Integer id);  // 统一返回 boolean

        void updateRoles(Integer  userId, List<String> roleCodes);


    boolean checkUsernameExists(String username);

    // 自定义方法：检查权限
    boolean hasPermission(Integer userId, String permCode);

//登录相关
    User verifyUser(String username, String rawPassword);
//注册相关
    void registerUser(User user) throws BizException;

    //更新密码
    boolean updatePassword(Integer userId, String oldPassword, String newPassword);


    boolean updateProfile(Integer userId, UserUpdateRequest request);


    void updateUserAvatar(Integer userId, String storedPath);
}