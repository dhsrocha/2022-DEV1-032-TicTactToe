package com.dhsrocha.kata.tictactoe.system;

import org.springdoc.api.ErrorMessage;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Standard HTTP status mapped to application, builtin and third-party exceptions.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@SuppressWarnings("unused")
@RestControllerAdvice
class ControllerAdvice extends ResponseEntityExceptionHandler {

  @ExceptionHandler(javax.validation.ConstraintViolationException.class)
  ResponseEntity<ErrorMessage> javaxValidation(javax.validation.ConstraintViolationException ex) {
    final var error = new ErrorMessage(ex.getMessage());
    return ResponseEntity.status(422).body(error);
  }

  @ExceptionHandler({
    DataIntegrityViolationException.class,
    org.hibernate.exception.ConstraintViolationException.class
  })
  ResponseEntity<ErrorMessage> persistenceConstraintViolation(RuntimeException ex) {
    final var error = new ErrorMessage(ex.getMessage());
    return ResponseEntity.status(422).body(error);
  }

  @ExceptionHandler(HttpClientErrorException.class)
  ResponseEntity<ErrorMessage> httpClientError(HttpClientErrorException ex) {
    final var error = new ErrorMessage(ex.getStatusText());
    return ResponseEntity.status(ex.getStatusCode()).body(error);
  }
}
