package com.example.demo.presentation.request.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateOrderItemRequest {
  @NotBlank private String bookIsbn;

  @Min(1)
  private Integer quantity;
}
