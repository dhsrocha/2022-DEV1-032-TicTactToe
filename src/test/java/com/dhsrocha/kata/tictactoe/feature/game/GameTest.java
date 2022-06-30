package com.dhsrocha.kata.tictactoe.feature.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dhsrocha.kata.tictactoe.feature.player.PlayerTest;
import com.dhsrocha.kata.tictactoe.helper.RandomStubExtension;
import com.dhsrocha.kata.tictactoe.system.ExceptionCode;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

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

  @Test
  @DisplayName(
      "GIVEN a valid game "
          + "AND two players enrolled to it "
          + "WHEN finishing it with one of the enrolled as the last round player "
          + "THEN this one is awarded as winner "
          + "AND the game is finished.")
  void givenGame_andTwoEnrolled_whenFinishingWithOne_thenThisOneIsWinner() {
    // Arrange
    final var stub =
        validStub().toBuilder().stage(Game.Stage.IN_PROGRESS).away(PlayerTest.validStub()).build();
    final var toAward = Stream.of(stub.getHome(), stub.getAway()).findAny().orElseThrow();
    // Act
    var game = stub.finish(toAward);
    // Assert
    assertEquals(game.getWinner(), game.getWinner());
    assertEquals(Game.Stage.FINISHED, game.getStage());
  }

  @Test
  @DisplayName(
      "GIVEN a valid game "
          + "AND two players NOT enrolled to it "
          + "WHEN finishing it with one of the enrolled as the last round player "
          + "THEN exception is thrown.")
  void givenGame_andTwoNotEnrolled_whenFinishing_throwPLAYER_NOT_IN_GAME() {
    // Arrange
    final var stub =
        validStub().toBuilder().stage(Game.Stage.IN_PROGRESS).away(PlayerTest.validStub()).build();
    // Act - Assert
    final var ex =
        assertThrows(HttpClientErrorException.class, () -> stub.finish(PlayerTest.validStub()));
    // Assert
    assertEquals(
        ex.getMessage(),
        HttpStatus.CONFLICT.value() + " " + ExceptionCode.PLAYER_NOT_IN_GAME.name());
  }

  @Test
  @DisplayName(
      "GIVEN a valid game "
          + "AND two players "
          + "AND not in IN_PROGRESS "
          + "WHEN finishing it with one of the enrolled as the last round player "
          + "THEN exception is thrown.")
  void givenGame_andTwoPLayers_whenFinishing_throwGAME_NOT_IN_PROGRESS() {
    // Arrange
    final var stub =
        validStub().toBuilder().stage(Game.Stage.AWAITS).away(PlayerTest.validStub()).build();
    // Act
    final var ex = assertThrows(HttpClientErrorException.class, () -> stub.finish(stub.getHome()));
    // Assert
    assertEquals(
        ex.getMessage(),
        HttpStatus.CONFLICT.value() + " " + ExceptionCode.GAME_NOT_IN_PROGRESS.name());
  }

  public static Game validStub() {
    return Game.builder()
        .type(RandomStubExtension.randomOf(Type.class))
        .stage(RandomStubExtension.randomOf(Game.Stage.class))
        .home(PlayerTest.validStub())
        .build();
  }
}
