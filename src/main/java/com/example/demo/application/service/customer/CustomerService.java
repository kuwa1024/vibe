package com.example.demo.application.service.customer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.application.dto.customer.CustomerDto;
import com.example.demo.domain.model.customer.Customer;
import com.example.demo.domain.repository.customer.CustomerRepository;
import com.example.demo.presentation.request.customer.CreateCustomerRequest;
import com.example.demo.presentation.request.customer.UpdateCustomerRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public List<CustomerDto> findAllCustomers() {
        return customerRepository.findAll().stream()
                .map(CustomerDto::from)
                .collect(Collectors.toList());
    }

    public CustomerDto findCustomerById(UUID id) {
        return customerRepository.findById(id)
                .map(CustomerDto::from)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + id));
    }

    @Transactional
    public CustomerDto createCustomer(CreateCustomerRequest request) {
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());

        customerRepository.save(customer);
        return CustomerDto.from(customer);
    }

    @Transactional
    public CustomerDto updateCustomer(UUID id, UpdateCustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + id));

        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setUpdatedAt(LocalDateTime.now());

        customerRepository.save(customer);
        return CustomerDto.from(customer);
    }

    @Transactional
    public void deleteCustomer(UUID id) {
        customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + id));
        customerRepository.deleteById(id);
    }
}
