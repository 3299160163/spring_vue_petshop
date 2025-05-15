package com.petshop.dto;

import lombok.Data;

// UserQueryDTO.java
@Data
public class UserQueryDTO {
    private String username;
    private String role;
    private Integer page = 1;
    private Integer size = 10;
}