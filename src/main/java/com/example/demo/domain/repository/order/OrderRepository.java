package com.example.demo.domain.repository.order;

import com.example.demo.domain.model.order.Order;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
  List<Order> findAll();

  Optional<Order> findById(UUID id);

  void insert(Order order);

  void update(Order order);

  void deleteById(UUID id);
}
