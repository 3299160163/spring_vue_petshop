package com.petshop.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor // 添加全参构造函数
@TableName("role")
public class Role {

    @TableId(value = "role_code")
    private String roleCode;

    @TableField("role_name")
    private String roleName;
}