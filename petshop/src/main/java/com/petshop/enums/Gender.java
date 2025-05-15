package com.petshop.enums;

import lombok.Getter;

@Getter
public enum Gender {
    FEMALE(0, "雌性"),
    MALE(1, "雄性");

    private final int code;
    private final String desc;

    // 定义私有构造方法，接收 code 和 desc
    private Gender(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // 根据 code 获取对应的枚举实例
    public static Gender getByCode(int code) {
        for (Gender gender : values()) {
            if (gender.code == code) {
                return gender;
            }
        }
        throw new IllegalArgumentException("无效的性别编码: " + code);
    }
}