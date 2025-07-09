package com.petshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petshop.entity.SeckillGoods;

import org.apache.ibatis.annotations.Param;

import org.apache.ibatis.annotations.Update;

public interface SeckillGoodsMapper extends BaseMapper<SeckillGoods> {
    
    @Update("UPDATE seckill_goods SET stock = stock - 1 " +
            "WHERE id = #{goodsId} AND stock > 0")
    int updateStock(@Param("goodsId") Integer goodsId);
}
