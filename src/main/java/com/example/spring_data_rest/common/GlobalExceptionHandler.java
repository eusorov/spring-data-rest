package com.example.spring_data_rest.common;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.example.spring_data_rest.backtest.exception.BacktestNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BacktestNotFoundException.class)
	public ResponseEntity<Map<String, Object>> handleBacktestNotFound(
		BacktestNotFoundException ex,
		WebRequest request
	) {
		Map<String, Object> body = createBody(HttpStatus.NOT_FOUND, ex.getMessage(), request);
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
	}

	private Map<String, Object> createBody(HttpStatus status, String message, WebRequest request) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", OffsetDateTime.now());
		body.put("status", status.value());
		body.put("error", status.getReasonPhrase());
		body.put("message", message);
		body.put("path", request.getDescription(false));
		return body;
	}
}

