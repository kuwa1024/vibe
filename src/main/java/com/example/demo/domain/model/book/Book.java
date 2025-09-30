package com.example.demo.domain.model.book;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {
  private String isbn;
  private String title;
  private Integer price;
  private Integer stock;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
