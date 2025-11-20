package com.example.cartserver.mapper;

import com.example.cartserver.data.dto.CartDto;
import com.example.cartserver.data.dto.CartItemDto;
import com.example.cartserver.data.entity.Cart;
import com.example.cartserver.data.entity.CartItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface appMapper {
    Cart map (CartDto cartDto);
    CartDto map (Cart cart);
    CartItem map (CartItemDto cartItemDto);
    CartItemDto map (CartItem cartItem);
}
