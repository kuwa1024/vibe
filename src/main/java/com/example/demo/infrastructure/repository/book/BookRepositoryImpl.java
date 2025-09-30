package com.example.demo.infrastructure.repository.book;

import com.example.demo.domain.model.book.Book;
import com.example.demo.domain.repository.book.BookRepository;
import com.example.demo.infrastructure.mapper.book.BookMapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BookRepositoryImpl implements BookRepository {

  private final BookMapper bookMapper;

  @Override
  public List<Book> findAll() {
    return bookMapper.findAll();
  }

  @Override
  public Optional<Book> findById(String isbn) {
    return bookMapper.findById(isbn);
  }

  @Override
  public void save(Book book) {
    if (bookMapper.countByIsbn(book.getIsbn()) > 0) {
      bookMapper.update(book);
    } else {
      bookMapper.insert(book);
    }
  }

  @Override
  public void deleteById(String isbn) {
    bookMapper.deleteById(isbn);
  }
}
