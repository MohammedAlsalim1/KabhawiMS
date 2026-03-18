package com.example.orderserver.controller;

import com.example.orderserver.data.dto.OrderDto;
import com.example.orderserver.data.entity.OrderStatus;
import com.example.orderserver.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // إنشاء طلب
    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@RequestBody OrderDto orderDto) {
        OrderDto created = orderService.createOrder(orderDto);
        return ResponseEntity.ok(created);
    }

    // جلب طلب حسب الـ ID
    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long id) {
        OrderDto order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    // جلب جميع الطلبات لمستخدم معين
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDto>> getOrdersByUserId(@PathVariable Long userId) {
        List<OrderDto> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    // جلب الطلبات حسب الحالة
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderDto>> getOrdersByStatus(@PathVariable OrderStatus status) {
        List<OrderDto> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    // جلب الطلبات حسب رقم الهاتف
    @GetMapping("/phone/{phoneNumber}")
    public ResponseEntity<List<OrderDto>> getOrdersByPhoneNumber(@PathVariable String phoneNumber) {
        List<OrderDto> orders = orderService.getOrdersByPhoneNumber(phoneNumber);
        return ResponseEntity.ok(orders);
    }

    // تحديث حالة الطلب
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderDto> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status
    ) {
        OrderDto updated = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(updated);
    }

    // حذف طلب
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}