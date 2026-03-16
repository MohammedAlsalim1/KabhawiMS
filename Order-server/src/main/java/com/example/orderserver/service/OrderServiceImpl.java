package com.example.orderserver.service;

import com.example.orderserver.data.dto.OrderDto;
import com.example.orderserver.data.entity.OrderStatus;

import java.util.List;

public class OrderServiceImpl implements OrderService {
    @Override
    public OrderDto createOrder(OrderDto orderDto) {
        return null;
    }

    @Override
    public OrderDto getOrderById(Long orderId) {
        return null;
    }

    @Override
    public List<OrderDto> getOrdersByUserId(Long userId) {
        return List.of();
    }

    @Override
    public List<OrderDto> getOrdersByStatus(OrderStatus status) {
        return List.of();
    }

    @Override
    public List<OrderDto> getOrdersByPhoneNumber(String phoneNumber) {
        return List.of();
    }

    @Override
    public OrderDto updateOrderStatus(Long orderId, OrderStatus status) {
        return null;
    }

    @Override
    public void deleteOrder(Long orderId) {

    }
}
