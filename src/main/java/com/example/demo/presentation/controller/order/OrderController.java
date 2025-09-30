package com.example.demo.presentation.controller.order;

import com.example.demo.application.dto.order.OrderDto;
import com.example.demo.application.service.order.OrderService;
import com.example.demo.presentation.request.order.CreateOrderRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;

  @GetMapping
  public List<OrderDto> getAllOrders() {
    return orderService.findAllOrders();
  }

  @GetMapping("/{id}")
  public ResponseEntity<OrderDto> getOrderById(@PathVariable UUID id) {
    OrderDto orderDto = orderService.findOrderById(id);
    return ResponseEntity.ok(orderDto);
  }

  @PostMapping
  public ResponseEntity<?> createOrder(@Valid @RequestBody CreateOrderRequest request) {
    try {
      OrderDto createdOrder = orderService.createOrder(request);
      URI location =
          ServletUriComponentsBuilder.fromCurrentRequest()
              .path("/{id}")
              .buildAndExpand(createdOrder.getId())
              .toUri();
      return ResponseEntity.created(location).body(createdOrder);
    } catch (com.example.demo.domain.exception.InsufficientStockException e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteOrder(@PathVariable UUID id) {
    OrderDto order = orderService.findOrderById(id);
    if ("SHIPPED".equals(order.getStatus())) {
      return ResponseEntity.badRequest()
          .body(Map.of("error", "この操作は注文のステータスが PENDING の場合のみ可能です。現在のステータス: SHIPPED"));
    }
    orderService.deleteOrder(id);
    return ResponseEntity.noContent().build();
  }
}
