package com.petshop.exception;

import lombok.Getter;

@Getter
public class InvalidFileTypeException extends RuntimeException {
    private final String fileType;

    public InvalidFileTypeException(String fileType) {
        super("不支持的文件类型: " + fileType);
        this.fileType = fileType;
    }

}