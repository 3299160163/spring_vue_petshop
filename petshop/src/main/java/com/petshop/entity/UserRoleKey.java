package com.petshop.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserRoleKey implements Serializable {
    private Integer userId;  // 对应 user_id
    private String roleCode; // 对应 role_code
}