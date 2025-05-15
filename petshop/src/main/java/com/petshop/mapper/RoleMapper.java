package com.petshop.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petshop.entity.Order;
import com.petshop.entity.Role;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RoleMapper extends BaseMapper<Role> {
    // 根据用户ID查询权限列表
    List<String> selectPermissionsByUserId(@Param("userId") Integer userId);
}