package com.petshop.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PetResponseDTO {
    private Integer id;
    private String name;
    private String category;
    private BigDecimal price;
    private String status;
    private String gender; // 转换为文字描述（如"雌性"/"雄性"）
    private Integer age;
    private LocalDateTime createTime;
    private String coverImageUrl; // 完整的图片访问URL
}