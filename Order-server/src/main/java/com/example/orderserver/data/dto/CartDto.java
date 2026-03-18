package com.example.orderserver.data.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CartDto {
    private Long id;
    private String cartId;
    private Long userId;
    private List<CartItemDto> items = new ArrayList<>();

}