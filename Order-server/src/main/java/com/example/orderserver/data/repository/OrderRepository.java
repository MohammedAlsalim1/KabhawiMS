package com.example.orderserver.data.repository;

import com.example.orderserver.data.entity.Order;
import com.example.orderserver.data.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByPhoneNumber(String phoneNumber);
}
