package com.dhsrocha.kata.tictactoe.system;

import org.springdoc.api.ErrorMessage;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
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

  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  @ExceptionHandler(javax.validation.ConstraintViolationException.class)
  ErrorMessage javaxValidation(javax.validation.ConstraintViolationException ex) {
    return new ErrorMessage(ex.getMessage());
  }

  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  @ExceptionHandler({
    DataIntegrityViolationException.class,
    org.hibernate.exception.ConstraintViolationException.class
  })
  ErrorMessage persistenceConstraintViolation(RuntimeException ex) {
    return new ErrorMessage(ex.getMessage());
  }

  @ExceptionHandler(HttpClientErrorException.class)
  ResponseEntity<ErrorMessage> httpClientError(HttpClientErrorException ex) {
    final var error = new ErrorMessage(ex.getStatusText());
    return ResponseEntity.status(ex.getStatusCode()).body(error);
  }
}
