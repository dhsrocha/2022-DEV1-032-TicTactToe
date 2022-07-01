package com.dhsrocha.kata.tictactoe.feature.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dhsrocha.kata.tictactoe.feature.player.PlayerTest;
import com.dhsrocha.kata.tictactoe.feature.turn.Bitboard;
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

  @Test
  @DisplayName(
      "GIVEN valid game "
          + "AND bitboard with blank state "
          + "WHEN resulting "
          + "THEN game has no winner "
          + "AND stage is in progress "
          + "AND returned instance is the same.")
  void givenGame_andBlankBitboard_whenResulting_thenGameHasNoWinner_andStageIsInProgress() {
    // Arrange
    final var game =
        validStub().toBuilder().stage(Game.Stage.IN_PROGRESS).type(Type.TIC_TAC_TOE).build();
    // Act
    final var finished = game.resultFrom(Bitboard.of(0));
    // Assert
    assertNull(finished.getWinner());
    assertEquals(Game.Stage.IN_PROGRESS, finished.getStage());
    assertEquals(game, finished);
  }

  @Test
  @DisplayName(
      "GIVEN valid game "
          + "AND bitboard with home state "
          + "WHEN resulting "
          + "THEN game is home winner "
          + "AND stage is finished "
          + "AND returned instance is the same.")
  void givenGame_andHomeBitboard_whenResulting_thenGameHasHomeAsWinner_andStageIsFinished() {
    // Arrange
    final var game =
        validStub().toBuilder()
            .away(PlayerTest.validStub())
            .stage(Game.Stage.IN_PROGRESS)
            .type(Type.TIC_TAC_TOE)
            .build();
    // Act
    final var finished = game.resultFrom(Bitboard.of(0b100010001010001010));
    // Assert
    assertEquals(game.getHome(), game.getWinner());
    assertEquals(Game.Stage.FINISHED, game.getStage());
    assertEquals(game, finished);
  }

  @Test
  @DisplayName(
      "GIVEN valid game "
          + "AND bitboard with away state "
          + "WHEN resulting "
          + "THEN game away winner "
          + "AND stage is finished "
          + "AND returned instance is the same.")
  void givenGame_andAwayBitboard_whenResulting_thenGameHasAwayAsWinner_andStageIsFinished() {
    // Arrange
    final var game =
        validStub().toBuilder()
            .away(PlayerTest.validStub())
            .stage(Game.Stage.IN_PROGRESS)
            .type(Type.TIC_TAC_TOE)
            .build();
    // Act
    final var finished = game.resultFrom(Bitboard.of(0b010001010100010001));
    // Assert
    assertEquals(game.getAway(), game.getWinner());
    assertEquals(Game.Stage.FINISHED, game.getStage());
    assertEquals(game, finished);
  }

  @Test
  @DisplayName(
      "GIVEN valid game "
          + "AND bitboard with tie state "
          + "WHEN resulting "
          + "THEN game has no winner "
          + "AND stage is finished "
          + "AND returned instance is the same.")
  void givenGame_andTieBitboard_whenResulting_thenGameHasNoWinner_andStageIsFinished() {
    // Arrange
    final var game =
        validStub().toBuilder()
            .stage(Game.Stage.IN_PROGRESS)
            .away(PlayerTest.validStub())
            .type(Type.TIC_TAC_TOE)
            .build();
    // Act
    final var finished = game.resultFrom(Bitboard.of(0b010011100101100011));
    // Assert
    assertNull(game.getWinner());
    assertEquals(Game.Stage.FINISHED, game.getStage());
    assertEquals(game, finished);
  }

  public static Game validStub() {
    return Game.builder()
        .type(RandomStubExtension.randomOf(Type.class))
        .stage(RandomStubExtension.randomOf(Game.Stage.class))
        .home(PlayerTest.validStub())
        .build();
  }
}
