package com.example.demo.application.dto.order;

import com.example.demo.domain.model.order.Order;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
  private UUID id;
  private UUID customerId;
  private LocalDateTime orderDatetime;
  private String status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private List<OrderItemDto> orderItems;

  public static OrderDto from(Order order) {
    List<OrderItemDto> itemDtos =
        order.getOrderItems().stream().map(OrderItemDto::from).collect(Collectors.toList());
    return new OrderDto(
        order.getId(),
        order.getCustomerId(),
        order.getOrderDatetime(),
        order.getStatus(),
        order.getCreatedAt(),
        order.getUpdatedAt(),
        itemDtos);
  }
}
