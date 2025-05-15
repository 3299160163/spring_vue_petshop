package com.petshop.controller;

import com.petshop.Api.ApiResult;
import com.petshop.dto.OrderCreateRequest;
import com.petshop.dto.OrderDTO;
import com.petshop.dto.UpdateOrderStatusRequest;
import com.petshop.entity.Order;
import com.petshop.exception.BizException;
import com.petshop.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // 获取订单详情（返回脱敏 DTO）
    @GetMapping("/{orderNo}")  // 使用订单号作为参数
    public ApiResult<OrderDTO> getOrderDetail(
            @PathVariable String orderNo,  // 改为String类型
            @RequestAttribute Integer userId,
            @RequestAttribute List<String> roles) {
        OrderDTO orderDTO = orderService.getOrderByNo(orderNo, userId, roles);
        return ApiResult.success(orderDTO);
    }

    // OrderController.java
    @PatchMapping("/{orderNo}/status")
    public ApiResult<OrderDTO> updateStatus(
            @PathVariable String orderNo,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            @RequestAttribute Integer userId,
            @RequestAttribute List<String> roles) {
        return ApiResult.success(orderService.updateStatus(orderNo, request.getAction(), userId, roles));
    }


    // 买家订单接口
    @GetMapping("/buyer")
    public ApiResult<List<OrderDTO>> listBuyerOrders(
            @RequestAttribute Integer userId) {
        return ApiResult.success(orderService.listBuyerOrders(userId));
    }

    // 卖家订单接口
    @GetMapping("/seller")
    public ApiResult<List<OrderDTO>> listSellerOrders(
            @RequestAttribute Integer userId) {
        return ApiResult.success(orderService.listSellerOrders(userId));
    }

    // 管理员订单接口
    @GetMapping("/admin")
    public ApiResult<List<OrderDTO>> listAllOrders(
            @RequestAttribute List<String> roles) {
        if (!roles.contains("ADMIN")) {
            return ApiResult.fail("权限不足", 403);
        }
        return ApiResult.success(orderService.listAllOrders());
    }



    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResult<Order> createOrder(
            @Valid @RequestBody OrderCreateRequest request, // 使用@Valid触发校验
            HttpServletRequest httpRequest
    ) {
        // 获取用户ID逻辑
        Integer buyerId = (Integer) httpRequest.getAttribute("userId");
        if (buyerId == null || buyerId <= 0) {
            return ApiResult.fail("用户未登录", 401);
        }

        try {
            Order order = orderService.createSimpleOrder(
                    buyerId,
                    request.getPetId(),
                    request.getAddress(),
                    request.getPhone()
            );
            return ApiResult.success(order, "订单创建成功");
        } catch (BizException e) {
            return ApiResult.fail(e.getMessage(), e.getCode());
        }
    }

    // 新增删除接口
    @DeleteMapping("/delete/{orderNo}")
    public ApiResult<Void> deleteOrder(@PathVariable String orderNo) {
        orderService.deleteOrder(orderNo);
        return ApiResult.success("订单删除成功");
    }
}

//POST http://localhost:8080/api/orders
//Content-Type: application/json
//Authorization: Bearer {{token}}
//
//{
//  "petId": 42,
//  "address": "北京市朝阳区",
//  "phone": "13800138000"
//}