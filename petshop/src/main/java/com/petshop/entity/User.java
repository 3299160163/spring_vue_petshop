package com.petshop.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("user_name")
    private String username;

    @TableField("password")
//    @JsonIgnore
    private String password;

    @TableField("email")
    private String email;

    @TableField("phone")
    private String phone;


    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField("avatar_url")
    private String avatarUrl; // 字段名为 avatarUrl

    @TableField(exist = false)  // 非数据库字段，通过关联查询获取角色
    private List<Role> roles = new ArrayList<>(); // 关键：初始化为空列表


}