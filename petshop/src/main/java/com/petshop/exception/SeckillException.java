// 文件路径: src/main/java/com/petshop/exception/SeckillException.java
package com.petshop.exception;

import com.petshop.enums.ErrorCode;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 秒杀业务异常（unchecked exception）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SeckillException extends RuntimeException {
    private final int code;
    public SeckillException(String message, int code) {
        super(message);
        this.code = code;
    }
    public SeckillException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

}