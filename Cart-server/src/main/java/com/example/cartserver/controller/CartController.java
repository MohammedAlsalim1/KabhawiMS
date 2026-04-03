package com.example.cartserver.controller;

import com.example.cartserver.data.dto.CartDto;
import com.example.cartserver.data.dto.CartItemDto;
import com.example.cartserver.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;


    // ✅ إنشاء أو جلب السلة (Guest أو User)
    @PostMapping("/get-or-create")
    public ResponseEntity<CartDto> getOrCreateCart(
            @RequestHeader(required = false) String cartId,
            @RequestHeader( required = false) String Authorization    ) {
        return ResponseEntity.ok(cartService.getOrCreateCart(cartId, Authorization));
    }

    @PostMapping("/add")
    public ResponseEntity<CartDto> addToCart(
            @RequestHeader (required = false) String cartId,
            @RequestHeader(required = false) String Authorization,
            @RequestBody CartItemDto cartItemDto
    ) {
        return ResponseEntity.ok(cartService.addItemToCart(
                cartId,
                Authorization,
                cartItemDto.getBarcode(),
                cartItemDto.getQuantity()
        ));
    }

    @PutMapping("/update")
    public ResponseEntity<CartDto> updateCart(
            @RequestHeader (required = false) String cartId,
            @RequestHeader(required = false) String Authorization,
            @RequestBody CartItemDto cartItemDto
    ) {
        return ResponseEntity.ok(cartService.updateCart(
                cartId,
                Authorization,
                cartItemDto.getBarcode(),
                cartItemDto.getQuantity()
        ));
    }

    @DeleteMapping("/remove")
    public ResponseEntity<CartDto> removeFromCart(
            @RequestHeader (required = false) String cartId,
            @RequestHeader(required = false) String Authorization,
            @RequestBody CartItemDto cartItemDto
    ) {
        return ResponseEntity.ok(cartService.removeItemFromCart(
                cartId,
                Authorization,
                cartItemDto.getBarcode()
        ));
    }

    @PostMapping("/merge")
    public ResponseEntity<CartDto> mergeGuestCart(
            @RequestHeader String cartId,
            @RequestHeader String Authorization
    ) {
        return ResponseEntity.ok(cartService.mergeGuestCartToUser(cartId, Authorization));
    }

    @GetMapping("/clear")
    public ResponseEntity<CartDto> clearCart(
            @RequestHeader (required = false) String cartId,
            @RequestHeader(required = false) String Authorization
    ) {
        return ResponseEntity.ok(cartService.clearCart(cartId, Authorization));
    }
}
