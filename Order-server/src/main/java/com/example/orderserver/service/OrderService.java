package com.example.orderserver.service;

import com.example.orderserver.data.dto.OrderDto;
import com.example.orderserver.data.entity.OrderStatus;

import java.util.List;

public interface OrderService {
    OrderDto createOrder(OrderDto orderDto);

    OrderDto getOrderById(Long orderId);

    List<OrderDto> getOrdersByUserId(Long userId);

    List<OrderDto> getOrdersByStatus(OrderStatus status);

    List<OrderDto> getOrdersByPhoneNumber(String phoneNumber);

    OrderDto updateOrderStatus(Long orderId, OrderStatus status);

    void deleteOrder(Long orderId);
}
