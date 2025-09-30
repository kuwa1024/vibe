package com.example.demo.application.dto.book;

import com.example.demo.domain.model.book.Book;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class BookDto {
  private String isbn;
  private String title;
  private Integer price;
  private Integer stock;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static BookDto from(Book book) {
    BookDto dto = new BookDto();
    dto.setIsbn(book.getIsbn());
    dto.setTitle(book.getTitle());
    dto.setPrice(book.getPrice());
    dto.setStock(book.getStock());
    dto.setCreatedAt(book.getCreatedAt());
    dto.setUpdatedAt(book.getUpdatedAt());
    return dto;
  }
}
