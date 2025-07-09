package com.petshop.common;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.petshop.entity.SeckillMessage;
import com.petshop.entity.SeckillOrder;
import com.petshop.enums.SeckillOrderStatus;
import com.petshop.mapper.SeckillGoodsMapper;
import com.petshop.mapper.SeckillOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@Slf4j
public class SeckillConsumer {
    
    @Autowired
    private SeckillOrderMapper orderMapper;
    @Autowired
    private SeckillGoodsMapper goodsMapper;
    
    @RabbitListener(queues = "seckill_queue")
    @Transactional(rollbackFor = Exception.class)
    public void handleOrder(SeckillMessage message) {
        Integer userId = message.getUserId();
        Integer goodsId = message.getGoodsId();
        
        // 1. 幂等性校验
        SeckillOrder existOrder = orderMapper.selectOne(Wrappers.<SeckillOrder>lambdaQuery()
            .eq(SeckillOrder::getUserId, userId)
            .eq(SeckillOrder::getGoodsId, goodsId));
        
        if (existOrder != null) {
            log.warn("重复订单: userId={}, goodsId={}", userId, goodsId);
            return;
        }
        
        // 2. 创建订单
        SeckillOrder order = new SeckillOrder();
        order.setOrderNo(generateOrderNo(userId, goodsId));
        order.setUserId(userId);
        order.setGoodsId(goodsId);
        order.setQuantity(1);
        order.setStatus(SeckillOrderStatus.CREATED);
        order.setExpireTime(LocalDateTime.now().plusMinutes(15));
        orderMapper.insert(order);
        
        // 3. 扣减数据库库存
        int updateCount = goodsMapper.updateStock(goodsId);
        if (updateCount == 0) {
            throw new RuntimeException("库存扣减失败");
        }
        
        log.info("订单创建成功: {}", order.getOrderNo());
    }
    
    private String generateOrderNo(Integer userId, Integer goodsId) {
        return "SK" + System.currentTimeMillis() + 
               userId % 1000 + 
               String.format("%04d", goodsId % 10000);
    }
}