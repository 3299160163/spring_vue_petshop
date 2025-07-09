package com.petshop.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeckillMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Integer userId;
    private Integer goodsId;
}