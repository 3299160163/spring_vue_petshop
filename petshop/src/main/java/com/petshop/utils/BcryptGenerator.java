package com.petshop.utils;

import cn.hutool.crypto.digest.BCrypt;

public class BcryptGenerator {
    public static void main(String[] args) {
        String rawPassword = "123456"; // 要加密的明文密码
        String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt(12)); // 生成哈希
        System.out.println("生成的哈希值: " + hashedPassword);
    }
}