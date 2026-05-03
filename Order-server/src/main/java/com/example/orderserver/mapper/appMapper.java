package com.example.orderserver.mapper;

import com.example.orderserver.data.dto.OrderDto;
import com.example.orderserver.data.dto.OrderItemDto;
import com.example.orderserver.data.entity.Order;
import com.example.orderserver.data.entity.OrderItem;
import org.mapstruct.Mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")

public interface appMapper {
    OrderDto map(Order order);
    Order map(OrderDto orderDto);
    OrderItemDto map(OrderItem orderItem);
    default List<OrderItemDto> map(List<OrderItem> items) {
        if (items == null) {
            return new ArrayList<>();
        }
        return items.stream()
                .map(this::map)
                .collect(Collectors.toList());
    }
}
