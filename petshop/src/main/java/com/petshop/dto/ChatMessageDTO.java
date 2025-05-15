package com.petshop.dto;

import lombok.Data;

import java.time.LocalDateTime;

// 📁 src/main/java/com/petshop/dto/ChatMessageDTO.java
import lombok.AllArgsConstructor;


@Data
@AllArgsConstructor // 自动生成全参构造函数
public class ChatMessageDTO {
    private Integer id;

    private String content;
    private LocalDateTime createdAt;
    private Integer senderId; // ✅ 必须添加发送方ID字段
    private Integer receiverId; // ✅ 新增字段
    private Boolean isMe;
}