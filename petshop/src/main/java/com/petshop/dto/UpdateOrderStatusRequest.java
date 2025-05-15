package com.petshop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {
    @NotBlank(message = "action不能为空")
    @Pattern(
            regexp = "(?i)confirm|cancel|complete", // (?i) 表示不区分大小写
            message = "action参数不合法，允许值: confirm/cancel/complete（不区分大小写）"
    )
    private String action;
}