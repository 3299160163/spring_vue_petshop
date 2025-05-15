package com.petshop.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import java.math.BigDecimal;

@Data
public class PetCreateDTO {
    @NotBlank(message = "宠物名称不能为空")
    @Size(max = 50, message = "名称最多50个字符")
    private String name;

    @NotBlank(message = "分类不能为空")
    @Size(max = 20, message = "分类最多20个字符")
    private String category;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.0", inclusive = false, message = "价格必须大于0")
    private BigDecimal price;

    @NotNull(message = "性别不能为空")
    @Range(min = 0, max = 1, message = "性别必须为0（雌性）或1（雄性）")
    private Integer gender;

    @NotNull(message = "年龄不能为空")
    @Min(value = 0, message = "年龄不能小于0")
    @Max(value = 30, message = "年龄不能超过30")
    private Integer age;

    // 不包含 sellerId（从当前用户自动获取）
    // 不包含 coverImage（通过单独接口上传）
}