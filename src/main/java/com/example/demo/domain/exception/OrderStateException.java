package com.example.demo.domain.exception;

public class OrderStateException extends RuntimeException {
  public OrderStateException(String message) {
    super(message);
  }
}
