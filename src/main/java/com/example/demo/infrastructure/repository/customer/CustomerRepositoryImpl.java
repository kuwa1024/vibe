package com.example.demo.infrastructure.repository.customer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.example.demo.domain.model.customer.Customer;
import com.example.demo.domain.repository.customer.CustomerRepository;
import com.example.demo.infrastructure.mapper.customer.CustomerMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CustomerRepositoryImpl implements CustomerRepository {

    private final CustomerMapper customerMapper;

    @Override
    public List<Customer> findAll() {
        return customerMapper.findAll();
    }

    @Override
    public Optional<Customer> findById(UUID id) {
        return customerMapper.findById(id);
    }

    @Override
    public void save(Customer customer) {
        customerMapper.save(customer);
    }

    @Override
    public void deleteById(UUID id) {
        customerMapper.deleteById(id);
    }
}
