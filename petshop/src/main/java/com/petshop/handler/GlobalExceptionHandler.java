package com.petshop.handler;

import com.petshop.Api.ApiResult;
import com.petshop.exception.BusinessException;
import com.petshop.exception.InvalidFilenameException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.io.IOException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    //------------------------ 自定义业务异常处理 ------------------------
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResult<Void>> handleBusinessException(BusinessException ex) {
        // 参数安全校验
        if (!HttpStatus.valueOf(ex.getStatusCode()).isError()) {
            log.error("非法HTTP状态码: {}", ex.getStatusCode());
            throw new IllegalArgumentException("状态码必须为4xx或5xx");
        }

        log.warn("业务异常: code={}, msg={}", ex.getStatusCode(), ex.getMessage());

        return ResponseEntity
                .status(ex.getStatusCode())
                .body(ApiResult.fail(ex.getMessage(), ex.getStatusCode()));
    }

    @ExceptionHandler(InvalidFilenameException.class)
    public ResponseEntity<ApiResult<Void>> handleInvalidFilename(InvalidFilenameException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResult.fail("非法文件名: " + ex.getFileName(), 400));
    }



    //------------------------ 文件上传异常处理 ------------------------
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResult<Void>> handleFileSizeExceeded() {
        String errorMsg = "文件大小超过系统限制";
        log.warn(errorMsg);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResult.fail(errorMsg, HttpStatus.BAD_REQUEST.value()));
    }

    //------------------------ 参数校验异常处理 ------------------------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMsg = ex.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("; "));

        log.warn("参数校验失败: {}", errorMsg);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResult.fail(errorMsg, HttpStatus.BAD_REQUEST.value()));
    }


    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiResult<Void>> handleMissingFile(MissingServletRequestPartException ex) {
        String paramName = ex.getRequestPartName();
        String errorMsg = String.format("缺少必需的文件参数: %s", paramName);
        return ResponseEntity.badRequest().body(ApiResult.fail(errorMsg, HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ApiResult<Void>> handleIOException(IOException ex) {
        return ResponseEntity.internalServerError()
                .body(ApiResult.fail("文件存储失败，请重试", HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ApiResult<Void> handleDuplicateUsernameException(DataIntegrityViolationException ex) {
        if (ex.getMessage().contains("uniq_username")) {
            return ApiResult.fail("用户名已存在", 400); // 返回 ApiResult<Void>
        }
        return ApiResult.fail("数据库错误", 500);
    }

    // 处理其他内容类型异常
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ApiResult<?> handleMediaTypeException() {
        return ApiResult.fail("请使用JSON格式提交数据", 415);
    }




    //------------------------ 系统级异常处理（兜底） ------------------------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<Void>> handleSystemException(Exception ex) {
        // 安全提示信息（避免暴露内部错误）
        String safeMessage = "系统繁忙，请稍后重试";

        // 关键错误记录
        log.error("系统异常: ", ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResult.fail(safeMessage, HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
}