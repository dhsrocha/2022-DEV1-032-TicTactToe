package com.dhsrocha.kata.tictactoe.feature.action;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dhsrocha.kata.tictactoe.feature.RandomStubExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test suite for {@link Bitboard}.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@DisplayName("Test suite for Bitboard class.")
final class BitboardTest implements RandomStubExtension {

  @Test
  @DisplayName(
      "GIVEN stub with valid state " //
          + "WHEN validating " //
          + "THEN not constraint violation is generated.")
  void validState() {
    // Arrange
    final var stub = validStub();
    // Act
    final var violations = VALIDATOR.getValidator().validate(stub);
    // Assert
    assertTrue(violations.isEmpty());
  }

  static Bitboard validStub() {
    return Bitboard.builder().state((byte) FAKER.number().randomDigit()).build();
  }
}
