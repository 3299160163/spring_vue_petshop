// 文件路径：src/main/java/com/petshop/service/UserRoleService.java
package com.petshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.petshop.dto.UserDTO;
import com.petshop.entity.UserRole;

import java.util.List;

public interface UserRoleService extends IService<UserRole> {
    // 定义分配角色给用户
    boolean assignRoleToUser(Integer userId, String roleCode);
    // 根据复合主键（用户ID + 角色编码）查询用户角色关联
    UserRole getByCompositeKey(Integer userId, String roleCode);


    //根据用户ID查询所有角色关联
    List<UserRole> getAllRolesByUserId(Integer userId);
    //更新
    UserRole updateUserRoleStatus(Integer userId, String roleCode);
    // 添加删除方法
    boolean deleteByCompositeKey(Integer userId, String roleCode);


    List<String> getRolesByUserId(Integer userId);

    List<String> getUserRoles(Integer userId);

    void saveUserRoles(Integer userId, List<String> roleCodes);

    void updateUserRoles(Integer userId, List<String> roleCodes);


}