package com.petshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petshop.entity.Pet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PetMapper extends BaseMapper<Pet> {
    // ✅ 新增方法：统计卖家拥有的宠物数量
    int countBySellerId(@Param("sellerId") Integer sellerId);
}