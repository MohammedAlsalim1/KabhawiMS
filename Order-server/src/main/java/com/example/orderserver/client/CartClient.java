package com.example.orderserver.client;

import com.example.cartserver.data.dto.CartDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "cart-service")
public interface CartClient {

    @GetMapping("/api/cart/get-or-create")
    CartDto getCart(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestHeader("X-CART-ID") String cartId
    );

    @DeleteMapping("/api/cart/clear")
    void clearCart(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestHeader("X-CART-ID") String cartId
    );
}