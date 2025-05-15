package com.petshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petshop.entity.Permission;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PermissionMapper extends BaseMapper<Permission> {
    // 根据用户ID查询所有权限编码
    List<String> getPermissionsByUserId(@Param("userId") Integer userId);
    // 根据权限代码列表查询权限
    List<Permission> selectByPermCodes(@Param("permCodes") List<String> permCodes);
}