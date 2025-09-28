package com.example.demo.presentation.request.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateCustomerRequest {
    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;
}
