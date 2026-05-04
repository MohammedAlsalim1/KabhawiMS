package com.example.orderserver.service;

import com.example.orderserver.client.CartClient;
import com.example.orderserver.data.dto.CartDto;
import com.example.orderserver.data.dto.CartItemDto;
import com.example.orderserver.data.dto.OrderDto;
import com.example.orderserver.data.dto.OrderItemDto;
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
    public OrderDto createOrder(OrderDto orderDto, String authorization, String cartId) {
        // جلب السلة من Cart Service باستخدام cartId القادم من الـ Header
        CartDto cart = cartClient.getCart(cartId, authorization);

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new EmptyException("Cart is empty");
        }
        orderDto.setItems(cart.getItems().stream()
                .map(this::toOrderItem)
                .collect(Collectors.toList()));

        // تحويل DTO إلى Entity
        Order order = mapper.map(orderDto);

        // تعبئة بيانات الطلب من السلة
        order.setCartId(cart.getCartId());
        order.setUserId(cart.getUserId());
        order.setTotalAmount(cart.getTotalPrice());
        order.setStatus(OrderStatus.CREATED);

        // حفظ الطلب
        order = orderRepository.save(order);

        // مسح السلة بعد إنشاء الطلب
        cartClient.clearCart(cartId, authorization);

        OrderDto map = mapper.map(order);

        return map;
    }

    @Override
    public OrderDto getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotExistException("Order not found with id: " + orderId));
        return mapper.map(order);
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
        List<Order> byPhoneNumber = orderRepository.findByPhoneNumber(phoneNumber);
        return byPhoneNumber
                .stream()
                .map(mapper::map)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDto> getOrdersByEmail(String email) {
        return orderRepository.findOrdersByEmail(email)
                .stream()
                .map(mapper::map)
                .collect(Collectors.toList());    }

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

    public OrderItemDto toOrderItem(CartItemDto cartItem) {
        OrderItemDto item = new OrderItemDto();
        item.setBarcode(cartItem.getBarcode());
        item.setQuantity(cartItem.getQuantity());
        item.setPrice(cartItem.getPrice());
        item.setTotalPrice(cartItem.getQuantity() * cartItem.getPrice());
        return item;
    }
}