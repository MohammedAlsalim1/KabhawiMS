package com.example.orderserver.data.dto;

import com.example.orderserver.data.entity.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderDto {
    private Long id;
    private Long userId;
    private String cartId;
    private int totalAmount;
    private OrderStatus status;
    private List<OrderItemDto> items;
}
