package com.petshop.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("seckill_user_limit")
public class SeckillUserLimit {

    @TableId(type = IdType.AUTO)
    private Long id;


    @TableField("user_id")
    private Integer userId;

    @TableField("goods_id")
    private Integer goodsId;

    @TableField("total")
    private Integer total;
    /**
     * 更新时间（隐式填充）
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}