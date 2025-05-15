package com.petshop.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.petshop.entity.UserRole;

public class WrapperUtils {
    public static QueryWrapper<UserRole> buildCompositeKeyWrapper(Integer userId, String roleCode) {
        return new QueryWrapper<UserRole>()
            .eq("user_id", userId)
            .eq("role_code", roleCode);
    }
}