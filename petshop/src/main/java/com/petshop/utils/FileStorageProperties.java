package com.petshop.utils;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

// FileStorageProperties.java
@Setter
@Getter
@ConfigurationProperties(prefix = "file")
public class FileStorageProperties {
    // Getter & Setter
    private String uploadDir;
    private List<String> allowedTypes;  // 对应配置中的 allowed-types

}