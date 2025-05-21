package com.petshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.petshop.dto.ChatMessageDTO;
import com.petshop.dto.ChatSummaryDTO;
import com.petshop.entity.ChatMessage;
import com.petshop.mapper.ChatMessageMapper;
import com.petshop.mapper.PetMapper;
import com.petshop.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

import java.util.stream.Collectors;

// 📁 src/main/java/com/example/service/impl/ChatServiceImpl.java
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private final ChatMessageMapper chatMessageMapper;
    private final PetMapper petMapper;

    // ====================== 实现接口方法 ======================
    @Override
    @Transactional // ✅ 添加事务注解
    public void saveMessage(ChatMessage message) {
        chatMessageMapper.insert(message);
    }

    @Override
    public List<ChatMessageDTO> findMessagesByParticipants(Integer sellerId, Integer buyerId) {
        validateUserPair(sellerId, buyerId);
        List<ChatMessage> messages = findMessagesBetweenUsers(sellerId, buyerId);
        return convertToDTO(messages, buyerId);
    }


    @Override
    public List<ChatSummaryDTO> findChatSummaries(Integer currentUserId) {
        if (!isValidUser(currentUserId)) {
            log.warn("非法用户请求聊天摘要: {}", currentUserId);
            return Collections.emptyList();
        }
        return chatMessageMapper.selectChatSummaries(currentUserId);
    }

    @Override
    public boolean isConversationParticipant(Integer currentUserId, Integer sellerId) {
        // 实现逻辑（示例：查询是否有对话记录）
        QueryWrapper<ChatMessage> query = new QueryWrapper<>();
        query.and(w -> w
                        .eq("sender_id", currentUserId)
                        .eq("receiver_id", sellerId)
                )
                .or(w -> w
                        .eq("sender_id", sellerId)
                        .eq("receiver_id", currentUserId)
                )
                .last("LIMIT 1");
        return chatMessageMapper.selectCount(query) > 0;
    }

    @Override
    @Transactional
    public void deleteMessageForUser(Integer messageId, Integer currentUserId) {
        // 1. 验证消息是否存在
        ChatMessage message = chatMessageMapper.selectById(messageId);
        if (message == null) {
            throw new IllegalArgumentException("消息不存在");
        }

        // 2. 验证当前用户是否有权限删除（发送方或接收方）
        if (!currentUserId.equals(message.getSenderId()) && !currentUserId.equals(message.getReceiverId())) {
            throw new SecurityException("无权删除该消息");
        }

        // 3. 根据用户角色更新删除标记
        if (currentUserId.equals(message.getSenderId())) {
            // 发送方删除：更新 sender_deleted
            chatMessageMapper.updateSenderDeleted(messageId, true);
        } else {
            // 接收方删除：更新 receiver_deleted
            chatMessageMapper.updateReceiverDeleted(messageId, true);
        }
    }


    // ====================== 私有工具方法 ======================
    private List<ChatMessage> findMessagesBetweenUsers(Integer user1, Integer user2) {
        QueryWrapper<ChatMessage> query = new QueryWrapper<>();
        query.and(w -> w
                        .eq("sender_id", user1)
                        .eq("receiver_id", user2)
                        .eq("sender_deleted", false) // 发送方未删除
                )
                .or(w -> w
                        .eq("sender_id", user2)
                        .eq("receiver_id", user1)
                        .eq("receiver_deleted", false) // 接收方未删除
                )
                .orderByAsc("created_at");
        return chatMessageMapper.selectList(query);
    }

    private void validateUserPair(Integer user1, Integer user2) {
        if (user1 == null || user2 == null || user1 <= 0 || user2 <= 0) {
            throw new IllegalArgumentException("用户ID必须为正整数");
        }
        if (user1.equals(user2)) {
            throw new SecurityException("禁止查询自我对话");
        }
    }

    private boolean isValidUser(Integer userId) {
        return userId != null && userId > 0;
    }

    private List<ChatMessageDTO> convertToDTO(List<ChatMessage> messages, Integer currentUserId) {
        return messages.stream()
                .map(msg -> new ChatMessageDTO(
                        msg.getId(),
                        msg.getContent(),
                        msg.getCreatedAt(),
                        msg.getSenderId(),    // ✅ 传递发送方ID
                        msg.getReceiverId(),  // ✅ 传递接收方ID
                        msg.getSenderId().equals(currentUserId)
                ))
                .collect(Collectors.toList());
    }
}