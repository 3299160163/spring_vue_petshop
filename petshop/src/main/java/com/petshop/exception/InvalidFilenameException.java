package com.petshop.exception; // 替换为实际包名

import lombok.Getter;

@Getter
public class InvalidFilenameException extends RuntimeException {
    private final String fileName;

    public InvalidFilenameException(String fileName) {
        super("非法文件名: " + fileName);
        this.fileName = fileName;
    }

}