package com.example.demo.presentation.controller.book;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.demo.application.dto.book.BookDto;
import com.example.demo.application.service.book.BookService;
import com.example.demo.presentation.request.book.CreateBookRequest;
import com.example.demo.presentation.request.book.UpdateBookRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping
    public ResponseEntity<List<BookDto>> getAllBooks() {
        return ResponseEntity.ok(bookService.findAllBooks());
    }

    @GetMapping("/{isbn}")
    public ResponseEntity<BookDto> getBookByIsbn(@PathVariable String isbn) {
        return ResponseEntity.ok(bookService.findBookById(isbn));
    }

    @PostMapping
    public ResponseEntity<BookDto> createBook(@RequestBody @Valid CreateBookRequest request) {
        BookDto createdBook = bookService.createBook(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{isbn}")
                .buildAndExpand(createdBook.getIsbn())
                .toUri();
        return ResponseEntity.created(location).body(createdBook);
    }

    @PutMapping("/{isbn}")
    public ResponseEntity<BookDto> updateBook(@PathVariable String isbn, @RequestBody @Valid UpdateBookRequest request) {
        return ResponseEntity.ok(bookService.updateBook(isbn, request));
    }

    @DeleteMapping("/{isbn}")
    public ResponseEntity<Void> deleteBook(@PathVariable String isbn) {
        bookService.deleteBook(isbn);
        return ResponseEntity.noContent().build();
    }
}
