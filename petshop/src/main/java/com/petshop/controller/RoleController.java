package com.petshop.controller;
import com.petshop.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;


@RestController
@RequestMapping("/api/roles")
public class RoleController {
    @Autowired
    private RoleService roleService;

    // 获取角色的权限列表（如：/api/roles/ADMIN/info）
    @GetMapping("/{roleCode}/info")
    public ResponseEntity<List<String>> getRolePermissions(@PathVariable String roleCode) {
        List<String> permissions = roleService.getPermissionsByRoleCode(roleCode);
        return ResponseEntity.ok(permissions);
    }

    // 其他角色管理接口...
}