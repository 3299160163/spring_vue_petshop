package com.petshop.config;

import com.petshop.service.OnlineStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Objects;

// 📁 src/main/java/com/petshop/config/WebSocketDisconnectListener.java
@Component
public class WebSocketDisconnectListener implements ApplicationListener<SessionDisconnectEvent> {

    @Autowired
    private OnlineStatusService onlineStatusService;

    @Override
    public void onApplicationEvent(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String userId = (String) Objects.requireNonNull(accessor.getSessionAttributes()).get("userId");
        if (userId != null) {
            onlineStatusService.userDisconnected(userId); // 标记用户离线
        }
    }
}