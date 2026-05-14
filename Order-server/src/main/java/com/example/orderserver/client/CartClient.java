package com.example.orderserver.client;

import com.example.orderserver.data.dto.CartDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

// 👈 التعديل هنا: اسم الخدمة بأحرف صغيرة، وإضافة path لتعويض مسار الـ API
@FeignClient(name = "cart-server", path = "/api/cart")
public interface CartClient {

    @PostMapping("/get-or-create")
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