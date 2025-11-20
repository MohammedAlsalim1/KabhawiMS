package com.example.cartserver.data.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CartDto {
    private Long id;
    private String cartId;  // UUID للزائر
    private Long userId;    // معرف المستخدم

    private List<CartItemDto> items = new ArrayList<>();
}
