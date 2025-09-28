package com.example.demo.infrastructure.repository.order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.example.demo.domain.model.order.Order;
import com.example.demo.domain.repository.order.OrderRepository;
import com.example.demo.infrastructure.mapper.order.OrderMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderMapper orderMapper;

    @Override
    public List<Order> findAll() {
        return orderMapper.findAll();
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return orderMapper.findById(id);
    }

    @Override
    public void insert(Order order) {
        orderMapper.insert(order);
    }

    @Override
    public void update(Order order) {
        orderMapper.update(order);
    }

    @Override
    public void deleteById(UUID id) {
        orderMapper.deleteById(id);
    }
}
