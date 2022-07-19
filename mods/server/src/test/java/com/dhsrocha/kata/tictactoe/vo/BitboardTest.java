package com.dhsrocha.kata.tictactoe.vo;

import com.dhsrocha.kata.tictactoe.helper.RandomStubExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test suite for {@link Bitboard}.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@DisplayName("Test suite for Bitboard class.")
public final class BitboardTest implements RandomStubExtension {

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
    Assertions.assertTrue(violations.isEmpty());
  }

  @Test
  @DisplayName(
      "GIVEN stub with invalid state " //
          + "WHEN validating " //
          + "THEN constraint violation is generated.")
  void invalidStates() {
    // Arrange
    final var invalidStub = Bitboard.of(FAKER.number().numberBetween(Integer.MIN_VALUE, -1));
    // Act
    final var violations = VALIDATOR.getValidator().validate(invalidStub);
    // Assert
    Assertions.assertFalse(violations.isEmpty());
  }

  public static Bitboard validStub() {
    final var base3 = Integer.toString(FAKER.number().randomDigit(), 3);
    final var nineChars = base3.length() > 9 ? base3.substring(base3.length() - 9) : base3;
    return Bitboard.of(Integer.parseInt(nineChars));
  }
}
