package com.dhsrocha.kata.tictactoe.feature.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dhsrocha.kata.tictactoe.helper.RandomStubExtension;
import com.dhsrocha.kata.tictactoe.system.ExceptionCode;
import com.dhsrocha.kata.tictactoe.vo.Bitboard;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Test suite for {@link Type#TIC_TAC_TOE}.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
@DisplayName("Test suite for Bitboard#TIC_TAC_TOE class.")
final class TicTacToeTest implements RandomStubExtension {

  @ParameterizedTest
  @MethodSource("validStubsAndResults")
  @DisplayName(
      "GIVEN valid bitboard "
          + "WHEN calculating a bitboard with tic-tac-toe rule set "
          + "THEN return expected result accordingly.")
  void expectedResult(final int state, final Bitboard.Result expected) {
    // Arrange
    final var rule = Type.TIC_TAC_TOE;
    final var valid = Bitboard.of(state);
    // Act
    final var actual = rule.resultOf(Bitboard.of(state).processWith(valid, rule));
    // Assert
    assertEquals(expected, actual);
  }

  @ParameterizedTest
  @MethodSource("invalidStubsAndResults")
  @DisplayName(
      "GIVEN invalid bitboard "
          + "WHEN calculating a bitboard with tic-tac-toe rule set "
          + "THEN IllegalArgumentException is thrown.")
  void invalidStub(final int invalidStub, final ExceptionCode code) {
    // Arrange
    final var rule = Type.TIC_TAC_TOE;
    final var invalid = Bitboard.of(invalidStub);
    // Act
    final var ex =
        assertThrows(HttpClientErrorException.class, () -> invalid.processWith(invalid, rule));
    // Assert
    assertEquals(HttpStatus.BAD_REQUEST.value() + " " + code.name(), ex.getMessage());
  }

  @Test
  @DisplayName(
      "GIVEN a valid bitboard "
          + "AND another valid bitboard with more two bits than the previous one "
          + "WHEN calculating a bitboard with tic-tac-toe rule set "
          + "THEN ExceptionCode#BITBOARD_EXCESSIVE_BITS_PER_ROUND is thrown.")
  void invalidStub_betweenLastAndCurrent() {
    // Arrange
    final var rule = Type.TIC_TAC_TOE;
    final var code = ExceptionCode.BITBOARD_EXCESSIVE_BITS_PER_ROUND.name();
    final var last = Bitboard.of(0b0__000_000_001);
    final var current = Bitboard.of(0b0__100_100_100);
    // Act
    final var ex =
        assertThrows(HttpClientErrorException.class, () -> current.processWith(last, rule));
    // Assert
    assertEquals(HttpStatus.BAD_REQUEST.value() + " " + code, ex.getMessage());
  }

  private static Stream<Arguments> validStubsAndResults() {
    return Stream.of(
        // Home
        Arguments.of(0b0__100_010_001__010_001_010, Bitboard.Result.HOME),
        Arguments.of(0b0__010_010_010__000_100_100, Bitboard.Result.HOME),
        Arguments.of(0b0__111_100_010__000_011_101, Bitboard.Result.HOME), // All filled up
        // Away
        Arguments.of(0b0__010_001_010__100_010_001, Bitboard.Result.AWAY),
        Arguments.of(0b0__001_001_000__010_010_010, Bitboard.Result.AWAY),
        Arguments.of(0b0__000_011_101__111_100_010, Bitboard.Result.AWAY), // All filled up
        // Tie
        Arguments.of(0b0__010_011_100__101_100_011, Bitboard.Result.TIE),
        Arguments.of(0b0__001_110_010__110_001_101, Bitboard.Result.TIE),
        Arguments.of(0b0__101_010_010__010_101_101, Bitboard.Result.TIE),
        // Not over
        Arguments.of(0b0__000_000_011__000_000_100, Bitboard.Result.NOT_OVER),
        Arguments.of(0b0__000_000_010__000_110_100, Bitboard.Result.NOT_OVER),
        Arguments.of(0b0__000_000_011__000_110_100, Bitboard.Result.NOT_OVER),
        Arguments.of(0b0__001_010_010__000_100_101, Bitboard.Result.NOT_OVER),
        Arguments.of(0b0__001_011_010__000_100_101, Bitboard.Result.NOT_OVER),
        Arguments.of(0b0__010_110_001__100_001_110, Bitboard.Result.NOT_OVER));
  }

  private static Stream<Arguments> invalidStubsAndResults() {
    return Stream.of(
        Arguments.of(0b0__000_000_000__000_000_000, ExceptionCode.BITBOARD_UNSET_STATE),
        Arguments.of(0b0__111_111_111__100_000_000, ExceptionCode.BITBOARD_EXCESSIVE_BITS),
        Arguments.of(0b0__100_000_000__100_000_000, ExceptionCode.BITBOARD_PIECE_IN_SAME_TILE));
  }
}
