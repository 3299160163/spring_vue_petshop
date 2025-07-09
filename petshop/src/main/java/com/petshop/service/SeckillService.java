package com.petshop.service;

import com.petshop.exception.SeckillException;

public interface SeckillService {

    boolean processSeckill(Integer userId, Integer goodsId);

}
