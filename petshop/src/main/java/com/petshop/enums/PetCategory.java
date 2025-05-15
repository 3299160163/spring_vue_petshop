package com.petshop.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum PetCategory {
    DOG("DOG"),
    CAT("CAT"),
    BIRD("BIRD");

    @EnumValue  // 标记此字段对应数据库实际存储的值
    private final String value;

    PetCategory(String value) {
        this.value = value;
    }

}