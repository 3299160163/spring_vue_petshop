// src/test/java/com/petshop/mapper/OrderMapperTest.java
package com.petshop;

import com.petshop.entity.Order;
import com.petshop.enums.OrderStatus;
import com.petshop.mapper.OrderMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@SpringBootTest
public class OrderMapperTest {
    @Autowired
    private OrderMapper orderMapper;

    @Test
    public void testInsertAndSelect() {
        // 创建订单对象，设置所有必填字段
        Order order = new Order();
        order.setOrderNo("ORDER_202403310001");
        order.setBuyerId(1);
        order.setPetId(100);
        order.setSellerId(1);
        order.setAmount(new BigDecimal("100.00"));
        order.setStatus(OrderStatus.PROCESSING);
        order.setAddress("北京市朝阳区XX街道123号");
        order.setPhone("13812345678");          // 联系电话
        order.setCreateTime(LocalDateTime.now());

        // 插入数据
        int rows = orderMapper.insert(order);
        Assertions.assertEquals(1, rows);

        // 查询并验证
        Order savedOrder = orderMapper.selectById(order.getId());
        Assertions.assertEquals("13812345678", savedOrder.getPhone());
    }
}