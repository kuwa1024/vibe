package com.example.demo.presentation.request.order;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateOrderRequest {
    @NotNull
    private UUID customerId;

    @Valid
    @Size(min = 1, message = "Order must contain at least one item")
    private List<CreateOrderItemRequest> orderItems;
}
