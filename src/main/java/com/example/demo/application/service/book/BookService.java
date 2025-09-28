package com.example.demo.application.service.book;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.application.dto.book.BookDto;
import com.example.demo.domain.model.book.Book;
import com.example.demo.domain.repository.book.BookRepository;
import com.example.demo.presentation.request.book.CreateBookRequest;
import com.example.demo.presentation.request.book.UpdateBookRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    public List<BookDto> findAllBooks() {
        return bookRepository.findAll().stream()
                .map(BookDto::from)
                .collect(Collectors.toList());
    }

    public BookDto findBookById(String isbn) {
        return bookRepository.findById(isbn)
                .map(BookDto::from)
                .orElseThrow(() -> new RuntimeException("Book not found with ISBN: " + isbn));
    }

    @Transactional
    public BookDto createBook(CreateBookRequest request) {
        Book book = new Book();
        book.setIsbn(request.getIsbn());
        book.setTitle(request.getTitle());
        book.setPrice(request.getPrice());
        book.setStock(request.getStock());
        book.setCreatedAt(LocalDateTime.now());
        book.setUpdatedAt(LocalDateTime.now());

        bookRepository.save(book);
        return BookDto.from(book);
    }

    @Transactional
    public BookDto updateBook(String isbn, UpdateBookRequest request) {
        Book book = bookRepository.findById(isbn)
                .orElseThrow(() -> new RuntimeException("Book not found with ISBN: " + isbn));

        book.setTitle(request.getTitle());
        book.setPrice(request.getPrice());
        book.setStock(request.getStock());
        book.setUpdatedAt(LocalDateTime.now());

        bookRepository.save(book);
        return BookDto.from(book);
    }

    @Transactional
    public void deleteBook(String isbn) {
        Book book = bookRepository.findById(isbn)
                .orElseThrow(() -> new RuntimeException("Book not found with ISBN: " + isbn));

        if (book.getStock() > 0) {
            throw new BookDeletionException("Book with stock cannot be deleted.");
        }

        bookRepository.deleteById(isbn);
    }
}
