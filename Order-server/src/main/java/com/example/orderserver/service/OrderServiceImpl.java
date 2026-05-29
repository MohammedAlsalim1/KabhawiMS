package com.example.orderserver.service;

import com.example.orderserver.client.CartClient;
import com.example.orderserver.data.dto.CartDto;
import com.example.orderserver.data.dto.CartItemDto;
import com.example.orderserver.data.dto.OrderDto;
import com.example.orderserver.data.dto.OrderItemDto;
import com.example.orderserver.data.entity.Order;
import com.example.orderserver.data.entity.OrderItem;
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
    private final EmailService emailService;
    @Override
    public OrderDto createOrder(OrderDto orderDto, String authorization, String cartId) {
        // 1. جلب السلة من Cart Service
        CartDto cart = cartClient.getCart(cartId, authorization);

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new EmptyException("Cart is empty");
        }

        // تحويل عناصر السلة ووضعها في الـ DTO
        orderDto.setItems(cart.getItems().stream()
                .map(this::toOrderItem)
                .collect(Collectors.toList()));

        // 2. تحويل DTO إلى Entity
        Order order = mapper.map(orderDto);

        // 3. تعبئة بيانات الطلب من السلة
        order.setCartId(cart.getCartId());
        order.setUserId(cart.getUserId());
        order.setTotalAmount(cart.getTotalPrice());
        order.setStatus(OrderStatus.CREATED);

        // 🔥 الـحـل هـنـا: ربط العناصر بالطلب الأب برمجياً قبل الحفظ 🔥
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                item.setOrder(order); // إخبار العنصر من هو الطلب الخاص به (ملء الـ Foreign Key)
            }
        }

        // 4. حفظ الطلب (الآن سيتم حفظ الـ order_id في القاعدة بنجاح)
        order = orderRepository.save(order);

        // 5. خطوة إرسال الإيميل
        try {
            String customerEmail = order.getEmail(); // الأفضل جلب الإيميل من الـ Entity بعد التأكد منه
            if (customerEmail != null && !customerEmail.isEmpty()) {
                emailService.sendOrderConfirmation(customerEmail, order.getId().toString());
            }
        } catch (Exception e) {
            System.err.println("فشل إرسال البريد لكن تم حفظ الطلب بنجاح: " + e.getMessage());
        }

        // 6. مسح السلة بعد إنشاء الطلب
        cartClient.clearCart(cartId, authorization);

        // 7. تحويل النتيجة النهائية إلى DTO وإرجاعها
        return mapper.map(order);
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

    @Override
    public List<OrderDto> getOrders() {
        return orderRepository.findAll()
                .stream()
                .map(mapper::map)
                .collect(Collectors.toList());
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