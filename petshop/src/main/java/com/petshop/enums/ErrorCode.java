package com.petshop.enums;

import lombok.Getter;

/**
 * 全局错误码枚举
 */
@Getter
public enum ErrorCode {
    SUCCESS(200, "操作成功"),
    USER_NOT_FOUND(404, "用户不存在"),
    OLD_PASSWORD_MISMATCH(400, "旧密码不正确"),
    INVALID_PASSWORD(400, "密码必须包含字母和数字且至少8位"),
    SYSTEM_ERROR(500, "系统内部错误"),
    SECKILL_FAILED(2001, "秒杀失败"),
    STOCK_NOT_ENOUGH(2002, "库存不足"),
    PURCHASE_LIMIT(2003, "已达购买上限"),
    ILLEGAL_REQUEST(2004, "非法请求参数");


    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

}