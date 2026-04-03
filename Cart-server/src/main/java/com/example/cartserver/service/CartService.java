package com.example.cartserver.service;

import com.example.cartserver.data.dto.CartDto;

public interface CartService {
    CartDto getOrCreateCart(String cartId, String userId);
    CartDto addItemToCart(String cartId, String userId, String productBarcode, int quantity);
    CartDto removeItemFromCart(String cartId, String userId, String productBarcode);
    CartDto updateCart(String cartId, String userId, String productBarcode, int quantity);
    CartDto clearCart(String cartId, String userId);
    CartDto mergeGuestCartToUser(String guestCartId, String userId);
}

