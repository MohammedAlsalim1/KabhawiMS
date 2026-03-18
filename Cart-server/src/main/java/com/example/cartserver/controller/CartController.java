package com.example.cartserver.controller;

import com.example.cartserver.data.dto.CartDto;
import com.example.cartserver.data.dto.CartItemDto;
import com.example.cartserver.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

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
            @RequestHeader String cartId,
            @RequestHeader(required = false) Long userId,
            @RequestBody CartItemDto cartItemDto
    ) {
        CartDto cart = cartService.addItemToCart(
                cartId,
                userId,
                cartItemDto.getBarcode(),
                cartItemDto.getQuantity()
        );
        return ResponseEntity.ok(cart);
    }

    // ✅ تحديث كمية منتج
    @PutMapping("/update")
    public ResponseEntity<CartDto> updateCart(
            @RequestHeader String cartId,
            @RequestHeader(required = false) Long userId,
            @RequestBody CartItemDto cartItemDto
    ) {
        CartDto cart = cartService.updateCart(
                cartId,
                userId,
                cartItemDto.getBarcode(),
                cartItemDto.getQuantity()
        );
        return ResponseEntity.ok(cart);
    }

    // ✅ حذف منتج من السلة
    @DeleteMapping("/remove")
    public ResponseEntity<CartDto> removeFromCart(
            @RequestHeader String cartId,
            @RequestHeader(required = false) Long userId,
            @RequestBody CartItemDto cartItemDto
    ) {
        CartDto cart = cartService.removeItemFromCart(
                cartId,
                userId,
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

    @GetMapping("/clear")
    public ResponseEntity<CartDto> clearCart( @RequestHeader String cartId,
                                              @RequestHeader(required = false) Long userId) {
        CartDto cartDto = cartService.clearCart(cartId, userId);
        return ResponseEntity.ok(cartDto);

    }
}
