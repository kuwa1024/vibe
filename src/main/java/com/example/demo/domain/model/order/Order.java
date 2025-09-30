package com.example.demo.domain.model.order;

import com.example.demo.domain.exception.OrderStateException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Order {
  private UUID id;
  private UUID customerId;
  private LocalDateTime orderDatetime;
  private String status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private List<OrderItem> orderItems;

  public static Order create(UUID customerId) {
    Order order = new Order();
    order.setId(UUID.randomUUID());
    order.setCustomerId(customerId);
    order.setOrderItems(new ArrayList<>());
    order.setStatus(OrderStatus.PENDING.name());
    order.setOrderDatetime(LocalDateTime.now());

    LocalDateTime now = LocalDateTime.now();
    order.setCreatedAt(now);
    order.setUpdatedAt(now);

    return order;
  }

  public void addOrderItem(String bookIsbn, int quantity) {
    validateOrderStatus(OrderStatus.PENDING);

    OrderItem orderItem = new OrderItem();
    orderItem.setOrderId(this.id);
    orderItem.setBookIsbn(bookIsbn);
    orderItem.setQuantity(quantity);

    LocalDateTime now = LocalDateTime.now();
    orderItem.setCreatedAt(now);
    orderItem.setUpdatedAt(now);

    if (this.orderItems == null) {
      this.orderItems = new ArrayList<>();
    }
    this.orderItems.add(orderItem);
    this.setUpdatedAt(now);
  }

  public void cancel() {
    validateOrderStatus(OrderStatus.PENDING);
    this.status = OrderStatus.CANCELLED.name();
    this.setUpdatedAt(LocalDateTime.now());
  }

  public void ship() {
    validateOrderStatus(OrderStatus.PENDING);
    this.status = OrderStatus.SHIPPED.name();
    this.setUpdatedAt(LocalDateTime.now());
  }

  public void validateCancellation() {
    validateOrderStatus(OrderStatus.PENDING);
  }

  private void validateOrderStatus(OrderStatus expectedStatus) {
    if (!this.status.equals(expectedStatus.name())) {
      throw new OrderStateException(
          String.format(
              "この操作は注文のステータスが %s の場合のみ可能です。現在のステータス: %s", expectedStatus.name(), this.status));
    }
  }

  public List<OrderItem> getOrderItems() {
    return Collections.unmodifiableList(orderItems);
  }

  public enum OrderStatus {
    PENDING,
    SHIPPED,
    CANCELLED
  }
}
