package com.petshop.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

// 配套状态枚举
public enum SeckillOrderStatus {
    CREATED(0, "订单已创建"),  // 新增状态
    PENDING(1, "待支付"),     // 原PENDING调整为1
    PAID(2, "已支付"),        // 原PAID调整为2
    CANCELLED(3, "已取消");   // 原CANCELLED调整为3

    @EnumValue // MyBatis-Plus注解，存储到数据库的值
    private final int code;
    private final String desc;

    SeckillOrderStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}