package com.petshop.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.petshop.enums.SeckillOrderStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("seckill_order")
public class SeckillOrder {

    @TableId(value = "order_no")
    private String orderNo;

    @TableField("user_id")
    private Integer userId;

    @TableField("goods_id")
    private Integer goodsId;

    @TableField("quantity")
    private Integer quantity;

    @TableField("status")
    private SeckillOrderStatus status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField("expire_time")
    private LocalDateTime expireTime;
}

