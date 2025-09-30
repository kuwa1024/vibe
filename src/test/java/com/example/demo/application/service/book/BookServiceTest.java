package com.example.demo.application.service.book;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.demo.application.dto.book.BookDto;
import com.example.demo.domain.exception.BookDeletionException;
import com.example.demo.domain.exception.BookNotFoundException;
import com.example.demo.domain.model.book.Book;
import com.example.demo.domain.repository.book.BookRepository;
import com.example.demo.presentation.request.book.CreateBookRequest;
import com.example.demo.presentation.request.book.UpdateBookRequest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

  @Mock private BookRepository bookRepository;

  @InjectMocks private BookService bookService;

  private Book book1;
  private Book book2;
  private CreateBookRequest createRequest;
  private UpdateBookRequest updateRequest;

  @BeforeEach
  void setUp() {
    LocalDateTime now = LocalDateTime.now();
    book1 = Book.create("9784297100339", "達人プログラマー", 3200, 10);
    book1.setCreatedAt(now);
    book1.setUpdatedAt(now);

    book2 = Book.create("9784798157622", "Clean Architecture", 3400, 5);
    book2.setCreatedAt(now);
    book2.setUpdatedAt(now);

    createRequest = new CreateBookRequest();
    createRequest.setIsbn("9784873119045");
    createRequest.setTitle("Effective Java 第3版");
    createRequest.setPrice(4950);
    createRequest.setStock(15);

    updateRequest = new UpdateBookRequest();
    updateRequest.setTitle("Effective Java 第3版 改訂版");
    updateRequest.setPrice(5000);
    updateRequest.setStock(20);
  }

  @Test
  @DisplayName("すべての書籍を取得できる")
  void testFindAllBooks() {
    when(bookRepository.findAll()).thenReturn(Arrays.asList(book1, book2));

    List<BookDto> result = bookService.findAllBooks();

    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(book1.getIsbn(), result.get(0).getIsbn());
    assertEquals(book2.getIsbn(), result.get(1).getIsbn());
    verify(bookRepository, times(1)).findAll();
  }

  @Test
  @DisplayName("ISBNで書籍を1件取得できる")
  void testFindBookByIdFound() {
    when(bookRepository.findById(book1.getIsbn())).thenReturn(Optional.of(book1));

    BookDto result = bookService.findBookById(book1.getIsbn());

    assertNotNull(result);
    assertEquals(book1.getIsbn(), result.getIsbn());
    verify(bookRepository, times(1)).findById(book1.getIsbn());
  }

  @Test
  @DisplayName("ISBNで書籍が見つからない場合にBookNotFoundExceptionをスローする")
  void testFindBookByIdNotFound() {
    String nonexistentIsbn = "nonexistent";
    when(bookRepository.findById(nonexistentIsbn)).thenReturn(Optional.empty());

    BookNotFoundException thrown =
        assertThrows(
            BookNotFoundException.class,
            () -> {
              bookService.findBookById(nonexistentIsbn);
            });

    assertTrue(thrown.getMessage().contains("書籍が見つかりません"));
    verify(bookRepository, times(1)).findById(nonexistentIsbn);
  }

  @Test
  @DisplayName("書籍を新規作成できる")
  void testCreateBook() {
    BookDto result = bookService.createBook(createRequest);

    assertNotNull(result);
    assertEquals(createRequest.getIsbn(), result.getIsbn());
    assertEquals(createRequest.getTitle(), result.getTitle());
    assertNotNull(result.getCreatedAt());
    assertNotNull(result.getUpdatedAt());
    verify(bookRepository, times(1)).save(any(Book.class));
  }

  @Test
  @DisplayName("書籍を更新できる")
  void testUpdateBook() {
    when(bookRepository.findById(book1.getIsbn())).thenReturn(Optional.of(book1));

    BookDto result = bookService.updateBook(book1.getIsbn(), updateRequest);

    assertNotNull(result);
    assertEquals(book1.getIsbn(), result.getIsbn());
    assertEquals(updateRequest.getTitle(), result.getTitle());
    assertEquals(updateRequest.getPrice(), result.getPrice());
    assertEquals(updateRequest.getStock(), result.getStock());
    assertNotNull(result.getUpdatedAt());
    verify(bookRepository, times(1)).findById(book1.getIsbn());
    verify(bookRepository, times(1)).save(any(Book.class));
  }

  @Test
  @DisplayName("更新対象の書籍が見つからない場合にBookNotFoundExceptionをスローする")
  void testUpdateBookNotFound() {
    String nonexistentIsbn = "nonexistent";
    when(bookRepository.findById(nonexistentIsbn)).thenReturn(Optional.empty());

    BookNotFoundException thrown =
        assertThrows(
            BookNotFoundException.class,
            () -> {
              bookService.updateBook(nonexistentIsbn, updateRequest);
            });

    assertTrue(thrown.getMessage().contains("書籍が見つかりません"));
    verify(bookRepository, times(1)).findById(nonexistentIsbn);
    verify(bookRepository, never()).save(any(Book.class));
  }

  @Test
  @DisplayName("書籍を削除できる")
  void testDeleteBook() {
    book1.setStock(0);
    when(bookRepository.findById(book1.getIsbn())).thenReturn(Optional.of(book1));
    doNothing().when(bookRepository).deleteById(book1.getIsbn());

    bookService.deleteBook(book1.getIsbn());

    verify(bookRepository, times(1)).findById(book1.getIsbn());
    verify(bookRepository, times(1)).deleteById(book1.getIsbn());
  }

  @Test
  @DisplayName("在庫のある書籍を削除しようとするとBookDeletionExceptionをスローする")
  void testDeleteBookWithStock() {
    when(bookRepository.findById(book1.getIsbn())).thenReturn(Optional.of(book1));

    BookDeletionException thrown =
        assertThrows(
            BookDeletionException.class,
            () -> {
              bookService.deleteBook(book1.getIsbn());
            });

    assertTrue(thrown.getMessage().contains("在庫のある書籍は削除できません"));
    verify(bookRepository, times(1)).findById(book1.getIsbn());
    verify(bookRepository, never()).deleteById(anyString());
  }

  @Test
  @DisplayName("削除対象の書籍が見つからない場合にBookNotFoundExceptionをスローする")
  void testDeleteBookNotFound() {
    String nonexistentIsbn = "nonexistent";
    when(bookRepository.findById(nonexistentIsbn)).thenReturn(Optional.empty());

    BookNotFoundException thrown =
        assertThrows(
            BookNotFoundException.class,
            () -> {
              bookService.deleteBook(nonexistentIsbn);
            });

    assertTrue(thrown.getMessage().contains("書籍が見つかりません"));
    verify(bookRepository, times(1)).findById(nonexistentIsbn);
    verify(bookRepository, never()).deleteById(anyString());
  }
}
