package com.petshop.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petshop.dto.OrderDTO;
import com.petshop.entity.Order;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface OrderMapper extends BaseMapper<Order> {
    @Select("SELECT COUNT(*) FROM `order` WHERE pet_id = #{petId}")
    int countByPetId(Integer petId);

    @Select("SELECT * FROM `order` WHERE order_no = #{orderNo}")
    Order selectByOrderNo(@Param("orderNo") String orderNo);

    // 删除关联订单（修复后的）
    @Delete("DELETE FROM `order` WHERE pet_id = #{petId}") // ✅ 添加反引号
    int deleteByPetId(@Param("petId") Integer petId);

    // 联表查询方法
    OrderDTO selectOrderWithDetail(@Param("orderNo") String orderNo);
}
