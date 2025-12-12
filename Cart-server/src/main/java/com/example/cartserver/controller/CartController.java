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
@RequestMapping("/api/cart")
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

    @GetMapping("/cart")
    public ResponseEntity<CartDto> getOrCreateCart(
            @RequestParam(required = false) String cartId,
            @RequestParam(required = false) Long userId
    ) {

        // إذا المستخدم مسجّل دخول → تجاهل cartId واستخدم userId فقط
        if (userId != null) {
            return ResponseEntity.ok(cartService.getOrCreateCart(null, userId));
        }

        // Guest → يجب أن يكون معه cartId
        if (cartId == null) {
            // إذا لا يوجد cartId نُنشئ واحد جديد
            CartDto newGuestCart = cartService.getOrCreateCart(null, null);
            return ResponseEntity.ok(newGuestCart);
        }

        // إذا guest ومعه cartId → نرجع السلة
        return ResponseEntity.ok(cartService.getOrCreateCart(cartId, null));
    }


    @PostMapping("/addToCart")
    public ResponseEntity<CartDto> addItemToCart(
            @RequestParam(required = false) String cartId,
            @RequestParam(required = false) Long userId,
            @RequestParam String productBarcode,
            @RequestParam int quantity
    ) {
        CartDto cart = cartService.addItemToCart(cartId, userId, productBarcode, quantity);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/removeFromTheCart")
    public ResponseEntity<CartDto> removeItemFromCart(
            @RequestParam(required = false) String cartId,
            @RequestParam(required = false) Long userId,
            @RequestParam String productBarcode
    ) {
        CartDto cart = cartService.removeItemFromCart(cartId, userId, productBarcode);
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/updateTheCart")
    public ResponseEntity<CartDto> updateCart(
            @RequestParam(required = false) String cartId,
            @RequestParam(required = false) Long userId,
            @RequestParam String productBarcode,
            @RequestParam int quantity
    ) {
        CartDto cart = cartService.updateCart(cartId, userId,productBarcode , quantity);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/merge")
    public ResponseEntity<Void> mergeGuestCart(
            @RequestParam String guestCartId,
            @RequestParam Long userId
    ) {
        cartService.mergeGuestCartToUser(guestCartId, userId);
        return ResponseEntity.ok().build();
    }}
