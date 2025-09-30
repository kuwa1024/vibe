package com.example.demo.domain.repository.book;

import com.example.demo.domain.model.book.Book;
import java.util.List;
import java.util.Optional;

public interface BookRepository {
  List<Book> findAll();

  Optional<Book> findById(String isbn);

  void save(Book book);

  void deleteById(String isbn);
}
