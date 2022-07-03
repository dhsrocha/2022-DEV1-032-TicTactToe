package com.dhsrocha.kata.tictactoe.feature.game;

import com.dhsrocha.kata.tictactoe.helper.RandomStubExtension;
import com.dhsrocha.kata.tictactoe.vo.Bitboard;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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
  void ticTacToeBitboard(final int bitboard, final Bitboard.Result expected) {
    // Act
    final var actual = Type.TIC_TAC_TOE.process(bitboard);
    // Assert
    Assertions.assertEquals(expected, actual);
  }

  @ParameterizedTest
  @MethodSource("invalidStubsAndResults")
  @DisplayName(
      "GIVEN invalid bitboard "
          + "WHEN calculating a bitboard with tic-tac-toe rule set "
          + "THEN IllegalArgumentException is thrown.")
  void ticTacToeInvalidBitboard(final int invalidStub) {
    // Act - Assert
    Assertions.assertThrows(
        IllegalArgumentException.class, () -> Type.TIC_TAC_TOE.process(invalidStub));
  }

  private static Stream<Arguments> validStubsAndResults() {
    return Stream.of(
        // Home
        Arguments.of(0b100_010_001__010_001_010, Bitboard.Result.HOME),
        Arguments.of(0b010_010_010__000_100_100, Bitboard.Result.HOME),
        Arguments.of(0b111_000_010__000_011_001, Bitboard.Result.HOME),
        // Away
        Arguments.of(0b010_001_010__100_010_001, Bitboard.Result.AWAY),
        Arguments.of(0b001_001_000__010_010_010, Bitboard.Result.AWAY),
        Arguments.of(0b000_011_001__111_000_010, Bitboard.Result.AWAY),
        // Tie
        Arguments.of(0b010_011_100__101_100_011, Bitboard.Result.TIE),
        Arguments.of(0b001_110_010__110_001_101, Bitboard.Result.TIE),
        Arguments.of(0b101_010_010__010_101_101, Bitboard.Result.TIE),
        // Not over
        Arguments.of(0b000_000_011__000_000_001, Bitboard.Result.NOT_OVER),
        Arguments.of(0b001_001_010__000_100_101, Bitboard.Result.NOT_OVER),
        Arguments.of(0b000_110_001__000_000_110, Bitboard.Result.NOT_OVER));
  }

  private static Stream<Integer> invalidStubsAndResults() {
    return Stream.of(
        FAKER.number().numberBetween(Integer.MIN_VALUE, -1), Integer.parseInt("1".repeat(10), 2));
  }
}
