package com.example.demo.application.service.order;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

import com.example.demo.application.dto.order.OrderDto;
import com.example.demo.domain.model.order.Order;
import com.example.demo.domain.model.order.Order.OrderStatus;
import com.example.demo.domain.model.order.OrderItem;
import com.example.demo.domain.repository.order.OrderItemRepository;
import com.example.demo.domain.repository.order.OrderRepository;
import com.example.demo.domain.model.book.Book;
import com.example.demo.domain.repository.book.BookRepository;
import com.example.demo.presentation.request.order.CreateOrderItemRequest;
import com.example.demo.presentation.request.order.CreateOrderRequest;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private OrderService orderService;

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

        orderItem1 = new OrderItem(1L, orderId1, "9784297100339", 2, now, now);
        orderItem2 = new OrderItem(2L, orderId1, "9784798157622", 1, now, now);

        order1 = new Order(orderId1, customerId, now, OrderStatus.PENDING.name(), now, now, Arrays.asList(orderItem1, orderItem2));
        order2 = new Order(orderId2, customerId, now, OrderStatus.SHIPPED.name(), now, now, Collections.emptyList());

        CreateOrderItemRequest createOrderItemRequest1 = new CreateOrderItemRequest();
        createOrderItemRequest1.setBookIsbn("9784873119045");
        createOrderItemRequest1.setQuantity(3);

        createRequest = new CreateOrderRequest();
        createRequest.setCustomerId(customerId);
        createRequest.setOrderItems(Arrays.asList(createOrderItemRequest1));

        bookInStock = new Book("9784873119045", "Effective Java 第3版", 4950, 10, now, now);
        bookOutOfStock = new Book("9784873119045", "Effective Java 第3版", 4950, 2, now, now);
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
    @DisplayName("IDで注文が見つからない場合にRuntimeExceptionをスローする")
    void testFindOrderByIdNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(orderRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            orderService.findOrderById(nonExistentId);
        });

        assertTrue(thrown.getMessage().contains("Order not found"));
        verify(orderRepository, times(1)).findById(nonExistentId);
        verify(orderItemRepository, never()).findByOrderId(any(UUID.class));
    }

    @Test
    @DisplayName("注文を新規作成できる")
    void testCreateOrder() {
        doAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(UUID.randomUUID());
            return null;
        }).when(orderRepository).insert(any(Order.class));
        doNothing().when(orderItemRepository).save(any(OrderItem.class));
        when(bookRepository.findById(anyString())).thenReturn(Optional.of(bookInStock));
        doNothing().when(bookRepository).save(any(Book.class));

        OrderDto result = orderService.createOrder(createRequest);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(createRequest.getCustomerId(), result.getCustomerId());
        assertEquals(OrderStatus.PENDING.name(), result.getStatus());
        assertEquals(1, result.getOrderItems().size());
        verify(orderRepository, times(1)).insert(any(Order.class));
        verify(orderItemRepository, times(1)).save(any(OrderItem.class));
        verify(bookRepository, times(1)).findById(anyString());
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    @DisplayName("在庫が不足している場合にInsufficientStockExceptionをスローする")
    void testCreateOrderInsufficientStock() {
        when(bookRepository.findById(anyString())).thenReturn(Optional.of(bookOutOfStock));

        InsufficientStockException thrown = assertThrows(InsufficientStockException.class, () -> {
            orderService.createOrder(createRequest);
        });

        assertTrue(thrown.getMessage().contains("Insufficient stock"));
        verify(orderRepository, times(1)).insert(any(Order.class));
        verify(orderItemRepository, never()).save(any(OrderItem.class));
        verify(bookRepository, times(1)).findById(anyString());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("注文を削除できる（在庫が元に戻ることを含む）")
    void testDeleteOrder() {
        // 注文アイテムの書籍の在庫をモック
        Book bookInOrder = new Book(orderItem1.getBookIsbn(), "Test Book", 100, 5, LocalDateTime.now(), LocalDateTime.now());
        when(orderRepository.findById(order1.getId())).thenReturn(Optional.of(order1));
        when(orderItemRepository.findByOrderId(order1.getId())).thenReturn(order1.getOrderItems());
        when(bookRepository.findById(orderItem1.getBookIsbn())).thenReturn(Optional.of(bookInOrder));
        when(bookRepository.findById(orderItem2.getBookIsbn())).thenReturn(Optional.of(bookInOrder)); // 同じ本をモック
        doNothing().when(orderItemRepository).deleteByOrderId(order1.getId());
        doNothing().when(orderRepository).deleteById(order1.getId());

        orderService.deleteOrder(order1.getId());

        verify(orderRepository, times(1)).findById(order1.getId());
        verify(orderItemRepository, times(1)).findByOrderId(order1.getId());
        verify(bookRepository, times(2)).findById(anyString()); // 2つの注文アイテムに対して呼ばれる
        verify(bookRepository, times(2)).save(any(Book.class)); // 2つの本の在庫が更新される
        verify(orderItemRepository, times(1)).deleteByOrderId(order1.getId());
        verify(orderRepository, times(1)).deleteById(order1.getId());

        // 在庫が元に戻ったことを確認 (モックのbookInStockのstockが更新されることを想定)
        assertEquals(5 + orderItem1.getQuantity() + orderItem2.getQuantity(), bookInOrder.getStock());
    }

    @Test
    @DisplayName("出荷済み注文を削除しようとするとOrderCancellationExceptionをスローする")
    void testDeleteShippedOrder() {
        when(orderRepository.findById(order2.getId())).thenReturn(Optional.of(order2));

        OrderCancellationException thrown = assertThrows(OrderCancellationException.class, () -> {
            orderService.deleteOrder(order2.getId());
        });

        assertTrue(thrown.getMessage().contains("cannot be cancelled"));
        verify(orderRepository, times(1)).findById(order2.getId());
        verify(orderItemRepository, never()).findByOrderId(any(UUID.class));
        verify(bookRepository, never()).findById(anyString());
        verify(bookRepository, never()).save(any(Book.class));
        verify(orderItemRepository, never()).deleteByOrderId(any(UUID.class));
        verify(orderRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    @DisplayName("削除対象の注文が見つからない場合にRuntimeExceptionをスローする")
    void testDeleteOrderNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(orderRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            orderService.deleteOrder(nonExistentId);
        });

        assertTrue(thrown.getMessage().contains("Order not found"));
        verify(orderRepository, times(1)).findById(nonExistentId);
        verify(orderItemRepository, never()).findByOrderId(any(UUID.class));
        verify(bookRepository, never()).findById(anyString());
        verify(bookRepository, never()).save(any(Book.class));
        verify(orderItemRepository, never()).deleteByOrderId(any(UUID.class));
        verify(orderRepository, never()).deleteById(any(UUID.class));
    }
}
