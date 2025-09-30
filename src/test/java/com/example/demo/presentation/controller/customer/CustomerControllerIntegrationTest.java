package com.example.demo.presentation.controller.customer;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.demo.domain.model.customer.Customer;
import com.example.demo.domain.repository.customer.CustomerRepository;
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
class CustomerControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private CustomerRepository customerRepository;

  @Autowired private JdbcTemplate jdbcTemplate;

  private Customer customer1;

  @BeforeEach
  void setUp() {
    jdbcTemplate.execute("DELETE FROM order_item");
    jdbcTemplate.execute("DELETE FROM \"order\"");
    jdbcTemplate.execute("DELETE FROM customer");
    jdbcTemplate.execute("DELETE FROM book");

    LocalDateTime now = LocalDateTime.now();
    customer1 = new Customer(UUID.randomUUID(), "John Doe", "john.doe@example.com", now, now);
    customerRepository.save(customer1);
  }

  @Test
  @DisplayName("GET /api/customers - すべての顧客を取得できる")
  void testGetAllCustomers() throws Exception {
    mockMvc
        .perform(get("/api/customers"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*]").isArray())
        .andExpect(jsonPath("$.length()", is(1)))
        .andExpect(jsonPath("$[0].id", is(customer1.getId().toString())));
  }

  @Test
  @DisplayName("GET /api/customers/{id} - IDで顧客を1件取得できる")
  void testGetCustomerById() throws Exception {
    mockMvc
        .perform(get("/api/customers/{id}", customer1.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(customer1.getId().toString())))
        .andExpect(jsonPath("$.name", is(customer1.getName())));
  }

  @Test
  @DisplayName("GET /api/customers/{id} - 存在しないIDで顧客を取得しようとすると404を返す")
  void testGetCustomerByIdNotFound() throws Exception {
    mockMvc.perform(get("/api/customers/{id}", UUID.randomUUID())).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("POST /api/customers - 顧客を新規作成できる")
  void testCreateCustomer() throws Exception {
    String newCustomerJson = "{\"name\":\"New Customer\",\"email\":\"new.customer@example.com\"}";

    mockMvc
        .perform(
            post("/api/customers").contentType(MediaType.APPLICATION_JSON).content(newCustomerJson))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name", is("New Customer")));
  }

  @Test
  @DisplayName("PUT /api/customers/{id} - 顧客を更新できる")
  void testUpdateCustomer() throws Exception {
    String updatedCustomerJson =
        "{\"name\":\"Updated Customer\",\"email\":\"updated.customer@example.com\"}";

    mockMvc
        .perform(
            put("/api/customers/{id}", customer1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatedCustomerJson))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name", is("Updated Customer")));
  }

  @Test
  @DisplayName("DELETE /api/customers/{id} - 顧客を削除できる")
  void testDeleteCustomer() throws Exception {
    mockMvc
        .perform(delete("/api/customers/{id}", customer1.getId()))
        .andExpect(status().isNoContent());

    mockMvc.perform(get("/api/customers/{id}", customer1.getId())).andExpect(status().isNotFound());
  }
}
