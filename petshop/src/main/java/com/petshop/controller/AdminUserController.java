package com.petshop.controller;

import com.petshop.Api.ApiResult;
import com.petshop.common.PageResult;
import com.petshop.dto.RoleUpdateDTO;
import com.petshop.dto.UserDTO;
import com.petshop.dto.UserQueryDTO;
import com.petshop.exception.BusinessException;
import com.petshop.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


// AdminUserController.java
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    private final UserService userService;

    @GetMapping
    public ApiResult<PageResult<UserDTO>> getUsers(@Valid UserQueryDTO queryDTO) {
        return ApiResult.success(userService.getUsers(queryDTO));
    }

    @PostMapping
    public ApiResult<UserDTO> createUser(@Valid @RequestBody UserDTO userDTO) {
        return ApiResult.success(userService.createUser(userDTO));
    }

    @PutMapping("/{id}")
    public ApiResult<UserDTO> updateUser(@PathVariable Integer  id,
                                         @Validated(UserDTO.Update.class)
                                         @Valid @RequestBody UserDTO userDTO) {
        userDTO.setId(id);
        return ApiResult.success(userService.updateUser(userDTO));
    }

    @DeleteMapping("/{id}")
    public ApiResult<Boolean> deleteUser(@PathVariable Integer id) {
        try {
            boolean success = userService.deleteUser(id);
            if (success) {
                return ApiResult.success(true, "用户删除成功");
            } else {
                // 用户不存在时返回 404 Not Found
                return ApiResult.fail("用户不存在",404);
            }
        } catch (BusinessException e) {
            // 捕获业务异常（如数据冲突）
            return ApiResult.fail( e.getMessage(),e.getStatusCode());
        } catch (Exception e) {
            // 其他未知错误
            return ApiResult.fail("服务器内部错误",500);
        }
    }




    // 修改接口      //多余的
    @PutMapping("/{userId}/roles")
    public ApiResult<Void> updateUserRoles(
            @PathVariable Integer userId,
            @RequestBody RoleUpdateDTO request  // 接收对象
    ) {
        userService.updateRoles(userId, request.getRoles());
        return ApiResult.success();
    }
}