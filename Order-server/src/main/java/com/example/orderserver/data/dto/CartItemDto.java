package com.example.orderserver.data.dto;

import lombok.Data;

@Data
public class CartItemDto {

    private String barcode;
    private int quantity;
    private double price;
    private double totalPrice;

}