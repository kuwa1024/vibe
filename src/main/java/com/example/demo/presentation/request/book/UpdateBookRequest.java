package com.example.demo.presentation.request.book;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateBookRequest {
  @NotEmpty private String title;

  @NotNull
  @Min(0)
  private Integer price;

  @NotNull
  @Min(0)
  private Integer stock;
}
