package com.example.demo.domain.exception;

public class BookDeletionException extends RuntimeException {
  public BookDeletionException(String message) {
    super(message);
  }
}
