package com.petshop.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRequest {
    @NotNull(message = "宠物ID不能为空")
    @Min(value = 1, message = "宠物ID必须大于0")
    private Integer petId;

    @NotBlank(message = "地址不能为空")
    @Size(max = 200, message = "地址长度不能超过200字符")
    private String address;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误")
    private String phone;
}