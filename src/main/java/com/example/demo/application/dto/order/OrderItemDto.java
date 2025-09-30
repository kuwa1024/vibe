package com.example.demo.application.dto.order;

import com.example.demo.domain.model.order.OrderItem;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {
  private Long id;
  private UUID orderId;
  private String bookIsbn;
  private Integer quantity;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static OrderItemDto from(OrderItem orderItem) {
    return new OrderItemDto(
        orderItem.getId(),
        orderItem.getOrderId(),
        orderItem.getBookIsbn(),
        orderItem.getQuantity(),
        orderItem.getCreatedAt(),
        orderItem.getUpdatedAt());
  }
}
