package com.petshop.Api;

import com.petshop.enums.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResult<T> {
    private Integer code; // code 应为 Integer 类型
    private String msg;
    private T data;

    // 成功响应（无消息）
    public static  ApiResult <Void> success() {
        return new ApiResult<>(200, "成功", null);
    }

    public static ApiResult<Void> success(String message) {
        return new ApiResult<>(200, message, null);
    }

    // 成功响应（带数据）
    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(200, "成功", data);
    }

    // 失败响应（自定义消息和状态码）
    public static <T> ApiResult<T> fail(String msg, Integer code) {
        return new ApiResult<>(code, msg, null);
    }

    // 失败响应（默认状态码 500）
    public static <T> ApiResult<T> fail(String msg) {
        return fail(msg, 500);
    }
    // 新增：带数据和消息的成功响应
    public static <T> ApiResult<T> success(T data, String msg) {
        return new ApiResult<>(200, msg, data);
    }

    // 在ApiResult类中添加新的错误响应方法
    public static <T> ApiResult<T> fail(ErrorCode errorCode) {
        return new ApiResult<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    public static <T> ApiResult<T> fail(ErrorCode errorCode, String customMessage) {
        return new ApiResult<>(errorCode.getCode(), customMessage, null);
    }

}