package com.petshop.dto;


import lombok.Data;

import java.time.LocalDateTime;


// 📁 src/main/java/com/petshop/dto/ChatSummaryDTO.java
@Data
public class ChatSummaryDTO {
    private Integer buyerId;
    private String buyerName;
    private Integer unreadCount;
    private String lastMessage;
    private LocalDateTime lastMessageTime; // 新增时间字段
}