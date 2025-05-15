package com.petshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petshop.entity.RolePermission;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface RolePermissionMapper extends BaseMapper<RolePermission> {
    /**
     * 根据复合主键查询记录
     * @param roleCode 角色编码
     * @param permCode 权限编码
     * @return 角色-权限关联记录
     */
    RolePermission selectByCompositeKey(
            @Param("roleCode") String roleCode,
            @Param("permCode") String permCode
    );
    /**
     * 检查权限是否已分配
     * @param roleCode 角色编码
     * @param permCode 权限编码
     * @return 是否存在记录
     */
    boolean exists(@Param("roleCode") String roleCode, @Param("permCode") String permCode);

    /**
     * 根据角色编码查询权限列表
     * @param roleCode 角色编码
     * @return 权限编码列表
     */
    List<String> selectPermissionsByRoleCode(@Param("roleCode") String roleCode);

    /**
     * 删除角色-权限关联
     * @param roleCode 角色编码
     * @param permCode 权限编码
     */
    void deleteByRoleAndPerm(@Param("roleCode") String roleCode, @Param("permCode") String permCode);
}