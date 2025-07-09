package com.petshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petshop.entity.SeckillOrder;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface SeckillOrderMapper extends BaseMapper<SeckillOrder> {
    
    @Select("SELECT COUNT(*) FROM seckill_order " +
            "WHERE user_id = #{userId} AND goods_id = #{goodsId}")
    int checkUserOrder(@Param("userId") Integer userId,
                      @Param("goodsId") Integer goodsId);
}