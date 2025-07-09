package com.petshop.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    // 订单交换机
    @Bean
    public TopicExchange seckillExchange() {
        return new TopicExchange("seckill_exchange", true, false);
    }

    // 订单队列（持久化）
    @Bean
    public Queue seckillQueue() {
        return new Queue("seckill_queue", true, false, false);
    }

    // 绑定关系
    @Bean
    public Binding seckillBinding() {
        return BindingBuilder.bind(seckillQueue())
                .to(seckillExchange())
                .with("seckill.order"); // 路由键匹配规则
    }

    // JSON消息转换器
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}