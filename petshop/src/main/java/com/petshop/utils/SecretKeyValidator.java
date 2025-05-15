package com.petshop.utils;

import java.util.Base64;

public class SecretKeyValidator {
    public static void main(String[] args) {
        String secret = "Qygr6GJDy5qFlSJcr4WGZLZC6oRExwCy1utw1hezh6w=";
        try {
            byte[] keyBytes = Base64.getDecoder().decode(secret);
            System.out.println("密钥长度: " + keyBytes.length + " 字节"); // 应输出 32
        } catch (IllegalArgumentException e) {
            System.out.println("密钥格式错误: " + e.getMessage());
        }
    }
}