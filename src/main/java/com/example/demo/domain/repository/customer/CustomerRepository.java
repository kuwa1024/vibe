package com.example.demo.domain.repository.customer;

import com.example.demo.domain.model.customer.Customer;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository {
  List<Customer> findAll();

  Optional<Customer> findById(UUID id);

  void save(Customer customer);

  void deleteById(UUID id);
}
