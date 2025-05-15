package com.petshop.entity;
//可能不用
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor    //无参
@AllArgsConstructor  //全参
@TableName("user_role")  // 确保表名正确
public class UserRole {

    @TableField("user_id")
    private Integer userId;

    @TableField("role_code")
    private String roleCode;
}