package com.example.cartserver.mapper;

import com.example.cartserver.data.dto.CartDto;
import com.example.cartserver.data.dto.CartItemDto;
import com.example.cartserver.data.entity.Cart;
import com.example.cartserver.data.entity.CartItem;
import org.mapstruct.Mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface appMapper {
    Cart map (CartDto cartDto);
    CartDto map (Cart cart);
    CartItem map (CartItemDto cartItemDto);
    CartItemDto map (CartItem cartItem);
    default List<CartItemDto> map(List<CartItem> items) {
        if (items == null) {
            return new ArrayList<>();
        }
        return items.stream()
                .map(this::map)
                .collect(Collectors.toList());
    }
}
