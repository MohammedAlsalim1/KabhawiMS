package com.example.cartserver.controller;

import com.example.cartserver.data.dto.CartDto;
import com.example.cartserver.data.entity.Login;
import com.example.cartserver.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class CartController {

    private final CartService cartService;
    private final RestTemplate restTemplate;

    private Login user(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Login> loginResponse
                = restTemplate.exchange("http://guardWay/parse-token", HttpMethod.GET, entity, Login.class);
        Login login = loginResponse.getBody();
        return login;
    }

    // الحصول على السلة أو إنشاء واحدة جديدة
    @GetMapping("/cart")
    public ResponseEntity<CartDto> getOrCreateCart(
            @RequestParam(required = false) String cartId,
            @RequestParam(required = false) Long userId
    ) {
        CartDto cart = cartService.getOrCreateCart(cartId, userId);
        return ResponseEntity.ok(cart);
    }

    // إضافة عنصر إلى السلة
    @PostMapping("/add")
    public ResponseEntity<CartDto> addItemToCart(
            @RequestParam(required = false) String cartId,
            @RequestParam(required = false) Long userId,
            @RequestParam Long productId,
            @RequestParam int quantity
    ) {
        CartDto cart = cartService.addItemToCart(cartId, userId, productId, quantity);
        return ResponseEntity.ok(cart);
    }

    // إزالة عنصر من السلة
    @DeleteMapping("/remove")
    public ResponseEntity<CartDto> removeItemFromCart(
            @RequestParam(required = false) String cartId,
            @RequestParam(required = false) Long userId,
            @RequestParam Long productId
    ) {
        CartDto cart = cartService.removeItemFromCart(cartId, userId, productId);
        return ResponseEntity.ok(cart);
    }

    // تحديث كمية عنصر في السلة
    @PutMapping("/update")
    public ResponseEntity<CartDto> updateCart(
            @RequestParam(required = false) String cartId,
            @RequestParam(required = false) Long userId,
            @RequestParam Long productId,
            @RequestParam int quantity
    ) {
        CartDto cart = cartService.updateCart(cartId, userId, productId, quantity);
        return ResponseEntity.ok(cart);
    }

    // دمج سلة الضيف مع المستخدم عند تسجيل الدخول
    @PostMapping("/merge")
    public ResponseEntity<Void> mergeGuestCart(
            @RequestParam String guestCartId,
            @RequestParam Long userId
    ) {
        cartService.mergeGuestCartToUser(guestCartId, userId);
        return ResponseEntity.ok().build();
    }}
