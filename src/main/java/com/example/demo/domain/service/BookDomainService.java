package com.example.demo.domain.service;

import com.example.demo.domain.exception.BookNotFoundException;
import com.example.demo.domain.model.book.Book;
import com.example.demo.domain.repository.book.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookDomainService {
  private final BookRepository bookRepository;

  public Book validateAndDecreaseStock(String isbn, int quantity) {
    Book book =
        bookRepository
            .findById(isbn)
            .orElseThrow(() -> new BookNotFoundException("書籍が見つかりません。ISBN: " + isbn));

    book.decreaseStock(quantity);
    return book;
  }

  public Book validateAndIncreaseStock(String isbn, int quantity) {
    Book book =
        bookRepository
            .findById(isbn)
            .orElseThrow(() -> new BookNotFoundException("書籍が見つかりません。ISBN: " + isbn));

    book.increaseStock(quantity);
    return book;
  }
}
