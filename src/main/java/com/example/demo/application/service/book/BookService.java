package com.example.demo.application.service.book;

import com.example.demo.application.dto.book.BookDto;
import com.example.demo.domain.exception.BookDeletionException;
import com.example.demo.domain.exception.BookNotFoundException;
import com.example.demo.domain.model.book.Book;
import com.example.demo.domain.repository.book.BookRepository;
import com.example.demo.presentation.request.book.CreateBookRequest;
import com.example.demo.presentation.request.book.UpdateBookRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookService {

  private final BookRepository bookRepository;

  public List<BookDto> findAllBooks() {
    return bookRepository.findAll().stream().map(BookDto::from).collect(Collectors.toList());
  }

  public BookDto findBookById(String isbn) {
    return bookRepository
        .findById(isbn)
        .map(BookDto::from)
        .orElseThrow(() -> new BookNotFoundException("書籍が見つかりません。ISBN: " + isbn));
  }

  @Transactional
  public BookDto createBook(CreateBookRequest request) {
    Book book =
        Book.create(request.getIsbn(), request.getTitle(), request.getPrice(), request.getStock());

    bookRepository.save(book);
    return BookDto.from(book);
  }

  @Transactional
  public BookDto updateBook(String isbn, UpdateBookRequest request) {
    Book book =
        bookRepository
            .findById(isbn)
            .orElseThrow(() -> new BookNotFoundException("書籍が見つかりません。ISBN: " + isbn));

    book.updatePrice(request.getPrice());
    if (!book.getTitle().equals(request.getTitle())) {
      book.setTitle(request.getTitle());
      book.setUpdatedAt(LocalDateTime.now());
    }

    // 在庫数の更新（増加または減少）
    int stockDiff = request.getStock() - book.getStock();
    if (stockDiff > 0) {
      book.increaseStock(stockDiff);
    } else if (stockDiff < 0) {
      book.decreaseStock(-stockDiff);
    }

    bookRepository.save(book);
    return BookDto.from(book);
  }

  @Transactional
  public void deleteBook(String isbn) {
    Book book =
        bookRepository
            .findById(isbn)
            .orElseThrow(() -> new BookNotFoundException("書籍が見つかりません。ISBN: " + isbn));

    if (book.getStock() > 0) {
      throw new BookDeletionException("在庫のある書籍は削除できません");
    }

    bookRepository.deleteById(isbn);
  }
}
