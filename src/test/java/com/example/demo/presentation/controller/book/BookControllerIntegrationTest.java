package com.example.demo.presentation.controller.book;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.demo.domain.model.book.Book;
import com.example.demo.domain.repository.book.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private BookRepository bookRepository;

  @Autowired private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void setUp() {
    // 各テストの前にデータをクリーンアップ
    jdbcTemplate.execute("DELETE FROM order_item");
    jdbcTemplate.execute("DELETE FROM \"order\"");
    jdbcTemplate.execute("DELETE FROM book");
  }

  @Test
  @DisplayName("GET /api/books - すべての書籍を取得できる")
  void testGetAllBooks() throws Exception {
    // 初期データ投入
    Book book1 = Book.create("9784297100339", "達人プログラマー", 3200, 10);
    Book book2 = Book.create("9784798157622", "Clean Architecture", 3400, 5);
    bookRepository.save(book1);
    bookRepository.save(book2);

    mockMvc
        .perform(get("/api/books"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*]").isArray())
        .andExpect(jsonPath("$.length()", is(2)))
        .andExpect(jsonPath("$[0].isbn", is("9784297100339")));
  }

  @Test
  @DisplayName("GET /api/books/{isbn} - ISBNで書籍を1件取得できる")
  void testGetBookByIsbn() throws Exception {
    Book book = Book.create("9784873119045", "Effective Java 第3版", 4950, 15);
    bookRepository.save(book);

    mockMvc
        .perform(get("/api/books/{isbn}", book.getIsbn()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.isbn", is(book.getIsbn())))
        .andExpect(jsonPath("$.title", is(book.getTitle())));
  }

  @Test
  @DisplayName("GET /api/books/{isbn} - 存在しないISBNで書籍を取得しようとすると404を返す")
  void testGetBookByIsbnNotFound() throws Exception {
    mockMvc
        .perform(get("/api/books/{isbn}", "nonexistent"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error", is("書籍が見つかりません。ISBN: nonexistent")));
  }

  @Test
  @DisplayName("POST /api/books - 書籍を新規作成できる")
  void testCreateBook() throws Exception {
    String newBookJson =
        "{\"isbn\":\"9781234567890\",\"title\":\"New Book\",\"price\":1000,\"stock\":10}";

    mockMvc
        .perform(post("/api/books").contentType(MediaType.APPLICATION_JSON).content(newBookJson))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", containsString("/api/books/9781234567890")))
        .andExpect(jsonPath("$.isbn", is("9781234567890")))
        .andExpect(jsonPath("$.title", is("New Book")));

    // DBに保存されたことを確認
    mockMvc.perform(get("/api/books/9781234567890")).andExpect(status().isOk());
  }

  @Test
  @DisplayName("POST /api/books - 不正なリクエストボディで書籍を作成しようとすると400を返す")
  void testCreateBookInvalidRequest() throws Exception {
    String invalidBookJson = "{\"isbn\":\"123\",\"title\":\"Invalid\",\"price\":-1,\"stock\":-1}";

    mockMvc
        .perform(
            post("/api/books").contentType(MediaType.APPLICATION_JSON).content(invalidBookJson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.isbn", is("size must be between 13 and 13")))
        .andExpect(jsonPath("$.price", is("must be greater than or equal to 0")))
        .andExpect(jsonPath("$.stock", is("must be greater than or equal to 0")));
  }

  @Test
  @DisplayName("PUT /api/books/{isbn} - 書籍を更新できる")
  void testUpdateBook() throws Exception {
    Book book = Book.create("9781111111111", "Old Title", 100, 5);
    bookRepository.save(book);

    String updatedBookJson = "{\"title\":\"Updated Title\",\"price\":200,\"stock\":10}";

    mockMvc
        .perform(
            put("/api/books/{isbn}", book.getIsbn())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatedBookJson))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.isbn", is(book.getIsbn())))
        .andExpect(jsonPath("$.title", is("Updated Title")))
        .andExpect(jsonPath("$.price", is(200)))
        .andExpect(jsonPath("$.stock", is(10)));
  }

  @Test
  @DisplayName("PUT /api/books/{isbn} - 存在しないISBNの書籍を更新しようとすると404を返す")
  void testUpdateBookNotFound() throws Exception {
    String updatedBookJson = "{\"title\":\"Updated Title\",\"price\":200,\"stock\":10}";

    mockMvc
        .perform(
            put("/api/books/{isbn}", "nonexistent")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatedBookJson))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error", is("書籍が見つかりません。ISBN: nonexistent")));
  }

  @Test
  @DisplayName("DELETE /api/books/{isbn} - 書籍を削除できる")
  void testDeleteBook() throws Exception {
    Book book = Book.create("9782222222222", "Delete Me", 50, 0);
    bookRepository.save(book);

    mockMvc.perform(delete("/api/books/{isbn}", book.getIsbn())).andExpect(status().isNoContent());

    // DBから削除されたことを確認
    mockMvc.perform(get("/api/books/{isbn}", book.getIsbn())).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("DELETE /api/books/{isbn} - 在庫のある書籍を削除しようとすると400エラー")
  void testDeleteBookWithStock() throws Exception {
    Book book = Book.create("9782222222222", "Delete Me", 50, 2);
    bookRepository.save(book);

    mockMvc
        .perform(delete("/api/books/{isbn}", book.getIsbn()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error", is("在庫のある書籍は削除できません")));
  }

  @Test
  @DisplayName("DELETE /api/books/{isbn} - 存在しないISBNの書籍を削除しようとすると404を返す")
  void testDeleteBookNotFound() throws Exception {
    mockMvc
        .perform(delete("/api/books/{isbn}", "nonexistent"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error", is("書籍が見つかりません。ISBN: nonexistent")));
  }
}
