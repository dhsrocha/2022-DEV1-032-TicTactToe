package com.dhsrocha.kata.tictactoe.feature.player;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dhsrocha.kata.tictactoe.feature.player.Player.Gender;
import com.dhsrocha.kata.tictactoe.helper.RandomStubExtension;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test suite for {@link Player}.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@DisplayName("Test suite for '" + Player.TAG + "' domain.")
public final class PlayerTest implements RandomStubExtension {

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

  @ParameterizedTest
  @MethodSource("invalidStubs")
  @DisplayName(
      "GIVEN stub with invalid state " //
          + "WHEN validating " //
          + "THEN constraint violation is generated.")
  void invalidStates(final Player invalidStub) {
    // Act
    final var violations = VALIDATOR.getValidator().validate(invalidStub);
    // Assert
    assertFalse(violations.isEmpty());
  }

  public static Player validStub() {
    return Player.builder()
        .active(FAKER.bool().bool())
        .username(FAKER.regexify("[a-z][a-z_]{19}"))
        .gender(RandomStubExtension.randomOf(Gender.class))
        .firstName(FAKER.regexify("[A-Z][a-z]{19}"))
        .lastName(FAKER.regexify("[A-Z][a-z]{19}"))
        .email(FAKER.internet().emailAddress())
        .birthDate(OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS).minusSeconds(1))
        .build();
  }

  private static Stream<Player> invalidStubs() {
    return Stream.of(
        validStub().toBuilder().username("t").build(),
        validStub().toBuilder().username("test2").build(),
        validStub().toBuilder().username("ttttttttttttttttttttttttttttttttttttttttttt").build(),
        validStub().toBuilder().username("test2_test2").build());
  }
}
