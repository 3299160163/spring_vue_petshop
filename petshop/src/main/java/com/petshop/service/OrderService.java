package com.petshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.petshop.dto.OrderDTO;
import com.petshop.entity.Order;
import com.petshop.exception.BizException;

import java.math.BigDecimal;
import java.util.List;

// OrderService.java
public interface OrderService extends IService<Order> {

    // 查询单个订单（带管理员权限校验 + DTO 转换）
    OrderDTO getOrderByNo(String orderNo, Integer userId, List<String> roles) throws BizException;



    Order createSimpleOrder(Integer buyerId, Integer petId, String address, String phone);


    OrderDTO updateStatus(String orderNo, String action, Integer userId, List<String> roles);

    void deleteOrder(String orderNo);

    List<OrderDTO> listBuyerOrders(Integer userId);

    List<OrderDTO> listSellerOrders(Integer userId);

    List<OrderDTO> listAllOrders();
}