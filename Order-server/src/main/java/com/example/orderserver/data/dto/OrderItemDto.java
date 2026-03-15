package com.example.orderserver.data.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemDto {
    private String barcode;
    private Integer quantity;
    private BigDecimal price;
}