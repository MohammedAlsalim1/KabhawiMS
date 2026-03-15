package com.example.cartserver.service;

import com.example.cartserver.data.dto.CartDto;

public interface CartService {
    CartDto getOrCreateCart(String cartId, Long userId);
    CartDto addItemToCart(String cartId, Long userId, String productBarcode, int quantity);
    CartDto removeItemFromCart(String cartId, Long userId, String productBarcode);
    CartDto updateCart(String cartId, Long userId, String productBarcode, int quantity);
    CartDto clearCart(String cartId, Long userId);
    void mergeGuestCartToUser(String guestCartId, Long userId);
}

