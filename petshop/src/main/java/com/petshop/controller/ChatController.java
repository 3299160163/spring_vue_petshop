package com.petshop.controller;

import com.petshop.dto.ChatMessageDTO;
import com.petshop.dto.ChatSummaryDTO;
import com.petshop.entity.ChatMessage;
import com.petshop.service.ChatService;
import com.petshop.utils.JwtUtils;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/api/chat")
public class ChatController {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final JwtUtils jwtUtils;

    @Autowired
    public ChatController(SimpMessagingTemplate messagingTemplate,
                          ChatService chatService,
                          JwtUtils jwtUtils) {
        this.messagingTemplate = messagingTemplate;
        this.chatService = chatService;
        this.jwtUtils = jwtUtils;
    }

    @MessageMapping("/chat.send")
    public void handleChatMessage(
            @Payload ChatMessage message,
            SimpMessageHeaderAccessor headerAccessor) {

        // ✅ 关键优化点1: 从Header获取Token并验证
        String token = extractTokenFromHeaders(headerAccessor);
        validateToken(token); // 新增独立验证方法

        // ✅ 关键优化点2: 直接从Token解析用户ID
        Integer senderId = jwtUtils.getUserIdFromToken(token);
        validateSender(senderId, message);

        // 保存并转发消息
        message.setSenderId(senderId);
        message.setCreatedAt(LocalDateTime.now());
        chatService.saveMessage(message);
        forwardMessageToReceiver(message);
    }

    //-- 私有工具方法 --//
     // ✅ 独立 Token 验证方法
    private void validateToken(String token) {
        jwtUtils.validateToken(token);  // 直接调用，失败时自动抛异常
    }

    private String extractTokenFromHeaders(SimpMessageHeaderAccessor headerAccessor) {

        // ✅ 方案1: 优先从会话属性获取 Token
        Map<String, Object> sessionAttrs = headerAccessor.getSessionAttributes();
        if (sessionAttrs != null) {
            String sessionToken = (String) sessionAttrs.get("token");
            if (StringUtils.hasText(sessionToken)) {
                return sessionToken;
            }
        }
        // ✅ 方案2:正确获取 Authorization 头的方法
        String authHeader = headerAccessor.getFirstNativeHeader("Authorization");

        if (!StringUtils.hasText(authHeader)) {
            log.warn("缺失 Authorization 请求头 | headers: {}", headerAccessor.toString());
            throw new SecurityException("未提供认证凭证");
        }

        if (authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        log.error("非法 Token 格式 | header: {}", authHeader);
        throw new SecurityException("Token 格式必须为 Bearer 类型");
    }

    private void validateSender(Integer senderId, ChatMessage message) {
        // ✅ 防御性空值检查
        if (senderId == null) {
            log.error("消息发送者身份验证失败 | senderId=null");
            throw new SecurityException("用户身份解析异常");
        }

        if (message.getReceiverId() == null) {
            log.error("消息接收方未指定 | senderId={}", senderId);
            throw new IllegalArgumentException("接收方不能为空");
        }

        // ✅ 允许自发消息的调试模式（按需开启）
        if (senderId.equals(message.getReceiverId())) {
            if (log.isDebugEnabled()) {
                log.debug("自发消息已被拦截 | senderId={}, content={}",
                        senderId, message.getContent());
            }
            throw new IllegalArgumentException("禁止向自己发送消息");
        }
    }

    private void forwardMessageToReceiver(ChatMessage message) {
        try {
            messagingTemplate.convertAndSendToUser(
                    message.getReceiverId().toString(),
                    "/queue/messages",
                    message
            );
        } catch (Exception e) {
            log.error("消息转发失败: receiverId={}", message.getReceiverId(), e);
            throw new RuntimeException("消息发送服务暂不可用");
        }
    }



    // ✅ 获取详细聊天记录（使用 DTO）
    @GetMapping("/messages")
    public ResponseEntity<?> getMessageList(
            @RequestParam("sellerId") Integer sellerId,
            @RequestHeader("Authorization") String token) { // ✅ 移除buyerId参数

        try {
            // ✅ 从 Token 获取当前用户身份
            Integer currentUserId = jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));

            // ✅ 验证用户是否参与对话
            if (!chatService.isConversationParticipant(currentUserId, sellerId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("无权访问此对话");
            }

            // ✅ 调用服务层（参数语义需调整）
            List<ChatMessageDTO> messages = chatService.findMessagesByParticipants(sellerId, currentUserId);
            return ResponseEntity.ok(messages);

        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("身份验证失败");
        }
    }




    // ✅ 获取聊天摘要列表
    @GetMapping("/list")
    public ResponseEntity<List<ChatSummaryDTO>> getChatList(
            @RequestHeader("Authorization") String token
    ) {
        try {
            Integer userId = jwtUtils.getUserIdFromToken(token);
            List<ChatSummaryDTO> list = chatService.findChatSummaries(userId);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<?> deleteMessage(
            @PathVariable Integer messageId,
            @RequestParam Integer currentUserId
    ) {
        try {
            chatService.deleteMessageForUser(messageId, currentUserId);
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}