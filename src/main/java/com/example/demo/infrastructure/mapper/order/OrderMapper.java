package com.example.demo.infrastructure.mapper.order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.ibatis.annotations.Mapper;

import com.example.demo.domain.model.order.Order;

@Mapper
public interface OrderMapper {
    List<Order> findAll();

    Optional<Order> findById(UUID id);

    void insert(Order order);

    void update(Order order);

    void deleteById(UUID id);
}
