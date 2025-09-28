package com.example.demo.domain.repository.order;

import java.util.List;
import java.util.UUID;

import com.example.demo.domain.model.order.OrderItem;

public interface OrderItemRepository {
    List<OrderItem> findByOrderId(UUID orderId);

    void save(OrderItem orderItem);

    void deleteByOrderId(UUID orderId);
}
