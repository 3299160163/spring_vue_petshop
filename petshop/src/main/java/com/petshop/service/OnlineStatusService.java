package com.petshop.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

// 📁 src/main/java/com/example/service/OnlineStatusService.java
// 📁 src/main/java/com/example/service/OnlineStatusService.java
@Service
@RequiredArgsConstructor
@Slf4j
public class OnlineStatusService {
    private final RedisTemplate<String, String> redisTemplate;

    // 使用 ConcurrentHashMap 作为本地缓存降级方案
    private static final ConcurrentHashMap<String, Boolean> localOnlineCache = new ConcurrentHashMap<>();
    private static final String ONLINE_USERS_KEY = "online_users";

    /**
     * 标记用户在线（优先Redis，失败时降级到本地缓存）
     */
    public void userOnline(String userId) {
        try {
            // 尝试写入 Redis
            redisTemplate.opsForSet().add(ONLINE_USERS_KEY, userId);
            log.debug("[Redis] 用户 {} 在线状态已更新", userId);
        } catch (RedisConnectionFailureException e) {
            // Redis 不可用时降级到本地缓存
            localOnlineCache.put(userId, true);
            log.warn("[Redis 不可用] 用户 {} 在线状态已保存到本地缓存", userId);
        }
    }

    /**
     * 标记用户离线（双写删除策略）
     */
    public void userDisconnected(String userId) {
        try {
            // 尝试从 Redis 移除
            redisTemplate.opsForSet().remove(ONLINE_USERS_KEY, userId);
            log.debug("[Redis] 用户 {} 离线状态已更新", userId);
        } catch (RedisConnectionFailureException e) {
            // Redis 不可用时仅操作本地缓存
            log.warn("[Redis 不可用] 用户 {} 离线状态仅更新本地缓存", userId);
        } finally {
            // 确保无论如何都清理本地缓存
            localOnlineCache.remove(userId);
        }
    }

    /**
     * 检查用户是否在线（混合查询策略）
     */
    public boolean isUserOnline(String userId) {
        // 优先检查本地缓存
        if (localOnlineCache.containsKey(userId)) {
            return true;
        }

        try {
            // 本地缓存未命中时查询 Redis
            return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(ONLINE_USERS_KEY, userId));
        } catch (RedisConnectionFailureException e) {
            log.warn("[Redis 不可用] 无法查询用户在线状态，默认返回离线");
            return false;
        }
    }

    /**
     * 同步本地缓存到 Redis（可用于恢复连接后）
     */
    @Scheduled(fixedDelay = 60000) // 每60秒同步一次
    public void syncLocalCacheToRedis() {
        if (localOnlineCache.isEmpty()) return;

        try {
            log.info("开始同步本地在线状态到Redis，共 {} 条记录", localOnlineCache.size());
            localOnlineCache.keySet().forEach(userId ->
                    redisTemplate.opsForSet().add(ONLINE_USERS_KEY, userId)
            );
            localOnlineCache.clear();
            log.info("本地缓存已成功同步至Redis");
        } catch (RedisConnectionFailureException e) {
            log.error("同步本地缓存到Redis失败，继续保持降级模式");
        }
    }
}