package com.petshop.service;

import com.petshop.dto.ChatMessageDTO;
import com.petshop.dto.ChatSummaryDTO;
import com.petshop.entity.ChatMessage;

import java.util.List;

// 📁 src/main/java/com/example/service/ChatService.java
public interface ChatService {
    void saveMessage(ChatMessage message);

    // ✅ 添加实际使用的新方法声明
    List<ChatMessageDTO> findMessagesByParticipants(Integer sellerId, Integer buyerId);

    List<ChatSummaryDTO> findChatSummaries(Integer currentUserId);

    boolean isConversationParticipant(Integer currentUserId, Integer sellerId);
}