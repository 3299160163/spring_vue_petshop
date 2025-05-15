package com.petshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

// Permission.java（权限实体）
@Data
@TableName("permission")
public class Permission {
    @TableId(value = "perm_code", type = IdType.INPUT) // 主键为 perm_code
    private String permCode;

    @TableField("perm_name")
    private String permName;
}