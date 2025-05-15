package com.petshop.entity;
//可能不用
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// RolePermission.java
@Data
@TableName("role_permission")
public class RolePermission {
    @TableField("role_code")
    private String roleCode;

    @TableField("perm_code")
    private String permCode;
}