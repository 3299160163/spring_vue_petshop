package com.petshop.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petshop.entity.UserRole;

import java.util.List;

public interface UserRoleMapper extends BaseMapper<UserRole> {


    void deleteByUserId(Integer userId);

    List<String> selectRoleCodesByUserId(Integer userId);
}