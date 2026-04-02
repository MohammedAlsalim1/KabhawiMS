package com.example.cartserver.controller;

import com.example.cartserver.client.UserClient;
import com.example.cartserver.data.dto.CartDto;
import com.example.cartserver.data.dto.CartItemDto;
import com.example.cartserver.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final UserClient userClient;


    // ✅ إنشاء أو جلب السلة (Guest أو User)
    @PostMapping("/get-or-create")
    public ResponseEntity<CartDto> getOrCreateCart(
            @RequestHeader(required = false) String cartId,@RequestHeader(required = false) String token
    ) {

        String userId = userClient.getUser(token).getUserId();
        CartDto cart = cartService.getOrCreateCart(
                cartId,
                userId
        );
        return ResponseEntity.ok(cart);
    }

    // ✅ إضافة منتج للسلة (باستخدام BARCODE)
    @PostMapping("/add")
    public ResponseEntity<CartDto> addToCart(
            @RequestHeader String cartId,
            @RequestHeader(required = false) String token,
            @RequestBody CartItemDto cartItemDto
    ) {
        String userId = userClient.getUser(token).getUserId();
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
            @RequestHeader(required = false) String token,
            @RequestBody CartItemDto cartItemDto
    ) {
        String userId = userClient.getUser(token).getUserId();
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
            @RequestHeader(required = false) String token,
            @RequestBody CartItemDto cartItemDto
    ) {
        String userId = userClient.getUser(token).getUserId();
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
            @RequestHeader String guestCartId,
            @RequestHeader String token
    ) {
        String userId = userClient.getUser(token).getUserId();
        cartService.mergeGuestCartToUser(guestCartId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/clear")
    public ResponseEntity<CartDto> clearCart( @RequestHeader String cartId,
                                              @RequestHeader(required = false) String token) {
        String userId = userClient.getUser(token).getUserId();
        CartDto cartDto = cartService.clearCart(cartId, userId);
        return ResponseEntity.ok(cartDto);

    }
}
