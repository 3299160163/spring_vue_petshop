package com.petshop.controller;
//角色跟权限数据库设定好的，基本可以不用修改
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petshop.Api.ApiResult;
import com.petshop.entity.Permission;
import com.petshop.entity.RolePermission;
import com.petshop.mapper.PermissionMapper;
import com.petshop.service.RolePermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/roles/{roleCode}/permissions")
public class RolePermissionController {
    @Autowired
    private RolePermissionService rolePermissionService;

    // 分配权限接口：POST /api/roles/ADMIN/permissions?permCode=PET_READ
    @PostMapping
    public ResponseEntity<ApiResult<Void>> assignPermissionToRole(
            @PathVariable String roleCode,
            @RequestParam String permCode
    ) {
        // 调用服务层分配权限
        rolePermissionService.assignPermissionToRole(roleCode, permCode);
        // 返回成功响应（使用 ApiResult 封装）
        return ResponseEntity.ok(ApiResult.success("权限分配成功"));
    }
    //通过角色获得其全部权限
    @GetMapping
    public ResponseEntity<List<Permission>> getPermissionsByRole(
            @PathVariable String roleCode
    ) {
        List<Permission> permissions = rolePermissionService.getPermissionsByRole(roleCode);
        return ResponseEntity.ok(permissions);
    }

    // 删除权限接口：http://localhost:8080/api/roles/ADMIN/permissions/pet:delete
    @DeleteMapping("/{permCode}")
    public ResponseEntity<ApiResult<Void>> removePermissionFromRole(
            @PathVariable String roleCode,
            @PathVariable String permCode
    ) {
        rolePermissionService.removePermissionFromRole(roleCode, permCode);
        return ResponseEntity.ok(ApiResult.success("权限解除成功"));
    }

    // 修改权限（替换旧权限为新权限）
    //PATCH http://localhost:8080/api/roles/{roleCode}/permissions/{oldPermCode}?newPermCode={newPermCode}
    //http://localhost:8080/api/roles/ADMIN/permissions/pet:add?newPermCode=pet:delete
    @PatchMapping("/{oldPermCode}")
    public ResponseEntity<ApiResult<Void>> replacePermission(
            @PathVariable String roleCode,
            @PathVariable String oldPermCode,
            @RequestParam String newPermCode
    ) {
        rolePermissionService.replacePermission(roleCode, oldPermCode, newPermCode);
        return ResponseEntity.ok(ApiResult.success("权限修改成功"));
    }
}
