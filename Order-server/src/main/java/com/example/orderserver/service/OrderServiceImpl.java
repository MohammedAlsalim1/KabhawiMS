package com.example.orderserver.service;

import com.example.orderserver.client.CartClient;
import com.example.orderserver.data.dto.CartDto;
import com.example.orderserver.data.dto.OrderDto;
import com.example.orderserver.data.entity.Order;
import com.example.orderserver.data.entity.OrderStatus;
import com.example.orderserver.data.repository.OrderRepository;
import com.example.orderserver.mapper.appMapper;
import com.example.orderserver.service.ex.EmptyException;
import com.example.orderserver.service.ex.NotExistException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class OrderServiceImpl implements OrderService {

    private final CartClient cartClient;
    private final OrderRepository orderRepository;
    private final appMapper mapper;

    @Override
    public OrderDto createOrder(OrderDto orderDto) {
        // جلب السلة من Cart Service
        CartDto cart = cartClient.getCart(orderDto.getUserId(), orderDto.getCartId());

        if (cart.getItems().isEmpty()) {
            throw new EmptyException("Cart is empty");
        }

        // تحويل DTO إلى Entity
        Order order = mapper.map(orderDto);
        order.setStatus(OrderStatus.CREATED);

        // حفظ الطلب
        order = orderRepository.save(order);

        // مسح السلة بعد إنشاء الطلب
        cartClient.clearCart(orderDto.getUserId(), orderDto.getCartId());

        return mapper.map(order);
    }

    @Override
    public OrderDto getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotExistException("Order not found with id: " + orderId));
        return mapper.map(order);
    }

    @Override
    public List<OrderDto> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId)
                .stream()
                .map(mapper::map)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDto> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status)
                .stream()
                .map(mapper::map)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDto> getOrdersByPhoneNumber(String phoneNumber) {
        return orderRepository.findByPhoneNumber(phoneNumber)
                .stream()
                .map(mapper::map)
                .collect(Collectors.toList());
    }

    @Override
    public OrderDto updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotExistException("Order not found with id: " + orderId));
        order.setStatus(status);
        order = orderRepository.save(order);
        return mapper.map(order);
    }

    @Override
    public void deleteOrder(Long orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new NotExistException("Order not found with id: " + orderId);
        }
        orderRepository.deleteById(orderId);
    }
}