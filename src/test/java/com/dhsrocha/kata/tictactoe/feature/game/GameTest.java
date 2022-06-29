package com.dhsrocha.kata.tictactoe.feature.game;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dhsrocha.kata.tictactoe.helper.RandomStubExtension;
import com.dhsrocha.kata.tictactoe.feature.player.PlayerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test suite for {@link Game}.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@DisplayName("Test suite for Game class.")
public final class GameTest implements RandomStubExtension {

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

  public static Game validStub() {
    return Game.builder()
        .home(PlayerTest.validStub())
        .away(PlayerTest.validStub())
        .stage(RandomStubExtension.randomOf(Game.Stage.class))
        .build();
  }
}
