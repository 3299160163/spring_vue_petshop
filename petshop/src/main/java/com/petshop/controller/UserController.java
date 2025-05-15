// src/main/java/com/petshop/controller/UserController.java
package com.petshop.controller;
import com.petshop.Api.ApiResult;
import com.petshop.dto.UpdatePasswordRequest;
import com.petshop.dto.UserUpdateRequest;
import com.petshop.entity.User;
import com.petshop.exception.BusinessException;
import com.petshop.service.UserService;
import com.petshop.utils.FileStorageUtil;
import io.jsonwebtoken.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

// UserController.java
@RestController
@RequestMapping("/api/users") // 普通用户接口路径前缀
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private FileStorageUtil fileStorageUtil; // 复用同一个工具类

    // 获取个人资料
    @GetMapping("/profile")
    public ApiResult<User> getProfile(@RequestAttribute("userId") Integer userId) {
        User user = userService.getById(userId);
        if (user == null) {
            return ApiResult.fail("用户不存在", 404);
        }
        user.setPassword(null); // 脱敏处理
        return ApiResult.success(user);
    }
    // 更新用户信息接口
    @PutMapping("/profile")
    public ApiResult<Boolean> updateProfile(
            @RequestBody @Valid UserUpdateRequest request,
            @RequestAttribute("userId") Integer userId) {
        boolean success = userService.updateProfile(userId, request);
        return success ?
                ApiResult.success(true, "信息更新成功") :
                ApiResult.fail("信息更新失败", 500);
    }


    @PostMapping(value = "/upload-avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResult<String> uploadAvatar(
            @RequestPart("avatar") MultipartFile avatarFile,
            HttpServletRequest request) {

        try {
            // 1. 获取用户ID
            Integer userId = (Integer) request.getAttribute("userId");
            if (userId == null) {
                throw new BusinessException("用户未登录", 401); // ✅ 统一抛出异常
            }

            // 2. 上传文件并获取路径
            String storedPath = fileStorageUtil.storeFile(avatarFile);

            // 3. 更新用户头像路径
            userService.updateUserAvatar(userId, storedPath);


        } catch (IOException | java.io.IOException e) {
            throw new BusinessException("头像上传失败: " + e.getMessage(), 500); // ✅ 统一抛出异常
        }
        return null;
    }

    // 修改密码接口
    @PutMapping("/password")
    public ApiResult<Boolean> updatePassword(
            @RequestBody @Valid UpdatePasswordRequest request,  //@Valid 启用参数校验
            @RequestAttribute("userId") Integer userId) {
        // 业务逻辑：验证旧密码、更新为新密码
        boolean success = userService.updatePassword(userId, request.getOldPassword(), request.getNewPassword());
        return success ? ApiResult.success(true) : ApiResult.fail("密码修改失败", 400);
    }
}
