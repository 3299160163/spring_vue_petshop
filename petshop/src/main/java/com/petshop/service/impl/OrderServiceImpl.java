package com.petshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petshop.dto.OrderDTO;
import com.petshop.entity.Order;
import com.petshop.entity.Pet;
import com.petshop.entity.User;
import com.petshop.enums.OrderStatus;
import com.petshop.exception.BizException;
import com.petshop.exception.BusinessException;
import com.petshop.mapper.OrderMapper;
import com.petshop.mapper.PetMapper;
import com.petshop.mapper.UserMapper;
import com.petshop.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// OrderServiceImpl.java
@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final PetMapper petMapper;


    // 查询单个订单（带管理员权限校验 + DTO 转换）
    @Override
    public OrderDTO getOrderByNo(String orderNo, Integer userId, List<String> roles) {
        if (StringUtils.isBlank(orderNo)) {
            throw new BizException("订单号不能为空", 400);
        }
        // 关键修改点：使用自定义联表查询方法
        OrderDTO orderDTO = orderMapper.selectOrderWithDetail(orderNo);
        if (orderDTO == null) {
            throw new BizException("订单不存在", 404);
        }
        // 权限校验（允许管理员、买家、卖家查看）
        boolean isAdmin = roles.stream().anyMatch(role -> role.equalsIgnoreCase("ADMIN"));
        boolean isBuyer = orderDTO.getBuyerId() != null && userId.equals(orderDTO.getBuyerId());
        boolean isSeller = orderDTO.getSellerId() != null && userId.equals(orderDTO.getSellerId());
        boolean hasPermission = isAdmin || isBuyer || isSeller;
        if (!hasPermission) {
            throw new BizException("无权查看此订单", 403);
        }
        // 数据脱敏：仅对非管理员脱敏
        if (!isAdmin) {
            orderDTO.setBuyerName(desensitizeName(orderDTO.getBuyerName()));
        }
        return orderDTO;
    }

    // 数据脱敏工具方法
    private String desensitizeName(String name) {
        if (name == null || name.isEmpty()) {
            return "匿名用户";
        }
        return name.charAt(0) + "*";
    }


    @Override
    public List<OrderDTO> listBuyerOrders(Integer buyerId) {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("buyer_id", buyerId)
                .orderByDesc("create_time");
        return executeQuery(wrapper);
    }

    @Override
    public List<OrderDTO> listSellerOrders(Integer sellerId) {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("seller_id", sellerId)
                .orderByDesc("create_time");
        return executeQuery(wrapper);
    }

    @Override
    public List<OrderDTO> listAllOrders() {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        return executeQuery(wrapper);
    }

    private List<OrderDTO> executeQuery(QueryWrapper<Order> wrapper) {
        return orderMapper.selectList(wrapper)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private OrderDTO convertToDTO(Order order) {
        // 获取订单状态，处理null值
        OrderStatus status = order.getStatus();
        if (status == null) {
            log.error("订单状态异常: orderNo={}, status=null", order.getId());
            status = OrderStatus.PENDING; // 设置默认状态
        }

        // 获取关联数据（添加空值保护）
        User buyer = Optional.ofNullable(order.getBuyerId())
                .map(userMapper::selectById)
                .orElseGet(() -> {
                    log.warn("订单 {} 买家信息缺失", order.getId());
                    return new User();
                });

        User seller = Optional.ofNullable(order.getSellerId())
                .map(userMapper::selectById)
                .orElseGet(() -> {
                    log.warn("订单 {} 卖家信息缺失", order.getId());
                    return new User();
                });

        Pet pet = Optional.ofNullable(order.getPetId())
                .map(petMapper::selectById)
                .orElseGet(() -> {
                    log.warn("订单 {} 宠物信息缺失", order.getId());
                    return new Pet();
                });

        return OrderDTO.builder()
                .orderNo(order.getOrderNo())
                .amount(order.getAmount())
                .status(status.getDescription())
                .buyerName(buyer.getUsername())
                .sellerName(seller.getUsername())
                .petName(pet.getName())
                .build();
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order createSimpleOrder(Integer buyerId, Integer petId, String address, String phone) {
        // 参数基础校验
        validateParameters(buyerId, petId, address, phone);
        // 获取并验证宠物信息
        Pet pet = petMapper.selectById(petId);
        if (pet == null) {
            throw new BizException("宠物不存在", 404);
        }
        validatePetStatus(pet);
        // 验证卖家有效性
        User seller = userMapper.selectById(pet.getSellerId());
        if (seller == null) {
            throw new BizException("卖家信息异常", 500);
        }
        // 构建并保存订单
        Order order = buildOrder(buyerId, pet, address, phone);
        saveOrderAndUpdatePet(order, pet);
        return order;
    }


    @Override
    @Transactional
    public OrderDTO updateStatus(String orderNo, String action, Integer userId, List<String> roles) {
        // 校验action非空
        if (StringUtils.isBlank(action)) {
            throw new BizException("操作类型不能为空", 400);
        }
        // 查询订单
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            log.error("订单不存在: orderNo={}", orderNo);
            throw new BizException("订单不存在", 404);
        }
        // 权限校验：买家、卖家或管理员
        boolean isPermitted = userId.equals(order.getBuyerId())
                || userId.equals(order.getSellerId())
                || roles.contains("ADMIN");
        if (!isPermitted) {
            throw new BizException("无权操作此订单", 403);
        }
        // 根据action转换状态
        OrderStatus newStatus = resolveNewStatus(order.getStatus(), action, roles.contains("ADMIN"));
        order.setStatus(newStatus);
        orderMapper.updateById(order);
        return convertToDTO(order);
    }

    private OrderStatus resolveNewStatus(OrderStatus currentStatus, String action, boolean isAdmin) {
        return switch (action.toUpperCase()) {
            case "CONFIRM" -> {
                if (currentStatus != OrderStatus.PENDING) {
                    throw new BizException("非待处理订单不可确认", 409);
                }
                yield OrderStatus.PROCESSING;
            }
            case "CANCEL" -> {
                if (currentStatus == OrderStatus.COMPLETED) {
                    throw new BizException("已完成订单不可取消", 409);
                }
                yield OrderStatus.CANCELLED;
            }
            case "COMPLETE" -> {
                if (currentStatus != OrderStatus.PROCESSING && !isAdmin) {
                    throw new BizException("非处理中订单不可标记完成", 409);
                }
                yield OrderStatus.COMPLETED;
            }
            default -> throw new BizException("无效操作类型: " + action, 400);
        };
    }


    private void validateParameters(Integer buyerId, Integer petId, String address, String phone) {
        if (buyerId == null || buyerId <= 0) {
            throw new BizException("无效用户ID", 400);
        }
        if (petId == null || petId <= 0) {
            throw new BizException("无效宠物ID", 400);
        }
        if (StringUtils.isBlank(address) || address.length() > 200) {
            throw new BizException("地址需在1-200字符之间", 400);
        }
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            throw new BizException("手机号格式错误", 400);
        }
    }

    private void validatePetStatus(Pet pet) {
        if ("SOLD".equals(pet.getStatus())) {
            throw new BizException("宠物已售出", 409);
        }
    }
    private Order buildOrder(Integer buyerId, Pet pet, String address, String phone) {
        return new Order()
                .setBuyerId(buyerId)
                .setPetId(pet.getId())
                .setSellerId(pet.getSellerId())
                .setAmount(pet.getPrice())
                .setAddress(address)
                .setPhone(phone)
                .setStatus(OrderStatus.PENDING) // 初始状态为已创建
                .setOrderNo("ORDER_" + System.currentTimeMillis());
    }

    private void saveOrderAndUpdatePet(Order order, Pet pet) {
        // 保存订单
        if (!save(order)) {
            throw new BizException("订单保存失败", 500);
        }
        // 更新宠物状态
        pet.setStatus("SOLD");
        if (petMapper.updateById(pet) == 0) {
            throw new BizException("宠物状态更新失败", 500);
        }
    }

    @Transactional
    public void deleteOrder(String orderNo) {
        // 根据订单号查询订单
        Order order = orderMapper.selectByOrderNo(orderNo);

        // 订单不存在时抛出异常（使用HTTP状态码数值）
        if (order == null) {
            throw new BusinessException("订单不存在", HttpStatus.NOT_FOUND.value());
        }

        // 直接删除（物理删除）
        int rows = orderMapper.deleteById(order.getId());
        if (rows == 0) {
            throw new BusinessException("删除订单失败", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
}