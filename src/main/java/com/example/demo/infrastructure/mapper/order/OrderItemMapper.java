package com.example.demo.infrastructure.mapper.order;

import java.util.List;
import java.util.UUID;

import org.apache.ibatis.annotations.Mapper;

import com.example.demo.domain.model.order.OrderItem;

@Mapper
public interface OrderItemMapper {
    List<OrderItem> findByOrderId(UUID orderId);

    void save(OrderItem orderItem);

    void deleteByOrderId(UUID orderId);
}
