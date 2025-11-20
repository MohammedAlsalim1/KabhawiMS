package com.example.cartserver.service;

import com.example.cartserver.data.dto.CartDto;

public interface CartService {
    CartDto getOrCreateCart(String cartId, Long userId);
    CartDto addItemToCart(String cartId, Long userId, Long productId, int quantity);
    CartDto removeItemFromCart(String cartId, Long userId, Long productId);
    CartDto updateCart(String cartId, Long userId, Long productId, int quantity);
    void mergeGuestCartToUser(String guestCartId, Long userId);
}

