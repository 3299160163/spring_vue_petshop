package com.petshop.controller;

import com.petshop.Api.ApiResult;
import com.petshop.dto.RoleUpdateDTO;
import com.petshop.entity.UserRole;
import com.petshop.exception.BizException;
import com.petshop.exception.BusinessException;
import com.petshop.service.UserRoleService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j // 自动生成 log 变量
@Validated  // 启用参数校验
@RestController
@RequestMapping("/api/user-roles")
public class UserRoleController {
    @Autowired
    private UserRoleService userRoleService;

    // 分配角色给用户
    @PostMapping("/assign")
    public ApiResult<Boolean> assignRole(
            @RequestParam @Min(value = 1, message = "用户ID必须大于0") Integer userId,
            @RequestParam @NotBlank(message = "角色编码不能为空") String roleCode
    ) {
        boolean success = userRoleService.assignRoleToUser(userId, roleCode);
        return success ?
                ApiResult.success(true, "角色分配成功") :
                ApiResult.fail("角色已分配");
    }

    // 查询用户角色关联
    @GetMapping("/get")
    public ApiResult<UserRole> getByCompositeKey(
            @RequestParam @Min(1) Integer userId,
            @RequestParam @NotBlank String roleCode
    ) {
        UserRole userRole = userRoleService.getByCompositeKey(userId, roleCode);
        if (userRole == null) {
            return ApiResult.fail("用户角色关联不存在", 404);
        }
        return ApiResult.success(userRole);
    }

    //通过userId查看用户的全部角色身份
    @GetMapping("/by-user")
    public ApiResult<List<UserRole>> getAllRolesByUserId(
            @RequestParam @Min(1) Integer userId
    ) {
        List<UserRole> roles = userRoleService.getAllRolesByUserId(userId);
        return ApiResult.success(roles);
    }

    // 删除用户角色关联
    @DeleteMapping("/delete")
    public ApiResult<Boolean> deleteByCompositeKey(
            @RequestParam @Min(1) Integer userId,
            @RequestParam @NotBlank String roleCode
    ) {
        boolean success = userRoleService.deleteByCompositeKey(userId, roleCode);
        return success ?
                ApiResult.success(true, "删除成功") :
                ApiResult.fail("用户角色关联不存在",404);
    }

    //更换角色信息
    @PutMapping("/change-role")
    public ApiResult<Void> changeUserRole(
            @RequestParam @Min(1) Integer userId,
            @RequestParam @NotBlank String oldRoleCode,
            @RequestParam @NotBlank String newRoleCode
    ) {
        // 删除旧角色
        userRoleService.deleteByCompositeKey(userId, oldRoleCode);
        // 分配新角色
        userRoleService.assignRoleToUser(userId, newRoleCode);
        return ApiResult.success("角色更换成功");
    }

    @GetMapping("/{userId}/roles")
    public ResponseEntity<ApiResult<List<String>>> getUserRoles(@PathVariable Integer userId) {
        try {
            List<String> roles = userRoleService.getRolesByUserId(userId);
            return ResponseEntity.ok(ApiResult.success(roles,"查询成功"));
        } catch (BizException e) {
            return ResponseEntity.status(e.getCode()).body(ApiResult.fail(e.getMessage(), e.getCode()));
        }
    }


    // 更新用户角色状态(不是更换角色身份，而是（启用或禁用）
    @PutMapping("/status")
    public ResponseEntity<ApiResult<UserRole>> updateUserRoleStatus(
            @RequestParam @Min(value = 1, message = "用户ID必须大于0") Integer userId,
            @RequestParam @NotBlank(message = "角色编码不能为空") String roleCode) {

        try {
            UserRole updated = userRoleService.updateUserRoleStatus(userId, roleCode);
            return ResponseEntity.ok(ApiResult.success(updated, "更新成功"));
        } catch (BusinessException ex) {
            // 业务异常（用户不存在、角色不存在、关联不存在等）
            log.warn("业务异常: {} - {}", ex.getMessage(), ex.getStatusCode());
            return ResponseEntity.status(ex.getStatusCode())
                    .body(ApiResult.fail(ex.getMessage(), ex.getStatusCode()));
        } catch (Exception ex) {
            // 系统级异常（数据库连接失败等）
            log.error("系统错误: userId={}, roleCode={}", userId, roleCode, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResult.fail("系统错误，请联系管理员", 500));
        }
    }


}
