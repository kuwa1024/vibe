package com.example.demo.infrastructure.mapper.order;

import com.example.demo.domain.model.order.Order;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper {
  List<Order> findAll();

  Optional<Order> findById(UUID id);

  void insert(Order order);

  void update(Order order);

  void deleteById(UUID id);
}
