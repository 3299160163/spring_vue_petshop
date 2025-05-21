package com.petshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_message")  // 关键：强制绑定表名
public class ChatMessage {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("sender_id")
    private Integer senderId;

    @TableField("receiver_id")
    private Integer receiverId;

    private String content;

    @TableField("is_read")
    private Boolean isRead;

    @TableField("created_at")
    private LocalDateTime createdAt;

    // 新增字段：发送方是否删除（默认false）
    @TableField("sender_deleted")
    private Boolean senderDeleted = false;

    // 新增字段：接收方是否删除（默认false）
    @TableField("receiver_deleted")
    private Boolean receiverDeleted = false;
}