// src/main/java/com/petshop/dto/LoginRequest.java
package com.petshop.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}