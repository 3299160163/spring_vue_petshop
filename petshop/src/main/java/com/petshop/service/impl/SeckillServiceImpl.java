package com.petshop.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petshop.entity.SeckillGoods;
import com.petshop.entity.SeckillMessage;
import com.petshop.entity.SeckillOrder;
import com.petshop.exception.SeckillException;
import com.petshop.mapper.SeckillGoodsMapper;
import com.petshop.mapper.SeckillOrderMapper;
import com.petshop.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;

@Service
@Slf4j
public class SeckillServiceImpl  implements SeckillService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private SeckillGoodsMapper goodsMapper;
    @Autowired
    private SeckillOrderMapper orderMapper;
    
    private static final String STOCK_KEY = "seckill:stock:%s";
    private static final String USER_LIMIT_KEY = "seckill:limit:%s";
    
    @Transactional(rollbackFor = Exception.class)
    public boolean processSeckill(Integer userId, Integer goodsId) {
        // 1. 校验商品有效性
        SeckillGoods goods = goodsMapper.selectOne(Wrappers.<SeckillGoods>lambdaQuery()
            .eq(SeckillGoods::getId, goodsId)
            .gt(SeckillGoods::getStock, 0)
            .le(SeckillGoods::getStartTime, LocalDateTime.now())
            .ge(SeckillGoods::getEndTime, LocalDateTime.now()));
        
        if (goods == null) {
            throw new SeckillException("商品不可用", 1001);
        }
        
        // 2. Redis预减库存（原子操作）
        Long stock = redisTemplate.opsForValue().decrement(STOCK_KEY.formatted(goodsId));
        if (stock == null || stock < 0) {
            redisTemplate.opsForValue().increment(STOCK_KEY.formatted(goodsId));
            throw new SeckillException("库存不足", 1002);
        }
        
        // 3. 用户购买限制（Lua脚本）
        String luaScript = """
            local key = KEYS[1]
            local userId = ARGV[1]
            local limit = tonumber(ARGV[2])
            
            local count = redis.call('HGET', key, userId) or 0
            if tonumber(count) >= limit then
                return 0
            end
            redis.call('HINCRBY', key, userId, 1)
            return 1
            """;
            
        Boolean allowed = redisTemplate.execute(
            new DefaultRedisScript<>(luaScript, Boolean.class),
            Collections.singletonList(USER_LIMIT_KEY.formatted(goodsId)),
            userId.toString(), 
            "1" // 每个用户限购1件
        );
        
        if (allowed == null || !allowed) {
            redisTemplate.opsForValue().increment(STOCK_KEY.formatted(goodsId));
            throw new SeckillException("已达购买上限", 1003);
        }
        
        // 4. 发送异步消息
        SeckillMessage message = new SeckillMessage(userId, goodsId);
        rabbitTemplate.convertAndSend("seckill_exchange", "seckill.order", message);
        
        return true;
    }
}