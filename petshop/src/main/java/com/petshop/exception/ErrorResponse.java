package com.petshop.exception;
import java.time.LocalDateTime;

public class ErrorResponse {
    private final int status;
    private final String message;
    private final LocalDateTime timestamp;

    // 全参构造函数
    public ErrorResponse(int status, String message, LocalDateTime timestamp) {
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
    }

    // Getter 方法
    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
}