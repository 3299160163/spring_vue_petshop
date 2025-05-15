package com.petshop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private String orderNo;
    private BigDecimal amount;
    private String status;       // 枚举 description（如 "待处理"）
    private Integer buyerId;
    private String buyerName;
    private Integer sellerId;
    private String sellerName;
    private Integer petId;
    private String petName;
}