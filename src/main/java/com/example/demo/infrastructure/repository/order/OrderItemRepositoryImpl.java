package com.example.demo.infrastructure.repository.order;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.example.demo.domain.model.order.OrderItem;
import com.example.demo.domain.repository.order.OrderItemRepository;
import com.example.demo.infrastructure.mapper.order.OrderItemMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderItemRepositoryImpl implements OrderItemRepository {

    private final OrderItemMapper orderItemMapper;

    @Override
    public List<OrderItem> findByOrderId(UUID orderId) {
        return orderItemMapper.findByOrderId(orderId);
    }

    @Override
    public void save(OrderItem orderItem) {
        orderItemMapper.save(orderItem);
    }

    @Override
    public void deleteByOrderId(UUID orderId) {
        orderItemMapper.deleteByOrderId(orderId);
    }
}
