package com.example.cartserver.data.dto;


import lombok.Data;

@Data
public class CartItemDto {
    private Long id;
    private String barcode;
    private int quantity;
}