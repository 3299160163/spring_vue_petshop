package com.petshop.utils;
import com.petshop.exception.InvalidFileTypeException;
import com.petshop.exception.InvalidFilenameException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
public class FileStorageUtil {
    private final Path uploadDir;
    private final List<String> allowedTypes;

    // 通过配置类注入参数
    @Autowired
    public FileStorageUtil(FileStorageProperties properties) {
        this.uploadDir = Paths.get(properties.getUploadDir()).toAbsolutePath().normalize();
        this.allowedTypes = properties.getAllowedTypes();
        createUploadDirIfNotExists();
    }

    // 核心文件存储方法
    public String storeFile(MultipartFile file) throws IOException {
        // 1. 基础校验（空文件）
        validateFileNotEmpty(file);

        // 2. 文件类型校验
        String fileExtension = extractFileExtension(file.getOriginalFilename());
        validateFileType(fileExtension);

        // 3. 生成安全文件名
        String sanitizedFileName = sanitizeFilename(file.getOriginalFilename());
        String uniqueFileName = generateUniqueName(sanitizedFileName);

        // 4. 保存文件
        Path targetPath = buildTargetPath(uniqueFileName);
        Files.copy(file.getInputStream(), targetPath);

        return "/uploads/pet_images/" + uniqueFileName;
    }

    //--------- 私有方法 ---------
    private void createUploadDirIfNotExists() {
        try {
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
        } catch (IOException e) {
            throw new RuntimeException("无法创建文件存储目录", e);
        }
    }

    private void validateFileNotEmpty(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
    }

    private String extractFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            throw new InvalidFileTypeException("无法识别文件类型");
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    private void validateFileType(String fileExtension) {
        if (!allowedTypes.contains(fileExtension)) {
            throw new InvalidFileTypeException(fileExtension);
        }
    }

    private String sanitizeFilename(String originalName) {
        String cleanedName = StringUtils.cleanPath(Objects.requireNonNull(originalName));
        if (cleanedName.contains("..")) {
            throw new InvalidFilenameException(cleanedName);
        }
        return cleanedName;
    }

    private String generateUniqueName(String originalName) {
        return "pet_" + UUID.randomUUID() + "_" + originalName;
    }

    private Path buildTargetPath(String fileName) {
        return uploadDir.resolve(fileName).normalize();
    }


    public void deleteFile(String fileUrl) throws IOException {
        // 1. 基础校验
        if (fileUrl == null || fileUrl.isEmpty()) {
            throw new IllegalArgumentException("文件路径不能为空");
        }

        // 2. 从 URL 中提取文件名（例如 "/uploads/pet_images/pet_xxx.jpg" → "pet_xxx.jpg"）
        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        if (fileName.isEmpty()) {
            throw new SecurityException("非法文件路径格式: " + fileUrl);
        }

        // 3. 构建安全路径
        Path targetPath = uploadDir.resolve(fileName).normalize();

        // 4. 校验路径合法性（防止路径穿越攻击）
        if (!targetPath.startsWith(uploadDir)) {
            throw new SecurityException("非法文件路径访问: " + targetPath);
        }

        // 5. 删除文件
        if (Files.exists(targetPath)) {
            Files.delete(targetPath);
        } else {
            throw new FileNotFoundException("文件不存在: " + fileName);
        }
    }
}