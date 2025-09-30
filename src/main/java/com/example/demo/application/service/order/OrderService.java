package com.example.demo.application.service.order;

import com.example.demo.application.dto.order.OrderDto;
import com.example.demo.domain.exception.OrderNotFoundException;
import com.example.demo.domain.model.book.Book;
import com.example.demo.domain.model.order.Order;
import com.example.demo.domain.repository.order.OrderItemRepository;
import com.example.demo.domain.repository.order.OrderRepository;
import com.example.demo.domain.service.BookDomainService;
import com.example.demo.presentation.request.order.CreateOrderRequest;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;
  private final BookDomainService bookDomainService;

  public List<OrderDto> findAllOrders() {
    List<Order> orders = orderRepository.findAll();
    return orders.stream()
        .map(
            order -> {
              order.setOrderItems(orderItemRepository.findByOrderId(order.getId()));
              return OrderDto.from(order);
            })
        .collect(Collectors.toList());
  }

  public OrderDto findOrderById(UUID id) {
    Order order =
        orderRepository
            .findById(id)
            .orElseThrow(() -> new OrderNotFoundException("注文が見つかりません。ID: " + id));
    order.setOrderItems(orderItemRepository.findByOrderId(order.getId()));
    return OrderDto.from(order);
  }

  @Transactional
  public OrderDto createOrder(CreateOrderRequest request) {
    Order order = Order.create(request.getCustomerId());
    orderRepository.insert(order);

    request
        .getOrderItems()
        .forEach(
            itemRequest -> {
              Book book =
                  bookDomainService.validateAndDecreaseStock(
                      itemRequest.getBookIsbn(), itemRequest.getQuantity());

              order.addOrderItem(book.getIsbn(), itemRequest.getQuantity());
              orderItemRepository.save(order.getOrderItems().get(order.getOrderItems().size() - 1));
            });

    return OrderDto.from(order);
  }

  @Transactional
  public void deleteOrder(UUID id) {
    Order order =
        orderRepository
            .findById(id)
            .orElseThrow(() -> new OrderNotFoundException("注文が見つかりません。ID: " + id));
    order.setOrderItems(orderItemRepository.findByOrderId(order.getId()));

    order.validateCancellation();

    // 在庫を元に戻す
    order
        .getOrderItems()
        .forEach(
            item -> {
              bookDomainService.validateAndIncreaseStock(item.getBookIsbn(), item.getQuantity());
            });

    orderItemRepository.deleteByOrderId(id);
    orderRepository.deleteById(id);
  }
}
