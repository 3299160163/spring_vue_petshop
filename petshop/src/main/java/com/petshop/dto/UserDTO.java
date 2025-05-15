package com.petshop.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

// UserDTO.java
@Data
public class UserDTO {
    @NotNull(groups = Update.class)
    private Integer id;

    @NotBlank(message = "用户名不能为空", groups = Create.class)
    @Size(min = 3, max = 20, message = "用户名长度3-20个字符", groups = {Create.class, Update.class})
    private String username;

    // 仅在创建时校验密码必填
    @NotBlank(message = "密码不能为空", groups = Create.class)
    private String password;

    @Email(message = "邮箱格式不正确", groups = {Create.class, Update.class})
    private String email;

    @NotEmpty(message = "至少分配一个角色", groups = Create.class)
    private List<String> roles;

    // 验证分组
    public interface Create {}
    public interface Update {}
}