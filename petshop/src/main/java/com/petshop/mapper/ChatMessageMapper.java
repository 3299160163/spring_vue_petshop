package com.petshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petshop.dto.ChatSummaryDTO;
import com.petshop.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

// 📁 src/main/java/com/example/mapper/ChatMessageMapper.java
@Mapper // 关键：标识为 MyBatis Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    List<ChatSummaryDTO> selectChatSummaries(@Param("userId") Integer userId);
}