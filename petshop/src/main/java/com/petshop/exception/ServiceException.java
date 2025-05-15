package com.petshop.exception;

public class ServiceException extends RuntimeException {
    private int code;

    public ServiceException(String message) {
        super(message);
        this.code = 400; // 默认错误码
    }

    public ServiceException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}