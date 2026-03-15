package com.example.cartserver.service;

import com.example.cartserver.data.dto.CartDto;
import com.example.cartserver.data.dto.CartItemDto;
import com.example.cartserver.data.entity.Cart;
import com.example.cartserver.data.entity.CartItem;
import com.example.cartserver.data.repository.CartItemRepository;
import com.example.cartserver.data.repository.CartRepository;
import com.example.cartserver.mapper.appMapper;
import com.example.cartserver.service.ex.NotExistException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final appMapper mapper;

    @Override
    public CartDto getOrCreateCart(String cartId, Long userId) {
        Cart cart;
        if (userId != null) {
            cart = cartRepository.findByUserId(userId)
                    .orElse(Cart.builder().userId(userId).build());
        } else {
            cart = cartRepository.findByCartId(cartId)
                    .orElse(Cart.builder().cartId(UUID.randomUUID().toString()).build());
        }
        cart = cartRepository.save(cart);
        return mapper.map(cart);
    }

    @Override
    @Transactional
    public CartDto addItemToCart(String cartId, Long userId, String productBarcode, int quantity) {
        Cart cart = (userId != null)
                ? cartRepository.findByUserId(userId).orElse(Cart.builder().userId(userId).build())
                : cartRepository.findByCartId(cartId).orElse(Cart.builder().cartId(UUID.randomUUID().toString()).build());

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(i -> i.getBarcode().equals(productBarcode))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + quantity);
        } else {
            CartItem newItem = CartItem.builder()
                    .barcode(productBarcode)
                    .quantity(quantity)
                    .cart(cart)
                    .build();
            cart.getItems().add(newItem);
        }

        cart = cartRepository.save(cart);
        return mapper.map(cart);
    }

    @Override
    @Transactional
    public CartDto removeItemFromCart(String cartId, Long userId, String productBarcode) {
        Cart cart = (userId != null)
                ? cartRepository.findByUserId(userId)
                .orElseThrow(() -> new NotExistException("Cart not found for userId: " + userId))
                : cartRepository.findByCartId(cartId)
                .orElseThrow(() -> new NotExistException("Cart not found for cartId: " + cartId));

        cart.getItems().removeIf(item -> item.getBarcode().equals(productBarcode));

        cart = cartRepository.save(cart);
        return mapper.map(cart);
    }


    @Override
    @Transactional
    public void mergeGuestCartToUser(String guestCartId, Long userId) {
        Cart guestCart = cartRepository.findByCartId(guestCartId).orElseThrow(() -> new NotExistException("Cart not found for cartId: " + guestCartId));
        if (guestCart == null) return;

        Cart userCart = cartRepository.findByUserId(userId)
                .orElse(Cart.builder().userId(userId).build());

        for (CartItem guestItem : guestCart.getItems()) {
            Optional<CartItem> existing = userCart.getItems().stream()
                    .filter(i -> i.getBarcode().equals(guestItem.getBarcode()))
                    .findFirst();
            if (existing.isPresent()) {
                existing.get().setQuantity(existing.get().getQuantity() + guestItem.getQuantity());
            } else {
                CartItem newItem = CartItem.builder()
                        .barcode(guestItem.getBarcode())
                        .quantity(guestItem.getQuantity())
                        .cart(userCart)
                        .build();
                userCart.getItems().add(newItem);
            }
        }
        cartRepository.save(userCart);
        cartRepository.delete(guestCart);
    }

    @Override
    @Transactional
    public CartDto updateCart(String cartId, Long userId, String productBarcode, int newQuantity) {
        // جلب السلة سواء كانت للمستخدم المسجل أو ضيف
        Cart cart = (userId != null)
                ? cartRepository.findByUserId(userId)
                .orElseThrow(() -> new NotExistException("Cart not found for userId: " + userId))
                : cartRepository.findByCartId(cartId)
                .orElseThrow(() -> new NotExistException("Cart not found for cartId: " + cartId));

        // البحث عن العنصر المطلوب تحديثه
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getBarcode().equals(productBarcode))
                .findFirst()
                .orElseThrow(() -> new NotExistException("Product not found in cart: " + productBarcode));

        // تحديث الكمية
        item.setQuantity(newQuantity);

        // حفظ السلة المحدثة
        cart = cartRepository.save(cart);

        return mapper.map(cart);
    }

    @Override
    public CartDto clearCart(String cartId,Long userId) {
        Cart cart = (userId != null)
                ? cartRepository.findByUserId(userId)
                .orElseThrow(() -> new NotExistException("Cart not found for userId: " + userId))
                : cartRepository.findByCartId(cartId)
                .orElseThrow(() -> new NotExistException("Cart not found for cartId: " + cartId));

        cart.getItems().clear();

        cartRepository.save(cart);

        return mapper.map(cart);
    }
}