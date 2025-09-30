package com.example.demo.presentation.controller.order;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.demo.domain.model.book.Book;
import com.example.demo.domain.model.customer.Customer;
import com.example.demo.domain.model.order.Order;
import com.example.demo.domain.repository.book.BookRepository;
import com.example.demo.domain.repository.customer.CustomerRepository;
import com.example.demo.domain.repository.order.OrderItemRepository;
import com.example.demo.domain.repository.order.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.UUID;
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
class OrderControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private OrderRepository orderRepository;

  @Autowired private OrderItemRepository orderItemRepository;

  @Autowired private CustomerRepository customerRepository;

  @Autowired private BookRepository bookRepository;

  @Autowired private JdbcTemplate jdbcTemplate;

  private Customer customer;
  private Book book1;
  private Book book2;
  private Order order;

  @BeforeEach
  void setUp() {
    jdbcTemplate.execute("DELETE FROM order_item");
    jdbcTemplate.execute("DELETE FROM \"order\"");
    jdbcTemplate.execute("DELETE FROM customer");
    jdbcTemplate.execute("DELETE FROM book");

    LocalDateTime now = LocalDateTime.now();

    customer = new Customer(UUID.randomUUID(), "Test Customer", "test@example.com", now, now);
    customerRepository.save(customer);

    book1 = Book.create("9784297100339", "達人プログラマー", 3200, 10);
    book1.setCreatedAt(now);
    book1.setUpdatedAt(now);
    bookRepository.save(book1);

    book2 = Book.create("9784798157622", "Clean Architecture", 3400, 5);
    book2.setCreatedAt(now);
    book2.setUpdatedAt(now);
    bookRepository.save(book2);

    order = Order.create(customer.getId());
    order.setOrderDatetime(now);
    order.setCreatedAt(now);
    order.setUpdatedAt(now);
    orderRepository.insert(order);

    order.addOrderItem(book1.getIsbn(), 2);
    orderItemRepository.save(order.getOrderItems().get(0));

    order.addOrderItem(book2.getIsbn(), 1);
    orderItemRepository.save(order.getOrderItems().get(1));
  }

  @Test
  @DisplayName("GET /api/orders - すべての注文を取得できる")
  void testGetAllOrders() throws Exception {
    mockMvc
        .perform(get("/api/orders"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*]").isArray())
        .andExpect(jsonPath("$.length()", is(1)))
        .andExpect(jsonPath("$[0].id", is(order.getId().toString())))
        .andExpect(jsonPath("$[0].orderItems.length()", is(2)));
  }

  @Test
  @DisplayName("GET /api/orders/{id} - IDで注文を1件取得できる")
  void testGetOrderById() throws Exception {
    mockMvc
        .perform(get("/api/orders/{id}", order.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(order.getId().toString())))
        .andExpect(jsonPath("$.customerId", is(customer.getId().toString())))
        .andExpect(jsonPath("$.orderItems.length()", is(2)));
  }

  @Test
  @DisplayName("GET /api/orders/{id} - 存在しないIDで注文を取得しようとすると404を返す")
  void testGetOrderByIdNotFound() throws Exception {
    mockMvc
        .perform(get("/api/orders/{id}", UUID.randomUUID()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error", matchesPattern("注文が見つかりません。ID: .*")));
  }

  @Test
  @DisplayName("POST /api/orders - 注文を新規作成できる")
  void testCreateOrder() throws Exception {
    LocalDateTime now = LocalDateTime.now();
    Customer newCustomer =
        new Customer(UUID.randomUUID(), "New Customer", "new@example.com", now, now);
    customerRepository.save(newCustomer);

    String newOrderJson =
        "{\"customerId\":\""
            + newCustomer.getId()
            + "\",\"orderItems\":[{\"bookIsbn\":\""
            + book1.getIsbn()
            + "\",\"quantity\":1}]}";

    mockMvc
        .perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(newOrderJson))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.customerId", is(newCustomer.getId().toString())))
        .andExpect(jsonPath("$.status", is(Order.OrderStatus.PENDING.name())))
        .andExpect(jsonPath("$.orderItems.length()", is(1)));
  }

  @Test
  @DisplayName("POST /api/orders - 在庫不足で注文を作成しようとすると400エラーを返す")
  void testCreateOrderInsufficientStock() throws Exception {
    LocalDateTime now = LocalDateTime.now();
    Customer newCustomer =
        new Customer(UUID.randomUUID(), "New Customer", "new@example.com", now, now);
    customerRepository.save(newCustomer);

    // book1の在庫は10なので、11個注文すると在庫不足エラーになる
    String newOrderJson =
        "{\"customerId\":\""
            + newCustomer.getId()
            + "\",\"orderItems\":[{\"bookIsbn\":\""
            + book1.getIsbn()
            + "\",\"quantity\":11}]}";

    mockMvc
        .perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(newOrderJson))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath(
                "$.error",
                is("在庫が不足しています（要求数: 11, 在庫数: 10, 書籍: 達人プログラマー [" + book1.getIsbn() + "]）")));
  }

  @Test
  @DisplayName("DELETE /api/orders/{id} - 注文を削除できる（在庫が元に戻ることを含む）")
  void testDeleteOrder() throws Exception {
    // 削除前の本の在庫数を取得
    Integer initialBook1Stock = bookRepository.findById(book1.getIsbn()).get().getStock();
    Integer initialBook2Stock = bookRepository.findById(book2.getIsbn()).get().getStock();

    mockMvc.perform(delete("/api/orders/{id}", order.getId())).andExpect(status().isNoContent());

    // DBから削除されたことを確認
    mockMvc.perform(get("/api/orders/{id}", order.getId())).andExpect(status().isNotFound());

    // 在庫が元に戻ったことを確認
    assertEquals(
        initialBook1Stock, (int) bookRepository.findById(book1.getIsbn()).get().getStock());
    assertEquals(
        initialBook2Stock, (int) bookRepository.findById(book2.getIsbn()).get().getStock());
  }

  @Test
  @DisplayName("DELETE /api/orders/{id} - 出荷済み注文を削除しようとすると400エラーを返す")
  void testDeleteShippedOrder() throws Exception {
    // 注文ステータスを出荷済みに変更
    order.ship();
    orderRepository.update(order);

    mockMvc
        .perform(delete("/api/orders/{id}", order.getId()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error", is("この操作は注文のステータスが PENDING の場合のみ可能です。現在のステータス: SHIPPED")));
  }
}
