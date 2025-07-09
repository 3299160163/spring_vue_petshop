package com.petshop.controller;

import com.petshop.Api.ApiResult;
import com.petshop.enums.ErrorCode;
import com.petshop.exception.SeckillException;
import com.petshop.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

// 文件路径: src/main/java/com/petshop/controller/SeckillController.java
@RestController
@RequestMapping("/seckill")
@Slf4j
public class SeckillController {

    @Autowired
    private SeckillService seckillService;

    @PostMapping("/{goodsId}")
    public ApiResult<?> seckill(
            @PathVariable("goodsId") Integer goodsId,
            @RequestHeader("X-User-Id") Integer userId) {

        // 参数校验
        if (goodsId == null || goodsId <= 0 || userId == null || userId <= 0) {
            return ApiResult.fail(ErrorCode.ILLEGAL_REQUEST);
        }

        try {
            boolean result = seckillService.processSeckill(userId, goodsId);
            return result ? ApiResult.success("请求已受理")
                    : ApiResult.fail(ErrorCode.SYSTEM_ERROR, "处理失败");
        } catch (SeckillException e) {
            log.error("秒杀异常: ", e);
            return ApiResult.fail(e.getMessage());
        }
    }
}