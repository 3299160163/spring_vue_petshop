package com.petshop.controller;

import com.petshop.entity.Permission;
import com.petshop.mapper.RoleMapper;
import com.petshop.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private PermissionService permissionService;

    @GetMapping
    public ResponseEntity<List<Permission>> getAllPermissions() {
        return ResponseEntity.ok(permissionService.getAllPermissions());
    }

    @PostMapping
    public ResponseEntity<Void> addPermission(@RequestBody Permission permission) {
        permissionService.addPermission(permission);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<String>> getPermissionsByUserId(@PathVariable Integer userId) {
        List<String> permissions = permissionService.getPermissionsByUserId(userId);
        if (permissions.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(permissions);
    }
}