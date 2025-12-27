package com.example.cartserver.data.dto;


import lombok.Data;

@Data
public class CartItemDto {
    private Long userId;
    private  String cartId;
    private String barcode;
    private int quantity;
}