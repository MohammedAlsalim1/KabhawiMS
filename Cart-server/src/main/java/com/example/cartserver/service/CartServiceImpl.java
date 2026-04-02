package com.example.cartserver.service;

import com.example.cartserver.client.ProductClient;
import com.example.cartserver.client.UserClient;
import com.example.cartserver.data.dto.CartDto;
import com.example.cartserver.data.dto.productDto;
import com.example.cartserver.data.entity.Cart;
import com.example.cartserver.data.entity.CartItem;
import com.example.cartserver.data.repository.CartRepository;
import com.example.cartserver.mapper.appMapper;
import com.example.cartserver.service.ex.NotExistException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final appMapper mapper;
    private final ProductClient productClient;

    @Override
    public CartDto getOrCreateCart(String cartId, String userId) {
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
    public CartDto addItemToCart(String cartId, String userId, String productBarcode, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        Cart cart = (userId != null)
                ? cartRepository.findByUserId(userId)
                .orElse(Cart.builder().userId(userId).build())
                : cartRepository.findByCartId(cartId)
                .orElse(Cart.builder().cartId(UUID.randomUUID().toString()).build());

        productDto product = productClient.getProduct(productBarcode);

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(i -> i.getBarcode().equals(productBarcode))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            item.setPrice(product.getPrice());
            item.setTotalPrice(item.getPrice() * item.getQuantity());
        } else {
            CartItem newItem = CartItem.builder()
                    .barcode(productBarcode)
                    .quantity(quantity)
                    .price(product.getPrice())
                    .totalPrice(product.getPrice() * quantity)
                    .cart(cart)
                    .build();
            cart.getItems().add(newItem);
        }

        recalculateCartTotal(cart);
        cart = cartRepository.save(cart);
        CartDto map = mapper.map(cart);
        return map;
    }

    @Override
    @Transactional
    public CartDto removeItemFromCart(String cartId, String userId, String productBarcode) {
        Cart cart = (userId != null)
                ? cartRepository.findByUserId(userId)
                .orElseThrow(() -> new NotExistException("Cart not found for userId: " + userId))
                : cartRepository.findByCartId(cartId)
                .orElseThrow(() -> new NotExistException("Cart not found for cartId: " + cartId));

        CartItem itemToRemove = cart.getItems().stream()
                .filter(item -> item.getBarcode().equals(productBarcode))
                .findFirst()
                .orElseThrow(() -> new NotExistException("Product not found in cart: " + productBarcode));

        cart.getItems().remove(itemToRemove);

        recalculateCartTotal(cart);
        cart = cartRepository.save(cart);
        return mapper.map(cart);
    }

    @Override
    @Transactional
    public void mergeGuestCartToUser(String guestCartId, String userId) {
        Cart guestCart = cartRepository.findByCartId(guestCartId)
                .orElseThrow(() -> new NotExistException("Cart not found for cartId: " + guestCartId));

        Cart userCart = cartRepository.findByUserId(userId)
                .orElse(Cart.builder().userId(userId).build());

        for (CartItem guestItem : guestCart.getItems()) {
            Optional<CartItem> existing = userCart.getItems().stream()
                    .filter(i -> i.getBarcode().equals(guestItem.getBarcode()))
                    .findFirst();

            if (existing.isPresent()) {
                CartItem item = existing.get();
                item.setQuantity(item.getQuantity() + guestItem.getQuantity());
                item.setPrice(guestItem.getPrice());
                item.setTotalPrice(item.getPrice() * item.getQuantity());
            } else {
                CartItem newItem = CartItem.builder()
                        .barcode(guestItem.getBarcode())
                        .quantity(guestItem.getQuantity())
                        .price(guestItem.getPrice())
                        .totalPrice(guestItem.getTotalPrice())
                        .cart(userCart)
                        .build();
                userCart.getItems().add(newItem);
            }
        }

        recalculateCartTotal(userCart);
        cartRepository.save(userCart);
        cartRepository.delete(guestCart);
    }

    @Override
    @Transactional
    public CartDto updateCart(String cartId, String userId, String productBarcode, int newQuantity) {
        if (newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        Cart cart = (userId != null)
                ? cartRepository.findByUserId(userId)
                .orElseThrow(() -> new NotExistException("Cart not found for userId: " + userId))
                : cartRepository.findByCartId(cartId)
                .orElseThrow(() -> new NotExistException("Cart not found for cartId: " + cartId));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getBarcode().equals(productBarcode))
                .findFirst()
                .orElseThrow(() -> new NotExistException("Product not found in cart: " + productBarcode));

        item.setQuantity(newQuantity);
        item.setTotalPrice(item.getPrice() * newQuantity);

        recalculateCartTotal(cart);
        cart = cartRepository.save(cart);

        return mapper.map(cart);
    }

    @Override
    @Transactional
    public CartDto clearCart(String cartId, String userId) {
        Cart cart = (userId != null)
                ? cartRepository.findByUserId(userId)
                .orElseThrow(() -> new NotExistException("Cart not found for userId: " + userId))
                : cartRepository.findByCartId(cartId)
                .orElseThrow(() -> new NotExistException("Cart not found for cartId: " + cartId));

        cart.getItems().clear();
        cart.setTotalPrice(0);

        cartRepository.save(cart);
        return mapper.map(cart);
    }

    private void recalculateCartTotal(Cart cart) {
        double total = cart.getItems().stream()
                .mapToDouble(CartItem::getTotalPrice)
                .sum();
        cart.setTotalPrice(total);
    }
}