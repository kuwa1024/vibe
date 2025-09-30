package com.example.demo.presentation.exception;

import com.example.demo.application.service.book.BookDeletionException;
import com.example.demo.application.service.order.InsufficientStockException;
import com.example.demo.application.service.order.OrderCancellationException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getAllErrors()
        .forEach(
            (error) -> {
              String fieldName = ((FieldError) error).getField();
              String errorMessage = error.getDefaultMessage();
              errors.put(fieldName, errorMessage);
            });
    return errors;
  }

  @ExceptionHandler(BookDeletionException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, String> handleBookDeletionException(BookDeletionException ex) {
    Map<String, String> error = new HashMap<>();
    error.put("error", ex.getMessage());
    return error;
  }

  @ExceptionHandler(InsufficientStockException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, String> handleInsufficientStockException(InsufficientStockException ex) {
    Map<String, String> error = new HashMap<>();
    error.put("error", ex.getMessage());
    return error;
  }

  @ExceptionHandler(OrderCancellationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, String> handleOrderCancellationException(OrderCancellationException ex) {
    Map<String, String> error = new HashMap<>();
    error.put("error", ex.getMessage());
    return error;
  }

  @ExceptionHandler(RuntimeException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public Map<String, String> handleRuntimeException(RuntimeException ex) {
    Map<String, String> error = new HashMap<>();
    error.put("error", ex.getMessage());
    return error;
  }
}
