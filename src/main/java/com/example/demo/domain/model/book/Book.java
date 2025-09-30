package com.example.demo.domain.model.book;

import com.example.demo.domain.exception.InsufficientStockException;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Book {
  private String isbn;
  private String title;
  private Integer price;
  private Integer stock;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static Book create(String isbn, String title, Integer price, Integer initialStock) {
    if (initialStock < 0) {
      throw new IllegalArgumentException("初期在庫数は0以上である必要があります");
    }
    if (price < 0) {
      throw new IllegalArgumentException("価格は0以上である必要があります");
    }
    if (isbn == null || isbn.trim().isEmpty()) {
      throw new IllegalArgumentException("ISBNは必須です");
    }
    if (title == null || title.trim().isEmpty()) {
      throw new IllegalArgumentException("タイトルは必須です");
    }

    Book book = new Book();
    book.setIsbn(isbn);
    book.setTitle(title);
    book.setPrice(price);
    book.setStock(initialStock);

    LocalDateTime now = LocalDateTime.now();
    book.setCreatedAt(now);
    book.setUpdatedAt(now);

    return book;
  }

  public void validateStock(int requestedQuantity) {
    if (requestedQuantity <= 0) {
      throw new IllegalArgumentException("注文数量は1以上である必要があります");
    }
    if (this.stock < requestedQuantity) {
      throw new InsufficientStockException(
          String.format(
              "在庫が不足しています（要求数: %d, 在庫数: %d, 書籍: %s [%s]）",
              requestedQuantity, this.stock, this.title, this.isbn));
    }
  }

  public void decreaseStock(int quantity) {
    validateStock(quantity);
    this.stock -= quantity;
    this.setUpdatedAt(LocalDateTime.now());
  }

  public void increaseStock(int quantity) {
    if (quantity <= 0) {
      throw new IllegalArgumentException("在庫増加数は1以上である必要があります");
    }
    this.stock += quantity;
    this.setUpdatedAt(LocalDateTime.now());
  }

  public void updatePrice(Integer newPrice) {
    if (newPrice < 0) {
      throw new IllegalArgumentException("価格は0以上である必要があります");
    }
    this.price = newPrice;
    this.setUpdatedAt(LocalDateTime.now());
  }
}
