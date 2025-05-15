package com.petshop.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    private Long total;     // 总记录数
    private Long pages;      // 总页数
    private Long current;    // 当前页码
    private Long size;       // 每页条数
    private List<T> records; // 数据列表

    // 基础构造方法（直接传入分页参数）
    public PageResult(long total, long pages, long current, long size, List<T> records) {
        this.total = total;
        this.pages = pages;
        this.current = current;
        this.size = size;
        this.records = records != null ? records : Collections.emptyList();
    }

    // 从MyBatis-Plus分页对象转换（无类型转换）
    public static <T> PageResult<T> from(IPage<T> page) {
        return new PageResult<>(
                page.getTotal(),
                page.getPages(),
                page.getCurrent(),
                page.getSize(),
                page.getRecords()
        );
    }

    // 带类型转换的构造方法（实体→DTO）
    public static <E, D> PageResult<D> from(IPage<E> page, Function<E, D> converter) {
        List<D> convertedRecords = page.getRecords()
                .stream()
                .map(converter)
                .collect(Collectors.toList());

        return new PageResult<>(
                page.getTotal(),
                page.getPages(),
                page.getCurrent(),
                page.getSize(),
                convertedRecords
        );
    }

    // 链式转换方法（适合Service层处理）
    public <R> PageResult<R> convert(Function<T, R> converter) {
        List<R> convertedList = this.records.stream()
                .map(converter)
                .collect(Collectors.toList());

        return new PageResult<>(
                this.total,
                this.pages,
                this.current,
                this.size,
                convertedList
        );
    }
}