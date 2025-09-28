package com.example.demo.application.service.order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.application.dto.order.OrderDto;
import com.example.demo.application.service.order.InsufficientStockException;
import com.example.demo.application.service.order.OrderCancellationException;
import com.example.demo.domain.model.order.Order;
import com.example.demo.domain.model.order.OrderItem;
import com.example.demo.domain.model.book.Book;
import com.example.demo.domain.repository.book.BookRepository;
import com.example.demo.domain.repository.order.OrderItemRepository;
import com.example.demo.domain.repository.order.OrderRepository;
import com.example.demo.presentation.request.order.CreateOrderRequest;
import com.example.demo.presentation.request.order.CreateOrderItemRequest;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final BookRepository bookRepository;

    public List<OrderDto> findAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream().map(order -> {
            order.setOrderItems(orderItemRepository.findByOrderId(order.getId()));
            return OrderDto.from(order);
        }).collect(Collectors.toList());
    }

    public OrderDto findOrderById(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + id));
        order.setOrderItems(orderItemRepository.findByOrderId(order.getId()));
        return OrderDto.from(order);
    }

    @Transactional
    public OrderDto createOrder(CreateOrderRequest request) {
        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setCustomerId(request.getCustomerId());
        order.setOrderDatetime(LocalDateTime.now());
        order.setStatus(Order.OrderStatus.PENDING.name());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        // 注文を先に保存してIDを確定させる
        orderRepository.insert(order);

        List<OrderItem> orderItems = request.getOrderItems().stream().map(itemRequest -> {
            // 在庫チェックと在庫削減
            Book book = bookRepository.findById(itemRequest.getBookIsbn())
                    .orElseThrow(() -> new RuntimeException("Book not found with ISBN: " + itemRequest.getBookIsbn()));
            if (book.getStock() < itemRequest.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock for book ISBN: " + book.getIsbn());
            }
            book.setStock(book.getStock() - itemRequest.getQuantity());
            bookRepository.save(book);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId()); // 確定した注文IDを使用
            orderItem.setBookIsbn(itemRequest.getBookIsbn());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setCreatedAt(LocalDateTime.now());
            orderItem.setUpdatedAt(LocalDateTime.now());
            orderItemRepository.save(orderItem);
            return orderItem;
        }).collect(Collectors.toList());
        order.setOrderItems(orderItems);

        return OrderDto.from(order);
    }



    @Transactional
    public void deleteOrder(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + id));

        if (!order.getStatus().equals(Order.OrderStatus.PENDING.name())) {
            throw new OrderCancellationException("Order with status " + order.getStatus() + " cannot be cancelled.");
        }

        // 在庫を元に戻す
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
        for (OrderItem item : orderItems) {
            Book book = bookRepository.findById(item.getBookIsbn())
                    .orElseThrow(() -> new RuntimeException("Book not found with ISBN: " + item.getBookIsbn()));
            book.setStock(book.getStock() + item.getQuantity());
            bookRepository.save(book);
        }

        orderItemRepository.deleteByOrderId(id);
        orderRepository.deleteById(id);
    }
}
