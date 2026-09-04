package com.suministrosnorte.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException exception) {
        int status = exception.getStatusCode().value();
        Map<String, Object> body = Map.of(
                "status", status,
                "error", exception.getStatusCode().toString(),
                "message", exception.getReason() == null ? "Solicitud no válida" : exception.getReason());
        return ResponseEntity.status(status).body(body);
    }
}