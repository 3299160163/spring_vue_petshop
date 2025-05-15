package com.petshop.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.petshop.enums.OrderStatus;
import lombok.Data;
import lombok.experimental.Accessors;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("`order`") // 注意：MySQL保留字需要用反引号转义
public class Order {

    // ================= 核心字段 =================
    @TableId(type = IdType.AUTO)
    private Integer id; // 主键ID（自增，对应数据库 id 列）

    @TableField("order_no")
    private String orderNo; // 业务唯一订单号（对应 order_no 列）

    // ================= 关联字段 =================
    @TableField("buyer_id")
    private Integer buyerId; // 买家ID（对应 buyer_id 列）

    @TableField("pet_id")
    private Integer petId; // 宠物ID（对应 pet_id 列）

    @TableField("seller_id")
    private Integer sellerId; // 卖家ID（对应 seller_id 列）

    @TableField(exist = false) // 标记为非数据库字段
    private String buyerName;   // 对应 buyer.username
    @TableField(exist = false) // 标记为非数据库字段
    private String sellerName;  // 对应 seller.username
    @TableField(exist = false) // 标记为非数据库字段
    private String petName;     // 对应 p.name

    // ================= 业务字段 =================
    private BigDecimal amount; // 订单金额（对应 amount 列，DECIMAL类型）

    @TableField("status")
    private OrderStatus status; // 订单状态（需确保枚举与数据库值匹配）

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime; // 创建时间（自动填充）

    private String address; // 收货地址（对应 address 列）

    private String phone; // 联系电话（对应 phone 列）
}

