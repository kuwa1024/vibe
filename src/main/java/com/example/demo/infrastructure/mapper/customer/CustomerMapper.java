package com.example.demo.infrastructure.mapper.customer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.ibatis.annotations.Mapper;

import com.example.demo.domain.model.customer.Customer;

@Mapper
public interface CustomerMapper {
    List<Customer> findAll();

    Optional<Customer> findById(UUID id);

    void save(Customer customer);

    void deleteById(UUID id);
}
