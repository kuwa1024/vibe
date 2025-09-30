package com.example.demo.application.service.customer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.demo.application.dto.customer.CustomerDto;
import com.example.demo.domain.model.customer.Customer;
import com.example.demo.domain.repository.customer.CustomerRepository;
import com.example.demo.presentation.request.customer.CreateCustomerRequest;
import com.example.demo.presentation.request.customer.UpdateCustomerRequest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

  @Mock private CustomerRepository customerRepository;

  @InjectMocks private CustomerService customerService;

  private Customer customer1;
  private Customer customer2;
  private CreateCustomerRequest createRequest;
  private UpdateCustomerRequest updateRequest;

  @BeforeEach
  void setUp() {
    LocalDateTime now = LocalDateTime.now();
    customer1 = new Customer(UUID.randomUUID(), "John Doe", "john.doe@example.com", now, now);
    customer2 = new Customer(UUID.randomUUID(), "Jane Smith", "jane.smith@example.com", now, now);

    createRequest = new CreateCustomerRequest();
    createRequest.setName("New Customer");
    createRequest.setEmail("new.customer@example.com");

    updateRequest = new UpdateCustomerRequest();
    updateRequest.setName("Updated Customer");
    updateRequest.setEmail("updated.customer@example.com");
  }

  @Test
  @DisplayName("すべての顧客を取得できる")
  void testFindAllCustomers() {
    when(customerRepository.findAll()).thenReturn(Arrays.asList(customer1, customer2));

    List<CustomerDto> result = customerService.findAllCustomers();

    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(customer1.getId(), result.get(0).getId());
    assertEquals(customer2.getId(), result.get(1).getId());
    verify(customerRepository, times(1)).findAll();
  }

  @Test
  @DisplayName("IDで顧客を1件取得できる")
  void testFindCustomerByIdFound() {
    when(customerRepository.findById(customer1.getId())).thenReturn(Optional.of(customer1));

    CustomerDto result = customerService.findCustomerById(customer1.getId());

    assertNotNull(result);
    assertEquals(customer1.getId(), result.getId());
    verify(customerRepository, times(1)).findById(customer1.getId());
  }

  @Test
  @DisplayName("IDで顧客が見つからない場合にRuntimeExceptionをスローする")
  void testFindCustomerByIdNotFound() {
    UUID nonExistentId = UUID.randomUUID();
    when(customerRepository.findById(nonExistentId)).thenReturn(Optional.empty());

    RuntimeException thrown =
        assertThrows(
            RuntimeException.class,
            () -> {
              customerService.findCustomerById(nonExistentId);
            });

    assertTrue(thrown.getMessage().contains("Customer not found"));
    verify(customerRepository, times(1)).findById(nonExistentId);
  }

  @Test
  @DisplayName("顧客を新規作成できる")
  void testCreateCustomer() {
    CustomerDto result = customerService.createCustomer(createRequest);

    assertNotNull(result);
    assertEquals(createRequest.getName(), result.getName());
    assertEquals(createRequest.getEmail(), result.getEmail());
    assertNotNull(result.getCreatedAt());
    assertNotNull(result.getUpdatedAt());
    verify(customerRepository, times(1)).save(any(Customer.class));
  }

  @Test
  @DisplayName("顧客を更新できる")
  void testUpdateCustomer() {
    when(customerRepository.findById(customer1.getId())).thenReturn(Optional.of(customer1));

    CustomerDto result = customerService.updateCustomer(customer1.getId(), updateRequest);

    assertNotNull(result);
    assertEquals(customer1.getId(), result.getId());
    assertEquals(updateRequest.getName(), result.getName());
    assertEquals(updateRequest.getEmail(), result.getEmail());
    assertNotNull(result.getUpdatedAt());
    verify(customerRepository, times(1)).findById(customer1.getId());
    verify(customerRepository, times(1)).save(any(Customer.class));
  }

  @Test
  @DisplayName("更新対象の顧客が見つからない場合にRuntimeExceptionをスローする")
  void testUpdateCustomerNotFound() {
    UUID nonExistentId = UUID.randomUUID();
    when(customerRepository.findById(nonExistentId)).thenReturn(Optional.empty());

    RuntimeException thrown =
        assertThrows(
            RuntimeException.class,
            () -> {
              customerService.updateCustomer(nonExistentId, updateRequest);
            });

    assertTrue(thrown.getMessage().contains("Customer not found"));
    verify(customerRepository, times(1)).findById(nonExistentId);
    verify(customerRepository, never()).save(any(Customer.class));
  }

  @Test
  @DisplayName("顧客を削除できる")
  void testDeleteCustomer() {
    when(customerRepository.findById(customer1.getId())).thenReturn(Optional.of(customer1));
    doNothing().when(customerRepository).deleteById(customer1.getId());

    customerService.deleteCustomer(customer1.getId());

    verify(customerRepository, times(1)).findById(customer1.getId());
    verify(customerRepository, times(1)).deleteById(customer1.getId());
  }

  @Test
  @DisplayName("削除対象の顧客が見つからない場合にRuntimeExceptionをスローする")
  void testDeleteCustomerNotFound() {
    UUID nonExistentId = UUID.randomUUID();
    when(customerRepository.findById(nonExistentId)).thenReturn(Optional.empty());

    RuntimeException thrown =
        assertThrows(
            RuntimeException.class,
            () -> {
              customerService.deleteCustomer(nonExistentId);
            });

    assertTrue(thrown.getMessage().contains("Customer not found"));
    verify(customerRepository, times(1)).findById(nonExistentId);
    verify(customerRepository, never()).deleteById(any(UUID.class));
  }
}
