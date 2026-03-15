package com.example.orderserver.data.dto;

import lombok.Data;

@Data
public class OrderItemDto {
    private String barcode;
    private Integer quantity;
    private double price;
}