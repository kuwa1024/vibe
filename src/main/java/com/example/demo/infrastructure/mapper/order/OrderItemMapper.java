package com.example.demo.infrastructure.mapper.order;

import com.example.demo.domain.model.order.OrderItem;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderItemMapper {
  List<OrderItem> findByOrderId(UUID orderId);

  void save(OrderItem orderItem);

  void deleteByOrderId(UUID orderId);
}
