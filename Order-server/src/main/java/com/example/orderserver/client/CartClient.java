package com.example.orderserver.client;

import com.example.orderserver.data.dto.CartDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "Cart-server", url = "http://localhost:8081/api/cart") // عدّل الـ URL حسب خدمة الكارت
public interface CartClient {

    @PostMapping ("/get-or-create")
    CartDto getCart(

            @RequestHeader(value = "cartId", required = false) String cartId,
            @RequestHeader(value = "Authorization", required = false) String authorization
    );

    @GetMapping("/clear")
    CartDto clearCart(
            @RequestHeader(value = "cartId", required = false) String cartId,
            @RequestHeader(value = "Authorization", required = false) String authorization
    );

}