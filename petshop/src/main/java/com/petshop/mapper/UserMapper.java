
// src/main/java/com/petshop/mapper/UserMapper.java
package com.petshop.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.petshop.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 继承BaseMapper后已包含基本CRUD方法
    // 如需自定义SQL可在此添加方法
    User findUserWithRolesByUsername(@Param("username") String username);


    default User selectByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name", username);
        return selectOne(queryWrapper);
    }


    boolean existsByUsername(@Param("username") String username);

    IPage<User> selectUsersPage(IPage<User> page, String username, String role);

    String findUsernameById(@Param("userId") Integer userId); // ✅ id 必须与 XML 中一致
}