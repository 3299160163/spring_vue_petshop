package com.petshop.exception;

import lombok.Getter;

@Getter
public class BizException extends RuntimeException {
    private final Integer code;

    public BizException(String message, Integer code) {
        super(message);
        this.code = code;
    }

}