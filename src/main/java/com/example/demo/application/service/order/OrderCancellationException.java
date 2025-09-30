package com.example.demo.application.service.order;

public class OrderCancellationException extends RuntimeException {
  public OrderCancellationException(String message) {
    super(message);
  }
}
