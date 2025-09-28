package com.example.demo.domain.model.order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private UUID id;
    private UUID customerId;
    private LocalDateTime orderDatetime;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItem> orderItems;

    public enum OrderStatus {
        PENDING, SHIPPED, CANCELLED
    }
}
