package com.example.orderserver.client;

import com.example.orderserver.data.dto.CartDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "Cart-server", url = "http://localhost:8081/api/cart") // عدّل الـ URL حسب خدمة الكارت
public interface CartClient {

    @PostMapping ("/get-or-create")
    CartDto getCart(
            @RequestHeader(value = "X-USER-ID", required = false) Long userId,
            @RequestHeader(value = "X-CART-ID", required = false) String cartId
    );

    @GetMapping("/clear")
    CartDto clearCart(
            @RequestHeader(value = "X-USER-ID", required = false) Long userId,
            @RequestHeader(value = "X-CART-ID", required = false) String cartId
    );

}