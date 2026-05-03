package com.example.orderserver.data.dto;

import com.example.orderserver.data.entity.OrderStatus;
import lombok.Data;

import java.util.List;

@Data
public class OrderDto {
    private Long id;
    private String userId;
    private String cartId;
    private String firstName;
    private String lastName;
    private String email;
    private String address;
    private String phoneNumber;
    private double totalAmount;
    private OrderStatus status;
    private List<OrderItemDto> items;
}
