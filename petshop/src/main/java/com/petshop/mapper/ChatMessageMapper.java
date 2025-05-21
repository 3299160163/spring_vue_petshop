package com.petshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petshop.dto.ChatSummaryDTO;
import com.petshop.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

// 📁 src/main/java/com/example/mapper/ChatMessageMapper.java
@Mapper // 关键：标识为 MyBatis Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    List<ChatSummaryDTO> selectChatSummaries(@Param("userId") Integer userId);
    // 更新发送方删除标记
    @Update("UPDATE chat_message SET sender_deleted = #{deleted} WHERE id = #{messageId}")
    void updateSenderDeleted(@Param("messageId") Integer messageId, @Param("deleted") Boolean deleted);

    // 更新接收方删除标记
    @Update("UPDATE chat_message SET receiver_deleted = #{deleted} WHERE id = #{messageId}")
    void updateReceiverDeleted(@Param("messageId") Integer messageId, @Param("deleted") Boolean deleted);
}