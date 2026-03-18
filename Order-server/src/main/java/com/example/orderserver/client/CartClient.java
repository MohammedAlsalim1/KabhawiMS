package com.example.orderserver.client;

import com.example.orderserver.data.dto.CartDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "cart-server")
public interface CartClient {

    @GetMapping("/api/cart")
    CartDto getCart(
            @RequestHeader(value = "X-USER-ID", required = false) Long userId,
            @RequestHeader(value = "X-CART-ID", required = false) String cartId
    );

    @DeleteMapping("/api/cart/clear")
    CartDto clearCart(
            @RequestHeader(value = "X-USER-ID", required = false) Long userId,
            @RequestHeader(value = "X-CART-ID", required = false) String cartId
    );

}