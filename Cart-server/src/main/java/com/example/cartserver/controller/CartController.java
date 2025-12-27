package com.example.cartserver.controller;

import com.example.cartserver.data.dto.CartDto;
import com.example.cartserver.data.dto.CartItemDto;
import com.example.cartserver.data.entity.Login;
import com.example.cartserver.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final RestTemplate restTemplate;

    // ✅ إنشاء أو جلب السلة (Guest أو User)
    @PostMapping("/get-or-create")
    public ResponseEntity<CartDto> getOrCreateCart(
            @RequestBody CartDto cartDto
    ) {
        CartDto cart = cartService.getOrCreateCart(
                cartDto.getCartId(),
                cartDto.getUserId()
        );
        return ResponseEntity.ok(cart);
    }

    // ✅ إضافة منتج للسلة (باستخدام BARCODE)
    @PostMapping("/add")
    public ResponseEntity<CartDto> addToCart(
            @RequestBody CartItemDto cartItemDto
    ) {
        CartDto cart = cartService.addItemToCart(
                cartItemDto.getCartId(),
                cartItemDto.getUserId(),
                cartItemDto.getBarcode(),
                cartItemDto.getQuantity()
        );
        return ResponseEntity.ok(cart);
    }

    // ✅ تحديث كمية منتج
    @PutMapping("/update")
    public ResponseEntity<CartDto> updateCart(
            @RequestBody CartItemDto cartItemDto
    ) {
        CartDto cart = cartService.updateCart(
                cartItemDto.getCartId(),
                cartItemDto.getUserId(),
                cartItemDto.getBarcode(),
                cartItemDto.getQuantity()
        );
        return ResponseEntity.ok(cart);
    }

    // ✅ حذف منتج من السلة
    @DeleteMapping("/remove")
    public ResponseEntity<CartDto> removeFromCart(
            @RequestBody CartItemDto cartItemDto
    ) {
        CartDto cart = cartService.removeItemFromCart(
                cartItemDto.getCartId(),
                cartItemDto.getUserId(),
                cartItemDto.getBarcode()
        );
        return ResponseEntity.ok(cart);
    }

    // ✅ دمج سلة الزائر بعد تسجيل الدخول
    @PostMapping("/merge")
    public ResponseEntity<Void> mergeGuestCart(
            @RequestParam String guestCartId,
            @RequestParam Long userId
    ) {
        cartService.mergeGuestCartToUser(guestCartId, userId);
        return ResponseEntity.ok().build();
    }
}
