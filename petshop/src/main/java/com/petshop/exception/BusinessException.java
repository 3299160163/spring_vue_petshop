package com.petshop.exception;
//自定义业务异常类
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final int statusCode;

    public BusinessException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

}