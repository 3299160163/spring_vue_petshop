package com.petshop.dto;
import lombok.Data;
import java.util.List;
@Data
// 定义 DTO
public class RoleUpdateDTO {
    private List<String> roles;
    // getters/setters
}