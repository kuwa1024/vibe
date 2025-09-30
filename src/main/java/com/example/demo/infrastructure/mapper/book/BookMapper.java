package com.example.demo.infrastructure.mapper.book;

import com.example.demo.domain.model.book.Book;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BookMapper {

  List<Book> findAll();

  Optional<Book> findById(String isbn);

  void insert(Book book);

  void update(Book book);

  void deleteById(String isbn);

  int countByIsbn(String isbn);
}
