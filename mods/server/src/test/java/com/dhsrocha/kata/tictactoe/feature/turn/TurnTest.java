package com.dhsrocha.kata.tictactoe.feature.turn;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dhsrocha.kata.tictactoe.feature.game.GameTest;
import com.dhsrocha.kata.tictactoe.feature.player.PlayerTest;
import com.dhsrocha.kata.tictactoe.helper.RandomStubExtension;
import com.dhsrocha.kata.tictactoe.vo.BitboardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test suite for {@link Turn} class.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@DisplayName("Test suite for '" + Turn.TAG + "' domain.")
public final class TurnTest implements RandomStubExtension {

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

  public static Turn validStub() {
    final var game = GameTest.validStub();
    final var last =
        Turn.builder().game(game).player(PlayerTest.validStub()).state(BitboardTest.validStub());
    return Turn.builder()
        .last(last.build())
        .player(PlayerTest.validStub())
        .game(game)
        .state(BitboardTest.validStub())
        .build();
  }
}
