package com.example.demo.domain.repository.book;

import java.util.List;
import java.util.Optional;

import com.example.demo.domain.model.book.Book;

public interface BookRepository {
    List<Book> findAll();

    Optional<Book> findById(String isbn);

    void save(Book book);

    void deleteById(String isbn);
}
