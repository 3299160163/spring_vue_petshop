// src/main/java/com/petshop/enums/OrderStatus.java
package com.petshop.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.Getter;

@Getter
public enum OrderStatus implements IEnum<Integer> {
    PENDING(1, "待处理"),
    PROCESSING(2, "处理中"),
    COMPLETED(3, "已完成"),
    CANCELLED(4, "已取消");

    @EnumValue  // 标记数据库存储的字段
    private final int code;
    private final String description;

    OrderStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public Integer getValue() {
        return this.code;
    }
}