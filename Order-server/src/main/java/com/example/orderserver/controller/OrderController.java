package com.example.orderserver.controller;

import com.example.orderserver.data.dto.OrderDto;
import com.example.orderserver.data.entity.OrderStatus;
import com.example.orderserver.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // إنشاء طلب
    @PostMapping("/checkout")
    public ResponseEntity<OrderDto> createOrder(@RequestBody OrderDto orderDto,@RequestHeader String authorization, @RequestHeader String cartId) {
        OrderDto created = orderService.createOrder(orderDto,authorization,cartId);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/getOrders")
    public ResponseEntity<List<OrderDto>> getOrders() {

       List<OrderDto> orders = orderService.getOrders();
        return ResponseEntity.ok(orders);
    }

    // جلب طلب حسب الـ ID
    @GetMapping("/trackbyOrderNumber")
    public ResponseEntity<OrderDto> getOrderById(@RequestParam Long id) {
        OrderDto order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    // جلب الطلبات حسب الحالة
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderDto>> getOrdersByStatus(@PathVariable OrderStatus status) {
        List<OrderDto> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    // جلب الطلبات حسب رقم الهاتف
    @GetMapping("/trackByPhoneNumber")
    public ResponseEntity<List<OrderDto>> getOrdersByPhoneNumber(@RequestParam String phoneNumber) {
        List<OrderDto> orders = orderService.getOrdersByPhoneNumber(phoneNumber);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/trackByEmail")
    public ResponseEntity<List<OrderDto>> getOrdersByEmail(@RequestParam String email) {
        List<OrderDto> orders = orderService.getOrdersByEmail(email);
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