package com.example.demo.application.service.order;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.demo.application.dto.order.OrderDto;
import com.example.demo.domain.exception.InsufficientStockException;
import com.example.demo.domain.exception.OrderNotFoundException;
import com.example.demo.domain.exception.OrderStateException;
import com.example.demo.domain.model.book.Book;
import com.example.demo.domain.model.order.Order;
import com.example.demo.domain.model.order.Order.OrderStatus;
import com.example.demo.domain.model.order.OrderItem;
import com.example.demo.domain.repository.book.BookRepository;
import com.example.demo.domain.repository.order.OrderItemRepository;
import com.example.demo.domain.repository.order.OrderRepository;
import com.example.demo.domain.service.BookDomainService;
import com.example.demo.presentation.request.order.CreateOrderItemRequest;
import com.example.demo.presentation.request.order.CreateOrderRequest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
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
class OrderServiceTest {

  @Mock private OrderRepository orderRepository;

  @Mock private OrderItemRepository orderItemRepository;

  @Mock private BookRepository bookRepository;

  @Mock private BookDomainService bookDomainService;

  @InjectMocks private OrderService orderService;

  private Order order1;
  private Order order2;
  private OrderItem orderItem1;
  private OrderItem orderItem2;
  private CreateOrderRequest createRequest;
  private Book bookInStock;
  private Book bookOutOfStock;

  @BeforeEach
  void setUp() {
    LocalDateTime now = LocalDateTime.now();
    UUID customerId = UUID.randomUUID();
    UUID orderId1 = UUID.randomUUID();
    UUID orderId2 = UUID.randomUUID();

    orderItem1 = new OrderItem();
    orderItem1.setId(1L);
    orderItem1.setOrderId(orderId1);
    orderItem1.setBookIsbn("9784297100339");
    orderItem1.setQuantity(2);
    orderItem1.setCreatedAt(now);
    orderItem1.setUpdatedAt(now);

    orderItem2 = new OrderItem();
    orderItem2.setId(2L);
    orderItem2.setOrderId(orderId1);
    orderItem2.setBookIsbn("9784798157622");
    orderItem2.setQuantity(1);
    orderItem2.setCreatedAt(now);
    orderItem2.setUpdatedAt(now);

    order1 = Order.create(customerId);
    order1.setId(orderId1);
    order1.setOrderItems(Arrays.asList(orderItem1, orderItem2));
    order1.setOrderDatetime(now);
    order1.setCreatedAt(now);
    order1.setUpdatedAt(now);

    order2 = Order.create(customerId);
    order2.setId(orderId2);
    order2.setStatus(OrderStatus.SHIPPED.name());
    order2.setOrderDatetime(now);
    order2.setCreatedAt(now);
    order2.setUpdatedAt(now);

    CreateOrderItemRequest createOrderItemRequest1 = new CreateOrderItemRequest();
    createOrderItemRequest1.setBookIsbn("9784873119045");
    createOrderItemRequest1.setQuantity(3);

    createRequest = new CreateOrderRequest();
    createRequest.setCustomerId(customerId);
    createRequest.setOrderItems(Arrays.asList(createOrderItemRequest1));

    bookInStock = Book.create("9784873119045", "Effective Java 第3版", 4950, 10);
    bookInStock.setCreatedAt(now);
    bookInStock.setUpdatedAt(now);

    bookOutOfStock = Book.create("9784873119045", "Effective Java 第3版", 4950, 2);
    bookOutOfStock.setCreatedAt(now);
    bookOutOfStock.setUpdatedAt(now);
  }

  @Test
  @DisplayName("すべての注文を取得できる")
  void testFindAllOrders() {
    when(orderRepository.findAll()).thenReturn(Arrays.asList(order1, order2));
    when(orderItemRepository.findByOrderId(order1.getId())).thenReturn(order1.getOrderItems());
    when(orderItemRepository.findByOrderId(order2.getId())).thenReturn(order2.getOrderItems());

    List<OrderDto> result = orderService.findAllOrders();

    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(order1.getId(), result.get(0).getId());
    assertEquals(order2.getId(), result.get(1).getId());
    verify(orderRepository, times(1)).findAll();
    verify(orderItemRepository, times(1)).findByOrderId(order1.getId());
    verify(orderItemRepository, times(1)).findByOrderId(order2.getId());
  }

  @Test
  @DisplayName("IDで注文を1件取得できる")
  void testFindOrderByIdFound() {
    when(orderRepository.findById(order1.getId())).thenReturn(Optional.of(order1));
    when(orderItemRepository.findByOrderId(order1.getId())).thenReturn(order1.getOrderItems());

    OrderDto result = orderService.findOrderById(order1.getId());

    assertNotNull(result);
    assertEquals(order1.getId(), result.getId());
    verify(orderRepository, times(1)).findById(order1.getId());
    verify(orderItemRepository, times(1)).findByOrderId(order1.getId());
  }

  @Test
  @DisplayName("IDで注文が見つからない場合にOrderNotFoundExceptionをスローする")
  void testFindOrderByIdNotFound() {
    UUID nonExistentId = UUID.randomUUID();
    when(orderRepository.findById(nonExistentId)).thenReturn(Optional.empty());

    OrderNotFoundException thrown =
        assertThrows(
            OrderNotFoundException.class,
            () -> {
              orderService.findOrderById(nonExistentId);
            });

    assertTrue(thrown.getMessage().contains("注文が見つかりません"));
    verify(orderRepository, times(1)).findById(nonExistentId);
    verify(orderItemRepository, never()).findByOrderId(any(UUID.class));
  }

  @Test
  @DisplayName("注文を新規作成できる")
  void testCreateOrder() {
    doAnswer(
            invocation -> {
              Order order = invocation.getArgument(0);
              order.setId(UUID.randomUUID());
              return null;
            })
        .when(orderRepository)
        .insert(any(Order.class));
    doNothing().when(orderItemRepository).save(any(OrderItem.class));
    when(bookDomainService.validateAndDecreaseStock(anyString(), anyInt())).thenReturn(bookInStock);

    OrderDto result = orderService.createOrder(createRequest);

    assertNotNull(result);
    assertNotNull(result.getId());
    assertEquals(createRequest.getCustomerId(), result.getCustomerId());
    assertEquals(OrderStatus.PENDING.name(), result.getStatus());
    assertEquals(1, result.getOrderItems().size());
    verify(orderRepository, times(1)).insert(any(Order.class));
    verify(orderItemRepository, times(1)).save(any(OrderItem.class));
    verify(bookDomainService, times(1)).validateAndDecreaseStock(anyString(), anyInt());
  }

  @Test
  @DisplayName("在庫が不足している場合にInsufficientStockExceptionをスローする")
  void testCreateOrderInsufficientStock() {
    when(bookDomainService.validateAndDecreaseStock(anyString(), anyInt()))
        .thenThrow(new InsufficientStockException("在庫が不足しています"));

    InsufficientStockException thrown =
        assertThrows(
            InsufficientStockException.class,
            () -> {
              orderService.createOrder(createRequest);
            });

    assertTrue(thrown.getMessage().contains("在庫が不足しています"));
    verify(orderRepository, times(1)).insert(any(Order.class));
    verify(orderItemRepository, never()).save(any(OrderItem.class));
    verify(bookDomainService, times(1)).validateAndDecreaseStock(anyString(), anyInt());
  }

  @Test
  @DisplayName("注文を削除できる（在庫が元に戻ることを含む）")
  void testDeleteOrder() {
    when(orderRepository.findById(order1.getId())).thenReturn(Optional.of(order1));
    when(orderItemRepository.findByOrderId(order1.getId())).thenReturn(order1.getOrderItems());
    doNothing().when(orderItemRepository).deleteByOrderId(order1.getId());
    doNothing().when(orderRepository).deleteById(order1.getId());

    Book updatedBook = Book.create(orderItem1.getBookIsbn(), "Test Book", 100, 5);
    when(bookDomainService.validateAndIncreaseStock(anyString(), anyInt())).thenReturn(updatedBook);

    orderService.deleteOrder(order1.getId());

    verify(orderRepository, times(1)).findById(order1.getId());
    verify(orderItemRepository, times(1)).findByOrderId(order1.getId());
    verify(bookDomainService, times(2)).validateAndIncreaseStock(anyString(), anyInt());
    verify(orderItemRepository, times(1)).deleteByOrderId(order1.getId());
    verify(orderRepository, times(1)).deleteById(order1.getId());
  }

  @Test
  @DisplayName("出荷済み注文を削除しようとするとOrderStateExceptionをスローする")
  void testDeleteShippedOrder() {
    when(orderRepository.findById(order2.getId())).thenReturn(Optional.of(order2));
    when(orderItemRepository.findByOrderId(order2.getId())).thenReturn(Collections.emptyList());

    OrderStateException thrown =
        assertThrows(
            OrderStateException.class,
            () -> {
              orderService.deleteOrder(order2.getId());
            });

    assertTrue(thrown.getMessage().contains("この操作は注文のステータスが"));
    verify(orderRepository, times(1)).findById(order2.getId());
    verify(orderItemRepository, times(1)).findByOrderId(order2.getId());
    verify(bookDomainService, never()).validateAndIncreaseStock(anyString(), anyInt());
    verify(orderItemRepository, never()).deleteByOrderId(any(UUID.class));
    verify(orderRepository, never()).deleteById(any(UUID.class));
  }

  @Test
  @DisplayName("削除対象の注文が見つからない場合にOrderNotFoundExceptionをスローする")
  void testDeleteOrderNotFound() {
    UUID nonExistentId = UUID.randomUUID();
    when(orderRepository.findById(nonExistentId)).thenReturn(Optional.empty());

    OrderNotFoundException thrown =
        assertThrows(
            OrderNotFoundException.class,
            () -> {
              orderService.deleteOrder(nonExistentId);
            });

    assertTrue(thrown.getMessage().contains("注文が見つかりません"));
    verify(orderRepository, times(1)).findById(nonExistentId);
    verify(orderItemRepository, never()).findByOrderId(any(UUID.class));
    verify(bookDomainService, never()).validateAndIncreaseStock(anyString(), anyInt());
    verify(orderItemRepository, never()).deleteByOrderId(any(UUID.class));
    verify(orderRepository, never()).deleteById(any(UUID.class));
  }
}
