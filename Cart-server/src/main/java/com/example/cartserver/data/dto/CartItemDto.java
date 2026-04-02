package com.example.cartserver.data.dto;


import lombok.Data;

@Data
public class CartItemDto {
    private String barcode;
    private int quantity;
    private double price;
    private double totalPrice;
}