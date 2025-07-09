package com.petshop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("seckill_goods")
public class SeckillGoods {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("goods_id")
    private Integer goodsId;

    @TableField("name")
    private String name;

    @TableField("original_price")
    private BigDecimal originalPrice;

    @TableField("seckill_price")
    private BigDecimal seckillPrice;

    @TableField("stock")
    private Integer stock;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField("verify_code")
    private String verifyCode;

    @TableField("is_deleted")
    @TableLogic
    private Integer isDeleted;
}