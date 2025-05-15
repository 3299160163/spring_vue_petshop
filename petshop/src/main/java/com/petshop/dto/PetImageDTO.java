package com.petshop.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class PetImageDTO {
    @NotNull(message = "图片文件不能为空")
    private MultipartFile imageFile;
}