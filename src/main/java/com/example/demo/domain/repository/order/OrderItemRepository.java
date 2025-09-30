package com.example.demo.domain.repository.order;

import com.example.demo.domain.model.order.OrderItem;
import java.util.List;
import java.util.UUID;

public interface OrderItemRepository {
  List<OrderItem> findByOrderId(UUID orderId);

  void save(OrderItem orderItem);

  void deleteByOrderId(UUID orderId);
}
