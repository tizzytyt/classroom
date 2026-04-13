package com.edu.classroom.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException e) {
    Map<String, Object> body = new HashMap<>();
    body.put("message", e.getMessage() == null ? "error" : e.getMessage());
    return ResponseEntity.badRequest().body(body);
  }
}

