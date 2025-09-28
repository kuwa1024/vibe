package com.example.demo.application.dto.customer;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.demo.domain.model.customer.Customer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {
    private UUID id;
    private String name;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CustomerDto from(Customer customer) {
        return new CustomerDto(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getCreatedAt(),
                customer.getUpdatedAt());
    }
}
