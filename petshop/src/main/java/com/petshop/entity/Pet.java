package com.petshop.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("pet") // MyBatis-Plus 注解，指定表名
public class Pet {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @NotBlank(message = "名称不能为空")
    private String name;

    @NotBlank(message = "分类不能为空")
    @TableField("category")
    private String category;

    @DecimalMin(value = "0.0", message = "价格必须大于0")
    private BigDecimal price;

    private String status;

    // 新增字段：性别（0-雌性，1-雄性）
    @TableField("gender")
    private Integer gender;

    // 新增字段：年龄（单位：年）
    @TableField("age")
    private Integer age;

    @NotNull(message = "卖家ID不能为空")
    @TableField("seller_id")
    private Integer sellerId;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("cover_image")
    private String coverImage;
}