package com.petshop.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum PetStatus {
    AVAILABLE("AVAILABLE"),
    SOLD("SOLD");

    @EnumValue
    private final String value;

    PetStatus(String value) {
        this.value = value;
    }

}